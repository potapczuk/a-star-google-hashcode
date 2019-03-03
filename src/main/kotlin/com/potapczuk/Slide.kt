data class Slide(val id: List<Int>, val orientation: Orientation, val tags: Set<String>) {
    fun toIdString(): String =
        when(orientation) {
            Orientation.HORIZONTAL -> id[0].toString()
            Orientation.SLIDE -> "${id[0]} ${id[1]}"
            Orientation.VERTICAL -> throw Exception("Vertical photos are not supposed to be printed")
        }
}

class SlideFactory(val id: Int, val line:String) {
    fun toSlide(): Slide {
        val values = line.split(" ")
        return Slide(listOf(id), Orientation.from(values[0]), values.subList(2, values.size - 1).toSet())
    }
}
