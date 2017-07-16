package me.mrkirby153.kcutils

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class ItemStackBuilder(material: Material, data: Int = 0) {

    var material = material
    var data = data
    var amount: Int = 1

    var unbreakable = false
    var name: String? = null

    var lore = mutableListOf<String>()
    var flags = mutableListOf<ItemFlag>()

    var enchantments = mutableMapOf<Enchantment, Int>()

    fun build(): ItemStack {
        val itemFactory = ItemFactory(this.material).data(this.data).amount(this.amount)

        if(this.unbreakable)
            itemFactory.unbreakable()

        if(!this.name.isNullOrEmpty())
            itemFactory.name(this.name)

        lore.forEach { itemFactory.lore(it) }
        flags.forEach { itemFactory.flags(it) }

        enchantments.forEach { ench, level -> itemFactory.enchantment(ench, level)}
        return itemFactory.construct()
    }

}