package org.bysenom.survivalPlus.enchantments

import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.Quality
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.world.LootGenerateEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.VillagerAcquireTradeEvent
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.entity.*
import kotlin.random.Random

/**
 * Listener für natürliche Enchantment-Quellen
 * - Enchanting Table
 * - Fishing
 * - Loot Chests
 * - Mob Drops (Bosses & Special Mobs)
 * - Villager Trades (Librarians)
 * - Mining Rare Ores (Ancient Debris, Diamonds)
 * - Raid Rewards
 */
class EnchantmentSourceListener(private val plugin: SurvivalPlus) : Listener {

    /**
     * Enchanting Table - Chance auf Custom Enchantments
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEnchant(event: EnchantItemEvent) {
        val player = event.enchanter
        val item = event.item
        val expLevelCost = event.expLevelCost

        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(player.world)) return

        // Chance auf Custom Enchantment basierend auf Level
        val customEnchantChance = when {
            expLevelCost >= 30 -> 0.50  // Level 30: 50% Chance
            expLevelCost >= 20 -> 0.30  // Level 20: 30% Chance
            expLevelCost >= 10 -> 0.15  // Level 10: 15% Chance
            else -> 0.05                 // Level 1-9: 5% Chance
        }

        if (Random.nextDouble() < customEnchantChance) {
            // Bestimme Qualität basierend auf Level
            val quality = when {
                expLevelCost >= 30 && Random.nextDouble() < 0.05 -> Quality.MYTHIC
                expLevelCost >= 30 && Random.nextDouble() < 0.15 -> Quality.LEGENDARY
                expLevelCost >= 20 && Random.nextDouble() < 0.25 -> Quality.EPIC
                expLevelCost >= 10 -> Quality.RARE
                else -> Quality.UNCOMMON
            }

            // Prüfe ItemType und hole anwendbare Enchantments
            val itemType = ItemType.fromMaterial(item.type)
            if (itemType != null) {
                val applicableEnchants = CustomEnchantment.getApplicableEnchantments(itemType, quality)
                    .filter { it.isApplicable(itemType) }

                if (applicableEnchants.isNotEmpty()) {
                    val enchant = applicableEnchants.random()
                    val level = Random.nextInt(1, enchant.maxLevel + 1)

                    // Verzögere das Hinzufügen um 1 Tick, damit Vanilla zuerst fertig ist
                    plugin.server.scheduler.runTaskLater(plugin, Runnable {
                        try {
                            plugin.enchantmentManager.addEnchantment(item, enchant, level)
                            player.sendMessage("§d✨ ${enchant.displayName} ${toRomanNumeral(level)} wurde hinzugefügt!")
                            player.playSound(player.location, org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.5f)
                            plugin.logger.info("[Enchant] ${player.name}: ${enchant.displayName} ${level} via Enchanter (Cost $expLevelCost)")
                        } catch (ex: Exception) {
                            plugin.logger.warning("[Enchant] Fehler beim Hinzufügen des Custom-Enchants: ${ex.message}")
                        }
                    }, 1L)
                } else {
                    plugin.logger.fine("[Enchant] Keine anwendbaren Custom-Enchants für ${item.type} und Qualität ${quality.name}")
                }
            } else {
                plugin.logger.fine("[Enchant] ItemType für ${item.type} unbekannt – kein Custom-Enchant")
            }
        } else {
            plugin.logger.fine("[Enchant] Chance verfehlt bei LevelCost $expLevelCost – kein Custom-Enchant")
        }
    }

    /**
     * Konvertiert Zahl zu römischen Ziffern für Anzeige
     */
    private fun toRomanNumeral(num: Int): String {
        return when (num) {
            1 -> "I"
            2 -> "II"
            3 -> "III"
            4 -> "IV"
            5 -> "V"
            6 -> "VI"
            7 -> "VII"
            8 -> "VIII"
            9 -> "IX"
            10 -> "X"
            else -> num.toString()
        }
    }

