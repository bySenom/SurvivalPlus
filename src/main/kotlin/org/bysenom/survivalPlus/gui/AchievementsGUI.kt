package org.bysenom.survivalPlus.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.achievements.Achievement
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Achievements GUI - Browse & Filter System
 */
class AchievementsGUI(private val plugin: SurvivalPlus) : Listener {

    private val achievementPages = mutableMapOf<Player, AchievementFilter>()

    enum class AchievementFilter {
        ALL, COMPLETED, IN_PROGRESS, LOCKED
    }

    /**
     * Öffnet das Achievements GUI
     */
    fun openGUI(player: Player, filter: AchievementFilter = AchievementFilter.ALL) {
        achievementPages[player] = filter
        
        val inventory = Bukkit.createInventory(null, 54, Component.text("🏆 Erfolge", NamedTextColor.GOLD, TextDecoration.BOLD))

        // Hole Spieler-Daten
        val completedAchievements = plugin.achievementManager.getCompletedAchievements(player.uniqueId)
        val progressMap = plugin.achievementManager.getAllProgress(player.uniqueId)

        // Filter Achievements
        val filteredAchievements = Achievement.entries.filter { achievement ->
            when (filter) {
                AchievementFilter.ALL -> true
                AchievementFilter.COMPLETED -> achievement in completedAchievements
                AchievementFilter.IN_PROGRESS -> {
                    achievement !in completedAchievements && 
                    progressMap.any { (key, value) -> key.contains(achievement.name) && value > 0 }
                }
                AchievementFilter.LOCKED -> achievement !in completedAchievements
            }
        }

        // Achievement Items erstellen (max 45 Slots für Achievements)
        filteredAchievements.take(45).forEachIndexed { index, achievement ->
            val isCompleted = achievement in completedAchievements
            val item = createAchievementItem(achievement, isCompleted, progressMap)
            inventory.setItem(index, item)
        }

        // Filter-Buttons (Bottom Row)
        val filterSlots = listOf(45, 46, 47, 48)
        val filters = listOf(
            Triple(AchievementFilter.ALL, Material.COMPASS, "Alle"),
            Triple(AchievementFilter.COMPLETED, Material.EMERALD, "Abgeschlossen"),
            Triple(AchievementFilter.IN_PROGRESS, Material.CLOCK, "In Arbeit"),
            Triple(AchievementFilter.LOCKED, Material.BARRIER, "Gesperrt")
        )

        filters.forEachIndexed { index, (filterType, material, name) ->
            val item = ItemStack(material)
            val meta = item.itemMeta
            val isActive = filter == filterType
            
            meta.displayName(
                Component.text("📁 $name", if (isActive) NamedTextColor.GREEN else NamedTextColor.GRAY, TextDecoration.BOLD)
            )
            
            val count = when (filterType) {
                AchievementFilter.ALL -> Achievement.entries.size
                AchievementFilter.COMPLETED -> completedAchievements.size
                AchievementFilter.IN_PROGRESS -> Achievement.entries.count { 
                    it !in completedAchievements && progressMap.any { (key, value) -> 
                        key.contains(it.name) && value > 0 
                    }
                }
                AchievementFilter.LOCKED -> Achievement.entries.size - completedAchievements.size
            }
            
            meta.lore(listOf(
                Component.text(""),
                Component.text("$count Erfolge", if (isActive) NamedTextColor.WHITE else NamedTextColor.DARK_GRAY),
                Component.text(""),
                if (isActive) 
                    Component.text("✓ Aktiver Filter", NamedTextColor.GREEN)
                else 
                    Component.text("Klicke zum Filtern", NamedTextColor.GRAY)
            ))
            
            item.itemMeta = meta
            inventory.setItem(filterSlots[index], item)
        }

        // Stats
        val statsItem = ItemStack(Material.BOOK)
        val statsMeta = statsItem.itemMeta
        statsMeta.displayName(Component.text("📊 Statistik", NamedTextColor.AQUA, TextDecoration.BOLD))
        statsMeta.lore(listOf(
            Component.text(""),
            Component.text("Gesamt: ", NamedTextColor.GRAY)
                .append(Component.text("${Achievement.entries.size}", NamedTextColor.WHITE)),
            Component.text("Abgeschlossen: ", NamedTextColor.GRAY)
                .append(Component.text("${completedAchievements.size}", NamedTextColor.GREEN)),
            Component.text("Verbleibend: ", NamedTextColor.GRAY)
                .append(Component.text("${Achievement.entries.size - completedAchievements.size}", NamedTextColor.YELLOW)),
            Component.text(""),
            Component.text("Fortschritt: ", NamedTextColor.GRAY)
                .append(Component.text("${(completedAchievements.size * 100 / Achievement.entries.size)}%", NamedTextColor.GOLD))
        ))
        statsItem.itemMeta = statsMeta
        inventory.setItem(49, statsItem)

        // Schließen-Button
        val closeItem = ItemStack(Material.BARRIER)
        val closeMeta = closeItem.itemMeta
        closeMeta.displayName(Component.text("❌ Schließen", NamedTextColor.RED, TextDecoration.BOLD))
        closeItem.itemMeta = closeMeta
        inventory.setItem(53, closeItem)

        player.openInventory(inventory)
    }

