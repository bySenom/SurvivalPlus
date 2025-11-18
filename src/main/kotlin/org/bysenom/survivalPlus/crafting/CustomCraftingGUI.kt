// kotlin
package org.bysenom.survivalPlus.crafting

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.Quality

class CustomCraftingGUI(private val plugin: SurvivalPlus) {

    companion object {
        const val CRAFT_BUTTON_SLOT = 40
        const val RESULT_SLOT = 24
        const val QUALITY_SELECTOR_SLOT = 29
        const val BASE_ITEM_SLOT = 20
        const val GUI_TITLE = "Custom Amboss"
    }

    fun openGUI(player: Player) {
        val inventory = Bukkit.createInventory(null, 54, Component.text(GUI_TITLE))
        setCraftButton(inventory, false)
        setQualitySelector(inventory, Quality.COMMON)
        fillBorders(inventory)
        player.openInventory(inventory)
    }

    private fun fillBorders(inventory: Inventory) {
        val glass = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text(" "))
            }
        }
        for (i in 0..8) inventory.setItem(i, glass)
        for (i in 0..8) inventory.setItem(i + 45, glass)
        for (i in 0..4) {
            inventory.setItem(i * 9, glass)
            inventory.setItem(i * 9 + 8, glass)
        }
    }

    fun setQualitySelector(inventory: Inventory, quality: Quality) {
        val item = ItemStack(getQualityMaterial(quality)).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text("Qualität: ${quality.displayName}")
                    .color(quality.color)
                    .decoration(TextDecoration.ITALIC, false))
                lore(listOf(
                    Component.text("Benötigt: ${getRequiredAmount(quality)}x ${getQualityMaterialName(quality)}"),
                    Component.empty(),
                    Component.text("Klicke zum Ändern").color(NamedTextColor.GRAY)
                ))
            }
        }
        inventory.setItem(QUALITY_SELECTOR_SLOT, item)
    }

    fun setCraftButton(inventory: Inventory, enabled: Boolean) {
        val material = if (enabled) Material.LIME_STAINED_GLASS_PANE else Material.RED_STAINED_GLASS_PANE
        val item = ItemStack(material).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text(if (enabled) "✔ Craften" else "✘ Nicht bereit")
                    .color(if (enabled) NamedTextColor.GREEN else NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false))
            }
        }
        inventory.setItem(CRAFT_BUTTON_SLOT, item)
    }

    fun updatePreview(inventory: Inventory, baseItem: ItemStack?, quality: Quality) {
        if (baseItem == null || baseItem.type.isAir) {
            inventory.setItem(RESULT_SLOT, null)
            return
        }
        val preview = plugin.itemManager.createItem(baseItem.type, quality)
        val withEnchants = plugin.enchantmentManager.addRandomEnchantments(preview, quality)
        inventory.setItem(RESULT_SLOT, withEnchants)
    }

    fun hasRequiredMaterial(player: Player, quality: Quality): Boolean {
        val material = getQualityMaterial(quality)
        val required = getRequiredAmount(quality)
        var count = 0
        player.inventory.contents.forEach { item ->
            if (item != null && item.type == material) count += item.amount
        }
        return count >= required
    }

    fun removeMaterial(player: Player, quality: Quality) {
        var remaining = getRequiredAmount(quality)
        val material = getQualityMaterial(quality)
        val contents = player.inventory.contents
        for (item in contents) {
            if (remaining == 0) break
            if (item != null && item.type == material && remaining > 0) {
                val toRemove = minOf(item.amount, remaining)
                item.amount -= toRemove
                remaining -= toRemove
            }
        }
        player.inventory.contents = contents
        player.updateInventory()
    }

    fun craftItem(player: Player, baseItem: ItemStack, quality: Quality): ItemStack {
        val customItem = plugin.itemManager.createItem(baseItem.type, quality)
        val withEnchants = plugin.enchantmentManager.addRandomEnchantments(customItem, quality)

        // Give item and feedback
        player.inventory.addItem(withEnchants)
        plugin.messageManager.showItemReceivedTitle(player, quality) // Annahme: existierende Methode
        player.sendMessage(Component.text("✨ Du hast ein ${quality.displayName} Item gecraftet!").toString())
        plugin.particleEffectManager.spawnItemDropParticles(player.location, quality)
        player.playSound(player.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f)
        player.playSound(player.location, org.bukkit.Sound.BLOCK_ANVIL_USE, 1f, 1f)

        return withEnchants
    }

    private fun getRequiredAmount(quality: Quality): Int = when (quality) {
        Quality.MYTHIC -> 32
        Quality.LEGENDARY -> 16
        Quality.EPIC -> 8
        Quality.RARE -> 4
        Quality.UNCOMMON -> 2
        Quality.COMMON -> 1
    }

    private fun getQualityMaterialName(quality: Quality): String = when (quality) {
        Quality.MYTHIC -> "Nether Stern"
        Quality.LEGENDARY -> "Netherite"
        Quality.EPIC -> "Smaragd"
        Quality.RARE -> "Diamant"
        Quality.UNCOMMON -> "Gold"
        Quality.COMMON -> "Eisen"
    }

    private fun getQualityMaterial(quality: Quality): Material = when (quality) {
        Quality.MYTHIC -> Material.NETHER_STAR
        Quality.LEGENDARY -> Material.NETHERITE_INGOT
        Quality.EPIC -> Material.EMERALD
        Quality.RARE -> Material.DIAMOND
        Quality.UNCOMMON -> Material.GOLD_INGOT
        Quality.COMMON -> Material.IRON_INGOT
    }
}
