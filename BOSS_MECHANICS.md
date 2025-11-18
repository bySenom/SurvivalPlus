# Boss Mechaniken - SurvivalPlus v1.3.1

## Titan Golem - World Boss mit Intermission-Phasen

### Übersicht
Der Titan Golem ist ein team-basierter World Boss mit **7 Phasen** und **2 Intermissions**. Spieler müssen während der Intermissions Heilungs-Türme zerstören, um den Kampf fortzusetzen.

### Phasen-System

#### Phase 1 (100-80% HP)
- **Ability**: Ground Stomp (AOE Knockback, 15 Schaden)
- **Cooldown**: 8 Sekunden
- **Mechanik**: Basic combat phase, lernt Spieler kennen

#### ⚡ INTERMISSION 1 (80% HP)
- Boss wird **INVULNERABLE**
- Spawnt **3 Healing Towers** im 15-Block Radius
- **Beam-Effekt**: END_ROD + FIREWORK Partikel zwischen Türmen und Boss
- **Heilung**: +2 HP pro Tower pro Tick (0.5s)
- **Tower Health**: 100 + (World Tier × 50)
- **Ziel**: Alle 3 Türme zerstören

#### Phase 2 (80-60% HP)
- **Abilities**: Boulder Throw, Ground Stomp
- **Cooldown**: 6 Sekunden
- **Mechanik**: Ranged attacks, mehr Mobilität erforderlich

#### Phase 3 (60-40% HP)
- **Abilities**: Ground Slam (verzögerte AOE), Summon Minions (3x)
- **Cooldown**: 5 Sekunden
- **Buff**: Speed I
- **Mechanik**: Gefährliche AOE + Minion pressure

#### ⚡ INTERMISSION 2 (40% HP)
- Boss wird **INVULNERABLE**
- Spawnt **4 Healing Towers** im 15-Block Radius
- Verstärkte Heilung durch mehr Türme
- **Tower Health**: 100 + (World Tier × 50)
- **Ziel**: Alle 4 Türme zerstören

#### Phase 4 (40-20% HP)
- **Abilities**: Alle vorherigen + Shield (500 HP Absorption)
- **Cooldown**: 4 Sekunden
- **Buffs**: Strength I, Resistance I
- **Mechanik**: Boss tankt mehr, alle Abilities kombiniert

#### Phase 5 (20-0% HP) - BERSERK
- **Abilities**: Alle Abilities, Minion Summons (5x)
- **Cooldown**: 3 Sekunden (schnellste Phase!)
- **Buffs**: Strength II, Speed II, Resistance II
- **Mechanik**: Endkampf, volle Power, maximaler Druck

### Healing Tower Mechanik

#### Tower Properties
- **Entität**: ArmorStand (invisible) + END_ROD Block
- **Health**: 100 + (World Tier × 50)
  - Tier 1: 150 HP
  - Tier 3: 250 HP
  - Tier 5: 350 HP
- **Spawn Pattern**: Gleichmäßig verteilt im Kreis um Boss (15 Block Radius)
- **PDC Marker**: `healing_tower` mit Boss UUID

#### Beam-System
- **Partikel**: END_ROD (alle 0.5 Blocks) + FIREWORK (jede 3. Position)
- **Update Rate**: Alle 0.5 Sekunden (10 Ticks)
- **Heilung**: +2 HP pro Tower pro Tick
- **Visual**: Verbindet jeden Tower mit Boss-Center (+1.5 Y)

#### Tower Destruction
- **Schaden**: Spieler können Türme angreifen
- **Health Display**: Name zeigt aktuelle HP (❤ Symbol)
- **Death Effect**: EXPLOSION Partikel, Block entfernt
- **Win Condition**: Alle Türme zerstört = Intermission endet

### Team-Mechaniken

#### Aggro-System
```kotlin
// Tanks: Ziehen Aggro durch Schaden
addAggro(playerUUID, damage)

// Boss greift Spieler mit höchster Aggro an
target = getHighestAggroTarget()
```

#### Rollen
- **Tank**: Hält Aggro, blockt Ground Stomp
- **DPS**: Fokussiert Türme während Intermission, Boss sonst
- **Support**: Heilt Team, bufft während Phase-Transitions

### World Tier Scaling

| Tier | Boss HP | Boss Damage | Tower HP | Minions |
|------|---------|-------------|----------|---------|
| 1    | 1000    | 20          | 150      | 2-3     |
| 2    | 1500    | 26          | 200      | 3-4     |
| 3    | 2000    | 32          | 250      | 3-5     |
| 4    | 2500    | 38          | 300      | 4-5     |
| 5    | 3000    | 44          | 350      | 5+      |

### Abilities im Detail

#### Ground Stomp
- **Typ**: AOE (10 Block Radius)
- **Schaden**: 15 + (World Tier × 2)
- **Effekt**: Knockback (Velocity × 1.5 upward)
- **Partikel**: EXPLOSION, BLOCK (STONE)
- **Sound**: ENTITY_GENERIC_EXPLODE

#### Boulder Throw
- **Typ**: Projectile
- **Entity**: Falling Block (STONE)
- **Velocity**: Richtung zu Spieler × 1.5
- **Schaden**: 10 (on hit)
- **Radius**: 3 Blocks AOE bei Impact

