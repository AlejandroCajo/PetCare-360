package com.example.petcare360.ui

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.petcare360.ui.theme.PetCare360Theme

data class HealthPet(
    val id: String,
    val name: String,
    val breed: String,
    val gender: String,
    val weight: String,
    val age: String,
    val nextVet: String,
    val img: String,
    val color: Color
)

data class Vaccine(
    val name: String,
    val status: String, // "done" | "pending"
    val date: String
)

data class Medication(
    val name: String,
    val desc: String,
    val next: String
)

data class VetVisit(
    val date: String,
    val reason: String,
    val vet: String
)

val SAMPLE_HEALTH_PETS = listOf(
    HealthPet(
        id = "1",
        name = "Luna",
        breed = "Golden Retriever",
        gender = "Hembra",
        weight = "24.5 kg",
        age = "3 años",
        nextVet = "15 mar",
        img = "https://images.unsplash.com/photo-1552053831-71594a27632d?w=300&h=200&fit=crop",
        color = Color(0xFFE8703A)
    ),
    HealthPet(
        id = "2",
        name = "Milo",
        breed = "Gato Siamés",
        gender = "Macho",
        weight = "4.2 kg",
        age = "2 años",
        nextVet = "28 abr",
        img = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=300&h=200&fit=crop",
        color = Color(0xFF7C3AED)
    ),
    HealthPet(
        id = "3",
        name = "Rocky",
        breed = "Bulldog Francés",
        gender = "Macho",
        weight = "12.0 kg",
        age = "4 años",
        nextVet = "10 may",
        img = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=300&h=200&fit=crop",
        color = Color(0xFF3D9E6B)
    )
)

val SAMPLE_VACCINES = listOf(
    Vaccine("Antirrábica", "pending", "15 mar 2026"),
    Vaccine("Múltiple Canina (Sextuple)", "done", "10 ene 2025"),
    Vaccine("Traqueobronquitis (Bordetella)", "done", "15 ago 2024")
)

val SAMPLE_MEDICATIONS = listOf(
    Medication("Bravecto", "Desparasitante externo · 1 pastilla", "Hoy, 8:00 pm"),
    Medication("Otopet Liquid", "Gotas para oídos · 3 gotas / 12h", "Mañana, 9:00 am")
)

val SAMPLE_VISITS = listOf(
    VetVisit("10 oct 2025", "Vacunación anual", "Dr. Mendoza"),
    VetVisit("5 ago 2025", "Revisión general", "Dra. López"),
    VetVisit("12 mar 2025", "Control de peso", "Dr. Mendoza")
)

/**
 * Sobrecarga Stateful de SaludScreen.
 */
@Composable
fun SaludScreen(
    supabaseClient: com.example.petcare360.data.remote.SupabaseClient? = null,
    userPets: List<com.example.petcare360.data.model.PetEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    var activePetIndex by remember { mutableIntStateOf(0) }
    
    var vaccines by remember { mutableStateOf<List<Vaccine>>(emptyList()) }
    var medications by remember { mutableStateOf<List<Medication>>(emptyList()) }
    var visits by remember { mutableStateOf<List<VetVisit>>(emptyList()) }

    LaunchedEffect(activePetIndex, userPets) {
        if (userPets.isNotEmpty() && supabaseClient != null) {
            val petId = userPets[activePetIndex].id
            if (petId != null) {
                val result = supabaseClient.getMedicalRecords(petId)
                result.onSuccess { records ->
                    vaccines = records.filter { it.recordType == "vacuna" }.map {
                        Vaccine(it.title, it.status ?: "pending", it.date)
                    }
                    medications = records.filter { it.recordType == "medicina" }.map {
                        Medication(it.title, it.description ?: "", it.nextDate ?: "")
                    }
                    visits = records.filter { it.recordType == "visita" }.map {
                        VetVisit(it.date, it.title, it.vetName ?: "")
                    }
                }.onFailure {
                    // Fallback a vacío
                    vaccines = emptyList()
                    medications = emptyList()
                    visits = emptyList()
                }
            }
        }
    }

    val colors = listOf(Color(0xFFE8703A), Color(0xFF7C3AED), Color(0xFF3D9E6B))
    val mappedPets = if (userPets.isNotEmpty()) {
        userPets.mapIndexed { index, pet ->
            HealthPet(
                id = pet.id ?: "",
                name = pet.name,
                breed = pet.breed ?: "Desconocida",
                gender = pet.species,
                weight = "-",
                age = pet.birthDate ?: "-",
                nextVet = "-",
                img = pet.photoUrl ?: "",
                color = colors[index % colors.size]
            )
        }
    } else {
        SAMPLE_HEALTH_PETS
    }

    SaludScreen(
        activePet = activePetIndex,
        setActivePet = { activePetIndex = it },
        pets = mappedPets,
        vaccines = if (userPets.isNotEmpty()) vaccines else SAMPLE_VACCINES,
        medications = if (userPets.isNotEmpty()) medications else SAMPLE_MEDICATIONS,
        visits = if (userPets.isNotEmpty()) visits else SAMPLE_VISITS,
        modifier = modifier
    )
}

