package com.example.petcare360

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.petcare360.ui.MainScreen
import com.example.petcare360.ui.theme.PetCare360Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetCare360Theme {
                MainScreen()
            }
        }
    }
}
