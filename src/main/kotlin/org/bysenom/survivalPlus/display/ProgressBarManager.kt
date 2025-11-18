package org.bysenom.survivalPlus.display

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.ReforgingTier
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.UUID

class ProgressBarManager(private val plugin: SurvivalPlus) {

    private val playerBossBars = mutableMapOf<UUID, BossBar>()

    /**
     * Zeigt eine Boss Bar für Reforging-Material-Sammlung
     */
    fun showReforgingProgress(player: Player, tier: ReforgingTier) {
        if (!plugin.config.getBoolean("features.boss-bars", true)) return

        // Entferne alte Boss Bar falls vorhanden
        removeProgressBar(player)

        // Erstelle neue Boss Bar
        val bossBar = createReforgingBossBar(player, tier)
        playerBossBars[player.uniqueId] = bossBar

        player.showBossBar(bossBar)

        // Update Boss Bar alle 20 Ticks (1 Sekunde)
        val taskId = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            if (!player.isOnline) {
                removeProgressBar(player)
                return@Runnable
            }

            updateReforgingBossBar(player, tier, bossBar)
        }, 0L, 20L).taskId

        // Speichere Task-ID in Boss Bar Name (Workaround)
        bossBar.addViewer(player)
    }

    /**
     * Erstellt eine Boss Bar für Reforging-Material
     */
    private fun createReforgingBossBar(player: Player, tier: ReforgingTier): BossBar {
        val materialCount = player.inventory.all(tier.material).values.sumOf { it.amount }
        val progress = (materialCount.toFloat() / tier.cost.toFloat()).coerceIn(0f, 1f)

        val title = Component.text("${tier.oreName}: $materialCount/${tier.cost}")
            .color(getColorForTier(tier))

        return BossBar.bossBar(
            title,
            progress,
            getBossBarColorForTier(tier),
            BossBar.Overlay.PROGRESS
        )
    }

    /**
     * Aktualisiert eine Reforging Boss Bar
     */
    private fun updateReforgingBossBar(player: Player, tier: ReforgingTier, bossBar: BossBar) {
        val materialCount = player.inventory.all(tier.material).values.sumOf { it.amount }
        val progress = (materialCount.toFloat() / tier.cost.toFloat()).coerceIn(0f, 1f)

        val title = Component.text("${tier.oreName}: $materialCount/${tier.cost}")
            .color(getColorForTier(tier))

        bossBar.name(title)
        bossBar.progress(progress)

        // Wenn genug Material: Grün
        if (materialCount >= tier.cost) {
            bossBar.color(BossBar.Color.GREEN)
        } else {
            bossBar.color(getBossBarColorForTier(tier))
        }
    }

    /**
     * Zeigt eine Boss Bar für allgemeinen Progress
     */
    fun showProgress(player: Player, title: String, progress: Float, color: BossBar.Color, duration: Long = 100L) {
        if (!plugin.config.getBoolean("features.boss-bars", true)) return

        removeProgressBar(player)

        val bossBar = BossBar.bossBar(
            Component.text(title),
            progress.coerceIn(0f, 1f),
            color,
            BossBar.Overlay.PROGRESS
        )

        playerBossBars[player.uniqueId] = bossBar
        player.showBossBar(bossBar)

        // Automatisch entfernen nach Duration
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            removeProgressBar(player)
        }, duration)
    }

    /**
     * Zeigt eine Boss Bar für Reforging-Prozess (Animation)
     */
    fun showReforgingProcess(player: Player) {
        if (!plugin.config.getBoolean("features.boss-bars", true)) return

        removeProgressBar(player)

        val bossBar = BossBar.bossBar(
            Component.text("Reforging..."),
            0f,
            BossBar.Color.PURPLE,
            BossBar.Overlay.PROGRESS
        )

        playerBossBars[player.uniqueId] = bossBar
        player.showBossBar(bossBar)

        // Animiere Progress von 0 zu 100%
        var progress = 0f
        val taskId = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            progress += 0.05f

            if (progress >= 1f) {
                bossBar.progress(1f)
                bossBar.name(Component.text("Reforging Abgeschlossen!"))
                bossBar.color(BossBar.Color.GREEN)

                // Entferne nach 2 Sekunden
                plugin.server.scheduler.runTaskLater(plugin, Runnable {
                    removeProgressBar(player)
                }, 40L)

                return@Runnable
            } else {
                bossBar.progress(progress)
            }
        }, 0L, 2L)
    }

    /**
     * Zeigt eine Boss Bar für Skill-Progress
     */
    fun showSkillProgress(player: Player, skillName: String, currentXP: Int, requiredXP: Int) {
        if (!plugin.config.getBoolean("features.boss-bars", true)) return

        val progress = (currentXP.toFloat() / requiredXP.toFloat()).coerceIn(0f, 1f)

        val title = Component.text("$skillName: $currentXP/$requiredXP XP")
            .color(net.kyori.adventure.text.format.NamedTextColor.AQUA)

        val bossBar = BossBar.bossBar(
            title,
            progress,
            BossBar.Color.BLUE,
            BossBar.Overlay.NOTCHED_10
        )

        removeProgressBar(player)
        playerBossBars[player.uniqueId] = bossBar
        player.showBossBar(bossBar)

        // Auto-Remove nach 5 Sekunden
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            removeProgressBar(player)
        }, 100L)
    }

    /**
     * Entfernt die Boss Bar eines Spielers
     */
    fun removeProgressBar(player: Player) {
        val bossBar = playerBossBars.remove(player.uniqueId) ?: return
        player.hideBossBar(bossBar)
    }

    /**
     * Entfernt alle Boss Bars (beim Plugin-Disable)
     */
    fun removeAllProgressBars() {
        playerBossBars.keys.toList().forEach { uuid ->
            plugin.server.getPlayer(uuid)?.let { player ->
                removeProgressBar(player)
            }
        }
    }

    /**
     * Hilfsmethode: Farbe basierend auf Tier
     */
    private fun getColorForTier(tier: ReforgingTier): net.kyori.adventure.text.format.TextColor {
        return when (tier) {
            ReforgingTier.TIER_1 -> net.kyori.adventure.text.format.NamedTextColor.GRAY
            ReforgingTier.TIER_2 -> net.kyori.adventure.text.format.NamedTextColor.GOLD
            ReforgingTier.TIER_3 -> net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
        }
    }

    /**
     * Hilfsmethode: Boss Bar Farbe basierend auf Tier
     */
    private fun getBossBarColorForTier(tier: ReforgingTier): BossBar.Color {
        return when (tier) {
            ReforgingTier.TIER_1 -> BossBar.Color.WHITE
            ReforgingTier.TIER_2 -> BossBar.Color.YELLOW
            ReforgingTier.TIER_3 -> BossBar.Color.PURPLE
        }
    }

    /**
     * Cleanup beim Logout
     */
    fun cleanup(player: Player) {
        removeProgressBar(player)
    }
}

