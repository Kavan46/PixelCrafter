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

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = PremiumElectricBlue,
  secondary = NeonCyan,
  tertiary = SlateNavy,
  background = MidnightBlack,
  surface = DeepMidnightBlue,
  onPrimary = Color.White,
  onSecondary = MidnightBlack,
  onBackground = IceBlueText,
  onSurface = IceBlueText,
  outline = DeepBlueBorder
)

private val LightColorScheme = darkColorScheme( // Fallback/unified dark blue and black for light mode too
  primary = PremiumElectricBlue,
  secondary = NeonCyan,
  tertiary = SlateNavy,
  background = MidnightBlack,
  surface = DeepMidnightBlue,
  onPrimary = Color.White,
  onSecondary = MidnightBlack,
  onBackground = IceBlueText,
  onSurface = IceBlueText,
  outline = DeepBlueBorder
)

@Composable
fun PixelCrafterTheme(
  themeName: String = "Midnight Cosmic",
  content: @Composable () -> Unit,
) {
  // Use custom dynamic color palettes based on chosen theme
  val colorScheme = when (themeName) {
    "Cyberpunk Neon" -> darkColorScheme(
      primary = Color(0xFFFF007F), // Neon Pink
      secondary = Color(0xFFD946EF), // Neon Fuchsia
      tertiary = Color(0xFF2E1065), // Cyber Void Deep
      background = Color(0xFF0F041C), // Deep cyber dark
      surface = Color(0xFF1E0B36), // Deep Cyber card
      onPrimary = Color.White,
      onSecondary = Color.Black,
      onBackground = Color(0xFFFDF4FF),
      onSurface = Color(0xFFFDF4FF),
      outline = Color(0xFF4A044E)
    )
    "Emerald Minimalist" -> darkColorScheme(
      primary = Color(0xFF10B981), // Emerald Teal
      secondary = Color(0xFF34D399), // Mint Green
      tertiary = Color(0xFF064E3B), // Deep Slate Green
      background = Color(0xFF03160F), // Dark rich green
      surface = Color(0xFF072C1E), // Deep Emerald card
      onPrimary = Color.White,
      onSecondary = Color.Black,
      onBackground = Color(0xFFECFDF5),
      onSurface = Color(0xFFECFDF5),
      outline = Color(0xFF065F46)
    )
    else -> DarkColorScheme // Default Midnight Cosmic
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
