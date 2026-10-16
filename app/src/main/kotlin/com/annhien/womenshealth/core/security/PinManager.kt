package com.annhien.womenshealth.core.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PIN management for the App Lock feature.
 *
 * Security rules:
 * - PIN is never stored in plaintext.
 * - PBKDF2-HMAC-SHA256 with a random salt is used for hashing.
 * - Minimum 100,000 iterations.
 * - Salt and hash are stored separately (not the PIN itself).
 * - No recovery mechanism — if PIN is lost, a reset wipes protected data.
 *
 * This is for App Lock only (Layer 1 security).
 * The database encryption key (Layer 2) is managed separately by KeystoreManager.
 */
object PinManager {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256 // bits
    private const val SALT_BYTES = 32

    /**
     * Hashes a PIN with a freshly generated salt.
     * @return PinHash containing the Base64-encoded salt and hash
     */
    fun hashPin(pin: String): PinHash {
        require(pin.length >= 4) { "PIN must be at least 4 digits" }
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        val hash = pbkdf2(pin.toCharArray(), salt)
        return PinHash(
            saltBase64 = Base64.encodeToString(salt, Base64.DEFAULT),
            hashBase64 = Base64.encodeToString(hash, Base64.DEFAULT)
        )
    }

    /**
     * Verifies a PIN against a stored PinHash.
     * Performs constant-time comparison to prevent timing attacks.
     */
    fun verifyPin(pin: String, stored: PinHash): Boolean {
        if (pin.length < 4) return false
        return try {
            val salt = Base64.decode(stored.saltBase64, Base64.DEFAULT)
            val expectedHash = Base64.decode(stored.hashBase64, Base64.DEFAULT)
            val actualHash = pbkdf2(pin.toCharArray(), salt)
            constantTimeEquals(expectedHash, actualHash)
        } catch (e: Exception) {
            false
        }
    }

    fun verifyPin(pin: String, saltBase64: String, hashBase64: String): Boolean {
        return verifyPin(pin, PinHash(saltBase64, hashBase64))
    }

    fun verifyPin(pin: String, salt: ByteArray, hashBase64: String): Boolean {
        if (pin.length < 4) return false
        return try {
            val expectedHash = Base64.decode(hashBase64, Base64.DEFAULT)
            val actualHash = pbkdf2(pin.toCharArray(), salt)
            constantTimeEquals(expectedHash, actualHash)
        } catch (e: Exception) {
            false
        }
    }

    private fun pbkdf2(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    /**
     * Constant-time byte array comparison to prevent timing-based PIN extraction.
     */
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}

data class PinHash(
    val saltBase64: String,
    val hashBase64: String
)

/**
 * App lock state — tracks whether the app is currently locked.
 * This is held in memory only (not persisted), so the app always
 * starts in locked state if a lock type is configured.
 */
object AppLockState {
    @Volatile
    private var isUnlocked: Boolean = false

    @Volatile
    private var lastUnlockTime: Long = 0L

    fun unlock() {
        isUnlocked = true
        lastUnlockTime = System.currentTimeMillis()
    }

    fun lock() {
        isUnlocked = false
    }

    fun isUnlocked(): Boolean = isUnlocked

    /**
     * Auto-lock if the configured timeout has passed.
     * @param timeoutMs 0 means never auto-lock (lock only on explicit action)
     */
    fun checkAutoLock(timeoutMs: Long): Boolean {
        if (!isUnlocked) return true
        if (timeoutMs <= 0) return false
        val elapsed = System.currentTimeMillis() - lastUnlockTime
        if (elapsed > timeoutMs) {
            lock()
            return true
        }
        return false
    }

    fun refreshUnlockTime() {
        if (isUnlocked) {
            lastUnlockTime = System.currentTimeMillis()
        }
    }
}
