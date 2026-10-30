package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EncryptionScriptEngine
import com.example.data.SecurityConfig
import com.example.data.VaultCategory
import com.example.data.VaultItemEntity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VaultCharcoal
import com.example.ui.theme.VaultCoral
import com.example.ui.theme.VaultCream
import com.example.ui.theme.VaultLime
import com.example.ui.theme.VaultMintAccent
import com.example.ui.theme.VaultMintCard
import com.example.ui.theme.VaultObsidian
import com.example.ui.theme.VaultPureWhite
import com.example.ui.theme.CosmicBorderHairline
import com.example.ui.theme.CosmicGlass
import com.example.ui.theme.CosmicGradientGlassCard
import com.example.ui.theme.CosmicGradientGlassHighlight
import com.example.ui.theme.CosmicGradientCta
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
fun VaultDashboardAndFilesContent(
    activeVaultId: Int,
    isFilesOnlyMode: Boolean,
    allItems: List<VaultItemEntity>,
    filteredItems: List<VaultItemEntity>,
    selectedCategory: VaultCategory?,
    searchQuery: String,
    securityConfig: SecurityConfig,
    onSelectCategory: (VaultCategory?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleEncryptItemWithPin: (VaultItemEntity) -> Unit,
    onBatchEncryptAllWithPin: (Boolean) -> Unit,
    onInspectItem: (VaultItemEntity) -> Unit,
    onOpenOrPlayItem: (VaultItemEntity) -> Unit,
    onDeleteItem: (VaultItemEntity) -> Unit,
    onImportFromDeviceClick: () -> Unit,
    onCreateSecretFileClick: () -> Unit,
    onOpenScriptEngineClick: () -> Unit,
    onSwitchVaultModeClick: (Int) -> Unit,
    onLockVaultClick: () -> Unit,
    onOpenZipScannerToolClick: () -> Unit = {},
    onOpenMoriDownloaderClick: () -> Unit = {}
) {
    val totalCount = allItems.size
    val encryptedCount = remember(allItems) { allItems.count { it.isEncrypted } }
    val decryptedCount = totalCount - encryptedCount
    val totalBytes = remember(allItems) { allItems.sumOf { it.sizeBytes } }
    val formattedTotalSize = remember(totalBytes) { formatFileSize(totalBytes) }

    val (p1, p2, p3) = securityConfig.getPinsForVault(activeVaultId)
    val keyFingerprint = remember(p1, p2, p3) {
        EncryptionScriptEngine.compute3LayerKeyFingerprint(p1, p2, p3)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(if (isFilesOnlyMode) "files_screen_list" else "dashboard_screen_list"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Minimalist Top Header (Mascot + Vault Badge + Switch/Lock Buttons)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    NyxMascotAvatar(
                        mood = MascotMood.HAPPY_VIBES,
                        size = 42.dp
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Arcanum Vault",
                                style = MaterialTheme.typography.headlineMedium,
                                color = CosmicTextPure,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = PastelMint.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PastelMint.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "TERENKRIPSI",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PastelMint,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = "$keyFingerprint • AES-256-GCM Private Space",
                            style = MaterialTheme.typography.labelMedium,
                            color = CosmicTextSubtle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .instantTouchPress { onLockVaultClick() }
                            .testTag("lock_vault_top_button"),
                        color = CosmicGlass,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PastelRose.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Kunci Brankas",
                                tint = PastelRose,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Kunci",
                                style = MaterialTheme.typography.labelLarge,
                                color = CosmicTextPure
                            )
                        }
                    }
                }
            }
        }

        // 1B. Duolingo-Style Animated Mascot & Speech Bubble Banner
        item {
            NyxMascotCompanionBanner(
                activeVaultId = activeVaultId,
                headline = if (totalCount == 0) {
                    "Halo! Ruang rahasiamu aman terlindungi enkripsi AES-256-GCM."
                } else {
                    "$encryptedCount dari $totalCount file aman terkunci (.NYX)!"
                },
                subtitle = "Ketuk maskot untuk animasi & interaksi kosmik",
                mood = MascotMood.WAVE_HELLO,
                onQuickActionClick = onOpenMoriDownloaderClick,
                quickActionLabel = "Mori Link"
            )
        }

        // 2. Minimalist Single Overview Card (Replacing Cluttered Multi-Card Stacks)
        if (!isFilesOnlyMode) {
            item {
                MinimalistVaultOverviewCard(
                    activeVaultId = activeVaultId,
                    formattedTotalSize = formattedTotalSize,
                    encryptedCount = encryptedCount,
                    decryptedCount = decryptedCount,
                    totalCount = totalCount,
                    onEncryptAllClick = { onBatchEncryptAllWithPin(true) },
                    onDecryptAllClick = { onBatchEncryptAllWithPin(false) },
                    onImportFileClick = onImportFromDeviceClick,
                    onCreateFileClick = onCreateSecretFileClick,
                    onOpenZipScannerToolClick = onOpenZipScannerToolClick,
                    onOpenMoriDownloaderClick = onOpenMoriDownloaderClick,
                    onOpenScriptEngineClick = onOpenScriptEngineClick
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onImportFromDeviceClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("files_import_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VaultObsidian,
                            contentColor = VaultLime
                        )
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Impor File / ZIP", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = onCreateSecretFileClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("files_create_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VaultLime,
                            contentColor = VaultObsidian
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Catatan Baru", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // 3. Clean Search & Filter Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_search_input"),
                    placeholder = {
                        Text(
                            "Cari nama file, video, foto, atau paket ZIP...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Cari", tint = StarlightCyan)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus pencarian")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CosmicGlass,
                        unfocusedContainerColor = CosmicGlass,
                        focusedBorderColor = StarlightCyan,
                        unfocusedBorderColor = CosmicBorderHairline,
                        focusedTextColor = CosmicTextPure,
                        unfocusedTextColor = CosmicTextPure
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onSelectCategory(null) },
                        label = { Text("Semua ($totalCount)", fontWeight = FontWeight.Bold) },
                        leadingIcon = if (selectedCategory == null) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp)) }
                        } else null,
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StarlightCyan.copy(alpha = 0.28f),
                            selectedLabelColor = StarlightCyan,
                            selectedLeadingIconColor = StarlightCyan,
                            containerColor = CosmicGlass,
                            labelColor = CosmicTextSubtle
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedCategory == null) StarlightCyan.copy(alpha = 0.6f) else CosmicBorderHairline
                        )
                    )

                    VaultCategory.entries.forEach { cat ->
                        val count = allItems.count { it.category == cat.name }
                        val isSelected = selectedCategory == cat
                        val catColor = when (cat) {
                            VaultCategory.VIDEO -> PastelSky
                            VaultCategory.AUDIO -> PastelLavender
                            VaultCategory.IMAGE -> PastelRose
                            VaultCategory.DOCUMENT -> SolarGold
                            VaultCategory.ARCHIVE -> PastelMint
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectCategory(if (isSelected) null else cat) },
                            label = { Text("${cat.label} ($count)", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = catColor.copy(alpha = 0.28f),
                                selectedLabelColor = catColor,
                                containerColor = CosmicGlass,
                                labelColor = CosmicTextSubtle
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) catColor.copy(alpha = 0.6f) else CosmicBorderHairline
                            )
                        )
                    }
                }
            }
        }

        // 4. Minimalist Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar File (${filteredItems.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CosmicTextPure
                )
                Text(
                    text = "Ketuk untuk membuka",
                    style = MaterialTheme.typography.labelMedium,
                    color = VaultTextMuted
                )
            }
        }

        // 5. Empty State or Clean File List
        if (filteredItems.isEmpty()) {
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
                            .padding(26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CosmicDragonAvatar(
                            mood = MascotMood.HAPPY_VIBES,
                            size = 96.dp,
                            persona = MascotPersona.KAWAII_FOLDER
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (allItems.isEmpty()) {
                                "Brankas Masih Kosong"
                            } else {
                                "Tidak Ada File yang Cocok"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = CosmicTextPure,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Simpan foto, video, dokumen, atau arsip .ZIP dengan enkripsi NYX2 kelas antariksa.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CosmicTextSubtle,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onImportFromDeviceClick,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(CosmicGradientCta)
                                .testTag("empty_state_import_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = CosmicTextPure
                            )
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = CosmicTextPure, modifier = Modifier.size(19.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Impor File / ZIP", style = MaterialTheme.typography.titleSmall, color = CosmicTextPure, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                VaultFileCard(
                    item = item,
                    onToggleEncryptWithPin = { onToggleEncryptItemWithPin(item) },
                    onInspect = { onInspectItem(item) },
                    onOpenOrPlay = { onOpenOrPlayItem(item) },
                    onDelete = { onDeleteItem(item) }
                )
            }
        }
    }
}

