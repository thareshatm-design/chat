package com.example.data.model

import com.example.crypto.EncryptedMessagePayload

enum class UserRole {
    USER, ADMIN
}

enum class UserStatus {
    APPROVED, PENDING, BLOCKED
}

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val avatarEmoji: String,
    val avatarColorHex: Long,
    val role: UserRole = UserRole.USER,
    val status: UserStatus = UserStatus.APPROVED,
    val publicKeyBase64: String,
    val privateKeyBase64: String,
    val isTwoFactorEnabled: Boolean = false,
    val twoFactorSecret: String = "JBSWY3DPEHPK3PXP",
    val pinHash: String = "",
    val isBiometricEnabled: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeenDescription: String = "Online",
    val statusBio: String = "Hey there! I am using SecureChat E2EE."
)

enum class MessageType {
    TEXT, IMAGE, VOICE, DOCUMENT, SECURITY_EVENT
}

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ
}

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val recipientId: String,
    val plaintext: String,
    val encryptedPayload: EncryptedMessagePayload,
    val messageType: MessageType = MessageType.TEXT,
    val mediaCaption: String? = null,
    val mediaAttachmentType: String? = null, // "image", "audio", "pdf"
    val voiceDurationSeconds: Int = 0,
    val voiceAudioSamples: List<Float> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.READ,
    val disappearingDurationSeconds: Long = 0L, // 0 = off, 10, 60, 86400
    val expiresAtTimestamp: Long = 0L,
    val reactionEmoji: String? = null,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val isStarred: Boolean = false
)

data class Chat(
    val id: String,
    val name: String,
    val isGroup: Boolean,
    val participantIds: List<String>,
    val lastMessageText: String,
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val disappearingDurationSeconds: Long = 0L,
    val isSafetyNumberVerified: Boolean = false,
    val avatarEmoji: String,
    val avatarColorHex: Long,
    val isTyping: Boolean = false,
    val typingUserName: String? = null
)

data class SecurityAuditLog(
    val id: String,
    val timestamp: Long,
    val eventType: String,
    val description: String,
    val severity: String, // "INFO", "WARN", "CRITICAL"
    val clientIp: String,
    val userIdentifier: String
)

data class InviteCode(
    val code: String,
    val createdBy: String,
    val isUsed: Boolean = false,
    val usedBy: String? = null,
    val expiresAt: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
)

data class ActiveSession(
    val sessionId: String,
    val deviceName: String,
    val clientType: String, // "Android 15 (Pixel)", "Web Client", "Desktop"
    val ipAddress: String,
    val location: String,
    val lastActive: String,
    val isCurrentDevice: Boolean = false
)
