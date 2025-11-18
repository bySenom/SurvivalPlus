package org.bysenom.survivalPlus.mobs

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bysenom.survivalPlus.SurvivalPlus
import java.time.Duration
import org.bysenom.survivalPlus.models.Quality
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random

/**
 * Custom Boss: The Butcher (aus Diablo)
 *
 * Features:
 * - Spawnt nur ab World Tier Heroic (Tier 2+)
 * - Extrem hohes HP und Schaden
 * - Spezielle Fähigkeiten (Bleed, Charge, Cleave)
 * - Boss-Bar mit HP-Anzeige
 * - Garantierte Legendary+ Drops
 * - Sound-Effekte und Partikel
 * - "FRESH MEAT!" Spawn-Nachricht
 */
class ButcherBoss(private val plugin: SurvivalPlus) {

    companion object {
        const val MIN_WORLD_TIER = 2 // Heroic
        const val SPAWN_CHANCE = 0.001 // 0.1% Chance beim Mob-Spawn
        const val BOSS_TAG = "butcher_boss"

        // Butcher Stats
        const val BASE_HEALTH = 500.0
        const val BASE_DAMAGE = 15.0
        const val MOVEMENT_SPEED = 0.35
        const val KNOCKBACK_RESISTANCE = 0.9

        // Abilities
        const val BLEED_CHANCE = 0.30 // 30% Chance on hit
        const val CHARGE_COOLDOWN = 10000L // 10 Sekunden
        const val CLEAVE_RANGE = 5.0
    }

    private val butcherKey = NamespacedKey(plugin, "butcher_boss")
    private val lastChargeKey = NamespacedKey(plugin, "butcher_last_charge")

    // Aktive Butcher Bosse
    private val activeButchers = mutableMapOf<Entity, BossBar>()

    /**
     * Versucht einen Butcher zu spawnen
     */
    fun trySpawnButcher(location: Location): Zombie? {
        val world = location.world ?: return null

        // Nicht in Boss Arena spawnen
        if (world.name == "Survival_boss") return null

        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(world)) return null

        // Prüfe World Tier
        val worldTier = plugin.worldTierManager.getWorldTier(world)
        if (worldTier.tier < MIN_WORLD_TIER) return null

        // Spawn-Chance
        if (Random.nextDouble() > SPAWN_CHANCE) return null

