 Lezzet Diyarı - Mobil Yemek Tarifleri Uygulaması
Lezzet Diyarı, Android platformu üzerinde geliştirilen ve Firebase altyapısını kullanan bir yemek tarifi paylaşım uygulamasıdır. Kullanıcılar bu uygulama aracılığıyla yemek tarifleri ekleyebilir, favori tariflerini listeleyebilir ve diğer kullanıcıların tariflerine yorum yapabilir.

Özellikler
 Kullanıcı Girişi: Firebase Authentication ile güvenli kullanıcı kaydı ve giriş işlemleri

 Tarif Ekleme: Görsel, malzeme, süre ve porsiyon bilgileriyle detaylı tarif ekleme

 Kategoriye Göre Listeleme: "Tatlılar", "Çorbalar", "Salatalar" gibi kategorilere ayrılmış içerikler

 Favorilere Ekleme: Beğenilen tarifler profil sayfasında listelenebilir

 Yorum Yapma: Her tarif için kullanıcılar yorum bırakabilir

 Gerçek Zamanlı Veri Güncellemesi: Firebase Realtime Database ile senkronize içerik yönetimi

 Responsive Arayüz: Mobil cihazlara uygun, sezgisel ve sade kullanıcı arayüzü

 Navigation Component & Fragment Yapısı: Kod modülerliği için parçalı yapı

Kullanılan Teknolojiler
Android Studio & Kotlin

Firebase Authentication

Firebase Realtime Database

Firebase Storage

Picasso Image Loader

RecyclerView

Navigation Component

View Binding & Data Binding

Veritabanı Yapısı
User: Kullanıcı bilgileri (userId, e-posta, kullanıcı adı, kullanıcı tipi)

Recipe: Tarif detayları (başlık, kategori, içerik, görsel, yazar, timestamp)

Category: Tariflerin ait olduğu kategori

Comment: Her tarif için kullanıcı yorumları

Favorite: Kullanıcıların beğendiği tarifler

 Uygulama Akışı
Giriş ekranı üzerinden kullanıcı kimlik doğrulaması yapılır.

Kullanıcı oturum açtığında ana sayfaya yönlendirilir.

Ana sayfada mevcut tarifler listelenir; kategori filtresi uygulanabilir.

Kullanıcı isterse yeni tarif ekleyebilir veya mevcut tarifleri favorilerine ekleyebilir.

Her tarifin detay ekranında yorum bırakabilir veya tarifin detaylarını inceleyebilir.

Navigation Drawer ve Bottom Navigation ile sayfalar arası geçiş sağlanır.

 Örnek Sayfalar
LoginFragment, SignUpFragment, ChangePasswordFragment

AddRecipeFragment, RecipeFragment, FavoritesFragment, RecipeDetailFragment

Tüm ekranlar fragment yapısıyla ayrıştırılmıştır.

 Karşılaşılan Hatalar ve Çözümleri
Firebase bağlantısı sırasında yetki hataları → google-services.json konumu düzeltildi, izinler yeniden tanımlandı.

XML kaynak sorunları → Eksik drawable tanımlamaları giderildi.

Navigation hataları → SafeArgs ve nav_graph.xml ile tür güvenliği sağlandı.

Firebase ile RecyclerView güncellenmemesi → notifyDataSetChanged() ile dinamik listeleme sağlandı.

📎 Rapor
Detaylı proje raporuna [buradan ulaşabilirsiniz](./221307036%20(9).pdf).
