import java.io.File

fun main() {
    val modules = File("input/modules.txt").readLines().map { it.toInt() }
    part1(modules)
    part2(modules)
}

fun part1(modules: List<Int>) {
    val fuel = modules.map { (it / 3) - 2 }.sum()
    println(fuel)
}

fun part2(modules: List<Int>) {
    val totalFuel = modules.map { requiredFuel(it) }.sum()
    assert(requiredFuel(14) == 2)
    assert(requiredFuel(1969) == 966)
    assert(requiredFuel(100756) == 50346)
    println(totalFuel)
}

fun requiredFuel(weight: Int): Int {
    val additionalFuel = (weight / 3 - 2)
    return if (additionalFuel <= 0)
        0
    else
        additionalFuel + requiredFuel(additionalFuel)
}