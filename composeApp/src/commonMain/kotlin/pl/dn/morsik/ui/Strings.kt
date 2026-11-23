package pl.dn.morsik.ui

/**
 * Wszystkie teksty widoczne w aplikacji w jednym miejscu.
 *
 * Dzięki temu w kodzie ekranu nie ma "gołych" napisów, a zmiana tekstu to zmiana w jednym pliku.
 * (W typowej aplikacji na Androida trzymałoby się je w res/values/strings.xml, ale ten moduł
 * jest wieloplatformowy, więc prosty `object` jest najłatwiejszy.)
 */
object Strings {
    const val AppName = "Morsik"
    const val AppNameMorse = "-- --- .-. ... .. -.-" // "MORSIK" alfabetem Morse'a

    const val MessageLabel = "WIADOMOŚĆ"
    const val MessagePlaceholder = "Wpisz wiadomość"
    const val Dictate = "Dyktuj"
    const val Menu = "Menu"
    const val Back = "Wstecz"

    const val StatusIdle = "GOTOWY DO NADAWANIA"
    const val SignalDot = "Kropka"
    const val SignalDash = "Kreska"
    const val SignalIdle = "Wpisz wiadomość"

    const val Flashlight = "Latarka"
    const val Vibration = "Wibracja"
    const val Sound = "Dźwięk"

    const val SpeedLabel = "SZYBKOŚĆ"
    const val SpeedSlow = "Wolno"
    const val SpeedFast = "Szybko"

    const val Start = "Nadaj"
    const val Stop = "Stop"

    /** Linia statusu podczas nadawania, np. "NADAWANIE · LITERA O · ZNAK 2 Z 3". */
    fun status(letter: String, symbolNumber: Int, symbolCount: Int) =
        "NADAWANIE · LITERA $letter · ZNAK $symbolNumber Z $symbolCount"

    /** Opis szybkości obok suwaka, np. "250 ms / jednostka". `$unitMs` wstawia wartość zmiennej do tekstu. */
    fun speedValue(unitMs: Int) = "$unitMs ms / jednostka"

    // ---- Menu (pozycje są też tytułami otwieranych ekranów) ----

    const val Help = "Pomoc"
    const val Accessibility = "Deklaracja dostępności"
    const val About = "O aplikacji"

    // ---- Ekran Pomocy ----

    const val HowToTitle = "Jak korzystać"
    val HowToSteps = listOf(
        "Wpisz wiadomość albo dotknij mikrofonu i ją podyktuj. Pod polem widać każdą literę z jej kodem.",
        "Wybierz kanały: latarka, wibracja, dźwięk. Można włączyć kilka naraz.",
        "Ustaw szybkość suwakiem i dotknij Nadaj (albo Wyślij na klawiaturze).",
        "Stop od razu przerywa nadawanie. Nadawanie zatrzymuje się też, gdy wyjdziesz z aplikacji.",
    )

    const val AlphabetTitle = "Alfabet Morse'a"
    const val AlphabetHint = "Dotknij znaku, aby go nadać. Używane są kanały i szybkość z ekranu głównego."
    const val GroupLetters = "Litery"
    const val GroupPolish = "Polskie litery"
    const val GroupDigits = "Cyfry"
    const val GroupPunctuation = "Znaki interpunkcyjne"

    /** TalkBack mówi wtedy "dwukrotnie dotknij, aby nadać". */
    const val SendAction = "nadać"

    /** TalkBack: stan podświetlonego kafelka. */
    const val SendingNow = "nadawanie"

    /**
     * Nazwa znaku do przeczytania przez TalkBack. Litery i cyfry czyta sam,
     * ale np. samo "(" przeczytałby różnie zależnie od telefonu albo wcale.
     */
    fun characterName(symbol: String): String = when (symbol) {
        "." -> "kropka"
        "," -> "przecinek"
        "?" -> "znak zapytania"
        "!" -> "wykrzyknik"
        "'" -> "apostrof"
        "\"" -> "cudzysłów"
        "/" -> "ukośnik"
        "(" -> "nawias otwierający"
        ")" -> "nawias zamykający"
        "&" -> "ampersand"
        ":" -> "dwukropek"
        ";" -> "średnik"
        "=" -> "znak równości"
        "+" -> "plus"
        "-" -> "minus"
        "_" -> "podkreślnik"
        "$" -> "dolar"
        "@" -> "małpa"
        else -> symbol
    }

    // ---- Deklaracja dostępności ----
    // Układ wzorowany na deklaracji wymaganej ustawą z 4 kwietnia 2019 r. o dostępności cyfrowej
    // stron internetowych i aplikacji mobilnych podmiotów publicznych. Po zmianach w aplikacji
    // trzeba zaktualizować datę i listy poniżej.

    const val ProjectUrl = "https://github.com/krzysiecc/pwr-eng-7th-mip"

