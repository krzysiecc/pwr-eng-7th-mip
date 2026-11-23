package pl.dn.morsik.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.dn.morsik.transmit.SignalChannel
import pl.dn.morsik.transmit.Transmitter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Testy ViewModelu: to, co robią przyciski, bez rysowania ekranu i bez telefonu.
//
// ViewModel uruchamia nadawanie w viewModelScope, który działa na wątku głównym (Dispatchers.Main).
// Na komputerze wątku głównego Androida nie ma, więc setMain podmienia go na dyspozytor testowy
// z wirtualnym czasem (jak w TransmitterTest).

@OptIn(ExperimentalCoroutinesApi::class)
class MorsikViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    /** Udawany kanał: pamięta, czy jest włączony i czy go zwolniono. */
    private class FakeChannel : SignalChannel {
        var lit = false // nie "on"/"isOn": Kotlin zrobiłby z tego setOn(), jak metoda z interfejsu
        var released = false

        override fun setOn(on: Boolean) {
            lit = on
        }

        override fun release() {
            released = true
        }
    }

    /** Kanały utworzone przez ViewModel, po jednym na każde nadawanie. */
    private val created = mutableListOf<FakeChannel>()

    private fun TestScope.viewModel() = MorsikViewModel(
        createChannels = { listOf(FakeChannel().also { created += it }) },
        transmitter = Transmitter(testScheduler.timeSource),
    )

    @Test
    fun typingUpdatesPreview() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onMessageChange("sos")

        assertEquals("sos", vm.main.value.message)
        assertEquals(listOf("S", "O", "S"), vm.main.value.letters.map { it.symbol })
    }

    @Test
    fun messageIsTransmittedUntilTheEnd() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onUnitMsChange(200)
        vm.onMessageChange("E")
        vm.startMessage()
        runCurrent()

        assertTrue(vm.main.value.isTransmitting)
        assertTrue(created.single().lit)

        advanceUntilIdle() // E = jedna kropka, 200 ms
        assertFalse(vm.main.value.isTransmitting)
        assertFalse(created.single().lit)
        assertTrue(created.single().released)
        assertEquals(200L, currentTime)
    }

    @Test
    fun stopSilencesChannelsAndClearsState() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onMessageChange("SOS")
        vm.startMessage()
        runCurrent()

        vm.stop()
        runCurrent()

        assertFalse(created.single().lit)
        assertTrue(created.single().released)
        assertNull(vm.main.value.transmission)
    }

    @Test
    fun startIsIgnoredWhileTransmitting() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onMessageChange("SOS")
        vm.startMessage()
        runCurrent()
        vm.startMessage()
        runCurrent()

        assertEquals(1, created.size)
    }

    @Test
    fun dictationDoesNotChangeTextWhileTransmitting() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onMessageChange("SOS")
        vm.startMessage()
        runCurrent()

        vm.onDictated("inny tekst")
        assertEquals("SOS", vm.main.value.message)
    }

    @Test
    fun speedIsKeptInAllowedRange() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onUnitMsChange(50)
        assertEquals(MainUiState.FastestUnitMs, vm.main.value.unitMs)
        vm.onUnitMsChange(5000)
        assertEquals(MainUiState.SlowestUnitMs, vm.main.value.unitMs)
    }

    @Test
    fun helpCharacterIsHighlightedWhileSent() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onUnitMsChange(200)
        vm.sendCharacter(LetterUi("T", "-"))
        runCurrent()

        assertEquals("T", vm.help.value.activeSymbol)
        assertTrue(created.single().lit)

        advanceUntilIdle() // T = jedna kreska, 3 x 200 ms
        assertNull(vm.help.value.activeSymbol)
        assertEquals(600L, currentTime)
    }

    @Test
    fun newCharacterReplacesPreviousOneWithoutCuttingItsSignal() = runTest(dispatcher) {
        val vm = viewModel()
        vm.sendCharacter(LetterUi("T", "-"))
        runCurrent()

        vm.sendCharacter(LetterUi("E", "."))
        runCurrent()

        // Pierwszy kanał zgaszony i zwolniony, drugi świeci: `finally` starego nadawania
        // wykonało się PRZED startem nowego, więc nie zgasiło nowej kropki.
        assertEquals(2, created.size)
        assertFalse(created[0].lit)
        assertTrue(created[0].released)
        assertTrue(created[1].lit)
        assertEquals("E", vm.help.value.activeSymbol)
    }

    @Test
    fun navigationStopsTransmission() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onMessageChange("SOS")
        vm.startMessage()
        runCurrent()

        vm.open(Screen.Help)
        runCurrent()

        assertEquals(Screen.Help, vm.screen.value)
        assertFalse(vm.main.value.isTransmitting)
        assertFalse(created.single().lit)
    }

    @Test
    fun backReturnsToMainScreen() = runTest(dispatcher) {
        val vm = viewModel()
        vm.open(Screen.Accessibility)
        vm.back()
        assertEquals(Screen.Main, vm.screen.value)
    }

    @Test
    fun speedChangeAppliesDuringTransmission() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onUnitMsChange(400)
        vm.onMessageChange("EE") // kropka, przerwa 3, kropka
        vm.startMessage()
        advanceTimeBy(100)

        vm.onUnitMsChange(200) // kolejne kroki już po 200 ms
        advanceUntilIdle()

        // 400 (kropka) + 3 x 200 (przerwa) + 200 (kropka)
        assertEquals(1200L, currentTime)
    }
}
