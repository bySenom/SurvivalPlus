# 🧪 Test-Anleitung: Kritische Fixes

> **Zum Testen der behobenen kritischen Probleme**  
> **Geschätzte Testzeit:** 10-15 Minuten

---

## ✅ 1. Mining Speed Test

### Vorbereitung
1. Server starten
2. Ingame joinen (Survival-Welt)
3. `/sp kit` ausführen (gibt Mythic Gear)

### Test 1: Verschiedene Qualitäten
```
/sp give <dein_name> DIAMOND_PICKAXE common
/sp give <dein_name> DIAMOND_PICKAXE rare
/sp give <dein_name> DIAMOND_PICKAXE mythic
```

**Was zu testen:**
- [ ] Common Pickaxe: Normale Geschwindigkeit
- [ ] Rare Pickaxe: Deutlich schneller (Haste II sichtbar)
- [ ] Mythic Pickaxe: Extrem schnell (Haste V sichtbar)

**Erwartetes Ergebnis:**
- ✅ Haste-Effekt wird angezeigt in Effekt-Liste
- ✅ Abbau ist WIRKLICH schneller (nicht nur visuell!)
- ✅ Mythic = Fast 2x so schnell wie Common

### Test 2: Instamine
```
/sp give <dein_name> DIAMOND_PICKAXE mythic
```

**Was zu testen:**
- [ ] Mit Mythic Pickaxe 20+ Blöcke abbauen
- [ ] Gelegentlich Partikel-Effekt sehen
- [ ] Gelegentlich "CRIT" Sound hören

**Erwartetes Ergebnis:**
- ✅ Circa 20% der Blöcke mit Partikel/Sound
- ✅ Diese Blöcke fühlen sich instant an

---

## ✅ 2. Material-Abdeckung Test

### Test: Alle Materialtypen
```
/sp give <dein_name> WOODEN_SWORD rare
/sp give <dein_name> STONE_AXE epic
/sp give <dein_name> IRON_PICKAXE legendary
/sp give <dein_name> GOLDEN_SHOVEL mythic
/sp give <dein_name> LEATHER_CHESTPLATE rare
```

**Was zu testen:**
- [ ] Alle Items haben Custom-Lore
- [ ] Alle Items haben Quality-Farbe
- [ ] Alle Items haben Stats
- [ ] Stats passen zum Material-Typ

**Erwartetes Ergebnis:**
- ✅ Holz-Schwert: ~4.0 Schaden
- ✅ Stein-Axt: ~9.0 Schaden
- ✅ Gold: Schnellste Abbaugeschwindigkeit
- ✅ Leder: Niedrigste Rüstung

### Test: Turtle Shell (NEU!)
```
/sp give <dein_name> TURTLE_HELMET epic
```

**Erwartetes Ergebnis:**
- ✅ Hat Custom-Lore
- ✅ Hat Rüstungs-Stats (Iron-Level)
- ✅ Funktioniert wie andere Helme

---

## ✅ 3. Enchantment Balance Test

### Test 1: Vein Miner
```
/sp enchant vein_miner 1
```

**Was zu testen:**
- [ ] Finde große Erzader (z.B. Copper)
- [ ] Baue 1 Block ab
- [ ] Zähle wie viele Blöcke abgebaut werden

**Erwartetes Ergebnis:**
- ✅ Maximal 32 Blöcke werden abgebaut
- ✅ Nicht mehr (früher 64)
- ✅ Performance ist gut

### Test 2: Timber
```
/sp enchant timber 1
```

**Was zu testen:**
- [ ] Finde großen Baum
- [ ] Baue 1 Log ab
- [ ] Zähle wie viele Logs fallen

**Erwartetes Ergebnis:**
- ✅ Maximal 64 Logs werden abgebaut
- ✅ Nicht mehr (früher 128)
- ✅ Keine Lag-Spikes

### Test 3: Explosive Cooldown
```
/sp give <dein_name> DIAMOND_SWORD mythic
/sp enchant explosive 2
```

**Was zu testen:**
- [ ] Schlage mehrere Mobs schnell hintereinander
- [ ] Achte auf Explosionen
- [ ] Achte auf "💥 Explosive!" Nachricht

**Erwartetes Ergebnis:**
- ✅ Nicht jeder Hit = Explosion
- ✅ Nur ~1 Explosion alle 5 Sekunden
- ✅ ActionBar zeigt "💥 Explosive!" bei Proc
- ✅ Explosionen sind kleiner als früher

### Test 4: Thunder Strike Cooldown
```
/sp give <dein_name> DIAMOND_SWORD mythic
/sp enchant thunder_strike 2
```

**Was zu testen:**
- [ ] Schlage mehrere Mobs schnell hintereinander
- [ ] Achte auf Blitze
- [ ] Achte auf "⚡ Thunder Strike!" Nachricht

