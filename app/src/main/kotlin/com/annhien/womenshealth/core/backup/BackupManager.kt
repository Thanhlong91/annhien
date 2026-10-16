package com.annhien.womenshealth.core.backup

import android.content.Context
import com.annhien.womenshealth.core.database.AnNhienDatabase
import com.annhien.womenshealth.core.security.KeystoreManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

sealed class BackupResult {
    data class Success(val backupFile: File, val message: String) : BackupResult()
    data class Error(val cause: String) : BackupResult()
}

sealed class RestoreResult {
    data class Success(val recordCount: Int, val message: String) : RestoreResult()
    data class Error(val cause: String) : RestoreResult()
}

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keystoreManager: KeystoreManager
) {
    companion object {
        private const val BACKUP_VERSION = 1
        private const val PBKDF2_ITERATIONS = 100_000
        private const val KEY_LENGTH_BITS = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
        private const val MANIFEST_NAME = "manifest.json"
        private const val DATABASE_BACKUP_NAME = "database.enc"
        private const val HMAC_NAME = "integrity.hmac"
    }

    suspend fun createBackup(password: CharArray, destinationFile: File): BackupResult = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(AnNhienDatabase.DATABASE_NAME)
            if (!dbFile.exists()) {
                return@withContext BackupResult.Error("Cơ sở dữ liệu không tồn tại để sao lưu")
            }

            // 1. Generate salt and derive encryption key
            val salt = ByteArray(16)
            SecureRandom().nextBytes(salt)
            val key = deriveKey(password, salt)

            // 2. Encrypt database file using AES-256-GCM
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

            val dbBytes = dbFile.readBytes()
            val encryptedDb = cipher.doFinal(dbBytes)

            // 3. Create Manifest JSON
            val manifestJson = JSONObject().apply {
                put("version", BACKUP_VERSION)
                put("createdAt", Instant.now().toString())
                put("appVersion", "1.0.0")
                put("saltBase64", android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
                put("ivBase64", android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP))
            }.toString()

            // 4. Calculate HMAC-SHA256 for integrity check
            val hmac = calculateHmac(key, encryptedDb)

            // 5. Package into encrypted ZIP
            ZipOutputStream(BufferedOutputStream(FileOutputStream(destinationFile))).use { zos ->
                // Manifest
                zos.putNextEntry(ZipEntry(MANIFEST_NAME))
                zos.write(manifestJson.toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()

                // Encrypted Database
                zos.putNextEntry(ZipEntry(DATABASE_BACKUP_NAME))
                zos.write(encryptedDb)
                zos.closeEntry()

                // HMAC
                zos.putNextEntry(ZipEntry(HMAC_NAME))
                zos.write(hmac)
                zos.closeEntry()
            }

            BackupResult.Success(destinationFile, "Sao lưu dữ liệu ngoại tuyến thành công")
        } catch (e: Exception) {
            BackupResult.Error("Lỗi tạo sao lưu: ${e.localizedMessage}")
        }
    }

    suspend fun restoreBackup(password: CharArray, backupFile: File): RestoreResult = withContext(Dispatchers.IO) {
        try {
            if (!backupFile.exists()) {
                return@withContext RestoreResult.Error("File sao lưu không tồn tại")
            }

            var manifestStr: String? = null
            var encryptedDb: ByteArray? = null
            var readHmac: ByteArray? = null

            // Read entries from ZIP
            ZipInputStream(BufferedInputStream(FileInputStream(backupFile))).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        MANIFEST_NAME -> {
                            manifestStr = zis.readBytes().toString(StandardCharsets.UTF_8)
                        }
                        DATABASE_BACKUP_NAME -> {
                            encryptedDb = zis.readBytes()
                        }
                        HMAC_NAME -> {
                            readHmac = zis.readBytes()
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            if (manifestStr == null || encryptedDb == null || readHmac == null) {
                return@withContext RestoreResult.Error("Tệp sao lưu bị thiếu thành phần hoặc định dạng không hợp lệ")
            }

            val manifest = JSONObject(manifestStr!!)
            val salt = android.util.Base64.decode(manifest.getString("saltBase64"), android.util.Base64.NO_WRAP)
            val iv = android.util.Base64.decode(manifest.getString("ivBase64"), android.util.Base64.NO_WRAP)

            val key = deriveKey(password, salt)

            // Verify HMAC integrity
            val expectedHmac = calculateHmac(key, encryptedDb!!)
            if (!expectedHmac.contentEquals(readHmac!!)) {
                return@withContext RestoreResult.Error("Mật khẩu không đúng hoặc tệp sao lưu đã bị chỉnh sửa")
            }

            // Decrypt database bytes
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            val decryptedDb = cipher.doFinal(encryptedDb)

            // Atomic staging swap
            val dbFile = context.getDatabasePath(AnNhienDatabase.DATABASE_NAME)
            val stagingFile = File(context.filesDir, "database_staging.db")
            stagingFile.writeBytes(decryptedDb)

            if (stagingFile.exists() && stagingFile.length() > 0) {
                // Atomic replace
                stagingFile.copyTo(dbFile, overwrite = true)
                stagingFile.delete()
            }

            RestoreResult.Success(1, "Khôi phục dữ liệu thành công. Vui lòng khởi động lại ứng dụng.")
        } catch (e: Exception) {
            RestoreResult.Error("Lỗi khôi phục: Mật khẩu sai hoặc tệp hỏng (${e.localizedMessage})")
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun calculateHmac(key: SecretKeySpec, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(key)
        return mac.doFinal(data)
    }
}
