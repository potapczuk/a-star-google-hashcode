import java.io.BufferedWriter
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


class Parser {
    fun loadFile(filename: String): List<Slide> {
        val path = Paths.get(
            javaClass.classLoader
                .getResource(filename)!!.toURI()
        )

        var index = 0
        val slides = mutableListOf<Slide>()
        Files.lines(path).skip(1).forEach {
            slides.add(SlideFactory(index, it).toSlide())
            index++
        }

        return slides
    }

    fun saveFile(filename: String, transitionSlides: List<Slide>) {
        File("${this.javaClass.getResource(".").path}new_${filename}.txt").bufferedWriter().use {
            it.writeLn(transitionSlides.size.toString())
            for (slide in transitionSlides) {
                it.writeLn(slide.toIdString())
            }
        }
    }
}

fun BufferedWriter.writeLn(line: String) {
    this.write(line)
    this.newLine()
}