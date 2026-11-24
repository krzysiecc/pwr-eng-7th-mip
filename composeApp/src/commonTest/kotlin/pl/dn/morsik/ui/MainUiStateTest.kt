package pl.dn.morsik.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Testy stanu ekranu głównego (MainUiState.kt) i tekstów (Strings.kt).

class MainUiStateTest {

    // Pomocnicza funkcja: stan "nadajemy SOS, litera nr [letter], znak nr [symbol]".
    private fun sosAt(letter: Int, symbol: Int, signalOn: Boolean = true) =
        MainUiState().withMessage("SOS").copy(
            transmission = TransmissionUi(activeLetter = letter, activeSymbol = symbol, signalOn = signalOn),
        )

    // --- stan początkowy i wpisywanie ---

    @Test
    fun defaultStateIsEmptyAndIdle() {
        val state = MainUiState()

        assertEquals("", state.message)
        assertTrue(state.letters.isEmpty())
        assertFalse(state.isTransmitting)
        assertEquals(ChannelsUi(flashlight = true, vibration = true, sound = false), state.channels)
        assertEquals(MainUiState.DefaultUnitMs, state.unitMs)
    }

    @Test
    fun typingUpdatesMessageAndLettersWithoutStartingTransmission() {
        val state = MainUiState().withMessage("ok")

        assertEquals("ok", state.message)
        assertEquals(listOf(LetterUi("O", "---"), LetterUi("K", "-.-")), state.letters)
        assertNull(state.transmission)
        assertNull(state.activeLetter)
        assertNull(state.activeSignal)
    }

    @Test
    fun typingKeepsOtherSettings() {
        val state = MainUiState(unitMs = 200, channels = ChannelsUi(sound = true)).withMessage("a")

        assertEquals(200, state.unitMs)
        assertTrue(state.channels.sound)
    }

    // --- canStart ---

    @Test
    fun canStartOnlyWithMessageAndWhenIdle() {
        assertFalse(MainUiState().canStart) // pusta wiadomość
        assertFalse(MainUiState().withMessage("###").canStart) // nic do nadania
        assertTrue(MainUiState().withMessage("SOS").canStart)
        assertFalse(sosAt(letter = 0, symbol = 0).canStart) // już nadaje
    }

    // --- activeLetter / activeSignal / isTransmitting ---

    @Test
    fun activeLetterFollowsTransmission() {
        assertEquals(LetterUi("O", "---"), sosAt(letter = 1, symbol = 0).activeLetter)
        assertTrue(sosAt(letter = 1, symbol = 0).isTransmitting)
    }

    @Test
    fun activeSignalIsDotOrDashOfActiveLetter() {
        assertEquals(Signal.Dot, sosAt(letter = 0, symbol = 2).activeSignal) // S = "...", trzecia kropka
        assertEquals(Signal.Dash, sosAt(letter = 1, symbol = 1).activeSignal) // O = "---", druga kreska
    }

    @Test
    fun activeSignalDoesNotDependOnSignalOn() {
        // W przerwie między znakami (signalOn = false) nadal wiemy, który znak jest "bieżący".
        assertEquals(Signal.Dash, sosAt(letter = 1, symbol = 0, signalOn = false).activeSignal)
    }

    @Test
    fun indexesOutsideMessageGiveNullInsteadOfCrash() {
        assertNull(sosAt(letter = 5, symbol = 0).activeLetter)
        assertNull(sosAt(letter = 5, symbol = 0).activeSignal)
        assertNull(sosAt(letter = 0, symbol = 9).activeSignal)
    }

    @Test
    fun demoStateMatchesMockup() {
        val demo = MainUiState.Demo

        assertEquals("SOS", demo.message)
        assertEquals(previewLetters("SOS"), demo.letters)
        assertEquals("O", demo.activeLetter?.symbol)
        assertEquals(Signal.Dash, demo.activeSignal)
    }

    // --- suwak szybkości ---

    @Test
    fun sliderIsMirrored() {
        // Wolno (350 ms) = lewy koniec suwaka (najmniejsza wartość), bardzo szybko (50 ms) = prawy koniec.
        assertEquals(MainUiState.FastestUnitMs.toFloat(), MainUiState.sliderPosition(MainUiState.SlowestUnitMs))
        assertEquals(MainUiState.SlowestUnitMs.toFloat(), MainUiState.sliderPosition(MainUiState.FastestUnitMs))
    }

    @Test
    fun sliderPositionRoundTripsToSameMilliseconds() {
        for (ms in MainUiState.FastestUnitMs..MainUiState.SlowestUnitMs) {
            assertEquals(ms, MainUiState.unitMsAt(MainUiState.sliderPosition(ms)), "dla $ms ms")
        }
    }

    @Test
    fun sliderPositionIsRoundedToWholeMilliseconds() {
        assertEquals(250, MainUiState.unitMsAt(150.4f)) // 400 - 150
        assertEquals(249, MainUiState.unitMsAt(150.6f)) // 400 - 151
    }

    @Test
    fun greyZoneStartsBelowSafeSpeed() {
        assertEquals(false, MainUiState.isBeyondSafeSpeed(MainUiState.SafeUnitMs))
        assertEquals(true, MainUiState.isBeyondSafeSpeed(MainUiState.SafeUnitMs - 1))
        assertEquals(true, MainUiState.isBeyondSafeSpeed(MainUiState.FastestUnitMs))
        // Granica na suwaku: 60% długości od lewej (wolno), czyli (350 - 170) / (350 - 50).
        assertEquals(0.6f, MainUiState.SafeZoneEnd, 0.001f)
    }

    // --- wyświetlanie kodu ---

    @Test
    fun displayCodeUsesBoldSymbolsWithSpaces() {
        assertEquals("– · ·", displayCode("-.."))
        assertEquals("·", displayCode("."))
        assertEquals("", displayCode(""))
    }

    @Test
    fun spokenLetterReadsCodeInPolish() {
        assertEquals("A: kropka kreska", spokenLetter("A", ".-"))
        assertEquals("T: kreska", spokenLetter("T", "-"))
    }

    // --- teksty ---

    @Test
    fun statusTextShowsLetterAndSymbolNumber() {
        assertEquals("NADAWANIE · LITERA O · ZNAK 2 Z 3", Strings.status("O", 2, 3))
    }

    @Test
    fun speedTextShowsMilliseconds() {
        assertEquals("250 ms / jedn.", Strings.speedValue(250))
    }
}
