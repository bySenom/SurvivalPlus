package org.bysenom.survivalPlus.trading

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * Repräsentiert einen aktiven Trade zwischen 2 Spielern
 */
class Trade(
    val id: UUID,
    val player1: UUID,
    val player2: UUID,
    private val plugin: SurvivalPlus
) {
    // Trade-Slots: 4x4 Grid = 16 Slots pro Spieler
    private val player1Items = Array<ItemStack?>(16) { null }
    private val player2Items = Array<ItemStack?>(16) { null }
    
    // Confirmation Status
    var player1Confirmed = false
    var player2Confirmed = false

    /**
     * Setzt Item in Trade-Slot
     */
    fun setItem(playerId: UUID, slot: Int, item: ItemStack?) {
        if (slot !in 0..15) return
        
        when (playerId) {
            player1 -> {
                player1Items[slot] = item?.clone()
                resetConfirmations()
            }
            player2 -> {
                player2Items[slot] = item?.clone()
                resetConfirmations()
            }
        }
    }

    /**
     * Holt Item aus Trade-Slot
     */
    fun getItem(playerId: UUID, slot: Int): ItemStack? {
        if (slot !in 0..15) return null
        
        return when (playerId) {
            player1 -> player1Items[slot]
            player2 -> player2Items[slot]
            else -> null
        }
    }

    /**
     * Holt alle Items eines Spielers
     */
    fun getItems(playerId: UUID): Array<ItemStack?> {
        return when (playerId) {
            player1 -> player1Items.clone()
            player2 -> player2Items.clone()
            else -> emptyArray()
        }
    }

    /**
     * Holt Items des Trade-Partners
     */
    fun getPartnerItems(playerId: UUID): Array<ItemStack?> {
        return when (playerId) {
            player1 -> player2Items.clone()
            player2 -> player1Items.clone()
            else -> emptyArray()
        }
    }

    /**
     * Setzt Confirmation Status
     */
    fun setConfirmed(playerId: UUID, confirmed: Boolean) {
        when (playerId) {
            player1 -> player1Confirmed = confirmed
            player2 -> player2Confirmed = confirmed
        }
    }

    /**
     * Prüft ob Spieler confirmed hat
     */
    fun isConfirmed(playerId: UUID): Boolean {
        return when (playerId) {
            player1 -> player1Confirmed
            player2 -> player2Confirmed
            else -> false
        }
    }

    /**
     * Prüft ob beide Spieler confirmed haben
     */
    fun isBothConfirmed(): Boolean = player1Confirmed && player2Confirmed

    /**
     * Resetet Confirmations (wenn Items geändert werden)
     */
    private fun resetConfirmations() {
        player1Confirmed = false
        player2Confirmed = false
    }

    /**
     * Führt Trade durch (Items tauschen)
     */
    fun executeTradeExchange() {
        val p1 = plugin.server.getPlayer(player1) ?: return
        val p2 = plugin.server.getPlayer(player2) ?: return

        // Items von Player1 zu Player2
        player1Items.filterNotNull().forEach { item ->
            val remaining = p2.inventory.addItem(item)
            if (remaining.isNotEmpty()) {
                // Inventory voll - Item droppen
                remaining.values.forEach { p2.world.dropItemNaturally(p2.location, it) }
            }
        }

        // Items von Player2 zu Player1
        player2Items.filterNotNull().forEach { item ->
            val remaining = p1.inventory.addItem(item)
            if (remaining.isNotEmpty()) {
                // Inventory voll - Item droppen
                remaining.values.forEach { p1.world.dropItemNaturally(p1.location, it) }
            }
        }
    }

    /**
     * Gibt Items zurück (bei Cancel)
     */
    fun returnItems() {
        val p1 = plugin.server.getPlayer(player1)
        val p2 = plugin.server.getPlayer(player2)

        // Player1 Items zurückgeben
        if (p1 != null && p1.isOnline) {
            player1Items.filterNotNull().forEach { item ->
                val remaining = p1.inventory.addItem(item)
                if (remaining.isNotEmpty()) {
                    remaining.values.forEach { p1.world.dropItemNaturally(p1.location, it) }
                }
            }
        }

        // Player2 Items zurückgeben
        if (p2 != null && p2.isOnline) {
            player2Items.filterNotNull().forEach { item ->
                val remaining = p2.inventory.addItem(item)
                if (remaining.isNotEmpty()) {
                    remaining.values.forEach { p2.world.dropItemNaturally(p2.location, it) }
                }
            }
        }
    }

    /**
     * Holt Partner-UUID
     */
    fun getPartner(playerId: UUID): UUID? {
        return when (playerId) {
            player1 -> player2
            player2 -> player1
            else -> null
        }
    }
}
