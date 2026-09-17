package com.example.petcare360

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.petcare360.data.remote.CloudinaryManager
import com.example.petcare360.data.remote.SessionManager
import com.example.petcare360.data.remote.SupabaseClient
import com.example.petcare360.ui.LoginScreen
import com.example.petcare360.ui.MainScreen
import com.example.petcare360.ui.RegisterScreen
import com.example.petcare360.ui.theme.PetCare360Theme
import kotlinx.coroutines.launch

enum class AppScreen {
    LOGIN,
    REGISTER,
    MAIN
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetCare360Theme {
                PetCareApp()
            }
        }
    }
}

@Composable
fun PetCareApp() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val supabaseClient = remember { SupabaseClient(sessionManager) }
    val cloudinaryManager = remember { CloudinaryManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val initialScreen = if (sessionManager.isLoggedIn()) AppScreen.MAIN else AppScreen.LOGIN
    var currentScreen by remember { mutableStateOf(initialScreen) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                AppScreen.LOGIN -> {
                    LoginScreen(
                        supabaseClient = supabaseClient,
                        onLoginSuccess = { currentScreen = AppScreen.MAIN },
                        onNavigateToRegister = { currentScreen = AppScreen.REGISTER },
                        onGoogleLoginClick = { currentScreen = AppScreen.MAIN }
                    )
                }

                AppScreen.REGISTER -> {
                    RegisterScreen(
                        supabaseClient = supabaseClient,
                        onRegisterSuccess = { currentScreen = AppScreen.MAIN },
                        onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                        onGoogleRegisterClick = { currentScreen = AppScreen.MAIN }
                    )
                }

                AppScreen.MAIN -> {
                    MainScreen(
                        userName = sessionManager.getUserName() ?: "Alejandro",
                        userEmail = sessionManager.getUserEmail() ?: "acajomejia@gmail.com",
                        userPhone = "+51 991019411",
                        cloudinaryManager = cloudinaryManager,
                        supabaseClient = supabaseClient,
                        onLogout = {
                            supabaseClient.signOut()
                            currentScreen = AppScreen.LOGIN
                        }
                    )
                }
            }
        }
    }
}


