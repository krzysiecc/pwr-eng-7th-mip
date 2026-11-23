package pl.dn.morsik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/**
 * Jedyna aktywność (ekran systemowy) aplikacji. Android uruchamia ją po kliknięciu ikony,
 * bo tak jest zapisane w AndroidManifest.xml (intent-filter MAIN + LAUNCHER).
 *
 * Moduł androidApp to tylko "opakowanie": cały interfejs i logika są w module composeApp.
 */
class MainActivity : ComponentActivity() {
    // onCreate wywołuje system przy tworzeniu aktywności (start aplikacji, ale też np. obrót ekranu).
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // zawsze najpierw wersja z klasy bazowej
        // setContent = od teraz zawartością okna jest interfejs Compose zwracany przez App().
        setContent {
            App()
        }
    }
}