    fun accessibilityStatement(version: String) = listOf(
        InfoSection(
            title = null,
            paragraphs = listOf(
                "Zespół DN chce, aby aplikacja mobilna Morsik była dostępna dla wszystkich. Nie jesteśmy " +
                    "podmiotem publicznym, ale tworzymy aplikację zgodnie z ustawą z dnia 4 kwietnia 2019 r. " +
                    "o dostępności cyfrowej stron internetowych i aplikacji mobilnych podmiotów publicznych.",
                "Deklaracja dotyczy aplikacji Morsik w wersji $version na system Android.",
                "Data sporządzenia deklaracji: 2026-11-23. Deklarację sporządzono na podstawie samooceny " +
                    "przeprowadzonej przez zespół.",
            ),
        ),
        InfoSection(
            title = "Stan dostępności",
            paragraphs = listOf(
                "Aplikacja jest częściowo zgodna ze standardem WCAG 2.1 na poziomie AA (norma EN 301 549) " +
                    "z powodu ograniczeń opisanych niżej.",
            ),
        ),
        InfoSection(
            title = "Co zapewniamy",
            paragraphs = listOf(
                "Kontrast tekstu co najmniej 4,5 : 1, a przycisków, suwaka i obwódek co najmniej 3 : 1. " +
                    "Kolory sprawdzają automatyczne testy.",
                "Stan przełączników nie jest pokazywany tylko kolorem: włączony kanał ma też znak ptaszka.",
                "Wszystkie przyciski mają opisy dla czytnika ekranu TalkBack, a kod Morse'a jest czytany " +
                    "słowami, np. \"A: kropka kreska\".",
                "Tekst rośnie razem z ustawieniem rozmiaru czcionki w telefonie, a ekrany można przewijać.",
                "Aplikacja działa w orientacji pionowej i poziomej.",
                "Pola dotyku mają co najmniej 48 na 48 dp.",
                "Nic nie miga częściej niż 3 razy na sekundę, dlatego najszybsza szybkość to 170 ms na jednostkę.",
                "Nadawanie można w każdej chwili przerwać przyciskiem Stop. Zatrzymuje się też samo, " +
                    "gdy aplikacja przechodzi w tło.",
                "Każdy sygnał nadawany latarką, wibracją lub dźwiękiem jest jednocześnie pokazany na ekranie.",
            ),
            bullets = true,
        ),
        InfoSection(
            title = "Ograniczenia",
            paragraphs = listOf(
                "Dyktowanie korzysta z systemowej usługi rozpoznawania mowy. Dla języka polskiego może " +
                    "wymagać internetu, a na niektórych telefonach jest niedostępne. Wiadomość zawsze " +
                    "można wpisać z klawiatury.",
                "Aplikacja nie ma trybu ciemnego.",
                "Aplikacji nie testowały jeszcze osoby z niepełnosprawnościami.",
            ),
            bullets = true,
        ),
        InfoSection(
            title = "Informacje zwrotne i kontakt",
            paragraphs = listOf(
                "Problemy z dostępnością prosimy zgłaszać jako zgłoszenie (Issue) w repozytorium projektu: " +
                    "$ProjectUrl. Tą samą drogą można poprosić o informację o niedostępnym elemencie " +
                    "albo o jego udostępnienie.",
            ),
        ),
        InfoSection(
            title = "Procedura wnioskowo-skargowa",
            paragraphs = listOf(
                "Każdy ma prawo zażądać zapewnienia dostępności cyfrowej aplikacji lub jej elementu. " +
                    "Żądanie powinno zawierać dane osoby zgłaszającej, wskazanie, o który element chodzi, " +
                    "oraz sposób kontaktu. Odpowiadamy niezwłocznie, nie później niż w ciągu 7 dni.",
                "W przypadku podmiotów publicznych po odmowie można złożyć skargę, a następnie wniosek do " +
                    "Rzecznika Praw Obywatelskich (www.rpo.gov.pl).",
            ),
        ),
    )

    // ---- O aplikacji ----

    fun about(version: String) = listOf(
        InfoSection(
            title = null,
            paragraphs = listOf(
                "Morsik nadaje wpisany tekst alfabetem Morse'a: latarką, wibracjami i dźwiękiem. " +
                    "Obsługuje polskie litery.",
            ),
        ),
        InfoSection(title = "Wersja", paragraphs = listOf(version)),
        InfoSection(
            title = "Autorzy",
            paragraphs = listOf(
                "Zespół DN, przedmiot Programowanie Interfejsów Mobilnych, Politechnika Wrocławska, " +
                    "semestr zimowy 2026/27.",
            ),
        ),
        InfoSection(title = "Kod źródłowy", paragraphs = listOf(ProjectUrl)),
        InfoSection(title = "Licencja", paragraphs = listOf("MIT")),
    )
}

/**
 * Fragment ekranu tekstowego (deklaracja, O aplikacji): nagłówek (albo brak) i akapity pod nim.
 * [bullets] = `true`, gdy akapity to punkty listy (każdy dostaje kropkę na początku).
 */
data class InfoSection(val title: String?, val paragraphs: List<String>, val bullets: Boolean = false)
