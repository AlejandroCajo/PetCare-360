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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.petcare360.data.model.PetEntity
import com.example.petcare360.data.model.PostEntity
import com.example.petcare360.data.remote.CloudinaryManager
import com.example.petcare360.data.remote.SupabaseClient
import com.example.petcare360.ui.components.CreatePostDialog
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 10 // Paginación de 10 en 10 posts

@Composable
fun ComunidadScreen(
    cloudinaryManager: CloudinaryManager? = null,
    supabaseClient: SupabaseClient? = null,
    userName: String = "Alejandro",
    userAvatar: String? = null,
    userPets: List<PetEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    var postsList by remember { mutableStateOf<List<PostEntity>>(emptyList()) }
    var likedPostIds by remember { mutableStateOf(setOf<String>()) }
    var isLoadingInitial by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var hasMorePosts by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentOffset by remember { mutableIntStateOf(0) }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val primaryColor = Color(0xFFE8703A)

    // Función para cargar publicaciones desde Supabase con paginación
    fun loadPosts(reset: Boolean = false) {
        if (supabaseClient == null) {
            isLoadingInitial = false
            return
        }

        coroutineScope.launch {
            val offset = if (reset) 0 else currentOffset
            if (reset) {
                isLoadingInitial = true
                errorMessage = null
            } else {
                isLoadingMore = true
            }

            val result = supabaseClient.getPosts(limit = PAGE_SIZE, offset = offset)
            isLoadingInitial = false
            isLoadingMore = false

            result.onSuccess { newPosts ->
                if (reset) {
                    postsList = newPosts
                    currentOffset = newPosts.size
                } else {
                    postsList = postsList + newPosts
                    currentOffset += newPosts.size
                }
                hasMorePosts = newPosts.size == PAGE_SIZE
            }.onFailure { error ->
                if (reset) {
                    errorMessage = error.localizedMessage ?: "Error al cargar la comunidad"
                }
            }
        }
    }

    // Carga inicial al entrar en la pantalla
    LaunchedEffect(Unit) {
        loadPosts(reset = true)
    }

    // Modal para crear post y subir a Cloudinary + Supabase
    if (showCreatePostDialog && cloudinaryManager != null) {
        CreatePostDialog(
            cloudinaryManager = cloudinaryManager,
            supabaseClient = supabaseClient,
            userName = userName,
            userAvatar = userAvatar,
            userPets = userPets,
            onDismiss = { showCreatePostDialog = false },
            onPostCreated = { createdPost ->
                showCreatePostDialog = false
                postsList = listOf(createdPost) + postsList
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF9FAFB)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            HeaderSection(
                onCameraClick = { showCreatePostDialog = true },
                onRefreshClick = { loadPosts(reset = true) }
            )

            // Contenido principal
            if (isLoadingInitial) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Cargando comunidad...",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            } else if (errorMessage != null) {
                // Mensaje de Error con Reintentar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DynamicFeed,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se pudieron obtener las publicaciones",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                        Button(
                            onClick = { loadPosts(reset = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reintentar", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            } else if (postsList.isEmpty()) {
                // Estado Vacío (Sin publicaciones aún en la BD)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFFFF3ED), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Pets,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aún no hay publicaciones",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Sé el primero de la comunidad en compartir una foto o anécdota de tu mascota.",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                        )
                        Button(
                            onClick = { showCreatePostDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Crear primera publicación", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                // Feed de Publicaciones Reales Paginadas
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(postsList) { index, post ->
                        val postId = post.id ?: "post_$index"
                        val isLiked = likedPostIds.contains(postId)

                        RealPostCard(
                            post = post,
                            isLiked = isLiked,
                            onToggleLike = {
                                likedPostIds = if (isLiked) likedPostIds - postId else likedPostIds + postId
                            }
                        )

                        if (index < postsList.size - 1) {
                            HorizontalDivider(
                                color = Color(0xFFF3F4F6),
                                thickness = 6.dp
                            )
                        }
                    }

                    // Botón / Indicador para cargar más publicaciones (Paginación de 10 en 10)
                    if (hasMorePosts) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoadingMore) {
                                    CircularProgressIndicator(
                                        color = primaryColor,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    OutlinedButton(
                                        onClick = { loadPosts(reset = false) },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                                    ) {
                                        Text("Cargar más publicaciones", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    onCameraClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "COMUNIDAD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = "Mundo PetCare 🐾",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF1F2937)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6))
                    .clickable { onRefreshClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Actualizar",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8703A))
                    .clickable { onCameraClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = "Crear publicación",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun RealPostCard(
    post: PostEntity,
    isLiked: Boolean,
    onToggleLike: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // Header del Post (Usuario y Mascota)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val displayAvatar = if (!post.petName.isNullOrBlank() && !post.petAvatar.isNullOrEmpty()) post.petAvatar else post.userAvatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF3ED)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!displayAvatar.isNullOrEmpty()) {
                            AsyncImage(
                                model = displayAvatar,
                                contentDescription = post.userName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = (post.petName?.take(1) ?: post.userName?.take(1) ?: "U").uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE8703A),
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.userName ?: "Usuario",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            if (!post.petName.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFF3ED), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "🐾 ${post.petName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE8703A)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (!post.petName.isNullOrBlank()) "Publicó sobre ${post.petName}" else "Miembro de la comunidad",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = "Opciones",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Texto de la Publicación
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    fontSize = 14.sp,
                    color = Color(0xFF374151),
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Foto de Cloudinary (si existe)
            if (!post.photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = post.photoUrl,
                    contentDescription = "Foto de publicación",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .aspectRatio(1.33f)
                        .background(Color(0xFFF3F4F6)),
                    contentScale = ContentScale.Crop
                )
            }

            // Barra de Interacciones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Me gusta
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onToggleLike() }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Me gusta",
                            tint = if (isLiked) Color(0xFFEF4444) else Color(0xFF4B5563),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${post.likesCount + if (isLiked) 1 else 0}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isLiked) Color(0xFFEF4444) else Color(0xFF4B5563)
                        )
                    }

                    // Comentarios
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comentarios",
                            tint = Color(0xFF4B5563),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${post.commentsCount}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563)
                        )
                    }
                }

                if (!post.createdAt.isNullOrEmpty()) {
                    Text(
                        text = post.createdAt.take(10),
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}
