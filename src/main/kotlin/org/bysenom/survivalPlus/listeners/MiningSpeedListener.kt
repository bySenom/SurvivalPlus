package org.bysenom.survivalPlus.listeners

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID

/**
 * Listener für Mining-Speed-Bonus basierend auf Item-Qualität
 * Nutzt Haste-Effekt für echte Mining-Speed-Erhöhung
 */
class MiningSpeedListener(private val plugin: SurvivalPlus) : Listener {

    private val activeHasteEffects = mutableMapOf<UUID, Int>()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player

        // Entferne alte Custom-Haste-Effekte
        if (activeHasteEffects.containsKey(player.uniqueId)) {
            player.removePotionEffect(PotionEffectType.HASTE)
            activeHasteEffects.remove(player.uniqueId)
        }

        // Füge neuen Haste-Effekt hinzu basierend auf Item-Qualität
        val newItem = player.inventory.getItem(event.newSlot)
        if (newItem != null && !newItem.type.isAir) {
            val quality = plugin.itemManager.getQuality(newItem)
            if (quality != null && isTool(newItem.type)) {
                val hasteLevel = getHasteLevel(quality.tier)
                if (hasteLevel > 0) {
                    // Haste-Effekt anwenden (0-basiert für API)
                    player.addPotionEffect(
                        PotionEffect(
                            PotionEffectType.HASTE,
                            Integer.MAX_VALUE,
                            hasteLevel - 1, // Level 1 = Haste I (API nutzt 0-basiert)
                            false,
                            false,
                            true // Ambient für subtile Darstellung
                        )
                    )
                    activeHasteEffects[player.uniqueId] = hasteLevel

                    // Debug-Info
                    if (plugin.config.getBoolean("debug", false)) {
                        player.sendActionBar(
                            net.kyori.adventure.text.Component.text("⛏ Mining Speed: +${(hasteLevel * 20)}%")
                                .color(net.kyori.adventure.text.format.NamedTextColor.GOLD)
                        )
                    }
                }
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        // Cleanup beim Disconnect
        activeHasteEffects.remove(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player

        // Nur im Survival-Mode
        if (player.gameMode != GameMode.SURVIVAL) return

        val tool = player.inventory.itemInMainHand
        if (tool.type.isAir) return

        val quality = plugin.itemManager.getQuality(tool) ?: return

        // Nur für Tools
        if (!isTool(tool.type)) return

        // Legendary+ (Tier 5+): Kleine Chance auf Instamine
        if (quality.tier >= 5) {
            val instantBreakChance = when(quality.tier) {
                5 -> 0.10  // Legendary - 10% Chance
                6 -> 0.20  // Mythic - 20% Chance
                else -> 0.0
            }

            if (Math.random() < instantBreakChance) {
                // Partikel-Effekt für Instamine
                player.world.spawnParticle(
                    org.bukkit.Particle.CRIT,
                    event.block.location.add(0.5, 0.5, 0.5),
                    15,
                    0.3, 0.3, 0.3,
                    0.1
                )

                // Sound-Effekt
                player.playSound(
                    player.location,
                    org.bukkit.Sound.BLOCK_AMETHYST_BLOCK_BREAK,
                    0.5f,
                    2.0f
                )
            }
        }
    }

    /**
     * Prüft ob ein Material ein Werkzeug ist
     */
    private fun isTool(material: Material): Boolean {
        val name = material.name
        return name.contains("PICKAXE") ||
               name.contains("AXE") && !name.contains("PICKAXE") || // Axe aber nicht Pickaxe
               name.contains("SHOVEL") ||
               name.contains("HOE")
    }

    /**
     * Berechnet Haste-Level basierend auf Qualitäts-Tier
     * Haste Level direkt = mehr Mining Speed
     */
    private fun getHasteLevel(qualityTier: Int): Int {
        return when(qualityTier) {
            1 -> 0  // Common - Kein Bonus
            2 -> 1  // Uncommon - Haste I (20% schneller)
            3 -> 2  // Rare - Haste II (40% schneller)
            4 -> 3  // Epic - Haste III (60% schneller)
            5 -> 4  // Legendary - Haste IV (80% schneller)
            6 -> 5  // Mythic - Haste V (100% schneller = 2x Speed!)
            else -> 0
        }
    }
}

