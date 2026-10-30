package com.example.ui

import android.media.MediaPlayer
import android.widget.VideoView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.data.VaultCategory
import com.example.data.VaultItemEntity
import com.example.ui.theme.CosmicBorderHairline
import com.example.ui.theme.CosmicCardElevated
import com.example.ui.theme.CosmicGlass
import com.example.ui.theme.CosmicObsidian
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicTextMuted
import com.example.ui.theme.CosmicTextPure
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelPeach
import com.example.ui.theme.PastelRose
import com.example.ui.theme.PastelSky
import com.example.ui.theme.PastelYellow
import com.example.ui.theme.StarlightCyan
import java.io.File
import kotlin.math.sin

/**
 * Unified Media Hub:
 * 3-Way Apple Minimalist Sub-Tabs:
 *   0 -> [Video & Scoped Playlist]
 *   1 -> [Musik & Audio Stream]
 *   2 -> [Galeri Foto & Stiker]
 *
 * Strict Multi-Vault Isolation: all queries filtered strictly by `activeVaultId`.
 * Scoped Folders: Videos from other folders are never queued or played next!
 * Hold-to-2x Speed: Pressing and holding video accelerates playback to 2.0x with "2X CEPAT" HUD.
 */
@Composable
fun VaultMediaPlayerScreen(
    activeVaultId: Int,
    mediaState: MediaPlayerUiState,
    allItems: List<VaultItemEntity>,
    activeMediaFolder: String? = null,
    onSelectMediaFolder: (String?) -> Unit = {},
    onMoveMediaItemToPlaylist: (VaultItemEntity, String) -> Unit = { _, _ -> },
    onSetHold2xSpeed: (Boolean) -> Unit = {},
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
    // 0: Video, 1: Musik, 2: Galeri Foto
    var mediaSubTab by remember { mutableIntStateOf(0) }

    // Strict multi-vault isolation: items must belong to the active vault
    val vaultItems = remember(allItems, activeVaultId) {
        allItems.filter { it.vaultProfileId == activeVaultId }
    }

    val videoItems = remember(vaultItems) {
        vaultItems.filter { it.category == VaultCategory.VIDEO.name }
    }

    val audioItems = remember(vaultItems) {
        vaultItems.filter { it.category == VaultCategory.AUDIO.name }
    }

    val photoItems = remember(vaultItems) {
        vaultItems.filter { it.category == VaultCategory.IMAGE.name }
    }

    // Modal state for creating new playlist
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistInputName by remember { mutableStateOf("") }

    // Modal state for moving video into playlist
    var movingVideoItem by remember { mutableStateOf<VaultItemEntity?>(null) }
    var moveTargetPlaylistName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicVoid)
            .testTag("media_player_screen")
    ) {
        // --- 1. TOP APPLE MINIMALIST PASTEL SUB-TAB BAR ---
        Surface(
            color = CosmicObsidian,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediaSubTabPill(
                    label = "Video (${videoItems.size})",
                    icon = Icons.Default.VideoLibrary,
                    selected = mediaSubTab == 0,
                    activeColor = PastelSky,
                    onClick = { mediaSubTab = 0 }
                )
                MediaSubTabPill(
                    label = "Musik (${audioItems.size})",
                    icon = Icons.Default.MusicNote,
                    selected = mediaSubTab == 1,
                    activeColor = PastelLavender,
                    onClick = { mediaSubTab = 1 }
                )
                MediaSubTabPill(
                    label = "Foto (${photoItems.size})",
                    icon = Icons.Default.PhotoLibrary,
                    selected = mediaSubTab == 2,
                    activeColor = PastelPeach,
                    onClick = { mediaSubTab = 2 }
                )
            }
        }

        // --- 2. SUB-TAB VIEWPORT ---
        when (mediaSubTab) {
            0 -> {
                // SUB-TAB 0: VIDEO & SCOPED PLAYLISTS
                VideoStageAndScopedPlaylistView(
                    activeVaultId = activeVaultId,
                    videoItems = videoItems,
                    mediaState = mediaState,
                    activeMediaFolder = activeMediaFolder,
                    onSelectMediaFolder = onSelectMediaFolder,
                    onCreatePlaylistClick = {
                        newPlaylistInputName = ""
                        showCreatePlaylistDialog = true
                    },
                    onMoveItemClick = { item ->
                        movingVideoItem = item
                        moveTargetPlaylistName = item.groupArchiveName ?: ""
                    },
                    onSetHold2xSpeed = onSetHold2xSpeed,
                    onTogglePlayPause = onTogglePlayPause,
                    onSeekToSeconds = onSeekToSeconds,
                    onSkipTrack = onSkipTrack,
                    onSelectOrOpenMedia = onSelectOrOpenMedia,
                    onOpenFullscreenOverlay = onOpenFullscreenOverlay,
                    onRequestPinToggleEncrypt = onRequestPinToggleEncrypt,
                    onRequestExportOrShareWithBiometric = onRequestExportOrShareWithBiometric,
                    onCycleSpeed = onCycleSpeed,
                    onToggleRepeat = onToggleRepeat,
                    onImportMediaClick = onImportMediaOrZipClick
                )
            }

            1 -> {
                // SUB-TAB 1: DEDICATED AUDIO & MUSIC STREAM
                MusicStreamAndAudioPlaylistView(
                    activeVaultId = activeVaultId,
                    audioItems = audioItems,
                    mediaState = mediaState,
                    onTogglePlayPause = onTogglePlayPause,
                    onSeekToSeconds = onSeekToSeconds,
                    onSkipTrack = onSkipTrack,
                    onSelectOrOpenMedia = onSelectOrOpenMedia,
                    onRequestPinToggleEncrypt = onRequestPinToggleEncrypt,
                    onRequestExportOrShareWithBiometric = onRequestExportOrShareWithBiometric,
                    onToggleRepeat = onToggleRepeat,
                    onImportAudioClick = onImportMediaOrZipClick
                )
            }

            2 -> {
                // SUB-TAB 2: DEDICATED PHOTO & STICKER SQUIRCLE GALLERY
                PhotoGallerySquircleView(
                    activeVaultId = activeVaultId,
                    photoItems = photoItems,
                    onSelectOrOpenPhoto = onOpenFullscreenOverlay,
                    onRequestPinToggleEncrypt = onRequestPinToggleEncrypt,
                    onRequestExportOrShareWithBiometric = onRequestExportOrShareWithBiometric,
                    onImportPhotoClick = onImportMediaOrZipClick
                )
            }
        }
    }

    // Modal: Buat Playlist Baru
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = {
                Text("Buat Playlist / Folder Baru", color = CosmicTextPure, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Masukkan nama playlist baru untuk mengelompokkan video secara terisolasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CosmicTextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPlaylistInputName,
                        onValueChange = { newPlaylistInputName = it },
                        label = { Text("Nama Playlist / Folder") },
                        placeholder = { Text("misal: Video Belajar, Liburan, Favorit") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CosmicVoid,
                            unfocusedContainerColor = CosmicVoid,
                            focusedBorderColor = PastelSky,
                            unfocusedBorderColor = CosmicSurface,
                            focusedTextColor = CosmicTextPure,
                            unfocusedTextColor = CosmicTextPure
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newPlaylistInputName.trim()
                        if (name.isNotBlank()) {
                            onSelectMediaFolder(name)
                        }
                        showCreatePlaylistDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSky, contentColor = CosmicVoid),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Buat & Buka Playlist", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCreatePlaylistDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Batal", color = CosmicTextPure)
                }
            },
            containerColor = CosmicObsidian,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Modal: Pindahkan Video ke Playlist
    if (movingVideoItem != null) {
        val item = movingVideoItem!!
        val availablePlaylists = remember(videoItems) {
            videoItems.mapNotNull { it.groupArchiveName }.filter { it.isNotBlank() }.distinct()
        }

        AlertDialog(
            onDismissRequest = { movingVideoItem = null },
            title = {
                Text("Pindahkan ke Playlist", color = CosmicTextPure, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Pindahkan '${item.name}' ke folder/playlist pilihan agar video lain tidak ikut terputar saat folder dipilih.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CosmicTextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = moveTargetPlaylistName,
                        onValueChange = { moveTargetPlaylistName = it },
                        label = { Text("Nama Playlist Target") },
                        placeholder = { Text("Ketik nama folder/playlist") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CosmicVoid,
                            unfocusedContainerColor = CosmicVoid,
                            focusedBorderColor = PastelPeach,
                            unfocusedBorderColor = CosmicSurface,
                            focusedTextColor = CosmicTextPure,
                            unfocusedTextColor = CosmicTextPure
                        )
                    )

                    if (availablePlaylists.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Atau pilih playlist yang sudah ada:", style = MaterialTheme.typography.labelSmall, color = CosmicTextMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availablePlaylists.forEach { pName ->
                                Surface(
                                    color = if (moveTargetPlaylistName == pName) PastelPeach.copy(alpha = 0.25f) else CosmicSurface,
                                    shape = RoundedCornerShape(50),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (moveTargetPlaylistName == pName) PastelPeach else CosmicBorderHairline),
                                    modifier = Modifier.clickable { moveTargetPlaylistName = pName }
                                ) {
                                    Text(
                                        text = pName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (moveTargetPlaylistName == pName) PastelPeach else CosmicTextPure,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = moveTargetPlaylistName.trim()
                        if (target.isNotBlank()) {
                            onMoveMediaItemToPlaylist(item, target)
                        }
                        movingVideoItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PastelPeach, contentColor = CosmicVoid),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pindahkan Video", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { movingVideoItem = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Batal", color = CosmicTextPure)
                }
            },
            containerColor = CosmicObsidian,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun MediaSubTabPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) activeColor.copy(alpha = 0.18f) else Color.Transparent,
        shape = RoundedCornerShape(50),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) activeColor.copy(alpha = 0.65f) else CosmicBorderHairline
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) activeColor else CosmicTextMuted,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) activeColor else CosmicTextPure.copy(alpha = 0.75f),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * Sub-Tab 0: Video Stage, Hold-to-2x Gesture, Repeat Modes & Scoped Playlists.
 */
