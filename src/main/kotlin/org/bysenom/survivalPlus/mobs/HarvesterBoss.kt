package org.bysenom.survivalPlus.mobs

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.Quality
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.ConcurrentHashMap

/**
 * The Harvester - Endgame Nether Boss
 * 
 * 4 Phasen Boss mit Feuer/Lava Mechaniken:
 * - Phase 1 (100-75%): Normal Attacks, Blaze Summons
 * - Phase 2 (75-50%): Fire Waves, Increased Speed
 * - Phase 3 (50-25%): Lava Pools, Heavy Damage
 * - Phase 4 (25-0%): Berserk, Rapid Attacks, Mass Summons
 */
class HarvesterBoss(private val plugin: SurvivalPlus) {

    /**
     * Boss Data Class
     */
    data class BossData(
        val boss: WitherSkeleton,
        val worldTier: Int,
        var phase: Int,
        val spawnLocation: Location,
        var lastBlazeSummon: Long,
        var lastFireWave: Long,
        var lastLavaPool: Long,
        var aiTask: BukkitTask? = null,
        var bossBar: BossBar? = null
    )

    private val activeBosses = ConcurrentHashMap<WitherSkeleton, BossData>()
    private val bossBarKey = NamespacedKey(plugin, "harvester_boss")

    companion object {
        private val BOSS_COLOR = TextColor.color(255, 80, 20) // Orange-Red
        
        // Base Stats (scale with World Tier)
        const val BASE_HEALTH = 500.0
        const val BASE_DAMAGE = 15.0
        const val BASE_ARMOR = 10.0
        
        // Phase Thresholds
        const val PHASE_2_HP = 0.75
        const val PHASE_3_HP = 0.50
        const val PHASE_4_HP = 0.25
        
        // Abilities Cooldowns (ticks)
        const val BLAZE_SUMMON_COOLDOWN = 200L // 10 Sekunden
        const val FIRE_WAVE_COOLDOWN = 300L    // 15 Sekunden
        const val LAVA_POOL_COOLDOWN = 250L    // 12.5 Sekunden
    }

    /**
     * Spawnt den Harvester Boss
     */
    fun spawn(location: Location, worldTier: Int = 1): WitherSkeleton? {
        val world = location.world ?: return null
        
        // Spawne Wither Skeleton
        val boss = world.spawnEntity(location, EntityType.WITHER_SKELETON) as WitherSkeleton
        
        // Setup Boss
        setupBossEntity(boss, worldTier)
        val bossBar = setupBossBar(worldTier)
        
        // Boss Data
        val data = BossData(
            boss = boss,
            worldTier = worldTier,
            phase = 1,
            spawnLocation = location,
            lastBlazeSummon = 0L,
            lastFireWave = 0L,
            lastLavaPool = 0L,
            bossBar = bossBar
        )
        
        activeBosses[boss] = data
        
        // Start AI Loop
        startBossAI(data)
        
        // Announce
        announceSpawn(location, worldTier)
        
        plugin.logger.info("[Harvester] Boss spawned at ${location.blockX}, ${location.blockY}, ${location.blockZ} (Tier $worldTier)")
        
        return boss
    }

