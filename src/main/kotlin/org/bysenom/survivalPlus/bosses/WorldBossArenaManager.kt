package org.bysenom.survivalPlus.bosses

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * World Boss Arena Manager
 * 
 * Verwaltet die Survival_boss Welt und Boss-Spawns alle 30 Minuten
 */
class WorldBossArenaManager(private val plugin: SurvivalPlus) {

    companion object {
        const val BOSS_WORLD_NAME = "Survival_boss"
        const val SPAWN_INTERVAL_MINUTES = 30
        const val ARENA_RADIUS = 50
        const val BOSS_WARNING_TIME = 60 // Sekunden vor Spawn
    }

    private var bossWorld: World? = null
    private var arenaCenter: Location? = null
    private var spawnTask: BukkitRunnable? = null
    private var activeBoss: UUID? = null
    private val playersInArena = ConcurrentHashMap.newKeySet<UUID>()
    
    private var nextBossTime: Long = 0
    private var warningGiven = false

    /**
     * Initialisiert das Arena-System
     */
    fun initialize() {
        // Lade oder erstelle Boss-Welt
        bossWorld = Bukkit.getWorld(BOSS_WORLD_NAME) ?: run {
            plugin.logger.warning("⚠ Welt '$BOSS_WORLD_NAME' nicht gefunden!")
            plugin.logger.warning("⚠ Erstelle bitte die Welt oder Boss-Arena wird nicht funktionieren!")
            return
        }

        // Setze Arena-Center (Spawn der Welt)
        arenaCenter = bossWorld?.spawnLocation?.clone()?.apply {
            x = 0.5
            y = 100.0
            z = 0.5
        }

        // Bereite Arena vor
        prepareArena()

        // Starte Boss-Spawn-Timer
        startSpawnTimer()

        plugin.logger.info("✓ World Boss Arena initialisiert")
        plugin.logger.info("  - Welt: $BOSS_WORLD_NAME")
        plugin.logger.info("  - Spawn-Intervall: $SPAWN_INTERVAL_MINUTES Minuten")
        plugin.logger.info("  - Arena-Zentrum: ${arenaCenter?.blockX}, ${arenaCenter?.blockY}, ${arenaCenter?.blockZ}")
    }

    /**
     * Bereitet die Arena vor (Barrier, Spawnpoint, etc.)
     */
    private fun prepareArena() {
        val center = arenaCenter ?: return
        val world = bossWorld ?: return

        // Setze Welteinstellungen
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        world.setGameRule(GameRule.KEEP_INVENTORY, false)
        world.time = 6000 // Mittag

        plugin.logger.fine("Arena vorbereitet: Mob-Spawning deaktiviert, Zeit fixiert")
    }

    /**
     * Startet den automatischen Boss-Spawn-Timer
     */
    private fun startSpawnTimer() {
        // Berechne erste Spawn-Zeit
        nextBossTime = System.currentTimeMillis() + (SPAWN_INTERVAL_MINUTES * 60 * 1000)
        
        spawnTask = object : BukkitRunnable() {
            override fun run() {
                val now = System.currentTimeMillis()
                val timeUntilBoss = nextBossTime - now

                // Warning 60 Sekunden vorher
                if (!warningGiven && timeUntilBoss <= BOSS_WARNING_TIME * 1000) {
                    broadcastBossWarning()
                    warningGiven = true
                }

                // Boss spawnen
                if (timeUntilBoss <= 0) {
                    spawnWorldBoss()
                    nextBossTime = System.currentTimeMillis() + (SPAWN_INTERVAL_MINUTES * 60 * 1000)
                    warningGiven = false
                }
            }
        }
        
        spawnTask?.runTaskTimer(plugin, 20L, 20L) // Jede Sekunde prüfen
    }

