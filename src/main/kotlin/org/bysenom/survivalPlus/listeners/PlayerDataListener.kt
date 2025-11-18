package org.bysenom.survivalPlus.listeners

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Lädt/Speichert Spieler-Daten bei Join/Quit
 */
class PlayerDataListener(private val plugin: SurvivalPlus) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId

        // Lade Skill-Daten
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            plugin.skillDataManager.loadPlayerSkills(uuid)
            plugin.logger.fine("Skill-Daten für ${player.name} geladen")
        })

        // Lade Achievement-Daten
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            plugin.achievementManager.loadPlayerAchievements(uuid)
            plugin.logger.fine("Achievement-Daten für ${player.name} geladen")
        })
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val uuid = player.uniqueId

        // Speichere Daten und entlade aus Cache
        plugin.skillDataManager.unloadPlayer(uuid)
        plugin.achievementManager.unloadPlayer(uuid)
        
        plugin.logger.fine("Spieler-Daten für ${player.name} gespeichert und entladen")
    }
}
