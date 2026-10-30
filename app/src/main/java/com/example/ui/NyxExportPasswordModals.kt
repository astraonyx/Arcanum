package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.VaultItemEntity
import com.example.ui.theme.CosmicBorderHairline
import com.example.ui.theme.CosmicGlass
import com.example.ui.theme.CosmicGradientCta
import com.example.ui.theme.CosmicGradientGlassHighlight
import com.example.ui.theme.CosmicObsidian
import com.example.ui.theme.CosmicTextMuted
import com.example.ui.theme.CosmicTextPure
import com.example.ui.theme.CosmicTextSubtle
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelRose
import com.example.ui.theme.SolarGold
import com.example.ui.theme.StarlightCyan
import com.example.ui.theme.VaultCoral
import com.example.ui.theme.VaultObsidian

/**
 * Dialog Penguncian Ekspor .nyx dengan Kata Sandi Pembuat:
 * Mengunci file yang diekspor tetap dalam format terenkripsi .nyx.
 * Siapapun yang menerima file ini wajib memasukkan password yang ditentukan saat impor.
 */
@Composable
fun ExportPasswordDialog(
    item: VaultItemEntity,
    onDismiss: () -> Unit,
    onConfirmPassword: (String) -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("export_password_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicGlass),
            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicGradientGlassHighlight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = StarlightCyan.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = null,
                                    tint = StarlightCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Kunci Ekspor Berkas .nyx",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CosmicTextPure
                            )
                            Text(
                                text = "Format Ekspor Terenkripsi Mandiri",
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicTextSubtle
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = CosmicTextSubtle)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Berkas \"${item.name}.nyx\" akan disegel dengan enkripsi AES-256-GCM. Orang lain yang mengimpor berkas ini di perangkat mereka wajib memasukkan kata sandi ini untuk membukanya.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CosmicTextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Kata Sandi Ekspor") },
                    placeholder = { Text("Buat sandi pembongkar...") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Sandi",
                                tint = CosmicTextSubtle
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicTextPure,
                        unfocusedTextColor = CosmicTextPure,
                        focusedBorderColor = StarlightCyan,
                        unfocusedBorderColor = CosmicBorderHairline
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPasswordInput,
                    onValueChange = { confirmPasswordInput = it },
                    label = { Text("Konfirmasi Kata Sandi") },
                    placeholder = { Text("Ketik ulang sandi...") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicTextPure,
                        unfocusedTextColor = CosmicTextPure,
                        focusedBorderColor = StarlightCyan,
                        unfocusedBorderColor = CosmicBorderHairline
                    )
                )

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = VaultCoral,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
                    ) {
                        Text("Batal", color = CosmicTextPure)
                    }

                    Button(
                        onClick = {
                            when {
                                passwordInput.length < 4 -> {
                                    validationError = "Kata sandi minimal 4 karakter!"
                                }
                                passwordInput != confirmPasswordInput -> {
                                    validationError = "Konfirmasi kata sandi tidak cocok!"
                                }
                                else -> {
                                    validationError = null
                                    onConfirmPassword(passwordInput)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CosmicGradientCta),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = CosmicTextPure
                        )
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = CosmicTextPure, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kunci & Ekspor", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Dialog Pembongkaran Berkas .nyx Terproteksi Sandi:
 * Ditampilkan saat pengguna atau penerima mengimpor berkas .nyx yang disegel kata sandi.
 */
@Composable
fun ImportNyxPasswordDialog(
    fileName: String,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirmPassword: (String) -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("import_nyx_password_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicGlass),
            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicGradientGlassHighlight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SolarGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = SolarGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Buka Berkas .nyx",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CosmicTextPure
                            )
                            Text(
                                text = "Berkas Terproteksi Kata Sandi",
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicTextSubtle
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = CosmicTextSubtle)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Berkas \"$fileName\" dilindungi dengan kata sandi oleh pembuatnya. Masukkan kata sandi untuk mendekripsi dan menyimpannya ke dalam brankas aktif Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CosmicTextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Kata Sandi Berkas .nyx") },
                    placeholder = { Text("Ketik kata sandi pembuka...") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Sandi",
                                tint = CosmicTextSubtle
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicTextPure,
                        unfocusedTextColor = CosmicTextPure,
                        focusedBorderColor = SolarGold,
                        unfocusedBorderColor = CosmicBorderHairline
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = VaultCoral,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
                    ) {
                        Text("Batal", color = CosmicTextPure)
                    }

                    Button(
                        onClick = {
                            if (passwordInput.isNotBlank()) {
                                onConfirmPassword(passwordInput)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SolarGold,
                            contentColor = VaultObsidian
                        )
                    ) {
                        Text("Buka & Impor", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
