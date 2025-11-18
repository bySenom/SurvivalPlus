# 🌍 Natürliches Gameplay-System - Dokumentation

> **SurvivalPlus v2.0** - Ohne Commands, nur durch natürliche Interaktion!

---

## 🎯 Design-Philosophie

**Ziel:** Alles soll natürlich und intuitiv funktionieren - KEINE Commands für normale Spieler!
- ✅ Strukturen finden → Erkunden → Nutzen
- ✅ Items farmen → Bessere Qualität durch höheres World Tier
- ✅ Progression durch Exploration, nicht durch Commands
- ✅ Balanciert durch Knappheit und Distanz

---

## 🏛️ World Tier System - Natürliche Shrines!

### 🆕 Wie funktioniert es? (NEU!)

#### 1. **World Tier Shrines FINDEN**
```
Natürlich generierte Strukturen!

Eigenschaften:
- Spawnen automatisch beim World-Load
- 1200 Blöcke Mindest-Abstand zwischen Shrines
- Ruinen-Ästhetik (Blackstone, teilweise zerstört)
- KEINE LIMITIERUNG der Anzahl pro Welt!
- Beacon-Laser (sichtbar aus der Ferne!)
```

**Wie finde ich einen Shrine?**
- 🔦 Beacon-Laser nach oben (End Rod Partikel)
- 🗺️ Exploration (500+ Blöcke vom Spawn)
- 👥 Frag andere Spieler
- 🧭 Nutze Koordinaten (wenn erlaubt)

#### 2. **Shrine-Struktur**
```
Design:
- 9x9 Plattform (Polished Blackstone)
- 4 Säulen an den Ecken (unterschiedliche Höhen)
- Lodestone-Altar in der Mitte
- Glowstone-Beleuchtung
- Ruinen-Look (teilweise zerstört)
- Geschützt (kein Griefing möglich!)
```

#### 3. **World Tier ändern**
- Rechtsklick auf den Lodestone-Altar
- GUI öffnet sich mit 5 Tiers:
  - **Normal** (Tier 1): Kostet 1x Iron Ingot
  - **Heroic** (Tier 2): Kostet 16x Gold Ingot
  - **Epic** (Tier 3): Kostet 16x Diamond
  - **Legendary** (Tier 4): Kostet 32x Emerald
  - **Mythic** (Tier 5): Kostet 16x Netherite Ingot

#### 4. **Optional: Craftbare Altäre**
```
Für Spieler die einen privaten Altar in ihrer Base wollen:

Rezept (Crafting Table):
┌─────────┬─────────┬─────────┐
│ Diamond │ Diamond │ Diamond │
│  Block  │  Block  │  Block  │
├─────────┼─────────┼─────────┤
│ Diamond │ Lodestone│ Diamond│
│  Block  │         │  Block  │
├─────────┼─────────┼─────────┤
│Netherite│Netherite│Netherite│
│ Ingot   │ Ingot   │ Ingot   │
└─────────┴─────────┴─────────┘

Kosten: 4 Diamond Blocks + 1 Lodestone + 3 Netherite Ingots
Vorteil: Kein Laufen nötig
Nachteil: KEIN Beacon-Laser (nur natürliche Shrines haben das!)
```

#### 5. **Was passiert?**
- **Sofort:** Alle Spieler in der Welt sehen einen Title
- **Mobs:** Werden stärker (mehr HP, mehr Schaden)
- **Drops:** Bessere Qualität (World Tier Boost!)
- **Special Mobs:** Höhere Spawn-Chance

---

## 📊 World Tier Übersicht

| Tier | Name | Mob HP | Mob DMG | Drop Boost | Special Mobs | Kosten |
|------|------|--------|---------|------------|--------------|--------|
| 1 | Normal | 100% | 100% | +0 | 0% | 1 Iron |
| 2 | Heroic | 150% | 125% | +1 | 5% | 16 Gold |
| 3 | Epic | 200% | 150% | +2 | 10% | 16 Diamond |
| 4 | Legendary | 300% | 200% | +3 | 15% | 32 Emerald |
| 5 | Mythic | 500% | 300% | +4 | 25% | 16 Netherite |

