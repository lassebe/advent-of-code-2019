import java.io.File

fun main() {
    val robotProgram = File("input/day-11-robot-program.txt").readText()
    var direction = Direction.Up
    var location = Pair(0, 0)
    val panels = mutableMapOf<Pair<Int, Int>, Int>()
    // robot things
    var program = execute(robotProgram, input = mutableListOf(0), suspendOnRead = true)
    println(program)
    val index = 0
    do {
        val colour = program.outputs[index].toInt()
        val nextDirection = program.outputs[index + 1].toInt()
        println("painting $location: $colour, and turning $nextDirection")

        panels[location] = colour
        direction = if (nextDirection == 0) {
            turnLeft(direction)
        } else {
            turnRight(direction)
        }
        location = move(location, direction)
        val input = listOf(panels.getOrDefault(location, 0))
        program = execute(program.instructions, initialPointer = program.pointer,  input = input, suspendOnRead = true)
    } while (!program.terminated)

    println(panels.keys.size)
}


fun move(location: Pair<Int, Int>, direction: Direction) = when (direction) {
    Direction.Left -> Pair(location.first - 1, location.second)
    Direction.Right -> Pair(location.first + 1, location.second)
    Direction.Up -> Pair(location.first , location.second + 1)
    Direction.Down -> Pair(location.first, location.second - 1)
}

fun turnLeft(direction: Direction) = when (direction) {
    Direction.Left -> Direction.Down
    Direction.Right -> Direction.Up
    Direction.Up -> Direction.Left
    Direction.Down -> Direction.Right
}

fun turnRight(direction: Direction) = when (direction) {
    Direction.Left -> Direction.Up
    Direction.Right -> Direction.Down
    Direction.Up -> Direction.Right
    Direction.Down -> Direction.Left
}

enum class Direction {
    Left,
    Right,
    Up,
    Down
}