package me.mrkirby153.kcutils.utils

/**
 * Interface for storing key-value paired data in various formats
 */
interface DataStore<in K, V> {

    /**
     * Retrieve a value from the data store
     *
     * @param key The key of the value to retrieve
     * @return  The value in the data store or null
     */
    operator fun get(key: K): V?

    /**
     * Puts a value in the data store
     *
     * @param key The key to store the data in
     * @param value The data to store
     *
     * @return The previous value or null
     */
    operator fun set(key: K, value: V): V?

    /**
     * Checks if the data store contains a key
     *
     * @param key The key to check
     *
     * @return True if the key exists
     */
    fun containsKey(key: K): Boolean

    /**
     * Checks if the data store contains a value
     *
     * @param value The value
     *
     * @return True if the value exists in the store
     */
    fun containsValue(value: V): Boolean

    /**
     * Clears the data store
     */
    fun clear()

    /**
     * Returns the size of the data store
     *
     * @return The size of the data store
     */
    fun size(): Int

    /**
     * Removes the specified key from the data store
     *
     * @param key The key whose value is retrieved from the data store
     *
     * @return The value associated with the key, or null if it doesn't exist
     */
    fun remove(key: K): V?
}