package org.bysenom.survivalPlus.display

import net.kyori.adventure.text.Component
import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.Quality
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkUnloadEvent
import java.util.UUID

class QualityPlateManager(private val plugin: SurvivalPlus) : Listener {

    private val itemHolograms = mutableMapOf<UUID, ArmorStand>()

    /**
     * Erstellt ein Hologramm über einem gedroppted Item
     */
    fun createPlateForItem(item: Item): ArmorStand? {
        if (!plugin.config.getBoolean("features.quality-plates", true)) return null

        val quality = plugin.itemManager.getQuality(item.itemStack) ?: return null

        // Optional: Nur für Rare+ anzeigen
        val minTier = plugin.config.getInt("quality-plates.min-tier", 1)
        if (quality.tier < minTier) return null

        val location = item.location.add(0.0, 0.5, 0.0)
        val hologram = createHologram(location, quality)

        itemHolograms[item.uniqueId] = hologram

        // Hologramm bewegt sich mit dem Item
        startFollowing(item, hologram)

        return hologram
    }

    /**
     * Erstellt das Armor Stand Hologramm
     */
    private fun createHologram(location: Location, quality: Quality): ArmorStand {
        val world = location.world

        return world.spawn(location, ArmorStand::class.java) { armorStand ->
            armorStand.isVisible = false
            armorStand.isSmall = true
            armorStand.isMarker = true
            armorStand.setGravity(false)
            armorStand.isCustomNameVisible = true
            armorStand.isPersistent = false
            armorStand.setCanPickupItems(false)

            // Setze den Namen mit Qualitätsfarbe
            val displayName = Component.text("✦ ${quality.displayName} ✦")
                .color(quality.color)

            armorStand.customName(displayName)
        }
    }

    /**
     * Lässt das Hologramm dem Item folgen
     */
    private fun startFollowing(item: Item, hologram: ArmorStand) {
        val task = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            if (!item.isValid || item.isDead) {
                removeHologram(item.uniqueId)
                return@Runnable
            }

            if (!hologram.isValid) {
                itemHolograms.remove(item.uniqueId)
                return@Runnable
            }

            // Bewege Hologramm zur Item-Position
            val newLocation = item.location.add(0.0, 0.5, 0.0)
            hologram.teleport(newLocation)

        }, 0L, 2L) // Alle 2 Ticks (0.1 Sekunden)

        // Task-ID im Hologramm speichern (für Cleanup)
        hologram.scoreboardTags.add("task_${task.taskId}")
    }

    /**
     * Entfernt ein Hologramm
     */
    fun removeHologram(itemUuid: UUID) {
        val hologram = itemHolograms.remove(itemUuid) ?: return

        // Stoppe den Task
        hologram.scoreboardTags
            .filter { it.startsWith("task_") }
            .forEach { tag ->
                val taskId = tag.substring(5).toIntOrNull()
                if (taskId != null) {
                    plugin.server.scheduler.cancelTask(taskId)
                }
            }

        hologram.remove()
    }

    /**
     * Entfernt alle Hologramme
     */
    fun removeAllHolograms() {
        itemHolograms.keys.toList().forEach { removeHologram(it) }
    }

    /**
     * Zeigt ein temporäres Hologramm für einen Spieler
     */
    fun showTemporaryPlate(@Suppress("UNUSED_PARAMETER") player: Player, location: Location, text: String, duration: Long = 40L) {
        if (!plugin.config.getBoolean("features.quality-plates", true)) return

        val hologram = location.world.spawn(location, ArmorStand::class.java) { armorStand ->
            armorStand.isVisible = false
            armorStand.isSmall = true
            armorStand.isMarker = true
            armorStand.setGravity(false)
            armorStand.isCustomNameVisible = true
            armorStand.isPersistent = false

            armorStand.customName(Component.text(text))
        }

        // Entferne nach Duration
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            hologram.remove()
        }, duration)
    }

    /**
     * Verhindert Memory Leaks wenn Chunks entladen werden
     */
    @EventHandler
    fun onChunkUnload(event: ChunkUnloadEvent) {
        event.chunk.entities
            .filterIsInstance<ArmorStand>()
            .filter { armorStand ->
                armorStand.scoreboardTags.any { it.startsWith("task_") }
            }
            .forEach { hologram ->
                // Find das Item für dieses Hologramm
                val itemUuid = itemHolograms.entries
                    .find { it.value == hologram }
                    ?.key
                
                if (itemUuid != null) {
                    removeHologram(itemUuid)
                }
            }
    }

    /**
     * Gibt die Anzahl aktiver Hologramme zurück (für Debug)
     */
    fun getPlateCount(): Int = itemHolograms.size

    /**
     * Cleanup beim Plugin-Disable
     */
    fun shutdown() {
        removeAllHolograms()
    }
}

