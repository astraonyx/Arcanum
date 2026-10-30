package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AuthStage
import com.example.ui.CreateVaultFileDialog
import com.example.ui.CryptoPinVerificationDialog
import com.example.ui.EncryptionScriptEngineScreen
import com.example.ui.FirstRunOnboardingWizardScreen
import com.example.ui.ForgotPinRecoveryDialog
import com.example.ui.MoriDownloaderScreen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import com.example.ui.theme.PastelSky
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.PastelPeach
import com.example.ui.theme.CosmicPillDock
import com.example.ui.SecondaryBiometricReAuthDialog
import com.example.ui.SecurityGateScreen
import com.example.ui.VaultDashboardAndFilesContent
import com.example.ui.VaultFileInspectorSheet
import com.example.ui.VaultMediaPlayerScreen
import com.example.ui.VaultRichMediaViewerOverlay
import com.example.ui.VaultTab
import com.example.ui.VaultViewModel
import com.example.ui.ZipScannerToolScreen
import com.example.ui.instantTouchPress
import com.example.ui.theme.CosmicBorderHairline
import com.example.ui.theme.CosmicObsidian
import com.example.ui.theme.CosmicTextPure
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.StarlightCyan
import com.example.ui.theme.VaultCream
import com.example.ui.theme.VaultLime
import com.example.ui.theme.VaultObsidian
import com.example.ui.theme.VaultPureWhite

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedText = if (intent?.action == Intent.ACTION_SEND) {
            intent?.getStringExtra(Intent.EXTRA_TEXT)
        } else null

        val sharedStreamUri: Uri? = if (intent?.action == Intent.ACTION_SEND) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent?.getParcelableExtra(Intent.EXTRA_STREAM)
            }
        } else null

        setContent {
            MyApplicationTheme {
                TrantorVaultApp(
                    initialSharedText = sharedText,
                    initialSharedStreamUri = sharedStreamUri
                )
            }
        }
    }
}

