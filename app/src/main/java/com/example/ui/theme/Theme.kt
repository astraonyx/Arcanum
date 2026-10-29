package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightVaultColorScheme = lightColorScheme(
    primary = VaultObsidian,
    onPrimary = VaultPureWhite,
    primaryContainer = VaultLime,
    onPrimaryContainer = VaultObsidian,
    secondary = VaultMintAccent,
    onSecondary = VaultPureWhite,
    secondaryContainer = VaultMintCard,
    onSecondaryContainer = VaultObsidian,
    tertiary = VaultAmber,
    onTertiary = VaultObsidian,
    tertiaryContainer = VaultLavender,
    onTertiaryContainer = VaultObsidian,
    background = VaultCream,
    onBackground = VaultTextDark,
    surface = VaultPureWhite,
    onSurface = VaultTextDark,
    surfaceVariant = ColorTokens.SurfaceSoft,
    onSurfaceVariant = VaultTextMuted,
    error = VaultCoral,
    onError = VaultPureWhite
)

private val DarkVaultColorScheme = darkColorScheme(
    primary = VaultLime,
    onPrimary = VaultObsidian,
    primaryContainer = VaultCharcoal,
    onPrimaryContainer = VaultLime,
    secondary = VaultMintCard,
    onSecondary = VaultObsidian,
    secondaryContainer = VaultSlate,
    onSecondaryContainer = VaultMintCard,
    tertiary = VaultAmber,
    onTertiary = VaultObsidian,
    background = VaultObsidian,
    onBackground = VaultTextLight,
    surface = VaultCharcoal,
    onSurface = VaultTextLight,
    surfaceVariant = VaultSlate,
    onSurfaceVariant = VaultTextLightMuted,
    error = VaultCoral,
    onError = VaultPureWhite
)

object ColorTokens {
    val SurfaceSoft = androidx.compose.ui.graphics.Color(0xFFECEFE6)
}

val VaultShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkVaultColorScheme else LightVaultColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = VaultShapes,
        content = content
    )
}
