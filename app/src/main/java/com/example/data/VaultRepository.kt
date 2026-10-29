package com.example.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

private val Context.securityDataStore by preferencesDataStore(name = "trantor_nyx2_prod_prefs_v3")

data class SecurityConfig(
    val isFirstRunCompleted: Boolean = false,
    val vault1Pin1: String = "111111",
    val vault1Pin2: String = "222222",
    val vault1Pin3: String = "333333",
    val vault2Pin1: String = "444444",
    val vault2Pin2: String = "555555",
    val vault2Pin3: String = "666666",
    val biometricEnabled: Boolean = true,
    val fingerprintEnrolled: Boolean = true,
    val recoveryQuestion: String = "Apa kata kunci pemulihan rahasia Anda?",
    val recoveryAnswer: String = "nyxvault",
    val activeScriptName: String = EncryptionPreset.NYX2_DUAL_CIPHER.id,
    val customScriptCode: String = EncryptionPreset.NYX2_DUAL_CIPHER.scriptTemplate
) {
    fun getPinsForVault(vaultId: Int): Triple<String, String, String> {
        return if (vaultId == 2) {
            Triple(vault2Pin1, vault2Pin2, vault2Pin3)
        } else {
            Triple(vault1Pin1, vault1Pin2, vault1Pin3)
        }
    }
}