### Drop Boost Erklärung

**Boost +1:** Mindestens Uncommon Quality bei Drops  
**Boost +2:** Mindestens Rare Quality bei Drops  
**Boost +3:** Mindestens Epic Quality bei Drops  
**Boost +4:** Mindestens Legendary Quality bei Drops (Mythic möglich!)

**Beispiel:**
- Normal Tier (Boost +0): 50% Common, 30% Uncommon, 15% Rare, 4% Epic, 0.9% Legendary, 0.1% Mythic
- Mythic Tier (Boost +4): 0% Common, 0% Uncommon, 0% Rare, 0% Epic, 70% Legendary, 30% Mythic

---

## 🎮 Natürliches Spieler-Erlebnis

### Szenario 1: Neuer Spieler

1. **Start:** Spawnt in Normal Tier Welt
2. **Farmen:** Bekommt normale Custom Items (meist Common/Uncommon)
3. **Progression:** 
   - Farmt Materialien
   - Craftet Custom Anvil für bessere Items
   - Craftet Reforging Station für Upgrades
   - Farmt für World Tier Altar
4. **Endgame:**
   - Platziert Altar
   - Wählt Heroic/Epic
   - Bekommt bessere Drops
   - Challenges sich selbst mit Mythic Tier

### Szenario 2: Erfahrener Spieler

1. **Zugriff:** Findet Altar am Spawn (von Admin/Community platziert)
2. **Wahl:** Entscheidet selbst, welches Tier er spielen möchte
3. **Kosten:** Muss Materialien farmen für Tier-Wechsel
4. **Belohnung:** Höheres Risiko = Höhere Belohnung

### Szenario 3: Server-Community

1. **Community-Altar:** Admin platziert Altar am Spawn
2. **Demokratie:** Spieler stimmen ab (durch Materialien-Einzahlung)
3. **Events:** "Mythic Weekend" - Community farmt für Tier 5
4. **Belohnungen:** Alle profitieren von besseren Drops

---

## 🔄 Integration mit anderen Systemen

### Custom Items
- **Ohne World Tier:** Items droppen normal (Quality.random())
- **Mit World Tier:** Items droppen besser (Quality.randomWithBoost())
- **Code:** `itemManager.createRandomItemWithWorldBoost(material, world)`

### Special Mobs
- **Spawn-Chance** steigt mit World Tier
- **Normal:** 0% Special Mobs
- **Heroic:** 5% Special Mobs
- **Mythic:** 25% Special Mobs
- **Bessere Drops:** Special Mobs droppen +1 Tier höher

### World Events
- **Event-Belohnungen** berücksichtigen World Tier
- **Event-Drops** bekommen World Tier Boost
- **Event-Schwierigkeit** skaliert mit World Tier

---

## ⚖️ Balancing

### Warum sind Rezepte so teuer?

**Custom Anvil:** 4 Netherite + 1 Diamond Block + 3 Nether Stars
- **Begründung:** Ermöglicht unbegrenztes Craften von Custom Items
- **Alternative:** Admin gibt Items per Command (weniger balanciert)

**Reforging Station:** 4 Emerald Blocks + 1 Anvil + 3 Obsidian
- **Begründung:** Ermöglicht Upgrade von Items
- **Alternative:** Items neu farmen (frustrierend)

**World Tier Altar:** 4 Diamond Blocks + 1 Lodestone + 3 Netherite
- **Begründung:** Beeinflusst ALLE Drops in der ganzen Welt
- **Alternative:** Jeder Spieler für sich (unfair)

### Tier-Wechsel Kosten

**Warum kosten Tier-Wechsel?**
1. **Verhindert Spam:** Spieler wechseln nicht ständig hin und her
2. **Commitment:** Spieler müssen sich für ein Tier entscheiden
3. **Farmen:** Materialien müssen gefarmt werden (Gameplay-Loop)
4. **Wirtschaft:** Schafft Nachfrage für Materialien

