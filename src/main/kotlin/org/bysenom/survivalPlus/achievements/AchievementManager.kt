package org.bysenom.survivalPlus.achievements

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.Sound
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Verwaltet Achievement-System
 * Tracked Fortschritt und vergibt Achievements
 */
class AchievementManager(private val plugin: SurvivalPlus) {

    private val dataFolder = File(plugin.dataFolder, "achievements")
    private val playerAchievements = ConcurrentHashMap<UUID, MutableSet<Achievement>>()
    private val playerProgress = ConcurrentHashMap<UUID, MutableMap<String, Int>>()

    init {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
    }

    /**
     * Lädt Achievement-Daten eines Spielers
     */
    fun loadPlayerAchievements(uuid: UUID) {
        val file = File(dataFolder, "$uuid.yml")
        if (!file.exists()) {
            playerAchievements[uuid] = mutableSetOf()
            playerProgress[uuid] = mutableMapOf()
            return
        }

        val config = YamlConfiguration.loadConfiguration(file)
        val achievements = mutableSetOf<Achievement>()
        val progress = mutableMapOf<String, Int>()

        config.getStringList("achievements").forEach { name ->
            try {
                achievements.add(Achievement.valueOf(name))
            } catch (e: Exception) {
                plugin.logger.warning("Unbekanntes Achievement: $name")
            }
        }

        config.getConfigurationSection("progress")?.getKeys(false)?.forEach { key ->
            progress[key] = config.getInt("progress.$key")
        }

        playerAchievements[uuid] = achievements
        playerProgress[uuid] = progress
    }

    /**
     * Speichert Achievement-Daten (Async)
     */
    fun savePlayerAchievements(uuid: UUID) {
        val achievements = playerAchievements[uuid] ?: return
        val progress = playerProgress[uuid] ?: return

        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            try {
                val file = File(dataFolder, "$uuid.yml")
                val config = YamlConfiguration()

                config.set("achievements", achievements.map { it.name })
                progress.forEach { (key, value) ->
                    config.set("progress.$key", value)
                }

                config.save(file)
            } catch (e: Exception) {
                plugin.logger.severe("Fehler beim Speichern von Achievements für $uuid: ${e.message}")
                e.printStackTrace()
            }
        })
    }

    /**
     * Prüft ob Spieler ein Achievement hat
     */
    fun hasAchievement(uuid: UUID, achievement: Achievement): Boolean {
        return playerAchievements[uuid]?.contains(achievement) ?: false
    }

    /**
     * Vergibt ein Achievement an einen Spieler
     */
    fun grantAchievement(player: Player, achievement: Achievement) {
        val uuid = player.uniqueId
        val achievements = playerAchievements.getOrPut(uuid) { mutableSetOf() }

        if (achievements.contains(achievement)) {
            return // Bereits erhalten
        }

        achievements.add(achievement)
        savePlayerAchievements(uuid)

        // Benachrichtigung
        player.sendMessage(achievement.createNotification())
        player.sendMessage(Component.text("  ${achievement.description}")
            .color(NamedTextColor.GRAY))

        // Sound & Effects
        player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f)
        player.showTitle(
            net.kyori.adventure.title.Title.title(
                Component.text("🏆 Erfolg!").color(NamedTextColor.GOLD),
                Component.text(achievement.displayName).color(NamedTextColor.YELLOW)
            )
        )

        // Belohnung
        achievement.grantReward(player)

        // Broadcast (optional)
        if (!achievement.hidden) {
            plugin.server.broadcast(
                Component.text("${player.name} hat das Achievement ")
                    .color(NamedTextColor.GRAY)
                    .append(Component.text(achievement.displayName)
                        .color(NamedTextColor.YELLOW))
                    .append(Component.text(" freigeschaltet!")
                        .color(NamedTextColor.GRAY))
            )
        }
    }

    /**
     * Erhöht einen Progress-Counter
     */
    fun incrementProgress(uuid: UUID, key: String, amount: Int = 1) {
        val progress = playerProgress.getOrPut(uuid) { mutableMapOf() }
        progress[key] = progress.getOrDefault(key, 0) + amount
        savePlayerAchievements(uuid)
    }

    /**
     * Holt Progress-Wert
     */
    fun getProgress(uuid: UUID, key: String): Int {
        return playerProgress[uuid]?.get(key) ?: 0
    }

    /**
     * Prüft alle Achievements für einen Spieler
     */
    fun checkAchievements(player: Player) {
        Achievement.entries.forEach { achievement ->
            if (!hasAchievement(player.uniqueId, achievement)) {
                if (checkRequirement(player, achievement)) {
                    grantAchievement(player, achievement)
                }
            }
        }
    }

    /**
     * Prüft eine Achievement-Anforderung
     */
    private fun checkRequirement(player: Player, achievement: Achievement): Boolean {
        return when (val req = achievement.requirement) {
            is ItemQualityRequirement -> {
                // Wird manuell getriggert wenn Item erhalten wird
                false
            }
            is CountRequirement -> {
                getProgress(player.uniqueId, req.key) >= req.count
            }
            is SkillLevelRequirement -> {
                if (req.skill != null) {
                    plugin.skillDataManager.getSkillLevel(player.uniqueId, req.skill) >= req.level
                } else {
                    // Irgendein Skill auf Level
                    org.bysenom.survivalPlus.skills.Skill.entries.any { skill ->
                        plugin.skillDataManager.getSkillLevel(player.uniqueId, skill) >= req.level
                    }
                }
            }
            is AllSkillsRequirement -> {
                org.bysenom.survivalPlus.skills.Skill.entries.all { skill ->
                    plugin.skillDataManager.getSkillLevel(player.uniqueId, skill) >= req.minLevel
                }
            }
            is WorldTierRequirement -> {
                plugin.worldTierManager.getWorldTier(player.world).tier >= req.tier
            }
            else -> false
        }
    }

    /**
     * Entlädt Spieler-Daten
     */
    fun unloadPlayer(uuid: UUID) {
        // Sync-Save beim Logout
        playerAchievements[uuid]?.let { achievements ->
            val progress = playerProgress[uuid] ?: mutableMapOf()
            val file = File(dataFolder, "$uuid.yml")
            val config = YamlConfiguration()

            config.set("achievements", achievements.map { it.name })
            progress.forEach { (key, value) ->
                config.set("progress.$key", value)
            }

            try {
                config.save(file)
            } catch (e: Exception) {
                plugin.logger.severe("Fehler beim Speichern von Achievements für $uuid: ${e.message}")
            }
        }
        
        playerAchievements.remove(uuid)
        playerProgress.remove(uuid)
    }

    /**
     * Gibt die Anzahl freigeschalteter Achievements zurück
     */
    fun getAchievementCount(uuid: UUID): Int {
        return playerAchievements[uuid]?.size ?: 0
    }

    /**
     * Gibt alle Achievements eines Spielers zurück
     */
    fun getPlayerAchievements(uuid: UUID): Set<Achievement> {
        return playerAchievements[uuid]?.toSet() ?: emptySet()
    }
}
