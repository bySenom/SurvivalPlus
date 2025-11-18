package org.bysenom.survivalPlus.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.skills.Skill
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Skills GUI - Zeigt alle 8 Skills mit Progress
 */
class SkillsGUI(private val plugin: SurvivalPlus) : Listener {

    /**
     * Öffnet das Skills GUI für einen Spieler
     */
    fun openGUI(player: Player) {
        val inventory = Bukkit.createInventory(null, 27, Component.text("✨ Deine Skills", NamedTextColor.GOLD, TextDecoration.BOLD))

        // Hole alle Skills des Spielers
        val playerSkills = plugin.skillManager.getAllSkills(player)

        // Positionierung im GUI (2 Reihen a 4 Skills)
        val slots = listOf(10, 11, 12, 13, 16, 19, 20, 21)

        Skill.entries.forEachIndexed { index, skill ->
            val skillData = playerSkills[skill]
            if (skillData != null) {
                val item = createSkillItem(skill, skillData.level, skillData.xp)
                inventory.setItem(slots[index], item)
            }
        }

        // Info-Item
        val infoItem = ItemStack(Material.BOOK)
        val infoMeta = infoItem.itemMeta
        infoMeta.displayName(Component.text("ℹ Skills Info", NamedTextColor.AQUA, TextDecoration.BOLD))
        infoMeta.lore(listOf(
            Component.text(""),
            Component.text("Levele deine Skills durch:", NamedTextColor.GRAY),
            Component.text("  • Kämpfen → Combat", NamedTextColor.WHITE),
            Component.text("  • Abbauen → Mining", NamedTextColor.WHITE),
            Component.text("  • Farmen → Farming", NamedTextColor.WHITE),
            Component.text("  • Angeln → Fishing", NamedTextColor.WHITE),
            Component.text("  • Verzaubern → Enchanting", NamedTextColor.WHITE),
            Component.text("  • Craften → Crafting", NamedTextColor.WHITE),
            Component.text("  • Schaden nehmen → Defense", NamedTextColor.WHITE),
            Component.text("  • Laufen/Springen → Agility", NamedTextColor.WHITE),
            Component.text(""),
            Component.text("Höhere Level = Bessere Boni!", NamedTextColor.GOLD)
        ))
        infoItem.itemMeta = infoMeta
        inventory.setItem(22, infoItem)

        // Schließen-Button
        val closeItem = ItemStack(Material.BARRIER)
        val closeMeta = closeItem.itemMeta
        closeMeta.displayName(Component.text("❌ Schließen", NamedTextColor.RED, TextDecoration.BOLD))
        closeItem.itemMeta = closeMeta
        inventory.setItem(26, closeItem)

        player.openInventory(inventory)
    }

    /**
     * Erstellt ein Item für einen Skill
     */
    private fun createSkillItem(skill: Skill, level: Int, xp: Int): ItemStack {
        val material = when (skill) {
            Skill.COMBAT -> Material.NETHERITE_SWORD
            Skill.MINING -> Material.DIAMOND_PICKAXE
            Skill.FARMING -> Material.GOLDEN_HOE
            Skill.FISHING -> Material.FISHING_ROD
            Skill.ENCHANTING -> Material.ENCHANTING_TABLE
            Skill.CRAFTING -> Material.CRAFTING_TABLE
            Skill.DEFENSE -> Material.SHIELD
            Skill.AGILITY -> Material.FEATHER
        }

        val item = ItemStack(material)
        val meta = item.itemMeta

        // Name mit Level
        meta.displayName(
            Component.text("${skill.displayName} ", skill.color, TextDecoration.BOLD)
                .append(Component.text("[Lvl $level]", NamedTextColor.YELLOW))
        )

        // Lore mit Progress Bar und Stats
        val requiredXP = skill.getRequiredXP(level)
        val progress = (xp.toDouble() / requiredXP * 100).toInt().coerceIn(0, 100)
        val progressBar = createProgressBar(progress)
        val bonus = skill.getBonusForLevel(level)

        val lore = mutableListOf(
            Component.text(""),
            Component.text(skill.description, NamedTextColor.GRAY, TextDecoration.ITALIC),
            Component.text(""),
            Component.text("Level: ", NamedTextColor.GRAY)
                .append(Component.text("$level / 100", NamedTextColor.WHITE)),
            Component.text("XP: ", NamedTextColor.GRAY)
                .append(Component.text("$xp / $requiredXP", NamedTextColor.WHITE)),
            Component.text(""),
            Component.text(progressBar, NamedTextColor.GREEN),
            Component.text("$progress% bis Level ${level + 1}", NamedTextColor.GOLD),
            Component.text(""),
            Component.text("Bonus: ", NamedTextColor.AQUA)
                .append(Component.text("+${String.format("%.1f", bonus)}%", NamedTextColor.GREEN))
        )

        // Milestone-Belohnungen anzeigen
        if (level >= 25) {
            lore.add(Component.text(""))
            lore.add(Component.text("🏆 Titel: ", NamedTextColor.GOLD))
            when {
                level >= 100 -> lore.add(Component.text("  Meister ${skill.displayName}", NamedTextColor.LIGHT_PURPLE))
                level >= 75 -> lore.add(Component.text("  Experte ${skill.displayName}", NamedTextColor.BLUE))
                level >= 50 -> lore.add(Component.text("  Fortgeschrittener ${skill.displayName}", NamedTextColor.GREEN))
                level >= 25 -> lore.add(Component.text("  Anfänger ${skill.displayName}", NamedTextColor.YELLOW))
            }
        }

        meta.lore(lore)
        item.itemMeta = meta

        return item
    }

    /**
     * Erstellt eine Progress Bar
     */
    private fun createProgressBar(percent: Int): String {
        val totalBars = 20
        val filledBars = (percent / 5).coerceIn(0, totalBars)
        val emptyBars = totalBars - filledBars

        return "§a${"█".repeat(filledBars)}§7${"█".repeat(emptyBars)}"
    }

    /**
     * Verhindert Item-Manipulation im GUI
     */
    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val title = event.view.title()
        
        if (title is Component) {
            val plainText = (title as? Component)?.let { extractPlainText(it) } ?: ""
            
            if (plainText.contains("Deine Skills")) {
                event.isCancelled = true

                val clickedItem = event.currentItem ?: return
                val player = event.whoClicked as? Player ?: return

                // Schließen-Button
                if (clickedItem.type == Material.BARRIER) {
                    player.closeInventory()
                    player.playSound(player.location, org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f)
                }
            }
        }
    }

    /**
     * Extrahiert Plain Text aus Component
     */
    private fun extractPlainText(component: Component): String {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component)
    }
}
