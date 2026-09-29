package com.example.petcare360.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare360.ui.theme.PetCare360Theme

enum class NavTab(
    val title: String,
    val icon: ImageVector
) {
    INICIO("Inicio", Icons.Outlined.Home),
    SALUD("Salud", Icons.AutoMirrored.Outlined.ShowChart),
    PERDIDOS("Perdidos", Icons.Outlined.LocationOn),
    COMUNIDAD("Comunidad", Icons.Outlined.Groups),
    SERVICIOS("Servicios", Icons.Outlined.ContentCut),
    PERFIL("Perfil", Icons.Outlined.Person)
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    userName: String = "Alejandro",
    userEmail: String = "usuario@correo.com",
    userPhone: String? = null,
    userAvatar: String? = null,
    cloudinaryManager: com.example.petcare360.data.remote.CloudinaryManager? = null,
    supabaseClient: com.example.petcare360.data.remote.SupabaseClient? = null,
    onAvatarUpdated: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var currentTab by remember { mutableStateOf(NavTab.INICIO) }
    var petsList by remember(userName) { mutableStateOf<List<com.example.petcare360.data.model.PetEntity>>(emptyList()) }
    var currentUserAvatar by remember(userAvatar) { mutableStateOf(userAvatar) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    // Cargar mascotas del usuario desde Supabase al iniciar
    androidx.compose.runtime.LaunchedEffect(userName) {
        if (supabaseClient != null) {
            val result = supabaseClient.getPets()
            result.onSuccess { pets ->
                petsList = pets
            }
        }
    }

    Scaffold(
        bottomBar = {
            PetCareBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.INICIO -> HomeScreen(
                    userName = userName,
                    pets = petsList,
                    cloudinaryManager = cloudinaryManager,
                    supabaseClient = supabaseClient,
                    onAddPet = { newPet ->
                        petsList = listOf(newPet) + petsList
                    },
                    onLogoutClick = onLogout
                )
                NavTab.SALUD -> SaludScreen(
                    supabaseClient = supabaseClient,
                    userPets = petsList
                )
                NavTab.PERDIDOS -> PerdidosScreen()
                NavTab.COMUNIDAD -> ComunidadScreen(
                    cloudinaryManager = cloudinaryManager,
                    supabaseClient = supabaseClient,
                    userName = userName,
                    userAvatar = currentUserAvatar,
                    userPets = petsList
                )
                NavTab.SERVICIOS -> ServiciosScreen(
                    supabaseClient = supabaseClient,
                    userPets = petsList
                )
                NavTab.PERFIL -> {
                    if (cloudinaryManager != null) {
                        ProfileScreen(
                            userName = userName,
                            userEmail = userEmail,
                            userPhone = userPhone,
                            userAvatar = currentUserAvatar,
                            pets = petsList,
                            cloudinaryManager = cloudinaryManager,
                            supabaseClient = supabaseClient,
                            onAddPet = { newPet ->
                                petsList = listOf(newPet) + petsList
                            },
                            onAvatarUpdated = { newUrl ->
                                currentUserAvatar = newUrl
                                onAvatarUpdated(newUrl)
                            },
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PetCareBottomBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                val tint = if (isSelected) Color(0xFFE8703A) else Color(0xFF9CA3AF)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(20.dp),
                        tint = tint
                    )
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = tint
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PetCare360Theme {
        MainScreen()
    }
}
