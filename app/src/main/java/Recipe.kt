package com.example.myapplication.model

data class Recipe(
    var id:String="",
    var title: String = "",
    var ingredients: String = "",
    var instructions: String = "",
    var servings: String = "",
    var prepTime: String = "",
    var cookTime: String = "",
    var imageUrl: String = "",
    var authorName: String = "",
    var category: String = "",
    var timestamp: Long = 0,
    var comments: Map<String, Comment>? = null // Yorumları saklamak için yeni alan
)
