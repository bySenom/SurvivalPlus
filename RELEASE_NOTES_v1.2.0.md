# SurvivalPlus v1.2.0 Release Notes

**Release-Datum:** November 18, 2025  
**Version:** 1.2.0-SNAPSHOT  
**Minecraft-Version:** Paper 1.21+

## 🚀 Major Features

### 🤝 Trading-System (Player-to-Player)
Ein komplett neues Handelssystem zwischen Spielern mit:
- **Trade-GUI**: 4x4 Item-Slots pro Spieler (16 Slots total)
- **Request-System**: `/sp trade <spieler>` sendet Anfrage mit 30s Timeout
- **Scam-Schutz**: Beide Spieler müssen bestätigen, Änderungen reseten Bestätigung
- **Safety Features**:
  - 5 Sekunden Cooldown zwischen Trades
  - Automatischer Item-Return bei Disconnect
  - Trade-Logging für Admin-Übersicht
- **Benutzerfreundlich**:
  - Live-Updates für beide Spieler
  - Status-Anzeige (Confirmed/Unconfirmed)
  - Separator für klare Übersicht

### ⚔️ Boss #1: Der Ernter (The Harvester)
Nether-Boss mit 4 Phasen und Feuer-Mechaniken:

**Stats:**
- Base HP: 500 (skaliert mit World Tier)
- Base Damage: 15 (skaliert mit World Tier)
- Spawn: Nether (manuell via Command)
- Model: Wither Skeleton mit Netherite-Rüstung

**Phasen:**
1. **Phase 1 (100-75% HP)**: Blaze Summons (2 Blazes alle 10s)
2. **Phase 2 (75-50% HP)**: Fire Waves (expandierende Feuerringe alle 15s) + Speed I
3. **Phase 3 (50-25% HP)**: Lava Pools (Lava-Zonen unter Spielern alle 12.5s) + Strength I
4. **Phase 4 (25-0% HP)**: Berserk Mode (5 Blazes, Speed II, Strength II, Resistance I)

**Abilities:**
- **Fire Wave**: Expandierende Feuerringe (2-15 Blöcke Radius), 5 Damage/Tier + Fire
- **Lava Pool**: Temporäre Lava-Zonen, 10 Damage/Tier + 100 Fire Ticks
- **Blaze Summon**: 2-5 Blazes je nach Phase (15 HP/Tier)

**Loot:**
- 2-5x Netherite Scrap
- 5-10x Blaze Rod
- 10-20x Magma Cream
- 5-15x Fire Charge
- 2-5x Custom Items (Rare/Epic/Legendary, skaliert mit Tier)
- 1-5x Enchanted Books (Epic/Legendary)

### ❄️ Boss #2: Frost-Titan
Ice Peaks Boss mit 4 Phasen und Freeze-Mechaniken:

**Stats:**
- Base HP: 600 (skaliert mit World Tier)
- Base Damage: 12 (skaliert mit World Tier)
- Spawn: Beliebige Welt (manuell via Command)
- Model: Iron Golem mit Ice-Particles

**Phasen:**
1. **Phase 1 (100-75% HP)**: Ice Spike Summons (3 Spikes alle 7.5s)
2. **Phase 2 (75-50% HP)**: Frost Aura (10 Blöcke Radius, Slow-Effekt) + Speed
3. **Phase 3 (50-25% HP)**: Blizzard (15 Blöcke Radius, 10s Dauer) + Resistance II
4. **Phase 4 (25-0% HP)**: Absolute Zero (Instant Freeze, Speed I, Resistance III)

**Abilities:**
- **Ice Spike**: 3-8 Eis-Spikes unter Spielern, 8 Damage/Tier + Slow II + Mining Fatigue I
- **Frost Aura**: Distanz-basierter Slow-Effekt (näher = stärker), 2 Damage/Tier in Phase 3+
- **Blizzard**: 10 Sekunden Schneesturm, 3 Damage/Tier + Slow III + Blindness
- **Freeze**: 3 Sekunden komplette Bewegungsunfähigkeit (Slow X + Jump Boost 250 + Mining Fatigue X)

**Loot:**
- 3-8x Diamond
- 10-20x Packed Ice
- 5-15x Blue Ice
- 20-40x Snowball
- 2-5x Custom Items (Rare/Epic/Legendary, skaliert mit Tier)
- 1-5x Enchanted Books (Epic/Legendary)

## 📝 Commands

### Trading
```
/sp trade <spieler>     - Sendet Trade-Anfrage
/sp trade accept        - Akzeptiert Trade-Anfrage
/sp trade deny          - Lehnt Trade-Anfrage ab
```

### Boss-Spawning
```
/sp boss spawn harvester [tier]     - Spawnt Den Ernter (Nether-Boss)
/sp boss spawn frosttitan [tier]    - Spawnt Frost-Titan (Ice-Boss)
```

**Tab-Completion:**
- `/sp trade <TAB>` → `accept`, `deny`, `<online players>`
- `/sp boss spawn <TAB>` → `harvester`, `frosttitan`
- `/sp boss spawn harvester <TAB>` → `1`, `2`, `3`, `4`, `5`

## 🔧 Technical Details

### Neue Dateien
**Trading-System:**
- `trading/TradeManager.kt` (283 Zeilen) - Core Trade-Logik
- `trading/Trade.kt` (165 Zeilen) - Trade-Daten-Klasse
- `trading/TradingGUI.kt` (288 Zeilen) - GUI & Listener
- `trading/TradingListener.kt` (16 Zeilen) - Disconnect-Handling

**Bosse:**
- `mobs/HarvesterBoss.kt` (566 Zeilen) - Harvester Boss mit 4-Phasen AI
- `mobs/FrostTitanBoss.kt` (581 Zeilen) - Frost Titan mit Freeze-System

