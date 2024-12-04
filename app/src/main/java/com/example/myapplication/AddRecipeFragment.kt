package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.model.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.util.Log
import org.w3c.dom.Text
import java.util.*

class AddRecipeFragment : Fragment() {

    private lateinit var recipeNameEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var servingsSpinner: Spinner
    private lateinit var prepTimeSpinner: Spinner
    private lateinit var cookTimeSpinner: Spinner
    private lateinit var ingredientsEditText: EditText
    private lateinit var instructionsEditText: EditText
    private lateinit var uploadImageButton: Button
    private lateinit var recipeImageView: ImageView
    private lateinit var addRecipeButton: Button
    private lateinit var loginButton: Button
    private lateinit var loginPrompt: TextView
    private lateinit var recipeNameLabel:TextView
    private lateinit var categoryLabel:TextView
    private lateinit var servingsLabel:TextView
    private lateinit var prepTimeLabel:TextView
    private lateinit var cookTimeLabel:TextView
    private lateinit var ingredientsLabel:TextView
    private lateinit var instructionsLabel:TextView


    private var recipeImageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storageRef: StorageReference

    private var isRecipeAlreadyAdded: Boolean = false  // Bu değişken tarifin zaten eklenip eklenmediğini kontrol eder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_recipe, container, false)

        // UI bileşenlerini başlat
        recipeNameEditText = view.findViewById(R.id.recipeNameEditText)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        servingsSpinner = view.findViewById(R.id.servingsSpinner)
        prepTimeSpinner = view.findViewById(R.id.prepTimeSpinner)
        cookTimeSpinner = view.findViewById(R.id.cookTimeSpinner)
        ingredientsEditText = view.findViewById(R.id.ingredientsEditText)
        instructionsEditText = view.findViewById(R.id.instructionsEditText)
        uploadImageButton = view.findViewById(R.id.uploadImageButton)
        recipeImageView = view.findViewById(R.id.recipeImageView)
        addRecipeButton = view.findViewById(R.id.addRecipeButton)
        loginButton = view.findViewById(R.id.loginButton)
        loginPrompt = view.findViewById(R.id.loginPrompt)
        recipeNameLabel= view.findViewById(R.id.recipeNameLabel)
        categoryLabel= view.findViewById(R.id.categoryLabel)
        servingsLabel= view.findViewById(R.id.servingsLabel)
        prepTimeLabel= view.findViewById(R.id.prepTimeLabel)
        cookTimeLabel= view.findViewById(R.id.cookTimeLabel)
        ingredientsLabel= view.findViewById(R.id.ingredientsLabel)
        instructionsLabel= view.findViewById(R.id.instructionsLabel)

        // Firebase öğelerini başlat
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("recipes")

        // Kullanıcı giriş durumunu kontrol et
        checkUserLoginStatus()

        // Kategorileri yükle
        fetchCategories()

        // Spinner'ları ayarla
        setupSpinners()

        // Resim Yükle butonunu ayarla
        uploadImageButton.setOnClickListener {
            openImagePicker()
        }

        // Tarif ekle butonunu ayarla
        addRecipeButton.setOnClickListener {
            if (auth.currentUser != null) {
                if (validateFields() && !isRecipeAlreadyAdded) {
                    addRecipe() // Giriş yaptıysa tarif ekleme işlemi yapılır
                } else if (isRecipeAlreadyAdded) {
                    Log.e("AddRecipeFragment", "Bu tarif zaten eklenmiş!")
                    Toast.makeText(context, "Bu tarif zaten eklenmiş!", Toast.LENGTH_SHORT).show()
                }
            } else {
                showLoginMessage() // Giriş yapmadıysa, mesajı göster
            }
        }

