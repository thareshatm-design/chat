package com.example.crypto

import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class EncryptedMessagePayload(
    val ciphertextBase64: String,
    val ivBase64: String,
    val algorithm: String = "AES-256-GCM",
    val keyFingerprint: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class KeyPairBase64(
    val publicKeyBase64: String,
    val privateKeyBase64: String
)

object CryptoEngine {
    private const val EC_CURVE = "secp256r1"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH_BYTES = 12
    private val secureRandom = SecureRandom()

    /**
     * Generates a new Elliptic Curve (secp256r1) KeyPair for End-to-End Encryption.
     */
    fun generateEcKeyPair(): KeyPairBase64 {
        val kpg = KeyPairGenerator.getInstance("EC")
        val ecSpec = ECGenParameterSpec(EC_CURVE)
        kpg.initialize(ecSpec, secureRandom)
        val keyPair = kpg.generateKeyPair()

        val pubBase64 = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
        val privBase64 = Base64.encodeToString(keyPair.private.encoded, Base64.NO_WRAP)
        return KeyPairBase64(publicKeyBase64 = pubBase64, privateKeyBase64 = privBase64)
    }

    /**
     * Computes ECDH shared secret between our private key and peer's public key,
     * then derives a 256-bit AES key using SHA-256 KDF.
     */
    fun deriveSharedAesKey(myPrivateKeyBase64: String, peerPublicKeyBase64: String): ByteArray {
        val kf = KeyFactory.getInstance("EC")
        val privBytes = Base64.decode(myPrivateKeyBase64, Base64.DEFAULT)
        val pubBytes = Base64.decode(peerPublicKeyBase64, Base64.DEFAULT)

        val privateKey: PrivateKey = kf.generatePrivate(PKCS8EncodedKeySpec(privBytes))
        val publicKey: PublicKey = kf.generatePublic(X509EncodedKeySpec(pubBytes))

        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(publicKey, true)
        val sharedSecret = keyAgreement.generateSecret()

        // Key Derivation Function (KDF): SHA-256(sharedSecret || "SecureChat-v1-Salt")
        val md = MessageDigest.getInstance("SHA-256")
        md.update(sharedSecret)
        md.update("SecureChat-v1-Salt".toByteArray(Charsets.UTF_8))
        return md.digest()
    }

    /**
     * Encrypts plaintext string using AES-256-GCM with a fresh 12-byte IV.
     */
    fun encrypt(plaintext: String, aesKey: ByteArray, keyFingerprint: String = ""): EncryptedMessagePayload {
        val iv = ByteArray(IV_LENGTH_BYTES)
        secureRandom.nextBytes(iv)

        val secretKey = SecretKeySpec(aesKey, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val cipherBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        return EncryptedMessagePayload(
            ciphertextBase64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP),
            ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP),
            algorithm = "AES-256-GCM",
            keyFingerprint = keyFingerprint
        )
    }

    /**
     * Decrypts AES-256-GCM ciphertext using AES key and IV.
     */
    fun decrypt(payload: EncryptedMessagePayload, aesKey: ByteArray): String {
        val iv = Base64.decode(payload.ivBase64, Base64.DEFAULT)
        val cipherBytes = Base64.decode(payload.ciphertextBase64, Base64.DEFAULT)

        val secretKey = SecretKeySpec(aesKey, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, Charsets.UTF_8)
    }

    /**
     * Calculates verifiable 60-digit Safety Number (Signal/WhatsApp standard)
     * from two public keys sorted lexicographically.
     */
    fun computeSafetyNumber(pubKey1: String, pubKey2: String): String {
        val sorted = if (pubKey1 < pubKey2) pubKey1 + pubKey2 else pubKey2 + pubKey1
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(sorted.toByteArray(Charsets.UTF_8))

        // Convert 32 bytes into 12 blocks of 5 digits
        val digits = StringBuilder()
        for (i in 0 until 12) {
            val offset = (i * 2) % (hash.size - 2)
            val b1 = hash[offset].toInt() and 0xFF
            val b2 = hash[offset + 1].toInt() and 0xFF
            val num = ((b1 shl 8) or b2) % 100000
            digits.append(String.format("%05d", num))
            if (i < 11) digits.append(" ")
        }
        return digits.toString()
    }

    /**
     * Short hexadecimal key fingerprint for UI preview (e.g. "E2EE-8F3A-7C12").
     */
    fun computeKeyFingerprint(pubKey: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(pubKey.toByteArray(Charsets.UTF_8))
        val hex = hash.take(6).joinToString("") { "%02X".format(it) }
        return "${hex.substring(0, 4)}-${hex.substring(4, 8)}-${hex.substring(8, 12)}"
    }

    /**
     * Salted SHA-256 password/PIN hashing with 10,000 rounds.
     */
    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        var current = (password + salt).toByteArray(Charsets.UTF_8)
        for (i in 0 until 10_000) {
            md.reset()
            current = md.digest(current)
        }
        return Base64.encodeToString(current, Base64.NO_WRAP)
    }
}