**Erwartetes Ergebnis:**
- ✅ Nicht jeder Hit = Blitz
- ✅ Nur ~1 Blitz alle 8 Sekunden
- ✅ ActionBar zeigt "⚡ Thunder Strike!" bei Proc
- ✅ Kein Lag durch Blitz-Spam

---

## 🔧 Config-Test

### Test: Config-Anpassungen
1. Stoppe Server
2. Öffne `config.yml`
3. Ändere Werte:
```yaml
enchantment-balance:
  vein-miner-max-blocks: 16  # Reduziert
  explosive-cooldown: 1      # Fast kein Cooldown
```
4. Starte Server
5. Teste Vein Miner & Explosive

**Erwartetes Ergebnis:**
- ✅ Vein Miner baut nur noch 16 Blöcke ab
- ✅ Explosive procct fast jeden Hit

---

## 📊 Performance-Test (Optional)

### Test: TPS während Vein Miner
1. Nutze TPS-Plugin (z.B. `/tps`)
2. Notiere aktuelle TPS (sollte 20 sein)
3. Nutze Vein Miner auf große Erzader
4. Checke TPS während/nach Abbau

**Erwartetes Ergebnis:**
- ✅ TPS bleibt bei ~19-20
- ✅ Keine großen Drops
- ✅ Kein Lag

### Test: TPS während Timber
1. Notiere aktuelle TPS
2. Nutze Timber auf großen Baum
3. Checke TPS während/nach Abbau

**Erwartetes Ergebnis:**
- ✅ TPS bleibt bei ~19-20
- ✅ Keine großen Drops
- ✅ Kein Lag

---

## ✅ Checkliste: Alles funktioniert?

### Mining Speed
- [ ] Haste-Effekt wird angezeigt
- [ ] Abbau ist tatsächlich schneller
- [ ] Mythic = ~2x schneller als Common
- [ ] Instamine funktioniert gelegentlich
- [ ] Partikel & Sound bei Instamine

### Materialien
- [ ] Wood Items funktionieren
- [ ] Stone Items funktionieren
- [ ] Iron Items funktionieren
- [ ] Gold Items funktionieren
- [ ] Diamond Items funktionieren
- [ ] Netherite Items funktionieren
- [ ] Leather Armor funktioniert
- [ ] Turtle Shell funktioniert
- [ ] Alle haben passende Stats

### Enchantment Balance
- [ ] Vein Miner max 32 Blöcke
- [ ] Timber max 64 Blöcke
- [ ] Explosive hat 5s Cooldown
- [ ] Thunder Strike hat 8s Cooldown
- [ ] ActionBar Feedback funktioniert
- [ ] Config-Werte werden angewendet

### Performance
- [ ] Keine Lag-Spikes
- [ ] TPS bleibt stabil
- [ ] Keine Fehler in Console

---

## 🐛 Falls etwas nicht funktioniert

### Mining Speed funktioniert nicht
**Check:**
1. Bist du in aktivierter Welt? (Survival, Survival_Nether, Survival_End)
2. Hast du das richtige Item in Main-Hand?
3. Siehst du Haste-Effekt in Effekt-Liste?

**Debug:**
```yaml
# In config.yml
debug: true
```
→ Zeigt Mining Speed Bonus in ActionBar

### Materialien zeigen keine Stats
**Check:**
1. Ist es ein Custom Item? (hat Quality in Lore)
2. Generiere neues Item: `/sp give <name> <material> mythic`

### Enchantment Cooldown funktioniert nicht
**Check:**
1. Hast du genug Mobs zum Testen? (mindestens 5+)
2. Wartest du die Cooldown-Zeit ab?
3. Schaust du auf ActionBar?

**Debug:**
```
# In Console sehen nach "Explosive" oder "Thunder Strike"
```

### Performance-Probleme
**Reduziere Config-Werte:**
```yaml
enchantment-balance:
  vein-miner-max-blocks: 16  # Statt 32
  timber-max-blocks: 32      # Statt 64
```

---

## 📝 Test-Report

Nach dem Testen, fülle aus:

**Mining Speed:** ✅ / ❌  
**Materialien:** ✅ / ❌  
**Vein Miner:** ✅ / ❌  
**Timber:** ✅ / ❌  
**Explosive:** ✅ / ❌  
**Thunder Strike:** ✅ / ❌  
**Performance:** ✅ / ❌  

**Notizen:**
```
[Hier deine Beobachtungen eintragen]
```

---

**Geschätzte Testzeit:** 10-15 Minuten  
**Bei Problemen:** Siehe "Falls etwas nicht funktioniert"  
**Build:** v1.2.0-SNAPSHOT  
**Datum:** 2025-11-18

