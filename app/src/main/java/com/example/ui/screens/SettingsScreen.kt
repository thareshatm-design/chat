package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.CryptoEngine
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.UserAvatar
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SecurityCyanAccent
import com.example.ui.theme.SecurityDangerRed
import com.example.ui.theme.SecurityShieldGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.ChatList)
    }

    val currentUser by viewModel.repository.currentUser.collectAsState()
    val allUsers by viewModel.repository.users.collectAsState()
    val sessions by viewModel.repository.sessions.collectAsState()
    val blockedUserIds by viewModel.repository.blockedUserIds.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var showSessionsDialog by remember { mutableStateOf(false) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var showUserSwitchDialog by remember { mutableStateOf(false) }

    val userFingerprint = remember(currentUser?.publicKeyBase64) {
        currentUser?.let { CryptoEngine.computeKeyFingerprint(it.publicKeyBase64) } ?: "E2EE-KEY"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security Settings", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.ChatList) },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Profile Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        emoji = currentUser?.avatarEmoji ?: "🛡️",
                        colorHex = currentUser?.avatarColorHex ?: 0xFF00A884,
                        sizeDp = 56,
                        isOnline = true
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.displayName ?: "User",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            if (currentUser?.role == UserRole.ADMIN) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = SecurityDangerRed.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "ADMIN",
                                        color = SecurityDangerRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = currentUser?.email ?: "",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ECDH: $userFingerprint",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = EmeraldLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Switch Active Identity Button (for instant testing in the emulator)
            OutlinedButton(
                onClick = { showUserSwitchDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("switch_identity_button")
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Switch Active Identity (Test Dual Client)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AUTHENTICATION & ACCESS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldLight,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // 2FA Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Two-Factor Authentication (2FA)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Require 6-digit TOTP verification on login", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = currentUser?.isTwoFactorEnabled == true,
                            onCheckedChange = { viewModel.repository.toggleTwoFactor(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

                    // App Lock / Biometric Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = SecurityShieldGold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Biometric & PIN Vault Lock", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Lock messenger after 1 minute of inactivity", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = currentUser?.isBiometricEnabled == true,
                            onCheckedChange = { viewModel.repository.toggleBiometric(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SecurityShieldGold)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

                    // Active Sessions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSessionsDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = SecurityCyanAccent)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Active Devices & Sessions", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${sessions.size} authenticated clients connected", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text("Manage", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PRIVACY & ENCRYPTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldLight,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Blocked Contacts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBlockedDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = SecurityDangerRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Blocked Contacts", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${blockedUserIds.size} users blocked from messaging", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text("View", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

                    // Theme selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleDarkTheme() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Theme Appearance", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(if (isDarkTheme) "High-Security Dark (Active)" else "Clean Light Mode", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text("Toggle", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = { viewModel.navigateTo(Screen.AuthLogin) },
                colors = ButtonDefaults.buttonColors(containerColor = SecurityDangerRed.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("logout_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out", tint = SecurityDangerRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out from SecureChat", color = SecurityDangerRed, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Sessions Dialog
    if (showSessionsDialog) {
        AlertDialog(
            onDismissRequest = { showSessionsDialog = false },
            title = { Text("Active Client Sessions", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Authorized cryptographic tokens active on SecureChat:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    sessions.forEach { sess ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (sess.isCurrentDevice) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(sess.deviceName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (sess.isCurrentDevice) {
                                        Text("THIS DEVICE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                                    }
                                }
                                Text("IP: ${sess.ipAddress} • ${sess.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Status: ${sess.lastActive}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.repository.revokeOtherSessions()
                            showSessionsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecurityDangerRed),
                        modifier = Modifier.fillMaxWidth().testTag("terminate_sessions_button")
                    ) {
                        Text("Terminate All Other Sessions")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSessionsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Blocked Users Dialog
    if (showBlockedDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedDialog = false },
            title = { Text("Blocked Contacts", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (blockedUserIds.isEmpty()) {
                        Text("No contacts are currently blocked.")
                    } else {
                        blockedUserIds.forEach { uid ->
                            val user = allUsers.find { it.id == uid }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(user?.displayName ?: uid, fontSize = 14.sp)
                                TextButton(onClick = { viewModel.repository.toggleBlockUser(uid) }) {
                                    Text("Unblock", color = EmeraldPrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBlockedDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Switch User Dialog
    if (showUserSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showUserSwitchDialog = false },
            title = { Text("Select Identity to Test", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    allUsers.forEach { user ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (user.id == currentUser?.id) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.repository.switchActiveUser(user.id)
                                    showUserSwitchDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    emoji = user.avatarEmoji,
                                    colorHex = user.avatarColorHex,
                                    sizeDp = 36,
                                    isOnline = user.isOnline
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = user.displayName + (if (user.id == currentUser?.id) " (Current)" else ""),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (user.role == UserRole.ADMIN) "Administrator" else "Verified User",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserSwitchDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
