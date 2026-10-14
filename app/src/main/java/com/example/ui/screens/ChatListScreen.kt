package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.model.Chat
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.AppHeader
import com.example.ui.components.SecurityNoticeBanner
import com.example.ui.components.UserAvatar
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SecurityShieldGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatListScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.repository.currentUser.collectAsState()
    val allUsers by viewModel.repository.users.collectAsState()
    val chats by viewModel.filteredChats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var showNewChatDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                AppHeader(
                    currentUser = currentUser,
                    allUsers = allUsers,
                    onSwitchUser = { viewModel.repository.switchActiveUser(it) },
                    onNavigate = { viewModel.navigateTo(it) },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleDarkTheme() },
                    onLockApp = { viewModel.repository.lockApp() }
                )
                SecurityNoticeBanner()
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already on Chats */ },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chats") },
                    label = { Text("Chats") },
                    modifier = Modifier.testTag("nav_chats")
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.Settings) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_settings")
                )

                if (currentUser?.role == UserRole.ADMIN) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.navigateTo(Screen.AdminPanel) },
                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "SecOps") },
                        label = { Text("SecOps") },
                        modifier = Modifier.testTag("nav_admin")
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewChatDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_new_chat")
            ) {
                Icon(Icons.Default.Chat, contentDescription = "New Encrypted Chat")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search encrypted chats...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("search_input")
                )
            }

            // Cryptographic Status Strip
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldLight)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Zero-Knowledge ECDH Channel Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Curve secp256r1",
                        fontSize = 10.sp,
                        color = EmeraldLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Chat List
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(chats, key = { it.id }) { chat ->
                    ChatItemRow(
                        chat = chat,
                        onClick = { viewModel.navigateTo(Screen.ChatDetail(chat.id)) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 76.dp)
                    )
                }

                if (chats.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No encrypted chats found",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New Chat Dialog
    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start End-to-End Encrypted Chat", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Select a contact to establish an authenticated ECDH session:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    allUsers.filter { it.id != currentUser?.id }.forEach { user ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    showNewChatDialog = false
                                    // Check existing or default to chat
                                    val existingChat = chats.find { it.participantIds.contains(user.id) }
                                    if (existingChat != null) {
                                        viewModel.navigateTo(Screen.ChatDetail(existingChat.id))
                                    } else {
                                        val chatId = "chat_${currentUser?.id}_${user.id}"
                                        viewModel.navigateTo(Screen.ChatDetail(chatId))
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    emoji = user.avatarEmoji,
                                    colorHex = user.avatarColorHex,
                                    sizeDp = 40,
                                    isOnline = user.isOnline
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(user.displayName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(user.statusBio, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ChatItemRow(
    chat: Chat,
    onClick: () -> Unit
) {
    val timeFormatted = remember(chat.lastMessageTime) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(chat.lastMessageTime))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chat_item_${chat.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            emoji = chat.avatarEmoji,
            colorHex = chat.avatarColorHex,
            sizeDp = 50,
            isOnline = true
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chat.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (chat.isSafetyNumberVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified Safety Number",
                            tint = EmeraldLight,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = timeFormatted,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (chat.disappearingDurationSeconds > 0) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Disappearing",
                            tint = SecurityShieldGold,
                            modifier = Modifier
                                .size(13.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    if (chat.isTyping) {
                        Text(
                            text = "${chat.typingUserName ?: "Contact"} is typing...",
                            fontSize = 13.sp,
                            color = EmeraldLight,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = chat.lastMessageText,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