    /**
     * Fishing - Chance auf Enchanted Books
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onFish(event: PlayerFishEvent) {
        if (event.state != PlayerFishEvent.State.CAUGHT_FISH) return

        val player = event.player

        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(player.world)) return

        val caught = event.caught as? org.bukkit.entity.Item ?: return
        val item = caught.itemStack

        // Nur bei Büchern oder Treasure
        if (item.type != Material.BOOK && item.type != Material.ENCHANTED_BOOK) return

        // World Tier beeinflusst Chance
        val worldTier = plugin.worldTierManager.getWorldTier(player.world)
        val baseChance = 0.05 + (worldTier.tier * 0.02) // 5% + 2% pro Tier

        if (Random.nextDouble() < baseChance) {
            // Erstelle Custom Enchanted Book
            val quality = when {
                Random.nextDouble() < 0.01 -> Quality.MYTHIC
                Random.nextDouble() < 0.05 -> Quality.LEGENDARY
                Random.nextDouble() < 0.15 -> Quality.EPIC
                Random.nextDouble() < 0.30 -> Quality.RARE
                else -> Quality.UNCOMMON
            }

            val enchantedBook = createEnchantedBook(quality)
            if (enchantedBook != null) {
                caught.itemStack = enchantedBook
                player.sendMessage("§b🎣 Du hast ein ${quality.displayName}§b Enchanted Book gefangen!")
                player.playSound(player.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f)
            }
        }
    }

    /**
     * Loot Chests - Custom Enchanted Books in Dungeon-Loot
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onLootGenerate(event: LootGenerateEvent) {
        val world = event.world

        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(world)) return

        val lootTable = event.lootTable

        // Nur für Dungeon/Structure Loot
        val key = lootTable.key.toString()

        // Trial Chambers - Spezielle Behandlung!
        val isTrialChamber = key.contains("trial_chamber") || key.contains("trial_spawner") ||
                             key.contains("vault") || key.contains("ominous")

        if (!key.contains("chest") && !key.contains("dungeon") && !key.contains("stronghold") &&
            !key.contains("mansion") && !key.contains("fortress") && !key.contains("end_city") && !isTrialChamber) {
            return
        }

        // World Tier beeinflusst Qualität
        val worldTier = plugin.worldTierManager.getWorldTier(world)

        // Trial Chambers haben höhere Chancen!
        val baseChance = if (isTrialChamber) 0.40 else 0.15  // 40% für Trial Chambers, 15% für normale
        val bookChance = baseChance + (worldTier.tier * 0.05) // + 5% pro Tier

        if (Random.nextDouble() < bookChance) {
            // Trial Chambers haben bessere Qualitäten
            val quality = if (isTrialChamber) {
                when {
                    worldTier.tier >= 5 && Random.nextDouble() < 0.20 -> Quality.MYTHIC  // 20% für Mythic
                    worldTier.tier >= 4 && Random.nextDouble() < 0.35 -> Quality.LEGENDARY
                    worldTier.tier >= 3 && Random.nextDouble() < 0.40 -> Quality.EPIC
                    worldTier.tier >= 2 -> Quality.RARE
                    else -> Quality.UNCOMMON
                }
            } else {
                when {
                    worldTier.tier >= 5 && Random.nextDouble() < 0.10 -> Quality.MYTHIC
                    worldTier.tier >= 4 && Random.nextDouble() < 0.20 -> Quality.LEGENDARY
                    worldTier.tier >= 3 && Random.nextDouble() < 0.30 -> Quality.EPIC
                    worldTier.tier >= 2 -> Quality.RARE
                    else -> Quality.UNCOMMON
                }
            }

            val enchantedBook = createEnchantedBook(quality)
            if (enchantedBook != null) {
                event.loot.add(enchantedBook)
                val source = if (isTrialChamber) "Trial Chamber" else "Dungeon"
                plugin.logger.fine("Custom Enchanted Book (${quality.displayName}) zu $source Loot hinzugefügt: $key")
            }
        }
    }

    /**
     * Erstellt ein Custom Enchanted Book mit zufälligem Enchantment
     */
    private fun createEnchantedBook(quality: Quality): ItemStack? {
        val applicableEnchants = CustomEnchantment.entries.filter { it.minQuality.tier <= quality.tier }
        if (applicableEnchants.isEmpty()) return null

        val enchant = applicableEnchants.random()
        val level = Random.nextInt(1, enchant.maxLevel + 1)

        val book = ItemStack(Material.ENCHANTED_BOOK)
        plugin.enchantmentManager.addEnchantment(book, enchant, level)

        return book
    }

