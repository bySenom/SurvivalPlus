# 📜 Changelog

Alle wichtigen Änderungen an diesem Projekt werden in dieser Datei dokumentiert.

Das Format basiert auf [Keep a Changelog](https://keepachangelog.com/de/1.0.0/),
und dieses Projekt folgt [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### In Arbeit
- Erweiterte Stats System
- Skill-System Grundgerüst
- Achievement-System
- Item-Upgrade-System

---

## [1.2.0-SNAPSHOT] - 2025-11-16

### ✨ Hinzugefügt

#### Custom Enchantments System
- **12 neue Custom Enchantments:**
  - Lifesteal (Lebensraub) - 3 Level
  - Explosive (Explosiv) - 2 Level
  - Soul Bound (Seelengebunden) - 1 Level
  - Thunder Strike (Blitzschlag) - 2 Level
  - Vampire (Vampirismus) - 3 Level
  - Divine Protection (Göttlicher Schutz) - 5 Level
  - Thorns+ (Dornen+) - 3 Level
  - Speed Boost (Geschwindigkeitsschub) - 2 Level
  - Auto Smelt (Auto-Schmelze) - 1 Level
  - Vein Miner (Erzader-Abbau) - 1 Level
  - Timber (Holzfäller) - 1 Level
  - Unbreakable (Unzerstörbar) - 1 Level
- EnchantmentManager für Enchantment-Verwaltung
- EnchantmentListener für funktionale Effekte
- Qualitätsabhängige Enchantment-Vergabe
- Enchantment-Anzeige in Item-Lore

#### Set-Boni System
- **6 Armor Sets:**
  - Guardian Set (Uncommon+) - Verteidigung & Gesundheit
  - Berserker Set (Rare+) - Angriff & Geschwindigkeit
  - Assassin Set (Epic+) - Crits & Mobilität
  - Tank Set (Legendary+) - Maximale Verteidigung
  - Elemental Set (Legendary+) - Elementar-Macht
  - Godlike Set (Mythic) - Ultimative Macht
- 2-Piece und 4-Piece Boni für jedes Set
- SetBonusManager mit Auto-Detection
- SetBonusListener für Event-Handling
- Kritische Treffer mit Set-Boni Integration
- Set-Informationen in Item-Lore
- Potion-Effekte bei aktiven Set-Boni
- Visual Feedback (Partikel, Sounds, Messages)

#### Dokumentation
- Vollständige Wiki-Dokumentation (WIKI.md)
- Anfänger-Guide mit Schritt-für-Schritt Anleitungen (BEGINNER_GUIDE.md)
- README.md mit Projekt-Übersicht
- Dieses Changelog

### 🔧 Geändert
- Item-Lore erweitert um Enchantments
- Item-Lore erweitert um Set-Informationen
- Drop-System berücksichtigt nun Enchantments
- Reforging kann Enchantments ändern

### 🐛 Behoben
- Particle-Namen für Minecraft 1.21 aktualisiert
- Attribute-Namen für Minecraft 1.21 aktualisiert
- PotionEffectType-Namen korrigiert

### 📊 Statistiken
- **Neue Dateien:** 6
- **Neue Zeilen Code:** ~1173
- **Build-Status:** ✅ Erfolgreich

---

## [1.1.0] - 2025-11-16

### ✨ Hinzugefügt

#### Reforging-GUI
- Vollständiges Inventory-basiertes GUI
- 54-Slot Interface mit professionellem Layout
- Item-Preview Slot in der Mitte
- 3 Reforging-Tier Buttons (Kalkstein, Pyrit, Galena)
- Bestätigungs- und Abbrechen-Buttons
- Info-Button mit Anleitung
- Dynamische Material-Verfügbarkeits-Checks
- Enchantment-Glint für ausgewählte Tiers
- Automatisches Material-Management
- `/sp reforge` Command

#### Particle-Effekt-System
- ParticleEffectManager für alle Partikel-Effekte
- Qualitätsabhängige Partikel beim Item-Drop:
  - Uncommon: Happy Villager
  - Rare: Electric Spark
  - Epic: Enchant
  - Legendary: End Rod + Firework
  - Mythic: Soul Fire Flame + Dragon Breath + Enchant
- Reforging-Erfolg Partikel
- Equip-Partikel für Legendary/Mythic Items
- Mythic Aura-Effekt (kontinuierlich)
- Konfigurierbare Partikel-Dichte

#### Sound-Effekt-System
- SoundManager für alle Sound-Effekte
- Qualitätsabhängige Sounds beim Item-Drop
- Reforging Erfolg/Fehler Sounds
- Equip-Sounds für High-Quality Items
- UI-Sounds (Klicks, Fehler)
- Konfigurierbare Lautstärke

#### Item-Glow Effekt
- Automatischer Enchantment-Glint für Epic+ Items
- Aktiviert für Qualität Tier 4+ (Epic, Legendary, Mythic)
- Macht hochwertige Items sofort erkennbar

#### Title/Subtitle Messages
- MessageManager für Title/Subtitle Nachrichten
- Item Received: Title/Subtitle bei Legendary/Mythic Drop
- Reforging Success: Zeigt Upgrade von alter → neuer Qualität
- Achievement-Title (vorbereitet)
- Error-Title für Fehlermeldungen
- Boss-Spawn-Title (vorbereitet)
- Animierte Timings (Fade In/Stay/Fade Out)
- Qualitätsfarben beibehalten

#### Quality Plates System
- QualityPlateManager für Hologramme
- Armor Stand basierte Hologramme über gedroppted Items
- Zeigt Qualität mit Farbe: ✦ [Qualität] ✦
- Automatisches Movement-Tracking (alle 2 Ticks)
- Entfernt sich beim Aufheben oder Despawn
- Konfigurierbare Mindest-Qualität
- Sauberes Cleanup-System
- ItemDropListener für Auto-Management

### 🔧 Geändert
- config.yml erweitert um neue Features
- ItemManager nutzt nun Particle- und Sound-Effekte
- ItemListener zeigt nun Title Messages
- ReforgingGUIListener mit Particle- und Sound-Integration

### ⚙️ Konfiguration
```yaml
features:
  particle-effects: true
  sound-effects: true
  mythic-aura: true
  title-messages: true
  item-glow: true
  quality-plates: true

sound:
  volume: 1.0

particles:
  density: 1.0

quality-plates:
  min-tier: 1
  show-for-all: true
  distance: 10

gui:
  reforging-title: "Reforging Station"
  confirm-on-click: false
```

### 📊 Statistiken
- **Neue Dateien:** 7
- **Neue Zeilen Code:** ~1084
- **Build-Status:** ✅ Erfolgreich

---

## [1.0.0] - 2025-11-15

### ✨ Hinzugefügt

#### Qualitätssystem
- 6 Qualitätsstufen implementiert:
  - Common (⚪ Weiß) - 50% Drop-Chance
  - Uncommon (🟢 Grün) - 30% Drop-Chance
  - Rare (🔵 Blau) - 15% Drop-Chance
  - Epic (🟣 Lila) - 4% Drop-Chance
  - Legendary (🟡 Gold) - 0.9% Drop-Chance
  - Mythic (🔴 Rot) - 0.1% Drop-Chance
- Quality Enum mit Farben und Gewichtungen
- Stat-Boni basierend auf Qualität (+0% bis +200%)

#### Reforging-System
- 3 Reforging-Tiers implementiert:
  - Tier 1: Kalkstein (Overworld) → Common/Uncommon/Rare
  - Tier 2: Pyrit (Nether) → Uncommon/Rare/Epic/Legendary
  - Tier 3: Galena (End) → Rare/Epic/Legendary/Mythic
- ReforgingManager für Reforging-Logik
- ReforgingResult sealed class für Ergebnis-Handling
- Material-basiertes Reforging-System

#### Item-System
- CustomItem Data Class für Item-Konfiguration
- ItemManager für Item-Erstellung und -Verwaltung
- Stat-Bonus-Berechnung basierend auf Qualität
- Persistent Data Storage für Item-Qualität
- Item-Lore mit Qualitäts-Anzeige und Stats

#### Commands
- `/sp help` - Zeigt alle verfügbaren Commands
- `/sp info` - Zeigt Item-Informationen
- `/sp give <player> <material> <quality>` - Gibt Custom Item
- `/sp reload` - Lädt Config neu
- Tab-Completion für alle Commands

#### Config-System
- config.yml mit vollständiger Konfiguration
- Feature-Toggles
- Drop-Chancen konfigurierbar
- Reforging-Materialien konfigurierbar
- Stat-Multiplikatoren anpassbar

#### Listeners
- ItemListener für Block-Break Events
- Quality-abhängige Item-Drops
- Chat-Nachrichten bei Quality-Items

#### Plugin-Struktur
- plugin.yml mit Commands und Permissions
- build.gradle.kts mit Shadow-Plugin
- Kotlin 2.0.0
- Paper API 1.21

### ⚙️ Konfiguration
```yaml
features:
  custom-item-drops: true
  reforging-system: true
  quality-plates: true
  stat-bonuses: true

drop-chances:
  common: 50.0
  uncommon: 30.0
  rare: 15.0
  epic: 4.0
  legendary: 0.9
  mythic: 0.1

reforging:
  tier1:
    material: STONE
    cost: 3
  tier2:
    material: GOLD_INGOT
    cost: 3
  tier3:
    material: NETHERITE_SCRAP
    cost: 3

stats:
  common: 1.0
  uncommon: 1.1
  rare: 1.25
  epic: 1.5
  legendary: 2.0
  mythic: 3.0
```

### 📊 Statistiken
- **Dateien:** 12
- **Zeilen Code:** ~1500
- **Build-Status:** ✅ Erfolgreich

---

## [0.1.0] - 2025-11-14 (Initial Release)

### ✨ Hinzugefügt
- Projekt-Setup mit Gradle und Kotlin
- Basis-Plugin-Struktur
- Paper API 1.21 Integration
- Shadow-Plugin für JAR-Packaging

---

## Legende

- ✨ **Hinzugefügt** - Neue Features
- 🔧 **Geändert** - Änderungen an existierenden Features
- 🗑️ **Entfernt** - Entfernte Features
- 🐛 **Behoben** - Bug-Fixes
- 🔒 **Sicherheit** - Sicherheits-Updates
- ⚙️ **Konfiguration** - Config-Änderungen
- 📊 **Statistiken** - Projekt-Statistiken
- ⚡ **Performance** - Performance-Verbesserungen
- 📚 **Dokumentation** - Dokumentations-Änderungen

---

## Version-Nummern

Dieses Projekt folgt [Semantic Versioning](https://semver.org/):

**MAJOR.MINOR.PATCH**

- **MAJOR** - Inkompatible API-Änderungen
- **MINOR** - Neue Features (abwärtskompatibel)
- **PATCH** - Bug-Fixes (abwärtskompatibel)

Zusätze:
- **-SNAPSHOT** - Entwicklungsversion
- **-ALPHA** - Alpha-Version (instabil)
- **-BETA** - Beta-Version (testing)
- **-RC** - Release Candidate

---

*Letzte Aktualisierung: 2025-11-16*

