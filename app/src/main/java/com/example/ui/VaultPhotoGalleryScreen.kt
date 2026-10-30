package com.example.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.VaultCategory
import com.example.data.VaultItemEntity
import com.example.ui.theme.CosmicBorderHairline
import com.example.ui.theme.CosmicObsidian
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicTextMuted
import com.example.ui.theme.CosmicTextPure
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelRose
import com.example.ui.theme.PastelSky
import com.example.ui.theme.StarlightCyan
import java.io.File

/**
 * Tab Galeri Foto Khusus (Dedicated Photo Gallery Tab):
 * 1. Isolasi Penuh Multi-Brankas: Hanya menampilkan foto milik Brankas Aktif (activeVaultId).
 * 2. Grid Squircle 3-Kolom Apple Minimalist dengan preview thumbnail instan.
 * 3. Filter Album / Subfolder terorganisir di bagian atas.
 * 4. Pencarian foto instan berdasarkan nama.
 * 5. Lightbox Fullscreen interaktif dengan gesture Multi-Touch Pinch-to-Zoom & Pan.
 */
@Composable
fun VaultPhotoGalleryScreen(
    activeVaultId: Int,
    allItems: List<VaultItemEntity>,
    onOpenMediaItem: (VaultItemEntity) -> Unit,
    onRequestPinToggleEncrypt: (VaultItemEntity) -> Unit,
    onRequestExportOrShareWithBiometric: (VaultItemEntity, Boolean) -> Unit,
    onDeleteItem: (VaultItemEntity) -> Unit
) {
    // 1. Strict vault isolation: hanya ambil file gambar milik brankas aktif
    val imageExtensions = remember { setOf("jpg", "jpeg", "png", "webp", "heic", "gif", "bmp", "svg") }
    val vaultPhotos = remember(allItems, activeVaultId) {
        allItems.filter { item ->
            item.vaultProfileId == activeVaultId &&
                    (item.category == VaultCategory.IMAGE.name || item.extension.lowercase() in imageExtensions)
        }
    }

    var selectedAlbum by remember { mutableStateOf<String?>(null) }
    var photoSearchQuery by remember { mutableStateOf("") }
    var activeLightboxPhoto by remember { mutableStateOf<VaultItemEntity?>(null) }

    // Ekstrak daftar album / folder dari foto di brankas
    val albums = remember(vaultPhotos) {
        vaultPhotos.mapNotNull { it.groupArchiveName?.ifBlank { null } }.distinct().sorted()
    }

    // Filter berdasarkan album dan query pencarian
    val displayedPhotos = remember(vaultPhotos, selectedAlbum, photoSearchQuery) {
        vaultPhotos.filter { item ->
            val matchAlbum = selectedAlbum == null || item.groupArchiveName.equals(selectedAlbum, ignoreCase = true)
            val matchQuery = photoSearchQuery.isBlank() ||
                    item.name.contains(photoSearchQuery, ignoreCase = true) ||
                    item.extension.contains(photoSearchQuery, ignoreCase = true)
            matchAlbum && matchQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicVoid)
            .testTag("vault_photo_gallery_screen")
    ) {
        // --- 1. HEADER & SEARCH BAR GALAXY GLASSMORPHISM ---
        Surface(
            color = CosmicObsidian,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = PastelSky.copy(alpha = 0.18f),
                            shape = CircleShape,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Collections,
                                    contentDescription = null,
                                    tint = PastelSky,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Galeri Foto Brankas $activeVaultId",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CosmicTextPure
                            )
                            Text(
                                text = "${displayedPhotos.size} foto terenkripsi • Terisolasi",
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicTextMuted
                            )
                        }
                    }

                    Surface(
                        color = PastelMint.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "BRANKAS $activeVaultId",
                            style = MaterialTheme.typography.labelSmall,
                            color = PastelMint,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // YouTube / Apple Minimalist Search Bar
                OutlinedTextField(
                    value = photoSearchQuery,
                    onValueChange = { photoSearchQuery = it },
                    placeholder = { Text("Cari nama foto atau album...", color = CosmicTextMuted, style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Cari", tint = PastelSky, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (photoSearchQuery.isNotBlank()) {
                            IconButton(onClick = { photoSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus", tint = CosmicTextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CosmicSurface,
                        unfocusedContainerColor = CosmicSurface,
                        focusedBorderColor = PastelSky.copy(alpha = 0.8f),
                        unfocusedBorderColor = CosmicBorderHairline,
                        focusedTextColor = CosmicTextPure,
                        unfocusedTextColor = CosmicTextPure
                    )
                )
            }
        }

        // --- 2. ALBUM FILTER ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isAllSelected = selectedAlbum == null
            Surface(
                color = if (isAllSelected) PastelSky else CosmicSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isAllSelected) PastelSky else CosmicBorderHairline),
                modifier = Modifier.clickable { selectedAlbum = null }
            ) {
                Text(
                    text = "Semua Foto (${vaultPhotos.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isAllSelected) CosmicVoid else CosmicTextPure,
                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            albums.forEach { albumName ->
                val isSelected = selectedAlbum == albumName
                val count = vaultPhotos.count { it.groupArchiveName.equals(albumName, ignoreCase = true) }
                Surface(
                    color = if (isSelected) PastelLavender else CosmicSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PastelLavender else CosmicBorderHairline),
                    modifier = Modifier.clickable { selectedAlbum = albumName }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (isSelected) CosmicVoid else PastelLavender,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$albumName ($count)",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) CosmicVoid else CosmicTextPure,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // --- 3. 3-COLUMN SQUIRCLE PHOTO GRID ---
        if (displayedPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CosmicDragonAvatar(mood = MascotMood.IDLE_BREATHE, size = 96.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (photoSearchQuery.isNotBlank()) "Tidak ada foto yang cocok" else "Belum Ada Foto di Brankas $activeVaultId",
                        style = MaterialTheme.typography.titleMedium,
                        color = CosmicTextPure,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Impor foto dari galeri HP Anda untuk dienkripsi dengan keamanan tingkat tinggi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CosmicTextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedPhotos, key = { it.id }) { photo ->
                    PhotoSquircleCard(
                        photo = photo,
                        onClick = { activeLightboxPhoto = photo },
                        onRequestPinToggleEncrypt = { onRequestPinToggleEncrypt(photo) }
                    )
                }
            }
        }
    }

    // --- 4. LIGHTBOX FULLSCREEN PINCH-TO-ZOOM OVERLAY ---
    activeLightboxPhoto?.let { photo ->
        PhotoLightboxDialog(
            photo = photo,
            allAlbumPhotos = displayedPhotos,
            onDismiss = { activeLightboxPhoto = null },
            onSelectPhoto = { activeLightboxPhoto = it },
            onRequestPinToggleEncrypt = { onRequestPinToggleEncrypt(photo) },
            onRequestExportOrShare = { isShare -> onRequestExportOrShareWithBiometric(photo, isShare) },
            onDeletePhoto = {
                onDeleteItem(photo)
                activeLightboxPhoto = null
            }
        )
    }
}

