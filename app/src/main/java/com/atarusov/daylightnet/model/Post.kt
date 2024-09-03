package com.atarusov.daylightnet.model

import java.util.UUID

data class Post(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val idsOfUsersLiked: MutableList<String> = mutableListOf(),
    val likes: Int = idsOfUsersLiked.size,
)