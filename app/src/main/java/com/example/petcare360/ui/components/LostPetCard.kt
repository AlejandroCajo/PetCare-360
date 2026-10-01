package com.example.petcare360.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.petcare360.ui.LostPet
import com.example.petcare360.ui.LostPetType

@Composable
fun LostPetCard(
    pet: LostPet,
    isOwner: Boolean = false,
    onContactClick: (LostPet) -> Unit,
    onShareClick: (LostPet) -> Unit,
    onMarkAsFoundClick: ((LostPet) -> Unit)? = null
) {
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
                if (isOwner && pet.type == LostPetType.PERDIDO) {
                    Button(
                        onClick = { onMarkAsFoundClick?.invoke(pet) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF22C55E),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text(
                            text = "¡Encontrado!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
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
