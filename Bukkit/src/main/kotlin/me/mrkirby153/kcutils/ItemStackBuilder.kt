package me.mrkirby153.kcutils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
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
    private var name: Component? = null

    /**
     * The Item's lore
     */
    private val lore = mutableListOf<Component>()

    /**
     * Any flags the item has
     */
    private val flags = mutableListOf<ItemFlag>()

    /**
     * All enchantments that the item has
     */
    private val enchantments = mutableMapOf<Enchantment, Int>()

    /**
     * Construct the [ItemStack]
     *
     * @return The built ItemStack
     */
    fun build(): ItemStack {
        val itemFactory = ItemFactory(this.material).damage(this.data).amount(this.amount)

        if (this.unbreakable)
            itemFactory.unbreakable()

        if (this.name != null)
            itemFactory.name(this.name!!)

        lore.forEach { itemFactory.lore(it) }
        flags.forEach { itemFactory.flags(it) }

        enchantments.forEach { ench, level -> itemFactory.enchantment(ench, level) }
        return itemFactory.construct()
    }

    /**
     * Sets the name of ths [ItemStack] to the provided [name]
     */
    fun name(name: String) {
        name(
            Component.text(name).decoration(TextDecoration.ITALIC, false)
                .asComponent()
        )
    }

    /**
     * Sets the name of this [ItemStack] to the provided [component]
     */
    fun name(component: Component) {
        this.name = component
    }

    /**
     * Adds the given [lore] to the [ItemStack]'s lore
     */
    fun lore(lore: String) {
        lore(Component.text(lore).decoration(TextDecoration.ITALIC, false))
    }

    /**
     * Adds the given [component] to the [ItemStack]'s lore
     */
    fun lore(component: Component) {
        this.lore.add(component)
    }

    /**
     * Adds the given [flags] to this [ItemStack]
     */
    fun flags(vararg flags: ItemFlag) {
        this.flags.addAll(flags)
    }

    /**
     * Adds the provided [enchantment] at the given [level] to this [ItemStack]
     */
    fun enchant(enchantment: Enchantment, level: Int) {
        this.enchantments[enchantment] = level
    }

}