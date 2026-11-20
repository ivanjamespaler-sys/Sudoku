package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.GameStatus
import com.example.domain.model.UserPreferences
import com.example.ui.components.SudokuBoard
import com.example.ui.components.WoodCard
import com.example.ui.components.WoodPlaqueBadge
import com.example.ui.components.WoodenNumberPad
import com.example.ui.components.WoodenToolStrip
import com.example.ui.theme.LocalWoodPalette
import com.example.ui.viewmodel.GameUiState
import com.example.ui.viewmodel.formatDuration

@Composable
fun GameScreen(
    state: GameUiState,
    preferences: UserPreferences,
    onBack: () -> Unit,
    onSelectCell: (Int, Int) -> Unit,
    onDigitClick: (Int) -> Unit,
    onUndo: () -> Unit,
    onErase: () -> Unit,
    onToggleNotes: () -> Unit,
    onFillCandidates: () -> Unit,
    onHint: () -> Unit,
    onDismissHint: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onContinueZen: () -> Unit,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWoodPalette.current
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(state.hapticTrigger) {
        if (state.hapticTrigger > 0L && preferences.hapticsEnabled) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    LaunchedEffect(state.errorHapticTrigger) {
        if (state.errorHapticTrigger > 0L && preferences.hapticsEnabled) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Workshop HUD Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            onPause()
                            onBack()
                        },
                        modifier = Modifier.testTag("game_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_workshop),
                            tint = palette.textPrimary
                        )
                    }
                    Column {
                        Text(
                            text = if (state.isDailyChallenge) {
                                stringResource(R.string.daily_challenge_title)
                            } else {
                                "${state.difficulty.displayName} • ${state.mode.label}"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        if (preferences.zenMode) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SelfImprovement,
                                    contentDescription = null,
                                    tint = palette.successWoodPine,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = stringResource(R.string.zen_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.successWoodPine
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.mistakes_label,
                                        state.mistakes,
                                        state.maxMistakes
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (state.mistakes > 0) palette.errorDigitColor else palette.textSecondary
                                )
                                for (i in 0 until state.maxMistakes) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (i < state.mistakes) palette.errorDigitColor
                                                else palette.cardBorder.copy(alpha = 0.4f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WoodPlaqueBadge {
                        Text(
                            text = formatDuration(state.elapsedSeconds),
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textPrimary
                        )
                    }
                    IconButton(
                        onClick = {
                            if (state.status == GameStatus.PAUSED) onResume() else onPause()
                        },
                        modifier = Modifier.testTag("game_pause_button")
                    ) {
                        Icon(
                            imageVector = if (state.status == GameStatus.PAUSED) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = stringResource(R.string.action_pause),
                            tint = palette.textPrimary
                        )
                    }
                }
            }

            // Smart Logical Hint Coach Banner
            AnimatedVisibility(
                visible = state.activeHint != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                state.activeHint?.let { hint ->
                    WoodCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("hint_coach_banner"),
                        useAltTone = true,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = palette.accentBrass,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = hint.technique,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.accentPrimary
                                )
                                Text(
                                    text = hint.explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textPrimary
                                )
                            }
                            IconButton(onClick = onDismissHint) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.dismiss_hint),
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Central Wooden Sudoku Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.status == GameStatus.LOADING) {
                    CircularProgressIndicator(color = palette.accentPrimary)
                } else {
                    SudokuBoard(
                        mode = state.mode,
                        cells = state.cells,
                        selectedRow = state.selectedRow,
                        selectedCol = state.selectedCol,
                        highlightSameDigits = preferences.highlightSameDigits,
                        celebratingIndices = state.celebratingIndices,
                        hintHighlightCell = state.activeHint?.let { it.row to it.col },
                        isPaused = state.status == GameStatus.PAUSED,
                        onCellSelected = onSelectCell
                    )
                }
            }

            // Wooden Tool Strip & Number Pad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WoodenToolStrip(
                    canUndo = state.canUndo,
                    isNotesMode = state.isNotesMode,
                    onUndo = onUndo,
                    onErase = onErase,
                    onToggleNotes = onToggleNotes,
                    onFillCandidates = onFillCandidates,
                    onHint = onHint
                )
                WoodenNumberPad(
                    mode = state.mode,
                    remainingCounts = state.remainingCounts,
                    isNotesMode = state.isNotesMode,
                    onDigitClick = onDigitClick
                )
            }
        }

        // Pause Overlay Plaque
        AnimatedVisibility(
            visible = state.status == GameStatus.PAUSED,
            enter = fadeIn() + scaleIn(initialScale = 0.94f),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.boardFrameOuter.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                WoodCard(
                    modifier = Modifier
                        .widthIn(max = 360.dp)
                        .fillMaxWidth(0.88f)
                        .testTag("pause_overlay"),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.paused_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = palette.textPrimary
                        )
                        Text(
                            text = stringResource(R.string.paused_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onResume,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.accentPrimary,
                                contentColor = palette.onAccentPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("resume_from_pause_button")
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_resume))
                        }
                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.back_to_workshop), color = palette.textPrimary)
                        }
                    }
                }
            }
        }

        // Victory Celebration Plaque
        AnimatedVisibility(
            visible = state.status == GameStatus.COMPLETED,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.boardFrameOuter.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                WoodCard(
                    modifier = Modifier
                        .widthIn(max = 380.dp)
                        .fillMaxWidth(0.9f)
                        .testTag("victory_overlay"),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(palette.accentBrass.copy(alpha = 0.25f))
                                .border(1.5.dp, palette.accentBrass, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = palette.accentBrass,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.victory_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = palette.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        if (state.isNewBestTime) {
                            WoodPlaqueBadge(accent = true) {
                                Text(
                                    text = stringResource(R.string.victory_new_best),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.onAccentPrimary
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Time", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                                Text(
                                    formatDuration(state.elapsedSeconds),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.textPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Mistakes", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                                Text(
                                    state.mistakes.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.textPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hints", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                                Text(
                                    state.hintsUsed.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.textPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onPlayAgain,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.accentPrimary,
                                contentColor = palette.onAccentPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("victory_play_again_button")
                        ) {
                            Text(stringResource(R.string.play_again))
                        }
                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.back_to_workshop), color = palette.textPrimary)
                        }
                    }
                }
            }
        }

        // Game Over (3 Mistakes) Plaque
        AnimatedVisibility(
            visible = state.status == GameStatus.GAME_OVER,
            enter = fadeIn() + scaleIn(initialScale = 0.92f),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.boardFrameOuter.copy(alpha = 0.52f)),
                contentAlignment = Alignment.Center
            ) {
                WoodCard(
                    modifier = Modifier
                        .widthIn(max = 380.dp)
                        .fillMaxWidth(0.9f)
                        .testTag("game_over_overlay"),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.game_over_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = palette.errorDigitColor
                        )
                        Text(
                            text = stringResource(R.string.game_over_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onContinueZen,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.accentPrimary,
                                contentColor = palette.onAccentPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("continue_zen_button")
                        ) {
                            Text(stringResource(R.string.continue_zen))
                        }
                        OutlinedButton(
                            onClick = onPlayAgain,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.play_again), color = palette.textPrimary)
                        }
                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.back_to_workshop), color = palette.textSecondary)
                        }
                    }
                }
            }
        }
    }
}
