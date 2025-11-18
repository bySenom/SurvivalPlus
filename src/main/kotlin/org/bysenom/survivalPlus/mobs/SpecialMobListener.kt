package org.bysenom.survivalPlus.mobs

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

/**
 * Event Listener für Special Mobs
 */
class SpecialMobListener(private val plugin: SurvivalPlus) : Listener {

    /**
     * Mob-Spawn mit Chance auf Special Mob + World-Tier-Modifikatoren
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onCreatureSpawn(event: CreatureSpawnEvent) {
        val entity = event.entity as? Mob ?: return

        // Nur natürliche Spawns
        if (event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL &&
            event.spawnReason != CreatureSpawnEvent.SpawnReason.CHUNK_GEN &&
            event.spawnReason != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return
        }

        // Prüfe ob Plugin in dieser Welt aktiv ist
        if (!plugin.worldTierManager.isEnabledWorld(entity.world)) {
            return
        }

        // Hole World-Tier
        val worldTier = plugin.worldTierManager.getWorldTier(entity.world)

        // Wende World-Tier-Modifikatoren auf ALLE Mobs an
        applyWorldTierModifiers(entity, worldTier)

        // Prüfe ob Special Mob spawnen soll
        val chance = worldTier.specialMobChance
        if (Random.nextDouble(100.0) < chance) {
            plugin.specialMobManager.createSpecialMob(entity)
        }
    }

    /**
     * Wendet World-Tier-Modifikatoren auf einen Mob an
     */
    private fun applyWorldTierModifiers(entity: Mob, worldTier: org.bysenom.survivalPlus.worldtier.WorldTier) {
        // Gesundheit erhöhen
        val maxHealth = entity.getAttribute(Attribute.MAX_HEALTH)
        maxHealth?.let {
            val newHealth = it.baseValue * worldTier.mobHealthMultiplier
            it.baseValue = newHealth
            entity.health = newHealth
        }

        // Schaden erhöhen
        val attackDamage = entity.getAttribute(Attribute.ATTACK_DAMAGE)
        attackDamage?.let {
            it.baseValue *= worldTier.mobDamageMultiplier
        }

        // Optionally: Visueller Effekt für höhere Tiers
        if (worldTier.tier >= 3) { // Ab Epic
            entity.world.spawnParticle(
                Particle.ENCHANT,
                entity.location.add(0.0, 1.0, 0.0),
                5,
                0.3, 0.3, 0.3,
                0.1
            )
        }
    }

