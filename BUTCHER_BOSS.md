# 🔪 The Butcher Boss - Dokumentation

## ⚔️ Übersicht

**The Butcher** ist ein Custom Boss inspiriert von Diablo, der ab World Tier Heroic (Tier 2+) spawnen kann.

### Boss-Features:
- 💀 Extrem hohes HP und Schaden
- 🔥 Spezielle Fähigkeiten (Bleed, Charge, Cleave)
- 📊 Boss-Bar mit HP-Anzeige
- 💎 Garantierte Legendary+ Drops
- 🎵 Sound-Effekte und Partikel
- 📢 "FRESH MEAT!" Spawn-Nachricht

---

## 📋 Boss-Stats

### Base Stats (Tier 2):
| Stat | Wert |
|------|------|
| **HP** | 500 ❤️ |
| **Schaden** | 15 💥 |
| **Geschwindigkeit** | 0.35 (schnell!) |
| **Knockback Resist** | 90% |
| **Rüstung** | 10 🛡️ |
| **Armor Toughness** | 8 |

### Skalierung:
**+50% HP und Schaden pro World Tier!**

| World Tier | HP | Schaden |
|------------|-----|---------|
| Tier 2 (Heroic) | 500 | 15 |
| Tier 3 (Elite) | 750 | 22.5 |
| Tier 4 (Champion) | 1000 | 30 |
| Tier 5 (Mythic) | 1250 | 37.5 |

---

## 🎮 Spawn-Mechanik

### Natürliches Spawning:
- **Chance:** 0.1% (1 in 1000) bei jedem natürlichen Zombie-Spawn
- **Min. World Tier:** Heroic (Tier 2)
- **Welten:** Survival, Survival_Nether, Survival_End
- **Spawn-Nachricht:** "FRESH MEAT!" an alle Spieler in der Welt

### Admin-Command:
```bash
/sp butcher spawn [tier]
```

**Beispiele:**
```bash
# Spawnt mit aktuellem World Tier
/sp butcher spawn

# Spawnt mit spezifischem Tier
/sp butcher spawn 2  # Heroic
/sp butcher spawn 5  # Mythic
```

**Permission:** `survivalplus.butcher`

---

## ⚔️ Boss-Fähigkeiten

### 1. 💉 Bleed (Bleeding Effect)
- **Trigger:** 30% Chance bei jedem Angriff
- **Effekt:** Wither II für 5 Sekunden
- **Nachricht:** "§4💉 You are bleeding!"

### 2. 🏃 Charge Attack
- **Cooldown:** 10 Sekunden
- **Range:** 5-20 Blöcke
- **Effekt:** Stürmt auf den Spieler zu
- **Knockback:** Ja
- **Nachricht:** "§c⚔ The Butcher charges at you!"

### 3. 💥 Cleave (AoE Attack)
- **Range:** 5 Blöcke
- **Schaden:** 70% des normalen Schadens
- **Effekt:** Trifft alle Spieler in Range
- **Bleed-Chance:** Ja (30%)
- **Knockback:** Nach oben

### 4. 😈 Rage Mode
- **Trigger:** HP < 30%
- **Effekte:**
  - Speed II (permanent)
  - Strength III (permanent)
  - Boss-Bar wird LILA
- **Nachricht:** "§c§l⚔ THE BUTCHER ENTERS RAGE MODE! ⚔"

---

## 🎁 Drops & Belohnungen

### Garantierte Drops:

#### 📚 Enchanted Books
**Anzahl:** 2 + World Tier (2-7 Books!)
- **Tier 5:** 40% Mythic, 60% Legendary
- **Tier 4:** 60% Legendary, 40% Epic
- **Tier 2-3:** 100% Legendary

#### ⚔️ Butcher's Cleaver (15% Chance)
Spezielle Netherite-Axt mit:
- **Lifesteal II** (Custom Enchantment)
- **Sharpness VII**
- **Looting III**
- **Unbreaking V**
- **Cleave Effect**

