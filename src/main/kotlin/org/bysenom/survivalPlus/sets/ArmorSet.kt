package org.bysenom.survivalPlus.sets

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bysenom.survivalPlus.models.Quality
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.potion.PotionEffectType

/**
 * Rüstungs-Sets mit speziellen Boni
 */
enum class ArmorSet(
    val displayName: String,
    val description: String,
    val minQuality: Quality,
    val color: TextColor,
    val twoPieceBonus: SetBonus,
    val fourPieceBonus: SetBonus
) {
    // Basis-Sets (für niedrigere Qualitäten)
    GUARDIAN(
        "Wächter-Set",
        "Erhöht Verteidigung und Gesundheit",
        Quality.UNCOMMON,
        NamedTextColor.BLUE,
        SetBonus(
            "Wächter I",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ARMOR, 2.0),
                BonusEffect.AttributeBonus(Attribute.MAX_HEALTH, 4.0)
            )
        ),
        SetBonus(
            "Wächter II",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ARMOR, 4.0),
                BonusEffect.AttributeBonus(Attribute.MAX_HEALTH, 10.0),
                BonusEffect.PotionBonus(PotionEffectType.RESISTANCE, 0)
            )
        )
    ),

    BERSERKER(
        "Berserker-Set",
        "Erhöht Angriffskraft und Geschwindigkeit",
        Quality.RARE,
        NamedTextColor.RED,
        SetBonus(
            "Berserker I",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ATTACK_DAMAGE, 2.0),
                BonusEffect.AttributeBonus(Attribute.MOVEMENT_SPEED, 0.02)
            )
        ),
        SetBonus(
            "Berserker II",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ATTACK_DAMAGE, 5.0),
                BonusEffect.AttributeBonus(Attribute.MOVEMENT_SPEED, 0.05),
                BonusEffect.PotionBonus(PotionEffectType.STRENGTH, 0)
            )
        )
    ),

    ASSASSIN(
        "Assassinen-Set",
        "Erhöht kritische Trefferchance und Geschwindigkeit",
        Quality.EPIC,
        NamedTextColor.DARK_PURPLE,
        SetBonus(
            "Assassine I",
            listOf(
                BonusEffect.AttributeBonus(Attribute.MOVEMENT_SPEED, 0.03),
                BonusEffect.CritChanceBonus(5.0)
            )
        ),
        SetBonus(
            "Assassine II",
            listOf(
                BonusEffect.AttributeBonus(Attribute.MOVEMENT_SPEED, 0.08),
                BonusEffect.CritChanceBonus(15.0),
                BonusEffect.CritDamageBonus(50.0),
                BonusEffect.PotionBonus(PotionEffectType.INVISIBILITY, 0)
            )
        )
    ),

    TANK(
        "Panzer-Set",
        "Maximale Verteidigung und Schadensreduzierung",
        Quality.LEGENDARY,
        NamedTextColor.GRAY,
        SetBonus(
            "Panzer I",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ARMOR, 4.0),
                BonusEffect.AttributeBonus(Attribute.ARMOR_TOUGHNESS, 2.0),
                BonusEffect.AttributeBonus(Attribute.KNOCKBACK_RESISTANCE, 0.2)
            )
        ),
        SetBonus(
            "Panzer II",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ARMOR, 8.0),
                BonusEffect.AttributeBonus(Attribute.ARMOR_TOUGHNESS, 4.0),
                BonusEffect.AttributeBonus(Attribute.KNOCKBACK_RESISTANCE, 0.5),
                BonusEffect.AttributeBonus(Attribute.MAX_HEALTH, 20.0),
                BonusEffect.PotionBonus(PotionEffectType.REGENERATION, 0)
            )
        )
    ),

    ELEMENTAL(
        "Elementar-Set",
        "Beherrsche die Elemente",
        Quality.LEGENDARY,
        NamedTextColor.AQUA,
        SetBonus(
            "Elementar I",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ATTACK_DAMAGE, 3.0),
                BonusEffect.ElementalDamageBonus(25.0)
            )
        ),
        SetBonus(
            "Elementar II",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ATTACK_DAMAGE, 6.0),
                BonusEffect.ElementalDamageBonus(50.0),
                BonusEffect.PotionBonus(PotionEffectType.FIRE_RESISTANCE, 0),
                BonusEffect.AbilityBonus("Elementar-Explosion")
            )
        )
    ),

    GODLIKE(
        "Göttliches Set",
        "Ultimative Macht für die Götter",
        Quality.MYTHIC,
        NamedTextColor.GOLD,
        SetBonus(
            "Göttlich I",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ATTACK_DAMAGE, 5.0),
                BonusEffect.AttributeBonus(Attribute.ARMOR, 5.0),
                BonusEffect.AttributeBonus(Attribute.MAX_HEALTH, 10.0),
                BonusEffect.AttributeBonus(Attribute.MOVEMENT_SPEED, 0.05)
            )
        ),
        SetBonus(
            "Göttlich II",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ATTACK_DAMAGE, 10.0),
                BonusEffect.AttributeBonus(Attribute.ARMOR, 10.0),
                BonusEffect.AttributeBonus(Attribute.MAX_HEALTH, 30.0),
                BonusEffect.AttributeBonus(Attribute.MOVEMENT_SPEED, 0.1),
                BonusEffect.CritChanceBonus(20.0),
                BonusEffect.CritDamageBonus(100.0),
                BonusEffect.PotionBonus(PotionEffectType.REGENERATION, 1),
                BonusEffect.PotionBonus(PotionEffectType.STRENGTH, 1),
                BonusEffect.AbilityBonus("Göttliche Macht")
            )
        )
    ),

    // Boss-Sets (v1.2.0+)
    INFERNO(
        "Inferno-Set",
        "Der Ernter's flammendes Erbe",
        Quality.LEGENDARY,
        TextColor.color(255, 80, 20), // Orange-Red
        SetBonus(
            "Inferno I",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ATTACK_DAMAGE, 3.0),
                BonusEffect.PotionBonus(PotionEffectType.FIRE_RESISTANCE, 0),
                BonusEffect.ElementalDamageBonus(0.15) // +15% Feuer-Schaden
            )
        ),
        SetBonus(
            "Inferno II",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ATTACK_DAMAGE, 6.0),
                BonusEffect.AttributeBonus(Attribute.MAX_HEALTH, 8.0),
                BonusEffect.PotionBonus(PotionEffectType.FIRE_RESISTANCE, 0),
                BonusEffect.ElementalDamageBonus(0.30), // +30% Feuer-Schaden
                BonusEffect.AbilityBonus("flame_aura") // Flammen-Aura (brennt Feinde in Nähe)
            )
        )
    ),

    GLACIAL(
        "Glacial-Set",
        "Frost-Titan's eisige Macht",
        Quality.LEGENDARY,
        TextColor.color(100, 200, 255), // Ice Blue
        SetBonus(
            "Glacial I",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ARMOR, 4.0),
                BonusEffect.PotionBonus(PotionEffectType.SLOWNESS, -1), // Slow Resistance
                BonusEffect.ElementalDamageBonus(0.15) // +15% Ice-Schaden
            )
        ),
        SetBonus(
            "Glacial II",
            listOf(
                BonusEffect.AttributeBonus(Attribute.ARMOR, 8.0),
                BonusEffect.AttributeBonus(Attribute.MAX_HEALTH, 10.0),
                BonusEffect.PotionBonus(PotionEffectType.SLOWNESS, -1), // Slow Immunity
                BonusEffect.ElementalDamageBonus(0.30), // +30% Ice-Schaden
                BonusEffect.AbilityBonus("freeze_chance") // 20% Chance Gegner zu frieren
            )
        )
    );

    companion object {
        /**
         * Gibt alle Sets zurück die für eine Qualität verfügbar sind
         */
        fun getAvailableSets(quality: Quality): List<ArmorSet> {
            return entries.filter { it.minQuality.tier <= quality.tier }
        }
    }
}

