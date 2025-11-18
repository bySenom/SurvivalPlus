package org.bysenom.survivalPlus.listeners

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerPickupArrowEvent

class ItemDropListener(private val plugin: SurvivalPlus) : Listener {

    /**
     * Wenn ein Item gespawnt wird, erstelle ein Quality Plate
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onItemSpawn(event: ItemSpawnEvent) {
        if (event.isCancelled) return

        val item = event.entity
        // Welt-Whitelist
        if (!plugin.worldTierManager.isEnabledWorld(item.world)) return

        // Kleiner Delay damit das Item sich stabilisiert
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (item.isValid && !item.isDead) {
                plugin.qualityPlateManager.createPlateForItem(item)
            }
        }, 5L) // 0.25 Sekunden Delay
    }

    /**
     * Wenn ein Item despawnt, entferne das Hologramm
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onItemDespawn(event: ItemDespawnEvent) {
        // In nicht aktivierten Welten gibt es keine Hologramme, trotzdem sicher entfernen
        plugin.qualityPlateManager.removeHologram(event.entity.uniqueId)
    }

    /**
     * Wenn ein Item aufgehoben wird, entferne das Hologramm
     * Hinweis: PlayerAttemptPickupItemEvent wäre besser, aber funktioniert mit allen Versionen
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onItemPickup(event: org.bukkit.event.entity.EntityPickupItemEvent) {
        // In nicht aktivierten Welten gibt es keine Hologramme, trotzdem sicher entfernen
        plugin.qualityPlateManager.removeHologram(event.item.uniqueId)
    }
}
