package com.example.petcare360.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.petcare360.data.model.PostEntity
import com.example.petcare360.data.remote.CloudinaryManager
import com.example.petcare360.data.remote.SupabaseClient
import kotlinx.coroutines.launch

@Composable
fun CreatePostDialog(
    cloudinaryManager: CloudinaryManager,
    supabaseClient: SupabaseClient? = null,
    userName: String,
    userPets: List<com.example.petcare360.data.model.PetEntity> = emptyList(),
    onDismiss: () -> Unit,
    onPostCreated: (PostEntity) -> Unit
) {
    val context = LocalContext.current
    var content by remember { mutableStateOf("") }
    var selectedPetId by remember { mutableStateOf<String?>(null) }
    var petName by remember { mutableStateOf(userPets.firstOrNull()?.name ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val primaryColor = Color(0xFFE8703A)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

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
                                .size(36.dp)
                                .background(Color(0xFFFFF3ED), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Nueva Publicación",
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

                Spacer(modifier = Modifier.height(14.dp))

                // Banner de Alerta de Error (si existe)
                if (!errorText.isNullOrEmpty()) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
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

                // Selección de Mascota Asociada
                Text(
                    text = "Mascota asociada",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                if (userPets.isNotEmpty()) {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            androidx.compose.material3.FilterChip(
                                selected = petName.isBlank(),
                                onClick = {
                                    petName = ""
                                    selectedPetId = null
                                },
                                label = { Text("General 🐾", fontSize = 12.sp) },
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = Color.White
                                ),
                                border = BorderStroke(1.dp, if (petName.isBlank()) primaryColor else Color(0xFFE5E7EB))
                            )
                        }

                        items(userPets.size) { index ->
                            val pet = userPets[index]
                            val isSelected = petName == pet.name
                            val emoji = when (pet.species.lowercase()) {
                                "perro" -> "🐶"
                                "gato" -> "🐱"
                                "ave" -> "🦜"
                                else -> "🐾"
                            }
                            androidx.compose.material3.FilterChip(
                                selected = isSelected,
                                onClick = {
                                    petName = pet.name
                                    selectedPetId = pet.id
                                },
                                label = { Text("${pet.name} $emoji", fontSize = 12.sp) },
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = Color.White
                                ),
                                border = BorderStroke(1.dp, if (isSelected) primaryColor else Color(0xFFE5E7EB))
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        placeholder = { Text("Nombre de tu mascota (opcional)", color = Color(0xFF9CA3AF), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de texto
                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        if (errorText != null) errorText = null
                    },
                    placeholder = { Text("¿Qué está haciendo tu mascota hoy? Comparte una foto o historia...", color = Color(0xFF9CA3AF), fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de Imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                        .clickable(enabled = !isUploading) {
                            try {
                                imagePickerLauncher.launch("image/*")
                            } catch (e: Exception) {
                                errorText = "No se pudo abrir el selector de fotos: ${e.message}"
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Foto seleccionada",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Subir foto",
                                tint = primaryColor,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Agregar foto (Cloudinary)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4B5563)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Botón Publicar
                Button(
                    onClick = {
                        val trimmedContent = content.trim()
                        if (trimmedContent.isBlank() && selectedImageUri == null) {
                            errorText = "Por favor escribe un mensaje o selecciona una foto para publicar."
                            return@Button
                        }

                        isUploading = true
                        errorText = null
                        statusMessage = "Subiendo imagen a Cloudinary..."

                        coroutineScope.launch {
                            var uploadedUrl: String? = null
                            if (selectedImageUri != null) {
                                val uploadResult = cloudinaryManager.uploadImage(selectedImageUri!!)
                                uploadResult.onSuccess { url ->
                                    uploadedUrl = url
                                }.onFailure { error ->
                                    errorText = "Error subiendo foto: ${error.localizedMessage ?: error.message}"
                                    isUploading = false
                                    return@launch
                                }
                            }

                            statusMessage = "Guardando en la comunidad..."

                            val postEntity = PostEntity(
                                content = trimmedContent,
                                photoUrl = uploadedUrl,
                                petName = petName.trim().ifBlank { null },
                                userName = userName
                            )

                            if (supabaseClient != null) {
                                val result = supabaseClient.createPost(postEntity)
                                isUploading = false
                                result.onSuccess { createdPost ->
                                    Toast.makeText(context, "¡Publicado en la comunidad!", Toast.LENGTH_SHORT).show()
                                    onPostCreated(createdPost)
                                    onDismiss()
                                }.onFailure { error ->
                                    errorText = "Error al publicar: ${error.localizedMessage ?: error.message}"
                                }
                            } else {
                                isUploading = false
                                onPostCreated(postEntity)
                                onDismiss()
                            }
                        }
                    },
                    enabled = !isUploading && (content.isNotBlank() || selectedImageUri != null),
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
                        Text(statusMessage.ifBlank { "Publicando..." }, color = Color.White, fontSize = 13.sp)
                    } else {
                        Text("Publicar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
