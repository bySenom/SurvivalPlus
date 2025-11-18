package org.bysenom.survivalPlus.listeners

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent

/**
 * Verhindert Portal-Konflikte mit HubPlugin
 * Stellt sicher dass Survival → Nether und zurück, sowie End → Survival geroutet wird
 */
class PortalListener(private val plugin: SurvivalPlus) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerPortal(event: PlayerPortalEvent) {
        if (!plugin.config.getBoolean("portals.enabled", true)) return

        val player = event.player
        val fromWorld = event.from.world ?: return
        val fromName = fromWorld.name

        // Nur unsere Whitelist-Welten
        if (!plugin.worldTierManager.isEnabledWorld(fromWorld)) return

        when (event.cause) {
            PlayerTeleportEvent.TeleportCause.NETHER_PORTAL -> {
                val targetWorldName = plugin.config.getString("portals.mappings.$fromName") ?: return
                val targetWorld = plugin.server.getWorld(targetWorldName) ?: return

                val to = calculateNetherPortalLocation(event.from, targetWorld, fromName.equals("Nether", ignoreCase = true))
                event.to = to
                event.canCreatePortal = true

                if (plugin.config.getBoolean("portals.debug", false)) {
                    plugin.logger.info("[PortalFix] NETHER_PORTAL: $fromName -> ${targetWorld.name} für ${player.name}")
                }
            }
            PlayerTeleportEvent.TeleportCause.END_PORTAL -> {
                if (fromName.equals("End", ignoreCase = true)) {
                    val targetWorldName = plugin.config.getString("portals.mappings.End") ?: "Survival"
                    val targetWorld = plugin.server.getWorld(targetWorldName) ?: return
                    event.to = targetWorld.spawnLocation
                    if (plugin.config.getBoolean("portals.debug", false)) {
                        plugin.logger.info("[PortalFix] END_PORTAL: End -> ${targetWorld.name} für ${player.name}")
                    }
                }
            }
            else -> {}
        }
    }

    // Fallback: Falls andere Plugins später die Zielwelt überschreiben, korrigieren wir unmittelbar danach
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (!plugin.config.getBoolean("portals.enabled", true)) return
        val player = event.player
        val cause = event.cause
        val fromWorld = event.from.world ?: return
        val fromName = fromWorld.name

        // Nur unsere Whitelist-Welten
        if (!plugin.worldTierManager.isEnabledWorld(fromWorld)) return

        val expectedTargetName = when (cause) {
            PlayerTeleportEvent.TeleportCause.NETHER_PORTAL -> plugin.config.getString("portals.mappings.$fromName")
            PlayerTeleportEvent.TeleportCause.END_PORTAL -> if (fromName.equals("End", true)) plugin.config.getString("portals.mappings.End") ?: "Survival" else null
            else -> null
        } ?: return

        val actualTargetWorld = event.to?.world?.name
        if (!expectedTargetName.equals(actualTargetWorld, ignoreCase = true)) {
            // Korrigiere Ziel 1 Tick später sicherheitshalber
            val targetWorld = plugin.server.getWorld(expectedTargetName) ?: return
            val dest = when (cause) {
                PlayerTeleportEvent.TeleportCause.NETHER_PORTAL -> calculateNetherPortalLocation(event.from, targetWorld, fromName.equals("Nether", true))
                PlayerTeleportEvent.TeleportCause.END_PORTAL -> targetWorld.spawnLocation
                else -> return
            }
            plugin.server.scheduler.runTask(plugin, Runnable {
                player.teleport(dest, PlayerTeleportEvent.TeleportCause.PLUGIN)
                if (plugin.config.getBoolean("portals.debug", false)) {
                    plugin.logger.info("[PortalFix] Fallback Teleport: $fromName -> ${targetWorld.name} (ursprünglich ${actualTargetWorld ?: "?"}) für ${player.name}")
                }
            })
        }
    }

    /**
     * Berechnet die korrekte Nether-Portal Location
     */
    private fun calculateNetherPortalLocation(from: Location, targetWorld: World, isFromNether: Boolean): Location {
        val scale = if (isFromNether) 8.0 else 0.125 // Nether → Overworld = x8, Overworld → Nether = /8
        val x = from.x * scale
        val z = from.z * scale
        val y = if (isFromNether) {
            targetWorld.getHighestBlockYAt(x.toInt(), z.toInt()).toDouble()
        } else {
            from.y.coerceIn(10.0, 120.0)
        }
        return Location(targetWorld, x, y, z, from.yaw, from.pitch)
    }
}
