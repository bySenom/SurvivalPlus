package org.bysenom.survivalPlus.events

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.World
import org.bukkit.entity.EntityType
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * Verwaltet World Events (vereinfachte Version)
 */
class WorldEventManager(private val plugin: SurvivalPlus) {

    // World UUID -> Aktives Event
    private val activeEvents = ConcurrentHashMap<UUID, WorldEvent>()

    private var checkTask: Int? = null

    /**
     * Startet den Event-Check-Task
     */
    fun start() {
        val interval = plugin.config.getLong("world-events.check-interval", 300) * 20L // Sekunden zu Ticks

        checkTask = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            checkAndStartEvents()
        }, interval, interval)

        plugin.logger.info("World Event System gestartet (Check alle ${interval / 20} Sekunden)")
    }

    /**
     * Prüft und startet zufällige Events
     */
    private fun checkAndStartEvents() {
        if (!plugin.config.getBoolean("world-events.enabled", true)) return

        plugin.server.worlds.forEach { world ->
            // Nur in aktivierten Welten
            if (!plugin.worldTierManager.isEnabledWorld(world)) return@forEach

            // Nur wenn kein Event aktiv ist
            if (hasActiveEvent(world)) return@forEach

            // Prüfe Mindest-Spieleranzahl
            val minPlayers = plugin.config.getInt("world-events.min-players", 1)
            if (world.playerCount < minPlayers) return@forEach

            // Zufällige Chance für Event
            if (Random.nextDouble(100.0) < 15.0) { // 15% Chance pro Check
                startRandomEvent(world)
            }
        }
    }


    /**
     * Startet ein zufälliges Event (public für Commands)
     */
    fun startRandomEvent(world: World) {
        if (!plugin.worldTierManager.isEnabledWorld(world)) {
            return
        }

        val eventType = WorldEventType.entries.random()

        val event = when (eventType) {
            WorldEventType.MOB_HORDE -> createMobHordeEvent(world)
            WorldEventType.TREASURE_GOBLIN -> createTreasureGoblinEvent(world)
            WorldEventType.MOON_EVENT -> createBloodMoonEvent(world)
            WorldEventType.FALLING_BLOCKS -> createMeteorShowerEvent(world)
            WorldEventType.ELITE_BOSS -> createBossHordeEvent(world)
        }

        startEvent(event)
    }

    /**
     * Startet ein Event
     */
    fun startEvent(event: WorldEvent) {
        activeEvents[event.world.uid] = event

        // Announce
        if (event.announceGlobally) {
            plugin.server.onlinePlayers.forEach { player ->
                val title = Title.title(
                    event.getDisplayComponent(),
                    Component.text("Ein Weltereignis hat begonnen!").color(NamedTextColor.YELLOW),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
                )
                player.showTitle(title)
                player.playSound(player.location, org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1f)
            }
        } else {
            event.world.players.forEach { player ->
                player.sendMessage(Component.text("⚔ ").color(NamedTextColor.GOLD)
                    .append(event.getDisplayComponent())
                    .append(Component.text(" hat begonnen!").color(NamedTextColor.YELLOW)))
            }
        }

        // Starte Event-Logik
        executeEventLogic(event)

        // Schedule Event-Ende
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            endEvent(event)
        }, event.duration * 20L)

        plugin.logger.info("World Event gestartet: ${event.name} in ${event.world.name}")
    }

    /**
     * Führt Event-spezifische Logik aus
     */
    private fun executeEventLogic(event: WorldEvent) {
        when (event.type) {
            WorldEventType.MOB_HORDE -> {
                // Spawn viele Mobs
                spawnMobHorde(event.world, 20)
            }
            WorldEventType.TREASURE_GOBLIN -> {
                // Spawn Schatzgoblin
                spawnTreasureGoblin(event.world)
            }
            WorldEventType.MOON_EVENT -> {
                // Setze Zeit auf Nacht
                event.world.setTime(13000)
            }
            WorldEventType.FALLING_BLOCKS -> {
                // Spawn Meteore (vereinfacht)
                scheduleMeteorShower(event.world, event.duration)
            }
            WorldEventType.ELITE_BOSS -> {
                // Spawn Boss mit Minions
                spawnEliteBoss(event.world)
            }
        }
    }

    /**
     * Beendet ein Event
     */
    private fun endEvent(event: WorldEvent) {
        activeEvents.remove(event.world.uid)

        event.world.players.forEach { player ->
            player.sendMessage(Component.text("⚔ ").color(NamedTextColor.GOLD)
                .append(event.getDisplayComponent())
                .append(Component.text(" ist beendet!").color(NamedTextColor.GRAY)))
        }

        plugin.logger.info("World Event beendet: ${event.name} in ${event.world.name}")
    }

    /**
     * Prüft ob eine Welt ein aktives Event hat
     */
    fun hasActiveEvent(world: World): Boolean {
        return activeEvents.containsKey(world.uid)
    }

    /**
     * Gibt das aktive Event einer Welt zurück
     */
    fun getActiveEvent(world: World): WorldEvent? {
        return activeEvents[world.uid]
    }

    // Event-Creation Helpers (vereinfacht)

    private fun createMobHordeEvent(world: World) = WorldEvent(
        UUID.randomUUID(),
        WorldEventType.MOB_HORDE,
        "Dämonische Invasion",
        world,
        300,
        System.currentTimeMillis(),
        2,
        true
    )

    private fun createTreasureGoblinEvent(world: World) = WorldEvent(
        UUID.randomUUID(),
        WorldEventType.TREASURE_GOBLIN,
        "Schatzgoblin",
        world,
        60,
        System.currentTimeMillis(),
        2,
        false
    )

    private fun createBloodMoonEvent(world: World) = WorldEvent(
        UUID.randomUUID(),
        WorldEventType.MOON_EVENT,
        "Blutmond",
        world,
        600,
        System.currentTimeMillis(),
        3,
        true
    )

    private fun createMeteorShowerEvent(world: World) = WorldEvent(
        UUID.randomUUID(),
        WorldEventType.FALLING_BLOCKS,
        "Meteoritenschauer",
        world,
        180,
        System.currentTimeMillis(),
        1,
        true
    )

    private fun createBossHordeEvent(world: World) = WorldEvent(
        UUID.randomUUID(),
        WorldEventType.ELITE_BOSS,
        "Boss-Horde",
        world,
        300,
        System.currentTimeMillis(),
        4,
        true
    )

    // Event-Logic Helpers (vereinfachte Implementierung)

    private fun spawnMobHorde(world: World, count: Int) {
        world.players.forEach { player ->
            repeat(count) {
                val loc = player.location.add(Random.nextDouble(-20.0, 20.0), 0.0, Random.nextDouble(-20.0, 20.0))
                val mob = world.spawnEntity(loc, EntityType.ZOMBIE) as? org.bukkit.entity.Mob
                mob?.let { plugin.specialMobManager.createSpecialMob(it) }
            }
        }
    }

    private fun spawnTreasureGoblin(world: World) {
        world.players.firstOrNull()?.let { player ->
            val loc = player.location.add(Random.nextDouble(-10.0, 10.0), 0.0, Random.nextDouble(-10.0, 10.0))
            val goblin = world.spawnEntity(loc, EntityType.PIGLIN_BRUTE) as? org.bukkit.entity.Mob
            goblin?.let {
                it.customName(Component.text("💰 Schatzgoblin 💰").color(NamedTextColor.GOLD))
                it.isCustomNameVisible = true
            }
        }
    }

    private fun scheduleMeteorShower(world: World, duration: Int) {
        var remaining = duration
        val task = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, {
            if (remaining <= 0) return@scheduleSyncRepeatingTask

            world.players.forEach { player ->
                val loc = player.location.add(Random.nextDouble(-30.0, 30.0), 50.0, Random.nextDouble(-30.0, 30.0))
                world.spawnFallingBlock(loc, org.bukkit.Material.MAGMA_BLOCK.createBlockData())
            }

            remaining -= 10
        }, 0L, 200L) // Alle 10 Sekunden
    }

    private fun spawnEliteBoss(world: World) {
        world.players.firstOrNull()?.let { player ->
            val loc = player.location.add(10.0, 0.0, 10.0)
            val boss = world.spawnEntity(loc, EntityType.WITHER_SKELETON) as? org.bukkit.entity.Mob
            boss?.let {
                plugin.specialMobManager.createSpecialMob(it, org.bysenom.survivalPlus.mobs.MobAffix.BERSERKER)

                // Spawn Minions
                repeat(5) {
                    val minionLoc = loc.add(Random.nextDouble(-5.0, 5.0), 0.0, Random.nextDouble(-5.0, 5.0))
                    val minion = world.spawnEntity(minionLoc, EntityType.SKELETON) as? org.bukkit.entity.Mob
                    minion?.let { m -> plugin.specialMobManager.createSpecialMob(m) }
                }
            }
        }
    }

    /**
     * Stoppt den Event-Manager
     */
    fun stop() {
        checkTask?.let { plugin.server.scheduler.cancelTask(it) }
        activeEvents.clear()
    }
}

