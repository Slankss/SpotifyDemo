package com.okankkl.spotifydemo.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val ColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    background = Background,
    surfaceContainer = Surface,
    onPrimary = PrimaryText,
    onBackground = PrimaryText,
    onSurface = SecondaryText
)

data class DefaultSize(
    val extraSmall: Dp,
    val small: Dp,
    val default: Dp,
    val large: Dp,
    val extraLarge: Dp
)

val LocalIconSize = staticCompositionLocalOf {
    DefaultSize(
        small = 0.dp,
        default = 0.dp,
        large = 0.dp,
        extraSmall = 0.dp,
        extraLarge = 0.dp
    )
}

val LocalPaddingSize = staticCompositionLocalOf {
    DefaultSize(
        small = 0.dp,
        default = 0.dp,
        large = 0.dp,
        extraSmall = 0.dp,
        extraLarge = 0.dp
    )
}

@Composable
fun SpotifyDemoTheme(
    content: @Composable () -> Unit
) {
    val localIconSize = DefaultSize(
        extraSmall = 24.dp,
        small = 30.dp,
        default = 36.dp,
        large = 42.dp,
        extraLarge = 48.dp
    )

    val localPaddingSize = DefaultSize(
        extraSmall = 4.dp,
        small = 8.dp,
        default = 16.dp,
        large = 24.dp,
        extraLarge = 32.dp
    )
    CompositionLocalProvider(
        LocalIconSize provides localIconSize,
        LocalPaddingSize provides localPaddingSize
    ){
        MaterialTheme(
            colorScheme = ColorScheme,
            typography = Typography,
            content = content
        )
    }

}