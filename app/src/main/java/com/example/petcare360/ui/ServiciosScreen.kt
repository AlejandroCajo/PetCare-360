package com.example.petcare360.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.petcare360.data.model.BusinessEntity
import com.example.petcare360.data.remote.SupabaseClient
import com.example.petcare360.ui.components.BusinessCard
import com.example.petcare360.ui.components.PetCareSearchBar
import com.example.petcare360.ui.components.PetCareTopBar
import kotlinx.coroutines.launch

val SERVICE_CATEGORIES = listOf("Todos", "Veterinaria", "Peluquería", "Paseador", "Guardería", "Pet Shop")

fun categoryToDbCode(cat: String): String? = when (cat) {
    "Veterinaria" -> "vet"
    "Peluquería" -> "groomer"
    "Paseador" -> "walker"
    "Guardería" -> "daycare"
    "Pet Shop" -> "shop"
    else -> null
}

fun dbCodeToDisplay(dbCat: String): String = when (dbCat.lowercase()) {
    "vet" -> "Veterinaria 🏥"
    "groomer" -> "Peluquería & Spa ✂️"
    "walker" -> "Paseador 🦮"
    "daycare" -> "Guardería & Hotel 🏨"
    "shop" -> "Pet Shop & Alimentos 🛍️"
    else -> dbCat.replaceFirstChar { it.uppercase() }
}

@Composable
fun ServiciosScreen(
    supabaseClient: SupabaseClient? = null,
    userPets: List<com.example.petcare360.data.model.PetEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val primaryColor = Color(0xFFE8703A)

    var activeCat by remember { mutableStateOf("Todos") }
    var searchQuery by remember { mutableStateOf("") }
    var businessesList by remember { mutableStateOf<List<BusinessEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadBusinesses() {
        if (supabaseClient == null) {
            isLoading = false
            return
        }
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            val dbCat = categoryToDbCode(activeCat)
            val result = supabaseClient.getBusinesses(category = dbCat)
            isLoading = false
            result.onSuccess { list ->
                businessesList = list
            }.onFailure { error ->
                errorMessage = error.localizedMessage ?: "Error al cargar servicios"
            }
        }
    }

    LaunchedEffect(activeCat) {
        loadBusinesses()
    }

    val filteredList = remember(businessesList, searchQuery) {
        if (searchQuery.isBlank()) businessesList
        else businessesList.filter { b ->
            b.name.contains(searchQuery, ignoreCase = true) ||
            (b.address ?: "").contains(searchQuery, ignoreCase = true) ||
            (b.priceInfo ?: "").contains(searchQuery, ignoreCase = true)
        }
    }

    var selectedBusiness by remember { mutableStateOf<BusinessEntity?>(null) }

    if (selectedBusiness != null) {
        BusinessDetailScreen(
            business = selectedBusiness!!,
            supabaseClient = supabaseClient,
            userPets = userPets,
            onBack = { selectedBusiness = null }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Header (Título + Barra de Búsqueda)
        PetCareTopBar(
            subtitle = "SERVICIOS & BIENESTAR",
            title = "Explorar Servicios",
            actions = {
                IconButton(
                    onClick = { loadBusinesses() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF3F4F6), androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Actualizar",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )
        PetCareSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Buscar veterinarias, paseadores, spa...",
            modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 12.dp)
        )

        // 2. Chips de Categoría
        CategoryPillsSection(
            categories = SERVICE_CATEGORIES,
            activeCat = activeCat,
            onActiveCatChange = { activeCat = it }
        )


        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Servicios Disponibles (${filteredList.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        color = primaryColor,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (isLoading && businessesList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else if (!errorMessage.isNullOrEmpty() && businessesList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error al conectar con servicios", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                        Text(text = errorMessage!!, fontSize = 12.sp, color = Color(0xFFB91C1C), modifier = Modifier.padding(top = 4.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { loadBusinesses() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            } else if (filteredList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Pets,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No se encontraron servicios",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4B5563)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "Intenta con otra búsqueda." else "Aún no hay negocios registrados en esta categoría.",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                filteredList.forEach { business ->
                    BusinessCard(
                        business = business,
                        onContactClick = {
                            selectedBusiness = business
                        },
                        categoryDisplay = dbCodeToDisplay(business.category)
                    )
                }
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
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { cat ->
            val isActive = activeCat == cat
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isActive) Color(0xFFE8703A) else Color(0xFFF3F4F6)
                    )
                    .clickable { onActiveCatChange(cat) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = cat,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) Color.White else Color(0xFF4B5563)
                )
            }
        }
    }
}



