package com.example.myapplication.model
data class User(
    val userId: String = "",
    val email: String = "",
    val userType: String = "",
    val username: String = "",
    val favorites: List<String>? = null, // Favoriler için bir alan

)