    /**
     * Broadcast Boss-Warnung an alle Spieler
     */
    private fun broadcastBossWarning() {
        val message = Component.text()
            .append(Component.text("⚔ ").color(NamedTextColor.RED))
            .append(Component.text("WORLD BOSS SPAWN IN 60 SEKUNDEN!").color(NamedTextColor.GOLD))
            .append(Component.text(" ⚔").color(NamedTextColor.RED))
            .build()

        Bukkit.getOnlinePlayers().forEach { player ->
            player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.DARK_GRAY))
            player.sendMessage(message)
            player.sendMessage(Component.text("Teleportiere mit: /sp boss enter").color(NamedTextColor.YELLOW))
            player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.DARK_GRAY))
            
            player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 0.8f)
        }
    }

    /**
     * Spawnt den World Boss
     */
    private fun spawnWorldBoss() {
        val center = arenaCenter ?: return
        
        // Wenn noch ein Boss lebt, überspringe
        if (activeBoss != null) {
            plugin.logger.info("Boss noch aktiv, Spawn übersprungen")
            return
        }

        // Spawne Team-Boss (Titan Golem)
        val boss = plugin.titanGolemBoss?.spawnBoss(center, 3) // Tier 3 default
        
        if (boss != null) {
            activeBoss = boss.uniqueId
            
            // Broadcast an alle Spieler
            val title = Title.title(
                Component.text("⚔ WORLD BOSS SPAWNED ⚔").color(NamedTextColor.RED),
                Component.text("The Titan Golem has awakened!").color(NamedTextColor.YELLOW),
                Title.Times.times(
                    Duration.ofMillis(500),
                    Duration.ofMillis(3000),
                    Duration.ofMillis(1000)
                )
            )
            
            Bukkit.getOnlinePlayers().forEach { player ->
                player.showTitle(title)
                player.playSound(player.location, Sound.ENTITY_WITHER_SPAWN, 1f, 0.5f)
            }
            
            plugin.logger.info("✓ World Boss gespawnt: Titan Golem")
        } else {
            plugin.logger.warning("⚠ Boss-Spawn fehlgeschlagen!")
        }
    }

    /**
     * Teleportiert einen Spieler in die Arena
     */
    fun teleportToArena(player: Player): Boolean {
        val center = arenaCenter ?: run {
            player.sendMessage(Component.text("⚠ Arena nicht verfügbar!").color(NamedTextColor.RED))
            return false
        }

        // Teleportiere zum Arena-Eingang
        val entryPoint = center.clone().add(0.0, 0.0, ARENA_RADIUS.toDouble() + 5)
        player.teleport(entryPoint)
        
        playersInArena.add(player.uniqueId)
        
        player.sendMessage(Component.text("✓ Willkommen in der World Boss Arena!").color(NamedTextColor.GREEN))
        player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f)
        
        return true
    }

    /**
     * Teleportiert einen Spieler aus der Arena zurück
     */
    fun teleportFromArena(player: Player): Boolean {
        // Teleportiere zum Survival Spawn
        val survivalWorld = Bukkit.getWorld("Survival") ?: run {
            player.sendMessage(Component.text("⚠ Survival-Welt nicht gefunden!").color(NamedTextColor.RED))
            return false
        }

        player.teleport(survivalWorld.spawnLocation)
        playersInArena.remove(player.uniqueId)
        
        player.sendMessage(Component.text("✓ Du hast die Arena verlassen").color(NamedTextColor.YELLOW))
        player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.8f)
        
        return true
    }

    /**
     * Prüft ob ein Spieler in der Arena ist
     */
    fun isInArena(player: Player): Boolean {
        return player.world.name == BOSS_WORLD_NAME
    }

    /**
     * Gibt die Zeit bis zum nächsten Boss zurück (in Sekunden)
     */
    fun getTimeUntilNextBoss(): Long {
        return ((nextBossTime - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
    }

    /**
     * Boss wurde getötet - Cleanup
     */
    fun onBossDefeated() {
        activeBoss = null
        
        // Broadcast Sieg
        val message = Component.text()
            .append(Component.text("⚔ ").color(NamedTextColor.GOLD))
            .append(Component.text("WORLD BOSS DEFEATED!").color(NamedTextColor.GREEN))
            .append(Component.text(" ⚔").color(NamedTextColor.GOLD))
            .build()

        Bukkit.getOnlinePlayers().forEach { player ->
            player.sendMessage(message)
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
        }
    }

    /**
     * Cleanup beim Plugin-Disable
     */
    fun shutdown() {
        spawnTask?.cancel()
        playersInArena.clear()
        plugin.logger.info("✓ World Boss Arena beendet")
    }
}
