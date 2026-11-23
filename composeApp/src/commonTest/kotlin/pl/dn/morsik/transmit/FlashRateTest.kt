package pl.dn.morsik.transmit

import pl.dn.morsik.morse.Morse
import pl.dn.morsik.ui.LetterUi
import pl.dn.morsik.ui.MainUiState
import kotlin.test.Test
import kotlin.test.assertTrue

// WCAG 2.3.1 "Trzy błyski" (poziom A, wymagany przez ustawę o dostępności cyfrowej):
// nic nie może migać częściej niż 3 razy na sekundę. Latarka i wskaźnik na ekranie migają
// w rytmie nadawania, więc nawet przy najszybszym ustawieniu suwaka dwa kolejne włączenia
// sygnału muszą być od siebie oddalone o więcej niż 1/3 sekundy.

class FlashRateTest {

    @Test
    fun fastestSpeedFlashesAtMostThreeTimesPerSecond() {
        // Cały alfabet jako jedna wiadomość: sprawdzamy każdą literę i przerwy między nimi.
        val letters = Morse.alphabet.map { (char, code) -> LetterUi(char.toString(), code) }
        val steps = Timeline.build(letters)

        // Momenty (w jednostkach), w których sygnał się włącza.
        val onsets = mutableListOf<Int>()
        var time = 0
        for (step in steps) {
            if (step.on) onsets += time
            time += step.units
        }

        val shortestUnits = onsets.zipWithNext { a, b -> b - a }.min()
        val shortestMs = shortestUnits * MainUiState.FastestUnitMs
        assertTrue(
            shortestMs > 1000.0 / 3,
            "najkrótszy odstęp między błyskami to $shortestMs ms, czyli ponad 3 błyski na sekundę",
        )
    }
}
