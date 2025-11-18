package org.bysenom.survivalPlus.models

import org.bukkit.Material

/**
 * Repräsentiert die verschiedenen Reforging-Tiers
 * Ähnlich wie Tierify mit seinen 3 Erzen
 */
enum class ReforgingTier(
    val displayName: String,
    val material: Material,
    val oreName: String,
    val dimension: String,
    val allowedQualities: List<Quality>,
    val cost: Int
) {
    TIER_1(
        "Tier 1 - Kalkstein",
        Material.CALCITE, // Vanilla Material als Platzhalter
        "Kalkstein",
        "Oberwelt",
        listOf(Quality.COMMON, Quality.UNCOMMON, Quality.RARE),
        3
    ),

    TIER_2(
        "Tier 2 - Pyrit",
        Material.RAW_GOLD, // Vanilla Material als Platzhalter
        "Pyrit",
        "Nether",
        listOf(Quality.UNCOMMON, Quality.RARE, Quality.EPIC, Quality.LEGENDARY),
        5
    ),

    TIER_3(
        "Tier 3 - Galena",
        Material.RAW_IRON, // Vanilla Material als Platzhalter
        "Galena",
        "End",
        listOf(Quality.RARE, Quality.EPIC, Quality.LEGENDARY, Quality.MYTHIC),
        7
    );

    /**
     * Prüft ob eine Qualität mit diesem Tier erreichbar ist
     */
    fun canReforgeToQuality(quality: Quality): Boolean {
        return allowedQualities.contains(quality)
    }

    companion object {
        fun fromMaterial(material: Material): ReforgingTier? {
            return entries.find { it.material == material }
        }
    }
}

