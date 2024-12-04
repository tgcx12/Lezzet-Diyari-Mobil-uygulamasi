package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.model.Recipe
import com.google.firebase.database.*

class RecipeFragment : Fragment() {

    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private var recipeList: MutableList<Recipe> = mutableListOf()
    private lateinit var recipesRef: DatabaseReference

    // onCreateView fonksiyonu, fragment'in arayüzünü oluşturur
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipe, container, false)
    }

    // onViewCreated fonksiyonu, fragment'in arayüzü oluşturulduktan sonra çağrılır
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView ve adapter ayarları
        recipeRecyclerView = view.findViewById(R.id.recipe_recycler_view)
        recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Adapter'i kur ve tıklama olayını ayarla
        recipeAdapter = RecipeAdapter(recipeList) { recipe ->
            val action = RecipeFragmentDirections.actionRecipeFragmentToRecipeDetailFragment(recipe.id)
            Log.d("RecipeFragment", "Navigating to detail with ID: ${recipe.id}") // Log ekleyin
            findNavController().navigate(action)
        }
        recipeRecyclerView.adapter = recipeAdapter

        // Firebase'den tarifleri yükle
        recipesRef = FirebaseDatabase.getInstance().getReference("recipes")
        fetchRecipes()
    }

    // Firebase'den tarifleri çeken fonksiyon
    private fun fetchRecipes() {
        recipesRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                recipeList.clear() // Önceki tarifleri temizle
                for (recipeSnapshot in snapshot.children) {
                    val recipe = recipeSnapshot.getValue(Recipe::class.java)
                    recipe?.let {
                        Log.d("RecipeFragment", "Recipe ID: ${it.id}") // Log ekleyin
                        recipeList.add(it) // Tarif nesnesini listeye ekle
                    }
                }
                recipeAdapter.notifyDataSetChanged() // Liste güncelle
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RecipeFragment", "Veritabanı hatası: ${error.message}")
            }
        })
    }
}
