package pl.dn.morsik

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pl.dn.morsik.channels.AndroidChannels
import pl.dn.morsik.ui.MorsikViewModel

/**
 * Jedyna aktywność (ekran systemowy) aplikacji. Android uruchamia ją po kliknięciu ikony,
 * bo tak jest zapisane w AndroidManifest.xml (intent-filter MAIN + LAUNCHER).
 *
 * Moduł androidApp to "opakowanie": interfejs i logika są w composeApp, a tutaj jest to,
 * co wymaga Androida: tworzenie kanałów (latarka, wibracja, dźwięk), dyktowanie i reakcja
 * na przejście aplikacji w tło.
 */
class MainActivity : ComponentActivity() {

    // `by viewModels` = daj ViewModel tej aktywności. Przy pierwszym uruchomieniu tworzy go fabryka
    // poniżej, a po obrocie ekranu (nowa aktywność) dostajemy TEN SAM obiekt, z tekstem i nadawaniem.
    // applicationContext, a nie `this`: ViewModel żyje dłużej niż aktywność i nie może jej trzymać
    // (stara aktywność po obrocie nie zostałaby usunięta z pamięci).
    private val viewModel: MorsikViewModel by viewModels {
        viewModelFactory {
            initializer {
                MorsikViewModel(createChannels = { selected -> AndroidChannels.create(applicationContext, selected) })
            }
        }
    }

    // Wynik systemowego okna rozpoznawania mowy. Okno robi cała aplikacja Google/producenta,
    // więc nasza aplikacja nie potrzebuje uprawnienia do mikrofonu (wymaganie "mikrofon tylko
    // przy dyktowaniu" jest spełnione z definicji). Wada: na polski czasem potrzebny jest internet.
    // Tekst trafia prosto do ViewModelu, więc dociera nawet wtedy, gdy w międzyczasie obrócono ekran.
    private val dictationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (result.resultCode == RESULT_OK && text != null) viewModel.onDictated(text)
    }

    // onCreate wywołuje system przy tworzeniu aktywności (start aplikacji, ale też np. obrót ekranu).
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // zawsze najpierw wersja z klasy bazowej
        val version = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
        // setContent = od teraz zawartością okna jest interfejs Compose zwracany przez App().
        setContent {
            App(viewModel = viewModel, startDictation = ::startDictation, appVersion = version)
        }
    }

    /**
     * onStop: aplikacja przestała być widoczna (przejście do ekranu głównego telefonu, blokada ekranu,
     * inna aplikacja na wierzchu). Zatrzymujemy nadawanie, żeby latarka nie świeciła w tle.
     *
     * Wyjątek: obrót ekranu też woła onStop (stara aktywność znika), ale wtedy isChangingConfigurations
     * = true i nadawanie trwa dalej w ViewModelu.
     */
    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) viewModel.stop()
    }

    /** Otwiera systemowe okno dyktowania po polsku. Gdy telefon go nie ma, pokazuje komunikat. */
    private fun startDictation() {
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
