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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.UserPreferences
import com.example.domain.model.WoodTheme
import com.example.ui.components.WoodCard
import com.example.ui.theme.LocalWoodPalette
import com.example.ui.theme.paletteFor

private data class TechniqueTip(val title: String, val body: String)

private val joineryTechniques = listOf(
    TechniqueTip(
        title = "1. Naked Singles",
        body = "Examine an empty wooden cell and check its row, column, and block. If eight of the nine digits (or five of six in Joinery 6×6) already appear among its peers, the remaining digit is locked in."
    ),
    TechniqueTip(
        title = "2. Hidden Singles",
        body = "Pick a digit and scan each block, row, or column. Even if a cell has several candidates, if a digit can only fit in one spot inside that unit, carve it there immediately."
    ),
    TechniqueTip(
        title = "3. Chisel Pencil Marks",
        body = "Toggle the Chisel button to sketch candidate digits inside cells. Auto-Clean automatically removes outdated marks when you inlay a matching number."
    ),
    TechniqueTip(
        title = "4. Joinery 6×6 Quick Mode",
        body = "Uses digits 1–6 arranged in 2×3 rectangular blocks—perfect for brisk 2-minute sessions while practicing block-line scanning."
    )
)

@Composable
fun GuideScreen(
    preferences: UserPreferences,
    onSelectWoodTheme: (WoodTheme) -> Unit,
    onSetZenMode: (Boolean) -> Unit,
    onSetAutoCleanNotes: (Boolean) -> Unit,
    onSetHighlightSameDigits: (Boolean) -> Unit,
    onSetHapticsEnabled: (Boolean) -> Unit,
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
            Column(modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.craft_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.textPrimary
                )
                Text(
                    text = stringResource(R.string.craft_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary
                )
            }
        }

        // Wood Species Gallery
        item {
            Text(
                text = stringResource(R.string.timber_gallery_title),
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
            )
        }

        items(WoodTheme.entries) { theme ->
            val themePal = paletteFor(theme)
            val selected = theme == preferences.woodTheme
            WoodCard(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .testTag("gallery_theme_${theme.name}"),
                useAltTone = selected,
                onClick = { onSelectWoodTheme(theme) },
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(themePal.boardFrameInner, themePal.boardFrameOuter)
                                )
                            )
                            .border(1.5.dp, themePal.accentBrass, RoundedCornerShape(12.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = theme.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = theme.originNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary
                        )
                    }
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Selected",
                            tint = palette.accentPrimary
                        )
                    }
                }
            }
        }

        // Bench Preferences
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.preferences_title),
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
            )
        }

        item {
            WoodCard(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PreferenceToggleRow(
                        title = stringResource(R.string.pref_zen_title),
                        subtitle = stringResource(R.string.pref_zen_desc),
                        checked = preferences.zenMode,
                        testTag = "pref_zen_switch",
                        onCheckedChange = onSetZenMode
                    )
                    PreferenceToggleRow(
                        title = stringResource(R.string.pref_autoclean_title),
                        subtitle = stringResource(R.string.pref_autoclean_desc),
                        checked = preferences.autoCleanNotes,
                        testTag = "pref_autoclean_switch",
                        onCheckedChange = onSetAutoCleanNotes
                    )
                    PreferenceToggleRow(
                        title = stringResource(R.string.pref_highlight_title),
                        subtitle = stringResource(R.string.pref_highlight_desc),
                        checked = preferences.highlightSameDigits,
                        testTag = "pref_highlight_switch",
                        onCheckedChange = onSetHighlightSameDigits
                    )
                    PreferenceToggleRow(
                        title = stringResource(R.string.pref_haptics_title),
                        subtitle = stringResource(R.string.pref_haptics_desc),
                        checked = preferences.hapticsEnabled,
                        testTag = "pref_haptics_switch",
                        onCheckedChange = onSetHapticsEnabled
                    )
                }
            }
        }

        // Techniques Guide
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.techniques_title),
                style = MaterialTheme.typography.titleMedium,
                color = palette.textPrimary,
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth()
            )
        }

        items(joineryTechniques) { tip ->
            WoodCard(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.accentPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tip.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textPrimary
                )
            }
        }
    }
}

@Composable
private fun PreferenceToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    val palette = LocalWoodPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = palette.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = palette.onAccentPrimary,
                checkedTrackColor = palette.accentPrimary,
                uncheckedThumbColor = palette.textSecondary,
                uncheckedTrackColor = palette.cardSurfaceAlt
            )
        )
    }
}
