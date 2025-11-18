package org.bysenom.survivalPlus.structures

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.world.WorldLoadEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

/**
 * Listener für Shrine Protection und World Generation
 */
class ShrineListener(private val plugin: SurvivalPlus) : Listener {

    /**
     * Wenn eine Welt geladen wird, generiere Shrines
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onWorldLoad(event: WorldLoadEvent) {
        val world = event.world

        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(world)) {
            return
        }

        // Verzögerte Generierung (damit Welt vollständig geladen ist)
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            plugin.shrineManager.generateShinesForWorld(world)
        }, 100L) // 5 Sekunden Delay
    }

    /**
     * Verhindert Block-Abbau in Shrine-Bereichen
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockBreak(event: BlockBreakEvent) {
        val location = event.block.location

        if (plugin.shrineManager.isShrineLocation(location)) {
            event.isCancelled = true
            event.player.sendMessage(
                Component.text("✘ Dieser Shrine ist geschützt!")
                    .color(NamedTextColor.RED)
            )
            event.player.playSound(
                event.player.location,
                org.bukkit.Sound.ENTITY_VILLAGER_NO,
                1f,
                1f
            )
        }
    }

    /**
     * Verhindert Block-Platzierung in Shrine-Bereichen
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val location = event.block.location

        if (plugin.shrineManager.isShrineLocation(location)) {
            event.isCancelled = true
            event.player.sendMessage(
                Component.text("✘ Du kannst hier nicht bauen!")
                    .color(NamedTextColor.RED)
            )
            event.player.playSound(
                event.player.location,
                org.bukkit.Sound.ENTITY_VILLAGER_NO,
                1f,
                1f
            )
        }
    }

    /**
     * Verhindert Explosions-Schaden an Shrines
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onEntityExplode(event: EntityExplodeEvent) {
        val toRemove = mutableListOf<org.bukkit.block.Block>()

        event.blockList().forEach { block ->
            if (plugin.shrineManager.isShrineLocation(block.location)) {
                toRemove.add(block)
            }
        }

        event.blockList().removeAll(toRemove)
    }
}

