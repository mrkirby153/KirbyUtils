package me.mrkirby153.kcutils.utils

/**
 * Utility for building out ASCII tables
 *
 * @param columns The column names of the table
 */
class TableBuilder(val columns: Array<String>) {

    private val rows = mutableListOf<Array<String?>>()

    /**
     * Builds and returns the header of the table
     *
     * @return The table's header
     */
    fun buildHeader(): String {
        return buildString {
            append("|")
            columns.forEachIndexed { index, col ->
                append(" ")
                // Find the max length
                val maxLength = getColSize(index)
                val remaining = Math.max(maxLength - col.length, 0)
                append(col)
                append(" ".repeat(remaining))
                append(" |")
            }
            val length = this.length
            append("\n")
            append("-".repeat(length))
        }
    }

    /**
     * Builds and returns the table's data
     *
     * @return The table's data
     */
    fun buildData(): String {
        return buildString {
            rows.forEach { row ->
                append("|")
                row.forEachIndexed { index, data ->
                    if(index >= columns.size)
                        return@forEachIndexed
                    append(" ")
                    val d = data ?: "NULL"
                    val remaining = Math.max(getColSize(index) - d.length, 0)
                    append(d)
                    append(" ".repeat(remaining))
                    append(" |")
                }
                if(row.size < columns.size){
                    var next = row.size
                    while(next < columns.size){
                        append(" ")
                        append(" ".repeat(getColSize(next)))
                        append(" |")
                        next++
                    }
                }
                append("\n")
            }
        }
    }

    /**
     * Builds and returns the table
     *
     * @return The table
     */
    fun buildTable(): String {
        return buildHeader() + "\n" + buildData()
    }

    private fun getColSize(index: Int): Int {
        var maxLength = columns[index].length
        rows.forEach { row ->
            if (index < row.size) {
                val data = row[index] ?: "NULL"
                if (maxLength < data.length)
                    maxLength = data.length
            }
        }
        return maxLength
    }

    /**
     * Adds a row to the table
     *
     * @param data The row to add
     */
    fun addRow(data: Array<String?>) {
        this.rows.add(data)
    }
}