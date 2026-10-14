package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.UserStatus
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.UserAvatar
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SecurityCyanAccent
import com.example.ui.theme.SecurityDangerRed
import com.example.ui.theme.SecurityShieldGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.ChatList)
    }

    val users by viewModel.repository.users.collectAsState()
    val auditLogs by viewModel.repository.auditLogs.collectAsState()
    val inviteCodes by viewModel.repository.inviteCodes.collectAsState()
    val messagesMap by viewModel.repository.messages.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var generatedCodeDialog by remember { mutableStateOf<String?>(null) }

    val pendingCount = users.count { it.status == UserStatus.PENDING }
    val totalCiphertexts = messagesMap.values.sumOf { it.size }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = SecurityDangerRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SecOps Admin Console", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.ChatList) },
                        modifier = Modifier.testTag("admin_back_button")
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
        ) {
            // Metrics Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Users",
                    value = users.size.toString(),
                    color = EmeraldPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Pending",
                    value = pendingCount.toString(),
                    color = if (pendingCount > 0) SecurityShieldGold else Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Ciphertexts",
                    value = totalCiphertexts.toString(),
                    color = SecurityCyanAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            // Zero-Knowledge Administrator Warning Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1412)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = SecurityDangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Zero-Knowledge Protection Active: Private keys are held exclusively on user client devices. Administrators cannot inspect plaintext messages.",
                        fontSize = 11.sp,
                        color = Color(0xFFFCA5A5),
                        lineHeight = 15.sp
                    )
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Users & Approvals", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Invite Codes", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Audit Logs", fontSize = 12.sp) }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Users and pending approvals
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(users, key = { it.id }) { user ->
                            UserManagementCard(
                                user = user,
                                onApprove = { viewModel.repository.approveUser(user.id) },
                                onBlock = { viewModel.repository.blockUserAdmin(user.id) }
                            )
                        }
                    }
                }

                1 -> {
                    // Invite Codes
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                val newCode = viewModel.repository.generateInviteCode()
                                generatedCodeDialog = newCode
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_invite_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Single-Use Cryptographic Invite Token")
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(inviteCodes, key = { it.code }) { invite ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.VpnKey,
                                                contentDescription = null,
                                                tint = if (invite.isUsed) Color.Gray else EmeraldLight,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = invite.code,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = if (invite.isUsed) "Redeemed by ${invite.usedBy}" else "Active & Valid (7 days)",
                                                    fontSize = 11.sp,
                                                    color = if (invite.isUsed) Color.Gray else EmeraldLight
                                                )
                                            }
                                        }

                                        Surface(
                                            color = if (invite.isUsed) Color.Gray.copy(alpha = 0.2f) else EmeraldPrimary.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (invite.isUsed) "REDEEMED" else "VALID",
                                                color = if (invite.isUsed) Color.Gray else EmeraldLight,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Security Audit Logs
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(auditLogs, key = { it.id }) { log ->
                            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val badgeColor = when (log.severity) {
                                                "WARN" -> SecurityShieldGold
                                                "CRITICAL" -> SecurityDangerRed
                                                else -> EmeraldLight
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(badgeColor)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = log.eventType,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor
                                            )
                                        }
                                        Text(timeStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = log.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "IP: ${log.clientIp} • User: ${log.userIdentifier}",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Generated Code Dialog
    generatedCodeDialog?.let { code ->
        AlertDialog(
            onDismissRequest = { generatedCodeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = EmeraldLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Cryptographic Invite Code", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Share this single-use code with an authorized user to permit registration into SecureChat:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = code,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = EmeraldLight,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { generatedCodeDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun UserManagementCard(
    user: com.example.data.model.User,
    onApprove: () -> Unit,
    onBlock: () -> Unit
) {
    val fingerprint = remember(user.publicKeyBase64) {
        CryptoEngine.computeKeyFingerprint(user.publicKeyBase64)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(
                        emoji = user.avatarEmoji,
                        colorHex = user.avatarColorHex,
                        sizeDp = 40,
                        isOnline = user.isOnline
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (user.role == UserRole.ADMIN) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("• Admin", fontSize = 10.sp, color = SecurityDangerRed, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(user.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Status chip
                val (statusText, statusBg, statusColor) = when (user.status) {
                    UserStatus.APPROVED -> Triple("APPROVED", EmeraldPrimary.copy(alpha = 0.15f), EmeraldLight)
                    UserStatus.PENDING -> Triple("PENDING", SecurityShieldGold.copy(alpha = 0.15f), SecurityShieldGold)
                    UserStatus.BLOCKED -> Triple("BLOCKED", SecurityDangerRed.copy(alpha = 0.15f), SecurityDangerRed)
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Public Key: $fingerprint",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (user.status == UserStatus.PENDING) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onBlock,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SecurityDangerRed),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Reject", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.height(34.dp).testTag("approve_user_${user.id}")
                    ) {
                        Text("Approve User", fontSize = 11.sp)
                    }
                }
            } else if (user.role != UserRole.ADMIN) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (user.status == UserStatus.APPROVED) {
                        TextButton(onClick = onBlock) {
                            Text("Block Access", color = SecurityDangerRed, fontSize = 11.sp)
                        }
                    } else {
                        TextButton(onClick = onApprove) {
                            Text("Unblock", color = EmeraldPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
