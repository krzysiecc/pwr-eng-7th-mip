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

    const val Stop = "Stop"

    /** Linia statusu podczas nadawania, np. "NADAWANIE · LITERA O · ZNAK 2 Z 3". */
    fun status(letter: String, symbolNumber: Int, symbolCount: Int) =
        "NADAWANIE · LITERA $letter · ZNAK $symbolNumber Z $symbolCount"

    /** Opis szybkości obok suwaka, np. "250 ms / jednostka". `$unitMs` wstawia wartość zmiennej do tekstu. */
    fun speedValue(unitMs: Int) = "$unitMs ms / jednostka"
}
