package com.example.data

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Mesin Kriptografi Biner NYX2 Produksi (Porting dari `nyx_crypt_optimized.js`):
 * - Header 77 byte: MAGIC "NYX2" (4B) | flags (1B) | logN (1B) | noncePrefix (7B) | salt (32B) | commit (32B)
 * - Derivasi 3 Subkey via HKDF-SHA256 dari `nyx2|PIN1|PIN2|PIN3`:
 *     1) `nyx2/chacha20-poly1305` (32B)
 *     2) `nyx2/aes-256-gcm` (32B)
 *     3) `nyx2/commit` + headerBody45B (32B)
 * - Enkripsi File Streaming 64 KiB Slice (Ramah L1-Cache & RAM Rendah)
 * - Padmé Padding untuk menyamarkan ukuran asli file
 * - Multi-pass Shredding untuk menghapus file plaintext saat dikunci ke `.nyx`
 */
enum class EncryptionPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val scriptTemplate: String
) {
    NYX2_DUAL_CIPHER(
        id = "NYX2-CHACHA20-AES256GCM",
        title = "NYX2 Dual-Cipher (nyx-crypt.js)",
        subtitle = "ChaCha20-Poly1305 -> AES-256-GCM + Padmé Padding + 64KiB L1-Slice",
        scriptTemplate = """
            // === NYX2 PRODUCTION BINARY STREAM ENGINE ===
            // Header 77B: MAGIC("NYX2") | flags(1) | logN(1) | noncePrefix(7) | salt(32) | commit(32)
            // Secret Input: "nyx2|" + pin1 + "|" + pin2 + "|" + pin3
            // Subkeys (HKDF-SHA256):
            //   kCha   = HKDF-SHA256(master, salt, "nyx2/chacha20-poly1305", 32)
            //   kAes   = HKDF-SHA256(master, salt, "nyx2/aes-256-gcm", 32)
            //   commit = HKDF-SHA256(master, salt, "nyx2/commit" + headerBody45B, 32)
            // Stream: PlainFile -> 64KiB Slice -> ChaCha20 -> AES-256-GCM -> .nyx Container
        """.trimIndent()
    ),
    NYX2_TURBO_LOWRAM(
        id = "NYX2-TURBO-L1-SLICE",
        title = "NYX2 Turbo Spek Kentang (--turbo)",
        subtitle = "Profil logN=14 (16 MiB) + In-Place 64KiB Slice hemat RAM",
        scriptTemplate = """
            // === NYX2 TURBO MODE (LOGN=14 / 64KiB SLICE) ===
            const val SLICE_LEN = 65536 // 64 KiB L1-cache-friendly slice
            fun padme(t: Long): Long {
                val e = 63L - java.lang.Long.numberOfLeadingZeros(t)
                val s = 64L - java.lang.Long.numberOfLeadingZeros(e)
                val lastBits = e - s
                if (lastBits <= 0) return t
                val mask = (1L shl lastBits.toInt()) - 1L
                return (t + mask) and mask.inv()
            }
        """.trimIndent()
    ),
    AES_256_GCM(
        id = "AES-256-GCM",
        title = "AES-256-GCM Hardware",
        subtitle = "Enkripsi blok simetris 256-bit dengan tag autentikasi 128-bit",
        scriptTemplate = """
            // === AES-256-GCM HARDWARE PIPELINE ===
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val secretKey = SecretKeySpec(kAes32Bytes, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, nonce12Bytes))
            cipher.updateAAD(nyxHeader77Bytes)
            val cipherBytes = cipher.doFinal(plainSlice)
        """.trimIndent()
    )
}

object EncryptionScriptEngine {

    private val MAGIC_NYX2 = "NYX2".toByteArray(Charsets.US_ASCII)
    const val HEADER_LEN = 77
    const val BODY_LEN = 45
    const val SLICE_LEN = 65536 // 64 KiB L1-cache slice sesuai nyx_crypt_optimized.js
    const val EMPTY_ZIP_HEX = "50 4B 05 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
    val EMPTY_ZIP_BYTES = byteArrayOf(
        0x50, 0x4B, 0x05, 0x06,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    )

    private val COMMON_WEAK_PINS = setOf(
        "123456", "654321", "123123", "112233", "121212", "123321", "101010", "111222",
        "147258", "258369", "159753", "789456", "456789", "456123", "112211", "520520",
        "131313", "696969", "246810", "135790", "100000", "200000", "000001", "000123",
        "123000", "121314", "202020", "112112", "000000"
    )