#### Ground Slam
- **Typ**: Delayed AOE (15 Block Radius)
- **Delay**: 2 Sekunden (Warnung)
- **Schaden**: 30 + (World Tier × 3)
- **Effekt**: Slowness III (5s)
- **Partikel**: LAVA, FLAME, EXPLOSION
- **Sound**: ENTITY_WITHER_HURT

#### Shield
- **Typ**: Absorption Shield
- **Health**: 500 HP
- **Decay**: -5 HP pro Tick (bis zerstört)
- **Visual**: BARRIER Partikel um Boss
- **Break Effect**: GLASS Block Partikel

#### Summon Minions
- **Count**: 2-5 (je nach Phase)
- **Type**: Stone Minions (Zombie mit Stein-Rüstung)
- **Marker**: `boss_minion` PDC (kein Loot)
- **Spawn**: Ring um Boss, 5 Block Radius

### Implementation Details

#### Code Structure
```
bosses/
├── TitanGolemBoss.kt (730+ Zeilen)
│   ├── BossData (data class mit intermission state)
│   ├── spawnBoss() - Main spawn
│   ├── updateBoss() - Intermission/Phase checks
│   ├── startIntermission() - Invulnerability + spawn towers
│   ├── spawnHealingTowers() - Tower placement logic
│   ├── startHealingBeams() - BukkitRunnable für beams
│   ├── drawBeam() - Partikel-Rendering
│   ├── updateIntermission() - Tower validation
│   ├── endIntermission() - Cleanup + continue fight
│   ├── transitionPhase() - Phase-Effekte
│   ├── executePhaseAbility() - Phase-spezifische Abilities
│   ├── ability*() - Individuelle Ability-Methoden
│   └── cleanupBoss() - Full cleanup inkl. towers

listeners/
└── BossListener.kt (NEU - 67 Zeilen)
    ├── onHealingTowerDamage() - Damage events für Türme
    └── onHealingTowerDeath() - Tower destruction cleanup
```

#### Key Features
1. **Invulnerability**: `boss.isInvulnerable = true` während Intermission
2. **BukkitRunnable**: Separate Task für Beam-Rendering (stoppt bei Intermission-Ende)
3. **PDC Tracking**: `healing_tower` Key für Tower-Identifikation
4. **Automatic Cleanup**: Türme werden bei Boss-Tod oder Intermission-Ende entfernt
5. **Visual Feedback**: Health-Display auf Tower-Namen, Beam-Partikel, Phase-Messages

### Config
```yaml
bosses:
  titan-golem:
    enabled: true
    base-health: 1000
    base-damage: 20
    spawn-interval: 30  # Minuten
    warning-time: 60    # Sekunden
    loot-multiplier: 2.0
```

### Commands
```
/sp arena enter - Teleport zur Boss Arena (Survival_boss)
/sp arena leave - Zurück zu Survival spawn
/sp arena info - Boss Timer und Info
```

### Testing Checklist
- [ ] Intermission 1 triggert bei 80% HP
- [ ] 3 Türme spawnen korrekt verteilt
- [ ] Beams sind visuell sichtbar
- [ ] Boss heilt während Intermission
- [ ] Boss ist invulnerable während Intermission
- [ ] Türme nehmen Schaden und zeigen HP
- [ ] Intermission endet nach Tower-Destruction
- [ ] Intermission 2 triggert bei 40% HP
- [ ] 4 Türme spawnen in Phase 2
- [ ] Phase 5 Berserk aktiviert korrekt
- [ ] Cleanup funktioniert bei Boss-Tod
- [ ] World Tier Scaling korrekt

### Known Issues
- Deprecated `spawnFallingBlock()` in Boulder Throw (nicht kritisch)
- Unnecessary safe calls (Kotlin warnings, funktional irrelevant)

### Nächste Schritte
1. **World-Tier-spezifische Bosse** (5 unique Bosse für Tier 1-5)
2. **Debug Command** (`/sp boss debug <tier>`) für Testing
3. **Boss Phasing System** in WorldBossArenaManager

## Entwickler-Notizen

### Wie füge ich einen neuen Boss hinzu?
1. Erstelle `<BossName>Boss.kt` in `bosses/` Package
2. Kopiere Struktur von `TitanGolemBoss.kt`
3. Definiere unique Abilities und Phasen
4. Registriere in `SurvivalPlus.kt` (`lateinit var`)
5. Initialisiere in `onEnable()`
6. Update `WorldBossArenaManager.spawnWorldBoss()` für Tier-Auswahl

### Ability-Pattern
```kotlin
private fun abilityMyAbility(data: BossData) {
    val boss = data.boss
    val location = boss.location
    
    // 1. Effekte spawnen
    location.world?.spawnParticle(...)
    location.world?.playSound(...)
    
    // 2. Spieler im Radius finden
    location.world?.getNearbyEntities(location, radius, ...)
        .filterIsInstance<Player>()
        .forEach { player ->
            // 3. Schaden/Effekte anwenden
            player.damage(damage)
            player.addPotionEffect(...)
            
            // 4. Aggro tracken
            addAggro(data, player.uniqueId, damage)
        }
}
```

### Intermission-Pattern
```kotlin
// Check in updateBoss():
if (healthPercent <= threshold && currentPhase == X) {
    startIntermission(data, intermissionNumber)
    return
}

// Intermission endet automatisch wenn alle Türme tot
// oder via manual call: endIntermission(data)
```

---

**Version**: v1.3.1  
**Datum**: 2024  
**Author**: bySenom  
**Status**: ✅ Intermission System implementiert
