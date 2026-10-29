package com.example.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.VideoView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.VaultCategory
import com.example.data.VaultItemEntity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VaultAmber
import com.example.ui.theme.VaultCharcoal
import com.example.ui.theme.VaultCoral
import com.example.ui.theme.VaultLime
import com.example.ui.theme.VaultMintCard
import com.example.ui.theme.VaultObsidian
import com.example.ui.theme.VaultPureWhite
import com.example.ui.theme.VaultSlate
import java.io.File

/**
 * 1. Dialog Verifikasi PIN 6-Digit Saat Hendak Mengenkripsi atau Mendekripsi File
 * (Production Stealth — Tanpa hint PIN dan tanpa tombol autofill)
 */
@Composable
fun CryptoPinVerificationDialog(
    modalState: CryptoPinModalState,
    onDigitClick: (Int) -> Unit,
    onBackspaceClick: () -> Unit,
    onDismiss: () -> Unit,
    onForgotPinClick: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .widthIn(max = 440.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = VaultObsidian),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, VaultLime.copy(alpha = 0.5f))
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
                        color = if (modalState.targetEncryptState) VaultLime else VaultMintCard,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (modalState.targetEncryptState) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = VaultObsidian,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (modalState.targetEncryptState) "PIN ENKRIPSI NYX2" else "PIN DEKRIPSI NYX2",
                                style = MaterialTheme.typography.labelMedium,
                                color = VaultObsidian,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Batal", tint = VaultPureWhite)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                NyxMascotAvatar(
                    mood = if (modalState.enteredPin.isNotEmpty()) MascotMood.SHIELD_EYES else MascotMood.GUARD_ALERT,
                    size = 64.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = modalState.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = VaultPureWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = modalState.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VaultPureWhite.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 6-Digit PIN Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    for (i in 0 until 6) {
                        val isFilled = i < modalState.enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isFilled) VaultLime else VaultCharcoal)
                                .border(
                                    width = 1.dp,
                                    color = if (isFilled) VaultLime else VaultSlate,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFilled) {
                                Box(
                                    modifier = Modifier
                                        .size(11.dp)
                                        .clip(CircleShape)
                                        .background(VaultObsidian)
                                )
                            } else {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = VaultPureWhite.copy(alpha = 0.35f)
                                )
                            }
                        }
                    }
                }

                if (modalState.errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = modalState.errorText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VaultCoral,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fast Zero-Lag Keypad Grid
                val rows = listOf(
                    listOf(1, 2, 3),
                    listOf(4, 5, 6),
                    listOf(7, 8, 9)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { digit ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(VaultCharcoal)
                                        .instantTouchPress { onDigitClick(digit) }
                                        .testTag("crypto_modal_key_$digit"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = digit.toString(),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = VaultPureWhite
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(VaultSlate)
                                .instantTouchPress { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("BATAL", style = MaterialTheme.typography.labelLarge, color = VaultCoral)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(VaultCharcoal)
                                .instantTouchPress { onDigitClick(0) }
                                .testTag("crypto_modal_key_0"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("0", style = MaterialTheme.typography.titleLarge, color = VaultPureWhite)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(VaultSlate)
                                .instantTouchPress { onBackspaceClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Hapus",
                                tint = VaultPureWhite
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .instantTouchPress {
                                    onDismiss()
                                    onForgotPinClick()
                                }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .testTag("crypto_modal_forgot_pin_button")
                        ) {
                            Text(
                                text = "Lupa PIN Otorisasi? Pulihkan di Sini",
                                style = MaterialTheme.typography.labelMedium,
                                color = VaultLime
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. Full-Screen Rich Media, Video (MP4/MOV), Image (JPG/PNG/WEBP/GIF/Sticker) & Document Viewer
 */
@Composable
fun VaultRichMediaViewerOverlay(
    item: VaultItemEntity,
    mediaState: MediaPlayerUiState,
    onClose: () -> Unit,
    onRequestReEncryptWithPin: () -> Unit,
    onRequestExportOrShareWithBiometric: (Boolean) -> Unit = {},
    onToggleVideoPlayPause: () -> Unit,
    onSeekVideoSeconds: (Int) -> Unit,
    onCycleSpeed: () -> Unit
) {
    val categoryEnum = VaultCategory.entries.find { it.name == item.category } ?: VaultCategory.DOCUMENT

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VaultObsidian)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .testTag("rich_media_viewer_overlay")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .align(Alignment.Center)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Viewer Header Bar
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
                            Surface(
                                color = VaultLime,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = item.extension.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VaultObsidian,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "${item.name}.${item.extension}",
                                style = MaterialTheme.typography.titleLarge,
                                color = VaultPureWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = buildString {
                                append("Brankas ${item.vaultProfileId} • ${formatFileSize(item.sizeBytes)}")
                                if (!item.groupArchiveName.isNullOrBlank()) {
                                    append(" • ZIP: ${item.groupArchiveName}")
                                }
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = VaultMintCard
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .instantTouchPress { onRequestExportOrShareWithBiometric(true) }
                                .testTag("viewer_share_biometric_button"),
                            color = VaultLime
                        ) {
                            Text(
                                text = "Bagikan",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = VaultObsidian,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .instantTouchPress { onRequestExportOrShareWithBiometric(false) }
                                .testTag("viewer_export_biometric_button"),
                            color = VaultMintCard
                        ) {
                            Text(
                                text = "Ekspor",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = VaultObsidian,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onRequestReEncryptWithPin() }
                                .testTag("viewer_lock_again_button"),
                            color = VaultCharcoal,
                            border = androidx.compose.foundation.BorderStroke(1.dp, VaultLime.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Kunci Kembali",
                                    tint = VaultLime,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Kunci",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = VaultLime
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { onClose() }
                                .testTag("viewer_close_button"),
                            color = VaultSlate
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = VaultPureWhite)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Center Viewport Based on File Type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (categoryEnum) {
                        VaultCategory.VIDEO -> {
                            CinemaVideoPlayerViewport(
                                item = item,
                                mediaState = mediaState,
                                onTogglePlayPause = onToggleVideoPlayPause,
                                onSeekSeconds = onSeekVideoSeconds,
                                onCycleSpeed = onCycleSpeed
                            )
                        }

                        VaultCategory.IMAGE -> {
                            InteractiveImageAndStickerViewport(item = item)
                        }

                        else -> {
                            DecryptedDocumentReaderViewport(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CinemaVideoPlayerViewport(
    item: VaultItemEntity,
    mediaState: MediaPlayerUiState,
    onTogglePlayPause: () -> Unit,
    onSeekSeconds: (Int) -> Unit,
    onCycleSpeed: () -> Unit
) {
    val plainFile = remember(item.internalPlainPath) { File(item.internalPlainPath) }
    var videoErrorFallback by remember(item.id, item.internalPlainPath) { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = VaultCharcoal),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, VaultSlate)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (!videoErrorFallback && plainFile.exists()) {
                    key(item.id, item.internalPlainPath) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    setOnErrorListener { _, _, _ ->
                                        videoErrorFallback = true
                                        true
                                    }
                                    setVideoPath(plainFile.absolutePath)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        start()
                                    }
                                }
                            },
                            update = { vv ->
                                try {
                                    if (mediaState.isPlaying && !vv.isPlaying) vv.start()
                                    else if (!mediaState.isPlaying && vv.isPlaying) vv.pause()
                                } catch (_: Exception) {
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else if (!videoErrorFallback && !item.mediaUriString.isNullOrBlank()) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setOnErrorListener { _, _, _ ->
                                    videoErrorFallback = true
                                    true
                                }
                                setVideoURI(Uri.parse(item.mediaUriString))
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    start()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        NyxMascotAvatar(
                            mood = if (mediaState.isPlaying) MascotMood.HAPPY_VIBES else MascotMood.WAVE_HELLO,
                            size = 84.dp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${item.name}.${item.extension}",
                            style = MaterialTheme.typography.titleMedium,
                            color = VaultPureWhite
                        )
                        Text(
                            text = "Memutar stream di dekoder internal NYX2",
                            style = MaterialTheme.typography.bodySmall,
                            color = VaultMintCard
                        )
                    }
                }

                // Top-left Live Codec HUD Overlay
                Surface(
                    color = VaultObsidian.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (mediaState.isPlaying) VaultLime else VaultAmber)
                        )
                        Text(
                            text = "${item.extension.uppercase()} • ${mediaState.playbackSpeed}x",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = VaultPureWhite
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = VaultCharcoal)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatSeconds(mediaState.currentSeconds),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp,
                        color = VaultLime
                    )
                    Text(
                        text = "NYX2 PRIVATE VIDEO STREAM",
                        style = MaterialTheme.typography.labelMedium,
                        color = VaultPureWhite.copy(alpha = 0.65f)
                    )
                    Text(
                        text = formatSeconds(mediaState.totalSeconds),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp,
                        color = VaultPureWhite
                    )
                }

                Slider(
                    value = mediaState.currentSeconds.toFloat(),
                    onValueChange = { onSeekSeconds(it.toInt()) },
                    valueRange = 0f..mediaState.totalSeconds.coerceAtLeast(1).toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = VaultLime,
                        activeTrackColor = VaultLime,
                        inactiveTrackColor = VaultSlate
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onSeekSeconds((mediaState.currentSeconds - 10).coerceAtLeast(0)) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Mundur 10 detik",
                            tint = VaultPureWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .clickable { onTogglePlayPause() }
                            .testTag("video_viewer_play_pause"),
                        color = VaultLime
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (mediaState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Putar / Jeda Video",
                                tint = VaultObsidian,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { onSeekSeconds((mediaState.currentSeconds + 10).coerceAtMost(mediaState.totalSeconds)) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Maju 10 detik",
                            tint = VaultPureWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onCycleSpeed() },
                        color = VaultSlate
                    ) {
                        Text(
                            text = "${mediaState.playbackSpeed}x",
                            style = MaterialTheme.typography.labelLarge,
                            color = VaultLime,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveImageAndStickerViewport(item: VaultItemEntity) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val plainFile = remember(item.internalPlainPath) { File(item.internalPlainPath) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = VaultCharcoal),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, VaultSlate)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Checkerboard transparency backdrop for PNG / WEBP / Sticker
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val tile = 24.dp.toPx()
                    val cols = (size.width / tile).toInt() + 1
                    val rows = (size.height / tile).toInt() + 1
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            if ((r + c) % 2 == 0) {
                                drawRect(
                                    color = Color.White.copy(alpha = 0.03f),
                                    topLeft = Offset(c * tile, r * tile),
                                    size = Size(tile, tile)
                                )
                            }
                        }
                    }
                }

                val modelData: Any? = when {
                    plainFile.exists() -> plainFile
                    !item.mediaUriString.isNullOrBlank() -> Uri.parse(item.mediaUriString)
                    else -> null
                }

                if (modelData != null) {
                    AsyncImage(
                        model = modelData,
                        contentDescription = item.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                    )
                }

                Surface(
                    color = VaultObsidian.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "FORMAT: ${item.extension.uppercase()} • ZOOM: ${String.format(java.util.Locale.US, "%.1fx", scale)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = VaultLime,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = VaultCharcoal)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${item.name}.${item.extension}",
                        style = MaterialTheme.typography.titleMedium,
                        color = VaultPureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Cubit layar untuk Zoom atau gunakan tombol di kanan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VaultMintCard
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            scale = if (scale < 2f) 2.2f else 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                        .testTag("image_viewer_zoom_toggle"),
                    color = VaultLime
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (scale > 1f) Icons.Default.ZoomOutMap else Icons.Default.ZoomIn,
                            contentDescription = "Zoom",
                            tint = VaultObsidian,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (scale > 1f) "Reset 1x" else "Zoom 2x",
                            style = MaterialTheme.typography.labelLarge,
                            color = VaultObsidian
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DecryptedDocumentReaderViewport(item: VaultItemEntity) {
    val plainFile = remember(item.internalPlainPath) { File(item.internalPlainPath) }
    val isPdf = item.extension.equals("pdf", ignoreCase = true)

    // Render halaman-halaman PDF menggunakan Android PdfRenderer jika file adalah .PDF
    val pdfBitmaps = remember(plainFile, isPdf) {
        if (isPdf && plainFile.exists() && plainFile.length() > 64L) {
            val pages = mutableListOf<Bitmap>()
            try {
                ParcelFileDescriptor.open(plainFile, ParcelFileDescriptor.MODE_READ_ONLY)?.use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        val count = renderer.pageCount.coerceAtMost(10)
                        for (i in 0 until count) {
                            renderer.openPage(i).use { page ->
                                val bmp = Bitmap.createBitmap(
                                    page.width.coerceAtLeast(1),
                                    page.height.coerceAtLeast(1),
                                    Bitmap.Config.ARGB_8888
                                )
                                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                pages.add(bmp)
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }
            pages.toList()
        } else {
            emptyList()
        }
    }

    DisposableEffect(pdfBitmaps) {
        onDispose {
            pdfBitmaps.forEach { bmp ->
                if (!bmp.isRecycled) bmp.recycle()
            }
        }
    }

    val textContent = remember(plainFile, item.rawContentPreview, isPdf) {
        if (!isPdf && plainFile.exists() && plainFile.length() <= 256 * 1024L) {
            try {
                plainFile.readText(Charsets.UTF_8)
            } catch (_: Exception) {
                item.rawContentPreview
            }
        } else {
            item.rawContentPreview
        }
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = VaultCharcoal),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, VaultLime.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = VaultObsidian,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isPdf) {
                            "MORI PDF CAROUSEL READER • ${pdfBitmaps.size} HALAMAN"
                        } else {
                            "NYX2 HEADER & INTEGRITY VERIFIED"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = VaultLime
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cipher: ${item.cipherAlgorithm} | Tag: ${item.checksumPreview}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = VaultMintCard
                    )
                }
            }

            if (pdfBitmaps.isNotEmpty()) {
                pdfBitmaps.forEachIndexed { idx, pageBmp ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Halaman PDF #${idx + 1} dari ${pdfBitmaps.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = VaultLime
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = VaultObsidian)
                        ) {
                            Image(
                                bitmap = pageBmp.asImageBitmap(),
                                contentDescription = "Halaman PDF ${idx + 1}",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "ISI FILE TERDEKRIPSI:",
                    style = MaterialTheme.typography.labelLarge,
                    color = VaultAmber
                )

                Surface(
                    color = VaultObsidian,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = textContent,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = VaultPureWhite,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        }
    }
}
