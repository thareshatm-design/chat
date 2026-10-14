package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.CryptoEngine
import com.example.data.ChatRepository
import com.example.data.model.Chat
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object AuthLogin : Screen()
    data object AuthRegister : Screen()
    data class Auth2FA(val email: String) : Screen()
    data object ChatList : Screen()
    data class ChatDetail(val chatId: String) : Screen()
    data object Settings : Screen()
    data class SafetyNumber(val chatId: String) : Screen()
    data object AdminPanel : Screen()
}

class MainViewModel(
    val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.ChatList)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Inspecting raw crypto payload dialog
    private val _inspectingMessage = MutableStateFlow<Message?>(null)
    val inspectingMessage: StateFlow<Message?> = _inspectingMessage.asStateFlow()

    // Voice recording state
    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private var voiceTimerJob: Job? = null

    // Quoting / replying to a message
    private val _replyingToMessage = MutableStateFlow<Message?>(null)
    val replyingToMessage: StateFlow<Message?> = _replyingToMessage.asStateFlow()

    // Filtered chats based on search query
    val filteredChats: StateFlow<List<Chat>> = combine(
        repository.chats,
        _searchQuery
    ) { chats, query ->
        if (query.isBlank()) chats
        else chats.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.lastMessageText.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun inspectPayload(message: Message?) {
        _inspectingMessage.value = message
    }

    fun setReplyTo(message: Message?) {
        _replyingToMessage.value = message
    }

    // Voice Note simulation
    fun startVoiceRecording() {
        _isRecordingVoice.value = true
        _recordingSeconds.value = 0
        voiceTimerJob?.cancel()
        voiceTimerJob = viewModelScope.launch {
            while (_isRecordingVoice.value) {
                delay(1000)
                _recordingSeconds.value += 1
            }
        }
    }

    fun stopAndSendVoiceRecording(chatId: String, recipientId: String) {
        val duration = _recordingSeconds.value
        _isRecordingVoice.value = false
        voiceTimerJob?.cancel()

        if (duration >= 1) {
            val samples = (1..15).map { (10..100).random() / 100f }
            repository.sendMessage(
                chatId = chatId,
                recipientId = recipientId,
                text = "Voice note (0:${String.format("%02d", duration)}) • AES-GCM Encrypted",
                messageType = MessageType.VOICE,
                voiceDurationSeconds = duration,
                voiceAudioSamples = samples
            )
        }
    }

    fun cancelVoiceRecording() {
        _isRecordingVoice.value = false
        voiceTimerJob?.cancel()
        _recordingSeconds.value = 0
    }

    fun sendTextMessage(chatId: String, recipientId: String, text: String) {
        if (text.isBlank()) return
        val reply = _replyingToMessage.value
        repository.sendMessage(
            chatId = chatId,
            recipientId = recipientId,
            text = text.trim(),
            replyToMessage = reply
        )
        _replyingToMessage.value = null
    }

    fun sendImageAttachment(chatId: String, recipientId: String, caption: String) {
        val reply = _replyingToMessage.value
        val text = if (caption.isNotBlank()) caption else "Encrypted image attachment (2.4 MB)"
        repository.sendMessage(
            chatId = chatId,
            recipientId = recipientId,
            text = text,
            messageType = MessageType.IMAGE,
            mediaCaption = caption.ifBlank { null },
            mediaAttachmentType = "image",
            replyToMessage = reply
        )
        _replyingToMessage.value = null
    }

    fun sendDocumentAttachment(chatId: String, recipientId: String, fileName: String) {
        repository.sendMessage(
            chatId = chatId,
            recipientId = recipientId,
            text = "Document: $fileName (Encrypted Blob)",
            messageType = MessageType.DOCUMENT,
            mediaAttachmentType = "pdf"
        )
    }

    // Login & Registration
    fun login(email: String, password: String): Boolean {
        val user = repository.users.value.find { it.email.equals(email.trim(), ignoreCase = true) }
        return if (user != null) {
            if (user.isTwoFactorEnabled) {
                navigateTo(Screen.Auth2FA(email))
            } else {
                repository.switchActiveUser(user.id)
                navigateTo(Screen.ChatList)
            }
            true
        } else {
            false
        }
    }

    fun verify2FA(email: String, code: String): Boolean {
        // Any 6 digit code for the demo
        if (code.length == 6) {
            val user = repository.users.value.find { it.email.equals(email.trim(), ignoreCase = true) }
            if (user != null) {
                repository.switchActiveUser(user.id)
                navigateTo(Screen.ChatList)
                return true
            }
        }
        return false
    }

    fun register(username: String, displayName: String, email: String, inviteCode: String): Pair<Boolean, String> {
        val codeMatch = repository.inviteCodes.value.find { it.code.equals(inviteCode.trim(), ignoreCase = true) && !it.isUsed }
        val requiresApproval = codeMatch == null

        val keyPair = CryptoEngine.generateEcKeyPair()
        val newUser = User(
            id = "user_${username.lowercase()}",
            username = username.lowercase(),
            displayName = displayName,
            email = email,
            avatarEmoji = "🛡️",
            avatarColorHex = 0xFF10B981,
            role = UserRole.USER,
            status = if (requiresApproval) com.example.data.model.UserStatus.PENDING else com.example.data.model.UserStatus.APPROVED,
            publicKeyBase64 = keyPair.publicKeyBase64,
            privateKeyBase64 = keyPair.privateKeyBase64,
            isOnline = true,
            statusBio = "Verified E2EE member"
        )

        val updated = repository.users.value + newUser
        // Switch to users flow or pending screen
        if (requiresApproval) {
            repository.addAuditLog(
                eventType = "SIGNUP_PENDING",
                description = "New registration by $username queued for Administrator approval",
                severity = "INFO",
                userIdentifier = username
            )
            return Pair(false, "Account created! Awaiting administrator approval before first login.")
        } else {
            repository.switchActiveUser(newUser.id)
            repository.addAuditLog(
                eventType = "SIGNUP_APPROVED",
                description = "New registration by $username authenticated via invite code $inviteCode",
                severity = "INFO",
                userIdentifier = username
            )
            navigateTo(Screen.ChatList)
            return Pair(true, "Registration successful! Cryptographic identity established.")
        }
    }
}
