package me.mrkirby153.kcutils.utils.argparser


/**
 * An argument parser for parsing a series of arguments into key-value pairs
 */
class ArgumentParser {

    private val SPACE_RE = Regex("\"[^\"]+\"|\\S+")
    private val QUOTE_RE = Regex("^\"|\"\$")
    private val ARG_RE = Regex("-([^-])|--(\\S+)")

    private val options = mutableMapOf<String, Option>()

    /**
     * Adds an option to the argument parser
     *
     * @param option The option to add
     *
     * @throws IllegalArgumentException If the option is "-"
     */
    fun addOption(option: Option) {
        if (option.key == "-")
            throw IllegalArgumentException("Cannot use \"-\" as an option key")
        option.aliases.forEach {
            if (it == "-")
                throw IllegalArgumentException("Cannot use \"-\" as option key")
        }
        options[option.key] = option
        option.aliases.forEach {
            options[it] = option
        }
    }

    /**
     * Parse the arguments
     *
     * @param args An array of arguments to parse
     * @return The parsed arguments
     * @throws IllegalArgumentException If a flag is passed that is unrecognized to the parser
     * @throws MissingFlagException If a required flag was not passed
     */
    fun parse(args: Array<String>): ParsedArguments {
        var index = 0
        var currentOption: Option? = null
        val arguments = ParsedArguments()
        var current = ""
        val matchedArguments = mutableSetOf<Option>()

        fun recordLast() {
            if (currentOption != null) {
                arguments[currentOption!!.key] = current.trim()
                current = ""
                matchedArguments.add(currentOption!!)
            } else {
                if (current.isNotEmpty())
                    arguments.default = current.trim()
            }
        }

        while (index < args.size) {
            val arg = args[index]
            val r = ARG_RE.matchEntire(arg)
            if (r != null) {
                // We found a flag
                val groups = r.groupValues
                val flag = if (groups[1] == "") groups[2] else groups[1]
                val option = options[flag] ?: throw IllegalArgumentException(
                        "Unrecognized flag \"$flag\"")
                // Record the last
                recordLast()
                currentOption = option
            } else {
                current += "$arg "
            }
            index++
        }
        recordLast()
        val missing = options.values.distinct().filter { it !in matchedArguments }
        missing.forEach {
            if (it.required)
                throw MissingFlagException(it)
            arguments[it.key] = null
        }
        return arguments
    }

    /**
     * Parse the arguments in a string.
     *
     * Strings will be split on whitespace and strings surrounded in quotes will be treated as one string
     *
     * @param args The string
     * @throws IllegalArgumentException If a flag is passed that is unrecognized to the parser
     * @throws MissingFlagException If a required flag was not passed
     */
    fun parse(args: String) = parse(
            SPACE_RE.findAll(args).map { it.value.replace(QUOTE_RE, "") }.toList().toTypedArray())

    /**
     * Gets a help string for the arguments
     *
     * @return A help string
     */
    fun getHelp(): String {
        return buildString {
            appendln("Usage:")
            val opts = mutableMapOf<String, String>()
            val o = mutableMapOf<Option, MutableList<String>>()
            options.forEach { s, opt ->
                o.getOrPut(opt) { mutableListOf() }.add(s)
            }
            o.forEach { o, a ->
                val s = a.map { if (it.length == 1) "-$it" else "--$it" }.joinToString(", ")
                opts[s] = o.help ?: ""
            }
            val max = (opts.keys.map { it.length }.max() ?: 0) + 5
            opts.forEach { opt, help ->
                append("  ")
                append(opt)
                append(" ".repeat(Math.max(0, max - opt.length)))
                appendln(help)
            }
        }
    }
}

data class Option(val key: String, val required: Boolean = true,
                  val aliases: Array<String> = emptyArray(),
                  val help: String? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Option) return false

        if (key != other.key) return false
        if (required != other.required) return false
        if (!aliases.contentEquals(other.aliases)) return false
        if (help != other.help) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + required.hashCode()
        result = 31 * result + aliases.contentHashCode()
        result = 31 * result + (help?.hashCode() ?: 0)
        return result
    }
}

class ParsedArguments : HashMap<String, String?>() {
    var default: String? = null
}

class MissingFlagException(val option: Option) : Exception("Missing flag \"${option.key}\"")