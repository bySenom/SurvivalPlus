# ⚔️ SurvivalPlus

> **Ein Tierify-ähnliches Qualitätssystem für Minecraft Paper 1.21+**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-green.svg)](https://papermc.io/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.2.0--SNAPSHOT-orange.svg)](https://github.com/yourusername/SurvivalPlus)

---

## 📖 Beschreibung

SurvivalPlus erweitert dein Minecraft-Survival-Erlebnis durch ein umfangreiches **Qualitätssystem** inspiriert von der Mod "Tierify". Jedes Item kann eine von **6 Qualitätsstufen** haben, von Common bis Mythic, mit einzigartigen Stats, Custom Enchantments und Set-Boni!

### ✨ Hauptfeatures

- 🎨 **6 Qualitätsstufen** - Common, Uncommon, Rare, Epic, Legendary, Mythic
- ⚒️ **3-Tier Reforging-System** - Verbessere Items mit speziellen Erzen
- 🏗️ **Custom Blocks** - Platzierbare Custom Anvils & Reforging Stations **[NEU!]**
- 🔮 **12 Custom Enchantments** - Lifesteal, Explosive, Vein Miner, Timber und mehr
- 🛡️ **6 Armor Sets** - Mit 2-Piece und 4-Piece Boni
- 🎯 **Kritische Treffer** - Mit Set-Boni Integration
- ✨ **Particle & Sound Effekte** - Qualitätsabhängige visuelle Effekte
- 🏷️ **Quality Plates** - Hologramme über gedroppted Items
- 💎 **Item-Glow** - Epic+ Items leuchten automatisch
- 📢 **Title Messages** - Bei wichtigen Events
- 🎮 **Custom GUIs** - Crafting & Reforging Interfaces

---

## 📸 Screenshots

```
Demnächst verfügbar!
```

---

## 🚀 Quick Start

### Installation

1. **Download** die neueste Version: [Releases](https://github.com/yourusername/SurvivalPlus/releases)
2. **Kopiere** die JAR-Datei in deinen `plugins/` Ordner
3. **Starte** deinen Server neu
4. **Fertig!** 🎉

### Erste Schritte

```bash
# Item erhalten
/sp give <spieler> DIAMOND_SWORD legendary

# Custom Block erhalten (NEU!)
/sp giveblock <spieler> custom_anvil

# Custom Crafting GUI öffnen
/sp craft

# Reforging-GUI öffnen
/sp reforge

# Item-Info anzeigen
/sp info
```

Für mehr Details siehe [Wiki](WIKI.md), [Custom Blocks Guide](CUSTOM_BLOCKS.md) und [Anfänger-Guide](BEGINNER_GUIDE.md)!

---

## 📊 Features im Detail

### 🎨 Qualitätssystem

| Qualität | Farbe | Drop-Chance | Stats | Enchantments |
|----------|-------|-------------|-------|--------------|
| Common | ⚪ Weiß | 50% | +0% | Keine |
| Uncommon | 🟢 Grün | 30% | +10% | Keine |
| Rare | 🔵 Blau | 15% | +25% | 30% für 1 |
| Epic | 🟣 Lila | 4% | +50% | 50% für 1 |
| Legendary | 🟡 Gold | 0.9% | +100% | 1-2 garantiert |
| Mythic | 🔴 Rot | 0.1% | +200% | 2-3 garantiert |

### ⚒️ Reforging-System

**3 Tiers mit verschiedenen Erzen:**

- **Tier 1 (Kalkstein):** Common → Rare
- **Tier 2 (Pyrit):** Uncommon → Legendary
- **Tier 3 (Galena):** Rare → Mythic

Jedes Reforging kostet **3 Materialien** und würfelt die Qualität neu!

### 🔮 Custom Enchantments

**12 einzigartige Enchantments:**

#### Waffen
- ❤️ **Lifesteal** - Heile dich beim Schaden verursachen
- 💥 **Explosive** - Chance auf Explosion
- 👻 **Soul Bound** - Behalte Item beim Tod
- ⚡ **Thunder Strike** - Beschwöre Blitze
- 🧛 **Vampire** - Absorbiere Leben

#### Rüstung
- ✨ **Divine Protection** - Schadensreduzierung
- 🌵 **Thorns+** - Schadens-Reflektion
- 💨 **Speed Boost** - Geschwindigkeit

#### Werkzeuge
- 🔥 **Auto Smelt** - Erze automatisch schmelzen
- ⛏️ **Vein Miner** - Ganze Erzadern abbauen
- 🪓 **Timber** - Ganze Bäume fällen

#### Universal
- 🛡️ **Unbreakable** - Keine Haltbarkeitsverlust

### 🛡️ Armor Sets

**6 Sets mit unterschiedlichen Themes:**

1. **Guardian** (Uncommon+) - Verteidigung & Gesundheit
2. **Berserker** (Rare+) - Angriff & Geschwindigkeit
3. **Assassin** (Epic+) - Crits & Mobilität
4. **Tank** (Legendary+) - Maximale Verteidigung
5. **Elemental** (Legendary+) - Elementar-Macht
6. **Godlike** (Mythic) - Ultimative Macht

Jedes Set hat **2-Piece** und **4-Piece** Boni!

---

## ⚙️ Konfiguration

### config.yml

```yaml
# Feature Toggles
features:
  custom-item-drops: true
  reforging-system: true
  quality-plates: true
  stat-bonuses: true
  particle-effects: true
  sound-effects: true
  mythic-aura: true
  title-messages: true
  item-glow: true

# Drop-Chancen anpassen
drop-chances:
  common: 50.0
  uncommon: 30.0
  rare: 15.0
  epic: 4.0
  legendary: 0.9
  mythic: 0.1

# Reforging-Materialien
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
```

Siehe [Konfiguration](WIKI.md#konfiguration) für mehr Details!

---

## 🎮 Commands

### Spieler-Commands

| Command | Beschreibung | Permission |
|---------|--------------|------------|
| `/sp help` | Zeigt Hilfe | `survivalplus.help` |
| `/sp info` | Item-Info | `survivalplus.info` |
| `/sp reforge` | Reforging-GUI | `survivalplus.reforge` |
| `/sp stats` | Eigene Stats | `survivalplus.stats` |

### Admin-Commands

| Command | Beschreibung | Permission |
|---------|--------------|------------|
| `/sp give <player> <material> <quality>` | Item geben | `survivalplus.give` |
| `/sp reload` | Config neu laden | `survivalplus.reload` |
| `/sp debug` | Debug-Mode | `survivalplus.debug` |
| `/sp enchant <enchantment> <level>` | Enchantment hinzufügen | `survivalplus.enchant` |

---

## 🛠️ Für Entwickler

### API-Nutzung

```kotlin
// Plugin-Zugriff
val survivalPlus = Bukkit.getPluginManager()
    .getPlugin("SurvivalPlus") as SurvivalPlus

// Custom Item erstellen
val item = survivalPlus.itemManager.createItem(
    Material.DIAMOND_SWORD, 
    Quality.LEGENDARY
)

// Enchantment hinzufügen
survivalPlus.enchantmentManager.addEnchantment(
    item, 
    CustomEnchantment.LIFESTEAL, 
    3
)

// Set zuweisen
survivalPlus.setBonusManager.assignSet(
    helmet, 
    ArmorSet.GODLIKE
)
```

### Events

```kotlin
@EventHandler
fun onQualityItemReceive(event: QualityItemReceiveEvent) {
    val player = event.player
    val quality = event.quality
    // Custom Logik
}
```

Siehe [API-Dokumentation](WIKI.md#api-nutzung) für mehr!

---

## 📚 Dokumentation

- **[Wiki](WIKI.md)** - Vollständige Dokumentation
- **[Anfänger-Guide](BEGINNER_GUIDE.md)** - Für neue Spieler
- **[FAQ](WIKI.md#faq)** - Häufige Fragen
- **[API-Docs](WIKI.md#api-nutzung)** - Für Entwickler

---

## 🏗️ Projektstruktur

```
SurvivalPlus/
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── org/bysenom/survivalPlus/
│       │       ├── commands/         # Commands
│       │       ├── display/          # GUI & Messages
│       │       ├── effects/          # Particles & Sounds
│       │       ├── enchantments/     # Custom Enchantments
│       │       ├── gui/              # Reforging-GUI
│       │       ├── listeners/        # Event-Listener
│       │       ├── managers/         # Core-Manager
│       │       ├── models/           # Datenmodelle
│       │       └── sets/             # Armor Sets
│       └── resources/
│           ├── config.yml
│           └── plugin.yml
├── WIKI.md                   # Haupt-Dokumentation
├── BEGINNER_GUIDE.md        # Anfänger-Guide
├── TODO.md                  # Entwicklungs-Roadmap
└── README.md                # Dieses Dokument
```

---

## 🗺️ Roadmap

### ✅ Phase 1: Basis-System (v1.0.0) - FERTIG
- Qualitätssystem
- Reforging-System
- Basic Commands
- Config-System

### ✅ Phase 2: GUI & Visuals (v1.1.0) - FERTIG
- Reforging-GUI
- Particle-Effekte
- Sound-Effekte
- Quality Plates
- Item-Glow
- Title Messages

### ✅ Phase 3: Erweiterte Features (v1.2.0) - IN ARBEIT (~40% fertig)
- ✅ Custom Enchantments (12 Stück)
- ✅ Set-Boni System (6 Sets)
- ⏳ Erweiterte Stats
- ⏳ Skill-System
- ⏳ Achievement-System

### 🔜 Phase 4: Endgame Content (v2.0.0) - GEPLANT
- Dungeon-System
- Boss-System
- Mythic-Only Features
- Raid-System
- Prestige-System

### 🔜 Phase 5: Weltintegration (v2.1.0) - GEPLANT
- Custom Ore Generation
- Trading-System
- Economy-Integration
- Custom Dimensions

Siehe [TODO.md](TODO.md) für Details!

---

## 📊 Statistiken

- **Zeilen Code:** ~8000+
- **Dateien:** 30+
- **Features:** 50+
- **Qualitäten:** 6
- **Enchantments:** 12
- **Armor Sets:** 6
- **Entwicklungszeit:** 2+ Wochen

---

## 🤝 Mitwirken

Contributions sind willkommen! 

1. **Fork** das Repository
2. **Erstelle** einen Feature-Branch (`git checkout -b feature/AmazingFeature`)
3. **Commit** deine Änderungen (`git commit -m 'Add some AmazingFeature'`)
4. **Push** zum Branch (`git push origin feature/AmazingFeature`)
5. **Öffne** einen Pull Request

### Coding-Standards

- **Sprache:** Kotlin
- **Style:** Kotlin Coding Conventions
- **Kommentare:** Deutsch oder Englisch
- **Tests:** Wo sinnvoll

---

## 📝 Changelog

### v1.2.0-SNAPSHOT (Aktuell)
- ✅ Custom Enchantments System (12 Enchantments)
- ✅ Set-Boni System (6 Armor Sets)
- ✅ Kritische Treffer Integration
- ✅ Vollständige Wiki-Dokumentation
- ✅ Anfänger-Guide

### v1.1.0
- ✅ Reforging-GUI
- ✅ Particle-Effekte
- ✅ Sound-Effekte
- ✅ Quality Plates (Hologramme)
- ✅ Item-Glow für Epic+
- ✅ Title/Subtitle Messages

### v1.0.0
- ✅ Basis-Qualitätssystem
- ✅ Reforging-System
- ✅ Basic Commands
- ✅ Config-System

Siehe vollständiges [Changelog](CHANGELOG.md) (falls vorhanden)

---

## 🐛 Bug-Reports & Feature-Requests

**Bug gefunden?** Öffne ein [Issue](https://github.com/yourusername/SurvivalPlus/issues)

**Feature-Idee?** Teile sie im [Discussions](https://github.com/yourusername/SurvivalPlus/discussions)

**Support benötigt?** Siehe [Wiki](WIKI.md) oder frage in Discord (falls vorhanden)

---

## 📜 Lizenz

Dieses Projekt ist lizenziert unter der MIT License - siehe [LICENSE](LICENSE) für Details.

---

## 🙏 Danksagungen

- **Tierify Mod** - Inspiration für das Qualitätssystem
- **Paper Team** - Für die großartige Server-Software
- **JetBrains** - Für IntelliJ IDEA und Kotlin
- **Community** - Für Feedback und Unterstützung

---

## 📞 Kontakt

**Entwickler:** SashaW  
**Discord:** [Noch nicht verfügbar]  
**Email:** [Noch nicht verfügbar]  
**GitHub:** [Repository-Link]

---

## ⭐ Support

Wenn dir das Plugin gefällt:
- ⭐ Gib dem Repository einen Stern
- 📢 Teile es mit deinen Freunden
- 💬 Gib Feedback
- 🐛 Melde Bugs

---

**Made with ❤️ and ☕ by SashaW**

*Happy Gaming!* 🎮✨

# SurvivalPlus
