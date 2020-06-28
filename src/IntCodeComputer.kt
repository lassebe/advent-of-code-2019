import ParameterMode.*

sealed class OpCode {
    companion object {
        fun fromInt(code: Int): OpCode {
            return when (code) {
                1 -> Add
                2 -> Multiply
                3 -> Read
                4 -> Write
                5 -> JumpIfTrue
                6 -> JumpIfFalse
                7 -> LessThan
                8 -> Equals
                99 -> Terminate
                else -> {
                    throw java.lang.Exception("Invalid opcode = $code!")
                }
            }
        }
    }
}

abstract class ArithmeticOperation : OpCode()

abstract class InputOutputOperation : OpCode()

abstract class JumpOperation : OpCode()

object Add : ArithmeticOperation()
object Multiply : ArithmeticOperation()
object LessThan : ArithmeticOperation()
object Equals : ArithmeticOperation()

object Read : InputOutputOperation()
object Write : InputOutputOperation()

object JumpIfTrue : JumpOperation()
object JumpIfFalse : JumpOperation()

object Terminate : OpCode()

fun instructionSize(opCode: OpCode): Int = when (opCode) {
    is ArithmeticOperation -> 4
    is InputOutputOperation -> 2
    is JumpOperation -> 3
    Terminate -> 1
}

class Instruction(private val opCode: OpCode, parameterValues: List<Int>, parameterModes: List<Int>) {
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

    override fun toString(): String = "${opCode.toString().split("@")[0]}: ${parameters.joinToString(" ")}"
}

data class Parameter(val value: Int, val mode: ParameterMode) {
//    override fun toString(): String = if(mode == PositionMode) {
//        "&$value"
//    } else {
//        value.toString()
//    }
}

enum class ParameterMode {
    PositionMode,
    ImmediateMode
}

data class Program(val instructions: MutableList<Int>, val input: Int, var output: Int, val debug: Boolean = false) {

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
                val operation = { a: Int, b: Int ->
                    when (opcode) {
                        is Add -> a + b
                        is Multiply -> a * b
                        is LessThan -> if (a < b) 1 else 0
                        is Equals -> if (a == b) 1 else 0
                        else -> throw Exception("Should not be possible")
                    }
                }
                if (debug) println("${opcode.toString().split("@")[0]} ${interpretOperand(op1)} ${interpretOperand(op2)} ${interpretTarget(target)}")

                applyOperation(target, op1, op2, operation)
                instructions
            }

            is InputOutputOperation -> {
                val parameter = instruct.parameters[0]

                if (opcode is Write) {
                    val value = interpretOperand(parameter)
                    if (debug) println("Write $value")
                    output = value
                } else if (opcode is Read) {
                    val value = interpretTarget(parameter)
                    if (debug) println("Read $input into $value")
                    instructions[value] = input
                }
                instructions
            }

            is JumpOperation -> {
                val guard = interpretOperand(instruct.parameters[0])
                val target = interpretOperand(instruct.parameters[1])
                val condition = { arg: Int ->
                    when (opcode) {
                        is JumpIfTrue -> {
                            if (debug) println("JumpIfTrue $arg")
                            arg != 0
                        }
                        is JumpIfFalse -> {
                            if (debug) println("JumpIfFalse $arg")
                            arg == 0
                        }
                        else -> {
                            throw Exception("Invalid Jump Instruction!")
                        }
                    }
                }

                if(condition(guard)) {
                    if (debug) println("Jump to $target")
                    return Pair(target, instructions)
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

fun run(programText: String, input: Int = -1, debug: Boolean = false): Pair<String, Int> {
    val instructions = programText.split(",").map { it.toInt() }.toMutableList()
    var program = Program(instructions, input = input, output = -1, debug = debug)
    var pointer = 0
    if(debug) {
        loop2@ while (true) {
            val ins = program.instructions[pointer]
            try {
                val opCode = OpCode.fromInt(ins.toString().takeLast(2).toInt())

                if (opCode is Terminate)
                    break@loop2
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
                println("$pointer: $instruct")
                pointer += instructionSize(opcode)

            }catch(e: java.lang.Exception) {
                pointer++
            }

        }
    }
    pointer = 0

    loop@ while (true) {
        val instruction = program.instructions[pointer]
        val opCode = OpCode.fromInt(instruction.toString().takeLast(2).toInt())
        if (debug) println("executing ${program.instructions.slice(pointer until pointer + instructionSize(opCode))} at $pointer")

        if (opCode is Terminate)
            break@loop
        val (updatedPointer, updatedInstructions) = program.execute(pointer)
        pointer = updatedPointer
        program = Program(updatedInstructions.toMutableList(), input, program.output, debug)
        if(debug) println("next is  $pointer\n")
    }
    if (debug) println()
    return Pair(program.instructions.joinToString(","), program.output)
}

