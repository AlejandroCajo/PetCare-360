package com.example.petcare360.ui

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

data class Story(
    val id: String,
    val name: String,
    val img: String
)

data class Post(
    val id: Int,
    val user: String,
    val pet: String,
    val time: String,
    val avatar: String,
    val text: String,
    val img: String? = null,
    val likes: Int,
    val comments: Int
)

val SAMPLE_STORIES = listOf(
    Story(
        id = "1",
        name = "Luna",
        img = "https://images.unsplash.com/photo-1537151625747-768eb6cf92b2?w=100&h=100&fit=crop&auto=format"
    ),
    Story(
        id = "2",
        name = "Thor",
        img = "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=100&h=100&fit=crop&auto=format"
    ),
    Story(
        id = "3",
        name = "Mochi",
        img = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=100&h=100&fit=crop&auto=format"
    ),
    Story(
        id = "4",
        name = "Rocky",
        img = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=100&h=100&fit=crop&auto=format"
    )
)

val SAMPLE_POSTS = listOf(
    Post(
        id = 1,
        user = "Ana Rodríguez",
        pet = "Luna",
        time = "Hace 2h",
        avatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop&auto=format",
        text = "¡Día de parque increíble! Luna hizo un nuevo amigo y no quería volver a casa. 🐕✨",
        img = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=600&h=350&fit=crop&auto=format",
        likes = 24,
        comments = 5
    ),
    Post(
        id = 2,
        user = "Carlos Mendoza",
        pet = "Thor",
        time = "Hace 5h",
        avatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&auto=format",
        text = "Recordatorio: La hidratación en los paseos de tarde es fundamental, especialmente con este clima. Traigan siempre su botella plegable. 💧🐶",
        likes = 42,
        comments = 12
    ),
    Post(
        id = 3,
        user = "Elena Gómez",
        pet = "Mochi",
        time = "Hace 8h",
        avatar = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop&auto=format",
        text = "Mochi estrenando su nueva plaquita de identificación. Quedó preciosa. 💕🐱",
        img = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600&h=350&fit=crop&auto=format",
        likes = 18,
        comments = 3
    )
)

/**
 * Sobrecarga Stateful de ComunidadScreen con soporte Cloudinary.
 */
@Composable
fun ComunidadScreen(
    cloudinaryManager: com.example.petcare360.data.remote.CloudinaryManager? = null,
    userName: String = "Alejandro",
    modifier: Modifier = Modifier
) {
    var likedPostIds by remember { mutableStateOf(setOf<Int>()) }
    var bookmarkedPostIds by remember { mutableStateOf(setOf<Int>()) }
    var postsList by remember { mutableStateOf(SAMPLE_POSTS) }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    if (showCreatePostDialog && cloudinaryManager != null) {
        com.example.petcare360.ui.components.CreatePostDialog(
            cloudinaryManager = cloudinaryManager,
            userName = userName,
            onDismiss = { showCreatePostDialog = false },
            onPostCreated = { text, photoUrl ->
                showCreatePostDialog = false
                val newPost = Post(
                    id = (postsList.maxOfOrNull { it.id } ?: 0) + 1,
                    user = userName,
                    pet = "Mascota",
                    time = "Hace un momento",
                    avatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop",
                    text = text,
                    img = photoUrl,
                    likes = 0,
                    comments = 0
                )
                postsList = listOf(newPost) + postsList
            }
        )
    }

    ComunidadScreen(
        likedPostIds = likedPostIds,
        onToggleLike = { postId ->
            likedPostIds = if (likedPostIds.contains(postId)) {
                likedPostIds - postId
            } else {
                likedPostIds + postId
            }
        },
        bookmarkedPostIds = bookmarkedPostIds,
        onToggleBookmark = { postId ->
            bookmarkedPostIds = if (bookmarkedPostIds.contains(postId)) {
                bookmarkedPostIds - postId
            } else {
                bookmarkedPostIds + postId
            }
        },
        posts = postsList,
        onCameraClick = { showCreatePostDialog = true },
        onAddStoryClick = { showCreatePostDialog = true },
        modifier = modifier
    )
}

