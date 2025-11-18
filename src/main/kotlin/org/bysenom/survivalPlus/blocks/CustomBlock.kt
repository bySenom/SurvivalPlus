package org.bysenom.survivalPlus.blocks

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material

/**
 * Enum für alle Custom Blocks im Plugin
 */
enum class CustomBlock(
    val displayName: String,
    val material: Material,
    val modelData: Int? = null,
    val color: TextColor,
    val isBold: Boolean = true
) {
    CUSTOM_ANVIL(
        "⚒ Custom Amboss",
        Material.ANVIL,
        1001,
        TextColor.fromHexString("#FFAA00")!!,
        true
    ),

    REFORGING_STATION(
        "✦ Reforging Station",
        Material.SMITHING_TABLE,
        1002,
        TextColor.fromHexString("#AA00AA")!!,
        true
    ),

    WORLD_TIER_ALTAR(
        "⚔ World Tier Altar",
        Material.LODESTONE,
        1003,
        TextColor.fromHexString("#FF5555")!!,
        true
    );

    /**
     * Gibt den Display-Namen als Adventure Component zurück
     */
    fun getDisplayComponent(): Component {
        return Component.text(displayName)
            .color(color)
            .decoration(TextDecoration.BOLD, if (isBold) TextDecoration.State.TRUE else TextDecoration.State.FALSE)
            .decoration(TextDecoration.ITALIC, false)
    }

    /**
     * Erstellt ein Item für diesen Block
     */
    fun createItem(): org.bukkit.inventory.ItemStack {
        val item = org.bukkit.inventory.ItemStack(material)
        val meta = item.itemMeta ?: return item

        meta.displayName(getDisplayComponent())

        // Custom Model Data setzen
        modelData?.let { meta.setCustomModelData(it) }

        // Lore hinzufügen
        val lore = mutableListOf<Component>()
        lore.add(Component.empty())

        when (this) {
            CUSTOM_ANVIL -> {
                lore.add(Component.text("Ein spezieller Amboss zum Craften")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false))
                lore.add(Component.text("von Custom Items.")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false))
                lore.add(Component.empty())
                lore.add(Component.text("Rechtsklick: ").color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("Custom Crafting GUI").color(NamedTextColor.WHITE)))
                lore.add(Component.empty())
                lore.add(Component.text("Platzierbar!").color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false))
            }
            REFORGING_STATION -> {
                lore.add(Component.text("Eine mächtige Station zum")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false))
                lore.add(Component.text("Reforgen von Items.")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false))
                lore.add(Component.empty())
                lore.add(Component.text("Rechtsklick: ").color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("Reforging GUI").color(NamedTextColor.WHITE)))
                lore.add(Component.empty())
                lore.add(Component.text("Platzierbar!").color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false))
            }
            WORLD_TIER_ALTAR -> {
                lore.add(Component.text("Ein mystischer Altar zur Kontrolle")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false))
                lore.add(Component.text("der Welt-Schwierigkeit.")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false))
                lore.add(Component.empty())
                lore.add(Component.text("Rechtsklick: ").color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("World Tier GUI").color(NamedTextColor.WHITE)))
                lore.add(Component.empty())
                lore.add(Component.text("Höheres Tier = Bessere Belohnungen!").color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false))
                lore.add(Component.text("Platzierbar!").color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false))
            }
        }

        meta.lore(lore)

        // Persistent Data Container Marker
        val pdc = meta.persistentDataContainer
        val key = org.bukkit.NamespacedKey("survivalplus", "custom_block")
        pdc.set(key, org.bukkit.persistence.PersistentDataType.STRING, this.name)

        item.itemMeta = meta
        return item
    }

    companion object {
        /**
         * Erkennt einen Custom Block aus einem ItemStack
         */
        fun fromItem(item: org.bukkit.inventory.ItemStack?): CustomBlock? {
            if (item == null || item.type.isAir) return null

            val meta = item.itemMeta ?: return null
            val pdc = meta.persistentDataContainer
            val key = org.bukkit.NamespacedKey("survivalplus", "custom_block")

            val blockName = pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING) ?: return null

            return try {
                valueOf(blockName)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}