    /**
     * Boss & Special Mob Drops - Hochwertige Custom Enchanted Books
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onMobDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val world = entity.world

        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(world)) return

        val killer = entity.killer ?: return
        val worldTier = plugin.worldTierManager.getWorldTier(world)

        // Prüfe ob Mob aus Trial Spawner kommt
        val isTrialMob = entity.scoreboardTags.contains("trial_spawner") ||
                         entity.scoreboardTags.contains("trial_chamber_mob") ||
                         entity.scoreboardTags.contains("ominous_trial")

        // Bestimme Drop-Chance basierend auf Mob-Typ
        val (baseChance, minQuality) = when {
            // Trial Chamber Mobs - Erhöhte Chance!
            isTrialMob -> Pair(0.25, Quality.RARE)  // 25% Base-Chance, mindestens Rare

            // Bosses - Sehr hohe Chance auf gute Enchants
            entity.type == EntityType.ENDER_DRAGON -> Pair(0.95, Quality.LEGENDARY)
            entity.type == EntityType.WITHER -> Pair(0.90, Quality.LEGENDARY)
            entity.type == EntityType.WARDEN -> Pair(0.85, Quality.EPIC)

            // Mini-Bosses
            entity.type == EntityType.ELDER_GUARDIAN -> Pair(0.50, Quality.EPIC)

            // Starke Mobs
            entity.type == EntityType.EVOKER -> Pair(0.30, Quality.RARE)
            entity.type == EntityType.RAVAGER -> Pair(0.25, Quality.RARE)
            entity.type == EntityType.PIGLIN_BRUTE -> Pair(0.20, Quality.RARE)
            entity.type == EntityType.VINDICATOR -> Pair(0.15, Quality.UNCOMMON)

            // Spezielle Mobs
            entity.type == EntityType.SHULKER -> Pair(0.10, Quality.RARE)
            entity.type == EntityType.BLAZE -> Pair(0.08, Quality.UNCOMMON)
            entity.type == EntityType.ENDERMAN -> Pair(0.05, Quality.UNCOMMON)

            else -> {
                // Special Mobs aus unserem System
                if (entity.scoreboardTags.contains("special_mob")) {
                    Pair(0.40, Quality.EPIC)
                } else {
                    return
                }
            }
        }

        // World Tier erhöht die Chance
        val finalChance = baseChance + (worldTier.tier * 0.05)

        if (Random.nextDouble() < finalChance) {
            // Bestimme Qualität (mindestens minQuality)
            val quality = when {
                worldTier.tier >= 5 && Random.nextDouble() < 0.15 -> Quality.MYTHIC
                worldTier.tier >= 4 && Random.nextDouble() < 0.30 -> Quality.LEGENDARY
                worldTier.tier >= 3 && Random.nextDouble() < 0.40 -> Quality.EPIC
                else -> maxOf(minQuality, Quality.RARE)
            }

            val enchantedBook = createEnchantedBook(quality)
            if (enchantedBook != null) {
                event.drops.add(enchantedBook)

                val source = if (isTrialMob) "Trial Mob" else entity.type.name
                killer.sendMessage("§d💀 $source hat ein ${quality.displayName}§d Enchanted Book gedroppt!")
                killer.playSound(killer.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f)
                plugin.logger.info("[Enchant] ${killer.name} erhielt ${quality.displayName} Book von $source")
            }
        }
    }

    /**
     * Villager Trading - Librarians können Custom Enchanted Books anbieten
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onVillagerTrade(event: VillagerAcquireTradeEvent) {
        val villager = event.entity
        val world = villager.world

        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(world)) return

        // Nur Librarians
        if (villager is Villager && villager.profession != Villager.Profession.LIBRARIAN) return

        val recipe = event.recipe
        val result = recipe.result

        // Nur bei Enchanted Books
        if (result.type != Material.ENCHANTED_BOOK) return

        val worldTier = plugin.worldTierManager.getWorldTier(world)

        // Chance auf Custom Enchantment basierend auf Villager-Level
        val villagerLevel = if (villager is Villager) villager.villagerLevel else 1
        val customChance = when {
            villagerLevel == 5 -> 0.40  // Master: 40%
            villagerLevel == 4 -> 0.25  // Expert: 25%
            villagerLevel == 3 -> 0.15  // Journeyman: 15%
            villagerLevel == 2 -> 0.08  // Apprentice: 8%
            else -> 0.03                // Novice: 3%
        }

        if (Random.nextDouble() < customChance) {
            val quality = when {
                villagerLevel == 5 && worldTier.tier >= 4 && Random.nextDouble() < 0.10 -> Quality.LEGENDARY
                villagerLevel >= 4 && worldTier.tier >= 3 && Random.nextDouble() < 0.20 -> Quality.EPIC
                villagerLevel >= 3 && Random.nextDouble() < 0.40 -> Quality.RARE
                else -> Quality.UNCOMMON
            }

            val customBook = createEnchantedBook(quality)
            if (customBook != null) {
                // Ersetze das normale Buch durch Custom-Buch
                val newRecipe = org.bukkit.inventory.MerchantRecipe(
                    customBook,
                    recipe.uses,
                    recipe.maxUses,
                    recipe.hasExperienceReward(),
                    recipe.villagerExperience,
                    recipe.priceMultiplier
                )
                newRecipe.ingredients = recipe.ingredients

                // Ersetzt das Original-Trade
                event.recipe = newRecipe

                plugin.logger.info("[Enchant] Librarian (Lvl $villagerLevel) bietet Custom ${quality.displayName} Book an")
            }
        }
    }

    /**
     * Mining - Sehr seltene Chance beim Abbau von wertvollen Erzen
     */
    /// @EventHandler(priority = EventPriority.NORMAL)
    ///fun onBlockBreak(event: BlockBreakEvent) {
    ///    val player = event.player
    ///    val block = event.block
    ///     val world = player.world

