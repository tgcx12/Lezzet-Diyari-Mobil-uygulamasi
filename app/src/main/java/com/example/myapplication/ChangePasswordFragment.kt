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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordFragment : Fragment() {

    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmNewPasswordEditText: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var errorMessageTextView: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI bileşenlerini bağla
        currentPasswordEditText = view.findViewById(R.id.current_password_edittext)
        newPasswordEditText = view.findViewById(R.id.new_password_edittext)
        confirmNewPasswordEditText = view.findViewById(R.id.confirm_new_password_edittext)
        changePasswordButton = view.findViewById(R.id.change_password_button)
        errorMessageTextView = view.findViewById(R.id.error_message)

        // FirebaseAuth başlat
        auth = FirebaseAuth.getInstance()

        changePasswordButton.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val confirmNewPassword = confirmNewPasswordEditText.text.toString()

            if (newPassword == confirmNewPassword) {
                changePassword(currentPassword, newPassword)
            } else {
                errorMessageTextView.text = "Yeni şifreler eşleşmiyor!"
                errorMessageTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser

        if (user != null) {
            val email = user.email

            if (email != null) {
                // Kullanıcıyı yeniden doğrulamak için mevcut şifreyi kullanıyoruz
                val credential = EmailAuthProvider.getCredential(email, currentPassword)

                // Kullanıcıyı yeniden doğrula
                user.reauthenticate(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Şifre doğrulandı, yeni şifreyi değiştir
                        user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(context, "Şifre başarıyla değiştirildi!", Toast.LENGTH_SHORT).show()
                                // Başka bir sayfaya yönlendirme yapılabilir (örneğin profil sayfası)
                            } else {
                                errorMessageTextView.text = "Şifre değiştirilemedi: ${updateTask.exception?.message}"
                                errorMessageTextView.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        errorMessageTextView.text = "Mevcut şifre hatalı!"
                        errorMessageTextView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
