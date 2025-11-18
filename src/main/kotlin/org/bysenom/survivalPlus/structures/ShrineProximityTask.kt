package org.bysenom.survivalPlus.structures

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Task der regelmäßig prüft ob Spieler in der Nähe von Shrines sind
 * und sie benachrichtigt
 */
class ShrineProximityTask(private val plugin: SurvivalPlus) : BukkitRunnable() {

    // Spieler -> Letzter Shrine in dessen Nähe sie waren
    private val playerLastNearbyShrine = ConcurrentHashMap<UUID, UUID>()

    // Spieler -> Letzter Notification-Timestamp
    private val playerLastNotification = ConcurrentHashMap<UUID, Long>()

    // Cooldown für Notifications (in Millisekunden)
    private val notificationCooldown = 30000L // 30 Sekunden

    override fun run() {
        // Prüfe alle Online-Spieler
        plugin.server.onlinePlayers.forEach { player ->
            checkPlayerProximity(player)
        }
    }

    private fun checkPlayerProximity(player: Player) {
        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(player.world)) {
            return
        }

        val location = player.location

        // Prüfe ob Spieler in der Nähe eines Shrines ist
        val nearbyShrine = plugin.shrineManager.getNearbyShrine(location, 50.0)

        if (nearbyShrine != null) {
            // Spieler ist in der Nähe eines Shrines
            handleShrineProximity(player, nearbyShrine)
        } else {
            // Spieler ist nicht mehr in der Nähe - Reset
            playerLastNearbyShrine.remove(player.uniqueId)
        }
    }

    private fun handleShrineProximity(player: Player, shrine: ShrineManager.ShrineData) {
        val playerId = player.uniqueId
        val shrineId = plugin.shrineManager.getAllShrines()
            .firstOrNull { it.location == shrine.location }
            ?.let { UUID.randomUUID() } // Temporäre ID

        // Prüfe ob dies ein neuer Shrine ist
        val lastShrineId = playerLastNearbyShrine[playerId]
        val isNewShrine = lastShrineId == null || lastShrineId != shrineId

        // Prüfe Cooldown
        val lastNotification = playerLastNotification[playerId] ?: 0L
        val now = System.currentTimeMillis()
        val cooldownExpired = (now - lastNotification) > notificationCooldown

        if (isNewShrine || cooldownExpired) {
            // Sende Notification
            sendShrineNotification(player, shrine)

            // Update Tracking
            if (shrineId != null) {
                playerLastNearbyShrine[playerId] = shrineId
            }
            playerLastNotification[playerId] = now
        }
    }

    private fun sendShrineNotification(player: Player, shrine: ShrineManager.ShrineData) {
        val distance = player.location.distance(shrine.location).toInt()
        val worldTier = plugin.worldTierManager.getWorldTier(player.world)

        // Action Bar Nachricht
        player.sendActionBar(
            Component.text("⚔ World Tier Shrine ⚔ ")
                .color(NamedTextColor.GOLD)
                .append(Component.text("${distance}m entfernt").color(NamedTextColor.GRAY))
        )

        // Nur bei erster Annäherung (< 30 Blöcke): Title + Sound
        if (distance < 30) {
            // Title
            val title = Title.title(
                Component.text("⚔ Shrine Entdeckt ⚔")
                    .color(NamedTextColor.GOLD),
                Component.text("Rechtsklick auf Altar um World Tier zu ändern")
                    .color(NamedTextColor.GRAY),
                Title.Times.times(
                    Duration.ofMillis(500),
                    Duration.ofMillis(2000),
                    Duration.ofMillis(500)
                )
            )
            player.showTitle(title)

            // Sound
            player.playSound(player.location, Sound.BLOCK_BELL_USE, 1f, 1f)
            player.playSound(player.location, Sound.BLOCK_BEACON_POWER_SELECT, 0.5f, 1.2f)

            // Chat-Nachricht
            player.sendMessage("")
            player.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.DARK_GRAY))
            player.sendMessage(Component.text("⚔ World Tier Shrine ⚔")
                .color(NamedTextColor.GOLD))
            player.sendMessage(Component.empty())
            player.sendMessage(Component.text("Aktuelles Tier: ")
                .color(NamedTextColor.YELLOW)
                .append(worldTier.getDisplayComponent()))
            player.sendMessage(Component.text("Koordinaten: ")
                .color(NamedTextColor.YELLOW)
                .append(Component.text("${shrine.location.blockX}, ${shrine.location.blockY}, ${shrine.location.blockZ}")
                    .color(NamedTextColor.WHITE)))
            player.sendMessage(Component.text("Entfernung: ")
                .color(NamedTextColor.YELLOW)
                .append(Component.text("${distance}m")
                    .color(NamedTextColor.WHITE)))
            player.sendMessage(Component.empty())
            player.sendMessage(Component.text("💡 Rechtsklick auf den Altar um das World Tier zu ändern!")
                .color(NamedTextColor.AQUA))
            player.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.DARK_GRAY))
            player.sendMessage("")
        }
    }

    /**
     * Cleanup beim Plugin-Disable
     */
    fun cleanup() {
        playerLastNearbyShrine.clear()
        playerLastNotification.clear()
        cancel()
    }
}

