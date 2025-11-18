package org.bysenom.survivalPlus.skills

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerHarvestBlockEvent

class SkillListener(private val plugin: SurvivalPlus) : Listener {

    /**
     * Combat XP beim Mob töten
     */
    @EventHandler
    fun onEntityKill(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        val entity = event.entity

        // XP basierend auf Mob-Typ
        val xp = when (entity) {
            is org.bukkit.entity.Zombie -> 5
            is org.bukkit.entity.Skeleton -> 5
            is org.bukkit.entity.Creeper -> 8
            is org.bukkit.entity.Enderman -> 15
            is org.bukkit.entity.Blaze -> 12
            is org.bukkit.entity.Wither -> 500
            is org.bukkit.entity.EnderDragon -> 1000
            else -> 3
        }

        plugin.skillManager.addXP(killer, Skill.COMBAT, xp)
    }

    /**
     * Combat XP beim Schaden verursachen
     */
    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        if (event.entity !is LivingEntity) return

        val player = event.damager as Player
        val damage = event.finalDamage

        // 1 XP pro 5 Schaden
        val xp = (damage / 5.0).toInt().coerceAtLeast(1)
        plugin.skillManager.addXP(player, Skill.COMBAT, xp)
    }

    /**
     * Mining XP beim Block abbauen
     */
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val block = event.block

        // XP basierend auf Block-Typ
        val xp = when (block.type) {
            org.bukkit.Material.STONE -> 1
            org.bukkit.Material.DEEPSLATE -> 2
            org.bukkit.Material.COAL_ORE -> 3
            org.bukkit.Material.IRON_ORE -> 5
            org.bukkit.Material.GOLD_ORE -> 8
            org.bukkit.Material.DIAMOND_ORE -> 15
            org.bukkit.Material.EMERALD_ORE -> 20
            org.bukkit.Material.ANCIENT_DEBRIS -> 30
            org.bukkit.Material.DEEPSLATE_DIAMOND_ORE -> 18
            org.bukkit.Material.DEEPSLATE_GOLD_ORE -> 10
            org.bukkit.Material.DEEPSLATE_IRON_ORE -> 7
            else -> 0
        }

        if (xp > 0) {
            plugin.skillManager.addXP(player, Skill.MINING, xp)
        }
    }

    /**
     * Farming XP bei Ernte
     */
    @EventHandler
    fun onHarvest(event: PlayerHarvestBlockEvent) {
        val player = event.player
        val block = event.harvestedBlock

        // XP basierend auf Pflanze
        val xp = when (block.type) {
            org.bukkit.Material.WHEAT -> 2
            org.bukkit.Material.CARROTS -> 2
            org.bukkit.Material.POTATOES -> 2
            org.bukkit.Material.BEETROOTS -> 2
            org.bukkit.Material.MELON -> 3
            org.bukkit.Material.PUMPKIN -> 3
            org.bukkit.Material.SWEET_BERRY_BUSH -> 2
            org.bukkit.Material.COCOA -> 2
            else -> 1
        }

        plugin.skillManager.addXP(player, Skill.FARMING, xp)
    }

    /**
     * Fishing XP beim Angeln
     */
    @EventHandler
    fun onFish(event: PlayerFishEvent) {
        if (event.state != PlayerFishEvent.State.CAUGHT_FISH) return

        val player = event.player

        // XP basierend auf Fang
        val caught = event.caught
        val xp = if (caught is org.bukkit.entity.Item) {
            when (caught.itemStack.type) {
                org.bukkit.Material.COD -> 3
                org.bukkit.Material.SALMON -> 4
                org.bukkit.Material.TROPICAL_FISH -> 5
                org.bukkit.Material.PUFFERFISH -> 5
                org.bukkit.Material.ENCHANTED_BOOK -> 20
                org.bukkit.Material.NAME_TAG -> 15
                org.bukkit.Material.SADDLE -> 15
                else -> 2
            }
        } else {
            5 // Entity gefangen
        }

        plugin.skillManager.addXP(player, Skill.FISHING, xp)
    }

    /**
     * Defense XP beim Schaden erhalten (und überleben)
     */
    @EventHandler
    fun onPlayerDamaged(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return

        val player = event.entity as Player
        val damage = event.finalDamage

        // 1 XP pro 2 Schaden überstanden
        val xp = (damage / 2.0).toInt().coerceAtLeast(1)
        plugin.skillManager.addXP(player, Skill.DEFENSE, xp)
    }

    /**
     * Agility XP beim Sprint
     */
    fun onPlayerMove(player: Player, distance: Double) {
        if (player.isSprinting) {
            // 1 XP pro 100 Blöcke Sprint
            if (distance > 100.0) {
                plugin.skillManager.addXP(player, Skill.AGILITY, 1)
            }
        }
    }
}

