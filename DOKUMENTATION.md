# SurvivalPlus - Projekt-Dokumentation

## Projektstruktur

```
SurvivalPlus/
│
├── src/main/
│   ├── kotlin/org/bysenom/survivalPlus/
│   │   ├── SurvivalPlus.kt                    # Haupt-Plugin-Klasse
│   │   │
│   │   ├── models/                             # Datenmodelle
│   │   │   ├── Quality.kt                      # Qualitätsstufen-Enum
│   │   │   ├── ReforgingTier.kt               # Reforging-System Tiers
│   │   │   └── CustomItem.kt                  # Custom Item Datenklasse
│   │   │
│   │   ├── managers/                           # Business Logic Manager
│   │   │   ├── ItemManager.kt                 # Item-Verwaltung
│   │   │   └── ReforgingManager.kt            # Reforging-Logik
│   │   │
│   │   ├── commands/                           # Command Handler
│   │   │   └── SurvivalPlusCommand.kt         # Hauptbefehl
│   │   │
│   │   └── listeners/                          # Event Listener
│   │       └── ItemListener.kt                # Item-Events
│   │
│   └── resources/
│       ├── plugin.yml                          # Plugin-Konfiguration
│       └── config.yml                          # Einstellungen
│
├── build.gradle.kts                            # Build-Konfiguration
├── settings.gradle.kts
└── README.md                                   # Projekt-Übersicht
```

## Architektur-Übersicht

### 1. Models Layer (Datenmodelle)
**Verantwortlich für:** Datenstrukturen und Enums

- **Quality.kt**
  - 6 Qualitätsstufen (COMMON bis MYTHIC)
  - Farben, Gewichtung, Multiplikatoren
  - Zufallsgenerierung basierend auf Gewichtung

- **ReforgingTier.kt**
  - 3 Reforging-Tiers (Kalkstein, Pyrit, Galena)
  - Material-Zuordnung
  - Erlaubte Qualitäten pro Tier

- **CustomItem.kt**
  - Datenklasse für Custom Items
  - Stats-System (Schaden, Rüstung, etc.)
  - ItemStack-Konvertierung

### 2. Managers Layer (Business Logic)
**Verantwortlich für:** Kernlogik und Operationen

- **ItemManager.kt**
  - Erstellt Custom Items
  - Verwaltet Qualitäten
  - Generiert Stats basierend auf Item-Typ
  - PersistentDataContainer für Speicherung

- **ReforgingManager.kt**
  - Reforging-Operationen
  - Material-Validierung
  - Kosten-Berechnung
  - Ergebnis-Handling (sealed class)

### 3. Commands Layer (User Interface)
**Verantwortlich für:** Spieler-Interaktion

- **SurvivalPlusCommand.kt**
  - `/sp give` - Items geben
  - `/sp reforge` - Reforging starten
  - `/sp info` - Item-Infos anzeigen
  - `/sp reload` - Config neu laden
  - Tab-Completion für alle Befehle

### 4. Listeners Layer (Event Handling)
**Verantwortlich für:** Minecraft-Events

- **ItemListener.kt**
  - BlockBreakEvent: Custom Item Drops
  - EntityDamageEvent: Schaden-Multiplikator
  - Action-Bar Feedback

## Kernfeatures im Detail

### Qualitätssystem

| Qualität | Farbe | Multiplikator | Gewichtung |
|----------|-------|---------------|------------|
| Common | Weiß | 1.0x | 40% |
| Uncommon | Grün | 1.2x | 30% |
| Rare | Blau | 1.5x | 15% |
| Epic | Lila | 2.0x | 10% |
| Legendary | Gold | 2.5x | 4% |
| Mythic | Rot | 3.0x | 1% |

**Eigenschaften:**
- Jede Qualität hat eine eigene Farbe
- Stats werden mit dem Multiplikator verstärkt
- Gewichtetes Zufallssystem für Drops
- Tier-System von 1-6

### Reforging-System

#### Tier 1: Kalkstein (Oberwelt)
```
Material: Calcite
Kosten: 3 Stück
Dimension: Oberwelt
Qualitäten: Common, Uncommon, Rare
```

#### Tier 2: Pyrit (Nether)
```
Material: Raw Gold
Kosten: 5 Stück
Dimension: Nether
Qualitäten: Uncommon, Rare, Epic, Legendary
```

