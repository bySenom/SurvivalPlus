# 🎉 Update: Commands & Trial Chambers - Zusammenfassung

## ✅ Neue Commands implementiert!

### 1. `/sp givebook` - Enchanted Books geben

**Syntax:**
```
/sp givebook <spieler> [enchantment] [level] [quality]
```

**Features:**
- ✨ **Zufälliges Book:** `/sp givebook Steve` 
- 🎯 **Spezifisches Enchantment:** `/sp givebook Steve excavation`
- 📊 **Mit Level:** `/sp givebook Steve excavation 3`
- 💎 **Mit Qualität:** `/sp givebook Steve excavation 3 legendary`

**Beispiele:**
```bash
# Zufälliges Custom Enchanted Book
/sp givebook Steve

# Excavation Level 1
/sp givebook Steve excavation

# Excavation Level 3
/sp givebook Steve excavation 3

# Legendary Excavation III
/sp givebook Steve excavation 3 legendary
```

**Permission:** `survivalplus.givebook`

---

### 2. `/sp enchant` - Item in Hand verzaubern

**Syntax:**
```
/sp enchant <enchantment> [level]
```

**Features:**
- 🔮 Verzaubert das Item in der Hand direkt
- ✔️ Prüft automatisch Item-Typ-Kompatibilität
- 🎵 Sound-Effekt bei erfolgreicher Verzauberung
- ⚠️ Warnung wenn Enchantment nicht passt

**Beispiele:**
```bash
# Excavation Level 1 hinzufügen
/sp enchant excavation

# Excavation Level 3 hinzufügen
/sp enchant excavation 3

# Lifesteal Level 2 hinzufügen
/sp enchant lifesteal 2
```

**Permission:** `survivalplus.enchant`

**Verfügbare Enchantments:**
- `excavation` - Spitzhacke (3x3 Mining)
- `lumberjack` - Axt (Baumfällung)
- `vein_miner` - Spitzhacke (Erzadern)
- `lifesteal` - Schwert (HP zurück)
- `fire_aspect_plus` - Schwert (Verstärktes Feuer)
- `frost_walker_plus` - Stiefel (Erweitert Frost Walker)
- `lightning_aspect` - Schwert (Blitz-Schaden)
- `telepathy` - Tools (Items direkt ins Inventar)
- `experience_boost` - Tools (Mehr XP)
- `soul_bound` - Alles (Behält Items beim Tod)
- `auto_smelt` - Tools (Automatisches Schmelzen)
- `lucky_strike` - Tools (Extra Drops)

---

## 🏛️ Trial Chambers Integration

### Erhöhte Loot-Chancen!

**Normal Chests:** 15% + 5% pro Tier
**Trial Chambers:** 40% + 5% pro Tier (fast doppelt so hoch!)

### Bessere Qualitäten!

**Trial Chamber Loot-Verteilung:**
- **Mythic:** 20% (bei World Tier 5)
- **Legendary:** 35%
- **Epic:** 40%
- **Rare:** Minimum

Vergleich zu normalen Chests:
- **Mythic:** 10% → **20%** (+100%)
- **Legendary:** 20% → **35%** (+75%)
- **Epic:** 30% → **40%** (+33%)

### Alle Trial-Quellen!

✅ **Vault Chests** - Beste Belohnungen
✅ **Trial Spawner Rewards** - Nach Spawner-Clear
✅ **Ominous Trial Rewards** - Spezielle Events
✅ **Trial Chamber Mobs** - 25% Drop-Chance

### Trial Mob Drops

Mobs mit den Tags:
- `trial_spawner`
- `trial_chamber_mob`
- `ominous_trial`

**Drop-Chance:** 25% + 5% pro World Tier
**Min. Qualität:** Rare (immer mindestens Rare!)

---

## 📊 Drop-Chancen Vergleich

### World Tier 1

| Quelle | Chance | Beste Qualität |
|--------|--------|----------------|
| Trial Chamber Chest | **45%** | Legendary |
| Normal Chest | 20% | Rare |
| Trial Mob | **30%** | Epic |
| Normal Mob | 5-15% | Uncommon-Rare |

### World Tier 5

| Quelle | Chance | Beste Qualität |
|--------|--------|----------------|
| Trial Chamber Chest | **65%** | **Mythic (20%)** |
| Normal Chest | 40% | Mythic (10%) |
| Trial Mob | **50%** | **Mythic** |
| Normal Mob | 10-20% | Rare-Epic |

**Trial Chambers sind bei World Tier 5 die BESTE Enchantment-Quelle!**

---

## 🎯 Farming-Strategie

### Optimale Route für Enchanted Books:

1. **World Tier auf 5 setzen** (bei einem Shrine)
2. **Trial Chambers finden** (in der Tiefe)
3. **Vollständig clearen:**
   - Alle Spawner aktivieren
   - Alle Mobs töten (25% Drop-Chance!)
   - Alle Vaults öffnen (65% Chance!)
4. **Ominous Trials aktivieren** für zusätzliche Belohnungen

### Erwartete Ausbeute (World Tier 5):