    /**
     * Erstellt ein Achievement Item
     */
    private fun createAchievementItem(
        achievement: Achievement, 
        isCompleted: Boolean, 
        progressMap: Map<String, Int>
    ): ItemStack {
        val item = ItemStack(if (isCompleted) achievement.icon else Material.GRAY_DYE)
        val meta = item.itemMeta

        // Name
        val color = if (isCompleted) NamedTextColor.GREEN else NamedTextColor.GRAY
        meta.displayName(
            Component.text(if (isCompleted) "✓ " else "  ", color)
                .append(Component.text(achievement.displayName, color, TextDecoration.BOLD))
        )

        // Lore
        val lore = mutableListOf(
            Component.text(""),
            Component.text(achievement.description, NamedTextColor.GRAY, TextDecoration.ITALIC)
        )

        if (isCompleted) {
            lore.add(Component.text(""))
            lore.add(Component.text("✔ Abgeschlossen!", NamedTextColor.GREEN, TextDecoration.BOLD))
            
            // Belohnung anzeigen
            achievement.reward?.let {
                lore.add(Component.text(""))
                lore.add(Component.text("🎁 Belohnung erhalten:", NamedTextColor.GOLD))
                lore.add(Component.text("  (Details im Chat)", NamedTextColor.YELLOW))
            }
        } else {
            lore.add(Component.text(""))
            
            // Progress anzeigen wenn verfügbar
            val progressKey = progressMap.keys.find { it.contains(achievement.name) }
            if (progressKey != null) {
                val current = progressMap[progressKey] ?: 0
                val required = getRequiredProgress(achievement)
                if (required > 0) {
                    val percent = (current * 100 / required).coerceIn(0, 100)
                    lore.add(Component.text("Fortschritt: ", NamedTextColor.AQUA)
                        .append(Component.text("$current / $required", NamedTextColor.WHITE)))
                    lore.add(Component.text(createProgressBar(percent), NamedTextColor.YELLOW))
                    lore.add(Component.text("$percent%", NamedTextColor.GOLD))
                }
            } else {
                lore.add(Component.text("Noch nicht begonnen", NamedTextColor.DARK_GRAY))
            }
        }

        meta.lore(lore)
        item.itemMeta = meta

        return item
    }

    /**
     * Holt erforderlichen Progress für Achievement
     */
    private fun getRequiredProgress(achievement: Achievement): Int {
        val req = achievement.requirement
        return when {
            req is org.bysenom.survivalPlus.achievements.CountRequirement -> req.count
            req is org.bysenom.survivalPlus.achievements.ItemQualityRequirement -> req.count
            else -> 0
        }
    }

    /**
     * Progress Bar
     */
    private fun createProgressBar(percent: Int): String {
        val totalBars = 10
        val filledBars = (percent / 10).coerceIn(0, totalBars)
        val emptyBars = totalBars - filledBars

        return "§a${"█".repeat(filledBars)}§7${"█".repeat(emptyBars)}"
    }

    /**
     * Click Handler
     */
    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val title = event.view.title()
        val plainText = extractPlainText(title)
        
        if (plainText.contains("Erfolge")) {
            event.isCancelled = true

            val player = event.whoClicked as? Player ?: return
            val slot = event.slot

            // Filter-Buttons (Slots 45-48)
            when (slot) {
                45 -> {
                    player.playSound(player.location, org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f)
                    openGUI(player, AchievementFilter.ALL)
                }
                46 -> {
                    player.playSound(player.location, org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f)
                    openGUI(player, AchievementFilter.COMPLETED)
                }
                47 -> {
                    player.playSound(player.location, org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f)
                    openGUI(player, AchievementFilter.IN_PROGRESS)
                }
                48 -> {
                    player.playSound(player.location, org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f)
                    openGUI(player, AchievementFilter.LOCKED)
                }
                53 -> {
                    // Schließen
                    player.closeInventory()
                    player.playSound(player.location, org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f)
                    achievementPages.remove(player)
                }
            }
        }
    }

    /**
     * Cleanup bei Inventory Close
     */
    @EventHandler
    fun onClose(event: org.bukkit.event.inventory.InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        achievementPages.remove(player)
    }

    /**
     * Extrahiert Plain Text
     */
    private fun extractPlainText(component: Component): String {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component)
    }
}
