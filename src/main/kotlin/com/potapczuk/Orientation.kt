enum class Orientation(val key: String) {
    HORIZONTAL("H"),
    VERTICAL("V"),
    SLIDE("S");

    companion object {
        fun from(value: String) = when(value) {
            "H" -> HORIZONTAL
            "V" -> VERTICAL
            "S" -> SLIDE
            else -> throw Exception("Orientation not recognized")
        }
    }
}