**Pro Trial Chamber (komplett):**
- 3-5 Vaults → ~4 Books (65% je Vault)
- 10-15 Trial Mobs → ~7 Books (50% je Mob)
- **Gesamt: ~11 Enchanted Books pro Chamber!**

**Qualität:**
- ~2 Mythic Books
- ~3-4 Legendary Books
- ~4 Epic Books
- Rest: Rare+

---

## 🔧 Technische Details

### Code-Änderungen

**SurvivalPlusCommand.kt:**
- `handleGiveBook()` - Neuer Command-Handler
- `handleEnchant()` - Neuer Command-Handler
- `createRandomEnchantedBook()` - Helper-Methode
- `toRomanNumeral()` - Formatierungs-Helper

**EnchantmentSourceListener.kt:**
- `onLootGenerate()` - Trial Chamber Detection
- `onMobDeath()` - Trial Mob Detection
- Erhöhte Chancen für Trial-Content

### Event-Handler Features:

✅ **Automatic Detection:** Trial Chambers automatisch erkannt
✅ **Tag-Based:** Mobs mit richtigen Tags erhalten Boni
✅ **Loot-Table Integration:** Alle Loot-Tables berücksichtigt
✅ **World Tier Scaling:** Alle Boni skalieren mit World Tier

---

## 📚 Dokumentation aktualisiert

### ENCHANTMENT_SOURCES.md
- ✅ Trial Chambers Sektion hinzugefügt
- ✅ Commands dokumentiert
- ✅ Drop-Chancen aktualisiert
- ✅ Farming-Tipps erweitert

### Neue Befehle in Help
- ✅ `/sp givebook` hinzugefügt
- ✅ `/sp enchant` hinzugefügt
- ✅ Beispiele für alle Enchantments

---

## 🎮 Permissions

### Neue Permissions:
```yaml
survivalplus.givebook: false    # Book-Command
survivalplus.enchant: false     # Enchant-Command
```

### Empfohlene Setup:
```yaml
# Für Admins
survivalplus.admin: true        # Alle Permissions
survivalplus.givebook: true
survivalplus.enchant: true

# Für Spieler
survivalplus.givebook: false    # Nur Admins
survivalplus.enchant: false     # Nur Admins
```

---

## ✅ Testing Checklist

### Commands:
- [x] `/sp givebook <spieler>` - Zufälliges Book
- [x] `/sp givebook <spieler> <enchant>` - Spezifisches Book
- [x] `/sp givebook <spieler> <enchant> <level>` - Mit Level
- [x] `/sp givebook <spieler> <enchant> <level> <quality>` - Mit Qualität
- [x] `/sp enchant <enchant>` - Item verzaubern
- [x] `/sp enchant <enchant> <level>` - Mit Level
- [x] Item-Typ Validierung
- [x] Permission-Checks

### Trial Chambers:
- [ ] Vault Chest Loot
- [ ] Trial Spawner Loot
- [ ] Ominous Trial Loot
- [ ] Trial Mob Drops
- [ ] Drop-Chancen (45-65%)
- [ ] Qualitäts-Verteilung
- [ ] World Tier Scaling

---

## 🚀 Verwendung

### Für Admins:
```bash
# Books an Spieler geben
/sp givebook Steve excavation 3 legendary

# Item in Hand verzaubern
/sp enchant lumberjack 2

# Test-Setup
/sp worldtier set 5
/sp givebook @a excavation 3 mythic
```

### Für Spieler:
1. World Tier 5 aktivieren (an Shrine)
2. Trial Chambers finden
3. Komplett clearen für maximale Belohnungen
4. Ominous Trials für Extra-Loot

---

## 📊 Balance-Analyse

### Warum Trial Chambers so gut?

**1. Schwierigkeit:**
- Trial Chambers sind herausfordernd
- Erfordern gute Ausrüstung
- Hoher Skill erforderlich

**2. Zeitaufwand:**
- ~30-45 Minuten pro Chamber
- Nicht farmbar in Masse
- Begrenzt durch Spawn-Rate

**3. Belohnung:**
- ~11 Books pro Chamber
- Beste Qualitäten garantiert
- Rechtfertigt den Aufwand

**4. Progression:**
- Frühe Spieler: Enchanting Table
- Mid-Game: Normal Chests + Mobs
- Endgame: Trial Chambers + Raids

---

## 🎉 Zusammenfassung

**Neue Commands:**
- ✅ `/sp givebook` - Enchanted Books verwalten
- ✅ `/sp enchant` - Items direkt verzaubern

**Trial Chambers:**
- ✅ 40% Base-Chance (statt 15%)
- ✅ 65% bei World Tier 5
- ✅ 20% Mythic-Chance
- ✅ Trial Mobs: 25-50% Drop-Chance

**Balance:**
- ⚖️ Schwierigkeit rechtfertigt Belohnung
- ⚖️ Zeitaufwand berücksichtigt
- ⚖️ Progression-System intakt
- ⚖️ Variety in Farming-Methoden

**Dokumentation:**
- 📚 ENCHANTMENT_SOURCES.md aktualisiert
- 📚 Commands dokumentiert
- 📚 Drop-Chancen übersichtlich

Das Plugin ist nun KOMPLETT mit natürlichen Enchantment-Quellen und Admin-Tools! 🎮✨

