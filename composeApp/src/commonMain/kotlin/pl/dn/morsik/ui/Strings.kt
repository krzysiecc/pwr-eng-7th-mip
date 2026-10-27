package pl.dn.morsik.ui

object Strings {
    const val AppName = "Morsik"
    const val AppNameMorse = "-- --- .-. ... .. -.-"

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

    fun status(letter: String, symbolNumber: Int, symbolCount: Int) =
        "NADAWANIE · LITERA $letter · ZNAK $symbolNumber Z $symbolCount"

    fun speedValue(unitMs: Int) = "$unitMs ms / jednostka"
}
