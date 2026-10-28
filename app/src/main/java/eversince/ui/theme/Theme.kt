package com.eversince.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(primary = RosePrimary, secondary = RoseSecondary, background = DeepDark, surface = CardBg, onBackground = TextWhite, onSurface = TextWhite)

@Composable
fun EverSinceTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
