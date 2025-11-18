# 📚 SurvivalPlus Wiki

> **Vollständige Dokumentation für SurvivalPlus**  
> Version: 1.2.0-SNAPSHOT  
> Letzte Aktualisierung: 2025-11-16

---

## 📑 Inhaltsverzeichnis

### 🎮 Für Spieler
1. [Erste Schritte](#erste-schritte)
2. [Qualitätssystem](#qualitätssystem)
3. [Reforging-System](#reforging-system)
4. [Custom Enchantments](#custom-enchantments)
5. [Enchantment-Quellen](#enchantment-quellen)
6. [Set-Boni System](#set-boni-system)
7. [World Tier System](#world-tier-system)
8. [Commands & Permissions](#commands--permissions)
9. [FAQ](#faq)

### 👨‍💼 Für Admins
1. [Installation](#installation)
2. [Konfiguration](#konfiguration)
3. [Permissions](#permissions-übersicht)
4. [Performance-Tipps](#performance-tipps)

### 👨‍💻 Für Entwickler
1. [API-Nutzung](#api-nutzung)
2. [Events](#events)
3. [Eigene Extensions](#eigene-extensions)

---

## 🎮 Erste Schritte

### Was ist SurvivalPlus?

SurvivalPlus ist ein umfangreiches Minecraft-Plugin, das das Survival-Erlebnis durch ein **Tierify-ähnliches Qualitätssystem** erweitert. Items haben unterschiedliche Qualitäten, Custom Enchantments und können durch Reforging verbessert werden.

### Hauptfeatures

- ✨ **6 Qualitätsstufen** (Common bis Mythic)
- ⚡ **3-Tier Reforging-System** mit speziellen Erzen
- 🔮 **12 Custom Enchantments** mit einzigartigen Effekten
- 🛡️ **6 Armor Sets** mit 2-Piece und 4-Piece Boni
- 🎨 **Visuelle Effekte** (Particles, Sounds, Hologramme)
- 🎯 **Kritische Treffer** mit Set-Boni
- 💎 **Item-Glow** für hochwertige Items

### Quick Start

1. **Item erhalten:**
   ```
   /sp give <name> <material> <qualität>
   ```
   Beispiel: `/sp give Sasha DIAMOND_SWORD legendary`

2. **Item reforgen:**
   ```
   /sp reforge
   ```
   Öffnet das Reforging-GUI

3. **Item-Info anzeigen:**
   ```
   /sp info
   ```
   Zeigt Details zum Item in deiner Hand

---

## 📊 Qualitätssystem

### Die 6 Qualitätsstufen

| Qualität | Farbe | Drop-Chance | Stat-Bonus | Enchantments |
|----------|-------|-------------|------------|--------------|
| **Common** | Weiß | 50% | +0% Stats | Keine |
| **Uncommon** | Grün | 30% | +10% Stats | Keine |
| **Rare** | Blau | 15% | +25% Stats | 30% für 1 |
| **Epic** | Lila | 4% | +50% Stats | 50% für 1 |
| **Legendary** | Gold | 0.9% | +100% Stats | 1-2 garantiert |
| **Mythic** | Rot | 0.1% | +200% Stats | 2-3 garantiert |

### Wie erkenne ich die Qualität?

1. **Farbe des Item-Namens** - Qualitätsfarbe im Inventar
2. **Item-Glow** - Epic+ Items haben Enchantment-Glint
3. **Lore** - Zeigt Qualität, Stats und Enchantments
4. **Hologramm** - Schwebt über gedroppted Items (✦ Qualität ✦)

### Stat-Boni

Items erhalten Boni basierend auf ihrer Qualität:

**Waffen:**
- Attack Damage: +X%
- Attack Speed: +X%

**Rüstung:**
- Armor: +X%
- Armor Toughness: +X%

**Werkzeuge:**
- Mining Speed: +X%
- Durability: +X%

---

## ⚒️ Reforging-System

### Was ist Reforging?

Reforging ermöglicht es dir, die **Qualität eines Items neu zu würfeln**. Du kannst ein Common Item potenziell zu einem Mythic Item upgraden!

### Die 3 Reforging-Tiers

#### 🪨 Tier 1: Kalkstein (Overworld)
- **Material:** Kalkstein (Limestone)
- **Kosten:** 3 Stück
- **Mögliche Qualitäten:** Common, Uncommon, Rare
- **Fundort:** Oberwelt (Y: 0-64)

#### 🔥 Tier 2: Pyrit (Nether)
- **Material:** Pyrit
- **Kosten:** 3 Stück
- **Mögliche Qualitäten:** Uncommon, Rare, Epic, Legendary
- **Fundort:** Nether

#### ⭐ Tier 3: Galena (End)
- **Material:** Galena
- **Kosten:** 3 Stück
- **Mögliche Qualitäten:** Rare, Epic, Legendary, Mythic
- **Fundort:** End

### Wie reforge ich?

1. **Item in Hand halten**
2. `/sp reforge` eingeben
3. **Reforging-GUI öffnet sich:**
   - Item wird in der Mitte angezeigt
   - Wähle einen Tier aus (Kalkstein/Pyrit/Galena)
   - Klicke auf "Bestätigen"
4. **Material wird abgezogen**
5. **Item erhält neue Qualität!**

### Reforging-GUI Übersicht

```
╔═══════════════════════════════════════╗
║  [?]     [    ITEM    ]           [?]  ║
║                                         ║
║     [Kalkstein] [Pyrit] [Galena]       ║
║                                         ║
║      [Abbrechen] [✔] [Info]            ║
╚═══════════════════════════════════════╝
```

### Tipps & Tricks

- 💡 **Nutze Tier 3 für die beste Chance auf Mythic**
- 💡 **Spare Material** - Reforge nur Items die du wirklich brauchst
- 💡 **Material-Check** - GUI zeigt an ob du genug Material hast
- 💡 **Kein Risiko** - Item geht nie verloren, nur Qualität ändert sich

---

## 🔮 Custom Enchantments

### Übersicht

SurvivalPlus bietet **12 einzigartige Custom Enchantments**, die normale Vanilla-Enchantments ergänzen.

### Waffen-Enchantments

#### ❤️ Lifesteal (Lebensraub)
- **Max Level:** 3
- **Min. Qualität:** Rare
- **Effekt:** Heile 5%/10%/15% des verursachten Schadens
- **Anwendbar auf:** Schwerter, Äxte

#### 💥 Explosive (Explosiv)
- **Max Level:** 2
- **Min. Qualität:** Epic
- **Effekt:** 10%/20% Chance auf Explosion beim Treffer
- **Anwendbar auf:** Schwerter, Äxte

#### 👻 Soul Bound (Seelengebunden)
- **Max Level:** 1
- **Min. Qualität:** Legendary
- **Effekt:** Item bleibt beim Tod erhalten
- **Anwendbar auf:** Alle Waffen, Werkzeuge, Rüstung

#### ⚡ Thunder Strike (Blitzschlag)
- **Max Level:** 2
- **Min. Qualität:** Legendary
- **Effekt:** 15%/30% Chance Blitze auf Gegner zu beschwören
- **Anwendbar auf:** Schwerter

#### 🧛 Vampire (Vampirismus)
- **Max Level:** 3
- **Min. Qualität:** Epic
- **Effekt:** Absorbiere 3%/6%/9% Leben von Gegnern
- **Anwendbar auf:** Schwerter, Äxte

### Rüstungs-Enchantments

#### ✨ Divine Protection (Göttlicher Schutz)
- **Max Level:** 5
- **Min. Qualität:** Mythic
- **Effekt:** Reduziere allen Schaden um 4%/8%/12%/16%/20%
- **Anwendbar auf:** Alle Rüstungsteile

#### 🌵 Thorns+ (Dornen+)
- **Max Level:** 3
- **Min. Qualität:** Rare
- **Effekt:** Reflektiere 50%/100%/150% des Schadens zurück
- **Anwendbar auf:** Alle Rüstungsteile

#### 💨 Speed Boost (Geschwindigkeitsschub)
- **Max Level:** 2
- **Min. Qualität:** Uncommon
- **Effekt:** Erhöhe Bewegungsgeschwindigkeit um 10%/20%
- **Anwendbar auf:** Alle Rüstungsteile

### Werkzeug-Enchantments

#### 🔥 Auto Smelt (Auto-Schmelze)
- **Max Level:** 1
- **Min. Qualität:** Epic
- **Effekt:** Erze werden automatisch beim Abbauen geschmolzen
- **Anwendbar auf:** Spitzhacke
- **Funktioniert mit:** Iron, Gold, Copper, Ancient Debris

#### ⛏️ Vein Miner (Erzader-Abbau)
- **Max Level:** 1
- **Min. Qualität:** Legendary
- **Effekt:** Baue ganze Erzadern auf einmal ab (max 64 Blöcke)
- **Anwendbar auf:** Spitzhacke

#### 🪓 Timber (Holzfäller)
- **Max Level:** 1
- **Min. Qualität:** Rare
- **Effekt:** Fälle ganze Bäume auf einmal (max 128 Blöcke)
- **Anwendbar auf:** Axt

### Universal-Enchantments

#### 🛡️ Unbreakable (Unzerstörbar)
- **Max Level:** 1
- **Min. Qualität:** Mythic
- **Effekt:** Item verliert keine Haltbarkeit
- **Anwendbar auf:** Alle Waffen, Werkzeuge, Rüstung

### Wie bekomme ich Enchantments?

1. **Automatisch beim Drop:**
   - Rare: 30% für 1 Enchantment
   - Epic: 50% für 1 Enchantment
   - Legendary: 1-2 Enchantments garantiert
   - Mythic: 2-3 Enchantments garantiert

2. **Durch Reforging:**
   - Items können Enchantments erhalten/verlieren

3. **Via Command (Admin):**
   ```
   /sp enchant <enchantment> <level>
   ```

---

## 🛡️ Set-Boni System

### Was sind Set-Boni?

Wenn du **2 oder 4 Teile** des gleichen Armor Sets trägst, erhältst du **spezielle Boni**!

### Die 6 Armor Sets

#### 🛡️ Guardian Set (Uncommon+)
**Thema:** Verteidigung & Gesundheit

**2-Piece Bonus:**
- +2 Armor
- +4 Max Health

**4-Piece Bonus:**
- +4 Armor
- +10 Max Health
- Resistance I (permanent)

**Ideal für:** Tank-Spieler, Anfänger

---

#### ⚔️ Berserker Set (Rare+)
**Thema:** Angriffskraft & Geschwindigkeit

**2-Piece Bonus:**
- +2 Attack Damage
- +2% Movement Speed

**4-Piece Bonus:**
- +5 Attack Damage
- +5% Movement Speed
- Strength I (permanent)

**Ideal für:** Aggressive Spieler, PvP

---

#### 🗡️ Assassin Set (Epic+)
**Thema:** Kritische Treffer & Mobilität

**2-Piece Bonus:**
- +3% Movement Speed
- +5% Crit Chance

**4-Piece Bonus:**
- +8% Movement Speed
- +15% Crit Chance
- +50% Crit Damage
- Invisibility (permanent)

**Ideal für:** Stealth-Spieler, Crit-Builds

---

#### 🛡️🔒 Tank Set (Legendary+)
**Thema:** Maximale Verteidigung

**2-Piece Bonus:**
- +4 Armor
- +2 Armor Toughness
- +0.2 Knockback Resistance

**4-Piece Bonus:**
- +8 Armor
- +4 Armor Toughness
- +0.5 Knockback Resistance
- +20 Max Health
- Regeneration I (permanent)

**Ideal für:** Boss-Fights, Dungeon-Runs

---

#### ⚡🔥❄️ Elemental Set (Legendary+)
**Thema:** Elementar-Macht

**2-Piece Bonus:**
- +3 Attack Damage
- +25% Elemental Damage

**4-Piece Bonus:**
- +6 Attack Damage
- +50% Elemental Damage
- Fire Resistance (permanent)
- **Ability:** Elementar-Explosion

**Ideal für:** Magier-Builds, AoE-Damage

---

#### 👑✨ Godlike Set (Mythic only!)
**Thema:** Ultimative Macht

**2-Piece Bonus:**
- +5 Attack Damage
- +5 Armor
- +10 Max Health
- +5% Movement Speed

**4-Piece Bonus:**
- +10 Attack Damage
- +10 Armor
- +30 Max Health
- +10% Movement Speed
- +20% Crit Chance
- +100% Crit Damage
- Regeneration II (permanent)
- Strength II (permanent)
- **Ability:** Göttliche Macht

**Ideal für:** Endgame-Content, absolutes Maximum

---

### Wie erkenne ich Set-Boni?

1. **Item-Lore** zeigt Set-Zugehörigkeit:
   ```
   Set: Wächter-Set
   
   ✦ 2 Teile: Wächter I
     +2 Armor
     +4 Max Health
   
   ✦✦ 4 Teile: Wächter II
     +4 Armor
     +10 Max Health
     Resistance I
   ```

2. **Chat-Nachricht** beim Equippen:
   ```
   ✦ Set-Bonus aktiviert: Wächter I
   ✦✦ Voller Set-Bonus aktiviert: Wächter II
   ```

3. **Potion-Effekte** erscheinen in deiner Hotbar

### Set-Boni kombinieren?

**NEIN!** Du kannst immer nur die Boni von **einem** Set gleichzeitig haben.
- Das Set mit den **meisten Teilen** wird aktiviert
- Bei Gleichstand: Das Set mit höherer Qualität

### Kritische Treffer mit Sets

Sets wie **Assassin** und **Godlike** geben dir **Crit-Chance** und **Crit-Damage**:

- **Crit-Chance:** % Wahrscheinlichkeit für kritischen Treffer
- **Crit-Damage:** % Extra-Schaden bei Crit
- **Visual:** "⚡ KRITISCHER TREFFER! ⚡" in Action Bar
- **Effekte:** Partikel + Sound bei Crit

**Beispiel:**
- Assassin 4-Piece: 15% Crit Chance, +50% Crit Damage
- Normaler Schaden: 10 HP
- Mit Crit: 10 + (10 × 0.5) = **15 HP**

---

## 📚 Enchantment-Quellen

Es gibt viele natürliche Wege, um Custom Enchantments zu erhalten!

### 🎨 Enchanting Table
**Chance:** 5% - 50% (abhängig vom XP-Level)
- Level 30: 50% Chance + höhere Qualität
- Custom Enchantments werden **zusätzlich** zu Vanilla-Enchantments hinzugefügt

### 🎣 Fishing (Angeln)
**Chance:** 5% + 2% pro World Tier
- Funktioniert bei Büchern und Enchanted Books
- Höheres World Tier = bessere Qualität

### 📦 Loot Chests
**Chance:** 15% + 5% pro World Tier
- Dungeon, Stronghold, Mansion, Fortress, End City
- Custom Enchanted Books als zusätzlicher Loot

### 💀 Boss & Special Mobs
Die besten Enchantment-Quellen!

**Top-Bosse:**
- 🐉 **Enddrache:** 95% Chance (Legendary+)
- 💀 **Wither:** 90% Chance (Legendary+)
- 🌊 **Warden:** 85% Chance (Epic+)
- 🛡️ **Elder Guardian:** 50% Chance (Epic+)

**Starke Mobs:**
- ⚔️ **Evoker:** 30% (Rare+)
- 🐗 **Ravager:** 25% (Rare+)
- 🔥 **Piglin Brute:** 20% (Rare+)
- 🪓 **Vindicator:** 15% (Uncommon+)

**Special Mobs (Plugin):**
- 🔥 Mobs mit "special_mob" Tag: 40% (Epic+)

### 📚 Villager Trading
**Librarians** können Custom Enchanted Books anbieten!
- **Master (Level 5):** 40% Chance
- **Expert (Level 4):** 25% Chance
- **Journeyman (Level 3):** 15% Chance

### ⛏️ Mining (Seltene Erze)
Beim Abbauen bestimmter Erze!

- 💎 **Ancient Debris:** 5% Chance (Legendary+, 20% Mythic!)
- 💎 **Diamond Ore:** 2% Chance (Epic+)
- 💚 **Emerald Ore:** 3% Chance (Rare+)
- ⚱️ **Gold Ore:** 1% Chance (Rare+)

### 🛡️ Raid Victory
Nach erfolgreichem Verteidigen eines Raids!
- **Chance:** 60% + 8% pro World Tier
- **World Tier 5:** 100% garantiert!
- Alle Spieler im Radius von 200 Blöcken erhalten Belohnungen

### 🐷 Piglin Bartering
- **Chance:** 5% + 2% pro World Tier
- Book wird als zusätzliches Item hinzugefügt

### 🌊 Conduit Power
Sehr seltene Belohnung beim Aktivieren!
- **Chance:** 5% + 1% pro World Tier
- Bis zu **Mythic** bei World Tier 5

### 👁️ Aggressive Endermen
Endermen, die gerade teleportieren!
- **Chance:** 15% + 3% pro World Tier
- **Bedingung:** Enderman muss aggressiv/teleportierend sein

### 💡 Farming-Tipps

**Für Anfänger:**
1. Enchanting Table (Level 30)
2. Fishing
3. Loot Chests durchsuchen

**Für Fortgeschrittene:**
1. Elder Guardian & Evoker farmen
2. Mining mit Fortune
3. Master Librarians aufbauen

**Für Endgame:**
1. Raids verteidigen (fast garantiert!)
2. Ancient Debris farmen (Mythic Chance!)
3. Enddrache & Wither besiegen
4. Warden bekämpfen

---

## 🌍 World Tier System

Das World Tier System beeinflusst Schwierigkeit und Belohnungen!

### World Tiers

| Tier | Name | Farbe | Mob-Multiplikator |
|------|------|-------|-------------------|
| 1 | **Normal** | §7Grau | 1.0x HP, 1.0x DMG |
| 2 | **Heroic** | §aGrün | 1.5x HP, 1.2x DMG |
| 3 | **Elite** | §9Blau | 2.0x HP, 1.5x DMG |
| 4 | **Champion** | §5Lila | 2.5x HP, 2.0x DMG |
| 5 | **Mythic** | §cRot | 3.0x HP, 2.5x DMG |

### Shrines finden

Shrines generieren natürlich in der Welt!

**Features:**
- 🏛️ Spezielle Struktur mit Altar
- ⚡ Beacon-Laser (sichtbar aus der Ferne)
- 📍 Mindestabstand: 5000 Blöcke
- 🌍 In allen Dimensionen (Overworld, Nether, End)

**Shrine finden:**
```
/sp locate shrine
```
Zeigt die nächsten 3 Shrines in deiner Welt!

### World Tier wechseln

1. Finde einen **Shrine**
2. Rechtsklick auf den **Altar-Block**
3. Wähle dein gewünschtes **World Tier**
4. Bestätige die Auswahl

**Wichtig:** World Tier gilt für die gesamte Dimension!
- Survival, Survival_Nether, Survival_End teilen sich das gleiche Tier

### Belohnungen

Höheres World Tier = Bessere Belohnungen!

- 📚 **Enchantment-Drops:** +2-5% Chance pro Tier
- 💎 **Qualität:** Höhere Chance auf Epic/Legendary/Mythic
- ⚔️ **Special Mobs:** Mehr Spawns, bessere Drops
- 🎁 **World Events:** Bessere Belohnungen

---

## 🎮 Commands & Permissions

### Spieler-Commands

| Command | Beschreibung | Permission |
|---------|--------------|------------|
| `/sp help` | Zeigt alle Commands | `survivalplus.help` |
| `/sp info` | Info über Item in Hand | `survivalplus.info` |
| `/sp reforge` | Öffnet Reforging-GUI | `survivalplus.reforge` |
| `/sp stats` | Zeigt deine Stats | `survivalplus.stats` |

### Admin-Commands

| Command | Beschreibung | Permission |
|---------|--------------|------------|
| `/sp give <player> <material> <quality>` | Gibt Custom Item | `survivalplus.give` |
| `/sp reload` | Lädt Config neu | `survivalplus.reload` |
| `/sp debug` | Toggle Debug-Mode | `survivalplus.debug` |
| `/sp enchant <enchantment> <level>` | Fügt Enchantment hinzu | `survivalplus.enchant` |
| `/sp setbonus <set>` | Weist Set zu | `survivalplus.setbonus` |
| `/sp worldtier info` | Zeigt aktuelles World Tier | `survivalplus.worldtier` |
| `/sp worldtier set <tier>` | Setzt World Tier (Admin) | `survivalplus.worldtier.admin` |
| `/sp locate shrine` | Findet nächste Shrines | `survivalplus.locate` |

### Permissions-Übersicht

#### Basis-Permissions
```yaml
survivalplus.help: true      # Jeder
survivalplus.info: true      # Jeder
survivalplus.reforge: true   # Jeder
survivalplus.stats: true     # Jeder
```

#### Admin-Permissions
```yaml
survivalplus.admin: false    # Alle Admin-Commands
survivalplus.give: false     # Items geben
survivalplus.reload: false   # Config reload
survivalplus.debug: false    # Debug-Mode
survivalplus.enchant: false  # Enchantments verwalten
survivalplus.setbonus: false # Sets zuweisen
```

#### Wildcard-Permissions
```yaml
survivalplus.*             # Alle Permissions
survivalplus.admin.*       # Alle Admin-Permissions
survivalplus.player.*      # Alle Spieler-Permissions
```

---

## ❓ FAQ

### Allgemeine Fragen

**Q: Wie selten ist ein Mythic Item?**
A: Mythic Items haben eine Drop-Chance von **0.1%** (1 in 1000). Sie sind extrem selten!

**Q: Kann ich ein Common Item zu Mythic reforgen?**
A: Theoretisch ja, aber es ist sehr unwahrscheinlich. Nutze Tier 3 (Galena) für die beste Chance.

**Q: Verliere ich mein Item beim Reforging?**
A: **NEIN!** Nur die Qualität ändert sich. Das Item selbst bleibt erhalten.

**Q: Können Enchantments verloren gehen?**
A: Ja, beim Reforging kann sich die Enchantment-Anzahl ändern.

**Q: Funktioniert Soul Bound im PvP?**
A: Ja, Items mit Soul Bound bleiben auch beim Tod im PvP erhalten.

### Reforging-Fragen

**Q: Wo finde ich Kalkstein/Pyrit/Galena?**
A: Siehe [Reforging-System](#die-3-reforging-tiers) - Fundorte sind gelistet.

**Q: Kann ich das Material zurückbekommen?**
A: Nein, Material wird beim Reforging verbraucht.

**Q: Was ist die beste Reforging-Strategie?**
A: Nutze Tier 3 (Galena) für Legendary/Mythic. Spare Material für wichtige Items.

### Set-Boni-Fragen

**Q: Kann ich 2 Sets gleichzeitig tragen?**
A: Nein, nur das Set mit den meisten Teilen ist aktiv.

**Q: Muss ich ein komplettes Set tragen?**
A: Nein, 2 Teile für Minor Bonus, 4 Teile für Major Bonus.

**Q: Welches Set ist am besten?**
A: Kommt auf deinen Spielstil an:
- Tank? → Tank Set
- Damage? → Berserker/Godlike
- Crit-Build? → Assassin
- Allrounder? → Guardian

### Enchantment-Fragen

**Q: Stacken Enchantments mit Vanilla?**
A: Ja! Custom Enchantments sind zusätzlich zu Vanilla.

**Q: Was macht Vein Miner genau?**
A: Baut alle verbundenen Erze (gleicher Typ) bis max 64 Blöcke ab.

**Q: Ist Timber zu OP?**
A: Es hat ein Limit von 128 Blöcken und verbraucht Haltbarkeit.

---

## 💻 Installation

### Voraussetzungen

- **Server:** Paper/Spigot 1.21+
- **Java:** 21+
- **RAM:** Mindestens 2GB für Server
- **Plugins:** Keine Abhängigkeiten (standalone)

### Installations-Schritte

1. **Plugin herunterladen:**
   - Lade `SurvivalPlus-1.2.0-SNAPSHOT-all.jar` herunter

2. **In plugins/ Ordner kopieren:**
   ```
   server/
   └── plugins/
       └── SurvivalPlus-1.2.0-SNAPSHOT-all.jar
   ```

3. **Server starten:**
   ```bash
   java -jar paper-1.21.jar
   ```

4. **Dateien werden erstellt:**
   ```
   plugins/SurvivalPlus/
   ├── config.yml
   └── data/ (erstellt beim ersten Start)
   ```

5. **Config anpassen** (optional)

6. **Server neustarten** oder `/sp reload`

### Erste Server-Starts

Beim ersten Start siehst du:
```
[SurvivalPlus] Enabling SurvivalPlus v1.2.0-SNAPSHOT
[SurvivalPlus]   ____                  _            _ ____  _           
[SurvivalPlus]  / ___| _   _ _ ____   _(_)_   ____ _| |  _ \| |_   _ ___ 
[SurvivalPlus]  \___ \| | | | '__\ \ / / \ \ / / _` | | |_) | | | | / __|
[SurvivalPlus]   ___) | |_| | |   \ V /| |\ V / (_| | |  __/| | |_| \__ \
[SurvivalPlus]  |____/ \__,_|_|    \_/ |_| \_/ \__,_|_|_|   |_|\__,_|___/
[SurvivalPlus] 
[SurvivalPlus] SurvivalPlus v1.2.0-SNAPSHOT wurde erfolgreich geladen!
```

---

## ⚙️ Konfiguration

### config.yml Übersicht

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

# Drop-Chancen (in %)
drop-chances:
  common: 50.0
  uncommon: 30.0
  rare: 15.0
  epic: 4.0
  legendary: 0.9
  mythic: 0.1

# Reforging-System
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

# Stat-Multiplikatoren
stats:
  common: 1.0
  uncommon: 1.1
  rare: 1.25
  epic: 1.5
  legendary: 2.0
  mythic: 3.0

# Sound Einstellungen
sound:
  volume: 1.0

# Particle Einstellungen
particles:
  density: 1.0

# Quality Plates
quality-plates:
  min-tier: 1
  show-for-all: true
  distance: 10

# GUI Einstellungen
gui:
  reforging-title: "Reforging Station"
  confirm-on-click: false
```

### Feature-Toggles

Schalte Features individuell an/aus:

```yaml
features:
  custom-item-drops: true    # Custom Items als Drops
  reforging-system: true     # Reforging-System
  quality-plates: true       # Hologramme über Items
  stat-bonuses: true         # Stat-Boni
  particle-effects: true     # Partikel-Effekte
  sound-effects: true        # Sound-Effekte
  mythic-aura: true          # Aura um Mythic-Spieler
  title-messages: true       # Title/Subtitle Messages
  item-glow: true            # Item-Glow für Epic+
```

### Drop-Chancen anpassen

```yaml
drop-chances:
  common: 50.0      # 50%
  uncommon: 30.0    # 30%
  rare: 15.0        # 15%
  epic: 4.0         # 4%
  legendary: 0.9    # 0.9%
  mythic: 0.1       # 0.1%
# Gesamt muss 100% ergeben!
```

### Reforging-Materialien ändern

```yaml
reforging:
  tier1:
    material: COBBLESTONE  # Anderes Material
    cost: 5                # Andere Kosten
  tier2:
    material: IRON_INGOT
    cost: 3
  tier3:
    material: DIAMOND
    cost: 1
```

### Performance-Optimierung

```yaml
# Reduziere Partikel-Dichte
particles:
  density: 0.5  # 50% weniger Partikel

# Deaktiviere Hologramme
features:
  quality-plates: false

# Erhöhe Min-Tier für Plates
quality-plates:
  min-tier: 3  # Nur Rare+ zeigen
```

---

## 🔧 Performance-Tipps

### Für Server-Admins

1. **Partikel-Dichte reduzieren:**
   ```yaml
   particles:
     density: 0.5
   ```

2. **Quality Plates limitieren:**
   ```yaml
   quality-plates:
     min-tier: 3  # Nur Rare+
     distance: 5  # Kleinere Sichtweite
   ```

3. **Features deaktivieren:**
   ```yaml
   features:
     mythic-aura: false  # Kein Performance-Impact
   ```

4. **Async-Operationen nutzen:**
   - Plugin nutzt bereits Async wo möglich

5. **Regelmäßige Backups:**
   ```bash
   # Backup-Script
   cp -r plugins/SurvivalPlus/data/ backups/survivalplus-$(date +%Y%m%d)/
   ```

### Optimale Einstellungen

**Kleine Server (1-20 Spieler):**
```yaml
particles:
  density: 1.0
quality-plates:
  min-tier: 1
features:
  # Alle an
```

**Mittlere Server (20-100 Spieler):**
```yaml
particles:
  density: 0.7
quality-plates:
  min-tier: 2
  distance: 8
```

**Große Server (100+ Spieler):**
```yaml
particles:
  density: 0.5
quality-plates:
  min-tier: 3
  distance: 5
features:
  mythic-aura: false
```

---

## 👨‍💻 API-Nutzung

### Für Entwickler

SurvivalPlus bietet eine umfangreiche API für eigene Plugins:

### Dependency hinzufügen

**Maven:**
```xml
<dependency>
    <groupId>org.bysenom</groupId>
    <artifactId>survivalplus</artifactId>
    <version>1.2.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

**Gradle:**
```gradle
dependencies {
    compileOnly 'org.bysenom:survivalplus:1.2.0-SNAPSHOT'
}
```

### API-Zugriff

```kotlin
val survivalPlus = Bukkit.getPluginManager()
    .getPlugin("SurvivalPlus") as SurvivalPlus

// Item-Manager
val item = survivalPlus.itemManager.createItem(Material.DIAMOND_SWORD, Quality.LEGENDARY)

// Reforging-Manager
survivalPlus.reforgingManager.reforgeItem(player, item, materialStack)

// Enchantment-Manager
survivalPlus.enchantmentManager.addEnchantment(item, CustomEnchantment.LIFESTEAL, 3)

// Set-Bonus-Manager
survivalPlus.setBonusManager.assignSet(helmet, ArmorSet.GODLIKE)
```

### Custom Events

```kotlin
// Event: Item mit Qualität erhalten
@EventHandler
fun onQualityItemReceive(event: QualityItemReceiveEvent) {
    val player = event.player
    val item = event.item
    val quality = event.quality
    
    if (quality == Quality.MYTHIC) {
        // Custom Logik
    }
}

// Event: Reforging erfolgreich
@EventHandler
fun onReforgingSuccess(event: ReforgingSuccessEvent) {
    val player = event.player
    val oldQuality = event.oldQuality
    val newQuality = event.newQuality
    
    // Custom Logik
}
```

### Eigene Enchantments

```kotlin
// Registriere Custom Enchantment
enum class MyCustomEnchantment : CustomEnchantment {
    POISON_STRIKE(
        "Gift-Schlag",
        "Vergiftet Gegner",
        3,
        Quality.EPIC,
        NamedTextColor.GREEN,
        listOf(ItemType.SWORD)
    )
}
```

---

## 🎯 Zusammenfassung

### Was du gelernt hast

- ✅ **Qualitätssystem** mit 6 Stufen
- ✅ **Reforging** mit 3 Tiers
- ✅ **12 Custom Enchantments**
- ✅ **6 Armor Sets** mit Boni
- ✅ **Commands & Permissions**
- ✅ **Konfiguration**

### Nächste Schritte

1. **Spielen!** - Probiere das System aus
2. **Experimentieren** - Teste verschiedene Builds
3. **Community** - Teile deine Erfahrungen
4. **Feedback** - Hilf uns das Plugin zu verbessern

---

## 📞 Support & Links

**Discord:** [Noch nicht verfügbar]  
**GitHub:** [Repository-Link]  
**Issues:** [GitHub Issues]  
**Wiki:** [Dieses Dokument]

---

**Viel Spaß mit SurvivalPlus!** 🎮✨

*Letzte Aktualisierung: 2025-11-16*

