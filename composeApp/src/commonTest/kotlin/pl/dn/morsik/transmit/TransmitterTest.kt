package pl.dn.morsik.transmit

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import pl.dn.morsik.ui.TransmissionUi
import pl.dn.morsik.ui.previewLetters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Testy silnika nadawania (Transmitter.kt).
// runTest uruchamia korutyny na "wirtualnym czasie": delay(1000) wykonuje się natychmiast,
// a test i tak widzi, ile czasu "upłynęło" (currentTime). Dzięki temu sprawdzamy dokładne
// momenty włączeń bez czekania i bez telefonu.

@OptIn(ExperimentalCoroutinesApi::class)
class TransmitterTest {

    /** Udawany kanał: zapisuje, kiedy (w ms wirtualnego czasu) go włączono lub wyłączono. */
    private class FakeChannel(private val now: () -> Long) : SignalChannel {
        val events = mutableListOf<Pair<Long, Boolean>>()
        var released = false

        override fun setOn(on: Boolean) {
            events += now() to on
        }

        override fun release() {
            released = true
        }
    }

    private fun steps(text: String) = Timeline.build(previewLetters(text))

    @Test
    fun sosSwitchesChannelAtExactTimes() = runTest {
        val channel = FakeChannel { currentTime }
        val transmitter = Transmitter(testScheduler.timeSource)

        transmitter.transmit(steps("SOS"), listOf(channel), unitMs = { 100 }, onProgress = {})

        // S = trzy kropki: włącz 0, wyłącz 100, włącz 200, wyłącz 300, włącz 400, wyłącz 500;
        // potem przerwa 3 jednostki i litera O zaczyna się o 800.
        val expectedStart = listOf(
            0L to true, 100L to false, 200L to true, 300L to false, 400L to true, 500L to false,
            800L to true, // pierwsza kreska O
        )
        assertEquals(expectedStart, channel.events.take(expectedStart.size))
        // Całość = 27 jednostek x 100 ms, na końcu kanał jest wyłączony.
        assertEquals(2700L, currentTime)
        assertEquals(2700L to false, channel.events.last())
    }

    @Test
    fun reportsProgressForEveryStep() = runTest {
        val progress = mutableListOf<TransmissionUi>()

        Transmitter(testScheduler.timeSource)
            .transmit(steps("A"), emptyList(), unitMs = { 100 }, onProgress = { progress += it })

        // A = ".-": kropka, przerwa po kropce, kreska
        assertEquals(
            listOf(
                TransmissionUi(0, 0, signalOn = true),
                TransmissionUi(0, 0, signalOn = false),
                TransmissionUi(0, 1, signalOn = true),
            ),
            progress,
        )
    }

    @Test
    fun switchesAllChannelsTogether() = runTest {
        val a = FakeChannel { currentTime }
        val b = FakeChannel { currentTime }

        Transmitter(testScheduler.timeSource).transmit(steps("E"), listOf(a, b), unitMs = { 150 }, onProgress = {})

        assertEquals(a.events, b.events)
        assertEquals(listOf(0L to true, 150L to false), a.events)
    }

    @Test
    fun stopSilencesChannelsImmediately() = runTest {
        val channel = FakeChannel { currentTime }
        val job = launch {
            Transmitter(testScheduler.timeSource).transmit(steps("SOS"), listOf(channel), unitMs = { 400 }, onProgress = {})
        }

        advanceTimeBy(250) // jesteśmy w połowie pierwszej kropki
        runCurrent()
        assertEquals(listOf(0L to true), channel.events)

        job.cancel() // przycisk Stop
        advanceUntilIdle()

        // Kanał zgasł w tej samej chwili (250 ms), a nie po końcu kropki (400 ms), czyli Stop < 0,5 s.
        assertEquals(listOf(0L to true, 250L to false), channel.events)
        assertEquals(250L, currentTime)
        assertTrue(job.isCancelled)
    }

    @Test
    fun channelsAreOffAfterNormalEnd() = runTest {
        val channel = FakeChannel { currentTime }

        Transmitter(testScheduler.timeSource).transmit(steps("T"), listOf(channel), unitMs = { 100 }, onProgress = {})

        assertFalse(channel.events.last().second)
    }

    @Test
    fun failingChannelDoesNotStopOthers() = runTest {
        val broken = object : SignalChannel {
            override fun setOn(on: Boolean) = throw IllegalStateException("kamera zajęta")
        }
        val good = FakeChannel { currentTime }

        Transmitter(testScheduler.timeSource).transmit(steps("E"), listOf(broken, good), unitMs = { 100 }, onProgress = {})

        assertEquals(listOf(0L to true, 100L to false), good.events)
    }

    @Test
    fun speedChangeAppliesToNextSteps() = runTest {
        val channel = FakeChannel { currentTime }
        var unit = 100
        // Po pierwszym kroku zmieniamy szybkość: dalsze kroki mają już 200 ms/jednostka.
        val onProgress = { _: TransmissionUi -> }

        val job = launch {
            Transmitter(testScheduler.timeSource).transmit(steps("EE"), listOf(channel), unitMs = { unit }, onProgress = onProgress)
        }
        runCurrent() // krok 1 (kropka, 100 ms) rozpoczęty
        unit = 200
        advanceUntilIdle()
        job.join()

        // kropka 100 ms (stara szybkość), przerwa 3 x 200, kropka 200 -> koniec w 100 + 600 + 200 = 900
        assertEquals(listOf(0L to true, 100L to false, 700L to true, 900L to false), channel.events)
    }

    @Test
    fun repeatedDotsKeepRegularRhythm() = runTest {
        val times = mutableListOf<Long>()
        val channel = object : SignalChannel {
            override fun setOn(on: Boolean) {
                if (on) times += currentTime
            }
        }

        Transmitter(testScheduler.timeSource).transmit(steps("EEEE"), listOf(channel), unitMs = { 100 }, onProgress = {})

        // Każde E to kropka (1) + przerwa między literami (3) = 4 jednostki = 400 ms.
        assertEquals(listOf(0L, 400L, 800L, 1200L), times)
    }

    @Test
    fun emptyStepsDoNothing() = runTest {
        val channel = FakeChannel { currentTime }

        Transmitter(testScheduler.timeSource).transmit(emptyList(), listOf(channel), unitMs = { 100 }, onProgress = {})

        assertEquals(0L, currentTime)
        // Kanał jest gaszony zawsze, także gdy nie było czego nadawać (bezpiecznik).
        assertEquals(listOf(0L to false), channel.events)
    }
}
