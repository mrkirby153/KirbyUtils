package me.mrkirby153.kcutils.structure.adapter.data

import org.bukkit.material.Directional
import org.bukkit.material.MaterialData

object MaterialDataAdapter {


    private val adapters = mutableMapOf<Class<*>, DataAdapter<*>>()

    init {
        adapters[Directional::class.java] = DirectionalData()
    }


    /**
     * Gets the [DataAdapter] for a given [MaterialData].
     *
     * If the material data is not explicitly overridden and the material is directional, [DirectionalData]
     * is returned by default.
     *
     * @param material The material data to lookup
     * @return The [DataAdapter] for the material, or the default material data
     */
    @JvmStatic
    fun getAdapter(material: MaterialData): DataAdapter<*>? {
        if (adapters.containsKey(material.javaClass))
            return adapters[material.javaClass]
        else {
            if (material is Directional) {
                return DirectionalData()
            }
        }
        return DefaultMaterialData()
    }

    /**
     * Registers a [DataAdapter]
     *
     * @param clazz The class of the object to register the data adapter for
     * @param data The data adapter to associate with the object
     */
    @JvmStatic
    fun registerAdapter(clazz: Class<*>, data: DataAdapter<*>){
        this.adapters[clazz] = data
    }
}