package org.bysenom.survivalPlus.models

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

/**
 * Repräsentiert ein Custom Item mit Qualität und Stats
 */
data class CustomItem(
    val baseItem: Material,
    val quality: Quality,
    val stats: MutableMap<String, Double> = mutableMapOf()
) {
    companion object {
        const val QUALITY_KEY = "survivalplus_quality"
        const val STAT_PREFIX = "survivalplus_stat_"
    }

    /**
     * Erstellt einen ItemStack mit allen notwendigen Daten
     */
    fun toItemStack(): ItemStack {
        val item = ItemStack(baseItem)
        val meta = item.itemMeta ?: return item

        // Name mit Qualitäts-Farbe
        meta.displayName(
            Component.text(getItemName())
                .color(quality.color)
                .decoration(TextDecoration.ITALIC, false)
        )

        // Lore mit Stats
        val lore = mutableListOf<Component>()
        lore.add(Component.text("Qualität: ${quality.displayName}")
            .color(quality.color)
            .decoration(TextDecoration.ITALIC, false))

        if (stats.isNotEmpty()) {
            lore.add(Component.empty())
            stats.forEach { (stat, value) ->
                lore.add(Component.text("$stat: +${String.format("%.1f", value)}")
                    .decoration(TextDecoration.ITALIC, false))
            }
        }

        meta.lore(lore)

        // Persistente Daten speichern
        val container = meta.persistentDataContainer
        // Qualität speichern (als String)
        // Stats speichern
        stats.forEach { (stat, value) ->
            // Hier würden wir die Stats speichern wenn wir NamespacedKeys hätten
        }

        item.itemMeta = meta
        return item
    }

    private fun getItemName(): String {
        return baseItem.name.replace("_", " ").lowercase()
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }

    /**
     * Wendet den Qualitäts-Multiplikator auf die Stats an
     */
    fun applyQualityMultiplier() {
        stats.replaceAll { _, value -> value * quality.statMultiplier }
    }
}

