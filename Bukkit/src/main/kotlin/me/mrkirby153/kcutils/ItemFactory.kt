package me.mrkirby153.kcutils

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentWrapper
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

/**
 * Utility class for creating items
 */
class ItemFactory(private val material: Material) {
    private var damage: Int = 0
    private var name: String? = null
    private var amount: Int = 1
    private var unbreakable: Boolean = false
    private val lore: MutableList<String> = mutableListOf()
    private val flags: MutableList<ItemFlag> = mutableListOf()
    private val enchantments: MutableMap<Enchantment, Int> = mutableMapOf()

    /**
     * Sets the item amount
     *
     * @param amount The item amount
     *
     * @return The factory
     */
    fun amount(amount: Int): ItemFactory {
        this.amount = amount
        return this
    }

    /**
     * Turns the item factory into an Item Stack
     *
     * @return An [ItemStack]
     */
    fun construct(): ItemStack {
        val stack = ItemStack(material, amount)
        val meta = stack.itemMeta!!
        if (name != null) {
            meta.setDisplayName(ChatColor.RESET.toString() + name!!)
        }
        if(meta is Damageable) {
            meta.damage = this.damage
        }
        meta.lore = lore
        meta.isUnbreakable = unbreakable
        meta.addItemFlags(*flags.toTypedArray())
        enchantments.forEach { e, l -> meta.addEnchant(e, l, true) }
        stack.itemMeta = meta

        return stack
    }

    /**
     * Sets the item's damage
     *
     * @param damage The damage
     *
     * @return The factory
     */
    fun damage(damage: Int): ItemFactory {
        this.damage = damage
        return this
    }

    /**
     * Adds an item's enchantment
     *
     * @param enchantment The enchantment
     * @param level       The level
     *
     * @return The factory
     */
    fun enchantment(enchantment: Enchantment, level: Int): ItemFactory {
        this.enchantments.put(enchantment, level)
        return this
    }

    /**
     * Adds flags to an item
     *
     * @param flags The flags
     *
     * @return The factory
     */
    fun flags(vararg flags: ItemFlag): ItemFactory {
        this.flags.addAll(flags)
        return this
    }

    /**
     * Makes the item glow
     *
     * @return The factory
     */
    fun glowing(): ItemFactory {
        this.enchantment(ENCHANTMENT_GLOWING, 1)
        return this
    }

    /**
     * Adds lore to an item
     *
     * @param lore The lore
     *
     * @return The factory
     */
    fun lore(vararg lore: String): ItemFactory {
        for (l in lore) {
            this.lore.add(ChatColor.RESET.toString() + l)
        }
        return this
    }

    /**
     * Sets the item's name
     *
     * @param name The name
     *
     * @return The factory
     */
    fun name(name: String): ItemFactory {
        this.name = name
        return this
    }

    /**
     * Makes the item unbreakable
     *
     * @return The factory
     */
    fun unbreakable(): ItemFactory {
        this.unbreakable = true
        return this
    }

    /**
     * An enchantment for making an item glow
     */
    class GlowEnchantment : EnchantmentWrapper("glow") {
        init {
            register()
        }

        override fun canEnchantItem(item: ItemStack): Boolean {
            return true
        }

        override fun conflictsWith(other: Enchantment): Boolean {
            return false
        }

        override fun getMaxLevel(): Int {
            return 1
        }

        override fun getName(): String {
            return "NullEnchantment"
        }

        override fun getStartLevel(): Int {
            return 1
        }

        private fun register() {
            try {
                // Register enchantment
                val f = Enchantment::class.java.getDeclaredField("acceptingNew")
                f.isAccessible = true
                f.isAccessible = true
                val previous = f.getBoolean(null)
                f.set(null, true)
                if (Enchantment.getByKey(NamespacedKey.minecraft("glow")) == null)
                    Enchantment.registerEnchantment(this)

                f.setBoolean(null, previous)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            }

        }
    }

    companion object {

        /**
         * The glowing enchantment -- An enchantment with no effects whose sole purpose is to make the item glow
         */
        val ENCHANTMENT_GLOWING: Enchantment = GlowEnchantment()
    }
}
