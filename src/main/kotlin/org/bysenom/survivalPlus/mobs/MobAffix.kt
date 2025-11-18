package org.bysenom.survivalPlus.mobs

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * Special Mob Affixe (Diablo-Style)
 */
enum class MobAffix(
    val displayName: String,
    val color: TextColor,
    val healthMultiplier: Double,
    val damageMultiplier: Double,
    val speedMultiplier: Double,
    val abilities: List<MobAbility>
) {
    BERSERKER(
        "Berserker",
        TextColor.fromHexString("#FF5555")!!,
        1.5,
        2.0,
        1.3,
        listOf(MobAbility.FIRE_AURA, MobAbility.KNOCKBACK_RESISTANCE)
    ),

    FROSTWARDEN(
        "Frostwächter",
        TextColor.fromHexString("#55FFFF")!!,
        2.0,
        1.3,
        0.8,
        listOf(MobAbility.FREEZE_ATTACK, MobAbility.ICE_ARMOR)
    ),

    STORMBRINGER(
        "Blitzrufer",
        TextColor.fromHexString("#FFFF55")!!,
        1.2,
        1.5,
        1.5,
        listOf(MobAbility.LIGHTNING_STRIKE, MobAbility.TELEPORT)
    ),

    VENOMOUS(
        "Giftschlund",
        TextColor.fromHexString("#55FF55")!!,
        1.3,
        1.2,
        1.0,
        listOf(MobAbility.POISON_CLOUD, MobAbility.WITHER_AURA)
    ),

    VAMPIRIC(
        "Vampir",
        TextColor.fromHexString("#AA0000")!!,
        1.8,
        1.4,
        1.2,
        listOf(MobAbility.LIFESTEAL, MobAbility.BLOOD_SHIELD)
    ),

    SHADOW(
        "Schatten",
        TextColor.fromHexString("#555555")!!,
        1.0,
        2.5,
        2.0,
        listOf(MobAbility.INVISIBILITY, MobAbility.CRITICAL_HITS)
    ),

    MOLTEN(
        "Moloch",
        TextColor.fromHexString("#FFAA00")!!,
        3.0,
        1.8,
        0.6,
        listOf(MobAbility.EXPLOSIVE_DEATH, MobAbility.FIRE_RESISTANCE)
    );

    /**
     * Gibt den Display-Namen als Component zurück
     */
    fun getDisplayComponent(): Component {
        return Component.text(displayName).color(color)
    }

    /**
     * Gibt eine formatierte Name-Komponente mit Präfix zurück
     */
    fun getNameWithPrefix(baseName: String): Component {
        return Component.text("$displayName ").color(color)
            .append(Component.text(baseName).color(TextColor.fromHexString("#FFFFFF")!!))
    }

    companion object {
        /**
         * Gibt einen zufälligen Affix zurück
         */
        fun random(): MobAffix {
            return entries.random()
        }
    }
}

/**
 * Fähigkeiten für Special Mobs
 */
enum class MobAbility {
    // Feuer
    FIRE_AURA,
    FIRE_RESISTANCE,
    EXPLOSIVE_DEATH,

    // Eis/Frost
    FREEZE_ATTACK,
    ICE_ARMOR,

    // Blitz
    LIGHTNING_STRIKE,
    TELEPORT,

    // Gift/Wither
    POISON_CLOUD,
    WITHER_AURA,

    // Vampir
    LIFESTEAL,
    BLOOD_SHIELD,

    // Schatten
    INVISIBILITY,
    CRITICAL_HITS,

    // Defensive
    KNOCKBACK_RESISTANCE,
    REGENERATION;

    /**
     * Wendet die Fähigkeit auf ein Entity an (passive Effekte)
     */
    fun applyPassive(entity: LivingEntity) {
        when (this) {
            FIRE_RESISTANCE -> entity.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, Int.MAX_VALUE, 0, false, false))
            KNOCKBACK_RESISTANCE -> entity.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, Int.MAX_VALUE, 1, false, false))
            ICE_ARMOR -> entity.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, Int.MAX_VALUE, 0, false, false))
            REGENERATION -> entity.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, Int.MAX_VALUE, 0, false, false))
            INVISIBILITY -> {
                // Nur zu 50% unsichtbar (sonst zu schwer)
                if (Math.random() < 0.5) {
                    entity.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false))
                }
            }
            else -> {} // Aktive Fähigkeiten werden im Listener behandelt
        }
    }
}

