package com.taskhero.feature.hero

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taskhero.core.ui.components.EmptyHeroProfile
import com.taskhero.domain.hero.model.Hero
import com.taskhero.domain.hero.model.Title
import com.taskhero.domain.hero.model.XpHistoryItem
import com.taskhero.feature.hero.components.StatCard
import com.taskhero.feature.hero.components.XpProgressBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main Hero profile screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroScreen(
    viewModel: HeroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HeroEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is HeroEffect.ShowAvatarPicker -> {
                    // TODO: Implement avatar picker
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hero Profile") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is HeroUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is HeroUiState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                is HeroUiState.Success -> {
                    HeroContent(
                        hero = state.hero,
                        titles = state.titles,
                        recentXpHistory = state.recentXpHistory,
                        onUpdateDisplayName = { name ->
                            viewModel.handleIntent(HeroIntent.UpdateDisplayName(name))
                        },
                        onSelectTitle = { titleId ->
                            viewModel.handleIntent(HeroIntent.SelectTitle(titleId))
                        }
                    )
                }
            }
        }
    }
}

/**
 * Content displayed when hero data is loaded successfully.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroContent(
    hero: Hero,
    titles: List<Title>,
    recentXpHistory: List<XpHistoryItem>,
    onUpdateDisplayName: (String) -> Unit,
    onSelectTitle: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Avatar and Name Section
        item {
            HeroHeaderSection(
                hero = hero,
                onEditName = { /* TODO: Show dialog */ }
            )
        }

        // XP Progress Bar
        item {
            XpProgressBar(
                level = hero.level,
                currentXp = hero.currentXp,
                xpToNextLevel = hero.getXpToNextLevel()
            )
        }

        // Stats Section
        item {
            Text(
                text = "Attributes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                StatCard(
                    statName = "STR",
                    statValue = hero.strength,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    statName = "DEX",
                    statValue = hero.dexterity,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    statName = "CON",
                    statValue = hero.constitution,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    statName = "INT",
                    statValue = hero.intelligence,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    statName = "WIS",
                    statValue = hero.wisdom,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    statName = "CHA",
                    statValue = hero.charisma,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Hero Stats Card
        item {
            HeroStatsCard(hero = hero)
        }

        // Unlocked Titles Section
        if (titles.isNotEmpty()) {
            item {
                Text(
                    text = "Unlocked Titles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    titles.forEach { title ->
                        FilterChip(
                            selected = title.name == hero.currentTitle,
                            onClick = { onSelectTitle(title.id) },
                            label = { Text(title.name) }
                        )
                    }
                }
            }
        }

        // Recent XP History Section
        if (recentXpHistory.isNotEmpty()) {
            item {
                Text(
                    text = "Recent XP Gains",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(recentXpHistory) { xpItem ->
                XpHistoryItemCard(xpItem = xpItem)
            }
        }
    }
}

/**
 * Hero header section with avatar and name.
 */
@Composable
private fun HeroHeaderSection(
    hero: Hero,
    onEditName: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hero.avatarUri != null) {
                // TODO: Load image from URI
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Hero Avatar",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Avatar",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Display Name with Edit Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = hero.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onEditName) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Name"
                )
            }
        }

        // Class and Title
        Text(
            text = "${hero.classType} - ${hero.currentTitle}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Card displaying hero statistics.
 */
@Composable
private fun HeroStatsCard(hero: Hero) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Hero Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            StatRow(label = "Tasks Completed", value = hero.tasksCompleted.toString())
            StatRow(label = "Current Streak", value = "${hero.currentStreak} days")
            StatRow(label = "Longest Streak", value = "${hero.longestStreak} days")
            StatRow(label = "Total XP Earned", value = hero.totalXpEarned.toString())
        }
    }
}

/**
 * Row displaying a stat label and value.
 */
@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Card displaying a single XP history item.
 */
@Composable
private fun XpHistoryItemCard(xpItem: XpHistoryItem) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(xpItem.timestamp) {
        dateFormat.format(Date(xpItem.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = xpItem.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "+${xpItem.xpEarned} XP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
