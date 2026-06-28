package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Modular high-radius shapes to match "Elegant Dark" aesthetics
val ElegantShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(24.dp),  // Typical card radius (matches rounded-[1.5rem]/rounded-[2rem] in theme HTML)
    large = RoundedCornerShape(32.dp),   // Hero card radius
    extraLarge = RoundedCornerShape(40.dp)
)

private val DarkColorScheme =
  darkColorScheme(
    primary = FitForgePrimaryDark,
    secondary = FitForgeSecondaryDark,
    tertiary = FitForgeTertiaryDark,
    background = FitForgeBackgroundDark,
    surface = FitForgeSurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = FitForgeCardDark,
    onSurfaceVariant = TextWhite
  )

private val LightColorScheme =
  lightColorScheme(
    primary = FitForgePrimaryLight,
    secondary = FitForgeSecondaryLight,
    tertiary = FitForgeTertiaryLight,
    background = FitForgeBackgroundLight,
    surface = FitForgeSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF070A13),
    onSurface = Color(0xFF070A13),
    surfaceVariant = FitForgeCardLight,
    onSurfaceVariant = Color(0xFF070A13)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to show premium FitForge theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = ElegantShapes,
    content = content
  )
}

