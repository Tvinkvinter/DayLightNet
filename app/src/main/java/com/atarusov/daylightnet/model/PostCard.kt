package com.atarusov.daylightnet.model

data class PostCard(
    val post: Post,
    val author: User,
    val isLikedByCurrentUser: Boolean
)