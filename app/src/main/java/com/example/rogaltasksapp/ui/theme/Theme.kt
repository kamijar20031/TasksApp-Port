package com.example.rogaltasksapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(

    primary = Color(0xFFA67126),
    // 0xFF80571F)
    secondary =  Color(0xff000000),
    tertiary =  Color(0xffff0000),
    background = Color(0xFF101010),
    surface = Color(0xFF0037FF),
    // 0xfffafafa
    onPrimary = Color(0xfffafafa),
    onSecondary = Color(0xfffafafa),
    onTertiary = Color(0xfffafafa),
    onBackground = Color(0xfffafafa),
    onSurface = Color(0xfffafafa),
    surfaceTint = Color(0xFF1D936A),
    inversePrimary = Color(0xFF003FFF),
    primaryContainer = Color(0xFF9D661B),
    onPrimaryContainer = Color(0xfffafafa),
    surfaceVariant = Color(0xFFFF0000),
    error = ErrorCol,
    secondaryContainer = Color(0xFFFF0000),
    onSecondaryContainer = Color.Black,

    tertiaryContainer = Color(0xFF0D07FF),
    onTertiaryContainer = Color.Black,
    /* primary
            onPrimary
            primaryContainer
            onPrimaryContainer

            secondary
            onSecondary
            secondaryContainer
            onSecondaryContainer

            tertiary
            onTertiary
            tertiaryContainer
            onTertiaryContainer

            background
            onBackground

            surface
            onSurface

            surfaceVariant
            onSurfaceVariant

            error
            onError
            errorContainer
            onErrorContainer

            outline
            outlineVariant

            inverseSurface
            inverseOnSurface
            inversePrimary

            surfaceTint
            scrim */
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xffffffff),
    secondary =  Color(0xff000000),
    tertiary =  Color(0xffff0000),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun RogalTasksAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}