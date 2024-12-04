package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.model.Comment
import com.example.myapplication.model.Recipe
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RecipeDetailFragment : Fragment() {

    private lateinit var recipeTitle: TextView
    private lateinit var recipeImage: ImageView
    private lateinit var authorName: TextView
    private lateinit var authorImage: ImageView
    private lateinit var servings: TextView
    private lateinit var prepTime: TextView
    private lateinit var cookTime: TextView
    private lateinit var ingredients: TextView
    private lateinit var instructions: TextView
    private lateinit var heartIcon: ImageView
    private lateinit var commentsContainer: LinearLayout
    private lateinit var etComment: EditText
    private lateinit var btnSendComment: ImageView
    private lateinit var tvNoComments: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var layoutRecipeDetails: ScrollView
    private var isFavorite = false
    private lateinit var recipeId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_detail, container, false)

        // UI bileşenlerini bağla
        recipeTitle = view.findViewById(R.id.recipeTitle)
        recipeImage = view.findViewById(R.id.recipeImage)
        authorName = view.findViewById(R.id.authorName)
        authorImage = view.findViewById(R.id.authorImage)
        servings = view.findViewById(R.id.servings)
        prepTime = view.findViewById(R.id.prepTime)
        cookTime = view.findViewById(R.id.cookTime)
        ingredients = view.findViewById(R.id.ingredients)
        instructions = view.findViewById(R.id.instructions)
        heartIcon = view.findViewById(R.id.heartIcon)
        commentsContainer = view.findViewById(R.id.commentsContainer)
        etComment = view.findViewById(R.id.etComment)
        btnSendComment = view.findViewById(R.id.btnSendComment)
        tvNoComments = view.findViewById(R.id.tvNoComments)
        tabLayout = view.findViewById(R.id.tabLayout)
        layoutRecipeDetails = view.findViewById(R.id.layoutRecipeDetails)
        authorName = view.findViewById(R.id.authorName)


        // Yorum yazma alanını gizle
        etComment.visibility = View.GONE
        btnSendComment.visibility = View.GONE

        // Argümanlardan recipeId'yi al
        recipeId = RecipeDetailFragmentArgs.fromBundle(requireArguments()).recipeId
        loadRecipeDetails(recipeId)

        // Favori ikonuna tıklama işlemi
        heartIcon.setOnClickListener {
            toggleFavorite(recipeId)
        }

        // Tab seçimi dinleyicisi
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showRecipeDetails()  // Tarif sekmesi seçildi
                    1 -> showComments()  // Yorumlar sekmesi seçildi
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Yorum gönderme işlemi
        btnSendComment.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser == null) {
                Toast.makeText(context, "Yorum yapmak için lütfen giriş yapınız.", Toast.LENGTH_SHORT).show()
            } else {
                addComment()
            }
        }

        return view
    }

    private fun showRecipeDetails() {
        layoutRecipeDetails.visibility = View.VISIBLE  // Tarif detayları görünür
        commentsContainer.visibility = View.GONE  // Yorumlar gizlenir

        // Yorum yazma alanını ve butonu gizle
        etComment.visibility = View.GONE
        btnSendComment.visibility = View.GONE
    }

    private fun showComments() {
        layoutRecipeDetails.visibility = View.GONE  // Tarif detayları gizlenir
        commentsContainer.visibility = View.VISIBLE  // Yorumlar görünür

        // Yorum yazma alanını ve butonunu her zaman görünür yap
        etComment.visibility = View.VISIBLE
        btnSendComment.visibility = View.VISIBLE

        loadComments(recipeId)
    }

    private fun loadRecipeDetails(recipeId: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("recipes")
        databaseRef.child(recipeId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipe = snapshot.getValue(Recipe::class.java)
                if (recipe != null) {
                    recipeTitle.text = recipe.title
                    authorName.text = recipe.authorName
                    servings.text = recipe.servings
                    prepTime.text = recipe.prepTime
                    cookTime.text = recipe.cookTime
                    ingredients.text = recipe.ingredients
                    instructions.text = recipe.instructions

                    // Yazarın profil resmini ve adını göster
                    loadAuthorDetails(recipe.authorName)

                    if (!recipe.imageUrl.isNullOrEmpty()) {
                        Picasso.get().load(recipe.imageUrl).into(recipeImage)
                    } else {
                        recipeImage.setImageResource(R.drawable.placeholder)
                        Toast.makeText(requireContext(), "Resim yüklenemedi", Toast.LENGTH_SHORT).show()
                    }

                    checkIfFavorite(recipe.id)
                } else {
                    Toast.makeText(requireContext(), "Tarif bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Veri yüklenemedi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadAuthorDetails(authorUsername: String) {
        // Firebase'den yazarın bilgilerini almak için referans oluşturuluyor
        val userRef = FirebaseDatabase.getInstance().getReference("users")

        // Firebase'deki "users" düğümünde username'e göre sorgulama yapıyoruz
        userRef.orderByChild("username").equalTo(authorUsername).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Eğer bu kullanıcı varsa, bilgilerini alıyoruz
                if (snapshot.exists()) {
                    // Sadece ilk kullanıcıyı alıyoruz çünkü "equalTo" sadece eşleşen değerleri döndürür
                    val userSnapshot = snapshot.children.iterator().next()

                    val username = userSnapshot.child("username").getValue(String::class.java) ?: "Bilinmeyen Kullanıcı"
                    val profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java)

                    // Yazar ismini "authorName" TextView'e set et
                    authorName.text = username

                    // Profil resmini yükle
                    if (profileImageUrl != null && profileImageUrl.isNotEmpty()) {
                        Picasso.get().load(profileImageUrl).into(authorImage)
                    } else {
                        authorImage.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                } else {
                    // Kullanıcı bulunamazsa varsayılan değerler
                    authorName.text = "Bilinmeyen Kullanıcı"
                    authorImage.setImageResource(R.drawable.ic_profile_placeholder)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunda bir mesaj göster
                Toast.makeText(requireContext(), "Yazar bilgileri yüklenemedi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun loadComments(recipeId: String) {
        val commentsRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId).child("comments")
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentsContainer.removeAllViews()

                etComment.visibility = View.VISIBLE
                btnSendComment.visibility = View.VISIBLE

                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(Comment::class.java)
                    comment?.let {
                        val commentView = LayoutInflater.from(requireContext()).inflate(R.layout.item_comment, commentsContainer, false)
                        val textViewUsername = commentView.findViewById<TextView>(R.id.textViewUsername)
                        val textViewComment = commentView.findViewById<TextView>(R.id.textViewComment)
                        val textViewTimestamp = commentView.findViewById<TextView>(R.id.textViewTimestamp)
                        val imageViewProfile = commentView.findViewById<ImageView>(R.id.imageViewProfile)

                        textViewUsername.text = it.username
                        textViewComment.text = it.content

                        // Yorum yapan kişinin profil resmini al
                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(it.userId)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val profileImageUrl = userSnapshot.child("profileImageUrl").getValue(String::class.java)
                                if (profileImageUrl != null && profileImageUrl.isNotEmpty()) {
                                    Picasso.get().load(profileImageUrl).into(imageViewProfile)
                                } else {
                                    imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(requireContext(), "Yorum yapan kişi bilgisi alınamadı", Toast.LENGTH_SHORT).show()
                            }
                        })

                        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        textViewTimestamp.text = dateFormat.format(Date(it.timestamp))

                        commentsContainer.addView(commentView)

                        val dividerView = View(requireContext())
                        dividerView.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 2
                        )
                        dividerView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
                        commentsContainer.addView(dividerView)
                    }
                }

                if (snapshot.childrenCount.toInt() == 0) {
                    tvNoComments.visibility = View.VISIBLE
                } else {
                    tvNoComments.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Yorumlar yüklenemedi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkIfFavorite(recipeId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favoriteRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("favorites").child(recipeId)

        favoriteRef.get().addOnSuccessListener { snapshot ->
            isFavorite = snapshot.exists()
            updateFavoriteIcon()
        }
    }

    private fun toggleFavorite(recipeId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favoriteRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("favorites").child(recipeId)

        if (isFavorite) {
            favoriteRef.removeValue().addOnSuccessListener {
                isFavorite = false
                updateFavoriteIcon()
                Toast.makeText(requireContext(), "Favorilerden çıkarıldı", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Favorilerden çıkarılamadı: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            favoriteRef.setValue(true).addOnSuccessListener {
                isFavorite = true
                updateFavoriteIcon()
                Toast.makeText(requireContext(), "Favorilere eklendi", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Favorilere eklenemedi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFavoriteIcon() {
        if (isFavorite) {
            heartIcon.setImageResource(R.drawable.ic_heart_filled)
            heartIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red))
        } else {
            heartIcon.setImageResource(R.drawable.ic_heart_outline)
            heartIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray))
        }
    }

    private fun addComment() {
        val commentText = etComment.text.toString()
        if (commentText.isNotEmpty()) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: return
            val commentId = FirebaseDatabase.getInstance().getReference("recipes")
                .child(recipeId)
                .child("comments")
                .push()
                .key

            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").getValue(String::class.java) ?: "Bilinmeyen Kullanıcı"

                    val comment = Comment(
                        userId = userId,
                        username = username,
                        content = commentText,
                        timestamp = System.currentTimeMillis(),
                        recipeId = recipeId
                    )

                    if (commentId != null) {
                        val commentsRef = FirebaseDatabase.getInstance().getReference("recipes")
                            .child(recipeId)
                            .child("comments")
                            .child(commentId)
                        commentsRef.setValue(comment).addOnSuccessListener {
                            Toast.makeText(context, "Yorum eklendi", Toast.LENGTH_SHORT).show()
                            etComment.text.clear()

                            val userCommentsRef = FirebaseDatabase.getInstance().getReference("users")
                                .child(userId)
                                .child("comments")
                                .child(commentId)
                            userCommentsRef.setValue(comment)
                        }.addOnFailureListener {
                            Toast.makeText(context, "Yorum tarif kaydına eklenemedi: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Kullanıcı adı alınamadı: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(context, "Yorum boş olamaz", Toast.LENGTH_SHORT).show()
        }
    }
}
