package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CosmicBorderHairline
import com.example.ui.theme.CosmicMagenta
import com.example.ui.theme.CosmicObsidian
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicTextMuted
import com.example.ui.theme.CosmicTextPure
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.SolarGold
import com.example.ui.theme.StarlightCyan
import com.example.ui.theme.StellarYellow
import com.example.ui.theme.VaultCharcoal
import com.example.ui.theme.VaultCoral
import com.example.ui.theme.VaultLime
import com.example.ui.theme.VaultMintCard
import com.example.ui.theme.VaultObsidian
import com.example.ui.theme.VaultPureWhite
import com.example.ui.theme.VaultSlate
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 8 Emotional States for the Cosmic Celestial Dragon Mascot.
 * Also includes backward-compatible enum values for existing call sites.
 */
enum class MascotMood {
    IDLE_BREATHE,
    TOUCH_REACTION,
    PIN_SHIELD,
    ALERT_FIRE,
    SUCCESS_LEAP,
    DOWNLOAD_HUNTER,
    MT_INSPECTING,
    STEALTH_SHADOW,

    // Backward-compatibility aliases
    WAVE_HELLO,
    GUARD_ALERT,
    SHIELD_EYES,
    HAPPY_VIBES
}

enum class MascotPersona(val label: String, val badgeColor: Color) {
    COSMIC_DRAGON("Astra Galaxy Dragon", Color(0xFF7C3AED)),
    AURORA_WYRM("Aurora Wyrm", Color(0xFF06B6D4)),
    SOLAR_DRAKE("Solar Drake", Color(0xFFF59E0B)),
    NEBULA_PHANTOM("Nebula Phantom", Color(0xFFEC4899)),

    // Backward-compatibility aliases
    SHADOW_GUARDIAN("Astra Galaxy Dragon", Color(0xFF7C3AED)),
    SPIRIT_FLAME("Aurora Wyrm", Color(0xFF06B6D4)),
    KITSUNE_FOX("Solar Drake", Color(0xFFF59E0B)),
    CYBER_PHANTOM("Nebula Phantom", Color(0xFFEC4899))
}

/**
 * 0ms Lightning-Fast Touch Trigger (Instant touch down)
 */
fun Modifier.instantTouchPress(
    enabled: Boolean = true,
    onPressInstant: () -> Unit
): Modifier = composed {
    val currentCallback by rememberUpdatedState(onPressInstant)
    var isPressed by remember { mutableStateOf(false) }

    this
        .graphicsLayer {
            val s = if (isPressed) 0.92f else 1f
            scaleX = s
            scaleY = s
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                down.consume()
                currentCallback()
                waitForUpOrCancellation()
                isPressed = false
            }
        }
}

/**
 * 0ms Zero-Overhead Click untuk elemen di dalam scroll
 */
fun Modifier.fastZeroLatencyClick(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.clickable(
        enabled = enabled,
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    )
}

/**
 * Cosmic Celestial Dragon Mascot Avatar ("Astra"):
 * 4-legged quadruped dragon with majestic wings, crystalline horns, swishing celestial tail,
 * dynamic shifting galaxy aura (Nebula Violet <-> Starlight Cyan <-> Cosmic Magenta),
 * and 8 Duolingo-style dynamic emotional states.
 */
