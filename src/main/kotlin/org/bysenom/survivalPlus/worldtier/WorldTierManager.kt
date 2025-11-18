package org.bysenom.survivalPlus.worldtier

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Verwaltet World-Tiers für alle Welten
 */
class WorldTierManager(private val plugin: SurvivalPlus) {

    // World UUID -> WorldTier
    private val worldTiers = ConcurrentHashMap<UUID, WorldTier>()

    // Player UUID -> aktuelles WorldTier (für Anzeige)
    private val playerTiers = ConcurrentHashMap<UUID, WorldTier>()

    private val dataFile = File(plugin.dataFolder, "world_tiers.yml")

    init {
        loadWorldTiers()
    }

    /**
     * Setzt das World-Tier für eine Welt
     */
    fun setWorldTier(world: World, tier: WorldTier) {
        val oldTier = worldTiers[world.uid] ?: WorldTier.NORMAL
        worldTiers[world.uid] = tier
        saveWorldTiers()

        // Aktualisiere bereits existierende Mobs
        updateExistingMobs(world, oldTier, tier)

        // Benachrichtige alle Spieler in der Welt
        world.players.forEach { player ->
            player.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.DARK_GRAY))
            player.sendMessage(Component.text("World Tier geändert zu: ").color(NamedTextColor.YELLOW)
                .append(tier.getDisplayComponent()))
            player.sendMessage(Component.empty())
            tier.getDescription().forEach { player.sendMessage(it) }
            player.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.DARK_GRAY))

            player.playSound(player.location, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
        }

        plugin.logger.info("World Tier für ${world.name} auf ${tier.displayName} gesetzt")
    }

    /**
     * Aktualisiert bereits existierende Mobs basierend auf neuem World-Tier
     */
    private fun updateExistingMobs(world: World, oldTier: WorldTier, newTier: WorldTier) {
        val multiplier = newTier.mobHealthMultiplier / oldTier.mobHealthMultiplier
        val damageMultiplier = newTier.mobDamageMultiplier / oldTier.mobDamageMultiplier

        world.entities.filterIsInstance<org.bukkit.entity.Mob>().forEach { mob ->
            // Gesundheit anpassen
            val maxHealth = mob.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)
            maxHealth?.let {
                val newMaxHealth = it.baseValue * multiplier
                it.baseValue = newMaxHealth
                // Setze aktuelle Gesundheit proportional
                mob.health = (mob.health / it.baseValue * newMaxHealth).coerceAtMost(newMaxHealth)
            }

            // Schaden anpassen
            val attackDamage = mob.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE)
            attackDamage?.let {
                it.baseValue *= damageMultiplier
            }

            // Visueller Effekt
            if (newTier.tier > oldTier.tier) {
                mob.world.spawnParticle(
                    org.bukkit.Particle.ENCHANT,
                    mob.location.add(0.0, 1.0, 0.0),
                    10,
                    0.3, 0.5, 0.3,
                    0.1
                )
            }
        }

        plugin.logger.info("${world.entities.filterIsInstance<org.bukkit.entity.Mob>().size} Mobs in ${world.name} aktualisiert")
    }

    /**
     * Gibt das World-Tier für eine Welt zurück
     * Berücksichtigt dimension-übergreifende Tiers (survival, survival_nether, survival_the_end)
     */
    fun getWorldTier(world: World): WorldTier {
        // Finde die "Hauptwelt" für dimension-übergreifende Tiers
        val baseWorldName = getBaseWorldName(world.name)
        val baseWorld = plugin.server.worlds.find { getBaseWorldName(it.name) == baseWorldName }

        // Nutze das Tier der Hauptwelt, falls vorhanden
        val tier = if (baseWorld != null && baseWorld.uid != world.uid) {
            worldTiers.getOrDefault(baseWorld.uid, WorldTier.NORMAL)
        } else {
            worldTiers.getOrDefault(world.uid, WorldTier.NORMAL)
        }

        return tier
    }

    /**
     * Extrahiert den Basisnamen einer Welt (ohne _nether, _the_end Suffix)
     */
    private fun getBaseWorldName(worldName: String): String {
        return worldName
            .replace("_nether", "", ignoreCase = true)
            .replace("_end", "", ignoreCase = true)
    }

    /**
     * Erhöht das World-Tier für eine Welt
     */
    fun increaseWorldTier(world: World): Boolean {
        val currentTier = getWorldTier(world)
        val nextTier = WorldTier.entries.find { it.tier == currentTier.tier + 1 }

        return if (nextTier != null) {
            setWorldTier(world, nextTier)
            true
        } else {
            false
        }
    }

    /**
     * Verringert das World-Tier für eine Welt
     */
    fun decreaseWorldTier(world: World): Boolean {
        val currentTier = getWorldTier(world)
        val previousTier = WorldTier.entries.find { it.tier == currentTier.tier - 1 }

        return if (previousTier != null) {
            setWorldTier(world, previousTier)
            true
        } else {
            false
        }
    }

    /**
     * Gibt alle Welt-Tiers zurück
     */
    fun getAllWorldTiers(): Map<UUID, WorldTier> {
        return worldTiers.toMap()
    }

    /**
     * Prüft ob das Plugin in dieser Welt aktiv sein soll
     */
    fun isEnabledWorld(world: World): Boolean {
        val enabledWorlds = plugin.config.getStringList("enabled-worlds")
        return enabledWorlds.any { it.equals(world.name, ignoreCase = true) }
    }

    /**
     * Speichert alle World-Tiers
     */
    private fun saveWorldTiers() {
        val config = YamlConfiguration()

        worldTiers.forEach { (uuid, tier) ->
            val worldName = plugin.server.getWorld(uuid)?.name ?: uuid.toString()
            config.set("worlds.$worldName.uuid", uuid.toString())
            config.set("worlds.$worldName.tier", tier.tier)
        }

        try {
            config.save(dataFile)
        } catch (e: Exception) {
            plugin.logger.warning("Fehler beim Speichern der World-Tiers: ${e.message}")
        }
    }

    /**
     * Lädt alle World-Tiers
     */
    private fun loadWorldTiers() {
        if (!dataFile.exists()) {
            plugin.logger.info("Keine World-Tier Datei gefunden, erstelle neue...")

            // Setze Standard-Tiers für aktivierte Welten
            plugin.server.worlds.forEach { world ->
                if (isEnabledWorld(world)) {
                    worldTiers[world.uid] = WorldTier.NORMAL
                }
            }

            saveWorldTiers()
            return
        }

        val config = YamlConfiguration.loadConfiguration(dataFile)
        val worldsSection = config.getConfigurationSection("worlds") ?: return

        var loaded = 0
        worldsSection.getKeys(false).forEach { worldName ->
            try {
                val uuidString = worldsSection.getString("$worldName.uuid") ?: return@forEach
                val tierNumber = worldsSection.getInt("$worldName.tier", 1)

                val uuid = UUID.fromString(uuidString)
                val tier = WorldTier.fromTier(tierNumber)

                worldTiers[uuid] = tier
                loaded++
            } catch (e: Exception) {
                plugin.logger.warning("Fehler beim Laden von World-Tier für $worldName: ${e.message}")
            }
        }

        plugin.logger.info("$loaded World-Tiers geladen!")
    }

    /**
     * Setzt das Spieler-Tier für Anzeige-Zwecke
     */
    fun setPlayerTier(playerUUID: UUID, tier: WorldTier) {
        playerTiers[playerUUID] = tier
    }

    /**
     * Gibt das Spieler-Tier zurück
     */
    fun getPlayerTier(playerUUID: UUID): WorldTier {
        return playerTiers.getOrDefault(playerUUID, WorldTier.NORMAL)
    }

    /**
     * Cleanup
     */
    fun shutdown() {
        saveWorldTiers()
    }
}