/**
 * Set-Bonus mit allen Effekten
 */
data class SetBonus(
    val name: String,
    val effects: List<BonusEffect>
)

/**
 * Verschiedene Arten von Bonus-Effekten
 */
sealed class BonusEffect {
    /**
     * Attribut-Bonus (Vanilla Minecraft Attribute)
     */
    data class AttributeBonus(
        val attribute: Attribute,
        val value: Double,
        val operation: AttributeModifier.Operation = AttributeModifier.Operation.ADD_NUMBER
    ) : BonusEffect()

    /**
     * Potion-Effekt
     */
    data class PotionBonus(
        val effect: PotionEffectType,
        val amplifier: Int,
        val duration: Int = Int.MAX_VALUE // Permanenter Effekt
    ) : BonusEffect()

    /**
     * Kritische Trefferchance
     */
    data class CritChanceBonus(val chance: Double) : BonusEffect()

    /**
     * Kritischer Schaden
     */
    data class CritDamageBonus(val multiplier: Double) : BonusEffect()

    /**
     * Elementar-Schaden Bonus
     */
    data class ElementalDamageBonus(val bonus: Double) : BonusEffect()

    /**
     * Spezielle Fähigkeit
     */
    data class AbilityBonus(val abilityName: String) : BonusEffect()
}

