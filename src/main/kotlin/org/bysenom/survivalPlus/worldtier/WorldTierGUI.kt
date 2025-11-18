package org.bysenom.survivalPlus.worldtier

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.time.Duration

/**
 * GUI für World Tier Auswahl - durch spezielle Altar-Blöcke aktiviert
 */
class WorldTierGUI(private val plugin: SurvivalPlus) {

    companion object {
        const val GUI_TITLE = "⚔ World Tier Altar ⚔"
    }

    /**
     * Öffnet das World Tier GUI für einen Spieler
     */
    fun openGUI(player: org.bukkit.entity.Player) {
        val world = player.world
        val currentTier = plugin.worldTierManager.getWorldTier(world)

        val inventory = Bukkit.createInventory(null, 27, Component.text(GUI_TITLE))

        // Setze World Tier Items
        WorldTier.entries.forEachIndexed { index, tier ->
            val slot = 10 + index
            inventory.setItem(slot, createTierItem(tier, tier == currentTier))
        }

        // Info Item
        inventory.setItem(13, createInfoItem())

        // Borders
        fillBorders(inventory)

        player.openInventory(inventory)
    }

    /**
     * Erstellt ein Item für ein World Tier
     */
    private fun createTierItem(tier: WorldTier, isCurrent: Boolean): ItemStack {
        val material = when (tier) {
            WorldTier.NORMAL -> Material.IRON_BLOCK
            WorldTier.HEROIC -> Material.GOLD_BLOCK
            WorldTier.EPIC -> Material.DIAMOND_BLOCK
            WorldTier.LEGENDARY -> Material.EMERALD_BLOCK
            WorldTier.MYTHIC -> Material.NETHERITE_BLOCK
        }

        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item

        meta.displayName(tier.getDisplayComponent()
            .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false))

        val lore = mutableListOf<Component>()
        lore.add(Component.empty())

        if (isCurrent) {
            lore.add(Component.text("✓ AKTIV").color(NamedTextColor.GREEN)
                .decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false))
            lore.add(Component.empty())
        }

        lore.addAll(tier.getDescription().map {
            it.decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false)
        })
        lore.add(Component.empty())

        // Kosten anzeigen
        val cost = getTierChangeCost(tier)
        lore.add(Component.text("Kosten:").color(NamedTextColor.YELLOW)
            .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false))
        lore.add(Component.text("${cost.amount}x ${cost.type.name}").color(NamedTextColor.WHITE)
            .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false))
        lore.add(Component.empty())

        if (!isCurrent) {
            lore.add(Component.text("» Klicken zum Aktivieren «").color(NamedTextColor.GOLD)
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false))
        }

        meta.lore(lore)

        // Glowing wenn aktiv
        if (isCurrent) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true)
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS)
        }

        item.itemMeta = meta
        return item
    }

    /**
     * Info Item
     */
    private fun createInfoItem(): ItemStack {
        val item = ItemStack(Material.ENCHANTED_BOOK)
        val meta = item.itemMeta ?: return item

        meta.displayName(Component.text("ℹ World Tier System")
            .color(NamedTextColor.AQUA)
            .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false))

        val lore = mutableListOf<Component>()
        lore.add(Component.empty())
        lore.add(Component.text("Erhöhe die Schwierigkeit für bessere Belohnungen!")
            .color(NamedTextColor.GRAY)
            .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false))
        lore.add(Component.empty())
        lore.add(Component.text("Höheres Tier = Stärkere Mobs")
            .color(NamedTextColor.RED)
            .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false))
        lore.add(Component.text("Höheres Tier = Bessere Drops")
            .color(NamedTextColor.GREEN)
            .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false))
        lore.add(Component.text("Höheres Tier = Mehr Special Mobs")
            .color(NamedTextColor.LIGHT_PURPLE)
            .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false))

        meta.lore(lore)
        item.itemMeta = meta
        return item
    }

    /**
     * Füllt Borders
     */
    private fun fillBorders(inventory: Inventory) {
        val glass = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text(" "))
            }
        }

        for (i in 0..26) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass)
            }
        }
    }

    /**
     * Gibt die Kosten für einen Tier-Wechsel zurück
     */
    fun getTierChangeCost(tier: WorldTier): ItemStack {
        return when (tier) {
            WorldTier.NORMAL -> ItemStack(Material.IRON_INGOT, 1)
            WorldTier.HEROIC -> ItemStack(Material.GOLD_INGOT, 16)
            WorldTier.EPIC -> ItemStack(Material.DIAMOND, 16)
            WorldTier.LEGENDARY -> ItemStack(Material.EMERALD, 32)
            WorldTier.MYTHIC -> ItemStack(Material.NETHERITE_INGOT, 16)
        }
    }

    /**
     * Prüft ob Spieler die Kosten hat
     */
    fun hasRequiredItems(player: org.bukkit.entity.Player, tier: WorldTier): Boolean {
        val cost = getTierChangeCost(tier)
        return player.inventory.containsAtLeast(cost, cost.amount)
    }

    /**
     * Entfernt die Kosten vom Spieler
     */
    fun removeRequiredItems(player: org.bukkit.entity.Player, tier: WorldTier) {
        val cost = getTierChangeCost(tier)
        player.inventory.removeItem(ItemStack(cost.type, cost.amount))
    }

    /**
     * Aktiviert ein World Tier
     */
    fun activateTier(player: org.bukkit.entity.Player, tier: WorldTier) {
        val world = player.world
        val currentTier = plugin.worldTierManager.getWorldTier(world)

        if (tier == currentTier) {
            player.sendMessage(Component.text("✘ Dieses Tier ist bereits aktiv!")
                .color(NamedTextColor.RED))
            return
        }

        if (!hasRequiredItems(player, tier)) {
            val cost = getTierChangeCost(tier)
            player.sendMessage(Component.text("✘ Du benötigst ${cost.amount}x ${cost.type.name}!")
                .color(NamedTextColor.RED))
            player.playSound(player.location, org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            return
        }

        // Entferne Kosten
        removeRequiredItems(player, tier)

        // Setze neues Tier
        plugin.worldTierManager.setWorldTier(world, tier)

        // Schließe GUI
        player.closeInventory()

        // Effekte
        player.playSound(player.location, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)

        // Title für alle Spieler in der Welt
        world.players.forEach { p ->
            val title = Title.title(
                Component.text("⚔ World Tier Geändert ⚔").color(NamedTextColor.GOLD),
                tier.getDisplayComponent(),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
            )
            p.showTitle(title)
            p.playSound(p.location, org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.3f, 1f)
        }
    }
}

