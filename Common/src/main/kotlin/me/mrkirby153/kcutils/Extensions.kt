package me.mrkirby153.kcutils

import java.io.File
import java.util.Properties

/**
 * Gets the child of a folder
 *
 * @param path The child folder's path
 *
 * @return A child file of the folder
 */
fun File.child(path: String) = File(this, path)

/**
 * Read a file as a [Properties] file
 *
 * @return A [Properties] file with the content of the file read
 */
fun File.readProperties(): Properties {
    return Properties().apply { load(this@readProperties.inputStream()) }
}

/**
 * Creates a blank file *only* if it doesn't exist
 *
 * @return The current file
 */
fun File.createFileIfNotExist(): File {
    if (!this.exists())
        this.createNewFile()
    return this
}

/**
 * Creates a directory *only* if it doesn't exist
 *
 * @return The current file
 */
fun File.mkdirIfNotExist(): File {
    if (!this.exists())
        this.mkdir()
    return this
}