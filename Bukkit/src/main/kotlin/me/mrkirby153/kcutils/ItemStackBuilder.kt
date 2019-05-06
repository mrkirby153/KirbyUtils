package me.mrkirby153.kcutils

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * A utility for quickly building an [ItemStack]
 *
 * @param material  The material the item is to be made of
 * @param data      The data value of the item
 */
class ItemStackBuilder(var material: Material, var data: Int = 0) {

    /**
     * The amount of items to include in the stack
     */
    var amount: Int = 1

    /**
     * If the item is unbreakable
     */
    var unbreakable = false

    /**
     * The Item's custom name
     */
    var name: String? = null

    /**
     * The Item's lore
     */
    var lore = mutableListOf<String>()

    /**
     * Any flags the item has
     */
    var flags = mutableListOf<ItemFlag>()

    /**
     * All enchantments that the item has
     */
    var enchantments = mutableMapOf<Enchantment, Int>()

    /**
     * Construct the [ItemStack]
     *
     * @return The built ItemStack
     */
    fun build(): ItemStack {
        val itemFactory = ItemFactory(this.material).damage(this.data).amount(this.amount)

        if (this.unbreakable)
            itemFactory.unbreakable()

        if (!this.name.isNullOrEmpty())
            itemFactory.name(this.name!!)

        lore.forEach { itemFactory.lore(it) }
        flags.forEach { itemFactory.flags(it) }

        enchantments.forEach { ench, level -> itemFactory.enchantment(ench, level) }
        return itemFactory.construct()
    }

}