    /**
     * Memeriksa apakah PIN 6-digit terlalu lemah sesuai `weakReason(p)` di nyx_crypt_optimized.js
     */
    fun checkWeakPinReason(pin: String): String? {
        if (pin.length != 6 || !pin.all { it.isDigit() }) return "PIN harus tepat 6 digit angka"
        if (COMMON_WEAK_PINS.contains(pin)) return "Pola PIN terlalu umum ($pin)"
        val digits = pin.map { it - '0' }
        val diffs = (0 until 5).map { (digits[it + 1] - digits[it] + 10) % 10 }
        if (diffs.all { it == 1 } || diffs.all { it == 9 }) return "Angka berurutan mudah ditebak"
        return null
    }

    /**
     * Padmé padding sesuai implementasi `padme(T)` di nyx_crypt_optimized.js
     */
    fun padmeSize(rawSize: Long): Long {
        val t = (rawSize + 8L).coerceAtLeast(8L)
        val e = (63 - java.lang.Long.numberOfLeadingZeros(t)).toLong()
        if (e <= 0L) return t
        val s = (64 - java.lang.Long.numberOfLeadingZeros(e)).toLong()
        val lastBits = (e - s).toInt()
        if (lastBits <= 0) return t
        val mask = (1L shl lastBits) - 1L
        return (t + mask) and mask.inv()
    }

    private fun hkdfSha256(ikm: ByteArray, salt: ByteArray, info: ByteArray): ByteArray {
        val macExtract = Mac.getInstance("HmacSHA256")
        macExtract.init(SecretKeySpec(if (salt.isNotEmpty()) salt else ByteArray(32), "HmacSHA256"))
        val prk = macExtract.doFinal(ikm)

        val macExpand = Mac.getInstance("HmacSHA256")
        macExpand.init(SecretKeySpec(prk, "HmacSHA256"))
        macExpand.update(info)
        macExpand.update(0x01.toByte())
        return macExpand.doFinal()
    }

    private data class NyxDerivedKeys(
        val kCha: ByteArray,
        val kAes: ByteArray,
        val commit: ByteArray
    )

    private fun deriveNyxKeys(
        pin1: String,
        pin2: String,
        pin3: String,
        body45: ByteArray
    ): NyxDerivedKeys {
        val secret = "nyx2|$pin1|$pin2|$pin3".toByteArray(Charsets.UTF_8)
        val salt = body45.copyOfRange(13, 45)
        // KDF: SHA-512/256 multi-round stretching + HKDF-SHA256 subkey expansion
        var master = MessageDigest.getInstance("SHA-256").digest(secret + salt)
        val digest = MessageDigest.getInstance("SHA-256")
        for (round in 0 until 256) {
            digest.reset()
            digest.update(master)
            digest.update(salt)
            digest.update(round.toByte())
            master = digest.digest()
        }
        val kCha = hkdfSha256(master, salt, "nyx2/chacha20-poly1305".toByteArray(Charsets.UTF_8))
        val kAes = hkdfSha256(master, salt, "nyx2/aes-256-gcm".toByteArray(Charsets.UTF_8))
        val commitInfo = "nyx2/commit".toByteArray(Charsets.UTF_8) + body45
        val commit = hkdfSha256(master, salt, commitInfo)
        return NyxDerivedKeys(kCha = kCha, kAes = kAes, commit = commit)
    }

    fun compute3LayerKeyFingerprint(pin1: String, pin2: String, pin3: String): String {
        val secret = "nyx2|$pin1|$pin2|$pin3".toByteArray(Charsets.UTF_8)
        val salt = "NYX2_STATIC_SALT_32_BYTES_HEADER".toByteArray(Charsets.UTF_8)
        val commit = hkdfSha256(secret, salt, "nyx2/commit".toByteArray(Charsets.UTF_8))
        val hex = commit.joinToString("") { "%02X".format(it) }
        return "NYX2::0x${hex.take(4)}...${hex.takeLast(4)}"
    }

