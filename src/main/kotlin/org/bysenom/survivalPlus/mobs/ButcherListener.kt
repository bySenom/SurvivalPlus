package org.bysenom.survivalPlus.mobs

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent

/**
 * Event-Listener für den Butcher Boss
 */
class ButcherListener(private val plugin: SurvivalPlus) : Listener {

    private val butcherBoss = plugin.butcherBoss

    /**
     * Versucht Butcher beim Mob-Spawn zu spawnen
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onMobSpawn(event: CreatureSpawnEvent) {
        // Nur Zombies
        if (event.entity !is Zombie) return

        // Nur natürliche Spawns
        if (event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL) return

        val location = event.location

        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(location.world)) return

        // Prüfe World Tier
        val worldTier = plugin.worldTierManager.getWorldTier(location.world)
        if (worldTier.tier < ButcherBoss.MIN_WORLD_TIER) return

        // Versuche Butcher zu spawnen
        val butcher = butcherBoss.trySpawnButcher(location)

        if (butcher != null) {
            // Ersetze den normalen Zombie durch den Butcher
            event.isCancelled = true
        }
    }

    /**
     * Butcher Angriffs-Effekte
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onButcherAttack(event: EntityDamageByEntityEvent) {
        val damager = event.damager

        // Nur Butcher
        if (!butcherBoss.isButcher(damager)) return

        val victim = event.entity as? Player ?: return

        // Bleed-Effekt
        butcherBoss.applyBleed(victim)

        // Extra Knockback
        victim.velocity = victim.velocity.multiply(1.5).setY(0.3)
    }

    /**
     * Butcher Tod
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onButcherDeath(event: EntityDeathEvent) {
        val entity = event.entity

        // Nur Butcher
        if (!butcherBoss.isButcher(entity)) return

        val butcher = entity as Zombie
        val killer = butcher.killer

        // Lösche normale Drops
        event.drops.clear()
        event.droppedExp = 0

        // Custom Drops und Effekte
        butcherBoss.onButcherDeath(butcher, killer)

        // Massive XP
        event.droppedExp = 500 + (plugin.worldTierManager.getWorldTier(butcher.world).tier * 200)
    }
}

