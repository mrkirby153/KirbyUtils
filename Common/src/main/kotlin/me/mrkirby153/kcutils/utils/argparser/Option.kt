package me.mrkirby153.kcutils.utils.argparser

/**
 * An option for the argument parser
 */
class Option(val key: String, val required: Boolean = true,
                  val aliases: Array<String> = emptyArray(), val help: String? = null,
                  val nargs: String = "*", val dest: String = key.replace("-", ""),
                  val default: String? = null) {

    val isFlag = key.startsWith("-")

    init {
        if (!isFlag && aliases.isNotEmpty())
            throw IllegalArgumentException("Aliases can only be used with flags")
    }

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

    fun match(flag: String): Boolean {
        return key.replace("-", "") == flag || aliases.map { it.replace("-", "") }.contains(flag)
    }
}

/*
    NARGS:
    n = number of arguments (throws exception if not present)
    null = Arguments gathered as a string until next flag
    * = Rest of the arguments as a list (until next flag or eos)
    + = Rest of the arguments (1 required) as a list
    ~ = All remaining command line arguments
 */