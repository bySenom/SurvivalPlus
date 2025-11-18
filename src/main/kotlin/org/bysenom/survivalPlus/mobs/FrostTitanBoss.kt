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
 * Frost Titan - Endgame Ice Peaks Boss
 * 
 * 4 Phasen Boss mit Freeze/Ice Mechaniken:
 * - Phase 1 (100-75%): Slow Effects, Ice Spike Summons
 * - Phase 2 (75-50%): Frost Aura, Ice Walls
 * - Phase 3 (50-25%): Blizzard, Heavy Slow
 * - Phase 4 (25-0%): Absolute Zero, Instant Freeze
 */
class FrostTitanBoss(private val plugin: SurvivalPlus) {

    private val activeBosses = ConcurrentHashMap<IronGolem, BossData>()
    private val bossBarKey = NamespacedKey(plugin, "frost_titan_boss")
    private val frozenPlayers = ConcurrentHashMap<Player, Long>() // Player -> Unfreeze Time

    companion object {
        private val BOSS_COLOR = TextColor.color(100, 200, 255) // Ice Blue
        
        // Base Stats (scale with World Tier)
        const val BASE_HEALTH = 600.0
        const val BASE_DAMAGE = 12.0
        const val BASE_ARMOR = 15.0
        
        // Phase Thresholds
        const val PHASE_2_HP = 0.75
        const val PHASE_3_HP = 0.50
        const val PHASE_4_HP = 0.25
        
        // Abilities Cooldowns (ticks)
        const val ICE_SPIKE_COOLDOWN = 150L  // 7.5 Sekunden
        const val FROST_AURA_COOLDOWN = 200L // 10 Sekunden
        const val BLIZZARD_COOLDOWN = 300L   // 15 Sekunden
        const val FREEZE_DURATION = 3000L    // 3 Sekunden Freeze
    }

    /**
     * Spawnt den Frost Titan Boss
     */
    fun spawn(location: Location, worldTier: Int = 1): IronGolem? {
        val world = location.world ?: return null
        
        // Spawne Iron Golem
        val boss = world.spawnEntity(location, EntityType.IRON_GOLEM) as IronGolem
        
        // Setup Boss
        setupBossEntity(boss, worldTier)
        val bossBar = setupBossBar(boss, worldTier)
        
        // Boss Data
        val data = BossData(
            boss = boss,
            worldTier = worldTier,
            phase = 1,
            spawnLocation = location,
            lastIceSpike = 0L,
            lastFrostAura = 0L,
            lastBlizzard = 0L,
            bossBar = bossBar
        )
        
        activeBosses[boss] = data
        
        // Start AI Loop
        startBossAI(data)
        
        // Announce
        announceSpawn(location, worldTier)
        
        plugin.logger.info("[FrostTitan] Boss spawned at ${location.blockX}, ${location.blockY}, ${location.blockZ} (Tier $worldTier)")
        
        return boss
    }

