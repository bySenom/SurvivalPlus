package org.bysenom.survivalPlus.scoreboard

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class ScoreboardListener(private val plugin: SurvivalPlus) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        // Verzögerter Setup (40 Ticks = 2 Sekunden), damit andere Plugins (HubPlugin) zuerst laufen
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            setupScoreboardIfNeeded(player)
        }, 40L) // 2 Sekunden Verzögerung
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        val player = event.player

        // Verzögert prüfen und Setup/Remove
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            setupScoreboardIfNeeded(player)
        }, 10L) // 0.5 Sekunden Verzögerung
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        // Optional: nichts nötig, Scoreboard wird automatisch verworfen
    }

    private fun setupScoreboardIfNeeded(player: org.bukkit.entity.Player) {
        val scoreboardWorlds = plugin.config.getStringList("scoreboard.enabled-worlds")
        if (scoreboardWorlds.isEmpty()) {
            // Keine Whitelist = überall anzeigen
            plugin.scoreboardManager.setup(player)
            return
        }

        val worldNameLower = player.world.name.lowercase()
        val isWorldEnabled = scoreboardWorlds.any { it.lowercase() == worldNameLower }

        if (isWorldEnabled) {
            // Welt ist in der Whitelist → Setup Scoreboard
            plugin.scoreboardManager.setup(player)
            plugin.logger.fine("Scoreboard aktiviert für ${player.name} in ${player.world.name}")
        } else {
            // Welt ist nicht in der Whitelist → Entferne Scoreboard
            plugin.scoreboardManager.remove(player)
            plugin.logger.fine("Scoreboard deaktiviert für ${player.name} in ${player.world.name}")
        }
    }
}

