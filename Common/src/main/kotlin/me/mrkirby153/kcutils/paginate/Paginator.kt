package me.mrkirby153.kcutils.paginate

/**
 * A paginator utility. Used to break a large list into multiple sub-lists
 *
 * @param T The type of the paginator
 */
class Paginator<out T>(private val items: List<T>, private val itemsPerPage: Int) {

    /**
     * Gets the amount of pages in the paginator
     *
     * @return The maximum number of pages
     */
    val maxPages: Int
        get() = Math.ceil(this.items.size.toDouble() / this.itemsPerPage.toDouble()).toInt()

    /**
     * Gets a list of items in a page of the paginator
     *
     * @param page The page
     * @return A list of items
     */
    fun getPage(page: Int): List<T> {
        val startIndex = this.itemsPerPage * (page - 1)
        val endIndex = Math.min(this.itemsPerPage * page, items.size)
        return this.items.subList(startIndex, endIndex)
    }
}