@Composable
private fun MinimalistVaultOverviewCard(
    activeVaultId: Int,
    formattedTotalSize: String,
    encryptedCount: Int,
    decryptedCount: Int,
    totalCount: Int,
    onEncryptAllClick: () -> Unit,
    onDecryptAllClick: () -> Unit,
    onImportFileClick: () -> Unit,
    onCreateFileClick: () -> Unit,
    onOpenZipScannerToolClick: () -> Unit,
    onOpenMoriDownloaderClick: () -> Unit,
    onOpenScriptEngineClick: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row: Storage Size + Stat Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Penyimpanan Terenkripsi",
                        style = MaterialTheme.typography.labelMedium,
                        color = CosmicTextSubtle
                    )
                    Text(
                        text = formattedTotalSize,
                        style = MaterialTheme.typography.displayMedium,
                        color = CosmicTextPure,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = PastelRose.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PastelRose.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$encryptedCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PastelRose
                            )
                            Text(
                                text = "Terkunci",
                                fontSize = 10.sp,
                                color = PastelRose,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        color = PastelMint.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PastelMint.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$decryptedCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PastelMint
                            )
                            Text(
                                text = "Terbuka",
                                fontSize = 10.sp,
                                color = PastelMint,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Primary Import CTA + Secret Note CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onImportFileClick,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CosmicGradientCta)
                        .testTag("dashboard_import_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = CosmicTextPure
                    )
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = CosmicTextPure, modifier = Modifier.size(19.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Impor File / ZIP", style = MaterialTheme.typography.titleSmall, color = CosmicTextPure, fontWeight = FontWeight.Bold)
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .instantTouchPress { onCreateFileClick() }
                        .testTag("dashboard_create_file_button"),
                    color = androidx.compose.ui.graphics.Color(0x3338BDF8),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StarlightCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PastelSky, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Catatan", style = MaterialTheme.typography.titleSmall, color = PastelSky, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick Actions Row: Kunci Semua | Buka Semua | ZIP Scanner | Mori
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.ui.graphics.Color(0x26F43F5E))
                        .border(1.dp, PastelRose.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .instantTouchPress { onEncryptAllClick() }
                        .testTag("hero_encrypt_all_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = PastelRose,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Kunci",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PastelRose
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.ui.graphics.Color(0x2610B981))
                        .border(1.dp, PastelMint.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .instantTouchPress { onDecryptAllClick() }
                        .testTag("hero_decrypt_all_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = PastelMint,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Buka",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PastelMint
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.ui.graphics.Color(0x26F59E0B))
                        .border(1.dp, SolarGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .instantTouchPress { onOpenZipScannerToolClick() }
                        .testTag("dashboard_open_zip_scanner_tool_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            tint = SolarGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "ZIP Scan",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SolarGold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.ui.graphics.Color(0x2606B6D4))
                        .border(1.dp, StarlightCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .instantTouchPress { onOpenMoriDownloaderClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = StarlightCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Mori",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = StarlightCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultFileCard(
    item: VaultItemEntity,
    onToggleEncryptWithPin: () -> Unit,
    onInspect: () -> Unit,
    onOpenOrPlay: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryEnum = VaultCategory.entries.find { it.name == item.category } ?: VaultCategory.DOCUMENT
    val openActionLabel = when (categoryEnum) {
        VaultCategory.VIDEO -> "Tonton"
        VaultCategory.IMAGE -> "Lihat"
        VaultCategory.AUDIO -> "Putar"
        else -> "Buka"
    }

    val catColor = when (categoryEnum) {
        VaultCategory.VIDEO -> PastelSky
        VaultCategory.AUDIO -> PastelLavender
        VaultCategory.IMAGE -> PastelRose
        VaultCategory.DOCUMENT -> SolarGold
        VaultCategory.ARCHIVE -> PastelMint
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fastZeroLatencyClick { onOpenOrPlay() }
            .testTag("vault_file_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicGlass),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (item.isEncrypted) CosmicBorderHairline else StarlightCyan.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Category Icon Badge in Colorful Squircle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(catColor.copy(alpha = 0.22f))
                    .border(1.dp, catColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(categoryEnum),
                    contentDescription = categoryEnum.label,
                    tint = catColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Middle File Title & Minimalist Metadata
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.name}.${item.extension}",
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicTextPure,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = formatFileSize(item.sizeBytes),
                        style = MaterialTheme.typography.labelMedium,
                        color = CosmicTextSubtle
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelMedium,
                        color = CosmicTextSubtle
                    )
                    Text(
                        text = if (item.isEncrypted) "Terkunci NYX2" else "Terbuka",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isEncrypted) PastelRose else PastelMint
                    )
                    if (!item.groupArchiveName.isNullOrBlank()) {
                        Text(
                            text = "• ZIP",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PastelPeach
                        )
                    }
                }
            }

            // Right Clean Actions: Open/Play + Lock/Unlock + Info + Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (item.isEncrypted) androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color(0x3338BDF8)) else CosmicGradientCta)
                        .instantTouchPress { onOpenOrPlay() }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("open_or_play_file_${item.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (categoryEnum == VaultCategory.VIDEO || categoryEnum == VaultCategory.AUDIO) {
                                Icons.Default.PlayArrow
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = null,
                            tint = CosmicTextPure,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = openActionLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CosmicTextPure
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color(0x331E293B))
                        .border(1.dp, (if (item.isEncrypted) PastelMint else PastelRose).copy(alpha = 0.4f), CircleShape)
                        .instantTouchPress { onToggleEncryptWithPin() }
                        .testTag("toggle_encrypt_file_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isEncrypted) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = if (item.isEncrypted) "Dekripsi dengan PIN" else "Enkripsi dengan PIN",
                        tint = if (item.isEncrypted) PastelMint else PastelRose,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color(0x331E293B))
                        .border(1.dp, CosmicBorderHairline, CircleShape)
                        .instantTouchPress { onInspect() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Detail File",
                        tint = CosmicTextSubtle,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PastelRose.copy(alpha = 0.2f))
                        .border(1.dp, PastelRose.copy(alpha = 0.4f), CircleShape)
                        .instantTouchPress { onDelete() }
                        .testTag("delete_file_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus File",
                        tint = PastelRose,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

fun categoryIcon(category: VaultCategory): ImageVector {
    return when (category) {
        VaultCategory.IMAGE -> Icons.Default.Image
        VaultCategory.VIDEO -> Icons.Default.VideoFile
        VaultCategory.AUDIO -> Icons.Default.AudioFile
        VaultCategory.DOCUMENT -> Icons.Default.Description
        VaultCategory.ARCHIVE -> Icons.Default.FolderZip
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(java.util.Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024.0) return String.format(java.util.Locale.US, "%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format(java.util.Locale.US, "%.2f GB", gb)
}
