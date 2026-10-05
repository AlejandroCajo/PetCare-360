package com.example.petcare360.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.Calendar
import androidx.compose.foundation.clickable
import com.example.petcare360.data.model.PetEntity
import com.example.petcare360.data.model.SosAlertEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSosAlertDialog(
    userPets: List<PetEntity>,
    onDismiss: () -> Unit,
    onSubmit: (SosAlertEntity, (Boolean) -> Unit) -> Unit
) {
    var selectedPetId by remember { mutableStateOf<String?>(userPets.firstOrNull()?.id) }
    var lastSeenLocation by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var lostDate by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reportar Mascota Perdida",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, enabled = !isLoading) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                if (userPets.isEmpty()) {
                    Text("No tienes mascotas registradas. Agrega una mascota en tu perfil primero.")
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = userPets.find { it.id == selectedPetId }?.name ?: "Selecciona tu mascota",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mascota") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            userPets.forEach { pet ->
                                DropdownMenuItem(
                                    text = { Text(pet.name) },
                                    onClick = {
                                        selectedPetId = pet.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = lastSeenLocation,
                        onValueChange = { lastSeenLocation = it },
                        label = { Text("Último lugar visto") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = lostDate,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Fecha y hora de pérdida") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val cal = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        TimePickerDialog(
                                            context,
                                            { _, hourOfDay, minute ->
                                                val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                                                lostDate = "$dayOfMonth/${month + 1}/$year $formattedTime"
                                            },
                                            cal.get(Calendar.HOUR_OF_DAY),
                                            cal.get(Calendar.MINUTE),
                                            true
                                        ).show()
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción / Recompensa") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Button(
                        onClick = {
                            if (selectedPetId != null && lastSeenLocation.isNotBlank() && lostDate.isNotBlank()) {
                                isLoading = true
                                onSubmit(
                                    SosAlertEntity(
                                        petId = selectedPetId!!,
                                        lastSeenLocation = lastSeenLocation,
                                        lossLocation = "POINT(0 0)",
                                        lostDate = lostDate,
                                        description = description,
                                        status = "active"
                                    )
                                ) { success ->
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && selectedPetId != null && lastSeenLocation.isNotBlank() && lostDate.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8703A))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Publicar Alerta")
                        }
                    }
                }
            }
        }
    }
}
