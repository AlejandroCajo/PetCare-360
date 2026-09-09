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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.petcare360.data.model.PetEntity
import com.example.petcare360.ui.theme.PetCare360Theme

private data class QuickActionItem(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userName: String = "Alejandro",
    pets: List<PetEntity> = emptyList(),
    cloudinaryManager: com.example.petcare360.data.remote.CloudinaryManager? = null,
    supabaseClient: com.example.petcare360.data.remote.SupabaseClient? = null,
    onAddPet: (PetEntity) -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var activePet by remember { mutableIntStateOf(0) }
    var showAddPetDialog by remember { mutableStateOf(false) }

    if (showAddPetDialog && cloudinaryManager != null) {
        com.example.petcare360.ui.components.AddPetDialog(
            cloudinaryManager = cloudinaryManager,
            supabaseClient = supabaseClient,
            onDismiss = { showAddPetDialog = false },
            onPetCreated = { newPet ->
                showAddPetDialog = false
                onAddPet(newPet)
            }
        )
    }

    HomeScreen(
        activePet = activePet,
        setActivePet = { activePet = it },
        userName = userName,
        pets = pets,
        modifier = modifier,
        onAddPetClick = { showAddPetDialog = true },
        onLogoutClick = onLogoutClick
    )
}

@Composable
fun HomeScreen(
    activePet: Int,
    setActivePet: (Int) -> Unit,
    modifier: Modifier = Modifier,
    userName: String = "María García",
    pets: List<PetEntity> = emptyList(),
    onNotificationClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onAddPetClick: () -> Unit = {},
    onSeeAllAppointmentsClick: () -> Unit = {},
    onAppointmentClick: () -> Unit = {},
    onQuickActionClick: (String) -> Unit = {},
    onReadArticleClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Header (Greeting + Actions)
        GreetingHeader(
            userName = userName,
            onNotificationClick = onNotificationClick,
            onLogoutClick = onLogoutClick
        )

        // 2. Pet Cards Carousel
        PetsSection(
            pets = pets,
            activePet = activePet,
            onActivePetChange = setActivePet,
            onAddPetClick = onAddPetClick
        )

        // 3. Upcoming Appointments & Medication Alert
        UpcomingSection(
            pets = pets,
            onAddPetClick = onAddPetClick,
            onSeeAllClick = onSeeAllAppointmentsClick,
            onAppointmentClick = onAppointmentClick
        )

        // 4. Quick Actions
        QuickActionsSection(
            onQuickActionClick = onQuickActionClick
        )

        // 5. Daily Tip Article
        DailyTipCard(
            onReadArticleClick = onReadArticleClick
        )
    }
}

@Composable
private fun GreetingHeader(
    userName: String,
    onNotificationClick: () -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "BUENOS DÍAS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$userName 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .clickable { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notificaciones",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 6.dp)
                        .size(8.dp)
                        .background(Color(0xFFE8703A), CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .clickable { onLogoutClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = "Cerrar sesión",
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun PetsSection(
    pets: List<PetEntity>,
    activePet: Int,
    onActivePetChange: (Int) -> Unit,
    onAddPetClick: () -> Unit
) {
    val petColors = listOf(
        Pair(Color(0xFFE8703A), Color(0xFFFFF3ED)),
        Pair(Color(0xFF7C3AED), Color(0xFFF5F3FF)),
        Pair(Color(0xFF3D9E6B), Color(0xFFEDFBF3)),
        Pair(Color(0xFF2563EB), Color(0xFFEFF6FF))
    )

    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mis Mascotas (${pets.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.clickable { onAddPetClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFE8703A)
                )
                Text(
                    text = "Agregar",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE8703A)
                )
            }
        }

        if (pets.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { onAddPetClick() },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF7ED),
                border = BorderStroke(1.dp, Color(0xFFFFEDD5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFFFE4D6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Pets,
                            contentDescription = null,
                            tint = Color(0xFFE8703A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Registra a tu primera mascota!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9A3412)
                        )
                        Text(
                            text = "Toca aquí para agregar foto, especie y placa QR.",
                            fontSize = 12.sp,
                            color = Color(0xFFC2410C)
                        )
                    }
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(pets) { i, pet ->
                    val isActive = activePet == i
                    val shape = RoundedCornerShape(16.dp)
                    val colorPair = petColors[i % petColors.size]
                    val themeColor = colorPair.first
                    val themeBg = colorPair.second

                    Box(
                        modifier = Modifier
                            .width(155.dp)
                            .then(
                                if (isActive) {
                                    Modifier.border(2.dp, themeColor, shape)
                                } else Modifier
                            )
                            .clip(shape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        themeBg,
                                        themeColor.copy(alpha = 0.13f)
                                    )
                                )
                            )
                            .clickable { onActivePetChange(i) }
                            .padding(14.dp)
                    ) {
                        Column {
                            if (!pet.photoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = pet.photoUrl,
                                    contentDescription = pet.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(88.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(88.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(themeColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Pets,
                                        contentDescription = null,
                                        tint = themeColor,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = pet.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${pet.species} • ${pet.breed ?: "Mestizo"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF3D9E6B), CircleShape)
                                )
                                Text(
                                    text = "Registrado",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingSection(
    pets: List<PetEntity>,
    onAddPetClick: () -> Unit = {},
    onSeeAllClick: () -> Unit,
    onAppointmentClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Próximas citas",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Ver todas",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE8703A),
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        if (pets.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sin citas ni recordatorios activos",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Registra a tu mascota para programar vacunas y citas médicas.",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        } else {
            val firstPet = pets.first()
            // Appointment Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAppointmentClick() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8703A).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MedicalServices,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFE8703A)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Control Preventivo — ${firstPet.name}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Historial médico al día",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 4.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onQuickActionClick: (String) -> Unit
) {
    val quickActions = remember {
        listOf(
            QuickActionItem(
                label = "Veterinario",
                icon = Icons.Outlined.Phone,
                color = Color(0xFFE8703A),
                bgColor = Color(0xFFE8703A).copy(alpha = 0.12f)
            ),
            QuickActionItem(
                label = "Peluquería",
                icon = Icons.Outlined.ContentCut,
                color = Color(0xFF7C3AED),
                bgColor = Color(0xFF7C3AED).copy(alpha = 0.10f)
            ),
            QuickActionItem(
                label = "Perdido",
                icon = Icons.Outlined.LocationOn,
                color = Color(0xFFDC2626),
                bgColor = Color(0xFFDC2626).copy(alpha = 0.09f)
            ),
            QuickActionItem(
                label = "Comunidad",
                icon = Icons.Outlined.Groups,
                color = Color(0xFF3D9E6B),
                bgColor = Color(0xFF3D9E6B).copy(alpha = 0.12f)
            )
        )
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        Text(
            text = "Acciones rápidas",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickActions.forEach { action ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onQuickActionClick(action.label) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(action.bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.label,
                            modifier = Modifier.size(20.dp),
                            tint = action.color
                        )
                    }
                    Text(
                        text = action.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyTipCard(
    onReadArticleClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
            .fillMaxWidth()
    ) {
        Column {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=600&h=200&fit=crop&auto=format",
                contentDescription = "Dos perros jugando en el parque",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CONSEJO DEL DÍA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFFE8703A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Los perros necesitan entre 30 min y 2 horas de ejercicio diario según su raza y edad.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.clickable { onReadArticleClick() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Leer más",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE8703A)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = Color(0xFFE8703A)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PetCare360Theme {
        HomeScreen()
    }
}
