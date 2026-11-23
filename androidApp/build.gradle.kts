// Moduł androidApp: właściwa aplikacja na Androida (z niego powstaje plik APK).
// Sam w sobie prawie nic nie zawiera - tylko MainActivity, która pokazuje interfejs z modułu composeApp.

plugins {
    alias(libs.plugins.androidApplication) // "to jest aplikacja", a nie biblioteka
    alias(libs.plugins.composeCompiler) // kompilator Compose (obsługa @Composable)
}

android {
    namespace = "pl.dn.morsik" // pakiet, w którym leży wygenerowana klasa R
    compileSdk = libs.versions.android.compileSdk.get().toInt() // wersje są w gradle/libs.versions.toml

    defaultConfig {
        applicationId = "pl.dn.morsik" // unikalny identyfikator aplikacji w telefonie i w sklepie
        minSdk = libs.versions.android.minSdk.get().toInt() // 26 = najstarszy obsługiwany Android 8.0
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1 // numer wersji dla systemu (rośnie przy każdym wydaniu)
        versionName = "0.8a" // numer wersji widoczny dla użytkownika
    }
}

dependencies {
    implementation(project(":composeApp")) // cały interfejs i logika
    implementation(libs.androidx.activity.compose) // setContent { } w MainActivity
}
