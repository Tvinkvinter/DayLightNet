package com.atarusov.daylightnet.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

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
        loadAndShowPostCards(true)
    }

    private fun showLoadingAnim() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
        }
    }

    private fun showPostCards() {
        viewModelScope.launch {
            _uiState.value = UiState.ShowingPostCards(postCards.value)
        }
    }

    private fun showNoPostsMessage() {
        viewModelScope.launch {
            _uiState.value = UiState.ShowingNoPostsMessage
        }
    }

    private fun scrollUpPage() {
        viewModelScope.launch {
            _scrollUpEvent.emit(true)
        }
    }

    fun loadAndShowPostCards(withScrollUp: Boolean, onFinishListener: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.Main) {
            postCardsRepository.getPostCards()
            if (postCards.value.isEmpty()) showNoPostsMessage()
            else {
                showPostCards()
                if (withScrollUp) scrollUpPage()
            }
            if (onFinishListener != null) onFinishListener()
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
                loadAndShowPostCards(false)
            }
        }
    }

    fun addPost(text: String) {
        val newPost = currentUserId.value?.let {
            Post(
                userId = it,
                content = text
            )
        }

        showLoadingAnim()
        viewModelScope.launch(Dispatchers.IO) {
            if (newPost != null) {
                postsRepository.addPost(newPost)
                withContext(Dispatchers.Main) {
                    loadAndShowPostCards(true)
                }
            } else {
                // emitting unexpected exception
                _errorSharedFlow.emit(Exception())
            }
        }
    }

    class Factory @Inject constructor(
        private val postsRepository: PostsRepository,
        private val usersRepository: UsersRepository,
        private val postCardsRepository: PostCardsRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == HomeViewModel::class.java) {
                return HomeViewModel(postsRepository, usersRepository, postCardsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}