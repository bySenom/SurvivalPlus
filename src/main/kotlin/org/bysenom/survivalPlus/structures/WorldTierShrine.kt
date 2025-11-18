package org.bysenom.survivalPlus.structures

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World

/**
 * Definiert die World Tier Shrine Struktur
 * Ruinen-Ästhetik mit Altar in der Mitte
 */
class WorldTierShrine {

    companion object {
        /**
         * Baut eine Shrine-Struktur an der angegebenen Location
         */
        fun build(location: Location): Boolean {
            val world = location.world ?: return false

            // Zentrum der Struktur
            val centerX = location.blockX
            val centerY = location.blockY
            val centerZ = location.blockZ

            try {
                // Basis-Plattform (9x9 Polished Blackstone)
                for (x in -4..4) {
                    for (z in -4..4) {
                        val distance = kotlin.math.sqrt((x * x + z * z).toDouble())
                        if (distance <= 4.5) {
                            world.getBlockAt(centerX + x, centerY, centerZ + z).type = Material.POLISHED_BLACKSTONE
                        }
                    }
                }

                // Innerer Ring (7x7 Chiseled Polished Blackstone)
                for (x in -3..3) {
                    for (z in -3..3) {
                        val distance = kotlin.math.sqrt((x * x + z * z).toDouble())
                        if (distance <= 3.5) {
                            world.getBlockAt(centerX + x, centerY, centerZ + z).type = Material.CHISELED_POLISHED_BLACKSTONE
                        }
                    }
                }

                // Zentrum (3x3 erhöht)
                for (x in -1..1) {
                    for (z in -1..1) {
                        world.getBlockAt(centerX + x, centerY + 1, centerZ + z).type = Material.CHISELED_POLISHED_BLACKSTONE
                    }
                }

                // Altar-Block (Lodestone in der Mitte)
                world.getBlockAt(centerX, centerY + 2, centerZ).type = Material.LODESTONE

                // 4 Säulen an den Ecken (mit Ruinen-Effekt)
                val corners = listOf(
                    Pair(-3, -3),
                    Pair(-3, 3),
                    Pair(3, -3),
                    Pair(3, 3)
                )

                corners.forEach { (x, z) ->
                    // Säulen-Höhe variiert (Ruinen-Look)
                    val height = kotlin.random.Random.nextInt(3, 6)
                    for (y in 1..height) {
                        world.getBlockAt(centerX + x, centerY + y, centerZ + z).type = Material.BLACKSTONE
                    }
                    // Top der Säule
                    world.getBlockAt(centerX + x, centerY + height + 1, centerZ + z).type = Material.CHISELED_POLISHED_BLACKSTONE

                    // Glowstone-Beleuchtung
                    world.getBlockAt(centerX + x, centerY + height + 2, centerZ + z).type = Material.GLOWSTONE
                }

                // Dekorative Elemente (Ruinen-Trümmer)
                val decorations = listOf(
                    Triple(-2, 0, -2) to Material.POLISHED_BLACKSTONE_STAIRS,
                    Triple(2, 0, -2) to Material.POLISHED_BLACKSTONE_STAIRS,
                    Triple(-2, 0, 2) to Material.POLISHED_BLACKSTONE_STAIRS,
                    Triple(2, 0, 2) to Material.POLISHED_BLACKSTONE_STAIRS
                )

                decorations.forEach { (pos, material) ->
                    val (x, y, z) = pos
                    world.getBlockAt(centerX + x, centerY + y, centerZ + z).type = material
                }

                // Cracked Blackstone Bricks für Ruinen-Look (zufällig verteilt)
                for (x in -4..4) {
                    for (z in -4..4) {
                        if (kotlin.random.Random.nextDouble() < 0.15) { // 15% Chance
                            val block = world.getBlockAt(centerX + x, centerY, centerZ + z)
                            if (block.type == Material.POLISHED_BLACKSTONE) {
                                block.type = Material.CRACKED_POLISHED_BLACKSTONE_BRICKS
                            }
                        }
                    }
                }

                return true
            } catch (e: Exception) {
                return false
            }
        }

        /**
         * Prüft ob die Location für einen Shrine geeignet ist
         */
        fun isValidLocation(location: Location): Boolean {
            val world = location.world ?: return false
            val x = location.blockX
            val y = location.blockY
            val z = location.blockZ

            // Höhen-Check (nicht zu tief, nicht zu hoch)
            if (y < 60 || y > 120) return false

            // Prüfe ob genug Platz ist (9x9 Bereich)
            for (checkX in -5..5) {
                for (checkZ in -5..5) {
                    val checkBlock = world.getBlockAt(x + checkX, y - 1, z + checkZ)

                    // Muss auf solidem Boden sein
                    if (!checkBlock.type.isSolid) return false

                    // Keine Player-Strukturen (keine non-natural Blöcke)
                    if (isPlayerPlaced(checkBlock.type)) return false
                }
            }

            // Prüfe ob Bereich frei ist (keine Blöcke über dem Boden)
            for (checkX in -4..4) {
                for (checkZ in -4..4) {
                    for (checkY in 0..10) {
                        val checkBlock = world.getBlockAt(x + checkX, y + checkY, z + checkZ)
                        if (checkBlock.type.isSolid && checkBlock.type != Material.SHORT_GRASS &&
                            checkBlock.type != Material.TALL_GRASS && checkBlock.type != Material.FERN) {
                            return false
                        }
                    }
                }
            }

            return true
        }

        /**
         * Prüft ob ein Block von Spielern platziert wurde
         */
        private fun isPlayerPlaced(material: Material): Boolean {
            val playerBlocks = setOf(
                Material.COBBLESTONE,
                Material.STONE_BRICKS,
                Material.OAK_PLANKS,
                Material.SPRUCE_PLANKS,
                Material.BIRCH_PLANKS,
                Material.JUNGLE_PLANKS,
                Material.ACACIA_PLANKS,
                Material.DARK_OAK_PLANKS,
                Material.GLASS,
                Material.BRICKS,
                Material.MOSSY_COBBLESTONE,
                Material.TORCH,
                Material.CRAFTING_TABLE,
                Material.FURNACE,
                Material.CHEST,
                Material.LADDER,
                Material.IRON_BLOCK,
                Material.GOLD_BLOCK,
                Material.DIAMOND_BLOCK,
                Material.EMERALD_BLOCK,
                Material.NETHERITE_BLOCK
            )

            return playerBlocks.contains(material)
        }

        /**
         * Gibt die Größe der Shrine-Struktur zurück (für Protection-Radius)
         */
        fun getProtectionRadius(): Int = 10
    }
}

