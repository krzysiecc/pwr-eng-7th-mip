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

/** Jasna zieleń z makiety. Tylko jako TŁO pod ciemnym tekstem (kontrast z MorsikInk ok. 8.7 : 1). */
val MorsikGreen = Color(0xFF76C84A)

/**
 * Ciemniejsza zieleń do elementów stojących bezpośrednio na białym tle (suwak, obwódka koła, kropka statusu).
 * Jasna [MorsikGreen] ma na białym tylko ok. 2 : 1 i byłaby słabo widoczna.
 */
val MorsikGreenDark = Color(0xFF3E8E1E)

/** Różowe tło pól i kafelków. */
val MorsikPink = Color(0xFFFDE2DE)

/**
 * Czerwień przycisku Stop i plakietki w nagłówku.
 * Jest trochę ciemniejsza niż na makiecie (było 0xFFFF2A0F), bo biały napis na tamtej czerwieni
 * miał kontrast tylko ok. 3.8 : 1. Ta ma ok. 5.2 : 1.
 */
val MorsikRed = Color(0xFFD62000)

/** Prawie czarny kolor głównego tekstu. */
val MorsikInk = Color(0xFF1B1411)

/**
 * Przygaszony tekst: etykiety sekcji, wyłączone przełączniki.
 * Wcześniej był to MorsikInk z przezroczystością 50-60%, co na różowym tle dawało za mały kontrast.
 * Pełny, szarobrązowy kolor ma ok. 7 : 1 na białym i ok. 6 : 1 na różowym.
 */
val MorsikInkMuted = Color(0xFF5C5450)

// Zestaw kolorów dla komponentów Material 3 (Button, Slider, TextField...).
// "onX" oznacza kolor tekstu/ikon, które leżą NA kolorze X.
private val MorsikColors = lightColorScheme(
    primary = MorsikGreenDark,
    onPrimary = Color.White,
    secondary = MorsikInk,
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = MorsikInk,
    surfaceVariant = MorsikPink,
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
    titleLarge = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium),
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
