package dev.tidesapp.wearos.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val TidesColorPalette = Colors(
    primary = TidesCyan,
    primaryVariant = TidesCyanDark,
    secondary = TidesCyanFill,
    secondaryVariant = TidesCyanDarker,
    error = TidesError,
    onPrimary = TidesBlack,
    onSecondary = TidesBlack,
    onError = TidesWhite,
    background = TidesBlack,
    onBackground = TidesWhite,
    surface = TidesGrayDarken40,
    onSurface = TidesWhite,
    onSurfaceVariant = TidesGray,
)

@Composable
fun TidesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = TidesColorPalette,
        typography = TidesTypography,
        content = content
    )
}
