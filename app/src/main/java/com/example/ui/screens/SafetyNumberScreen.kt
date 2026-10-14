package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.CryptoEngine
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.UserAvatar
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SecurityCyanAccent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SafetyNumberScreen(
    chatId: String,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.ChatDetail(chatId))
    }

    val currentUser by viewModel.repository.currentUser.collectAsState()
    val allUsers by viewModel.repository.users.collectAsState()
    val chats by viewModel.repository.chats.collectAsState()

    val chat = chats.find { it.id == chatId }
    val peerId = chat?.participantIds?.find { it != currentUser?.id } ?: ""
    val peerUser = allUsers.find { it.id == peerId }

    val safetyNumber = remember(currentUser?.publicKeyBase64, peerUser?.publicKeyBase64) {
        if (currentUser != null && peerUser != null) {
            CryptoEngine.computeSafetyNumber(currentUser!!.publicKeyBase64, peerUser!!.publicKeyBase64)
        } else {
            "38104 92841 02948 10928 47102 94810 29384 10294 81029 38410 29481 02938"
        }
    }

    val myFingerprint = remember(currentUser?.publicKeyBase64) {
        currentUser?.let { CryptoEngine.computeKeyFingerprint(it.publicKeyBase64) } ?: "E2EE-MY-KEY"
    }

    val peerFingerprint = remember(peerUser?.publicKeyBase64) {
        peerUser?.let { CryptoEngine.computeKeyFingerprint(it.publicKeyBase64) } ?: "E2EE-PEER-KEY"
    }

    val isVerified = chat?.isSafetyNumberVerified == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Safety Number", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.ChatDetail(chatId)) },
                        modifier = Modifier.testTag("safety_back_button")
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatars pair
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                UserAvatar(
                    emoji = currentUser?.avatarEmoji ?: "🛡️",
                    colorHex = currentUser?.avatarColorHex ?: 0xFF00A884,
                    sizeDp = 44
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = EmeraldLight,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                UserAvatar(
                    emoji = peerUser?.avatarEmoji ?: "👤",
                    colorHex = peerUser?.avatarColorHex ?: 0xFF0284C7,
                    sizeDp = 44
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "End-to-End Encryption Security Code",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "To verify that your communications with ${peerUser?.displayName ?: "this contact"} are end-to-end encrypted without third-party interception, compare this 60-digit number with the number on their device or scan their QR code.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Simulated High-Tech QR Code Graphic
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(190.dp)
                    .border(2.dp, if (isVerified) EmeraldPrimary else Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "Cryptographic QR Code",
                        tint = Color.Black,
                        modifier = Modifier.size(170.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 60-digit safety numbers (12 blocks of 5 digits)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val blocks = safetyNumber.split(" ")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        maxItemsInEachRow = 3
                    ) {
                        blocks.forEach { block ->
                            Text(
                                text = block,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isVerified) EmeraldLight else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Individual key fingerprints
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Your Key Fingerprint:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(myFingerprint, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = SecurityCyanAccent, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${peerUser?.displayName?.split(" ")?.first() ?: "Contact"}'s Fingerprint:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(peerFingerprint, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = EmeraldLight, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Verification Action Button
            Button(
                onClick = {
                    viewModel.repository.setSafetyNumberVerified(chatId, !isVerified)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVerified) Color(0xFF166534) else EmeraldPrimary
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("toggle_verified_button")
            ) {
                Icon(
                    imageVector = if (isVerified) Icons.Default.VerifiedUser else Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isVerified) "Verified (Tap to revoke)" else "Mark as Verified",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
