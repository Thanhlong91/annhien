package com.annhien.womenshealth.core.backup

import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class BackupIntegrityTest {

    @Test
    fun testHmacIntegrity_detectsDataTampering() {
        val secretKey = SecretKeySpec("test_backup_secret_key_12345678".toByteArray(), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)

        val originalData = "DATABASE_ENCRYPTED_DUMP_BYTES_12345".toByteArray()
        val originalHmac = mac.doFinal(originalData)

        // 1. Unmodified data validates
        mac.reset()
        val verifyMac = mac.doFinal(originalData)
        assertTrue(MessageDigest.isEqual(originalHmac, verifyMac))

        // 2. Tampered data fails HMAC check
        val tamperedData = "DATABASE_ENCRYPTED_DUMP_BYTES_12345_TAMPERED".toByteArray()
        mac.reset()
        val tamperedMac = mac.doFinal(tamperedData)
        assertFalse(MessageDigest.isEqual(originalHmac, tamperedMac))
    }

    @Test
    fun testHmacIntegrity_wrongPasswordFailsCheck() {
        val key1 = SecretKeySpec("correct_password_derived_key_123".toByteArray(), "HmacSHA256")
        val key2 = SecretKeySpec("wrong_password_derived_key_98765".toByteArray(), "HmacSHA256")

        val data = "CONFIDENTIAL_HEALTH_RECORDS".toByteArray()

        val mac1 = Mac.getInstance("HmacSHA256").apply { init(key1) }
        val mac2 = Mac.getInstance("HmacSHA256").apply { init(key2) }

        val hmac1 = mac1.doFinal(data)
        val hmac2 = mac2.doFinal(data)

        assertFalse(MessageDigest.isEqual(hmac1, hmac2))
    }
}
