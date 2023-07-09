package me.mrkirby153.kcutils.extensions

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

inline fun <reified T : ItemMeta> itemStack(
    material: Material,
    amount: Int = 1,
    meta: T.() -> Unit = {}
) = ItemStack(material, amount).meta<T>(meta)

@JvmName("itemStackGenericMeta")
inline fun itemStack(material: Material, amount: Int = 1, meta: ItemMeta.() -> Unit = {}) =
    itemStack<ItemMeta>(material, amount, meta)

/**
 * Sets an items [ItemMeta]
 */
inline fun <reified T : ItemMeta> ItemStack.meta(block: T.() -> Unit) = apply {
    check(itemMeta is T) { "Cannot set item meta of type ${T::class.java} as it is of type ${itemMeta::class.java}" }
    itemMeta = (itemMeta as T).apply(block)
}

/**
 * Adds the provided component as lore
 */
fun ItemMeta.addLore(component: Component) {
    val existingLore = lore() ?: mutableListOf()
    existingLore.add(component)
    lore(existingLore)
}

/**
 * The item's display name
 */
inline var ItemStack.displayName: Component?
    get() = this.itemMeta?.displayName()
    set(value) {
        this.itemMeta?.displayName(value)
    }

/**
 * Any [ItemFlag]s on this [ItemMeta]
 */
inline val ItemMeta.flags: ItemFlags
    get() = ItemFlags(this)

/**
 * Any [ItemFlag]s on this [ItemStack]
 */
inline val ItemStack.flags: ItemFlags?
    get() = this.itemMeta?.flags

inline var ItemMeta.glowing: Boolean
    get() {
        return this.hasEnchant(Enchantment.PROTECTION_ENVIRONMENTAL)
    }
    set(value) {
        if (value) {
            check(this.enchants.isEmpty()) { "Attempting to mark a previously enchanted item as glowing" }
            this.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true)
            flags[ItemFlag.HIDE_ENCHANTS] = true
        } else {
            this.removeEnchant(Enchantment.PROTECTION_ENVIRONMENTAL)
        }
    }

/**
 * Backing class for manipulating [ItemFlag]
 */
class ItemFlags(private val meta: ItemMeta) {

    operator fun get(key: ItemFlag): Boolean {
        return key in meta.itemFlags
    }

    operator fun set(key: ItemFlag, value: Boolean) {
        if (value) {
            meta.addItemFlags(key)
        } else {
            meta.removeItemFlags(key)
        }
    }

    fun all(): Set<ItemFlag> = ItemFlag.values().filter { this[it] }.toSet()
}