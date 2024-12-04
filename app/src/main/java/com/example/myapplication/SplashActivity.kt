package com.example.myapplication // Projenizin doğru paket adıyla değiştirin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // Kenarlara kadar uzanan bir tasarım için
        setContentView(R.layout.activity_splash) // Layout dosyasını ayarlayın

        // WindowInsetsListener'ı ayarla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ana iş parçacığında bir Handler oluştur
        Handler(Looper.getMainLooper()).postDelayed({
            // MainActivity'ye geçiş yap
            startActivity(Intent(this, MainActivity::class.java))
            finish()  // SplashActivity'yi kapat
        }, 3000) // 3000 milisaniye = 3 saniye
    }
}
