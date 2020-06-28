import java.io.File

@ExperimentalStdlibApi
fun main() {
    val exampleGraph = createGraph("input/day-6-small-map.txt")
    assert(depths(exampleGraph) == 42)
    val graph = createGraph("input/day-6-map.txt")
    println(depths(graph))
}

private fun createGraph(fileName: String): Map<String, String> {
    val graph = mutableMapOf<String, String>()
    File(fileName).readLines().map {
        val from = it.split(")")[0]
        val to = it.split(")")[1]
        graph.put(to, from)
    }
    return graph
}

@ExperimentalStdlibApi
private fun depths(graph: Map<String, String>) = graph.keys.map {
            countOrbits(it, graph, 0)
        }.sum()

private fun countOrbits(node: String, graph: Map<String, String>, acc: Int): Int {
    val planet = graph.get(node)
    return if(planet == null) {
        acc
    } else {
        countOrbits(planet, graph, 1 + acc)
    }
}
