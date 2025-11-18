package org.bysenom.survivalPlus.effects

import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.Quality
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player

class ParticleEffectManager(private val plugin: SurvivalPlus) {

    fun spawnItemDropParticles(location: Location, quality: Quality) {
        if (!plugin.config.getBoolean("features.particle-effects", true)) return

        val world = location.world ?: return

        when (quality) {
            Quality.COMMON -> {
                // No particles for common
            }
            Quality.UNCOMMON -> {
                world.spawnParticle(
                    Particle.HAPPY_VILLAGER,
                    location.add(0.0, 0.5, 0.0),
                    5,
                    0.2, 0.2, 0.2,
                    0.05
                )
            }
            Quality.RARE -> {
                world.spawnParticle(
                    Particle.ELECTRIC_SPARK,
                    location.add(0.0, 0.5, 0.0),
                    10,
                    0.3, 0.3, 0.3,
                    0.1
                )
            }
            Quality.EPIC -> {
                world.spawnParticle(
                    Particle.ENCHANT,
                    location.add(0.0, 0.5, 0.0),
                    15,
                    0.4, 0.4, 0.4,
                    0.5
                )
            }
            Quality.LEGENDARY -> {
                world.spawnParticle(
                    Particle.END_ROD,
                    location.add(0.0, 0.5, 0.0),
                    20,
                    0.5, 0.5, 0.5,
                    0.1
                )
                world.spawnParticle(
                    Particle.FIREWORK,
                    location.add(0.0, 0.5, 0.0),
                    10,
                    0.3, 0.3, 0.3,
                    0.05
                )
            }
            Quality.MYTHIC -> {
                world.spawnParticle(
                    Particle.SOUL_FIRE_FLAME,
                    location.add(0.0, 0.5, 0.0),
                    30,
                    0.5, 0.5, 0.5,
                    0.05
                )
                world.spawnParticle(
                    Particle.DRAGON_BREATH,
                    location.add(0.0, 0.5, 0.0),
                    20,
                    0.4, 0.4, 0.4,
                    0.02
                )
                world.spawnParticle(
                    Particle.ENCHANT,
                    location.add(0.0, 0.5, 0.0),
                    25,
                    0.6, 0.6, 0.6,
                    0.8
                )
            }
        }
    }

    fun spawnReforgingSuccessParticles(player: Player, quality: Quality) {
        if (!plugin.config.getBoolean("features.particle-effects", true)) return

        val location = player.location.add(0.0, 1.0, 0.0)
        val world = player.world

        world.spawnParticle(
            Particle.HAPPY_VILLAGER,
            location,
            30,
            0.5, 0.5, 0.5,
            0.1
        )

        when (quality) {
            Quality.MYTHIC -> {
                world.spawnParticle(
                    Particle.SOUL_FIRE_FLAME,
                    location,
                    50,
                    0.5, 0.5, 0.5,
                    0.1
                )
            }
            Quality.LEGENDARY -> {
                world.spawnParticle(
                    Particle.END_ROD,
                    location,
                    40,
                    0.5, 0.5, 0.5,
                    0.1
                )
            }
            Quality.EPIC -> {
                world.spawnParticle(
                    Particle.ENCHANT,
                    location,
                    30,
                    0.5, 0.5, 0.5,
                    0.5
                )
            }
            else -> {
                world.spawnParticle(
                    Particle.ELECTRIC_SPARK,
                    location,
                    20,
                    0.5, 0.5, 0.5,
                    0.1
                )
            }
        }
    }

    fun spawnEquipParticles(player: Player, quality: Quality) {
        if (!plugin.config.getBoolean("features.particle-effects", true)) return
        if (quality.tier < 5) return

        val location = player.location.add(0.0, 1.0, 0.0)
        val world = player.world

        when (quality) {
            Quality.LEGENDARY -> {
                world.spawnParticle(
                    Particle.FIREWORK,
                    location,
                    15,
                    0.3, 0.5, 0.3,
                    0.05
                )
            }
            Quality.MYTHIC -> {
                world.spawnParticle(
                    Particle.SOUL_FIRE_FLAME,
                    location,
                    20,
                    0.3, 0.5, 0.3,
                    0.02
                )
            }
            else -> {}
        }
    }

    fun startMythicAura(player: Player) {
        if (!plugin.config.getBoolean("features.mythic-aura", true)) return

        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            if (!player.isOnline) return@Runnable

            val location = player.location.add(0.0, 1.0, 0.0)
            player.world.spawnParticle(
                Particle.SOUL_FIRE_FLAME,
                location,
                2,
                0.3, 0.3, 0.3,
                0.01
            )
        }, 0L, 10L)
    }

    /**
     * Partikel-Effekt beim Platzieren von Custom Blocks
     */
    fun spawnBlockPlaceParticles(location: Location) {
        if (!plugin.config.getBoolean("features.particle-effects", true)) return

        val world = location.world ?: return
        val centerLocation = location.clone().add(0.5, 0.5, 0.5)

        // Haupteffekt
        world.spawnParticle(
            Particle.CLOUD,
            centerLocation,
            20,
            0.3, 0.3, 0.3,
            0.05
        )

        // Glitzer-Effekt
        world.spawnParticle(
            Particle.END_ROD,
            centerLocation,
            10,
            0.4, 0.4, 0.4,
            0.02
        )

        // Funken
        world.spawnParticle(
            Particle.ELECTRIC_SPARK,
            centerLocation,
            15,
            0.2, 0.2, 0.2,
            0.1
        )
    }
}

