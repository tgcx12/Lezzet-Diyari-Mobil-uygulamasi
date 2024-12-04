package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.myapplication.model.Recipe
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), RecipeListener {

    lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navigationView: NavigationView
    private var isUserLoggedIn = false
    private lateinit var profileImageView: ImageView
    private lateinit var userEmailTextView: TextView
    private lateinit var usernameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Navigation Controller ayarlama
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navController = navHostFragment?.navController ?: return

        // Alt Navigasyon Görünümü ayarlama
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_recipes -> {
                    navController.navigate(R.id.recipeFragment)
                    true
                }
                R.id.nav_favorites -> {
                    navController.navigate(R.id.favoritesFragment)
                    true
                }
                R.id.nav_add_recipe -> {
                    navController.navigate(R.id.addRecipeFragment)
                    true
                }
                R.id.nav_login -> {
                    if (isUserLoggedIn) {
                        navController.navigate(R.id.profileFragment)
                    } else {
                        navController.navigate(R.id.loginFragment)
                    }
                    true
                }
                else -> false
            }
        }

        // Drawer Layout ayarlama
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        // Profil header'ını bul ve dinamik olarak kullanıcı bilgilerini yükle
        profileImageView = navigationView.getHeaderView(0).findViewById(R.id.profile_image)
        userEmailTextView = navigationView.getHeaderView(0).findViewById(R.id.user_email)
        usernameTextView = navigationView.getHeaderView(0).findViewById(R.id.username)

        // Toolbar ayarlama
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar) // Toolbar'ı ActionBar olarak ayarla

        // Toolbar başlığını gizle
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Başlık metni için TextView'i bulma
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        toolbarTitle.text = "Lezzet Diyarı" // Başlık metnini ayarla

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_drawer, R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Menüdeki öğelere tıklanınca yapılacak işlemler
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_profile -> {
                    if (isUserLoggedIn) {
                        navController.navigate(R.id.profileFragment)
                    } else {
                        navController.navigate(R.id.loginFragment)
                    }
                    true
                }
                R.id.nav_categories -> {
                    navController.navigate(R.id.categoriesFragment)
                    true
                }
                R.id.nav_out -> {
                    FirebaseAuth.getInstance().signOut()
                    isUserLoggedIn = false
                    updateMenuItems() // Menü durumunu güncelle
                    navController.navigate(R.id.loginFragment)
                    true
                }
                else -> false
            }
        }

        checkUserLogin()
        updateMenuItems()
        navigateToInitialFragment()
    }

    override fun onRecipeAdded(recipe: Recipe) {
        Log.d("MainActivity", "Yeni tarif eklendi: ${recipe.title}")
        Toast.makeText(this, "Tarif başarıyla eklendi!", Toast.LENGTH_SHORT).show()
        navController.navigate(R.id.recipeFragment) // Tarif eklendikten sonra tarif listesine geri dön
    }

    override fun onRecipeAddFailed(message: String) {
        Log.e("MainActivity", "Tarif ekleme başarısız: $message")
        Toast.makeText(this, "Tarif eklenemedi: $message", Toast.LENGTH_SHORT).show()
    }
    override fun onStart() {
        super.onStart()
        // Kullanıcı giriş durumu kontrolünü onResume yerine onStart'ta yapın
        checkUserLogin()
    }

    private fun checkUserLogin() {
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        isUserLoggedIn = currentUser != null
        currentUser?.let { user ->
            setUserProfile(user)  // Eğer kullanıcı giriş yaptıysa profil bilgilerini al
        } ?: run {
            showNoUserInfo()  // Eğer giriş yapılmamışsa varsayılan bilgileri göster
        }
        updateMenuItems()  // Menü öğelerini güncelle
    }

    private fun setUserProfile(user: FirebaseUser?) {
        if (user == null) {
            showNoUserInfo()  // Kullanıcı giriş yapmamışsa profil bilgilerini sıfırla
            return
        }

        val userId = user.uid
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val email = user.email ?: "Giriş yapmadı"
                    val username = snapshot.child("username").getValue(String::class.java) ?: "Kullanıcı adı mevcut değil"
                    val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                    userEmailTextView.text = email
                    usernameTextView.text = username

                    if (profileImageUrl != null && profileImageUrl.isNotEmpty()) {
                        Glide.with(this@MainActivity)
                            .load(profileImageUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                } else {
                    showNoUserInfo()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Veri okuma hatası: ${error.message}", Toast.LENGTH_SHORT).show()
                showNoUserInfo()
            }
        })
    }

    private fun showNoUserInfo() {
        userEmailTextView.text = "Giriş yapmadınız"
        usernameTextView.text = "Kullanıcı adı mevcut değil"
        profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
    }

    fun onUserLogout() {
        FirebaseAuth.getInstance().signOut()
        showNoUserInfo() // Profil bilgilerini sıfırlayın
        navController.navigate(R.id.loginFragment)  // LoginFragment'e yönlendirme
    }

    private fun updateMenuItems() {
        val loginMenuItem = bottomNavigationView.menu.findItem(R.id.nav_login)
        loginMenuItem.title = if (isUserLoggedIn) "Profil" else "Giriş Yap"
    }


    // Giriş Yapılmadığı Durumda LoginFragment'e Yönlendirme
    fun navigateToLoginFragment() {
        navController.navigate(R.id.loginFragment)
    }

    // İlk Fragment yönlendirmesi
    private fun navigateToInitialFragment() {
        if (isUserLoggedIn) {
            navController.navigate(R.id.recipeFragment)
        } else {
            navController.navigate(R.id.loginFragment)
        }
    }
    fun navigateToSignUpFragment() {
        navController.navigate(R.id.signUpFragment)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    fun onUserLogin(email: String) {
        isUserLoggedIn = true
        Toast.makeText(this, "$email ile giriş yapıldı.", Toast.LENGTH_SHORT).show()
        updateMenuItems() // Menü durumunu güncelle
        navController.navigate(R.id.profileFragment)
    }
    fun onUserLogoutp() {
        // Bottom Navigation veya başka bileşenlerdeki güncellemeleri yap
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.menu.findItem(R.id.nav_login)?.title = "Giriş Yap"
        val navController =
            findNavController(R.id.nav_host_fragment)
        navController.navigate(R.id.loginFragment)
    }

}



