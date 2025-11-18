package org.bysenom.survivalPlus.enchantments

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bysenom.survivalPlus.models.Quality

/**
 * Custom Enchantments für SurvivalPlus
 * Ähnlich wie Vanilla-Enchantments aber mit eigenen Effekten
 */
enum class CustomEnchantment(
    val displayName: String,
    val description: String,
    val maxLevel: Int,
    val minQuality: Quality,
    val color: TextColor,
    val applicableItems: List<ItemType>
) {
    // Waffen-Enchantments
    LIFESTEAL(
        "Lebensraub",
        "Heile dich für einen Prozentsatz des verursachten Schadens",
        3,
        Quality.RARE,
        NamedTextColor.RED,
        listOf(ItemType.SWORD, ItemType.AXE)
    ),

    EXPLOSIVE(
        "Explosiv",
        "Chance auf Explosion beim Treffer",
        2,
        Quality.EPIC,
        NamedTextColor.GOLD,
        listOf(ItemType.SWORD, ItemType.AXE)
    ),

    SOUL_BOUND(
        "Seelengebunden",
        "Item bleibt beim Tod erhalten",
        1,
        Quality.LEGENDARY,
        NamedTextColor.LIGHT_PURPLE,
        listOf(ItemType.SWORD, ItemType.AXE, ItemType.PICKAXE, ItemType.ARMOR)
    ),

    THUNDER_STRIKE(
        "Blitzschlag",
        "Beschwört Blitze auf getroffene Gegner",
        2,
        Quality.LEGENDARY,
        NamedTextColor.AQUA,
        listOf(ItemType.SWORD)
    ),

    VAMPIRE(
        "Vampirismus",
        "Absorbiere Leben von Gegnern",
        3,
        Quality.EPIC,
        NamedTextColor.DARK_RED,
        listOf(ItemType.SWORD, ItemType.AXE)
    ),

    // Rüstungs-Enchantments
    DIVINE_PROTECTION(
        "Goettlicher Schutz",
        "Reduziere allen eingehenden Schaden",
        5,
        Quality.MYTHIC,
        NamedTextColor.YELLOW,
        listOf(ItemType.ARMOR)
    ),

    THORNS_PLUS(
        "Dornen+",
        "Reflektiere Schaden zurück zum Angreifer",
        3,
        Quality.RARE,
        NamedTextColor.DARK_GREEN,
        listOf(ItemType.ARMOR)
    ),

    SPEED_BOOST(
        "Geschwindigkeitsschub",
        "Erhöhe deine Bewegungsgeschwindigkeit",
        2,
        Quality.UNCOMMON,
        NamedTextColor.WHITE,
        listOf(ItemType.ARMOR)
    ),

    // Werkzeug-Enchantments
    AUTO_SMELT(
        "Auto-Schmelze",
        "Schmelze Erze automatisch beim Abbauen",
        1,
        Quality.EPIC,
        NamedTextColor.GOLD,
        listOf(ItemType.PICKAXE)
    ),

    VEIN_MINER(
        "Erzader-Abbau",
        "Baue ganze Erzadern auf einmal ab",
        1,
        Quality.LEGENDARY,
        NamedTextColor.DARK_PURPLE,
        listOf(ItemType.PICKAXE)
    ),

    TIMBER(
        "Holzfäller",
        "Fälle ganze Bäume auf einmal",
        1,
        Quality.RARE,
        NamedTextColor.GREEN,
        listOf(ItemType.AXE)
    ),

    // Universal-Enchantments
    UNBREAKABLE(
        "Unzerstörbar",
        "Item verliert keine Haltbarkeit",
        1,
        Quality.MYTHIC,
        NamedTextColor.AQUA,
        listOf(ItemType.SWORD, ItemType.AXE, ItemType.PICKAXE, ItemType.ARMOR)
    );

    /**
     * Prüft ob das Enchantment auf einen Item-Typ anwendbar ist
     */
    fun isApplicable(type: ItemType): Boolean {
        return applicableItems.contains(type)
    }

    /**
     * Gibt den Effekt-Wert für ein bestimmtes Level zurück
     */
    fun getEffectValue(level: Int): Double {
        require(level in 1..maxLevel) { "Level muss zwischen 1 und $maxLevel sein" }

        return when (this) {
            LIFESTEAL -> 0.05 * level // 5%, 10%, 15%
            EXPLOSIVE -> 0.10 * level // 10%, 20%
            VAMPIRE -> 0.03 * level // 3%, 6%, 9%
            DIVINE_PROTECTION -> 0.04 * level // 4%, 8%, 12%, 16%, 20%
            THORNS_PLUS -> 0.5 * level // 50%, 100%, 150%
            SPEED_BOOST -> 0.1 * level // 10%, 20%
            THUNDER_STRIKE -> 0.15 * level // 15%, 30%
            else -> level.toDouble() // Für andere: einfach das Level
        }
    }

    companion object {
        /**
         * Gibt alle Enchantments zurück die auf einen Item-Typ anwendbar sind
         */
        fun getApplicableEnchantments(type: ItemType, quality: Quality): List<CustomEnchantment> {
            return entries.filter {
                it.isApplicable(type) && quality.tier >= it.minQuality.tier
            }
        }
    }
}

/**
 * Item-Typen für Enchantment-Anwendung
 */
enum class ItemType {
    SWORD,
    AXE,
    PICKAXE,
    SHOVEL,
    HOE,
    ARMOR,
    BOW,
    CROSSBOW;

    companion object {
        fun fromMaterial(material: org.bukkit.Material): ItemType? {
            return when {
                material.name.contains("SWORD") -> SWORD
                material.name.contains("AXE") -> AXE
                material.name.contains("PICKAXE") -> PICKAXE
                material.name.contains("SHOVEL") -> SHOVEL
                material.name.contains("HOE") -> HOE
                material.name.contains("HELMET") || material.name.contains("CHESTPLATE") ||
                material.name.contains("LEGGINGS") || material.name.contains("BOOTS") -> ARMOR
                material.name.contains("BOW") && !material.name.contains("CROSS") -> BOW
                material.name.contains("CROSSBOW") -> CROSSBOW
                else -> null
            }
        }
    }
}

