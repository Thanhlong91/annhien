package com.annhien.womenshealth.core.security

import org.junit.Assert.*
import org.junit.Test

class PinManagerTest {

    @Test
    fun testPinHashing_correctPinVerifiesSuccessfully() {
        val pin = "1234"
        val salt = PinManager.generateSalt()

        val hashed = PinManager.hashPin(pin, salt)

        assertTrue(PinManager.verifyPin(pin, salt, hashed))
    }

    @Test
    fun testPinHashing_wrongPinFailsVerification() {
        val pin = "1234"
        val wrongPin = "4321"
        val salt = PinManager.generateSalt()

        val hashed = PinManager.hashPin(pin, salt)

        assertFalse(PinManager.verifyPin(wrongPin, salt, hashed))
    }

    @Test
    fun testDifferentSalts_produceDifferentHashesForSamePin() {
        val pin = "5678"
        val salt1 = PinManager.generateSalt()
        val salt2 = PinManager.generateSalt()

        val hash1 = PinManager.hashPin(pin, salt1)
        val hash2 = PinManager.hashPin(pin, salt2)

        assertNotEquals(hash1, hash2)
    }

    @Test
    fun testConstantTimeComparison_matchesExactLengthAndContent() {
        val a = "sensitive_token_123"
        val b = "sensitive_token_123"
        val c = "sensitive_token_456"

        assertTrue(PinManager.constantTimeEquals(a, b))
        assertFalse(PinManager.constantTimeEquals(a, c))
    }
}
