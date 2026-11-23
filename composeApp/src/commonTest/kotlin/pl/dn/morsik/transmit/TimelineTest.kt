package pl.dn.morsik.transmit

import pl.dn.morsik.ui.previewLetters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Testy osi czasu (Timeline.kt): ile jednostek trwa każdy sygnał i każda przerwa.

class TimelineTest {

    /** Skrót: kroki jako czytelny tekst, np. "+1 -1 +1": plus = sygnał, minus = przerwa, liczba = jednostki. */
    private fun render(text: String) = Timeline.build(previewLetters(text))
        .joinToString(" ") { (if (it.on) "+" else "-") + it.units }

    @Test
    fun dotIsOneUnitAndDashIsThree() {
        assertEquals("+1", render("E")) // E = .
        assertEquals("+3", render("T")) // T = -
    }

    @Test
    fun gapInsideLetterIsOneUnit() {
        assertEquals("+1 -1 +3", render("A")) // A = .-
    }

    @Test
    fun gapBetweenLettersIsThreeUnits() {
        assertEquals("+1 -3 +3", render("ET"))
    }

    @Test
    fun gapBetweenWordsIsSevenUnits() {
        assertEquals("+1 -7 +3", render("E T"))
        assertEquals("+1 -7 +3", render("E     T")) // wiele spacji to nadal jedna przerwa
    }

    @Test
    fun noGapAfterLastSignal() {
        assertTrue(Timeline.build(previewLetters("SOS")).last().on)
    }

    @Test
    fun sosLastsTwentySevenUnits() {
        // S: 1+1+1+1+1 = 5, przerwa 3, O: 3+1+3+1+3 = 11, przerwa 3, S: 5
        assertEquals(27, Timeline.totalUnits(Timeline.build(previewLetters("SOS"))))
    }

    @Test
    fun stepsPointAtTheirLetterAndSymbol() {
        val steps = Timeline.build(previewLetters("EA")) // E = ".", A = ".-"

        // (litera, znak, sygnał włączony)
        assertEquals(
            listOf(
                Triple(0, 0, true), // kropka E
                Triple(0, 0, false), // przerwa między literami należy do E
                Triple(1, 0, true), // kropka A
                Triple(1, 0, false), // przerwa wewnątrz A należy do jej kropki
                Triple(1, 1, true), // kreska A
            ),
            steps.map { Triple(it.letter, it.symbol, it.on) },
        )
    }

    @Test
    fun skipsUnknownCharacters() {
        assertEquals(render("AB"), render("A#B"))
    }

    @Test
    fun emptyMessageHasNoSteps() {
        assertTrue(Timeline.build(emptyList()).isEmpty())
        assertEquals(0, Timeline.totalUnits(emptyList()))
    }
}
