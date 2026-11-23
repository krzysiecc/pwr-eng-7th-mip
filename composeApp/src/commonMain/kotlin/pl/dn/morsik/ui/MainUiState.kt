package pl.dn.morsik.ui

import pl.dn.morsik.morse.Morse
import kotlin.math.roundToInt

// Ten plik opisuje, CO ma być na ekranie głównym (dane), a MainScreen.kt - JAK to narysować.
// Tu nie ma żadnego @Composable, więc wszystko testujemy zwykłymi testami (MainUiStateTest.kt).

/**
 * Jeden kafelek w podglądzie liter: litera i jej kod, np. "O" i "---".
 *
 * [startsWord] = `true` dla pierwszej litery każdego słowa OPRÓCZ pierwszego,
 * żeby ekran mógł zrobić większy odstęp między słowami.
 */
data class LetterUi(val symbol: String, val code: String, val startsWord: Boolean = false)

/**
 * Zamienia wiadomość na listę kafelków do podglądu, w kolejności nadawania.
 * Znaki bez kodu Morse'a (np. emoji, `#`) pomijamy, bo i tak nie zostaną nadane.
 *
 * Przykład: `"ala ma"` -> A, L, A, M(startsWord), A
 */
fun previewLetters(message: String): List<LetterUi> {
    val result = mutableListOf<LetterUi>()
    for (word in Morse.encode(message)) {
        var firstInWord = true
        for (letter in word.letters) {
            // `?: continue` = jeśli kod jest null, przejdź od razu do następnej litery.
            val code = letter.code ?: continue
            result += LetterUi(
                symbol = letter.text.uppercase(),
                code = code,
                // Odstęp robimy tylko przed słowem, przed którym coś już jest na liście.
                startsWord = firstInWord && result.isNotEmpty(),
            )
            firstInWord = false
        }
    }
    return result
}

/**
 * Kod do wyświetlenia na kafelku: kropkę i kreskę zamieniamy na grubsze znaki
 * i dodajemy odstępy, żeby było czytelnie: `"-.."` -> `"– · ·"`.
 */
fun displayCode(code: String): String =
    code.map { if (it == Morse.DOT) "·" else "–" }.joinToString(" ")

/**
 * Tekst dla czytnika ekranu (TalkBack), żeby zamiast "kreska, kropek" czytał po polsku:
 * `"A", ".-"` -> `"A: kropka kreska"`.
 */
fun spokenLetter(symbol: String, code: String): String =
    symbol + ": " + code.map { if (it == Morse.DOT) Strings.SignalDot else Strings.SignalDash }
        .joinToString(" ") { it.lowercase() }

/** Który sygnał jest teraz nadawany. `enum` = typ z zamkniętą listą wartości. */
enum class Signal { Dot, Dash }

/**
 * Gdzie jesteśmy w nadawaniu: która litera ([activeLetter], indeks w `letters`),
 * który znak tej litery ([activeSymbol], indeks w jej kodzie) i czy sygnał jest teraz włączony.
 * Wypełni to silnik nadawania (zadanie 7), na razie ustawiamy to tylko w [MainUiState.Demo].
 */
data class TransmissionUi(
    val activeLetter: Int,
    val activeSymbol: Int,
    val signalOn: Boolean,
)

/** Które kanały nadawania są włączone. Wartości po `=` to ustawienia domyślne. */
data class ChannelsUi(
    val flashlight: Boolean = true,
    val vibration: Boolean = true,
    val sound: Boolean = false,
)

/**
 * Cały stan ekranu głównego w jednym obiekcie.
 *
 * Obiekt jest niezmienny (same `val`): żeby coś zmienić, tworzymy kopię przez `copy(...)`.
 * Compose zauważa nowy obiekt i przerysowuje ekran.
 *
 * [transmission] = `null` oznacza "nic nie nadajemy".
 */
data class MainUiState(
    val message: String = "",
    val letters: List<LetterUi> = emptyList(),
    val transmission: TransmissionUi? = null,
    val channels: ChannelsUi = ChannelsUi(),
    val unitMs: Int = DefaultUnitMs,
) {
    /** `true`, gdy trwa nadawanie. */
    val isTransmitting: Boolean get() = transmission != null

    /**
     * Litera, która jest teraz nadawana, albo `null`.
     * `?.let { ... }` wykona blok tylko, gdy transmission nie jest null.
     * `getOrNull` zwraca null zamiast rzucać wyjątek, gdy indeks jest poza listą.
     */
    val activeLetter: LetterUi? get() = transmission?.let { letters.getOrNull(it.activeLetter) }

    /** Kropka albo kreska, która jest teraz nadawana, albo `null`, gdy nic nie nadajemy. */
    val activeSignal: Signal?
        get() {
            val symbolIndex = transmission?.activeSymbol ?: return null
            return when (activeLetter?.code?.getOrNull(symbolIndex)) {
                Morse.DOT -> Signal.Dot
                Morse.DASH -> Signal.Dash
                else -> null
            }
        }

    /** Nowy tekst wpisany przez użytkownika: zapisujemy go i od razu przeliczamy podgląd liter. */
    fun withMessage(message: String): MainUiState = copy(message = message, letters = previewLetters(message))

    // companion object = rzeczy wspólne dla całej klasy, używane jako MainUiState.Nazwa.
    companion object {
        /** Długość jednej jednostki (kropki) w milisekundach: 400 = wolno, 150 = szybko. */
        const val SlowestUnitMs = 400
        const val FastestUnitMs = 150
        const val DefaultUnitMs = 250

        /**
         * Pozycja suwaka dla danej szybkości.
         *
         * Suwak ma iść od "Wolno" (lewo) do "Szybko" (prawo), a szybciej = MNIEJ milisekund.
         * Dlatego liczbę odbijamy lustrzanie: 400 ms -> 150 (lewy koniec), 150 ms -> 400 (prawy koniec).
         * Suwak dostaje zakres 150..400, więc to po prostu (400 + 150) - unitMs.
         */
        fun sliderPosition(unitMs: Int): Float = (SlowestUnitMs + FastestUnitMs - unitMs).toFloat()

        /** Odwrotność [sliderPosition]: z pozycji suwaka wraca do milisekund (zaokrąglone do pełnych). */
        fun unitMsAt(sliderPosition: Float): Int = SlowestUnitMs + FastestUnitMs - sliderPosition.roundToInt()

        /** Stan "na sztywno" jak na makiecie (nadawanie SOS, druga kreska litery O). Używany w podglądach. */
        val Demo = MainUiState(
            message = "SOS",
            letters = previewLetters("SOS"),
            transmission = TransmissionUi(activeLetter = 1, activeSymbol = 1, signalOn = true),
        )
    }
}
