package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.MessageBubble
import com.example.ui.components.PayloadInspectorDialog
import com.example.ui.components.UserAvatar
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SecurityDangerRed
import com.example.ui.theme.SecurityShieldGold
import com.example.ui.theme.SecurityVerifiedBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.ChatList)
    }

    val currentUser by viewModel.repository.currentUser.collectAsState()
    val allUsers by viewModel.repository.users.collectAsState()
    val chats by viewModel.repository.chats.collectAsState()
    val messagesMap by viewModel.repository.messages.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    val inspectingMessage by viewModel.inspectingMessage.collectAsState()
    val replyingToMessage by viewModel.replyingToMessage.collectAsState()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()
    val recordingSeconds by viewModel.recordingSeconds.collectAsState()

    val chat = chats.find { it.id == chatId }
    val peerId = chat?.participantIds?.find { it != currentUser?.id } ?: ""
    val peerUser = allUsers.find { it.id == peerId }
    val messages = messagesMap[chatId] ?: emptyList()

    var textInput by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showDisappearingDialog by remember { mutableStateOf(false) }
    var showFeatureAlert by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            viewModel.navigateTo(Screen.SafetyNumber(chatId))
                        }
                    ) {
                        UserAvatar(
                            emoji = chat?.avatarEmoji ?: "👤",
                            colorHex = chat?.avatarColorHex ?: 0xFF00A884,
                            sizeDp = 38,
                            isOnline = peerUser?.isOnline == true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = chat?.name ?: "Encrypted Chat",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "E2EE",
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = if (chat?.isTyping == true) "typing..." else if (peerUser?.isOnline == true) "Active now • E2EE" else "Offline • E2EE",
                                fontSize = 11.sp,
                                color = if (chat?.isTyping == true) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.ChatList) },
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFeatureAlert = "Encrypted VoIP Audio Call initiated with peer..." }) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { showFeatureAlert = "Secure Video Call initiated with peer..." }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.SafetyNumber(chatId)) },
                        modifier = Modifier.testTag("verify_safety_code_button")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = "Safety Code", tint = EmeraldLight)
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Verify Safety Number") },
                            onClick = {
                                showMenu = false
                                viewModel.navigateTo(Screen.SafetyNumber(chatId))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Disappearing Messages (${if ((chat?.disappearingDurationSeconds ?: 0) > 0) "${chat?.disappearingDurationSeconds}s" else "Off"})") },
                            onClick = {
                                showMenu = false
                                showDisappearingDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Block Contact", color = SecurityDangerRed) },
                            onClick = {
                                showMenu = false
                                peerUser?.let { viewModel.repository.toggleBlockUser(it.id) }
                                showFeatureAlert = "Contact privacy status updated."
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Reply banner if replying to a message
                AnimatedVisibility(visible = replyingToMessage != null) {
                    replyingToMessage?.let { replyMsg ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(30.dp)
                                            .background(EmeraldPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Replying to ${replyMsg.senderName}", fontSize = 11.sp, color = EmeraldLight, fontWeight = FontWeight.Bold)
                                        Text(replyMsg.plaintext, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                IconButton(onClick = { viewModel.setReplyTo(null) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // Voice recording banner if active
                if (isRecordingVoice) {
                    Surface(
                        color = Color(0xFF1B2D25),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(SecurityDangerRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recording Encrypted Audio 0:${String.format("%02d", recordingSeconds)}",
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Row {
                                TextButton(onClick = { viewModel.cancelVoiceRecording() }) {
                                    Text("Cancel", color = SecurityDangerRed)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.stopAndSendVoiceRecording(chatId, peerId) },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary)
                                        .size(36.dp)
                                        .testTag("send_voice_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send voice", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                } else {
                    // Normal message input row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Attachment paperclip
                        IconButton(
                            onClick = { showAttachmentSheet = true },
                            modifier = Modifier.testTag("attachment_button")
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Message Text Field
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Encrypted message...", fontSize = 14.sp) },
                            singleLine = false,
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("message_input_field")
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Mic or Send FAB
                        if (textInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    viewModel.sendTextMessage(chatId, peerId, textInput)
                                    textInput = ""
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary)
                                    .size(44.dp)
                                    .testTag("send_message_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.startVoiceRecording() },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary)
                                    .size(44.dp)
                                    .testTag("record_voice_button")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Record Voice Note", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isDarkTheme) Color(0xFF0C1317) else Color(0xFFEFEAE2))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
            ) {
                // E2EE Top Security Warning
                item {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("End-to-End Encrypted Session", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldLight)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Messages to this chat are secured with authenticated AES-256-GCM. Tap any message to inspect cryptographic ciphertext and session keys.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    val isOutgoing = message.senderId == currentUser?.id
                    MessageBubble(
                        message = message,
                        isOutgoing = isOutgoing,
                        isDarkTheme = isDarkTheme,
                        onInspectPayload = { viewModel.inspectPayload(it) },
                        onReaction = { emoji -> viewModel.repository.addReaction(chatId, message.id, emoji) },
                        onReply = { viewModel.setReplyTo(it) },
                        onDelete = { viewModel.repository.deleteMessage(chatId, it) }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Attachments
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text("Send Encrypted Media", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            showAttachmentSheet = false
                            viewModel.sendImageAttachment(chatId, peerId, "Encrypted photo transmission")
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Photo", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Photo / Image", fontSize = 12.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            showAttachmentSheet = false
                            viewModel.sendDocumentAttachment(chatId, peerId, "classified_keys.asc")
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Description, contentDescription = "Document", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Encrypted Doc", fontSize = 12.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            showAttachmentSheet = false
                            showDisappearingDialog = true
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SecurityShieldGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = "Timer", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Disappearing", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Disappearing Messages Selector Dialog
    if (showDisappearingDialog) {
        AlertDialog(
            onDismissRequest = { showDisappearingDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = SecurityShieldGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Disappearing Messages", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "For added privacy, new messages will automatically self-destruct and disappear from this chat after the chosen timer expires.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    listOf(
                        0L to "Off (Keep messages)",
                        10L to "10 Seconds (High privacy test)",
                        60L to "1 Minute",
                        86400L to "24 Hours (Standard)"
                    ).forEach { (seconds, label) ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (chat?.disappearingDurationSeconds == seconds) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.repository.setDisappearingTimer(chatId, seconds)
                                    showDisappearingDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (chat?.disappearingDurationSeconds == seconds) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (chat?.disappearingDurationSeconds == seconds) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDisappearingDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Inspecting Raw Crypto Payload Dialog
    inspectingMessage?.let { msg ->
        PayloadInspectorDialog(
            message = msg,
            onDismiss = { viewModel.inspectPayload(null) }
        )
    }

    // Feature notification dialog
    showFeatureAlert?.let { alertText ->
        AlertDialog(
            onDismissRequest = { showFeatureAlert = null },
            title = { Text("Secure Telephony") },
            text = { Text(alertText) },
            confirmButton = {
                TextButton(onClick = { showFeatureAlert = null }) {
                    Text("OK")
                }
            }
        )
    }
}
