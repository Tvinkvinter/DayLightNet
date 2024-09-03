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
import com.atarusov.daylightnet.model.User
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val postsRepository: PostsRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    val posts: StateFlow<List<Post>> = postsRepository.posts
    var currentUserData: User? = null

    init {
        viewModelScope.launch {
            //currentUserData = usersRepository.getCurrentUserDataOrNull()
            usersRepository.currentUserId.collect {
                currentUserData = usersRepository.getCurrentUserDataOrNull()
            }
        }
    }

    fun handleLikeButtonClick(post: Post) {
        val current_user = currentUserData
        viewModelScope.launch {
            if (current_user != null) {
                if (post.idsOfUsersLiked.contains(current_user.uid)) {
                    postsRepository.unlikePost(post, current_user.uid)
                } else {
                    postsRepository.likePost(post, current_user.uid)
                }
            }
        }
    }

    fun addPost(post: Post) {
        viewModelScope.launch {
            postsRepository.addPost(post)
        }
    }

    fun addTestPost(){
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