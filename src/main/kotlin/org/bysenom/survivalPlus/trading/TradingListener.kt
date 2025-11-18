package org.bysenom.survivalPlus.trading

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Listener für Trading-Events
 */
class TradingListener(private val plugin: SurvivalPlus) : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // Handle Disconnect während Trade
        plugin.tradeManager.handlePlayerDisconnect(event.player)
    }
}
