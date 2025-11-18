package org.bysenom.survivalPlus.achievements

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bysenom.survivalPlus.models.Quality
import org.bysenom.survivalPlus.skills.Skill
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Achievement System
 * Verfolgt Spieler-Errungenschaften
 */
enum class Achievement(
    val displayName: String,
    val description: String,
    val icon: Material,
    val requirement: AchievementRequirement,
    val reward: AchievementReward? = null,
    val hidden: Boolean = false
) {
    // Erste Schritte
    FIRST_CUSTOM_ITEM(
        "Erster Anfang",
        "Erhalte dein erstes Custom Item",
        Material.IRON_SWORD,
        ItemQualityRequirement(Quality.COMMON, 1)
    ),
    
    FIRST_RARE(
        "Seltener Fund",
        "Erhalte ein Rare Item",
        Material.DIAMOND_SWORD,
        ItemQualityRequirement(Quality.RARE, 1)
    ),
    
    FIRST_MYTHIC(
        "Legendärer Moment",
        "Erhalte dein erstes Mythic Item",
        Material.NETHERITE_SWORD,
        ItemQualityRequirement(Quality.MYTHIC, 1),
        ItemReward(Material.DIAMOND, 10)
    ),
    
    // Reforging
    FIRST_REFORGE(
        "Neu Geschmiedet",
        "Reforge dein erstes Item",
        Material.ANVIL,
        CountRequirement("reforge", 1)
    ),
    
    REFORGE_MASTER(
        "Reforge Meister",
        "Reforge 100 Items",
        Material.ANVIL,
        CountRequirement("reforge", 100),
        ItemReward(Material.DIAMOND_BLOCK, 5)
    ),
    
    // Combat
    KILL_SPECIAL_MOB(
        "Besondere Beute",
        "Töte einen Special Mob",
        Material.ZOMBIE_HEAD,
        CountRequirement("special_mob_kills", 1)
    ),
    
    KILL_BUTCHER(
        "Der Schlachter fällt",
        "Besiege The Butcher",
        Material.WITHER_SKELETON_SKULL,
        CountRequirement("butcher_kills", 1),
        ItemReward(Material.NETHERITE_INGOT, 3)
    ),
    
    // Skills
    MAX_SKILL(
        "Meisterschaft",
        "Erreiche Level 50 in einem Skill",
        Material.EXPERIENCE_BOTTLE,
        SkillLevelRequirement(null, 50)
    ),
    
    ALL_SKILLS_10(
        "Vielseitig",
        "Erreiche Level 10 in allen Skills",
        Material.ENCHANTED_BOOK,
        AllSkillsRequirement(10)
    ),
    
    // World Tier
    REACH_TIER_3(
        "Höhere Gefahr",
        "Erreiche World Tier 3 (Epic)",
        Material.BEACON,
        WorldTierRequirement(3)
    ),
    
    REACH_TIER_5(
        "Mythische Herausforderung",
        "Erreiche World Tier 5 (Mythic)",
        Material.NETHER_STAR,
        WorldTierRequirement(5),
        ItemReward(Material.DIAMOND_BLOCK, 10)
    ),
    
    // Collection
    COLLECT_ALL_SETS(
        "Vollständige Sammlung",
        "Erhalte alle 6 Armor Sets",
        Material.NETHERITE_CHESTPLATE,
        SetCollectionRequirement(6),
        hidden = true
    ),
    
    // Hidden Achievements
    FIND_SHRINE(
        "Heiliger Ort",
        "Entdecke einen World Tier Shrine",
        Material.END_CRYSTAL,
        CountRequirement("shrines_found", 1),
        hidden = true
    );

    /**
     * Prüft ob der Spieler das Achievement erfüllt
     */
    fun check(player: Player): Boolean {
        return requirement.check(player)
    }

    /**
     * Gibt dem Spieler die Belohnung
     */
    fun grantReward(player: Player) {
        reward?.grant(player)
    }

    /**
     * Erstellt die Achievement-Benachrichtigung
     */
    fun createNotification(): Component {
        return Component.text("🏆 Achievement Freigeschaltet: ")
            .color(NamedTextColor.GOLD)
            .decorate(TextDecoration.BOLD)
            .append(Component.text(displayName)
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.BOLD, false))
    }
}

/**
 * Basis-Interface für Achievement-Anforderungen
 */
interface AchievementRequirement {
    fun check(player: Player): Boolean
}

/**
 * Item-Qualität Anforderung
 */
data class ItemQualityRequirement(
    val quality: Quality,
    val count: Int
) : AchievementRequirement {
    override fun check(player: Player): Boolean {
        // Wird vom AchievementManager geprüft
        return false
    }
}

/**
 * Zähler-Anforderung (generisch)
 */
data class CountRequirement(
    val key: String,
    val count: Int
) : AchievementRequirement {
    override fun check(player: Player): Boolean {
        // Wird vom AchievementManager geprüft
        return false
    }
}

/**
 * Skill-Level Anforderung
 */
data class SkillLevelRequirement(
    val skill: Skill?,
    val level: Int
) : AchievementRequirement {
    override fun check(player: Player): Boolean {
        // Wird vom AchievementManager geprüft
        return false
    }
}

/**
 * Alle Skills Anforderung
 */
data class AllSkillsRequirement(
    val minLevel: Int
) : AchievementRequirement {
    override fun check(player: Player): Boolean {
        return false
    }
}

/**
 * World Tier Anforderung
 */
data class WorldTierRequirement(
    val tier: Int
) : AchievementRequirement {
    override fun check(player: Player): Boolean {
        return false
    }
}

/**
 * Set-Collection Anforderung
 */
data class SetCollectionRequirement(
    val count: Int
) : AchievementRequirement {
    override fun check(player: Player): Boolean {
        return false
    }
}

/**
 * Basis-Interface für Achievement-Belohnungen
 */
interface AchievementReward {
    fun grant(player: Player)
}

/**
 * Item-Belohnung
 */
data class ItemReward(
    val material: Material,
    val amount: Int
) : AchievementReward {
    override fun grant(player: Player) {
        val item = org.bukkit.inventory.ItemStack(material, amount)
        player.inventory.addItem(item)
        
        player.sendMessage(Component.text("✓ Belohnung erhalten: $amount x ${material.name}")
            .color(NamedTextColor.GREEN))
    }
}
