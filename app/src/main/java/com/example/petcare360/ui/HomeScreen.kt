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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.petcare360.ui.theme.PetCare360Theme

data class Pet(
    val id: String,
    val name: String,
    val breed: String,
    val img: String,
    val color: Color,
    val colorBg: Color,
    val statusOk: Boolean
)

val SAMPLE_PETS = listOf(
    Pet(
        id = "1",
        name = "Luna",
        breed = "Golden Retriever",
        img = "https://images.unsplash.com/photo-1552053831-71594a27632d?w=300&h=200&fit=crop",
        color = Color(0xFFE8703A),
        colorBg = Color(0xFFFFF3ED),
        statusOk = true
    ),
    Pet(
        id = "2",
        name = "Milo",
        breed = "Gato Siamés",
        img = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=300&h=200&fit=crop",
        color = Color(0xFF7C3AED),
        colorBg = Color(0xFFF5F3FF),
        statusOk = false
    ),
    Pet(
        id = "3",
        name = "Rocky",
        breed = "Bulldog Francés",
        img = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=300&h=200&fit=crop",
        color = Color(0xFF3D9E6B),
        colorBg = Color(0xFFEDFBF3),
        statusOk = true
    )
)

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
    cloudinaryManager: com.example.petcare360.data.remote.CloudinaryManager? = null,
    onAddPet: (com.example.petcare360.data.model.PetEntity) -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var activePet by remember { mutableIntStateOf(0) }
    var showAddPetDialog by remember { mutableStateOf(false) }

    if (showAddPetDialog && cloudinaryManager != null) {
        com.example.petcare360.ui.components.AddPetDialog(
            cloudinaryManager = cloudinaryManager,
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
    pets: List<Pet> = SAMPLE_PETS,
    onNotificationClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onAddPetClick: () -> Unit = {},
    onSeeAllAppointmentsClick: () -> Unit = {},
    onAppointmentClick: () -> Unit = {},
    onMarkMedicationDone: () -> Unit = {},
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
            onSeeAllClick = onSeeAllAppointmentsClick,
            onAppointmentClick = onAppointmentClick,
            onMarkMedicationDone = onMarkMedicationDone
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
    pets: List<Pet>,
    activePet: Int,
    onActivePetChange: (Int) -> Unit,
    onAddPetClick: () -> Unit
) {
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
                text = "Mis Mascotas",
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
                    modifier = Modifier.size(12.dp),
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

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(pets) { i, pet ->
                val isActive = activePet == i
                val shape = RoundedCornerShape(16.dp)

                Box(
                    modifier = Modifier
                        .width(155.dp)
                        .then(
                            if (isActive) {
                                Modifier.border(2.dp, pet.color, shape)
                            } else Modifier
                        )
                        .clip(shape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    pet.colorBg,
                                    pet.color.copy(alpha = 0.13f)
                                )
                            )
                        )
                        .clickable { onActivePetChange(i) }
                        .padding(14.dp)
                ) {
                    Column {
                        AsyncImage(
                            model = pet.img,
                            contentDescription = pet.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = pet.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = pet.breed,
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
                                    .background(
                                        if (pet.statusOk) Color(0xFF3D9E6B) else Color(0xFFF59E0B),
                                        CircleShape
                                    )
                            )
                            Text(
                                text = if (pet.statusOk) "Al día" else "Pendiente",
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

@Composable
private fun UpcomingSection(
    onSeeAllClick: () -> Unit,
    onAppointmentClick: () -> Unit,
    onMarkMedicationDone: () -> Unit
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

        // Appointment Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
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
                        text = "Vacuna Antirrábica — Luna",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Dr. Mendoza · Clínica VetCare",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "15 mar 2026",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "10:30 am",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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

        // Medication Alert Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF59E0B).copy(alpha = 0.08f),
            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Medication,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFFD97706)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Medicamento hoy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = "Bravecto para Luna",
                        fontSize = 12.sp,
                        color = Color(0xFFB45309)
                    )
                }

                Button(
                    onClick = onMarkMedicationDone,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Marcar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
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
