package com.atarusov.daylightnet.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.atarusov.App
import com.atarusov.daylightnet.data.PostsRepository
import com.atarusov.daylightnet.data.UsersRepository
import com.atarusov.daylightnet.model.Post
import com.atarusov.daylightnet.model.PostCard
import com.atarusov.daylightnet.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val postsRepository: PostsRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    val posts: StateFlow<List<Post>?> = postsRepository.posts
    val users: StateFlow<List<User>> = usersRepository.users
    var currentUserId: StateFlow<String?> = usersRepository.currentUserId

    private val _postCards = MutableStateFlow<List<PostCard>>(emptyList())
    val postCards: StateFlow<List<PostCard>> = _postCards

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _noPostsMessage = MutableSharedFlow<Boolean>()
    val noPostsMessage: SharedFlow<Boolean> = _noPostsMessage

    private val _scrollUpEvent = MutableSharedFlow<Boolean>()
    val scrollUpEvent: SharedFlow<Boolean> = _scrollUpEvent

    private val _errorSharedFlow = MutableSharedFlow<Exception>()
    val errorSharedFlow: SharedFlow<Exception> = _errorSharedFlow

    init {
        viewModelScope.launch {
            _isLoading.value = true
            posts.first { it != null }
            users.first { it.isNotEmpty() }
            getPostCards()
            _isLoading.value = false
        }
    }

    fun getPostCards() {
        viewModelScope.launch {

            val newPostCards = mutableListOf<PostCard>()
            posts.value?.let { posts ->
                for (post in posts) {
                    val author = users.value.find { it.uid == post.userId }

                    if (author != null) newPostCards.add(PostCard(post, author))
                }

                _postCards.value = newPostCards
            }

            if (newPostCards.isEmpty()) _noPostsMessage.emit(true)
            else _noPostsMessage.emit(false)
        }
    }

    fun handleLikeButtonClick(postCard: PostCard) {
        currentUserId.value?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                val result = if (postCard.post.idsOfUsersLiked.contains(id)) {
                    postsRepository.unlikePost(postCard.post, id)
                } else {
                    postsRepository.likePost(postCard.post, id)
                }

                if (result.isFailure)
                    withContext(Dispatchers.Main) {
                        _errorSharedFlow.emit(result.exceptionOrNull() as Exception)
                    }
            }
        }
    }

    fun addPost(post: Post) {
        viewModelScope.launch(Dispatchers.IO) {
            postsRepository.addPost(post)
            _scrollUpEvent.emit(true)
            getPostCards()
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
        } else {
            // TODO() проработать логи и ошибки
            Log.i("Posts", "Error")
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val postsRepository = (this[APPLICATION_KEY] as App).postsRepository
                val usersRepository = (this[APPLICATION_KEY] as App).usersRepository
                HomeViewModel(
                    postsRepository = postsRepository,
                    usersRepository = usersRepository
                )
            }
        }
    }
}