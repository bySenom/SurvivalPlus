package org.bysenom.survivalPlus.managers

import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.skills.Skill
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Verwaltet Skill-Daten der Spieler
 * Speichert XP und Level persistent
 */
class SkillDataManager(private val plugin: SurvivalPlus) {

    private val dataFolder = File(plugin.dataFolder, "playerdata")
    private val playerSkills = ConcurrentHashMap<UUID, MutableMap<Skill, SkillData>>()
    
    init {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
    }

    /**
     * Skill-Daten Struktur
     */
    data class SkillData(
        var level: Int = 1,
        var xp: Int = 0
    )

    /**
     * Lädt Skill-Daten eines Spielers
     */
    fun loadPlayerSkills(uuid: UUID): Map<Skill, SkillData> {
        // Prüfe Cache zuerst
        playerSkills[uuid]?.let { return it }

        val file = File(dataFolder, "$uuid.yml")
        if (!file.exists()) {
            val emptyData = mutableMapOf<Skill, SkillData>()
            Skill.entries.forEach { skill ->
                emptyData[skill] = SkillData()
            }
            playerSkills[uuid] = emptyData
            return emptyData
        }

        val config = YamlConfiguration.loadConfiguration(file)
        val skills = mutableMapOf<Skill, SkillData>()

        Skill.entries.forEach { skill ->
            val level = config.getInt("skills.${skill.name}.level", 1)
            val xp = config.getInt("skills.${skill.name}.xp", 0)
            skills[skill] = SkillData(level, xp)
        }

        playerSkills[uuid] = skills
        return skills
    }

    /**
     * Speichert Skill-Daten eines Spielers (Async)
     */
    fun savePlayerSkills(uuid: UUID, skills: Map<Skill, SkillData>) {
        playerSkills[uuid] = skills.toMutableMap()

        // Async save
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            try {
                val file = File(dataFolder, "$uuid.yml")
                val config = YamlConfiguration()

                skills.forEach { (skill, data) ->
                    config.set("skills.${skill.name}.level", data.level)
                    config.set("skills.${skill.name}.xp", data.xp)
                }

                config.save(file)
            } catch (e: Exception) {
                plugin.logger.severe("Fehler beim Speichern von Skill-Daten für $uuid: ${e.message}")
                e.printStackTrace()
            }
        })
    }

    /**
     * Holt Skill-Level eines Spielers
     */
    fun getSkillLevel(uuid: UUID, skill: Skill): Int {
        return playerSkills[uuid]?.get(skill)?.level ?: 1
    }

    /**
     * Holt Skill-XP eines Spielers
     */
    fun getSkillXP(uuid: UUID, skill: Skill): Int {
        return playerSkills[uuid]?.get(skill)?.xp ?: 0
    }

    /**
     * Setzt Skill-Level
     */
    fun setSkillLevel(uuid: UUID, skill: Skill, level: Int) {
        val skills = playerSkills.getOrPut(uuid) { mutableMapOf() }
        val data = skills.getOrPut(skill) { SkillData() }
        data.level = level
        savePlayerSkills(uuid, skills)
    }

    /**
     * Fügt Skill-XP hinzu
     */
    fun addSkillXP(uuid: UUID, skill: Skill, xp: Int) {
        val skills = playerSkills.getOrPut(uuid) { mutableMapOf() }
        val data = skills.getOrPut(skill) { SkillData() }
        data.xp += xp

        // Level-Up prüfen
        val requiredXP = skill.getRequiredXP(data.level)
        if (data.xp >= requiredXP) {
            data.xp -= requiredXP
            data.level++
            
            // Benachrichtige SkillManager über Level-Up
            plugin.server.scheduler.runTask(plugin, Runnable {
                plugin.server.getPlayer(uuid)?.let { player ->
                    plugin.skillManager.handleLevelUp(player, skill, data.level - 1, data.level)
                }
            })
        }

        savePlayerSkills(uuid, skills)
    }

    /**
     * Entfernt Spieler-Daten aus Cache (bei Logout)
     */
    fun unloadPlayer(uuid: UUID) {
        playerSkills[uuid]?.let { skills ->
            // Finales Sync-Save beim Logout
            val file = File(dataFolder, "$uuid.yml")
            val config = YamlConfiguration()

            skills.forEach { (skill, data) ->
                config.set("skills.${skill.name}.level", data.level)
                config.set("skills.${skill.name}.xp", data.xp)
            }

            try {
                config.save(file)
            } catch (e: Exception) {
                plugin.logger.severe("Fehler beim Speichern von Skill-Daten für $uuid: ${e.message}")
            }
        }
        playerSkills.remove(uuid)
    }

    /**
     * Speichert alle geladenen Spieler-Daten
     */
    fun saveAll() {
        plugin.logger.info("Speichere Skill-Daten für ${playerSkills.size} Spieler...")
        playerSkills.forEach { (uuid, skills) ->
            val file = File(dataFolder, "$uuid.yml")
            val config = YamlConfiguration()

            skills.forEach { (skill, data) ->
                config.set("skills.${skill.name}.level", data.level)
                config.set("skills.${skill.name}.xp", data.xp)
            }

            try {
                config.save(file)
            } catch (e: Exception) {
                plugin.logger.severe("Fehler beim Speichern von Skill-Daten für $uuid: ${e.message}")
            }
        }
        plugin.logger.info("✓ Skill-Daten gespeichert!")
    }

    /**
     * Gibt die Anzahl geladener Spieler zurück (für Debug)
     */
    fun getLoadedPlayerCount(): Int = playerSkills.size
}
