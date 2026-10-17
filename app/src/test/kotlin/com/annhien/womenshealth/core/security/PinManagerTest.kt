package com.annhien.womenshealth.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinManagerTest {

    @Test
    fun testPinHashing_correctPinVerifiesSuccessfully() {
        val pin = "1234"
        val hashResult = PinManager.hashPin(pin)

        assertTrue(PinManager.verifyPin(pin, hashResult))
        assertTrue(PinManager.verifyPin(pin, hashResult.saltBase64, hashResult.hashBase64))
    }

    @Test
    fun testPinHashing_wrongPinFailsVerification() {
        val pin = "1234"
        val wrongPin = "4321"
        val hashResult = PinManager.hashPin(pin)

        assertFalse(PinManager.verifyPin(wrongPin, hashResult))
        assertFalse(PinManager.verifyPin(wrongPin, hashResult.saltBase64, hashResult.hashBase64))
    }

    @Test
    fun testDifferentSalts_produceDifferentHashesForSamePin() {
        val pin = "5678"
        val hash1 = PinManager.hashPin(pin)
        val hash2 = PinManager.hashPin(pin)

        assertNotEquals(hash1.saltBase64, hash2.saltBase64)
        assertNotEquals(hash1.hashBase64, hash2.hashBase64)
        assertTrue(PinManager.verifyPin(pin, hash1))
        assertTrue(PinManager.verifyPin(pin, hash2))
    }

    @Test
    fun testConstantTimeComparison_matchesExactLengthAndContent() {
        val a = "sensitive_token_123".toByteArray(Charsets.UTF_8)
        val b = "sensitive_token_123".toByteArray(Charsets.UTF_8)
        val c = "sensitive_token_456".toByteArray(Charsets.UTF_8)

        assertTrue(PinManager.constantTimeEquals(a, b))
        assertFalse(PinManager.constantTimeEquals(a, c))
    }
}
