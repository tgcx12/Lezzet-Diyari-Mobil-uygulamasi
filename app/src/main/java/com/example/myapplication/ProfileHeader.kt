package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ProfileHeaderFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var userEmailTextView: TextView
    private lateinit var usernameTextView: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_header, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Görünümleri bağla
        profileImageView = view.findViewById(R.id.profile_image)
        userEmailTextView = view.findViewById(R.id.user_email)
        usernameTextView = view.findViewById(R.id.username)

        // FirebaseAuth ve Database referanslarını başlat
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        // Geçerli kullanıcıyı kontrol et ve bilgileri yükle
        auth.currentUser?.let { currentUser ->
            Log.d("ProfileHeaderFragment", "Geçerli Kullanıcı UID: ${currentUser.uid}")
            displayUserInfo(currentUser)
        } ?: run {
            Log.e("ProfileHeaderFragment", "Kullanıcı oturumu açık değil")
            showNoUserInfo()
        }
    }

    private fun displayUserInfo(currentUser: FirebaseUser) {
        val userEmail = currentUser.email ?: "Giriş yapmadı"
        userEmailTextView.text = "Email: $userEmail"

        // Firebase'den kullanıcı verilerini al
        val userId = currentUser.uid
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ProfileFragment", "Snapshot: $snapshot")
                if (snapshot.exists()) {
                    val username = snapshot.child("username").getValue(String::class.java)
                    usernameTextView.text = "Kullanıcı adı: ${username ?: "Mevcut değil"}"

                    // Profil resmi mevcutsa yükle
                    val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(profileImageUrl)
                            .circleCrop() // Oval görünüm sağlar
                            .placeholder(R.drawable.ic_profile_placeholder) // Yüklenirken gösterilecek varsayılan resim
                            .into(profileImageView)
                    }
                } else {
                    usernameTextView.text = "Kullanıcı bilgileri bulunamadı"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileHeaderFragment", "Veri okuma hatası: ${error.message}")
                Toast.makeText(requireContext(), "Veri okuma hatası: ${error.message}", Toast.LENGTH_SHORT).show()
                showNoUserInfo()
            }
        })
    }

    private fun showNoUserInfo() {
        // Kullanıcı bilgileri bulunamadığında gösterilecek varsayılan değerler
        userEmailTextView.text = "Giriş yapmadınız"
        usernameTextView.text = "Kullanıcı adı mevcut değil"
        profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
        Log.w("ProfileHeaderFragment", "Kullanıcı bilgileri bulunamadı, varsayılan ayarlandı.")
    }
}