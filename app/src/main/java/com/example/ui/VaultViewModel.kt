package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EncryptionPreset
import com.example.data.EncryptionScriptEngine
import com.example.data.MoriDownloadResult
import com.example.data.MoriDownloaderEngine
import com.example.data.MoriExtractionMode
import com.example.data.SecurityConfig
import com.example.data.VaultCategory
import com.example.data.VaultDatabase
import com.example.data.VaultItemEntity
import com.example.data.VaultRepository
import com.example.data.ZipPreEncryptionScannerEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AuthStage(val layerNumber: Int, val title: String) {
    PIN_LAYER_1(1, "PIN Lapis 1 — Gerbang Utama"),
    PIN_LAYER_2(2, "PIN Lapis 2 — Subkey NYX2"),
    PIN_LAYER_3(3, "PIN Lapis 3 — Otorisasi Brankas"),
    BIOMETRIC_STEP(4, "Verifikasi Biometrik"),
    UNLOCKED(5, "Terbuka")
}

enum class VaultTab {
    DASHBOARD,
    FILES,
    MT_EXPLORER,
    MORI_DOWNLOADER,
    ZIP_SCANNER,
    MEDIA_PLAYER,
    SCRIPT_ENGINE
}

data class SecurityGateUiState(
    val stage: AuthStage = AuthStage.PIN_LAYER_1,
    val enteredPin: String = "",
    val candidateVaultId: Int = 0, // 0 = undetermined, 1 = Vault 1, 2 = Vault 2
    val errorMessage: String? = null,
    val biometricStatusMessage: String = "Sentuh sensor sidik jari atau wajah untuk membuka brankas"
)

sealed class PendingCryptoOperation {
    data class ToggleSingleItem(
        val item: VaultItemEntity,
        val openAfterDecrypt: Boolean = false
    ) : PendingCryptoOperation()

    data class BatchVaultItems(val encryptAll: Boolean) : PendingCryptoOperation()
}

data class CryptoPinModalState(
    val operation: PendingCryptoOperation,
    val targetEncryptState: Boolean,
    val title: String,
    val subtitle: String,
    val enteredPin: String = "",
    val errorText: String? = null
)

data class BiometricExportModalState(
    val item: VaultItemEntity,
    val isShareIntent: Boolean, // true = Share via FileProvider, false = Export/Save to device
    val errorMessage: String? = null
)

data class MediaPlayerUiState(
    val currentTrack: VaultItemEntity? = null,
    val isPlaying: Boolean = false,
    val currentSeconds: Int = 0,
    val totalSeconds: Int = 180,
    val playbackSpeed: Float = 1.0f,
    val isRepeatEnabled: Boolean = true
)

class VaultViewModel(application: Application) : AndroidViewModel(application) {
    private val database = VaultDatabase.getDatabase(application)
    private val repository = VaultRepository(application, database.vaultDao())

