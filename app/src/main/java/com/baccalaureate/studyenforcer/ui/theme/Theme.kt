package com.baccalaureate.studyenforcer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AccentEmerald,
    onPrimary = SurfaceWhite,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = InkTitanium,
    secondary = AccentGold,
    onSecondary = SurfaceWhite,
    secondaryContainer = GoldLight,
    onSecondaryContainer = InkTitanium,
    tertiary = InkSpaceGray,
    background = CanvasAlabaster,
    onBackground = InkTitanium,
    surface = SurfaceWhite,
    onSurface = InkTitanium,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = InkSpaceGray,
    outline = BorderHairline,
    error = AlertCrimson,
    errorContainer = CrimsonLight,
    onError = SurfaceWhite
)

@Composable
fun StudyEnforcerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Strict Apple-inspired warm alabaster aesthetic
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = CanvasAlabaster.toArgb()
                window.navigationBarColor = CanvasAlabaster.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
