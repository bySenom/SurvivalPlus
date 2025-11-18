package org.bysenom.survivalPlus.trading

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Trading GUI für Player-to-Player Trades
 */
class TradingGUI(private val plugin: SurvivalPlus) : Listener {

    companion object {
        private const val GUI_SIZE = 54 // 6 Reihen
        
        // Layout: Player1 (links 4x4), Mitte (Controls), Player2 (rechts 4x4)
        private val PLAYER_SLOTS = listOf(
            // Erste 4 Spalten = Player1, Letzte 4 Spalten = Player2
            0, 1, 2, 3,      // Reihe 1
            9, 10, 11, 12,   // Reihe 2
            18, 19, 20, 21,  // Reihe 3
            27, 28, 29, 30   // Reihe 4
        )
        
        private val PARTNER_SLOTS = listOf(
            5, 6, 7, 8,       // Reihe 1
            14, 15, 16, 17,   // Reihe 2
            23, 24, 25, 26,   // Reihe 3
            32, 33, 34, 35    // Reihe 4
        )

        private const val SEPARATOR_SLOT = 4
        private const val CONFIRM_SLOT = 49
        private const val CANCEL_SLOT = 45
        private const val INFO_SLOT = 40
    }

    /**
     * Öffnet Trade GUI für Spieler
     */
    fun openTradeGUI(player: Player, partner: Player, trade: Trade) {
        val title = Component.text("Handel: ", TextColor.fromHexString("#3F3F3F"))
            .append(Component.text(partner.name, NamedTextColor.YELLOW))
        val inv = Bukkit.createInventory(null, GUI_SIZE, title)
        
        setupStaticItems(inv, player, partner, trade)
        updateTradeItems(inv, player, trade)
        
        player.openInventory(inv)
    }

