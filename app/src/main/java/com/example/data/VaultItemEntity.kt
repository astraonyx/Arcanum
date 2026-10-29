package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VaultCategory(val label: String, val badgeHex: Long) {
    VIDEO("Video (MP4/MOV)", 0xFFEB6A79),
    IMAGE("Gambar & Sticker", 0xFF6B48C8),
    AUDIO("Audio & Musik", 0xFF2A7B6F),
    DOCUMENT("Dokumen & NYX2", 0xFFF7B948),
    ARCHIVE("Paket ZIP & Arsip", 0xFF8FB832)
}

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vaultProfileId: Int = 1, // 1 = Brankas Utama, 2 = Brankas Kedua (Dual Mode)
    val name: String,
    val extension: String, // mp4, mov, mkv, webm, jpg, png, webp, gif, sticker, flac, mp3, pdf, nyx
    val category: String, // VaultCategory.name
    val groupArchiveName: String? = null, // Nama paket .zip asal (agar video dalam 1 ZIP tampil satu playlist di bawah player seperti YouTube)
    val internalPlainPath: String = "", // Lokasi file saat didekripsi sementara di internal private vault
    val internalNyxPath: String = "", // Lokasi file biner terenkripsi .nyx di internal private vault
    val sizeBytes: Long,
    val paddedSizeBytes: Long = 0L, // Ukuran setelah Padmé padding sesuai nyx_crypt_optimized.js
    val isEncrypted: Boolean = true,
    val cipherAlgorithm: String = "NYX2-CHACHA20-AES256GCM",
    val checksumPreview: String = "NYX2::0x8F4C...E29A",
    val rawContentPreview: String = "",
    val encryptedHexPreview: String = "",
    val durationSeconds: Int = 0, // > 0 untuk VIDEO / AUDIO
    val mediaUriString: String? = null,
    val synthPresetIndex: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