#### Tier 3: Galena (End)
```
Material: Raw Iron
Kosten: 7 Stück
Dimension: End
Qualitäten: Rare, Epic, Legendary, Mythic
```

**Mechanik:**
1. Spieler hält Item in der Hand
2. Verwendet Reforging-Material
3. Zufällige Qualität aus erlaubtem Pool
4. Material wird verbraucht
5. Item erhält neue Qualität mit angepassten Stats

### Stats-System

**Waffen:**
- Schaden
- Angriffsgeschwindigkeit

**Werkzeuge:**
- Abbaugeschwindigkeit
- Effizienz

**Rüstung:**
- Rüstungswert
- Härte (Toughness)

Alle Stats werden mit dem Qualitäts-Multiplikator multipliziert!

## Technische Details

### Persistente Datenspeicherung
Items speichern ihre Qualität im `PersistentDataContainer`:
```kotlin
NamespacedKey(plugin, "quality")
```

### Event-Driven Architecture
- Listener reagieren auf Minecraft-Events
- Manager verarbeiten die Business Logic
- Commands bieten die User Interface

### Sealed Classes für Ergebnisse
```kotlin
sealed class ReforgingResult {
    data class Success(val newQuality: Quality)
    data class Insufficient(val required: Int)
    object NotCustomItem
    object InvalidMaterial
}
```

Typsichere Ergebnisbehandlung mit Kotlin!

## Konfiguration

### config.yml Struktur
```yaml
drop-chances:         # Anpassbare Drop-Raten
reforging-costs:      # Kosten pro Tier
quality-multipliers:  # Stat-Multiplikatoren
features:             # Feature-Toggles
messages:             # Nachrichten-Templat es
endgame:              # Endgame-Optionen
```

## Permissions-System

```
survivalplus.*                    # Alle Rechte
  ├── survivalplus.give           # Items geben (OP only)
  ├── survivalplus.reforge        # Reforging nutzen (alle)
  ├── survivalplus.info           # Item-Info (alle)
  └── survivalplus.reload         # Config reload (OP only)
```

## Entwicklungs-Roadmap

### Phase 1: Grundsystem ✅
- [x] Qualitätsstufen
- [x] Custom Items
- [x] Reforging-System
- [x] Commands & Permissions
- [x] Config-System

### Phase 2: GUI & Visuals
- [ ] Reforging-GUI (Inventory)
- [ ] Quality Plates (über Items)
- [ ] Particle-Effekte
- [ ] Sound-Effekte

### Phase 3: Erweiterte Features
- [ ] Custom Enchantments
- [ ] Set-Boni (Armor Sets)
- [ ] Skill-System
- [ ] Achievement-System

### Phase 4: Endgame Content
- [ ] Custom Dungeon-System
- [ ] Boss-Fights
- [ ] Mythic-only Features
- [ ] Prestige-System

### Phase 5: Welterweiterung
- [ ] Custom Ore Generation
- [ ] Custom Dimensions
- [ ] Trading-System
- [ ] Economy-Integration

## Best Practices

### Code-Stil
- Kotlin Coding Conventions
- Lateinit für Plugin-Dependencies
- Sealed Classes für Ergebnisse
- Extension Functions wo sinnvoll

### Performance
- Caching von häufig genutzten Daten
- Async Operations für I/O
- Efficient Event Handling

### Wartbarkeit
- Klare Trennung der Layers
- Single Responsibility Principle
- Dokumentierte Public APIs

## Testing

### Manuelle Tests
```
1. /sp give <player> DIAMOND_SWORD legendary
2. Rechtsklick mit Item
3. /sp info
4. /sp reforge (mit Material)
```

### Geplante Unit Tests
- Quality.random() Gewichtung
- ReforgingTier Material-Mapping
- ItemManager Stats-Generierung

## Support & Contribution

### Issues melden
1. Problem beschreiben
2. Schritte zur Reproduktion
3. Erwartetes vs. tatsächliches Verhalten
4. Server-Version & Plugin-Version

### Feature Requests
Ideen für neue Features sind willkommen!

## Lizenz

Free to use for private and commercial purposes.

---

**Version:** 1.0-SNAPSHOT  
**Author:** SashaW  
**Inspiriert von:** Tierify (Minecraft Mod)

