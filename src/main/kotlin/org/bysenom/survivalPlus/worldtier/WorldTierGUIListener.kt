package org.bysenom.survivalPlus.worldtier

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * Listener für World Tier GUI
 */
class WorldTierGUIListener(private val plugin: SurvivalPlus) : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onInventoryClick(event: InventoryClickEvent) {
        val view = event.view
        if (!view.title().toString().contains(WorldTierGUI.GUI_TITLE)) return

        event.isCancelled = true

        val player = event.whoClicked as? org.bukkit.entity.Player ?: return
        val clickedItem = event.currentItem ?: return

        // Finde angeklicktes Tier
        val clickedTier = WorldTier.entries.find { tier ->
            val tierItem = plugin.worldTierGUI.getTierChangeCost(tier)
            clickedItem.type == when (tier) {
                WorldTier.NORMAL -> org.bukkit.Material.IRON_BLOCK
                WorldTier.HEROIC -> org.bukkit.Material.GOLD_BLOCK
                WorldTier.EPIC -> org.bukkit.Material.DIAMOND_BLOCK
                WorldTier.LEGENDARY -> org.bukkit.Material.EMERALD_BLOCK
                WorldTier.MYTHIC -> org.bukkit.Material.NETHERITE_BLOCK
            }
        }

        if (clickedTier != null) {
            plugin.worldTierGUI.activateTier(player, clickedTier)
        }
    }
}

