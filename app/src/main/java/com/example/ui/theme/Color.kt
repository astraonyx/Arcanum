package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// COSMIC CELESTIAL DESIGN SYSTEM TOKENS (Arcanum Galaxy Theme)
// =========================================================================

// Deep Space Void & Obsidian Canvas
val CosmicVoid = Color(0xFF08090D)
val CosmicObsidian = Color(0xFF11131C)
val CosmicSurface = Color(0xFF181B28)
val CosmicCardElevated = Color(0xFF1F2333)
val CosmicBorder = Color(0xFF2B3145)
val CosmicBorderHairline = Color(0x33A5B4FC)

// Stellar Nebula & Planetary Accents
val NebulaViolet = Color(0xFF7C3AED)
val NebulaVioletSoft = Color(0xFF8B5CF6)
val NebulaIndigo = Color(0xFF4F46E5)
val StarlightCyan = Color(0xFF06B6D4)
val StarlightSky = Color(0xFF38BDF8)
val CosmicMagenta = Color(0xFFEC4899)
val SolarGold = Color(0xFFF59E0B)
val StellarYellow = Color(0xFFFDE047)
val AuroraEmerald = Color(0xFF10B981)

// Cosmic Text Hierarchy
val CosmicTextPure = Color(0xFFF8FAFC)
val CosmicTextSubtle = Color(0xFFCBD5E1)
val CosmicTextMuted = Color(0xFF94A3B8)
val CosmicTextDark = Color(0xFF08090D)

// Dynamic Gradients
val CosmicGradientNebula = Brush.linearGradient(
    colors = listOf(NebulaViolet, StarlightCyan)
)
val CosmicGradientDragonAura = Brush.linearGradient(
    colors = listOf(NebulaViolet, StarlightSky, CosmicMagenta)
)
val CosmicGradientFolderGold = Brush.linearGradient(
    colors = listOf(StellarYellow, SolarGold)
)
val CosmicGradientDarkCard = Brush.verticalGradient(
    colors = listOf(CosmicCardElevated, CosmicObsidian)
)

// =========================================================================
// BACKWARD-COMPATIBILITY ALIASES
// =========================================================================
val VaultObsidian = CosmicVoid
val VaultCharcoal = CosmicObsidian
val VaultSlate = CosmicSurface
val VaultCream = Color(0xFFF6F7F2)
val VaultPureWhite = CosmicTextPure

val VaultLime = StarlightCyan
val VaultLimeDark = Color(0xFF0891B2)
val VaultMintCard = Color(0xFF162A36)
val VaultMintAccent = StarlightCyan

val VaultAmber = SolarGold
val VaultCoral = CosmicMagenta
val VaultLavender = NebulaVioletSoft
val VaultLavenderDeep = NebulaViolet

val VaultTextDark = CosmicTextDark
val VaultTextMuted = CosmicTextMuted
val VaultTextLight = CosmicTextPure
val VaultTextLightMuted = CosmicTextSubtle
