import ParameterMode.*

sealed class OpCode() {
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

abstract class ArithmeticOperation : OpCode()

abstract class InputOutputOperation: OpCode()

object Add : ArithmeticOperation()

object Multiply : ArithmeticOperation()

object Read : InputOutputOperation()

object Write : InputOutputOperation()

object Terminate : OpCode()

fun instructionSize(opCode: OpCode): Int = when(opCode) {
    is ArithmeticOperation -> 4
    is InputOutputOperation -> 2
    Terminate -> 1
}

class Instruction(opCode: OpCode, parameterValues: List<Int>, parameterModes: List<Int>) {
    val parameters: List<Parameter> = when (opCode) {
        is Terminate -> listOf()
        else -> {
            assert(parameterValues.size == parameterModes.size)
            parameterValues.mapIndexed { index, _ ->
                if (parameterModes[index] == 0) {
                    Parameter(parameterValues[index], PositionMode)
                } else {
                    Parameter(parameterValues[index], ImmediateMode)
                }
            }
        }
    }
}

data class Parameter(val value: Int, val mode: ParameterMode)

enum class ParameterMode {
    PositionMode,
    ImmediateMode
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
        val parameterValues = instructions.slice((pointer + 1 until pointer + instructionSize(opcode)))

        val modes = if (instruction.length != 1) {
            val givenModes = instruction.dropLast(2)
            givenModes.padStart(parameterValues.size, '0').asSequence().map { Character.getNumericValue(it) }.toList().reversed()
        } else {
            // backward compatibility for Day 2
            List(parameterValues.size) { 0 }
        }

        val instruct = Instruction(opcode, parameterValues, modes)

        val result = when (opcode) {
            is ArithmeticOperation -> {
                val op1 = instruct.parameters[0]
                val op2 = instruct.parameters[1]
                val target = instruct.parameters[2]
                if (opcode is Add) {
                    applyOperation(target, op1, op2) { a, b -> a + b }
                } else if (opcode is Multiply) {
                    applyOperation(target, op1, op2) { a, b -> a * b }
                }
                instructions
            }
            is InputOutputOperation -> {
                val value = interpretTarget(instruct.parameters[0])
                if(opcode is Write) {
                    println("OUT: ${instructions[value]}")
                } else if(opcode is Read) {
                    instructions[value] = input
                }
                instructions
            }
            is Terminate -> instructions
        }
        return Pair(pointer + instructionSize(opcode), result)
    }

    private fun applyOperation(target: Parameter, op1: Parameter, op2: Parameter, operation: (Int, Int) -> Int) {
        instructions[interpretTarget(target)] = operation(interpretOperand(op1), interpretOperand(op2))
    }

    private fun interpretOperand(param: Parameter) = if (param.mode == PositionMode) {
        instructions[param.value]
    } else {
        param.value
    }

    private fun interpretTarget(param: Parameter) = if (param.mode == PositionMode) {
        param.value
    } else {
        instructions[param.value]
    }
}

fun run(programText: String, input: Int = -1): String {
    val instructions = programText.split(",").map { it.toInt() }.toMutableList()
    var program = Program(instructions, input = input)
    var pointer = 0
    loop@ while (true) {
        val instruction = program.instructions[pointer]
        val opCode = OpCode.fromInt(instruction.toString().takeLast(2).toInt())
//        println("executing ${program.instructions.slice(pointer until pointer+instructionSize(opCode))} at $pointer")
        if (opCode is Terminate)
            break@loop
        val (updatedPointer, updatedInstructions) = program.execute(pointer)
        pointer = updatedPointer
        program = Program(updatedInstructions.toMutableList(), input)
//        println("next is  $pointer")
    }
    return program.instructions.joinToString(",")
}

