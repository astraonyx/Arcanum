package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.io.File
import kotlinx.coroutines.delay
import com.example.data.EncryptionPreset
import com.example.data.EncryptionScriptEngine
import com.example.data.SecurityConfig
import com.example.data.VaultCategory
import com.example.data.VaultItemEntity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VaultAmber
import com.example.ui.theme.VaultCharcoal
import com.example.ui.theme.VaultCoral
import com.example.ui.theme.VaultCream
import com.example.ui.theme.VaultLime
import com.example.ui.theme.VaultMintAccent
import com.example.ui.theme.VaultMintCard
import com.example.ui.theme.VaultObsidian
import com.example.ui.theme.VaultPureWhite
import com.example.ui.theme.VaultSlate
import com.example.ui.theme.CosmicBorderHairline
import com.example.ui.theme.CosmicObsidian
import com.example.ui.theme.CosmicTextMuted
import com.example.ui.theme.CosmicGlass
import com.example.ui.theme.CosmicGradientGlassCard
import com.example.ui.theme.CosmicGradientGlassHighlight
import com.example.ui.theme.CosmicGradientCta
import com.example.ui.theme.CosmicGradientGoldCta
import com.example.ui.theme.CosmicNebulaMesh
import com.example.ui.theme.CosmicTextPure
import com.example.ui.theme.CosmicTextSubtle
import com.example.ui.theme.PastelSky
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.PastelPeach
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelRose
import com.example.ui.theme.PastelYellow
import com.example.ui.theme.SolarGold
import com.example.ui.theme.StarlightCyan
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.VaultTextMuted

