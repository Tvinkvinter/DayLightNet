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

    val posts: StateFlow<List<Post>> = postsRepository.posts
    var currentUserData: User? = null

    private val _postCards = MutableStateFlow<List<PostCard>>(emptyList())
    val postCards: StateFlow<List<PostCard>> = _postCards

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorSharedFlow = MutableSharedFlow<Exception>()
    val errorSharedFlow: SharedFlow<Exception> = _errorSharedFlow

    init {
        viewModelScope.launch {
            posts.first { it.isNotEmpty() }
            getPostCards()
        }

        viewModelScope.launch(Dispatchers.Main) {
            usersRepository.currentUserId.collect {

                val userData = withContext(Dispatchers.IO) {
                    usersRepository.getCurrentUserDataOrNull()
                }

                currentUserData = userData
            }
        }
    }

    fun getPostCards() {
        viewModelScope.launch {
            _isLoading.value = true

            val newPostCards = mutableListOf<PostCard>()

            for (post in posts.value) {
                val author = withContext(Dispatchers.IO) {
                    usersRepository.getUserDataOrNullById(post.userId)
                }

                if (author != null) newPostCards.add(PostCard(post, author))
            }

            _postCards.value = newPostCards

            _isLoading.value = false
        }
    }

    fun handleLikeButtonClick(postCard: PostCard) {
        currentUserData?.let { user ->
            viewModelScope.launch(Dispatchers.IO) {
                val result = if (postCard.post.idsOfUsersLiked.contains(user.uid)) {
                    postsRepository.unlikePost(postCard.post, user.uid)
                } else {
                    postsRepository.likePost(postCard.post, user.uid)
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
        }
    }

    fun addTestPost() {
        val test_post = currentUserData?.let {
            Post(
                userId = it.uid,
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