        // Nur in aktivierten Welten
    ///    if (!plugin.worldTierManager.isEnabledWorld(world)) return

        // Nur bestimmte wertvolle Erze
    ///   val (baseChance, minQuality) = when (block.type) {
    ///        Material.ANCIENT_DEBRIS -> Pair(0.05, Quality.LEGENDARY)  // 5% Chance
    ///       Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE -> Pair(0.02, Quality.EPIC)  // 2% Chance
    ///       Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE -> Pair(0.03, Quality.RARE)  // 3% Chance
    ///       Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE -> Pair(0.01, Quality.RARE)  // 1% Chance
    ///       else -> return
    ///   }

    ///   val worldTier = plugin.worldTierManager.getWorldTier(world)
    ///   val finalChance = baseChance + (worldTier.tier * 0.005)  // Kleiner Bonus durch World Tier

    ///   if (Random.nextDouble() < finalChance) {
    ///       val quality = when {
    ///           block.type == Material.ANCIENT_DEBRIS && Random.nextDouble() < 0.20 -> Quality.MYTHIC
    ///           worldTier.tier >= 4 && Random.nextDouble() < 0.15 -> Quality.LEGENDARY
    ///           worldTier.tier >= 3 && Random.nextDouble() < 0.30 -> Quality.EPIC
    ///           else -> minQuality
    ///       }