@Composable
fun EncryptionScriptEngineScreen(
    activeVaultId: Int,
    securityConfig: SecurityConfig,
    onSaveScriptConfig: (EncryptionPreset, String) -> Unit,
    onSaveVaultPins: (vaultId: Int, String, String, String) -> Unit,
    onAddNewVault: (String, String, String) -> Unit = { _, _, _ -> },
    onSaveRecoveryCredentials: (String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    onToggleBiometricGate: (Boolean) -> Unit,
    onLockVaultNow: () -> Unit,
    onLinkGoogleAccount: (String) -> Unit = {},
    onUnlinkGoogleAccount: () -> Unit = {},
    onBackupToCloud: () -> Unit = {},
    onRestoreFromCloud: (File) -> Unit = {}
) {
    val context = LocalContext.current
    val activePresetInit = EncryptionPreset.entries.find { it.id == securityConfig.activeScriptName }
        ?: EncryptionPreset.NYX2_DUAL_CIPHER

    var selectedPreset by remember(securityConfig.activeScriptName) { mutableStateOf(activePresetInit) }
    var customCodeText by remember(securityConfig.customScriptCode) {
        mutableStateOf(securityConfig.customScriptCode)
    }
    var samplePlainInput by remember {
        mutableStateOf("NYX2_STREAM::4K_Video_And_Secret_Key_0091")
    }

    var editingVaultTab by remember { mutableIntStateOf(activeVaultId) }
    val (currentP1, currentP2, currentP3) = securityConfig.getPinsForVault(editingVaultTab)

    var pin1Input by remember(editingVaultTab, currentP1) { mutableStateOf(currentP1) }
    var pin2Input by remember(editingVaultTab, currentP2) { mutableStateOf(currentP2) }
    var pin3Input by remember(editingVaultTab, currentP3) { mutableStateOf(currentP3) }
    var pinValidationError by remember { mutableStateOf<String?>(null) }

    var showVaultIdentityBanner by remember { mutableStateOf(false) }
    var showCreateVaultDialog by remember { mutableStateOf(false) }
    var showGoogleAccountDialog by remember { mutableStateOf(false) }
    var isRecoveryUnlocked by remember { mutableStateOf(false) }
    var showPasswordAnswers by remember { mutableStateOf(false) }

    LaunchedEffect(showVaultIdentityBanner) {
        if (showVaultIdentityBanner) {
            delay(5000)
            showVaultIdentityBanner = false
        }
    }

    var recoveryQuestion1Input by remember(securityConfig.recoveryQuestion1) {
        mutableStateOf(securityConfig.recoveryQuestion1)
    }
    var recoveryAnswer1Input by remember(securityConfig.recoveryAnswer1) {
        mutableStateOf(securityConfig.recoveryAnswer1)
    }
    var recoveryQuestion2Input by remember(securityConfig.recoveryQuestion2) {
        mutableStateOf(securityConfig.recoveryQuestion2)
    }
    var recoveryAnswer2Input by remember(securityConfig.recoveryAnswer2) {
        mutableStateOf(securityConfig.recoveryAnswer2)
    }
    var recoveryQuestion3Input by remember(securityConfig.recoveryQuestion3) {
        mutableStateOf(securityConfig.recoveryQuestion3)
    }
    var recoveryAnswer3Input by remember(securityConfig.recoveryAnswer3) {
        mutableStateOf(securityConfig.recoveryAnswer3)
    }

    val keySeed = "$currentP1:$currentP2:$currentP3:${customCodeText.hashCode()}"
    val (liveHexOutput, liveChecksum) = remember(samplePlainInput, keySeed, selectedPreset) {
        EncryptionScriptEngine.transformPreview(samplePlainInput, keySeed, selectedPreset)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("script_engine_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "NYX2 Script Engine & Pengaturan Brankas",
                    style = MaterialTheme.typography.headlineLarge,
                    color = CosmicTextPure,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Terintegrasi dengan nyx_crypt_optimized.js (ChaCha20-Poly1305 -> AES-256-GCM + Padmé Padding).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VaultTextMuted
                )
            }
        }

        // 2. Interlocking NYX2 Input/Output Cipher Sandbox
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicGlass),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicGradientGlassHighlight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "INPUT PAYLOAD (PADMÉ PADDED)",
                                style = MaterialTheme.typography.labelLarge,
                                color = CosmicTextPure,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = VaultObsidian,
                                shape = RoundedCornerShape(50)
                            ) {
                                val rawLen = samplePlainInput.toByteArray().size.toLong()
                                val padded = EncryptionScriptEngine.padmeSize(rawLen)
                                Text(
                                    text = "$rawLen B -> Padmé $padded B",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VaultLime,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = samplePlainInput,
                            onValueChange = { samplePlainInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sandbox_plaintext_input"),
                            label = { Text("Uji Payload Sebelum Dienkripsi NYX2") },
                            textStyle = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 13.sp,
                                color = VaultObsidian
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = VaultPureWhite,
                                unfocusedContainerColor = VaultPureWhite,
                                focusedBorderColor = VaultObsidian,
                                unfocusedBorderColor = VaultObsidian.copy(alpha = 0.25f)
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .offset(y = 0.dp)
                            .clip(CircleShape)
                            .background(VaultObsidian)
                            .border(4.dp, VaultCream, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Transformasi NYX2",
                            tint = VaultLime,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultObsidian)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OUTPUT .NYX (MAGIC 4E 59 58 32)",
                                style = MaterialTheme.typography.labelLarge,
                                color = VaultLime
                            )
                            Text(
                                text = liveChecksum,
                                style = MaterialTheme.typography.labelMedium,
                                color = VaultMintCard
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = VaultCharcoal,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = liveHexOutput,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                color = VaultPureWhite,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Stealth Active Vault PIN Configuration (Zero-Knowledge Isolation)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicGlass),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicGradientGlassHighlight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = StarlightCyan.copy(alpha = 0.16f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = StarlightCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Konfigurasi 3-Lapis PIN Ruang Aktif",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CosmicTextPure
                            )
                            Text(
                                text = "Zero-Knowledge: Mengatur sandi untuk ruang brankas yang sedang terbuka.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicTextSubtle
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    PinLayerTextField(
                        label = "PIN Lapis 1 (6 Karakter Huruf A-Z / Angka)",
                        value = pin1Input,
                        onValueChange = { pin1Input = it.uppercase().take(6) },
                        tag = "input_pin_layer_1"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PinLayerTextField(
                        label = "PIN Lapis 2 (6 Karakter Huruf A-Z / Angka)",
                        value = pin2Input,
                        onValueChange = { pin2Input = it.uppercase().take(6) },
                        tag = "input_pin_layer_2"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PinLayerTextField(
                        label = "PIN Lapis 3 (6 Karakter Otorisasi)",
                        value = pin3Input,
                        onValueChange = { pin3Input = it.uppercase().take(6) },
                        tag = "input_pin_layer_3"
                    )

                    if (pinValidationError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pinValidationError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VaultCoral,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (SecurityConfig.isAlphanumericValidPin(pin1Input) &&
                                    SecurityConfig.isAlphanumericValidPin(pin2Input) &&
                                    SecurityConfig.isAlphanumericValidPin(pin3Input)
                                ) {
                                    pinValidationError = null
                                    onSaveVaultPins(editingVaultTab, pin1Input, pin2Input, pin3Input)
                                } else {
                                    pinValidationError = "Semua PIN wajib 6 karakter alfanumerik (huruf kapital A-Z dan angka 0-9)!"
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CosmicGradientCta)
                                .testTag("save_3layer_pins_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                contentColor = CosmicTextPure
                            )
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = CosmicTextPure, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan PIN", style = MaterialTheme.typography.labelLarge, color = CosmicTextPure, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onLockVaultNow,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("test_lock_gate_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PastelRose, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kunci Brankas", style = MaterialTheme.typography.labelLarge, color = PastelRose)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Biometric Toggle Row
                    Surface(
                        color = CosmicObsidian.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline),
                        modifier = Modifier.fillMaxWidth()
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
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Wajibkan Sensor Sidik Jari",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CosmicTextPure
                                    )
                                    Text(
                                        text = "Verifikasi biometrik fisik perangkat untuk akses instan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CosmicTextSubtle
                                    )
                                }
                            }
                            Switch(
                                checked = securityConfig.biometricEnabled,
                                onCheckedChange = onToggleBiometricGate,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StarlightCyan,
                                    checkedTrackColor = CosmicObsidian
                                ),
                                modifier = Modifier.testTag("biometric_toggle_switch")
                            )
                        }
                    }
                }
            }
        }

        // 3B. Dedicated Card: Tambah Ruang Brankas Terisolasi Baru
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicGlass),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicGradientGlassHighlight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PastelMint.copy(alpha = 0.2f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AddModerator,
                                        contentDescription = null,
                                        tint = PastelMint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Tambah Ruang Brankas Baru",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CosmicTextPure
                                )
                                Text(
                                    text = "Total brankas terdaftar: ${securityConfig.vaultCount} (Maksimal 10)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CosmicTextSubtle
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Setiap brankas baru terisolasi mandiri dengan PIN 3-lapis baru tanpa saling mengetahui isi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CosmicTextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showCreateVaultDialog = true },
                        enabled = securityConfig.vaultCount < 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("add_new_vault_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PastelMint,
                            contentColor = VaultObsidian
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (securityConfig.vaultCount < 10) "+ Buat Ruang Brankas Baru" else "Maksimal 10 Brankas Tercapai",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // 3B. Pencadangan Google Cloud Terenkripsi (Manual AES-256)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicGlass),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicGradientGlassHighlight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PastelSky.copy(alpha = 0.22f))
                                .border(1.dp, PastelSky.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = PastelSky,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Pencadangan Google Cloud Terenkripsi",
                            style = MaterialTheme.typography.titleLarge,
                            color = CosmicTextPure,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Cadangkan file dan database brankas ke paket arsip terenkripsi AES-256-GCM (.nyxpkg). Pencadangan ini bersifat manual murni tanpa sinkronisasi otomatis latar belakang.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VaultTextMuted
                    )

                    Surface(
                        color = VaultCream,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = if (securityConfig.isGoogleLinked) VaultLime else VaultObsidian,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = if (securityConfig.isGoogleLinked) "Akun Google Terhubung" else "Belum Tertaut Akun Google",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CosmicTextPure,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = securityConfig.googleAccountEmail.ifBlank { "Tautkan akun Google untuk backup/restore cloud" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VaultTextMuted
                                    )
                                }
                            }

                            if (securityConfig.isGoogleLinked) {
                                OutlinedButton(
                                    onClick = onUnlinkGoogleAccount,
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, VaultCoral)
                                ) {
                                    Text("Putuskan", color = VaultCoral, style = MaterialTheme.typography.labelMedium)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        try {
                                            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                                                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                                            ).requestEmail().requestProfile().build()
                                            val client = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                                            val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
                                            if (account != null && !account.email.isNullOrBlank()) {
                                                onLinkGoogleAccount(account.email ?: "")
                                            } else {
                                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.google.com/"))
                                                context.startActivity(webIntent)
                                            }
                                        } catch (_: Exception) {
                                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.google.com/"))
                                            context.startActivity(webIntent)
                                        }
                                    },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CosmicGradientCta),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        contentColor = CosmicTextPure
                                    )
                                ) {
                                    Text("Tautkan Otomatis", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CosmicTextPure)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onBackupToCloud,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CosmicGradientCta),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = CosmicTextPure
                        )
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = CosmicTextPure, modifier = Modifier.size(19.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cadangkan Brankas ke Google Cloud", style = MaterialTheme.typography.titleSmall, color = CosmicTextPure, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3B. Forgot PIN / Password Security Recovery Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicGlass),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicGradientGlassHighlight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SolarGold.copy(alpha = 0.22f))
                                .border(1.dp, SolarGold.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = SolarGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Pemulihan 3 Pertanyaan Keamanan",
                                style = MaterialTheme.typography.titleLarge,
                                color = CosmicTextPure,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kunci darurat reset PIN 3 Lapis saat lupa sandi",
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicTextSubtle
                            )
                        }
                    }

                    if (!isRecoveryUnlocked) {
                        // Shield: Protected recovery credentials
                        Surface(
                            color = CosmicObsidian.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SolarGold.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SolarGold.copy(alpha = 0.2f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = SolarGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Kredensial Pemulihan Dilindungi",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CosmicTextPure
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Jawaban pertanyaan keamanan disembunyikan demi mencegah kebocoran saat layar dilihat orang lain.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CosmicTextSubtle,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        triggerHardwareBiometricIfAvailable(
                                            context = context,
                                            title = "Buka Kunci Pertanyaan Pemulihan",
                                            subtitle = "Verifikasi sidik jari Anda untuk melihat & mengubah jawaban keamanan",
                                            onSuccess = { isRecoveryUnlocked = true },
                                            onErrorFeedback = { isRecoveryUnlocked = true }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("unlock_recovery_credentials_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SolarGold,
                                        contentColor = VaultObsidian
                                    )
                                ) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Buka Kunci Jawaban (Verifikasi Sensor)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Unlocked: Masked with PasswordVisualTransformation and toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Jawaban Terbuka (Dapat Diedit)",
                                style = MaterialTheme.typography.labelMedium,
                                color = SolarGold,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { showPasswordAnswers = !showPasswordAnswers },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showPasswordAnswers) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Jawaban",
                                        tint = CosmicTextSubtle
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showPasswordAnswers) "Sembunyikan" else "Perlihatkan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CosmicTextSubtle
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                OutlinedButton(
                                    onClick = { isRecoveryUnlocked = false },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Kunci", fontSize = 11.sp, color = CosmicTextPure)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Pertanyaan 1
                        OutlinedTextField(
                            value = recoveryQuestion1Input,
                            onValueChange = { recoveryQuestion1Input = it },
                            label = { Text("Pertanyaan Pemulihan 1") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("settings_recovery_question_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = recoveryAnswer1Input,
                            onValueChange = { recoveryAnswer1Input = it },
                            label = { Text("Jawaban Rahasia 1") },
                            placeholder = { Text("Ketik jawaban 1...") },
                            singleLine = true,
                            visualTransformation = if (showPasswordAnswers) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("settings_recovery_answer_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Pertanyaan 2
                        OutlinedTextField(
                            value = recoveryQuestion2Input,
                            onValueChange = { recoveryQuestion2Input = it },
                            label = { Text("Pertanyaan Pemulihan 2") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = recoveryAnswer2Input,
                            onValueChange = { recoveryAnswer2Input = it },
                            label = { Text("Jawaban Rahasia 2") },
                            placeholder = { Text("Ketik jawaban 2...") },
                            singleLine = true,
                            visualTransformation = if (showPasswordAnswers) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Pertanyaan 3
                        OutlinedTextField(
                            value = recoveryQuestion3Input,
                            onValueChange = { recoveryQuestion3Input = it },
                            label = { Text("Pertanyaan Pemulihan 3") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = recoveryAnswer3Input,
                            onValueChange = { recoveryAnswer3Input = it },
                            label = { Text("Jawaban Rahasia 3") },
                            placeholder = { Text("Ketik jawaban 3...") },
                            singleLine = true,
                            visualTransformation = if (showPasswordAnswers) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                onSaveRecoveryCredentials(
                                    recoveryQuestion1Input, recoveryAnswer1Input,
                                    recoveryQuestion2Input, recoveryAnswer2Input,
                                    recoveryQuestion3Input, recoveryAnswer3Input
                                )
                                isRecoveryUnlocked = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CosmicGradientGoldCta)
                                .testTag("save_recovery_credentials_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                contentColor = CosmicTextPure
                            )
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = CosmicTextPure, modifier = Modifier.size(19.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simpan 3 Pertanyaan Pemulihan", style = MaterialTheme.typography.titleSmall, color = CosmicTextPure, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. NYX2 Custom Script Preset Selector & Editor
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicGlass),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicGradientGlassHighlight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PastelLavender.copy(alpha = 0.22f))
                                .border(1.dp, PastelLavender.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = PastelLavender,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Profil Enkripsi NYX2 (nyx_crypt_optimized.js)",
                                style = MaterialTheme.typography.titleLarge,
                                color = CosmicTextPure,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ChaCha20-Poly1305 -> AES-256-GCM + Padmé",
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicTextSubtle
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EncryptionPreset.entries.forEach { preset ->
                            val isSelected = selectedPreset == preset
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) PastelLavender.copy(alpha = 0.25f) else androidx.compose.ui.graphics.Color(0x221E293B))
                                    .border(1.dp, if (isSelected) PastelLavender else CosmicBorderHairline, RoundedCornerShape(16.dp))
                                    .clickable {
                                        selectedPreset = preset
                                        customCodeText = preset.scriptTemplate
                                    }
                                    .padding(12.dp)
                                    .testTag("preset_option_${preset.name}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isSelected) PastelLavender else CosmicTextPure,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = preset.subtitle,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) VaultPureWhite.copy(alpha = 0.75f) else VaultTextMuted
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Aktif",
                                            tint = VaultLime
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customCodeText,
                        onValueChange = { customCodeText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                            .testTag("custom_script_code_editor"),
                        label = { Text("Pipeline Kriptografi NYX2") },
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = VaultPureWhite
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = VaultObsidian,
                            unfocusedContainerColor = VaultObsidian,
                            focusedLabelColor = VaultLime,
                            unfocusedLabelColor = VaultLime.copy(alpha = 0.7f),
                            focusedBorderColor = VaultLime,
                            unfocusedBorderColor = VaultSlate
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onSaveScriptConfig(selectedPreset, customCodeText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_script_config_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VaultObsidian,
                            contentColor = VaultLime
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Konfigurasi NYX2", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }

    if (showCreateVaultDialog) {
        CreateNewVaultModalDialog(
            nextVaultId = securityConfig.vaultCount + 1,
            onDismiss = { showCreateVaultDialog = false },
            onCreateVault = { p1, p2, p3 ->
                showCreateVaultDialog = false
                onAddNewVault(p1, p2, p3)
            }
        )
    }

    if (showGoogleAccountDialog) {
        LinkGoogleAccountModalDialog(
            currentEmail = securityConfig.googleAccountEmail,
            onDismiss = { showGoogleAccountDialog = false },
            onLinkAccount = { email ->
                showGoogleAccountDialog = false
                onLinkGoogleAccount(email)
            }
        )
    }
}

@Composable
private fun PinLayerTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    tag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val clean = input.uppercase().filter { it.isDigit() || (it in 'A'..'Z') }
            if (clean.length <= 6) {
                onValueChange(clean)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        textStyle = TextStyle(
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        ),
        shape = RoundedCornerShape(14.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultFileInspectorSheet(
    item: VaultItemEntity,
    onDismiss: () -> Unit,
    onToggleEncryptWithPin: () -> Unit,
    onOpenOrPlayMedia: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryEnum = VaultCategory.entries.find { it.name == item.category } ?: VaultCategory.DOCUMENT

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = VaultObsidian,
        contentColor = VaultPureWhite,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (item.isEncrypted) VaultLime else VaultMintCard,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (item.isEncrypted) "NYX2 SEALED (${item.cipherAlgorithm})" else "TERDEKRIPSI SIAP BUKA",
                        style = MaterialTheme.typography.labelMedium,
                        color = VaultObsidian,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = VaultPureWhite)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${item.name}.${item.extension}",
                style = MaterialTheme.typography.headlineMedium,
                color = VaultPureWhite
            )
            Text(
                text = "Ruang Terisolasi • ${categoryEnum.label} • Ukuran: ${formatFileSize(item.sizeBytes)} • ${item.checksumPreview}",
                style = MaterialTheme.typography.labelMedium,
                color = VaultLime
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = VaultCharcoal,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "HEADER 77-BYTE NYX2 & CIPHERTEXT STREAM:",
                        style = MaterialTheme.typography.labelMedium,
                        color = VaultMintCard
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.encryptedHexPreview,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        color = VaultPureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = VaultCharcoal,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (item.isEncrypted) "PROTEKSI DECOY EMPTY_ZIP AKTIF:" else "ISI FILE ASLI (TERDEKRIPSI):",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (item.isEncrypted) VaultAmber else VaultLime
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (item.isEncrypted) {
                            "Terkunci dengan ChaCha20-Poly1305 -> AES-256-GCM.\nJika dibuka paksa tanpa PIN yang benar, sistem akan menghasilkan EMPTY_ZIP (${EncryptionScriptEngine.EMPTY_ZIP_HEX})."
                        } else {
                            item.rawContentPreview
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        color = VaultPureWhite.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onToggleEncryptWithPin()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sheet_toggle_encrypt_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VaultLime,
                        contentColor = VaultObsidian
                    )
                ) {
                    Icon(
                        imageVector = if (item.isEncrypted) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (item.isEncrypted) "PIN Dekripsi" else "PIN Enkripsi",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = {
                        onDismiss()
                        onOpenOrPlayMedia()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sheet_play_media_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VaultMintAccent,
                        contentColor = VaultPureWhite
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka / Tonton", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }
    }
}

@Composable
private fun CreateNewVaultModalDialog(
    nextVaultId: Int,
    onDismiss: () -> Unit,
    onCreateVault: (pin1: String, pin2: String, pin3: String) -> Unit
) {
    var p1 by remember { mutableStateOf("") }
    var p2 by remember { mutableStateOf("") }
    var p3 by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicGlass),
            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicGradientGlassHighlight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tambah Ruang Brankas Baru",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTextPure
                )
                Text(
                    text = "Masukkan 3 lapis PIN alfanumerik (6 karakter huruf besar A-Z dan angka 0-9) untuk brankas baru ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CosmicTextSubtle
                )

                PinLayerTextField(
                    label = "PIN Lapis 1 (6 Karakter)",
                    value = p1,
                    onValueChange = { p1 = it.uppercase().take(6) },
                    tag = "new_vault_pin1"
                )
                PinLayerTextField(
                    label = "PIN Lapis 2 (6 Karakter)",
                    value = p2,
                    onValueChange = { p2 = it.uppercase().take(6) },
                    tag = "new_vault_pin2"
                )
                PinLayerTextField(
                    label = "PIN Lapis 3 (6 Karakter)",
                    value = p3,
                    onValueChange = { p3 = it.uppercase().take(6) },
                    tag = "new_vault_pin3"
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = VaultCoral,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            val valid = SecurityConfig.isAlphanumericValidPin(p1) &&
                                    SecurityConfig.isAlphanumericValidPin(p2) &&
                                    SecurityConfig.isAlphanumericValidPin(p3)
                            if (valid) {
                                onCreateVault(p1, p2, p3)
                            } else {
                                errorMsg = "Semua PIN wajib 6 karakter huruf kapital (A-Z) atau angka (0-9)!"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VaultLime,
                            contentColor = VaultObsidian
                        )
                    ) {
                        Text("Buat Brankas")
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkGoogleAccountModalDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onLinkAccount: (email: String) -> Unit
) {
    var emailInput by remember { mutableStateOf(currentEmail.ifBlank { "" }) }
    var err by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = VaultPureWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tautkan Akun Google",
                    style = MaterialTheme.typography.titleLarge,
                    color = VaultObsidian
                )
                Text(
                    text = "Masukkan alamat email akun Google Anda untuk tujuan pencadangan terenkripsi manual ke Google Drive.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VaultTextMuted
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email Google (e.g. user@gmail.com)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                if (err != null) {
                    Text(text = err!!, color = VaultCoral, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            if (emailInput.contains("@") && emailInput.contains(".")) {
                                onLinkAccount(emailInput.trim())
                            } else {
                                err = "Format email tidak valid!"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VaultObsidian,
                            contentColor = VaultLime
                        )
                    ) {
                        Text("Tautkan")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateVaultFileDialog(
    onDismiss: () -> Unit,
    onCreateFile: (title: String, ext: String, category: VaultCategory, payload: String, encryptNow: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var extension by remember { mutableStateOf("mp4") }
    var selectedCategory by remember { mutableStateOf(VaultCategory.VIDEO) }
    var payload by remember { mutableStateOf("") }
    var encryptImmediately by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = VaultPureWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "Buat File / Media Demo Baru",
                    style = MaterialTheme.typography.titleLarge,
                    color = VaultObsidian
                )
                Text(
                    text = "Pilih format MP4, MOV, JPG, PNG, WEBP, GIF, Sticker, atau Dokumen NYX.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VaultTextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_file_name_input"),
                        label = { Text("Nama File") },
                        placeholder = { Text("Demo_Clip_4K") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = extension,
                        onValueChange = { extension = it },
                        modifier = Modifier.width(96.dp),
                        label = { Text("Format") },
                        placeholder = { Text("mp4") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple(VaultCategory.VIDEO, "Video", "mp4"),
                        Triple(VaultCategory.IMAGE, "GIF/Img", "gif"),
                        Triple(VaultCategory.IMAGE, "Sticker", "sticker"),
                        Triple(VaultCategory.AUDIO, "Audio", "flac")
                    ).forEach { (cat, shortLabel, defaultExt) ->
                        FilterChip(
                            selected = extension == defaultExt,
                            onClick = {
                                selectedCategory = cat
                                extension = defaultExt
                            },
                            label = { Text(shortLabel, fontSize = 11.sp) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VaultObsidian,
                                selectedLabelColor = VaultLime
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = payload,
                    onValueChange = { payload = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .testTag("dialog_file_payload_input"),
                    label = { Text("Metadata / Isi Rahasia") },
                    placeholder = { Text("Stream 4K HDR atau catatan rahasia...") },
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Kunci Enkripsi NYX2 Saat Disimpan",
                        style = MaterialTheme.typography.labelLarge,
                        color = VaultObsidian
                    )
                    Switch(
                        checked = encryptImmediately,
                        onCheckedChange = { encryptImmediately = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VaultLime,
                            checkedTrackColor = VaultObsidian
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Batal", color = VaultObsidian)
                    }
                    Button(
                        onClick = {
                            onCreateFile(title, extension, selectedCategory, payload, encryptImmediately)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_confirm_save_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VaultObsidian,
                            contentColor = VaultLime
                        )
                    ) {
                        Text("Simpan File", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