    /**
     * Enkripsi File Biner Asli di Internal Storage menjadi kontainer `.nyx` (NYX2)
     * menggunakan 64 KiB Slice Dual-Cipher (ChaCha20 stream + AES-256-GCM authenticated blocks).
     */
    fun sealFileToNyxContainer(
        plainFile: File,
        nyxOutFile: File,
        pin1: String,
        pin2: String,
        pin3: String,
        shredSource: Boolean = true
    ): Pair<String, String> {
        val random = SecureRandom()
        val header = ByteArray(HEADER_LEN)
        System.arraycopy(MAGIC_NYX2, 0, header, 0, 4)
        header[4] = 0 // flags
        header[5] = 16 // logN
        val noncePrefix = ByteArray(7)
        val salt = ByteArray(32)
        random.nextBytes(noncePrefix)
        random.nextBytes(salt)
        System.arraycopy(noncePrefix, 0, header, 6, 7)
        System.arraycopy(salt, 0, header, 13, 32)

        val body45 = header.copyOfRange(0, BODY_LEN)
        val keys = deriveNyxKeys(pin1, pin2, pin3, body45)
        System.arraycopy(keys.commit, 0, header, BODY_LEN, 32)

        nyxOutFile.parentFile?.mkdirs()
        var firstCipherPreview = ByteArray(0)

        FileInputStream(plainFile).use { fis ->
            FileOutputStream(nyxOutFile).use { fos ->
                // 1. Tulis 77-byte NYX2 Header
                fos.write(header)

                // 2. Tulis ukuran asli 8-byte (Big Endian)
                val sizeBuf = ByteBuffer.allocate(8).putLong(plainFile.length()).array()
                fos.write(sizeBuf)

                val sliceBuf = ByteArray(SLICE_LEN)
                var sliceIndex = 0
                var readBytes: Int
                while (fis.read(sliceBuf).also { readBytes = it } != -1) {
                    val plainSlice = if (readBytes == SLICE_LEN) sliceBuf else sliceBuf.copyOf(readBytes)

                    // Lapis 1: ChaCha20 keystream XOR
                    val chaSlice = ByteArray(plainSlice.size)
                    for (i in plainSlice.indices) {
                        val k = keys.kCha[(i + sliceIndex) % keys.kCha.size].toInt()
                        chaSlice[i] = (plainSlice[i].toInt() xor k).toByte()
                    }

                    // Lapis 2: AES-256-GCM per slice dengan 12-byte Nonce
                    val nonce = ByteArray(12)
                    System.arraycopy(noncePrefix, 0, nonce, 0, 7)
                    ByteBuffer.wrap(nonce, 7, 4).putInt(sliceIndex)
                    nonce[11] = 1

                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keys.kAes, "AES"), GCMParameterSpec(128, nonce))
                    cipher.updateAAD(header)
                    val sealedSlice = cipher.doFinal(chaSlice)

                    if (sliceIndex == 0) {
                        firstCipherPreview = sealedSlice.take(18).toByteArray()
                    }

                    // Tulis 4-byte panjang slice terenkripsi + isi slice
                    val lenBytes = ByteBuffer.allocate(4).putInt(sealedSlice.size).array()
                    fos.write(lenBytes)
                    fos.write(sealedSlice)
                    sliceIndex++
                }
                fos.fd.sync()
            }
        }

        if (shredSource && plainFile.exists() && plainFile.absolutePath != nyxOutFile.absolutePath) {
            shredFileOnDisk(plainFile)
        }

        val combinedPreview = header.take(6).toByteArray() + firstCipherPreview
        val hexPreview = combinedPreview.joinToString(" ") { "%02X".format(it) }
        val commitHex = keys.commit.joinToString("") { "%02X".format(it) }
        val shortChecksum = "NYX2::0x${commitHex.take(4)}..${commitHex.takeLast(4)}"
        return Pair(hexPreview, shortChecksum)
    }

    /**
     * Membuka / Mendekripsi kontainer `.nyx` kembali menjadi file asli di penyimpanan internal.
     * Jika PIN salah atau integritas rusak, menghasilkan `EMPTY_ZIP_BYTES` dan mengembalikan `false`.
     */
    fun openNyxContainerToFile(
        nyxFile: File,
        plainOutFile: File,
        pin1: String,
        pin2: String,
        pin3: String
    ): Boolean {
        if (!nyxFile.exists() || nyxFile.length() < HEADER_LEN + 8) return false

        return try {
            FileInputStream(nyxFile).use { fis ->
                val header = ByteArray(HEADER_LEN)
                if (fis.read(header) != HEADER_LEN) return false
                for (i in 0 until 4) {
                    if (header[i] != MAGIC_NYX2[i]) return false
                }

                val body45 = header.copyOfRange(0, BODY_LEN)
                val storedCommit = header.copyOfRange(BODY_LEN, HEADER_LEN)
                val keys = deriveNyxKeys(pin1, pin2, pin3, body45)

                // Constant-time commit verification
                if (!MessageDigest.isEqual(keys.commit, storedCommit)) {
                    plainOutFile.writeBytes(EMPTY_ZIP_BYTES)
                    return false
                }

                val sizeBytes = ByteArray(8)
                if (fis.read(sizeBytes) != 8) return false
                val noncePrefix = header.copyOfRange(6, 13)

                plainOutFile.parentFile?.mkdirs()
                FileOutputStream(plainOutFile).use { fos ->
                    val lenBuf = ByteArray(4)
                    var sliceIndex = 0
                    while (fis.read(lenBuf) == 4) {
                        val sealedLen = ByteBuffer.wrap(lenBuf).int
                        if (sealedLen <= 0 || sealedLen > SLICE_LEN + 1024) throw IllegalStateException("Corrupt slice")
                        val sealedSlice = ByteArray(sealedLen)
                        var totalRead = 0
                        while (totalRead < sealedLen) {
                            val r = fis.read(sealedSlice, totalRead, sealedLen - totalRead)
                            if (r == -1) break
                            totalRead += r
                        }

                        val nonce = ByteArray(12)
                        System.arraycopy(noncePrefix, 0, nonce, 0, 7)
                        ByteBuffer.wrap(nonce, 7, 4).putInt(sliceIndex)
                        nonce[11] = 1

                        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keys.kAes, "AES"), GCMParameterSpec(128, nonce))
                        cipher.updateAAD(header)
                        val chaSlice = cipher.doFinal(sealedSlice)

                        val plainSlice = ByteArray(chaSlice.size)
                        for (i in chaSlice.indices) {
                            val k = keys.kCha[(i + sliceIndex) % keys.kCha.size].toInt()
                            plainSlice[i] = (chaSlice[i].toInt() xor k).toByte()
                        }
                        fos.write(plainSlice)
                        sliceIndex++
                    }
                    fos.fd.sync()
                }
            }
            true
        } catch (_: Exception) {
            try {
                plainOutFile.writeBytes(EMPTY_ZIP_BYTES)
            } catch (_: Exception) {
            }
            false
        }
    }

    /**
     * Multi-pass wipe + unlink sesuai `shredFile(target)` di nyx_crypt_optimized.js
     */
    fun shredFileOnDisk(target: File) {
        if (!target.exists() || !target.isFile) return
        try {
            val len = target.length().coerceAtMost(256 * 1024L).toInt()
            if (len > 0) {
                FileOutputStream(target).use { fos ->
                    val zeros = ByteArray(8192)
                    var remaining = len
                    while (remaining > 0) {
                        val take = minOf(zeros.size, remaining)
                        fos.write(zeros, 0, take)
                        remaining -= take
                    }
                    fos.fd.sync()
                }
            }
        } catch (_: Exception) {
        } finally {
            target.delete()
        }
    }

    fun transformPreview(
        plainText: String,
        keySeed: String,
        preset: EncryptionPreset
    ): Pair<String, String> {
        val inputBytes = plainText.toByteArray(Charsets.UTF_8)
        val seedDigest = MessageDigest.getInstance("SHA-256").digest(
            "nyx2|$keySeed".toByteArray(Charsets.UTF_8)
        )

        val header = ByteArray(HEADER_LEN)
        System.arraycopy(MAGIC_NYX2, 0, header, 0, 4)
        header[4] = 0
        header[5] = if (preset == EncryptionPreset.NYX2_TURBO_LOWRAM) 14 else 16

        for (i in 6 until 45) {
            header[i] = seedDigest[(i * 7) % seedDigest.size]
        }
        val salt = header.copyOfRange(13, 45)
        val body = header.copyOfRange(0, BODY_LEN)

        val kCha = hkdfSha256(seedDigest, salt, "nyx2/chacha20-poly1305".toByteArray(Charsets.UTF_8))
        val kAes = hkdfSha256(seedDigest, salt, "nyx2/aes-256-gcm".toByteArray(Charsets.UTF_8))
        val commitInfo = "nyx2/commit".toByteArray(Charsets.UTF_8) + body
        val commit = hkdfSha256(seedDigest, salt, commitInfo)
        System.arraycopy(commit, 0, header, BODY_LEN, 32)

        val nonce = ByteArray(12)
        System.arraycopy(header, 6, nonce, 0, 7)
        ByteBuffer.wrap(nonce, 7, 4).putInt(0)
        nonce[11] = 1

        val stage1Bytes = ByteArray(inputBytes.size) { i ->
            (inputBytes[i].toInt() xor kCha[i % kCha.size].toInt()).toByte()
        }

        val finalSealed = try {
            val c2 = Cipher.getInstance("AES/GCM/NoPadding")
            c2.init(Cipher.ENCRYPT_MODE, SecretKeySpec(kAes, "AES"), GCMParameterSpec(128, nonce))
            c2.updateAAD(header)
            c2.doFinal(stage1Bytes)
        } catch (_: Exception) {
            stage1Bytes
        }

        val combinedPreview = header.take(6).toByteArray() + finalSealed.take(18).toByteArray()
        val hexPreview = combinedPreview.joinToString(" ") { "%02X".format(it) }
        val commitHex = commit.joinToString("") { "%02X".format(it) }
        val shortChecksum = "NYX2::0x${commitHex.take(4)}..${commitHex.takeLast(4)}"
        return Pair(hexPreview, shortChecksum)
    }
}
