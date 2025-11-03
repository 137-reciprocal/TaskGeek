package com.taskhero.domain.backup

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Google Drive backup operations.
 */
interface DriveBackupRepository {
    /**
     * Backup database to Google Drive.
     *
     * @return Result containing the file ID on success, or error on failure
     */
    suspend fun backupToGoogleDrive(): Result<String>

    /**
     * Restore database from Google Drive.
     *
     * @param fileId The ID of the backup file to restore from
     * @return Result indicating success or failure
     */
    suspend fun restoreFromGoogleDrive(fileId: String): Result<Unit>

    /**
     * Get the timestamp of the last backup.
     *
     * @return Flow emitting the last backup timestamp in milliseconds, or null if no backup exists
     */
    fun getLastBackupTime(): Flow<Long?>
}
