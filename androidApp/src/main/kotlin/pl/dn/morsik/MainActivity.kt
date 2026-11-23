package pl.dn.morsik

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import pl.dn.morsik.channels.AndroidChannels

/**
 * Jedyna aktywność (ekran systemowy) aplikacji. Android uruchamia ją po kliknięciu ikony,
 * bo tak jest zapisane w AndroidManifest.xml (intent-filter MAIN + LAUNCHER).
 *
 * Moduł androidApp to "opakowanie": interfejs i logika są w composeApp, a tutaj jest to,
 * co wymaga Androida: tworzenie kanałów (latarka, wibracja, dźwięk) i dyktowanie.
 */
class MainActivity : ComponentActivity() {

    /** Co zrobić z rozpoznanym tekstem (ustawia startDictation). */
    private var onDictated: ((String) -> Unit)? = null

    // Wynik systemowego okna rozpoznawania mowy. Okno robi cała aplikacja Google/producenta,
    // więc nasza aplikacja nie potrzebuje uprawnienia do mikrofonu (wymaganie "mikrofon tylko
    // przy dyktowaniu" jest spełnione z definicji). Wada: na polski czasem potrzebny jest internet.
    private val dictationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (result.resultCode == RESULT_OK && text != null) onDictated?.invoke(text)
    }

    // onCreate wywołuje system przy tworzeniu aktywności (start aplikacji, ale też np. obrót ekranu).
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // zawsze najpierw wersja z klasy bazowej
        // setContent = od teraz zawartością okna jest interfejs Compose zwracany przez App().
        setContent {
            App(
                createChannels = { selected -> AndroidChannels.create(this, selected) },
                startDictation = ::startDictation,
            )
        }
    }

    /** Otwiera systemowe okno dyktowania po polsku. Gdy telefon go nie ma, pokazuje komunikat. */
    private fun startDictation(onResult: (String) -> Unit) {
        onDictated = onResult
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pl-PL")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Powiedz wiadomość")
        }
        try {
            dictationLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Dyktowanie jest niedostępne na tym telefonie", Toast.LENGTH_LONG).show()
        }
    }
}
