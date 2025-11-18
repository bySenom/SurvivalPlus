package org.bysenom.survivalPlus.generation

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import kotlin.random.Random

/**
 * Custom Ore Generation Manager
 * 
 * Generiert Custom Ores (Limestone, Pyrite, Galena) in der Welt
 */
class CustomOreManager(private val plugin: SurvivalPlus) : Listener {

    companion object {
        // Ore-Mappings
        val LIMESTONE = Material.CALCITE  // Overworld Reforging Tier 1
        val PYRITE = Material.RAW_GOLD_BLOCK  // Nether Reforging Tier 2
        val GALENA = Material.TUFF  // End Reforging Tier 3
    }

    /**
     * Generiert Ores beim Chunk-Load
     */
    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        val chunk = event.chunk
        val world = event.world

        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(world)) return

        // Nur neue Chunks generieren
        if (!event.isNewChunk) return

        when (world.environment) {
            World.Environment.NORMAL -> generateLimestone(chunk)
            World.Environment.NETHER -> generatePyrite(chunk)
            World.Environment.THE_END -> generateGalena(chunk)
            else -> {}
        }
    }

    /**
     * Generiert Limestone (Calcite) in der Overworld
     * Y-Level: -64 bis 64
     * Vein Size: 3-6 Blöcke
     * Chance: 8 Veins pro Chunk
     */
    private fun generateLimestone(chunk: Chunk) {
        val config = plugin.config
        if (!config.getBoolean("ore-generation.limestone.enabled", true)) return

        val veinsPerChunk = config.getInt("ore-generation.limestone.veins-per-chunk", 8)
        val minY = config.getInt("ore-generation.limestone.min-y", -64)
        val maxY = config.getInt("ore-generation.limestone.max-y", 64)
        val minVeinSize = config.getInt("ore-generation.limestone.min-vein-size", 3)
        val maxVeinSize = config.getInt("ore-generation.limestone.max-vein-size", 6)

        repeat(veinsPerChunk) {
            val x = Random.nextInt(16)
            val z = Random.nextInt(16)
            val y = Random.nextInt(minY, maxY + 1)

            val block = chunk.getBlock(x, y, z)
            if (block.type == Material.STONE || block.type == Material.DEEPSLATE) {
                generateVein(block, LIMESTONE, Random.nextInt(minVeinSize, maxVeinSize + 1))
            }
        }
    }

    /**
     * Generiert Pyrite (Raw Gold Block) im Nether
     * Y-Level: 10 bis 118
     * Vein Size: 2-4 Blöcke
     * Chance: 5 Veins pro Chunk
     */
    private fun generatePyrite(chunk: Chunk) {
        val config = plugin.config
        if (!config.getBoolean("ore-generation.pyrite.enabled", true)) return

        val veinsPerChunk = config.getInt("ore-generation.pyrite.veins-per-chunk", 5)
        val minY = config.getInt("ore-generation.pyrite.min-y", 10)
        val maxY = config.getInt("ore-generation.pyrite.max-y", 118)
        val minVeinSize = config.getInt("ore-generation.pyrite.min-vein-size", 2)
        val maxVeinSize = config.getInt("ore-generation.pyrite.max-vein-size", 4)

        repeat(veinsPerChunk) {
            val x = Random.nextInt(16)
            val z = Random.nextInt(16)
            val y = Random.nextInt(minY, maxY + 1)

            val block = chunk.getBlock(x, y, z)
            if (block.type == Material.NETHERRACK || block.type == Material.BLACKSTONE) {
                generateVein(block, PYRITE, Random.nextInt(minVeinSize, maxVeinSize + 1))
            }
        }
    }

    /**
     * Generiert Galena (Tuff) im End
     * Y-Level: 0 bis 128
     * Vein Size: 2-3 Blöcke
     * Chance: 4 Veins pro Chunk
     */
    private fun generateGalena(chunk: Chunk) {
        val config = plugin.config
        if (!config.getBoolean("ore-generation.galena.enabled", true)) return

        val veinsPerChunk = config.getInt("ore-generation.galena.veins-per-chunk", 4)
        val minY = config.getInt("ore-generation.galena.min-y", 0)
        val maxY = config.getInt("ore-generation.galena.max-y", 128)
        val minVeinSize = config.getInt("ore-generation.galena.min-vein-size", 2)
        val maxVeinSize = config.getInt("ore-generation.galena.max-vein-size", 3)

        repeat(veinsPerChunk) {
            val x = Random.nextInt(16)
            val z = Random.nextInt(16)
            val y = Random.nextInt(minY, maxY + 1)

            val block = chunk.getBlock(x, y, z)
            if (block.type == Material.END_STONE) {
                generateVein(block, GALENA, Random.nextInt(minVeinSize, maxVeinSize + 1))
            }
        }
    }

    /**
     * Generiert eine Ore-Vein um einen Startblock
     */
    private fun generateVein(startBlock: Block, oreMaterial: Material, size: Int) {
        val toProcess = mutableListOf(startBlock)
        val processed = mutableSetOf<Block>()
        var placed = 0

        while (toProcess.isNotEmpty() && placed < size) {
            val block = toProcess.removeAt(0)
            if (processed.contains(block)) continue
            processed.add(block)

            // Ersetze Block mit Ore
            val validReplacement = when (startBlock.world.environment) {
                World.Environment.NORMAL -> block.type == Material.STONE || block.type == Material.DEEPSLATE
                World.Environment.NETHER -> block.type == Material.NETHERRACK || block.type == Material.BLACKSTONE
                World.Environment.THE_END -> block.type == Material.END_STONE
                else -> false
            }

            if (validReplacement) {
                block.type = oreMaterial
                placed++

                // Füge benachbarte Blöcke hinzu
                if (placed < size) {
                    val neighbors = listOf(
                        block.getRelative(1, 0, 0),
                        block.getRelative(-1, 0, 0),
                        block.getRelative(0, 1, 0),
                        block.getRelative(0, -1, 0),
                        block.getRelative(0, 0, 1),
                        block.getRelative(0, 0, -1)
                    )
                    toProcess.addAll(neighbors.filter { !processed.contains(it) && Random.nextDouble() < 0.7 })
                }
            }
        }
    }

    /**
     * Debug-Info
     */
    fun logGenerationInfo() {
        plugin.logger.info("✓ Custom Ore Generation aktiviert")
        plugin.logger.info("  - Limestone (Calcite): Overworld, Y: -64 bis 64")
        plugin.logger.info("  - Pyrite (Raw Gold Block): Nether, Y: 10 bis 118")
        plugin.logger.info("  - Galena (Tuff): End, Y: 0 bis 128")
    }
}
