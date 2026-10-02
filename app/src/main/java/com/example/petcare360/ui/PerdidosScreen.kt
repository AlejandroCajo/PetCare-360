package com.example.petcare360.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.petcare360.ui.theme.PetCare360Theme
import com.example.petcare360.ui.components.PetCareTopBar
import com.example.petcare360.ui.components.PetCareSearchBar
import com.example.petcare360.ui.components.LostPetCard
import androidx.compose.runtime.collectAsState
import com.example.petcare360.data.remote.SupabaseClient
import com.example.petcare360.ui.viewmodels.PerdidosViewModel
import com.example.petcare360.data.model.SosAlertEntity
import com.example.petcare360.data.model.PetEntity
import com.example.petcare360.ui.components.CreateSosAlertDialog
import com.example.petcare360.ui.components.CreateSightingDialog
import androidx.compose.material3.CircularProgressIndicator
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

enum class LostPetType {
    PERDIDO, ENCONTRADO
}

data class LostPet(
    val id: String,
    val name: String,
    val breed: String,
    val img: String,
    val location: String,
    val daysAgo: String,
    val type: LostPetType,
    val reward: String? = null
)

val SAMPLE_LOST_PETS = listOf(
    LostPet(
        id = "1",
        name = "Max",
        breed = "Beagle · Macho",
        img = "https://images.unsplash.com/photo-1505628346881-b72b27e84530?w=300&h=200&fit=crop",
        location = "Parque del Este, Sector 4",
        daysAgo = "Perdido hace 2 días",
        type = LostPetType.PERDIDO,
        reward = "$150 USD"
    ),
    LostPet(
        id = "2",
        name = "Coco",
        breed = "Poodle Toy · Hembra",
        img = "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=300&h=200&fit=crop",
        location = "Av. Las Flores, cerca de la plaza",
        daysAgo = "Perdido ayer",
        type = LostPetType.PERDIDO
    ),
    LostPet(
        id = "3",
        name = "Gato Naranja sin collar",
        breed = "Mestizo · Macho",
        img = "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=300&h=200&fit=crop",
        location = "Urbanización Altamira",
        daysAgo = "Encontrado hoy a las 8:00 am",
        type = LostPetType.ENCONTRADO
    )
)

