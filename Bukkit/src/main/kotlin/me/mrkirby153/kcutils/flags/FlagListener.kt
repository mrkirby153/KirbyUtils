package me.mrkirby153.kcutils.flags

import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.*
import org.bukkit.event.player.*
import org.bukkit.event.weather.WeatherChangeEvent
import java.util.*

class FlagListener(private val module: FlagModule) : Listener {

    private val authorizedChanges = HashSet<String>()

    /**
     * Authorize a world weather change
     *
     * @param world The world to change
     */
    fun authorizeChange(world: World) {
        this.authorizedChanges.add(world.name)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        event.isCancelled = module.shouldCancel(event.block.world, WorldFlags.BLOCK_BREAK)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBurn(event: BlockBurnEvent) {
        event.isCancelled = module.shouldCancel(event.block.world, WorldFlags.BLOCK_BURN)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockDispense(event: BlockDispenseEvent) {
        event.isCancelled = module.shouldCancel(event.block.world, WorldFlags.BLOCK_DISPENSE)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        event.isCancelled = module.shouldCancel(event.block.world, WorldFlags.EXPLOSIONS)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockGrow(event: BlockGrowEvent) {
        event.isCancelled = module.shouldCancel(event.block.world, WorldFlags.BLOCK_GROW)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockPistonExtend(event: BlockPistonExtendEvent) {
        event.isCancelled = module.shouldCancel(event.block.world, WorldFlags.BLOCK_PUSH)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockPistonRetract(event: BlockPistonRetractEvent) {
        event.isCancelled = module.shouldCancel(event.block.world, WorldFlags.BLOCK_PUSH)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        event.isCancelled = module.shouldCancel(event.blockPlaced.world, WorldFlags.BLOCK_PLACE)
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockRedstone(event: BlockRedstoneEvent) {
        if (module.shouldCancel(event.block.world, WorldFlags.BLOCK_REDSTONE))
            event.newCurrent = event.oldCurrent
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.FALL)
            return
        if (event.damager.type == EntityType.PLAYER) {
            event.isCancelled = module.shouldCancel(event.entity.world, WorldFlags.PVP_ENABLE)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.damager.type == EntityType.PLAYER) {
            if (event.entity.type == EntityType.PLAYER) {
                event.isCancelled = module.shouldCancel(event.entity.world, WorldFlags.PVP_ENABLE)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        event.isCancelled = module.shouldCancel(event.entity.world, WorldFlags.EXPLOSIONS)
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityRegainHealth(event: EntityRegainHealthEvent) {
        if (event.entityType == EntityType.PLAYER)
            if (event.regainReason == EntityRegainHealthEvent.RegainReason.SATIATED)
                event.isCancelled = module.shouldCancel(event.entity.world, WorldFlags.HEALTH_REGEN)
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntitySpawn(event: CreatureSpawnEvent) {
        if (event.spawnReason == CreatureSpawnEvent.SpawnReason.JOCKEY) {
            event.isCancelled = module.shouldCancel(event.entity.world, WorldFlags.HOSTILE_SPAWN)
            return
        }
        if (event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL)
            return
        if (module.shouldCancel(event.entity.world, WorldFlags.PEACEFUL_SPAWN) && peaceful.contains(event.entityType)) {
            event.isCancelled = peaceful.contains(event.entityType)
        }
        if (module.shouldCancel(event.entity.world, WorldFlags.HOSTILE_SPAWN) && hostile.contains(event.entityType)) {
            event.isCancelled = hostile.contains(event.entityType)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onFallDamage(event: EntityDamageEvent) {
        if (event.entity.type == EntityType.PLAYER && event.cause == EntityDamageEvent.DamageCause.FALL) {
            event.isCancelled = module.shouldCancel(event.entity.world, WorldFlags.FALL_DAMAGE)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        event.isCancelled = module.shouldCancel(event.entity.world, WorldFlags.HUNGER_DEPLETE)
    }

    @EventHandler(ignoreCancelled = true)
    fun onLeavesDecay(event: LeavesDecayEvent) {
        event.isCancelled = module.shouldCancel(event.block.world, WorldFlags.LEAF_DECAY)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerBucketEmpty(event: PlayerBucketEmptyEvent) {
        event.isCancelled = module.shouldCancel(event.blockClicked.world, WorldFlags.BUCKET_USE)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerBucketFill(event: PlayerBucketFillEvent) {
        event.isCancelled = module.shouldCancel(event.blockClicked.world, WorldFlags.BUCKET_USE)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        event.isCancelled = module.shouldCancel(event.player.world, WorldFlags.ITEM_DROP)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            event.isCancelled = module.shouldCancel(event.clickedBlock.world, WorldFlags.BLOCK_INTERACT)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerPickupItem(event: PlayerPickupItemEvent) {
        event.isCancelled = module.shouldCancel(event.player.world, WorldFlags.ITEM_PICKUP)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerShearEntity(event: PlayerShearEntityEvent) {
        event.isCancelled = module.shouldCancel(event.entity.world, WorldFlags.SHEEP_SHEARING)
    }

    @EventHandler(ignoreCancelled = true)
    fun onSheepRegrowWool(event: SheepRegrowWoolEvent) {
        event.isCancelled = module.shouldCancel(event.entity.world, WorldFlags.SHEEP_REGROW_WOLL)
    }

    @EventHandler(ignoreCancelled = true)
    fun onWeatherChange(event: WeatherChangeEvent) {
        if (authorizedChanges.remove(event.world.name))
            return
        event.isCancelled = module.shouldCancel(event.world, WorldFlags.WEATHER_CHANGE)
    }

    @EventHandler(ignoreCancelled = true)
    fun playerDamage(event: EntityDamageEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.FALL || event.cause == EntityDamageEvent.DamageCause.VOID)
            return  // Fall damage is its own flag
        if (event.entity.type == EntityType.PLAYER) {
            event.isCancelled = module.shouldCancel(event.entity.world, WorldFlags.PVE_ENABLE)
        }
    }

    companion object {

        private val hostile = ArrayList<EntityType>()
        private val peaceful = ArrayList<EntityType>()

        init {
            // Hostile
            hostile.add(EntityType.BLAZE)
            hostile.add(EntityType.CREEPER)
            hostile.add(EntityType.ELDER_GUARDIAN)
            hostile.add(EntityType.ENDERMITE)
            hostile.add(EntityType.ENDERMAN)
            hostile.add(EntityType.EVOKER)
            hostile.add(EntityType.GHAST)
            hostile.add(EntityType.GUARDIAN)
            hostile.add(EntityType.HUSK)
            hostile.add(EntityType.MAGMA_CUBE)
            hostile.add(EntityType.SHULKER)
            hostile.add(EntityType.SILVERFISH)
            hostile.add(EntityType.SKELETON)
            hostile.add(EntityType.SLIME)
            hostile.add(EntityType.STRAY)
            hostile.add(EntityType.VEX)
            hostile.add(EntityType.VINDICATOR)
            hostile.add(EntityType.WITCH)
            hostile.add(EntityType.WITHER_SKELETON)
            hostile.add(EntityType.ZOMBIE)
            hostile.add(EntityType.ZOMBIE_VILLAGER)
            hostile.add(EntityType.PIG_ZOMBIE)
            hostile.add(EntityType.POLAR_BEAR)

            // Peaceful
            peaceful.add(EntityType.BAT)
            peaceful.add(EntityType.CHICKEN)
            peaceful.add(EntityType.COW)
            peaceful.add(EntityType.PIG)
            peaceful.add(EntityType.BAT)
            peaceful.add(EntityType.SHEEP)
            peaceful.add(EntityType.SKELETON_HORSE)
            peaceful.add(EntityType.SQUID)
            peaceful.add(EntityType.VILLAGER)
            peaceful.add(EntityType.DONKEY)
            peaceful.add(EntityType.LLAMA)
            peaceful.add(EntityType.OCELOT)
            peaceful.add(EntityType.WOLF)

        }
    }


}
