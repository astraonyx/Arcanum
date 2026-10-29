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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ApkMetadataSummary
import com.example.data.ArchiveEntryItem
import com.example.data.MtExplorerEngine
import com.example.data.MtExplorerScope
import com.example.data.MtFileItem
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicBorderHairline
import com.example.ui.theme.CosmicCardElevated
import com.example.ui.theme.CosmicMagenta
import com.example.ui.theme.CosmicObsidian
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicTextMuted
import com.example.ui.theme.CosmicTextPure
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.SolarGold
import com.example.ui.theme.StarlightCyan
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MtExplorerScreen(
    activeVaultId: Int,
    onEncryptFileToVault: (File) -> Unit,
    onStatusMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val vaultStorageDir = remember(activeVaultId) {
        File(context.filesDir, "nyx_vault/vault_$activeVaultId").also {
            if (!it.exists()) it.mkdirs()
        }
    }

    var currentScope by remember { mutableStateOf(MtExplorerScope.DEVICE_ALL) }
    var activePaneIndex by remember { mutableIntStateOf(0) } // 0 = Panel Kiri, 1 = Panel Kanan

    // Panel A & Panel B State
    var paneAPath by remember { mutableStateOf(MtExplorerEngine.getDefaultDirectoryForScope(context, currentScope, vaultStorageDir)) }
    var paneBPath by remember { mutableStateOf(vaultStorageDir) }

    var paneAFiles by remember { mutableStateOf<List<MtFileItem>>(emptyList()) }
    var paneBFiles by remember { mutableStateOf<List<MtFileItem>>(emptyList()) }

    var selectedItemA by remember { mutableStateOf<MtFileItem?>(null) }
    var selectedItemB by remember { mutableStateOf<MtFileItem?>(null) }

    // Dialogs State
    var inspectingApkSummary by remember { mutableStateOf<Pair<MtFileItem, ApkMetadataSummary>?>(null) }
    var inspectingArchiveEntries by remember { mutableStateOf<Pair<MtFileItem, List<ArchiveEntryItem>>?>(null) }
    var viewingHexContent by remember { mutableStateOf<Pair<MtFileItem, String>?>(null) }
    var viewingTextContent by remember { mutableStateOf<Pair<MtFileItem, String>?>(null) }

    fun refreshPanes() {
        paneAFiles = MtExplorerEngine.listDirectory(paneAPath, currentScope, vaultStorageDir)
        paneBFiles = MtExplorerEngine.listDirectory(paneBPath, MtExplorerScope.VAULT_EXCLUSIVE, vaultStorageDir)
    }

    LaunchedEffect(currentScope, paneAPath, paneBPath) {
        refreshPanes()
    }

    val activeSelectedFile = if (activePaneIndex == 0) selectedItemA else selectedItemB
    val targetDirectory = if (activePaneIndex == 0) paneBPath else paneAPath

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicVoid)
            .testTag("mt_explorer_screen")
    ) {
        // 1. Top Cosmic Scope & Engine Bar
        Surface(
            color = CosmicObsidian,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NebulaViolet
                        ) {
                            Text(
                                text = "MT DUAL-PANE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CosmicTextPure,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "Binary & Archive Suite",
                            style = MaterialTheme.typography.titleMedium,
                            color = CosmicTextPure,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Ambient Mascot Inspector Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CosmicDragonAvatar(
                            mood = MascotMood.MT_INSPECTING,
                            size = 36.dp
                        )
                        IconButton(
                            onClick = { refreshPanes() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Segarkan", tint = StarlightCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scope Switcher Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MtExplorerScope.entries.forEach { scope ->
                        val isSelected = currentScope == scope
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                currentScope = scope
                                paneAPath = MtExplorerEngine.getDefaultDirectoryForScope(context, scope, vaultStorageDir)
                                selectedItemA = null
                            },
                            label = { Text(scope.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (scope == MtExplorerScope.DEVICE_ALL) StarlightCyan else NebulaViolet,
                                selectedLabelColor = CosmicVoid,
                                containerColor = CosmicSurface,
                                labelColor = CosmicTextPure
                            )
                        )
                    }
                }
            }
        }

        // 2. Action Toolbar for Selected File
        Surface(
            color = CosmicSurface,
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1-Tap Encrypt to Vault Bridge
                Button(
                    onClick = {
                        activeSelectedFile?.let { item ->
                            if (!item.isDirectory) {
                                onEncryptFileToVault(item.file)
                                onStatusMessage("Menyegel ${item.name} ke Brankas $activeVaultId...")
                                refreshPanes()
                            }
                        }
                    },
                    enabled = activeSelectedFile != null && !activeSelectedFile.isDirectory && !activeSelectedFile.isEncryptedNyx,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StarlightCyan,
                        contentColor = CosmicVoid
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("mt_encrypt_to_vault_button")
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kunci ke Vault", style = MaterialTheme.typography.labelMedium)
                }

                // Copy to other pane
                Button(
                    onClick = {
                        activeSelectedFile?.let { item ->
                            val ok = MtExplorerEngine.copyFileOrDirectory(item.file, targetDirectory)
                            onStatusMessage(if (ok) "Berhasil menyalin ${item.name}" else "Gagal menyalin berkas")
                            refreshPanes()
                        }
                    },
                    enabled = activeSelectedFile != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicCardElevated,
                        contentColor = CosmicTextPure
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salin", style = MaterialTheme.typography.labelMedium)
                }

                // Move to other pane
                Button(
                    onClick = {
                        activeSelectedFile?.let { item ->
                            val ok = MtExplorerEngine.moveFileOrDirectory(item.file, targetDirectory)
                            onStatusMessage(if (ok) "Berhasil memindahkan ${item.name}" else "Gagal memindahkan berkas")
                            refreshPanes()
                        }
                    },
                    enabled = activeSelectedFile != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicCardElevated,
                        contentColor = CosmicTextPure
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.DriveFileMove, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pindah", style = MaterialTheme.typography.labelMedium)
                }

                // Inspect APK / ZIP
                Button(
                    onClick = {
                        activeSelectedFile?.let { item ->
                            if (item.isApk) {
                                val meta = MtExplorerEngine.inspectApkPackage(item.file)
                                inspectingApkSummary = Pair(item, meta)
                            } else if (item.isZipArchive) {
                                val entries = MtExplorerEngine.inspectArchiveEntries(item.file)
                                inspectingArchiveEntries = Pair(item, entries)
                            }
                        }
                    },
                    enabled = activeSelectedFile != null && (activeSelectedFile.isApk || activeSelectedFile.isZipArchive),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NebulaViolet,
                        contentColor = CosmicTextPure
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspeksi APK/ZIP", style = MaterialTheme.typography.labelMedium)
                }

                // Hex Viewer
                Button(
                    onClick = {
                        activeSelectedFile?.let { item ->
                            if (!item.isDirectory) {
                                val hex = MtExplorerEngine.readHexDump(item.file)
                                viewingHexContent = Pair(item, hex)
                            }
                        }
                    },
                    enabled = activeSelectedFile != null && !activeSelectedFile.isDirectory,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicCardElevated,
                        contentColor = CosmicTextPure
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hex View", style = MaterialTheme.typography.labelMedium)
                }

                // Text Viewer
                Button(
                    onClick = {
                        activeSelectedFile?.let { item ->
                            if (!item.isDirectory) {
                                val txt = MtExplorerEngine.readTextPreview(item.file)
                                viewingTextContent = Pair(item, txt)
                            }
                        }
                    },
                    enabled = activeSelectedFile != null && !activeSelectedFile.isDirectory,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicCardElevated,
                        contentColor = CosmicTextPure
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Teks View", style = MaterialTheme.typography.labelMedium)
                }

                // Delete
                Button(
                    onClick = {
                        activeSelectedFile?.let { item ->
                            val ok = MtExplorerEngine.deleteFileOrDirectory(item.file)
                            onStatusMessage(if (ok) "Dihapus: ${item.name}" else "Gagal menghapus berkas")
                            refreshPanes()
                        }
                    },
                    enabled = activeSelectedFile != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicMagenta.copy(alpha = 0.2f),
                        contentColor = CosmicMagenta
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // 3. Dual-Pane Content View (Panel A & Panel B)
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Panel A (Kiri: Sumber / Device Storage)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (activePaneIndex == 0) CosmicObsidian else CosmicVoid)
                    .border(
                        width = 1.dp,
                        color = if (activePaneIndex == 0) StarlightCyan.copy(alpha = 0.5f) else CosmicBorderHairline
                    )
                    .fastZeroLatencyClick { activePaneIndex = 0 }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    PaneHeaderBar(
                        title = "Panel 1 (${currentScope.displayName})",
                        currentPath = paneAPath,
                        isActive = activePaneIndex == 0,
                        onGoUp = {
                            val parent = paneAPath.parentFile
                            if (parent != null && parent.canRead()) {
                                paneAPath = parent
                                selectedItemA = null
                            }
                        }
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 60.dp)
                    ) {
                        items(paneAFiles, key = { it.file.absolutePath }) { item ->
                            MtFileRow(
                                item = item,
                                isSelected = selectedItemA == item,
                                onClick = {
                                    activePaneIndex = 0
                                    if (item.isDirectory) {
                                        paneAPath = item.file
                                        selectedItemA = null
                                    } else {
                                        selectedItemA = if (selectedItemA == item) null else item
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Divider Line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(CosmicBorder)
            )

            // Panel B (Kanan: Brankas Arcanum / Destinasi)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (activePaneIndex == 1) CosmicObsidian else CosmicVoid)
                    .border(
                        width = 1.dp,
                        color = if (activePaneIndex == 1) NebulaViolet.copy(alpha = 0.5f) else CosmicBorderHairline
                    )
                    .fastZeroLatencyClick { activePaneIndex = 1 }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    PaneHeaderBar(
                        title = "Panel 2 (Brankas $activeVaultId)",
                        currentPath = paneBPath,
                        isActive = activePaneIndex == 1,
                        onGoUp = {
                            val parent = paneBPath.parentFile
                            if (parent != null && parent.absolutePath.startsWith(vaultStorageDir.absolutePath)) {
                                paneBPath = parent
                                selectedItemB = null
                            }
                        }
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 60.dp)
                    ) {
                        items(paneBFiles, key = { it.file.absolutePath }) { item ->
                            MtFileRow(
                                item = item,
                                isSelected = selectedItemB == item,
                                onClick = {
                                    activePaneIndex = 1
                                    if (item.isDirectory) {
                                        paneBPath = item.file
                                        selectedItemB = null
                                    } else {
                                        selectedItemB = if (selectedItemB == item) null else item
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: APK Inspector
    inspectingApkSummary?.let { (fileItem, summary) ->
        Dialog(onDismissRequest = { inspectingApkSummary = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicObsidian),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = StarlightCyan)
                            Text(
                                text = "Inspeksi Paket APK",
                                style = MaterialTheme.typography.titleMedium,
                                color = CosmicTextPure,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { inspectingApkSummary = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = CosmicTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = fileItem.name, style = MaterialTheme.typography.labelLarge, color = StarlightCyan)
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = CosmicVoid,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = summary.manifestSummary,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            color = CosmicTextPure,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { inspectingApkSummary = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaViolet)
                    ) {
                        Text("Tutup", color = CosmicTextPure)
                    }
                }
            }
        }
    }

    // Modal: Archive (ZIP/JAR) Inspector
    inspectingArchiveEntries?.let { (fileItem, entries) ->
        Dialog(onDismissRequest = { inspectingArchiveEntries = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicObsidian),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline),
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.FolderZip, contentDescription = null, tint = SolarGold)
                            Text(
                                text = "Daftar Entri Arsip (${entries.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = CosmicTextPure,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { inspectingArchiveEntries = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = CosmicTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = fileItem.name, style = MaterialTheme.typography.labelMedium, color = SolarGold)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(entries) { entry ->
                            Surface(
                                color = CosmicSurface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (entry.isDirectory) StarlightCyan else CosmicTextPure,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${entry.size / 1024} KB",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = CosmicTextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { inspectingArchiveEntries = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = StarlightCyan, contentColor = CosmicVoid)
                    ) {
                        Text("Tutup", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal: Hex Dump Viewer
    viewingHexContent?.let { (fileItem, hexDump) ->
        Dialog(onDismissRequest = { viewingHexContent = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicObsidian),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline),
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hex Viewer: ${fileItem.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = StarlightCyan,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(onClick = { viewingHexContent = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = CosmicTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = CosmicVoid,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                            item {
                                Text(
                                    text = hexDump,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    color = CosmicTextPure
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewingHexContent = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = StarlightCyan, contentColor = CosmicVoid)
                    ) {
                        Text("Tutup", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal: Text Preview Viewer
    viewingTextContent?.let { (fileItem, textContent) ->
        Dialog(onDismissRequest = { viewingTextContent = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicObsidian),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderHairline),
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Text Viewer: ${fileItem.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = NebulaViolet,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(onClick = { viewingTextContent = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = CosmicTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = CosmicVoid,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                            item {
                                Text(
                                    text = textContent,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    color = CosmicTextPure
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewingTextContent = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaViolet, contentColor = CosmicTextPure)
                    ) {
                        Text("Tutup", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaneHeaderBar(
    title: String,
    currentPath: File,
    isActive: Boolean,
    onGoUp: () -> Unit
) {
    Surface(
        color = if (isActive) CosmicCardElevated else CosmicSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onGoUp,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Naik Satu Tingkat",
                        tint = if (isActive) StarlightCyan else CosmicTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) StarlightCyan else CosmicTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentPath.name.ifBlank { "/" },
                        style = MaterialTheme.typography.labelMedium,
                        color = CosmicTextPure,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun MtFileRow(
    item: MtFileItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dateStr = remember(item.lastModified) {
        SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(item.lastModified))
    }

    Surface(
        color = if (isSelected) StarlightCyan.copy(alpha = 0.18f) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .fastZeroLatencyClick { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon
            Icon(
                imageVector = when {
                    item.isDirectory -> Icons.Default.Folder
                    item.isApk -> Icons.Default.Android
                    item.isZipArchive -> Icons.Default.FolderZip
                    item.isEncryptedNyx -> Icons.Default.Lock
                    else -> Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                tint = when {
                    item.isDirectory -> SolarGold
                    item.isApk -> StarlightCyan
                    item.isZipArchive -> SolarGold
                    item.isEncryptedNyx -> CosmicMagenta
                    else -> CosmicTextMuted
                },
                modifier = Modifier.size(20.dp)
            )

            // Name & Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) StarlightCyan else CosmicTextPure,
                    fontWeight = if (item.isDirectory || isSelected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (item.isDirectory) "Folder" else "${item.sizeBytes / 1024} KB",
                        fontSize = 9.sp,
                        color = CosmicTextMuted
                    )
                    Text(
                        text = "• $dateStr",
                        fontSize = 9.sp,
                        color = CosmicTextMuted
                    )
                }
            }
        }
    }
}
