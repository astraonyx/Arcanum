package com.example.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.BatchZipScanReport
import com.example.data.StagedFileStatus
import com.example.data.StagedScanItem
import com.example.data.VaultCategory
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VaultAmber
import com.example.ui.theme.VaultCharcoal
import com.example.ui.theme.VaultCoral
import com.example.ui.theme.VaultCream
import com.example.ui.theme.VaultLavender
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
import com.example.ui.theme.VaultSlate
import com.example.ui.theme.VaultTextMuted
import java.io.File

data class ZipScannerUiState(
    val isScanning: Boolean = false,
    val progressMessage: String = "",
    val currentReport: BatchZipScanReport? = null,
    val showVirusWarningModal: Boolean = false
)

@Composable
fun ZipScannerToolScreen(
    activeVaultId: Int,
    scannerState: ZipScannerUiState,
    onPickMultipleZipsOrFilesClick: () -> Unit,
    onRunSimulatedMultiZipTestClick: () -> Unit,
    onCommitCleanOnlyToVault: () -> Unit,
    onCommitAllIncludingInfectedToVault: () -> Unit,
    onOpenVirusWarningModal: () -> Unit,
    onDismissVirusWarningModal: () -> Unit,
    onClearScanReport: () -> Unit
) {
    val report = scannerState.currentReport
    var selectedFilterTab by remember { mutableIntStateOf(0) }

    val displayedItems = remember(report, selectedFilterTab) {
        if (report == null) emptyList()
        else when (selectedFilterTab) {
            1 -> report.readyCleanItems
            2 -> report.duplicateRemovedItems
            3 -> report.infectedItems
            else -> report.items
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("zip_scanner_tool_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header dengan Maskot Nyx Guardian
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    NyxMascotAvatar(
                        mood = when {
                            report != null && report.virusFlaggedCount > 0 -> MascotMood.STEALTH_SHADOW
                            scannerState.isScanning -> MascotMood.GUARD_ALERT
                            report != null -> MascotMood.HAPPY_VIBES
                            else -> MascotMood.WAVE_HELLO
                        },
                        persona = MascotPersona.SPIRIT_FLAME,
                        size = 48.dp
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ZIP & Video Scanner",
                                style = MaterialTheme.typography.headlineMedium,
                                color = VaultObsidian
                            )
                            Surface(
                                color = VaultObsidian,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "BRANKAS $activeVaultId",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VaultLime,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = "Ekstrak Banyak ZIP • Render & Hapus Video Kembar • Deteksi Virus Pra-Enkripsi",
                            style = MaterialTheme.typography.labelMedium,
                            color = VaultTextMuted
                        )
                    }
                }
            }
        }

        // 1b. Interactive Mascot Companion Banner for Scanner
        item {
            NyxMascotCompanionBanner(
                activeVaultId = activeVaultId,
                headline = if (report != null) {
                    "Hasil Scan: ${report.uniqueCleanFilesCount} Unik • ${report.duplicatesRemovedCount} Kembar Dihapus"
                } else {
                    "Penjaga Antivirus & Anti-Duplikat Video Siap"
                },
                subtitle = if (report != null && report.virusFlaggedCount > 0) {
                    "Waspada! ${report.virusFlaggedCount} file terdeteksi indikasi virus. Konfirmasi sebelum enkripsi."
                } else {
                    "Pilih karakter maskot pendampingmu & masukkan banyak file .ZIP sekaligus."
                },
                mood = when {
                    report != null && report.virusFlaggedCount > 0 -> MascotMood.STEALTH_SHADOW
                    scannerState.isScanning -> MascotMood.GUARD_ALERT
                    else -> MascotMood.HAPPY_VIBES
                },
                quickActionLabel = "Uji Scan",
                onQuickActionClick = onRunSimulatedMultiZipTestClick
            )
        }

        // 2. Kartu Utama Input Multi-ZIP & Pipeline Penjelasan
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = VaultObsidian)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
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
                                    imageVector = Icons.Default.MovieFilter,
                                    contentDescription = null,
                                    tint = VaultObsidian,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "FILTER PRA-ENKRIPSI NYX2",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VaultObsidian,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "SHA-256 + 8x8 VFrame",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = VaultMintCard
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Masukkan Banyak File .ZIP Sekaligus Sebelum Dienkripsi",
                        style = MaterialTheme.typography.titleLarge,
                        color = VaultPureWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. Semua paket .ZIP diekstrak ke ruang isolasi.\n" +
                                "2. Setiap video dirender frame-nya: jika ada video yang sama/kembar, salah satu otomatis dihapus dan hanya 1 yang dipakai.\n" +
                                "3. Setiap file dipindai dari Virus/Malware: jika terdeteksi virus, muncul peringatan apakah tetap ingin dimasukkan ke Vault Enkripsi atau dibuang.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VaultPureWhite.copy(alpha = 0.78f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onPickMultipleZipsOrFilesClick,
                        enabled = !scannerState.isScanning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("scanner_pick_multi_zip_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VaultLime,
                            contentColor = VaultObsidian
                        )
                    ) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pilih Banyak File .ZIP / Video dari Perangkat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onRunSimulatedMultiZipTestClick,
                        enabled = !scannerState.isScanning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("scanner_run_test_batch_button"),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VaultMintCard.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = VaultMintCard,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simulasi Uji 2 ZIP (Tes Video Kembar + Deteksi Virus)",
                            style = MaterialTheme.typography.labelLarge,
                            color = VaultMintCard
                        )
                    }
                }
            }
        }

        // 3. Indikator Pemindaian & Render Frame Aktif
        if (scannerState.isScanning) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultMintCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(
                            color = VaultObsidian,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mengekstrak ZIP, Merender Frame Video & Memindai Virus...",
                                style = MaterialTheme.typography.titleMedium,
                                color = VaultObsidian
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = scannerState.progressMessage.ifBlank { "Memproses paket ZIP..." },
                                style = MaterialTheme.typography.bodyMedium,
                                color = VaultObsidian.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
        }

        // 4. Laporan Hasil Ekstraksi, De-duplikasi Video, & Deteksi Virus
        if (report != null && !scannerState.isScanning) {
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
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Hasil Pindai Pra-Enkripsi (${report.totalZipArchivesProcessed} ZIP)",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = VaultObsidian
                                )
                                Text(
                                    text = "Total ${report.totalExtractedFiles} file diekstrak & dianalisis sebelum masuk Vault",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VaultTextMuted
                                )
                            }
                            IconButton(onClick = onClearScanReport) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Bersihkan Hasil",
                                    tint = VaultObsidian
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3 Bento Stat Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScannerMetricPill(
                                modifier = Modifier.weight(1f),
                                count = report.uniqueCleanFilesCount.toString(),
                                title = "Video/File Unik",
                                subtitle = "Siap Enkripsi",
                                bgColor = VaultLime,
                                textColor = VaultObsidian
                            )
                            ScannerMetricPill(
                                modifier = Modifier.weight(1f),
                                count = report.duplicatesRemovedCount.toString(),
                                title = "Duplikat Dihapus",
                                subtitle = "Hemat ${formatFileSize(report.bytesSavedByDeduplication)}",
                                bgColor = VaultMintCard,
                                textColor = VaultObsidian
                            )
                            ScannerMetricPill(
                                modifier = Modifier.weight(1f),
                                count = report.virusFlaggedCount.toString(),
                                title = "Indikasi Virus",
                                subtitle = if (report.virusFlaggedCount > 0) "Perlu Konfirmasi" else "0 Ancaman",
                                bgColor = if (report.virusFlaggedCount > 0) VaultCoral else VaultObsidian,
                                textColor = VaultPureWhite
                            )
                        }

                        // Jika ada virus terdeteksi, tampilkan banner peringatan interaktif
                        if (report.virusFlaggedCount > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { onOpenVirusWarningModal() }
                                    .testTag("virus_alert_banner_button"),
                                color = VaultCoral.copy(alpha = 0.14f),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, VaultCoral)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = VaultCoral,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "PERINGATAN: ${report.virusFlaggedCount} File Mengandung Indikasi Virus!",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = VaultCoral,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Ketuk di sini untuk memilih apakah file bervirus ingin dihapus atau tetap dimasukkan ke dalam Vault Enkripsi (.nyx).",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = VaultObsidian
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tombol Eksekusi Enkripsi ke Brankas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (report.virusFlaggedCount > 0) {
                                        onOpenVirusWarningModal()
                                    } else {
                                        onCommitCleanOnlyToVault()
                                    }
                                },
                                enabled = report.uniqueCleanFilesCount > 0 || report.virusFlaggedCount > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("commit_staged_to_vault_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VaultObsidian,
                                    contentColor = VaultLime
                                )
                            ) {
                                Icon(Icons.Default.EnhancedEncryption, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (report.virusFlaggedCount > 0) {
                                        "Konfirmasi Virus & Enkripsi ke Brankas $activeVaultId"
                                    } else {
                                        "Enkripsi ${report.uniqueCleanFilesCount} File Unik ke Brankas $activeVaultId"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 5. Filter Chip Hasil Ekstraksi & Render Video
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        0 to "Semua (${report.items.size})",
                        1 to "Unik & Aman (${report.uniqueCleanFilesCount})",
                        2 to "Duplikat Dihapus (${report.duplicatesRemovedCount})",
                        3 to "Terdeteksi Virus (${report.virusFlaggedCount})"
                    )
                    tabs.forEach { (idx, label) ->
                        FilterChip(
                            selected = selectedFilterTab == idx,
                            onClick = { selectedFilterTab = idx },
                            label = { Text(label) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VaultObsidian,
                                selectedLabelColor = VaultLime,
                                containerColor = VaultPureWhite,
                                labelColor = VaultObsidian
                            )
                        )
                    }
                }
            }

            // 6. Daftar Item yang Diekstrak, Dirender Videonya, Dihapus Duplikatnya, atau Diperingatkan Virusnya
            items(displayedItems, key = { it.id }) { item ->
                StagedScanResultCard(item = item)
            }
        }
    }

    // Dialog Peringatan Virus & Pilihan Tetap Masukkan ke Vault Enkripsi atau Tidak
    if (scannerState.showVirusWarningModal && report != null && report.infectedItems.isNotEmpty()) {
        VirusWarningConfirmationDialog(
            activeVaultId = activeVaultId,
            report = report,
            onCommitCleanOnly = onCommitCleanOnlyToVault,
            onCommitIncludingInfected = onCommitAllIncludingInfectedToVault,
            onCancelAndClearAll = onClearScanReport,
            onDismissDialogOnly = onDismissVirusWarningModal
        )
    }
}

