package utils

fun List<*>.wrappingCursor(value: Int): Int = when {
    isEmpty() -> {
        0
    }
    value < 0 -> {
        value + size
    }
    else -> {
        value % size
    }
}