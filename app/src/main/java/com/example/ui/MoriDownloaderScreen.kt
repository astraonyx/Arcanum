package com.example.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MoriDownloadResult
import com.example.data.MoriDownloaderEngine
import com.example.data.MoriExtractionMode
import com.example.data.MoriPlatform
import com.example.data.MoriResolvedMetadata
import com.example.data.VaultCategory
import com.example.data.VaultItemEntity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VaultCharcoal
import com.example.ui.theme.VaultCoral
import com.example.ui.theme.VaultCream
import com.example.ui.theme.VaultLime
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

data class MoriDownloaderUiState(
    val urlInput: String = "",
    val customFileName: String = "",
    val selectedMode: MoriExtractionMode = MoriExtractionMode.HD_VIDEO_NO_WM,
    val encryptImmediately: Boolean = false,
    val isAnalyzing: Boolean = false,
    val resolvedPreview: MoriResolvedMetadata? = null,
    val isDownloading: Boolean = false,
    val progressRatio: Float = 0f,
    val statusLogLine: String = "Siap mengekstrak link secara lokal (Zero-Server Client-Side)",
    val scraperLogs: List<String> = emptyList(),
    val otaBundleVersion: String = "v2.5.0-mori-bundle",
    val isCheckingOta: Boolean = false,
    val lastDownloadResult: MoriDownloadResult? = null
)

