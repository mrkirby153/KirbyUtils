package me.mrkirby153.kcutils.structure.adapter.data

import org.bukkit.material.Directional
import org.bukkit.material.MaterialData

object MaterialDataAdapter {


    private val adapters = mutableMapOf<Class<*>, DataAdapter<*>>()

    init {
        adapters.put(Directional::class.java, DirectionalData())
    }


    fun getAdapter(adapter: MaterialData): DataAdapter<*>? {
        if (adapters.containsKey(adapter.javaClass))
            return adapters[adapter.javaClass]
        else {
            if (adapter is Directional) {
                return DirectionalData()
            }
        }
        return null
    }
}