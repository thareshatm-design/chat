package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AppLockVaultOverlay
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.SafetyNumberScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TwoFactorScreen
import com.example.ui.theme.SecureChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
            val isAppLocked by mainViewModel.repository.isAppLocked.collectAsState()

            SecureChatTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (isAppLocked) {
                        AppLockVaultOverlay(
                            onUnlock = { pin -> mainViewModel.repository.unlockApp(pin) }
                        )
                    } else {
                        SecureChatNavigation(viewModel = mainViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun SecureChatNavigation(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    when (val screen = currentScreen) {
        is Screen.ChatList -> {
            ChatListScreen(viewModel = viewModel)
        }
        is Screen.ChatDetail -> {
            ChatDetailScreen(chatId = screen.chatId, viewModel = viewModel)
        }
        is Screen.SafetyNumber -> {
            SafetyNumberScreen(chatId = screen.chatId, viewModel = viewModel)
        }
        is Screen.Settings -> {
            SettingsScreen(viewModel = viewModel)
        }
        is Screen.AdminPanel -> {
            AdminScreen(viewModel = viewModel)
        }
        is Screen.AuthLogin -> {
            LoginScreen(viewModel = viewModel)
        }
        is Screen.AuthRegister -> {
            RegisterScreen(viewModel = viewModel)
        }
        is Screen.Auth2FA -> {
            TwoFactorScreen(email = screen.email, viewModel = viewModel)
        }
    }
}
