package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.WorkshopStats
import com.example.ui.components.WoodCard
import com.example.ui.components.WoodPlaqueBadge
import com.example.ui.theme.LocalWoodPalette
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.viewmodel.formatDuration

@Composable
fun StatsScreen(
    stats: WorkshopStats,
    modifier: Modifier = Modifier
) {
    val palette = LocalWoodPalette.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.ledger_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.textPrimary
                )
                Text(
                    text = stringResource(R.string.ledger_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary
                )
            }
        }

        // 2x2 Summary KPI Grid
        item {
            Column(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatKpiCard(
                        icon = Icons.Filled.EmojiEvents,
                        label = stringResource(R.string.stat_solved),
                        value = stats.totalSolved.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatKpiCard(
                        icon = Icons.Filled.LocalFireDepartment,
                        label = stringResource(R.string.stat_streak),
                        value = "${stats.currentDailyStreak}d",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatKpiCard(
                        icon = Icons.Filled.Timer,
                        label = stringResource(R.string.stat_best_time),
                        value = stats.bestClassicSeconds?.let { formatDuration(it) } ?: "--:--",
                        modifier = Modifier.weight(1f)
                    )
                    val cleanPct = if (stats.totalSolved > 0) {
                        "${(stats.cleanSolves * 100) / stats.totalSolved}%"
                    } else "--"
                    StatKpiCard(
                        icon = Icons.Filled.Verified,
                        label = stringResource(R.string.stat_accuracy),
                        value = cleanPct,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (stats.totalSolved == 0) {
            item {
                WoodCard(
                    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                    useAltTone = true,
                    contentPadding = PaddingValues(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = palette.accentBrass,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = stringResource(R.string.empty_ledger_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.empty_ledger_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Breakdown by Difficulty
            item {
                WoodCard(
                    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Text(
                        text = stringResource(R.string.difficulty_breakdown),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val maxSolved = (stats.difficultyBreakdown.maxOfOrNull { it.solvedCount } ?: 1).coerceAtLeast(1)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        stats.difficultyBreakdown.forEach { item ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.difficulty.displayName} (${item.solvedCount})",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = palette.textPrimary
                                    )
                                    Text(
                                        text = item.bestSeconds?.let { "Best ${formatDuration(it)}" } ?: "Not carved yet",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = palette.textSecondary
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { item.solvedCount.toFloat() / maxSolved.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = palette.accentPrimary,
                                    trackColor = palette.cardSurfaceAlt
                                )
                            }
                        }
                    }
                }
            }

            // Recent Carvings Header
            item {
                Text(
                    text = stringResource(R.string.recent_carvings),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
                )
            }

            items(stats.recentCarvings, key = { it.id }) { carving ->
                WoodCard(
                    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (carving.isDailyChallenge) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(palette.accentBrass.copy(alpha = 0.2f))
                                        .border(1.dp, palette.accentBrass, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CalendarToday,
                                        contentDescription = "Daily Challenge",
                                        tint = palette.accentPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (carving.isDailyChallenge) {
                                        "Daily Challenge • ${carving.difficulty.displayName}"
                                    } else {
                                        "${carving.difficulty.displayName} • ${carving.mode.label}"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = "${carving.mistakes} mistakes • ${carving.hintsUsed} hints",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textSecondary
                                )
                            }
                        }
                        WoodPlaqueBadge {
                            Text(
                                text = formatDuration(carving.elapsedSeconds),
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatKpiCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalWoodPalette.current
    WoodCard(
        modifier = modifier,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.accentBrass.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = palette.accentPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary
                )
            }
        }
    }
}
