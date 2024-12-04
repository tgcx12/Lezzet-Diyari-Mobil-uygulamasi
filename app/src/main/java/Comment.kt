package com.example.myapplication.model

data class Comment(
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val recipeId: String = "",
    val profileImageUrl: String = "" // Profil resim URL'si
)