    /**
     * Special Mob greift an - Aktiviere Fähigkeiten
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onSpecialMobAttack(event: EntityDamageByEntityEvent) {
        val attacker = event.damager as? Mob ?: return
        val victim = event.entity as? LivingEntity ?: return

        if (!plugin.specialMobManager.isSpecialMob(attacker)) return

        val affix = plugin.specialMobManager.getMobAffix(attacker) ?: return

        // Aktiviere Angriffs-Fähigkeiten
        affix.abilities.forEach { ability ->
            when (ability) {
                MobAbility.FREEZE_ATTACK -> {
                    victim.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 100, 2))
                    victim.addPotionEffect(PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 1))
                    victim.world.spawnParticle(Particle.SNOWFLAKE, victim.location.add(0.0, 1.0, 0.0), 20)
                }

                MobAbility.POISON_CLOUD -> {
                    victim.addPotionEffect(PotionEffect(PotionEffectType.POISON, 100, 1))
                    victim.world.spawnParticle(Particle.ITEM_SLIME, victim.location.add(0.0, 1.0, 0.0), 15)
                }

                MobAbility.WITHER_AURA -> {
                    victim.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 60, 0))
                    victim.world.spawnParticle(Particle.SMOKE, victim.location.add(0.0, 1.0, 0.0), 15)
                }

                MobAbility.LIFESTEAL -> {
                    val healAmount = event.damage * 0.5
                    val newHealth = (attacker.health + healAmount).coerceAtMost(attacker.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0)
                    attacker.health = newHealth
                    attacker.world.spawnParticle(Particle.HEART, attacker.location.add(0.0, 1.0, 0.0), 5)
                }

                MobAbility.CRITICAL_HITS -> {
                    if (Random.nextDouble() < 0.3) { // 30% Crit Chance
                        event.damage *= 2.0
                        victim.world.spawnParticle(Particle.CRIT, victim.location.add(0.0, 1.0, 0.0), 20)
                        victim.world.playSound(victim.location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1f)
                    }
                }

                MobAbility.LIGHTNING_STRIKE -> {
                    if (Random.nextDouble() < 0.15) { // 15% Chance
                        victim.world.strikeLightning(victim.location)
                    }
                }

                else -> {}
            }
        }
    }

    /**
     * MOB greift Spieler an - Wende World-Tier-Schaden an
     * WICHTIG: Dieser Handler erhöht den Schaden ALLER Mobs basierend auf World-Tier!
     * Unterstützt auch Projektile (Pfeile von Skeletons)
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onMobAttackPlayer(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? Player ?: return

        // Direkter Mob-Angriff
        val attacker = event.damager as? Mob

        // Projektil-Angriff (z.B. Skeleton-Pfeile)
        val projectile = event.damager as? org.bukkit.entity.Projectile
        val projectileShooter = projectile?.shooter as? Mob

        val mobAttacker = attacker ?: projectileShooter ?: return

        // Prüfe ob Plugin in dieser Welt aktiv ist
        if (!plugin.worldTierManager.isEnabledWorld(mobAttacker.world)) return

        // Hole World-Tier
        val worldTier = plugin.worldTierManager.getWorldTier(mobAttacker.world)

        // Wende Schadens-Multiplikator an
        if (worldTier.mobDamageMultiplier > 1.0) {
            event.damage *= worldTier.mobDamageMultiplier

            // Debug-Log (optional, kann später entfernt werden)
            plugin.logger.fine("Mob ${mobAttacker.type.name} Schaden erhöht: ${event.damage / worldTier.mobDamageMultiplier} -> ${event.damage} (${worldTier.displayName})")
        }
    }

    /**
     * Special Mob wird getroffen - Defensive Fähigkeiten
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onSpecialMobDamaged(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? Mob ?: return
        val attacker = event.damager as? Player ?: return

        if (!plugin.specialMobManager.isSpecialMob(victim)) return

        val affix = plugin.specialMobManager.getMobAffix(victim) ?: return

        // Defensive Fähigkeiten
        affix.abilities.forEach { ability ->
            when (ability) {
                MobAbility.FIRE_AURA -> {
                    attacker.fireTicks = 100
                    attacker.world.spawnParticle(Particle.FLAME, attacker.location.add(0.0, 1.0, 0.0), 20)
                }

                MobAbility.BLOOD_SHIELD -> {
                    if (victim.health < victim.getAttribute(Attribute.MAX_HEALTH)?.value?.times(0.5) ?: 10.0) {
                        event.damage *= 0.5 // 50% Schadensreduktion bei low HP
                        victim.world.spawnParticle(Particle.CRIMSON_SPORE, victim.location.add(0.0, 1.0, 0.0), 30)
                    }
                }

                MobAbility.ICE_ARMOR -> {
                    attacker.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 40, 1))
                    victim.world.spawnParticle(Particle.SNOWFLAKE, victim.location.add(0.0, 1.0, 0.0), 15)
                }

                MobAbility.TELEPORT -> {
                    if (Random.nextDouble() < 0.2) { // 20% Chance
                        teleportAway(victim)
                    }
                }

                else -> {}
            }
        }
    }

    /**
     * Special Mob stirbt
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onSpecialMobDeath(event: EntityDeathEvent) {
        val entity = event.entity as? Mob ?: return

        if (!plugin.specialMobManager.isSpecialMob(entity)) return

        val affix = plugin.specialMobManager.getMobAffix(entity) ?: return

        // Aktiviere Tod-Fähigkeiten
        affix.abilities.forEach { ability ->
            when (ability) {
                MobAbility.EXPLOSIVE_DEATH -> {
                    entity.world.createExplosion(entity.location, 3f, false, false)
                    entity.world.spawnParticle(Particle.EXPLOSION, entity.location, 10)
                }

                else -> {}
            }
        }

        // Verbesserte Drops basierend auf World-Tier
        val worldTier = plugin.worldTierManager.getWorldTier(entity.world)
        val qualityBoost = worldTier.dropQualityBoost + 1 // +1 für Special Mob Bonus

        // Erhöhe XP
        event.droppedExp = (event.droppedExp * 2.0).toInt()

        // Partikel-Effekt beim Tod
        entity.world.spawnParticle(Particle.END_ROD, entity.location, 30, 0.5, 0.5, 0.5, 0.1)
        entity.world.playSound(entity.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f)

        // Entferne aus Tracking
        plugin.specialMobManager.removeSpecialMob(entity.uniqueId)

        plugin.logger.fine("Special Mob getötet: ${affix.displayName} ${entity.type.name}")
    }

    /**
     * Special Mob wird target - Aggressive Fähigkeiten
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onSpecialMobTarget(event: EntityTargetEvent) {
        val entity = event.entity as? Mob ?: return
        val target = event.target as? LivingEntity ?: return

        if (!plugin.specialMobManager.isSpecialMob(entity)) return

        val affix = plugin.specialMobManager.getMobAffix(entity) ?: return

        // Aktiviere Target-Fähigkeiten
        affix.abilities.forEach { ability ->
            when (ability) {
                MobAbility.FIRE_AURA -> {
                    // Spawn Feuer-Partikel um den Mob
                    entity.world.spawnParticle(Particle.FLAME, entity.location.add(0.0, 1.0, 0.0), 5)
                }

                else -> {}
            }
        }
    }

    /**
     * Teleportiert einen Mob weg vom Angreifer
     */
    private fun teleportAway(entity: Mob) {
        val currentLoc = entity.location
        val world = entity.world

        // Versuche 10 mal einen sicheren Teleport-Punkt zu finden
        repeat(10) {
            val x = currentLoc.x + Random.nextDouble(-10.0, 10.0)
            val z = currentLoc.z + Random.nextDouble(-10.0, 10.0)
            val y = world.getHighestBlockYAt(x.toInt(), z.toInt()).toDouble()

            val newLoc = Location(world, x, y, z)

            if (newLoc.block.type.isAir && newLoc.add(0.0, 1.0, 0.0).block.type.isAir) {
                entity.teleport(newLoc)
                entity.world.spawnParticle(Particle.PORTAL, currentLoc, 30)
                entity.world.spawnParticle(Particle.PORTAL, newLoc, 30)
                entity.world.playSound(currentLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                entity.world.playSound(newLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                return
            }
        }
    }
}

