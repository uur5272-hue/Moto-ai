package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val MotoAiColorScheme =
  darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepSpaceBackground,
    primaryContainer = LaserBlueDark,
    onPrimaryContainer = NeonCyan,
    secondary = ElectricViolet,
    onSecondary = DeepSpaceBackground,
    secondaryContainer = MetallicSurfaceVariant,
    onSecondaryContainer = ElectricViolet,
    tertiary = CyberNeonGreen,
    onTertiary = DeepSpaceBackground,
    background = DeepSpaceBackground,
    onBackground = TextPrimary,
    surface = DarkMetallicSurface,
    onSurface = TextPrimary,
    surfaceVariant = MetallicSurfaceVariant,
    onSurfaceVariant = TextSecondary,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = MotoAiColorScheme,
    typography = Typography,
    content = content
  )
}
