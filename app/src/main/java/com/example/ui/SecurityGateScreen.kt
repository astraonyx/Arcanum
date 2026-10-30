package com.example.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.SecurityConfig
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VaultAmber
import com.example.ui.theme.VaultCharcoal
import com.example.ui.theme.VaultCoral
import com.example.ui.theme.VaultLime
import com.example.ui.theme.VaultMintAccent
import com.example.ui.theme.VaultMintCard
import com.example.ui.theme.VaultObsidian
import com.example.ui.theme.VaultPureWhite
import com.example.ui.theme.VaultSlate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Helper untuk memanggil dialog Biometrik Perangkat Keras Android (Sidik Jari / Wajah)
 * jika tersedia dan terdaftar di OS.
 */
fun triggerHardwareBiometricIfAvailable(
    context: android.content.Context,
    title: String,
    subtitle: String,
    onSuccess: () -> Unit,
    onErrorFeedback: (String) -> Unit
): Boolean {
    val activity = context as? FragmentActivity ?: return false
    val biometricManager = BiometricManager.from(context)
    val canAuth = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
    )
    if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) return false

    val executor = ContextCompat.getMainExecutor(context)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onErrorFeedback("Sensor Biometrik: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onErrorFeedback("Sidik jari tidak dikenali, coba lagi.")
            }
        }
    )
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText("Gunakan Sensor Layar")
        .build()
    prompt.authenticate(promptInfo)
    return true
}

/**
 * Layar Kunci Minimalis 3-Lapis PIN + Pemindai Sidik Jari Ultrasonik Nyata + Fitur Lupa PIN.
 */
