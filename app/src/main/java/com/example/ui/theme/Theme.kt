package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
  DARK,
  DARKER,
  OLED
}

private val DarkColorScheme =
  darkColorScheme(
    primary = RobotAccentBlue,
    onPrimary = Color.White,
    primaryContainer = RobotElevatedPanel,
    onPrimaryContainer = RobotCyan,
    secondary = RobotCyan,
    onSecondary = RobotBgDark,
    secondaryContainer = RobotPanel,
    onSecondaryContainer = RobotTextPrimary,
    tertiary = RobotWarning,
    background = RobotBgDark,
    onBackground = RobotTextPrimary,
    surface = RobotPanel,
    onSurface = RobotTextPrimary,
    surfaceVariant = RobotElevatedPanel,
    onSurfaceVariant = RobotTextSecondary,
    outline = RobotBorder,
    error = RobotDanger,
    onError = Color.White
  )

private val DarkerColorScheme =
  darkColorScheme(
    primary = RobotAccentBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0D141F),
    onPrimaryContainer = RobotCyan,
    secondary = RobotCyan,
    onSecondary = Color.Black,
    background = Color(0xFF04060A),
    onBackground = RobotTextPrimary,
    surface = Color(0xFF080C14),
    onSurface = RobotTextPrimary,
    surfaceVariant = Color(0xFF0F1724),
    onSurfaceVariant = RobotTextSecondary,
    outline = Color(0xFF1B2433),
    error = RobotDanger,
    onError = Color.White
  )

private val OledColorScheme =
  darkColorScheme(
    primary = RobotAccentBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0A0F1A),
    onPrimaryContainer = RobotCyan,
    secondary = RobotCyan,
    onSecondary = Color.Black,
    background = RobotOledBlack,
    onBackground = RobotTextPrimary,
    surface = RobotOledSurface,
    onSurface = RobotTextPrimary,
    surfaceVariant = Color(0xFF0A0E18),
    onSurfaceVariant = RobotTextSecondary,
    outline = Color(0xFF162030),
    error = RobotDanger,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  themeMode: AppThemeMode = AppThemeMode.DARK,
  content: @Composable () -> Unit,
) {
  val colorScheme = when (themeMode) {
    AppThemeMode.DARK -> DarkColorScheme
    AppThemeMode.DARKER -> DarkerColorScheme
    AppThemeMode.OLED -> OledColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
