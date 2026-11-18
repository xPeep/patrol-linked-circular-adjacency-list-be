package warmup

import jakarta.validation.constraints.Null
import java.lang.foreign.StructLayout

// Zadání je vedle v zadani.txt. Třídy a další funkce piš mimo main.
val storageName: String = "Na Růžku"
var visitorCount: Int = 0

fun main() {
    description()
    val schranka = Schranka()
    schranka.vloz(text = "Prdel")
    println(schranka.nahled())
    schranka.vyzvedni()
    println(schranka.nahled())
}


fun description() {
    if (visitorCount > 0) {
        println("Sklad $storageName: $visitorCount návštěv")
    }
    else println("Sklad $storageName zatím nikdo nenavštívil")
}

class Schranka() {
    var obsah: String? = null

    fun vloz(text: String) {
        obsah = text
    }

    fun nahled(): String {
        return obsah ?: "Schránka je prázdná"
    }

    fun vyzvedni(): String? {
        println(obsah)
        obsah = null
        return null
    }
}