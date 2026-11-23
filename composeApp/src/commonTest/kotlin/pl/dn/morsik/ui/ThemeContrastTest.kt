package pl.dn.morsik.ui

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertTrue

// Sprawdza, czy kolory z Theme.kt mają wystarczający kontrast (wymaganie WCAG 2.1, poziom AA):
//  * tekst:                       co najmniej 4.5 : 1
//  * elementy sterujące, obwódki: co najmniej 3 : 1
// To była uwaga prowadzącego z biweekly 3.11 ("kontrast i dostępność na napisach").
// Jeśli ktoś zmieni kolor na zbyt jasny, ten test od razu to wyłapie.

class ThemeContrastTest {

    /** Wzór WCAG na "względną jasność" koloru: 0 = czarny, 1 = biały. */
    private fun luminance(color: Color): Double {
        // Kanał z zakresu 0..1 zamieniamy z zapisu sRGB na wartość liniową (tak widzi to oko).
        fun linear(channel: Float): Double {
            val c = channel.toDouble()
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linear(color.red) + 0.7152 * linear(color.green) + 0.0722 * linear(color.blue)
    }

    /** Kontrast dwóch kolorów: od 1 (te same) do 21 (czarny na białym). */
    private fun contrast(a: Color, b: Color): Double {
        val lighter = max(luminance(a), luminance(b))
        val darker = min(luminance(a), luminance(b))
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun assertContrast(minimum: Double, foreground: Color, background: Color, what: String) {
        val ratio = contrast(foreground, background)
        assertTrue(ratio >= minimum, "$what: kontrast $ratio, a wymagane jest co najmniej $minimum")
    }

    @Test
    fun formulaGivesKnownValues() {
        // Sprawdzenie samego wzoru: czarny na białym to dokładnie 21 : 1, ten sam kolor to 1 : 1.
        assertTrue(contrast(Color.Black, Color.White) in 20.99..21.01)
        assertTrue(contrast(MorsikPink, MorsikPink) in 0.99..1.01)
    }

    @Test
    fun textColorsAreReadable() {
        assertContrast(4.5, MorsikInk, Color.White, "główny tekst na białym")
        assertContrast(4.5, MorsikInk, MorsikPink, "tekst na różowym polu / kafelku")
        assertContrast(4.5, MorsikInk, MorsikGreen, "tekst na zielonym (aktywny kafelek, włączony kanał)")
        assertContrast(4.5, MorsikInkMuted, Color.White, "przygaszony tekst (etykiety) na białym")
        assertContrast(4.5, MorsikInkMuted, MorsikPink, "przygaszony tekst na różowym (wyłączony kanał, Stop)")
        assertContrast(4.5, Color.White, MorsikRed, "biały napis na czerwonym (Stop, plakietka)")
        assertContrast(4.5, Color.White, MorsikInk, "biała ikona mikrofonu")
    }

    @Test
    fun controlsAreVisible() {
        assertContrast(3.0, MorsikGreenDark, Color.White, "suwak na białym")
        assertContrast(3.0, MorsikGreenDark, MorsikPink, "znak i obwódka w kole wskaźnika")
    }
}