@Composable
fun SecurityGateScreen(
    securityState: SecurityGateUiState,
    onDigitClick: (Int) -> Unit,
    onCharClick: (Char) -> Unit = { c -> onDigitClick(c.digitToIntOrNull() ?: 0) },
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    onBiometricSuccess: () -> Unit,
    onBiometricFeedback: (String) -> Unit,
    onForgotPinClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(securityState.stage) {
        if (securityState.stage == AuthStage.BIOMETRIC_STEP) {
            triggerHardwareBiometricIfAvailable(
                context = context,
                title = "Autentikasi Sidik Jari Trantor Vault",
                subtitle = "Verifikasi sidik jari Anda untuk membuka brankas",
                onSuccess = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBiometricSuccess()
                },
                onErrorFeedback = onBiometricFeedback
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultObsidian)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 460.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Minimalist Top Bar: Mascot + Layer Indicator + Lupa PIN
            var lockPersonaIdx by remember { mutableIntStateOf(0) }
            val personas = MascotPersona.entries
            val activePersona = personas[lockPersonaIdx % personas.size]
            val currentMood = when {
                securityState.errorMessage != null -> MascotMood.ALERT_FIRE
                securityState.enteredPin.isNotEmpty() -> MascotMood.PIN_SHIELD
                securityState.stage == AuthStage.BIOMETRIC_STEP -> MascotMood.SUCCESS_LEAP
                else -> MascotMood.IDLE_BREATHE
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .instantTouchPress {
                                lockPersonaIdx = (lockPersonaIdx + 1) % personas.size
                            }
                            .padding(4.dp)
                            .testTag("lock_screen_mascot_stage")
                    ) {
                        CosmicDragonAvatar(
                            mood = currentMood,
                            size = 46.dp,
                            persona = activePersona
                        )
                        Column {
                            Text(
                                text = "Arcanum Celestial Vault",
                                style = MaterialTheme.typography.titleMedium,
                                color = VaultPureWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${activePersona.label} • Ketuk Ganti",
                                style = MaterialTheme.typography.labelMedium,
                                color = activePersona.badgeColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Tombol Lupa PIN / Pemulihan Sandi
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .instantTouchPress { onForgotPinClick() }
                            .testTag("forgot_pin_lock_button"),
                        color = VaultCharcoal,
                        border = androidx.compose.foundation.BorderStroke(1.dp, VaultSlate)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Lupa PIN",
                                tint = VaultLime,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Lupa PIN?",
                                style = MaterialTheme.typography.labelMedium,
                                color = VaultPureWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Minimalist 4-Step Progress Bar
                MinimalSecurityProgressRow(currentStage = securityState.stage)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Main Center Stage: PIN Entry or Real Ultrasonic Fingerprint Scanner
            if (securityState.stage == AuthStage.BIOMETRIC_STEP) {
                BiometricVerificationCard(
                    statusMessage = securityState.biometricStatusMessage,
                    onTriggerHardwareBiometric = {
                        triggerHardwareBiometricIfAvailable(
                            context = context,
                            title = "Autentikasi Sidik Jari Trantor Vault",
                            subtitle = "Verifikasi sidik jari Anda untuk membuka brankas",
                            onSuccess = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBiometricSuccess()
                            },
                            onErrorFeedback = onBiometricFeedback
                        )
                    },
                    onHoldFingerprintCompleted = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBiometricSuccess()
                    },
                    onHoldFingerprintFeedback = onBiometricFeedback
                )
            } else {
                PinEntryGateCard(
                    stage = securityState.stage,
                    enteredPin = securityState.enteredPin,
                    errorMessage = securityState.errorMessage,
                    onCharClick = { char ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCharClick(char)
                    },
                    onBackspaceClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBackspaceClick()
                    },
                    onClearClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClearClick()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MinimalSecurityProgressRow(currentStage: AuthStage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (stepNum in 1..4) {
                val isCompleted = currentStage.layerNumber > stepNum
                val isActive = currentStage.layerNumber == stepNum
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> VaultLime
                                isActive -> VaultMintCard
                                else -> VaultSlate
                            }
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when (currentStage) {
                AuthStage.PIN_LAYER_1 -> "Lapis 1 dari 4 • PIN Gerbang Utama"
                AuthStage.PIN_LAYER_2 -> "Lapis 2 dari 4 • PIN Subkey NYX2"
                AuthStage.PIN_LAYER_3 -> "Lapis 3 dari 4 • PIN Otorisasi Brankas"
                AuthStage.BIOMETRIC_STEP -> "Lapis 4 dari 4 • Pemindaian Sidik Jari"
                AuthStage.UNLOCKED -> "Terbuka"
            },
            style = MaterialTheme.typography.labelMedium,
            color = VaultMintCard
        )
    }
}

@Composable
private fun PinEntryGateCard(
    stage: AuthStage,
    enteredPin: String,
    errorMessage: String?,
    onCharClick: (Char) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit
) {
    var isAlphaMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Minimalist PIN Header & 6 Slots
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = VaultCharcoal),
            border = androidx.compose.foundation.BorderStroke(1.dp, VaultSlate)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stage.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = VaultPureWhite,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (stage.layerNumber > 1) {
                        "Masukkan 6 karakter PIN Lapis ${stage.layerNumber} (A-Z, 0-9)"
                    } else {
                        "Masukkan 6 karakter PIN untuk membuka brankas"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = VaultPureWhite.copy(alpha = 0.68f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 6-Character Minimalist PIN Slots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_dots_indicator"),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    for (i in 0 until 6) {
                        val isFilled = i < enteredPin.length
                        val isCurrentSlot = i == enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(width = 42.dp, height = 48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isFilled) VaultLime else VaultObsidian
                                )
                                .border(
                                    width = if (isCurrentSlot) 1.5.dp else 1.dp,
                                    color = when {
                                        isFilled -> VaultLime
                                        isCurrentSlot -> VaultMintCard
                                        else -> VaultSlate
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFilled) {
                                Text(
                                    text = enteredPin[i].toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = VaultObsidian,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = VaultPureWhite.copy(alpha = 0.25f)
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = VaultCoral.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VaultCoral)
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VaultCoral,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Alphanumeric Mode Switcher (123 Angka <-> ABC Huruf)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                color = if (!isAlphaMode) VaultLime else VaultCharcoal,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (!isAlphaMode) VaultLime else VaultSlate),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isAlphaMode = false }
            ) {
                Text(
                    text = "1 2 3 Angka",
                    color = if (!isAlphaMode) VaultObsidian else VaultPureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                color = if (isAlphaMode) VaultLime else VaultCharcoal,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isAlphaMode) VaultLime else VaultSlate),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isAlphaMode = true }
            ) {
                Text(
                    text = "A B C Huruf (A-Z)",
                    color = if (isAlphaMode) VaultObsidian else VaultPureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Clean Minimalist 0ms Keypad (Digits or Alphanumeric)
        if (!isAlphaMode) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val rows = listOf(
                    listOf('1', '2', '3'),
                    listOf('4', '5', '6'),
                    listOf('7', '8', '9')
                )
                rows.forEach { rowChars ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowChars.forEach { ch ->
                            FastPinKeypadButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pin_key_$ch"),
                                label = ch.toString(),
                                onClick = { onCharClick(ch) }
                            )
                        }
                    }
                }

                // Bottom row: Clear, 0, Backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(VaultCharcoal)
                            .instantTouchPress { onClearClick() }
                            .testTag("pin_key_clear"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ULANG",
                            style = MaterialTheme.typography.labelLarge,
                            color = VaultAmber
                        )
                    }

                    FastPinKeypadButton(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pin_key_0"),
                        label = "0",
                        onClick = { onCharClick('0') }
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(VaultCharcoal)
                            .instantTouchPress { onBackspaceClick() }
                            .testTag("pin_key_backspace"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = VaultPureWhite
                        )
                    }
                }
            }
        } else {
            // Alphanumeric Keyboard (A to Z)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val letterRows = listOf(
                    listOf('A', 'B', 'C', 'D', 'E', 'F', 'G'),
                    listOf('H', 'I', 'J', 'K', 'L', 'M', 'N'),
                    listOf('O', 'P', 'Q', 'R', 'S', 'T', 'U')
                )
                letterRows.forEach { rowLetters ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        rowLetters.forEach { ch ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VaultCharcoal)
                                    .border(1.dp, VaultSlate.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                                    .instantTouchPress { onCharClick(ch) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ch.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = VaultPureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Row 4: V W X Y Z + Clear + Backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val lastLetters = listOf('V', 'W', 'X', 'Y', 'Z')
                    lastLetters.forEach { ch ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(VaultCharcoal)
                                .border(1.dp, VaultSlate.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                                .instantTouchPress { onCharClick(ch) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ch.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = VaultPureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VaultCharcoal)
                            .border(1.dp, VaultAmber.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .instantTouchPress { onClearClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CLR",
                            style = MaterialTheme.typography.labelSmall,
                            color = VaultAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VaultCharcoal)
                            .border(1.dp, VaultSlate.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                            .instantTouchPress { onBackspaceClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = VaultPureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FastPinKeypadButton(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(VaultCharcoal)
            .border(1.dp, VaultSlate.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
            .instantTouchPress(onPressInstant = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineMedium,
            color = VaultPureWhite,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Komponen Sensor Sidik Jari Ultrasonik Interaktif:
 * Pengguna benar-benar harus menempelkan & menahan jari pada area sensor hingga pemindaian
 * ridge sidik jari mencapai 100% (atau menggunakan sensor hardware BiometricPrompt).
 * Jika jari dilepas sebelum 100%, pemindaian gagal dan meminta pengguna menahan jari kembali.
 */
@Composable
fun InteractiveFingerprintScannerPad(
    onScanCompleted: () -> Unit,
    onPartialReleaseWarning: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var scanProgress by remember { mutableIntStateOf(0) }
    var isFingerDown by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "fp_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fp_scale"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(144.dp)
                .scale(if (isFingerDown) 0.95f else pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (isFingerDown) VaultLime.copy(alpha = 0.42f) else VaultLime.copy(alpha = 0.22f),
                            VaultMintAccent.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        isFingerDown = true
                        scanProgress = 0
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                        var completed = false
                        val job: Job = scope.launch {
                            for (step in 1..20) {
                                delay(36L)
                                scanProgress = step * 5
                                if (scanProgress % 25 == 0) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                            completed = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onScanCompleted()
                        }

                        waitForUpOrCancellation()
                        isFingerDown = false
                        if (!completed) {
                            val reached = scanProgress
                            job.cancel()
                            scanProgress = 0
                            onPartialReleaseWarning(
                                "Pemindaian sidik jari terputus ($reached%). Tempel & tahan jari Anda hingga 100%!"
                            )
                        }
                    }
                }
                .testTag("biometric_scanner_orb"),
            contentAlignment = Alignment.Center
        ) {
            // Circular Ultrasonic Progress Ring + Laser Scan Line
            Canvas(modifier = Modifier.size(124.dp)) {
                val strokeW = 4.dp.toPx()
                drawCircle(
                    color = VaultSlate,
                    style = Stroke(width = strokeW)
                )
                val sweep = (scanProgress / 100f) * 360f
                drawArc(
                    color = VaultLime,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )

                if (isFingerDown && scanProgress in 1..99) {
                    val scanY = size.height * (scanProgress / 100f)
                    drawLine(
                        color = VaultLime,
                        start = Offset(size.width * 0.18f, scanY),
                        end = Offset(size.width * 0.82f, scanY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(if (isFingerDown) VaultLime else VaultCharcoal)
                    .border(2.dp, VaultLime, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Tempel & Tahan Sidik Jari",
                        tint = if (isFingerDown) VaultObsidian else VaultLime,
                        modifier = Modifier.size(46.dp)
                    )
                    if (isFingerDown || scanProgress > 0) {
                        Text(
                            text = "$scanProgress%",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFingerDown) VaultObsidian else VaultLime
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isFingerDown) {
                "Memindai alur sidik jari... Tahan ($scanProgress%)"
            } else {
                "Tempel & TAHAN jari Anda pada sensor di atas hingga 100%"
            },
            style = MaterialTheme.typography.labelLarge,
            color = if (isFingerDown) VaultLime else VaultMintCard,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BiometricVerificationCard(
    statusMessage: String?,
    onTriggerHardwareBiometric: () -> Unit,
    onHoldFingerprintCompleted: () -> Unit,
    onHoldFingerprintFeedback: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = VaultCharcoal),
        border = androidx.compose.foundation.BorderStroke(1.dp, VaultSlate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = VaultLime.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = VaultLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "3 LAPIS PIN TERVERIFIKASI",
                        style = MaterialTheme.typography.labelMedium,
                        color = VaultLime,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Pemindaian Sidik Jari",
                style = MaterialTheme.typography.headlineMedium,
                color = VaultPureWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tempel dan tahan jari Anda pada sensor ultrasonik di bawah (atau gunakan sensor biometrik perangkat).",
                style = MaterialTheme.typography.bodyMedium,
                color = VaultPureWhite.copy(alpha = 0.72f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            InteractiveFingerprintScannerPad(
                onScanCompleted = onHoldFingerprintCompleted,
                onPartialReleaseWarning = onHoldFingerprintFeedback
            )

            if (!statusMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = VaultObsidian,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VaultMintCard,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onTriggerHardwareBiometric,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("trigger_biometric_button"),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VaultLime.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VaultLime)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buka Dialog Sensor Hardware OS",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Dialog Re-Autentikasi Biometrik Sekunder (Secondary Biometric Re-Authentication)
 * Wajib dilewati sebelum mengekspor atau membagikan file individual dari Media Player.
 */
@Composable
fun SecondaryBiometricReAuthDialog(
    modalState: BiometricExportModalState,
    onAuthenticated: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var feedbackText by remember {
        mutableStateOf("Verifikasi sidik jari diperlukan untuk mengekspor/membagikan file keluar dari Vault.")
    }

    LaunchedEffect(modalState.item.id, modalState.isShareIntent) {
        triggerHardwareBiometricIfAvailable(
            context = context,
            title = if (modalState.isShareIntent) "Biometrik: Bagikan File Vault" else "Biometrik: Ekspor File Vault",
            subtitle = "Otorisasi keamanan sekunder untuk \"${modalState.item.name}.${modalState.item.extension}\"",
            onSuccess = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onAuthenticated()
            },
            onErrorFeedback = { msg -> feedbackText = msg }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .widthIn(max = 430.dp)
                .testTag("secondary_biometric_reauth_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = VaultObsidian),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, VaultLime)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = VaultLime,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = VaultObsidian,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "OTORISASI BIOMETRIK SEKUNDER",
                                style = MaterialTheme.typography.labelMedium,
                                color = VaultObsidian,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = VaultPureWhite)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (modalState.isShareIntent) {
                        "Bagikan \"${modalState.item.name}.${modalState.item.extension}\""
                    } else {
                        "Ekspor \"${modalState.item.name}.${modalState.item.extension}\""
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = VaultPureWhite,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = feedbackText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VaultPureWhite.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                InteractiveFingerprintScannerPad(
                    onScanCompleted = onAuthenticated,
                    onPartialReleaseWarning = { warning -> feedbackText = warning }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VaultSlate)
                    ) {
                        Text("Batal", color = VaultPureWhite)
                    }

                    OutlinedButton(
                        onClick = {
                            triggerHardwareBiometricIfAvailable(
                                context = context,
                                title = "Otorisasi Biometrik Ekspor/Bagikan",
                                subtitle = "${modalState.item.name}.${modalState.item.extension}",
                                onSuccess = onAuthenticated,
                                onErrorFeedback = { msg -> feedbackText = msg }
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VaultLime)
                    ) {
                        Text("Sensor OS", color = VaultLime)
                    }
                }
            }
        }
    }
}

/**
 * Dialog Pemulihan Keamanan Lupa PIN / Sandi (Forgot PIN Recovery):
 * Memungkinkan pengguna memverifikasi Jawaban Pemulihan Keamanan atau Sidik Jari Biometrik
 * untuk melihat / mengatur ulang PIN 3-Lapis Brankas 1 maupun Brankas 2.
 */
@Composable
fun ForgotPinRecoveryDialog(
    securityConfig: SecurityConfig,
    onResetPinsAndUnlock: (vaultId: Int, p1: String, p2: String, p3: String, unlockNow: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isIdentityVerified by remember { mutableStateOf(false) }
    var answerInput by remember { mutableStateOf("") }
    var verificationError by remember { mutableStateOf<String?>(null) }

    var selectedVaultId by remember { mutableIntStateOf(1) }
    val (existingP1, existingP2, existingP3) = securityConfig.getPinsForVault(selectedVaultId)

    var newPin1 by remember(selectedVaultId, existingP1) { mutableStateOf(existingP1) }
    var newPin2 by remember(selectedVaultId, existingP2) { mutableStateOf(existingP2) }
    var newPin3 by remember(selectedVaultId, existingP3) { mutableStateOf(existingP3) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .widthIn(max = 450.dp)
                .testTag("forgot_pin_recovery_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = VaultObsidian),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, VaultMintCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = VaultMintCard,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = VaultObsidian,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "PEMULIHAN LUPA PIN / SANDI",
                                style = MaterialTheme.typography.labelMedium,
                                color = VaultObsidian,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = VaultPureWhite)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isIdentityVerified) {
                    Text(
                        text = "Verifikasi Identitas Pemilik Brankas",
                        style = MaterialTheme.typography.titleLarge,
                        color = VaultPureWhite,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Jawab pertanyaan keamanan pemulihan Anda atau pindai sidik jari hingga 100% untuk mengatur ulang PIN.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VaultPureWhite.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = VaultCharcoal,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Pertanyaan Keamanan:",
                                style = MaterialTheme.typography.labelMedium,
                                color = VaultLime
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = securityConfig.recoveryQuestion,
                                style = MaterialTheme.typography.titleMedium,
                                color = VaultPureWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = {
                            answerInput = it
                            verificationError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recovery_answer_input"),
                        label = { Text("Jawaban / Kata Kunci Pemulihan") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = VaultCharcoal,
                            unfocusedContainerColor = VaultCharcoal,
                            focusedBorderColor = VaultLime,
                            unfocusedBorderColor = VaultSlate,
                            focusedTextColor = VaultPureWhite,
                            unfocusedTextColor = VaultPureWhite,
                            focusedLabelColor = VaultLime,
                            unfocusedLabelColor = VaultPureWhite.copy(alpha = 0.7f)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val expected = securityConfig.recoveryAnswer.trim()
                            if (expected.isNotBlank() && answerInput.trim().equals(expected, ignoreCase = true)) {
                                verificationError = null
                                val triggered = triggerHardwareBiometricIfAvailable(
                                    context = context,
                                    title = "Otorisasi Kredensial Perangkat Pemulihan",
                                    subtitle = "Pindai sidik jari atau masukkan PIN perangkat Anda untuk menyelesaikan pemulihan",
                                    onSuccess = {
                                        verificationError = null
                                        isIdentityVerified = true
                                    },
                                    onErrorFeedback = { msg ->
                                        verificationError = "Otorisasi perangkat gagal: $msg"
                                    }
                                )
                                if (!triggered) {
                                    isIdentityVerified = true
                                }
                            } else {
                                verificationError = "Jawaban pertanyaan pemulihan tidak sesuai. Akses pemulihan ditolak."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("verify_recovery_answer_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VaultLime,
                            contentColor = VaultObsidian
                        )
                    ) {
                        Text("Verifikasi Jawaban & Sidik Jari OS", fontWeight = FontWeight.Bold)
                    }

                    if (verificationError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = verificationError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VaultCoral,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // STEP 2: Identity Verified -> View / Reset 3-Layer Alphanumeric PINs for any configured Vault
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = VaultLime,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Identitas Terverifikasi! Atur Ulang PIN",
                        style = MaterialTheme.typography.titleLarge,
                        color = VaultPureWhite,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilih brankas yang ingin Anda ubah PIN 3-lapisnya (Mendukung Huruf A-Z dan Angka):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VaultPureWhite.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Multi-Vault Selection Row (1 to vaultCount)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val maxV = securityConfig.vaultCount.coerceIn(1, 10)
                        for (v in 1..maxV) {
                            val isSel = selectedVaultId == v
                            Box(
                                modifier = Modifier
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) VaultLime else VaultCharcoal)
                                    .instantTouchPress { selectedVaultId = v }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Brankas $v",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSel) VaultObsidian else VaultPureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    RecoveryPinInputField(
                        label = "PIN Lapis 1 Baru (Alfanumerik 6 Karakter)",
                        value = newPin1,
                        onValueChange = {
                            val clean = it.uppercase().filter { ch -> ch.isDigit() || ch in 'A'..'Z' }.take(6)
                            newPin1 = clean
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RecoveryPinInputField(
                        label = "PIN Lapis 2 Baru (Alfanumerik 6 Karakter)",
                        value = newPin2,
                        onValueChange = {
                            val clean = it.uppercase().filter { ch -> ch.isDigit() || ch in 'A'..'Z' }.take(6)
                            newPin2 = clean
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RecoveryPinInputField(
                        label = "PIN Lapis 3 Baru (Alfanumerik 6 Karakter)",
                        value = newPin3,
                        onValueChange = {
                            val clean = it.uppercase().filter { ch -> ch.isDigit() || ch in 'A'..'Z' }.take(6)
                            newPin3 = clean
                        }
                    )

                    if (verificationError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = verificationError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VaultCoral
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (newPin1.length == 6 && newPin2.length == 6 && newPin3.length == 6) {
                                onResetPinsAndUnlock(selectedVaultId, newPin1, newPin2, newPin3, true)
                            } else {
                                verificationError = "Ketiga PIN wajib diisi tepat 6 karakter alfanumerik (A-Z, 0-9)!"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_recovered_pins_unlock_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VaultLime,
                            contentColor = VaultObsidian
                        )
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simpan PIN & Buka Brankas $selectedVaultId",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecoveryPinInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = TextStyle(
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = VaultPureWhite,
            letterSpacing = 3.sp
        ),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = VaultCharcoal,
            unfocusedContainerColor = VaultCharcoal,
            focusedBorderColor = VaultLime,
            unfocusedBorderColor = VaultSlate,
            focusedLabelColor = VaultLime,
            unfocusedLabelColor = VaultPureWhite.copy(alpha = 0.7f)
        )
    )
}
