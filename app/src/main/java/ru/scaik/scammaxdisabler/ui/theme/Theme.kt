package ru.scaik.scammaxdisabler.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NeomorphicDarkColorScheme =
        darkColorScheme(
                primary = CrimsonMedium,
                onPrimary = TextPrimaryDark,
                primaryContainer = CrimsonDark,
                onPrimaryContainer = TextPrimaryDark,
                secondary = SkyBlueMedium,
                onSecondary = TextPrimaryDark,
                secondaryContainer = SkyBlueDark,
                onSecondaryContainer = TextPrimaryDark,
                tertiary = SkyBlueAccent,
                onTertiary = TextPrimaryDark,
                tertiaryContainer = SkyBlueDark,
                onTertiaryContainer = TextPrimaryDark,
                error = ErrorRed,
                errorContainer = ErrorRed.copy(alpha = 0.2f),
                onError = TextPrimaryDark,
                onErrorContainer = TextPrimaryDark,
                background = CharcoalMedium,
                onBackground = TextPrimaryDark,
                surface = CharcoalLight,
                onSurface = TextPrimaryDark,
                surfaceVariant = CharcoalSoft,
                onSurfaceVariant = TextSecondaryDark,
                outline = DividerDark,
                outlineVariant = DividerDark.copy(alpha = 0.5f),
                inverseSurface = NeumorphicLightSurface,
                inverseOnSurface = TextPrimaryLight,
                inversePrimary = SkyBlueMedium,
                surfaceTint = CrimsonAccent
        )

private val NeomorphicLightColorScheme =
        lightColorScheme(
                primary = SkyBlueMedium,
                onPrimary = Color.White,
                primaryContainer = SkyBlueLight.copy(alpha = 0.3f),
                onPrimaryContainer = TextPrimaryLight,
                secondary = CrimsonMedium,
                onSecondary = Color.White,
                secondaryContainer = CrimsonBright.copy(alpha = 0.2f),
                onSecondaryContainer = TextPrimaryLight,
                tertiary = SkyBlueAccent,
                onTertiary = Color.White,
                tertiaryContainer = SkyBlueAccent.copy(alpha = 0.2f),
                onTertiaryContainer = TextPrimaryLight,
                error = ErrorRed,
                errorContainer = ErrorRed.copy(alpha = 0.1f),
                onError = Color.White,
                onErrorContainer = ErrorRed,
                background = NeumorphicLightBackground,
                onBackground = TextPrimaryLight,
                surface = NeumorphicLightSurface,
                onSurface = TextPrimaryLight,
                surfaceVariant = NeumorphicLightSurface.copy(alpha = 0.8f),
                onSurfaceVariant = TextSecondaryLight,
                outline = DividerLight,
                outlineVariant = DividerLight.copy(alpha = 0.5f),
                inverseSurface = CharcoalMedium,
                inverseOnSurface = TextPrimaryDark,
                inversePrimary = CrimsonMedium,
                surfaceTint = SkyBlueAccent
        )

@Composable
fun ScamMaxDisablerTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = false,
        isBlockerActive: Boolean = false,
        content: @Composable () -> Unit
) {
    val colorScheme =
            when {
                darkTheme -> NeomorphicDarkColorScheme
                else -> NeomorphicLightColorScheme
            }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            WindowCompat.setDecorFitsSystemWindows(window, false)

            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT

            WindowCompat.getInsetsController(window, view).apply {
                // When blocker is active, use dark status bars for better visibility
                // against the dark red gradient background
                isAppearanceLightStatusBars = if (isBlockerActive) false else !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
