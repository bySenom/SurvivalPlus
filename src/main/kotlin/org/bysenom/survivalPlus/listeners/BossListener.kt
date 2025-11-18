package org.bysenom.survivalPlus.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.NamespacedKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.persistence.PersistentDataType

/**
 * Listener für Boss-bezogene Events
 */
class BossListener(private val plugin: SurvivalPlus) : Listener {

    private val healingTowerKey = NamespacedKey(plugin, "healing_tower")

    /**
     * Healing Towers können Schaden nehmen
     */
    @EventHandler
    fun onHealingTowerDamage(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        if (entity !is ArmorStand) return
        
        // Prüfe ob es ein Healing Tower ist
        if (!entity.persistentDataContainer.has(healingTowerKey, PersistentDataType.STRING)) return
        
        val damager = event.damager
        if (damager !is Player) return
        
        // Health Display
        val currentHealth = entity.health - event.finalDamage
        if (currentHealth > 0) {
            entity.customName(
                Component.text("Heilungs-Turm ").color(NamedTextColor.GREEN)
                    .append(Component.text("❤ ${currentHealth.toInt()}").color(NamedTextColor.RED))
            )
        } else {
            // Tower zerstört
            entity.location.block.type = org.bukkit.Material.AIR
            damager.sendMessage(
                Component.text("✓ Heilungs-Turm zerstört!").color(NamedTextColor.GREEN)
            )
            entity.location.world?.spawnParticle(
                org.bukkit.Particle.EXPLOSION,
                entity.location,
                10,
                0.5, 0.5, 0.5,
                0.1
            )
        }
    }

    /**
     * Cleanup bei Tower-Tod
     */
    @EventHandler
    fun onHealingTowerDeath(event: EntityDeathEvent) {
        val entity = event.entity
        if (entity !is ArmorStand) return
        
        if (!entity.persistentDataContainer.has(healingTowerKey, PersistentDataType.STRING)) return
        
        // Entferne Block
        entity.location.block.type = org.bukkit.Material.AIR
        
        // Keine Drops
        event.drops.clear()
        event.droppedExp = 0
    }
}
