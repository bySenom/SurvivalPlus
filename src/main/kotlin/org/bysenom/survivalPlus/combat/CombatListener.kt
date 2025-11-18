package org.bysenom.survivalPlus.combat

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * Combat-System für Ausweichen und Schild-Blocken
 * Mit Cooldown-System (Max Procs pro Minute)
 */
class CombatListener(private val plugin: SurvivalPlus) : Listener {

    // Spieler UUID -> Mechanic -> Liste der letzten Proc-Timestamps
    private val combatProcs = ConcurrentHashMap<UUID, ConcurrentHashMap<CombatMechanic, MutableList<Long>>>()

    enum class CombatMechanic {
        DODGE,
        SHIELD_BLOCK
    }

    /**
     * Prüft ob eine Combat-Mechanik procen kann (Cooldown-Check)
     */
    private fun canProc(player: Player, mechanic: CombatMechanic): Boolean {
        val maxProcs = when (mechanic) {
            CombatMechanic.DODGE ->
                plugin.config.getInt("combat.dodge.max-procs-per-minute", 10)
            CombatMechanic.SHIELD_BLOCK ->
                plugin.config.getInt("combat.shield-block.max-procs-per-minute", 15)
        }

        val playerProcs = combatProcs.getOrPut(player.uniqueId) { ConcurrentHashMap() }
        val mechanicProcs = playerProcs.getOrPut(mechanic) { mutableListOf() }

        val now = System.currentTimeMillis()
        val oneMinuteAgo = now - 60000L

        // Entferne alte Procs (älter als 1 Minute)
        mechanicProcs.removeIf { it < oneMinuteAgo }

        // Prüfe ob Limit erreicht
        if (mechanicProcs.size >= maxProcs) {
            // Zeige Cooldown-Nachricht
            if (mechanicProcs.size == maxProcs) {
                val mechanicName = when (mechanic) {
                    CombatMechanic.DODGE -> "Ausweichen"
                    CombatMechanic.SHIELD_BLOCK -> "Blocken"
                }
                player.sendActionBar(net.kyori.adventure.text.Component.text("⏱ $mechanicName Cooldown!")
                    .color(net.kyori.adventure.text.format.NamedTextColor.GRAY))
            }
            return false
        }

        // Füge neuen Proc hinzu
        mechanicProcs.add(now)
        return true
    }

    /**
     * Ausweichen-Mechanik
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        if (event.entity !is Player) return

        val player = event.entity as Player

        // Prüfe ob Dodge aktiviert ist
        if (!plugin.config.getBoolean("combat.dodge.enabled", true)) return

        // Prüfe nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(player.world)) return

        // Dodge-Chance
        val dodgeChance = plugin.config.getDouble("combat.dodge.dodge-chance", 0.15)

        if (Random.nextDouble() < dodgeChance) {
            // Prüfe Cooldown
            if (!canProc(player, CombatMechanic.DODGE)) {
                return // Cooldown aktiv
            }

            // Ausweichen erfolgreich!
            event.isCancelled = true

            // Visual Feedback
            player.sendActionBar(net.kyori.adventure.text.Component.text("⚡ Ausgewichen!")
                .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW))

            player.playSound(player.location, org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.5f)

            // Partikel-Effekt
            player.world.spawnParticle(
                org.bukkit.Particle.CLOUD,
                player.location.clone().add(0.0, 1.0, 0.0),
                10, 0.3, 0.5, 0.3, 0.05
            )
        }
    }

    /**
     * Schild-Blocken Mechanik
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onShieldBlock(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        if (event.entity !is Player) return

        val player = event.entity as Player

        // Prüfe ob Shield-Block aktiviert ist
        if (!plugin.config.getBoolean("combat.shield-block.enabled", true)) return

        // Prüfe nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(player.world)) return

        // Prüfe ob Spieler ein Schild in der Hand hat und blockt
        val mainHand = player.inventory.itemInMainHand
        val offHand = player.inventory.itemInOffHand

        val hasShield = mainHand.type == org.bukkit.Material.SHIELD ||
                       offHand.type == org.bukkit.Material.SHIELD

        if (!hasShield) return
        if (!player.isBlocking) return

        // Prüfe Cooldown
        if (!canProc(player, CombatMechanic.SHIELD_BLOCK)) {
            return // Cooldown aktiv
        }

        // Schild-Block erfolgreich!
        val blockReduction = plugin.config.getDouble("combat.shield-block.block-reduction", 0.5)
        event.damage *= (1.0 - blockReduction)

        // Visual Feedback
        player.sendActionBar(net.kyori.adventure.text.Component.text("🛡 Geblockt! -${(blockReduction * 100).toInt()}%")
            .color(net.kyori.adventure.text.format.NamedTextColor.BLUE))

        player.playSound(player.location, org.bukkit.Sound.ITEM_SHIELD_BLOCK, 1f, 1f)
    }

    /**
     * Cleanup
     */
    fun cleanup() {
        combatProcs.clear()
    }
}

