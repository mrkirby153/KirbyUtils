package me.mrkirby153.kcutils.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class UpdateEvent(val type: UpdateType) : Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
       @JvmStatic val handlerList = HandlerList()
    }
}
