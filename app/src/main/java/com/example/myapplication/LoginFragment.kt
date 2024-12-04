package com.example.myapplication

import android.content.Context
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

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Fragment'in layout'unu inflate ediyoruz
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firebase Authentication örneğini alıyoruz
        auth = FirebaseAuth.getInstance()

        // UI bileşenlerini alıyoruz
        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val signupTextView = view.findViewById<TextView>(R.id.signupTextView)

        // Giriş butonuna tıklanma işlemi
        loginButton.setOnClickListener {
            // EditText'ten e-posta ve şifreyi alıyoruz
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // E-posta ve şifre boş mu kontrolü
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "E-posta ve şifre alanları boş olamaz.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kullanıcıyı giriş yap
            loginUser(email, password)
        }

        // Kayıt sayfasına yönlendirme
        signupTextView.setOnClickListener {
            (activity as? MainActivity)?.navigateToSignUpFragment()
        }
    }

    // Giriş işlemi için Firebase Authentication kullanımı
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Giriş başarılı
                    Toast.makeText(context, "Giriş başarılı", Toast.LENGTH_SHORT).show()

                    // Giriş durumunu SharedPreferences'e kaydet
                    val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("isLoggedIn", true)
                    editor.apply()

                    // Kullanıcıyı profiline yönlendirme
                    (activity as? MainActivity)?.onUserLogin(email) // Burada sadece email parametresi geçiyoruz
                } else {
                    // Giriş başarısız
                    Toast.makeText(context, "Giriş başarısız: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
