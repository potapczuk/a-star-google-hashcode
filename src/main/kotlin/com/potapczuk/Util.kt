import java.time.LocalDateTime

fun <T> timeIt(message: String = "", block: () -> T): T {
    val start = System.currentTimeMillis()
    val r = block()
    val end = System.currentTimeMillis()
    log("$message: ${end - start} ms / ${(end - start) / 1000 / 60} m")
    return r
}

fun log(line: String) {
    println("[${LocalDateTime.now()}] ${Thread.currentThread().name}: $line")
}