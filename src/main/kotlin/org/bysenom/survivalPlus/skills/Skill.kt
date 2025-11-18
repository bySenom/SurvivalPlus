package org.bysenom.survivalPlus.skills

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

enum class Skill(
    val displayName: String,
    val description: String,
    val color: TextColor,
    val maxLevel: Int = 100
) {
    COMBAT("Kampf", "Verbessert Kampffähigkeiten", NamedTextColor.RED),
    MINING("Bergbau", "Erhöht Abbaugeschwindigkeit", NamedTextColor.GRAY),
    FARMING("Landwirtschaft", "Verbessert Ernte-Erträge", NamedTextColor.GREEN),
    FISHING("Angeln", "Erhöht Angel-Geschwindigkeit", NamedTextColor.AQUA),
    ENCHANTING("Verzauberung", "Bessere Enchantments", NamedTextColor.LIGHT_PURPLE),
    CRAFTING("Handwerk", "Schnelleres Craften", NamedTextColor.GOLD),
    DEFENSE("Verteidigung", "Reduziert Schaden", NamedTextColor.BLUE),
    AGILITY("Beweglichkeit", "Erhöht Geschwindigkeit", NamedTextColor.YELLOW);

    fun getRequiredXP(currentLevel: Int): Int {
        val base = 100
        val xp = base * Math.pow(currentLevel.toDouble(), 1.5)
        return xp.toInt()
    }

    fun getBonusForLevel(level: Int): Double {
        return when (this) {
            COMBAT -> level * 0.5
            MINING -> level * 0.3
            FARMING -> level * 0.4
            FISHING -> level * 0.5
            ENCHANTING -> level * 0.2
            CRAFTING -> level * 0.3
            DEFENSE -> level * 0.3
            AGILITY -> level * 0.2
        }
    }

    fun getRewardsForLevel(level: Int): List<String> {
        val rewards = mutableListOf<String>()
        rewards.add("+${String.format("%.1f", getBonusForLevel(level))}% ${displayName} Bonus")

        if (level % 10 == 0) {
            rewards.add("Spezielle Fähigkeit freigeschaltet!")
        }

        when (level) {
            25 -> rewards.add("Titel: Anfänger $displayName")
            50 -> rewards.add("Titel: Fortgeschrittener $displayName")
            75 -> rewards.add("Titel: Experte $displayName")
            100 -> rewards.add("Titel: Meister $displayName")
        }

        return rewards
    }
}

