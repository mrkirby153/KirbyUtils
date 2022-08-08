package me.mrkirby153.kcutils.utils.argparser

import java.util.LinkedList


/**
 * An argument parser for parsing a series of arguments into key-value pairs
 */
class ArgumentParser {

    private val SPACE_RE = Regex("\"[^\"]+\"|\\S+")
    private val QUOTE_RE = Regex("^\"|\"\$")
    private val ARG_RE = Regex("-([^-])|--(\\S+)")

    private val arguments = mutableListOf<Option>()


    /**
     * Adds an option to the argument parser
     *
     * @param option The option to add
     *
     * @throws IllegalArgumentException
     */
    fun addOption(option: Option) {
        if (option.key == "-")
            throw IllegalArgumentException("Cannot use \"-\" as an option key")
        option.aliases.forEach {
            if (it == "-")
                throw IllegalArgumentException("Cannot use \"-\" as an option key")
        }
        arguments.add(option)
    }


    /**
     * Parse the arguments
     *
     * @param args An array of arguments to parse
     * @return The parsed arguments
     *
     * @throws IllegalArgumentException
     * @throws MissingArgumentException
     */
    fun parse(args: Array<String>): ParsedArguments {
        val remainingArgs = LinkedList<String>()
        remainingArgs.addAll(args)
        val arguments = ParsedArguments()


        fun processArg(arg: Option): String {
            val flagValue = mutableListOf<String>()
            var count = 0
            when (arg.nargs) {
                // All remaining arguments
                "~" -> remainingArgs.forEach { flagValue.add(it) }
                // All arguments until next flag
                "*" -> {
                    while (remainingArgs.isNotEmpty()) {
                        val next = remainingArgs.peek()
                        if (ARG_RE.matchEntire(next) != null)
                            break
                        else
                            flagValue.add(remainingArgs.pop())
                    }
                }
                // All arguments until next flag (mandates one)
                "+" -> {
                    var found = false
                    while (remainingArgs.isNotEmpty()) {
                        val next = remainingArgs.peek()
                        if (ARG_RE.matchEntire(next) != null)
                            break
                        else {
                            flagValue.add(remainingArgs.pop())
                            found = true
                        }
                    }
                    if (!found)
                        throw IllegalArgumentException(
                                "Argument \"${arg.key}\" requires at least one value")
                }
                else -> {
                    val maxArgs = try {
                        arg.nargs.toInt()
                    } catch (e: NumberFormatException) {
                        throw IllegalArgumentException("Invalid nargs ${arg.nargs}")
                    }
                    while (remainingArgs.isNotEmpty()) {
                        if (count >= maxArgs || ARG_RE.matchEntire(remainingArgs.peek()) != null)
                            break
                        flagValue.add(remainingArgs.pop())
                        count++
                    }
                    if (count < maxArgs)
                        throw IllegalArgumentException(
                                "Invalid number of args. Expected $maxArgs found $count")
                }
            }
            return flagValue.joinToString(" ")
        }


        this.arguments.forEach { arg ->
            if (remainingArgs.isEmpty())
                if (arg.required)
                // Missing a required flag
                    throw MissingArgumentException(arg)
                else
                    return@forEach

            if (arg.isFlag) {
                // Check if we've found the flag
                val r = ARG_RE.matchEntire(remainingArgs.peek())
                if (r != null) {
                    val groups = r.groupValues
                    val flag = if (groups[1] == "") groups[2] else groups[1]

                    if (!arg.match(flag)) {
                        // The flag we found wasn't the flag we were expecting.
                        if (arg.required)
                            throw IllegalArgumentException(
                                    "Unexpected flag \"$flag\". Expected \"${arg.key}\"")
                    } else {
                        remainingArgs.pop() // Drop the flag from the arg list
                        arguments[arg.dest] = processArg(arg)
                    }
                } else {
                    // The next arg wasn't a flag
                    if (arg.required)
                        throw IllegalStateException(
                                "Expected to find the flag ${arg.key}. Found ${remainingArgs.peek()} instead")
                }
            } else {
                // Not a flag
                if (remainingArgs.isEmpty() && arg.required)
                    throw MissingArgumentException(arg)
                val processArg = processArg(arg)
                if (processArg != "")
                    arguments[arg.dest] = processArg
            }
        }
        // Set default
        this.arguments.filter { !arguments.containsKey(it.dest) && it.default != null }.forEach {
            arguments[it.dest] = it.default!!
        }
        return arguments
    }

    /**
     * Parse the arguments
     *
     * @param args An array of arguments to parse
     * @return The parsed arguments
     *
     * @throws IllegalArgumentException
     * @throws MissingArgumentException
     */
    fun parse(args: String) = parse(
            SPACE_RE.findAll(args).map { it.value.replace(QUOTE_RE, "") }.toList().toTypedArray())

    /**
     * Gets a help string for the arguments
     *
     * @param prefix The prefix to append to the help if any
     * @return A help string
     */
    @JvmOverloads
    fun getHelp(prefix: String? = ""): String {
        return buildString {
            appendln(getShortHelp(prefix))
            appendln("Usage: ")
            val opts = mutableMapOf<String, String>()
            arguments.forEach {
                opts[it.key + " " + it.aliases.joinToString(", ")] = it.help ?: ""
            }
            val max = opts.keys.maxOf { it.length } + 5
            opts.forEach { (opt, help) ->
                append(" ")
                append(opt)
                append(" ".repeat(kotlin.math.max(0, max - opt.length)))
                appendln(help)
            }
        }
    }

    /**
     * Gets a shortened help string
     *
     * @param prefix The prefix to append to the help string if any
     * @return The help string
     */
    @JvmOverloads
    fun getShortHelp(prefix: String? = ""): String {
        return buildString {
            if(prefix != "") {
                append(prefix)
                append(" ")
            }
            arguments.forEach { arg ->
                if (!arg.required)
                    append("[")
                append(arg.key)
                if (arg.isFlag) {
                    append(" ")
                    append(arg.dest)
                }
                if (!arg.required)
                    append("]")
                append(" ")
            }
        }
    }
}

class MissingArgumentException(val option: Option) : Exception("Missing argument \"${option.key}\"")