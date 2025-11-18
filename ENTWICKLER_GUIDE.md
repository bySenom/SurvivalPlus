# SurvivalPlus - Entwickler-Guide

## Schnellstart für neue Features

### 1. Ein neues Custom Item hinzufügen

```kotlin
// In ItemManager.kt - createItem() erweitern
when {
    material.name.contains("TRIDENT") -> {
        customItem.stats["Schaden"] = 8.0
        customItem.stats["Wurfgeschwindigkeit"] = 1.2
    }
}
```

### 2. Neue Qualitätsstufe hinzufügen

```kotlin
// In Quality.kt
enum class Quality {
    // ...existing qualities...
    DIVINE("Goettlich", NamedTextColor.LIGHT_PURPLE, 0.1, 4.0, 7)
}
```

### 3. Neuen Reforging-Tier hinzufügen

```kotlin
// In ReforgingTier.kt
enum class ReforgingTier {
    // ...existing tiers...
    TIER_4(
        "Tier 4 - Adamantit",
        Material.NETHERITE_SCRAP,
        "Adamantit",
        "Custom Dimension",
        listOf(Quality.LEGENDARY, Quality.MYTHIC, Quality.DIVINE),
        10
    )
}
```

### 4. Neuen Command hinzufügen

```kotlin
// In SurvivalPlusCommand.kt - onCommand()
when (args[0].lowercase()) {
    // ...existing commands...
    "upgrade" -> handleUpgrade(sender, args)
}

private fun handleUpgrade(sender: CommandSender, args: Array<out String>) {
    // Deine Logic hier
}
```

### 5. Neuen Event Listener hinzufügen

```kotlin
// Neue Datei: PlayerInteractListener.kt
package org.bysenom.survivalPlus.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class PlayerInteractListener(private val plugin: SurvivalPlus) : Listener {
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // Deine Logic
    }
}

// In SurvivalPlus.kt registrieren:
server.pluginManager.registerEvents(PlayerInteractListener(this), this)
```

## GUI-System implementieren

### Reforging-GUI Beispiel

```kotlin
// Neue Datei: ReforgingGUI.kt
package org.bysenom.survivalPlus.gui

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ReforgingGUI(private val plugin: SurvivalPlus) {
    
    fun open(player: Player, item: ItemStack) {
        val inv = Bukkit.createInventory(null, 27, Component.text("Reforging"))
        
        // Item zum Reforgen
        inv.setItem(13, item)
        
        // Reforging-Materialien
        inv.setItem(10, createMaterialIcon(Material.CALCITE, "Tier 1"))
        inv.setItem(12, createMaterialIcon(Material.RAW_GOLD, "Tier 2"))
        inv.setItem(14, createMaterialIcon(Material.RAW_IRON, "Tier 3"))
        
        player.openInventory(inv)
    }
    
    private fun createMaterialIcon(material: Material, name: String): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName(Component.text(name))
        item.itemMeta = meta
        return item
    }
}

// Listener für GUI-Klicks
class ReforgingGUIListener(private val plugin: SurvivalPlus) : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        // Handle clicks
    }
}
```

## Custom Ore Generation

### Beispiel: Kalkstein-Erz

```kotlin
// Neue Datei: OreGenerator.kt
package org.bysenom.survivalPlus.worldgen

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import java.util.*

class CustomOrePopulator : BlockPopulator() {
    
    override fun populate(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        limitedRegion: LimitedRegion
    ) {
        // Kalkstein generieren
        for (i in 0..3) { // 4 Versuche pro Chunk
            val x = chunkX * 16 + random.nextInt(16)
            val z = chunkZ * 16 + random.nextInt(16)
            val y = random.nextInt(64) + 16 // Y: 16-80
            
            val location = limitedRegion.getBlockAt(x, y, z).location
            if (location.block.type == Material.STONE) {
                location.block.type = Material.CALCITE // Kalkstein
            }
        }
    }
}

// In SurvivalPlus.kt registrieren:
override fun onEnable() {
    // ...
    server.worlds.forEach { world ->
        world.populators.add(CustomOrePopulator())
    }
}
```

## Stat-System erweitern

### Neue Stats hinzufügen

```kotlin
// In CustomItem.kt
data class CustomItem(
    // ...existing fields...
    val extraStats: MutableMap<ExtraStat, Double> = mutableMapOf()
)

enum class ExtraStat {
    CRITICAL_CHANCE,
    CRITICAL_DAMAGE,
    LIFESTEAL,
    SPEED,
    KNOCKBACK_RESISTANCE
}

// In ItemManager.kt
customItem.extraStats[ExtraStat.CRITICAL_CHANCE] = 5.0
customItem.extraStats[ExtraStat.LIFESTEAL] = 2.5
```

## Achievement-System

