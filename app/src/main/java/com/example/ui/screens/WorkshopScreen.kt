package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.Difficulty
import com.example.domain.model.GameMode
import com.example.domain.model.ResumePreview
import com.example.domain.model.UserPreferences
import com.example.domain.model.WoodTheme
import com.example.ui.components.JoineryEmblemArtwork
import com.example.ui.components.WoodCard
import com.example.ui.components.WoodPlaqueBadge
import com.example.ui.theme.LocalWoodPalette
import com.example.ui.theme.paletteFor
import com.example.ui.viewmodel.formatDuration

@Composable
fun WorkshopScreen(
    preferences: UserPreferences,
    selectedMode: GameMode,
    resumePreview: ResumePreview?,
    dailySolvedToday: Boolean,
    dailyStreak: Int,
    onSelectWoodTheme: (WoodTheme) -> Unit,
    onSelectMode: (GameMode) -> Unit,
    onToggleZenMode: (Boolean) -> Unit,
    onResumeGame: (String) -> Unit,
    onStartDaily: () -> Unit,
    onStartNewGame: (GameMode, Difficulty) -> Unit,
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
        // Hero Workshop Header
        item {
            WoodCard(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                useAltTone = true,
                contentPadding = PaddingValues(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    JoineryEmblemArtwork(modifier = Modifier.size(64.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = stringResource(R.string.workshop_tagline),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary
                        )
                    }
                }
            }
        }

        // Instant Wood Species Theme Switcher
        item {
            Column(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_wood_grain),
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textSecondary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(WoodTheme.entries) { theme ->
                        val themePal = paletteFor(theme)
                        val isSelected = theme == preferences.woodTheme
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) palette.accentPrimary else palette.cardSurface
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) palette.accentBrass else palette.cardBorder.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelectWoodTheme(theme) }
                                .testTag("theme_chip_${theme.name}")
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(themePal.boardFrameInner, themePal.boardFrameOuter)
                                        )
                                    )
                                    .border(1.dp, themePal.accentBrass, CircleShape)
                            )
                            Text(
                                text = theme.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) palette.onAccentPrimary else palette.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Resume Active Carving
        if (resumePreview != null) {
            item {
                AnimatedVisibility(visible = true) {
                    WoodCard(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                            .testTag("resume_game_card"),
                        onClick = { onResumeGame(resumePreview.gameId) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.resume_carving),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(
                                        R.string.resume_subtitle_format,
                                        resumePreview.mode.label,
                                        resumePreview.difficulty.displayName,
                                        resumePreview.completionPercent
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            WoodPlaqueBadge(accent = true) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = stringResource(R.string.resume_carving),
                                        tint = palette.onAccentPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = formatDuration(resumePreview.elapsedSeconds),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = palette.onAccentPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Timber Challenge Card
        item {
            WoodCard(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .testTag("daily_challenge_card"),
                useAltTone = false
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.accentBrass.copy(alpha = 0.22f))
                                .border(1.dp, palette.accentBrass, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (dailySolvedToday) Icons.Filled.CheckCircle else Icons.Filled.CalendarToday,
                                contentDescription = stringResource(R.string.daily_challenge_title),
                                tint = if (dailySolvedToday) palette.successWoodPine else palette.accentPrimary
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.daily_challenge_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = palette.accentBrass,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (dailySolvedToday) {
                                        stringResource(R.string.daily_challenge_solved, dailyStreak)
                                    } else {
                                        stringResource(R.string.daily_challenge_ready, dailyStreak)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textSecondary
                                )
                            }
                        }
                    }
                    Button(
                        onClick = onStartDaily,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.accentPrimary,
                            contentColor = palette.onAccentPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("daily_challenge_button")
                    ) {
                        Text(
                            text = stringResource(R.string.play_daily),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Board Mode Switcher & Zen Toggle
        item {
            Column(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_mode_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GameMode.entries.forEach { mode ->
                        val active = mode == selectedMode
                        WoodCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mode_${mode.name}"),
                            useAltTone = active,
                            onClick = { onSelectMode(mode) },
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (mode == GameMode.CLASSIC_9X9) Icons.Filled.GridView else Icons.Filled.ViewModule,
                                    contentDescription = mode.label,
                                    tint = if (active) palette.accentPrimary else palette.textSecondary
                                )
                                Column {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary
                                    )
                                    Text(
                                        text = if (mode == GameMode.CLASSIC_9X9) "3×3 Blocks" else "2×3 Quick Joinery",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                FilterChip(
                    selected = preferences.zenMode,
                    onClick = { onToggleZenMode(!preferences.zenMode) },
                    label = {
                        Text(
                            text = stringResource(R.string.zen_mode_chip),
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.SelfImprovement,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.accentPrimary,
                        selectedLabelColor = palette.onAccentPrimary,
                        selectedLeadingIconColor = palette.onAccentPrimary,
                        containerColor = palette.cardSurface,
                        labelColor = palette.textPrimary,
                        iconColor = palette.textSecondary
                    ),
                    modifier = Modifier.testTag("zen_mode_chip")
                )
            }
        }

        // Difficulty Launch Cards
        items(Difficulty.entries) { difficulty ->
            WoodCard(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .testTag("start_${difficulty.name}"),
                onClick = { onStartNewGame(selectedMode, difficulty) },
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = difficulty.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = difficulty.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary
                        )
                    }
                    WoodPlaqueBadge {
                        Text(
                            text = "${difficulty.clueCount(selectedMode)} clues",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textPrimary
                        )
                    }
                }
            }
        }
    }
}