    /**
     * Setup Boss Entity Stats
     */
    private fun setupBossEntity(boss: IronGolem, worldTier: Int) {
        // Stats skalieren mit World Tier
        val tierMultiplier = 1.0 + (worldTier - 1) * 0.5 // +50% pro Tier
        
        val maxHealth = BASE_HEALTH * tierMultiplier
        val damage = BASE_DAMAGE * tierMultiplier
        val armor = BASE_ARMOR * tierMultiplier
        
        boss.getAttribute(Attribute.MAX_HEALTH)?.baseValue = maxHealth
        boss.health = maxHealth
        boss.getAttribute(Attribute.ATTACK_DAMAGE)?.baseValue = damage
        boss.getAttribute(Attribute.ARMOR)?.baseValue = armor
        boss.getAttribute(Attribute.KNOCKBACK_RESISTANCE)?.baseValue = 0.9
        boss.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue = 0.22
        
        // Boss Name
        boss.customName(Component.text("Frost-Titan").color(BOSS_COLOR).decorate(TextDecoration.BOLD))
        boss.isCustomNameVisible = true
        
        // Boss Marker
        boss.persistentDataContainer.set(bossBarKey, PersistentDataType.INTEGER, worldTier)
        
        // Boss Effects
        boss.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, Int.MAX_VALUE, 1, false, false))
        boss.addPotionEffect(PotionEffect(PotionEffectType.SLOW_FALLING, Int.MAX_VALUE, 0, false, false))
    }

    /**
     * Setup Boss Bar
     */
    private fun setupBossBar(boss: IronGolem, worldTier: Int): BossBar {
        val bossBar = Bukkit.createBossBar(
            "§b§lFrost-Titan §8(Tier $worldTier) §c❤",
            BarColor.BLUE,
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
            
            // Update Frozen Players
            updateFrozenPlayers()
            
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
            val oldPhase = data.phase
            data.phase = newPhase
            onPhaseChange(data, oldPhase, newPhase)
        }
    }

    /**
     * Phase Change Handler
     */
    private fun onPhaseChange(data: BossData, oldPhase: Int, newPhase: Int) {
        val boss = data.boss
        val location = boss.location
        
        when (newPhase) {
            2 -> {
                // Phase 2: Frost Aura aktiviert
                announcePhase(location, 2, "§bDer Frost-Titan entfesselt seine eisige Aura!")
                boss.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 0, false, false))
                
                // Visual Effect
                location.world?.spawnParticle(Particle.SNOWFLAKE, location, 100, 3.0, 3.0, 3.0, 0.1)
                location.world?.playSound(location, Sound.BLOCK_GLASS_BREAK, 2f, 0.5f)
            }
            3 -> {
                // Phase 3: Blizzard aktiviert
                announcePhase(location, 3, "§3Ein eisiger Blizzard bricht herein!")
                boss.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, Int.MAX_VALUE, 2, false, false))
                
                // Visual Effect
                location.world?.spawnParticle(Particle.SNOWFLAKE, location, 200, 5.0, 5.0, 5.0, 0.2)
                location.world?.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1f, 1.5f)
            }
            4 -> {
                // Phase 4: Absolute Zero
                announcePhase(location, 4, "§9§lABSOLUTER NULLPUNKT!")
                boss.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 1, false, false))
                boss.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, Int.MAX_VALUE, 3, false, false))
                
                // Visual Effect
                location.world?.spawnParticle(Particle.FALLING_DUST, location, 300, 8.0, 8.0, 8.0, 0.3, Material.SNOW_BLOCK.createBlockData())
                location.world?.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 2f, 0.5f)
            }
        }
    }

    /**
     * Execute Boss Abilities basierend auf Phase
     */
    private fun executeAbilities(data: BossData) {
        val now = System.currentTimeMillis()
        val boss = data.boss
        
        // Phase 1+: Ice Spike Summons
        if (now - data.lastIceSpike > ICE_SPIKE_COOLDOWN * 50) {
            summonIceSpikes(data)
            data.lastIceSpike = now
        }
        
        // Phase 2+: Frost Aura
        if (data.phase >= 2 && now - data.lastFrostAura > FROST_AURA_COOLDOWN * 50) {
            castFrostAura(data)
            data.lastFrostAura = now
        }
        
        // Phase 3+: Blizzard
        if (data.phase >= 3 && now - data.lastBlizzard > BLIZZARD_COOLDOWN * 50) {
            castBlizzard(data)
            data.lastBlizzard = now
        }
        
        // Phase 4: Instant Freeze (passive, handled in damage listener)
    }

    /**
     * Summon Ice Spikes
     */
    private fun summonIceSpikes(data: BossData) {
        val boss = data.boss
        val target = boss.target as? Player ?: return
        val targetLoc = target.location
        
        val amount = when (data.phase) {
            1 -> 3
            2, 3 -> 5
            4 -> 8 // Absolute Zero: mehr Spikes
            else -> 3
        }
        
        // Warning particles first
        repeat(amount) {
            val offset = (it - amount / 2) * 2.0
            val spikeLoc = targetLoc.clone().add(offset, 0.0, 0.0)
            spikeLoc.world?.spawnParticle(Particle.SNOWFLAKE, spikeLoc, 20, 0.5, 2.0, 0.5, 0.0)
        }
        
        targetLoc.world?.playSound(targetLoc, Sound.BLOCK_GLASS_PLACE, 1f, 0.8f)
        
        // After 1 second: spawn damage spikes
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            repeat(amount) {
                val offset = (it - amount / 2) * 2.0
                val spikeLoc = targetLoc.clone().add(offset, 0.0, 0.0)
                
                // Damage + Slow
                spikeLoc.world?.getNearbyEntities(spikeLoc, 1.5, 3.0, 1.5)?.forEach { entity ->
                    if (entity is Player) {
                        entity.damage(8.0 * data.worldTier, boss)
                        entity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 100, 2))
                        entity.addPotionEffect(PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 1))
                    }
                }
                
                // Visual
                spikeLoc.world?.spawnParticle(Particle.BLOCK, spikeLoc.clone().add(0.0, 1.0, 0.0), 50, 0.5, 1.0, 0.5, 0.0, Material.PACKED_ICE.createBlockData())
            }
            
            targetLoc.world?.playSound(targetLoc, Sound.BLOCK_GLASS_BREAK, 2f, 0.5f)
        }, 20L)
    }

    /**
     * Cast Frost Aura
     */
    private fun castFrostAura(data: BossData) {
        val boss = data.boss
        val location = boss.location
        val radius = 10.0
        
        // Find all players in range
        location.world?.getNearbyEntities(location, radius, radius, radius)
            ?.filterIsInstance<Player>()
            ?.forEach { player ->
                val distance = player.location.distance(location)
                val intensity = ((radius - distance) / radius).coerceIn(0.0, 1.0)
                
                // Apply slow based on distance (closer = stronger)
                val slowLevel = (intensity * 3).toInt().coerceAtMost(3)
                player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 100, slowLevel))
                player.addPotionEffect(PotionEffect(PotionEffectType.MINING_FATIGUE, 100, slowLevel))
                
                // Damage over time
                if (data.phase >= 3) {
                    player.damage(2.0 * data.worldTier * intensity, boss)
                }
                
                // Freeze effect (Phase 4)
                if (data.phase >= 4 && intensity > 0.7 && Math.random() < 0.3) {
                    freezePlayer(player)
                }
            }
        
        // Visual: expanding frost rings
        for (i in 1..3) {
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                val ringRadius = radius * i / 3.0
                for (angle in 0 until 36) {
                    val rad = Math.toRadians(angle * 10.0)
                    val x = location.x + ringRadius * Math.cos(rad)
                    val z = location.z + ringRadius * Math.sin(rad)
                    val particleLoc = Location(location.world, x, location.y + 0.1, z)
                    location.world?.spawnParticle(Particle.SNOWFLAKE, particleLoc, 3, 0.1, 0.1, 0.1, 0.0)
                }
            }, (i * 5).toLong())
        }
        
        location.world?.playSound(location, Sound.BLOCK_SNOW_BREAK, 2f, 0.5f)
    }

    /**
     * Cast Blizzard
     */
    private fun castBlizzard(data: BossData) {
        val boss = data.boss
        val location = boss.location
        val radius = 15.0
        val duration = 10L // 10 Sekunden
        
        var ticks = 0L
        val task = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            if (ticks++ >= duration * 20) return@Runnable
            
            // Spawn snow particles everywhere
            location.world?.spawnParticle(Particle.FALLING_DUST, location, 100, radius, 10.0, radius, 0.1, Material.SNOW_BLOCK.createBlockData())
            
            // Damage + Effects every second
            if (ticks % 20L == 0L) {
                location.world?.getNearbyEntities(location, radius, 10.0, radius)
                    ?.filterIsInstance<Player>()
                    ?.forEach { player ->
                        player.damage(3.0 * data.worldTier, boss)
                        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 40, 3))
                        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 0))
                        
                        // Phase 4: Chance to freeze
                        if (data.phase >= 4 && Math.random() < 0.2) {
                            freezePlayer(player)
                        }
                    }
            }
        }, 0L, 1L)
        
        plugin.server.scheduler.runTaskLater(plugin, Runnable { task.cancel() }, duration * 20)
        
        location.world?.playSound(location, Sound.ENTITY_ENDER_DRAGON_FLAP, 2f, 1.5f)
        announcePhase(location, data.phase, "§3Ein eisiger Blizzard wütet!")
    }

    /**
     * Freeze Player (Phase 4)
     */
    private fun freezePlayer(player: Player) {
        val freezeUntil = System.currentTimeMillis() + FREEZE_DURATION
        frozenPlayers[player] = freezeUntil
        
        // Visual + Effects
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, FREEZE_DURATION.toInt() / 50, 10))
        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, FREEZE_DURATION.toInt() / 50, 250)) // Can't jump
        player.addPotionEffect(PotionEffect(PotionEffectType.MINING_FATIGUE, FREEZE_DURATION.toInt() / 50, 10))
        
        player.sendMessage("§b§lDu bist eingefroren!")
        player.world.spawnParticle(Particle.BLOCK, player.location.add(0.0, 1.0, 0.0), 100, 0.5, 1.0, 0.5, 0.0, Material.ICE.createBlockData())
        player.world.playSound(player.location, Sound.BLOCK_GLASS_BREAK, 1f, 0.5f)
    }

    /**
     * Update Frozen Players
     */
    private fun updateFrozenPlayers() {
        val now = System.currentTimeMillis()
        val toRemove = mutableListOf<Player>()
        
        frozenPlayers.forEach { (player, unfreezeTime) ->
            if (now >= unfreezeTime) {
                // Unfreeze
                player.sendMessage("§aDu kannst dich wieder bewegen!")
                player.world.spawnParticle(Particle.SNOWFLAKE, player.location.add(0.0, 1.0, 0.0), 50, 0.5, 1.0, 0.5, 0.1)
                toRemove.add(player)
            } else {
                // Keep frozen (particles)
                player.world.spawnParticle(Particle.FALLING_DUST, player.location.add(0.0, 1.0, 0.0), 5, 0.3, 0.5, 0.3, 0.0, Material.SNOW_BLOCK.createBlockData())
            }
        }
        
        toRemove.forEach { frozenPlayers.remove(it) }
    }

    /**
     * Spawn Phase-specific Particles
     */
    private fun spawnPhaseParticles(data: BossData) {
        val location = data.boss.location
        
        when (data.phase) {
            1 -> location.world?.spawnParticle(Particle.SNOWFLAKE, location.clone().add(0.0, 1.5, 0.0), 3, 0.3, 0.5, 0.3, 0.0)
            2 -> location.world?.spawnParticle(Particle.FALLING_DUST, location.clone().add(0.0, 1.5, 0.0), 5, 0.4, 0.6, 0.4, 0.0, Material.SNOW_BLOCK.createBlockData())
            3 -> location.world?.spawnParticle(Particle.SNOWFLAKE, location.clone().add(0.0, 1.5, 0.0), 8, 0.5, 0.8, 0.5, 0.01)
            4 -> {
                location.world?.spawnParticle(Particle.FALLING_DUST, location.clone().add(0.0, 2.0, 0.0), 15, 0.8, 1.2, 0.8, 0.02, Material.SNOW_BLOCK.createBlockData())
                location.world?.spawnParticle(Particle.SNOWFLAKE, location.clone().add(0.0, 1.5, 0.0), 10, 1.0, 1.0, 1.0, 0.1)
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
        bossBar.setTitle("§b§lFrost-Titan §7(Phase ${data.phase}) §c❤ §7${String.format("%.1f", hpPercent * 100)}%")
        
        // Change color by phase
        bossBar.color = when (data.phase) {
            1 -> BarColor.BLUE
            2 -> BarColor.WHITE
            3 -> BarColor.PURPLE
            4 -> BarColor.PINK
            else -> BarColor.BLUE
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
        data.boss.location.world?.playSound(data.boss.location, Sound.BLOCK_GLASS_BREAK, 2f, 0.5f)
        
        plugin.logger.info("[FrostTitan] Boss defeated (Tier ${data.worldTier})")
    }

    /**
     * Drop Boss Loot
     */
    private fun dropLoot(data: BossData) {
        val location = data.boss.location
        val tierMultiplier = 1.0 + (data.worldTier - 1) * 0.3
        
        // Guaranteed Drops
        val guaranteedDrops = listOf(
            ItemStack(Material.DIAMOND, (3..8).random()),
            ItemStack(Material.PACKED_ICE, (10..20).random()),
            ItemStack(Material.BLUE_ICE, (5..15).random()),
            ItemStack(Material.SNOWBALL, (20..40).random())
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
                Material.DIAMOND_SWORD,
                Material.DIAMOND_AXE,
                Material.DIAMOND_PICKAXE,
                Material.DIAMOND_HELMET,
                Material.DIAMOND_CHESTPLATE,
                Material.DIAMOND_LEGGINGS,
                Material.DIAMOND_BOOTS
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
        
        // Glacial-Set Item (Unique Boss Drop) - 15% Chance
        val glacialSetChance = plugin.config.getDouble("bosses.frost-titan.glacial-set-chance", 0.15)
        if (Math.random() < glacialSetChance) {
            val glacialMaterials = listOf(
                Material.DIAMOND_HELMET,
                Material.DIAMOND_CHESTPLATE,
                Material.DIAMOND_LEGGINGS,
                Material.DIAMOND_BOOTS
            )
            val material = glacialMaterials.random()
            val glacialItem = plugin.itemManager.createItem(material, Quality.LEGENDARY)
            
            // Add Glacial Set Tag
            val meta = glacialItem.itemMeta!!
            val setKey = org.bukkit.NamespacedKey(plugin, "armor_set")
            meta.persistentDataContainer.set(setKey, org.bukkit.persistence.PersistentDataType.STRING, "GLACIAL")
            
            // Update lore
            val lore = meta.lore()?.toMutableList() ?: mutableListOf()
            lore.add(net.kyori.adventure.text.Component.text(""))
            lore.add(net.kyori.adventure.text.Component.text("§b§l❄ GLACIAL-SET ❄", net.kyori.adventure.text.format.TextColor.color(100, 200, 255)))
            lore.add(net.kyori.adventure.text.Component.text("§7Frost-Titan's Erbe", net.kyori.adventure.text.format.NamedTextColor.GRAY))
            meta.lore(lore)
            glacialItem.itemMeta = meta
            
            location.world?.dropItemNaturally(location, glacialItem)
            
            // Announce rare drop
            location.world?.players?.forEach { player ->
                player.sendMessage(net.kyori.adventure.text.Component.text("§b§l❄ SELTENER DROP: §3${meta.displayName()} §b(Glacial-Set)"))
                player.playSound(player.location, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.5f)
            }
        }
    }

    /**
     * Announce Boss Spawn
     */
    private fun announceSpawn(location: Location, worldTier: Int) {
        val world = location.world ?: return
        val message = Component.text()
            .append(Component.text("❄ ", BOSS_COLOR, TextDecoration.BOLD))
            .append(Component.text("Der Frost-Titan ", BOSS_COLOR, TextDecoration.BOLD))
            .append(Component.text("ist erschienen! ", NamedTextColor.AQUA))
            .append(Component.text("(Tier $worldTier)", NamedTextColor.GRAY))
            .build()
        
        world.players.forEach { it.sendMessage(message) }
        world.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f)
    }

    /**
     * Announce Phase Change
     */
    private fun announcePhase(location: Location, phase: Int, message: String) {
        val world = location.world ?: return
        world.getNearbyEntities(location, 50.0, 50.0, 50.0)
            .filterIsInstance<Player>()
            .forEach { player ->
                player.sendMessage(message)
                player.playSound(player.location, Sound.BLOCK_GLASS_BREAK, 1f, 0.8f)
            }
    }

    /**
     * Announce Boss Death
     */
    private fun announceDeath(location: Location, worldTier: Int) {
        val world = location.world ?: return
        val message = Component.text()
            .append(Component.text("❄ ", NamedTextColor.AQUA, TextDecoration.BOLD))
            .append(Component.text("Der Frost-Titan ", BOSS_COLOR, TextDecoration.BOLD))
            .append(Component.text("wurde besiegt! ", NamedTextColor.GREEN))
            .append(Component.text("(Tier $worldTier)", NamedTextColor.GRAY))
            .build()
        
        world.players.forEach { it.sendMessage(message) }
    }

    /**
     * Cleanup
     */
    fun cleanup() {
        activeBosses.values.forEach { data ->
            data.aiTask?.cancel()
            data.bossBar?.removeAll()
            data.boss.remove()
        }
        activeBosses.clear()
        frozenPlayers.clear()
    }

    /**
     * Boss Data Class
     */
    data class BossData(
        val boss: IronGolem,
        val worldTier: Int,
        var phase: Int,
        val spawnLocation: Location,
        var lastIceSpike: Long,
        var lastFrostAura: Long,
        var lastBlizzard: Long,
        var aiTask: BukkitTask? = null,
        var bossBar: BossBar? = null
    )
}
