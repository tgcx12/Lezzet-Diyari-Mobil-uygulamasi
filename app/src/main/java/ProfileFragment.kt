package com.example.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileFragment : Fragment() {

    private lateinit var imageViewProfile: ImageView
    private lateinit var textViewEmail: TextView
    private lateinit var textViewUsername: TextView
    private lateinit var buttonLogout: Button
    private lateinit var buttonUploadImage: Button
    private lateinit var buttonChangePassword: Button

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var storageRef: StorageReference

    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View'ları başlat
        imageViewProfile = view.findViewById(R.id.imageViewProfile)
        textViewEmail = view.findViewById(R.id.textViewEmail)
        textViewUsername = view.findViewById(R.id.textViewUsername)
        buttonLogout = view.findViewById(R.id.buttonLogout)
        buttonUploadImage = view.findViewById(R.id.buttonUploadImage)
        buttonChangePassword = view.findViewById(R.id.buttonChangePassword)

        // Firebase referanslarını başlat
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().getReference("users")
        storageRef = FirebaseStorage.getInstance().reference.child("profile_images")

        // Kullanıcı bilgilerini Firebase'den al
        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            displayUserInfo(currentUser)
        } ?: run {
            showNoUserInfo()
        }

        // Çıkış yap butonuna tıklama işlemi
        buttonLogout.setOnClickListener {
            logoutUser()
        }

        // Profil resmi yükleme butonuna tıklama işlemi
        buttonUploadImage.setOnClickListener {
            requestImageUpload()
        }

        // Şifre değişikliği butonuna tıklama işlemi
        buttonChangePassword.setOnClickListener {
            // Şifre değiştirme ekranına git
            findNavController().navigate(R.id.changePasswordFragment)
        }
    }

    private fun displayUserInfo(currentUser: FirebaseUser) {
        val userEmail = currentUser.email ?: "Giriş yapmadı"
        textViewEmail.text = "Email: $userEmail"

        // Firebase'den kullanıcı verilerini al
        val userId = currentUser.uid
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ProfileFragment", "Snapshot: $snapshot")
                if (snapshot.exists()) {
                    val username = snapshot.child("username").getValue(String::class.java)
                    textViewUsername.text = "Kullanıcı adı: ${username ?: "Mevcut değil"}"

                    // Profil resmi mevcutsa yükle
                    val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(profileImageUrl)
                            .circleCrop() // Oval görünüm sağlar
                            .placeholder(R.drawable.ic_profile_placeholder) // Yüklenirken gösterilecek varsayılan resim
                            .into(imageViewProfile)
                    }
                } else {
                    textViewUsername.text = "Kullanıcı bilgileri bulunamadı"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Veri okuma hatası: ${error.message}")
                textViewUsername.text = "Veri okuma hatası: ${error.message}"
            }
        })
    }

    private fun showNoUserInfo() {
        textViewEmail.text = "Kullanıcı giriş yapmadı"
        textViewUsername.text = "Kullanıcı adı mevcut değil"
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        sharedPreferences.edit().remove("userEmail").apply()
        (activity as? MainActivity)?.onUserLogoutp() // Giriş durumu güncelle
    }


    private fun requestImageUpload() {
        // Görsel seçici aç
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                imageViewProfile.setImageURI(imageUri) // Seçilen resmi ImageView'de göster
                uploadProfileImageToFirebase() // Firebase'e yükle
            }
        }
    }

    private fun uploadProfileImageToFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val imageFileName = "$userId.jpg" // Tercihe bağlı olarak benzersiz bir isim oluşturabilirsiniz
        val imageRef = storageRef.child(imageFileName)

        imageRef.putFile(imageUri!!).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                // Kullanıcının profil resmi URL'sini veritabanında güncelle
                database.child(userId).child("profileImageUrl").setValue(uri.toString())
                Toast.makeText(requireContext(), "Profil resmi başarıyla yüklendi", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Resim yüklenirken hata: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