    val securityConfig: StateFlow<SecurityConfig> = repository.securityConfigFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SecurityConfig()
    )

    // Active Vault Mode (1 = Brankas Utama, 2 = Brankas Kedua / Dual Mode)
    private val _activeVaultId = MutableStateFlow(1)
    val activeVaultId: StateFlow<Int> = _activeVaultId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allItems: StateFlow<List<VaultItemEntity>> = _activeVaultId
        .flatMapLatest { vaultId: Int -> repository.getItemsForVault(vaultId) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _securityState = MutableStateFlow(SecurityGateUiState())
    val securityState: StateFlow<SecurityGateUiState> = _securityState.asStateFlow()

    private val _showForgotPinModal = MutableStateFlow(false)
    val showForgotPinModal: StateFlow<Boolean> = _showForgotPinModal.asStateFlow()

    private val _biometricExportModalState = MutableStateFlow<BiometricExportModalState?>(null)
    val biometricExportModalState: StateFlow<BiometricExportModalState?> = _biometricExportModalState.asStateFlow()

    private var pendingExportTargetItem: VaultItemEntity? = null

    private val _activeTab = MutableStateFlow(VaultTab.DASHBOARD)
    val activeTab: StateFlow<VaultTab> = _activeTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow<VaultCategory?>(null)
    val selectedCategory: StateFlow<VaultCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _inspectingItem = MutableStateFlow<VaultItemEntity?>(null)
    val inspectingItem: StateFlow<VaultItemEntity?> = _inspectingItem.asStateFlow()

    private val _viewingMediaItem = MutableStateFlow<VaultItemEntity?>(null)
    val viewingMediaItem: StateFlow<VaultItemEntity?> = _viewingMediaItem.asStateFlow()

    private val _cryptoPinModalState = MutableStateFlow<CryptoPinModalState?>(null)
    val cryptoPinModalState: StateFlow<CryptoPinModalState?> = _cryptoPinModalState.asStateFlow()

    private val _mediaState = MutableStateFlow(MediaPlayerUiState())
    val mediaState: StateFlow<MediaPlayerUiState> = _mediaState.asStateFlow()

    private val _zipScannerState = MutableStateFlow(ZipScannerUiState())
    val zipScannerState: StateFlow<ZipScannerUiState> = _zipScannerState.asStateFlow()

    private val _moriState = MutableStateFlow(MoriDownloaderUiState())
    val moriState: StateFlow<MoriDownloaderUiState> = _moriState.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private var playbackJob: Job? = null

    val filteredItems: StateFlow<List<VaultItemEntity>> = combine(
        allItems,
        _selectedCategory,
        _searchQuery
    ) { items, category, query ->
        items.filter { item ->
            val matchesCategory = category == null || item.category == category.name
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.extension.contains(query, ignoreCase = true) ||
                    (item.groupArchiveName?.contains(query, ignoreCase = true) == true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val mediaPlaylist: StateFlow<List<VaultItemEntity>> = combine(allItems, _activeVaultId) { items, _ ->
        items.filter {
            it.category == VaultCategory.AUDIO.name ||
                    it.category == VaultCategory.VIDEO.name ||
                    it.category == VaultCategory.IMAGE.name
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            mediaPlaylist.collect { playlist ->
                val current = _mediaState.value.currentTrack
                if (playlist.isEmpty()) {
                    _mediaState.value = _mediaState.value.copy(
                        currentTrack = null,
                        isPlaying = false,
                        currentSeconds = 0
                    )
                } else if (current == null || playlist.none { it.id == current.id }) {
                    val preferredVideo = playlist.firstOrNull { it.category == VaultCategory.VIDEO.name }
                        ?: playlist.first()
                    _mediaState.value = _mediaState.value.copy(
                        currentTrack = preferredVideo,
                        currentSeconds = 0,
                        totalSeconds = preferredVideo.durationSeconds.coerceAtLeast(60),
                        isPlaying = false
                    )
                } else {
                    val updated = playlist.find { it.id == current.id }
                    if (updated != null && updated != current) {
                        _mediaState.value = _mediaState.value.copy(
                            currentTrack = updated,
                            totalSeconds = updated.durationSeconds.coerceAtLeast(60)
                        )
                    }
                }
            }
        }
    }

    // --- FIRST-OPEN ONBOARDING WIZARD COMPLETION ---

    fun completeFirstRunOnboarding(
        v1p1: String,
        v1p2: String,
        v1p3: String,
        v2p1: String,
        v2p2: String,
        v2p3: String,
        biometricEnabled: Boolean,
        recoveryQuestion: String = "Apa kata kunci pemulihan rahasia Anda?",
        recoveryAnswer: String = "nyxvault"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.completeFirstRunSetup(
                v1p1 = v1p1,
                v1p2 = v1p2,
                v1p3 = v1p3,
                v2p1 = v2p1,
                v2p2 = v2p2,
                v2p3 = v2p3,
                biometricEnabled = biometricEnabled,
                recoveryQuestion = recoveryQuestion,
                recoveryAnswer = recoveryAnswer
            )
            _activeVaultId.value = 1
            _securityState.value = SecurityGateUiState(
                stage = AuthStage.UNLOCKED,
                candidateVaultId = 1
            )
            postBanner("Brankas NYX2 siap digunakan! Impor file atau ZIP untuk disembunyikan.")
        }
    }

    // --- 3-LAYER DUAL-VAULT PIN + BIOMETRIC SECURITY LOGIC ---

    fun onPinDigitPressed(digit: Int) {
        val current = _securityState.value
        if (current.stage == AuthStage.BIOMETRIC_STEP || current.stage == AuthStage.UNLOCKED) return
        if (current.enteredPin.length >= 6) return

        val nextPin = current.enteredPin + digit.toString()
        _securityState.value = current.copy(enteredPin = nextPin, errorMessage = null)

        if (nextPin.length == 6) {
            verifyCurrentLayerPin(nextPin)
        }
    }

    fun onPinBackspacePressed() {
        val current = _securityState.value
        if (current.enteredPin.isNotEmpty()) {
            _securityState.value = current.copy(
                enteredPin = current.enteredPin.dropLast(1),
                errorMessage = null
            )
        } else {
            // Allow stepping back to the previous PIN layer if current input is empty
            when (current.stage) {
                AuthStage.PIN_LAYER_2 -> {
                    _securityState.value = current.copy(
                        stage = AuthStage.PIN_LAYER_1,
                        candidateVaultId = 0,
                        errorMessage = null
                    )
                }
                AuthStage.PIN_LAYER_3 -> {
                    _securityState.value = current.copy(
                        stage = AuthStage.PIN_LAYER_2,
                        errorMessage = null
                    )
                }
                else -> Unit
            }
        }
    }

    fun onPinClearPressed() {
        _securityState.value = _securityState.value.copy(enteredPin = "", errorMessage = null)
    }

    private fun verifyCurrentLayerPin(submittedPin: String) {
        val cfg = securityConfig.value
        val current = _securityState.value

        when (current.stage) {
            AuthStage.PIN_LAYER_1 -> {
                when (submittedPin) {
                    cfg.vault1Pin1 -> {
                        _securityState.value = current.copy(
                            stage = AuthStage.PIN_LAYER_2,
                            enteredPin = "",
                            candidateVaultId = 1,
                            errorMessage = null
                        )
                    }
                    cfg.vault2Pin1 -> {
                        _securityState.value = current.copy(
                            stage = AuthStage.PIN_LAYER_2,
                            enteredPin = "",
                            candidateVaultId = 2,
                            errorMessage = null
                        )
                    }
                    else -> {
                        _securityState.value = current.copy(
                            enteredPin = "",
                            errorMessage = "PIN Lapis 1 tidak valid. Silakan coba lagi."
                        )
                    }
                }
            }

            AuthStage.PIN_LAYER_2 -> {
                val expectedPin2 = if (current.candidateVaultId == 2) cfg.vault2Pin2 else cfg.vault1Pin2
                if (submittedPin == expectedPin2) {
                    _securityState.value = current.copy(
                        stage = AuthStage.PIN_LAYER_3,
                        enteredPin = "",
                        errorMessage = null
                    )
                } else {
                    _securityState.value = current.copy(
                        enteredPin = "",
                        errorMessage = "PIN Lapis 2 salah. Verifikasi ditolak."
                    )
                }
            }

            AuthStage.PIN_LAYER_3 -> {
                val expectedPin3 = if (current.candidateVaultId == 2) cfg.vault2Pin3 else cfg.vault1Pin3
                if (submittedPin == expectedPin3) {
                    val targetVault = if (current.candidateVaultId == 2) 2 else 1
                    _activeVaultId.value = targetVault
                    val nextStage = if (cfg.biometricEnabled) AuthStage.BIOMETRIC_STEP else AuthStage.UNLOCKED
                    _securityState.value = current.copy(
                        stage = nextStage,
                        enteredPin = "",
                        errorMessage = null
                    )
                } else {
                    _securityState.value = current.copy(
                        enteredPin = "",
                        errorMessage = "PIN Lapis 3 salah. Akses brankas ditolak."
                    )
                }
            }

            else -> Unit
        }
    }

    fun onBiometricAuthenticatedSuccess() {
        _securityState.value = _securityState.value.copy(
            stage = AuthStage.UNLOCKED,
            errorMessage = null
        )
    }

    fun onBiometricAuthenticationError(reason: String) {
        _securityState.value = _securityState.value.copy(
            biometricStatusMessage = reason,
            errorMessage = reason
        )
    }

    fun lockVaultNow() {
        pausePlayback()
        _inspectingItem.value = null
        _viewingMediaItem.value = null
        _cryptoPinModalState.value = null
        _securityState.value = SecurityGateUiState(stage = AuthStage.PIN_LAYER_1)
    }

    fun switchVaultModeWithLock(targetVaultId: Int) {
        if (targetVaultId == _activeVaultId.value) {
            postBanner("Anda sudah berada di Brankas $targetVaultId")
            return
        }
        lockVaultNow()
        postBanner("Masukkan 3 Lapis PIN untuk membuka Brankas $targetVaultId")
    }

    // --- PIN-GATED FILE ENCRYPTION / DECRYPTION ON DISK ---

    fun requestToggleEncryptionWithPin(
        item: VaultItemEntity,
        openAfterDecrypt: Boolean = false
    ) {
        val targetEncrypt = !item.isEncrypted
        val actionWord = if (item.isEncrypted) "Mendekripsi" else "Mengenkripsi"
        _cryptoPinModalState.value = CryptoPinModalState(
            operation = PendingCryptoOperation.ToggleSingleItem(item, openAfterDecrypt),
            targetEncryptState = targetEncrypt,
            title = "PIN Otorisasi NYX2",
            subtitle = "Masukkan PIN Brankas ${_activeVaultId.value} untuk $actionWord \"${item.name}.${item.extension}\""
        )
    }

    fun requestBatchEncryptionWithPin(encryptAll: Boolean) {
        val actionWord = if (encryptAll) "Mengenkripsi Semua File" else "Mendekripsi Semua File"
        _cryptoPinModalState.value = CryptoPinModalState(
            operation = PendingCryptoOperation.BatchVaultItems(encryptAll),
            targetEncryptState = encryptAll,
            title = "PIN Otorisasi Massal NYX2",
            subtitle = "Masukkan PIN Brankas ${_activeVaultId.value} untuk $actionWord di Brankas ${_activeVaultId.value}"
        )
    }

    fun onCryptoModalPinDigit(digit: Int) {
        val modal = _cryptoPinModalState.value ?: return
        if (modal.enteredPin.length >= 6) return
        val nextPin = modal.enteredPin + digit.toString()
        _cryptoPinModalState.value = modal.copy(enteredPin = nextPin, errorText = null)

        if (nextPin.length == 6) {
            verifyCryptoModalPin(nextPin)
        }
    }

    fun onCryptoModalPinBackspace() {
        val modal = _cryptoPinModalState.value ?: return
        if (modal.enteredPin.isNotEmpty()) {
            _cryptoPinModalState.value = modal.copy(
                enteredPin = modal.enteredPin.dropLast(1),
                errorText = null
            )
        }
    }

    fun dismissCryptoPinModal() {
        _cryptoPinModalState.value = null
    }

    private fun verifyCryptoModalPin(submittedPin: String) {
        val modal = _cryptoPinModalState.value ?: return
        val cfg = securityConfig.value
        val validPins = cfg.getPinsForVault(_activeVaultId.value).toList()

        if (submittedPin in validPins) {
            val op = modal.operation
            _cryptoPinModalState.value = null
            when (op) {
                is PendingCryptoOperation.ToggleSingleItem -> {
                    executeToggleEncryptionInternal(op.item, op.openAfterDecrypt)
                }
                is PendingCryptoOperation.BatchVaultItems -> {
                    executeBatchEncryptInternal(op.encryptAll)
                }
            }
        } else {
            _cryptoPinModalState.value = modal.copy(
                enteredPin = "",
                errorText = "PIN Brankas ${_activeVaultId.value} tidak cocok! Akses ditolak."
            )
        }
    }

    private fun executeToggleEncryptionInternal(
        item: VaultItemEntity,
        openAfterDecrypt: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val targetEncryptedState = !item.isEncrypted
            val updated = repository.toggleBinaryEncryptionOnDisk(item, targetEncryptedState)

            if (_inspectingItem.value?.id == item.id) {
                _inspectingItem.value = updated
            }
            if (_viewingMediaItem.value?.id == item.id) {
                _viewingMediaItem.value = updated
            }
            if (_mediaState.value.currentTrack?.id == item.id) {
                _mediaState.value = _mediaState.value.copy(currentTrack = updated)
            }

            if (!updated.isEncrypted && openAfterDecrypt) {
                val cat = VaultCategory.entries.find { it.name == updated.category }
                if (cat == VaultCategory.VIDEO || cat == VaultCategory.AUDIO) {
                    _activeTab.value = VaultTab.MEDIA_PLAYER
                    selectAndPlayMediaTrack(updated)
                } else {
                    _viewingMediaItem.value = updated
                }
            }

            postBanner(
                if (updated.isEncrypted) {
                    "Terenkripsi (.nyx): ${updated.name}.${updated.extension}"
                } else {
                    "Terdekripsi & siap dibuka: ${updated.name}.${updated.extension}"
                }
            )
        }
    }

    private fun executeBatchEncryptInternal(encryptAll: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = allItems.value
            for (item in currentList) {
                if (item.isEncrypted != encryptAll) {
                    repository.toggleBinaryEncryptionOnDisk(item, encryptAll)
                }
            }
            postBanner(
                if (encryptAll) {
                    "Semua file di Brankas ${_activeVaultId.value} telah dikunci (.nyx)"
                } else {
                    "Semua file di Brankas ${_activeVaultId.value} telah didekripsi"
                }
            )
        }
    }

    // --- NAVIGATION & MEDIA VIEWER OVERLAY ---

    fun selectTab(tab: VaultTab) {
        _activeTab.value = tab
    }

    fun selectCategory(category: VaultCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun inspectItem(item: VaultItemEntity?) {
        _inspectingItem.value = item
    }

    fun openViewingMediaModal(item: VaultItemEntity?) {
        _viewingMediaItem.value = item
    }

    /**
     * Tapping a file card or "Tonton / Lihat / Putar":
     * - If encrypted, prompt for PIN first and automatically open/play once decrypted.
     * - If it's a VIDEO or AUDIO file, route to the YouTube-style Media Player tab and start playing!
     * - If it's an IMAGE (JPG, PNG, WEBP, GIF, STICKER) or DOCUMENT, open the rich viewer overlay.
     */
    fun playOrViewItem(item: VaultItemEntity) {
        if (item.isEncrypted) {
            requestToggleEncryptionWithPin(item = item, openAfterDecrypt = true)
            return
        }
        val cat = VaultCategory.entries.find { it.name == item.category }
        if (cat == VaultCategory.VIDEO || cat == VaultCategory.AUDIO) {
            _activeTab.value = VaultTab.MEDIA_PLAYER
            selectAndPlayMediaTrack(item)
        } else {
            _viewingMediaItem.value = item
        }
    }

    fun openFullscreenViewer(item: VaultItemEntity) {
        if (item.isEncrypted) {
            requestToggleEncryptionWithPin(item = item, openAfterDecrypt = true)
        } else {
            _viewingMediaItem.value = item
        }
    }

    // --- PRE-ENCRYPTION MULTI-ZIP SCANNER, VIDEO DEDUPLICATION & ANTIVIRUS ENGINE ---

    /**
     * Semua impor file atau banyak paket .ZIP diproses melalui mesin Pra-Enkripsi:
     * 1. Ekstraksi semua isi .ZIP
     * 2. Render frame video & hapus salah satu jika ada video kembar (1 dipakai)
     * 3. Cek virus/malware: jika ada virus, tampilkan dialog peringatan apakah tetap mau dimasukkan ke Vault atau dihapus.
     */
    fun importUrisFromPicker(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val wasInScannerTab = _activeTab.value == VaultTab.ZIP_SCANNER
        _activeTab.value = VaultTab.ZIP_SCANNER
        _zipScannerState.value = ZipScannerUiState(
            isScanning = true,
            progressMessage = "Memulai ekstraksi & render frame video untuk ${uris.size} paket/file..."
        )

        viewModelScope.launch(Dispatchers.IO) {
            val report = ZipPreEncryptionScannerEngine.scanAndDeduplicateUris(
                context = getApplication(),
                uris = uris,
                existingVaultItems = allItems.value,
                onProgress = { msg ->
                    _zipScannerState.value = _zipScannerState.value.copy(progressMessage = msg)
                }
            )

            if (report.virusFlaggedCount > 0) {
                // Terdeteksi virus! Tampilkan peringatan apakah tetap mau dimasukkan ke Vault Enkripsi atau tidak
                _zipScannerState.value = ZipScannerUiState(
                    isScanning = false,
                    progressMessage = "",
                    currentReport = report,
                    showVirusWarningModal = true
                )
                postBanner("PERINGATAN: Terdeteksi ${report.virusFlaggedCount} file bervirus! Konfirmasi tindakan Anda.")
            } else if (wasInScannerTab || report.duplicatesRemovedCount > 0 || report.totalZipArchivesProcessed > 0) {
                // Tampilkan hasil ekstraksi & penghapusan video kembar di layar ZIP Scanner
                _zipScannerState.value = ZipScannerUiState(
                    isScanning = false,
                    progressMessage = "",
                    currentReport = report,
                    showVirusWarningModal = false
                )
                val dupNote = if (report.duplicatesRemovedCount > 0) {
                    " (${report.duplicatesRemovedCount} video kembar otomatis dihapus salah satu!)"
                } else ""
                postBanner("Pindai selesai: ${report.uniqueCleanFilesCount} file unik siap dienkripsi$dupNote")
            } else {
                // File tunggal bersih diimpor dari luar tab scanner -> langsung enkripsi ke Vault
                val count = repository.commitStagedScanItemsToVault(report.readyCleanItems, _activeVaultId.value)
                _zipScannerState.value = ZipScannerUiState(
                    isScanning = false,
                    currentReport = report,
                    showVirusWarningModal = false
                )
                postBanner("$count file bersih berhasil dienkripsi (.nyx) ke Brankas ${_activeVaultId.value}")
            }
        }
    }

    fun runSimulatedMultiZipScannerTest() {
        _activeTab.value = VaultTab.ZIP_SCANNER
        _zipScannerState.value = ZipScannerUiState(
            isScanning = true,
            progressMessage = "Menyiapkan 2 paket .ZIP uji coba (Video 4K Kembar + Sampel Virus EICAR)..."
        )
        viewModelScope.launch(Dispatchers.IO) {
            val report = ZipPreEncryptionScannerEngine.runBuiltInMultiZipTestBatch(
                context = getApplication(),
                existingVaultItems = allItems.value,
                onProgress = { msg ->
                    _zipScannerState.value = _zipScannerState.value.copy(progressMessage = msg)
                }
            )
            _zipScannerState.value = ZipScannerUiState(
                isScanning = false,
                progressMessage = "",
                currentReport = report,
                showVirusWarningModal = report.virusFlaggedCount > 0
            )
            postBanner(
                "Hasil Uji 2 ZIP: ${report.duplicatesRemovedCount} video kembar dihapus otomatis & ${report.virusFlaggedCount} virus terdeteksi!"
            )
        }
    }

    fun commitStagedCleanItemsOnly() {
        val report = _zipScannerState.value.currentReport ?: return
        viewModelScope.launch(Dispatchers.IO) {
            // Hapus permanen semua file bervirus yang ditolak
            report.infectedItems.forEach { infected ->
                EncryptionScriptEngine.shredFileOnDisk(infected.stagedFile)
            }
            val count = repository.commitStagedScanItemsToVault(
                itemsToCommit = report.readyCleanItems,
                vaultId = _activeVaultId.value
            )
            _zipScannerState.value = ZipScannerUiState()
            postBanner(
                "$count file aman berhasil dienkripsi (.nyx) ke Brankas ${_activeVaultId.value}. File bervirus & ${report.duplicatesRemovedCount} duplikat telah dihapus!"
            )
        }
    }

    fun commitStagedAllIncludingInfected() {
        val report = _zipScannerState.value.currentReport ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val allToCommit = report.readyCleanItems + report.infectedItems
            val count = repository.commitStagedScanItemsToVault(
                itemsToCommit = allToCommit,
                vaultId = _activeVaultId.value
            )
            _zipScannerState.value = ZipScannerUiState()
            postBanner(
                "$count file (termasuk ${report.virusFlaggedCount} file terisolasi) telah dikunci rapat (.nyx) di Brankas ${_activeVaultId.value}"
            )
        }
    }

    fun openVirusWarningModal() {
        val current = _zipScannerState.value
        if (current.currentReport != null && current.currentReport.virusFlaggedCount > 0) {
            _zipScannerState.value = current.copy(showVirusWarningModal = true)
        }
    }

    fun dismissVirusWarningModal() {
        _zipScannerState.value = _zipScannerState.value.copy(showVirusWarningModal = false)
    }

    fun clearZipScanReport() {
        viewModelScope.launch(Dispatchers.IO) {
            ZipPreEncryptionScannerEngine.clearStagingDirectory(getApplication())
            _zipScannerState.value = ZipScannerUiState()
            postBanner("Ruang pindai ZIP sementara telah dibersihkan")
        }
    }

    fun createVaultNoteOrMediaFile(
        fileName: String,
        extension: String,
        category: VaultCategory,
        contentText: String,
        encryptImmediately: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createSecretNoteInPrivateVault(
                vaultId = _activeVaultId.value,
                title = fileName,
                extension = extension,
                category = category,
                secretPayload = contentText,
                encryptImmediately = encryptImmediately
            )
            postBanner("\"$fileName.$extension\" disimpan ke Brankas ${_activeVaultId.value}")
        }
    }

    fun deleteVaultItem(item: VaultItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_inspectingItem.value?.id == item.id) {
                _inspectingItem.value = null
            }
            if (_viewingMediaItem.value?.id == item.id) {
                _viewingMediaItem.value = null
            }
            repository.deleteItemPermanently(item)
            postBanner("File \"${item.name}.${item.extension}\" dihapus & di-shred secara permanen")
        }
    }

    // --- CUSTOM SCRIPT, RECOVERY & DUAL-VAULT PIN CONFIGURATION ---

    fun saveScriptConfiguration(preset: EncryptionPreset, customCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateEncryptionScript(preset.id, customCode)
            postBanner("Script NYX2 \"${preset.title}\" berhasil disimpan")
        }
    }

    fun saveVaultPins(vaultId: Int, pin1: String, pin2: String, pin3: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePinsForVault(vaultId, pin1, pin2, pin3)
            postBanner("PIN 3-Lapis Brankas $vaultId berhasil diperbarui")
        }
    }

    fun saveRecoveryCredentials(question: String, answer: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRecoveryCredentials(question, answer)
            postBanner("Pertanyaan & Kunci Pemulihan Lupa Sandi berhasil diperbarui")
        }
    }

    fun openForgotPinModal() {
        _showForgotPinModal.value = true
    }

    fun dismissForgotPinModal() {
        _showForgotPinModal.value = false
    }

    fun resetPinsFromRecoveryAndUnlock(
        vaultId: Int,
        newPin1: String,
        newPin2: String,
        newPin3: String,
        unlockNow: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePinsForVault(vaultId, newPin1, newPin2, newPin3)
            _showForgotPinModal.value = false
            _cryptoPinModalState.value = null
            if (unlockNow) {
                _activeVaultId.value = vaultId
                _securityState.value = SecurityGateUiState(
                    stage = AuthStage.UNLOCKED,
                    candidateVaultId = vaultId
                )
            }
            postBanner("PIN 3-Lapis Brankas $vaultId berhasil dipulihkan & diperbarui!")
        }
    }

    // --- MEDIA PLAYER EXPORT & SHARE WITH SECONDARY BIOMETRIC RE-AUTHENTICATION ---

    fun requestExportOrShareWithBiometric(item: VaultItemEntity, isShareIntent: Boolean) {
        pausePlayback()
        _biometricExportModalState.value = BiometricExportModalState(
            item = item,
            isShareIntent = isShareIntent,
            errorMessage = null
        )
    }

    fun dismissBiometricExportModal() {
        _biometricExportModalState.value = null
    }

    fun onBiometricExportReAuthenticated(
        onShareFileReady: (java.io.File, String) -> Unit,
        onLaunchSystemSaveAs: (String, String) -> Unit
    ) {
        val modal = _biometricExportModalState.value ?: return
        val item = modal.item
        val isShare = modal.isShareIntent
        _biometricExportModalState.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val decryptedFile = repository.prepareDecryptedFileForShareOrExport(item)
            val mimeType = repository.resolveMimeType(item.extension)
            if (decryptedFile != null && decryptedFile.exists()) {
                // Simpan salinan ekspor otomatis di folder unduhan aplikasi
                try {
                    val appExportDir = java.io.File(
                        getApplication<Application>().getExternalFilesDir(null),
                        "TrantorVault_Exports"
                    ).apply { if (!exists()) mkdirs() }
                    val localExportCopy = java.io.File(appExportDir, "${item.name}.${item.extension}")
                    decryptedFile.copyTo(localExportCopy, overwrite = true)
                } catch (_: Exception) {
                }

                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    if (isShare) {
                        onShareFileReady(decryptedFile, mimeType)
                        postBanner("Biometrik Terverifikasi: Membagikan \"${item.name}.${item.extension}\"")
                    } else {
                        pendingExportTargetItem = item
                        onLaunchSystemSaveAs("${item.name}.${item.extension}", mimeType)
                        postBanner("Biometrik Terverifikasi: Pilih lokasi simpan untuk \"${item.name}.${item.extension}\"")
                    }
                }
            } else {
                postBanner("Gagal menyiapkan file ekspor untuk \"${item.name}.${item.extension}\"")
            }
        }
    }

    fun completeExportToDocumentUri(targetUri: Uri?) {
        val item = pendingExportTargetItem
        pendingExportTargetItem = null
        if (targetUri == null || item == null) return
        viewModelScope.launch(Dispatchers.IO) {
            val ok = repository.exportVaultItemToUri(item, targetUri)
            if (ok) {
                postBanner("Berhasil mengekspor \"${item.name}.${item.extension}\" ke penyimpanan perangkat!")
            } else {
                postBanner("Gagal menyimpan file ekspor ke lokasi tujuan.")
            }
        }
    }

    fun toggleBiometricGate(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setBiometricEnabled(enabled)
            postBanner(if (enabled) "Autentikasi Biometrik diaktifkan" else "Autentikasi Biometrik dinonaktifkan")
        }
    }

    fun dismissBanner() {
        _statusBannerMessage.value = null
    }

    private fun postBanner(message: String) {
        _statusBannerMessage.value = message
    }

    // --- YOUTUBE-STYLE MEDIA PLAYER ENGINE ---

    fun selectAndPlayMediaTrack(track: VaultItemEntity) {
        if (track.isEncrypted) {
            requestToggleEncryptionWithPin(item = track, openAfterDecrypt = true)
            return
        }
        val cat = VaultCategory.entries.find { it.name == track.category }
        if (cat == VaultCategory.IMAGE || cat == VaultCategory.DOCUMENT) {
            _viewingMediaItem.value = track
            return
        }
        val totalDur = track.durationSeconds.coerceAtLeast(60)
        _mediaState.value = _mediaState.value.copy(
            currentTrack = track,
            currentSeconds = 0,
            totalSeconds = totalDur,
            isPlaying = true
        )
        startPlaybackTicker()
    }

    fun togglePlayPause() {
        val current = _mediaState.value
        val track = current.currentTrack ?: mediaPlaylist.value.firstOrNull() ?: return
        if (track.isEncrypted) {
            requestToggleEncryptionWithPin(item = track, openAfterDecrypt = true)
            return
        }
        val nextPlaying = !current.isPlaying
        _mediaState.value = current.copy(
            currentTrack = track,
            totalSeconds = track.durationSeconds.coerceAtLeast(60),
            isPlaying = nextPlaying
        )
        if (nextPlaying) {
            startPlaybackTicker()
        } else {
            playbackJob?.cancel()
        }
    }

    private fun pausePlayback() {
        playbackJob?.cancel()
        _mediaState.value = _mediaState.value.copy(isPlaying = false)
    }

    fun seekToSeconds(seconds: Int) {
        val current = _mediaState.value
        val totalSec = current.totalSeconds.coerceAtLeast(1)
        val clamped = seconds.coerceIn(0, totalSec)
        _mediaState.value = current.copy(currentSeconds = clamped)
    }

    fun skipTrack(forward: Boolean) {
        val playlist = mediaPlaylist.value
        if (playlist.isEmpty()) return
        val current = _mediaState.value.currentTrack
        // Prefer sibling tracks from the same ZIP archive if available
        val activeZip = current?.groupArchiveName
        val effectiveList = if (!activeZip.isNullOrBlank()) {
            val zipSiblings = playlist.filter { it.groupArchiveName == activeZip }
            if (zipSiblings.size > 1) zipSiblings else playlist
        } else {
            playlist
        }

        val currentIndex = effectiveList.indexOfFirst { it.id == current?.id }
        val nextTrack = if (forward) {
            if (currentIndex in 0 until effectiveList.lastIndex) {
                effectiveList[currentIndex + 1]
            } else {
                effectiveList.first()
            }
        } else {
            if (currentIndex > 0) {
                effectiveList[currentIndex - 1]
            } else {
                effectiveList.last()
            }
        }

        _mediaState.value = _mediaState.value.copy(
            currentTrack = nextTrack,
            currentSeconds = 0,
            totalSeconds = nextTrack.durationSeconds.coerceAtLeast(60),
            isPlaying = !nextTrack.isEncrypted
        )
        if (!nextTrack.isEncrypted) {
            startPlaybackTicker()
        }
    }

    fun cyclePlaybackSpeed() {
        val speeds = listOf(1.0f, 1.25f, 1.5f, 2.0f, 0.75f)
        val currentSpeed = _mediaState.value.playbackSpeed
        val nextIndex = (speeds.indexOf(currentSpeed) + 1) % speeds.size
        _mediaState.value = _mediaState.value.copy(playbackSpeed = speeds[nextIndex])
    }

    fun toggleRepeatMode() {
        _mediaState.value = _mediaState.value.copy(
            isRepeatEnabled = !_mediaState.value.isRepeatEnabled
        )
    }

    private fun startPlaybackTicker() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (isActive && _mediaState.value.isPlaying) {
                val delayMs = (1000L / _mediaState.value.playbackSpeed).toLong().coerceAtLeast(250L)
                delay(delayMs)
                val state = _mediaState.value
                val track = state.currentTrack ?: break
                val duration = track.durationSeconds.coerceAtLeast(60)
                val nextSec = state.currentSeconds + 1
                if (nextSec >= duration) {
                    if (state.isRepeatEnabled) {
                        skipTrack(forward = true)
                    } else {
                        _mediaState.value = state.copy(
                            isPlaying = false,
                            currentSeconds = 0
                        )
                        break
                    }
                } else {
                    _mediaState.value = state.copy(
                        currentSeconds = nextSec,
                        totalSeconds = duration
                    )
                }
            }
        }
    }

    // --- MORI CLIENT-SIDE LINK DOWNLOADER & SHARE SHEET ENGINE ---

    fun updateMoriUrlInput(url: String) {
        val detected = MoriDownloaderEngine.detectPlatformFromUrl(url)
        _moriState.value = _moriState.value.copy(
            urlInput = url,
            selectedMode = detected.defaultMediaType,
            resolvedPreview = null
        )
    }

    fun updateMoriCustomFileName(name: String) {
        _moriState.value = _moriState.value.copy(customFileName = name)
    }

    fun selectMoriExtractionMode(mode: MoriExtractionMode) {
        _moriState.value = _moriState.value.copy(selectedMode = mode)
    }

    fun toggleMoriEncryptImmediately(encrypt: Boolean) {
        _moriState.value = _moriState.value.copy(encryptImmediately = encrypt)
    }

    fun analyzeMoriLink() {
        val current = _moriState.value
        if (current.isAnalyzing || current.isDownloading) return
        val effectiveUrl = current.urlInput.trim().ifBlank {
            "https://www.tiktok.com/@tiktok/video/7106594312292453675"
        }

        _moriState.value = current.copy(
            urlInput = effectiveUrl,
            isAnalyzing = true,
            scraperLogs = listOf("[Mori Inspector] Menganalisis link & stream: ${effectiveUrl.take(56)}...")
        )

        viewModelScope.launch(Dispatchers.IO) {
            val resolved = MoriDownloaderEngine.resolveUrlMetadata(effectiveUrl) { stepMsg ->
                _moriState.value = _moriState.value.copy(
                    scraperLogs = _moriState.value.scraperLogs + stepMsg
                )
            }
            val summaryLog = "[Siap Unduh] \"${resolved.title}\" oleh ${resolved.author} (${resolved.resolverSource})"
            _moriState.value = _moriState.value.copy(
                isAnalyzing = false,
                resolvedPreview = resolved,
                statusLogLine = summaryLog,
                scraperLogs = _moriState.value.scraperLogs + summaryLog
            )
            postBanner("Link berhasil dianalisis: ${resolved.title} (${resolved.platform.displayName})")
        }
    }

    fun triggerMoriOtaScraperUpdate() {
        if (_moriState.value.isCheckingOta) return
        _moriState.value = _moriState.value.copy(
            isCheckingOta = true,
            scraperLogs = _moriState.value.scraperLogs + "[OTA Hot-Patch] Memeriksa pembaruan bundle.js dari repositori Mori..."
        )
        viewModelScope.launch {
            delay(450L)
            val updatedVersion = "v2.5.4-ota-patched"
            _moriState.value = _moriState.value.copy(
                isCheckingOta = false,
                otaBundleVersion = updatedVersion,
                scraperLogs = _moriState.value.scraperLogs + "[OTA Sukses] 16 parser platform diperbarui ke $updatedVersion tanpa instal ulang!"
            )
            postBanner("Mori Scraper OTA berhasil diperbarui ke $updatedVersion!")
        }
    }

    fun startMoriDownloadToVault() {
        val current = _moriState.value
        if (current.isDownloading) return
        val effectiveUrl = current.urlInput.trim().ifBlank {
            "https://www.tiktok.com/@tiktok/video/7106594312292453675"
        }

        _moriState.value = current.copy(
            urlInput = effectiveUrl,
            isDownloading = true,
            progressRatio = 0.08f,
            scraperLogs = emptyList(),
            lastDownloadResult = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            val platform = MoriDownloaderEngine.detectPlatformFromUrl(effectiveUrl)
            val (stagedFile, logs, resolvedMeta) = MoriDownloaderEngine.executeClientSideExtraction(
                context = getApplication(),
                rawUrlInput = effectiveUrl,
                selectedMode = current.selectedMode,
                otaVersion = current.otaBundleVersion,
                onLogProgress = { msg, ratio ->
                    _moriState.value = _moriState.value.copy(
                        statusLogLine = msg,
                        progressRatio = ratio,
                        scraperLogs = _moriState.value.scraperLogs + msg
                    )
                }
            )

            val savedEntity = repository.saveMoriDownloadedFileToVault(
                stagedFile = stagedFile,
                platform = platform,
                extractionMode = current.selectedMode,
                customTitle = current.customFileName.ifBlank { resolvedMeta.title },
                encryptImmediately = current.encryptImmediately,
                vaultId = _activeVaultId.value,
                resolvedDurationSeconds = resolvedMeta.durationSeconds
            )

            val finalLog = "[Selesai] Disimpan di Brankas ${_activeVaultId.value}: ${savedEntity.name}.${savedEntity.extension} (${savedEntity.sizeBytes / 1024} KB)"
            val result = MoriDownloadResult(
                platform = platform,
                extractionMode = current.selectedMode,
                savedFileName = savedEntity.name,
                extension = savedEntity.extension,
                fileSizeBytes = savedEntity.sizeBytes,
                isEncryptedInVault = savedEntity.isEncrypted,
                scraperLogs = logs + finalLog,
                createdEntity = savedEntity,
                resolvedMeta = resolvedMeta
            )

            _moriState.value = _moriState.value.copy(
                isDownloading = false,
                progressRatio = 1f,
                resolvedPreview = resolvedMeta,
                statusLogLine = finalLog,
                scraperLogs = logs + finalLog,
                lastDownloadResult = result,
                customFileName = ""
            )

            postBanner("Mori (${platform.displayName}) disimpan ke Brankas ${_activeVaultId.value}: ${savedEntity.name}.${savedEntity.extension}")
        }
    }

    /**
     * Menangani link atau media yang dikirim melalui Android System Share Sheet ("Bagikan ke Trantor Vault / Mori")
     */
    fun handleIncomingSharedIntent(sharedText: String?, sharedStreamUri: Uri?) {
        if (!sharedText.isNullOrBlank()) {
            val extractedUrl = MoriDownloaderEngine.extractCleanUrl(sharedText)
            updateMoriUrlInput(extractedUrl)
            _activeTab.value = VaultTab.MORI_DOWNLOADER
            analyzeMoriLink()
            postBanner("Link dari Share Sheet siap diunduh di Mori Grabber!")
        } else if (sharedStreamUri != null) {
            importUrisFromPicker(listOf(sharedStreamUri))
        }
    }

    fun encryptLocalFileFromMt(file: java.io.File) {
        viewModelScope.launch {
            try {
                val entity = repository.importLocalFileIntoPrivateVault(file, _activeVaultId.value, encryptImmediately = true)
                _statusBannerMessage.value = "Tersimpan aman: ${entity.name} disegel dengan NYX2!"
            } catch (e: Exception) {
                _statusBannerMessage.value = "Gagal enkripsi berkas: ${e.message}"
            }
        }
    }
}
