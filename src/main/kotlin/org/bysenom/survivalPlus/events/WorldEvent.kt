package org.bysenom.survivalPlus.events

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.World
import java.util.UUID

/**
 * World Event Typen (Diablo-Style)
 */
enum class WorldEventType {
    MOB_HORDE,          // Dämonische Invasion
    FALLING_BLOCKS,     // Meteoritenschauer
    MOON_EVENT,         // Blutmond
    TREASURE_GOBLIN,    // Schatzgoblin
    ELITE_BOSS          // Boss-Horde
}

/**
 * Repräsentiert ein aktives World Event
 */
data class WorldEvent(
    val id: UUID,
    val type: WorldEventType,
    val name: String,
    val world: World,
    val duration: Int,  // Sekunden
    val startTime: Long,
    val qualityBoost: Int,
    val announceGlobally: Boolean
) {
    /**
     * Prüft ob das Event noch aktiv ist
     */
    fun isActive(): Boolean {
        val elapsed = (System.currentTimeMillis() - startTime) / 1000
        return elapsed < duration
    }

    /**
     * Gibt die verbleibende Zeit in Sekunden zurück
     */
    fun getRemainingTime(): Int {
        val elapsed = (System.currentTimeMillis() - startTime) / 1000
        return (duration - elapsed).toInt().coerceAtLeast(0)
    }

    /**
     * Gibt eine formatierte Anzeige des Events zurück
     */
    fun getDisplayComponent(): Component {
        val color = when (type) {
            WorldEventType.MOB_HORDE -> NamedTextColor.RED
            WorldEventType.FALLING_BLOCKS -> NamedTextColor.GOLD
            WorldEventType.MOON_EVENT -> NamedTextColor.DARK_RED
            WorldEventType.TREASURE_GOBLIN -> NamedTextColor.YELLOW
            WorldEventType.ELITE_BOSS -> NamedTextColor.DARK_PURPLE
        }

        return Component.text("⚔ $name")
            .color(color)
            .decoration(TextDecoration.BOLD, true)
    }

    /**
     * Gibt eine Zeitleiste zurück
     */
    fun getTimeDisplay(): Component {
        val remaining = getRemainingTime()
        val minutes = remaining / 60
        val seconds = remaining % 60

        return Component.text("Zeit: ${minutes}m ${seconds}s")
            .color(NamedTextColor.GRAY)
    }
}