@Composable
fun MoriDownloaderScreen(
    activeVaultId: Int,
    moriState: MoriDownloaderUiState,
    allVaultItems: List<VaultItemEntity> = emptyList(),
    onUrlInputChange: (String) -> Unit,
    onCustomFileNameChange: (String) -> Unit,
    onSelectExtractionMode: (MoriExtractionMode) -> Unit,
    onToggleEncryptImmediately: (Boolean) -> Unit,
    onAnalyzeLinkClick: () -> Unit = {},
    onStartDownloadToVault: () -> Unit,
    onTriggerOtaScraperUpdate: () -> Unit,
    onOpenDownloadedItem: (VaultItemEntity) -> Unit,
    onToggleEncryptItemWithPin: (VaultItemEntity) -> Unit = {},
    onShareItemWithBiometric: (VaultItemEntity) -> Unit = {},
    onDeleteItem: (VaultItemEntity) -> Unit = {}
) {
    val context = LocalContext.current
    var clipboardDetectedUrl by remember { mutableStateOf<String?>(null) }

    // Smart Clipboard Auto-Detection (Seperti coflyn/Mori saat membuka layar)
    LaunchedEffect(Unit) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipText = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
            if (!clipText.isNullOrBlank() && (clipText.contains("http://") || clipText.contains("https://"))) {
                val clean = MoriDownloaderEngine.extractCleanUrl(clipText)
                clipboardDetectedUrl = clean
                if (moriState.urlInput.isBlank()) {
                    onUrlInputChange(clean)
                }
            }
        } catch (_: Exception) {
        }
    }

    val detectedPlatform = remember(moriState.urlInput) {
        MoriDownloaderEngine.detectPlatformFromUrl(moriState.urlInput)
    }

    val moriSavedItems = remember(allVaultItems) {
        allVaultItems.filter { it.groupArchiveName?.startsWith("Mori") == true }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mori_downloader_screen"),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Minimalist Header + OTA Bundle Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Mori Media Grabber",
                            style = MaterialTheme.typography.headlineMedium,
                            color = CosmicTextPure,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = StarlightCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StarlightCyan.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "16 PLATFORM",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = StarlightCyan,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = "Client-Side Downloader Tanpa Watermark -> Ruang Rahasia",
                        style = MaterialTheme.typography.labelMedium,
                        color = CosmicTextSubtle
                    )
                }

                // OTA Scraper Hot-Patch Pill
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .instantTouchPress { onTriggerOtaScraperUpdate() }
                        .testTag("mori_ota_update_button"),
                    color = VaultObsidian
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdateAlt,
                            contentDescription = "OTA Scraper Update",
                            tint = VaultLime,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (moriState.isCheckingOta) "Patching..." else "OTA Sync",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = VaultPureWhite
                        )
                    }
                }
            }
        }

        // 2. Cosmic Dragon Companion Banner for Mori
        item {
            CosmicDragonCompanionBanner(
                activeVaultId = activeVaultId,
                headline = when {
                    moriState.isDownloading -> "Astra terbang mengejar stream ${detectedPlatform.displayName}..."
                    moriState.isAnalyzing -> "Astra menganalisis komet metadata ${detectedPlatform.displayName}..."
                    moriState.lastDownloadResult != null -> "Media tersimpan di Orbit Brankas: ${moriState.lastDownloadResult.savedFileName}.${moriState.lastDownloadResult.extension}!"
                    else -> "Tempel link TikTok, IG, YT, X, Spotify untuk ekstraksi instan!"
                },
                subtitle = "Scraper ${moriState.otaBundleVersion} aktif • Enkripsi langsung ke Brankas NYX2",
                mood = when {
                    moriState.isDownloading -> MascotMood.DOWNLOAD_HUNTER
                    moriState.lastDownloadResult != null -> MascotMood.SUCCESS_LEAP
                    moriState.isAnalyzing -> MascotMood.MT_INSPECTING
                    else -> MascotMood.IDLE_BREATHE
                }
            )
        }

        // 3. Smart Clipboard Banner (if a URL was detected in system clipboard)
        clipboardDetectedUrl?.let { clipUrl ->
            if (clipUrl != moriState.urlInput) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fastZeroLatencyClick { onUrlInputChange(clipUrl) }
                            .testTag("mori_clipboard_detected_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VaultMintCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Link Terdeteksi Otomatis di Clipboard",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = VaultObsidian,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = clipUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VaultObsidian.copy(alpha = 0.75f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Surface(
                                color = VaultObsidian,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Gunakan",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VaultLime,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Main Link Input, Platform Auto-Detector & Quick Action Presets
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = Color(detectedPlatform.accentHex),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = detectedPlatform.badgeTag,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = VaultObsidian,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = detectedPlatform.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CosmicTextPure
                            )
                        }

                        // 1-Tap Paste from Clipboard Button
                        Surface(
                            color = PastelSky.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PastelSky.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .instantTouchPress {
                                    try {
                                        val clipboard =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val text = clipboard?.primaryClip
                                            ?.getItemAt(0)
                                            ?.coerceToText(context)
                                            ?.toString()
                                            ?.trim()
                                        if (!text.isNullOrBlank()) {
                                            onUrlInputChange(MoriDownloaderEngine.extractCleanUrl(text))
                                        }
                                    } catch (_: Exception) {
                                    }
                                }
                                .testTag("mori_paste_clipboard_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Tempel Clipboard",
                                    tint = VaultObsidian,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Tempel Link",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VaultObsidian
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = moriState.urlInput,
                        onValueChange = onUrlInputChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mori_url_input"),
                        placeholder = {
                            Text("https://www.tiktok.com/... atau https://x.com/... atau link media")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = VaultObsidian)
                        },
                        trailingIcon = {
                            if (moriState.urlInput.isNotEmpty()) {
                                IconButton(onClick = { onUrlInputChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Bersihkan")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = VaultCream,
                            unfocusedContainerColor = VaultCream,
                            focusedBorderColor = VaultObsidian,
                            unfocusedBorderColor = VaultObsidian.copy(alpha = 0.14f)
                        )
                    )

                    // Quick Sample Platform Links for Instant 1-Tap Testing
                    Text(
                        text = "Coba Cepat (1-Ketuk Pilih Platform & Format):",
                        style = MaterialTheme.typography.labelMedium,
                        color = VaultTextMuted
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickSamples = listOf(
                            Triple(
                                "TikTok Video HD",
                                "https://www.tiktok.com/@tiktok/video/7106594312292453675",
                                MoriExtractionMode.HD_VIDEO_NO_WM
                            ),
                            Triple(
                                "IG Carousel -> PDF",
                                "https://www.instagram.com/p/C9mori_carousel_slides/",
                                MoriExtractionMode.COMPILE_CAROUSEL_PDF
                            ),
                            Triple(
                                "Spotify Synthwave Audio",
                                "https://open.spotify.com/track/4cOdK2wGLETKBW3PvgPWqT",
                                MoriExtractionMode.AUDIO_STREAM_MP3
                            ),
                            Triple(
                                "YouTube Video Stream",
                                "https://www.youtube.com/watch?v=aqz-KE-bpKQ",
                                MoriExtractionMode.HD_VIDEO_NO_WM
                            ),
                            Triple(
                                "Pinterest Artwork HD",
                                "https://www.pinterest.com/pin/mori_hd_wallpaper/",
                                MoriExtractionMode.PHOTO_CAROUSEL
                            ),
                            Triple(
                                "X / Twitter Clip",
                                "https://x.com/NASA/status/1777355895781613691",
                                MoriExtractionMode.HD_VIDEO_NO_WM
                            )
                        )
                        quickSamples.forEach { (label, sampleUrl, recommendedMode) ->
                            FilterChip(
                                selected = moriState.urlInput == sampleUrl,
                                onClick = {
                                    onUrlInputChange(sampleUrl)
                                    onSelectExtractionMode(recommendedMode)
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VaultObsidian,
                                    selectedLabelColor = VaultLime
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = moriState.customFileName,
                            onValueChange = onCustomFileNameChange,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mori_custom_filename_input"),
                            placeholder = {
                                Text("Nama file opsional (kosongkan = otomatis)")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = VaultPureWhite,
                                unfocusedContainerColor = VaultPureWhite,
                                focusedBorderColor = VaultObsidian,
                                unfocusedBorderColor = VaultObsidian.copy(alpha = 0.12f)
                            )
                        )

                        OutlinedButton(
                            onClick = onAnalyzeLinkClick,
                            enabled = !moriState.isAnalyzing && !moriState.isDownloading,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("mori_analyze_link_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Analisis Link",
                                tint = VaultObsidian,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (moriState.isAnalyzing) "Cek..." else "Cek Link",
                                style = MaterialTheme.typography.labelLarge,
                                color = VaultObsidian,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Resolved Link Metadata Preview Card (when analyzed or after download)
                    val activePreview = moriState.resolvedPreview ?: moriState.lastDownloadResult?.resolvedMeta
                    if (activePreview != null) {
                        Surface(
                            color = VaultCream,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VaultObsidian.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (!activePreview.coverImageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = activePreview.coverImageUrl,
                                        contentDescription = activePreview.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(VaultObsidian)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(VaultObsidian),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = activePreview.platform.displayName.take(2).uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = VaultLime,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activePreview.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = VaultObsidian,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${activePreview.author} • ${activePreview.resolverSource}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = VaultTextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = buildString {
                                            append("Stream: ")
                                            if (activePreview.directVideoUrl != null) append("MP4 HD • ")
                                            if (activePreview.directAudioUrl != null) append("Audio • ")
                                            if (activePreview.carouselImageUrls.isNotEmpty()) {
                                                append("${activePreview.carouselImageUrls.size} Slide Foto • ")
                                            }
                                            append("Siap Ekstrak")
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VaultObsidian,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Mori 4 Flexible Extraction Modes (Video No-WM, Audio MP3/WAV, Photo Carousel, Compile to PDF)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = VaultPureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, VaultObsidian.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pilih Mode Ekstraksi Mori",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VaultObsidian
                    )

                    MoriExtractionMode.entries.forEach { mode ->
                        val isSelected = moriState.selectedMode == mode
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) VaultObsidian else VaultCream)
                                .fastZeroLatencyClick { onSelectExtractionMode(mode) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("mori_mode_${mode.name}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) VaultLime else VaultPureWhite),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (mode) {
                                            MoriExtractionMode.HD_VIDEO_NO_WM -> Icons.Default.VideoFile
                                            MoriExtractionMode.AUDIO_STREAM_MP3 -> Icons.Default.AudioFile
                                            MoriExtractionMode.PHOTO_CAROUSEL -> Icons.Default.Image
                                            MoriExtractionMode.COMPILE_CAROUSEL_PDF -> Icons.Default.Description
                                        },
                                        contentDescription = mode.title,
                                        tint = VaultObsidian,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${mode.title} (.${mode.defaultExtension.uppercase()})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected) VaultPureWhite else VaultObsidian
                                    )
                                    Text(
                                        text = mode.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) VaultPureWhite.copy(alpha = 0.72f) else VaultTextMuted
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Aktif",
                                        tint = VaultLime,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Toggle: Seal immediately with NYX2 vs Keep Ready for Player
                    Surface(
                        color = VaultMintCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Kunci Enkripsi NYX2 Otomatis (.nyx)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = VaultObsidian
                                )
                                Text(
                                    text = if (moriState.encryptImmediately) {
                                        "Langsung disegel ChaCha20+AES-GCM di Ruang Rahasia"
                                    } else {
                                        "Simpan terbuka di Ruang Rahasia agar langsung siap diputar/dibuka"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VaultObsidian.copy(alpha = 0.75f)
                                )
                            }
                            Switch(
                                checked = moriState.encryptImmediately,
                                onCheckedChange = onToggleEncryptImmediately,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = VaultLime,
                                    checkedTrackColor = VaultObsidian
                                ),
                                modifier = Modifier.testTag("mori_encrypt_toggle")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onStartDownloadToVault,
                        enabled = !moriState.isDownloading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("mori_download_submit_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VaultObsidian,
                            contentColor = VaultLime
                        )
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (moriState.isDownloading) {
                                "Mengekstrak & Menyimpan ke Ruang Rahasia..."
                            } else {
                                "Unduh & Simpan ke Ruang Rahasia"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 6. Live Progress / Result & Client-Side Scraper Terminal Logs
        if (moriState.isDownloading || moriState.scraperLogs.isNotEmpty() || moriState.lastDownloadResult != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultObsidian)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MORI CLIENT-SIDE SCRAPER LOG",
                                style = MaterialTheme.typography.labelLarge,
                                color = VaultLime
                            )
                            Text(
                                text = "${(moriState.progressRatio * 100).toInt()}%",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                color = VaultMintCard
                            )
                        }

                        LinearProgressIndicator(
                            progress = { moriState.progressRatio.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            color = VaultLime,
                            trackColor = VaultCharcoal
                        )

                        Surface(
                            color = VaultCharcoal,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                moriState.scraperLogs.takeLast(6).forEach { logLine ->
                                    Text(
                                        text = logLine,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = VaultPureWhite.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        moriState.lastDownloadResult?.let { res ->
                            Button(
                                onClick = { onOpenDownloadedItem(res.createdEntity) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("mori_open_downloaded_item_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VaultLime,
                                    contentColor = VaultObsidian
                                )
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Buka / Putar ${res.savedFileName}.${res.extension} Sekarang",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Koleksi & Riwayat Unduhan Mori di Brankas Aktif (Bisa Langsung Diputar / Dibuka / Dikunci PIN / Dibagikan)
        if (moriSavedItems.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hasil Unduhan Mori (${moriSavedItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VaultObsidian
                    )
                    Text(
                        text = "Ketuk untuk Putar/Buka",
                        style = MaterialTheme.typography.labelMedium,
                        color = VaultTextMuted
                    )
                }
            }

            items(moriSavedItems, key = { it.id }) { item ->
                val cat = VaultCategory.entries.find { it.name == item.category } ?: VaultCategory.VIDEO
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fastZeroLatencyClick { onOpenDownloadedItem(item) }
                        .testTag("mori_history_item_${item.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = VaultPureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VaultObsidian.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(VaultObsidian),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (cat) {
                                    VaultCategory.VIDEO -> Icons.Default.VideoFile
                                    VaultCategory.AUDIO -> Icons.Default.AudioFile
                                    VaultCategory.IMAGE -> Icons.Default.Image
                                    else -> Icons.Default.Description
                                },
                                contentDescription = null,
                                tint = VaultLime,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.name}.${item.extension}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = VaultObsidian,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.groupArchiveName ?: "Mori"} • ${formatFileSize(item.sizeBytes)} • ${if (item.isEncrypted) "Terkunci .nyx" else "Siap Diputar"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = VaultTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .instantTouchPress { onOpenDownloadedItem(item) },
                                color = VaultLime
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Putar / Buka",
                                        tint = VaultObsidian,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .instantTouchPress { onToggleEncryptItemWithPin(item) },
                                color = VaultMintCard
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (item.isEncrypted) Icons.Default.LockOpen else Icons.Default.Lock,
                                        contentDescription = "Kunci / Buka PIN",
                                        tint = VaultObsidian,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .instantTouchPress { onShareItemWithBiometric(item) },
                                color = VaultCream
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Bagikan dengan Biometrik",
                                        tint = VaultObsidian,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .instantTouchPress { onDeleteItem(item) },
                                color = VaultCream
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Hapus",
                                        tint = VaultCoral,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Supported 16 Platforms Badge Showcase (Tap any badge to fill sample link)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = VaultPureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, VaultObsidian.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "16 Platform Didukung (Ketuk Platform atau Share dari Aplikasi Lain)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = VaultObsidian
                    )
                    Text(
                        text = "Klik tombol 'Bagikan (Share)' dari TikTok, IG, YouTube, X, atau Browser lalu pilih Trantor Vault untuk otomatis mengunduh ke Brankas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = VaultTextMuted
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MoriPlatform.entries.filter { it != MoriPlatform.DIRECT_WEB }.forEach { platform ->
                            val isCurrent = detectedPlatform == platform
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .fastZeroLatencyClick {
                                        val domain = platform.domainKeywords.firstOrNull() ?: "example.com"
                                        onUrlInputChange("https://$domain/mori_stream_${platform.name.lowercase()}")
                                        onSelectExtractionMode(platform.defaultMediaType)
                                    },
                                color = if (isCurrent) VaultObsidian else VaultCream,
                                shape = RoundedCornerShape(50),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    VaultObsidian.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(platform.accentHex))
                                    )
                                    Text(
                                        text = platform.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isCurrent) VaultLime else VaultObsidian
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
