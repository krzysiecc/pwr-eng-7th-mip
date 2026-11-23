package pl.dn.morsik.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------------------------
// Kolory aplikacji.
//
// Zapis Color(0xAARRGGBB): AA = przezroczystość (FF = pełny kolor), potem czerwony, zielony, niebieski.
//
// Każda para "tekst na tle" ma kontrast co najmniej 4.5 : 1, a elementy sterujące (suwak, obwódki)
// co najmniej 3 : 1 - tyle wymaga standard dostępności WCAG 2.1 (poziom AA).
// Sprawdza to automatycznie test ThemeContrastTest, więc po zmianie koloru wystarczy uruchomić testy.
// ---------------------------------------------------------------------------------------------

/**
 * Zieleń przycisków, włączonych kanałów, aktywnych kafelków, suwaka i obwódki koła. Tekst na niej jest BIAŁY
 * (kontrast ok. 5.2 : 1), a sama na białym i szarym tle ma ponad 3 : 1.
 */
val MorsikGreen = Color(0xFF2E7D1C)

/**
 * Wyszarzała zieleń: strefa suwaka szybszej niż bezpieczne 170 ms (patrz MainUiState.SafeUnitMs).
 * Ma co najmniej 3 : 1 na białym, więc suwak w tej strefie nadal widać.
 */
val MorsikGreenMuted = Color(0xFF7A9471)

/** Jasnoszary kolor pól tekstowych, kafelków, menu i wyłączonych przycisków (zamiast dawnego różu). */
val MorsikGrey = Color(0xFFD9D5D3)

/**
 * Czerwień przycisku Stop (tylko gdy trwa nadawanie) i plakietki w nagłówku.
 * Biały napis ma na niej ok. 6 : 1.
 */
val MorsikRed = Color(0xFFC81E00)

/** Prawie czarny kolor głównego tekstu. */
val MorsikInk = Color(0xFF14100E)

/**
 * Przygaszony tekst: etykiety sekcji, wyłączone przyciski. Pełny, ciemny szarobrązowy kolor
 * (ok. 9 : 1 na białym i ok. 6 : 1 na szarym), a nie przezroczystość, która psuła kontrast.
 */
val MorsikInkMuted = Color(0xFF4A4340)

// Zestaw kolorów dla komponentów Material 3 (Button, Slider, TextField...).
// "onX" oznacza kolor tekstu/ikon, które leżą NA kolorze X.
private val MorsikColors = lightColorScheme(
    primary = MorsikGreen,
    onPrimary = Color.White,
    secondary = MorsikInk,
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = MorsikInk,
    surfaceVariant = MorsikGrey,
    onSurfaceVariant = MorsikInk,
    background = Color.White,
    onBackground = MorsikInk,
    error = MorsikRed,
    onError = Color.White,
)

// Style tekstu. Nazwy (headlineMedium, labelSmall...) to standardowe "role" z Material 3,
// a w kodzie ekranu używamy ich przez MaterialTheme.typography.<nazwa>.
// sp = jednostka czcionki, która rośnie razem z ustawieniem "rozmiar czcionki" w telefonie.
private val MorsikTypography = Typography(
    headlineMedium = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium),
    // Zwykły tekst ekranów z treścią (Pomoc, deklaracja, O aplikacji): mały i nie pogrubiony.
    bodyMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp),
    labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
    // Najmniejszy tekst w aplikacji. Było 10 sp - za mało do wygodnego czytania, teraz 12 sp.
    labelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
)

// Zaokrąglenia rogów. dp = jednostka niezależna od gęstości ekranu (na każdym telefonie wygląda podobnie).
private val MorsikShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
)

/**
 * Motyw aplikacji. Wszystko, co jest w środku [content], dostaje nasze kolory, czcionki i kształty
 * przez MaterialTheme.colorScheme / .typography / .shapes.
 */
@Composable
fun MorsikTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MorsikColors,
        typography = MorsikTypography,
        shapes = MorsikShapes,
        content = content,
    )
}