@Composable
fun CosmicDragonAvatar(
    mood: MascotMood,
    modifier: Modifier = Modifier,
    size: Dp = 112.dp,
    persona: MascotPersona = MascotPersona.COSMIC_DRAGON,
    externalCheerKey: Int = 0
) {
    val scope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_dragon_anim")

    // 1. Shifting Galaxy Aura Cycle (0f..1f)
    val auraColorPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dragon_aura_color"
    )

    // 2. Breathing & Gentle Hover Bounce (0f..1f)
    val breathePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dragon_breathe"
    )

    // 3. Wing Flap & Tail Swish (-1f..1f)
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 620, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dragon_wave"
    )

    // 4. Natural Eye Blink Cycle
    val blinkHeightRatio by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3600
                1f at 0
                1f at 2800
                0.08f at 2920 // Blink 1
                1f at 3040
                1f at 3180
                0.08f at 3280 // Blink 2
                1f at 3400
                1f at 3600
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dragon_blink"
    )

    // 5. Look Phase
    val lookPhase by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dragon_look"
    )

    // 6. Tap / Cheer Burst Animation
    val tapCheerProgress = remember { Animatable(0f) }

    androidx.compose.runtime.LaunchedEffect(externalCheerKey) {
        if (externalCheerKey > 0) {
            tapCheerProgress.snapTo(0f)
            tapCheerProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .instantTouchPress {
                scope.launch {
                    tapCheerProgress.snapTo(0f)
                    tapCheerProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
            }
            .testTag("nyx_mascot_avatar"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w * 0.5f
            val cy = h * 0.52f
            val r = (w.coerceAtMost(h) * 0.44f)

            // Resolve Normalized Mood
            val effectiveMood = when (mood) {
                MascotMood.WAVE_HELLO -> MascotMood.IDLE_BREATHE
                MascotMood.GUARD_ALERT -> MascotMood.ALERT_FIRE
                MascotMood.SHIELD_EYES -> MascotMood.PIN_SHIELD
                MascotMood.HAPPY_VIBES -> MascotMood.SUCCESS_LEAP
                else -> mood
            }

            val cheerSin = sin(tapCheerProgress.value * PI).toFloat().coerceAtLeast(0f)
            val jumpOffsetY = -breathePhase * r * 0.12f - cheerSin * r * 0.38f
            val squashX = 1f + breathePhase * 0.04f + cheerSin * 0.16f
            val squashY = 1f - breathePhase * 0.04f + cheerSin * 0.14f
            val tiltDegrees = wavePhase * 4.5f + (sin(tapCheerProgress.value * PI * 2).toFloat() * 15f)

            // Dynamic Shifting Galaxy Palette
            val auraColor = when {
                auraColorPhase < 0.33f -> {
                    val t = auraColorPhase / 0.33f
                    Color(
                        red = (1f - t) * 0.48f + t * 0.02f,
                        green = (1f - t) * 0.23f + t * 0.71f,
                        blue = (1f - t) * 0.93f + t * 0.83f
                    )
                }
                auraColorPhase < 0.66f -> {
                    val t = (auraColorPhase - 0.33f) / 0.33f
                    Color(
                        red = (1f - t) * 0.02f + t * 0.92f,
                        green = (1f - t) * 0.71f + t * 0.28f,
                        blue = (1f - t) * 0.83f + t * 0.60f
                    )
                }
                else -> {
                    val t = (auraColorPhase - 0.66f) / 0.34f
                    Color(
                        red = (1f - t) * 0.92f + t * 0.48f,
                        green = (1f - t) * 0.28f + t * 0.23f,
                        blue = (1f - t) * 0.60f + t * 0.93f
                    )
                }
            }

            // 1. Planetary Cosmic Aura Badge
            val badgeBrush = Brush.radialGradient(
                colors = listOf(
                    auraColor.copy(alpha = 0.32f + cheerSin * 0.25f),
                    NebulaViolet.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = r * 1.55f
            )
            drawCircle(brush = badgeBrush, radius = r * 1.45f, center = Offset(cx, cy))

            // Planetary Orbit Ring (Cosmic Dragon Domain)
            val orbitPath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(cx - r * 1.25f, cy + r * 0.2f, cx + r * 1.25f, cy + r * 0.62f))
            }
            drawPath(
                path = orbitPath,
                color = auraColor.copy(alpha = 0.45f),
                style = Stroke(width = (w * 0.018f).coerceAtLeast(1.5f), cap = StrokeCap.Round)
            )

            // Dynamic Floor Shadow
            val shadowScale = (1f - breathePhase * 0.3f - cheerSin * 0.45f).coerceIn(0.3f, 1.1f)
            drawOval(
                color = Color.Black.copy(alpha = 0.28f * shadowScale),
                topLeft = Offset(cx - r * 0.72f * shadowScale, h * 0.86f),
                size = Size(r * 1.44f * shadowScale, h * 0.08f)
            )

            // 2. Transformed Dragon Body & Anatomy
            withTransform({
                translate(left = 0f, top = jumpOffsetY)
                rotate(degrees = tiltDegrees, pivot = Offset(cx, cy + r * 0.4f))
                scale(scaleX = squashX, scaleY = squashY, pivot = Offset(cx, cy + r * 0.5f))
            }) {
                val dragonBodyColor = Color(0xFF0F121C)
                val dragonScaleColor = Color(0xFF1B2030)
                val dragonBellyColor = Color(0xFF282F45)
                val wingMembraneColor = auraColor.copy(alpha = 0.85f)
                val clawColor = Color(0xFFE2E8F0)

                // A. Sinuous Dragon Tail Swishing Behind Body
                val tailWiggle = wavePhase * r * 0.16f
                val tailPath = Path().apply {
                    moveTo(cx + r * 0.42f, cy + r * 0.38f)
                    cubicTo(
                        cx + r * 0.75f, cy + r * 0.32f,
                        cx + r * 0.95f + tailWiggle, cy - r * 0.05f,
                        cx + r * 0.82f + tailWiggle, cy - r * 0.38f
                    )
                }
                drawPath(
                    path = tailPath,
                    color = dragonBodyColor,
                    style = Stroke(width = (r * 0.22f), cap = StrokeCap.Round)
                )

                // Tail Starlight Flame Spade
                val spadeX = cx + r * 0.82f + tailWiggle
                val spadeY = cy - r * 0.38f
                val flameSpade = Path().apply {
                    moveTo(spadeX, spadeY - r * 0.22f)
                    lineTo(spadeX + r * 0.15f, spadeY)
                    lineTo(spadeX, spadeY + r * 0.10f)
                    lineTo(spadeX - r * 0.15f, spadeY)
                    close()
                }
                drawPath(path = flameSpade, color = auraColor)
                drawCircle(color = Color.White, radius = r * 0.06f, center = Offset(spadeX, spadeY - r * 0.04f))

                // B. Quadruped Hind Legs (Left & Right)
                // Hind Left Leg
                drawRoundRect(
                    color = dragonBodyColor,
                    topLeft = Offset(cx - r * 0.78f, cy + r * 0.32f),
                    size = Size(r * 0.28f, r * 0.46f),
                    cornerRadius = CornerRadius(r * 0.12f, r * 0.12f)
                )
                // Hind Left Claws
                drawCircle(color = clawColor, radius = r * 0.04f, center = Offset(cx - r * 0.74f, cy + r * 0.78f))
                drawCircle(color = clawColor, radius = r * 0.04f, center = Offset(cx - r * 0.65f, cy + r * 0.78f))

                // Hind Right Leg
                drawRoundRect(
                    color = dragonBodyColor,
                    topLeft = Offset(cx + r * 0.48f, cy + r * 0.32f),
                    size = Size(r * 0.28f, r * 0.46f),
                    cornerRadius = CornerRadius(r * 0.12f, r * 0.12f)
                )
                // Hind Right Claws
                drawCircle(color = clawColor, radius = r * 0.04f, center = Offset(cx + r * 0.54f, cy + r * 0.78f))
                drawCircle(color = clawColor, radius = r * 0.04f, center = Offset(cx + r * 0.63f, cy + r * 0.78f))

                // C. Dragon Wings (Left Wing & Right Wing)
                val leftWingAngle = when (effectiveMood) {
                    MascotMood.SUCCESS_LEAP -> -42f + wavePhase * 24f - cheerSin * 35f
                    MascotMood.DOWNLOAD_HUNTER -> -55f + wavePhase * 10f
                    MascotMood.ALERT_FIRE -> -35f + wavePhase * 18f
                    else -> -16f + wavePhase * 10f
                }
                val rightWingAngle = when (effectiveMood) {
                    MascotMood.SUCCESS_LEAP -> 42f - wavePhase * 24f + cheerSin * 35f
                    MascotMood.DOWNLOAD_HUNTER -> 55f - wavePhase * 10f
                    MascotMood.ALERT_FIRE -> 35f - wavePhase * 18f
                    else -> 16f - wavePhase * 10f
                }

                if (effectiveMood != MascotMood.PIN_SHIELD) {
                    // Left Dragon Wing
                    withTransform({
                        rotate(degrees = leftWingAngle, pivot = Offset(cx - r * 0.48f, cy - r * 0.05f))
                    }) {
                        val wingPath = Path().apply {
                            moveTo(cx - r * 0.48f, cy - r * 0.05f)
                            lineTo(cx - r * 1.15f, cy - r * 0.75f)
                            cubicTo(
                                cx - r * 1.25f, cy - r * 0.25f,
                                cx - r * 0.85f, cy + r * 0.12f,
                                cx - r * 0.48f, cy + r * 0.08f
                            )
                            close()
                        }
                        drawPath(path = wingPath, color = dragonScaleColor)
                        drawPath(path = wingPath, color = wingMembraneColor.copy(alpha = 0.55f))
                        // Wing bone strut
                        drawLine(
                            color = auraColor,
                            start = Offset(cx - r * 0.48f, cy - r * 0.05f),
                            end = Offset(cx - r * 1.15f, cy - r * 0.75f),
                            strokeWidth = (w * 0.024f).coerceAtLeast(2f),
                            cap = StrokeCap.Round
                        )
                    }

                    // Right Dragon Wing
                    withTransform({
                        rotate(degrees = rightWingAngle, pivot = Offset(cx + r * 0.48f, cy - r * 0.05f))
                    }) {
                        val wingPath = Path().apply {
                            moveTo(cx + r * 0.48f, cy - r * 0.05f)
                            lineTo(cx + r * 1.15f, cy - r * 0.75f)
                            cubicTo(
                                cx + r * 1.25f, cy - r * 0.25f,
                                cx + r * 0.85f, cy + r * 0.12f,
                                cx + r * 0.48f, cy + r * 0.08f
                            )
                            close()
                        }
                        drawPath(path = wingPath, color = dragonScaleColor)
                        drawPath(path = wingPath, color = wingMembraneColor.copy(alpha = 0.55f))
                        // Wing bone strut
                        drawLine(
                            color = auraColor,
                            start = Offset(cx + r * 0.48f, cy - r * 0.05f),
                            end = Offset(cx + r * 1.15f, cy - r * 0.75f),
                            strokeWidth = (w * 0.024f).coerceAtLeast(2f),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // D. Main Dragon Torso
                drawRoundRect(
                    color = dragonBodyColor,
                    topLeft = Offset(cx - r * 0.68f, cy - r * 0.32f),
                    size = Size(r * 1.36f, r * 1.05f),
                    cornerRadius = CornerRadius(r * 0.52f, r * 0.52f)
                )

                // Segmented Cosmic Underbelly
                drawRoundRect(
                    color = dragonBellyColor,
                    topLeft = Offset(cx - r * 0.42f, cy + r * 0.06f),
                    size = Size(r * 0.84f, r * 0.56f),
                    cornerRadius = CornerRadius(r * 0.32f, r * 0.32f)
                )
                // Underbelly glowing segment lines
                for (seg in 0..2) {
                    val lineY = cy + r * 0.18f + seg * r * 0.14f
                    drawLine(
                        color = auraColor.copy(alpha = 0.35f),
                        start = Offset(cx - r * 0.28f, lineY),
                        end = Offset(cx + r * 0.28f, lineY),
                        strokeWidth = (w * 0.015f).coerceAtLeast(1.2f)
                    )
                }

                // E. Quadruped Front Legs (Left & Right)
                // Front Left Leg
                drawRoundRect(
                    color = dragonBodyColor,
                    topLeft = Offset(cx - r * 0.45f, cy + r * 0.38f),
                    size = Size(r * 0.24f, r * 0.44f),
                    cornerRadius = CornerRadius(r * 0.10f, r * 0.10f)
                )
                drawCircle(color = clawColor, radius = r * 0.038f, center = Offset(cx - r * 0.42f, cy + r * 0.82f))
                drawCircle(color = clawColor, radius = r * 0.038f, center = Offset(cx - r * 0.33f, cy + r * 0.82f))

                // Front Right Leg
                drawRoundRect(
                    color = dragonBodyColor,
                    topLeft = Offset(cx + r * 0.21f, cy + r * 0.38f),
                    size = Size(r * 0.24f, r * 0.44f),
                    cornerRadius = CornerRadius(r * 0.10f, r * 0.10f)
                )
                drawCircle(color = clawColor, radius = r * 0.038f, center = Offset(cx + r * 0.24f, cy + r * 0.82f))
                drawCircle(color = clawColor, radius = r * 0.038f, center = Offset(cx + r * 0.33f, cy + r * 0.82f))

                // F. Dragon Crystalline Horns & Crest Spines
                val hornWiggle = wavePhase * r * 0.05f
                val leftHorn = Path().apply {
                    moveTo(cx - r * 0.52f, cy - r * 0.32f)
                    lineTo(cx - r * 0.72f - hornWiggle, cy - r * 0.96f)
                    lineTo(cx - r * 0.24f, cy - r * 0.55f)
                    close()
                }
                val rightHorn = Path().apply {
                    moveTo(cx + r * 0.52f, cy - r * 0.32f)
                    lineTo(cx + r * 0.72f + hornWiggle, cy - r * 0.96f)
                    lineTo(cx + r * 0.24f, cy - r * 0.55f)
                    close()
                }
                drawPath(path = leftHorn, color = auraColor)
                drawPath(path = rightHorn, color = auraColor)

                // Central Crown Spike
                val centerSpike = Path().apply {
                    moveTo(cx - r * 0.12f, cy - r * 0.58f)
                    lineTo(cx, cy - r * 0.82f)
                    lineTo(cx + r * 0.12f, cy - r * 0.58f)
                    close()
                }
                drawPath(path = centerSpike, color = StarlightCyan)

                // G. Expressive Luminous Anime Dragon Eyes
                val eyeY = cy - r * 0.14f
                val leftEyeX = cx - r * 0.32f
                val rightEyeX = cx + r * 0.32f
                val eyeW = r * 0.44f
                val fullEyeH = r * 0.50f
                val currentEyeH = (fullEyeH * blinkHeightRatio).coerceAtLeast(r * 0.05f)

                // Luminous Eye Sockets
                drawCircle(color = auraColor.copy(alpha = 0.25f), radius = eyeW * 0.65f, center = Offset(leftEyeX, eyeY))
                drawCircle(color = auraColor.copy(alpha = 0.25f), radius = eyeW * 0.65f, center = Offset(rightEyeX, eyeY))

                drawOval(
                    color = Color.White,
                    topLeft = Offset(leftEyeX - eyeW * 0.5f, eyeY - currentEyeH * 0.5f),
                    size = Size(eyeW, currentEyeH)
                )
                drawOval(
                    color = Color.White,
                    topLeft = Offset(rightEyeX - eyeW * 0.5f, eyeY - currentEyeH * 0.5f),
                    size = Size(eyeW, currentEyeH)
                )

                if (blinkHeightRatio > 0.25f) {
                    val pupilOffsetX = lookPhase * (eyeW * 0.12f)
                    val pupilOffsetY = sin(breathePhase * PI).toFloat() * (-eyeW * 0.04f)
                    val pupilW = eyeW * 0.32f
                    val pupilH = currentEyeH * 0.68f

                    // Slit/Oval Dragon Pupil
                    drawOval(
                        color = dragonBodyColor,
                        topLeft = Offset(leftEyeX + pupilOffsetX - pupilW * 0.5f, eyeY + pupilOffsetY - pupilH * 0.5f),
                        size = Size(pupilW, pupilH)
                    )
                    drawOval(
                        color = dragonBodyColor,
                        topLeft = Offset(rightEyeX + pupilOffsetX - pupilW * 0.5f, eyeY + pupilOffsetY - pupilH * 0.5f),
                        size = Size(pupilW, pupilH)
                    )

                    // Stardust Eye Reflections
                    drawCircle(color = Color.White, radius = pupilW * 0.38f, center = Offset(leftEyeX + pupilOffsetX - pupilW * 0.2f, eyeY + pupilOffsetY - pupilH * 0.25f))
                    drawCircle(color = Color.White, radius = pupilW * 0.38f, center = Offset(rightEyeX + pupilOffsetX - pupilW * 0.2f, eyeY + pupilOffsetY - pupilH * 0.25f))
                    drawCircle(color = auraColor, radius = pupilW * 0.22f, center = Offset(leftEyeX + pupilOffsetX + pupilW * 0.2f, eyeY + pupilOffsetY + pupilH * 0.22f))
                    drawCircle(color = auraColor, radius = pupilW * 0.22f, center = Offset(rightEyeX + pupilOffsetX + pupilW * 0.2f, eyeY + pupilOffsetY + pupilH * 0.22f))
                }

                // H. Friendly Dragon Snout & Mouth
                val snoutY = cy + r * 0.10f
                // Tiny dragon nostrils
                drawCircle(color = dragonScaleColor, radius = r * 0.03f, center = Offset(cx - r * 0.08f, snoutY))
                drawCircle(color = dragonScaleColor, radius = r * 0.03f, center = Offset(cx + r * 0.08f, snoutY))

                if (effectiveMood == MascotMood.SUCCESS_LEAP || cheerSin > 0.1f) {
                    // Joyous smiling dragon mouth open
                    val mouthPath = Path().apply {
                        moveTo(cx - r * 0.24f, snoutY + r * 0.08f)
                        quadraticTo(cx, snoutY + r * 0.32f, cx + r * 0.24f, snoutY + r * 0.08f)
                        close()
                    }
                    drawPath(path = mouthPath, color = CosmicMagenta)
                } else if (effectiveMood == MascotMood.ALERT_FIRE) {
                    // Snarl / Alert spark mouth
                    drawLine(
                        color = SolarGold,
                        start = Offset(cx - r * 0.22f, snoutY + r * 0.12f),
                        end = Offset(cx + r * 0.22f, snoutY + r * 0.12f),
                        strokeWidth = (w * 0.024f).coerceAtLeast(2f),
                        cap = StrokeCap.Round
                    )
                } else {
                    // Gentle cute smile
                    val mouthPath = Path().apply {
                        moveTo(cx - r * 0.18f, snoutY + r * 0.08f)
                        quadraticTo(cx, snoutY + r * 0.16f, cx + r * 0.18f, snoutY + r * 0.08f)
                    }
                    drawPath(path = mouthPath, color = auraColor, style = Stroke(width = (w * 0.022f).coerceAtLeast(1.8f), cap = StrokeCap.Round))
                }

                // Rosy Dragon Cheeks
                drawOval(
                    color = CosmicMagenta.copy(alpha = 0.45f),
                    topLeft = Offset(leftEyeX - eyeW * 0.58f, eyeY + currentEyeH * 0.32f),
                    size = Size(eyeW * 0.45f, currentEyeH * 0.22f)
                )
                drawOval(
                    color = CosmicMagenta.copy(alpha = 0.45f),
                    topLeft = Offset(rightEyeX + eyeW * 0.12f, eyeY + currentEyeH * 0.32f),
                    size = Size(eyeW * 0.45f, currentEyeH * 0.22f)
                )

                // I. PIN Shield: Wings wrap forward over eyes (Shy Dragon PIN Shield)
                if (effectiveMood == MascotMood.PIN_SHIELD) {
                    val peekGap = (sin(wavePhase * PI).toFloat() * r * 0.04f).coerceAtLeast(0f)
                    // Left Wing Folded Over Left Eye
                    drawRoundRect(
                        color = dragonBodyColor,
                        topLeft = Offset(leftEyeX - eyeW * 0.65f, eyeY - fullEyeH * 0.38f + peekGap),
                        size = Size(eyeW * 1.35f, fullEyeH * 0.78f),
                        cornerRadius = CornerRadius(r * 0.28f, r * 0.28f)
                    )
                    drawRoundRect(
                        color = auraColor,
                        topLeft = Offset(leftEyeX - eyeW * 0.65f, eyeY - fullEyeH * 0.38f + peekGap),
                        size = Size(eyeW * 1.35f, fullEyeH * 0.78f),
                        cornerRadius = CornerRadius(r * 0.28f, r * 0.28f),
                        style = Stroke(width = (w * 0.022f).coerceAtLeast(1.5f))
                    )

                    // Right Wing Folded Over Right Eye
                    drawRoundRect(
                        color = dragonBodyColor,
                        topLeft = Offset(rightEyeX - eyeW * 0.70f, eyeY - fullEyeH * 0.38f + peekGap),
                        size = Size(eyeW * 1.35f, fullEyeH * 0.78f),
                        cornerRadius = CornerRadius(r * 0.28f, r * 0.28f)
                    )
                    drawRoundRect(
                        color = auraColor,
                        topLeft = Offset(rightEyeX - eyeW * 0.70f, eyeY - fullEyeH * 0.38f + peekGap),
                        size = Size(eyeW * 1.35f, fullEyeH * 0.78f),
                        cornerRadius = CornerRadius(r * 0.28f, r * 0.28f),
                        style = Stroke(width = (w * 0.022f).coerceAtLeast(1.5f))
                    )
                }

                // J. MT Inspecting Accessory: Cosmic Magnifying Glass
                if (effectiveMood == MascotMood.MT_INSPECTING) {
                    val glassX = cx + r * 0.45f
                    val glassY = cy + r * 0.12f
                    drawCircle(color = StarlightCyan, radius = r * 0.22f, center = Offset(glassX, glassY), style = Stroke(width = 3f))
                    drawCircle(color = StarlightCyan.copy(alpha = 0.25f), radius = r * 0.20f, center = Offset(glassX, glassY))
                    drawLine(
                        color = SolarGold,
                        start = Offset(glassX + r * 0.14f, glassY + r * 0.14f),
                        end = Offset(glassX + r * 0.35f, glassY + r * 0.35f),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Orbiting Twinkling Stardust Particles (Duolingo Stardust Burst)
            val sparkleAlpha = if (cheerSin > 0.05f) 1f else (0.45f + 0.45f * breathePhase)
            val starRadius = (w * 0.045f) * (1f + cheerSin * 0.6f)
            val starPositions = listOf(
                Offset(w * 0.14f, h * (0.20f - breathePhase * 0.05f - cheerSin * 0.08f)),
                Offset(w * 0.86f, h * (0.18f - (1f - breathePhase) * 0.05f - cheerSin * 0.10f)),
                Offset(w * 0.88f, h * (0.72f - breathePhase * 0.04f)),
                Offset(w * 0.16f, h * (0.76f + breathePhase * 0.03f))
            )
            starPositions.forEachIndexed { idx, pos ->
                val starColor = when (idx % 3) {
                    0 -> SolarGold.copy(alpha = sparkleAlpha)
                    1 -> StarlightCyan.copy(alpha = sparkleAlpha)
                    else -> CosmicTextPure.copy(alpha = sparkleAlpha)
                }
                drawLine(
                    color = starColor,
                    start = Offset(pos.x - starRadius, pos.y),
                    end = Offset(pos.x + starRadius, pos.y),
                    strokeWidth = (w * 0.022f).coerceAtLeast(1.5f),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = starColor,
                    start = Offset(pos.x, pos.y - starRadius),
                    end = Offset(pos.x, pos.y + starRadius),
                    strokeWidth = (w * 0.022f).coerceAtLeast(1.5f),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Backward-Compatibility Alias for NyxMascotAvatar
 */
@Composable
fun NyxMascotAvatar(
    mood: MascotMood,
    modifier: Modifier = Modifier,
    size: Dp = 112.dp,
    persona: MascotPersona = MascotPersona.COSMIC_DRAGON,
    externalCheerKey: Int = 0
) {
    CosmicDragonAvatar(
        mood = mood,
        modifier = modifier,
        size = size,
        persona = persona,
        externalCheerKey = externalCheerKey
    )
}

/**
 * Omnipresent Cosmic Dragon Dialogue Companion Banner:
 * Displays the 4-legged dragon alongside interactive speech bubbles with 0ms touch reactions.
 */
@Composable
fun CosmicDragonCompanionBanner(
    activeVaultId: Int,
    headline: String,
    subtitle: String,
    mood: MascotMood = MascotMood.SUCCESS_LEAP,
    onQuickActionClick: (() -> Unit)? = null,
    quickActionLabel: String? = null
) {
    var tapCounter by remember { mutableIntStateOf(0) }
    val personas = MascotPersona.entries
    val currentPersona = personas[tapCounter % 4]

    val dragonQuips = remember(activeVaultId, headline) {
        listOf(
            headline,
            "Astra siaga! Orbit Brankas $activeVaultId terlindungi penuh!",
            "Sentuh aku untuk melihat semburan api bintang!",
            "MT Explorer & Mori Downloader siap meluncur!"
        )
    }
    val activeHeadline = dragonQuips[tapCounter % dragonQuips.size]
    val activeMood = when (tapCounter % 4) {
        1 -> MascotMood.TOUCH_REACTION
        2 -> MascotMood.SUCCESS_LEAP
        3 -> MascotMood.DOWNLOAD_HUNTER
        else -> mood
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fastZeroLatencyClick { tapCounter++ }
            .testTag("nyx_mascot_companion_banner"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicObsidian),
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CosmicDragonAvatar(
                mood = activeMood,
                size = 58.dp,
                persona = currentPersona,
                externalCheerKey = tapCounter
            )

            Surface(
                modifier = Modifier.weight(1f),
                color = CosmicSurface,
                shape = RoundedCornerShape(
                    topStart = 6.dp,
                    topEnd = 16.dp,
                    bottomEnd = 16.dp,
                    bottomStart = 16.dp
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = currentPersona.badgeColor.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = currentPersona.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = currentPersona.badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Ketuk aku!",
                            style = MaterialTheme.typography.labelMedium,
                            color = CosmicTextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = activeHeadline,
                        style = MaterialTheme.typography.titleMedium,
                        color = CosmicTextPure,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 11.sp,
                        color = CosmicTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (onQuickActionClick != null && quickActionLabel != null) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .instantTouchPress { onQuickActionClick() },
                    color = currentPersona.badgeColor
                ) {
                    Text(
                        text = quickActionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 11.sp,
                        color = CosmicVoid,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

/**
 * Backward-Compatibility Alias for NyxMascotCompanionBanner
 */
@Composable
fun NyxMascotCompanionBanner(
    activeVaultId: Int,
    headline: String,
    subtitle: String,
    mood: MascotMood = MascotMood.HAPPY_VIBES,
    onQuickActionClick: (() -> Unit)? = null,
    quickActionLabel: String? = null
) {
    CosmicDragonCompanionBanner(
        activeVaultId = activeVaultId,
        headline = headline,
        subtitle = subtitle,
        mood = mood,
        onQuickActionClick = onQuickActionClick,
        quickActionLabel = quickActionLabel
    )
}

/**
 * First-Open Setup Wizard dengan Maskot Naga Kosmik
 */
@Composable
fun FirstRunOnboardingWizardScreen(
    onCompleteSetup: (
        v1p1: String,
        v1p2: String,
        v1p3: String,
        v2p1: String,
        v2p2: String,
        v2p3: String,
        biometricEnabled: Boolean,
        recoveryQuestion: String,
        recoveryAnswer: String
    ) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }

    var v1Pin1 by remember { mutableStateOf("") }
    var v1Pin2 by remember { mutableStateOf("") }
    var v1Pin3 by remember { mutableStateOf("") }

    var v2Pin1 by remember { mutableStateOf("") }
    var v2Pin2 by remember { mutableStateOf("") }
    var v2Pin3 by remember { mutableStateOf("") }

    var biometricEnabled by remember { mutableStateOf(true) }
    var isFingerprintRegisteredInWizard by remember { mutableStateOf(false) }
    var fingerprintStatusMessage by remember {
        mutableStateOf("Tempel & tahan jari Anda pada sensor di bawah hingga 100% untuk mendaftarkan sidik jari.")
    }

    var recoveryQuestion by remember { mutableStateOf("Apa kata kunci pemulihan rahasia Anda?") }
    var recoveryAnswer by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicVoid)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 500.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (step in 0..3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (step <= currentStep) StarlightCyan else CosmicSurface)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            CosmicDragonAvatar(
                mood = when (currentStep) {
                    0 -> MascotMood.IDLE_BREATHE
                    1 -> MascotMood.PIN_SHIELD
                    2 -> MascotMood.STEALTH_SHADOW
                    else -> MascotMood.SUCCESS_LEAP
                },
                size = 96.dp,
                externalCheerKey = currentStep
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = StarlightCyan.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "CELESTIAL SETUP • LANGKAH ${currentStep + 1} / 4",
                    style = MaterialTheme.typography.labelMedium,
                    color = StarlightCyan,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (currentStep) {
                0 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicObsidian),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Selamat Datang di Arcanum",
                                style = MaterialTheme.typography.headlineMedium,
                                color = CosmicTextPure,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ruang rahasia kosmik dengan enkripsi NYX2, downloader media Mori, dan dual-pane MT Explorer.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CosmicTextMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            FeatureHighlightRow(
                                title = "3-Lapis PIN + Sensor Sidik Jari Nyata",
                                desc = "Kunci diturunkan dari 3 PIN pilihan Anda & pemindaian sidik jari 100%."
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FeatureHighlightRow(
                                title = "Dual-Mode Brankas & Pemulihan Lupa Sandi",
                                desc = "2 ruang brankas terpisah serta pemulihan aman jika Anda lupa PIN."
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { currentStep = 1 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("onboarding_start_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StarlightCyan,
                                    contentColor = CosmicVoid
                                )
                            ) {
                                Text(
                                    text = "Mulai Buat PIN Brankas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                            }
                        }
                    }
                }

                1 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicObsidian),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "PIN 3-Lapis Brankas 1 (Utama)",
                                style = MaterialTheme.typography.headlineMedium,
                                color = CosmicTextPure
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Masukkan masing-masing 6 digit angka untuk Lapis 1, Lapis 2, dan Lapis 3.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CosmicTextMuted
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OnboardingPinField(
                                label = "PIN Lapis 1 (Gerbang Utama)",
                                value = v1Pin1,
                                onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) v1Pin1 = it },
                                tag = "setup_v1_pin1"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OnboardingPinField(
                                label = "PIN Lapis 2 (Kunci Cipher NYX2)",
                                value = v1Pin2,
                                onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) v1Pin2 = it },
                                tag = "setup_v1_pin2"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OnboardingPinField(
                                label = "PIN Lapis 3 (Otorisasi Brankas)",
                                value = v1Pin3,
                                onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) v1Pin3 = it },
                                tag = "setup_v1_pin3"
                            )

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CosmicMagenta,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        errorMessage = null
                                        currentStep = 0
                                    },
                                    modifier = Modifier
                                        .weight(0.4f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
                                ) {
                                    Text("Kembali", color = CosmicTextPure)
                                }

                                Button(
                                    onClick = {
                                        if (v1Pin1.length != 6 || v1Pin2.length != 6 || v1Pin3.length != 6) {
                                            errorMessage = "Ketiga PIN Brankas 1 wajib tepat 6 digit angka!"
                                        } else {
                                            errorMessage = null
                                            currentStep = 2
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(0.6f)
                                        .height(48.dp)
                                        .testTag("onboarding_next_v1"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StarlightCyan,
                                        contentColor = CosmicVoid
                                    )
                                ) {
                                    Text("Lanjut ke Brankas 2", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicObsidian),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "PIN 3-Lapis Brankas 2 (Kedua)",
                                style = MaterialTheme.typography.headlineMedium,
                                color = CosmicTextPure
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Brankas kedua dengan isi terpisah. PIN Lapis 1 wajib berbeda dari Brankas 1 ($v1Pin1).",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CosmicTextMuted
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OnboardingPinField(
                                label = "Brankas 2 - PIN Lapis 1",
                                value = v2Pin1,
                                onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) v2Pin1 = it },
                                tag = "setup_v2_pin1"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OnboardingPinField(
                                label = "Brankas 2 - PIN Lapis 2",
                                value = v2Pin2,
                                onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) v2Pin2 = it },
                                tag = "setup_v2_pin2"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OnboardingPinField(
                                label = "Brankas 2 - PIN Lapis 3",
                                value = v2Pin3,
                                onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) v2Pin3 = it },
                                tag = "setup_v2_pin3"
                            )

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CosmicMagenta,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        errorMessage = null
                                        currentStep = 1
                                    },
                                    modifier = Modifier
                                        .weight(0.4f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
                                ) {
                                    Text("Kembali", color = CosmicTextPure)
                                }

                                Button(
                                    onClick = {
                                        when {
                                            v2Pin1.length != 6 || v2Pin2.length != 6 || v2Pin3.length != 6 -> {
                                                errorMessage = "Ketiga PIN Brankas 2 wajib tepat 6 digit angka!"
                                            }
                                            v2Pin1 == v1Pin1 -> {
                                                errorMessage = "PIN Lapis 1 Brankas 2 harus berbeda dari Brankas 1!"
                                            }
                                            else -> {
                                                errorMessage = null
                                                currentStep = 3
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(0.6f)
                                        .height(48.dp)
                                        .testTag("onboarding_next_v2"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NebulaViolet,
                                        contentColor = CosmicTextPure
                                    )
                                ) {
                                    Text("Lanjut Sidik Jari & Pemulihan", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }

                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicObsidian),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sidik Jari & Pemulihan Lupa Sandi",
                                style = MaterialTheme.typography.headlineMedium,
                                color = CosmicTextPure,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Atur kata kunci pemulihan jika lupa PIN dan daftarkan sidik jari Anda.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CosmicTextMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = recoveryQuestion,
                                onValueChange = { recoveryQuestion = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Pertanyaan Keamanan (Lupa PIN)") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CosmicVoid,
                                    unfocusedContainerColor = CosmicVoid,
                                    focusedBorderColor = StarlightCyan,
                                    unfocusedBorderColor = CosmicSurface,
                                    focusedTextColor = CosmicTextPure,
                                    unfocusedTextColor = CosmicTextPure,
                                    focusedLabelColor = StarlightCyan,
                                    unfocusedLabelColor = CosmicTextMuted
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = recoveryAnswer,
                                onValueChange = { recoveryAnswer = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_recovery_answer_input"),
                                label = { Text("Jawaban / Kata Kunci Pemulihan Rahasia") },
                                placeholder = { Text("Contoh: kucingku99 (Default: nyxvault)") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CosmicVoid,
                                    unfocusedContainerColor = CosmicVoid,
                                    focusedBorderColor = StarlightCyan,
                                    unfocusedBorderColor = CosmicSurface,
                                    focusedTextColor = CosmicTextPure,
                                    unfocusedTextColor = CosmicTextPure,
                                    focusedLabelColor = StarlightCyan,
                                    unfocusedLabelColor = CosmicTextMuted
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Surface(
                                color = CosmicSurface,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = null,
                                            tint = StarlightCyan,
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Autentikasi Sidik Jari Lapis 4",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = CosmicTextPure
                                            )
                                            Text(
                                                text = if (isFingerprintRegisteredInWizard) {
                                                    "Sidik Jari Terdaftar (100%)"
                                                } else {
                                                    "Wajib pindai sidik jari saat masuk & ekspor"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = CosmicTextMuted
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = biometricEnabled,
                                        onCheckedChange = { biometricEnabled = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = StarlightCyan,
                                            checkedTrackColor = CosmicVoid
                                        )
                                    )
                                }
                            }

                            if (biometricEnabled) {
                                Spacer(modifier = Modifier.height(14.dp))
                                InteractiveFingerprintScannerPad(
                                    onScanCompleted = {
                                        isFingerprintRegisteredInWizard = true
                                        fingerprintStatusMessage = "Sidik jari Anda berhasil direkam & diverifikasi (100%)!"
                                    },
                                    onPartialReleaseWarning = { msg ->
                                        fingerprintStatusMessage = msg
                                    }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = fingerprintStatusMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isFingerprintRegisteredInWizard) StarlightCyan else CosmicTextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    onCompleteSetup(
                                        v1Pin1, v1Pin2, v1Pin3,
                                        v2Pin1, v2Pin2, v2Pin3,
                                        biometricEnabled,
                                        recoveryQuestion.ifBlank { "Apa kata kunci pemulihan rahasia Anda?" },
                                        recoveryAnswer.ifBlank { "nyxvault" }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("onboarding_finish_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StarlightCyan,
                                    contentColor = CosmicVoid
                                )
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Simpan & Buka Brankas Sekarang",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveFingerprintScannerPad(
    onScanCompleted: () -> Unit,
    onPartialReleaseWarning: (String) -> Unit
) {
    var scanProgress by remember { mutableStateOf(0f) }
    var isScanning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(CosmicSurface)
            .border(2.dp, if (scanProgress >= 1f) StarlightCyan else CosmicBorderHairline, CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isScanning = true
                    down.consume()
                    scope.launch {
                        while (isScanning && scanProgress < 1f) {
                            kotlinx.coroutines.delay(40)
                            scanProgress = (scanProgress + 0.05f).coerceAtMost(1f)
                        }
                        if (scanProgress >= 1f) {
                            onScanCompleted()
                        }
                    }
                    waitForUpOrCancellation()
                    if (scanProgress < 1f) {
                        isScanning = false
                        scanProgress = 0f
                        onPartialReleaseWarning("Pelepasan jari terlalu cepat. Tahan hingga lingkaran penuh 100%.")
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = StarlightCyan,
                startAngle = -90f,
                sweepAngle = 360f * scanProgress,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "Pindai Sidik Jari",
            tint = if (scanProgress >= 1f) StarlightCyan else CosmicTextPure,
            modifier = Modifier.size(42.dp)
        )
    }
}

@Composable
private fun FeatureHighlightRow(title: String, desc: String) {
    Surface(
        color = CosmicSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StarlightCyan),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = CosmicVoid,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicTextPure
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CosmicTextMuted
                )
            }
        }
    }
}

@Composable
private fun OnboardingPinField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    tag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        label = { Text(label) },
        placeholder = { Text("6 digit angka (contoh: 284910)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = TextStyle(
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = CosmicTextPure,
            letterSpacing = 4.sp
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CosmicVoid,
            unfocusedContainerColor = CosmicVoid,
            focusedBorderColor = StarlightCyan,
            unfocusedBorderColor = CosmicSurface,
            focusedLabelColor = StarlightCyan,
            unfocusedLabelColor = CosmicTextMuted
        )
    )
}
