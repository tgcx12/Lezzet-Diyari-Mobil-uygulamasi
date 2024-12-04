package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.model.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoritesFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private var recipeList: MutableList<Recipe> = mutableListOf()
    private lateinit var favoritesRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipeRecyclerView = view.findViewById(R.id.recipe_recycler_view)
        recipeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2) // 2 sütun
        recipeAdapter = RecipeAdapter(recipeList) { recipe ->
            val action = FavoritesFragmentDirections.actionFavoritesFragmentToRecipeDetailFragment(recipe.id)
            findNavController().navigate(action)
        }
        recipeRecyclerView.adapter = recipeAdapter

        // Kullanıcı giriş durumunu kontrol et
        if (auth.currentUser == null) {
            view.findViewById<TextView>(R.id.loginPrompt).visibility = View.VISIBLE
            view.findViewById<Button>(R.id.loginButton).visibility = View.VISIBLE
            view.findViewById<Button>(R.id.loginButton).setOnClickListener {
                findNavController().navigate(R.id.action_favoritesFragment_to_loginFragment)
            }
        } else {
            // Kullanıcı giriş yaptıysa favori tarifleri yükle
            favoritesRef = FirebaseDatabase.getInstance().getReference("users").child(auth.currentUser!!.uid).child("favorites")
            loadFavoriteRecipes()
        }
    }

    private fun loadFavoriteRecipes() {
        favoritesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recipeList.clear() // Önceki tarifleri temizle
                if (snapshot.exists()) {
                    for (favoriteSnapshot in snapshot.children) {
                        val recipeId = favoriteSnapshot.key // Favori tariflerin ID'si
                        recipeId?.let {
                            fetchRecipeDetails(it) // Tarifin detaylarını yükle
                        } ?: run {
                            Log.e("FavoritesFragment", "Favori tarif ID'si null")
                        }
                    }
                } else {
                    Log.e("FavoritesFragment", "Favori tarifler bulunamadı.")
                    view?.findViewById<TextView>(R.id.emptyView)?.visibility = View.VISIBLE // Favori yok mesajı
                    recipeRecyclerView.visibility = View.GONE // RecyclerView'u gizle
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FavoritesFragment", "Favoriler yüklenemedi: ${error.message}")
                Toast.makeText(requireContext(), "Favoriler yüklenemedi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchRecipeDetails(recipeId: String) {
        val recipeRef = FirebaseDatabase.getInstance().getReference("recipes").orderByChild("id").equalTo(recipeId)
        recipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (recipeSnapshot in snapshot.children) {
                        val recipe = recipeSnapshot.getValue(Recipe::class.java)
                        recipe?.let {
                            recipeList.add(it) // Tarifi listeye ekle
                            Log.d("FavoritesFragment", "Favori tarif eklendi: ${it.title}")
                        }
                    }
                    recipeAdapter.notifyDataSetChanged() // Adapteri güncelle
                    view?.findViewById<TextView>(R.id.emptyView)?.visibility = View.GONE // Favori yok mesajını gizle
                    recipeRecyclerView.visibility = View.VISIBLE // RecyclerView'u göster
                } else {
                    Log.e("FavoritesFragment", "Tarif bulunamadı: $recipeId")
                    Toast.makeText(requireContext(), "Tarif bulunamadı: $recipeId", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FavoritesFragment", "Tarif yüklenemedi: ${error.message}")
                Toast.makeText(requireContext(), "Tarif yüklenemedi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

