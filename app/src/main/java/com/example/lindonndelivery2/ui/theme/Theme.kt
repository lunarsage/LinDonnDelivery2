package com.example.lindonndelivery2.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = RustOrange,
    onPrimary = White,
    primaryContainer = RustOrangeDark,
    onPrimaryContainer = White,
    secondary = Grey700,
    onSecondary = White,
    secondaryContainer = Grey900,
    onSecondaryContainer = White,
    background = Grey900,
    onBackground = White,
    surface = Grey900,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = RustOrange,
    onPrimary = White,
    primaryContainer = RustOrangeLight,
    onPrimaryContainer = Black,
    secondary = Grey700,
    onSecondary = White,
    secondaryContainer = Grey100,
    onSecondaryContainer = Grey900,
    background = White,
    onBackground = Grey900,
    surface = White,
    onSurface = Grey900
)

@Composable
fun LinDonnDelivery2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}