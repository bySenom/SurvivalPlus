package org.bysenom.survivalPlus.enchantments

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class EnchantmentListener(private val plugin: SurvivalPlus) : Listener {

    // Cooldown-System für starke Enchantments (Spieler UUID -> Enchantment -> Last Use Time)
    private val enchantmentCooldowns = mutableMapOf<java.util.UUID, MutableMap<CustomEnchantment, Long>>()

    /**
     * Prüft ob ein Enchantment vom Cooldown bereit ist
     */
    private fun canUseEnchantment(playerUUID: java.util.UUID, enchantment: CustomEnchantment, cooldownSeconds: Int): Boolean {
        val now = System.currentTimeMillis()
        val playerCooldowns = enchantmentCooldowns.getOrPut(playerUUID) { mutableMapOf() }
        val lastUse = playerCooldowns[enchantment] ?: 0L

        if (now - lastUse >= cooldownSeconds * 1000L) {
            playerCooldowns[enchantment] = now
            return true
        }
        return false
    }

    /**
     * Holt Cooldown aus Config
     */
    private fun getCooldownFromConfig(enchantment: CustomEnchantment): Int {
        return when (enchantment) {
            CustomEnchantment.EXPLOSIVE -> plugin.config.getInt("enchantment-balance.explosive-cooldown", 5)
            CustomEnchantment.THUNDER_STRIKE -> plugin.config.getInt("enchantment-balance.thunder-strike-cooldown", 8)
            else -> 0
        }
    }

    /**
     * Kampf-Enchantments: Lifesteal, Explosive, Vampire, Thunder Strike
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        if (event.damager !is Player) return

        val player = event.damager as Player
        val weapon = player.inventory.itemInMainHand

        if (weapon.type.isAir) return

        val enchantments = plugin.enchantmentManager.getEnchantments(weapon)

        enchantments.forEach { (enchantment, level) ->
            when (enchantment) {
                CustomEnchantment.LIFESTEAL -> handleLifesteal(player, event.finalDamage, level)
                CustomEnchantment.VAMPIRE -> handleVampire(player, event.finalDamage, level)
                CustomEnchantment.EXPLOSIVE -> {
                    val cooldown = getCooldownFromConfig(CustomEnchantment.EXPLOSIVE)
                    if (canUseEnchantment(player.uniqueId, CustomEnchantment.EXPLOSIVE, cooldown)) {
                        handleExplosive(event, level, player)
                    }
                }
                CustomEnchantment.THUNDER_STRIKE -> {
                    val cooldown = getCooldownFromConfig(CustomEnchantment.THUNDER_STRIKE)
                    if (canUseEnchantment(player.uniqueId, CustomEnchantment.THUNDER_STRIKE, cooldown)) {
                        handleThunderStrike(event, level, player)
                    }
                }
                else -> {}
            }
        }
    }

    private fun handleLifesteal(player: Player, damage: Double, level: Int) {
        val healAmount = damage * CustomEnchantment.LIFESTEAL.getEffectValue(level)
        val maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.value ?: 20.0
        val newHealth = min(player.health + healAmount, maxHealth)
        player.health = newHealth

        // Visual Feedback
        player.sendActionBar(net.kyori.adventure.text.Component.text("+${String.format("%.1f", healAmount)} ❤")
            .color(net.kyori.adventure.text.format.NamedTextColor.RED))
    }
    private fun handleVampire(player: Player, damage: Double, level: Int) {
        val healAmount = damage * CustomEnchantment.VAMPIRE.getEffectValue(level)
        val maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.value ?: 20.0
        val newHealth = min(player.health + healAmount, maxHealth)
        player.health = newHealth
    }

    private fun handleExplosive(event: EntityDamageByEntityEvent, level: Int, player: org.bukkit.entity.Player) {
        val chance = CustomEnchantment.EXPLOSIVE.getEffectValue(level)

        if (Math.random() < chance) {
            val entity = event.entity
            // Explosionsstärke aus Config (default: 0.5 pro Level)
            val multiplier = plugin.config.getDouble("enchantment-balance.explosive-power-multiplier", 0.5)
            val power = (1.0f + (multiplier * level)).toFloat()
            entity.world.createExplosion(
                entity.location,
                power,
                false,  // Kein Feuer
                false   // Kein Block-Schaden
            )

            // Visual Feedback
            player.sendActionBar(
                net.kyori.adventure.text.Component.text("💥 Explosive!")
                    .color(net.kyori.adventure.text.format.NamedTextColor.GOLD)
            )
        }
    }

    private fun handleThunderStrike(event: EntityDamageByEntityEvent, level: Int, player: org.bukkit.entity.Player) {
        val chance = CustomEnchantment.THUNDER_STRIKE.getEffectValue(level)

        if (Math.random() < chance) {
            val entity = event.entity
            entity.world.strikeLightning(entity.location)

            // Visual Feedback
            player.sendActionBar(
                net.kyori.adventure.text.Component.text("⚡ Thunder Strike!")
                    .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW)
            )
        }
    }

    /**
     * Rüstungs-Enchantments: Divine Protection, Thorns+, Speed Boost
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        if (event.entity !is Player) return

        val player = event.entity as Player

        // Prüfe alle Rüstungsteile
        val armor = listOfNotNull(
            player.inventory.helmet,
            player.inventory.chestplate,
            player.inventory.leggings,
            player.inventory.boots
        )

        armor.forEach { piece ->
            val enchantments = plugin.enchantmentManager.getEnchantments(piece)

            enchantments.forEach { (enchantment, level) ->
                when (enchantment) {
                    CustomEnchantment.DIVINE_PROTECTION -> handleDivineProtection(event, level)
                    CustomEnchantment.THORNS_PLUS -> handleThornsPlus(event, level)
                    else -> {}
                }
            }
        }
    }

    private fun handleDivineProtection(event: EntityDamageByEntityEvent, level: Int) {
        val reduction = CustomEnchantment.DIVINE_PROTECTION.getEffectValue(level)
        event.damage *= (1.0 - reduction)
    }

    private fun handleThornsPlus(event: EntityDamageByEntityEvent, level: Int) {
        if (event.damager !is LivingEntity) return

        val attacker = event.damager as LivingEntity
        val reflectMultiplier = CustomEnchantment.THORNS_PLUS.getEffectValue(level)
        val reflectDamage = event.finalDamage * reflectMultiplier

        attacker.damage(reflectDamage)
    }

    /**
     * Werkzeug-Enchantments: Auto Smelt, Vein Miner, Timber
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.isCancelled) return

        val player = event.player
        val tool = player.inventory.itemInMainHand

        if (tool.type.isAir) return

        val enchantments = plugin.enchantmentManager.getEnchantments(tool)
        val hasAutoSmelt = enchantments.containsKey(CustomEnchantment.AUTO_SMELT)
        val hasVeinMiner = enchantments.containsKey(CustomEnchantment.VEIN_MINER)
        val hasTimber = enchantments.containsKey(CustomEnchantment.TIMBER)

        // Auto Smelt für den Haupt-Block
        if (hasAutoSmelt) {
            handleAutoSmelt(event)
        }

        // Vein Miner mit Auto Smelt Support
        if (hasVeinMiner) {
            handleVeinMiner(event, player, hasAutoSmelt)
        }

        // Timber mit Auto Smelt Support (falls gewünscht)
        if (hasTimber) {
            handleTimber(event, player)
        }
    }

    private fun handleAutoSmelt(event: BlockBreakEvent) {
        val block = event.block
        val smelted = getSmeltedMaterial(block.type) ?: return

        event.isDropItems = false
        block.world.dropItemNaturally(block.location, ItemStack(smelted))
    }

    /**
     * Helper-Methode um geschmolzenes Material zu bekommen
     */
    private fun getSmeltedMaterial(material: org.bukkit.Material): org.bukkit.Material? {
        return when (material) {
            org.bukkit.Material.IRON_ORE, org.bukkit.Material.DEEPSLATE_IRON_ORE ->
                org.bukkit.Material.IRON_INGOT
            org.bukkit.Material.GOLD_ORE, org.bukkit.Material.DEEPSLATE_GOLD_ORE ->
                org.bukkit.Material.GOLD_INGOT
            org.bukkit.Material.COPPER_ORE, org.bukkit.Material.DEEPSLATE_COPPER_ORE ->
                org.bukkit.Material.COPPER_INGOT
            org.bukkit.Material.ANCIENT_DEBRIS ->
                org.bukkit.Material.NETHERITE_SCRAP
            else -> null
        }
    }

    private fun handleVeinMiner(event: BlockBreakEvent, player: Player, autoSmelt: Boolean) {
        val block = event.block
        if (!block.type.name.contains("ORE")) return

        val oreType = block.type
        val toBreak = mutableListOf<org.bukkit.block.Block>()

        // Finde alle verbundenen Erze (aus Config, default: 32 für Balance und Performance)
        val maxBlocks = plugin.config.getInt("enchantment-balance.vein-miner-max-blocks", 32)
        findConnectedOres(block, oreType, toBreak, maxBlocks)

        toBreak.forEach { oreBlock ->
            if (autoSmelt) {
                // Mit Auto Smelt: Droppe geschmolzenes Material
                val smelted = getSmeltedMaterial(oreBlock.type)
                if (smelted != null) {
                    oreBlock.type = org.bukkit.Material.AIR
                    oreBlock.world.dropItemNaturally(oreBlock.location, ItemStack(smelted))
                } else {
                    oreBlock.breakNaturally(player.inventory.itemInMainHand)
                }
            } else {
                // Ohne Auto Smelt: Normale Drops
                oreBlock.breakNaturally(player.inventory.itemInMainHand)
            }
        }
    }

    private fun findConnectedOres(
        block: org.bukkit.block.Block,
        oreType: org.bukkit.Material,
        found: MutableList<org.bukkit.block.Block>,
        max: Int
    ) {
        if (found.size >= max) return
        if (found.contains(block)) return
        if (block.type != oreType) return

        found.add(block)

        // Check all 26 surrounding blocks
        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    if (x == 0 && y == 0 && z == 0) continue
                    val neighbor = block.getRelative(x, y, z)
                    findConnectedOres(neighbor, oreType, found, max)
                }
            }
        }
    }

    private fun handleTimber(event: BlockBreakEvent, player: Player) {
        val block = event.block
        if (!block.type.name.contains("LOG")) return

        val logType = block.type
        val toBreak = mutableListOf<org.bukkit.block.Block>()

        // Finde alle Logs im Baum (aus Config, default: 64)
        val maxBlocks = plugin.config.getInt("enchantment-balance.timber-max-blocks", 64)
        findConnectedLogs(block, logType, toBreak, maxBlocks)

        toBreak.forEach { logBlock ->
            logBlock.breakNaturally(player.inventory.itemInMainHand)
        }
    }

    private fun findConnectedLogs(
        block: org.bukkit.block.Block,
        logType: org.bukkit.Material,
        found: MutableList<org.bukkit.block.Block>,
        max: Int
    ) {
        if (found.size >= max) return
        if (found.contains(block)) return
        if (block.type != logType) return

        found.add(block)

        // Check surrounding blocks (prioritize upwards)
        for (x in -1..1) {
            for (z in -1..1) {
                for (y in -1..2) { // Check more upwards
                    if (x == 0 && y == 0 && z == 0) continue
                    val neighbor = block.getRelative(x, y, z)
                    findConnectedLogs(neighbor, logType, found, max)
                }
            }
        }
    }

    /**
     * Soul Bound: Behalte Item beim Tod
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val itemsToKeep = mutableListOf<ItemStack>()

        event.drops.removeIf { item ->
            if (plugin.enchantmentManager.hasEnchantment(item, CustomEnchantment.SOUL_BOUND)) {
                itemsToKeep.add(item)
                true
            } else {
                false
            }
        }

        // Gib Items zurück nach Respawn
        if (itemsToKeep.isNotEmpty()) {
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                itemsToKeep.forEach { item ->
                    player.inventory.addItem(item)
                }
            }, 1L)
        }
    }

    /**
     * Unbreakable: Verhindere Haltbarkeitsverlust
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onItemDamage(event: PlayerItemDamageEvent) {
        if (plugin.enchantmentManager.hasEnchantment(event.item, CustomEnchantment.UNBREAKABLE)) {
            event.isCancelled = true
        }
    }

    /**
     * Cleanup von Cooldowns wenn Spieler den Server verlässt
     * Verhindert Memory-Leaks bei vielen Spielern
     */
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        enchantmentCooldowns.remove(event.player.uniqueId)
    }

    /**
     * Bereinigt alte Cooldown-Einträge (älter als 10 Minuten)
     * Kann periodisch vom Plugin aufgerufen werden
     */
    fun cleanupOldCooldowns() {
        val now = System.currentTimeMillis()
        val tenMinutes = 600_000L
        
        enchantmentCooldowns.values.forEach { playerCooldowns ->
            playerCooldowns.entries.removeIf { (_, lastUse) ->
                now - lastUse > tenMinutes
            }
        }
        
        // Entferne leere Maps
        enchantmentCooldowns.entries.removeIf { it.value.isEmpty() }
    }
}

