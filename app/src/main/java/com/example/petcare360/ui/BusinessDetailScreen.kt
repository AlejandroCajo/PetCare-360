package com.example.petcare360.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.petcare360.data.model.BusinessEntity
import com.example.petcare360.data.model.BusinessProductEntity
import com.example.petcare360.data.model.BusinessServiceEntity
import com.example.petcare360.data.remote.SupabaseClient
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.example.petcare360.data.model.PetEntity
import com.example.petcare360.data.model.AppointmentEntity
import com.example.petcare360.data.model.OrderEntity
import com.example.petcare360.data.model.OrderItemEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun BusinessDetailScreen(
    business: BusinessEntity,
    supabaseClient: SupabaseClient?,
    userPets: List<PetEntity>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val primaryColor = Color(0xFFE8703A)

    var services by remember { mutableStateOf<List<BusinessServiceEntity>>(emptyList()) }
    var products by remember { mutableStateOf<List<BusinessProductEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedServiceForBooking by remember { mutableStateOf<BusinessServiceEntity?>(null) }
    var selectedProductForBuying by remember { mutableStateOf<BusinessProductEntity?>(null) }

    LaunchedEffect(business.id) {
        if (supabaseClient != null && business.id != null) {
            isLoading = true
            val servicesResult = supabaseClient.getBusinessServices(business.id)
            val productsResult = supabaseClient.getBusinessProducts(business.id)

            servicesResult.onSuccess { services = it }
            productsResult.onSuccess { products = it }
            isLoading = false
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // Header con imagen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFE5E7EB))
        ) {
            if (!business.photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = business.photoUrl,
                    contentDescription = business.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            // Botón de atrás
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Info principal del negocio
            item {
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = business.name,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = dbCodeToDisplay(business.category),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = primaryColor,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFFFFFBEB), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(String.format("%.1f", business.avgRating), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!business.address.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(business.address, fontSize = 14.sp, color = Color(0xFF4B5563))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (!business.phone.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${business.phone}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Llamar al ${business.phone}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.Phone, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(business.phone, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = primaryColor)
                            }
                        }
                    }
                }
            }

            // Estado de carga
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                }
            } else {
                // Lista de Servicios
                if (services.isNotEmpty()) {
                    item {
                        Text(
                            text = "Servicios Disponibles",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                        )
                    }
                    items(services) { service ->
                        ServiceItem(service) {
                            selectedServiceForBooking = service
                        }
                    }
                }

                // Lista de Productos (si es tienda)
                if (products.isNotEmpty()) {
                    item {
                        Text(
                            text = "Productos en Tienda",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                        )
                    }
                    items(products) { product ->
                        ProductItem(product) {
                            selectedProductForBuying = product
                        }
                    }
                }

                if (services.isEmpty() && products.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.Info, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Aún no hay servicios o productos", color = Color(0xFF6B7280))
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedServiceForBooking != null && business.id != null) {
        BookingDialog(
            service = selectedServiceForBooking!!,
            businessId = business.id,
            userPets = userPets,
            supabaseClient = supabaseClient,
            onDismiss = { selectedServiceForBooking = null },
            onSuccess = { selectedServiceForBooking = null }
        )
    }

    if (selectedProductForBuying != null && business.id != null) {
        PurchaseDialog(
            product = selectedProductForBuying!!,
            businessId = business.id,
            supabaseClient = supabaseClient,
            onDismiss = { selectedProductForBuying = null },
            onSuccess = { selectedProductForBuying = null }
        )
    }
}

@Composable
fun ServiceItem(service: BusinessServiceEntity, onBookClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                if (!service.description.isNullOrBlank()) {
                    Text(service.description, fontSize = 13.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("S/ ${service.price}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                    if (service.durationMinutes != null && service.durationMinutes > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("•", color = Color(0xFF9CA3AF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${service.durationMinutes} min", fontSize = 13.sp, color = Color(0xFF6B7280))
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onBookClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8703A)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("Reservar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ProductItem(product: BusinessProductEntity, onAddClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF9CA3AF))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                if (!product.description.isNullOrBlank()) {
                    Text(product.description, fontSize = 12.sp, color = Color(0xFF6B7280), maxLines = 1, modifier = Modifier.padding(top = 2.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("S/ ${product.price}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = Color(0xFF1F2937)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("Comprar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun BookingDialog(
    service: BusinessServiceEntity,
    businessId: String,
    userPets: List<PetEntity>,
    supabaseClient: SupabaseClient?,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedPet by remember { mutableStateOf<PetEntity?>(userPets.firstOrNull()) }
    var dateText by remember { mutableStateOf("2026-10-15 10:00:00") }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reservar Cita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Servicio: ${service.name}")
                Text("Precio: S/ ${service.price}")

                if (userPets.isNotEmpty()) {
                    Text("Selecciona una mascota:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    LazyColumn(modifier = Modifier.height(100.dp)) {
                        items(userPets) { pet ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPet = pet }
                                    .background(if (selectedPet == pet) Color(0xFFFFFBEB) else Color.Transparent)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(pet.name, fontWeight = if (selectedPet == pet) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                } else {
                    Text("No tienes mascotas registradas.", color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (supabaseClient != null) {
                        isSubmitting = true
                        coroutineScope.launch {
                            val appointment = AppointmentEntity(
                                businessId = businessId,
                                serviceId = service.id,
                                petId = selectedPet?.id,
                                appointmentDate = dateText
                            )
                            val result = supabaseClient.createAppointment(appointment)
                            isSubmitting = false
                            if (result.isSuccess) {
                                Toast.makeText(context, "Reserva confirmada", Toast.LENGTH_SHORT).show()
                                onSuccess()
                            } else {
                                Toast.makeText(context, "Error al reservar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = !isSubmitting && (userPets.isEmpty() || selectedPet != null),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8703A))
            ) {
                Text(if (isSubmitting) "Procesando..." else "Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun PurchaseDialog(
    product: BusinessProductEntity,
    businessId: String,
    supabaseClient: SupabaseClient?,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comprar Producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Producto: ${product.name}")
                Text("Precio total: S/ ${product.price}", fontWeight = FontWeight.Bold, color = Color(0xFF047857))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (supabaseClient != null) {
                        isSubmitting = true
                        coroutineScope.launch {
                            val order = OrderEntity(
                                businessId = businessId,
                                totalAmount = product.price
                            )
                            val result = supabaseClient.createOrder(order)
                            if (result.isSuccess) {
                                val createdOrder = result.getOrNull()
                                if (createdOrder?.id != null && product.id != null) {
                                    val item = OrderItemEntity(
                                        orderId = createdOrder.id,
                                        productId = product.id,
                                        quantity = 1,
                                        priceAtPurchase = product.price
                                    )
                                    supabaseClient.createOrderItem(item)
                                }
                                Toast.makeText(context, "Compra exitosa", Toast.LENGTH_SHORT).show()
                                onSuccess()
                            } else {
                                Toast.makeText(context, "Error en la compra", Toast.LENGTH_SHORT).show()
                            }
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
            ) {
                Text(if (isSubmitting) "Procesando..." else "Comprar ahora")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
