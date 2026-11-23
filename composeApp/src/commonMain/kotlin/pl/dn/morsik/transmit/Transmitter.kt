package pl.dn.morsik.transmit

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import pl.dn.morsik.ui.TransmissionUi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Silnik nadawania (zadanie 7): przechodzi po krokach z [Timeline], włącza i wyłącza kanały
 * i na bieżąco raportuje, gdzie jest.
 *
 * Działa jako funkcja `suspend` w korutynie. Korutyna to "lekki wątek", który może się zawiesić
 * w `delay` bez blokowania interfejsu. **Stop = anulowanie korutyny** (`job.cancel()`):
 * `delay` natychmiast się przerywa, więc Stop działa od razu, a nie dopiero po końcu bieżącej kropki.
 *
 * @param timeSource źródło czasu; domyślnie zegar monotoniczny (nie skacze przy zmianie godziny w telefonie).
 *   W testach podajemy zegar wirtualny.
 */
class Transmitter(private val timeSource: TimeSource = TimeSource.Monotonic) {

    /**
     * Nadaje [steps] na wszystkich [channels]. Kończy się po ostatnim kroku albo po anulowaniu.
     *
     * @param unitMs długość jednostki w ms, odczytywana przed KAŻDYM krokiem, więc suwak
     *   działa także w trakcie nadawania
     * @param onProgress wołane przy każdym kroku: która litera, który znak, czy sygnał jest włączony
     */
    suspend fun transmit(
        steps: List<SignalStep>,
        channels: List<SignalChannel>,
        unitMs: () -> Int,
        onProgress: (TransmissionUi) -> Unit,
    ) {
        // Dlaczego nie samo `delay(czas kroku)` po kolei? Każdy delay trwa trochę dłużej niż zadano,
        // a błędy się sumują (przy 170 ms/jednostka SOS rozjechałby się wyraźnie). Dlatego liczymy
        // "ile czasu powinno minąć od startu" i czekamy tylko do tego momentu.
        val start = timeSource.markNow()
        var planned = Duration.ZERO
        try {
            for (step in steps) {
                setAll(channels, step.on)
                onProgress(TransmissionUi(step.letter, step.symbol, signalOn = step.on))

                planned += (step.units * unitMs()).milliseconds
                val wait = planned - start.elapsedNow()
                if (wait > Duration.ZERO) delay(wait) // jeśli jesteśmy spóźnieni, nie czekamy wcale
            }
        } finally {
            // Wykona się zawsze: po końcu, po Stop i przy błędzie. Kanały muszą zgasnąć.
            // NonCancellable: w anulowanej korutynie zawieszanie jest zabronione; tu go nie używamy,
            // ale to zabezpiecza kod przed późniejszą zmianą.
            withContext(NonCancellable) { setAll(channels, false) }
        }
    }

    /** Ustawia wszystkie kanały. Błąd jednego (np. zajęta kamera) nie może zatrzymać pozostałych. */
    private fun setAll(channels: List<SignalChannel>, on: Boolean) {
        for (channel in channels) runCatching { channel.setOn(on) }
    }
}
