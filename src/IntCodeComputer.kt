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
                9 -> RelativeBase
                99 -> Terminate
                else -> {
                    throw java.lang.Exception("Invalid opcode = $code!")
                }
            }
        }
    }
}

abstract class ArithmeticOperation : OpCode() {
    abstract fun operation(a: Long, b: Long): Long
}

abstract class InputOutputOperation : OpCode()

abstract class JumpOperation : OpCode() {
    abstract fun condition(arg: Long): Boolean
}

object Add : ArithmeticOperation() {
    override fun operation(a: Long, b: Long): Long = a + b
}

object Multiply : ArithmeticOperation() {
    override fun operation(a: Long, b: Long): Long = a * b
}

object LessThan : ArithmeticOperation() {
    override fun operation(a: Long, b: Long): Long = if (a < b) 1 else 0
}

object Equals : ArithmeticOperation() {
    override fun operation(a: Long, b: Long): Long = if (a == b) 1 else 0
}

object Read : InputOutputOperation()
object Write : InputOutputOperation()

object JumpIfTrue : JumpOperation() {
    override fun condition(arg: Long): Boolean = arg != 0L
}

object JumpIfFalse : JumpOperation() {
    override fun condition(arg: Long): Boolean = arg == 0L
}

object RelativeBase : OpCode()

object Terminate : OpCode()

data class Parameter(val value: Long, val mode: ParameterMode) {
    override fun toString(): String = when (mode) {
        PositionMode -> {
            "&$value"
        }
        ImmediateMode -> {
            value.toString()
        }
        RelativeMode -> {
            "R + $value"
        }
    }
}

fun instructionSize(opCode: OpCode): Int = when (opCode) {
    is ArithmeticOperation -> 4
    is InputOutputOperation -> 2
    is JumpOperation -> 3
    is RelativeBase -> 2
    Terminate -> 1
}

class Instruction(
        val opCode: OpCode,
        parameterValues: List<Long>,
        parameterModes: Map<Int, Int>
) {
    val parameters: List<Parameter> = when (opCode) {
        is Terminate -> listOf()
        else -> {
            parameterValues.mapIndexed { index, _ ->
                when (parameterModes.getOrDefault(index, 0)) {
                    0 -> {
                        Parameter(parameterValues[index], PositionMode)
                    }
                    1 -> {
                        Parameter(parameterValues[index], ImmediateMode)
                    }
                    2 -> {
                        Parameter(parameterValues[index], RelativeMode)
                    }
                    else -> throw Exception("Invalid parameter mode.")
                }
            }
        }
    }

    override fun toString(): String = "${opCode.toString().split("@")[0]}: ${parameters.joinToString(" ")}"
}

enum class ParameterMode {
    PositionMode,
    ImmediateMode,
    RelativeMode
}

data class Program(
        var instructions: MutableList<Long>,
        val input: MutableList<Long>,
        var output: MutableList<Long> = mutableListOf(),
        var relativeBase: Long = 0L,
        val debug: Boolean = false
) {
    override fun toString(): String = "i=$input o=$output"

    fun execute(pointer: Int): Pair<Int, List<Long>> {
        val instruction = parseInstruction(pointer)

        return when (val opCode = instruction.opCode) {
            is ArithmeticOperation -> {
                val op1 = interpretOperand(instruction.parameters[0])
                val op2 = interpretOperand(instruction.parameters[1])
                val target = interpretTarget(instruction.parameters[2])
                if (debug) println("${opCode.toString().split("@")[0]} $op1 $op2 $target")
                expandMemoryIfNecessary(target.toInt())
                instructions[target.toInt()] = opCode.operation(op1, op2)
                Pair(pointer + instructionSize(opCode), instructions)
            }

            is InputOutputOperation -> {
                val parameter = instruction.parameters[0]

                if (opCode is Write) {
                    val value = interpretOperand(parameter)
                    if (debug) println("Write $value")
                    output.add(value)
                } else if (opCode is Read) {
                    val value = interpretTarget(parameter)
                    if (input.size == 0) {
                        if (debug) println("Read suspending until given input")
                        return Pair(pointer, instructions)
                    }
                    if (debug) println("Read ${input[0]} into $value")
                    instructions[value.toInt()] = input.removeAt(0)
                }
                Pair(pointer + instructionSize(opCode), instructions)
            }

            is JumpOperation -> {
                val guard = interpretOperand(instruction.parameters[0])
                val target = interpretOperand(instruction.parameters[1])

                if (opCode.condition(guard)) {
                    if (debug) println("Jump to :$target")
                    return Pair(target.toInt(), instructions)
                }
                Pair(pointer + instructionSize(opCode), instructions)
            }

            is RelativeBase -> {
                val parameter = interpretOperand(instruction.parameters[0])
                relativeBase += parameter
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

    private fun interpretOperand(param: Parameter): Long {
        return when (param.mode) {
            PositionMode -> {
                val index = param.value.toInt()
                expandMemoryIfNecessary(index)
                instructions[index]
            }
            ImmediateMode -> {
                param.value
            }
            RelativeMode -> {
                val index = (param.value + relativeBase).toInt()
                expandMemoryIfNecessary(index)
                instructions[index]
            }
        }
    }

    private fun interpretTarget(param: Parameter): Long {
        return when (param.mode) {
            PositionMode -> {
                param.value
            }
            ImmediateMode -> {
                val index = param.value.toInt()
                expandMemoryIfNecessary(index)
                instructions[index]
            }
            RelativeMode -> {
                param.value + relativeBase
            }
        }
    }

    private fun expandMemoryIfNecessary(index: Int) {
        if (index >= instructions.size) {
            instructions = MutableList(index * 2) {
                if (it < instructions.size) {
                    instructions[it]
                } else {
                    0
                }
            }
        }
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

fun execute(programText: String, input: Int, debug: Boolean = false): SuspendedProgram =
        execute(programText, listOf(input), debug = debug)

fun execute(programText: String,
            input: List<Int> = listOf(),
            initialPointer: Int = 0,
            debug: Boolean = false,
            suspendOnRead: Boolean = false
): SuspendedProgram {
    val instructions = programText.split(",").map { it.toLong() }.toMutableList()
    var program = Program(instructions, input = input.map { it.toLong() }.toMutableList(), debug = debug)
    var pointer = initialPointer
    if (debug) {
        program.printReadable()
    }

    loop@ while (true) {
        val instruction = program.instructions[pointer]
        val opCode = OpCode.fromInt(instruction.toString().takeLast(2).toInt())
        if (debug) println("executing at :$pointer ${program.instructions.slice(pointer until pointer + instructionSize(opCode))} ")

        if (opCode is Terminate) {
            if (debug) println("Terminating with ${program.output}")
            return SuspendedProgram(program.instructions.joinToString(","), program.output)
        }
        if (opCode is Read && program.input.size == 0 && suspendOnRead) {
            if (debug) println("Suspending with ${program.output}")
            return SuspendedProgram(program.instructions.joinToString(","), program.output, pointer, false)
        }

        val (updatedPointer, updatedInstructions) = program.execute(pointer)
        pointer = updatedPointer
        program = Program(
                updatedInstructions.toMutableList(),
                program.input,
                program.output,
                program.relativeBase,
                debug
        )
        if (debug) println("next is :$pointer\n")
    }
}

data class SuspendedProgram(
        val instructions: String,
        val outputs: List<Long>,
        val pointer: Int = -1,
        val terminated: Boolean = true
) {
    val output: Int by lazy { outputs.last().toInt() }
}