**Lore:**
> A blood-soaked weapon  
> from The Butcher's arsenal

#### 💎 Custom Items
**Anzahl:** 1 + (World Tier - 1) (1-5 Items)
- Netherite Waffen & Rüstung
- **Qualität:**
  - Tier 5: 30% Mythic, 50% Legendary, 20% Epic
  - Tier 4: 50% Legendary, 50% Epic
  - Tier 3: 100% Epic
  - Tier 2: 100% Rare

#### 💰 Resources
- **Emeralds:** 16 + (Tier × 8) = 16-56
- **Diamonds:** 8 + (Tier × 4) = 8-28

#### ✨ XP
- **Base:** 500 XP
- **Bonus:** +200 XP pro World Tier
- **Total:** 500-1500 XP

---

## 🎭 Visual Effects

### Spawn-Effekte:
- ⚡ Lightning Strike (visual)
- 💥 Explosion Partikel
- 🩸 Rote Dust Partikel
- 🔊 Dragon Growl + Zombie Convert Sound
- 📢 Title: "FRESH MEAT!"

### Im Kampf:
- 🩸 Kontinuierliche Blut-Partikel
- 💨 Cloud-Partikel beim Charge
- ⚔️ Sweep-Attack Partikel beim Cleave
- 😡 Angry Villager bei Rage Mode

### Tod-Effekte:
- ⚡ Lightning Strike
- 💥 Massive Explosion
- 🩸 Viele Blut-Partikel
- 🔊 Ender Dragon Death Sound
- 🏆 Toast: Challenge Complete Sound

---

## 📊 Boss-Bar

### Features:
- **Name:** "§c§l⚔ THE BUTCHER ⚔"
- **Farbe:** ROT (normal), LILA (Rage Mode)
- **Style:** Segmentiert (10 Segmente)
- **Range:** 50 Blöcke
- **Update:** Jede 0.5 Sekunden

### Anzeige:
```
§c§l⚔ THE BUTCHER ⚔ §7[85%]
████████████████░░░░ (Rot)
```

Bei < 30% HP:
```
§c§l⚔ THE BUTCHER ⚔ §7[25%] RAGE
█████░░░░░░░░░░░░░░░ (Lila)
```

---

## 💡 Kampf-Tipps

### Für Anfänger (Tier 2):
1. **Halte Distanz** - Vermeide Cleave
2. **Dodge Charges** - Seitlich ausweichen
3. **Heile sofort** - Bleed kann tödlich sein
4. **Rüstung:** Mindestens Diamond mit Protection

### Für Fortgeschrittene (Tier 3-4):
1. **Shield verwenden** - Blockt Charge
2. **Totems bereithalten** - Rage Mode ist gefährlich
3. **Gruppe:** 2-3 Spieler empfohlen
4. **Rüstung:** Netherite mit Protection IV+

### Für Profis (Tier 5):
1. **Volle Netherite-Ausrüstung** mit Custom Enchantments
2. **Gruppe:** 3-4 Spieler
3. **Potions:** Strength, Speed, Regeneration
4. **Golden Apples** bereithalten
5. **Totem of Undying** (mehrere!)

**Rage Mode Taktik:**
- ⚠️ Rage Mode startet bei 30% HP
- 🏃 KITE ihn! (Renne weg und schieße)
- 🛡️ Blocke ALLE Angriffe
- 💊 Heile SOFORT nach jedem Hit

---

## 🎯 Farming-Guide

### Beste Farming-Methode:

**Setup:**
1. World Tier 5 aktivieren
2. AFK-Zombie-Farm bauen
3. Warte auf Butcher-Spawn (0.1% Chance)
4. Alarm-System einbauen (z.B. Sound bei Boss-Spawn)

**Erwartete Spawn-Rate:**
- Pro 1000 Zombies: ~1 Butcher
- Effiziente Farm: ~1 Butcher pro Stunde

