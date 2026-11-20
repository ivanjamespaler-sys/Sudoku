package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalWoodPalette
import com.example.ui.theme.WoodPalette
import kotlin.math.sin

fun Modifier.woodGrainBackground(palette: WoodPalette): Modifier = drawBehind {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(palette.canvasBgTop, palette.canvasBgBottom)
        )
    )
    // Organic longitudinal wood grain curves
    val grainCount = 18
    val stepX = size.width / grainCount
    for (i in 0..grainCount) {
        val baseX = i * stepX
        val path = Path()
        path.moveTo(baseX, 0f)
        val segments = 10
        val segH = size.height / segments
        for (s in 1..segments) {
            val y = s * segH
            val wave = sin((s * 0.75f + i * 0.9f).toDouble()).toFloat() * (stepX * 0.32f)
            val prevY = (s - 1) * segH
            val ctrlY = (prevY + y) / 2f
            path.quadraticTo(baseX + wave, ctrlY, baseX + wave * 0.6f, y)
        }
        drawPath(
            path = path,
            color = palette.woodGrainStroke,
            style = Stroke(width = if (i % 3 == 0) 2.2f else 1.1f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun WoodCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    useAltTone: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalWoodPalette.current
    val shape = RoundedCornerShape(cornerRadius)
    val bgTop = if (useAltTone) palette.cardSurfaceAlt else palette.cardSurface
    val bgBottom = if (useAltTone) palette.cardSurface else palette.cardSurfaceAlt.copy(alpha = 0.7f)

    Surface(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = shape, ambientColor = palette.cardShadowBottom, spotColor = palette.cardShadowBottom)
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
            ),
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.2.dp, palette.cardBorder.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
                .drawBehind {
                    // Top inner bevel sheen
                    drawLine(
                        color = palette.cardHighlightTop,
                        start = Offset(12f, 1.5f),
                        end = Offset(size.width - 12f, 1.5f),
                        strokeWidth = 2f
                    )
                }
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun WoodPlaqueBadge(
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalWoodPalette.current
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (accent) {
                    Brush.verticalGradient(listOf(palette.accentPrimary, palette.boardFrameOuter))
                } else {
                    Brush.verticalGradient(listOf(palette.keypadTileTop, palette.keypadTileBottom))
                }
            )
            .border(1.dp, palette.cardBorder.copy(alpha = 0.7f), shape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        content = content
    )
}

@Composable
fun JoineryEmblemArtwork(modifier: Modifier = Modifier) {
    val palette = LocalWoodPalette.current
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cellW = w / 3f
        val cellH = h / 3f

        // Wood tile backing
        drawRoundRect(
            brush = Brush.linearGradient(listOf(palette.keypadTileTop, palette.keypadTileBottom)),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
        )

        // Subtle marquetry checkerboard
        for (r in 0..2) {
            for (c in 0..2) {
                if ((r + c) % 2 == 1) {
                    drawRect(
                        color = palette.cardSurfaceAlt.copy(alpha = 0.55f),
                        topLeft = Offset(c * cellW, r * cellH),
                        size = androidx.compose.ui.geometry.Size(cellW, cellH)
                    )
                }
            }
        }

        // Brass center highlight cell
        drawRect(
            color = palette.accentBrass.copy(alpha = 0.35f),
            topLeft = Offset(cellW, cellH),
            size = androidx.compose.ui.geometry.Size(cellW, cellH)
        )

        // Joinery grid lines
        for (i in 1..2) {
            drawLine(
                color = palette.gridJoineryLine,
                start = Offset(i * cellW, 4f),
                end = Offset(i * cellW, h - 4f),
                strokeWidth = 3f
            )
            drawLine(
                color = palette.gridJoineryLine,
                start = Offset(4f, i * cellH),
                end = Offset(w - 4f, i * cellH),
                strokeWidth = 3f
            )
        }

        // Outer border
        drawRoundRect(
            color = palette.boardFrameOuter,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
            style = Stroke(width = 4f)
        )
    }
}
