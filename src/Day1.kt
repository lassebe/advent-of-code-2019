import java.lang.Integer.sum

fun main() {
    val modules = readInts("input/day1.txt")
    val fuel = modules.map { (it / 3) - 2 }.sum()
    println(fuel)
}