    ///       val enchantedBook = createEnchantedBook(quality)
    ///       if (enchantedBook != null) {
    ///           world.dropItemNaturally(block.location, enchantedBook)
    ///           player.sendMessage("§e⛏ Ein ${quality.displayName}§e Enchanted Book wurde im Gestein gefunden!")
    ///           player.playSound(player.location, org.bukkit.Sound.ENTITY_ITEM_PICKUP, 1f, 1.5f)
    ///           player.playSound(player.location, org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.7f, 1.2f)
    ///           plugin.logger.info("[Enchant] ${player.name} fand ${quality.displayName} Book beim Mining (${block.type})")
    ///       }
    ///   }
///}

    /**
     * Raid Victory - Belohnung für erfolgreiches Verteidigen
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onRaidComplete(event: org.bukkit.event.raid.RaidFinishEvent) {
        val raid = event.raid
        val world = event.world

        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(world)) return

        val worldTier = plugin.worldTierManager.getWorldTier(world)

        // Höheres World Tier = bessere Belohnung
        val bookChance = 0.60 + (worldTier.tier * 0.08)  // 60% - 100% Chance

        // Belohne nahe Spieler (typischerweise die Raid-Teilnehmer)
        val nearbyPlayers = world.players.filter { player ->
            raid.location.distance(player.location) < 200  // 200 Blöcke Radius
        }

        for (player in nearbyPlayers) {
            if (Random.nextDouble() < bookChance) {
                val quality = when {
                    worldTier.tier >= 5 && Random.nextDouble() < 0.20 -> Quality.MYTHIC
                    worldTier.tier >= 4 && Random.nextDouble() < 0.35 -> Quality.LEGENDARY
                    worldTier.tier >= 3 && Random.nextDouble() < 0.40 -> Quality.EPIC
                    worldTier.tier >= 2 -> Quality.RARE
                    else -> Quality.UNCOMMON
                }

                val enchantedBook = createEnchantedBook(quality)
                if (enchantedBook != null) {
                    // Füge direkt zum Inventar hinzu oder droppe es
                    if (player.inventory.firstEmpty() != -1) {
                        player.inventory.addItem(enchantedBook)
                    } else {
                        world.dropItemNaturally(player.location, enchantedBook)
                    }

                    player.sendMessage("§6🛡 Raid-Belohnung: ${quality.displayName}§6 Enchanted Book!")
                    player.playSound(player.location, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
                    plugin.logger.info("[Enchant] ${player.name} erhielt ${quality.displayName} Book als Raid-Belohnung")
                }
            }
        }
    }

    /**
     * Piglin Bartering - Chance auf Custom Enchanted Books
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onPiglinBarter(event: org.bukkit.event.entity.PiglinBarterEvent) {
        val piglin = event.entity
        val world = piglin.world

        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(world)) return

        val worldTier = plugin.worldTierManager.getWorldTier(world)

        // Kleine Chance auf Custom Enchanted Book als zusätzliches Loot
        val bookChance = 0.05 + (worldTier.tier * 0.02)  // 5% + 2% pro Tier

        if (Random.nextDouble() < bookChance) {
            val quality = when {
                worldTier.tier >= 5 && Random.nextDouble() < 0.05 -> Quality.LEGENDARY
                worldTier.tier >= 4 && Random.nextDouble() < 0.15 -> Quality.EPIC
                worldTier.tier >= 3 && Random.nextDouble() < 0.30 -> Quality.RARE
                else -> Quality.UNCOMMON
            }

            val enchantedBook = createEnchantedBook(quality)
            if (enchantedBook != null) {
                // Füge Book zum Outcome hinzu
                event.outcome.add(enchantedBook)

                plugin.logger.fine("[Enchant] Piglin Barter: ${quality.displayName} Book hinzugefügt")
            }
        }
    }


    /**
     * Konduit-Aktivierung - Sehr seltene Chance beim Aktivieren eines Conduits
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onConduitEffect(event: org.bukkit.event.entity.EntityPotionEffectEvent) {
        val entity = event.entity as? Player ?: return
        val world = entity.world

        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(world)) return

        // Nur Conduit Power Effect
        if (event.newEffect?.type != org.bukkit.potion.PotionEffectType.CONDUIT_POWER) return
        if (event.cause != org.bukkit.event.entity.EntityPotionEffectEvent.Cause.CONDUIT) return

        val worldTier = plugin.worldTierManager.getWorldTier(world)

        // Sehr seltene Chance (einmalig pro Conduit-Session)
        val bookChance = 0.05 + (worldTier.tier * 0.01)  // 5% - 10%

        if (Random.nextDouble() < bookChance) {
            val quality = when {
                worldTier.tier >= 5 && Random.nextDouble() < 0.15 -> Quality.MYTHIC
                worldTier.tier >= 4 && Random.nextDouble() < 0.30 -> Quality.LEGENDARY
                worldTier.tier >= 3 -> Quality.EPIC
                else -> Quality.RARE
            }

            val enchantedBook = createEnchantedBook(quality)
            if (enchantedBook != null) {
                if (entity.inventory.firstEmpty() != -1) {
                    entity.inventory.addItem(enchantedBook)
                } else {
                    world.dropItemNaturally(entity.location, enchantedBook)
                }

                entity.sendMessage("§3🌊 Die Kraft des Conduits offenbart ein ${quality.displayName}§3 Enchanted Book!")
                entity.playSound(entity.location, org.bukkit.Sound.BLOCK_CONDUIT_ACTIVATE, 1f, 1.5f)
                plugin.logger.info("[Enchant] ${entity.name} erhielt ${quality.displayName} Book durch Conduit")
            }
        }
    }

    /**
     * Enderman Teleport Kill - Seltene Chance wenn man Enderman während Teleport tötet
     */
    @EventHandler(priority = EventPriority.NORMAL)
    fun onEndermanDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val world = entity.world

