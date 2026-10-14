package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Message
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.Screen
import com.example.ui.theme.DarkMessageIncoming
import com.example.ui.theme.DarkMessageOutgoing
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.LightMessageIncoming
import com.example.ui.theme.LightMessageOutgoing
import com.example.ui.theme.SecurityCyanAccent
import com.example.ui.theme.SecurityDangerRed
import com.example.ui.theme.SecurityShieldGold
import com.example.ui.theme.SecurityVerifiedBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserAvatar(
    emoji: String,
    colorHex: Long,
    sizeDp: Int = 48,
    isOnline: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .background(Color(colorHex)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = (sizeDp * 0.48).sp
            )
        }
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size((sizeDp * 0.28).dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

@Composable
fun AppHeader(
    currentUser: User?,
    allUsers: List<User>,
    onSwitchUser: (String) -> Unit,
    onNavigate: (Screen) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLockApp: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Title & Brand
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigate(Screen.ChatList) }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "SecureChat Lock",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SecureChat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
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
                        text = "End-to-End Encrypted",
                        fontSize = 11.sp,
                        color = EmeraldLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right side actions: User Switcher Pill, Admin, Lock, Theme
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Identity Switcher Pill (Critical for fast testing in browser emulator)
                if (currentUser != null) {
                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { showUserMenu = true }
                                .testTag("user_switcher_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentUser.avatarEmoji,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentUser.displayName.split(" ").first(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Switch User",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            Text(
                                text = "Switch Active Identity",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            allUsers.forEach { user ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(user.avatarEmoji, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = user.displayName + (if (user.id == currentUser.id) " (You)" else ""),
                                                    fontWeight = if (user.id == currentUser.id) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = if (user.role == UserRole.ADMIN) "Administrator" else "Verified User",
                                                    fontSize = 11.sp,
                                                    color = if (user.role == UserRole.ADMIN) SecurityDangerRed else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        showUserMenu = false
                                        onSwitchUser(user.id)
                                    }
                                )
                            }
                        }
                    }
                }

                // Admin Button if user is Admin
                if (currentUser?.role == UserRole.ADMIN) {
                    IconButton(
                        onClick = { onNavigate(Screen.AdminPanel) },
                        modifier = Modifier.size(36.dp).testTag("admin_panel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Panel",
                            tint = SecurityDangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Lock Vault Button
                IconButton(
                    onClick = onLockApp,
                    modifier = Modifier.size(36.dp).testTag("lock_app_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Lock App",
                        tint = SecurityShieldGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Theme Toggle
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.size(36.dp).testTag("toggle_theme_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityNoticeBanner(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF132A22)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = EmeraldLight,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Messages are end-to-end encrypted with ECDH secp256r1 & AES-256-GCM. No one outside of this chat can read them.",
                fontSize = 11.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isOutgoing: Boolean,
    isDarkTheme: Boolean,
    onInspectPayload: (Message) -> Unit,
    onReaction: (String) -> Unit,
    onReply: (Message) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showActionSheet by remember { mutableStateOf(false) }

    val bubbleColor = when {
        isOutgoing && isDarkTheme -> DarkMessageOutgoing
        isOutgoing && !isDarkTheme -> LightMessageOutgoing
        !isOutgoing && isDarkTheme -> DarkMessageIncoming
        else -> LightMessageIncoming
    }

    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val timeString = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = bubbleShape,
            tonalElevation = 1.dp,
            modifier = Modifier
                .clickable { showActionSheet = true }
                .padding(end = if (isOutgoing) 0.dp else 40.dp, start = if (isOutgoing) 40.dp else 0.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Reply quote header if replying
                if (!message.replyToText.isNullOrEmpty()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(modifier = Modifier.padding(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(EmeraldLight)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.replyToText,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Message Type Renderer
                when (message.messageType) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.plaintext,
                            fontSize = 14.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }

                    MessageType.IMAGE -> {
                        Column {
                            // Simulated secure image thumbnail
                            Box(
                                modifier = Modifier
                                    .size(width = 220.dp, height = 130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F261E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Encrypted Media",
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🔒 Encrypted Photo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "AES-256-GCM authenticated",
                                        fontSize = 10.sp,
                                        color = EmeraldLight
                                    )
                                }
                            }
                            if (!message.mediaCaption.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message.mediaCaption,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    MessageType.VOICE -> {
                        // Voice note audio player bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = EmeraldPrimary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play voice note",
                                    tint = Color.White,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // Waveform bars
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                val samples = if (message.voiceAudioSamples.isNotEmpty()) {
                                    message.voiceAudioSamples
                                } else {
                                    listOf(0.4f, 0.7f, 0.3f, 0.9f, 0.6f, 0.8f, 0.3f, 0.5f, 0.8f, 0.4f)
                                }
                                samples.forEach { sample ->
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height((8 + (sample * 16)).dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(EmeraldLight)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "0:${String.format("%02d", message.voiceDurationSeconds.coerceAtLeast(1))}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    MessageType.DOCUMENT -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Document",
                                tint = SecurityCyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.plaintext,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    MessageType.SECURITY_EVENT -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = SecurityShieldGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.plaintext,
                                fontSize = 12.sp,
                                color = SecurityShieldGold,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }

                // Footer: Disappearing timer icon, Timestamp, and Delivery status ticks
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.disappearingDurationSeconds > 0) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Disappearing",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(11.dp)
                                .padding(end = 2.dp)
                        )
                    }

                    Text(
                        text = timeString,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        when (message.status) {
                            MessageStatus.SENDING -> {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Sending",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            MessageStatus.SENT -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sent",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            MessageStatus.DELIVERED -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Delivered",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            MessageStatus.READ -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Read",
                                    tint = SecurityVerifiedBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reaction pill display
        if (message.reactionEmoji != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clickable { onReaction(message.reactionEmoji) }
            ) {
                Text(
                    text = message.reactionEmoji,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }

    // Interactive Action Dialog
    if (showActionSheet) {
        AlertDialog(
            onDismissRequest = { showActionSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = EmeraldLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Message Actions", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    // Quick emoji reactions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("👍", "❤️", "🔒", "🔥", "😂", "😮").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        onReaction(emoji)
                                        showActionSheet = false
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }

                    // Inspect raw E2EE cipher payload
                    Button(
                        onClick = {
                            showActionSheet = false
                            onInspectPayload(message)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("inspect_crypto_button")
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Inspect AES-256-GCM Payload")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showActionSheet = false
                            onReply(message)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reply / Quote", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showActionSheet = false
                            onDelete(message.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecurityDangerRed.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete Message", color = SecurityDangerRed)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActionSheet = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PayloadInspectorDialog(
    message: Message,
    onDismiss: () -> Unit
) {
    val payload = message.encryptedPayload

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = EmeraldLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text("E2EE Cryptographic Verification", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "This message was encrypted on device using authenticated AES-256-GCM with a dynamic session key derived via ECDH secp256r1. The server and network routers only ever transmit this raw ciphertext.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Cipher parameters
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF091410)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Algorithm: ${payload.algorithm}", fontSize = 11.sp, color = EmeraldLight, fontWeight = FontWeight.Bold)
                        Text("Authentication Tag: 128-bit GCM", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        Text("Recipient Key Fingerprint: ${payload.keyFingerprint.ifBlank { "VERIFIED-PEER" }}", fontSize = 11.sp, color = SecurityCyanAccent)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Raw Ciphertext (Base64):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Surface(
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = payload.ciphertextBase64.chunked(32).joinToString("\n"),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text("Cryptographic IV (12 Bytes Base64):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Surface(
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = payload.ivBase64,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = SecurityShieldGold,
                        modifier = Modifier.padding(6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text("Decrypted Local Plaintext (Your Client Memory Only):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message.plaintext,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldLight,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Dismiss Verification")
            }
        }
    )
}
