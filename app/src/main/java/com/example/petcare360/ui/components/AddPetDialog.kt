package com.example.petcare360.ui.components

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.petcare360.data.model.PetEntity
import com.example.petcare360.data.remote.CloudinaryManager
import com.example.petcare360.data.remote.SupabaseClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun AddPetDialog(
    cloudinaryManager: CloudinaryManager,
    supabaseClient: SupabaseClient? = null,
    onDismiss: () -> Unit,
    onPetCreated: (PetEntity) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val primaryColor = Color(0xFFE8703A)

    var name by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf("Perro") }
    var breed by remember { mutableStateOf("") }

    // Fecha / Edad
    var isAgeMode by remember { mutableStateOf(true) } // true: Edad aprox, false: Fecha exacta
    var ageValue by remember { mutableStateOf("") }
    var ageUnit by remember { mutableStateOf("Años") } // "Años" o "Meses"
    var exactBirthDate by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showOwnerPhone by remember { mutableStateOf(true) }
    var showOwnerName by remember { mutableStateOf(true) }
    var showAddress by remember { mutableStateOf(false) }

    var isUploading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // DatePicker Dialog helper
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            exactBirthDate = formatted
            errorText = null
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Dialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFFFFF3ED), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Pets,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Registrar Mascota",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFF1F2937)
                        )
                    }

                    IconButton(onClick = onDismiss, enabled = !isUploading) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Banner de Alerta de Error (si existe)
                if (!errorText.isNullOrEmpty()) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorText!!,
                                color = Color(0xFF991B1B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // 1. Selector de Imagen con Cloudinary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                        .clickable(enabled = !isUploading) {
                            try {
                                imagePickerLauncher.launch("image/*")
                            } catch (e: Exception) {
                                errorText = "No se pudo abrir el selector de imágenes: ${e.message}"
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Foto de mascota",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Subir foto",
                                tint = primaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Subir foto a Cloudinary",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4B5563)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Nombre
                Text(
                    text = "Nombre de la mascota *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (errorText != null) errorText = null
                    },
                    placeholder = { Text("Ej. Luna, Firulais...", color = Color(0xFF9CA3AF), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Especie chips
                Text(
                    text = "Especie *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Perro 🐶", "Gato 🐱", "Ave 🦜", "Otro 🐾").forEach { species ->
                        val cleanSpecies = species.substringBefore(" ")
                        val isSelected = selectedSpecies == cleanSpecies
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSpecies = cleanSpecies },
                            label = { Text(species, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.White
                            ),
                            border = BorderStroke(1.dp, if (isSelected) primaryColor else Color(0xFFE5E7EB))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Raza
                Text(
                    text = "Raza",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    placeholder = { Text("Ej. Golden Retriever, Mestizo...", color = Color(0xFF9CA3AF), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Fecha de nacimiento / Edad Aprox.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAgeMode) "Edad Aproximada" else "Fecha de Nacimiento",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = isAgeMode,
                            onClick = { isAgeMode = true },
                            label = { Text("Edad", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.White
                            ),
                            border = BorderStroke(1.dp, if (isAgeMode) primaryColor else Color(0xFFE5E7EB))
                        )
                        FilterChip(
                            selected = !isAgeMode,
                            onClick = { isAgeMode = false },
                            label = { Text("Fecha exacta", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.White
                            ),
                            border = BorderStroke(1.dp, if (!isAgeMode) primaryColor else Color(0xFFE5E7EB))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (isAgeMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = ageValue,
                            onValueChange = {
                                ageValue = it.filter { ch -> ch.isDigit() }
                                if (errorText != null) errorText = null
                            },
                            placeholder = { Text("Ej. 2, 6, 12...", color = Color(0xFF9CA3AF), fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color(0xFFE5E7EB)
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Años", "Meses").forEach { unit ->
                                FilterChip(
                                    selected = ageUnit == unit,
                                    onClick = { ageUnit = unit },
                                    label = { Text(unit, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryColor,
                                        selectedLabelColor = Color.White
                                    ),
                                    border = BorderStroke(1.dp, if (ageUnit == unit) primaryColor else Color(0xFFE5E7EB))
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = exactBirthDate,
                        onValueChange = {
                            exactBirthDate = it
                            if (errorText != null) errorText = null
                        },
                        placeholder = { Text("AAAA-MM-DD (Ej. 2023-05-10)", color = Color(0xFF9CA3AF), fontSize = 13.sp) },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = "Seleccionar fecha",
                                    tint = primaryColor
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 6. Privacidad QR
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF9FAFB),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.QrCode2,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Privacidad Plaquita QR",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mostrar teléfono en escaneo QR", fontSize = 12.sp, color = Color(0xFF4B5563))
                            Switch(
                                checked = showOwnerPhone,
                                onCheckedChange = { showOwnerPhone = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = primaryColor, checkedTrackColor = Color(0xFFFFD4BE))
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mostrar nombre de dueño", fontSize = 12.sp, color = Color(0xFF4B5563))
                            Switch(
                                checked = showOwnerName,
                                onCheckedChange = { showOwnerName = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = primaryColor, checkedTrackColor = Color(0xFFFFD4BE))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botón Guardar
                Button(
                    onClick = {
                        // 1. Validaciones
                        val trimmedName = name.trim()
                        if (trimmedName.length < 2) {
                            errorText = "Por favor ingresa un nombre válido para la mascota (mínimo 2 letras)."
                            return@Button
                        }

                        // Calcular / validar birth_date para PostgreSQL DATE
                        val computedBirthDate: String?
                        if (isAgeMode) {
                            if (ageValue.isNotBlank()) {
                                val num = ageValue.toLongOrNull()
                                if (num == null || num <= 0 || (ageUnit == "Años" && num > 30) || (ageUnit == "Meses" && num > 360)) {
                                    errorText = "La edad ingresada no es válida. Ingresa un número razonable."
                                    return@Button
                                }
                                val calculated = if (ageUnit == "Años") {
                                    LocalDate.now().minusYears(num)
                                } else {
                                    LocalDate.now().minusMonths(num)
                                }
                                computedBirthDate = calculated.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            } else {
                                computedBirthDate = null
                            }
                        } else {
                            if (exactBirthDate.isNotBlank()) {
                                val cleanDate = exactBirthDate.trim()
                                val parsed = tryParseDate(cleanDate)
                                if (parsed == null) {
                                    errorText = "Formato de fecha inválido. Usa AAAA-MM-DD (ejemplo: 2023-05-10) o toca el calendario."
                                    return@Button
                                }
                                computedBirthDate = parsed
                            } else {
                                computedBirthDate = null
                            }
                        }

                        isUploading = true
                        errorText = null
                        statusMessage = "Subiendo imagen..."

                        coroutineScope.launch {
                            var uploadedPhotoUrl: String? = null
                            if (selectedImageUri != null) {
                                val uploadResult = cloudinaryManager.uploadImage(selectedImageUri!!)
                                uploadResult.onSuccess { url ->
                                    uploadedPhotoUrl = url
                                }.onFailure { error ->
                                    // Mostramos el error pero no bloqueamos si el usuario prefiere guardar sin foto o reintentar
                                    isUploading = false
                                    errorText = "Error al subir foto a Cloudinary: ${error.localizedMessage ?: error.message}. Puedes reintentar o deseleccionar la foto."
                                    return@launch
                                }
                            }

                            statusMessage = "Guardando mascota en Supabase..."

                            val newPet = PetEntity(
                                name = trimmedName,
                                species = selectedSpecies,
                                breed = breed.ifBlank { "Mestizo" },
                                birthDate = computedBirthDate,
                                photoUrl = uploadedPhotoUrl,
                                showOwnerPhone = showOwnerPhone,
                                showOwnerName = showOwnerName,
                                showAddress = showAddress
                            )

                            if (supabaseClient != null) {
                                val saveResult = supabaseClient.createPet(newPet)
                                isUploading = false
                                saveResult.onSuccess { savedPet ->
                                    Toast.makeText(context, "¡${savedPet.name} registrada con éxito!", Toast.LENGTH_SHORT).show()
                                    onPetCreated(savedPet)
                                    onDismiss()
                                }.onFailure { error ->
                                    errorText = "Error de base de datos al guardar: ${error.localizedMessage ?: error.message}"
                                }
                            } else {
                                isUploading = false
                                onPetCreated(newPet)
                                onDismiss()
                            }
                        }
                    },
                    enabled = !isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(statusMessage.ifBlank { "Guardando..." }, color = Color.White, fontSize = 13.sp)
                    } else {
                        Text("Guardar Mascota", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Función auxiliar para interpretar formatos de fecha comunes (YYYY-MM-DD, DD/MM/YYYY, etc.)
 */
private fun tryParseDate(input: String): String? {
    // 1. Regex ISO: YYYY-MM-DD
    val isoRegex = Regex("""^(\d{4})-(\d{2})-(\d{2})$""")
    if (isoRegex.matches(input)) {
        return try {
            val date = LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE)
            date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: Exception) {
            null
        }
    }

    // 2. DD/MM/YYYY o DD-MM-YYYY
    val dmyRegex = Regex("""^(\d{1,2})[/.-](\d{1,2})[/.-](\d{4})$""")
    val match = dmyRegex.find(input)
    if (match != null) {
        val (day, month, year) = match.destructured
        return try {
            val date = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
            date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: Exception) {
            null
        }
    }

    return null
}
