package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.domain.model.WoodTheme

val LocalWoodPalette = staticCompositionLocalOf { paletteFor(WoodTheme.HONEY_OAK) }

@Composable
fun TimberSudokuTheme(
    woodTheme: WoodTheme = WoodTheme.HONEY_OAK,
    content: @Composable () -> Unit
) {
    val palette = paletteFor(woodTheme)
    CompositionLocalProvider(LocalWoodPalette provides palette) {
        MaterialTheme(
            colorScheme = palette.toMaterialColorScheme(),
            typography = TimberTypography,
            content = content
        )
    }
}
