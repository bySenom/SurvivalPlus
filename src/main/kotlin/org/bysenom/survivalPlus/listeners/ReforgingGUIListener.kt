package org.bysenom.survivalPlus.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.gui.ReforgingGUI
import org.bysenom.survivalPlus.managers.ReforgingResult
import org.bysenom.survivalPlus.models.ReforgingTier
import org.bysenom.survivalPlus.display.MessageManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

class ReforgingGUIListener(private val plugin: SurvivalPlus) : Listener {

    // Store selected tier per player
    private val selectedTiers = mutableMapOf<UUID, ReforgingTier>()

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val view = event.view

        // Check if it's our GUI
        if (view.title() != Component.text(ReforgingGUI.GUI_TITLE)
                .color(NamedTextColor.DARK_PURPLE)) {
            return
        }

        event.isCancelled = true // Cancel all clicks in our GUI

        val clickedSlot = event.rawSlot
        if (clickedSlot < 0 || clickedSlot >= ReforgingGUI.GUI_SIZE) return

        when (clickedSlot) {
            ReforgingGUI.TIER_1_SLOT -> handleTierSelection(player, ReforgingTier.TIER_1, event)
            ReforgingGUI.TIER_2_SLOT -> handleTierSelection(player, ReforgingTier.TIER_2, event)
            ReforgingGUI.TIER_3_SLOT -> handleTierSelection(player, ReforgingTier.TIER_3, event)
            ReforgingGUI.CONFIRM_SLOT -> handleConfirm(player, event)
            ReforgingGUI.CANCEL_SLOT -> handleCancel(player)
            ReforgingGUI.INFO_SLOT -> {} // Info button does nothing
            ReforgingGUI.ITEM_SLOT -> {} // Item slot is locked
        }
    }

    private fun handleTierSelection(player: Player, tier: ReforgingTier, event: InventoryClickEvent) {
        // Check if player has enough material
        if (!player.inventory.contains(tier.material, tier.cost)) {
            player.sendMessage(
                Component.text("Nicht genug ${tier.oreName}! Benoetigt: ${tier.cost}")
                    .color(NamedTextColor.RED)
            )
            player.playSound(player.location, org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            return
        }

        selectedTiers[player.uniqueId] = tier
        player.sendMessage(
            Component.text("${tier.displayName} ausgewaehlt!")
                .color(NamedTextColor.GREEN)
        )
        player.playSound(player.location, org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f)

        // Add enchantment glint to selected tier
        highlightSelectedTier(event.inventory, tier)
    }

    private fun highlightSelectedTier(inventory: org.bukkit.inventory.Inventory, tier: ReforgingTier) {
        // Visual feedback by adding glint (enchantment effect)
        val slots = listOf(
            ReforgingGUI.TIER_1_SLOT to ReforgingTier.TIER_1,
            ReforgingGUI.TIER_2_SLOT to ReforgingTier.TIER_2,
            ReforgingGUI.TIER_3_SLOT to ReforgingTier.TIER_3
        )

        slots.forEach { (slot, tierValue) ->
            val item = inventory.getItem(slot) ?: return@forEach
            val meta = item.itemMeta ?: return@forEach

            if (tierValue == tier) {
                // Add glint to selected
                meta.setEnchantmentGlintOverride(true)
            } else {
                // Remove glint from others
                meta.setEnchantmentGlintOverride(false)
            }

            item.itemMeta = meta
            inventory.setItem(slot, item)
        }
    }

    private fun handleConfirm(player: Player, event: InventoryClickEvent) {
        val tier = selectedTiers[player.uniqueId]

        if (tier == null) {
            player.sendMessage(
                Component.text("Bitte waehle zuerst einen Tier aus!")
                    .color(NamedTextColor.RED)
            )
            player.playSound(player.location, org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            return
        }

        // Get the item to reforge
        val itemToReforge = event.inventory.getItem(ReforgingGUI.ITEM_SLOT)
        if (itemToReforge == null || itemToReforge.type.isAir) {
            player.sendMessage(
                Component.text("Kein Item zum Reforgen gefunden!")
                    .color(NamedTextColor.RED)
            )
            player.closeInventory()
            return
        }

        // Find material in inventory
        val materialStack = findMaterial(player, tier)
        if (materialStack == null) {
            player.sendMessage(
                Component.text("Material nicht mehr vorhanden!")
                    .color(NamedTextColor.RED)
            )
            player.closeInventory()
            return
        }

        // Get old quality for comparison
        val oldQuality = plugin.itemManager.getQuality(itemToReforge)

        // Close GUI
        player.closeInventory()

        // Zeige Boss Bar Animation
        plugin.progressBarManager.showReforgingProcess(player)

        // Perform reforging nach kurzer Verzögerung (für Animation)
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            val result = plugin.reforgingManager.reforgeItem(player, itemToReforge, materialStack)

            // Send result message
            player.sendMessage(result.getMessage())

            // Play sound and effects based on result
            when (result) {
                is ReforgingResult.Success -> {
                    player.playSound(player.location, org.bukkit.Sound.BLOCK_ANVIL_USE, 1f, 1f)
                    player.playSound(player.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f)

                    // Spawn particles
                    plugin.server.scheduler.runTaskLater(plugin, Runnable {
                        spawnSuccessParticles(player)
                    }, 1L)

                    // Show title if quality improved
                    if (oldQuality != null) {
                        plugin.messageManager.showReforgingSuccessTitle(player, oldQuality, result.newQuality)
                    }
                }
                else -> {
                    player.playSound(player.location, org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f)
                }
            }
        }, 40L) // 2 Sekunden Delay für Animation

        selectedTiers.remove(player.uniqueId)
    }

    private fun handleCancel(player: Player) {
        player.closeInventory()
        player.sendMessage(
            Component.text("Reforging abgebrochen")
                .color(NamedTextColor.GRAY)
        )
        player.playSound(player.location, org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 0.8f)
    }

    private fun findMaterial(player: Player, tier: ReforgingTier): ItemStack? {
        return player.inventory.contents.find {
            it != null && it.type == tier.material && it.amount >= tier.cost
        }
    }

    private fun spawnSuccessParticles(player: Player) {
        val location = player.location.add(0.0, 1.0, 0.0)
        player.world.spawnParticle(
            org.bukkit.Particle.HAPPY_VILLAGER,
            location,
            30,
            0.5, 0.5, 0.5,
            0.1
        )
        player.world.spawnParticle(
            org.bukkit.Particle.ENCHANT,
            location,
            50,
            0.5, 0.5, 0.5,
            1.0
        )
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return

        // Clean up selection when GUI is closed
        if (event.view.title() == Component.text(ReforgingGUI.GUI_TITLE)
                .color(NamedTextColor.DARK_PURPLE)) {
            selectedTiers.remove(player.uniqueId)
        }
    }
}

