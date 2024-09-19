package com.atarusov.daylightnet.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atarusov.App
import com.atarusov.daylightnet.data.PostCardsRepository
import com.atarusov.daylightnet.data.PostsRepository
import com.atarusov.daylightnet.data.UsersRepository
import com.atarusov.daylightnet.model.Post
import com.atarusov.daylightnet.model.PostCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val postsRepository: PostsRepository,
    private val usersRepository: UsersRepository,
    private val postCardsRepository: PostCardsRepository,
) : ViewModel() {

    sealed class UiState {
        data object Loading : UiState()
        data class ShowingPostCards(val postCards: List<PostCard>) : UiState()
        data object ShowingNoPostsMessage : UiState()
    }

    val currentUserId: StateFlow<String?> = usersRepository.currentUserId
    val postCards: StateFlow<List<PostCard>> = postCardsRepository.postCards

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _scrollUpEvent = MutableSharedFlow<Boolean>()
    val scrollUpEvent: SharedFlow<Boolean> = _scrollUpEvent

    private val _errorSharedFlow = MutableSharedFlow<Exception>()
    val errorSharedFlow: SharedFlow<Exception> = _errorSharedFlow

    init {
        loadPostCards()
    }

    private fun showLoadingAnim() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
        }
    }

    private fun showPostCards() {
        viewModelScope.launch {
            _uiState.value = UiState.ShowingPostCards(postCards.value)
            _scrollUpEvent.emit(true)
        }
    }

    private fun showNoPostsMessage() {
        viewModelScope.launch {
            _uiState.value = UiState.ShowingNoPostsMessage
        }
    }

    fun loadPostCards() {
        viewModelScope.launch(Dispatchers.Main) {
            postCardsRepository.getPostCards()
            if (postCards.value.isEmpty()) showNoPostsMessage()
            else showPostCards()
        }
    }

    fun handleLikeButtonClick(postCard: PostCard) {
        val isActionLike =
            !postCard.post.idsOfUsersLiked.contains(usersRepository.currentUserId.value)
        viewModelScope.launch {
            val result = postCardsRepository.likeOrUnlikePostCard(postCard, isActionLike) {
                showPostCards()
            }
            if (result.isFailure) {
                _errorSharedFlow.emit(result.exceptionOrNull() as Exception)
                // Synchronization with firestore
                loadPostCards()
            }
        }
    }

    fun addPost(post: Post) {
        viewModelScope.launch(Dispatchers.IO) {
            postsRepository.addPost(post)
            withContext(Dispatchers.Main) {
                loadPostCards()
            }
        }
    }

    fun addTestPost() {
        val test_post = currentUserId.value?.let {
            Post(
                userId = it,
                content = "teeeest",
            )
        }

        if (test_post != null) {
            addPost(test_post)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val postsRepository = (this[APPLICATION_KEY] as App).postsRepository
                val usersRepository = (this[APPLICATION_KEY] as App).usersRepository
                val postCardsRepository = (this[APPLICATION_KEY] as App).postCardsRepository
                HomeViewModel(
                    postsRepository = postsRepository,
                    usersRepository = usersRepository,
                    postCardsRepository = postCardsRepository,
                )
            }
        }
    }
}