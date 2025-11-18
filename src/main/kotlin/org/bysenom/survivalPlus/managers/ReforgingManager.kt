package org.bysenom.survivalPlus.managers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.Quality
import org.bysenom.survivalPlus.models.ReforgingTier
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Verwaltet das Reforging-System
 */
class ReforgingManager(private val plugin: SurvivalPlus) {

    private val itemManager = plugin.itemManager

    /**
     * Versucht ein Item mit einem Reforging-Material zu reforgen
     */
    fun reforgeItem(player: Player, item: ItemStack, reforgeMaterial: ItemStack): ReforgingResult {
        // Prüfen ob das Item ein Custom Item ist
        if (!itemManager.isCustomItem(item)) {
            return ReforgingResult.NOT_CUSTOM_ITEM
        }

        // Reforging-Tier ermitteln
        val tier = ReforgingTier.fromMaterial(reforgeMaterial.type)
            ?: return ReforgingResult.INVALID_MATERIAL

        // Prüfen ob genug Material vorhanden ist
        if (reforgeMaterial.amount < tier.cost) {
            return ReforgingResult.insufficient(tier.cost)
        }

        // Zufällige Qualität aus erlaubten Qualitäten wählen
        val newQuality = tier.allowedQualities.random()

        // Item reforgen
        val reforgedItem = itemManager.setQuality(item, newQuality)

        // Material abziehen
        reforgeMaterial.amount -= tier.cost

        // Item im Inventar ersetzen
        val slot = player.inventory.indexOf(item)
        if (slot >= 0) {
            player.inventory.setItem(slot, reforgedItem)
        }

        return ReforgingResult.success(newQuality)
    }

    /**
     * Reforging mit GUI
     */
    fun openReforgingGUI(player: Player, item: ItemStack) {
        plugin.reforgingGUI.open(player, item)
    }
}

/**
 * Ergebnis einer Reforging-Operation
 */
sealed class ReforgingResult {
    data class Success(val newQuality: Quality) : ReforgingResult()
    data class Insufficient(val required: Int) : ReforgingResult()
    object NotCustomItem : ReforgingResult()
    object InvalidMaterial : ReforgingResult()

    companion object {
        fun success(quality: Quality) = Success(quality)
        fun insufficient(required: Int) = Insufficient(required)
        val NOT_CUSTOM_ITEM = NotCustomItem
        val INVALID_MATERIAL = InvalidMaterial
    }

    fun getMessage(): Component {
        return when (this) {
            is Success -> Component.text("Erfolgreich zu ${newQuality.displayName} reforged!")
                .color(newQuality.color)
            is Insufficient -> Component.text("Nicht genug Material! Benötigt: $required")
                .color(NamedTextColor.RED)
            NotCustomItem -> Component.text("Dieses Item kann nicht reforged werden!")
                .color(NamedTextColor.RED)
            InvalidMaterial -> Component.text("Ungültiges Reforging-Material!")
                .color(NamedTextColor.RED)
        }
    }
}

