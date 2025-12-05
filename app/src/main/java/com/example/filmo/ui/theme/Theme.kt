package com.example.filmo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = gold,
            secondary = gold,
            background = darkBackground,
            surface = darkBackground,
            onPrimary = Color.Black,
            onBackground = gold
        )
    } else {
        lightColorScheme(
            primary = Maroon,
            secondary = LightGray,
            background = BackGround,
            surface = BackGround,
            onPrimary = Color.White,
            onBackground = Maroon
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
