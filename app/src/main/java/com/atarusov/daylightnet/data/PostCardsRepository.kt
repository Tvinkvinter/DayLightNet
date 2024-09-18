package com.atarusov.daylightnet.data

import android.util.Log
import com.atarusov.daylightnet.model.Post
import com.atarusov.daylightnet.model.PostCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class PostCardsRepository(
    private val postsRepository: PostsRepository,
    private val usersRepository: UsersRepository
) {
    val posts: StateFlow<List<Post>> = postsRepository.posts

    private val _postCards = MutableStateFlow<List<PostCard>>(emptyList())
    val postCards: StateFlow<List<PostCard>> = _postCards

    suspend fun getPostCards(): Result<String> {
        withContext(Dispatchers.IO) {
            postsRepository.fetchAllPosts()
        }
        Log.d(TAG, "Start getting post cards")
        _postCards.value = posts.value.mapNotNull { post ->
            val author = withContext(Dispatchers.IO) {
                usersRepository.getUserDataOrNullById(post.userId)
            }
            if (author != null) {
                val isPostLikedByCurrentUser =
                    post.idsOfUsersLiked.contains(usersRepository.currentUserId.value)
                PostCard(post, author, isPostLikedByCurrentUser)
            } else {
                Log.w(
                    TAG,
                    "Post Card for post with ID: ${post.id} is not created because its author is not found"
                )
                null
            }
        }
        return Result.success("${postCards.value.size} Post cards are created")
    }

    suspend fun likeOrUnlikePostCard(
        postCard: PostCard,
        actionLike: Boolean,
        onLocalChangesPerformed: () -> Unit
    ): Result<String> {
        val userId = usersRepository.currentUserId.value
        return if (userId != null) {
            // Updating list of likes locally
            val updatedIdsOfUsersLiked = postCard.post.idsOfUsersLiked.toMutableList()
                .also { if (actionLike) it.add(userId) else it.remove(userId) }
            val updatedPost = postCard.post.copy(idsOfUsersLiked = updatedIdsOfUsersLiked)
            val updatedPostCard = PostCard(
                post = updatedPost,
                author = postCard.author,
                isLikedByCurrentUser = actionLike
            )
            updatePostCardLocally(updatedPostCard)
            onLocalChangesPerformed()

            // Making a request to firestore
            val result = withContext(Dispatchers.IO) {
                if (actionLike) postsRepository.likePost(postCard.post, userId)
                else postsRepository.unlikePost(postCard.post, userId)
            }

            if (result.isFailure) {
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error occurred"))
            } else {
                Result.success("Like or unlike action was successfully sent to Firestore")
            }
        } else {
            Log.w(TAG, "Like or unlike action wasn't handled because currentUserId is null")
            Result.failure(IllegalStateException("Current user ID is null"))
        }
    }

    fun updatePostCardLocally(updatedPostCard: PostCard) {
        val postCardToUpdate = postCards.value.find { it.post.id == updatedPostCard.post.id }
        val indexOfPostCardToUpdate = postCards.value.indexOf(postCardToUpdate)
        val updatedPostCards = postCards.value.toMutableList()
        updatedPostCards[indexOfPostCardToUpdate] = updatedPostCard
        _postCards.value = updatedPostCards
    }

    companion object {
        val TAG = PostCardsRepository::class.java.simpleName
    }
}
