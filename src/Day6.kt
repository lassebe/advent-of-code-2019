import java.io.File

fun main() {
    val exampleGraph = createGraph("input/day-6-small-map.txt")
    assert(depths(exampleGraph) == 42)
    val graph = createGraph("input/day-6-map.txt")
    println(depths(graph))

    val santasOrbits = allOrbits("SAN", graph)
    val yourOrbits = allOrbits("YOU", graph)
    loop@for(planet in yourOrbits) {
        if(santasOrbits.contains(planet)) {
            println(santasOrbits.indexOf(planet) + yourOrbits.indexOf(planet))
            break@loop
        }
    }
}

private fun createGraph(fileName: String): Map<String, String> {
    val graph = mutableMapOf<String, String>()
    File(fileName).readLines().map {
        val parent = it.split(")")[0]
        val child = it.split(")")[1]
        graph.put(child, parent)
    }
    return graph
}


private fun allOrbits(planet: String, graph: Map<String, String>): Set<String> {
    val parent = graph.entries.find { (k, _) -> k == planet }?.value ?: return emptySet()
    return setOf(parent) + allOrbits(parent, graph)
}

private fun depths(graph: Map<String, String>) = graph.keys.map {
    countOrbits(it, graph, 0)
}.sum()

private fun countOrbits(node: String, graph: Map<String, String>, acc: Int): Int {
    val planet = graph[node]
    return if (planet == null) {
        acc
    } else {
        countOrbits(planet, graph, 1 + acc)
    }
}
