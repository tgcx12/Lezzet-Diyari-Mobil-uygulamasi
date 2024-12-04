package com.example.myapplication

import com.example.myapplication.model.Recipe // Recipe sınıfını doğru yoldan içe aktarın

interface RecipeListener {
    fun onRecipeAdded(recipe: Recipe)
    fun onRecipeAddFailed(message: String)
}