        // Nur in aktivierten Welten
        if (!plugin.worldTierManager.isEnabledWorld(world)) return

        // Nur Enderman
        if (entity.type != EntityType.ENDERMAN) return

        val killer = entity.killer ?: return
        val worldTier = plugin.worldTierManager.getWorldTier(world)

        // Prüfe ob Enderman gerade teleportiert hat (ist aggressiv)
        val enderman = entity as org.bukkit.entity.Enderman
        if (!enderman.isScreaming) return  // War nicht aggressiv/teleporting

        // Mittlere Chance auf Book
        val bookChance = 0.15 + (worldTier.tier * 0.03)  // 15% - 30%

        if (Random.nextDouble() < bookChance) {
            val quality = when {
                worldTier.tier >= 5 && Random.nextDouble() < 0.20 -> Quality.MYTHIC
                worldTier.tier >= 4 && Random.nextDouble() < 0.35 -> Quality.LEGENDARY
                worldTier.tier >= 3 -> Quality.EPIC
                else -> Quality.RARE
            }

            val enchantedBook = createEnchantedBook(quality)
            if (enchantedBook != null) {
                event.drops.add(enchantedBook)
                killer.sendMessage("§5👁 Der teleportierende Enderman hat ein ${quality.displayName}§5 Enchanted Book gedroppt!")
                killer.playSound(killer.location, org.bukkit.Sound.ENTITY_ENDERMAN_DEATH, 1f, 0.8f)
                plugin.logger.info("[Enchant] ${killer.name} erhielt ${quality.displayName} Book von teleportierendem Enderman")
            }
        }
    }
}

