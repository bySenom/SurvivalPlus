package org.bysenom.survivalPlus.structures

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * Verwaltet alle World Tier Shrines
 */
class ShrineManager(private val plugin: SurvivalPlus) {

    data class ShrineData(
        val location: Location,
        val worldUUID: UUID,
        val beaconTaskId: Int? = null
    )

    private val shrines = ConcurrentHashMap<UUID, ShrineData>()
    private val dataFile = File(plugin.dataFolder, "shrines.yml")

    // Minimaler Abstand zwischen Shrines (in Blöcken)
    private val minDistance: Int
        get() = plugin.config.getInt("shrines.min-distance", 1200)

    // Minimaler Abstand vom Spawn
    private val minSpawnDistance: Int
        get() = plugin.config.getInt("shrines.min-spawn-distance", 500)

    // Maximale Shrines pro Welt
    private val maxPerWorld: Int
        get() = plugin.config.getInt("shrines.max-per-world", 3)

    init {
        loadShrines()
    }

    /**
     * Generiert Shrines für eine Welt
     */
    fun generateShinesForWorld(world: World) {
        // Prüfe ob Plugin in dieser Welt aktiv ist
        if (!plugin.worldTierManager.isEnabledWorld(world)) {
            plugin.logger.fine("Welt ${world.name} ist nicht aktiviert - überspringe Shrine-Generierung")
            return
        }

        // Nur für spezifische Welten (aus Config)
        val worldName = world.name.lowercase()
        val targetWorldNames = plugin.config.getStringList("shrines.target-worlds")
            .map { it.lowercase() }

        if (targetWorldNames.isNotEmpty() && !targetWorldNames.contains(worldName)) {
            plugin.logger.fine("Welt ${world.name} ist nicht in der Shrine-Ziel-Liste - überspringe")
            return
        }

        // Prüfe ob bereits Shrines existieren
        val existingShrines = shrines.values.count { it.worldUUID == world.uid }
        if (existingShrines > 0) {
            plugin.logger.info("Welt ${world.name} hat bereits $existingShrines Shrines")
            return
        }

        plugin.logger.info("Generiere World Tier Shrines für ${world.name}...")

        // Versuche Shrines zu spawnen (config-basiert)
        var attempts = 0
        var spawned = 0
        val maxAttempts = 50 // Reduziert für Performance
        val targetShrines = maxPerWorld // Aus Config

        while (attempts < maxAttempts && spawned < targetShrines) {
            attempts++

            val location = findValidShrineLocation(world)
            if (location != null) {
                // Spawn auf Main-Thread ausführen
                plugin.server.scheduler.runTask(plugin, Runnable {
                    if (spawnShrine(location)) {
                        spawned++
                        plugin.logger.info("✓ Shrine #$spawned gespawnt in ${world.name} bei ${location.blockX}, ${location.blockY}, ${location.blockZ}")
                    }
                })

                // Kleine Pause zwischen Spawns (aber ohne Thread.sleep in Async)
                try {
                    Thread.sleep(50) // Reduziert von 100ms
                } catch (e: InterruptedException) {
                    break
                }
            }
        }

        plugin.logger.info("✓ $spawned Shrines in ${world.name} generiert (nach $attempts Versuchen)")
    }

    /**
     * Findet eine gültige Location für einen Shrine
     */
    private fun findValidShrineLocation(world: World): Location? {
        val spawn = world.spawnLocation

        // Zufällige Location in der Welt
        val angle = Random.nextDouble(0.0, 2 * Math.PI)
        val distance = Random.nextInt(minSpawnDistance, 15000) // Bis 15k Blöcke vom Spawn

        val x = spawn.blockX + (distance * kotlin.math.cos(angle)).toInt()
        val z = spawn.blockZ + (distance * kotlin.math.sin(angle)).toInt()
        val y = world.getHighestBlockYAt(x, z) + 1

        val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

        // Validierungs-Checks
        if (!WorldTierShrine.isValidLocation(location)) {
            return null
        }

        // Prüfe Abstand zu existierenden Shrines
        if (!isMinDistanceToOtherShrines(location)) {
            return null
        }

        return location
    }

