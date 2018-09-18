package me.mrkirby153.kcutils.reflections

import java.lang.reflect.Field

/**
 * A wrapper class for easily reflecting a class
 */
class WrappedReflectedClass(val clazz: Class<*>) {

    /**
     * A list of fields in the class, grouped by their type
     */
    private val fields = mutableMapOf<Class<*>, MutableList<Field>>()

    /**
     * A list of values that each field will be set to on construction
     */
    private val fieldValues = mutableMapOf<Field, Any>()

    init {
        parseFields()
    }

    /**
     * Parses the fields, grouping them by type
     */
    private fun parseFields() {
        this.clazz.declaredFields.forEach { f ->
            var list = fields[f.type]
            if (list == null) {
                list = mutableListOf()
                fields[f.type] = list
            }
            list.add(f)
        }
    }

    /**
     * Sets a field
     *
     * @param clazz The type of field
     * @param index The index in the class
     * @param value The value of the field
     */
    fun set(clazz: Class<*>, index: Int, value: Any) {
        val field = fields[Reflections.mapToPrimitive(clazz)]?.get(index) ?: throw IllegalArgumentException("Not found!")
        fieldValues[field] = value
    }

    /**
     * Sets a field with a NMS Class
     *
     * @param clazz The NMS class name
     * @param index The index in the class
     * @param value The value of the field
     */
    fun set(clazz: String, index: Int, value: Any) {
        set(Reflections.getNMSClass(clazz), index, value)
    }

    /**
     * Sets a field by its name
     *
     * @param name The name of the field
     * @param value The value of the field
     */
    fun setByName(name: String, value: Any) {
        val field = this.clazz.declaredFields.firstOrNull { it.name == name }
                ?: throw IllegalArgumentException("No field with the name $name found")
        fieldValues[field] = value
    }

    /**
     * Constructs an instance of the class
     */
    fun get(): Any {
        val instance = clazz.newInstance()
        fieldValues.forEach { field, value ->
            field.isAccessible = true
            field.set(instance, value)
        }
        return instance
    }
}