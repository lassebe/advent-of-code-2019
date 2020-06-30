import java.io.File

fun main() {
    val image = File("input/day-8-image.txt").readText()
    val width = 25
    val height = 6

    val example = layers("121256789012", 3, 2)
            .sortedBy { countDigits(it, 0) }
    assert(countDigits(example.first(), 1) * countDigits(example.first(), 2) == 4)


    val layers = layers(image, width, height)
    val layerSize = width * height
    val output = MutableList(layerSize) { 2 }

    loop@for (j in (0 until layerSize)) {
        for (i in (layers.indices)) {
            if(layers[i][j] != 2) {
                output[j] = layers[i][j]
                continue@loop
            }
        }
    }

    for (i in (width..width * (output.size / width) step width)) {
        println(output.subList(i-width, i).map { if(it == 0) "   " else " ⬜ " }.joinToString(""))
    }
}

fun layers(imageData: String, height: Int, width: Int): List<List<Int>> {
    val layerSize = width * height
    val output = mutableListOf<List<Int>>()
    for (i in (layerSize..layerSize * (imageData.length / layerSize) step layerSize)) {
        output.add(imageData.substring(i - layerSize, i).map { Character.getNumericValue(it) })
    }
    return output
}

fun countDigits(layer: List<Int>, digit: Int): Int {
    return layer.count { it == digit }
}
