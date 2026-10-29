package com.example.ui

import android.widget.VideoView
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import com.example.ui.theme.VaultTextMuted
import java.io.File
import kotlin.math.sin

@Composable
fun VaultMediaPlayerScreen(
    activeVaultId: Int,
    mediaState: MediaPlayerUiState,
    allItems: List<VaultItemEntity>,
    onTogglePlayPause: () -> Unit,
    onSeekToSeconds: (Int) -> Unit,
    onSkipTrack: (Boolean) -> Unit,
    onSelectOrOpenMedia: (VaultItemEntity) -> Unit,
    onOpenFullscreenOverlay: (VaultItemEntity) -> Unit,
    onRequestPinToggleEncrypt: (VaultItemEntity) -> Unit,
    onRequestExportOrShareWithBiometric: (VaultItemEntity, Boolean) -> Unit,
    onCycleSpeed: () -> Unit,
    onToggleRepeat: () -> Unit,
    onImportMediaOrZipClick: () -> Unit
) {
    val allPlayableItems = remember(allItems) {
        allItems.filter {
            it.category == VaultCategory.VIDEO.name ||
                    it.category == VaultCategory.AUDIO.name ||
                    it.category == VaultCategory.IMAGE.name
        }
    }

    val currentTrack = mediaState.currentTrack ?: allPlayableItems.firstOrNull()
    val currentZipGroup = currentTrack?.groupArchiveName

    var playlistFilterMode by remember { mutableIntStateOf(0) }

    val displayedPlaylist = remember(allPlayableItems, currentZipGroup, playlistFilterMode) {
        when (playlistFilterMode) {
            1 -> {
                if (!currentZipGroup.isNullOrBlank()) {
                    allPlayableItems.filter { it.groupArchiveName == currentZipGroup }
                } else {
                    allPlayableItems
                }
            }
            2 -> allPlayableItems.filter { it.category == VaultCategory.VIDEO.name }
            3 -> allPlayableItems.filter { it.category != VaultCategory.VIDEO.name }
            else -> {
                if (!currentZipGroup.isNullOrBlank()) {
                    val sameZip = allPlayableItems.filter { it.groupArchiveName == currentZipGroup }
                    val others = allPlayableItems.filter { it.groupArchiveName != currentZipGroup }
                    sameZip + others
                } else {
                    allPlayableItems
                }
            }
        }
    }

    if (allPlayableItems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("media_player_empty_state"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            NyxMascotAvatar(mood = MascotMood.WAVE_HELLO, size = 104.dp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Belum Ada Media di Brankas $activeVaultId",
                style = MaterialTheme.typography.headlineMedium,
                color = VaultObsidian,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Impor video (MP4/MOV), audio, gambar, atau paket .ZIP untuk memutar dan mengekspornya dengan aman.",
                style = MaterialTheme.typography.bodyMedium,
                color = VaultTextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onImportMediaOrZipClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("player_empty_import_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VaultObsidian,
                    contentColor = VaultLime
                )
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Impor Video atau Paket .ZIP", style = MaterialTheme.typography.labelLarge)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("media_player_screen")
    ) {
        // 1. DOCKED TOP 16:9 VIDEO & MEDIA STAGE
        if (currentTrack != null) {
            YouTubeDockedVideoStage(
                track = currentTrack,
                mediaState = mediaState,
                onTogglePlayPause = onTogglePlayPause,
                onSeekToSeconds = onSeekToSeconds,
                onSkipPrevious = { onSkipTrack(false) },
                onSkipNext = { onSkipTrack(true) },
                onCycleSpeed = onCycleSpeed,
                onToggleRepeat = onToggleRepeat,
                onOpenFullscreen = { onOpenFullscreenOverlay(currentTrack) },
                onRequestPinDecrypt = { onRequestPinToggleEncrypt(currentTrack) }
            )
        }

        // 2. MINIMALIST SCROLLABLE TRACK CONTROLS & PLAYLIST
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (currentTrack != null) {
                item {
                    ActiveTrackMetadataCard(
                        track = currentTrack,
                        activeVaultId = activeVaultId,
                        onRequestPinToggleEncrypt = { onRequestPinToggleEncrypt(currentTrack) },
                        onRequestShareWithBiometric = {
                            onRequestExportOrShareWithBiometric(currentTrack, true)
                        },
                        onRequestExportWithBiometric = {
                            onRequestExportOrShareWithBiometric(currentTrack, false)
                        },
                        onOpenFullscreen = { onOpenFullscreenOverlay(currentTrack) },
                        onImportMoreClick = onImportMediaOrZipClick
                    )
                }
            }

            item {
                NyxMascotCompanionBanner(
                    activeVaultId = activeVaultId,
                    headline = if (mediaState.isPlaying) {
                        "Asyik! Sedang memutar ${currentTrack?.name ?: "Media"}"
                    } else {
                        "Ketuk video di bawah untuk langsung menonton!"
                    },
                    subtitle = "Ketuk maskot untuk lompatan selebrasi ala Duolingo",
                    mood = if (mediaState.isPlaying) MascotMood.HAPPY_VIBES else MascotMood.WAVE_HELLO
                )
            }

            // Minimalist Playlist Header & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistPlay,
                                contentDescription = null,
                                tint = VaultObsidian
                            )
                            Text(
                                text = if (!currentZipGroup.isNullOrBlank()) {
                                    "Playlist ZIP & Brankas (${displayedPlaylist.size})"
                                } else {
                                    "Antrean Media (${displayedPlaylist.size})"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = VaultObsidian,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        NyxMascotAvatar(
                            mood = if (mediaState.isPlaying) MascotMood.HAPPY_VIBES else MascotMood.GUARD_ALERT,
                            size = 32.dp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = playlistFilterMode == 0,
                            onClick = { playlistFilterMode = 0 },
                            label = { Text("Semua (${allPlayableItems.size})") },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VaultObsidian,
                                selectedLabelColor = VaultLime
                            )
                        )

                        if (!currentZipGroup.isNullOrBlank()) {
                            val sameZipCount = allPlayableItems.count { it.groupArchiveName == currentZipGroup }
                            FilterChip(
                                selected = playlistFilterMode == 1,
                                onClick = { playlistFilterMode = 1 },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FolderZip,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                },
                                label = { Text("ZIP: $currentZipGroup ($sameZipCount)") },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VaultLime,
                                    selectedLabelColor = VaultObsidian
                                )
                            )
                        }

                        FilterChip(
                            selected = playlistFilterMode == 2,
                            onClick = { playlistFilterMode = 2 },
                            label = { Text("Video") },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VaultObsidian,
                                selectedLabelColor = VaultLime
                            )
                        )

                        FilterChip(
                            selected = playlistFilterMode == 3,
                            onClick = { playlistFilterMode = 3 },
                            label = { Text("Audio & Gambar") },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VaultObsidian,
                                selectedLabelColor = VaultLime
                            )
                        )
                    }
                }
            }

            // Minimalist Up Next Video / Media Rows with Export/Share Button
            itemsIndexed(displayedPlaylist, key = { _, item -> item.id }) { index, item ->
                val isCurrentlySelected = currentTrack?.id == item.id
                val isSameZipAsCurrent = !currentZipGroup.isNullOrBlank() && item.groupArchiveName == currentZipGroup

                YouTubeUpNextVideoRow(
                    index = index + 1,
                    item = item,
                    isSelected = isCurrentlySelected,
                    isPlaying = isCurrentlySelected && mediaState.isPlaying,
                    isSameZipSibling = isSameZipAsCurrent,
                    onClickPlay = { onSelectOrOpenMedia(item) },
                    onClickPinLockToggle = { onRequestPinToggleEncrypt(item) },
                    onClickShareWithBiometric = {
                        onRequestExportOrShareWithBiometric(item, true)
                    },
                    onClickExportWithBiometric = {
                        onRequestExportOrShareWithBiometric(item, false)
                    }
                )
            }
        }
    }
}

