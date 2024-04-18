package utils

fun Int.withSign(): String {
    return if (this < 0) this.toString() else "+$this"
}