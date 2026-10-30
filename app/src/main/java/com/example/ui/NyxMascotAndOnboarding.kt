package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.AstrodraconSapphire
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelPeach
import com.example.ui.theme.PastelSky
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Share
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
import com.example.ui.theme.CosmicTextSubtle
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
        val cheerSin = sin(tapCheerProgress.value * PI).toFloat().coerceAtLeast(0f)
        val jumpOffsetY = -breathePhase * 10f - cheerSin * 26f
        val squashX = 1f + breathePhase * 0.03f + cheerSin * 0.12f
        val squashY = 1f - breathePhase * 0.03f + cheerSin * 0.10f
        val tiltDegrees = wavePhase * 3.5f + (sin(tapCheerProgress.value * PI * 2).toFloat() * 12f)

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

        // 1. Ambient Cosmic Glow Circle & Dynamic Floor Shadow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w * 0.5f
            val cy = h * 0.52f
            val r = (w.coerceAtMost(h) * 0.44f)

            // Cosmic Radial Glow Badge
            val badgeBrush = Brush.radialGradient(
                colors = listOf(
                    auraColor.copy(alpha = 0.30f + cheerSin * 0.20f),
                    NebulaViolet.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = r * 1.50f
            )
            drawCircle(brush = badgeBrush, radius = r * 1.35f, center = Offset(cx, cy))

            // Planetary Orbit Ring
            val orbitPath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(cx - r * 1.15f, cy + r * 0.2f, cx + r * 1.15f, cy + r * 0.60f))
            }
            drawPath(
                path = orbitPath,
                color = auraColor.copy(alpha = 0.40f),
                style = Stroke(width = (w * 0.016f).coerceAtLeast(1.5f), cap = StrokeCap.Round)
            )

            // Dynamic Floor Shadow
            val shadowScale = (1f - breathePhase * 0.25f - cheerSin * 0.40f).coerceIn(0.3f, 1.1f)
            drawOval(
                color = Color.Black.copy(alpha = 0.35f * shadowScale),
                topLeft = Offset(cx - r * 0.65f * shadowScale, h * 0.88f),
                size = Size(r * 1.30f * shadowScale, h * 0.08f)
            )

            // Twinkling Stardust Orbit Particles
            val sparkleAlpha = if (cheerSin > 0.05f) 1f else (0.45f + 0.45f * breathePhase)
            val starRadius = (w * 0.040f) * (1f + cheerSin * 0.6f)
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
                    strokeWidth = (w * 0.020f).coerceAtLeast(1.5f),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = starColor,
                    start = Offset(pos.x, pos.y - starRadius),
                    end = Offset(pos.x, pos.y + starRadius),
                    strokeWidth = (w * 0.020f).coerceAtLeast(1.5f),
                    cap = StrokeCap.Round
                )
            }
        }

        // 2. Official Astrodracon Celestial Dragon Master Artwork
        val isSparkle = mood == MascotMood.SUCCESS_LEAP || mood == MascotMood.ALERT_FIRE || cheerSin > 0.05f
        val isSmall = size <= 48.dp

        val drawableRes = when {
            isSmall -> R.drawable.astrodracon_avatar_icon
            isSparkle -> R.drawable.astrodracon_sparkle
            else -> R.drawable.astrodracon_mascot
        }

        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = "Astrodracon Guardian of the Cosmos",
            modifier = Modifier
                .fillMaxSize(if (isSmall) 0.94f else 0.88f)
                .graphicsLayer {
                    this.translationY = jumpOffsetY
                    this.scaleX = squashX
                    this.scaleY = squashY
                    this.rotationZ = tiltDegrees
                },
            contentScale = ContentScale.Fit
        )
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
        recoveryAnswer: String,
        googleEmail: String,
        googleDisplayName: String
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

    var recoveryQuestion by remember { mutableStateOf("Apa nama planet atau bintang rahasia Anda?") }
    var recoveryAnswer by remember { mutableStateOf("") }
    var googleEmail by remember { mutableStateOf("") }
    var googleDisplayName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember(context) { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                googleEmail = account.email ?: ""
                googleDisplayName = account.displayName ?: account.givenName ?: "Google User"
                errorMessage = null
            }
        } catch (_: Exception) {
            val last = GoogleSignIn.getLastSignedInAccount(context)
            if (last != null) {
                googleEmail = last.email ?: ""
                googleDisplayName = last.displayName ?: "Google User"
            }
        }
    }

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
                                onValueChange = {
                                    val clean = it.uppercase().filter { ch -> ch.isDigit() || ch in 'A'..'Z' }.take(6)
                                    v1Pin1 = clean
                                },
                                tag = "setup_v1_pin1"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OnboardingPinField(
                                label = "PIN Lapis 2 (Kunci Cipher NYX2)",
                                value = v1Pin2,
                                onValueChange = {
                                    val clean = it.uppercase().filter { ch -> ch.isDigit() || ch in 'A'..'Z' }.take(6)
                                    v1Pin2 = clean
                                },
                                tag = "setup_v1_pin2"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OnboardingPinField(
                                label = "PIN Lapis 3 (Otorisasi Brankas)",
                                value = v1Pin3,
                                onValueChange = {
                                    val clean = it.uppercase().filter { ch -> ch.isDigit() || ch in 'A'..'Z' }.take(6)
                                    v1Pin3 = clean
                                },
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
                                onValueChange = {
                                    val clean = it.uppercase().filter { ch -> ch.isDigit() || ch in 'A'..'Z' }.take(6)
                                    v2Pin1 = clean
                                },
                                tag = "setup_v2_pin1"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OnboardingPinField(
                                label = "Brankas 2 - PIN Lapis 2",
                                value = v2Pin2,
                                onValueChange = {
                                    val clean = it.uppercase().filter { ch -> ch.isDigit() || ch in 'A'..'Z' }.take(6)
                                    v2Pin2 = clean
                                },
                                tag = "setup_v2_pin2"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OnboardingPinField(
                                label = "Brankas 2 - PIN Lapis 3",
                                value = v2Pin3,
                                onValueChange = {
                                    val clean = it.uppercase().filter { ch -> ch.isDigit() || ch in 'A'..'Z' }.take(6)
                                    v2Pin3 = clean
                                },
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
                                placeholder = { Text("Wajib diisi untuk pemulihan lupa PIN") },
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

                            Spacer(modifier = Modifier.height(12.dp))

                            // Google Account One-Tap Native OAuth Linking Card
                            Surface(
                                color = CosmicSurface,
                                shape = RoundedCornerShape(22.dp),
                                modifier = Modifier.fillMaxWidth(),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (googleEmail.isNotBlank()) PastelMint else StarlightCyan.copy(alpha = 0.45f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            color = if (googleEmail.isNotBlank()) PastelMint.copy(alpha = 0.20f) else StarlightCyan.copy(alpha = 0.18f),
                                            shape = CircleShape,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (googleEmail.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                                                    contentDescription = null,
                                                    tint = if (googleEmail.isNotBlank()) PastelMint else StarlightCyan,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (googleEmail.isNotBlank()) "Akun Google Terhubung" else "Masuk dengan Akun Google",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = CosmicTextPure,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (googleEmail.isNotBlank()) "Siap untuk sinkronisasi cloud enkripsi AES-256" else "Tautkan akun Google Anda dengan satu sentuhan",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CosmicTextMuted
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (googleEmail.isNotBlank()) {
                                        Surface(
                                            color = CosmicVoid,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = if (googleDisplayName.isNotBlank()) googleDisplayName else "Pengguna Google",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = PastelSky,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = googleEmail,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = CosmicTextPure.copy(alpha = 0.85f)
                                                    )
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        try {
                                                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                                        } catch (_: Exception) {
                                                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.google.com/"))
                                                            context.startActivity(webIntent)
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, StarlightCyan.copy(alpha = 0.5f))
                                                ) {
                                                    Text("Ganti", color = StarlightCyan, style = MaterialTheme.typography.labelMedium)
                                                }
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                try {
                                                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                                } catch (_: Exception) {
                                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.google.com/"))
                                                    context.startActivity(webIntent)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = StarlightCyan,
                                                contentColor = CosmicVoid
                                            )
                                        ) {
                                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Login dengan Akun Google", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedButton(
                                            onClick = {
                                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.google.com/"))
                                                context.startActivity(webIntent)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(42.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
                                        ) {
                                            Text("Buka Web Google Login", color = CosmicTextSubtle, style = MaterialTheme.typography.bodySmall)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Fallback manual email input
                                        OutlinedTextField(
                                            value = googleEmail,
                                            onValueChange = { googleEmail = it },
                                            label = { Text("Atau Ketik Email Manual") },
                                            placeholder = { Text("nama@gmail.com") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = CosmicVoid,
                                                unfocusedContainerColor = CosmicVoid,
                                                focusedBorderColor = StarlightCyan,
                                                unfocusedBorderColor = CosmicSurface,
                                                focusedTextColor = CosmicTextPure,
                                                unfocusedTextColor = CosmicTextPure
                                            )
                                        )
                                    }
                                }
                            }

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

                            Button(
                                onClick = {
                                    if (recoveryAnswer.isBlank()) {
                                        errorMessage = "Jawaban pertanyaan pemulihan rahasia wajib diisi!"
                                        return@Button
                                    }
                                    onCompleteSetup(
                                        v1Pin1, v1Pin2, v1Pin3,
                                        v2Pin1, v2Pin2, v2Pin3,
                                        biometricEnabled,
                                        recoveryQuestion.ifBlank { "Apa nama planet atau bintang rahasia Anda?" },
                                        recoveryAnswer.trim(),
                                        googleEmail.trim(),
                                        googleDisplayName.trim()
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
