# 🚀 Custom Anvil System - Quick Start Guide

> **Schnelleinstieg:** In 5 Minuten loslegen!

---

## 📋 Voraussetzungen

- ✅ SurvivalPlus Plugin installiert
- ✅ Spigot/Paper 1.20+ Server
- ✅ Berechtigung: `survivalplus.give` (für Admin-Commands)

---

## 🎮 Für Spieler

### Schritt 1: Materialien farmen
```
Custom Anvil benötigt:
├─ 4x Netherite Ingot (Ancient Debris farmen!)
├─ 1x Diamond Block (9 Diamanten)
└─ 3x Nether Star (Wither Boss töten!)

Reforging Station benötigt:
├─ 4x Emerald Block (36 Emeralds)
├─ 1x Anvil (3 Eisenblöcke + 4 Eisen)
└─ 3x Obsidian (Lava + Wasser)
```

### Schritt 2: Craften
1. Öffne Crafting Table
2. Lege Materialien nach Rezept ein
3. Nimm Custom Block aus dem Output-Slot

**Custom Anvil Rezept:**
```
┌─────┬─────┬─────┐
│  N  │  N  │  N  │
├─────┼─────┼─────┤
│  N  │  D  │  N  │
├─────┼─────┼─────┤
│  S  │  S  │  S  │
└─────┴─────┴─────┘
N = Netherite Ingot
D = Diamond Block
S = Nether Star
```

### Schritt 3: Platzieren
1. Custom Block in die Hand nehmen
2. Auf den Boden rechtsklicken
3. ✨ Partikel-Effekt erscheint!
4. Block ist platziert

### Schritt 4: Verwenden
**Custom Anvil:**
1. Rechtsklick auf den Block
2. GUI öffnet sich
3. Lege ein Base-Item in den Slot (z.B. Diamond Sword)
4. Wähle Qualität (Klick auf Qualitäts-Selector)
5. Lege benötigtes Material ein (z.B. 32x Nether Star für Mythic)
6. Klick auf grünen "✔ Craften" Button
7. ✅ Erhalte dein Custom Item!

**Reforging Station:**
1. Item in die Hand nehmen
2. Rechtsklick auf den Block
3. Reforging GUI öffnet sich
4. Folge den Anweisungen

### Schritt 5: Abbauen
1. Block mit beliebigem Werkzeug abbauen
2. Custom Block droppt zurück ins Inventar
3. Kann neu platziert werden

---

## 👨‍💼 Für Admins

### Quick-Test (30 Sekunden)

```bash
# 1. Block geben
/sp giveblock <dein_name> custom_anvil

# 2. Platzieren
# Rechtsklick auf Boden

# 3. Testen
# Rechtsklick auf Block → GUI sollte öffnen

# 4. Abbauen
# Linksklick → Block sollte droppen
```

### Debug-Checks

```bash
# 1. Prüfe ob Plugin geladen ist
/plugins

# 2. Prüfe ob Rezepte registriert sind
# → Server-Log beim Start prüfen:
# "✓ Custom Anvil Rezept registriert!"
# "✓ Reforging Station Rezept registriert!"

# 3. Prüfe platzierte Blocks
# → Datei: plugins/SurvivalPlus/custom_blocks.yml
```

### Häufige Probleme

#### Problem: "Unresolved reference 'customBlockManager'"
**Lösung:** Server neu starten (IDE-Cache-Problem)

#### Problem: Block droppt nicht
**Lösung:** 
- Prüfe GameMode (nicht Kreativ)
- Prüfe WorldGuard/Protection-Plugins

#### Problem: GUI öffnet nicht
**Lösung:**
- Prüfe Permissions
- Prüfe Server-Log für Fehler
- Verify Block ist in custom_blocks.yml

#### Problem: Armor Stand ist sichtbar
**Lösung:** 
- Normal! Design-Entscheidung
- Armor Stand ist "marker" und sollte minimal sein
- Kann mit Resource Pack optimiert werden

---

## 🎯 Verwendungs-Beispiele

### Survival-Modus
```
1. Farme Wither Bosse (für Nether Stars)
2. Sammle 4 Ancient Debris → 4 Netherite
3. Mine 9 Diamanten → 1 Diamond Block
4. Crafte Custom Anvil
5. Baue eine Schmieden-Hütte
6. Platziere Custom Anvil in der Mitte
7. Erstelle Custom Items!
```

### Creative Testing
```
1. /gamemode creative
2. /sp giveblock @s custom_anvil
3. Platzieren & testen
4. /sp giveblock @s reforging_station
5. Platzieren & testen
```

### Community-Server
```
1. Erstelle geschützten Spawn-Bereich
2. Platziere Custom Anvils für alle Spieler
3. Setze Permissions für Nutzung
4. Optional: Gebühr via Economy-Plugin
```

### Minigame-Integration
```
1. Spieler bekommt Custom Anvil als Belohnung
2. Kann in persönlicher Base platzieren
3. Nutzt ihn zum Upgraden von Equipment
4. Konkurriert mit anderen Spielern
```

---

## 💡 Pro-Tipps

### Für Spieler:
1. **Sichere Location:** Platziere Blocks in deinem Claim/geschütztem Gebiet
2. **Backup-Blocks:** Crafte mehrere für verschiedene Locations
3. **Community-Crafting:** Teile Crafting-Stations mit Freunden
4. **Wither-Farm:** Baue eine automatische Wither-Farm für Nether Stars
5. **Emerald-Farm:** Villager-Trading für Emeralds (Reforging Station)

### Für Admins:
1. **Spawn-Protection:** Platziere 1-2 Blocks am Spawn für alle
2. **Shop-Integration:** Verkaufe Blocks im Admin-Shop
3. **Event-Rewards:** Gib Blocks als Quest-Belohnungen
4. **VIP-Perks:** Gib VIPs Blocks als Benefit
5. **Balancing:** Monitor wie oft Blocks benutzt werden (Logs)

---

## 📊 Material-Kalkulation

### Custom Anvil (Gesamt-Kosten)
```
4x Ancient Debris farmen (Nether)
  └─→ 4x Netherite Scrap
      └─→ + 16x Gold Ingots
          └─→ 4x Netherite Ingot

9x Diamond minen (Y-Level -59 bis 16)
  └─→ 1x Diamond Block

3x Wither Boss töten
  └─→ 3x Nether Star
      └─→ Benötigt: 12x Wither Skeleton Skulls + Soul Sand

Geschätzte Zeit: 3-5 Stunden Farmarbeit
```

### Reforging Station (Gesamt-Kosten)
```
36x Emerald
  ├─→ Trading mit Villagers (empfohlen)
  └─→ Oder Mining (sehr selten!)
  └─→ 4x Emerald Block

31x Iron Ingots
  └─→ 3x Iron Block + 4x Iron Ingot
      └─→ 1x Anvil

3x Obsidian minen
  └─→ Diamond Pickaxe benötigt

Geschätzte Zeit: 1-2 Stunden mit Villager-Farm
```

---

## 🎓 Nächste Schritte

Nach dem Setup:
1. ✅ Lies `CUSTOM_BLOCKS.md` für Details
2. ✅ Teste alle Features
3. ✅ Gib Feedback
4. ✅ Hab Spaß! 🎉

---

**Happy Crafting! ⚒️✨**