@Composable
fun PerdidosScreen(
    modifier: Modifier = Modifier,
    supabaseClient: SupabaseClient? = null,
    userPets: List<PetEntity> = emptyList(),
    onAddPetClick: () -> Unit = {},
    onFullMapClick: () -> Unit = {},
    onContactClick: (LostPet) -> Unit = {},
    onShareClick: (LostPet) -> Unit = {}
) {
    val viewModel = remember(supabaseClient) { PerdidosViewModel(supabaseClient) }
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(LostPetType.PERDIDO) }

    val onSearchQueryChange = { query: String -> searchQuery = query }
    val onTabSelected = { tab: LostPetType -> selectedTab = tab }

    // Transformar SosAlertEntity a LostPet
    val pets = uiState.alerts.map { alert ->
        LostPet(
            id = alert.id ?: "",
            name = alert.petDetails?.name ?: "Desconocido",
            breed = alert.petDetails?.breed ?: "Mascota",
            img = alert.photoUrl ?: alert.petDetails?.avatarUrl ?: "https://images.unsplash.com/photo-1524661135-423995f22d0b?w=300&h=200&fit=crop",
            location = alert.lastSeenLocation ?: "Ubicación desconocida",
            daysAgo = alert.lostDate ?: "Fecha desconocida",
            type = if (alert.status == "resolved") LostPetType.ENCONTRADO else LostPetType.PERDIDO,
            reward = alert.description
        )
    }

    val filteredPets = remember(pets, searchQuery, selectedTab) {
        pets.filter { pet ->
            val matchesTab = pet.type == selectedTab
            val matchesSearch = searchQuery.isBlank() ||
                pet.name.contains(searchQuery, ignoreCase = true) ||
                pet.breed.contains(searchQuery, ignoreCase = true) ||
                pet.location.contains(searchQuery, ignoreCase = true)
            matchesTab && matchesSearch
        }
    }

    val lostCount = remember(pets) { pets.count { it.type == LostPetType.PERDIDO } }
    val foundCount = remember(pets) { pets.count { it.type == LostPetType.ENCONTRADO } }

    var showCreateDialog by remember { mutableStateOf(false) }
    var sightingPet by remember { mutableStateOf<LostPet?>(null) }
    val context = LocalContext.current

    if (showCreateDialog) {
        CreateSosAlertDialog(
            userPets = userPets,
            onDismiss = { showCreateDialog = false },
            onSubmit = { alert ->
                viewModel.createSosAlert(alert) {
                    showCreateDialog = false
                    Toast.makeText(context, "Alerta publicada exitosamente", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (sightingPet != null) {
        CreateSightingDialog(
            petName = sightingPet!!.name,
            onDismiss = { sightingPet = null },
            onSubmit = { location, notes ->
                viewModel.createSighting(sightingPet!!.id, location, notes) {
                    sightingPet = null
                    Toast.makeText(context, "Avistamiento registrado. ¡Gracias!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Encabezado y Barra de Búsqueda
        PetCareTopBar(
            subtitle = "EMERGENCIAS",
            title = "Mascotas Perdidas",
            actions = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8703A))
                        .clickable { showCreateDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar reporte",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                }
            }
        )
        PetCareSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = "Buscar por nombre, raza, zona...",
            modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp)
        )

        // 2. Tarjeta del Mini Mapa
        MiniMapCard(
            onFullMapClick = onFullMapClick
        )

        // 3. Segmented Control / Pestañas (Perdidos vs Encontrados)
        TabsSection(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            lostCount = lostCount,
            foundCount = foundCount
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE8703A))
            }
        } else {
            // 4. Lista de Tarjetas de Mascotas
            PetCardsList(
                pets = filteredPets,
                currentUserId = uiState.currentUserId,
                originalAlerts = uiState.alerts,
                onContactClick = onContactClick,
                onShareClick = onShareClick,
                onMarkAsFoundClick = { pet -> viewModel.markAsFound(pet.id) },
                onSightingClick = { pet -> sightingPet = pet }
            )
        }
    }
}


@Composable
private fun MiniMapCard(
    onFullMapClick: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .height(128.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onFullMapClick() }
    ) {
        val width = maxWidth
        val height = maxHeight

        AsyncImage(
            model = "https://images.unsplash.com/photo-1524661135-423995f22d0b?w=600&h=300&fit=crop&auto=format",
            contentDescription = "Vista aérea del mapa",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.6f),
            contentScale = ContentScale.Crop
        )

        // Marcadores de mapa
        Box(
            modifier = Modifier
                .offset(x = width * 0.38f, y = height * 0.15f)
                .size(16.dp)
                .border(2.dp, Color.White, CircleShape)
                .background(Color(0xFFEF4444), CircleShape)
        )

        Box(
            modifier = Modifier
                .offset(x = width * 0.70f, y = height * 0.30f)
                .size(16.dp)
                .border(2.dp, Color.White, CircleShape)
                .background(Color(0xFFEF4444), CircleShape)
        )

        Box(
            modifier = Modifier
                .offset(x = width * 0.52f, y = height * 0.65f)
                .size(16.dp)
                .border(2.dp, Color.White, CircleShape)
                .background(Color(0xFF22C55E), CircleShape)
        )

        // Botón flotante central
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.92f),
            shadowElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFE8703A)
                )
                Text(
                    text = "Ver mapa completo",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
            }
        }
    }
}

@Composable
private fun TabsSection(
    selectedTab: LostPetType,
    onTabSelected: (LostPetType) -> Unit,
    lostCount: Int,
    foundCount: Int
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(4.dp)
        ) {
            val isPerdidoSelected = selectedTab == LostPetType.PERDIDO
            val isEncontradoSelected = selectedTab == LostPetType.ENCONTRADO

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isPerdidoSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                    )
                    .clickable { onTabSelected(LostPetType.PERDIDO) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Perdidos ($lostCount)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPerdidoSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isEncontradoSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                    )
                    .clickable { onTabSelected(LostPetType.ENCONTRADO) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Encontrados ($foundCount)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEncontradoSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PetCardsList(
    pets: List<LostPet>,
    currentUserId: String?,
    originalAlerts: List<SosAlertEntity>,
    onContactClick: (LostPet) -> Unit,
    onShareClick: (LostPet) -> Unit,
    onMarkAsFoundClick: (LostPet) -> Unit,
    onSightingClick: (LostPet) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        pets.forEach { pet ->
            val alert = originalAlerts.find { it.id == pet.id }
            val isOwner = alert?.userId != null && alert.userId == currentUserId
            LostPetCard(
                pet = pet,
                isOwner = isOwner,
                onContactClick = onContactClick,
                onShareClick = onShareClick,
                onMarkAsFoundClick = onMarkAsFoundClick,
                onSightingClick = onSightingClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerdidosScreenPreview() {
    PetCare360Theme {
        PerdidosScreen()
    }
}
