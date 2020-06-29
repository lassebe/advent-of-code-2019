import java.io.File

fun main() {
    val program = File("input/test.txt").readText()
    assert(run("1002,4,3,4,33").instructions == "1002,4,3,4,99")
    assert(run("1101,100,-1,4,0").instructions == "1101,100,-1,4,99")

    assert(run("3,0,4,0,99", input = 42).output == 42)
    assert(run(program, 1).output == 13285749)

    // Equal to 8 - position mode
    assert(run("3,9,8,9,10,9,4,9,99,-1,8", input = 13).output == 0)
    assert(run("3,9,8,9,10,9,4,9,99,-1,8", 5).output == 0)
    assert(run("3,9,8,9,10,9,4,9,99,-1,8", 8).output == 1)

    // Equal to 8 - immediate mode
    assert(run("3,3,1108,-1,8,3,4,3,99", 13).output == 0)
    assert(run("3,3,1108,-1,8,3,4,3,99", 5).output == 0)
    assert(run("3,3,1108,-1,8,3,4,3,99", 8).output == 1)

    // Less than 8 - position mode
    assert(run("3,9,7,9,10,9,4,9,99,-1,8", 9).output == 0)
    assert(run("3,9,7,9,10,9,4,9,99,-1,8", 8).output == 0)
    assert(run("3,9,7,9,10,9,4,9,99,-1,8", 7).output == 1)
    // Less than 8 - position mode
    assert(run("3,3,1107,-1,8,3,4,3,99", 9).output == 0)
    assert(run("3,3,1107,-1,8,3,4,3,99", 8).output == 0)
    assert(run("3,3,1107,-1,8,3,4,3,99", 7).output == 1)

    // Jump return if input != 0 - position mode
    assert(run("3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9", 112).output == 1)
    assert(run("3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9", 0).output == 0)
    // Jump return if input != 0 - immediate mode
    assert(run("3,3,1105,-1,9,1101,0,0,12,4,12,99,1", 112).output == 1)
    assert(run("3,3,1105,-1,9,1101,0,0,12,4,12,99,1", 0).output == 0)

    // Output 999 if input < 8 ; 1000 if input == 8 ; 1001 if input > 8
    val largerProgram = "3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99"
    assert(run(largerProgram, 7).output == 999)
    assert(run(largerProgram, 8).output == 1000)
    assert(run(largerProgram, 9).output == 1001)

    assert(run(program, 5).output == 5000972)

}
