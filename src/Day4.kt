import kotlin.math.abs

fun main() {
    println(validPasswords(246540, 787419).size)
}

fun validPasswords(start: Int, end: Int): MutableList<Int> {
    val validPasswords = mutableListOf<Int>()
    loop@ for (pw in start..end) {
        val password = pw.toString()
        if (password.adjacentEquality()) {
            for (i in password.indices) {
                if (!password.drop(i + 1).increasing(password[i].toInt()))
                    continue@loop
            }
            validPasswords.add(pw)
        }
    }
    return validPasswords
}

fun String.adjacentEquality(): Boolean {
    val mappings = mutableMapOf<Char, List<Int>>()
    (this.indices).forEach {
        val digit = this[it]
        mappings[digit] =  mappings[digit]?.plus(it) ?: listOf(it)
    }
    return mappings.any { (_, indices) -> indices.size == 2 && abs(indices[0] - indices[1]) == 1 }
}

fun String.increasing(digit: Int): Boolean = this.asSequence().all { it.toInt() >= digit }