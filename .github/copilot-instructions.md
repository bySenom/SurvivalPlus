# SurvivalPlus - AI Coding Agent Instructions

## Project Overview
Minecraft Paper 1.21+ plugin with Tierify-inspired quality system. Written in Kotlin 2.3.0, implements quality tiers (Common→Mythic), reforging mechanics, custom enchantments, world tier system, and Diablo-style special mobs/bosses. Currently v1.0-SNAPSHOT (Phase 3 complete, ~90% feature-complete).

## Architecture & Core Components

### Manager Pattern
All core systems use manager classes initialized in `SurvivalPlus.kt#onEnable()`:
- **ItemManager** - Creates/manages items with quality tiers, uses `NamespacedKey(plugin, "quality")` for persistence
- **ReforgingManager** - Handles 3-tier reforging system (Limestone→Pyrite→Galena)
- **EnchantmentManager** - 12 custom enchantments stored as `customench_<name>` in PersistentDataContainer
- **WorldTierManager** - 5 tiers (Normal→Mythic) affecting drops/mobs, configurable per world
- **SpecialMobManager** - Spawns mobs with affixes (Diablo-style), uses `mob_affix` PDC key
- **SetBonusManager** - 6 armor sets with 2/4-piece bonuses, stored via `armor_set` PDC key

Access managers through plugin instance: `plugin.itemManager.createItem(material, quality)`

### Data Persistence Strategy
Uses Bukkit's PersistentDataContainer with NamespacedKeys extensively:
```kotlin
// Pattern used throughout codebase
private val myKey = NamespacedKey(plugin, "my_data")
meta.persistentDataContainer.set(myKey, PersistentDataType.STRING, value)
val data = meta.persistentDataContainer.get(myKey, PersistentDataType.STRING)
```
**Never** use lore for data storage - only for display. All item state in PDC.

### Event-Driven Architecture
Listeners registered in `SurvivalPlus.kt` (see lines 148-171):
- ItemListener - Mob drops, fishing (15-30% drop chance based on mob type)
- EnchantmentListener - Handles custom enchantment triggers with cooldowns
- SetBonusListener - Equipment changes, updates active set bonuses
- CustomBlockListener - Custom block placement/interaction (uses armor stands for visualization)

### Quality System (`models/Quality.kt`)
6 tiers with weight-based random selection:
- COMMON (40%), UNCOMMON (30%), RARE (15%), EPIC (10%), LEGENDARY (4%), MYTHIC (1%)
- `Quality.randomWithBoost(boost)` increases minimum tier (used by World Tier system)
- `statMultiplier` field ranges 1.0x→3.0x, applied to base stats in ItemManager

## Development Workflows

### Building & Testing
```powershell
# Build plugin (auto-deploys to test server via deployPlugin task)
./gradlew build

# Run test server (Paper 1.21 via run-paper plugin)
./gradlew runServer

# Deploy manually
./gradlew deployPlugin  # Copies to C:/Users/SashaW/IdeaProjects/HubPlugin/run/plugins
```

### Adding Custom Items
See `ENTWICKLER_GUIDE.md` for templates. Key pattern in `ItemManager.createItem()`:
1. Determine material type (NETHERITE/DIAMOND/IRON/COPPER/STONE/WOOD/LEATHER)
2. Set base stats based on type and item category (SWORD/AXE/PICKAXE/HELMET/etc)
3. Apply quality multiplier: `stats[key] = baseValue * quality.statMultiplier`
4. Store quality in PDC, build lore via `CustomItem.buildItem()`

### Adding New Managers
Standard pattern (see any `*Manager.kt`):
```kotlin
class MyManager(private val plugin: SurvivalPlus) {
    // Initialize in SurvivalPlus.kt:
    lateinit var myManager: MyManager
        private set
    // In onEnable(): myManager = MyManager(this)
}
```

### Custom Enchantments
Defined in `enchantments/CustomEnchantment.kt` enum. Implementation:
1. Add enum entry with maxLevel, cooldown, applicable types
2. Handle in `EnchantmentListener.kt` event handlers (use `cooldownManager.isOnCooldown()`)
3. Update lore automatically via `EnchantmentManager.updateLore()`