@Composable
private fun YouTubeDockedVideoStage(
    track: VaultItemEntity,
    mediaState: MediaPlayerUiState,
    onTogglePlayPause: () -> Unit,
    onSeekToSeconds: (Int) -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onCycleSpeed: () -> Unit,
    onToggleRepeat: () -> Unit,
    onOpenFullscreen: () -> Unit,
    onRequestPinDecrypt: () -> Unit
) {
    val isVideo = track.category == VaultCategory.VIDEO.name
    val isAudio = track.category == VaultCategory.AUDIO.name
    val plainFile = remember(track.internalPlainPath) { File(track.internalPlainPath) }
    var videoDecodeError by remember(track.id, track.internalPlainPath) { mutableStateOf(false) }

    // Pemutar Audio Nyata (MediaPlayer) untuk file WAV / MP3 / M4A dari Mori atau Brankas
    val audioPlayerRef = remember(track.id, track.internalPlainPath, track.isEncrypted) {
        if (isAudio && !track.isEncrypted && plainFile.exists() && plainFile.length() > 64L) {
            try {
                MediaPlayer().apply {
                    setDataSource(plainFile.absolutePath)
                    isLooping = true
                    prepare()
                }
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    LaunchedEffect(audioPlayerRef, mediaState.isPlaying, mediaState.currentSeconds, mediaState.playbackSpeed) {
        try {
            if (audioPlayerRef != null) {
                if (mediaState.isPlaying && !audioPlayerRef.isPlaying) {
                    audioPlayerRef.start()
                } else if (!mediaState.isPlaying && audioPlayerRef.isPlaying) {
                    audioPlayerRef.pause()
                }

                // Two-way seek sync for audio
                val audioPosSec = audioPlayerRef.currentPosition / 1000
                if (kotlin.math.abs(audioPosSec - mediaState.currentSeconds) > 1) {
                    audioPlayerRef.seekTo(mediaState.currentSeconds * 1000)
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    audioPlayerRef.playbackParams = audioPlayerRef.playbackParams.setSpeed(mediaState.playbackSpeed)
                }
            }
        } catch (_: Exception) {
        }
    }

    DisposableEffect(audioPlayerRef) {
        onDispose {
            try {
                audioPlayerRef?.stop()
            } catch (_: Exception) {
            }
            try {
                audioPlayerRef?.release()
            } catch (_: Exception) {
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(VaultObsidian)
    ) {
        if (track.isEncrypted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NyxMascotAvatar(mood = MascotMood.GUARD_ALERT, size = 56.dp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "File Terenkripsi NYX2",
                    style = MaterialTheme.typography.titleMedium,
                    color = VaultPureWhite
                )
                Text(
                    text = "Masukkan PIN untuk memutar '${track.name}.${track.extension}'",
                    style = MaterialTheme.typography.bodySmall,
                    color = VaultPureWhite.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onRequestPinDecrypt,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VaultLime,
                        contentColor = VaultObsidian
                    ),
                    modifier = Modifier.testTag("decrypt_and_play_button")
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka PIN & Putar", style = MaterialTheme.typography.labelLarge)
                }
            }
        } else if (isVideo && !videoDecodeError && plainFile.exists()) {
            key(track.id, track.internalPlainPath) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setOnErrorListener { _, _, _ ->
                                videoDecodeError = true
                                true
                            }
                            setVideoPath(plainFile.absolutePath)
                            setOnPreparedListener { mp ->
                                mp.isLooping = mediaState.isRepeatEnabled
                                try {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                        mp.playbackParams = mp.playbackParams.setSpeed(mediaState.playbackSpeed)
                                    }
                                } catch (_: Exception) {
                                }
                                if (mediaState.isPlaying) {
                                    start()
                                }
                            }
                            setOnCompletionListener {
                                if (mediaState.isRepeatEnabled) {
                                    start()
                                } else {
                                    onSkipNext()
                                }
                            }
                        }
                    },
                    update = { videoView ->
                        try {
                            if (mediaState.isPlaying && !videoView.isPlaying) {
                                videoView.start()
                            } else if (!mediaState.isPlaying && videoView.isPlaying) {
                                videoView.pause()
                            }

                            // Two-way synchronized seeking
                            val currentPosSec = videoView.currentPosition / 1000
                            if (kotlin.math.abs(currentPosSec - mediaState.currentSeconds) > 1) {
                                videoView.seekTo(mediaState.currentSeconds * 1000)
                            }
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF141E24), Color(0xFF0E1317))
                    )
                )
                val barCount = 28
                val barW = (w * 0.76f) / barCount
                val startX = w * 0.12f
                val centerY = h * 0.46f
                for (i in 0 until barCount) {
                    val wave = if (mediaState.isPlaying) {
                        0.2f + 0.75f * kotlin.math.abs(sin((i * 0.55f) + mediaState.currentSeconds))
                    } else 0.18f
                    val barH = (h * 0.42f) * wave
                    drawLine(
                        color = if (i % 2 == 0) VaultLime else VaultMintCard,
                        start = Offset(startX + i * barW, centerY - barH * 0.5f),
                        end = Offset(startX + i * barW, centerY + barH * 0.5f),
                        strokeWidth = (barW * 0.55f).coerceAtLeast(4f),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        if (!track.isEncrypted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(VaultObsidian.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = VaultLime,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = track.extension.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = VaultObsidian,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (!track.groupArchiveName.isNullOrBlank()) {
                        Surface(
                            color = VaultObsidian.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ZIP: ${track.groupArchiveName}",
                                fontSize = 10.sp,
                                color = VaultMintCard,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(VaultObsidian.copy(alpha = 0.75f))
                            .instantTouchPress { onCycleSpeed() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("player_speed_button")
                    ) {
                        Text(
                            text = "${mediaState.playbackSpeed}x",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = VaultLime
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (mediaState.isRepeatEnabled) VaultLime else VaultObsidian.copy(alpha = 0.75f)
                            )
                            .instantTouchPress { onToggleRepeat() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("player_repeat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Loop",
                            tint = if (mediaState.isRepeatEnabled) VaultObsidian else VaultPureWhite,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(VaultObsidian.copy(alpha = 0.75f))
                            .instantTouchPress { onOpenFullscreen() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("open_fullscreen_viewer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Layar Penuh",
                            tint = VaultPureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, VaultObsidian.copy(alpha = 0.92f))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onSkipPrevious,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("player_prev_button")
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Sebelumnya", tint = VaultPureWhite)
                        }

                        IconButton(
                            onClick = { onSeekToSeconds((mediaState.currentSeconds - 10).coerceAtLeast(0)) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Replay10, contentDescription = "-10s", tint = VaultPureWhite)
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(VaultLime)
                                .instantTouchPress { onTogglePlayPause() }
                                .testTag("player_play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (mediaState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Putar/Jeda",
                                tint = VaultObsidian,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { onSeekToSeconds((mediaState.currentSeconds + 10).coerceAtMost(mediaState.totalSeconds)) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Forward10, contentDescription = "+10s", tint = VaultPureWhite)
                        }

                        IconButton(
                            onClick = onSkipNext,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("player_next_button")
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Berikutnya", tint = VaultPureWhite)
                        }
                    }

                    Text(
                        text = "${formatSeconds(mediaState.currentSeconds)} / ${formatSeconds(mediaState.totalSeconds)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = VaultPureWhite,
                        modifier = Modifier.testTag("player_time_readout")
                    )
                }

                Slider(
                    value = mediaState.currentSeconds.toFloat(),
                    onValueChange = { onSeekToSeconds(it.toInt()) },
                    valueRange = 0f..mediaState.totalSeconds.coerceAtLeast(1).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = VaultCoral,
                        activeTrackColor = VaultCoral,
                        inactiveTrackColor = VaultPureWhite.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

@Composable
private fun ActiveTrackMetadataCard(
    track: VaultItemEntity,
    activeVaultId: Int,
    onRequestPinToggleEncrypt: () -> Unit,
    onRequestShareWithBiometric: () -> Unit,
    onRequestExportWithBiometric: () -> Unit,
    onOpenFullscreen: () -> Unit,
    onImportMoreClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = VaultPureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, VaultObsidian.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "${track.name}.${track.extension}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VaultObsidian,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = buildString {
                    append("Brankas $activeVaultId • ${formatFileSize(track.sizeBytes)}")
                    if (!track.groupArchiveName.isNullOrBlank()) {
                        append(" • ZIP: ${track.groupArchiveName}")
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                color = VaultTextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Clean Action Pills: Bagikan (Biometrik), Ekspor (Biometrik), Kunci/Dekripsi, Layar Penuh
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share Individual File with Secondary Biometric Re-Auth
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(VaultObsidian)
                        .instantTouchPress { onRequestShareWithBiometric() }
                        .padding(horizontal = 13.dp, vertical = 8.dp)
                        .testTag("player_share_biometric_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan File",
                            tint = VaultLime,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Bagikan (Sidik Jari)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = VaultPureWhite
                        )
                    }
                }

                // Export Individual File to Device Storage with Secondary Biometric Re-Auth
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(VaultLime)
                        .instantTouchPress { onRequestExportWithBiometric() }
                        .padding(horizontal = 13.dp, vertical = 8.dp)
                        .testTag("player_export_biometric_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Ekspor File",
                            tint = VaultObsidian,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Ekspor File (Sidik Jari)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = VaultObsidian
                        )
                    }
                }

                // Toggle Encrypt/Decrypt with PIN
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(VaultMintCard)
                        .instantTouchPress { onRequestPinToggleEncrypt() }
                        .padding(horizontal = 13.dp, vertical = 8.dp)
                        .testTag("player_toggle_encrypt_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (track.isEncrypted) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = VaultObsidian,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (track.isEncrypted) "Buka PIN" else "Kunci PIN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = VaultObsidian
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(VaultCream)
                        .border(1.dp, VaultObsidian.copy(alpha = 0.16f), RoundedCornerShape(50))
                        .instantTouchPress { onOpenFullscreen() }
                        .padding(horizontal = 13.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = null,
                            tint = VaultObsidian,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Layar Penuh",
                            style = MaterialTheme.typography.labelMedium,
                            color = VaultObsidian
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(VaultCream)
                        .border(1.dp, VaultObsidian.copy(alpha = 0.16f), RoundedCornerShape(50))
                        .instantTouchPress { onImportMoreClick() }
                        .padding(horizontal = 13.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = VaultObsidian,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "+ Impor",
                            style = MaterialTheme.typography.labelMedium,
                            color = VaultObsidian
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YouTubeUpNextVideoRow(
    index: Int,
    item: VaultItemEntity,
    isSelected: Boolean,
    isPlaying: Boolean,
    isSameZipSibling: Boolean,
    onClickPlay: () -> Unit,
    onClickPinLockToggle: () -> Unit,
    onClickShareWithBiometric: () -> Unit,
    onClickExportWithBiometric: () -> Unit
) {
    val cat = VaultCategory.entries.find { it.name == item.category } ?: VaultCategory.VIDEO

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fastZeroLatencyClick { onClickPlay() }
            .testTag("media_queue_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) VaultObsidian else VaultPureWhite
        ),
        border = if (!isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = if (isSameZipSibling) 1.2.dp else 1.dp,
                color = if (isSameZipSibling) VaultMintAccent.copy(alpha = 0.4f) else VaultObsidian.copy(alpha = 0.08f)
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Compact Thumbnail Box
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) VaultCharcoal else Color(0xFF1A2228)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isPlaying -> Icons.Default.Equalizer
                        cat == VaultCategory.VIDEO -> Icons.Default.VideoFile
                        cat == VaultCategory.IMAGE -> Icons.Default.Image
                        else -> Icons.Default.AudioFile
                    },
                    contentDescription = null,
                    tint = if (isSelected) VaultLime else VaultMintCard,
                    modifier = Modifier.size(24.dp)
                )

                Surface(
                    color = VaultObsidian.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(3.dp)
                ) {
                    Text(
                        text = "#$index",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = VaultLime,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                Surface(
                    color = VaultObsidian.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(3.dp)
                ) {
                    Text(
                        text = if (item.durationSeconds > 0) {
                            formatSeconds(item.durationSeconds)
                        } else {
                            item.extension.uppercase()
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = VaultPureWhite,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            // Title & Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.name}.${item.extension}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) VaultPureWhite else VaultObsidian,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!item.groupArchiveName.isNullOrBlank()) {
                    Text(
                        text = "ZIP: ${item.groupArchiveName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) VaultLime else VaultMintAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "${formatFileSize(item.sizeBytes)} • ${if (item.isEncrypted) "Terkunci" else "Terbuka"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) VaultPureWhite.copy(alpha = 0.65f) else VaultTextMuted
                )
            }

            // Share / Export Biometric Quick Actions + Play/Unlock Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) VaultCharcoal else VaultCream)
                        .instantTouchPress { onClickShareWithBiometric() }
                        .testTag("playlist_share_item_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Bagikan dengan Sidik Jari",
                        tint = if (isSelected) VaultLime else VaultObsidian,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) VaultCharcoal else VaultMintCard)
                        .instantTouchPress { onClickExportWithBiometric() }
                        .testTag("playlist_export_item_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Ekspor dengan Sidik Jari",
                        tint = if (isSelected) VaultLime else VaultObsidian,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            when {
                                item.isEncrypted -> VaultCoral
                                isSelected -> VaultLime
                                else -> VaultObsidian
                            }
                        )
                        .instantTouchPress {
                            if (item.isEncrypted) onClickPinLockToggle() else onClickPlay()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                item.isEncrypted -> Icons.Default.LockOpen
                                isPlaying -> Icons.Default.Pause
                                else -> Icons.Default.PlayArrow
                            },
                            contentDescription = null,
                            tint = if (item.isEncrypted) VaultPureWhite else if (isSelected) VaultObsidian else VaultLime,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = when {
                                item.isEncrypted -> "PIN"
                                isPlaying -> "Aktif"
                                else -> "Putar"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isEncrypted) VaultPureWhite else if (isSelected) VaultObsidian else VaultPureWhite
                        )
                    }
                }
            }
        }
    }
}

fun formatSeconds(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val mins = safe / 60
    val secs = safe % 60
    return "%02d:%02d".format(mins, secs)
}
