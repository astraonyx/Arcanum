package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Engine untuk Pencadangan & Pemulihan Brankas Terenkripsi ke Google Cloud / Google Drive.
 * Bersifat 100% MANUAL (Zero Automatic Sync) dan Zero-Knowledge:
 * File yang dikirim keluar SELALU dalam bentuk ciphertext terenkripsi AES-256-GCM.
 */
object GoogleCloudVaultBackupEngine {

    private const val BACKUP_MAGIC = "ARCANUM_ENC_CLOUD_V2"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12
    private const val SALT_LENGTH = 16
    private const val PBKDF2_ITERATIONS = 40000

    /**
     * Mengemas seluruh file dan metadata Room dari vaultId yang dipilih,
     * lalu mengenkripsinya dengan AES-256-GCM menggunakan PIN 3-lapis.
     */
    suspend fun createEncryptedVaultPackage(
        context: Context,
        vaultId: Int,
        items: List<VaultItemEntity>,
        p1: String,
        p2: String,
        p3: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.cacheDir, "cloud_backups").apply { if (!exists()) mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val tempZipFile = File(backupDir, "temp_vault_${vaultId}_${timeStamp}.zip")
            val finalEncFile = File(backupDir, "Arcanum_Vault_${vaultId}_Backup_${timeStamp}.arcanum.enc")

            // 1. Buat Arsip ZIP berisi data JSON & file terenkripsi
            ZipOutputStream(FileOutputStream(tempZipFile)).use { zos ->
                // A. Tulis Manifest Metadata
                val manifestJson = JSONObject().apply {
                    put("version", 2)
                    put("vaultId", vaultId)
                    put("timestamp", System.currentTimeMillis())
                    val itemsArray = JSONArray()
                    items.forEach { item ->
                        val obj = JSONObject().apply {
                            put("id", item.id)
                            put("name", item.name)
                            put("extension", item.extension)
                            put("sizeBytes", item.sizeBytes)
                            put("category", item.category)
                            put("isEncrypted", item.isEncrypted)
                            put("durationSeconds", item.durationSeconds)
                            put("groupArchiveName", item.groupArchiveName ?: "")
                        }
                        itemsArray.put(obj)
                    }
                    put("items", itemsArray)
                }

                zos.putNextEntry(ZipEntry("manifest.json"))
                zos.write(manifestJson.toString(2).toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()

                // B. Simpan file internal brankas (baik .NYX maupun plain internal)
                items.forEach { item ->
                    val fileToBundle = if (item.isEncrypted) {
                        File(item.internalNyxPath)
                    } else {
                        File(item.internalPlainPath)
                    }

                    if (fileToBundle.exists() && fileToBundle.length() > 0) {
                        val entryName = "files/${item.id}_${fileToBundle.name}"
                        zos.putNextEntry(ZipEntry(entryName))
                        FileInputStream(fileToBundle).use { fis ->
                            fis.copyTo(zos)
                        }
                        zos.closeEntry()
                    }
                }
            }

            // 2. Enkripsi tempZipFile dengan AES-256-GCM menggunakan KDF PIN 3-lapis
            val rawZipBytes = tempZipFile.readBytes()
            tempZipFile.delete()

            val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
            val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }

            // Key derivation dari p1 + p2 + p3
            val keySecret = "$p1::$p2::$p3::arcanum_cloud_salt_v2"
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(keySecret.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
            val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            val cipherText = cipher.doFinal(rawZipBytes)

            // Tulis Final Encrypted Package
            FileOutputStream(finalEncFile).use { fos ->
                fos.write(BACKUP_MAGIC.toByteArray(StandardCharsets.UTF_8)) // Magic Header
                fos.write(salt)                                             // 16 bytes salt
                fos.write(iv)                                               // 12 bytes IV
                fos.write(cipherText)                                       // GCM ciphertext + tag
            }

            Result.success(finalEncFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Memverifikasi dan mendekripsi file backup `.arcanum.enc`, lalu mengekstraknya
     * kembali ke direktori brankas lokal target.
     */
    suspend fun restoreEncryptedVaultPackage(
        context: Context,
        targetVaultId: Int,
        encPackageFile: File,
        p1: String,
        p2: String,
        p3: String,
        repository: VaultRepository
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (!encPackageFile.exists()) return@withContext Result.failure(Exception("File backup tidak ditemukan."))

            FileInputStream(encPackageFile).use { fis ->
                val magicBytes = ByteArray(BACKUP_MAGIC.length)
                val readMagic = fis.read(magicBytes)
                if (readMagic != BACKUP_MAGIC.length || String(magicBytes, StandardCharsets.UTF_8) != BACKUP_MAGIC) {
                    return@withContext Result.failure(Exception("Format file backup tidak valid atau rusak."))
                }

                val salt = ByteArray(SALT_LENGTH).also { fis.read(it) }
                val iv = ByteArray(GCM_IV_LENGTH).also { fis.read(it) }
                val cipherBytes = fis.readBytes()

                // Key derivation
                val keySecret = "$p1::$p2::$p3::arcanum_cloud_salt_v2"
                val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                val spec = PBEKeySpec(keySecret.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
                val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
                val rawZipBytes = cipher.doFinal(cipherBytes)

                // Ekstrak ZIP
                val tempZip = File(context.cacheDir, "restore_temp.zip").apply { writeBytes(rawZipBytes) }
                var restoredCount = 0

                ZipInputStream(FileInputStream(tempZip)).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    var manifestJson: JSONObject? = null
                    val extractedFiles = mutableMapOf<String, File>()

                    while (entry != null) {
                        if (entry.name == "manifest.json") {
                            val baos = ByteArrayOutputStream()
                            zis.copyTo(baos)
                            manifestJson = JSONObject(baos.toString("UTF-8"))
                        } else if (entry.name.startsWith("files/")) {
                            val targetFile = File(context.filesDir, "vault_${targetVaultId}_" + File(entry.name).name)
                            FileOutputStream(targetFile).use { fos ->
                                zis.copyTo(fos)
                            }
                            extractedFiles[entry.name] = targetFile
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }

                    if (manifestJson != null) {
                        val itemsArray = manifestJson.getJSONArray("items")
                        for (i in 0 until itemsArray.length()) {
                            val obj = itemsArray.getJSONObject(i)
                            val originalId = obj.getLong("id")
                            val name = obj.getString("name")
                            val ext = obj.getString("extension")
                            val isEncrypted = obj.getBoolean("isEncrypted")
                            val sizeBytes = obj.getLong("sizeBytes")
                            val categoryStr = obj.optString("category", VaultCategory.DOCUMENT.name)
                            val category = try { VaultCategory.valueOf(categoryStr) } catch (_: Exception) { VaultCategory.DOCUMENT }

                            // Tautkan kembali file fisik ke entity baru
                            val matchedFile = extractedFiles.entries.find { it.key.contains("${originalId}_") }?.value
                            val plainPath = if (!isEncrypted) (matchedFile?.absolutePath ?: "") else ""
                            val nyxPath = if (isEncrypted) (matchedFile?.absolutePath ?: "") else ""

                            if (matchedFile != null && matchedFile.exists()) {
                                repository.insertItem(
                                    VaultItemEntity(
                                        name = name,
                                        extension = ext,
                                        sizeBytes = sizeBytes,
                                        isEncrypted = isEncrypted,
                                        vaultProfileId = targetVaultId,
                                        category = category.name,
                                        internalPlainPath = plainPath,
                                        internalNyxPath = nyxPath,
                                        addedAt = System.currentTimeMillis()
                                    )
                                )
                                restoredCount++
                            }
                        }
                    }
                }

                tempZip.delete()
                Result.success(restoredCount)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