**Kostenstruktur:**
- **Aufwärts:** Teurer (16-32 Items)
- **Abwärts:** Billiger (1-16 Items) - ermöglicht "Downgrade" wenn zu schwer
- **Linear:** Jedes Tier ist erreichbar

---

## 🎯 Best Practices für Server-Owner

### Setup

1. **Spawn-Altar:** Platziere einen Altar am Spawn
   - Gibt per Command: `/sp giveblock <admin> world_tier_altar`
   - Platzieren und schützen (WorldGuard Region)

2. **Start-Tier:** Setze Normal Tier als Default
   - Neue Spieler sind nicht überfordert
   - Erfahrene Spieler können upgraden

3. **Community-Entscheidung:** Lass Spieler abstimmen
   - Wöchentliche Umfrage: "Welches Tier nächste Woche?"
   - Materialien sammeln als Community-Ziel

### Event-Ideen

**Mythic Weekend:**
- Freitag bis Sonntag: Tier 5 aktiv
- Community farmt 16 Netherite zusammen
- Alle bekommen bessere Drops

**Tier-Challenges:**
- Woche 1: Heroic (5% Special Mobs)
- Woche 2: Epic (10% Special Mobs)
- Woche 3: Legendary (15% Special Mobs)
- Woche 4: Mythic (25% Special Mobs)

**Saisonale Events:**
- Halloween: Mythic Tier + mehr Special Mobs
- Weihnachten: Legendary Tier + Event Items
- Ostern: Epic Tier + Schatzgoblin-Event

---

## 📈 Progression-Pfad

### Early Game (Level 1-20)
- **Tier:** Normal
- **Fokus:** Basis-Items farmen
- **Ziel:** Crafting-Materialien sammeln

### Mid Game (Level 20-50)
- **Tier:** Heroic/Epic
- **Fokus:** Custom Anvil/Reforging Station bauen
- **Ziel:** Bessere Items craften/upgraden

### Late Game (Level 50-80)
- **Tier:** Epic/Legendary
- **Fokus:** Set-Boni sammeln
- **Ziel:** Komplette Armor-Sets mit Boni

### End Game (Level 80+)
- **Tier:** Legendary/Mythic
- **Fokus:** Mythic Items farmen
- **Ziel:** Perfekte Items mit max Stats

---

## 🛡️ Anti-Exploit Maßnahmen

### World Tier Altar

**Problem:** Spieler könnten Altar spammen
**Lösung:** 
- Teures Rezept (4 Diamond Blocks + Netherite)
- Nur sinnvoll als zentrale Station
- Server-Owner können Anzahl limitieren

**Problem:** Spieler wechseln zwischen Tiers für beste Situation
**Lösung:**
- Kosten für jeden Wechsel
- 5-Minuten Cooldown nach Wechsel (geplant)
- Community-Voting erforderlich (geplant)

### Drop-System

**Problem:** Spieler farmen in Mythic Tier nur Legendary Items
**Lösung:**
- Mythic Tier = schwere Mobs (3x Schaden, 5x HP)
- Kosten für Tier-Wechsel
- Special Mobs sind gefährlich
- Balanciert durch Risk vs. Reward

---

## 🔮 Geplante Features (Coming Soon)

### World Tier Improvements
- [ ] Cooldown zwischen Tier-Wechseln (5 Minuten)
- [ ] Community-Voting (50% der Online-Spieler müssen zustimmen)
- [ ] Tier-History (welche Tiers wurden wann aktiv?)
- [ ] Statistiken (Kills/Deaths pro Tier)

### GUI Improvements
- [ ] Preview von Mob-Stats im GUI
- [ ] Beispiel-Items mit Drop-Chancen
- [ ] Tier-Empfehlungen basierend auf Spieler-Level

### Economy Integration
- [ ] Vault-Support für Tier-Wechsel
- [ ] Geld statt Items als Kosten
- [ ] Server-Shop für Altar-Zugang

---

**Entwickelt mit ❤️ für natürliches Gameplay!**  
**Keine Commands, nur pure Interaktion! 🎮**

