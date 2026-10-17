package com.annhien.womenshealth.core.backup

import android.content.Context
import com.annhien.womenshealth.core.database.AnNhienDatabase
import com.annhien.womenshealth.core.security.KeystoreManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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
    data class Success(val restoredFileCount: Int, val message: String) : RestoreResult()
    data class Error(val cause: String) : RestoreResult()
}

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keystoreManager: KeystoreManager
) {
    companion object {
        private const val BACKUP_VERSION = 2
        private const val PBKDF2_ITERATIONS = 100_000
        private const val KEY_LENGTH_BITS = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
        private const val MANIFEST_NAME = "manifest.json"
        private const val DATABASE_BACKUP_NAME = "database.enc"
        private const val DATABASE_PASSPHRASE_NAME = "database-passphrase.enc"
        private const val HMAC_NAME = "integrity.hmac"
        private const val ATTACHMENTS_PREFIX = "attachments/"
        private const val MAX_BACKUP_BYTES = 512L * 1024L * 1024L
    }

    suspend fun createBackup(password: CharArray, destinationFile: File): BackupResult = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(AnNhienDatabase.DATABASE_NAME)
            if (!dbFile.exists()) {
                return@withContext BackupResult.Error("Cơ sở dữ liệu không tồn tại để sao lưu")
            }

            val salt = ByteArray(16)
            SecureRandom().nextBytes(salt)
            val key = deriveKey(password, salt)

            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))

            val dbBytes = dbFile.readBytes()
            val encryptedDb = cipher.doFinal(dbBytes)

            val dbPassphrase = keystoreManager.getDatabasePassphrase()
            val passphraseIv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(passphraseIv)
            val passphraseCipher = Cipher.getInstance("AES/GCM/NoPadding")
            passphraseCipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, passphraseIv))
            val encryptedPassphrase = passphraseCipher.doFinal(dbPassphrase)

            val attachmentDir = File(context.filesDir, "attachments")
            val attachments = attachmentDir.listFilesRecursively()
            val attachmentEntries = attachments.map { it.relativeTo(context.filesDir).invariantSeparatorsPath }

            val manifestJson = JSONObject().apply {
                put("version", BACKUP_VERSION)
                put("createdAt", Instant.now().toString())
                put("appVersion", "1.0.0")
                put("saltBase64", android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
                put("ivBase64", android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP))
                put("passphraseIvBase64", android.util.Base64.encodeToString(passphraseIv, android.util.Base64.NO_WRAP))
                put("attachments", JSONArray(attachmentEntries))
            }.toString()

            val hmacInput = encryptedDb + encryptedPassphrase
            val hmac = calculateHmac(key, hmacInput)

            ZipOutputStream(BufferedOutputStream(FileOutputStream(destinationFile))).use { zos ->
                zos.putNextEntry(ZipEntry(MANIFEST_NAME))
                zos.write(manifestJson.toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()

                zos.putNextEntry(ZipEntry(DATABASE_BACKUP_NAME))
                zos.write(encryptedDb)
                zos.closeEntry()

                zos.putNextEntry(ZipEntry(DATABASE_PASSPHRASE_NAME))
                zos.write(encryptedPassphrase)
                zos.closeEntry()

                zos.putNextEntry(ZipEntry(HMAC_NAME))
                zos.write(hmac)
                zos.closeEntry()

                attachments.forEach { file ->
                    val relativePath = file.relativeTo(context.filesDir).invariantSeparatorsPath
                    zos.putNextEntry(ZipEntry(relativePath))
                    file.inputStream().use { input ->
                        input.copyTo(zos, 64 * 1024)
                    }
                    zos.closeEntry()
                }
            }

            BackupResult.Success(destinationFile, "Sao lưu dữ liệu và tài liệu thành công")
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
            var encryptedPassphrase: ByteArray? = null
            var readHmac: ByteArray? = null
            val attachmentEntries = mutableMapOf<String, ByteArray>()

            ZipInputStream(BufferedInputStream(FileInputStream(backupFile))).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val entryBytes = readEntrySafely(zis)
                    when {
                        entry.name == MANIFEST_NAME -> manifestStr = entryBytes.toString(StandardCharsets.UTF_8)
                        entry.name == DATABASE_BACKUP_NAME -> encryptedDb = entryBytes
                        entry.name == DATABASE_PASSPHRASE_NAME -> encryptedPassphrase = entryBytes
                        entry.name == HMAC_NAME -> readHmac = entryBytes
                        entry.name.startsWith(ATTACHMENTS_PREFIX) -> attachmentEntries[entry.name] = entryBytes
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            if (manifestStr == null || encryptedDb == null || encryptedPassphrase == null || readHmac == null) {
                return@withContext RestoreResult.Error("Tệp sao lưu bị thiếu thành phần hoặc định dạng không hợp lệ")
            }

            val manifest = JSONObject(manifestStr!!)
            val version = manifest.optInt("version", 0)
            if (version != BACKUP_VERSION) {
                return@withContext RestoreResult.Error("Phiên bản tệp sao lưu không được hỗ trợ")
            }

            val expectedAttachments = manifest.optJSONArray("attachments")
                ?.let { array -> (0 until array.length()).map { array.getString(it) } }
                .orEmpty()

            if (attachmentEntries.keys.toSet() != expectedAttachments.toSet()) {
                return@withContext RestoreResult.Error("Danh sách tài liệu trong tệp sao lưu không khớp")
            }

            val salt = android.util.Base64.decode(manifest.getString("saltBase64"), android.util.Base64.NO_WRAP)
            val iv = android.util.Base64.decode(manifest.getString("ivBase64"), android.util.Base64.NO_WRAP)
            val passphraseIv = android.util.Base64.decode(
                manifest.getString("passphraseIvBase64"),
                android.util.Base64.NO_WRAP
            )
            val key = deriveKey(password, salt)

            val expectedHmac = calculateHmac(key, encryptedDb!! + encryptedPassphrase!!)
            if (!expectedHmac.contentEquals(readHmac!!)) {
                return@withContext RestoreResult.Error("Mật khẩu không đúng hoặc tệp sao lưu đã bị chỉnh sửa")
            }

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
            val decryptedDb = cipher.doFinal(encryptedDb)

            val passphraseCipher = Cipher.getInstance("AES/GCM/NoPadding")
            passphraseCipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, passphraseIv))
            val restoredPassphrase = passphraseCipher.doFinal(encryptedPassphrase)

            val dbFile = context.getDatabasePath(AnNhienDatabase.DATABASE_NAME)
            val stagingFile = File(context.filesDir, "database_staging.db")
            stagingFile.writeBytes(decryptedDb)

            if (!stagingFile.exists() || stagingFile.length() == 0L) {
                stagingFile.delete()
                return@withContext RestoreResult.Error("Không thể chuẩn bị dữ liệu khôi phục")
            }

            val previousFile = File(context.filesDir, "database_previous.db")
            val attachmentsDir = File(context.filesDir, "attachments")
            val attachmentsBackupDir = File(context.filesDir, "attachments_previous")

            if (dbFile.exists()) {
                dbFile.copyTo(previousFile, overwrite = true)
            }
            if (attachmentsDir.exists()) {
                attachmentsDir.copyRecursively(attachmentsBackupDir, overwrite = true)
            }

            try {
                stagingFile.copyTo(dbFile, overwrite = true)
                keystoreManager.importDatabasePassphrase(restoredPassphrase)

                attachmentsDir.deleteRecursively()
                attachmentsDir.mkdirs()
                attachmentEntries.forEach { (zipPath, bytes) ->
                    val safeRelative = zipPath.removePrefix(ATTACHMENTS_PREFIX)
                    require(!safeRelative.contains("..") && !safeRelative.startsWith("/")) {
                        "Đường dẫn tài liệu không hợp lệ"
                    }
                    val target = File(context.filesDir, safeRelative)
                    target.parentFile?.mkdirs()
                    target.writeBytes(bytes)
                }

                stagingFile.delete()
                previousFile.delete()
                attachmentsBackupDir.deleteRecursively()
            } catch (e: Exception) {
                if (previousFile.exists()) {
                    previousFile.copyTo(dbFile, overwrite = true)
                    previousFile.delete()
                }
                if (attachmentsBackupDir.exists()) {
                    attachmentsDir.deleteRecursively()
                    attachmentsBackupDir.renameTo(attachmentsDir)
                }
                stagingFile.delete()
                throw e
            }

            RestoreResult.Success(
                restoredFileCount = attachmentEntries.size,
                message = "Khôi phục dữ liệu và ${attachmentEntries.size} tài liệu thành công. Vui lòng khởi động lại ứng dụng."
            )
        } catch (e: Exception) {
            RestoreResult.Error("Lỗi khôi phục: Mật khẩu sai hoặc tệp hỏng (${e.localizedMessage})")
        }
    }

    private fun readEntrySafely(zis: ZipInputStream): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(64 * 1024)
        var total = 0L

        while (true) {
            val read = zis.read(buffer)
            if (read == -1) break
            total += read
            if (total > MAX_BACKUP_BYTES) {
                throw SecurityException("Thành phần trong tệp sao lưu vượt quá giới hạn cho phép")
            }
            output.write(buffer, 0, read)
        }

        return output.toByteArray()
    }

    private fun File.listFilesRecursively(): List<File> {
        return if (!exists()) {
            emptyList()
        } else {
            walkTopDown().filter { it.isFile }.toList()
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
