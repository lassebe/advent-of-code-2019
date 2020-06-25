import java.io.File

fun readInts(fileName: String) = File(fileName).readLines().map { it.toInt() }