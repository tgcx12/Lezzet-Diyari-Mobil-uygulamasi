package com.example.myapplication.model

data class Category(
    var name: String = "",
    var recipes: List<Recipe> = emptyList() // Tarifleri tutmak için
)
