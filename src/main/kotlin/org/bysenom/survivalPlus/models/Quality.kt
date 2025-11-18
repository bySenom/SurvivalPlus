package org.bysenom.survivalPlus.models

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

enum class Quality(
    val displayName: String,
    val color: TextColor,
    val weight: Double,
    val statMultiplier: Double,
    val tier: Int
) {
    COMMON("Gewoehnlich", NamedTextColor.WHITE, 40.0, 1.0, 1),
    UNCOMMON("Ungewoehnlich", NamedTextColor.GREEN, 30.0, 1.2, 2),
    RARE("Selten", NamedTextColor.BLUE, 15.0, 1.5, 3),
    EPIC("Episch", NamedTextColor.DARK_PURPLE, 10.0, 2.0, 4),
    LEGENDARY("Legendaer", NamedTextColor.GOLD, 4.0, 2.5, 5),
    MYTHIC("Mythisch", NamedTextColor.DARK_RED, 1.0, 3.0, 6);

    companion object {
        fun random(): Quality {
            val totalWeight = entries.sumOf { it.weight }
            var random = Math.random() * totalWeight

            for (quality in entries) {
                random -= quality.weight
                if (random <= 0) {
                    return quality
                }
            }
            return COMMON
        }

        /**
         * Gibt eine zufällige Qualität mit Boost zurück
         * Boost verschiebt die Wahrscheinlichkeit zu höheren Qualitäten
         */
        fun randomWithBoost(boost: Int): Quality {
            if (boost <= 0) return random()

            // Erhöhe die Minimal-Qualität basierend auf Boost
            val minTier = (boost - 1).coerceIn(0, 5)
            val eligibleQualities = entries.filter { it.tier >= minTier }

            if (eligibleQualities.isEmpty()) return MYTHIC

            val totalWeight = eligibleQualities.sumOf { it.weight }
            var random = Math.random() * totalWeight

            for (quality in eligibleQualities) {
                random -= quality.weight
                if (random <= 0) {
                    return quality
                }
            }

            return eligibleQualities.last()
        }

        fun fromName(name: String): Quality? {
            return entries.find { it.name.equals(name, ignoreCase = true) }
        }
    }
}

