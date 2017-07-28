package me.mrkirby153.kcutils.paginate;

import java.util.List;

/**
 * A paginator utility. Used to break a large list into multiple sub-lists
 *
 * @param <T>
 */
public class Paginator<T> {

    private final List<T> items;

    private final int itemsPerPage;

    public Paginator(List<T> items, int itemsPerPage) {
        this.items = items;
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * Gets the amount of pages in the paginator
     *
     * @return The maximum number of pages
     */
    public int getMaxPages() {
        return (int) Math.ceil((double) this.items.size() / (double) this.itemsPerPage);
    }

    /**
     * Gets a list of items in a page of the paginator
     *
     * @param page The page
     * @return A list of items
     */
    public List<T> getPage(int page) {
        int startIndex = this.itemsPerPage * (page - 1);
        int endIndex = Math.min(this.itemsPerPage * page, items.size());
        return this.items.subList(startIndex, endIndex);
    }
}
