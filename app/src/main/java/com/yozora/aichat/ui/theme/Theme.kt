package com.yozora.aichat.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val AppBackground = Color(0xFF090A12)
val AppSurface = Color(0xFF11131D)
val AppSurface2 = Color(0xFF1A1C27)
val AppStroke = Color(0xFF343545)
val AppAccent = Color(0xFF7C5CFF)
val AppAccentSoft = Color(0xFFA98CFF)
val AppAccentDim = Color(0xFF211A3B)
val AppTextPrimary = Color(0xFFF4F2FF)
val AppTextSecondary = Color(0xFFB4B0C8)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = AppAccent,
    onPrimary = Color.White,
    secondary = AppAccentSoft,
    background = AppBackground,
    onBackground = AppTextPrimary,
    surface = AppSurface,
    onSurface = AppTextPrimary,
    surfaceVariant = AppSurface2,
    onSurfaceVariant = AppTextSecondary,
    outline = AppStroke
)

@Composable
fun AIChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AppBackground.toArgb()
            window.navigationBarColor = AppBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content
    )
}
