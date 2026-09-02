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

/**
 * Sobrecarga Stateful de PerdidosScreen.
 */
@Composable
fun PerdidosScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(LostPetType.PERDIDO) }
    var searchQuery by remember { mutableStateOf("") }

    PerdidosScreen(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        modifier = modifier
    )
}

/**
 * Sobrecarga Stateless de PerdidosScreen equivalente a la versión de React.
 */
@Composable
fun PerdidosScreen(
    selectedTab: LostPetType,
    onTabSelected: (LostPetType) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    pets: List<LostPet> = SAMPLE_LOST_PETS,
    onAddPetClick: () -> Unit = {},
    onFullMapClick: () -> Unit = {},
    onContactClick: (LostPet) -> Unit = {},
    onShareClick: (LostPet) -> Unit = {}
) {
    val filteredPets = remember(selectedTab, searchQuery, pets) {
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Encabezado y Barra de Búsqueda
        HeaderSection(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onAddPetClick = onAddPetClick
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

        // 4. Lista de Tarjetas de Mascotas
        PetCardsList(
            pets = filteredPets,
            onContactClick = onContactClick,
            onShareClick = onShareClick
        )
    }
}

@Composable
private fun HeaderSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddPetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mascotas Perdidas",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8703A))
                    .clickable { onAddPetClick() },
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

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de Búsqueda
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Buscar",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Buscar por nombre, raza, zona...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
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
    onContactClick: (LostPet) -> Unit,
    onShareClick: (LostPet) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        pets.forEach { pet ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AsyncImage(
                            model = pet.img,
                            contentDescription = pet.name,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pet.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = pet.breed,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val isPerdido = pet.type == LostPetType.PERDIDO
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (isPerdido) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isPerdido) "Perdido" else "Encontrado",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isPerdido) Color(0xFFB91C1C) else Color(0xFF15803D)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = pet.location,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = pet.daysAgo,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (pet.reward != null) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.10f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Recompensa: ${pet.reward}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Acciones Rápidas (Contactar / Compartir)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onContactClick(pet) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE8703A),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text(
                                text = "Contactar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { onShareClick(pet) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text(
                                text = "Compartir",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
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
