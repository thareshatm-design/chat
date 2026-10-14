package com.example

import com.example.crypto.CryptoEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CryptoEngineTest {

    @Test
    fun testEcKeyGenerationAndEcdhSharedSecret() {
        // Alice generates EC keypair
        val alice = CryptoEngine.generateEcKeyPair()
        // Bob generates EC keypair
        val bob = CryptoEngine.generateEcKeyPair()

        assertTrue(alice.publicKeyBase64.isNotEmpty())
        assertTrue(alice.privateKeyBase64.isNotEmpty())
        assertTrue(bob.publicKeyBase64.isNotEmpty())
        assertTrue(bob.privateKeyBase64.isNotEmpty())

        // Alice derives shared secret using Bob's public key
        val aliceSharedKey = CryptoEngine.deriveSharedAesKey(alice.privateKeyBase64, bob.publicKeyBase64)
        // Bob derives shared secret using Alice's public key
        val bobSharedKey = CryptoEngine.deriveSharedAesKey(bob.privateKeyBase64, alice.publicKeyBase64)

        // The derived AES keys MUST match exactly (Diffie-Hellman property)
        assertEquals(32, aliceSharedKey.size)
        assertEquals(32, bobSharedKey.size)
        assertTrue(aliceSharedKey.contentEquals(bobSharedKey))
    }

    @Test
    fun testAesGcmEncryptionAndDecryption() {
        val alice = CryptoEngine.generateEcKeyPair()
        val bob = CryptoEngine.generateEcKeyPair()

        val aliceKey = CryptoEngine.deriveSharedAesKey(alice.privateKeyBase64, bob.publicKeyBase64)
        val bobKey = CryptoEngine.deriveSharedAesKey(bob.privateKeyBase64, alice.publicKeyBase64)

        val secretMessage = "Top Secret Classified Payload: Alpha-1092"

        // Alice encrypts
        val payload = CryptoEngine.encrypt(secretMessage, aliceKey, "PEER-FINGERPRINT")
        assertNotEquals(secretMessage, payload.ciphertextBase64)
        assertEquals("AES-256-GCM", payload.algorithm)

        // Bob decrypts
        val decrypted = CryptoEngine.decrypt(payload, bobKey)
        assertEquals(secretMessage, decrypted)
    }

    @Test
    fun testSafetyNumberDeterminism() {
        val alice = CryptoEngine.generateEcKeyPair()
        val bob = CryptoEngine.generateEcKeyPair()

        // Order should not matter: computeSafetyNumber(A, B) == computeSafetyNumber(B, A)
        val code1 = CryptoEngine.computeSafetyNumber(alice.publicKeyBase64, bob.publicKeyBase64)
        val code2 = CryptoEngine.computeSafetyNumber(bob.publicKeyBase64, alice.publicKeyBase64)

        assertEquals(code1, code2)
        // 12 groups of 5 digits = 60 digits + 11 spaces = 71 characters
        assertEquals(71, code1.length)
    }
}
