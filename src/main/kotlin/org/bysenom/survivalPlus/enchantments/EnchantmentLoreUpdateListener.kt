package org.bysenom.survivalPlus.enchantments

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack

/**
 * Listener der die Lore von Items mit Custom Enchantments aktualisiert
 * Damit die Enchantments und ihre Beschreibungen angezeigt werden
 */
class EnchantmentLoreUpdateListener(private val plugin: SurvivalPlus) : Listener {

    /**
     * Aktualisiert alle Items im Inventar wenn ein Spieler joint
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        // Aktualisiere alle Items im Inventar asynchron
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            updateAllItems(player.inventory.contents)
            updateAllItems(player.inventory.armorContents)
            updateAllItems(arrayOf(player.inventory.itemInOffHand))
        }, 20L) // 1 Sekunde Verzögerung
    }

    /**
     * Aktualisiert Items wenn ein Inventar geöffnet wird
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryOpen(event: InventoryOpenEvent) {
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            updateAllItems(event.inventory.contents)
        }, 1L)
    }

    /**
     * Aktualisiert Item wenn es in die Hand genommen wird
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        val item = player.inventory.getItem(event.newSlot)

        if (item != null && !item.type.isAir) {
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                updateItemLore(item)
            }, 1L)
        }
    }

    /**
     * Aktualisiert Items bei Hand-Swap (F-Taste)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onSwapHands(event: PlayerSwapHandItemsEvent) {
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            event.mainHandItem?.let { updateItemLore(it) }
            event.offHandItem?.let { updateItemLore(it) }
        }, 1L)
    }

    /**
     * Aktualisiert Items bei Inventory-Clicks
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClick(event: InventoryClickEvent) {
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            event.currentItem?.let { updateItemLore(it) }
            event.cursor?.let { updateItemLore(it) }
        }, 1L)
    }

    /**
     * Aktualisiert die Lore eines einzelnen Items
     */
    private fun updateItemLore(item: ItemStack) {
        if (item.type.isAir) return

        val meta = item.itemMeta ?: return

        // Prüfe ob Item Custom Enchantments hat
        val hasCustomEnchants = CustomEnchantment.entries.any { enchant ->
            val key = org.bukkit.NamespacedKey(plugin, "customench_${enchant.name.lowercase()}")
            meta.persistentDataContainer.has(key)
        }

        if (!hasCustomEnchants) return

        // Erzwinge Lore-Update durch Entfernen und Neu-Hinzufügen
        val enchantments = plugin.enchantmentManager.getEnchantments(item)

        if (enchantments.isNotEmpty()) {
            // Trigger Update durch temporäres Entfernen/Hinzufügen
            val firstEnchant = enchantments.keys.first()
            val firstLevel = enchantments[firstEnchant] ?: 1

            plugin.enchantmentManager.removeEnchantment(item, firstEnchant)
            plugin.enchantmentManager.addEnchantment(item, firstEnchant, firstLevel)
        }
    }

    /**
     * Aktualisiert alle Items in einem Array
     */
    private fun updateAllItems(items: Array<ItemStack?>) {
        items.filterNotNull().forEach { item ->
            if (!item.type.isAir) {
                updateItemLore(item)
            }
        }
    }
}