    /**
     * Setup Boss Entity Stats
     */
    private fun setupBossEntity(boss: WitherSkeleton, worldTier: Int) {
        // Stats skalieren mit World Tier
        val tierMultiplier = 1.0 + (worldTier - 1) * 0.5 // +50% pro Tier
        
        val maxHealth = BASE_HEALTH * tierMultiplier
        val damage = BASE_DAMAGE * tierMultiplier
        val armor = BASE_ARMOR * tierMultiplier
        
        boss.getAttribute(Attribute.MAX_HEALTH)?.baseValue = maxHealth
        boss.health = maxHealth
        boss.getAttribute(Attribute.ATTACK_DAMAGE)?.baseValue = damage
        boss.getAttribute(Attribute.ARMOR)?.baseValue = armor
        boss.getAttribute(Attribute.KNOCKBACK_RESISTANCE)?.baseValue = 0.8
        boss.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue = 0.28
        
        // Boss Name
        boss.customName(Component.text("Der Ernter").color(BOSS_COLOR).decorate(TextDecoration.BOLD))
        boss.isCustomNameVisible = true
        
        // Boss Marker
        boss.persistentDataContainer.set(bossBarKey, PersistentDataType.INTEGER, worldTier)
        
        // Equipment
        boss.equipment?.apply {
            helmet = ItemStack(Material.NETHERITE_HELMET)
            chestplate = ItemStack(Material.NETHERITE_CHESTPLATE)
            leggings = ItemStack(Material.NETHERITE_LEGGINGS)
            boots = ItemStack(Material.NETHERITE_BOOTS)
            setItemInMainHand(ItemStack(Material.NETHERITE_AXE))
        }
        
        // Boss Effects
        boss.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, Int.MAX_VALUE, 0, false, false))
    }

    /**
     * Setup Boss Bar
     */
    private fun setupBossBar(worldTier: Int): BossBar {
        val bossBar = Bukkit.createBossBar(
            "§6§lDer Ernter §8(Tier $worldTier) §c❤",
            BarColor.RED,
            BarStyle.SEGMENTED_10
        )
        
        bossBar.isVisible = true
        bossBar.progress = 1.0
        
        return bossBar
    }

    /**
     * Start Boss AI Loop
     */
    private fun startBossAI(data: BossData) {
        val task = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            if (!data.boss.isValid || data.boss.isDead) {
                handleBossDeath(data)
                return@Runnable
            }
            
            // Update Phase
            updatePhase(data)
            
            // Update Boss Bar
            updateBossBar(data)
            
            // Execute Abilities
            executeAbilities(data)
            
            // Particles
            spawnPhaseParticles(data)
            
        }, 20L, 20L) // Alle 1 Sekunde
        
        data.aiTask = task
    }

    /**
     * Update Boss Phase basierend auf HP
     */
    private fun updatePhase(data: BossData) {
        val hpPercent = data.boss.health / data.boss.getAttribute(Attribute.MAX_HEALTH)!!.value
        
        val newPhase = when {
            hpPercent > PHASE_2_HP -> 1
            hpPercent > PHASE_3_HP -> 2
            hpPercent > PHASE_4_HP -> 3
            else -> 4
        }
        
        if (newPhase != data.phase) {
            data.phase = newPhase
            onPhaseChange(data, newPhase)
        }
    }

    /**
     * Phase Change Handler
     */
    private fun onPhaseChange(data: BossData, newPhase: Int) {
        val location = data.boss.location
        
        when (newPhase) {
            2 -> {
                // Phase 2: Fire Waves aktiviert
                announcePhase(location, "§6Der Ernter entfesselt Feuerwellen!")
                data.boss.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 1, false, false))
                
                // Visual Effect
                location.world?.spawnParticle(Particle.LAVA, location, 100, 3.0, 3.0, 3.0, 0.1)
                location.world?.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1f, 0.8f)
            }
            3 -> {
                // Phase 3: Lava Pools aktiviert
                announcePhase(location, "§cDer Ernter beschwört Lava-Becken!")
                data.boss.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, Int.MAX_VALUE, 1, false, false))
                
                // Visual Effect
                location.world?.spawnParticle(Particle.FLAME, location, 200, 5.0, 5.0, 5.0, 0.2)
                location.world?.playSound(location, Sound.ITEM_FIRECHARGE_USE, 2f, 0.5f)
            }
            4 -> {
                // Phase 4: Berserk Mode
                announcePhase(location, "§4§lDER ERNTER IST RASEND!")
                data.boss.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 2, false, false))
                data.boss.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, Int.MAX_VALUE, 2, false, false))
                data.boss.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, Int.MAX_VALUE, 1, false, false))
                
                // Visual Effect
                location.world?.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 300, 8.0, 8.0, 8.0, 0.3)
                location.world?.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.5f)
            }
        }
    }

    /**
     * Execute Boss Abilities basierend auf Phase
     */
    private fun executeAbilities(data: BossData) {
        val now = System.currentTimeMillis()
        
        // Phase 1+: Blaze Summons
        if (now - data.lastBlazeSummon > BLAZE_SUMMON_COOLDOWN * 50) {
            summonBlazes(data)
            data.lastBlazeSummon = now
        }
        
        // Phase 2+: Fire Waves
        if (data.phase >= 2 && now - data.lastFireWave > FIRE_WAVE_COOLDOWN * 50) {
            castFireWave(data)
            data.lastFireWave = now
        }
        
        // Phase 3+: Lava Pools
        if (data.phase >= 3 && now - data.lastLavaPool > LAVA_POOL_COOLDOWN * 50) {
            createLavaPool(data)
            data.lastLavaPool = now
        }
        
        // Phase 4: Rapid Attacks (handled in damage listener)
    }

    /**
     * Summon Blazes
     */
    private fun summonBlazes(data: BossData) {
        val boss = data.boss
        val location = boss.location
        val amount = when (data.phase) {
            1 -> 2
            2, 3 -> 3
            4 -> 5 // Berserk: mehr Summons
            else -> 2
        }
        
        repeat(amount) {
            val spawnLoc = location.clone().add(
                (Math.random() - 0.5) * 6,
                0.0,
                (Math.random() - 0.5) * 6
            )
            
            val blaze = location.world?.spawnEntity(spawnLoc, EntityType.BLAZE) as? Blaze
            blaze?.let {
                it.customName(Component.text("Ernter's Diener").color(BOSS_COLOR))
                it.getAttribute(Attribute.MAX_HEALTH)?.baseValue = 15.0 * data.worldTier
                it.health = it.getAttribute(Attribute.MAX_HEALTH)!!.value
                
                // Markiere als Boss-Minion (kein Loot)
                val minionKey = org.bukkit.NamespacedKey(plugin, "boss_minion")
                it.persistentDataContainer.set(minionKey, org.bukkit.persistence.PersistentDataType.BYTE, 1)
            }
        }
        
        location.world?.spawnParticle(Particle.FLAME, location, 50, 3.0, 1.0, 3.0, 0.1)
        location.world?.playSound(location, Sound.ENTITY_BLAZE_AMBIENT, 1f, 0.8f)
    }

    /**
     * Cast Fire Wave
     */
    private fun castFireWave(data: BossData) {
        val boss = data.boss
        val location = boss.location
        
        // Ring of fire expanding outwards
        var radius = 2.0
        val task = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            if (radius > 15.0) return@Runnable
            
            // Spawn particles in circle
            for (i in 0 until 36) {
                val angle = Math.toRadians(i * 10.0)
                val x = location.x + radius * Math.cos(angle)
                val z = location.z + radius * Math.sin(angle)
                val particleLoc = Location(location.world, x, location.y, z)
                
                location.world?.spawnParticle(Particle.FLAME, particleLoc, 3, 0.2, 0.5, 0.2, 0.0)
                
                // Damage nearby players
                location.world?.getNearbyEntities(particleLoc, 1.5, 2.0, 1.5)?.forEach { entity ->
                    if (entity is Player) {
                        entity.damage(5.0 * data.worldTier, data.boss)
                        entity.fireTicks = 60
                    }
                }
            }
            
            radius += 1.5
        }, 0L, 5L)
        
        plugin.server.scheduler.runTaskLater(plugin, Runnable { task.cancel() }, 100L)
        
        location.world?.playSound(location, Sound.ENTITY_BLAZE_SHOOT, 2f, 0.6f)
    }

    /**
     * Create Lava Pool
     */
    private fun createLavaPool(data: BossData) {
        val target = data.boss.target as? Player ?: return
        val location = target.location
        
        // Create temporary lava pool
        val blocks = mutableListOf<Location>()
        for (x in -2..2) {
            for (z in -2..2) {
                val blockLoc = location.clone().add(x.toDouble(), -1.0, z.toDouble())
                if (blockLoc.block.type == Material.AIR || blockLoc.block.type.isSolid) {
                    val aboveLoc = blockLoc.clone().add(0.0, 1.0, 0.0)
                    if (aboveLoc.block.type == Material.AIR) {
                        blocks.add(blockLoc)
                    }
                }
            }
        }
        
        // Spawn particles as warning
        blocks.forEach { loc ->
            loc.world?.spawnParticle(Particle.LAVA, loc.clone().add(0.5, 1.0, 0.5), 10, 0.3, 0.3, 0.3, 0.0)
        }
        
        location.world?.playSound(location, Sound.BLOCK_LAVA_POP, 1f, 0.8f)
        
        // After 1 second: damage zone
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            blocks.forEach { loc ->
                loc.world?.spawnParticle(Particle.FLAME, loc.clone().add(0.5, 1.0, 0.5), 30, 0.5, 0.5, 0.5, 0.1)
                
                // Damage entities in zone
                loc.world?.getNearbyEntities(loc, 1.0, 2.0, 1.0)?.forEach { entity ->
                    if (entity is Player) {
                        entity.damage(10.0 * data.worldTier, data.boss)
                        entity.fireTicks = 100
                    }
                }
            }
        }, 20L)
    }

    /**
     * Spawn Phase-specific Particles
     */
    private fun spawnPhaseParticles(data: BossData) {
        val location = data.boss.location
        
        when (data.phase) {
            1 -> location.world?.spawnParticle(Particle.FLAME, location.clone().add(0.0, 1.0, 0.0), 3, 0.3, 0.5, 0.3, 0.0)
            2 -> location.world?.spawnParticle(Particle.LAVA, location.clone().add(0.0, 1.0, 0.0), 5, 0.4, 0.6, 0.4, 0.0)
            3 -> location.world?.spawnParticle(Particle.SOUL_FIRE_FLAME, location.clone().add(0.0, 1.0, 0.0), 8, 0.5, 0.8, 0.5, 0.01)
            4 -> {
                location.world?.spawnParticle(Particle.SOUL_FIRE_FLAME, location.clone().add(0.0, 1.0, 0.0), 15, 0.8, 1.2, 0.8, 0.02)
                location.world?.spawnParticle(Particle.FLAME, location.clone().add(0.0, 2.0, 0.0), 10, 1.0, 1.0, 1.0, 0.1)
            }
        }
    }

    /**
     * Update Boss Bar
     */
    private fun updateBossBar(data: BossData) {
        val bossBar = data.bossBar ?: return
        val hpPercent = data.boss.health / data.boss.getAttribute(Attribute.MAX_HEALTH)!!.value
        
        bossBar.progress = hpPercent.coerceIn(0.0, 1.0)
        bossBar.setTitle("§6§lDer Ernter §7(Phase ${data.phase}) §c❤ §7${String.format("%.1f", hpPercent * 100)}%")
        
        // Change color by phase
        bossBar.color = when (data.phase) {
            1 -> BarColor.RED
            2 -> BarColor.YELLOW
            3 -> BarColor.PURPLE
            4 -> BarColor.WHITE
            else -> BarColor.RED
        }
        
        // Update players in range
        data.boss.world.getNearbyEntities(data.boss.location, 50.0, 50.0, 50.0)
            .filterIsInstance<Player>()
            .forEach { player ->
                if (!bossBar.players.contains(player)) {
                    bossBar.addPlayer(player)
                }
            }
    }

    /**
     * Handle Boss Death
     */
    private fun handleBossDeath(data: BossData) {
        data.aiTask?.cancel()
        data.bossBar?.removeAll()
        
        activeBosses.remove(data.boss)
        
        // Drop Loot
        dropLoot(data)
        
        // Announce
        announceDeath(data.boss.location, data.worldTier)
        
        // Effects
        data.boss.location.world?.spawnParticle(Particle.EXPLOSION_EMITTER, data.boss.location, 5, 2.0, 2.0, 2.0, 0.0)
        data.boss.location.world?.playSound(data.boss.location, Sound.ENTITY_WITHER_DEATH, 2f, 0.5f)
        
        plugin.logger.info("[Harvester] Boss defeated (Tier ${data.worldTier})")
    }

    /**
     * Drop Boss Loot
     */
    private fun dropLoot(data: BossData) {
        val location = data.boss.location
        
        // Guaranteed Drops (scale with world tier)
        val guaranteedDrops = listOf(
            ItemStack(Material.NETHERITE_SCRAP, ((2..5).random() * (1.0 + (data.worldTier - 1) * 0.3)).toInt().coerceAtLeast(1)),
            ItemStack(Material.BLAZE_ROD, ((5..10).random() * (1.0 + (data.worldTier - 1) * 0.3)).toInt().coerceAtLeast(1)),
            ItemStack(Material.MAGMA_CREAM, ((10..20).random() * (1.0 + (data.worldTier - 1) * 0.3)).toInt().coerceAtLeast(1)),
            ItemStack(Material.FIRE_CHARGE, ((5..15).random() * (1.0 + (data.worldTier - 1) * 0.3)).toInt().coerceAtLeast(1))
        )
        
        guaranteedDrops.forEach { item ->
            location.world?.dropItemNaturally(location, item)
        }
        
        // Custom Items (scale with tier)
        val customItemCount = (2 + data.worldTier).coerceAtMost(5)
        repeat(customItemCount) {
            val quality = when {
                Math.random() < 0.3 -> Quality.LEGENDARY
                Math.random() < 0.6 -> Quality.EPIC
                else -> Quality.RARE
            }
            
            val materials = listOf(
                Material.NETHERITE_SWORD,
                Material.NETHERITE_AXE,
                Material.NETHERITE_PICKAXE,
                Material.NETHERITE_HELMET,
                Material.NETHERITE_CHESTPLATE,
                Material.NETHERITE_LEGGINGS,
                Material.NETHERITE_BOOTS
            )
            
            val material = materials.random()
            val item = plugin.itemManager.createItem(material, quality)
            location.world?.dropItemNaturally(location, item)
        }
        
        // Enchanted Books (guaranteed)
        repeat(data.worldTier) {
            val bookQuality = if (data.worldTier >= 4) Quality.LEGENDARY else Quality.EPIC
            val enchantedBook = plugin.enchantmentManager.createRandomEnchantedBook(bookQuality)
            location.world?.dropItemNaturally(location, enchantedBook)
        }
        
        // Inferno-Set Item (Unique Boss Drop) - 15% Chance
        val infernoSetChance = plugin.config.getDouble("bosses.harvester.inferno-set-chance", 0.15)
        if (Math.random() < infernoSetChance) {
            val infernoMaterials = listOf(
                Material.NETHERITE_HELMET,
                Material.NETHERITE_CHESTPLATE,
                Material.NETHERITE_LEGGINGS,
                Material.NETHERITE_BOOTS
            )
            val material = infernoMaterials.random()
            val infernoItem = plugin.itemManager.createItem(material, Quality.LEGENDARY)
            
            // Add Inferno Set Tag
            val meta = infernoItem.itemMeta!!
            val setKey = org.bukkit.NamespacedKey(plugin, "armor_set")
            meta.persistentDataContainer.set(setKey, org.bukkit.persistence.PersistentDataType.STRING, "INFERNO")
            
            // Update lore
            val lore = meta.lore()?.toMutableList() ?: mutableListOf()
            lore.add(net.kyori.adventure.text.Component.text(""))
            lore.add(net.kyori.adventure.text.Component.text("§6§l✦ INFERNO-SET ✦", net.kyori.adventure.text.format.TextColor.color(255, 80, 20)))
            lore.add(net.kyori.adventure.text.Component.text("§7Der Ernter's Erbe", net.kyori.adventure.text.format.NamedTextColor.GRAY))
            meta.lore(lore)
            infernoItem.itemMeta = meta
            
            location.world?.dropItemNaturally(location, infernoItem)
            
            // Announce rare drop
            location.world?.players?.forEach { player ->
                player.sendMessage(net.kyori.adventure.text.Component.text("§6§l✦ SELTENER DROP: §c${meta.displayName()} §6(Inferno-Set)"))
                player.playSound(player.location, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f)
            }
        }
    }

    /**
     * Announce Boss Spawn
     */
    private fun announceSpawn(location: Location, worldTier: Int) {
        val world = location.world ?: return
        val message = Component.text()
            .append(Component.text("⚔ ", BOSS_COLOR, TextDecoration.BOLD))
            .append(Component.text("Der Ernter ", BOSS_COLOR, TextDecoration.BOLD))
            .append(Component.text("ist erschienen! ", NamedTextColor.YELLOW))
            .append(Component.text("(Tier $worldTier)", NamedTextColor.GRAY))
            .build()
        
        world.players.forEach { it.sendMessage(message) }
        world.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1f, 0.7f)
    }

    /**
     * Announce Phase Change
     */
    private fun announcePhase(location: Location, message: String) {
        val world = location.world ?: return
        world.getNearbyEntities(location, 50.0, 50.0, 50.0)
            .filterIsInstance<Player>()
            .forEach { player ->
                player.sendMessage(message)
                player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1.2f)
            }
    }

    /**
     * Announce Boss Death
     */
    private fun announceDeath(location: Location, worldTier: Int) {
        val world = location.world ?: return
        val message = Component.text()
            .append(Component.text("⚔ ", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("Der Ernter ", BOSS_COLOR, TextDecoration.BOLD))
            .append(Component.text("wurde besiegt! ", NamedTextColor.GREEN))
            .append(Component.text("(Tier $worldTier)", NamedTextColor.GRAY))
            .build()
        
        world.players.forEach { it.sendMessage(message) }
        world.playSound(location, Sound.ENTITY_WITHER_DEATH, 1f, 0.5f)
    }
}
