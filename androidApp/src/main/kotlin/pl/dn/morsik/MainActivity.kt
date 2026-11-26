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

class MainActivity : ComponentActivity() {

    // applicationContext, bo ViewModel żyje dłużej niż aktywność.
    private val viewModel: MorsikViewModel by viewModels {
        viewModelFactory {
            initializer {
                MorsikViewModel(createChannels = { selected -> AndroidChannels.create(applicationContext, selected) })
            }
        }
    }

    // Systemowe dyktowanie nie wymaga uprawnienia do mikrofonu.
    private val dictationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (result.resultCode == RESULT_OK && text != null) viewModel.onDictated(text)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val version = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
        setContent {
            App(viewModel = viewModel, startDictation = ::startDictation, appVersion = version)
        }
    }

    override fun onStop() {
        super.onStop()
        // Obrót ekranu nie przerywa nadawania.
        if (!isChangingConfigurations) viewModel.stop()
    }

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
