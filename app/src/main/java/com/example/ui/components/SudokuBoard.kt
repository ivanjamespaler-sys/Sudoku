package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.domain.model.GameMode
import com.example.domain.model.SudokuCell
import com.example.ui.theme.LocalWoodPalette
import com.example.ui.theme.OutfitFontFamily

@Composable
fun SudokuBoard(
    mode: GameMode,
    cells: List<SudokuCell>,
    selectedRow: Int,
    selectedCol: Int,
    highlightSameDigits: Boolean,
    celebratingIndices: Set<Int>,
    hintHighlightCell: Pair<Int, Int>?,
    isPaused: Boolean,
    onCellSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalWoodPalette.current
    val n = mode.gridSize
    val selectedCell = if (selectedRow in 0 until n && selectedCol in 0 until n && cells.size == n * n) {
        cells[selectedRow * n + selectedCol]
    } else null
    val selectedDigit = selectedCell?.value ?: 0

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val boardSize = min(maxWidth, maxHeight).coerceAtMost(540.dp)
        val outerFrameShape = RoundedCornerShape(18.dp)
        val innerGridShape = RoundedCornerShape(10.dp)

        // Mitered hardwood outer tray
        Box(
            modifier = Modifier
                .size(boardSize)
                .widthIn(max = 540.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = outerFrameShape,
                    ambientColor = palette.cardShadowBottom,
                    spotColor = palette.cardShadowBottom
                )
                .clip(outerFrameShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(palette.boardFrameInner, palette.boardFrameOuter)
                    )
                )
                .border(1.5.dp, palette.cardHighlightTop.copy(alpha = 0.45f), outerFrameShape)
                .padding(10.dp)
        ) {
            // Subtle brass corner pins on the mitered frame
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pinOffset = 4f
                val pinRadius = 3.2f
                val corners = listOf(
                    Offset(pinOffset, pinOffset),
                    Offset(size.width - pinOffset, pinOffset),
                    Offset(pinOffset, size.height - pinOffset),
                    Offset(size.width - pinOffset, size.height - pinOffset)
                )
                corners.forEach { pt ->
                    drawCircle(color = palette.accentBrass, radius = pinRadius, center = pt)
                }
            }

            // Inner Sudoku grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(innerGridShape)
                    .background(palette.gridJoineryLine)
                    .border(2.dp, palette.gridJoineryLine, innerGridShape)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until n) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            for (c in 0 until n) {
                                val index = r * n + c
                                val cell = cells.getOrNull(index) ?: SudokuCell(r, c)
                                val isSelected = r == selectedRow && c == selectedCol
                                val isSameBox = selectedRow >= 0 && selectedCol >= 0 &&
                                    (r / mode.boxRows == selectedRow / mode.boxRows) &&
                                    (c / mode.boxCols == selectedCol / mode.boxCols)
                                val isPeer = selectedRow >= 0 && selectedCol >= 0 &&
                                    (r == selectedRow || c == selectedCol || isSameBox)
                                val isSameDigit = highlightSameDigits && selectedDigit != 0 &&
                                    cell.value == selectedDigit && !isSelected
                                val isCelebrating = index in celebratingIndices
                                val isHintTarget = hintHighlightCell?.first == r && hintHighlightCell.second == c

                                val boxIndex = (r / mode.boxRows) * (n / mode.boxCols) + (c / mode.boxCols)
                                val baseMarquetry = if ((boxIndex % 2) == 0) palette.cellBaseLight else palette.cellBaseWarm

                                val targetBg = when {
                                    isPaused -> baseMarquetry
                                    isCelebrating -> palette.shimmerGold.copy(alpha = 0.72f)
                                    cell.isConflict -> palette.cellErrorTint
                                    isSelected -> palette.cellSelectedTile
                                    isHintTarget -> palette.accentBrass.copy(alpha = 0.55f)
                                    isSameDigit -> palette.cellSameDigitHighlight
                                    isPeer -> palette.cellPeerHighlight
                                    cell.isGiven -> palette.cellGivenTile
                                    else -> baseMarquetry
                                }

                                val animatedBg by animateColorAsState(
                                    targetValue = targetBg,
                                    animationSpec = if (isCelebrating) {
                                        spring(stiffness = Spring.StiffnessMediumLow)
                                    } else {
                                        tween(durationMillis = 150)
                                    },
                                    label = "cell_bg"
                                )

                                // Joinery border thicknesses (thicker between boxes)
                                val rightPadding = if (c == n - 1) 0.dp else if ((c + 1) % mode.boxCols == 0) 2.2.dp else 0.7.dp
                                val bottomPadding = if (r == n - 1) 0.dp else if ((r + 1) % mode.boxRows == 0) 2.2.dp else 0.7.dp

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(end = rightPadding, bottom = bottomPadding)
                                        .background(animatedBg)
                                        .drawBehind {
                                            // Subtle bevel on pre-filled branded wood tiles
                                            if (cell.isGiven && !isPaused) {
                                                drawLine(
                                                    color = palette.cardHighlightTop.copy(alpha = 0.55f),
                                                    start = Offset(0f, 1f),
                                                    end = Offset(size.width, 1f),
                                                    strokeWidth = 1.4f
                                                )
                                            }
                                            if (isSelected && !isPaused) {
                                                drawRect(
                                                    color = palette.accentPrimary,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
                                                )
                                            }
                                        }
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true, color = palette.accentBrass)
                                        ) { onCellSelected(r, c) }
                                        .testTag("cell_${r}_${c}")
                                        .semantics {
                                            contentDescription = "Row ${r + 1} Column ${c + 1} " +
                                                if (cell.value != 0) "value ${cell.value}" else "empty"
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!isPaused) {
                                        if (cell.value != 0) {
                                            val digitColor = when {
                                                cell.isConflict -> palette.errorDigitColor
                                                isSelected -> palette.onAccentPrimary
                                                cell.isGiven -> palette.givenDigitColor
                                                else -> palette.playerDigitColor
                                            }
                                            val fontSize = if (n == 6) 24.sp else 20.sp
                                            Text(
                                                text = cell.value.toString(),
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.SemiBold,
                                                fontSize = fontSize,
                                                color = digitColor,
                                                textAlign = TextAlign.Center
                                            )
                                        } else if (cell.notes.isNotEmpty()) {
                                            CellNotesGrid(
                                                mode = mode,
                                                notes = cell.notes,
                                                noteColor = if (isSelected) palette.onAccentPrimary.copy(alpha = 0.9f) else palette.notesDigitColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CellNotesGrid(
    mode: GameMode,
    notes: Set<Int>,
    noteColor: Color
) {
    val rows = if (mode == GameMode.JOINERY_6X6) 2 else 3
    val cols = 3
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (nr in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (nc in 0 until cols) {
                    val digit = nr * cols + nc + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (digit in notes) {
                            Text(
                                text = digit.toString(),
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = if (mode == GameMode.JOINERY_6X6) 11.sp else 9.sp,
                                lineHeight = 10.sp,
                                color = noteColor
                            )
                        }
                    }
                }
            }
        }
    }
}