    /**
     * Setup statische GUI Elemente
     */
    private fun setupStaticItems(inv: Inventory, player: Player, partner: Player, trade: Trade) {
        // Separator (Spalte 5)
        val separator = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val separatorMeta = separator.itemMeta!!
        separatorMeta.displayName(Component.text(" "))
        separator.itemMeta = separatorMeta
        
        for (i in 0 until 6) {
            inv.setItem(SEPARATOR_SLOT + i * 9, separator)
        }

        // Info Item
        updateInfoItem(inv, player, partner, trade)

        // Confirm Button
        updateConfirmButton(inv, player, trade)

        // Cancel Button
        val cancel = ItemStack(Material.RED_CONCRETE)
        val cancelMeta = cancel.itemMeta!!
        cancelMeta.displayName(Component.text("ABBRECHEN").color(NamedTextColor.RED).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD))
        cancelMeta.lore(listOf(Component.text("Handel abbrechen").color(NamedTextColor.GRAY)))
        cancel.itemMeta = cancelMeta
        inv.setItem(CANCEL_SLOT, cancel)
    }

    /**
     * Updated Trade Items im GUI
     */
    private fun updateTradeItems(inv: Inventory, player: Player, trade: Trade) {
        // Eigene Items (links)
        val myItems = trade.getItems(player.uniqueId)
        PLAYER_SLOTS.forEachIndexed { index, slot ->
            inv.setItem(slot, myItems.getOrNull(index))
        }

        // Partner Items (rechts)
        val partnerItems = trade.getPartnerItems(player.uniqueId)
        PARTNER_SLOTS.forEachIndexed { index, slot ->
            val item = partnerItems.getOrNull(index)
            if (item != null) {
                // Partner-Items sind nicht verschiebbar (nur Anzeige)
                val display = item.clone()
                val meta = display.itemMeta
                if (meta != null) {
                    val lore = meta.lore()?.toMutableList() ?: mutableListOf()
                    lore.add(Component.empty())
                    lore.add(Component.text("Angebot von ").color(NamedTextColor.GRAY)
                        .append(Component.text(plugin.server.getPlayer(trade.getPartner(player.uniqueId)!!)?.name ?: "?").color(NamedTextColor.YELLOW)))
                    meta.lore(lore)
                    display.itemMeta = meta
                }
                inv.setItem(slot, display)
            } else {
                inv.setItem(slot, null)
            }
        }
    }

    /**
     * Updated Info Item
     */
    private fun updateInfoItem(inv: Inventory, player: Player, partner: Player, trade: Trade) {
        val info = ItemStack(Material.PAPER)
        val infoMeta = info.itemMeta!!
        infoMeta.displayName(Component.text("Handel Info").color(NamedTextColor.GOLD).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD))
        
        val myConfirmed = trade.isConfirmed(player.uniqueId)
        val partnerConfirmed = trade.isConfirmed(trade.getPartner(player.uniqueId)!!)
        
        infoMeta.lore(listOf(
            Component.empty(),
            Component.text("Dein Status: ").color(NamedTextColor.GRAY)
                .append(Component.text(if (myConfirmed) "✓ Bestätigt" else "✗ Nicht bestätigt").color(if (myConfirmed) NamedTextColor.GREEN else NamedTextColor.RED)),
            Component.text("${partner.name}: ").color(NamedTextColor.GRAY)
                .append(Component.text(if (partnerConfirmed) "✓ Bestätigt" else "✗ Nicht bestätigt").color(if (partnerConfirmed) NamedTextColor.GREEN else NamedTextColor.RED)),
            Component.empty(),
            Component.text("Lege Items in die ").color(NamedTextColor.GRAY)
                .append(Component.text("linken Slots").color(NamedTextColor.YELLOW))
                .append(Component.text(",").color(NamedTextColor.GRAY)),
            Component.text("um sie zu handeln.").color(NamedTextColor.GRAY),
            Component.empty(),
            if (myConfirmed && partnerConfirmed) {
                Component.text("Handel wird ausgeführt...").color(NamedTextColor.GREEN).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD)
            } else {
                Component.text("Bestätige mit dem ").color(NamedTextColor.GRAY)
                    .append(Component.text("Grünen Button").color(NamedTextColor.GREEN))
            }
        ))
        info.itemMeta = infoMeta
        inv.setItem(INFO_SLOT, info)
    }

    /**
     * Updated Confirm Button
     */
    private fun updateConfirmButton(inv: Inventory, player: Player, trade: Trade) {
        val confirmed = trade.isConfirmed(player.uniqueId)
        
        val button = ItemStack(if (confirmed) Material.LIME_CONCRETE else Material.GREEN_CONCRETE)
        val buttonMeta = button.itemMeta!!
        buttonMeta.displayName(Component.text(if (confirmed) "BESTÄTIGT" else "BESTÄTIGEN").color(NamedTextColor.GREEN).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD))
        buttonMeta.lore(if (confirmed) {
            listOf(
                Component.text("Du hast den Handel bestätigt.").color(NamedTextColor.GRAY),
                Component.text("Warte auf ").color(NamedTextColor.GRAY)
                    .append(Component.text(plugin.server.getPlayer(trade.getPartner(player.uniqueId)!!)?.name ?: "?").color(NamedTextColor.YELLOW)),
                Component.empty(),
                Component.text("Klicke erneut um abzubrechen").color(NamedTextColor.RED)
            )
        } else {
            listOf(
                Component.text("Bestätige den Handel.").color(NamedTextColor.GRAY),
                Component.text("Änderungen setzen die Bestätigung zurück!").color(NamedTextColor.RED)
            )
        })
        button.itemMeta = buttonMeta
        inv.setItem(CONFIRM_SLOT, button)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val inv = event.inventory
        
        // Prüfe ob Trade GUI
        if (!inv.holder.toString().contains("Handel:")) return
        
        val trade = plugin.tradeManager.getTradeForPlayer(player) ?: return
        val partner = plugin.server.getPlayer(trade.getPartner(player.uniqueId)!!) ?: return

        val clickedSlot = event.rawSlot
        val clickedItem = event.currentItem

        // Cancel Button
        if (clickedSlot == CANCEL_SLOT) {
            event.isCancelled = true
            plugin.tradeManager.closeTrade(trade.id, TradeCloseReason.CANCELLED)
            return
        }

        // Confirm Button
        if (clickedSlot == CONFIRM_SLOT) {
            event.isCancelled = true
            val confirmed = trade.isConfirmed(player.uniqueId)
            trade.setConfirmed(player.uniqueId, !confirmed)
            
            // GUI updaten
            refreshGUI(player, partner, trade)
            
            // Wenn beide confirmed -> Trade ausführen
            if (trade.isBothConfirmed()) {
                plugin.server.scheduler.runTaskLater(plugin, Runnable {
                    plugin.tradeManager.closeTrade(trade.id, TradeCloseReason.COMPLETED)
                }, 20L) // 1 Sekunde Delay
            }
            return
        }

        // Player Slots (eigene Items)
        if (clickedSlot in PLAYER_SLOTS) {
            event.isCancelled = true
            val slotIndex = PLAYER_SLOTS.indexOf(clickedSlot)
            
            when {
                // Item platzieren
                event.cursor.type != Material.AIR -> {
                    val cursorItem = event.cursor.clone()
                    trade.setItem(player.uniqueId, slotIndex, cursorItem)
                    player.setItemOnCursor(null)
                }
                // Item entfernen
                clickedItem != null && clickedItem.type != Material.AIR -> {
                    val removedItem = clickedItem.clone()
                    trade.setItem(player.uniqueId, slotIndex, null)
                    player.setItemOnCursor(removedItem)
                }
            }
            
            // GUI updaten
            refreshGUI(player, partner, trade)
            return
        }

        // Partner Slots (nicht interaktiv)
        if (clickedSlot in PARTNER_SLOTS) {
            event.isCancelled = true
            return
        }

        // Statische Items (nicht interaktiv)
        if (clickedSlot in 0 until GUI_SIZE) {
            event.isCancelled = true
            return
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val inv = event.inventory
        
        // Prüfe ob Trade GUI
        if (!inv.holder.toString().contains("Handel:")) return
        
        val trade = plugin.tradeManager.getTradeForPlayer(player) ?: return
        
        // Trade abbrechen wenn GUI geschlossen wird (außer Trade ist completed)
        if (!trade.isBothConfirmed()) {
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                // Prüfe ob Trade noch aktiv ist
                if (plugin.tradeManager.getTradeForPlayer(player) != null) {
                    plugin.tradeManager.closeTrade(trade.id, TradeCloseReason.CANCELLED)
                }
            }, 5L) // 5 Ticks Delay
        }
    }

    /**
     * Refreshed beide Trade GUIs
     */
    private fun refreshGUI(player: Player, partner: Player, trade: Trade) {
        // Player GUI updaten
        val playerInv = player.openInventory.topInventory
        updateTradeItems(playerInv, player, trade)
        updateInfoItem(playerInv, player, partner, trade)
        updateConfirmButton(playerInv, player, trade)

        // Partner GUI updaten
        val partnerInv = partner.openInventory.topInventory
        if (partnerInv.holder.toString().contains("Handel:")) {
            updateTradeItems(partnerInv, partner, trade)
            updateInfoItem(partnerInv, partner, player, trade)
            updateConfirmButton(partnerInv, partner, trade)
        }
    }
}
