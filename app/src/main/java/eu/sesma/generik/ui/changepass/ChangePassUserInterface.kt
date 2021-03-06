package eu.sesma.generik.ui.changepass

interface ChangePassUserInterface {

    fun initialize(delegate: Delegate)

    interface Delegate {

        fun onNewPass(pass: String)
    }
}