package org.bysenom.survivalPlus.display

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.Quality
import org.bukkit.entity.Player
import java.time.Duration

class MessageManager(private val plugin: SurvivalPlus) {

    /**
     * Zeigt eine Title/Subtitle Nachricht wenn ein hochwertiges Item erhalten wird
     */
    fun showItemReceivedTitle(player: Player, quality: Quality) {
        if (!plugin.config.getBoolean("features.title-messages", true)) return
        if (quality.tier < 5) return // Nur für Legendary und Mythic

        val title = Component.text(quality.displayName)
            .color(quality.color)

        val subtitle = when (quality) {
            Quality.LEGENDARY -> Component.text("Ein legendäres Item!")
                .color(quality.color)
            Quality.MYTHIC -> Component.text("Ein mythisches Item!")
                .color(quality.color)
            else -> Component.empty()
        }

        val times = Title.Times.times(
            Duration.ofMillis(500),  // Fade in
            Duration.ofMillis(2000), // Stay
            Duration.ofMillis(500)   // Fade out
        )

        val titleMessage = Title.title(title, subtitle, times)
        player.showTitle(titleMessage)
    }

    /**
     * Zeigt eine Title/Subtitle Nachricht beim erfolgreichen Reforging
     */
    fun showReforgingSuccessTitle(player: Player, oldQuality: Quality, newQuality: Quality) {
        if (!plugin.config.getBoolean("features.title-messages", true)) return

        // Nur anzeigen wenn die Qualität sich verbessert hat
        if (newQuality.tier <= oldQuality.tier) return

        val title = Component.text("Reforging Erfolgreich!")
            .color(newQuality.color)

        val subtitle = Component.text("${oldQuality.displayName} → ${newQuality.displayName}")
            .color(newQuality.color)

        val times = Title.Times.times(
            Duration.ofMillis(300),
            Duration.ofMillis(1500),
            Duration.ofMillis(300)
        )

        val titleMessage = Title.title(title, subtitle, times)
        player.showTitle(titleMessage)
    }

    /**
     * Zeigt eine Title wenn ein Achievement freigeschaltet wird
     */
    fun showAchievementTitle(player: Player, achievementName: String) {
        if (!plugin.config.getBoolean("features.title-messages", true)) return

        val title = Component.text("Achievement Freigeschaltet!")
            .color(net.kyori.adventure.text.format.NamedTextColor.GOLD)

        val subtitle = Component.text(achievementName)
            .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW)

        val times = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofMillis(3000),
            Duration.ofMillis(500)
        )

        val titleMessage = Title.title(title, subtitle, times)
        player.showTitle(titleMessage)
    }

    /**
     * Zeigt einen Skill Level-Up Title
     */
    fun showSkillLevelUp(player: Player, skill: org.bysenom.survivalPlus.skills.Skill, level: Int) {
        if (!plugin.config.getBoolean("features.title-messages", true)) return

        val title = Component.text("⭐ LEVEL UP! ⭐")
            .color(skill.color)

        val subtitle = Component.text("${skill.displayName} Level $level")
            .color(skill.color)

        player.showTitle(
            net.kyori.adventure.title.Title.title(
                title,
                subtitle,
                net.kyori.adventure.title.Title.Times.times(
                    java.time.Duration.ofMillis(500),
                    java.time.Duration.ofMillis(2000),
                    java.time.Duration.ofMillis(500)
                )
            )
        )
    }

    /**
     * Zeigt einen Error-Title
     */
    fun showErrorTitle(player: Player, message: String) {
        val title = Component.text("Fehler!")
            .color(net.kyori.adventure.text.format.NamedTextColor.RED)

        val subtitle = Component.text(message)
            .color(net.kyori.adventure.text.format.NamedTextColor.GRAY)

        val times = Title.Times.times(
            Duration.ofMillis(200),
            Duration.ofMillis(1000),
            Duration.ofMillis(200)
        )

        val titleMessage = Title.title(title, subtitle, times)
        player.showTitle(titleMessage)
    }

    /**
     * Zeigt eine Boss-Spawn Ankündigung
     */
    fun showBossSpawnTitle(player: Player, bossName: String) {
        if (!plugin.config.getBoolean("features.title-messages", true)) return

        val title = Component.text("⚠ Boss Erschienen! ⚠")
            .color(net.kyori.adventure.text.format.NamedTextColor.DARK_RED)

        val subtitle = Component.text(bossName)
            .color(net.kyori.adventure.text.format.NamedTextColor.RED)

        val times = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofMillis(4000),
            Duration.ofMillis(500)
        )

        val titleMessage = Title.title(title, subtitle, times)
        player.showTitle(titleMessage)
    }
}

