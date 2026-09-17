package com.example.petcare360.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare360.data.model.PostEntity
import com.example.petcare360.data.remote.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ComunidadUiState(
    val posts: List<PostEntity> = emptyList(),
    val likedPostIds: Set<String> = emptySet(),
    val isLoadingInitial: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMorePosts: Boolean = true,
    val errorMessage: String? = null
)

class ComunidadViewModel(
    private val supabaseClient: SupabaseClient?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComunidadUiState())
    val uiState: StateFlow<ComunidadUiState> = _uiState.asStateFlow()

    private var currentOffset = 0
    private val PAGE_SIZE = 10

    init {
        loadPosts(reset = true)
    }

    fun loadPosts(reset: Boolean = false) {
        if (supabaseClient == null) {
            _uiState.update { it.copy(isLoadingInitial = false) }
            return
        }

        viewModelScope.launch {
            val offset = if (reset) 0 else currentOffset
            if (reset) {
                _uiState.update { it.copy(isLoadingInitial = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            val result = supabaseClient.getPosts(limit = PAGE_SIZE, offset = offset)
            
            result.onSuccess { newPosts ->
                if (reset) {
                    currentOffset = newPosts.size
                    _uiState.update {
                        it.copy(
                            posts = newPosts,
                            isLoadingInitial = false,
                            isLoadingMore = false,
                            hasMorePosts = newPosts.size == PAGE_SIZE
                        )
                    }
                } else {
                    currentOffset += newPosts.size
                    _uiState.update {
                        it.copy(
                            posts = it.posts + newPosts,
                            isLoadingInitial = false,
                            isLoadingMore = false,
                            hasMorePosts = newPosts.size == PAGE_SIZE
                        )
                    }
                }
            }.onFailure { error ->
                if (reset) {
                    _uiState.update {
                        it.copy(
                            isLoadingInitial = false,
                            isLoadingMore = false,
                            errorMessage = error.localizedMessage ?: "Error al cargar la comunidad"
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
            }
        }
    }

    fun toggleLike(postId: String) {
        _uiState.update { state ->
            val isLiked = state.likedPostIds.contains(postId)
            val newLikedPostIds = if (isLiked) {
                state.likedPostIds - postId
            } else {
                state.likedPostIds + postId
            }
            state.copy(likedPostIds = newLikedPostIds)
        }
    }

    fun addPost(post: PostEntity) {
        _uiState.update {
            it.copy(posts = listOf(post) + it.posts)
        }
    }
}