    /**
     * Prüft ob die Mindest-Distanz zu anderen Shrines eingehalten wird
     */
    private fun isMinDistanceToOtherShrines(location: Location): Boolean {
        shrines.values.forEach { shrine ->
            if (shrine.worldUUID == location.world?.uid) {
                val distance = shrine.location.distance(location)
                if (distance < minDistance) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Spawnt einen Shrine an der Location
     */
    fun spawnShrine(location: Location): Boolean {
        // Baue Struktur
        if (!WorldTierShrine.build(location)) {
            return false
        }

        // Erstelle Beacon-Laser
        val beaconTaskId = startBeaconLaser(location)

        // Speichere Shrine
        val shrineId = UUID.randomUUID()
        val shrineData = ShrineData(
            location = location,
            worldUUID = location.world?.uid ?: return false,
            beaconTaskId = beaconTaskId
        )

        shrines[shrineId] = shrineData
        saveShrines()

        return true
    }

    /**
     * Startet einen Beacon-Laser Effekt
     */
    private fun startBeaconLaser(location: Location): Int {
        val world = location.world ?: return -1

        return plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            // End Rod Partikel als Laser
            for (y in location.blockY + 3 until 256 step 2) {
                world.spawnParticle(
                    org.bukkit.Particle.END_ROD,
                    location.blockX + 0.5,
                    y.toDouble(),
                    location.blockZ + 0.5,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
                )
            }

            // Zusätzliche Partikel am Altar selbst
            world.spawnParticle(
                org.bukkit.Particle.ENCHANT,
                location.blockX + 0.5,
                location.blockY + 2.5,
                location.blockZ + 0.5,
                5,
                0.3, 0.3, 0.3,
                0.1
            )
        }, 0L, 20L) // Jede Sekunde
    }

    /**
     * Gibt alle Shrines zurück
     */
    fun getAllShrines(): Collection<ShrineData> {
        return shrines.values
    }

    /**
     * Gibt alle Shrines einer Welt zurück
     */
    fun getShrinesInWorld(world: World): List<ShrineData> {
        return shrines.values.filter { it.worldUUID == world.uid }
    }

    /**
     * Prüft ob eine Location ein Shrine ist
     */
    fun isShrineLocation(location: Location): Boolean {
        return shrines.values.any { shrine ->
            shrine.location.world?.uid == location.world?.uid &&
            shrine.location.distance(location) < WorldTierShrine.getProtectionRadius()
        }
    }

    /**
     * Findet den nächsten Shrine zu einer Location
     */
    fun findNearestShrine(location: Location): ShrineData? {
        return shrines.values
            .filter { it.worldUUID == location.world?.uid }
            .minByOrNull { it.location.distance(location) }
    }

    /**
     * Prüft ob ein Spieler in der Nähe eines Shrines ist
     * @param location Die Location des Spielers
     * @param radius Der Radius in Blöcken (Standard: 50)
     * @return Der nächste Shrine in Reichweite oder null
     */
    fun getNearbyShrine(location: Location, radius: Double = 50.0): ShrineData? {
        return shrines.values
            .filter { it.worldUUID == location.world?.uid }
            .filter { it.location.distance(location) <= radius }
            .minByOrNull { it.location.distance(location) }
    }

    /**
     * Prüft ob ein Spieler direkt bei einem Shrine ist (für GUI-Öffnung)
     * @param location Die Location des Spielers
     * @return true wenn Spieler nah genug ist
     */
    fun isPlayerNearShrine(location: Location): Boolean {
        val nearbyShrine = getNearbyShrine(location, 10.0) // 10 Blöcke Radius
        return nearbyShrine != null
    }

    /**
     * Gibt die Distanz zum nächsten Shrine zurück
     * @param location Die Location des Spielers
     * @return Distanz in Blöcken oder null wenn kein Shrine in der Welt
     */
    fun getDistanceToNearestShrine(location: Location): Double? {
        val nearest = findNearestShrine(location)
        return nearest?.location?.distance(location)
    }

    /**
     * Speichert alle Shrines
     */
    private fun saveShrines() {
        val config = YamlConfiguration()

        shrines.forEach { (id, shrine) ->
            val section = config.createSection("shrines.$id")
            section.set("world", shrine.worldUUID.toString())
            section.set("x", shrine.location.blockX)
            section.set("y", shrine.location.blockY)
            section.set("z", shrine.location.blockZ)
        }

        try {
            config.save(dataFile)
        } catch (e: Exception) {
            plugin.logger.warning("Fehler beim Speichern der Shrines: ${e.message}")
        }
    }

    /**
     * Lädt alle Shrines
     */
    private fun loadShrines() {
        if (!dataFile.exists()) {
            plugin.logger.info("Keine Shrines-Datei gefunden")
            return
        }

        val config = YamlConfiguration.loadConfiguration(dataFile)
        val shrinesSection = config.getConfigurationSection("shrines") ?: return

        var loaded = 0
        shrinesSection.getKeys(false).forEach { key ->
            try {
                val section = shrinesSection.getConfigurationSection(key) ?: return@forEach

                val worldUUID = UUID.fromString(section.getString("world") ?: return@forEach)
                val world = plugin.server.getWorld(worldUUID) ?: return@forEach

                val x = section.getInt("x")
                val y = section.getInt("y")
                val z = section.getInt("z")

                val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

                // Starte Beacon-Laser
                val beaconTaskId = startBeaconLaser(location)

                val shrineData = ShrineData(location, worldUUID, beaconTaskId)
                shrines[UUID.fromString(key)] = shrineData

                loaded++
            } catch (e: Exception) {
                plugin.logger.warning("Fehler beim Laden von Shrine $key: ${e.message}")
            }
        }

        plugin.logger.info("$loaded Shrines geladen!")
    }

    /**
     * Cleanup - Stoppt alle Beacon-Laser
     */
    fun shutdown() {
        shrines.values.forEach { shrine ->
            shrine.beaconTaskId?.let { plugin.server.scheduler.cancelTask(it) }
        }
        saveShrines()
    }
}