### Modifizierte Dateien
- `SurvivalPlus.kt`: TradeManager, HarvesterBoss, FrostTitanBoss initialisiert
- `commands/SurvivalPlusCommand.kt`: `/sp trade` und `/sp boss` Commands
- `build.gradle.kts`: Version → 1.2.0-SNAPSHOT

### Architektur-Highlights

**Trading-System:**
- **Thread-Safe**: ConcurrentHashMap für Trade-Storage
- **Request-Timeout**: Automatisches Cleanup nach 30s
- **Item-Safety**: Items werden in Trade-Objekt kopiert
- **GUI-Sync**: Live-Updates für beide Spieler gleichzeitig
- **Layout**: 6 Reihen (4x4 Player, 4x4 Partner, Mitte = Controls)

**Boss-Systeme:**
- **Phase-Management**: HP-basierte Phase-Transitions mit Events
- **Boss-Bar**: Live-Updates mit Phase-Anzeige und Farbwechsel
- **AI-Loop**: 1 Sekunde Tick-Rate für Ability-Execution
- **Cleanup**: Automatisches Cleanup bei Boss-Death oder Server-Stop
- **Particle-Effects**: Phase-spezifische Particle-Systeme
- **Loot-Scaling**: World-Tier-basierte Loot-Quality

**Performance:**
- Harvester Fire Wave: Async Particle-Spawning
- Frost Titan Blizzard: 10s Task mit 1-Tick-Rate
- Boss-Bars: Range-Check (50 Blöcke) für Player-Updates
- Cooldown-System: Timestamp-basiert (keine Scheduler-Tasks)

## 🎮 Gameplay-Balance

### Trading
- **Cooldown**: 5 Sekunden zwischen Trades verhindert Spam
- **Timeout**: 30 Sekunden Request-Timeout verhindert AFK-Requests
- **Inventory-Full**: Items werden gedroppt wenn Inventory voll

### Harvester
- **Nether-Boss**: Passt perfekt ins Nether-Theme
- **Schwierigkeit**: Hoher Damage, aber ausweichbare Attacks
- **Loot**: Nether-Items + Custom Gear (Netherite-Focus)

### Frost Titan
- **Ice-Boss**: Freeze-Mechaniken einzigartig
- **Schwierigkeit**: Höhere HP, aber weniger Damage als Harvester
- **Loot**: Ice-Items + Custom Gear (Diamond-Focus)

## 📊 Statistics

**Code-Umfang v1.2.0:**
- **Neue Zeilen**: 2053 insertions
- **Neue Dateien**: 6 Dateien
- **Geänderte Dateien**: 3 Dateien
- **Build-Zeit**: 11 Sekunden
- **Warnings**: 12 (Deprecation-Warnings, nicht kritisch)

**Feature-Komplexität:**
- Trading-System: ~750 Zeilen
- Harvester Boss: ~570 Zeilen
- Frost Titan Boss: ~580 Zeilen
- Commands & Integration: ~150 Zeilen

## 🔄 Migration von v1.1.0

**Keine Breaking Changes!**
- Alle v1.1.0 Features funktionieren weiterhin
- Neue Commands sind optional
- Bosse spawnen nur via Command (kein Auto-Spawn)
- Trading benötigt keine Config-Änderungen

## 🐛 Known Issues

1. **GitHub Push**: Commit erfolgreich, aber Push-Error (GitHub Server 500)
   - **Status**: Commit lokal gespeichert (1e4aa82)
   - **Workaround**: Retry push später

2. **Boss-Bar Distance**: Boss-Bar verschwindet bei >50 Blöcken
   - **Status**: Working as intended (Performance)
   - **Workaround**: Näher am Boss bleiben

3. **Freeze-Effekt**: Jump Boost 250 = kein Springen, aber Client-Side-Flicker möglich
   - **Status**: Minecraft-Engine Limitation
   - **Impact**: Minimal, Effekt funktioniert

## 🎯 Next Steps (v1.3.0)

Mögliche Features für v1.3.0:
- **Boss-Summon Items**: Crafting-Rezepte für Boss-Spawn (kein Command nötig)
- **Boss-Cooldowns**: Globale Spawn-Cooldowns pro Boss-Typ
- **Trade-History**: Log-System für Admin-Übersicht
- **Boss-Leaderboards**: Scoreboard für schnellste Boss-Kills
- **Custom Boss-Drops**: Einzigartige Set-Items pro Boss

## ⚠️ Developer Notes

**Particle-Änderungen:**
- `Particle.EXPLOSION_HUGE` → `Particle.EXPLOSION_EMITTER` (Paper 1.21+)
- `Particle.FALLING_SNOW` → `Particle.FALLING_DUST` + BlockData (Paper 1.21+)
- `Particle.BLOCK_CRACK` → `Particle.BLOCK` (Paper 1.21+)

**Attribute-API:**
- `Attribute.GENERIC_MAX_HEALTH` → `Attribute.MAX_HEALTH` (Bukkit API)
- `Attribute.GENERIC_ATTACK_DAMAGE` → `Attribute.ATTACK_DAMAGE`
- Alle `GENERIC_*` Prefixe entfernt

**Event-API:**
- `event.cursor` ist val → Verwende `player.setItemOnCursor()` statt direkter Zuweisung

## 🏆 Credits

- **Trading-System**: Inspiriert von Hypixel SkyBlock Trading
- **Harvester Boss**: Diablo 4 Fire-Mechaniken
- **Frost Titan**: Inspired by Dark Souls Frost-Builds
- **Boss-Phases**: Terraria Calamity Mod Boss-Design

---

**Viel Spaß mit v1.2.0! 🎮**
