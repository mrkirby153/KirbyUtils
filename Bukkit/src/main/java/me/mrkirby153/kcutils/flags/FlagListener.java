package me.mrkirby153.kcutils.flags;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.ArrayList;
import java.util.HashSet;

public class FlagListener implements Listener {

    private static final ArrayList<EntityType> hostile = new ArrayList<>();
    private static final ArrayList<EntityType> peaceful = new ArrayList<>();

    static {
        // Hostile
        hostile.add(EntityType.BLAZE);
        hostile.add(EntityType.CREEPER);
        hostile.add(EntityType.ELDER_GUARDIAN);
        hostile.add(EntityType.ENDERMITE);
        hostile.add(EntityType.ENDERMAN);
        hostile.add(EntityType.EVOKER);
        hostile.add(EntityType.GHAST);
        hostile.add(EntityType.GUARDIAN);
        hostile.add(EntityType.HUSK);
        hostile.add(EntityType.MAGMA_CUBE);
        hostile.add(EntityType.SHULKER);
        hostile.add(EntityType.SILVERFISH);
        hostile.add(EntityType.SKELETON);
        hostile.add(EntityType.SLIME);
        hostile.add(EntityType.STRAY);
        hostile.add(EntityType.VEX);
        hostile.add(EntityType.VINDICATOR);
        hostile.add(EntityType.WITCH);
        hostile.add(EntityType.WITHER_SKELETON);
        hostile.add(EntityType.ZOMBIE);
        hostile.add(EntityType.ZOMBIE_VILLAGER);
        hostile.add(EntityType.PIG_ZOMBIE);
        hostile.add(EntityType.POLAR_BEAR);

        // Peaceful
        peaceful.add(EntityType.BAT);
        peaceful.add(EntityType.CHICKEN);
        peaceful.add(EntityType.COW);
        peaceful.add(EntityType.PIG);
        peaceful.add(EntityType.BAT);
        peaceful.add(EntityType.SHEEP);
        peaceful.add(EntityType.SKELETON_HORSE);
        peaceful.add(EntityType.SQUID);
        peaceful.add(EntityType.VILLAGER);
        peaceful.add(EntityType.DONKEY);
        peaceful.add(EntityType.LLAMA);
        peaceful.add(EntityType.OCELOT);
        peaceful.add(EntityType.WOLF);

    }

    private HashSet<String> authorizedChanges = new HashSet<>();
    private FlagModule module;

    public FlagListener(FlagModule module) {
        this.module = module;
    }

    /**
     * Authorize a world weather change
     *
     * @param world The world to change
     */
    public void authorizeChange(World world) {
        this.authorizedChanges.add(world.getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(module.shouldCancel(event.getBlock().getWorld(), WorldFlags.BLOCK_BREAK));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(module.shouldCancel(event.getBlock().getWorld(), WorldFlags.BLOCK_BURN));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        event.setCancelled(module.shouldCancel(event.getBlock().getWorld(), WorldFlags.BLOCK_DISPENSE));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(module.shouldCancel(event.getBlock().getWorld(), WorldFlags.EXPLOSIONS));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        event.setCancelled(module.shouldCancel(event.getBlock().getWorld(), WorldFlags.BLOCK_GROW));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        event.setCancelled(module.shouldCancel(event.getBlock().getWorld(), WorldFlags.BLOCK_PUSH));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        event.setCancelled(module.shouldCancel(event.getBlock().getWorld(), WorldFlags.BLOCK_PUSH));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(module.shouldCancel(event.getBlockPlaced().getWorld(), WorldFlags.BLOCK_PLACE));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (module.shouldCancel(event.getBlock().getWorld(), WorldFlags.BLOCK_REDSTONE))
            event.setNewCurrent(event.getOldCurrent());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() == EntityType.PLAYER) {
            if (event.getEntity().getType() == EntityType.PLAYER) {
                event.setCancelled(module.shouldCancel(event.getEntity().getWorld(), WorldFlags.PVE_ENABLE));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.setCancelled(module.shouldCancel(event.getEntity().getWorld(), WorldFlags.EXPLOSIONS));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        event.setCancelled(module.shouldCancel(event.getEntity().getWorld(), WorldFlags.HEALTH_REGEN));
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(module.shouldCancel(event.getEntity().getWorld(), WorldFlags.HUNGER_DEPLETE));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.JOCKEY) {
            event.setCancelled(module.shouldCancel(event.getEntity().getWorld(), WorldFlags.HOSTILE_SPAWN));
            return;
        }
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL)
            return;
        if (module.shouldCancel(event.getEntity().getWorld(), WorldFlags.PEACEFUL_SPAWN)) {
            event.setCancelled(peaceful.contains(event.getEntityType()));
        }
        if (module.shouldCancel(event.getEntity().getWorld(), WorldFlags.HOSTILE_SPAWN)) {
            event.setCancelled(hostile.contains(event.getEntityType()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(module.shouldCancel(event.getEntity().getWorld(), WorldFlags.FALL_DAMAGE));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        event.setCancelled(module.shouldCancel(event.getBlockClicked().getWorld(), WorldFlags.BUCKET_USE));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        event.setCancelled(module.shouldCancel(event.getBlockClicked().getWorld(), WorldFlags.BUCKET_USE));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(module.shouldCancel(event.getPlayer().getWorld(), WorldFlags.ITEM_DROP));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(module.shouldCancel(event.getClickedBlock().getWorld(), WorldFlags.BLOCK_INTERACT));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        event.setCancelled(module.shouldCancel(event.getPlayer().getWorld(), WorldFlags.ITEM_PICKUP));
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(module.shouldCancel(event.getBlock().getWorld(), WorldFlags.LEAF_DECAY));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if(event.getCause() == EntityDamageEvent.DamageCause.FALL)
            return;
        if(event.getDamager().getType() == EntityType.PLAYER){
            event.setCancelled(module.shouldCancel(event.getEntity().getWorld(), WorldFlags.PVP_ENABLE));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        event.setCancelled(module.shouldCancel(event.getEntity().getWorld(), WorldFlags.SHEEP_SHEARING));
    }

    @EventHandler(ignoreCancelled = true)
    public void onSheepRegrowWool(SheepRegrowWoolEvent event) {
        event.setCancelled(module.shouldCancel(event.getEntity().getWorld(), WorldFlags.SHEEP_REGROW_WOLL));
    }

    @EventHandler(ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (authorizedChanges.remove(event.getWorld().getName()))
            return;
        event.setCancelled(module.shouldCancel(event.getWorld(), WorldFlags.WEATHER_CHANGE));
    }

    @EventHandler(ignoreCancelled = true)
    public void playerDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL)
            return; // Fall damage is its own flag
        if (event.getEntity().getType()== EntityType.PLAYER) {
            event.setCancelled(module.shouldCancel(event.getEntity().getWorld(), WorldFlags.PVE_ENABLE));
        }
    }


}
