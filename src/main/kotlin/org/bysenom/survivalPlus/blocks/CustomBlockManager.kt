package org.bysenom.survivalPlus.blocks

import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bysenom.survivalPlus.SurvivalPlus
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Verwaltet alle platzierten Custom Blocks
 */
class CustomBlockManager(private val plugin: SurvivalPlus) {

    // Location String (world,x,y,z) -> CustomBlock
    private val placedBlocks = ConcurrentHashMap<String, PlacedBlock>()

    // UUID von Armor Stands -> Location String
    private val armorStandToLocation = ConcurrentHashMap<UUID, String>()

    private val dataFile = File(plugin.dataFolder, "custom_blocks.yml")

    data class PlacedBlock(
        val type: CustomBlock,
        val location: Location,
        val armorStandUUID: UUID? = null,
        val placedBy: UUID? = null
    )

    init {
        loadBlocks()
    }

    /**
     * Registriert einen platzierten Block
     */
    fun placeBlock(location: Location, type: CustomBlock, armorStandUUID: UUID?, placedBy: UUID?) {
        val key = locationToString(location)
        val placedBlock = PlacedBlock(type, location, armorStandUUID, placedBy)
        placedBlocks[key] = placedBlock

        armorStandUUID?.let {
            armorStandToLocation[it] = key
        }

        saveBlocks()
    }

    /**
     * Entfernt einen platzierten Block
     */
    fun removeBlock(location: Location): PlacedBlock? {
        val key = locationToString(location)
        val block = placedBlocks.remove(key)

        block?.armorStandUUID?.let {
            armorStandToLocation.remove(it)
        }

        saveBlocks()
        return block
    }

    /**
     * Gibt den Custom Block an einer Location zurück
     */
    fun getBlock(location: Location): PlacedBlock? {
        return placedBlocks[locationToString(location)]
    }

    /**
     * Gibt den Custom Block für einen Armor Stand zurück
     */
    fun getBlockByArmorStand(uuid: UUID): PlacedBlock? {
        val key = armorStandToLocation[uuid] ?: return null
        return placedBlocks[key]
    }

    /**
     * Prüft ob an der Location ein Custom Block ist
     */
    fun isCustomBlock(location: Location): Boolean {
        return placedBlocks.containsKey(locationToString(location))
    }

    /**
     * Gibt alle platzierten Blocks zurück
     */
    fun getAllBlocks(): Collection<PlacedBlock> {
        return placedBlocks.values
    }

    /**
     * Speichert alle Blocks in die Datei
     */
    fun saveBlocks() {
        val config = YamlConfiguration()

        placedBlocks.forEach { (key, block) ->
            val section = config.createSection("blocks.$key")
            section.set("type", block.type.name)
            section.set("world", block.location.world?.name)
            section.set("x", block.location.blockX)
            section.set("y", block.location.blockY)
            section.set("z", block.location.blockZ)
            block.armorStandUUID?.let { section.set("armorstand", it.toString()) }
            block.placedBy?.let { section.set("placedBy", it.toString()) }
        }

        try {
            config.save(dataFile)
        } catch (e: Exception) {
            plugin.logger.warning("Fehler beim Speichern der Custom Blocks: ${e.message}")
        }
    }

    /**
     * Lädt alle Blocks aus der Datei
     */
    fun loadBlocks() {
        if (!dataFile.exists()) {
            plugin.logger.info("Keine Custom Blocks Datei gefunden, erstelle neue...")
            return
        }

        val config = YamlConfiguration.loadConfiguration(dataFile)
        val blocksSection = config.getConfigurationSection("blocks") ?: return

        var loaded = 0
        blocksSection.getKeys(false).forEach { key ->
            try {
                val section = blocksSection.getConfigurationSection(key) ?: return@forEach

                val typeName = section.getString("type") ?: return@forEach
                val type = CustomBlock.valueOf(typeName)

                val worldName = section.getString("world") ?: return@forEach
                val world = plugin.server.getWorld(worldName) ?: return@forEach

                val x = section.getInt("x")
                val y = section.getInt("y")
                val z = section.getInt("z")

                val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

                val armorStandUUID = section.getString("armorstand")?.let { UUID.fromString(it) }
                val placedBy = section.getString("placedBy")?.let { UUID.fromString(it) }

                val placedBlock = PlacedBlock(type, location, armorStandUUID, placedBy)
                placedBlocks[key] = placedBlock

                armorStandUUID?.let {
                    armorStandToLocation[it] = key
                }

                loaded++
            } catch (e: Exception) {
                plugin.logger.warning("Fehler beim Laden von Block $key: ${e.message}")
            }
        }

        plugin.logger.info("$loaded Custom Blocks geladen!")
    }

    /**
     * Konvertiert Location zu String-Key
     */
    private fun locationToString(location: Location): String {
        return "${location.world?.name},${location.blockX},${location.blockY},${location.blockZ}"
    }

    /**
     * Cleanup - Entfernt alle Armor Stands
     */
    fun shutdown() {
        placedBlocks.values.forEach { block ->
            block.armorStandUUID?.let { uuid ->
                block.location.world?.entities?.find { it.uniqueId == uuid }?.remove()
            }
        }
        saveBlocks()
    }
    
    /**
     * Gibt die Anzahl platzierter Custom Blocks zurück (für Debug)
     */
    fun getBlockCount(): Int = placedBlocks.size
}

