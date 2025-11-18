package org.bysenom.survivalPlus.crafting

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.Quality
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import java.util.UUID

class CustomCraftingListener(private val plugin: SurvivalPlus) : Listener {

    private val selectedQuality = mutableMapOf<UUID, Quality>()

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val view = event.view
        if (!view.title().toString().contains(CustomCraftingGUI.GUI_TITLE)) return

        val player = event.whoClicked as? Player ?: return
        val clickedSlot = event.rawSlot
        val inventory = view.topInventory

        if (clickedSlot < 54) {
            when (clickedSlot) {
                CustomCraftingGUI.BASE_ITEM_SLOT -> {
                    plugin.server.scheduler.runTaskLater(plugin, Runnable {
                        updateGUI(player, inventory)
                    }, 1L)
                }
                CustomCraftingGUI.QUALITY_SELECTOR_SLOT -> {
                    event.isCancelled = true
                    cycleQuality(player)
                    updateGUI(player, inventory)
                }
                CustomCraftingGUI.CRAFT_BUTTON_SLOT -> {
                    event.isCancelled = true
                    handleCraft(player, inventory)
                }
                else -> event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val view = event.view
        if (!view.title().toString().contains(CustomCraftingGUI.GUI_TITLE)) return

        val player = event.player as? Player ?: return
        val inventory = view.topInventory

        val baseItem = inventory.getItem(CustomCraftingGUI.BASE_ITEM_SLOT)
        if (baseItem != null && !baseItem.type.isAir) {
            player.inventory.addItem(baseItem)
        }
        selectedQuality.remove(player.uniqueId)
    }

    private fun cycleQuality(player: Player) {
        val current = selectedQuality.getOrDefault(player.uniqueId, Quality.COMMON)
        val next = Quality.entries.let { qualities ->
            val currentIndex = qualities.indexOf(current)
            qualities[(currentIndex + 1) % qualities.size]
        }
        selectedQuality[player.uniqueId] = next
        player.playSound(player.location, org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f)
    }

    private fun updateGUI(player: Player, inventory: org.bukkit.inventory.Inventory) {
        val quality = selectedQuality.getOrDefault(player.uniqueId, Quality.COMMON)
        val baseItem = inventory.getItem(CustomCraftingGUI.BASE_ITEM_SLOT)

        plugin.customCraftingGUI.setQualitySelector(inventory, quality)
        plugin.customCraftingGUI.updatePreview(inventory, baseItem, quality)

        val canCraft = baseItem != null && !baseItem.type.isAir &&
                       plugin.customCraftingGUI.hasRequiredMaterial(player, quality)
        plugin.customCraftingGUI.setCraftButton(inventory, canCraft)
    }

    private fun handleCraft(player: Player, inventory: org.bukkit.inventory.Inventory) {
        val quality = selectedQuality.getOrDefault(player.uniqueId, Quality.COMMON)
        val baseItem = inventory.getItem(CustomCraftingGUI.BASE_ITEM_SLOT)

        if (baseItem == null || baseItem.type.isAir) {
            player.sendMessage(Component.text("✘ Platziere zuerst ein Item!")
                .color(NamedTextColor.RED))
            player.playSound(player.location, org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            return
        }

        if (!plugin.customCraftingGUI.hasRequiredMaterial(player, quality)) {
            player.sendMessage(Component.text("✘ Nicht genug Material!")
                .color(NamedTextColor.RED))
            player.playSound(player.location, org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            return
        }

        val result = plugin.customCraftingGUI.craftItem(player, baseItem, quality)
        plugin.customCraftingGUI.removeMaterial(player, quality)
        inventory.setItem(CustomCraftingGUI.BASE_ITEM_SLOT, null)
        player.inventory.addItem(result)
        updateGUI(player, inventory)
        plugin.skillManager.addXP(player, org.bysenom.survivalPlus.skills.Skill.CRAFTING, quality.tier * 10)
    }
}

