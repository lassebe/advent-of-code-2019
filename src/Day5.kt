import java.io.File

fun main() {
    val program = File("input/test.txt").readText()
    assert(run("1002,4,3,4,33") == "1002,4,3,4,99")
    assert(run("1101,100,-1,4,0") == "1101,100,-1,4,99")

    run("3,0,4,0,99", input = 42)
    println(run(program, 1))

}
