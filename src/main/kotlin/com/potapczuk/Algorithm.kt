import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    Algorithm().start()
}

class Algorithm() {
    private val filenames = setOf("a_example.txt",
                            "b_lovely_landscapes.txt",
                            "c_memorable_moments.txt",
                            "d_pet_pictures.txt",
                            "e_shiny_selfies.txt")

    fun start() {
        for (filename in filenames)
            executeAlgorithmForFilename(filename)
    }

    private fun executeAlgorithmForFilename(filename: String) {
        log("Running $filename")

        timeIt("Total time taken") {
            val slides = timeIt("Time on getSlides") {
                getSlides(filename)
            }

            val transitionSlides = timeIt("Time on getTransitionSlidesByBuckets") {
                runBlocking {
                    getTransitionSlidesByBuckets(slides)
                }
            }

            Parser().saveFile(filename, transitionSlides)
        }

        println("\n\n")
    }

    private fun getSlides(filename: String): List<Slide> {
        val allPhotoSlides = Parser().loadFile(filename).sortedByDescending { it.tags.size }
        val verticals = extractVerticals(allPhotoSlides)
        val verticalSlides = createVerticalSlides(verticals)
        return combineSlidesWithNoVertical(allPhotoSlides, verticalSlides)
    }

    private fun extractVerticals(allPhotoSlides: List<Slide>): List<Slide> =
        allPhotoSlides.filter { it.orientation == Orientation.VERTICAL }

    private fun createVerticalSlides(verticals: List<Slide>): List<Slide> {
        val allVerticals = mutableListOf<Slide>().apply { addAll(verticals) }
        val verticalSlides = mutableListOf<Slide>()

        for (vertical in verticals) {
            if (allVerticals.contains(vertical)) {
                allVerticals.remove(vertical)
                val maxVertical = getMaxVertical(allVerticals, vertical)

                maxVertical?.let {
                    allVerticals.remove(maxVertical)
                    verticalSlides.add(
                        Slide(
                            listOf(vertical.id[0], maxVertical.id[0]),
                            Orientation.SLIDE,
                            vertical.tags + maxVertical.tags
                        )
                    )

                    if (allVerticals.size % 500 == 0)
                        log("vertical remaining: ${allVerticals.size}")
                }
            }
        }

        return verticalSlides
    }

    private fun getMaxVertical(allVerticals: MutableList<Slide>, vertical: Slide): Slide? {
        return allVerticals.maxBy { it.tags.size + vertical.tags.size }
    }

    private fun combineSlidesWithNoVertical(allPhotoSlides: List<Slide>, verticalSlides: List<Slide>): List<Slide> =
        allPhotoSlides.filter { it.orientation != Orientation.VERTICAL } + verticalSlides

    private suspend fun getTransitionSlidesByBuckets(slides: List<Slide>): List<Slide> {
        val bucketSize = 0

        if (bucketSize == 0)
            return getTransitionSlides(slides, 0)

        val transitionSlides = mutableListOf<Slide>()
        val buckets = slides.groupBy {
            it.tags.size + (bucketSize - it.tags.size % bucketSize)
        }

        log("Total buckets: ${buckets.size}")

        val jobs = mutableListOf<Job>()
        for ((index, bucketSlides) in buckets) {
            jobs += GlobalScope.launch {
                transitionSlides.addAll(getTransitionSlides(bucketSlides, index))
            }
        }

        jobs.forEach { it.join() }

        return transitionSlides
    }

    private suspend fun getTransitionSlides(slides: List<Slide>, index: Int = -1): List<Slide> {
        val allSlides = mutableListOf<Slide>().apply { addAll(slides) }
        val transitionSlides = mutableListOf<Slide>()

        var slide = slides[0]
        allSlides.remove(slide)
        transitionSlides.add(slide)

        while (allSlides.isNotEmpty()) {
            if (allSlides.size < 2)
                break

            if (allSlides.size % 500 == 0)
                log("$index - ${allSlides.size}")

            val maxSlide = getMaxSlide(allSlides, slide)

            requireNotNull(maxSlide)

            allSlides.remove(maxSlide)
            transitionSlides.add(maxSlide)
            slide = maxSlide
        }

        return transitionSlides
    }

    private fun getMaxSlide(allSlides: MutableList<Slide>, slide: Slide): Slide? {
        return allSlides.maxBy { getScore(it, slide) }
    }

    private fun getScore(slide1: Slide, slide2: Slide): Int {
        val intersectTags = slide1.tags.intersect(slide2.tags)
        return minOf(slide1.tags.size - intersectTags.size, intersectTags.size, slide2.tags.size)
    }
}