@Composable
private fun VideoStageAndScopedPlaylistView(
    activeVaultId: Int,
    videoItems: List<VaultItemEntity>,
    mediaState: MediaPlayerUiState,
    activeMediaFolder: String?,
    onSelectMediaFolder: (String?) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onMoveItemClick: (VaultItemEntity) -> Unit,
    onSetHold2xSpeed: (Boolean) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekToSeconds: (Int) -> Unit,
    onSkipTrack: (Boolean) -> Unit,
    onSelectOrOpenMedia: (VaultItemEntity) -> Unit,
    onOpenFullscreenOverlay: (VaultItemEntity) -> Unit,
    onRequestPinToggleEncrypt: (VaultItemEntity) -> Unit,
    onRequestExportOrShareWithBiometric: (VaultItemEntity, Boolean) -> Unit,
    onCycleSpeed: () -> Unit,
    onToggleRepeat: () -> Unit,
    onImportMediaClick: () -> Unit
) {
    // Collect all unique folder/playlist names from existing videos
    val existingPlaylists = remember(videoItems) {
        videoItems.mapNotNull { it.groupArchiveName }.filter { it.isNotBlank() }.distinct()
    }

    // STRICT FOLDER-SCOPED QUEUE:
    // If activeMediaFolder is selected, ONLY videos inside this folder are rendered and auto-played!
    val scopedVideoPlaylist = remember(videoItems, activeMediaFolder) {
        if (!activeMediaFolder.isNullOrBlank()) {
            videoItems.filter { it.groupArchiveName == activeMediaFolder }
        } else {
            videoItems
        }
    }

    var videoSearchQuery by remember { mutableStateOf("") }

    val displayedVideoPlaylist = remember(scopedVideoPlaylist, videoSearchQuery) {
        if (videoSearchQuery.isBlank()) scopedVideoPlaylist
        else scopedVideoPlaylist.filter { it.name.contains(videoSearchQuery, ignoreCase = true) }
    }

    val currentVideo = mediaState.currentTrack?.takeIf { it.category == VaultCategory.VIDEO.name }
        ?: displayedVideoPlaylist.firstOrNull() ?: scopedVideoPlaylist.firstOrNull()

    if (videoItems.isEmpty()) {
        EmptyMediaCategoryState(
            title = "Belum Ada Video di Brankas $activeVaultId",
            subtitle = "Impor video (MP4/MKV) atau unduh melalui Mori Downloader.",
            buttonText = "Impor Video ke Brankas",
            onClickImport = onImportMediaClick
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. DOCKED TOP 16:9 VIDEO STAGE WITH PRESS-AND-HOLD 2X SPEED
        if (currentVideo != null) {
            AppleMinimalistVideoStage(
                track = currentVideo,
                mediaState = mediaState,
                onSetHold2xSpeed = onSetHold2xSpeed,
                onTogglePlayPause = onTogglePlayPause,
                onSeekToSeconds = onSeekToSeconds,
                onSkipPrevious = { onSkipTrack(false) },
                onSkipNext = { onSkipTrack(true) },
                onCycleSpeed = onCycleSpeed,
                onToggleRepeat = onToggleRepeat,
                onOpenFullscreen = { onOpenFullscreenOverlay(currentVideo) },
                onRequestPinDecrypt = { onRequestPinToggleEncrypt(currentVideo) }
            )
        }

        // 2. FOLDER / PLAYLIST PICKER CHIPS ROW & UP NEXT LIST
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Folder / Playlist Selection Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (activeMediaFolder != null) "Folder: $activeMediaFolder" else "Semua Video (${scopedVideoPlaylist.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = CosmicTextPure,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = when (mediaState.repeatMode) {
                                PlaybackRepeatMode.REPEAT_ONE -> "🔁 Ulangi 1"
                                PlaybackRepeatMode.AUTO_ADVANCE -> "⏭️ Auto Lanjut"
                                PlaybackRepeatMode.REPEAT_PLAYLIST -> "🔁 Ulangi Folder"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = PastelSky
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "Semua Video" Chip
                        FilterChip(
                            selected = activeMediaFolder == null,
                            onClick = { onSelectMediaFolder(null) },
                            label = { Text("Semua (${videoItems.size})") },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelSky.copy(alpha = 0.22f),
                                selectedLabelColor = PastelSky,
                                containerColor = CosmicObsidian,
                                labelColor = CosmicTextMuted
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (activeMediaFolder == null) PastelSky else CosmicBorderHairline
                            )
                        )

                        // "+ Playlist Baru" Action Chip
                        Surface(
                            color = PastelPeach.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PastelPeach.copy(alpha = 0.6f)),
                            modifier = Modifier.clickable(onClick = onCreatePlaylistClick)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = PastelPeach, modifier = Modifier.size(15.dp))
                                Text("+ Playlist Baru", style = MaterialTheme.typography.labelMedium, color = PastelPeach, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Each existing playlist chip
                        existingPlaylists.forEach { pName ->
                            val count = videoItems.count { it.groupArchiveName == pName }
                            FilterChip(
                                selected = activeMediaFolder == pName,
                                onClick = { onSelectMediaFolder(pName) },
                                leadingIcon = {
                                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(15.dp))
                                },
                                label = { Text("$pName ($count)") },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PastelPeach.copy(alpha = 0.25f),
                                    selectedLabelColor = PastelPeach,
                                    containerColor = CosmicObsidian,
                                    labelColor = CosmicTextMuted
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (activeMediaFolder == pName) PastelPeach else CosmicBorderHairline
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // YouTube Video Search Bar
                    OutlinedTextField(
                        value = videoSearchQuery,
                        onValueChange = { videoSearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_search_input"),
                        placeholder = {
                            Text("Cari judul video dalam brankas ini...", color = CosmicTextMuted, style = MaterialTheme.typography.bodyMedium)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Cari", tint = PastelSky, modifier = Modifier.size(19.dp))
                        },
                        trailingIcon = {
                            if (videoSearchQuery.isNotBlank()) {
                                IconButton(onClick = { videoSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Hapus", tint = CosmicTextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CosmicGlass,
                            unfocusedContainerColor = CosmicGlass,
                            focusedBorderColor = PastelSky,
                            unfocusedBorderColor = CosmicBorderHairline,
                            focusedTextColor = CosmicTextPure,
                            unfocusedTextColor = CosmicTextPure
                        )
                    )
                }
            }

            // Up Next Videos Queue (Scoped strictly to chosen folder)
            if (scopedVideoPlaylist.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicObsidian)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Playlist '$activeMediaFolder' masih kosong.",
                                color = CosmicTextMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Pindahkan video ke playlist ini menggunakan tombol folder pada daftar video.",
                                color = CosmicTextMuted.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(displayedVideoPlaylist, key = { _, item -> item.id }) { index, item ->
                    val isPlayingNow = currentVideo?.id == item.id
                    ScopedVideoItemRow(
                        index = index + 1,
                        item = item,
                        isSelected = isPlayingNow,
                        isPlaying = isPlayingNow && mediaState.isPlaying,
                        onClickPlay = { onSelectOrOpenMedia(item) },
                        onMoveClick = { onMoveItemClick(item) },
                        onClickPinToggle = { onRequestPinToggleEncrypt(item) },
                        onClickShare = { onRequestExportOrShareWithBiometric(item, true) }
                    )
                }
            }
        }
    }
}

/**
 * Apple Minimalist Video Stage with Press-and-Hold 2x Acceleration.
 */
@Composable
private fun AppleMinimalistVideoStage(
    track: VaultItemEntity,
    mediaState: MediaPlayerUiState,
    onSetHold2xSpeed: (Boolean) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekToSeconds: (Int) -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onCycleSpeed: () -> Unit,
    onToggleRepeat: () -> Unit,
    onOpenFullscreen: () -> Unit,
    onRequestPinDecrypt: () -> Unit
) {
    val plainFile = remember(track.internalPlainPath) { File(track.internalPlainPath) }
    var videoDecodeError by remember(track.id, track.internalPlainPath) { mutableStateOf(false) }

    var showSeekLeftHUD by remember { mutableStateOf(false) }
    var showSeekRightHUD by remember { mutableStateOf(false) }

    LaunchedEffect(showSeekLeftHUD) {
        if (showSeekLeftHUD) {
            kotlinx.coroutines.delay(650)
            showSeekLeftHUD = false
        }
    }
    LaunchedEffect(showSeekRightHUD) {
        if (showSeekRightHUD) {
            kotlinx.coroutines.delay(650)
            showSeekRightHUD = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(CosmicVoid)
            // YOUTUBE-STYLE GESTURES: Double Tap Left (-5s), Double Tap Right (+5s), Hold (2x Speed)
            .pointerInput(track.id, mediaState.currentSeconds, mediaState.totalSeconds) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        val isRightSide = offset.x >= size.width / 2f
                        if (isRightSide) {
                            val nextSec = (mediaState.currentSeconds + 5).coerceAtMost(mediaState.totalSeconds)
                            onSeekToSeconds(nextSec)
                            showSeekRightHUD = true
                        } else {
                            val prevSec = (mediaState.currentSeconds - 5).coerceAtLeast(0)
                            onSeekToSeconds(prevSec)
                            showSeekLeftHUD = true
                        }
                    },
                    onPress = {
                        onSetHold2xSpeed(true)
                        tryAwaitRelease()
                        onSetHold2xSpeed(false)
                    }
                )
            }
    ) {
        if (track.isEncrypted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = PastelRose, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Video Terenkripsi AES-256",
                    style = MaterialTheme.typography.titleMedium,
                    color = CosmicTextPure,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Buka kunci untuk memutar: ${track.name}.${track.extension}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CosmicTextMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onRequestPinDecrypt,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelSky, contentColor = CosmicVoid)
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka PIN & Putar", fontWeight = FontWeight.Bold)
                }
            }
        } else if (!videoDecodeError && plainFile.exists()) {
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
                                mp.isLooping = mediaState.repeatMode == PlaybackRepeatMode.REPEAT_ONE
                                try {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                        val effectiveSpeed = if (mediaState.isHold2xActive) 2.0f else mediaState.playbackSpeed
                                        mp.playbackParams = mp.playbackParams.setSpeed(effectiveSpeed)
                                    }
                                } catch (_: Exception) {
                                }
                                if (mediaState.isPlaying) {
                                    start()
                                }
                            }
                            setOnCompletionListener {
                                if (mediaState.repeatMode == PlaybackRepeatMode.REPEAT_ONE) {
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

                            val currentPosSec = videoView.currentPosition / 1000
                            val diffSec = kotlin.math.abs(currentPosSec - mediaState.currentSeconds)
                            if (diffSec >= 3) {
                                videoView.seekTo(mediaState.currentSeconds * 1000)
                            }
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Placeholder / Audio preview stage
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CosmicSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.VideoFile, contentDescription = null, tint = PastelSky, modifier = Modifier.size(48.dp))
            }
        }

        // --- YOUTUBE 5S SEEK HUD OVERLAYS ---
        if (showSeekLeftHUD) {
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PastelSky),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 28.dp)
                    .size(64.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("⏪", fontSize = 16.sp)
                    Text("-5s", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showSeekRightHUD) {
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PastelSky),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 28.dp)
                    .size(64.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("⏩", fontSize = 16.sp)
                    Text("+5s", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- FLOATING HUD BADGE: "2X CEPAT" (Appears while holding finger on video) ---
        if (mediaState.isHold2xActive) {
            Surface(
                color = Color(0xEE090D1A),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PastelSky),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.FastForward, contentDescription = null, tint = PastelSky, modifier = Modifier.size(16.dp))
                    Text(
                        text = "⚡ 2X CEPAT",
                        style = MaterialTheme.typography.labelLarge,
                        color = PastelSky,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }
            }
        }

        // --- DOCKED MINIMALIST VIDEO CONTROLS OVERLAY ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC050814))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Column {
                // Seek Bar
                Slider(
                    value = mediaState.currentSeconds.toFloat(),
                    onValueChange = { onSeekToSeconds(it.toInt()) },
                    valueRange = 0f..mediaState.totalSeconds.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = PastelSky,
                        activeTrackColor = PastelSky,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current / Total time
                    Text(
                        text = "${formatSec(mediaState.currentSeconds)} / ${formatSec(mediaState.totalSeconds)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicTextPure,
                        fontFamily = JetBrainsMonoFontFamily
                    )

                    // Control buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onSkipPrevious) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Sebelumnya", tint = CosmicTextPure)
                        }

                        IconButton(onClick = onTogglePlayPause) {
                            Icon(
                                imageVector = if (mediaState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (mediaState.isPlaying) "Pause" else "Play",
                                tint = PastelSky,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(onClick = onSkipNext) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Selanjutnya", tint = CosmicTextPure)
                        }

                        // Repeat Mode Toggle
                        IconButton(onClick = onToggleRepeat) {
                            Icon(
                                imageVector = when (mediaState.repeatMode) {
                                    PlaybackRepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                                    PlaybackRepeatMode.AUTO_ADVANCE -> Icons.Default.SkipNext
                                    PlaybackRepeatMode.REPEAT_PLAYLIST -> Icons.Default.Repeat
                                },
                                contentDescription = "Mode Ulang",
                                tint = if (mediaState.repeatMode != PlaybackRepeatMode.AUTO_ADVANCE) PastelSky else CosmicTextPure.copy(alpha = 0.6f)
                            )
                        }

                        // Speed Cycle Button
                        Surface(
                            color = Color(0x44FFFFFF),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable(onClick = onCycleSpeed)
                        ) {
                            Text(
                                text = "${mediaState.playbackSpeed}x",
                                style = MaterialTheme.typography.labelSmall,
                                color = CosmicTextPure,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        IconButton(onClick = onOpenFullscreen) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = CosmicTextPure)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScopedVideoItemRow(
    index: Int,
    item: VaultItemEntity,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClickPlay: () -> Unit,
    onMoveClick: () -> Unit,
    onClickPinToggle: () -> Unit,
    onClickShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickPlay),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) androidx.compose.ui.graphics.Color(0xD918233C) else CosmicGlass
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) PastelSky else CosmicBorderHairline
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Index & Video Thumbnail/Icon
            Surface(
                color = if (isSelected) PastelSky.copy(alpha = 0.2f) else CosmicVoid,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isPlaying) {
                        Icon(Icons.Default.Equalizer, contentDescription = null, tint = PastelSky, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = if (isSelected) PastelSky else CosmicTextMuted)
                    }
                }
            }

            // Video Meta
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.name}.${item.extension}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) PastelSky else CosmicTextPure,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!item.groupArchiveName.isNullOrBlank()) {
                        Surface(
                            color = PastelPeach.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = item.groupArchiveName!!,
                                style = MaterialTheme.typography.labelSmall,
                                color = PastelPeach,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${item.sizeBytes / 1024} KB • ${formatSec(item.durationSeconds)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CosmicTextMuted
                    )
                }
            }

            // Actions: Move to Playlist, PIN Lock Toggle, Biometric Share
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onMoveClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DriveFileMove, contentDescription = "Pindahkan ke Playlist", tint = PastelPeach, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onClickPinToggle, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (item.isEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock/Unlock",
                        tint = if (item.isEncrypted) PastelRose else PastelMint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onClickShare, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Bagikan", tint = CosmicTextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/**
 * Sub-Tab 1: Dedicated Audio & Music Stream View with Waveform Visualizer.
 */
@Composable
private fun MusicStreamAndAudioPlaylistView(
    activeVaultId: Int,
    audioItems: List<VaultItemEntity>,
    mediaState: MediaPlayerUiState,
    onTogglePlayPause: () -> Unit,
    onSeekToSeconds: (Int) -> Unit,
    onSkipTrack: (Boolean) -> Unit,
    onSelectOrOpenMedia: (VaultItemEntity) -> Unit,
    onRequestPinToggleEncrypt: (VaultItemEntity) -> Unit,
    onRequestExportOrShareWithBiometric: (VaultItemEntity, Boolean) -> Unit,
    onToggleRepeat: () -> Unit,
    onImportAudioClick: () -> Unit
) {
    val currentAudio = mediaState.currentTrack?.takeIf { it.category == VaultCategory.AUDIO.name }
        ?: audioItems.firstOrNull()

    if (audioItems.isEmpty()) {
        EmptyMediaCategoryState(
            title = "Belum Ada Musik di Brankas $activeVaultId",
            subtitle = "Impor audio (MP3/WAV/M4A) atau unduh lagu audio via Mori.",
            buttonText = "Impor Lagu ke Brankas",
            onClickImport = onImportAudioClick
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Dedicated Audio Stage Card
        if (currentAudio != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicObsidian),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PastelLavender.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Music Vinyl / Album Art with Lavender Aura
                        Surface(
                            color = CosmicSurface,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .size(140.dp)
                                .border(2.dp, PastelLavender.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = PastelLavender,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "${currentAudio.name}.${currentAudio.extension}",
                            style = MaterialTheme.typography.titleLarge,
                            color = CosmicTextPure,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Format: ${currentAudio.extension.uppercase()} • Brankas $activeVaultId",
                            style = MaterialTheme.typography.bodySmall,
                            color = PastelLavender
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Waveform Animated Visualizer
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            val w = size.width
                            val h = size.height
                            val barCount = 32
                            val barW = (w / barCount) * 0.7f
                            for (i in 0 until barCount) {
                                val wave = if (mediaState.isPlaying) {
                                    0.25f + 0.70f * kotlin.math.abs(sin(i * 0.45f + mediaState.currentSeconds))
                                } else {
                                    0.25f
                                }
                                val barH = h * wave
                                drawRoundRect(
                                    color = if (i % 2 == 0) PastelLavender else PastelSky,
                                    topLeft = Offset(i * (w / barCount), h - barH),
                                    size = androidx.compose.ui.geometry.Size(barW, barH),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Audio Seek Slider
                        Slider(
                            value = mediaState.currentSeconds.toFloat(),
                            onValueChange = { onSeekToSeconds(it.toInt()) },
                            valueRange = 0f..mediaState.totalSeconds.toFloat().coerceAtLeast(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = PastelLavender,
                                activeTrackColor = PastelLavender,
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(formatSec(mediaState.currentSeconds), color = CosmicTextMuted, style = MaterialTheme.typography.labelSmall)
                            Text(formatSec(mediaState.totalSeconds), color = CosmicTextMuted, style = MaterialTheme.typography.labelSmall)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Playback Controls
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onToggleRepeat) {
                                Icon(Icons.Default.Repeat, contentDescription = null, tint = if (mediaState.isRepeatEnabled) PastelLavender else CosmicTextMuted)
                            }
                            IconButton(onClick = { onSkipTrack(false) }, modifier = Modifier.size(44.dp)) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = CosmicTextPure, modifier = Modifier.size(28.dp))
                            }
                            Button(
                                onClick = onTogglePlayPause,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = PastelLavender, contentColor = CosmicVoid),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Icon(
                                    imageVector = if (mediaState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            IconButton(onClick = { onSkipTrack(true) }, modifier = Modifier.size(44.dp)) {
                                Icon(Icons.Default.SkipNext, contentDescription = null, tint = CosmicTextPure, modifier = Modifier.size(28.dp))
                            }
                            IconButton(onClick = { onRequestPinToggleEncrypt(currentAudio) }) {
                                Icon(
                                    imageVector = if (currentAudio.isEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (currentAudio.isEncrypted) PastelRose else PastelMint
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Daftar Lagu di Brankas $activeVaultId (${audioItems.size})", style = MaterialTheme.typography.titleMedium, color = CosmicTextPure, fontWeight = FontWeight.Bold)
        }

        itemsIndexed(audioItems, key = { _, item -> item.id }) { index, item ->
            val isPlayingNow = currentAudio?.id == item.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectOrOpenMedia(item) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = if (isPlayingNow) CosmicSurface else CosmicObsidian),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isPlayingNow) PastelLavender else CosmicBorderHairline)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = if (isPlayingNow) PastelLavender.copy(alpha = 0.2f) else CosmicVoid,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlayingNow && mediaState.isPlaying) Icons.Default.Equalizer else Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (isPlayingNow) PastelLavender else CosmicTextMuted
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${item.name}.${item.extension}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isPlayingNow) PastelLavender else CosmicTextPure,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.sizeBytes / 1024} KB • ${formatSec(item.durationSeconds)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CosmicTextMuted
                        )
                    }

                    IconButton(onClick = { onRequestExportOrShareWithBiometric(item, true) }) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = CosmicTextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/**
 * Sub-Tab 2: Dedicated Photo & Sticker Squircle Gallery Grid.
 * Isolated strictly to active vault.
 */
@Composable
private fun PhotoGallerySquircleView(
    activeVaultId: Int,
    photoItems: List<VaultItemEntity>,
    onSelectOrOpenPhoto: (VaultItemEntity) -> Unit,
    onRequestPinToggleEncrypt: (VaultItemEntity) -> Unit,
    onRequestExportOrShareWithBiometric: (VaultItemEntity, Boolean) -> Unit,
    onImportPhotoClick: () -> Unit
) {
    if (photoItems.isEmpty()) {
        EmptyMediaCategoryState(
            title = "Belum Ada Foto di Brankas $activeVaultId",
            subtitle = "Impor foto, stiker, atau unduh gambar dari Mori untuk disimpan terenkripsi.",
            buttonText = "Impor Foto ke Brankas",
            onClickImport = onImportPhotoClick
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(photoItems, key = { it.id }) { item ->
            val plainFile = remember(item.internalPlainPath) { File(item.internalPlainPath) }

            Surface(
                color = CosmicObsidian,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (item.isEncrypted) PastelRose.copy(alpha = 0.5f) else CosmicBorderHairline),
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onSelectOrOpenPhoto(item) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (item.isEncrypted) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(6.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PastelRose, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.extension.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = PastelRose,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (plainFile.exists()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = plainFile),
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.Image, contentDescription = null, tint = PastelPeach, modifier = Modifier.size(32.dp))
                    }

                    // Bottom subtle title badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color(0xBB0A0F1D))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicTextPure,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMediaCategoryState(
    title: String,
    subtitle: String,
    buttonText: String,
    onClickImport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CosmicDragonAvatar(mood = MascotMood.IDLE_BREATHE, size = 96.dp)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = CosmicTextPure,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = CosmicTextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onClickImport,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PastelSky, contentColor = CosmicVoid)
        ) {
            Icon(Icons.Default.UploadFile, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(buttonText, fontWeight = FontWeight.Bold)
        }
    }
}

fun formatSeconds(totalSec: Int): String {
    val m = totalSec / 60
    val s = totalSec % 60
    return String.format("%02d:%02d", m, s)
}

fun formatSec(totalSec: Int): String = formatSeconds(totalSec)
