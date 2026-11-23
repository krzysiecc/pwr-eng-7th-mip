// Moduł composeApp: cały interfejs (Compose) i logika (alfabet Morse'a, stan ekranu).
// Jest napisany jako moduł Kotlin Multiplatform, ale budujemy go tylko na Androida.

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary) // biblioteka dla Androida (używa jej androidApp)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "pl.dn.morsik.composeapp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        // Testy jednostkowe uruchamiane na komputerze (bez telefonu):
        // ./gradlew :composeApp:testAndroidHostTest
        withHostTest {}
    }

    sourceSets {
        // commonMain = kod w src/commonMain, commonTest = testy w src/commonTest.
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.kotlinx.coroutines.core) // korutyny: delay(), launch, anulowanie (Stop)
            implementation(libs.compose.foundation) // Row, Column, Box, LazyRow...
            implementation(libs.compose.material3) // Button, TextField, Slider...
            implementation(libs.compose.material.icons) // ikony Icons.Filled.*
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.backhandler) // BackHandler: systemowy przycisk/gest Wstecz
            implementation(libs.androidx.lifecycle.viewmodel.compose) // ViewModel: stan przetrwa obrót ekranu
            implementation(libs.compose.ui.tooling.preview) // adnotacja @Preview
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test) // @Test, assertEquals...
            implementation(libs.kotlinx.coroutines.test) // runTest: testy z "wirtualnym" czasem
        }
    }
}

dependencies {
    // Narzędzia do podglądu @Preview w Android Studio (nie trafiają do kodu aplikacji).
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
