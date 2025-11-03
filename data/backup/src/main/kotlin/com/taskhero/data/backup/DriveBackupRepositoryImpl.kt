package com.taskhero.data.backup

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.taskhero.core.database.TaskHeroDatabase
import com.taskhero.data.backup.model.BackupData
import com.taskhero.domain.backup.DriveBackupRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of DriveBackupRepository using Google Drive API v3.
 */
@Singleton
class DriveBackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: TaskHeroDatabase,
    private val json: Json
) : DriveBackupRepository {

    private val _lastBackupTime = MutableStateFlow<Long?>(null)

    companion object {
        private const val APP_FOLDER_NAME = "TaskHero Backups"
        private const val BACKUP_FILE_NAME = "taskhero_backup.json"
        private const val MIME_TYPE_JSON = "application/json"
    }

    override suspend fun backupToGoogleDrive(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()
                ?: return@withContext Result.failure(Exception("Google Drive not authenticated"))

            // Export database to JSON
            val backupData = exportDatabaseToJson()
            val backupJson = json.encodeToString(backupData)

            // Get or create app folder
            val folderId = getOrCreateAppFolder(driveService)

            // Check if backup file already exists
            val existingFileId = findBackupFile(driveService, folderId)

            val fileId = if (existingFileId != null) {
                // Update existing file
                updateFile(driveService, existingFileId, backupJson)
            } else {
                // Create new file
                createFile(driveService, folderId, backupJson)
            }

            _lastBackupTime.value = System.currentTimeMillis()
            Result.success(fileId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreFromGoogleDrive(fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()
                ?: return@withContext Result.failure(Exception("Google Drive not authenticated"))

            // Download file content
            val outputStream = ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            val backupJson = outputStream.toString("UTF-8")

            // Parse backup data
            val backupData = json.decodeFromString<BackupData>(backupJson)

            // Clear database and restore
            restoreDatabaseFromJson(backupData)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLastBackupTime(): Flow<Long?> = _lastBackupTime

    /**
     * Get authenticated Drive service.
     */
    private fun getDriveService(): Drive? {
        // Get the signed-in Google account
        // This requires GoogleSignIn to be configured in the app
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE)
        )

        // Set the account - this would come from GoogleSignIn
        // For now, we'll return null if no account is set
        // This should be properly implemented with GoogleSignIn
        val accountName = getSignedInAccountEmail() ?: return null
        credential.selectedAccountName = accountName

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("TaskHero")
            .build()
    }

    /**
     * Get the signed-in account email from SharedPreferences.
     * In production, this would integrate with GoogleSignIn.
     */
    private fun getSignedInAccountEmail(): String? {
        val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
        return prefs.getString("google_account_email", null)
    }

    /**
     * Export database to JSON.
     */
    private suspend fun exportDatabaseToJson(): BackupData {
        return BackupData(
            tasks = database.taskDao().getAllTasksForBackup(),
            annotations = database.annotationDao().getAllAnnotationsForBackup(),
            tags = database.tagDao().getAllTagsForBackup(),
            taskTags = database.taskTagDao().getAllTaskTags(),
            taskDependencies = database.taskDependencyCrossRefDao().getAllDependencies(),
            heroes = database.heroDao().getAllHeroes(),
            unlockedTitles = database.unlockedTitleDao().getAllUnlockedTitlesForBackup(),
            xpHistory = database.xpHistoryDao().getAllXpHistoryForBackup(),
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Restore database from JSON.
     */
    private suspend fun restoreDatabaseFromJson(backupData: BackupData) {
        database.runInTransaction {
            // Clear all tables
            database.clearAllTables()

            // Restore data in correct order (to maintain foreign key constraints)
            backupData.tasks.forEach { database.taskDao().insertTask(it) }
            backupData.annotations.forEach { database.annotationDao().insertAnnotation(it) }
            backupData.tags.forEach { database.tagDao().insertTag(it) }
            backupData.taskTags.forEach { database.taskTagDao().insertTaskTag(it) }
            backupData.taskDependencies.forEach { database.taskDependencyCrossRefDao().insertDependency(it) }
            backupData.heroes.forEach { database.heroDao().insertHero(it) }
            backupData.unlockedTitles.forEach { database.unlockedTitleDao().insertUnlockedTitle(it) }
            backupData.xpHistory.forEach { database.xpHistoryDao().insertXpHistory(it) }
        }
    }

    /**
     * Get or create the app folder in Google Drive.
     */
    private fun getOrCreateAppFolder(driveService: Drive): String {
        // Search for existing app folder
        val query = "mimeType='application/vnd.google-apps.folder' and name='$APP_FOLDER_NAME' and trashed=false"
        val result = driveService.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        return if (result.files.isNotEmpty()) {
            result.files[0].id
        } else {
            // Create new folder
            val folderMetadata = File().apply {
                name = APP_FOLDER_NAME
                mimeType = "application/vnd.google-apps.folder"
            }
            driveService.files().create(folderMetadata)
                .setFields("id")
                .execute()
                .id
        }
    }

    /**
     * Find existing backup file in the app folder.
     */
    private fun findBackupFile(driveService: Drive, folderId: String): String? {
        val query = "'$folderId' in parents and name='$BACKUP_FILE_NAME' and trashed=false"
        val result = driveService.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        return result.files.firstOrNull()?.id
    }

    /**
     * Create a new backup file in Google Drive.
     */
    private fun createFile(driveService: Drive, folderId: String, content: String): String {
        val fileMetadata = File().apply {
            name = BACKUP_FILE_NAME
            parents = listOf(folderId)
            mimeType = MIME_TYPE_JSON
        }

        val contentStream = ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))
        val mediaContent = com.google.api.client.http.InputStreamContent(
            MIME_TYPE_JSON,
            contentStream
        )

        return driveService.files().create(fileMetadata, mediaContent)
            .setFields("id")
            .execute()
            .id
    }

    /**
     * Update an existing backup file in Google Drive.
     */
    private fun updateFile(driveService: Drive, fileId: String, content: String): String {
        val contentStream = ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))
        val mediaContent = com.google.api.client.http.InputStreamContent(
            MIME_TYPE_JSON,
            contentStream
        )

        driveService.files().update(fileId, null, mediaContent).execute()
        return fileId
    }
}
