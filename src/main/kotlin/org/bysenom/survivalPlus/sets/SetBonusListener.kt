package org.bysenom.survivalPlus.sets

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class SetBonusListener(private val plugin: SurvivalPlus) : Listener {

    /**
     * Prüfe Set-Boni wenn Spieler joint
     */
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // Delay damit Inventar geladen ist
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            plugin.setBonusManager.checkSetBonuses(event.player)
        }, 20L)
    }

    /**
     * Cleanup wenn Spieler quit
     */
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        plugin.setBonusManager.cleanup(event.player)
    }

    /**
     * Prüfe Set-Boni wenn Inventar geändert wird
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.isCancelled) return
        if (event.whoClicked !is Player) return

        val player = event.whoClicked as Player

        // Delay für Inventar-Update
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            plugin.setBonusManager.checkSetBonuses(player)
        }, 1L)
    }

    /**
     * Prüfe Set-Boni wenn Item in Hand geändert wird
     */
    @EventHandler
    fun onItemHeld(event: PlayerItemHeldEvent) {
        // Delay für Update
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            plugin.setBonusManager.checkSetBonuses(event.player)
        }, 1L)
    }

    /**
     * Kritische Treffer mit Set-Boni
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerAttack(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        if (event.damager !is Player) return

        val player = event.damager as Player

        // Hole Krit-Chance von Set-Boni
        val critChance = plugin.setBonusManager.getCritChanceBonus(player)

        if (critChance > 0 && Math.random() * 100 < critChance) {
            // Kritischer Treffer!
            val critDamage = plugin.setBonusManager.getCritDamageBonus(player)
            val multiplier = 1.0 + (critDamage / 100.0)

            event.damage *= multiplier

            // Visual Feedback
            player.sendActionBar(
                net.kyori.adventure.text.Component.text("⚡ KRITISCHER TREFFER! ⚡")
                    .color(net.kyori.adventure.text.format.NamedTextColor.GOLD)
            )

            player.playSound(player.location, org.bukkit.Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1f)

            // Partikel
            event.entity.world.spawnParticle(
                org.bukkit.Particle.CRIT,
                event.entity.location.add(0.0, 1.0, 0.0),
                20,
                0.5, 0.5, 0.5,
                0.1
            )
        }
    }
}

