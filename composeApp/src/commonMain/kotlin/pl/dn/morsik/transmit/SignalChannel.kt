package pl.dn.morsik.transmit

/**
 * Jeden sposób przekazania sygnału: latarka, wibracja albo dźwięk (zadania 8 i 9).
 *
 * To tylko "gniazdko": silnik nadawania ([Transmitter]) zna wyłącznie ten interfejs, więc nie wie nic
 * o Androidzie i da się go testować na komputerze z udawanym kanałem. Prawdziwe implementacje
 * leżą w module androidApp (pakiet pl.dn.morsik.channels), bo potrzebują systemowego Context.
 */
interface SignalChannel {
    /** Włącza (`true`) albo wyłącza (`false`) sygnał. Ma działać natychmiast i nie rzucać wyjątków. */
    fun setOn(on: Boolean)

    /** Zwalnia zasoby (np. generator dźwięku). Wołane raz, po zakończeniu nadawania. */
    fun release() {}
}