@Composable
fun TrantorVaultApp(
    viewModel: VaultViewModel = viewModel(),
    initialSharedText: String? = null,
    initialSharedStreamUri: Uri? = null
) {
    val context = LocalContext.current
    val securityState by viewModel.securityState.collectAsStateWithLifecycle()
    val securityConfig by viewModel.securityConfig.collectAsStateWithLifecycle()
    val activeVaultId by viewModel.activeVaultId.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val inspectingItem by viewModel.inspectingItem.collectAsStateWithLifecycle()
    val viewingMediaItem by viewModel.viewingMediaItem.collectAsStateWithLifecycle()
    val cryptoPinModalState by viewModel.cryptoPinModalState.collectAsStateWithLifecycle()
    val biometricExportModalState by viewModel.biometricExportModalState.collectAsStateWithLifecycle()
    val showForgotPinModal by viewModel.showForgotPinModal.collectAsStateWithLifecycle()
    val mediaState by viewModel.mediaState.collectAsStateWithLifecycle()
    val zipScannerState by viewModel.zipScannerState.collectAsStateWithLifecycle()
    val moriState by viewModel.moriState.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val activeMediaFolder by viewModel.activeMediaFolder.collectAsStateWithLifecycle()

    var showCreateSecretDialog by remember { mutableStateOf(false) }

    // Forward incoming Android Share Sheet links/media (like coflyn/Mori Quick Save) once unlocked
    LaunchedEffect(securityState.stage, initialSharedText, initialSharedStreamUri) {
        if (securityState.stage == AuthStage.UNLOCKED &&
            (!initialSharedText.isNullOrBlank() || initialSharedStreamUri != null)
        ) {
            viewModel.handleIncomingSharedIntent(initialSharedText, initialSharedStreamUri)
        }
    }

    // System Multi-Document Picker for importing files, media, or .ZIP archives
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // VaultRepository copies bytes immediately into internal private storage
                }
            }
            viewModel.importUrisFromPicker(uris)
        }
    }

    // System Save-As Document Picker for exporting individual files from Media Player after secondary biometric auth
    val saveExportDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { targetUri ->
        if (targetUri != null) {
            viewModel.completeExportToDocumentUri(targetUri)
        }
    }

    // 1. First-Open Onboarding & Custom PIN + Fingerprint + Recovery Setup Wizard
    if (!securityConfig.isFirstRunCompleted) {
        FirstRunOnboardingWizardScreen(
            onCompleteSetup = viewModel::completeFirstRunOnboarding
        )
        return
    }

    // Forgot PIN / Password Security Recovery Dialog (Accessible from Lock Screen or Inside Vault)
    if (showForgotPinModal) {
        ForgotPinRecoveryDialog(
            securityConfig = securityConfig,
            onResetPinsAndUnlock = viewModel::resetPinsFromRecoveryAndUnlock,
            onDismiss = viewModel::dismissForgotPinModal
        )
    }

    // 2. 3-Layer 6-Digit PIN + Genuine Fingerprint Security Gate
    if (securityState.stage != AuthStage.UNLOCKED) {
        SecurityGateScreen(
            securityState = securityState,
            onDigitClick = viewModel::onPinDigitPressed,
            onBackspaceClick = viewModel::onPinBackspacePressed,
            onClearClick = viewModel::onPinClearPressed,
            onBiometricSuccess = viewModel::onBiometricAuthenticatedSuccess,
            onBiometricFeedback = viewModel::onBiometricAuthenticationError,
            onForgotPinClick = viewModel::openForgotPinModal
        )
        return
    }

    // Back button behavior for overlays and secondary tabs
    BackHandler(
        enabled = biometricExportModalState != null ||
                showForgotPinModal ||
                viewingMediaItem != null ||
                cryptoPinModalState != null ||
                activeTab != VaultTab.DASHBOARD
    ) {
        when {
            biometricExportModalState != null -> viewModel.dismissBiometricExportModal()
            showForgotPinModal -> viewModel.dismissForgotPinModal()
            cryptoPinModalState != null -> viewModel.dismissCryptoPinModal()
            viewingMediaItem != null -> viewModel.openViewingMediaModal(null)
            activeTab != VaultTab.DASHBOARD -> viewModel.selectTab(VaultTab.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CosmicVoid,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            TrantorDockedBottomBar(
                activeTab = activeTab,
                onSelectTab = viewModel::selectTab
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                VaultTab.DASHBOARD -> {
                    VaultDashboardAndFilesContent(
                        activeVaultId = activeVaultId,
                        isFilesOnlyMode = false,
                        allItems = allItems,
                        filteredItems = filteredItems,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        securityConfig = securityConfig,
                        onSelectCategory = viewModel::selectCategory,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onToggleEncryptItemWithPin = { item ->
                            viewModel.requestToggleEncryptionWithPin(item, openAfterDecrypt = false)
                        },
                        onBatchEncryptAllWithPin = viewModel::requestBatchEncryptionWithPin,
                        onInspectItem = viewModel::inspectItem,
                        onOpenOrPlayItem = viewModel::playOrViewItem,
                        onDeleteItem = viewModel::deleteVaultItem,
                        onImportFromDeviceClick = {
                            documentPickerLauncher.launch(arrayOf("*/*"))
                        },
                        onCreateSecretFileClick = { showCreateSecretDialog = true },
                        onOpenScriptEngineClick = { viewModel.selectTab(VaultTab.SCRIPT_ENGINE) },
                        onSwitchVaultModeClick = viewModel::switchVaultModeWithLock,
                        onLockVaultClick = viewModel::lockVaultNow,
                        onOpenZipScannerToolClick = { viewModel.selectTab(VaultTab.ZIP_SCANNER) },
                        onOpenMoriDownloaderClick = { viewModel.selectTab(VaultTab.MORI_DOWNLOADER) }
                    )
                }

                VaultTab.FILES -> {
                    VaultDashboardAndFilesContent(
                        activeVaultId = activeVaultId,
                        isFilesOnlyMode = true,
                        allItems = allItems,
                        filteredItems = filteredItems,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        securityConfig = securityConfig,
                        onSelectCategory = viewModel::selectCategory,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onToggleEncryptItemWithPin = { item ->
                            viewModel.requestToggleEncryptionWithPin(item, openAfterDecrypt = false)
                        },
                        onBatchEncryptAllWithPin = viewModel::requestBatchEncryptionWithPin,
                        onInspectItem = viewModel::inspectItem,
                        onOpenOrPlayItem = viewModel::playOrViewItem,
                        onDeleteItem = viewModel::deleteVaultItem,
                        onImportFromDeviceClick = {
                            documentPickerLauncher.launch(arrayOf("*/*"))
                        },
                        onCreateSecretFileClick = { showCreateSecretDialog = true },
                        onOpenScriptEngineClick = { viewModel.selectTab(VaultTab.SCRIPT_ENGINE) },
                        onSwitchVaultModeClick = viewModel::switchVaultModeWithLock,
                        onLockVaultClick = viewModel::lockVaultNow,
                        onOpenZipScannerToolClick = { viewModel.selectTab(VaultTab.ZIP_SCANNER) },
                        onOpenMoriDownloaderClick = { viewModel.selectTab(VaultTab.MORI_DOWNLOADER) }
                    )
                }

                VaultTab.MORI_DOWNLOADER -> {
                    MoriDownloaderScreen(
                        activeVaultId = activeVaultId,
                        moriState = moriState,
                        allVaultItems = allItems,
                        onUrlInputChange = viewModel::updateMoriUrlInput,
                        onCustomFileNameChange = viewModel::updateMoriCustomFileName,
                        onSelectExtractionMode = viewModel::selectMoriExtractionMode,
                        onToggleEncryptImmediately = viewModel::toggleMoriEncryptImmediately,
                        onAnalyzeLinkClick = viewModel::analyzeMoriLink,
                        onStartDownloadToVault = viewModel::startMoriDownloadToVault,
                        onTriggerOtaScraperUpdate = viewModel::triggerMoriOtaScraperUpdate,
                        onOpenDownloadedItem = viewModel::playOrViewItem,
                        onToggleEncryptItemWithPin = { item ->
                            viewModel.requestToggleEncryptionWithPin(item, openAfterDecrypt = false)
                        },
                        onShareItemWithBiometric = { item ->
                            viewModel.requestExportOrShareWithBiometric(item, isShareIntent = true)
                        },
                        onDeleteItem = viewModel::deleteVaultItem
                    )
                }

                VaultTab.ZIP_SCANNER -> {
                    ZipScannerToolScreen(
                        activeVaultId = activeVaultId,
                        scannerState = zipScannerState,
                        onPickMultipleZipsOrFilesClick = {
                            documentPickerLauncher.launch(arrayOf("*/*"))
                        },
                        onRunSimulatedMultiZipTestClick = viewModel::runSimulatedMultiZipScannerTest,
                        onCommitCleanOnlyToVault = viewModel::commitStagedCleanItemsOnly,
                        onCommitAllIncludingInfectedToVault = viewModel::commitStagedAllIncludingInfected,
                        onOpenVirusWarningModal = viewModel::openVirusWarningModal,
                        onDismissVirusWarningModal = viewModel::dismissVirusWarningModal,
                        onClearScanReport = viewModel::clearZipScanReport
                    )
                }

                VaultTab.MEDIA_PLAYER -> {
                    VaultMediaPlayerScreen(
                        activeVaultId = activeVaultId,
                        mediaState = mediaState,
                        allItems = allItems,
                        activeMediaFolder = activeMediaFolder,
                        onSelectMediaFolder = viewModel::selectMediaFolder,
                        onMoveMediaItemToPlaylist = viewModel::moveMediaItemToPlaylist,
                        onSetHold2xSpeed = viewModel::setHold2xSpeed,
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onSeekToSeconds = viewModel::seekToSeconds,
                        onSkipTrack = viewModel::skipTrack,
                        onSelectOrOpenMedia = viewModel::selectAndPlayMediaTrack,
                        onOpenFullscreenOverlay = viewModel::openFullscreenViewer,
                        onRequestPinToggleEncrypt = { item ->
                            viewModel.requestToggleEncryptionWithPin(item, openAfterDecrypt = false)
                        },
                        onRequestExportOrShareWithBiometric = viewModel::requestExportOrShareWithBiometric,
                        onCycleSpeed = viewModel::cyclePlaybackSpeed,
                        onToggleRepeat = viewModel::toggleRepeatMode,
                        onImportMediaOrZipClick = {
                            documentPickerLauncher.launch(arrayOf("*/*"))
                        }
                    )
                }

                VaultTab.SCRIPT_ENGINE -> {
                    EncryptionScriptEngineScreen(
                        activeVaultId = activeVaultId,
                        securityConfig = securityConfig,
                        onSaveScriptConfig = viewModel::saveScriptConfiguration,
                        onSaveVaultPins = viewModel::saveVaultPins,
                        onAddNewVault = { p1, p2, p3 -> viewModel.addNewVault(p1, p2, p3) },
                        onSaveRecoveryCredentials = viewModel::saveRecoveryCredentials,
                        onToggleBiometricGate = viewModel::toggleBiometricGate,
                        onLockVaultNow = viewModel::lockVaultNow,
                        onLinkGoogleAccount = { email -> viewModel.linkGoogleAccount(email, "Pengguna Arcanum") },
                        onUnlinkGoogleAccount = { viewModel.unlinkGoogleAccount() },
                        onBackupToCloud = { viewModel.backupActiveVaultToGoogleCloud(context) },
                        onRestoreFromCloud = { file -> viewModel.restoreVaultFromEncryptedPackage(context, file) }
                    )
                }
            }

            // Floating Status Toast Banner
            statusBannerMessage?.let { msg ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    color = VaultObsidian
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = VaultLime,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = VaultPureWhite
                            )
                        }
                        IconButton(
                            onClick = viewModel::dismissBanner,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup Notifikasi",
                                tint = VaultPureWhite.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Inspect NYX2 Container Bottom Sheet
    inspectingItem?.let { item ->
        VaultFileInspectorSheet(
            item = item,
            onDismiss = { viewModel.inspectItem(null) },
            onToggleEncryptWithPin = {
                viewModel.requestToggleEncryptionWithPin(item, openAfterDecrypt = false)
            },
            onOpenOrPlayMedia = {
                viewModel.inspectItem(null)
                viewModel.playOrViewItem(item)
            },
            onDelete = {
                viewModel.deleteVaultItem(item)
                viewModel.inspectItem(null)
            }
        )
    }

    // Create New Secret Note / File Dialog
    if (showCreateSecretDialog) {
        CreateVaultFileDialog(
            onDismiss = { showCreateSecretDialog = false },
            onCreateFile = { title, ext, category, payload, encryptNow ->
                viewModel.createVaultNoteOrMediaFile(
                    fileName = title,
                    extension = ext,
                    category = category,
                    contentText = payload,
                    encryptImmediately = encryptNow
                )
            }
        )
    }

    // Full-Screen Rich Media, Video & Document Viewer Overlay
    viewingMediaItem?.let { item ->
        VaultRichMediaViewerOverlay(
            item = item,
            mediaState = mediaState,
            onClose = { viewModel.openViewingMediaModal(null) },
            onRequestReEncryptWithPin = {
                viewModel.openViewingMediaModal(null)
                viewModel.requestToggleEncryptionWithPin(item, openAfterDecrypt = false)
            },
            onRequestExportOrShareWithBiometric = { isShareIntent ->
                viewModel.requestExportOrShareWithBiometric(item, isShareIntent)
            },
            onToggleVideoPlayPause = viewModel::togglePlayPause,
            onSeekVideoSeconds = viewModel::seekToSeconds,
            onCycleSpeed = viewModel::cyclePlaybackSpeed
        )
    }

    // PIN Verification Modal for Encrypting / Decrypting Files
    cryptoPinModalState?.let { modalState ->
        CryptoPinVerificationDialog(
            modalState = modalState,
            onDigitClick = viewModel::onCryptoModalPinDigit,
            onBackspaceClick = viewModel::onCryptoModalPinBackspace,
            onDismiss = viewModel::dismissCryptoPinModal,
            onForgotPinClick = viewModel::openForgotPinModal
        )
    }

    // Secondary Biometric Re-Authentication Modal for Exporting or Sharing Files from Media Player
    biometricExportModalState?.let { exportState ->
        SecondaryBiometricReAuthDialog(
            modalState = exportState,
            onAuthenticated = {
                viewModel.onBiometricExportReAuthenticated(
                    onShareFileReady = { decryptedFile, mimeType ->
                        try {
                            val authority = "${context.packageName}.fileprovider"
                            val shareUri = FileProvider.getUriForFile(context, authority, decryptedFile)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = mimeType
                                putExtra(Intent.EXTRA_STREAM, shareUri)
                                putExtra(Intent.EXTRA_SUBJECT, decryptedFile.name)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            val chooser = Intent.createChooser(sendIntent, "Bagikan ${decryptedFile.name}")
                            context.startActivity(chooser)
                        } catch (_: Exception) {
                            // Handled gracefully with banner feedback in ViewModel
                        }
                    },
                    onLaunchSystemSaveAs = { suggestedFileName, _ ->
                        saveExportDocumentLauncher.launch(suggestedFileName)
                    }
                )
            },
            onDismiss = viewModel::dismissBiometricExportModal
        )
    }
}

@Composable
private fun TrantorDockedBottomBar(
    activeTab: VaultTab,
    onSelectTab: (VaultTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .shadow(16.dp, RoundedCornerShape(36.dp))
                .clip(RoundedCornerShape(36.dp))
                .background(CosmicPillDock)
                .border(1.dp, CosmicBorderHairline, RoundedCornerShape(36.dp))
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavPillItem(
                label = "Brankas",
                icon = Icons.Default.Shield,
                selected = activeTab == VaultTab.DASHBOARD || activeTab == VaultTab.FILES,
                activeColor = StarlightCyan,
                onClick = { onSelectTab(VaultTab.DASHBOARD) },
                tag = "nav_tab_dashboard"
            )
            BottomNavPillItem(
                label = "Mori",
                icon = Icons.Default.CloudDownload,
                selected = activeTab == VaultTab.MORI_DOWNLOADER,
                activeColor = PastelLavender,
                onClick = { onSelectTab(VaultTab.MORI_DOWNLOADER) },
                tag = "nav_tab_mori_downloader"
            )
            BottomNavPillItem(
                label = "Media",
                icon = Icons.Default.PlayArrow,
                selected = activeTab == VaultTab.MEDIA_PLAYER,
                activeColor = PastelSky,
                onClick = { onSelectTab(VaultTab.MEDIA_PLAYER) },
                tag = "nav_tab_media_player"
            )
            if (activeTab == VaultTab.ZIP_SCANNER) {
                BottomNavPillItem(
                    label = "ZIP",
                    icon = Icons.Default.FolderZip,
                    selected = true,
                    activeColor = PastelPeach,
                    onClick = { onSelectTab(VaultTab.ZIP_SCANNER) },
                    tag = "nav_tab_zip_scanner"
                )
            }
            BottomNavPillItem(
                label = "Pengaturan",
                icon = Icons.Default.Settings,
                selected = activeTab == VaultTab.SCRIPT_ENGINE,
                activeColor = PastelPeach,
                onClick = { onSelectTab(VaultTab.SCRIPT_ENGINE) },
                tag = "nav_tab_script_engine"
            )
        }
    }
}

@Composable
private fun BottomNavPillItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    activeColor: Color = StarlightCyan,
    onClick: () -> Unit,
    tag: String
) {
    val bg = if (selected) activeColor.copy(alpha = 0.22f) else Color.Transparent
    val contentColor = if (selected) activeColor else CosmicTextPure.copy(alpha = 0.70f)

    Row(
        modifier = Modifier
            .height(42.dp)
            .clip(CircleShape)
            .background(bg)
            .instantTouchPress { onClick() }
            .padding(horizontal = if (selected) 11.dp else 8.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(19.dp)
        )
        if (selected) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}
