package com.example.petcare360.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.petcare360.data.model.PetEntity
import com.example.petcare360.data.remote.CloudinaryManager
import com.example.petcare360.ui.components.AddPetDialog
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    userPhone: String?,
    userAvatar: String?,
    pets: List<PetEntity>,
    cloudinaryManager: CloudinaryManager,
    onAddPet: (PetEntity) -> Unit = {},
    onAvatarUpdated: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddPetDialog by remember { mutableStateOf(false) }
    var isUploadingAvatar by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val primaryColor = Color(0xFFE8703A)
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFE8703A), Color(0xFFFF8E53))
    )

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingAvatar = true
            coroutineScope.launch {
                val res = cloudinaryManager.uploadImage(uri)
                isUploadingAvatar = false
                res.onSuccess { url ->
                    onAvatarUpdated(url)
                }
            }
        }
    }

    if (showAddPetDialog) {
        AddPetDialog(
            cloudinaryManager = cloudinaryManager,
            onDismiss = { showAddPetDialog = false },
            onPetCreated = { newPet ->
                showAddPetDialog = false
                onAddPet(newPet)
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF9FAFB)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Header Card con Avatar y Datos de Usuario
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryGradient)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mi Perfil",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color.White
                        )

                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "Cerrar sesión",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Avatar + Datos
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    if (!isUploadingAvatar) {
                                        avatarPickerLauncher.launch("image/*")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUploadingAvatar) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = primaryColor,
                                    strokeWidth = 2.dp
                                )
                            } else if (!userAvatar.isNullOrEmpty()) {
                                AsyncImage(
                                    model = userAvatar,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }

                            // Icono pequeño de cámara
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(22.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Cambiar avatar",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = userName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = userEmail,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            if (!userPhone.isNullOrBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Phone,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = userPhone,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Badges / Logros
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Dueño VIP", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cuenta Verificada", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // 2. Tabs de Secciones
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = primaryColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = primaryColor
                    )
                }
            ) {
                listOf("Mis Mascotas", "Consultas", "Pedidos").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) primaryColor else Color(0xFF6B7280)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Contenido según Tab seleccionado
            when (selectedTab) {
                0 -> {
                    // Pestaña Mascotas
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mascotas Registradas (${pets.size})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )

                            Button(
                                onClick = { showAddPetDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Añadir", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (pets.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Pets,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Aún no has registrado mascotas",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF4B5563),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Añade a tus peludos con foto para generar su placa QR y carnet de salud.",
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 12.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        } else {
                            pets.forEach { pet ->
                                PetProfileCard(pet = pet)
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }

                1 -> {
                    // Pestaña Consultas y Citas Médicas
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Historial de Consultas Veterinarias",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        ConsultationCard(
                            vetName = "Clínica Veterinaria San José",
                            doctor = "Dra. Carmen Soto",
                            date = "15 Octubre, 2026 - 10:30 AM",
                            reason = "Vacunación Sextuple & Chequeo general",
                            status = "Confirmada"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ConsultationCard(
                            vetName = "Hospital Veterinario 24 Horas",
                            doctor = "Dr. Roberto Peña",
                            date = "28 Septiembre, 2026 - 04:00 PM",
                            reason = "Limpieza dental y desparasitación",
                            status = "Completada"
                        )
                    }
                }

                2 -> {
                    // Pestaña Pedidos y Servicios
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Mis Reservas y Pedidos",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        ServiceOrderCard(
                            serviceName = "Baño y Corte Spa Canino",
                            provider = "Happy Paws Grooming",
                            price = "S/ 65.00",
                            date = "Mañana a las 11:00 AM",
                            status = "En Proceso"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ServiceOrderCard(
                            serviceName = "Paseo de 1 Hora (Parque Olivar)",
                            provider = "Paseadores Pro Lima",
                            price = "S/ 25.00",
                            date = "05 Octubre, 2026",
                            status = "Entregado"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun PetProfileCard(pet: PetEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFF3ED)),
                contentAlignment = Alignment.Center
            ) {
                if (!pet.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = pet.photoUrl,
                        contentDescription = pet.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Pets,
                        contentDescription = null,
                        tint = Color(0xFFE8703A),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = "${pet.species} • ${pet.breed ?: "Mestizo"}",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                if (!pet.birthDate.isNullOrEmpty()) {
                    Text(
                        text = "Nac: ${pet.birthDate}",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFF3F4F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCode,
                    contentDescription = "Ver Placa QR",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ConsultationCard(
    vetName: String,
    doctor: String,
    date: String,
    reason: String,
    status: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.MedicalServices,
                        contentDescription = null,
                        tint = Color(0xFFE8703A),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = vetName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                }

                Surface(
                    color = if (status == "Confirmada") Color(0xFFE0F2FE) else Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (status == "Confirmada") Color(0xFF0284C7) else Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Text(text = "$doctor • $reason", fontSize = 12.sp, color = Color(0xFF4B5563), modifier = Modifier.padding(top = 6.dp))
            Text(text = "📅 $date", fontSize = 11.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun ServiceOrderCard(
    serviceName: String,
    provider: String,
    price: String,
    date: String,
    status: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                        tint = Color(0xFFE8703A),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = serviceName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                }

                Text(text = price, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE8703A))
            }

            Text(text = "Proveedor: $provider", fontSize = 12.sp, color = Color(0xFF4B5563), modifier = Modifier.padding(top = 4.dp))
            Text(text = "🕒 $date • Estado: $status", fontSize = 11.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(top = 2.dp))
        }
    }
}
