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

// Apple Minimalist Pastel Tokens (DESIGN.md Reference Standards)
val PastelSky = Color(0xFF38BDF8)       // Vibrant Apple Sky
val PastelSkySoft = Color(0xFF7DD3FC)
val PastelLavender = Color(0xFFA855F7)  // Vibrant Apple Purple
val PastelLavenderSoft = Color(0xFFC4B5FD)
val PastelPeach = Color(0xFFF97316)     // Vibrant Apple Coral/Peach
val PastelPeachSoft = Color(0xFFFED7AA)
val PastelMint = Color(0xFF10B981)      // Vibrant Apple Mint/Emerald
val PastelMintSoft = Color(0xFF86EFAC)
val PastelRose = Color(0xFFF43F5E)      // Vibrant Apple Rose/Crimson
val PastelRoseSoft = Color(0xFFFCA5A5)
val PastelYellow = Color(0xFFFBBF24)    // Vibrant Apple Sunshine Gold
val PastelYellowSoft = Color(0xFFFEF08A)

// Glassmorphism & Translucent Aura Tokens
val CosmicGlass = Color(0xCC111827)          // 80% opacity deep space slate
val CosmicGlassSurface = Color(0xD916223D)   // Elevated glass top
val CosmicGlassSurfaceDark = Color(0xB80E1528) // Elevated glass bottom
val CosmicPillDock = Color(0xE60D1322)       // Floating frosted glass dock
val AstrodraconSapphire = Color(0xFF3B82F6)

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
val CosmicGradientGlassCard = Brush.verticalGradient(
    colors = listOf(Color(0xD918233C), Color(0xB80E1528))
)
val CosmicGradientGlassHighlight = Brush.linearGradient(
    colors = listOf(Color(0x6638BDF8), Color(0x33A855F7), Color(0x1AFFFFFF), Color(0x00FFFFFF))
)
val CosmicGradientCta = Brush.horizontalGradient(
    colors = listOf(Color(0xFF06B6D4), Color(0xFF6366F1), Color(0xFFA855F7))
)
val CosmicGradientGoldCta = Brush.horizontalGradient(
    colors = listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
)
val CosmicGradientEmeraldCta = Brush.horizontalGradient(
    colors = listOf(Color(0xFF10B981), Color(0xFF06B6D4))
)
val CosmicGradientRoseCta = Brush.horizontalGradient(
    colors = listOf(Color(0xFFF43F5E), Color(0xFFEC4899))
)
val CosmicNebulaMesh = Brush.verticalGradient(
    colors = listOf(Color(0xFF070913), Color(0xFF0E1326), Color(0xFF080A15))
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
