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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.petcare360.ui.LoginScreen
import com.example.petcare360.ui.MainScreen
import com.example.petcare360.ui.RegisterScreen
import com.example.petcare360.ui.theme.PetCare360Theme

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
    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                AppScreen.LOGIN -> {
                    LoginScreen(
                        onLoginClick = { _, _ ->
                            currentScreen = AppScreen.MAIN
                        },
                        onNavigateToRegister = {
                            currentScreen = AppScreen.REGISTER
                        },
                        onGoogleLoginClick = {
                            currentScreen = AppScreen.MAIN
                        }
                    )
                }

                AppScreen.REGISTER -> {
                    RegisterScreen(
                        onRegisterClick = { _, _, _, _ ->
                            currentScreen = AppScreen.MAIN
                        },
                        onNavigateToLogin = {
                            currentScreen = AppScreen.LOGIN
                        },
                        onGoogleRegisterClick = {
                            currentScreen = AppScreen.MAIN
                        }
                    )
                }

                AppScreen.MAIN -> {
                    MainScreen(
                        onLogout = {
                            currentScreen = AppScreen.LOGIN
                        }
                    )
                }
            }
        }
    }
}

