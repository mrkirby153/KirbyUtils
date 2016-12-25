package me.mrkirby153.kcutils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class for creating items
 */
public class ItemFactory {

    public static final Enchantment ENCHANTMENT_GLOWING = new GlowEnchantment();

    private Material material;
    private short data;
    private String name;
    private int amount;
    private boolean unbreakable;
    private List<String> lore;
    private List<ItemFlag> flags;
    private HashMap<Enchantment, Integer> enchantments;


    public ItemFactory(Material material) {
        this.material = material;
        this.data = 0;
        this.amount = 1;
        this.unbreakable = false;
        this.lore = new ArrayList<>();
        this.flags = new ArrayList<>();
        this.enchantments = new HashMap<>();
    }

    /**
     * Sets the item amount
     *
     * @param amount The item amount
     * @return The factory
     */
    public ItemFactory amount(int amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Turns the item factory into an Item Stack
     *
     * @return An {@link ItemStack}
     */
    public ItemStack construct() {
        ItemStack stack = new ItemStack(material, amount, data);
        ItemMeta meta = stack.getItemMeta();
        if (name != null)
            meta.setDisplayName(ChatColor.RESET + name);
        meta.setLore(lore);
        meta.setUnbreakable(unbreakable);
        meta.addItemFlags(flags.toArray(new ItemFlag[0]));
        enchantments.forEach((e, l) -> meta.addEnchant(e, l, true));
        stack.setItemMeta(meta);

        return stack;
    }

    /**
     * Sets the item's data
     *
     * @param data The data
     * @return The factory
     */
    public ItemFactory data(int data) {
        this.data = (short) data;
        return this;
    }

    /**
     * Adds an item's enchantment
     *
     * @param enchantment The enchantment
     * @param level       The level
     * @return The factory
     */
    public ItemFactory enchantment(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    /**
     * Adds flags to an item
     *
     * @param flags The flags
     * @return The factory
     */
    public ItemFactory flags(ItemFlag... flags) {
        Collections.addAll(this.flags, flags);
        return this;
    }

    /**
     * Makes the item glow
     *
     * @return The factory
     */
    public ItemFactory glowing() {
        this.enchantment(ENCHANTMENT_GLOWING, 1);
        return this;
    }

    /**
     * Adds lore to an item
     *
     * @param lore The lore
     * @return The factory
     */
    public ItemFactory lore(String... lore) {
        for (String l : lore) {
            this.lore.add(ChatColor.RESET + l);
        }
        return this;
    }

    /**
     * Sets the item's name
     *
     * @param name The name
     * @return The factory
     */
    public ItemFactory name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Makes the item unbreakable
     *
     * @return The factory
     */
    public ItemFactory unbreakable() {
        this.unbreakable = true;
        return this;
    }

    public static class GlowEnchantment extends EnchantmentWrapper {

        public GlowEnchantment() {
            super(120);
            register();
        }

        @Override
        public boolean canEnchantItem(ItemStack item) {
            return true;
        }

        @Override
        public boolean conflictsWith(Enchantment other) {
            return false;
        }

        @Override
        public EnchantmentTarget getItemTarget() {
            return null;
        }

        @Override
        public int getMaxLevel() {
            return 1;
        }

        @Override
        public String getName() {
            return "NullEnchantment";
        }

        @Override
        public int getStartLevel() {
            return 1;
        }

        private void register() {
            try {
                // Register enchantment
                Field f = Enchantment.class.getDeclaredField("acceptingNew");
                f.setAccessible(true);
                f.setAccessible(true);
                boolean previous = f.getBoolean(null);
                f.set(null, true);
                if (Enchantment.getByName("NullEnchantment") == null)
                    Enchantment.registerEnchantment(this);

                f.setBoolean(null, previous);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }
}
