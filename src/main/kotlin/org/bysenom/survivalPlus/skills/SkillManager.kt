package org.bysenom.survivalPlus.skills

import net.kyori.adventure.text.Component
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.entity.Player
import java.util.UUID

class SkillManager(private val plugin: SurvivalPlus) {

    // Player UUID -> Skill -> SkillData
    private val playerSkills = mutableMapOf<UUID, MutableMap<Skill, SkillData>>()

    /**
     * Gebe einem Spieler XP für einen Skill
     */
    fun addXP(player: Player, skill: Skill, amount: Int) {
        val skillData = getSkillData(player, skill)
        val oldLevel = skillData.level

        skillData.xp += amount

        // Level-Up prüfen
        var leveledUp = false
        while (skillData.xp >= skill.getRequiredXP(skillData.level) && skillData.level < skill.maxLevel) {
            skillData.xp -= skill.getRequiredXP(skillData.level)
            skillData.level++
            leveledUp = true
        }

        if (leveledUp) {
            handleLevelUp(player, skill, oldLevel, skillData.level)
        } else {
            // Zeige XP-Fortschritt
            showXPGain(player, skill, amount, skillData)
        }
    }

    /**
     * Handle Level-Up (Public für SkillDataManager)
     */
    fun handleLevelUp(player: Player, skill: Skill, oldLevel: Int, newLevel: Int) {
        // Message
        player.sendMessage(
            Component.text("⭐ LEVEL UP! ⭐")
                .color(skill.color)
        )
        player.sendMessage(
            Component.text("${skill.displayName}: $oldLevel → $newLevel")
                .color(skill.color)
        )

        // Title
        plugin.messageManager.showSkillLevelUp(player, skill, newLevel)

        // Sound
        player.playSound(player.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f)

        // Partikel
        player.world.spawnParticle(
            org.bukkit.Particle.FIREWORK,
            player.location.add(0.0, 1.0, 0.0),
            50,
            0.5, 1.0, 0.5,
            0.1
        )

        // Belohnungen
        val rewards = skill.getRewardsForLevel(newLevel)
        rewards.forEach { reward ->
            player.sendMessage(
                Component.text("  $reward")
                    .color(skill.color)
            )
        }

        // Boss Bar für Progress
        val skillData = getSkillData(player, skill)
        plugin.progressBarManager.showSkillProgress(
            player,
            skill.displayName,
            skillData.xp,
            skill.getRequiredXP(newLevel)
        )
    }

    /**
     * Zeige XP-Gewinn ohne Level-Up
     */
    private fun showXPGain(player: Player, skill: Skill, xpGained: Int, skillData: SkillData) {
        player.sendActionBar(
            Component.text("${skill.displayName}: +${xpGained} XP")
                .color(skill.color)
        )
    }

    /**
     * Hole Skill-Daten eines Spielers
     */
    fun getSkillData(player: Player, skill: Skill): SkillData {
        val playerSkillMap = playerSkills.getOrPut(player.uniqueId) { mutableMapOf() }
        return playerSkillMap.getOrPut(skill) { SkillData(skill) }
    }

    /**
     * Hole Skill-Level eines Spielers
     */
    fun getSkillLevel(player: Player, skill: Skill): Int {
        return getSkillData(player, skill).level
    }

    /**
     * Hole Skill-Bonus eines Spielers
     */
    fun getSkillBonus(player: Player, skill: Skill): Double {
        val level = getSkillLevel(player, skill)
        return skill.getBonusForLevel(level)
    }

    /**
     * Hole alle Skills eines Spielers
     */
    fun getAllSkills(player: Player): Map<Skill, SkillData> {
        return playerSkills.getOrPut(player.uniqueId) {
            Skill.entries.associateWith { SkillData(it) }.toMutableMap()
        }
    }

    /**
     * Reset Skills eines Spielers (für Testing)
     */
    fun resetSkills(player: Player) {
        playerSkills.remove(player.uniqueId)
        player.sendMessage(
            Component.text("Alle Skills wurden zurückgesetzt!")
                .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW)
        )
    }

    /**
     * Cleanup beim Logout
     */
    fun cleanup(player: Player) {
        // Skills werden gecacht, kein Cleanup nötig
        // In Zukunft: Speichern in Datenbank
    }
}

/**
 * Skill-Daten für einen Spieler
 */
data class SkillData(
    val skill: Skill,
    var level: Int = 1,
    var xp: Int = 0
) {
    /**
     * Fortschritt zum nächsten Level (0.0 - 1.0)
     */
    fun getProgress(): Double {
        val required = skill.getRequiredXP(level)
        return if (required > 0) xp.toDouble() / required.toDouble() else 0.0
    }
}