```kotlin
// Neue Datei: Achievement.kt
package org.bysenom.survivalPlus.achievements

enum class Achievement(
    val id: String,
    val displayName: String,
    val description: String,
    val reward: ItemStack?
) {
    FIRST_RARE(
        "first_rare",
        "Erste Seltene!",
        "Erhalte dein erstes seltenes Item",
        null
    ),
    FIRST_MYTHIC(
        "first_mythic",
        "Mythische Macht!",
        "Erhalte dein erstes mythisches Item",
        null
    )
}

// Neue Datei: AchievementManager.kt
class AchievementManager(private val plugin: SurvivalPlus) {
    
    private val playerAchievements = mutableMapOf<UUID, MutableSet<Achievement>>()
    
    fun grantAchievement(player: Player, achievement: Achievement) {
        val uuid = player.uniqueId
        val achievements = playerAchievements.getOrPut(uuid) { mutableSetOf() }
        
        if (achievements.add(achievement)) {
            player.sendMessage(
                Component.text("Achievement freigeschaltet: ${achievement.displayName}")
                    .color(NamedTextColor.GOLD)
            )
            achievement.reward?.let { player.inventory.addItem(it) }
        }
    }
}
```

## Dungeon-System Grundgerüst

```kotlin
// Neue Datei: Dungeon.kt
package org.bysenom.survivalPlus.dungeons

import org.bukkit.Location
import org.bukkit.entity.Player

data class Dungeon(
    val id: String,
    val name: String,
    val spawnLocation: Location,
    val minPlayers: Int,
    val maxPlayers: Int,
    val difficulty: DungeonDifficulty,
    val bosses: List<DungeonBoss>
)

enum class DungeonDifficulty {
    EASY, NORMAL, HARD, MYTHIC
}

data class DungeonBoss(
    val name: String,
    val health: Double,
    val drops: List<ItemStack>
)

class DungeonManager(private val plugin: SurvivalPlus) {
    
    private val activeDungeons = mutableMapOf<String, DungeonInstance>()
    
    fun startDungeon(dungeon: Dungeon, players: List<Player>) {
        val instance = DungeonInstance(dungeon, players)
        activeDungeons[dungeon.id] = instance
        instance.start()
    }
}

class DungeonInstance(
    val dungeon: Dungeon,
    val players: List<Player>
) {
    fun start() {
        players.forEach { player ->
            player.teleport(dungeon.spawnLocation)
            player.sendMessage("Dungeon gestartet: ${dungeon.name}")
        }
    }
}
```

## Custom Enchantments

```kotlin
// Neue Datei: CustomEnchantment.kt
package org.bysenom.survivalPlus.enchantments

enum class CustomEnchantment(
    val displayName: String,
    val maxLevel: Int,
    val rarityQuality: Quality
) {
    LIFESTEAL("Lebensraub", 3, Quality.RARE),
    EXPLOSIVE("Explosiv", 2, Quality.EPIC),
    SOUL_BOUND("Seelengebunden", 1, Quality.LEGENDARY),
    DIVINE_PROTECTION("Goettlicher Schutz", 5, Quality.MYTHIC)
}

class EnchantmentManager(private val plugin: SurvivalPlus) {
    
    fun applyEnchantment(
        item: ItemStack,
        enchantment: CustomEnchantment,
        level: Int
    ): ItemStack {
        // Apply enchantment logic
        return item
    }
}
```

## Tipps & Best Practices

### 1. Verwende lateinit für Plugin-Dependencies
```kotlin
lateinit var dungeonManager: DungeonManager
    private set
```

### 2. Sealed Classes für Type-Safe Results
```kotlin
sealed class DungeonResult {
    object Success : DungeonResult()
    data class Error(val message: String) : DungeonResult()
}
```

### 3. Extension Functions für Bukkit-Klassen
```kotlin
fun Player.hasCustomItem(): Boolean {
    return plugin.itemManager.isCustomItem(inventory.itemInMainHand)
}
```

### 4. Companion Objects für Constants
```kotlin
companion object {
    const val MAX_STAT_VALUE = 1000.0
    const val MIN_PLAYERS = 1
    const val MAX_PLAYERS = 4
}
```

### 5. Coroutines für Async Operations
```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

// Usage:
GlobalScope.launch {
    // Async operation
}
```

## Debugging

### Logging
```kotlin
plugin.logger.info("Info message")
plugin.logger.warning("Warning message")
plugin.logger.severe("Error message")
```

### Debug-Mode in config.yml
```yaml
debug:
  enabled: true
  verbose-logging: true
```

### Performance Monitoring
```kotlin
val start = System.currentTimeMillis()
// ... operation ...
val end = System.currentTimeMillis()
plugin.logger.info("Operation took ${end - start}ms")
```

## Nützliche Gradle-Befehle

```bash
# Build
./gradlew build

# Test-Server starten
./gradlew runServer

# Clean Build
./gradlew clean build

# Dependencies anzeigen
./gradlew dependencies

# Shadowjar erstellen
./gradlew shadowJar
```

## Ressourcen

- **Paper API Docs:** https://jd.papermc.io/paper/1.21/
- **Spigot Forums:** https://www.spigotmc.org/
- **Kotlin Docs:** https://kotlinlang.org/docs/home.html
- **Adventure Text Docs:** https://docs.adventure.kyori.net/

---

Viel Erfolg beim Entwickeln! 🚀

