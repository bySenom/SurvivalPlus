package org.bysenom.survivalPlus.worldtier

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

/**
 * World Tier System (ähnlich Diablo's Schwierigkeitsgrade)
 */
enum class WorldTier(
    val tier: Int,
    val displayName: String,
    val color: TextColor,
    val mobHealthMultiplier: Double,
    val mobDamageMultiplier: Double,
    val dropQualityBoost: Int,
    val specialMobChance: Double
) {
    NORMAL(
        1,
        "Normal",
        TextColor.fromHexString("#AAAAAA")!!,
        1.0,
        1.0,
        0,
        0.0
    ),

    HEROIC(
        2,
        "Heroic",
        TextColor.fromHexString("#55FF55")!!,
        1.5,
        1.25,
        1,
        5.0
    ),

    EPIC(
        3,
        "Epic",
        TextColor.fromHexString("#AA00AA")!!,
        2.0,
        1.5,
        2,
        10.0
    ),

    LEGENDARY(
        4,
        "Legendary",
        TextColor.fromHexString("#FFAA00")!!,
        3.0,
        2.0,
        3,
        15.0
    ),

    MYTHIC(
        5,
        "Mythic",
        TextColor.fromHexString("#FF5555")!!,
        5.0,
        3.0,
        4,
        25.0
    );

    /**
     * Gibt den Display-Namen als Component zurück
     */
    fun getDisplayComponent(): Component {
        return Component.text(displayName).color(color)
    }

    /**
     * Gibt eine Beschreibung des Tiers zurück
     */
    fun getDescription(): List<Component> {
        return listOf(
            Component.text("Stufe: $tier").color(color),
            Component.empty(),
            Component.text("Mob-Leben: ${(mobHealthMultiplier * 100).toInt()}%").color(TextColor.fromHexString("#FFAA00")!!),
            Component.text("Mob-Schaden: ${(mobDamageMultiplier * 100).toInt()}%").color(TextColor.fromHexString("#FF5555")!!),
            Component.text("Qualitäts-Boost: +$dropQualityBoost").color(TextColor.fromHexString("#55FF55")!!),
            Component.text("Special Mob Chance: ${specialMobChance.toInt()}%").color(TextColor.fromHexString("#AA00AA")!!)
        )
    }

    companion object {
        /**
         * Gibt das World-Tier für eine Tier-Nummer zurück
         */
        fun fromTier(tier: Int): WorldTier {
            return entries.find { it.tier == tier } ?: NORMAL
        }

        /**
         * Gibt das nächste Tier zurück
         */
        fun WorldTier.next(): WorldTier? {
            return entries.find { it.tier == this.tier + 1 }
        }

        /**
         * Gibt das vorherige Tier zurück
         */
        fun WorldTier.previous(): WorldTier? {
            return entries.find { it.tier == this.tier - 1 }
        }
    }
}

