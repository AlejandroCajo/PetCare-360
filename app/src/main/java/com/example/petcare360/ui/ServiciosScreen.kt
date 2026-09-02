package com.example.petcare360.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.petcare360.ui.theme.PetCare360Theme

data class PetService(
    val id: String,
    val name: String,
    val type: String,
    val rating: Double,
    val reviews: Int,
    val dist: String,
    val price: String,
    val available: Boolean,
    val img: String,
    val tag: String? = null
)

val SERVICE_CATS = listOf("Todos", "Veterinaria", "Peluquería", "Paseador", "Guardería")

val SAMPLE_SERVICES = listOf(
    PetService(
        id = "1",
        name = "Clínica Veterinaria VetCare",
        type = "Veterinaria",
        rating = 4.9,
        reviews = 128,
        dist = "1.2 km",
        price = "Consulta $25",
        available = true,
        img = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=200&h=200&fit=crop&auto=format",
        tag = "TOP"
    ),
    PetService(
        id = "2",
        name = "Spa & Peluquería Canina HappyPaws",
        type = "Peluquería",
        rating = 4.8,
        reviews = 95,
        dist = "2.5 km",
        price = "Baño y corte $30",
        available = true,
        img = "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=200&h=200&fit=crop&auto=format"
    ),
    PetService(
        id = "3",
        name = "Paseos Felices con Mateo",
        type = "Paseador",
        rating = 4.7,
        reviews = 42,
        dist = "0.8 km",
        price = "1 hora $15",
        available = false,
        img = "https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=200&h=200&fit=crop&auto=format"
    ),
    PetService(
        id = "4",
        name = "Hotel & Guardería Pet Paradise",
        type = "Guardería",
        rating = 4.9,
        reviews = 76,
        dist = "3.8 km",
        price = "Día completo $40",
        available = true,
        img = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=200&h=200&fit=crop&auto=format",
        tag = "PROMO"
    )
)

/**
 * Sobrecarga Stateful de ServiciosScreen.
 */
@Composable
fun ServiciosScreen(
    modifier: Modifier = Modifier
) {
    var activeCat by remember { mutableStateOf("Todos") }
    var searchQuery by remember { mutableStateOf("") }

    ServiciosScreen(
        activeCat = activeCat,
        onActiveCatChange = { activeCat = it },
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        modifier = modifier
    )
}

/**
 * Sobrecarga Stateless de ServiciosScreen equivalente a la versión de React.
 */
@Composable
fun ServiciosScreen(
    activeCat: String,
    onActiveCatChange: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    categories: List<String> = SERVICE_CATS,
    services: List<PetService> = SAMPLE_SERVICES,
    onFilterClick: () -> Unit = {},
    onBookPromoClick: () -> Unit = {},
    onBookServiceClick: (PetService) -> Unit = {}
) {
    val filteredServices = remember(activeCat, searchQuery, services) {
        services.filter { svc ->
            val matchesCat = if (activeCat == "Todos") true else svc.type == activeCat
            val matchesSearch = searchQuery.isBlank() ||
                svc.name.contains(searchQuery, ignoreCase = true) ||
                svc.type.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Encabezado (Título + Barra de Búsqueda y Filtro)
        HeaderSection(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onFilterClick = onFilterClick
        )

        // 2. Chips de Categoría
        CategoryPillsSection(
            categories = categories,
            activeCat = activeCat,
            onActiveCatChange = onActiveCatChange
        )

        // 3. Banner Promocional
        PromoBannerCard(
            onBookPromoClick = onBookPromoClick
        )

        // 4. Lista de Servicios ("Cerca de ti")
        ServiceListSection(
            services = filteredServices,
            onBookServiceClick = onBookServiceClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HeaderSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 16.dp)
    ) {
        Text(
            text = "Servicios",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra de Búsqueda
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1f)
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
                                text = "Buscar veterinarios, peluquerías...",
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

            // Botón de Filtro
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                    .clickable { onFilterClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = "Filtros",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryPillsSection(
    categories: List<String>,
    activeCat: String,
    onActiveCatChange: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        items(categories) { cat ->
            val isActive = activeCat == cat
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isActive) Color(0xFFE8703A) else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onActiveCatChange(cat) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = cat,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PromoBannerCard(
    onBookPromoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1628009368231-7bb7cfcb0def?w=600&h=300&fit=crop&auto=format",
            contentDescription = "Veterinaria con perro",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Degradado de sombra oscuro a la izquierda
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.70f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Contenido del Banner
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "OFERTA ESPECIAL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = "Primera consulta gratis",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onBookPromoClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F2937)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Text(
                    text = "Reservar ahora",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ServiceListSection(
    services: List<PetService>,
    onBookServiceClick: (PetService) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Cerca de ti",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (services.isEmpty()) {
            Text(
                text = "No hay servicios disponibles en esta categoría.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        } else {
            services.forEach { svc ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Contenedor de la Imagen con Tag Opcional
                            Box(modifier = Modifier.size(64.dp)) {
                                AsyncImage(
                                    model = svc.img,
                                    contentDescription = svc.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )

                                if (svc.tag != null) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFF59E0B))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = svc.tag,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Detalles del Servicio
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = svc.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = svc.type,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFE8703A)
                                        )
                                    }

                                    // Rating
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = Color(0xFFF59E0B)
                                        )
                                        Text(
                                            text = "${svc.rating}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "(${svc.reviews})",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = svc.dist,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = svc.price,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Estado de disponibilidad
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                if (svc.available) Color(0xFF3D9E6B) else Color(0xFF9CA3AF),
                                                CircleShape
                                            )
                                    )
                                    Text(
                                        text = if (svc.available) "Disponible hoy" else "Sin disponibilidad",
                                        fontSize = 12.sp,
                                        color = if (svc.available) Color(0xFF166534) else Color(0xFF6B7280)
                                    )
                                }
                            }
                        }

                        // Botón de Reserva
                        Button(
                            onClick = { onBookServiceClick(svc) },
                            enabled = svc.available,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE8703A),
                                contentColor = Color.White,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(vertical = 10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .height(40.dp)
                        ) {
                            Text(
                                text = if (svc.available) "Reservar cita" else "Sin disponibilidad",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
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
fun ServiciosScreenPreview() {
    PetCare360Theme {
        ServiciosScreen()
    }
}
