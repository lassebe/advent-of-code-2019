import java.io.File

fun main() {
    val program = File("input/acs.txt").readText()
    assert(maxPhaseSettings("3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0") == "43210" to 43210)
    assert(maxPhaseSettings("3,23,3,24,1002,24,10,24,1002,23,-1,23,101,5,23,23,1,24,23,23,4,23,99,0,0") == "01234" to 54321)
    assert(maxPhaseSettings("3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33,1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0") == "10432" to 65210)

    assert(maxPhaseSettings(program) == "03421" to 65464)

    assert(maxPhaseSettingsFeedback("3,26,1001,26,-4,26,3,27,1002,27,2,27,1,27,26,27,4,27,1001,28,-1,28,1005,28,6,99,0,0,5") == "98765" to 139629729)
    assert(maxPhaseSettingsFeedback("3,52,1001,52,-5,52,3,53,1,52,56,54,1007,54,5,55,1005,55,26,1001,54,-5,54,1105,1,12,1,53,54,53,1008,54,0,55,1001,55,1,55,2,53,55,53,4,53,1001,56,-1,56,1005,56,6,99,0,0,0,0,10") == "97856" to 18216)
    assert(maxPhaseSettingsFeedback(program) == "79568" to 1518124)
}

fun maxPhaseSettings(program: String): Pair<String, Int> =
        findPhaseSettings(program).maxBy { it.value }?.toPair() ?: Pair("", 0)

private fun findPhaseSettings(program: String): Map<String, Int> {
    val outputs = mutableMapOf<String, Int>()
    val phaseSettings = validPhaseSettings(0, 4)

    phaseSettings.forEach { settings ->
        val programs = MutableList(5) { SuspendedProgram(program, 0, 0, false) }
        val inputs = MutableList(5) { i -> mutableListOf(settings[i]) }
        inputs[0].add(0)
        programs[0] = run(programs[0].instructions, inputs[0])
        for (acs in (1..4)) {
            inputs[acs].add(programs[acs-1].output)
            programs[acs] = run(programs[acs].instructions, inputs[acs])
        }
        outputs[settings.joinToString("")] = programs[4].output
    }
    return outputs
}

private fun validPhaseSettings(lower: Int, upper: Int): Set<List<Int>> {
    val validSettings = mutableSetOf<List<Int>>()
    for (a in (lower..upper)) {
        for (b in (lower..upper)) {
            for (c in (lower..upper)) {
                for (d in (lower..upper)) {
                    for (e in (lower..upper)) {
                        val settings = setOf(a, b, c, d, e)
                        if (settings.size != 5)
                            continue
                        validSettings.add(settings.toList())
                    }
                }
            }
        }
    }
    return validSettings
}

fun maxPhaseSettingsFeedback(program: String): Pair<String, Int> =
        findPhaseSettingsFeedback(program).maxBy { it.value }?.toPair() ?: Pair("", 0)


private fun findPhaseSettingsFeedback(program: String): Map<String, Int> {
    val outputs = mutableMapOf<String, Int>()
    val phaseSettings = validPhaseSettings(5, 9)

    phaseSettings.forEach { settings ->
        val programs = MutableList(5) { SuspendedProgram(program, 0, 0, false) }
        val inputs = MutableList(5) { i -> mutableListOf(settings[i]) }

        while (!programs[4].terminated) {
            for (acs in (0..4)) {
                inputs[acs].add(programs[(acs + 4) % 5].output)
                programs[acs] = run(programs[acs].instructions, inputs[acs], programs[acs].pointer, suspendOnRead = true)
            }
            outputs[settings.joinToString("")] = programs[4].output
        }
    }


    return outputs
}