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
    for (i in 1 until this.length) {
        if (this[i - 1] == this[i]) return true
    }

    return false
}

fun String.increasing(digit: Int): Boolean = this.asSequence().all { it.toInt() >= digit }