class VaultRepository(
    private val context: Context,
    private val vaultDao: VaultDao
) {
    private object Keys {
        val FIRST_RUN_COMPLETED = booleanPreferencesKey("first_run_completed")
        val V1_PIN_1 = stringPreferencesKey("v1_pin_1")
        val V1_PIN_2 = stringPreferencesKey("v1_pin_2")
        val V1_PIN_3 = stringPreferencesKey("v1_pin_3")
        val V2_PIN_1 = stringPreferencesKey("v2_pin_1")
        val V2_PIN_2 = stringPreferencesKey("v2_pin_2")
        val V2_PIN_3 = stringPreferencesKey("v2_pin_3")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val FINGERPRINT_ENROLLED = booleanPreferencesKey("fingerprint_enrolled")
        val RECOVERY_QUESTION = stringPreferencesKey("recovery_question")
        val RECOVERY_ANSWER = stringPreferencesKey("recovery_answer")
        val ACTIVE_SCRIPT_ID = stringPreferencesKey("active_script_id")
        val CUSTOM_SCRIPT_CODE = stringPreferencesKey("custom_script_code")
    }

    val allItems: Flow<List<VaultItemEntity>> = vaultDao.getAllItems()

    fun getItemsForVault(vaultId: Int): Flow<List<VaultItemEntity>> =
        vaultDao.getItemsForVault(vaultId)

    val securityConfigFlow: Flow<SecurityConfig> = context.securityDataStore.data.map { prefs ->
        SecurityConfig(
            isFirstRunCompleted = prefs[Keys.FIRST_RUN_COMPLETED] ?: false,
            vault1Pin1 = prefs[Keys.V1_PIN_1] ?: "111111",
            vault1Pin2 = prefs[Keys.V1_PIN_2] ?: "222222",
            vault1Pin3 = prefs[Keys.V1_PIN_3] ?: "333333",
            vault2Pin1 = prefs[Keys.V2_PIN_1] ?: "444444",
            vault2Pin2 = prefs[Keys.V2_PIN_2] ?: "555555",
            vault2Pin3 = prefs[Keys.V2_PIN_3] ?: "666666",
            biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: true,
            fingerprintEnrolled = prefs[Keys.FINGERPRINT_ENROLLED] ?: true,
            recoveryQuestion = prefs[Keys.RECOVERY_QUESTION] ?: "Apa kata kunci pemulihan rahasia Anda?",
            recoveryAnswer = prefs[Keys.RECOVERY_ANSWER] ?: "nyxvault",
            activeScriptName = prefs[Keys.ACTIVE_SCRIPT_ID] ?: EncryptionPreset.NYX2_DUAL_CIPHER.id,
            customScriptCode = prefs[Keys.CUSTOM_SCRIPT_CODE] ?: EncryptionPreset.NYX2_DUAL_CIPHER.scriptTemplate
        )
    }

    suspend fun completeFirstRunSetup(
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
        context.securityDataStore.edit { prefs ->
            prefs[Keys.FIRST_RUN_COMPLETED] = true
            prefs[Keys.V1_PIN_1] = v1p1
            prefs[Keys.V1_PIN_2] = v1p2
            prefs[Keys.V1_PIN_3] = v1p3
            prefs[Keys.V2_PIN_1] = v2p1
            prefs[Keys.V2_PIN_2] = v2p2
            prefs[Keys.V2_PIN_3] = v2p3
            prefs[Keys.BIOMETRIC_ENABLED] = biometricEnabled
            prefs[Keys.FINGERPRINT_ENROLLED] = true
            prefs[Keys.RECOVERY_QUESTION] = recoveryQuestion.trim().ifBlank { "Apa kata kunci pemulihan rahasia Anda?" }
            prefs[Keys.RECOVERY_ANSWER] = recoveryAnswer.trim().ifBlank { "nyxvault" }
        }
    }

    suspend fun updateRecoveryCredentials(question: String, answer: String) {
        context.securityDataStore.edit { prefs ->
            if (question.isNotBlank()) prefs[Keys.RECOVERY_QUESTION] = question.trim()
            if (answer.isNotBlank()) prefs[Keys.RECOVERY_ANSWER] = answer.trim()
        }
    }

    suspend fun updatePinsForVault(vaultId: Int, pin1: String, pin2: String, pin3: String) {
        if (pin1.length == 6 && pin2.length == 6 && pin3.length == 6) {
            context.securityDataStore.edit { prefs ->
                if (vaultId == 2) {
                    prefs[Keys.V2_PIN_1] = pin1
                    prefs[Keys.V2_PIN_2] = pin2
                    prefs[Keys.V2_PIN_3] = pin3
                } else {
                    prefs[Keys.V1_PIN_1] = pin1
                    prefs[Keys.V1_PIN_2] = pin2
                    prefs[Keys.V1_PIN_3] = pin3
                }
            }
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.securityDataStore.edit { prefs ->
            prefs[Keys.BIOMETRIC_ENABLED] = enabled
            if (enabled) prefs[Keys.FINGERPRINT_ENROLLED] = true
        }
    }

    suspend fun updateEncryptionScript(presetId: String, customCode: String) {
        context.securityDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_SCRIPT_ID] = presetId
            prefs[Keys.CUSTOM_SCRIPT_CODE] = customCode
        }
    }

    /**
     * Menyiapkan salinan terdekripsi dari file brankas untuk keperluan Ekspor / Bagikan (Share)
     * setelah pengguna berhasil melewati verifikasi biometrik sekunder (Secondary Biometric Re-Authentication).
     */
    suspend fun prepareDecryptedFileForShareOrExport(item: VaultItemEntity): File? {
        val cfg = securityConfigFlow.first()
        val (pin1, pin2, pin3) = cfg.getPinsForVault(item.vaultProfileId)
        val exportDir = File(context.cacheDir, "vault_exports").apply { if (!exists()) mkdirs() }
        val safeFileName = "${item.name.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")}.${item.extension}"
        val outFile = File(exportDir, safeFileName)

        val plainSource = File(item.internalPlainPath)
        val nyxSource = File(item.internalNyxPath)

        return try {
            when {
                !item.isEncrypted && plainSource.exists() && plainSource.length() > 0L -> {
                    plainSource.copyTo(outFile, overwrite = true)
                    outFile
                }
                nyxSource.exists() && nyxSource.length() > 0L -> {
                    val ok = EncryptionScriptEngine.openNyxContainerToFile(
                        nyxFile = nyxSource,
                        plainOutFile = outFile,
                        pin1 = pin1,
                        pin2 = pin2,
                        pin3 = pin3
                    )
                    if (ok && outFile.exists()) outFile else null
                }
                plainSource.exists() -> {
                    plainSource.copyTo(outFile, overwrite = true)
                    outFile
                }
                else -> {
                    outFile.writeBytes(item.rawContentPreview.toByteArray(Charsets.UTF_8))
                    outFile
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Mengekspor file terdekripsi ke URI tujuan (Storage Access Framework CreateDocument)
     * serta menyimpannya ke folder unduhan eksternal aplikasi.
     */
    suspend fun exportVaultItemToUri(item: VaultItemEntity, targetUri: Uri): Boolean {
        val decryptedTemp = prepareDecryptedFileForShareOrExport(item) ?: return false
        return try {
            context.contentResolver.openOutputStream(targetUri)?.use { output ->
                decryptedTemp.inputStream().use { input ->
                    input.copyTo(output, bufferSize = 65536)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun resolveMimeType(extension: String): String = when (extension.lowercase()) {
        "mp4", "m4v" -> "video/mp4"
        "mov" -> "video/quicktime"
        "mkv" -> "video/x-matroska"
        "webm" -> "video/webm"
        "mp3" -> "audio/mpeg"
        "flac" -> "audio/flac"
        "wav" -> "audio/wav"
        "m4a", "aac" -> "audio/mp4"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp", "sticker" -> "image/webp"
        "gif" -> "image/gif"
        "zip" -> "application/zip"
        "pdf" -> "application/pdf"
        "txt", "md", "json", "js", "kt" -> "text/plain"
        else -> "application/octet-stream"
    }

    suspend fun insertItem(item: VaultItemEntity): Long = vaultDao.insertItem(item)

    suspend fun updateItem(item: VaultItemEntity) = vaultDao.updateItem(item)

    suspend fun deleteItemPermanently(item: VaultItemEntity) {
        if (item.internalPlainPath.isNotBlank()) {
            EncryptionScriptEngine.shredFileOnDisk(File(item.internalPlainPath))
        }
        if (item.internalNyxPath.isNotBlank()) {
            EncryptionScriptEngine.shredFileOnDisk(File(item.internalNyxPath))
        }
        vaultDao.deleteById(item.id)
    }

    private fun getVaultStorageDir(vaultId: Int): File {
        val dir = File(context.filesDir, "nyx_private_vault/vault_$vaultId")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun resolveCategoryFromExtension(ext: String): VaultCategory = when (ext.lowercase()) {
        "mp4", "mkv", "mov", "webm", "avi", "3gp", "m4v" -> VaultCategory.VIDEO
        "jpg", "jpeg", "png", "webp", "heic", "gif", "sticker", "bmp" -> VaultCategory.IMAGE
        "mp3", "wav", "flac", "ogg", "m4a", "aac", "opus" -> VaultCategory.AUDIO
        "zip", "rar", "7z", "tar", "kt", "js", "py", "sh" -> VaultCategory.ARCHIVE
        else -> VaultCategory.DOCUMENT
    }

    private fun extractMediaDurationSeconds(file: File, category: VaultCategory): Int {
        if (category != VaultCategory.VIDEO && category != VaultCategory.AUDIO) return 0
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val durMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            (durMs / 1000L).toInt().coerceAtLeast(1)
        } catch (_: Exception) {
            0
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Mengimpor file asli atau paket `.zip` ke dalam internal private vault (`filesDir/nyx_private_vault`).
     * Jika file adalah `.zip`, semua video/gambar/audio/dokumen di dalamnya diekstrak dan dikelompokkan
     * dengan `groupArchiveName` yang sama sehingga bisa diputar berurutan di bawah video player seperti YouTube!
     */
    suspend fun importUriIntoPrivateVault(uri: Uri, vaultId: Int): Int {
        val cfg = securityConfigFlow.first()
        val (pin1, pin2, pin3) = cfg.getPinsForVault(vaultId)
        val vaultDir = getVaultStorageDir(vaultId)

        var displayName = "Hidden_File_${System.currentTimeMillis() % 10000}"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    displayName = cursor.getString(nameIndex) ?: displayName
                }
            }
        } catch (_: Exception) {
        }

        val dotIdx = displayName.lastIndexOf('.')
        val baseName = if (dotIdx > 0) displayName.substring(0, dotIdx) else displayName
        val ext = if (dotIdx > 0 && dotIdx < displayName.length - 1) {
            displayName.substring(dotIdx + 1).lowercase()
        } else "bin"

        // Jika file yang diimpor adalah arsip .zip, ekstrak semua file media/dokumen di dalamnya!
        if (ext == "zip") {
            val importedCount = unpackZipIntoPrivateVault(
                uri = uri,
                zipDisplayName = displayName,
                vaultId = vaultId,
                vaultDir = vaultDir,
                pin1 = pin1,
                pin2 = pin2,
                pin3 = pin3,
                cipherAlgo = cfg.activeScriptName
            )
            if (importedCount > 0) return importedCount
        }

        val uniqueStamp = System.currentTimeMillis()
        val safeBase = baseName.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(40)
        val plainFile = File(vaultDir, "${uniqueStamp}_${safeBase}.$ext")
        val nyxFile = File(vaultDir, "${uniqueStamp}_${safeBase}.$ext.nyx")

        // Salin stream dari URI ke internal private file
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(plainFile).use { output ->
                input.copyTo(output, bufferSize = 65536)
            }
        } ?: return 0

        val fileSize = plainFile.length().coerceAtLeast(1L)
        val category = resolveCategoryFromExtension(ext)
        val durationSec = extractMediaDurationSeconds(plainFile, category)

        val rawPreview = readSafeFileHeaderPreview(plainFile, displayName)

        // Enkripsi biner ke .nyx dan shred file plaintext agar benar-benar tersembunyi
        val (hexPreview, checksum) = EncryptionScriptEngine.sealFileToNyxContainer(
            plainFile = plainFile,
            nyxOutFile = nyxFile,
            pin1 = pin1,
            pin2 = pin2,
            pin3 = pin3,
            shredSource = true
        )

        val entity = VaultItemEntity(
            vaultProfileId = vaultId,
            name = baseName,
            extension = ext,
            category = category.name,
            groupArchiveName = null,
            internalPlainPath = plainFile.absolutePath,
            internalNyxPath = nyxFile.absolutePath,
            sizeBytes = fileSize,
            paddedSizeBytes = EncryptionScriptEngine.padmeSize(fileSize),
            isEncrypted = true,
            cipherAlgorithm = cfg.activeScriptName,
            checksumPreview = checksum,
            rawContentPreview = rawPreview,
            encryptedHexPreview = hexPreview,
            durationSeconds = durationSec,
            mediaUriString = Uri.fromFile(plainFile).toString()
        )
        vaultDao.insertItem(entity)
        return 1
    }

    private suspend fun unpackZipIntoPrivateVault(
        uri: Uri,
        zipDisplayName: String,
        vaultId: Int,
        vaultDir: File,
        pin1: String,
        pin2: String,
        pin3: String,
        cipherAlgo: String
    ): Int {
        var count = 0
        val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
        ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val entryName = File(entry.name).name
                    if (!entryName.startsWith(".") && entryName.isNotBlank()) {
                        val dot = entryName.lastIndexOf('.')
                        val itemBase = if (dot > 0) entryName.substring(0, dot) else entryName
                        val itemExt = if (dot > 0 && dot < entryName.length - 1) {
                            entryName.substring(dot + 1).lowercase()
                        } else "dat"

                        val stamp = System.nanoTime()
                        val safeName = itemBase.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(36)
                        val plainFile = File(vaultDir, "${stamp}_${safeName}.$itemExt")
                        val nyxFile = File(vaultDir, "${stamp}_${safeName}.$itemExt.nyx")

                        FileOutputStream(plainFile).use { fos ->
                            val buffer = ByteArray(65536)
                            var len: Int
                            while (zis.read(buffer).also { len = it } > 0) {
                                fos.write(buffer, 0, len)
                            }
                        }

                        val fSize = plainFile.length()
                        if (fSize > 0L) {
                            val cat = resolveCategoryFromExtension(itemExt)
                            val dur = extractMediaDurationSeconds(plainFile, cat)
                            val preview = readSafeFileHeaderPreview(plainFile, entryName)

                            // Untuk file dari paket ZIP, kita buat kontainer .nyx terenkripsi
                            val (hex, checksum) = EncryptionScriptEngine.sealFileToNyxContainer(
                                plainFile = plainFile,
                                nyxOutFile = nyxFile,
                                pin1 = pin1,
                                pin2 = pin2,
                                pin3 = pin3,
                                shredSource = true
                            )

                            vaultDao.insertItem(
                                VaultItemEntity(
                                    vaultProfileId = vaultId,
                                    name = itemBase,
                                    extension = itemExt,
                                    category = cat.name,
                                    groupArchiveName = zipDisplayName,
                                    internalPlainPath = plainFile.absolutePath,
                                    internalNyxPath = nyxFile.absolutePath,
                                    sizeBytes = fSize,
                                    paddedSizeBytes = EncryptionScriptEngine.padmeSize(fSize),
                                    isEncrypted = true,
                                    cipherAlgorithm = cipherAlgo,
                                    checksumPreview = checksum,
                                    rawContentPreview = preview,
                                    encryptedHexPreview = hex,
                                    durationSeconds = dur,
                                    mediaUriString = Uri.fromFile(plainFile).toString(),
                                    addedAt = System.currentTimeMillis() + count
                                )
                            )
                            count++
                        } else {
                            plainFile.delete()
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return count
    }

    private fun readSafeFileHeaderPreview(file: File, fallbackName: String): String {
        return try {
            file.inputStream().use { stream ->
                val buf = ByteArray(96)
                val read = stream.read(buf)
                if (read > 0) {
                    buf.take(read).joinToString("") { b ->
                        val c = b.toInt() and 0xFF
                        if (c in 32..126) c.toChar().toString() else "."
                    }
                } else "BINARY_STREAM::$fallbackName"
            }
        } catch (_: Exception) {
            "BINARY_STREAM::$fallbackName"
        }
    }

    /**
     * Membuat catatan rahasia terenkripsi langsung di dalam penyimpanan internal brankas
     */
    suspend fun createSecretNoteInPrivateVault(
        vaultId: Int,
        title: String,
        extension: String,
        category: VaultCategory,
        secretPayload: String,
        encryptImmediately: Boolean
    ) {
        val cfg = securityConfigFlow.first()
        val (pin1, pin2, pin3) = cfg.getPinsForVault(vaultId)
        val vaultDir = getVaultStorageDir(vaultId)

        val cleanTitle = title.trim().ifBlank { "Catatan_Rahasia_${System.currentTimeMillis() % 1000}" }
        val cleanExt = extension.trim().lowercase().removePrefix(".").ifBlank { "txt" }
        val payloadBytes = secretPayload.ifBlank { "CATATAN_RAHASIA::$cleanTitle" }.toByteArray(Charsets.UTF_8)

        val stamp = System.currentTimeMillis()
        val safeBase = cleanTitle.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(36)
        val plainFile = File(vaultDir, "${stamp}_${safeBase}.$cleanExt")
        val nyxFile = File(vaultDir, "${stamp}_${safeBase}.$cleanExt.nyx")

        plainFile.writeBytes(payloadBytes)
        val fSize = plainFile.length()

        val (hex, checksum) = EncryptionScriptEngine.sealFileToNyxContainer(
            plainFile = plainFile,
            nyxOutFile = nyxFile,
            pin1 = pin1,
            pin2 = pin2,
            pin3 = pin3,
            shredSource = encryptImmediately
        )

        vaultDao.insertItem(
            VaultItemEntity(
                vaultProfileId = vaultId,
                name = cleanTitle,
                extension = cleanExt,
                category = category.name,
                groupArchiveName = null,
                internalPlainPath = plainFile.absolutePath,
                internalNyxPath = nyxFile.absolutePath,
                sizeBytes = fSize,
                paddedSizeBytes = EncryptionScriptEngine.padmeSize(fSize),
                isEncrypted = encryptImmediately,
                cipherAlgorithm = cfg.activeScriptName,
                checksumPreview = checksum,
                rawContentPreview = secretPayload.take(250),
                encryptedHexPreview = hex,
                durationSeconds = 0,
                mediaUriString = Uri.fromFile(plainFile).toString()
            )
        )
    }

    /**
     * Menjalankan enkripsi atau dekripsi biner nyata pada file di internal private vault
     */
    suspend fun toggleBinaryEncryptionOnDisk(item: VaultItemEntity, targetEncryptState: Boolean): VaultItemEntity {
        val cfg = securityConfigFlow.first()
        val (pin1, pin2, pin3) = cfg.getPinsForVault(item.vaultProfileId)
        val plainFile = File(item.internalPlainPath)
        val nyxFile = File(item.internalNyxPath)

        if (targetEncryptState) {
            // Mengunci kembali: jika plainFile ada, enkripsi ke nyxFile dan shred plainFile
            if (plainFile.exists()) {
                val (hex, checksum) = EncryptionScriptEngine.sealFileToNyxContainer(
                    plainFile = plainFile,
                    nyxOutFile = nyxFile,
                    pin1 = pin1,
                    pin2 = pin2,
                    pin3 = pin3,
                    shredSource = true
                )
                val updated = item.copy(
                    isEncrypted = true,
                    encryptedHexPreview = hex,
                    checksumPreview = checksum
                )
                vaultDao.updateItem(updated)
                return updated
            } else {
                val updated = item.copy(isEncrypted = true)
                vaultDao.updateItem(updated)
                return updated
            }
        } else {
            // Membuka kunci: dekripsi nyxFile menjadi plainFile agar bisa diputar di VideoView / Image Viewer
            if (nyxFile.exists()) {
                EncryptionScriptEngine.openNyxContainerToFile(
                    nyxFile = nyxFile,
                    plainOutFile = plainFile,
                    pin1 = pin1,
                    pin2 = pin2,
                    pin3 = pin3
                )
            }
            val updated = item.copy(
                isEncrypted = false,
                mediaUriString = if (plainFile.exists()) Uri.fromFile(plainFile).toString() else item.mediaUriString
            )
            vaultDao.updateItem(updated)
            return updated
        }
    }

    /**
     * Memindahkan daftar file hasil ekstraksi & pemindaian (yang sudah bebas duplikat dan disetujui pengguna)
     * dari folder staging ke dalam brankas privat internal lalu mengenkripsinya menjadi kontainer biner .nyx.
     */
    suspend fun commitStagedScanItemsToVault(
        itemsToCommit: List<StagedScanItem>,
        vaultId: Int
    ): Int {
        val cfg = securityConfigFlow.first()
        val (pin1, pin2, pin3) = cfg.getPinsForVault(vaultId)
        val vaultDir = getVaultStorageDir(vaultId)
        var committedCount = 0

        for ((idx, staged) in itemsToCommit.withIndex()) {
            if (!staged.stagedFile.exists() || staged.stagedFile.length() <= 0L) continue

            val stamp = System.currentTimeMillis() + idx
            val safeBase = staged.fileName.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(36)
            val plainFile = File(vaultDir, "${stamp}_${safeBase}.${staged.extension}")
            val nyxFile = File(vaultDir, "${stamp}_${safeBase}.${staged.extension}.nyx")

            try {
                staged.stagedFile.copyTo(plainFile, overwrite = true)
                EncryptionScriptEngine.shredFileOnDisk(staged.stagedFile)

                val fSize = plainFile.length().coerceAtLeast(1L)
                val preview = readSafeFileHeaderPreview(plainFile, "${staged.fileName}.${staged.extension}")

                val (hex, checksum) = EncryptionScriptEngine.sealFileToNyxContainer(
                    plainFile = plainFile,
                    nyxOutFile = nyxFile,
                    pin1 = pin1,
                    pin2 = pin2,
                    pin3 = pin3,
                    shredSource = true
                )

                vaultDao.insertItem(
                    VaultItemEntity(
                        vaultProfileId = vaultId,
                        name = staged.fileName,
                        extension = staged.extension,
                        category = staged.category.name,
                        groupArchiveName = staged.sourceZipName.takeIf { it != "Impor Langsung" },
                        internalPlainPath = plainFile.absolutePath,
                        internalNyxPath = nyxFile.absolutePath,
                        sizeBytes = fSize,
                        paddedSizeBytes = EncryptionScriptEngine.padmeSize(fSize),
                        isEncrypted = true,
                        cipherAlgorithm = cfg.activeScriptName,
                        checksumPreview = checksum,
                        rawContentPreview = preview,
                        encryptedHexPreview = hex,
                        durationSeconds = staged.durationSeconds,
                        mediaUriString = Uri.fromFile(plainFile).toString(),
                        addedAt = stamp
                    )
                )
                committedCount++
            } catch (_: Exception) {
            }
        }

        ZipPreEncryptionScannerEngine.clearStagingDirectory(context)
        return committedCount
    }

    /**
     * MT Explorer Cryptographic Bridge:
     * Menyalin & menyegel berkas lokal dari penyimpanan perangkat langsung ke wadah privat NYX2 Brankas Arcanum.
     */
    suspend fun importLocalFileIntoPrivateVault(
        sourceFile: File,
        vaultId: Int,
        encryptImmediately: Boolean = true
    ): VaultItemEntity {
        val cfg = securityConfigFlow.first()
        val (pin1, pin2, pin3) = cfg.getPinsForVault(vaultId)
        val vaultDir = getVaultStorageDir(vaultId)
        val stamp = System.currentTimeMillis()
        val safeName = sourceFile.nameWithoutExtension.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(40)
            .ifBlank { "mt_file" }
        val ext = sourceFile.extension.lowercase().ifBlank { "bin" }
        val category = resolveCategoryFromExtension(ext)

        val plainFile = File(vaultDir, "${stamp}_${safeName}.$ext")
        val nyxFile = File(vaultDir, "${stamp}_${safeName}.$ext.nyx")

        sourceFile.copyTo(plainFile, overwrite = true)
        val fSize = plainFile.length().coerceAtLeast(1L)
        val preview = readSafeFileHeaderPreview(plainFile, sourceFile.name)

        val (hex, checksum) = EncryptionScriptEngine.sealFileToNyxContainer(
            plainFile = plainFile,
            nyxOutFile = nyxFile,
            pin1 = pin1,
            pin2 = pin2,
            pin3 = pin3,
            shredSource = encryptImmediately
        )

        val entity = VaultItemEntity(
            name = sourceFile.name,
            originalUri = sourceFile.absolutePath,
            internalPlainPath = plainFile.absolutePath,
            internalEncryptedPath = nyxFile.absolutePath,
            extension = ext,
            category = category.name,
            sizeBytes = fSize,
            isEncrypted = encryptImmediately,
            previewSnippet = preview,
            addedTimestamp = stamp,
            nyxHeaderHex = hex,
            sha256Checksum = checksum,
            vaultId = vaultId,
            durationSeconds = extractMediaDurationSeconds(plainFile, category)
        )

        val newId = vaultDao.insertItem(entity)
        return entity.copy(id = newId)
    }

    /**
     * Menyimpan file hasil unduhan/ekstraksi Mori Downloader ke dalam Brankas Privat (Brankas 1 / Brankas 2)
     */
    suspend fun saveMoriDownloadedFileToVault(
        stagedFile: File,
        platform: MoriPlatform,
        extractionMode: MoriExtractionMode,
        customTitle: String?,
        encryptImmediately: Boolean,
        vaultId: Int,
        resolvedDurationSeconds: Int = 0
    ): VaultItemEntity {
        val cfg = securityConfigFlow.first()
        val (pin1, pin2, pin3) = cfg.getPinsForVault(vaultId)
        val vaultDir = getVaultStorageDir(vaultId)
        val stamp = System.currentTimeMillis()

        val rawTitle = customTitle?.trim()?.takeIf { it.isNotEmpty() }
            ?: stagedFile.nameWithoutExtension
        val safeBase = rawTitle.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(36)
            .ifBlank { "Mori_${platform.name.lowercase()}" }
        val ext = stagedFile.extension.lowercase().ifBlank { extractionMode.defaultExtension }

        val plainFile = File(vaultDir, "${stamp}_${safeBase}.$ext")
        val nyxFile = File(vaultDir, "${stamp}_${safeBase}.$ext.nyx")

        stagedFile.copyTo(plainFile, overwrite = true)
        EncryptionScriptEngine.shredFileOnDisk(stagedFile)

        val fSize = plainFile.length().coerceAtLeast(1L)
        val preview = readSafeFileHeaderPreview(plainFile, "$safeBase.$ext")

        val (hex, checksum) = EncryptionScriptEngine.sealFileToNyxContainer(
            plainFile = plainFile,
            nyxOutFile = nyxFile,
            pin1 = pin1,
            pin2 = pin2,
            pin3 = pin3,
            shredSource = encryptImmediately
        )

        val durationSec = if (resolvedDurationSeconds > 0) {
            resolvedDurationSeconds
        } else {
            when (extractionMode) {
                MoriExtractionMode.HD_VIDEO_NO_WM -> 15
                MoriExtractionMode.AUDIO_STREAM_MP3 -> 30
                else -> 0
            }
        }

        val entity = VaultItemEntity(
            vaultProfileId = vaultId,
            name = safeBase,
            extension = ext,
            category = extractionMode.vaultCategory.name,
            groupArchiveName = "Mori • ${platform.displayName}",
            internalPlainPath = plainFile.absolutePath,
            internalNyxPath = nyxFile.absolutePath,
            sizeBytes = fSize,
            paddedSizeBytes = EncryptionScriptEngine.padmeSize(fSize),
            isEncrypted = encryptImmediately,
            cipherAlgorithm = cfg.activeScriptName,
            checksumPreview = checksum,
            rawContentPreview = preview,
            encryptedHexPreview = hex,
            durationSeconds = durationSec,
            mediaUriString = Uri.fromFile(plainFile).toString(),
            addedAt = stamp
        )
        val insertedId = vaultDao.insertItem(entity).toInt()
        return entity.copy(id = insertedId)
    }
}

