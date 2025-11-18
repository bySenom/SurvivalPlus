package org.bysenom.survivalPlus.bosses

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * Titan Golem - Team-basierter World Boss
 * 
 * Benötigt Zusammenspiel: Tanks (aggro), DPS (damage), Support (healing/buffs)
 * 
 * Phasen:
 * 1. Phase (100-75%): Basic Attacks, Stomp AOE
 * 2. Phase (75-50%): Summon Minions, Boulder Throw
 * 3. Phase (50-25%): Enrage, Ground Slam, Shields
 * 4. Phase (25-0%): Berserk Mode, alle Abilities kombiniert
 */
class TitanGolemBoss(private val plugin: SurvivalPlus) {

    companion object {
        const val BOSS_NAME = "Titan Golem"
        val BOSS_COLOR = NamedTextColor.GOLD
        const val MIN_WORLD_TIER = 1
    }

    data class BossData(
        val boss: IronGolem,
        val worldTier: Int,
        val spawnLocation: Location,
        val bossBar: BossBar,
        var currentPhase: Int = 1,
        var lastAbilityTime: Long = 0,
        val aggroMap: MutableMap<UUID, Double> = mutableMapOf(),
        var shieldActive: Boolean = false,
        var shieldHealth: Double = 0.0
    )

    private val activeBosses = ConcurrentHashMap<UUID, BossData>()
    private val abilityTask: BukkitRunnable

    init {
        abilityTask = object : BukkitRunnable() {
            override fun run() {
                activeBosses.values.forEach { data ->
                    updateBoss(data)
                }
            }
        }
        abilityTask.runTaskTimer(plugin, 20L, 10L) // Alle 0.5 Sekunden
    }

    /**
     * Spawnt den Titan Golem Boss
     */
    fun spawnBoss(location: Location, worldTier: Int): IronGolem? {
        val world = location.world ?: return null

        // Base Stats (skaliert mit World Tier)
        val baseHealth = plugin.config.getDouble("bosses.titan-golem.base-health", 1000.0)
        val baseDamage = plugin.config.getDouble("bosses.titan-golem.base-damage", 20.0)
        val healthMultiplier = 1.0 + (worldTier - 1) * 0.5 // +50% pro Tier
        val damageMultiplier = 1.0 + (worldTier - 1) * 0.3 // +30% pro Tier

        // Spawn Iron Golem
        val boss = world.spawnEntity(location, EntityType.IRON_GOLEM) as IronGolem

        // Boss-Konfiguration
        boss.customName(Component.text("⚔ $BOSS_NAME ⚔").color(BOSS_COLOR))
        boss.isCustomNameVisible = true
        boss.removeWhenFarAway = false
        boss.setAI(true)
        boss.isPlayerCreated = false // Kein Player-Golem

        // Stats setzen
        val maxHealth = baseHealth * healthMultiplier
        boss.getAttribute(Attribute.MAX_HEALTH)?.baseValue = maxHealth
        boss.health = maxHealth
        boss.getAttribute(Attribute.ATTACK_DAMAGE)?.baseValue = baseDamage * damageMultiplier
        boss.getAttribute(Attribute.KNOCKBACK_RESISTANCE)?.baseValue = 1.0
        boss.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue = 0.25

        // Boss Bar erstellen
        val bossBar = setupBossBar(boss, maxHealth)

        // Boss Data speichern
        val data = BossData(
            boss = boss,
            worldTier = worldTier,
            spawnLocation = location.clone(),
            bossBar = bossBar
        )
        activeBosses[boss.uniqueId] = data

        // Spawn-Effekte
        playSpawnEffects(boss)

        // AI Task starten
        startBossAI(boss)

        plugin.logger.info("✓ Titan Golem gespawnt (Tier $worldTier)")
        return boss
    }

    /**
     * Boss Bar Setup
     */
    private fun setupBossBar(boss: IronGolem, maxHealth: Double): BossBar {
        val bossBar = BossBar.bossBar(
            Component.text("⚔ $BOSS_NAME ⚔").color(BOSS_COLOR),
            1.0f,
            BossBar.Color.YELLOW,
            BossBar.Overlay.PROGRESS
        )

        // Zeige allen Spielern in der Nähe
        boss.world.players.forEach { player ->
            if (player.location.distance(boss.location) <= 100) {
                player.showBossBar(bossBar)
            }
        }

        return bossBar
    }

