package org.bysenom.survivalPlus.scoreboard

import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

class ScoreboardManager(private val plugin: SurvivalPlus) {

    private val active = ConcurrentHashMap<Player, Boolean>()
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    private var taskId: Int? = null

    fun start() {
        if (!plugin.config.getBoolean("scoreboard.enabled", true)) return
        val interval = plugin.config.getInt("scoreboard.update-interval-ticks", 40).toLong()
        taskId = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, { updateAll() }, 20L, interval)
        plugin.logger.info("Scoreboard Task gestartet (Intervall: ${interval} Ticks)")
    }

    fun stop() {
        taskId?.let { plugin.server.scheduler.cancelTask(it) }
        taskId = null
    }

    fun toggle(player: Player) {
        val current = active.getOrDefault(player, true)
        active[player] = !current
        if (!current) {
            // Aktivieren
            setup(player)
            player.sendMessage("§aScoreboard aktiviert")
        } else {
            // Deaktivieren
            remove(player)
            player.sendMessage("§cScoreboard deaktiviert")
        }
    }

    fun setup(player: Player) {
        if (!plugin.config.getBoolean("scoreboard.enabled", true)) {
            plugin.logger.fine("Scoreboard ist in Config deaktiviert")
            return
        }

        // Prüfe ob Welt in scoreboard.enabled-worlds ist (case-insensitive)
        val scoreboardWorlds = plugin.config.getStringList("scoreboard.enabled-worlds")
        if (scoreboardWorlds.isNotEmpty()) {
            val worldNameLower = player.world.name.lowercase()
            val isWorldEnabled = scoreboardWorlds.any { it.lowercase() == worldNameLower }
            if (!isWorldEnabled) {
                plugin.logger.fine("Scoreboard Setup abgebrochen für ${player.name}: Welt ${player.world.name} nicht in scoreboard.enabled-worlds")
                return
            }
        }

        active.putIfAbsent(player, true)
        val manager = Bukkit.getScoreboardManager() ?: return
        val board = manager.newScoreboard

        val titleRaw = plugin.config.getString("scoreboard.title") ?: "§6§lSurvivalPlus"
        val objective: Objective = board.registerNewObjective("sp", "dummy", ChatColor.translateAlternateColorCodes('&', titleRaw))
        objective.displaySlot = DisplaySlot.SIDEBAR

        player.scoreboard = board
        update(player)

        plugin.logger.fine("Scoreboard erfolgreich setup für ${player.name} in ${player.world.name}")
    }

    fun remove(player: Player) {
        val manager = Bukkit.getScoreboardManager() ?: return
        player.scoreboard = manager.newScoreboard
    }

    private fun updateAll() {
        Bukkit.getOnlinePlayers().forEach { player ->
            if (active.getOrDefault(player, true)) {
                update(player)
            }
        }
    }

    private fun replacePlaceholders(player: Player, line: String): String {
        // Erst alle Farbcodes konvertieren
        var result = ChatColor.translateAlternateColorCodes('&', line)

        val worldTier = plugin.worldTierManager.getWorldTier(player.world)

        // World Tier mit Farbe anzeigen - NACH der Konvertierung einfügen
        val colorCode = when(worldTier.tier) {
            1 -> "§7"  // Normal - Grau
            2 -> "§a"  // Heroic - Grün
            3 -> "§5"  // Epic - Lila
            4 -> "§6"  // Legendary - Gold
            5 -> "§c"  // Mythic - Rot
            else -> "§7"
        }
        val coloredTierName = colorCode + worldTier.displayName
        result = result.replace("%world_tier%", coloredTierName)

        result = result.replace("%online_players%", Bukkit.getOnlinePlayers().size.toString())
        val activeEventName = plugin.worldEventManager.getActiveEvent(player.world)?.name
            ?: plugin.config.getString("scoreboard.placeholders.empty-event") ?: "Kein"
        result = result.replace("%active_event%", activeEventName)
        result = result.replace("%special_mobs%", plugin.specialMobManager.getSpecialMobCount(player.world.uid).toString())
        result = result.replace("%shrines%", plugin.shrineManager.getShrinesInWorld(player.world).size.toString())
        result = result.replace("%ping%", player.ping.toString())
        result = result.replace("%x%", player.location.blockX.toString())
        result = result.replace("%z%", player.location.blockZ.toString())
        val biomeName = try { player.location.block.biome.toString() } catch (_: Exception) { plugin.config.getString("scoreboard.placeholders.empty-biome") ?: "?" }
        result = result.replace("%biome%", biomeName)
        val time = LocalTime.now().format(formatter)
        result = result.replace("%time%", time)
        return result
    }

    fun update(player: Player) {
        val board = player.scoreboard
        val objective = board.getObjective("sp") ?: return

        // Lösche alte Scores (Neuschreiben für dynamische Reihenfolge)
        board.entries.forEach { board.resetScores(it) }

        val lines = plugin.config.getStringList("scoreboard.lines")
        var scoreValue = lines.size
        lines.forEach { raw ->
            val line = replacePlaceholders(player, raw).take(40) // Max 40 Zeichen
            objective.getScore(line).score = scoreValue--
        }
    }
}
