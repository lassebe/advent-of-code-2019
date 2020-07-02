import java.io.File
import java.math.BigInteger

fun main() {
    assert(execute("109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99").outputs.joinToString(",")
            == "109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99")

    assert(execute("1102,34915192,34915192,7,4,7,99,0").outputs.first() ==
            34915192L * 34915192)

    assert(execute("104,1125899906842624,99").outputs.first() == 1125899906842624L)

    val boost = File("input/day-9-boost.txt").readText()
    assert(
            execute(
                    boost,
                    input = 1
            ).outputs
                    == listOf(4261108180)
    )

    assert(
            execute(
                    boost,
                    input = 2
            ).outputs
                    == listOf(77944L)
    )
}