    /**
     * Spawn-Effekte
     */
    private fun playSpawnEffects(boss: IronGolem) {
        val location = boss.location

        // Partikel-Explosion
        location.world?.spawnParticle(Particle.EXPLOSION, location.clone().add(0.0, 1.0, 0.0), 30, 2.0, 2.0, 2.0, 0.2)
        location.world?.spawnParticle(Particle.FLASH, location.clone().add(0.0, 1.0, 0.0), 5)

        // Sound
        location.world?.playSound(location, Sound.ENTITY_IRON_GOLEM_HURT, 2f, 0.5f)
        location.world?.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1f, 0.8f)
    }

    /**
     * Boss AI Loop
     */
    private fun startBossAI(boss: IronGolem) {
        object : BukkitRunnable() {
            override fun run() {
                val data = activeBosses[boss.uniqueId]
                if (data == null || !boss.isValid) {
                    cancel()
                    return
                }

                // Aggro-System: Greife Spieler mit höchster Aggro an
                val target = getHighestAggroTarget(data)
                if (target != null && target.isValid) {
                    boss.target = target
                }
            }
        }.runTaskTimer(plugin, 40L, 40L) // Alle 2 Sekunden
    }

    /**
     * Boss Update (Phasen-Check, Abilities)
     */
    private fun updateBoss(data: BossData) {
        val boss = data.boss
        if (!boss.isValid) {
            cleanupBoss(boss.uniqueId)
            return
        }

        // Phase-Check
        val healthPercent = (boss.health / boss.getAttribute(Attribute.MAX_HEALTH)!!.value) * 100
        val newPhase = when {
            healthPercent > 75 -> 1
            healthPercent > 50 -> 2
            healthPercent > 25 -> 3
            else -> 4
        }

        if (newPhase != data.currentPhase) {
            transitionPhase(data, newPhase)
        }

        // Boss Bar Update
        data.bossBar.progress((boss.health / boss.getAttribute(Attribute.MAX_HEALTH)!!.value).toFloat())

        // Abilities ausführen
        val now = System.currentTimeMillis()
        if (now - data.lastAbilityTime >= getAbilityCooldown(data.currentPhase)) {
            executePhaseAbility(data)
            data.lastAbilityTime = now
        }

        // Shield-Decay
        if (data.shieldActive && data.shieldHealth > 0) {
            data.shieldHealth -= 5.0 // 5 HP pro Tick
            if (data.shieldHealth <= 0) {
                data.shieldActive = false
                boss.world.spawnParticle(Particle.BLOCK, boss.location, 50, 1.0, 1.0, 1.0, Material.GLASS.createBlockData())
            }
        }
    }

    /**
     * Phasen-Übergang
     */
    private fun transitionPhase(data: BossData, newPhase: Int) {
        data.currentPhase = newPhase
        val boss = data.boss

        // Broadcast Phase-Change
        boss.world.players.forEach { player ->
            if (player.location.distance(boss.location) <= 100) {
                player.sendMessage(
                    Component.text("⚠ $BOSS_NAME").color(BOSS_COLOR)
                        .append(Component.text(" entered Phase $newPhase!").color(NamedTextColor.RED))
                )
                player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 0.7f)
            }
        }

        // Phase-Effekte
        when (newPhase) {
            2 -> {
                boss.world.spawnParticle(Particle.ANGRY_VILLAGER, boss.location, 20, 1.0, 1.0, 1.0)
                summonMinions(data, 3)
            }
            3 -> {
                boss.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false))
                boss.world.spawnParticle(Particle.FLAME, boss.location, 50, 2.0, 2.0, 2.0, 0.1)
            }
            4 -> {
                boss.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, false))
                boss.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, false, false))
                boss.world.spawnParticle(Particle.LAVA, boss.location, 100, 3.0, 2.0, 3.0)
            }
        }

        plugin.logger.info("Titan Golem Phase $newPhase")
    }

    /**
     * Führt Phasen-spezifische Ability aus
     */
    private fun executePhaseAbility(data: BossData) {
        when (data.currentPhase) {
            1 -> abilityGroundStomp(data)
            2 -> if (Random.nextBoolean()) abilityBoulderThrow(data) else summonMinions(data, 2)
            3 -> if (Random.nextBoolean()) abilityGroundSlam(data) else abilityActivateShield(data)
            4 -> {
                // Kombiniere alle Abilities
                when (Random.nextInt(4)) {
                    0 -> abilityGroundStomp(data)
                    1 -> abilityBoulderThrow(data)
                    2 -> abilityGroundSlam(data)
                    3 -> summonMinions(data, 4)
                }
            }
        }
    }

    /**
     * Ability: Ground Stomp - AOE Knockback
     */
    private fun abilityGroundStomp(data: BossData) {
        val boss = data.boss
        val location = boss.location

        // Effekte
        location.world?.spawnParticle(Particle.EXPLOSION, location, 20, 5.0, 0.1, 5.0)
        location.world?.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.7f)

        // Schade + Knockback alle Spieler im Radius
        location.world?.getNearbyEntities(location, 8.0, 3.0, 8.0)?.forEach { entity ->
            if (entity is Player) {
                entity.damage(15.0 * data.worldTier, boss)
                
                // Knockback
                val direction = entity.location.toVector().subtract(location.toVector()).normalize()
                entity.velocity = direction.multiply(2.0).setY(0.8)
                
                // Aggro erhöhen
                addAggro(data, entity.uniqueId, 50.0)
            }
        }
    }

    /**
     * Ability: Boulder Throw - Projektil-Angriff
     */
    private fun abilityBoulderThrow(data: BossData) {
        val boss = data.boss
        val target = getHighestAggroTarget(data) ?: return

        // Spawn Falling Block als "Boulder"
        val location = boss.eyeLocation.clone()
        val direction = target.location.toVector().subtract(location.toVector()).normalize()

        val boulder = boss.world.spawnFallingBlock(location, Material.STONE.createBlockData())
        boulder.velocity = direction.multiply(1.5).setY(0.5)
        boulder.dropItem = false
        boulder.setHurtEntities(true)
        boulder.setDamagePerBlock(25.0f * data.worldTier)

        // Partikel-Trail
        object : BukkitRunnable() {
            var ticks = 0
            override fun run() {
                if (!boulder.isValid || boulder.isOnGround || ticks++ > 100) {
                    // Explosion beim Aufprall
                    boulder.world.spawnParticle(Particle.EXPLOSION, boulder.location, 10, 2.0, 2.0, 2.0)
                    boulder.world.playSound(boulder.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
                    boulder.remove()
                    cancel()
                    return
                }
                boulder.world.spawnParticle(Particle.SMOKE, boulder.location, 3, 0.3, 0.3, 0.3, 0.01)
            }
        }.runTaskTimer(plugin, 1L, 1L)

        boss.world.playSound(boss.location, Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.8f)
    }

    /**
     * Ability: Ground Slam - Massive AOE Damage
     */
    private fun abilityGroundSlam(data: BossData) {
        val boss = data.boss
        val location = boss.location

        // Warning Particles
        boss.world.spawnParticle(Particle.CLOUD, location, 50, 10.0, 0.5, 10.0, 0.1)
        boss.world.playSound(location, Sound.ENTITY_RAVAGER_ROAR, 2f, 0.7f)

        // Nach 2 Sekunden: Slam
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if (!boss.isValid) return@Runnable

            // Massive AOE
            location.world?.spawnParticle(Particle.EXPLOSION_EMITTER, location, 5, 0.0, 0.0, 0.0)
            location.world?.spawnParticle(Particle.BLOCK, location, 200, 12.0, 1.0, 12.0, Material.STONE.createBlockData())
            location.world?.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 3f, 0.5f)

            // Schade + Stun
            location.world?.getNearbyEntities(location, 15.0, 5.0, 15.0)?.forEach { entity ->
                if (entity is Player) {
                    entity.damage(30.0 * data.worldTier, boss)
                    entity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 60, 2))
                    entity.addPotionEffect(PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 2))
                    
                    addAggro(data, entity.uniqueId, 100.0)
                }
            }
        }, 40L)
    }

    /**
     * Ability: Activate Shield - Absorbiert Schaden
     */
    private fun abilityActivateShield(data: BossData) {
        val boss = data.boss

        data.shieldActive = true
        data.shieldHealth = 500.0 * data.worldTier

        // Visuelle Effekte
        boss.world.spawnParticle(Particle.END_ROD, boss.location.clone().add(0.0, 1.0, 0.0), 50, 1.5, 2.0, 1.5, 0.1)
        boss.world.playSound(boss.location, Sound.BLOCK_BEACON_ACTIVATE, 2f, 1.5f)

        // Broadcast
        boss.world.players.forEach { player ->
            if (player.location.distance(boss.location) <= 50) {
                player.sendMessage(
                    Component.text("⚠ $BOSS_NAME activated a shield!").color(NamedTextColor.YELLOW)
                )
            }
        }
    }

    /**
     * Summon Minions - Steinstatuen
     */
    private fun summonMinions(data: BossData, amount: Int) {
        val boss = data.boss
        val location = boss.location

        repeat(amount) {
            val spawnLoc = location.clone().add(
                (Random.nextDouble() - 0.5) * 8,
                0.0,
                (Random.nextDouble() - 0.5) * 8
            )

            val minion = location.world?.spawnEntity(spawnLoc, EntityType.ZOMBIE) as? Zombie
            minion?.let {
                it.customName(Component.text("Stone Minion").color(NamedTextColor.GRAY))
                it.getAttribute(Attribute.MAX_HEALTH)?.baseValue = 50.0 * data.worldTier
                it.health = it.getAttribute(Attribute.MAX_HEALTH)!!.value
                it.equipment?.helmet = org.bukkit.inventory.ItemStack(Material.STONE)
                
                // Markiere als Boss-Minion
                val minionKey = org.bukkit.NamespacedKey(plugin, "boss_minion")
                it.persistentDataContainer.set(minionKey, org.bukkit.persistence.PersistentDataType.BYTE, 1)
            }
        }

        location.world?.spawnParticle(Particle.SMOKE, location, 30, 3.0, 1.0, 3.0, 0.1)
        location.world?.playSound(location, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1f, 0.7f)
    }

    /**
     * Aggro-System
     */
    private fun addAggro(data: BossData, playerUUID: UUID, amount: Double) {
        data.aggroMap[playerUUID] = (data.aggroMap[playerUUID] ?: 0.0) + amount
    }

    private fun getHighestAggroTarget(data: BossData): Player? {
        return data.aggroMap.entries
            .maxByOrNull { it.value }
            ?.let { plugin.server.getPlayer(it.key) }
            ?.takeIf { it.isValid && it.world == data.boss.world }
    }

    /**
     * Ability Cooldowns (in Millisekunden)
     */
    private fun getAbilityCooldown(phase: Int): Long {
        return when (phase) {
            1 -> 8000L  // 8 Sekunden
            2 -> 6000L  // 6 Sekunden
            3 -> 5000L  // 5 Sekunden
            4 -> 4000L  // 4 Sekunden
            else -> 8000L
        }
    }

    /**
     * Boss Cleanup
     */
    fun cleanupBoss(bossUUID: UUID) {
        val data = activeBosses.remove(bossUUID) ?: return
        
        // Verstecke Boss Bar
        data.boss.world.players.forEach { player ->
            player.hideBossBar(data.bossBar)
        }

        // Benachrichtige Arena Manager
        plugin.worldBossArenaManager?.onBossDefeated()
        
        plugin.logger.info("✓ Titan Golem entfernt")
    }

    /**
     * Shutdown
     */
    fun shutdown() {
        abilityTask.cancel()
        activeBosses.keys.toList().forEach { cleanupBoss(it) }
    }
}
