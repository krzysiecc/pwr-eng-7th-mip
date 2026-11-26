package pl.dn.morsik.transmit

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import pl.dn.morsik.ui.TransmissionUi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class Transmitter(private val timeSource: TimeSource = TimeSource.Monotonic) {

    // `unitMs` jest odczytywane przed każdym krokiem, więc suwak działa na żywo.
    suspend fun transmit(
        steps: List<SignalStep>,
        channels: List<SignalChannel>,
        unitMs: () -> Int,
        onProgress: (TransmissionUi) -> Unit,
    ) {
        // Czekamy względem startu, żeby opóźnienia `delay` się nie sumowały.
        val start = timeSource.markNow()
        var planned = Duration.ZERO
        try {
            for (step in steps) {
                setAll(channels, step.on)
                onProgress(TransmissionUi(step.letter, step.symbol, signalOn = step.on))

                planned += (step.units * unitMs()).milliseconds
                val wait = planned - start.elapsedNow()
                if (wait > Duration.ZERO) delay(wait)
            }
        } finally {
            withContext(NonCancellable) { setAll(channels, false) }
        }
    }

    // Błąd jednego kanału nie zatrzymuje pozostałych.
    private fun setAll(channels: List<SignalChannel>, on: Boolean) {
        for (channel in channels) runCatching { channel.setOn(on) }
    }
}
