package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF003640),
    primaryContainer = Color(0xFF004F5C),
    onPrimaryContainer = Color(0xFFADE8F4),
    secondary = DarkSecondary,
    onSecondary = Color(0xFF003544),
    secondaryContainer = Color(0xFF004D64),
    onSecondaryContainer = Color(0xFFCBE6FF),
    tertiary = DarkTertiary,
    onTertiary = Color(0xFF003733),
    background = DarkBackground,
    onBackground = Color(0xFFE2EBF0),
    surface = DarkSurface,
    onSurface = Color(0xFFE2EBF0),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC4D5E2),
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = OceanBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3EFFF),
    onPrimaryContainer = Color(0xFF001E30),
    secondary = CeruleanSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F4FF),
    onSecondaryContainer = Color(0xFF001F2B),
    tertiary = AquaTertiary,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF14212D),
    surface = LightSurface,
    onSurface = Color(0xFF14212D),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF425565),
    outline = LightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our signature water palette for brand distinction
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
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
        content = content
    )
}