        // Spawn Butcher
        return spawnButcher(location, worldTier.tier)
    }

    /**
     * Spawnt einen Butcher Boss
     */
    fun spawnButcher(location: Location, worldTier: Int): Zombie {
        val world = location.world!!

        // Spawn Zombie als Butcher
        val butcher = world.spawn(location, Zombie::class.java)


        butcher.equipment?.helmet = ItemStack(Material.IRON_HELMET).apply {
            addUnsafeEnchantment(Enchantment.PROTECTION, 5)
        }

        val chestplate = ItemStack(Material.LEATHER_CHESTPLATE)
        val chestMeta = chestplate.itemMeta as? org.bukkit.inventory.meta.LeatherArmorMeta
        chestMeta?.setColor(Color.RED)
        chestplate.itemMeta = chestMeta
        chestplate.addUnsafeEnchantment(Enchantment.PROTECTION, 5)
        butcher.equipment?.chestplate = chestplate

        butcher.equipment?.setItemInMainHand(ItemStack(Material.IRON_AXE).apply {
            addUnsafeEnchantment(Enchantment.SHARPNESS, 8)
        })

        // Custom Name
        butcher.customName(Component.text("⚔ THE BUTCHER ⚔")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.BOLD, true))
        butcher.isCustomNameVisible = true

        // Attribute
        val tierMultiplier = 1.0 + (worldTier - 1) * 0.5 // +50% pro Tier

        butcher.getAttribute(Attribute.MAX_HEALTH)?.baseValue = BASE_HEALTH * tierMultiplier
        butcher.health = BASE_HEALTH * tierMultiplier

        butcher.getAttribute(Attribute.ATTACK_DAMAGE)?.baseValue = BASE_DAMAGE * tierMultiplier
        butcher.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue = MOVEMENT_SPEED
        butcher.getAttribute(Attribute.KNOCKBACK_RESISTANCE)?.baseValue = KNOCKBACK_RESISTANCE
        butcher.getAttribute(Attribute.ARMOR)?.baseValue = 10.0
        butcher.getAttribute(Attribute.ARMOR_TOUGHNESS)?.baseValue = 8.0

        // Boss-Effekte
        butcher.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, Int.MAX_VALUE, 0, false, false))
        butcher.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, Int.MAX_VALUE, worldTier - 2, false, false))

        // Boss-Tag
        butcher.scoreboardTags.add(BOSS_TAG)
        butcher.scoreboardTags.add("special_mob")
        butcher.persistentDataContainer.set(butcherKey, PersistentDataType.INTEGER, worldTier)
        butcher.persistentDataContainer.set(lastChargeKey, PersistentDataType.LONG, 0L)

        // Boss-Bar erstellen
        createBossBar(butcher)

        // Spawn-Effekte
        playSpawnEffects(butcher)

        // Spawn-Nachricht
        broadcastSpawnMessage(location)

        // Start AI Task
        startButcherAI(butcher)

        plugin.logger.info("[Butcher] Spawned in ${world.name} at ${location.blockX}, ${location.blockY}, ${location.blockZ} (Tier $worldTier)")

        return butcher
    }

    /**
     * Prüft ob Entity ein Butcher ist
     */
    fun isButcher(entity: Entity): Boolean {
        return entity.scoreboardTags.contains(BOSS_TAG)
    }

    /**
     * Erstellt die Boss-Bar
     */
    private fun createBossBar(butcher: Zombie) {
        val bossBar = Bukkit.createBossBar(
            "§c§l⚔ THE BUTCHER ⚔", BarColor.RED,
            BarStyle.SEGMENTED_10
        )

        bossBar.progress = 1.0
        bossBar.isVisible = true

        activeButchers[butcher] = bossBar

        // Update Boss-Bar
        object : BukkitRunnable() {
            override fun run() {
                if (!butcher.isValid || butcher.isDead) {
                    removeBossBar(butcher)
                    cancel()
                    return
                }

                // Update Progress
                val health = butcher.health
                val maxHealth = butcher.getAttribute(Attribute.MAX_HEALTH)?.value ?: 1.0
                bossBar.progress = (health / maxHealth).coerceIn(0.0, 1.0)

                // Update Title mit HP
                val hpPercent = (bossBar.progress * 100).toInt()
                bossBar.setTitle("§c§l⚔ THE BUTCHER ⚔ §7[$hpPercent%]")

                // Update Spieler in Range
                val players = butcher.location.getNearbyPlayers(50.0)
                bossBar.removeAll()
                players.forEach { bossBar.addPlayer(it) }

                // Rage Mode bei niedrigem HP
                if (bossBar.progress < 0.3) {
                    bossBar.color = BarColor.PURPLE
                    if (!butcher.hasPotionEffect(PotionEffectType.SPEED)) {
                        butcher.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 1, false, false))
                        butcher.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, Int.MAX_VALUE, 2, false, false))
                        broadcastRageMode(butcher)
                    }
                }
            }
        }.runTaskTimer(plugin, 10L, 10L)
    }

    /**
     * Entfernt die Boss-Bar
     */
    private fun removeBossBar(butcher: Entity) {
        activeButchers.remove(butcher)?.let { bossBar ->
            bossBar.removeAll()
        }
    }

    /**
     * Spawn-Effekte
     */
    private fun playSpawnEffects(butcher: Zombie) {
        val location = butcher.location

        // Partikel-Explosion
        location.world.spawnParticle(
            Particle.EXPLOSION,
            location.clone().add(0.0, 1.0, 0.0),
            20, 0.5, 0.5, 0.5, 0.1
        )

        location.world.spawnParticle(
            Particle.ASH,
            location.clone().add(0.0, 1.0, 0.0),
            50, 1.0, 1.0, 1.0, 0.1
        )

        // Sound
        location.world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.5f)
        location.world.playSound(location, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 2f, 0.5f)

        // Lightning (visual only)
        location.world.strikeLightningEffect(location)
    }

    /**
     * Spawn-Nachricht broadcast
     */
    private fun broadcastSpawnMessage(location: Location) {
        val world = location.world!!
        val x = location.blockX
        val y = location.blockY
        val z = location.blockZ
        
        // Konvertiere Component zu Plain Text für Tier $worldTier
        val tierComponent = plugin.worldTierManager.getWorldTier(world).getDisplayComponent()
        val tierText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(tierComponent)

        world.players.forEach { player ->
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            player.sendMessage("")
            player.sendMessage("  §c§l⚔ FRESH MEAT! ⚔")
            player.sendMessage("")
            player.sendMessage("  §7The §c§lBUTCHER §7has spawned!")
            player.sendMessage("  §7Location: §f$x, $y, $z")
            player.sendMessage("  §7World Tier: §f$tierText")
            player.sendMessage("")
            player.playSound(player.location, Sound.ENTITY_WITHER_SPAWN, 0.5f, 0.5f)
            player.showTitle(Title.title(
                Component.text("⚔ FRESH MEAT! ⚔")
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.BOLD, true),
                Component.text("The Butcher has spawned")
                    .color(NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofSeconds(1))
            ))
        }
    }

    /**
     * Rage Mode Nachricht
     */
    private fun broadcastRageMode(butcher: Zombie) {
        butcher.location.getNearbyPlayers(50.0).forEach { player ->
            player.sendMessage("§c§l⚔ THE BUTCHER ENTERS RAGE MODE! ⚔")
            player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 0.5f)
        }

        // Effect
        butcher.location.world.spawnParticle(
            Particle.ANGRY_VILLAGER,
            butcher.location.clone().add(0.0, 2.0, 0.0),
            30, 1.0, 1.0, 1.0, 0.1
        )
    }

    /**
     * Butcher AI
     */
    private fun startButcherAI(butcher: Zombie) {
        object : BukkitRunnable() {
            override fun run() {
                if (!butcher.isValid || butcher.isDead) {
                    cancel()
                    return
                }

                val target = butcher.target as? Player ?: return
                val distance = butcher.location.distance(target.location)

                // Charge Ability
                val lastCharge = butcher.persistentDataContainer.get(lastChargeKey, PersistentDataType.LONG) ?: 0L
                val now = System.currentTimeMillis()

                if (now - lastCharge > CHARGE_COOLDOWN && distance > 5.0 && distance < 20.0) {
                    performCharge(butcher, target)
                    butcher.persistentDataContainer.set(lastChargeKey, PersistentDataType.LONG, now)
                }

                // Cleave bei nahen Spielern
                if (distance < CLEAVE_RANGE) {
                    performCleave(butcher)
                }

                // Blut-Partikel
                if (Random.nextDouble() < 0.3) {
                    butcher.location.world.spawnParticle(
                        Particle.DUST,
                        butcher.location.clone().add(0.0, 1.0, 0.0),
                        3, 0.3, 0.5, 0.3, 0.0,
                        Particle.DustOptions(Color.RED, 1.0f)
                    )
                }
            }
        }.runTaskTimer(plugin, 20L, 20L) // Jede Sekunde
    }

    /**
     * Charge Attack
     */
    private fun performCharge(butcher: Zombie, target: Player) {
        val direction = target.location.toVector().subtract(butcher.location.toVector()).normalize()
        butcher.velocity = direction.multiply(1.5).setY(0.3)

        butcher.location.world.playSound(butcher.location, Sound.ENTITY_RAVAGER_ROAR, 1f, 0.8f)
        butcher.location.world.spawnParticle(
            Particle.CLOUD,
            butcher.location,
            20, 0.3, 0.3, 0.3, 0.2
        )

        target.sendMessage("§c⚔ The Butcher charges at you!")
    }

    /**
     * Cleave Attack (AoE)
     */
    private fun performCleave(butcher: Zombie) {
        val nearbyPlayers = butcher.location.getNearbyPlayers(CLEAVE_RANGE)

        if (nearbyPlayers.isEmpty()) return

        val damage = (butcher.getAttribute(Attribute.ATTACK_DAMAGE)?.value ?: BASE_DAMAGE) * 0.7

        nearbyPlayers.forEach { player ->
            player.damage(damage, butcher)
            applyBleed(player)
            player.velocity = player.velocity.setY(0.5)
        }

        butcher.location.world.playSound(butcher.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.5f)
        butcher.location.world.spawnParticle(
            Particle.SWEEP_ATTACK,
            butcher.location.clone().add(0.0, 1.0, 0.0),
            10, 2.0, 0.5, 2.0, 0.0
        )
    }

    /**
     * Wendet Bleed-Effect an
     */
    fun applyBleed(player: Player) {
        if (Random.nextDouble() < BLEED_CHANCE) {
            player.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 100, 1, false, true))
            player.sendMessage("§4💉 You are bleeding!")
            player.playSound(player.location, Sound.ENTITY_PLAYER_HURT, 0.5f, 0.8f)
        }
    }

    /**
     * Butcher Tod - Drops und Effekte
     */
    fun onButcherDeath(butcher: Zombie, killer: Player?) {
        val location = butcher.location
        val world = location.world!!
        val worldTier = butcher.persistentDataContainer.get(butcherKey, PersistentDataType.INTEGER) ?: 2

        // Boss-Bar entfernen
        removeBossBar(butcher)

        // Tod-Effekte
        world.spawnParticle(Particle.EXPLOSION, location.clone().add(0.0, 1.0, 0.0), 30, 1.0, 1.0, 1.0, 0.1)
        world.spawnParticle(Particle.DUST, location.clone().add(0.0, 1.0, 0.0), 100, 2.0, 1.0, 2.0, 0.1, Particle.DustOptions(Color.RED, 1.0f))
        world.playSound(location, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 0.5f)
        world.strikeLightningEffect(location)

        // Drops
        generateButcherDrops(location, worldTier, killer)

        // Tod-Nachricht
        world.players.forEach { player ->
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            player.sendMessage("")
            player.sendMessage("  §c§l⚔ THE BUTCHER HAS BEEN SLAIN! ⚔")
            if (killer != null) {
                player.sendMessage("  §7Killed by: §f${killer.name}")
            }
            player.sendMessage("")
            player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
        }

        plugin.logger.info("[Butcher] Defeated in ${world.name} (Tier $worldTier)${if (killer != null) " by ${killer.name}" else ""}")
    }

    /**
     * Generiert Butcher Drops
     */
    private fun generateButcherDrops(location: Location, worldTier: Int, killer: Player?) {
        val world = location.world!!

        // Garantierte Legendary+ Enchanted Books (2-4 Stück)
        val bookCount = 2 + worldTier
        repeat(bookCount) {
            val quality = when {
                worldTier >= 5 && Random.nextDouble() < 0.40 -> Quality.MYTHIC
                worldTier >= 4 && Random.nextDouble() < 0.60 -> Quality.LEGENDARY
                else -> Quality.LEGENDARY
            }

            val book = plugin.enchantmentManager.createRandomEnchantedBook(quality)
            world.dropItemNaturally(location, book)
        }

        // Butcher's Cleaver (Spezielle Axt)
        if (Random.nextDouble() < 0.15) { // 15% Chance
            val cleaver = ItemStack(Material.NETHERITE_AXE)
            val meta = cleaver.itemMeta
            meta?.displayName(Component.text("⚔ Butcher's Cleaver ⚔")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true))

            val lore = mutableListOf<Component>()
            lore.add(Component.text("A blood-soaked weapon").color(NamedTextColor.GRAY))
            lore.add(Component.text("from The Butcher's arsenal").color(NamedTextColor.GRAY))
            lore.add(Component.empty())
            lore.add(Component.text("Lifesteal II").color(NamedTextColor.RED))
            lore.add(Component.text("Sharpness VII").color(NamedTextColor.RED))
            lore.add(Component.text("Cleave Effect").color(NamedTextColor.DARK_RED))
            meta?.lore(lore)

            cleaver.itemMeta = meta
            cleaver.addUnsafeEnchantment(Enchantment.SHARPNESS, 7)
            cleaver.addUnsafeEnchantment(Enchantment.LOOTING, 3)
            cleaver.addUnsafeEnchantment(Enchantment.UNBREAKING, 5)

            // Custom Enchantment
            plugin.enchantmentManager.addEnchantment(cleaver, org.bysenom.survivalPlus.enchantments.CustomEnchantment.LIFESTEAL, 2)

            world.dropItemNaturally(location, cleaver)

            killer?.sendMessage("§c§l⚔ You obtained the Butcher's Cleaver! ⚔")
        }

        // Bonus Custom Items (World Tier abhängig)
        val itemCount = 1 + (worldTier - 1)
        repeat(itemCount) {
            val quality = when {
                worldTier >= 5 && Random.nextDouble() < 0.30 -> Quality.MYTHIC
                worldTier >= 4 && Random.nextDouble() < 0.50 -> Quality.LEGENDARY
                worldTier >= 3 -> Quality.EPIC
                else -> Quality.RARE
            }

            val materials = listOf(
                Material.NETHERITE_SWORD,
                Material.NETHERITE_AXE,
                Material.NETHERITE_HELMET,
                Material.NETHERITE_CHESTPLATE,
                Material.NETHERITE_LEGGINGS,
                Material.NETHERITE_BOOTS
            )

            val item = plugin.itemManager.createItem(materials.random(), quality)
            world.dropItemNaturally(location, item)
        }

        // Geld/Resources
        world.dropItemNaturally(location, ItemStack(Material.EMERALD, 16 + worldTier * 8))
        world.dropItemNaturally(location, ItemStack(Material.DIAMOND, 8 + worldTier * 4))
    }

    /**
     * Cleanup bei Plugin-Disable
     */
    fun cleanup() {
        activeButchers.values.forEach { it.removeAll() }
        activeButchers.clear()
    }
}

