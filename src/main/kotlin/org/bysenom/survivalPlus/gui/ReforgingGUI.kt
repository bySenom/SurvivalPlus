package org.bysenom.survivalPlus.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.ReforgingTier
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ReforgingGUI(private val plugin: SurvivalPlus) {

    companion object {
        const val GUI_TITLE = "Reforging Station"
        const val GUI_SIZE = 54

        const val ITEM_SLOT = 13
        const val TIER_1_SLOT = 29
        const val TIER_2_SLOT = 31
        const val TIER_3_SLOT = 33
        const val CONFIRM_SLOT = 49
        const val CANCEL_SLOT = 48
        const val INFO_SLOT = 50
    }

    fun open(player: Player, item: ItemStack) {
        if (!plugin.itemManager.isCustomItem(item)) {
            player.sendMessage(
                Component.text("Dieses Item kann nicht reforged werden!")
                    .color(NamedTextColor.RED)
            )
            return
        }

        val inv = createInventory(player, item)
        player.openInventory(inv)
    }

    private fun createInventory(player: Player, item: ItemStack): Inventory {
        val inv = Bukkit.createInventory(
            null,
            GUI_SIZE,
            Component.text(GUI_TITLE).color(NamedTextColor.DARK_PURPLE)
        )

        fillBackground(inv)
        inv.setItem(ITEM_SLOT, item)
        inv.setItem(TIER_1_SLOT, createTierButton(ReforgingTier.TIER_1, player))
        inv.setItem(TIER_2_SLOT, createTierButton(ReforgingTier.TIER_2, player))
        inv.setItem(TIER_3_SLOT, createTierButton(ReforgingTier.TIER_3, player))
        inv.setItem(CONFIRM_SLOT, createConfirmButton())
        inv.setItem(CANCEL_SLOT, createCancelButton())
        inv.setItem(INFO_SLOT, createInfoButton())

        return inv
    }

    private fun fillBackground(inv: Inventory) {
        val glassPane = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val meta = glassPane.itemMeta
        meta.displayName(Component.text(" "))
        glassPane.itemMeta = meta

        for (i in 0 until GUI_SIZE) {
            if (i !in listOf(ITEM_SLOT, TIER_1_SLOT, TIER_2_SLOT, TIER_3_SLOT,
                             CONFIRM_SLOT, CANCEL_SLOT, INFO_SLOT)) {
                inv.setItem(i, glassPane)
            }
        }
    }

    private fun createTierButton(tier: ReforgingTier, player: Player): ItemStack {
        val item = ItemStack(tier.material)
        val meta = item.itemMeta

        meta.displayName(
            Component.text(tier.displayName)
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
        )

        val lore = mutableListOf<Component>()
        lore.add(Component.empty())
        lore.add(Component.text("Dimension: ${tier.dimension}")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false))
        lore.add(Component.text("Kosten: ${tier.cost} ${tier.oreName}")
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false))
        lore.add(Component.empty())
        lore.add(Component.text("Moegliche Qualitaeten:")
            .color(NamedTextColor.AQUA)
            .decoration(TextDecoration.ITALIC, false))

        tier.allowedQualities.forEach { quality ->
            lore.add(Component.text("  - ${quality.displayName}")
                .color(quality.color)
                .decoration(TextDecoration.ITALIC, false))
        }

        lore.add(Component.empty())

        val hasEnough = player.inventory.contains(tier.material, tier.cost)
        if (hasEnough) {
            lore.add(Component.text("✔ Material vorhanden")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false))
        } else {
            lore.add(Component.text("✘ Nicht genug Material")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false))
        }

        meta.lore(lore)
        item.itemMeta = meta

        return item
    }

    private fun createConfirmButton(): ItemStack {
        val item = ItemStack(Material.LIME_STAINED_GLASS_PANE)
        val meta = item.itemMeta

        meta.displayName(
            Component.text("✔ Bestaetigen")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
        )

        val lore = listOf(
            Component.empty(),
            Component.text("Klicke um das Reforging zu starten")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
        )

        meta.lore(lore)
        item.itemMeta = meta

        return item
    }

    private fun createCancelButton(): ItemStack {
        val item = ItemStack(Material.RED_STAINED_GLASS_PANE)
        val meta = item.itemMeta

        meta.displayName(
            Component.text("✘ Abbrechen")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
        )

        meta.lore(listOf(
            Component.empty(),
            Component.text("Klicke um das GUI zu schliessen")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
        ))

        item.itemMeta = meta
        return item
    }

    private fun createInfoButton(): ItemStack {
        val item = ItemStack(Material.BOOK)
        val meta = item.itemMeta

        meta.displayName(
            Component.text("ℹ Information")
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
        )

        val lore = listOf(
            Component.empty(),
            Component.text("Waehle einen Tier aus")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false),
            Component.text("und klicke auf Bestaetigen")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false),
            Component.empty(),
            Component.text("Die Qualitaet wird zufaellig")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false),
            Component.text("aus dem erlaubten Pool gewaehlt")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false)
        )

        meta.lore(lore)
        item.itemMeta = meta

        return item
    }
}

