package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpFragment : Fragment() {
    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSignUp: Button
    private lateinit var textViewLogin: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextUsername = view.findViewById(R.id.editTextUsername)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        buttonSignUp = view.findViewById(R.id.buttonSignUp)
        textViewLogin = view.findViewById(R.id.textViewLogin)

        buttonSignUp.setOnClickListener {
            performSignUp()
        }

        textViewLogin.setOnClickListener {
            (activity as? MainActivity)?.navigateToLoginFragment()
        }
    }

    private fun performSignUp() {
        val username = editTextUsername.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        val user = User(userId, email, "normal", username)
                        saveUserToDatabase(user)
                    }
                } else {
                    Toast.makeText(context, "Kayıt başarısız: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToDatabase(user: User) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(user.userId!!)
        userRef.setValue(user).addOnCompleteListener { dbTask ->
            if (dbTask.isSuccessful) {
                Toast.makeText(context, "Kayıt başarılı! Hoş geldiniz.", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.navigateToProfileFragment()
            } else {
                Toast.makeText(context, "Kullanıcı bilgileri eklenemedi: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// MainActivity'de profil fragment'ına geçiş yapmak için
fun MainActivity.navigateToProfileFragment() {
    navController.navigate(R.id.profileFragment)
}
