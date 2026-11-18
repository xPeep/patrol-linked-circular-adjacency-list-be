package warmup

// Zadání je vedle v zadani.txt. Třídy a další funkce piš mimo main.
val storageName: String = "Na Růžku"
var visitorCount: Int = 0

fun main() {
    description()
}


fun description() {
    if (visitorCount > 0) {
        println("Sklad $storageName: $visitorCount návštěv")
    }
    else println("Sklad $storageName zatím nikdo nenavštívil")
}

class Schranka() {

}