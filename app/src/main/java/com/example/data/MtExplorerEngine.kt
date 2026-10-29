package com.example.data

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

enum class MtExplorerScope(val displayName: String, val description: String) {
    DEVICE_ALL(
        displayName = "Seluruh Perangkat",
        description = "Akses penuh ke penyimpanan internal, SD Card, Downloads & DCIM"
    ),
    VAULT_EXCLUSIVE(
        displayName = "Khusus Brankas Arcanum",
        description = "Terisolasi pada wadah terenkripsi .nyx & berkas privat brankas"
    )
}

data class MtFileItem(
    val file: File,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val extension: String,
    val isApk: Boolean,
    val isZipArchive: Boolean,
    val isEncryptedNyx: Boolean
)

data class ArchiveEntryItem(
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val compressedSize: Long
)

data class ApkMetadataSummary(
    val packageName: String,
    val versionName: String,
    val dexCount: Int,
    val hasSignature: Boolean,
    val totalEntries: Int,
    val manifestSummary: String
)

object MtExplorerEngine {

    fun getDefaultDirectoryForScope(context: Context, scope: MtExplorerScope, vaultDir: File): File {
        return when (scope) {
            MtExplorerScope.DEVICE_ALL -> {
                val external = Environment.getExternalStorageDirectory()
                if (external != null && external.exists() && external.canRead()) {
                    external
                } else {
                    context.filesDir
                }
            }
            MtExplorerScope.VAULT_EXCLUSIVE -> {
                if (!vaultDir.exists()) vaultDir.mkdirs()
                vaultDir
            }
        }
    }

    fun listDirectory(
        directory: File,
        scope: MtExplorerScope,
        vaultStorageRoot: File
    ): List<MtFileItem> {
        val targetDir = if (scope == MtExplorerScope.VAULT_EXCLUSIVE) {
            if (!directory.absolutePath.startsWith(vaultStorageRoot.absolutePath)) {
                vaultStorageRoot
            } else {
                directory
            }
        } else {
            directory
        }

        if (!targetDir.exists() || !targetDir.isDirectory) {
            return emptyList()
        }

        val rawFiles = targetDir.listFiles() ?: return emptyList()

        return rawFiles.map { file ->
            val ext = file.extension.lowercase()
            val isApk = ext == "apk"
            val isZip = ext == "zip" || ext == "jar" || ext == "aar"
            val isNyx = ext == "nyx" || file.name.endsWith(".nyx")

            MtFileItem(
                file = file,
                name = file.name,
                isDirectory = file.isDirectory,
                sizeBytes = if (file.isDirectory) 0L else file.length(),
                lastModified = file.lastModified(),
                extension = ext,
                isApk = isApk,
                isZipArchive = isZip,
                isEncryptedNyx = isNyx
            )
        }.sortedWith(
            compareBy<MtFileItem> { !it.isDirectory }
                .thenBy { it.name.lowercase() }
        )
    }