**Balance rules** (per `RELEASE_NOTES_v1.2.0.md`):
- Vein Miner: max 32 blocks (not 64)
- Timber: max 64 blocks (not 128)
- Explosive/Thunder Strike: 5-8s cooldowns required

## Configuration & Conventions

### Config Structure (`src/main/resources/config.yml`)
- `enabled-worlds`: Plugin only active in these worlds (default: Survival + nether/end)
- `drop-chances`: Override quality weights per tier
- `reforging-costs`: Materials needed per tier (3/5/7)
- `shrines`: World Tier shrine generation settings (auto-generate: true)

### Commands (`commands/SurvivalPlusCommand.kt`)
Main command `/sp` with subcommands:
- `give <player> <material> [quality]` - Create custom item
- `giveblock <player> <blocktype>` - Custom blocks (custom_anvil, reforging_station)
- `enchant <enchantment> <level>` - Apply custom enchantment to held item
- `worldtier [set|info]` - Manage world difficulty tiers
- Tab completion implemented in `onTabComplete()`

### File Organization
```
src/main/kotlin/org/bysenom/survivalPlus/
├── blocks/         - Custom placeable blocks (armor stand visualization)
├── combat/         - Dodge/shield mechanics
├── crafting/       - Custom crafting GUI
├── enchantments/   - Custom enchantment system (12 enchants)
├── events/         - World events (5 types)
├── gui/            - Inventory-based interfaces
├── listeners/      - Event handlers
├── managers/       - Core business logic (ItemManager, ReforgingManager)
├── mobs/           - SpecialMobs + ButcherBoss
├── models/         - Data classes (Quality, CustomItem, ReforgingTier)
├── sets/           - Armor set bonuses
├── worldtier/      - World difficulty system
└── SurvivalPlus.kt - Main plugin class, manager initialization
```

## Project-Specific Patterns

### German Language
All display text in German (displayName fields, lore, messages). Example:
- Quality.COMMON.displayName = "Gewoehnlich" (not "Common")
- Use umlauts: ö→oe, ü→ue, ä→ae in code identifiers

### Custom Block Implementation
Uses armor stands + invisible entities for visualization:
1. Place armor stand at block center (`y + 0.5`)
2. Store block data in CustomBlockManager with location key
3. Listen for right-click (PlayerInteractEvent) to open GUI
4. On break: remove armor stand, drop item, clear storage

### Mining Speed System
Quality-based Haste effects applied in equipment listeners:
- Common: no bonus
- Uncommon→Mythic: Haste I-V (20% increments)
- Legendary/Mythic: 10-20% instamine chance (see `ItemListener.kt`)

### World Tier Integration
Mobs/drops scale with world tier (configurable per world):
- `WorldTierManager.getWorldTier(world)` returns tier (1-5)
- `dropQualityBoost` increases minimum quality tier
- Special mobs spawn 15% more frequently per tier above Normal

## Testing Strategy
See `TEST_GUIDE.md` for manual testing procedures. Key test commands:
```
/sp kit                           # Mythic gear set for testing
/sp give <name> <material> mythic # Specific quality testing
/sp worldtier set <tier>          # Test tier scaling
/sp startevent <event>            # Trigger world events
```

## Important Notes
- **Never** modify PDC keys - breaks existing items in production
- Always apply cooldowns to AOE enchantments (Vein Miner, Timber, Explosive)
- Custom blocks require armor stand cleanup in `onDisable()` (see `SurvivalPlus.kt#onDisable`)
- Plugin incompatible outside `enabled-worlds` - check before item operations
- World Tier shrines auto-generate on world load (max 3 per world, 1200+ blocks apart)

## Documentation Files
- `TODO.md` - Feature roadmap, 948 lines, tracks Phase 2-4 progress
- `FEATURES.md` - Complete system reference with ASCII diagrams
- `CUSTOM_BLOCKS.md` - Custom block system, recipes, GUI details
- `ENTWICKLER_GUIDE.md` - Code templates for common tasks
- `BEGINNER_GUIDE.md` - Player-facing guide
- `BUTCHER_BOSS.md` - Diablo-style boss mechanics documentation
