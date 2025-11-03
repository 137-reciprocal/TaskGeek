package com.taskhero.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.taskhero.domain.hero.model.Hero
import com.taskhero.domain.hero.model.StatType
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Glance widget showing hero stats and progress.
 * Displays level, XP progress, streak, and all 6 stats in a grid.
 */
class HeroStatsWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HeroStatsWidgetEntryPoint {
        fun widgetRepository(): WidgetRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Get widget repository through Hilt entry point
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            HeroStatsWidgetEntryPoint::class.java
        )
        val repository = entryPoint.widgetRepository()

        // Fetch hero
        val hero = try {
            repository.getHeroForWidget()
        } catch (e: Exception) {
            null
        }

        provideContent {
            GlanceTheme {
                HeroStatsWidgetContent(hero = hero, context = context)
            }
        }
    }
}

@Composable
private fun HeroStatsWidgetContent(hero: Hero?, context: Context) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .cornerRadius(16.dp)
            .padding(16.dp)
            .clickable(
                onClick = actionStartActivity(
                    Intent(context, Class.forName("com.taskhero.MainActivity")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("navigate_to", "hero")
                    }
                )
            )
    ) {
        if (hero == null) {
            // No hero found
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create your hero!",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                // Header with level and name
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Level badge
                    Box(
                        modifier = GlanceModifier
                            .size(48.dp)
                            .background(Color(0xFF4CAF50))
                            .cornerRadius(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "LVL",
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = hero.level.toString(),
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.width(12.dp))

                    Column {
                        Text(
                            text = hero.displayName,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onSurface
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = hero.currentTitle,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                // XP Progress
                Column {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "XP: ${hero.currentXp} / ${hero.getXpToNextLevel()}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Text(
                            text = "${hero.getXpProgress().toInt()}%",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    // Progress bar
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color(0xFF2C2C2E))
                            .cornerRadius(4.dp)
                    ) {
                        val progressWidth = (hero.getXpProgress() / 100f).coerceIn(0f, 1f)
                        if (progressWidth > 0) {
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxWidth(progressWidth)
                                    .height(8.dp)
                                    .background(Color(0xFF4CAF50))
                                    .cornerRadius(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                // Streak info
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Streak: ${hero.currentStreak} days",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurface
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "Tasks: ${hero.tasksCompleted}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurface
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                // Stats grid (2 columns x 3 rows)
                val stats = listOf(
                    StatType.STRENGTH to hero.strength,
                    StatType.DEXTERITY to hero.dexterity,
                    StatType.CONSTITUTION to hero.constitution,
                    StatType.INTELLIGENCE to hero.intelligence,
                    StatType.WISDOM to hero.wisdom,
                    StatType.CHARISMA to hero.charisma
                )

                Column {
                    stats.chunked(2).forEach { rowStats ->
                        Row(
                            modifier = GlanceModifier.fillMaxWidth()
                        ) {
                            rowStats.forEach { (statType, value) ->
                                StatItem(
                                    statType = statType,
                                    value = value,
                                    modifier = GlanceModifier.defaultWeight()
                                )
                                if (rowStats.indexOf(statType to value) == 0 && rowStats.size > 1) {
                                    Spacer(modifier = GlanceModifier.width(8.dp))
                                }
                            }
                        }
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(statType: StatType, value: Int, modifier: GlanceModifier = GlanceModifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF2C2C2E))
            .cornerRadius(8.dp)
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stat abbreviation
            Text(
                text = getStatAbbreviation(statType),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = getStatColor(statType)
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            // Stat value
            Text(
                text = value.toString(),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }
    }
}

private fun getStatAbbreviation(statType: StatType): String {
    return when (statType) {
        StatType.STRENGTH -> "STR"
        StatType.DEXTERITY -> "DEX"
        StatType.CONSTITUTION -> "CON"
        StatType.INTELLIGENCE -> "INT"
        StatType.WISDOM -> "WIS"
        StatType.CHARISMA -> "CHA"
    }
}

private fun getStatColor(statType: StatType): Color {
    return when (statType) {
        StatType.STRENGTH -> Color(0xFFEF5350)
        StatType.DEXTERITY -> Color(0xFF66BB6A)
        StatType.CONSTITUTION -> Color(0xFFFF7043)
        StatType.INTELLIGENCE -> Color(0xFF42A5F5)
        StatType.WISDOM -> Color(0xFFAB47BC)
        StatType.CHARISMA -> Color(0xFFFFA726)
    }
}
