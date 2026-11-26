package pl.dn.morsik.transmit

interface SignalChannel {
    // Nie powinno rzucać wyjątków.
    fun setOn(on: Boolean)

    // Wołane raz, po zakończeniu nadawania.
    fun release() {}
}
