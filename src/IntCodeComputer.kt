import ParameterMode.*

sealed class OpCode() {
    abstract fun size(): Int

    companion object {
        fun fromInt(code: Int): OpCode {
            return when (code) {
                1 -> Add
                2 -> Multiply
                3 -> Read
                4 -> Write
                99 -> Terminate
                else -> {
                    throw java.lang.Exception("Invalid opcode = $code!")
                }
            }
        }
    }
}


object Add : OpCode() {
    override fun size(): Int = 4
}

object Multiply : OpCode() {
    override fun size(): Int = 4
}

object Read : OpCode() {
    override fun size(): Int = 2
}

object Write : OpCode() {
    override fun size(): Int = 2
}

object Terminate : OpCode() {
    override fun size(): Int = 1
}

class Instruction(opCode: OpCode, parameterValues: List<Int>, parameterModes: List<Int>) {
    val parameters: List<Parameter> = when (opCode) {
        is Add, is Multiply -> {
            assert(parameterValues.size == parameterModes.size)
            parameterValues.mapIndexed { index, _ ->
                if (parameterModes[index] == 0) {
                    Parameter(parameterValues[index], PositionMode)
                } else {
                    Parameter(parameterValues[index], ImmediateMode)
                }
            }
        }
        is Read, is Write -> {
            if (parameterModes[0] == 0) {
                listOf(Parameter(parameterValues.first(), PositionMode))
            } else {
                listOf(Parameter(parameterValues.first(), ImmediateMode))
            }
        }
        is Terminate -> listOf()
    }
}

data class Parameter(val value: Int, val mode: ParameterMode)

enum class ParameterMode(val mode: Int) {
    PositionMode(0),
    ImmediateMode(1)
}

data class Program(val instructions: MutableList<Int>, val input: Int) {
    fun execute(pointer: Int): Pair<Int, List<Int>> {
        val instruction = instructions[pointer].toString()

        val opcode = if (instruction.length != 1) {
            OpCode.fromInt(instruction.takeLast(2).toInt())
        } else {
            // backward compatibility for Day 2
            OpCode.fromInt(instruction.toInt())
        }
        val parameterValues = instructions.slice((pointer + 1 until pointer + opcode.size()))

        val modes = if (instruction.length != 1) {
            val givenModes = instruction.dropLast(2)
            givenModes.padStart(parameterValues.size, '0').asSequence().map { Character.getNumericValue(it) }.toList().reversed()
        } else {
            // backward compatibility for Day 2
            List(parameterValues.size) { 0 }
        }

        val instruct = Instruction(opcode, parameterValues, modes)

        val result = when (opcode) {
            is Add, is Multiply -> {
                val op1 = instruct.parameters[0]
                val op2 = instruct.parameters[1]
                val target = instruct.parameters[2]
                if (opcode is Add) {
                    applyOperation(target, op1, op2) { a, b -> a + b }
                } else {
                    applyOperation(target, op1, op2) { a, b -> a * b }
                }
                instructions
            }
            is Read -> {
                val param = instruct.parameters[0]
                if(param.mode == PositionMode) {
                    instructions[param.value] = input
                } else {
                    val address = instructions[param.value]
                    instructions[address] = input
                }
                instructions
            }
            is Write -> {
                val param = instruct.parameters[0]
                if(param.mode == PositionMode) {
                    println("OUT: ${instructions[param.value]}")
                } else {
                    val address = instructions[param.value]
                    println("OUT: ${instructions[instructions[address]]}")
                }
                instructions
            }
            is Terminate -> instructions
        }
        return Pair(pointer + opcode.size(), result)
    }

    private fun applyOperation(target: Parameter, op1: Parameter, op2: Parameter, operation: (Int, Int) -> Int) {
        if (target.mode == PositionMode) {
            instructions[target.value] = operation(interpretParameter(op1), interpretParameter(op2))
        } else {
            val address = instructions[target.value]
            instructions[address] = operation(interpretParameter(op1), interpretParameter(op2))
        }
    }

    private fun interpretParameter(op1: Parameter) = if (op1.mode == PositionMode) {
        instructions[op1.value]
    } else {
        op1.value
    }

}

fun run(programText: String, input: Int = -1): String {
    val instructions = programText.split(",").map { it.toInt() }.toMutableList()
    var program = Program(instructions, input = input)
    var pointer = 0
    loop@ while (true) {
        val instruction = program.instructions[pointer]
        val opCode = OpCode.fromInt(instruction.toString().takeLast(2).toInt())
//        println("executing ${program.instructions.slice(pointer until pointer+opCode.size())} at $pointer")
        if (opCode is Terminate)
            break@loop
        val (updatedPointer, updatedInstructions) = program.execute(pointer)
        pointer = updatedPointer
        program = Program(updatedInstructions.toMutableList(), input)
//        println("next is  $pointer")
    }
    return program.instructions.joinToString(",")
}

