package com.annhien.womenshealth.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreManager(private val context: Context) {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS_DB = "annhien_db_key_v1"
        private const val KEY_ALIAS_BACKUP = "annhien_backup_key_v1"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
        private const val ENCRYPTED_PASSPHRASE_PREFS = "annhien_sec"
        private const val PREF_KEY_ENCRYPTED_PASSPHRASE = "ep_v1"
        private const val PREF_KEY_IV = "ep_iv_v1"
        private const val PASSPHRASE_BYTE_LENGTH = 32
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEYSTORE_PROVIDER).also { it.load(null) }
    }

    private val prefs by lazy {
        context.getSharedPreferences(ENCRYPTED_PASSPHRASE_PREFS, Context.MODE_PRIVATE)
    }

    fun getDatabasePassphrase(): ByteArray {
        val encrypted = prefs.getString(PREF_KEY_ENCRYPTED_PASSPHRASE, null)
        val iv = prefs.getString(PREF_KEY_IV, null)

        if (encrypted == null || iv == null) {
            return generateAndStorePassphrase()
        }

        val key = getKey(KEY_ALIAS_DB)
            ?: throw SecurityException("Không tìm thấy khóa bảo vệ cơ sở dữ liệu. Không thể tự tạo khóa mới vì có thể làm mất dữ liệu hiện có.")

        return try {
            decryptPassphrase(
                encryptedBytes = android.util.Base64.decode(encrypted, android.util.Base64.DEFAULT),
                ivBytes = android.util.Base64.decode(iv, android.util.Base64.DEFAULT)
            )
        } catch (e: Exception) {
            throw SecurityException("Không thể giải mã khóa cơ sở dữ liệu. Không tự xóa hoặc tạo khóa thay thế.", e)
        }
    }

    fun importDatabasePassphrase(passphrase: ByteArray) {
        require(passphrase.size == PASSPHRASE_BYTE_LENGTH) {
            "Độ dài passphrase không hợp lệ"
        }
        ensureKeyExists(KEY_ALIAS_DB)
        val (encrypted, iv) = encryptBytes(passphrase, KEY_ALIAS_DB)
        prefs.edit()
            .putString(PREF_KEY_ENCRYPTED_PASSPHRASE, android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT))
            .putString(PREF_KEY_IV, android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT))
            .apply()
    }

    private fun generateAndStorePassphrase(): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_BYTE_LENGTH)
        java.security.SecureRandom().nextBytes(passphrase)
        ensureKeyExists(KEY_ALIAS_DB)
        val (encrypted, iv) = encryptBytes(passphrase, KEY_ALIAS_DB)
        prefs.edit()
            .putString(PREF_KEY_ENCRYPTED_PASSPHRASE, android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT))
            .putString(PREF_KEY_IV, android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT))
            .apply()
        return passphrase
    }

    private fun decryptPassphrase(encryptedBytes: ByteArray, ivBytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getKey(KEY_ALIAS_DB), GCMParameterSpec(GCM_TAG_LENGTH, ivBytes))
        return cipher.doFinal(encryptedBytes)
    }

    private fun encryptBytes(plaintext: ByteArray, alias: String): Pair<ByteArray, ByteArray> {
        val key = getKey(alias) ?: generateKey(alias)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plaintext)
        return Pair(encrypted, iv)
    }

    private fun ensureKeyExists(alias: String) {
        if (!keyStore.containsAlias(alias)) {
            generateKey(alias)
        }
    }

    private fun generateKey(alias: String): SecretKey {
        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        keyGen.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return keyGen.generateKey()
    }

    private fun getKey(alias: String): SecretKey? {
        return if (keyStore.containsAlias(alias)) {
            (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.secretKey
        } else null
    }

    fun encryptFileData(plaintext: ByteArray): EncryptedData {
        ensureKeyExists(KEY_ALIAS_DB)
        val (ciphertext, iv) = encryptBytes(plaintext, KEY_ALIAS_DB)
        return EncryptedData(ciphertext = ciphertext, iv = iv)
    }

    fun decryptFileData(encryptedData: EncryptedData): ByteArray {
        val key = getKey(KEY_ALIAS_DB)
            ?: throw SecurityException("Không tìm thấy khóa bảo vệ tệp đính kèm")
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, encryptedData.iv))
        return cipher.doFinal(encryptedData.ciphertext)
    }

    fun isKeyAccessible(): Boolean {
        return try {
            getKey(KEY_ALIAS_DB) != null
        } catch (e: Exception) {
            false
        }
    }
}

data class EncryptedData(
    val ciphertext: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedData) return false
        return ciphertext.contentEquals(other.ciphertext) && iv.contentEquals(other.iv)
    }

    override fun hashCode(): Int = 31 * ciphertext.contentHashCode() + iv.contentHashCode()
}
