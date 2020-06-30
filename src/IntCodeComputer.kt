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

abstract class ArithmeticOperation : OpCode() {
    abstract fun operation(a: Int, b: Int): Int
}

abstract class InputOutputOperation : OpCode()

abstract class JumpOperation : OpCode() {
    abstract fun condition(arg: Int): Boolean
}

object Add : ArithmeticOperation() {
    override fun operation(a: Int, b: Int): Int = a + b
}

object Multiply : ArithmeticOperation() {
    override fun operation(a: Int, b: Int): Int = a * b
}

object LessThan : ArithmeticOperation() {
    override fun operation(a: Int, b: Int): Int = if (a < b) 1 else 0
}

object Equals : ArithmeticOperation() {
    override fun operation(a: Int, b: Int): Int = if (a == b) 1 else 0
}

object Read : InputOutputOperation()
object Write : InputOutputOperation()

object JumpIfTrue : JumpOperation() {
    override fun condition(arg: Int): Boolean = arg != 0
}

object JumpIfFalse : JumpOperation() {
    override fun condition(arg: Int): Boolean = arg == 0
}

object Terminate : OpCode()

data class Parameter(val value: Int, val mode: ParameterMode) {
    override fun toString(): String = if (mode == PositionMode) {
        "&$value"
    } else {
        value.toString()
    }
}

fun instructionSize(opCode: OpCode): Int = when (opCode) {
    is ArithmeticOperation -> 4
    is InputOutputOperation -> 2
    is JumpOperation -> 3
    Terminate -> 1
}

class Instruction(val opCode: OpCode, parameterValues: List<Int>, parameterModes: Map<Int, Int>) {
    val parameters: List<Parameter> = when (opCode) {
        is Terminate -> listOf()
        else -> {
            parameterValues.mapIndexed { index, _ ->
                if (parameterModes.getOrDefault(index, 0) == 0) {
                    Parameter(parameterValues[index], PositionMode)
                } else {
                    Parameter(parameterValues[index], ImmediateMode)
                }
            }
        }
    }

    override fun toString(): String = "${opCode.toString().split("@")[0]}: ${parameters.joinToString(" ")}"
}

enum class ParameterMode {
    PositionMode,
    ImmediateMode
}

data class Program(val instructions: MutableList<Int>, val input: MutableList<Int>, var output: Int, val debug: Boolean = false) {

    fun execute(pointer: Int): Pair<Int, List<Int>> {
        val instruction = parseInstruction(pointer)

        return when (val opCode = instruction.opCode) {
            is ArithmeticOperation -> {
                val op1 = interpretOperand(instruction.parameters[0])
                val op2 = interpretOperand(instruction.parameters[1])
                val target = interpretTarget(instruction.parameters[2])
                if (debug) println("${opCode.toString().split("@")[0]} $op1 $op2 $target")
                instructions[target] = opCode.operation(op1, op2)
                Pair(pointer + instructionSize(opCode), instructions)
            }

            is InputOutputOperation -> {
                val parameter = instruction.parameters[0]

                if (opCode is Write) {
                    val value = interpretOperand(parameter)
                    if (debug) println("Write $value")
                    output = value
                } else if (opCode is Read) {
                    val value = interpretTarget(parameter)
                    if (input.size == 0) {
                        return Pair(pointer, instructions)
                    }
                    if (debug) println("Read ${input[0]} into $value")
                    instructions[value] = input.removeAt(0)
                }
                Pair(pointer + instructionSize(opCode), instructions)
            }

            is JumpOperation -> {
                val guard = interpretOperand(instruction.parameters[0])
                val target = interpretOperand(instruction.parameters[1])

                if (opCode.condition(guard)) {
                    if (debug) println("Jump to :$target")
                    return Pair(target, instructions)
                }
                Pair(pointer + instructionSize(opCode), instructions)
            }
            is Terminate -> Pair(pointer + instructionSize(opCode), instructions)

        }
    }


    private fun parseInstruction(pointer: Int): Instruction {
        val instruction = instructions[pointer].toString()

        val opcode = if (instruction.length != 1) {
            OpCode.fromInt(instruction.takeLast(2).toInt())
        } else {
            // backward compatibility for Day 2
            OpCode.fromInt(instruction.toInt())
        }

        val parameterValues = instructions.slice((pointer + 1 until pointer + instructionSize(opcode)))
        val modes = if (instruction.length != 1) {
            instruction.dropLast(2).reversed().mapIndexed { index, mode ->
                index to Character.getNumericValue(mode)
            }.toMap()
        } else {
            // backward compatibility for Day 2
            emptyMap()
        }

        return Instruction(opcode, parameterValues, modes)
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


    fun printReadable() {
        var pointer = 0
        loop@ while (pointer < instructions.size) {
            val ins = instructions[pointer]
            try {
                val opCode = OpCode.fromInt(ins.toString().takeLast(2).toInt())

                if (opCode is Terminate)
                    break@loop

                val instruction = parseInstruction(pointer)
                println("$pointer: $instruction")
                pointer += instructionSize(instruction.opCode)
            } catch (e: java.lang.Exception) {
                println("Invalid instruction")
                pointer++
            }
        }
    }

}

fun run(programText: String, input: Int, debug: Boolean = false): SuspendedProgram =
        run(programText, mutableListOf(input), debug = debug)


fun run(programText: String,
        input: MutableList<Int> = mutableListOf(),
        initialPointer: Int = 0,
        debug: Boolean = false,
        suspendOnRead: Boolean = false
): SuspendedProgram {
    val instructions = programText.split(",").map { it.toInt() }.toMutableList()
    var program = Program(instructions, input = input, output = -1, debug = debug)
    var pointer = initialPointer
    if (debug) {
        program.printReadable()
    }

    loop@ while (true) {
        val instruction = program.instructions[pointer]
        val opCode = OpCode.fromInt(instruction.toString().takeLast(2).toInt())
        if (debug) println("executing ${program.instructions.slice(pointer until pointer + instructionSize(opCode))} at :$pointer")

        if (opCode is Terminate) {
            if (debug) println("Terminating with ${program.output}")
            return SuspendedProgram(program.instructions.joinToString(","), program.output)
        }

        if (opCode is Read && input.size == 0 && suspendOnRead) {
            val (updatedPointer, updatedInstructions) = program.execute(pointer)
            return SuspendedProgram(updatedInstructions.joinToString(","), program.output, updatedPointer, false)
        }

        val (updatedPointer, updatedInstructions) = program.execute(pointer)
        pointer = updatedPointer
        program = Program(updatedInstructions.toMutableList(), input, program.output, debug)
        if (debug) println("next is :$pointer\n")
    }
}

data class SuspendedProgram(
        val instructions: String,
        val output: Int,
        val pointer: Int = -1,
        val terminated: Boolean = true
)


