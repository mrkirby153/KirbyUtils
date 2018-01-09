package me.mrkirby153.kcutils.utils

import java.util.Random

/**
 * Class to quickly generate random IDs
 */
class IdGenerator(val validChars: String) {

    private val random = Random()

    /**
     * Generate a random ID
     * @param length The length of the Id to generate
     *
     * @return The ID
     */
    fun generate(length: Int = 5): String {
        return buildString {
            for (i in 0 until length) {
                append(validChars[random.nextInt(validChars.length)])
            }
        }
    }

    companion object {
        val UPPERCASE_ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val LOWERCASE_ALPHA = UPPERCASE_ALPHA.toLowerCase()
        val NUMBERS = "0123456789"
        val ALPHA = UPPERCASE_ALPHA + LOWERCASE_ALPHA
    }
}