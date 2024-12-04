package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.model.Category
import com.example.myapplication.model.Recipe
import com.google.firebase.database.*

class CategoriesFragment : Fragment() {

    private lateinit var expandableListView: ExpandableListView
    private lateinit var customExpandableListAdapter: CustomExpandableListAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expandableListView = view.findViewById(R.id.expandableListView)
        loadCategories()  // Kategorileri yüklemeye başla
    }

    private fun loadCategories() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("categories")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categories.clear()
                for (categorySnapshot in snapshot.children) {
                    // Kategorinin değerini alıyoruz
                    val categoryValue = categorySnapshot.value.toString() // Kategori değerini al
                    val categoryName = categoryValue // Kategorinin adı artık kategori değeridir.

                    val categoryRecipes = mutableListOf<Recipe>()

                    // Kategoriye ait tarifleri al
                    val recipeRef = FirebaseDatabase.getInstance().getReference("recipes")
                    recipeRef.orderByChild("category").equalTo(categoryValue).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(recipeSnapshot: DataSnapshot) {
                            for (recipe in recipeSnapshot.children) {
                                val recipeObj = recipe.getValue(Recipe::class.java)
                                recipeObj?.let { categoryRecipes.add(it) }
                            }
                            // Kategorinin adı ve tarifleri ile Category objesini ekliyoruz
                            categories.add(Category(categoryName, categoryRecipes))
                            updateExpandableListView() // Görünümü güncelle
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Tarifler yüklenemedi: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Kategoriler yüklenemedi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateExpandableListView() {
        customExpandableListAdapter = CustomExpandableListAdapter(requireContext(), categories)
        expandableListView.setAdapter(customExpandableListAdapter)

        expandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val recipe = categories[groupPosition].recipes[childPosition]
            navigateToRecipeDetail(recipe.id) // Tarif detay sayfasına geçiş yap
            true
        }
    }

    private fun navigateToRecipeDetail(recipeId: String) {
        val action = CategoriesFragmentDirections.actionCategoriesFragmentToRecipeDetailFragment(recipeId)
        findNavController().navigate(action)
    }
}
