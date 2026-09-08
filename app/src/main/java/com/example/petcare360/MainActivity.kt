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
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onLoginClick = { email, pass ->
                            errorMessage = null
                            isLoading = true
                            coroutineScope.launch {
                                val result = supabaseClient.signIn(email, pass)
                                isLoading = false
                                result.onSuccess {
                                    currentScreen = AppScreen.MAIN
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "Error al iniciar sesión"
                                }
                            }
                        },
                        onNavigateToRegister = {
                            errorMessage = null
                            currentScreen = AppScreen.REGISTER
                        },
                        onGoogleLoginClick = {
                            // Flujo social rápido por ahora
                            currentScreen = AppScreen.MAIN
                        }
                    )
                }

                AppScreen.REGISTER -> {
                    RegisterScreen(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onRegisterClick = { fullName, email, phone, pass ->
                            errorMessage = null
                            isLoading = true
                            coroutineScope.launch {
                                val result = supabaseClient.signUp(
                                    email = email,
                                    pass = pass,
                                    fullName = fullName,
                                    phone = phone
                                )
                                isLoading = false
                                result.onSuccess {
                                    currentScreen = AppScreen.MAIN
                                }.onFailure { error ->
                                    errorMessage = error.localizedMessage ?: "Error al registrarse"
                                }
                            }
                        },
                        onNavigateToLogin = {
                            errorMessage = null
                            currentScreen = AppScreen.LOGIN
                        },
                        onGoogleRegisterClick = {
                            currentScreen = AppScreen.MAIN
                        }
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


