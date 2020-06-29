import java.io.File

fun main() {
    val program = File("input/acs.txt").readText()
    assert(maxPhaseSettings("3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0") == "43210" to 43210)
    assert(maxPhaseSettings("3,23,3,24,1002,24,10,24,1002,23,-1,23,101,5,23,23,1,24,23,23,4,23,99,0,0") == "01234" to 54321)
    assert(maxPhaseSettings("3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33,1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0") == "10432" to 65210)

    assert(maxPhaseSettings(program) == "03421" to 65464)
}

fun maxPhaseSettings(program: String): Pair<String, Int> =
        findPhaseSettings(program).maxBy { it.value }?.toPair() ?: Pair("", 0)

private fun findPhaseSettings(program: String) : Map<String,Int>{
    val outputs = mutableMapOf<String,Int>()
    for(i in (1234 .. 43210)) {
        val settings = i.toString().padStart(5, '0')

        val phaseSettings = settings.split("").subList(1,6).map { it.toInt() }
        if(phaseSettings.toSet().size != 5 || phaseSettings.any { it > 4 }) {
            continue
        }
        val aOutput = run(program, mutableListOf(phaseSettings[0], 0)).second
        val bOutput = run(program, mutableListOf(phaseSettings[1], aOutput)).second
        val cOutput = run(program, mutableListOf(phaseSettings[2], bOutput)).second
        val dOutput = run(program, mutableListOf(phaseSettings[3], cOutput)).second
        val eOutput = run(program, mutableListOf(phaseSettings[4], dOutput)).second
        outputs[settings] = eOutput
    }
    return outputs
}

