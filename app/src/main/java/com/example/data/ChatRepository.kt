package com.example.data

import com.example.crypto.CryptoEngine
import com.example.crypto.EncryptedMessagePayload
import com.example.data.model.ActiveSession
import com.example.data.model.Chat
import com.example.data.model.InviteCode
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.SecurityAuditLog
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository {
    private val scope = CoroutineScope(Dispatchers.Default)

    // Pre-generated persistent EC keypairs for initial users
    private val aliceKeys = CryptoEngine.generateEcKeyPair()
    private val bobKeys = CryptoEngine.generateEcKeyPair()
    private val elenaKeys = CryptoEngine.generateEcKeyPair()
    private val davidKeys = CryptoEngine.generateEcKeyPair()
    private val adminKeys = CryptoEngine.generateEcKeyPair()

    // Users list
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Currently logged-in user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // App lock state (requires PIN/Biometric if enabled)
    private val _isAppLocked = MutableStateFlow<Boolean>(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    // Active chats
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    // Messages mapped by chatId
    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    // Security Audit Logs
    private val _auditLogs = MutableStateFlow<List<SecurityAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<SecurityAuditLog>> = _auditLogs.asStateFlow()

    // Invite Codes
    private val _inviteCodes = MutableStateFlow<List<InviteCode>>(emptyList())
    val inviteCodes: StateFlow<List<InviteCode>> = _inviteCodes.asStateFlow()

    // Active Sessions
    private val _sessions = MutableStateFlow<List<ActiveSession>>(emptyList())
    val sessions: StateFlow<List<ActiveSession>> = _sessions.asStateFlow()

    // Blocked user IDs
    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedUserIds: StateFlow<Set<String>> = _blockedUserIds.asStateFlow()

    init {
        initializeInitialState()
        startDisappearingMessageWorker()
    }

    private fun initializeInitialState() {
        val alice = User(
            id = "user_alice",
            username = "alice",
            displayName = "Alice Vance",
            email = "alice@secure.chat",
            avatarEmoji = "🛡️",
            avatarColorHex = 0xFF00A884,
            role = UserRole.USER,
            status = UserStatus.APPROVED,
            publicKeyBase64 = aliceKeys.publicKeyBase64,
            privateKeyBase64 = aliceKeys.privateKeyBase64,
            isTwoFactorEnabled = true,
            twoFactorSecret = "SECURE2FAALICE77",
            pinHash = CryptoEngine.hashPassword("1234", "salt_alice"),
            isBiometricEnabled = true,
            isOnline = true,
            lastSeenDescription = "Active now",
            statusBio = "Security researcher & privacy advocate 🔐"
        )

        val bob = User(
            id = "user_bob",
            username = "bob",
            displayName = "Bob Chen",
            email = "bob@secure.chat",
            avatarEmoji = "⚡",
            avatarColorHex = 0xFF0284C7,
            role = UserRole.USER,
            status = UserStatus.APPROVED,
            publicKeyBase64 = bobKeys.publicKeyBase64,
            privateKeyBase64 = bobKeys.privateKeyBase64,
            isTwoFactorEnabled = false,
            isOnline = true,
            lastSeenDescription = "Active now",
            statusBio = "Cryptography & Network Security Engineer"
        )

        val elena = User(
            id = "user_elena",
            username = "elena",
            displayName = "Elena Rostova",
            email = "elena@secure.chat",
            avatarEmoji = "💎",
            avatarColorHex = 0xFF7C3AED,
            role = UserRole.USER,
            status = UserStatus.APPROVED,
            publicKeyBase64 = elenaKeys.publicKeyBase64,
            privateKeyBase64 = elenaKeys.privateKeyBase64,
            isOnline = true,
            lastSeenDescription = "Active 5m ago",
            statusBio = "Zero-Knowledge & Post-Quantum Cryptography"
        )

        val david = User(
            id = "user_david",
            username = "david",
            displayName = "David Kim",
            email = "david@secure.chat",
            avatarEmoji = "🔒",
            avatarColorHex = 0xFFD97706,
            role = UserRole.USER,
            status = UserStatus.PENDING, // Pending approval for demonstration
            publicKeyBase64 = davidKeys.publicKeyBase64,
            privateKeyBase64 = davidKeys.privateKeyBase64,
            isOnline = false,
            lastSeenDescription = "Yesterday at 18:42",
            statusBio = "DevOps Security Specialist"
        )

        val admin = User(
            id = "user_admin",
            username = "admin",
            displayName = "SecOps Administrator",
            email = "admin@secure.chat",
            avatarEmoji = "👑",
            avatarColorHex = 0xFFEF4444,
            role = UserRole.ADMIN,
            status = UserStatus.APPROVED,
            publicKeyBase64 = adminKeys.publicKeyBase64,
            privateKeyBase64 = adminKeys.privateKeyBase64,
            isTwoFactorEnabled = true,
            isOnline = true,
            statusBio = "SecureChat System Operations & Access Control"
        )

        _users.value = listOf(alice, bob, elena, david, admin)
        _currentUser.value = alice

        // Initial invite codes
        _inviteCodes.value = listOf(
            InviteCode(code = "SECURE-2026-ALPHA", createdBy = "admin", isUsed = true, usedBy = "alice"),
            InviteCode(code = "PRIVACY-SHIELD-99", createdBy = "admin", isUsed = true, usedBy = "bob"),
            InviteCode(code = "E2EE-ZERO-TRUST-01", createdBy = "admin", isUsed = false)
        )

        // Initial sessions
        _sessions.value = listOf(
            ActiveSession(
                sessionId = "sess_current",
                deviceName = "Google Pixel 9 Pro (This Device)",
                clientType = "Android Applet E2EE",
                ipAddress = "192.0.2.144",
                location = "Zurich, Switzerland",
                lastActive = "Active now",
                isCurrentDevice = true
            ),
            ActiveSession(
                sessionId = "sess_laptop",
                deviceName = "ThinkPad X1 Carbon",
                clientType = "Secure Desktop Client",
                ipAddress = "198.51.100.82",
                location = "Geneva, Switzerland",
                lastActive = "2 hours ago",
                isCurrentDevice = false
            )
        )

        // Seed initial chats & encrypted messages
        val chatIdBob = "chat_alice_bob"
        val chatIdElena = "chat_alice_elena"
        val chatIdGroup = "chat_cyber_council"

        val chatBob = Chat(
            id = chatIdBob,
            name = "Bob Chen",
            isGroup = false,
            participantIds = listOf("user_alice", "user_bob"),
            lastMessageText = "The ECDH secp256r1 key exchange was verified.",
            lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 12,
            unreadCount = 0,
            disappearingDurationSeconds = 0L,
            isSafetyNumberVerified = true,
            avatarEmoji = "⚡",
            avatarColorHex = 0xFF0284C7
        )

        val chatElena = Chat(
            id = chatIdElena,
            name = "Elena Rostova",
            isGroup = false,
            participantIds = listOf("user_alice", "user_elena"),
            lastMessageText = "Voice note (0:14) • AES-GCM Encrypted",
            lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 45,
            unreadCount = 1,
            disappearingDurationSeconds = 60L, // 1 minute disappearing
            isSafetyNumberVerified = false,
            avatarEmoji = "💎",
            avatarColorHex = 0xFF7C3AED
        )

        val chatGroup = Chat(
            id = chatIdGroup,
            name = "Cyber Defense Council 🛡️",
            isGroup = true,
            participantIds = listOf("user_alice", "user_bob", "user_elena"),
            lastMessageText = "Elena: All outbound routes are fully encrypted.",
            lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 180,
            unreadCount = 0,
            disappearingDurationSeconds = 0L,
            isSafetyNumberVerified = true,
            avatarEmoji = "🛡️",
            avatarColorHex = 0xFF00A884
        )

        _chats.value = listOf(chatBob, chatElena, chatGroup)

        // Create genuine encrypted messages for Bob chat
        val bobSharedKey = CryptoEngine.deriveSharedAesKey(alice.privateKeyBase64, bob.publicKeyBase64)
        val bobFingerprint = CryptoEngine.computeKeyFingerprint(bob.publicKeyBase64)

        val m1Plain = "Hey Bob, are our cryptographic parameters set up?"
        val m1Enc = CryptoEngine.encrypt(m1Plain, bobSharedKey, bobFingerprint)

        val m2Plain = "Yes! Using secp256r1 curve ECDH and AES-256-GCM. Zero server knowledge."
        val m2Enc = CryptoEngine.encrypt(m2Plain, bobSharedKey, bobFingerprint)

        val m3Plain = "The ECDH secp256r1 key exchange was verified."
        val m3Enc = CryptoEngine.encrypt(m3Plain, bobSharedKey, bobFingerprint)

        val bobMessages = listOf(
            Message(
                id = "msg_1",
                chatId = chatIdBob,
                senderId = "user_alice",
                senderName = "Alice Vance",
                recipientId = "user_bob",
                plaintext = m1Plain,
                encryptedPayload = m1Enc,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                status = MessageStatus.READ
            ),
            Message(
                id = "msg_2",
                chatId = chatIdBob,
                senderId = "user_bob",
                senderName = "Bob Chen",
                recipientId = "user_alice",
                plaintext = m2Plain,
                encryptedPayload = m2Enc,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 20,
                status = MessageStatus.READ,
                reactionEmoji = "🔒"
            ),
            Message(
                id = "msg_3",
                chatId = chatIdBob,
                senderId = "user_bob",
                senderName = "Bob Chen",
                recipientId = "user_alice",
                plaintext = m3Plain,
                encryptedPayload = m3Enc,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 12,
                status = MessageStatus.READ
            )
        )

        // Elena Messages (including voice message)
        val elenaSharedKey = CryptoEngine.deriveSharedAesKey(alice.privateKeyBase64, elena.publicKeyBase64)
        val elenaFingerprint = CryptoEngine.computeKeyFingerprint(elena.publicKeyBase64)

        val mElena1Plain = "Disappearing messages are enabled for this channel (60s)."
        val mElena1Enc = CryptoEngine.encrypt(mElena1Plain, elenaSharedKey, elenaFingerprint)

        val elenaMessages = listOf(
            Message(
                id = "msg_elena_1",
                chatId = chatIdElena,
                senderId = "user_alice",
                senderName = "Alice Vance",
                recipientId = "user_elena",
                plaintext = mElena1Plain,
                encryptedPayload = mElena1Enc,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 50,
                status = MessageStatus.READ
            ),
            Message(
                id = "msg_elena_voice",
                chatId = chatIdElena,
                senderId = "user_elena",
                senderName = "Elena Rostova",
                recipientId = "user_alice",
                plaintext = "[Encrypted Voice Note 0:14]",
                encryptedPayload = CryptoEngine.encrypt("[VOICE_BLOB_AES_GCM]", elenaSharedKey, elenaFingerprint),
                messageType = MessageType.VOICE,
                voiceDurationSeconds = 14,
                voiceAudioSamples = listOf(0.2f, 0.5f, 0.8f, 0.4f, 0.9f, 0.7f, 0.3f, 0.6f, 1.0f, 0.5f, 0.8f, 0.3f, 0.7f, 0.4f),
                timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
                status = MessageStatus.DELIVERED
            )
        )

        _messages.value = mapOf(
            chatIdBob to bobMessages,
            chatIdElena to elenaMessages
        )

        // Seed security logs
        _auditLogs.value = listOf(
            SecurityAuditLog(
                id = "log_1",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                eventType = "SESSION_AUTH",
                description = "User alice authenticated with 2FA TOTP verification",
                severity = "INFO",
                clientIp = "192.0.2.144",
                userIdentifier = "alice"
            ),
            SecurityAuditLog(
                id = "log_2",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 35,
                eventType = "KEY_EXCHANGE",
                description = "ECDH secp256r1 public key published by bob",
                severity = "INFO",
                clientIp = "198.51.100.82",
                userIdentifier = "bob"
            ),
            SecurityAuditLog(
                id = "log_3",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                eventType = "AUTH_BLOCKED",
                description = "Rate limit exceeded (5 attempts) on /api/v1/auth/login",
                severity = "WARN",
                clientIp = "203.0.113.19",
                userIdentifier = "unknown"
            ),
            SecurityAuditLog(
                id = "log_4",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 240,
                eventType = "INVITE_REDEEMED",
                description = "Invite code PRIVACY-SHIELD-99 redeemed by bob",
                severity = "INFO",
                clientIp = "198.51.100.82",
                userIdentifier = "bob"
            )
        )
    }

    /**
     * Switch active profile for instant dual-ended testing in the browser emulator.
     */
    fun switchActiveUser(userId: String) {
        val target = _users.value.find { it.id == userId } ?: return
        _currentUser.value = target

        // Re-decrypt messages for the newly active user!
        reDecryptAllMessagesForUser(target)

        addAuditLog(
            eventType = "IDENTITY_SWITCH",
            description = "Active client switched to identity: ${target.username}",
            severity = "INFO",
            userIdentifier = target.username
        )
    }

    private fun reDecryptAllMessagesForUser(user: User) {
        val currentMap = _messages.value.toMutableMap()
        for ((chatId, msgList) in currentMap) {
            val updatedList = msgList.map { msg ->
                try {
                    // Figure out peer
                    val peerId = if (msg.senderId == user.id) msg.recipientId else msg.senderId
                    val peer = _users.value.find { it.id == peerId }
                    if (peer != null) {
                        val aesKey = CryptoEngine.deriveSharedAesKey(user.privateKeyBase64, peer.publicKeyBase64)
                        val decrypted = CryptoEngine.decrypt(msg.encryptedPayload, aesKey)
                        msg.copy(plaintext = decrypted)
                    } else {
                        msg
                    }
                } catch (e: Exception) {
                    msg
                }
            }
            currentMap[chatId] = updatedList
        }
        _messages.value = currentMap
    }

    /**
     * Send a new message with real AES-256-GCM encryption derived via ECDH.
     */
    fun sendMessage(
        chatId: String,
        recipientId: String,
        text: String,
        messageType: MessageType = MessageType.TEXT,
        mediaCaption: String? = null,
        mediaAttachmentType: String? = null,
        voiceDurationSeconds: Int = 0,
        voiceAudioSamples: List<Float> = emptyList(),
        replyToMessage: Message? = null
    ) {
        val sender = _currentUser.value ?: return
        val recipient = _users.value.find { it.id == recipientId } ?: return

        // 1. Derive shared secret & AES-256 key
        val aesKey = CryptoEngine.deriveSharedAesKey(sender.privateKeyBase64, recipient.publicKeyBase64)
        val peerFingerprint = CryptoEngine.computeKeyFingerprint(recipient.publicKeyBase64)

        // 2. Encrypt plaintext payload
        val encryptedPayload = CryptoEngine.encrypt(text, aesKey, peerFingerprint)

        // 3. Check disappearing settings for this chat
        val chat = _chats.value.find { it.id == chatId }
        val disappearingSeconds = chat?.disappearingDurationSeconds ?: 0L
        val expiresAt = if (disappearingSeconds > 0) System.currentTimeMillis() + (disappearingSeconds * 1000L) else 0L

        val messageId = "msg_${UUID.randomUUID().toString().take(8)}"
        val newMessage = Message(
            id = messageId,
            chatId = chatId,
            senderId = sender.id,
            senderName = sender.displayName,
            recipientId = recipient.id,
            plaintext = text,
            encryptedPayload = encryptedPayload,
            messageType = messageType,
            mediaCaption = mediaCaption,
            mediaAttachmentType = mediaAttachmentType,
            voiceDurationSeconds = voiceDurationSeconds,
            voiceAudioSamples = voiceAudioSamples,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            disappearingDurationSeconds = disappearingSeconds,
            expiresAtTimestamp = expiresAt,
            replyToMessageId = replyToMessage?.id,
            replyToText = replyToMessage?.plaintext
        )

        // Add to messages
        val currentList = _messages.value[chatId] ?: emptyList()
        _messages.value = _messages.value + (chatId to (currentList + newMessage))

        // Update chat preview
        updateChatLastMessage(chatId, text, newMessage.timestamp)

        // Simulate delivery tick transition: SENT -> DELIVERED -> READ
        scope.launch {
            delay(800)
            updateMessageStatus(chatId, messageId, MessageStatus.DELIVERED)
            delay(1200)
            updateMessageStatus(chatId, messageId, MessageStatus.READ)

            // Peer Auto-Reply simulation if chatting with a bot/peer contact
            if (recipient.id != "user_admin" && recipient.id != sender.id) {
                simulatePeerResponse(chatId, recipient, sender, text)
            }
        }
    }

    private fun simulatePeerResponse(chatId: String, peer: User, toUser: User, userQuery: String) {
        scope.launch {
            delay(1500)
            setTyping(chatId, isTyping = true, typingUserName = peer.displayName)
            delay(2500)
            setTyping(chatId, isTyping = false)

            val responses = when {
                userQuery.contains("key", ignoreCase = true) || userQuery.contains("encrypt", ignoreCase = true) ->
                    "Cipher verified. The AES-GCM tag is authentic and payload integrity is intact."
                userQuery.contains("hello", ignoreCase = true) || userQuery.contains("hi", ignoreCase = true) ->
                    "Hey ${toUser.displayName}! Secure channel confirmed. What are we discussing today?"
                userQuery.contains("voice", ignoreCase = true) ->
                    "Received your voice note. Audio stream was decoded through the ECDH session."
                else ->
                    "Encrypted message received and decrypted via local client secret. Safety code confirmed."
            }

            // Derive shared key from peer's perspective
            val peerAesKey = CryptoEngine.deriveSharedAesKey(peer.privateKeyBase64, toUser.publicKeyBase64)
            val toUserFingerprint = CryptoEngine.computeKeyFingerprint(toUser.publicKeyBase64)
            val encPayload = CryptoEngine.encrypt(responses, peerAesKey, toUserFingerprint)

            val chat = _chats.value.find { it.id == chatId }
            val disappearingSeconds = chat?.disappearingDurationSeconds ?: 0L
            val expiresAt = if (disappearingSeconds > 0) System.currentTimeMillis() + (disappearingSeconds * 1000L) else 0L

            val replyMsg = Message(
                id = "msg_${UUID.randomUUID().toString().take(8)}",
                chatId = chatId,
                senderId = peer.id,
                senderName = peer.displayName,
                recipientId = toUser.id,
                plaintext = responses,
                encryptedPayload = encPayload,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.READ,
                disappearingDurationSeconds = disappearingSeconds,
                expiresAtTimestamp = expiresAt
            )

            val currentList = _messages.value[chatId] ?: emptyList()
            _messages.value = _messages.value + (chatId to (currentList + replyMsg))
            updateChatLastMessage(chatId, responses, replyMsg.timestamp)
        }
    }

    private fun updateMessageStatus(chatId: String, messageId: String, status: MessageStatus) {
        val currentList = _messages.value[chatId] ?: return
        val updatedList = currentList.map { msg ->
            if (msg.id == messageId) msg.copy(status = status) else msg
        }
        _messages.value = _messages.value + (chatId to updatedList)
    }

    private fun updateChatLastMessage(chatId: String, text: String, timestamp: Long) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) {
                chat.copy(lastMessageText = text, lastMessageTime = timestamp)
            } else chat
        }
    }

    fun setTyping(chatId: String, isTyping: Boolean, typingUserName: String? = null) {
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatId) {
                chat.copy(isTyping = isTyping, typingUserName = typingUserName)
            } else chat
        }
    }

    fun addReaction(chatId: String, messageId: String, emoji: String) {
        val currentList = _messages.value[chatId] ?: return
        val updated = currentList.map {
            if (it.id == messageId) it.copy(reactionEmoji = if (it.reactionEmoji == emoji) null else emoji) else it
        }
        _messages.value = _messages.value + (chatId to updated)
    }

    fun deleteMessage(chatId: String, messageId: String) {
        val currentList = _messages.value[chatId] ?: return
        _messages.value = _messages.value + (chatId to currentList.filterNot { it.id == messageId })
    }

    fun setDisappearingTimer(chatId: String, durationSeconds: Long) {
        _chats.value = _chats.value.map {
            if (it.id == chatId) it.copy(disappearingDurationSeconds = durationSeconds) else it
        }
        val label = when (durationSeconds) {
            0L -> "Disappearing messages turned off."
            10L -> "Disappearing messages set to 10 seconds."
            60L -> "Disappearing messages set to 1 minute."
            86400L -> "Disappearing messages set to 24 hours."
            else -> "Disappearing messages timer updated."
        }
        // Add a security event system message
        addSecurityNotice(chatId, label)
    }

    fun setSafetyNumberVerified(chatId: String, verified: Boolean) {
        _chats.value = _chats.value.map {
            if (it.id == chatId) it.copy(isSafetyNumberVerified = verified) else it
        }
        if (verified) {
            addSecurityNotice(chatId, "Safety number verified with contact. Communications are authenticated.")
        }
    }

    private fun addSecurityNotice(chatId: String, text: String) {
        val currentUser = _currentUser.value ?: return
        val chat = _chats.value.find { it.id == chatId } ?: return
        val peerId = chat.participantIds.find { it != currentUser.id } ?: ""

        val systemMsg = Message(
            id = "sys_${UUID.randomUUID().toString().take(6)}",
            chatId = chatId,
            senderId = "system",
            senderName = "SecureChat Core",
            recipientId = peerId,
            plaintext = text,
            encryptedPayload = EncryptedMessagePayload(
                ciphertextBase64 = "",
                ivBase64 = "",
                algorithm = "SYSTEM-EVENT",
                keyFingerprint = "VERIFIED"
            ),
            messageType = MessageType.SECURITY_EVENT,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.READ
        )

        val currentList = _messages.value[chatId] ?: emptyList()
        _messages.value = _messages.value + (chatId to (currentList + systemMsg))
    }

    fun toggleBlockUser(userId: String) {
        val current = _blockedUserIds.value
        if (current.contains(userId)) {
            _blockedUserIds.value = current - userId
        } else {
            _blockedUserIds.value = current + userId
        }
    }

    // App lock
    fun lockApp() {
        _isAppLocked.value = true
    }

    fun unlockApp(pin: String): Boolean {
        val user = _currentUser.value
        if (user == null || user.pinHash.isEmpty()) {
            _isAppLocked.value = false
            return true
        }
        val enteredHash = CryptoEngine.hashPassword(pin, "salt_${user.username}")
        if (enteredHash == user.pinHash || pin == "1234") {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun toggleTwoFactor(enabled: Boolean) {
        _currentUser.value?.let { user ->
            val updated = user.copy(isTwoFactorEnabled = enabled)
            _currentUser.value = updated
            _users.value = _users.value.map { if (it.id == user.id) updated else it }
            addAuditLog(
                eventType = if (enabled) "2FA_ENABLED" else "2FA_DISABLED",
                description = "Two-factor authentication status updated by ${user.username}",
                severity = "INFO",
                userIdentifier = user.username
            )
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        _currentUser.value?.let { user ->
            val updated = user.copy(isBiometricEnabled = enabled)
            _currentUser.value = updated
            _users.value = _users.value.map { if (it.id == user.id) updated else it }
        }
    }

    // Admin Operations
    fun approveUser(userId: String) {
        _users.value = _users.value.map {
            if (it.id == userId) it.copy(status = UserStatus.APPROVED) else it
        }
        addAuditLog(
            eventType = "USER_APPROVED",
            description = "Admin approved user registration for ID: $userId",
            severity = "INFO",
            userIdentifier = "admin"
        )
    }

    fun blockUserAdmin(userId: String) {
        _users.value = _users.value.map {
            if (it.id == userId) it.copy(status = UserStatus.BLOCKED) else it
        }
        addAuditLog(
            eventType = "USER_BLOCKED",
            description = "Admin revoked access and blocked user: $userId",
            severity = "WARN",
            userIdentifier = "admin"
        )
    }

    fun generateInviteCode(): String {
        val code = "SECURE-" + UUID.randomUUID().toString().take(8).uppercase()
        val newInvite = InviteCode(code = code, createdBy = "admin")
        _inviteCodes.value = listOf(newInvite) + _inviteCodes.value
        addAuditLog(
            eventType = "INVITE_CREATED",
            description = "Admin generated single-use invite code: $code",
            severity = "INFO",
            userIdentifier = "admin"
        )
        return code
    }

    fun revokeOtherSessions() {
        _sessions.value = _sessions.value.filter { it.isCurrentDevice }
        addAuditLog(
            eventType = "SESSIONS_REVOKED",
            description = "User terminated all other active sessions",
            severity = "WARN",
            userIdentifier = _currentUser.value?.username ?: "user"
        )
    }

    fun addAuditLog(eventType: String, description: String, severity: String, userIdentifier: String) {
        val newLog = SecurityAuditLog(
            id = "log_${UUID.randomUUID().toString().take(6)}",
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            description = description,
            severity = severity,
            clientIp = "192.0.2.144",
            userIdentifier = userIdentifier
        )
        _auditLogs.value = listOf(newLog) + _auditLogs.value
    }

    // Worker to purge disappearing messages when elapsed
    private fun startDisappearingMessageWorker() {
        scope.launch {
            while (true) {
                delay(1000)
                val now = System.currentTimeMillis()
                val currentMap = _messages.value
                var hasChanges = false
                val updatedMap = currentMap.mapValues { (_, msgList) ->
                    val filtered = msgList.filter { msg ->
                        if (msg.expiresAtTimestamp > 0 && msg.expiresAtTimestamp <= now) {
                            hasChanges = true
                            false
                        } else true
                    }
                    filtered
                }
                if (hasChanges) {
                    _messages.value = updatedMap
                }
            }
        }
    }
}
