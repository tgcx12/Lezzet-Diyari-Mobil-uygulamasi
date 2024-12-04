package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.model.Recipe
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class RecipeAdapter(
    private val recipeList: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeTitle: TextView = itemView.findViewById(R.id.recipe_title)
        private val recipeAuthor: TextView = itemView.findViewById(R.id.recipe_author)
        private val recipeCategory: TextView = itemView.findViewById(R.id.recipe_category)
        private val recipeTimestamp: TextView = itemView.findViewById(R.id.recipe_timestamp)
        private val recipeImage: ImageView = itemView.findViewById(R.id.recipe_image)

        fun bind(recipe: Recipe) {
            recipeTitle.text = recipe.title
            recipeAuthor.text = recipe.authorName ?: "Unknown"
            recipeCategory.text = recipe.category

            // Timestamp'i formatlayarak göster
            val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
            recipeTimestamp.text = dateFormat.format(recipe.timestamp)

            // Resmi yükle
            Picasso.get()
                .load(recipe.imageUrl)
                .error(R.drawable.placeholder)
                .into(recipeImage)

            // Tıklama olayını ayarla
            itemView.setOnClickListener { onItemClick(recipe) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipeList[position])
    }

    override fun getItemCount(): Int = recipeList.size
}