        return view
    }

    private fun checkUserLoginStatus() {
        if (auth.currentUser == null) {
            loginPrompt.visibility = View.VISIBLE
            loginButton.visibility = View.VISIBLE
            toggleRecipeFields(false) // Giriş yapmadıysa formu gizle

            loginButton.setOnClickListener {
                findNavController().navigate(R.id.action_addRecipeFragment_to_loginFragment)
            }
        } else {
            toggleRecipeFields(true) // Giriş yaptıysa formu göster
            loginPrompt.visibility = View.GONE
            loginButton.visibility = View.GONE
        }
    }

    private fun showLoginMessage() {
        loginPrompt.text = "Tarif eklemek için lütfen giriş yapınız"
        loginPrompt.visibility = View.VISIBLE
        loginButton.visibility = View.VISIBLE
    }

    private fun toggleRecipeFields(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        recipeNameEditText.visibility = visibility
        categorySpinner.visibility = visibility
        servingsSpinner.visibility = visibility
        prepTimeSpinner.visibility = visibility
        cookTimeSpinner.visibility = visibility
        ingredientsEditText.visibility = visibility
        instructionsEditText.visibility = visibility
        uploadImageButton.visibility = visibility
        recipeImageView.visibility = visibility
        addRecipeButton.visibility = visibility
        recipeNameLabel.visibility = visibility
        categoryLabel.visibility = visibility
        servingsLabel.visibility = visibility
        prepTimeLabel.visibility = visibility
        cookTimeLabel.visibility = visibility
        ingredientsLabel.visibility = visibility
        instructionsLabel.visibility = visibility
    }

    private fun addRecipe() {
        val recipeName = recipeNameEditText.text.toString()
        val category = categorySpinner.selectedItem.toString()
        val servings = servingsSpinner.selectedItem.toString()
        val prepTime = prepTimeSpinner.selectedItem.toString()
        val cookTime = cookTimeSpinner.selectedItem.toString()
        val ingredients = ingredientsEditText.text.toString()
        val instructions = instructionsEditText.text.toString()

        if (recipeName.isEmpty() || ingredients.isEmpty() || instructions.isEmpty() || recipeImageUri == null) {
            Toast.makeText(context, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val authorName = snapshot.child("username").getValue(String::class.java) ?: "Bilinmeyen Kullanıcı"
                    val imageFileName = "${UUID.randomUUID()}.jpg"
                    val imageRef = storageRef.child(imageFileName)

                    imageRef.putFile(recipeImageUri!!).addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val recipeId = UUID.randomUUID().toString()
                            val newRecipe = Recipe(
                                id = recipeId,
                                title = recipeName,
                                category = category,
                                servings = servings,
                                prepTime = prepTime,
                                cookTime = cookTime,
                                ingredients = ingredients,
                                instructions = instructions,
                                imageUrl = uri.toString(),
                                authorName = authorName,
                                timestamp = System.currentTimeMillis()
                            )

                            // Tarif veritabanına kaydedilirken belirlenen ID kullanılır
                            database.getReference("recipes").child(recipeId).setValue(newRecipe).addOnSuccessListener {
                                Toast.makeText(context, "Tarif başarıyla eklendi!", Toast.LENGTH_SHORT).show()
                                isRecipeAlreadyAdded = true  // Tarif eklenmiş olarak işaretlenir
                                clearFields()
                            }.addOnFailureListener { exception ->
                                Log.e("AddRecipeFragment", "Tarif eklenirken hata: ${exception.message}")
                                Toast.makeText(context, "Tarif eklenirken hata: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("AddRecipeFragment", "Resim yüklenirken hata: ${exception.message}")
                        Toast.makeText(context, "Resim yüklenirken hata: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AddRecipeFragment", "Kullanıcı adı alınamadı: ${error.message}")
                    Toast.makeText(context, "Kullanıcı adı alınamadı: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun clearFields() {
        recipeNameEditText.text.clear()
        ingredientsEditText.text.clear()
        instructionsEditText.text.clear()
        recipeImageView.setImageURI(null)
        recipeImageUri = null
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            recipeImageUri = it
            recipeImageView.setImageURI(recipeImageUri)
        }
    }

    private fun setupSpinners() {
        val servingsOptions = arrayOf("1-2", "2-4", "6-8", "8-10", "10-12")
        servingsSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, servingsOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val timeOptions = arrayOf("10 dakika", "20 dakika", "30 dakika", "40 dakika", "50 dakika", "1 saat")
        prepTimeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        cookTimeSpinner.adapter = prepTimeSpinner.adapter
    }

    private fun fetchCategories() {
        val categoryList = mutableListOf<String>()
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryList)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        val categoriesRef = database.getReference("categories")
        categoriesRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                snapshot.children.forEach { categorySnapshot ->
                    categoryList.add(categorySnapshot.getValue(String::class.java) ?: "")
                }
                categoryAdapter.notifyDataSetChanged()
            } else {
                Log.e("AddRecipeFragment", "Kategoriler bulunamadı.")
                Toast.makeText(context, "Kategoriler bulunamadı.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Log.e("AddRecipeFragment", "Kategorileri yüklerken hata: ${it.message}")
            Toast.makeText(context, "Kategorileri yüklerken hata: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateFields(): Boolean {
        if (recipeNameEditText.text.isEmpty() || ingredientsEditText.text.isEmpty() || instructionsEditText.text.isEmpty()) {
            Log.e("AddRecipeFragment", "Boş alanlar var.")
            Toast.makeText(context, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