/**
 * Sobrecarga Stateless de SaludScreen equivalente a la versión de React.
 */
@Composable
fun SaludScreen(
    activePet: Int,
    setActivePet: (Int) -> Unit,
    modifier: Modifier = Modifier,
    pets: List<HealthPet>,
    vaccines: List<Vaccine>,
    medications: List<Medication>,
    visits: List<VetVisit>,
    onAddVaccineClick: () -> Unit = {},
    onAddMedicationClick: () -> Unit = {}
) {
    val pet = pets.getOrElse(activePet) { pets.firstOrNull() } ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Encabezado y Chips de Selección de Mascotas
        HealthHeader(
            pets = pets,
            activePet = activePet,
            onActivePetChange = setActivePet
        )

        // 2. Tarjeta Resumen de la Mascota
        PetSummaryCard(pet = pet)

        // 3. Vacunas
        VaccinesSection(
            vaccines = vaccines,
            onAddVaccineClick = onAddVaccineClick
        )

        // 4. Medicamentos
        MedicationsSection(
            medications = medications,
            onAddMedicationClick = onAddMedicationClick
        )

        // 5. Últimas Visitas
        VisitHistorySection(
            visits = visits
        )
    }
}

@Composable
private fun HealthHeader(
    pets: List<HealthPet>,
    activePet: Int,
    onActivePetChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 16.dp)
    ) {
        Text(
            text = "Historial Médico",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(pets) { i, p ->
                val isSelected = activePet == i
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) p.color else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onActivePetChange(i) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AsyncImage(
                        model = p.img,
                        contentDescription = p.name,
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = p.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PetSummaryCard(pet: HealthPet) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        pet.color,
                        pet.color.copy(alpha = 0.73f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = pet.img,
                contentDescription = pet.name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentScale = ContentScale.Crop
            )

            Column {
                Text(
                    text = pet.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif,
                    color = Color.White
                )
                Text(
                    text = "${pet.breed} · ${pet.gender}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(
                            text = "Peso",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = pet.weight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "Edad",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = pet.age,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "Próx. cita",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = pet.nextVet,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaccinesSection(
    vaccines: List<Vaccine>,
    onAddVaccineClick: () -> Unit
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
                text = "Vacunas",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.clickable { onAddVaccineClick() },
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

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                vaccines.forEachIndexed { index, v ->
                    val isDone = v.status == "done"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDone) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isDone) Color(0xFF16A34A) else Color(0xFFD97706)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = v.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${if (isDone) "Aplicada" else "Próxima"}: ${v.date}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isDone) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isDone) "Al día" else "Pendiente",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDone) Color(0xFF15803D) else Color(0xFFB45309)
                            )
                        }
                    }

                    if (index < vaccines.size - 1) {
                        HorizontalDivider(
                            color = Color(0xFFE5E7EB),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationsSection(
    medications: List<Medication>,
    onAddMedicationClick: () -> Unit
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
                text = "Medicamentos",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.clickable { onAddMedicationClick() },
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

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            medications.forEach { med ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
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
                                .background(Color(0xFFE8703A).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Medication,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFFE8703A)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = med.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = med.desc,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Próx. dosis",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = med.next,
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

@Composable
private fun VisitHistorySection(
    visits: List<VetVisit>
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Últimas visitas",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                visits.forEachIndexed { index, visit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFE8703A), CircleShape)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = visit.reason,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${visit.vet} · ${visit.date}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (index < visits.size - 1) {
                        HorizontalDivider(
                            color = Color(0xFFE5E7EB),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SaludScreenPreview() {
    PetCare360Theme {
        SaludScreen()
    }
}