@Composable
private fun ScannerMetricPill(
    modifier: Modifier = Modifier,
    count: String,
    title: String,
    subtitle: String,
    bgColor: Color,
    textColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = textColor.copy(alpha = 0.75f),
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun StagedScanResultCard(item: StagedScanItem) {
    val cardBg = when (item.status) {
        StagedFileStatus.CLEAN_READY -> CosmicGlass
        StagedFileStatus.DUPLICATE_REMOVED -> androidx.compose.ui.graphics.Color(0x331E293B)
        StagedFileStatus.INFECTED_WARNING -> androidx.compose.ui.graphics.Color(0x33F43F5E)
    }
    val borderColor = when (item.status) {
        StagedFileStatus.CLEAN_READY -> PastelMint.copy(alpha = 0.4f)
        StagedFileStatus.DUPLICATE_REMOVED -> SolarGold.copy(alpha = 0.4f)
        StagedFileStatus.INFECTED_WARNING -> PastelRose
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("staged_scan_card_${item.fileName}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Baris Status Atas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (item.status) {
                        StagedFileStatus.CLEAN_READY -> VaultLime
                        StagedFileStatus.DUPLICATE_REMOVED -> VaultAmber
                        StagedFileStatus.INFECTED_WARNING -> VaultCoral
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = when (item.status) {
                                StagedFileStatus.CLEAN_READY -> Icons.Default.CheckCircle
                                StagedFileStatus.DUPLICATE_REMOVED -> Icons.Default.FilterNone
                                StagedFileStatus.INFECTED_WARNING -> Icons.Default.BugReport
                            },
                            contentDescription = null,
                            tint = if (item.status == StagedFileStatus.INFECTED_WARNING) VaultPureWhite else VaultObsidian,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = when (item.status) {
                                StagedFileStatus.CLEAN_READY -> "UNIK & BERSIH (DIPAKAI)"
                                StagedFileStatus.DUPLICATE_REMOVED -> "VIDEO KEMBAR (DIHAPUS SALAH SATU)"
                                StagedFileStatus.INFECTED_WARNING -> "TERDETEKSI VIRUS / MALWARE!"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = CosmicTextPure,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Sumber: ${item.sourceZipName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.status == StagedFileStatus.INFECTED_WARNING) VaultLime else VaultTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thumbnail Hasil Render Frame Video atau Ikon File
                val thumbPath = item.renderedThumbPath
                val thumbFile = remember(thumbPath) { thumbPath?.let { File(it) } }

                Box(
                    modifier = Modifier
                        .size(width = 96.dp, height = 62.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VaultObsidian),
                    contentAlignment = Alignment.Center
                ) {
                    if (thumbFile != null && thumbFile.exists()) {
                        AsyncImage(
                            model = thumbFile,
                            contentDescription = "Rendered Video Frame ${item.fileName}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = if (item.category == VaultCategory.VIDEO) Icons.Default.VideoFile else Icons.Default.FolderZip,
                            contentDescription = null,
                            tint = if (item.status == StagedFileStatus.INFECTED_WARNING) VaultCoral else VaultLime,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    if (item.category == VaultCategory.VIDEO) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp),
                            color = VaultObsidian.copy(alpha = 0.82f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${item.videoResolution} • ${item.durationSeconds}s",
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 9.sp,
                                color = VaultLime,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${item.fileName}.${item.extension}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (item.status == StagedFileStatus.INFECTED_WARNING) VaultPureWhite else VaultObsidian,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatFileSize(item.sizeBytes)} • SHA256: ${item.sha256Hex.take(12)}...",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = if (item.status == StagedFileStatus.INFECTED_WARNING) {
                            VaultPureWhite.copy(alpha = 0.7f)
                        } else {
                            VaultTextMuted
                        }
                    )
                    if (item.category == VaultCategory.VIDEO) {
                        Text(
                            text = "Render Frame ID: ${item.videoFrameSignature.take(26)}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = if (item.status == StagedFileStatus.INFECTED_WARNING) VaultMintCard else VaultMintAccent
                        )
                    }
                }
            }

            // Penjelasan De-duplikasi atau Detail Ancaman Virus
            if (item.status == StagedFileStatus.DUPLICATE_REMOVED && item.duplicateOfFileName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = VaultAmber.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = VaultObsidian,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Video ini terdeteksi identik dengan \"${item.duplicateOfFileName}\". Salinan kembar ini telah dihapus otomatis dan sistem menggunakan 1 video utama.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            color = VaultObsidian
                        )
                    }
                }
            }

            if (item.status == StagedFileStatus.INFECTED_WARNING) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = VaultCoral.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Ancaman: ${item.virusThreatName ?: "Heuristic.Malware"}",
                            style = MaterialTheme.typography.labelLarge,
                            color = VaultCoral,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.virusThreatDetail ?: "Terdeteksi kode eksekusi mencurigakan di dalam file.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            color = VaultPureWhite.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog Peringatan Virus & Konfirmasi Pengguna:
 * Menanyakan apakah file yang terdeteksi bervirus tetap ingin dimasukkan ke dalam Vault Enkripsi (.nyx)
 * atau dihapus sebelum proses enkripsi berjalan.
 */