/**
 * Sobrecarga Stateless de ComunidadScreen equivalente a la versión de React.
 */
@Composable
fun ComunidadScreen(
    likedPostIds: Set<Int>,
    onToggleLike: (Int) -> Unit,
    bookmarkedPostIds: Set<Int>,
    onToggleBookmark: (Int) -> Unit,
    modifier: Modifier = Modifier,
    stories: List<Story> = SAMPLE_STORIES,
    posts: List<Post> = SAMPLE_POSTS,
    onAddStoryClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onStoryClick: (Story) -> Unit = {},
    onCommentClick: (Post) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Encabezado de la Comunidad
        HeaderSection(
            onCameraClick = onCameraClick
        )

        // 2. Historias (Stories Row)
        StoriesSection(
            stories = stories,
            onAddStoryClick = onAddStoryClick,
            onStoryClick = onStoryClick
        )

        // 3. Feed de Publicaciones
        FeedSection(
            posts = posts,
            likedPostIds = likedPostIds,
            onToggleLike = onToggleLike,
            bookmarkedPostIds = bookmarkedPostIds,
            onToggleBookmark = onToggleBookmark,
            onCommentClick = onCommentClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HeaderSection(
    onCameraClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Comunidad",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE8703A))
                .clickable { onCameraClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = "Crear publicación",
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun StoriesSection(
    stories: List<Story>,
    onAddStoryClick: () -> Unit,
    onStoryClick: (Story) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ítem "Tu historia"
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onAddStoryClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar historia",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Tu historia",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Historias de Mascotas
            items(stories) { story ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onStoryClick(story) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFE8703A),
                                        Color(0xFFF59E0B)
                                    )
                                )
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = story.img,
                            contentDescription = story.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text(
                        text = story.name,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(
            color = Color(0xFFE5E7EB),
            thickness = 1.dp
        )
    }
}

@Composable
private fun FeedSection(
    posts: List<Post>,
    likedPostIds: Set<Int>,
    onToggleLike: (Int) -> Unit,
    bookmarkedPostIds: Set<Int>,
    onToggleBookmark: (Int) -> Unit,
    onCommentClick: (Post) -> Unit
) {
    Column {
        posts.forEachIndexed { index, post ->
            val isLiked = likedPostIds.contains(post.id)
            val isBookmarked = bookmarkedPostIds.contains(post.id)
            val currentLikes = post.likes + if (isLiked) 1 else 0

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Encabezado del Post
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = post.avatar,
                        contentDescription = post.user,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.user,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${post.pet} · ${post.time}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.MoreHoriz,
                        contentDescription = "Opciones",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { /* onMoreClick */ },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Texto del Post
                Text(
                    text = post.text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp)
                )

                // Imagen adjunta (si aplica)
                if (post.img != null) {
                    AsyncImage(
                        model = post.img,
                        contentDescription = "Publicación de comunidad",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                }

                // Barra de Acciones (Like, Comentarios, Guardar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Botón Me gusta
                    Row(
                        modifier = Modifier.clickable { onToggleLike(post.id) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Me gusta",
                            modifier = Modifier.size(18.dp),
                            tint = if (isLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currentLikes",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Botón Comentarios
                    Row(
                        modifier = Modifier.clickable { onCommentClick(post) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comentarios",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${post.comments}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Botón Guardar
                    Icon(
                        imageVector = if (isBookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Guardar",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onToggleBookmark(post.id) },
                        tint = if (isBookmarked) Color(0xFFE8703A) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (index < posts.size - 1) {
                    HorizontalDivider(
                        color = Color(0xFFE5E7EB),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ComunidadScreenPreview() {
    PetCare360Theme {
        ComunidadScreen()
    }
}
