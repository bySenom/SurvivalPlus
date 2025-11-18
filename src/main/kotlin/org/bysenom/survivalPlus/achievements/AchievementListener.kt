package org.bysenom.survivalPlus.achievements

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerItemHeldEvent

/**
 * Tracked Achievement-relevante Events
 */
class AchievementListener(private val plugin: SurvivalPlus) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onItemReceived(event: PlayerItemHeldEvent) {
        val player = event.player
        val item = player.inventory.getItem(event.newSlot) ?: return
        
        // Prüfe ob Custom Item
        if (plugin.itemManager.getQuality(item) == null) return
        
        // Triggere Achievement-Check
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            plugin.achievementManager.checkAchievements(player)
        }, 1L)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onSpecialMobKill(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        
        // Prüfe ob Special Mob
        if (plugin.specialMobManager.isSpecialMob(event.entity)) {
            plugin.achievementManager.incrementProgress(killer.uniqueId, "special_mob_kills")
            plugin.achievementManager.checkAchievements(killer)
        }
        
        // Prüfe ob Butcher
        if (event.entity.customName()?.toString()?.contains("Butcher") == true) {
            plugin.achievementManager.incrementProgress(killer.uniqueId, "butcher_kills")
            plugin.achievementManager.checkAchievements(killer)
        }
    }
}