@Composable
private fun PhotoSquircleCard(
    photo: VaultItemEntity,
    onClick: () -> Unit,
    onRequestPinToggleEncrypt: () -> Unit
) {
    val plainFile = remember(photo.internalPlainPath) { File(photo.internalPlainPath) }
    val bitmap = remember(photo.id, photo.isEncrypted) {
        if (!photo.isEncrypted && plainFile.exists()) {
            try {
                BitmapFactory.decodeFile(plainFile.absolutePath)
            } catch (_: Exception) {
                null
            }
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, CosmicBorderHairline, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("photo_card_${photo.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicObsidian)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = photo.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CosmicSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = PastelSky.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Lock / Encrypted Badge Indicator
            if (photo.isEncrypted) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Terenkripsi",
                            tint = PastelRose,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Interactive Lightbox with Pinch-to-Zoom & Pan
 */
@Composable
private fun PhotoLightboxDialog(
    photo: VaultItemEntity,
    allAlbumPhotos: List<VaultItemEntity>,
    onDismiss: () -> Unit,
    onSelectPhoto: (VaultItemEntity) -> Unit,
    onRequestPinToggleEncrypt: () -> Unit,
    onRequestExportOrShare: (Boolean) -> Unit,
    onDeletePhoto: () -> Unit
) {
    val plainFile = remember(photo.internalPlainPath) { File(photo.internalPlainPath) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    val bitmap = remember(photo.id, photo.isEncrypted) {
        if (!photo.isEncrypted && plainFile.exists()) {
            try {
                BitmapFactory.decodeFile(plainFile.absolutePath)
            } catch (_: Exception) {
                null
            }
        } else null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
        ) {
            // Top Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${photo.name}.${photo.extension}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatFileSize(photo.sizeBytes)} • ${if (photo.isEncrypted) "Terenkripsi NYX2" else "Terbuka"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { onRequestExportOrShare(true) }) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan", tint = PastelSky)
                    }
                    IconButton(onClick = onDeletePhoto) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = PastelRose)
                    }
                }
            }

            // Central Zoomable Image Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                if (photo.isEncrypted) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = PastelRose, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Foto Terenkripsi AES-256",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Buka kunci menggunakan PIN brankas untuk melihat foto ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onRequestPinToggleEncrypt,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PastelSky, contentColor = CosmicVoid)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka PIN & Tampilkan", fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = photo.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(photo.id) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    zoomScale = (zoomScale * zoom).coerceIn(1f, 5f)
                                    if (zoomScale > 1f) {
                                        panOffsetX += pan.x
                                        panOffsetY += pan.y
                                    } else {
                                        panOffsetX = 0f
                                        panOffsetY = 0f
                                    }
                                }
                            }
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = panOffsetX
                                translationY = panOffsetY
                            }
                    )
                } else {
                    Text("Gagal memuat gambar", color = Color.White)
                }
            }

            // Bottom Reset Zoom button if zoomed
            if (zoomScale > 1.1f) {
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .clickable {
                            zoomScale = 1f
                            panOffsetX = 0f
                            panOffsetY = 0f
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = PastelSky, modifier = Modifier.size(16.dp))
                        Text("Reset Zoom (100%)", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
            }
        }
    }
}
