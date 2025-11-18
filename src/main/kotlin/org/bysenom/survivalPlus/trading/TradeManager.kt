package org.bysenom.survivalPlus.trading

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Verwaltet Player-to-Player Trading
 */
class TradeManager(private val plugin: SurvivalPlus) {

    // Active Trades: TradeID -> Trade
    private val activeTrades = ConcurrentHashMap<UUID, Trade>()
    
    // Trade Requests: Target UUID -> Requester UUID
    private val pendingRequests = ConcurrentHashMap<UUID, TradeRequest>()
    
    // Cooldowns: Player UUID -> Timestamp
    private val tradeCooldowns = ConcurrentHashMap<UUID, Long>()

    companion object {
        const val REQUEST_TIMEOUT = 30_000L // 30 Sekunden
        const val TRADE_COOLDOWN = 5_000L   // 5 Sekunden zwischen Trades
    }

    /**
     * Sendet eine Trade-Anfrage
     */
    fun sendTradeRequest(requester: Player, target: Player): Boolean {
        // Validierung
        if (requester.uniqueId == target.uniqueId) {
            requester.sendMessage("§cDu kannst nicht mit dir selbst handeln!")
            return false
        }

        // Cooldown prüfen
        val cooldown = tradeCooldowns[requester.uniqueId]
        if (cooldown != null && System.currentTimeMillis() - cooldown < TRADE_COOLDOWN) {
            val remaining = (TRADE_COOLDOWN - (System.currentTimeMillis() - cooldown)) / 1000
            requester.sendMessage("§cBitte warte noch ${remaining}s bevor du erneut handelst!")
            return false
        }

        // Prüfe ob bereits Trade läuft
        if (isInTrade(requester) || isInTrade(target)) {
            requester.sendMessage("§c${target.name} handelt bereits mit jemand anderem!")
            return false
        }

        // Prüfe ob bereits Request pending
        if (pendingRequests.containsKey(target.uniqueId)) {
            requester.sendMessage("§c${target.name} hat bereits eine ausstehende Trade-Anfrage!")
            return false
        }

        // Request erstellen
        val request = TradeRequest(requester.uniqueId, target.uniqueId, System.currentTimeMillis())
        pendingRequests[target.uniqueId] = request

        // Nachrichten
        requester.sendMessage("§aHandels-Anfrage an §e${target.name}§a gesendet!")
        target.sendMessage("§e${requester.name}§a möchte mit dir handeln!")
        target.sendMessage("§7/sp trade accept §a- Akzeptieren")
        target.sendMessage("§7/sp trade deny §c- Ablehnen")

        // Auto-Timeout
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            val req = pendingRequests.remove(target.uniqueId)
            if (req != null && req.requesterId == requester.uniqueId) {
                requester.sendMessage("§cHandels-Anfrage an ${target.name} ist abgelaufen!")
                target.sendMessage("§cHandels-Anfrage von ${requester.name} ist abgelaufen!")
            }
        }, REQUEST_TIMEOUT / 50) // Ticks

        return true
    }

    /**
     * Akzeptiert eine Trade-Anfrage
     */
    fun acceptTradeRequest(accepter: Player): Boolean {
        val request = pendingRequests.remove(accepter.uniqueId)
        if (request == null) {
            accepter.sendMessage("§cKeine ausstehende Trade-Anfrage!")
            return false
        }

        // Timeout prüfen
        if (System.currentTimeMillis() - request.timestamp > REQUEST_TIMEOUT) {
            accepter.sendMessage("§cDie Trade-Anfrage ist abgelaufen!")
            return false
        }

        val requester = plugin.server.getPlayer(request.requesterId)
        if (requester == null || !requester.isOnline) {
            accepter.sendMessage("§c${requester?.name ?: "Spieler"} ist nicht mehr online!")
            return false
        }

        // Trade starten
        return startTrade(requester, accepter)
    }

    /**
     * Lehnt eine Trade-Anfrage ab
     */
    fun denyTradeRequest(denier: Player): Boolean {
        val request = pendingRequests.remove(denier.uniqueId)
        if (request == null) {
            denier.sendMessage("§cKeine ausstehende Trade-Anfrage!")
            return false
        }

        val requester = plugin.server.getPlayer(request.requesterId)
        requester?.sendMessage("§c${denier.name} hat deine Trade-Anfrage abgelehnt!")
        denier.sendMessage("§cTrade-Anfrage abgelehnt!")
        return true
    }

    /**
     * Startet einen Trade
     */
    private fun startTrade(player1: Player, player2: Player): Boolean {
        val tradeId = UUID.randomUUID()
        val trade = Trade(tradeId, player1.uniqueId, player2.uniqueId, plugin)
        
        activeTrades[tradeId] = trade
        
        // GUI öffnen
        plugin.tradingGUI.openTradeGUI(player1, player2, trade)
        plugin.tradingGUI.openTradeGUI(player2, player1, trade)
        
        player1.sendMessage("§aHandel mit §e${player2.name}§a gestartet!")
        player2.sendMessage("§aHandel mit §e${player1.name}§a gestartet!")
        
        return true
    }

    /**
     * Schließt einen Trade (Cancel oder Complete)
     */
    fun closeTrade(tradeId: UUID, reason: TradeCloseReason) {
        val trade = activeTrades.remove(tradeId) ?: return
        
        val player1 = plugin.server.getPlayer(trade.player1)
        val player2 = plugin.server.getPlayer(trade.player2)
        
        when (reason) {
            TradeCloseReason.COMPLETED -> {
                // Items tauschen
                trade.executeTradeExchange()
                
                player1?.sendMessage("§aHandel erfolgreich abgeschlossen!")
                player2?.sendMessage("§aHandel erfolgreich abgeschlossen!")
                
                // Cooldown setzen
                tradeCooldowns[trade.player1] = System.currentTimeMillis()
                tradeCooldowns[trade.player2] = System.currentTimeMillis()
                
                // Log
                plugin.logger.info("[Trade] ${player1?.name} <-> ${player2?.name} completed")
            }
            TradeCloseReason.CANCELLED -> {
                // Items zurückgeben
                trade.returnItems()
                
                player1?.sendMessage("§cHandel abgebrochen!")
                player2?.sendMessage("§cHandel abgebrochen!")
            }
            TradeCloseReason.PLAYER_DISCONNECTED -> {
                // Items zurückgeben
                trade.returnItems()
                
                player1?.sendMessage("§cHandel abgebrochen - Spieler hat die Verbindung verloren!")
                player2?.sendMessage("§cHandel abgebrochen - Spieler hat die Verbindung verloren!")
            }
        }
        
        // GUIs schließen
        player1?.closeInventory()
        player2?.closeInventory()
    }

    /**
     * Prüft ob Spieler in einem Trade ist
     */
    fun isInTrade(player: Player): Boolean {
        return activeTrades.values.any { 
            it.player1 == player.uniqueId || it.player2 == player.uniqueId 
        }
    }

    /**
     * Holt Trade eines Spielers
     */
    fun getTradeForPlayer(player: Player): Trade? {
        return activeTrades.values.find { 
            it.player1 == player.uniqueId || it.player2 == player.uniqueId 
        }
    }

    /**
     * Cleanup beim Server-Stop
     */
    fun shutdown() {
        // Alle Trades abbrechen und Items zurückgeben
        activeTrades.values.forEach { trade ->
            trade.returnItems()
        }
        activeTrades.clear()
        pendingRequests.clear()
    }

    /**
     * Cleanup bei Player-Disconnect
     */
    fun handlePlayerDisconnect(player: Player) {
        // Pending Request entfernen
        pendingRequests.remove(player.uniqueId)
        
        // Aktiven Trade abbrechen
        val trade = getTradeForPlayer(player)
        if (trade != null) {
            closeTrade(trade.id, TradeCloseReason.PLAYER_DISCONNECTED)
        }
    }
}

/**
 * Trade-Request Daten
 */
data class TradeRequest(
    val requesterId: UUID,
    val targetId: UUID,
    val timestamp: Long
)

/**
 * Trade Close Reasons
 */
enum class TradeCloseReason {
    COMPLETED,
    CANCELLED,
    PLAYER_DISCONNECTED
}
