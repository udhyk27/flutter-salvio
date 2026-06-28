package com.ydh.salvio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SalvioDarkColorScheme = darkColorScheme(
    primary = GitHubBlue,
    secondary = GitHubGreen,
    tertiary = GitHubYellow,
    background = GitHubDark,
    surface = GitHubSurface,
    onPrimary = GitHubText,
    onSecondary = GitHubText,
    onBackground = GitHubText,
    onSurface = GitHubText,
    outline = GitHubBorder,
    error = GitHubRed
)

@Composable
fun SalvioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SalvioDarkColorScheme,
        typography = Typography,
        content = content
    )
}