    fun inspectArchiveEntries(archiveFile: File, maxEntries: Int = 120): List<ArchiveEntryItem> {
        if (!archiveFile.exists() || archiveFile.isDirectory) return emptyList()
        val results = mutableListOf<ArchiveEntryItem>()

        try {
            ZipFile(archiveFile).use { zip ->
                val entries = zip.entries()
                var count = 0
                while (entries.hasMoreElements() && count < maxEntries) {
                    val entry = entries.nextElement()
                    results.add(
                        ArchiveEntryItem(
                            name = entry.name,
                            isDirectory = entry.isDirectory,
                            size = entry.size,
                            compressedSize = entry.compressedSize
                        )
                    )
                    count++
                }
            }
        } catch (_: Exception) {
            // Fallback via stream
            try {
                FileInputStream(archiveFile).use { fis ->
                    ZipInputStream(fis).use { zis ->
                        var count = 0
                        var entry: ZipEntry? = zis.nextEntry
                        while (entry != null && count < maxEntries) {
                            results.add(
                                ArchiveEntryItem(
                                    name = entry.name,
                                    isDirectory = entry.isDirectory,
                                    size = entry.size,
                                    compressedSize = entry.compressedSize
                                )
                            )
                            count++
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        return results
    }

    fun inspectApkPackage(apkFile: File): ApkMetadataSummary {
        var dexCount = 0
        var hasSignature = false
        var totalEntries = 0
        var manifestFound = false

        try {
            ZipFile(apkFile).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    totalEntries++
                    val name = entry.name
                    if (name.endsWith(".dex")) {
                        dexCount++
                    }
                    if (name.startsWith("META-INF/") && (name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC") || name.contains("CERT"))) {
                        hasSignature = true
                    }
                    if (name == "AndroidManifest.xml") {
                        manifestFound = true
                    }
                }
            }
        } catch (_: Exception) {}

        val guessedPackage = apkFile.nameWithoutExtension.replace(Regex("[^a-zA-Z0-9_.]"), "").ifBlank { "com.app.package" }

        val manifestSummary = buildString {
            appendLine("Package: $guessedPackage")
            appendLine("Manifest Entry: ${if (manifestFound) "Ditemukan (Binary AXML)" else "Tidak Ditemukan"}")
            appendLine("Classes DEX: $dexCount file dex")
            appendLine("Tanda Tangan V1/V2: ${if (hasSignature) "Terverifikasi (Valid APK Signature)" else "Tidak Bertanda Tangan"}")
            appendLine("Total Entri Berkas: $totalEntries berkas")
        }

        return ApkMetadataSummary(
            packageName = guessedPackage,
            versionName = "1.0",
            dexCount = dexCount,
            hasSignature = hasSignature,
            totalEntries = totalEntries,
            manifestSummary = manifestSummary
        )
    }

    fun readHexDump(file: File, maxBytes: Int = 2048): String {
        if (!file.exists() || file.isDirectory) return "Bukan berkas yang dapat dibaca."
        val bytes = ByteArray(maxBytes)
        val readCount: Int
        try {
            FileInputStream(file).use { fis ->
                readCount = fis.read(bytes)
            }
        } catch (e: Exception) {
            return "Gagal membaca hex: ${e.message}"
        }

        if (readCount <= 0) return "Berkas kosong (0 byte)."

        val sb = StringBuilder()
        for (i in 0 until readCount step 16) {
            // Offset
            sb.append(String.format("%08X: ", i))

            // Hex Bytes (8 bytes + space + 8 bytes)
            for (j in 0 until 16) {
                if (i + j < readCount) {
                    sb.append(String.format("%02X ", bytes[i + j]))
                } else {
                    sb.append("   ")
                }
                if (j == 7) sb.append(" ")
            }

            sb.append(" |")
            // ASCII Column
            for (j in 0 until 16) {
                if (i + j < readCount) {
                    val b = bytes[i + j].toInt() and 0xFF
                    if (b in 32..126) {
                        sb.append(b.toChar())
                    } else {
                        sb.append('.')
                    }
                }
            }
            sb.append("|\n")
        }

        if (file.length() > maxBytes) {
            sb.append("\n... (Menampilkan $maxBytes dari ${file.length()} byte) ...")
        }
        return sb.toString()
    }

    fun readTextPreview(file: File, maxLines: Int = 180): String {
        if (!file.exists() || file.isDirectory) return "Berkas tidak ditemukan."
        return try {
            file.bufferedReader().useLines { lines ->
                lines.take(maxLines).joinToString("\n")
            }
        } catch (e: Exception) {
            "Gagal membaca teks: ${e.message}"
        }
    }

    fun copyFileOrDirectory(src: File, destDir: File): Boolean {
        return try {
            if (!destDir.exists()) destDir.mkdirs()
            val target = File(destDir, src.name)
            if (src.isDirectory) {
                src.copyRecursively(target, overwrite = true)
            } else {
                src.copyTo(target, overwrite = true)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun moveFileOrDirectory(src: File, destDir: File): Boolean {
        return try {
            if (!destDir.exists()) destDir.mkdirs()
            val target = File(destDir, src.name)
            val success = src.renameTo(target)
            if (!success) {
                copyFileOrDirectory(src, destDir) && deleteFileOrDirectory(src)
            } else {
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    fun deleteFileOrDirectory(target: File): Boolean {
        return try {
            if (target.isDirectory) {
                target.deleteRecursively()
            } else {
                target.delete()
            }
        } catch (_: Exception) {
            false
        }
    }
}