**Alternative:** Admin-Spawn für Tests:
```bash
/sp butcher spawn 5
```

---

## 📈 Balance-Analyse

### Schwierigkeit vs. Belohnung:

| World Tier | Schwierigkeit | Belohnungen | Wert |
|------------|---------------|-------------|------|
| Tier 2 | Mittel | 4 Books + Items | ⭐⭐⭐ |
| Tier 3 | Schwer | 5 Books + Items | ⭐⭐⭐⭐ |
| Tier 4 | Sehr Schwer | 6 Books + Items | ⭐⭐⭐⭐⭐ |
| Tier 5 | Extrem | 7 Books + Items | ⭐⭐⭐⭐⭐⭐ |

### Vergleich mit anderen Bossen:

| Boss | HP | Drops | Schwierigkeit |
|------|-----|-------|---------------|
| **The Butcher (T5)** | 1250 | 7 Books + Cleaver | ⭐⭐⭐⭐⭐ |
| Wither | 300 | Nether Star | ⭐⭐⭐⭐ |
| Ender Dragon | 200 | Dragon Egg | ⭐⭐⭐⭐ |
| Warden | 500 | Sculk | ⭐⭐⭐⭐⭐ |

**The Butcher ist der wertvollste Boss für Enchanted Books!**

---

## 🐛 Bekannte Mechaniken

### Spawn-Verhalten:
- ✅ Spawnt nur natürlich bei Zombie-Spawns
- ✅ Nicht von Spawnern
- ✅ Nicht von Spawn-Eggs
- ✅ Nur in aktivierten Welten

### Combat:
- ✅ Greift nur Spieler an (keine anderen Mobs)
- ✅ Kann nicht despawnen
- ✅ Behält Target auch bei großer Distanz
- ✅ Ignoriert Elytra-Spieler nicht

### Boss-Bar:
- ✅ Sichtbar für alle Spieler in 50 Blöcken
- ✅ Verschwindet automatisch bei Tod
- ✅ Updated kontinuierlich

---

## 🔧 Technische Details

### Entity-Tags:
- `butcher_boss` - Markiert als Butcher
- `special_mob` - Für Special Mob System

### Persistent Data:
- `butcher_boss` (Integer) - World Tier
- `butcher_last_charge` (Long) - Letzter Charge-Timestamp

### AI:
- Update-Rate: 1x pro Sekunde (20 Ticks)
- Target-Suche: Vanilla Zombie AI
- Ability-Checks: Jede Sekunde

---

## 📝 Commands & Permissions

### Commands:
```bash
/sp butcher spawn [tier]  # Spawnt Butcher
```

### Permissions:
```yaml
survivalplus.butcher: false  # Butcher spawnen
```

### Config (in config.yml):
```yaml
butcher:
  enabled: true
  spawn-chance: 0.001  # 0.1%
  min-world-tier: 2
  max-per-world: 1  # Optional: Limit
```

---

## 🎉 Fun Facts

- 🎮 **Inspiriert von:** Diablo 1, 2, 3 & 4
- 🗣️ **Spawn-Quote:** "FRESH MEAT!" (Original aus Diablo)
- 🔪 **Name:** The Butcher (Der Metzger)
- 🩸 **Thema:** Blut, Horror, Dunkelheit
- ⚡ **Besonderheit:** Einer der ersten Bosse in Diablo 1
- 💀 **Schwierigkeit:** Galt als einer der schwersten Early-Game Bosse

---

## 📚 Siehe auch

- [Enchantment Sources](ENCHANTMENT_SOURCES.md)
- [World Tier System](WIKI.md#world-tier-system)
- [Custom Items](FEATURES.md)
- [Commands](WIKI.md#commands--permissions)

---

**Viel Erfolg beim Jagen des Butchers! 🔪⚔️**

> "FRESH MEAT!" - The Butcher

