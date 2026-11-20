package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.GameMode
import com.example.ui.theme.LocalWoodPalette
import com.example.ui.theme.OutfitFontFamily

@Composable
fun WoodenToolStrip(
    canUndo: Boolean,
    isNotesMode: Boolean,
    onUndo: () -> Unit,
    onErase: () -> Unit,
    onToggleNotes: () -> Unit,
    onFillCandidates: () -> Unit,
    onHint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WoodenToolButton(
            icon = Icons.AutoMirrored.Filled.Undo,
            label = stringResource(R.string.action_undo),
            enabled = canUndo,
            isActive = false,
            testTag = "tool_undo",
            onClick = onUndo,
            modifier = Modifier.weight(1f)
        )
        WoodenToolButton(
            icon = Icons.Filled.Backspace,
            label = stringResource(R.string.action_erase),
            enabled = true,
            isActive = false,
            testTag = "tool_erase",
            onClick = onErase,
            modifier = Modifier.weight(1f)
        )
        WoodenToolButton(
            icon = Icons.Filled.Edit,
            label = stringResource(R.string.action_notes),
            enabled = true,
            isActive = isNotesMode,
            testTag = "tool_notes",
            onClick = onToggleNotes,
            modifier = Modifier.weight(1f)
        )
        WoodenToolButton(
            icon = Icons.Filled.AutoFixHigh,
            label = stringResource(R.string.action_candidates),
            enabled = true,
            isActive = false,
            testTag = "tool_candidates",
            onClick = onFillCandidates,
            modifier = Modifier.weight(1f)
        )
        WoodenToolButton(
            icon = Icons.Filled.Lightbulb,
            label = stringResource(R.string.action_hint),
            enabled = true,
            isActive = false,
            testTag = "tool_hint",
            onClick = onHint,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WoodenToolButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    isActive: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWoodPalette.current
    val shape = RoundedCornerShape(12.dp)
    val bgTop by animateColorAsState(
        targetValue = if (isActive) palette.accentPrimary else palette.keypadTileTop,
        label = "tool_bg_top"
    )
    val bgBottom by animateColorAsState(
        targetValue = if (isActive) palette.boardFrameOuter else palette.keypadTileBottom,
        label = "tool_bg_bottom"
    )
    val fgColor = if (isActive) palette.onAccentPrimary else palette.textPrimary

    Column(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .heightIn(min = 52.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .shadow(2.dp, shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) palette.accentBrass else palette.cardBorder.copy(alpha = 0.65f),
                shape = shape
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .testTag(testTag)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = fgColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = fgColor,
            maxLines = 1
        )
    }
}

@Composable
fun WoodenNumberPad(
    mode: GameMode,
    remainingCounts: Map<Int, Int>,
    isNotesMode: Boolean,
    onDigitClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWoodPalette.current
    val n = mode.gridSize
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (n == 6) 8.dp else 5.dp)
    ) {
        for (digit in 1..n) {
            val remaining = remainingCounts[digit] ?: n
            val isCompleted = remaining <= 0 && !isNotesMode

            Column(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .heightIn(min = 58.dp)
                    .alpha(if (isCompleted) 0.38f else 1f)
                    .shadow(elevation = if (isCompleted) 0.dp else 3.dp, shape = shape)
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(palette.keypadTileTop, palette.keypadTileBottom)
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (isNotesMode) palette.accentBrass else palette.cardBorder.copy(alpha = 0.75f),
                        shape = shape
                    )
                    .drawBehind {
                        drawLine(
                            color = palette.cardHighlightTop,
                            start = Offset(6f, 1.5f),
                            end = Offset(size.width - 6f, 1.5f),
                            strokeWidth = 2f
                        )
                    }
                    .clickable(
                        enabled = !isCompleted,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = palette.accentBrass)
                    ) { onDigitClick(digit) }
                    .testTag("num_key_$digit")
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = digit.toString(),
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (n == 6) 24.sp else 21.sp,
                    color = if (isNotesMode) palette.accentPrimary else palette.givenDigitColor
                )
                Text(
                    text = if (remaining > 0) remaining.toString() else "✓",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = palette.textSecondary
                )
            }
        }
    }
}