@Composable
fun VirusWarningConfirmationDialog(
    activeVaultId: Int,
    report: BatchZipScanReport,
    onCommitCleanOnly: () -> Unit,
    onCommitIncludingInfected: () -> Unit,
    onCancelAndClearAll: () -> Unit,
    onDismissDialogOnly: () -> Unit
) {
    val infectedList = report.infectedItems

    Dialog(
        onDismissRequest = onDismissDialogOnly,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .widthIn(max = 460.dp)
                .testTag("virus_warning_confirmation_dialog"),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = VaultObsidian),
            border = androidx.compose.foundation.BorderStroke(2.dp, VaultCoral)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = VaultCoral,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = VaultPureWhite,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "PERINGATAN DETEKSI VIRUS!",
                                style = MaterialTheme.typography.labelMedium,
                                color = VaultPureWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    NyxMascotAvatar(
                        mood = MascotMood.STEALTH_SHADOW,
                        size = 44.dp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${infectedList.size} File Mengandung Indikasi Virus / Malware",
                    style = MaterialTheme.typography.titleLarge,
                    color = VaultPureWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Saat mengekstrak paket .ZIP (${report.duplicatesRemovedCount} video kembar telah dihapus otomatis), pemindai menemukan file berbahaya berikut:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VaultPureWhite.copy(alpha = 0.78f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Daftar File Terinfeksi
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    infectedList.take(3).forEach { infected ->
                        Surface(
                            color = VaultCharcoal,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VaultCoral.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${infected.fileName}.${infected.extension}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = VaultCoral,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = infected.sourceZipName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = VaultLime
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Signature: ${infected.virusThreatName}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    color = VaultAmber
                                )
                                Text(
                                    text = infected.virusThreatDetail ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 11.sp,
                                    color = VaultPureWhite.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Apakah Anda tetap ingin memasukkan file ini ke dalam Vault Enkripsi (.NYX) atau menghapusnya?",
                    style = MaterialTheme.typography.labelLarge,
                    color = VaultLime,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pilihan 1: Hapus Virus & Simpan File Aman Saja
                Button(
                    onClick = onCommitCleanOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("virus_dialog_clean_only_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VaultLime,
                        contentColor = VaultObsidian
                    )
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hapus File Virus & Enkripsi ${report.uniqueCleanFilesCount} File Aman Saja",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pilihan 2: Tetap Masukkan Semua ke Vault Enkripsi (Karantina .NYX)
                Button(
                    onClick = onCommitIncludingInfected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("virus_dialog_keep_infected_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VaultCoral,
                        contentColor = VaultPureWhite
                    )
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tetap Masukkan ke Vault & Enkripsi Semua (.NYX)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pilihan 3: Batalkan Impor
                OutlinedButton(
                    onClick = onCancelAndClearAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("virus_dialog_cancel_all_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VaultPureWhite.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Batalkan & Hapus Semua Hasil Ekstraksi",
                        style = MaterialTheme.typography.labelLarge,
                        color = VaultPureWhite
                    )
                }
            }
        }
    }
}
