package org.bysenom.survivalPlus

import org.bysenom.survivalPlus.commands.SurvivalPlusCommand
import org.bysenom.survivalPlus.display.MessageManager
import org.bysenom.survivalPlus.display.ProgressBarManager
import org.bysenom.survivalPlus.display.QualityPlateManager
import org.bysenom.survivalPlus.effects.ParticleEffectManager
import org.bysenom.survivalPlus.effects.SoundManager
import org.bysenom.survivalPlus.enchantments.EnchantmentListener
import org.bysenom.survivalPlus.enchantments.EnchantmentManager
import org.bysenom.survivalPlus.gui.ReforgingGUI
import org.bysenom.survivalPlus.listeners.ItemDropListener
import org.bysenom.survivalPlus.listeners.ItemListener
import org.bysenom.survivalPlus.listeners.ReforgingGUIListener
import org.bysenom.survivalPlus.managers.ConfigCacheManager
import org.bysenom.survivalPlus.managers.ItemManager
import org.bysenom.survivalPlus.managers.ReforgingManager
import org.bukkit.plugin.java.JavaPlugin
import org.bysenom.survivalPlus.sets.SetBonusListener
import org.bysenom.survivalPlus.sets.SetBonusManager
import org.bysenom.survivalPlus.skills.SkillListener
import org.bysenom.survivalPlus.skills.SkillManager
import org.bysenom.survivalPlus.crafting.CustomCraftingGUI
import org.bysenom.survivalPlus.crafting.CustomCraftingListener
import org.bysenom.survivalPlus.blocks.CustomBlockManager
import org.bysenom.survivalPlus.blocks.CustomBlockRecipes
import org.bysenom.survivalPlus.blocks.CustomBlockListener
import org.bysenom.survivalPlus.worldtier.WorldTierManager
import org.bysenom.survivalPlus.mobs.SpecialMobManager
import org.bysenom.survivalPlus.mobs.SpecialMobListener
import org.bysenom.survivalPlus.events.WorldEventManager
import org.bysenom.survivalPlus.worldtier.WorldTierListener

class SurvivalPlus : JavaPlugin() {

    // Manager Instanzen
    lateinit var configCacheManager: ConfigCacheManager
        private set

    lateinit var itemManager: ItemManager
        private set

    lateinit var reforgingManager: ReforgingManager
        private set

    lateinit var reforgingGUI: ReforgingGUI
        private set

    lateinit var particleEffectManager: ParticleEffectManager
        private set

    lateinit var soundManager: SoundManager
        private set

    lateinit var messageManager: MessageManager
        private set

    lateinit var qualityPlateManager: QualityPlateManager
        private set

    lateinit var enchantmentManager: EnchantmentManager
        private set

    lateinit var setBonusManager: SetBonusManager
        private set

    lateinit var progressBarManager: ProgressBarManager
        private set


    lateinit var skillManager: SkillManager
        private set

    lateinit var customCraftingGUI: CustomCraftingGUI
        private set

    lateinit var customBlockManager: CustomBlockManager
        private set

    lateinit var customBlockRecipes: CustomBlockRecipes
        private set

    lateinit var worldTierManager: WorldTierManager
        private set

    lateinit var worldTierGUI: org.bysenom.survivalPlus.worldtier.WorldTierGUI
        private set

    lateinit var specialMobManager: SpecialMobManager
        private set

    lateinit var butcherBoss: org.bysenom.survivalPlus.mobs.ButcherBoss
        private set

    lateinit var harvesterBoss: org.bysenom.survivalPlus.mobs.HarvesterBoss
        private set

    lateinit var frostTitanBoss: org.bysenom.survivalPlus.mobs.FrostTitanBoss
        private set

    lateinit var worldEventManager: WorldEventManager
        private set

    lateinit var shrineManager: org.bysenom.survivalPlus.structures.ShrineManager
        private set

    private var shrineProximityTask: org.bysenom.survivalPlus.structures.ShrineProximityTask? = null

    lateinit var scoreboardManager: org.bysenom.survivalPlus.scoreboard.ScoreboardManager
        private set

    lateinit var skillDataManager: org.bysenom.survivalPlus.managers.SkillDataManager
        private set

    lateinit var achievementManager: org.bysenom.survivalPlus.achievements.AchievementManager
        private set

    lateinit var skillsGUI: org.bysenom.survivalPlus.gui.SkillsGUI
        private set

    lateinit var achievementsGUI: org.bysenom.survivalPlus.gui.AchievementsGUI
        private set

    lateinit var tradeManager: org.bysenom.survivalPlus.trading.TradeManager
        private set

    lateinit var tradingGUI: org.bysenom.survivalPlus.trading.TradingGUI
        private set

    lateinit var customOreManager: org.bysenom.survivalPlus.generation.CustomOreManager
        private set

    lateinit var worldBossArenaManager: org.bysenom.survivalPlus.bosses.WorldBossArenaManager
        private set

    var titanGolemBoss: org.bysenom.survivalPlus.bosses.TitanGolemBoss? = null
        private set

    override fun onEnable() {
        // ASCII Art Logo
        logger.info("  ____                  _            _ ____  _           ")
        logger.info(" / ___| _   _ _ ____   _(_)_   ____ _| |  _ \\| |_   _ ___ ")
        logger.info(" \\___ \\| | | | '__\\ \\ / / \\ \\ / / _` | | |_) | | | | / __|")
        logger.info("  ___) | |_| | |   \\ V /| |\\ V / (_| | |  __/| | |_| \\__ \\")
        logger.info(" |____/ \\__,_|_|    \\_/ |_| \\_/ \\__,_|_|_|   |_|\\__,_|___/")
        logger.info("")

        // Config erstellen/laden
        saveDefaultConfig()

        // Manager initialisieren (ConfigCache zuerst!)
        configCacheManager = ConfigCacheManager(this)
        itemManager = ItemManager(this)
        reforgingManager = ReforgingManager(this)
        reforgingGUI = ReforgingGUI(this)
        particleEffectManager = ParticleEffectManager(this)
        soundManager = SoundManager(this)
        messageManager = MessageManager(this)
        qualityPlateManager = QualityPlateManager(this)
        enchantmentManager = EnchantmentManager(this)
        setBonusManager = SetBonusManager(this)
        progressBarManager = ProgressBarManager(this)
        skillManager = SkillManager(this)
        customCraftingGUI = CustomCraftingGUI(this)
        customBlockManager = CustomBlockManager(this)
        customBlockRecipes = CustomBlockRecipes(this)
        worldTierManager = WorldTierManager(this)
        worldTierGUI = org.bysenom.survivalPlus.worldtier.WorldTierGUI(this)
        specialMobManager = SpecialMobManager(this)
        butcherBoss = org.bysenom.survivalPlus.mobs.ButcherBoss(this)
        harvesterBoss = org.bysenom.survivalPlus.mobs.HarvesterBoss(this)
        frostTitanBoss = org.bysenom.survivalPlus.mobs.FrostTitanBoss(this)
        worldEventManager = WorldEventManager(this)
        shrineManager = org.bysenom.survivalPlus.structures.ShrineManager(this)
        scoreboardManager = org.bysenom.survivalPlus.scoreboard.ScoreboardManager(this)
        skillDataManager = org.bysenom.survivalPlus.managers.SkillDataManager(this)
        achievementManager = org.bysenom.survivalPlus.achievements.AchievementManager(this)
        skillsGUI = org.bysenom.survivalPlus.gui.SkillsGUI(this)
        achievementsGUI = org.bysenom.survivalPlus.gui.AchievementsGUI(this)
        tradeManager = org.bysenom.survivalPlus.trading.TradeManager(this)
        tradingGUI = org.bysenom.survivalPlus.trading.TradingGUI(this)
        customOreManager = org.bysenom.survivalPlus.generation.CustomOreManager(this)
        worldBossArenaManager = org.bysenom.survivalPlus.bosses.WorldBossArenaManager(this)
        titanGolemBoss = org.bysenom.survivalPlus.bosses.TitanGolemBoss(this)

        // Commands registrieren
        val command = getCommand("survivalplus")
        val commandHandler = SurvivalPlusCommand(this)
        command?.setExecutor(commandHandler)
        command?.tabCompleter = commandHandler

        // Listener registrieren
        server.pluginManager.registerEvents(ItemListener(this), this)
        server.pluginManager.registerEvents(ReforgingGUIListener(this), this)
        server.pluginManager.registerEvents(ItemDropListener(this), this)
        val enchantmentListener = EnchantmentListener(this)
        server.pluginManager.registerEvents(enchantmentListener, this)
        server.pluginManager.registerEvents(org.bysenom.survivalPlus.enchantments.EnchantmentLoreUpdateListener(this), this)
        server.pluginManager.registerEvents(org.bysenom.survivalPlus.combat.CombatListener(this), this)
        server.pluginManager.registerEvents(SetBonusListener(this), this)
        server.pluginManager.registerEvents(SkillListener(this), this)
        server.pluginManager.registerEvents(CustomCraftingListener(this), this)
        server.pluginManager.registerEvents(CustomBlockListener(this), this)
        server.pluginManager.registerEvents(SpecialMobListener(this), this)
        server.pluginManager.registerEvents(org.bysenom.survivalPlus.mobs.ButcherListener(this), this)
        server.pluginManager.registerEvents(WorldTierListener(this), this)
        server.pluginManager.registerEvents(org.bysenom.survivalPlus.worldtier.WorldTierGUIListener(this), this)
        server.pluginManager.registerEvents(org.bysenom.survivalPlus.structures.ShrineListener(this), this)
        server.pluginManager.registerEvents(org.bysenom.survivalPlus.scoreboard.ScoreboardListener(this), this)
        server.pluginManager.registerEvents(org.bysenom.survivalPlus.listeners.PortalListener(this), this)
        server.pluginManager.registerEvents(org.bysenom.survivalPlus.listeners.MiningSpeedListener(this), this)
        server.pluginManager.registerEvents(org.bysenom.survivalPlus.enchantments.EnchantmentSourceListener(this), this)
        server.pluginManager.registerEvents(qualityPlateManager, this) // Für ChunkUnload Events
        server.pluginManager.registerEvents(org.bysenom.survivalPlus.listeners.PlayerDataListener(this), this)
        server.pluginManager.registerEvents(org.bysenom.survivalPlus.achievements.AchievementListener(this), this)
        server.pluginManager.registerEvents(skillsGUI, this)
        server.pluginManager.registerEvents(achievementsGUI, this)
        server.pluginManager.registerEvents(tradingGUI, this)
        server.pluginManager.registerEvents(org.bysenom.survivalPlus.trading.TradingListener(this), this)
        server.pluginManager.registerEvents(customOreManager, this) // Ore Generation

        // Rezepte registrieren
        customBlockRecipes.registerRecipes()

        // Starte Systeme
        worldEventManager.start()
        scoreboardManager.start()
        worldBossArenaManager.initialize()
        customOreManager.logGenerationInfo()

        // Starte Shrine Proximity Task (wenn aktiviert)
        if (config.getBoolean("shrines.proximity-check", true)) {
            shrineProximityTask = org.bysenom.survivalPlus.structures.ShrineProximityTask(this)
            val interval = config.getLong("shrines.proximity-interval", 40L)
            shrineProximityTask?.runTaskTimer(this, interval, interval)
            logger.info("✓ Shrine Proximity System aktiviert (Interval: ${interval} Ticks)")
        }

        // Generiere Shrines für bereits geladene Welten (wenn aktiviert)
        if (config.getBoolean("shrines.auto-generate", true)) {
            server.scheduler.runTaskLaterAsynchronously(this, Runnable {
                val targetWorldNames = config.getStringList("shrines.target-worlds")
                    .map { it.lowercase() }

                val targetWorlds = server.worlds.filter { world ->
                    val name = world.name.lowercase()
                    targetWorldNames.contains(name) && worldTierManager.isEnabledWorld(world)
                }

                if (targetWorlds.isEmpty()) {
                    logger.warning("⚠ Keine Welten für Shrine-Generierung gefunden!")
                    logger.warning("  Konfigurierte Welten: $targetWorldNames")
                    logger.warning("  Geladene Welten: ${server.worlds.map { it.name }}")
                    return@Runnable
                }

                logger.info("Generiere Shrines für ${targetWorlds.size} Welt(en): ${targetWorlds.map { it.name }}...")
                targetWorlds.forEach { world ->
                    shrineManager.generateShinesForWorld(world)
                }
                logger.info("✓ Shrine-Generierung abgeschlossen!")
            }, 100L) // 5 Sekunden nach Plugin-Enable
        } else {
            logger.info("ℹ Automatische Shrine-Generierung ist deaktiviert")
        }

        // Cleanup-Task für Special Mobs (alle 5 Minuten)
        server.scheduler.scheduleSyncRepeatingTask(this, {
            specialMobManager.cleanup()
        }, 6000L, 6000L)

        // Cleanup-Task für Enchantment Cooldowns (alle 10 Minuten)
        server.scheduler.scheduleSyncRepeatingTask(this, {
            enchantmentListener.cleanupOldCooldowns()
        }, 12000L, 12000L)

        logger.info("SurvivalPlus v${pluginMeta.version} wurde erfolgreich geladen!")
        logger.info("Entwickelt für Endgame-Content ähnlich wie Tierify")

        // Statistiken
        server.scheduler.runTaskLater(this, Runnable {
            logger.info("=== SurvivalPlus Features ===")
            logger.info("- 6 Qualitätsstufen (Common bis Mythic)")
            logger.info("- 5 World-Tiers (Normal bis Mythic)")
            logger.info("- 7 Special Mob Affixe (Diablo-Style)")
            logger.info("- 5 World Events (Invasionen, Blutmond, etc.)")
            logger.info("- 3 Reforging-Tiers mit verschiedenen Erzen")
            logger.info("- Custom Item & Block System")
            logger.info("- Endgame Content für fortgeschrittene Spieler")
        }, 20L)
    }

    override fun onDisable() {
        logger.info("SurvivalPlus wird deaktiviert...")

        // Cleanup
        if (::qualityPlateManager.isInitialized) {
            qualityPlateManager.shutdown()
        }

        if (::progressBarManager.isInitialized) {
            progressBarManager.removeAllProgressBars()
        }

        if (::customBlockManager.isInitialized) {
            customBlockManager.shutdown()
        }

        if (::worldTierManager.isInitialized) {
            worldTierManager.shutdown()
        }

        if (::butcherBoss.isInitialized) {
            butcherBoss.cleanup()
        }

        if (::harvesterBoss.isInitialized) {
            harvesterBoss.cleanup()
        }

        if (::frostTitanBoss.isInitialized) {
            frostTitanBoss.cleanup()
        }

        titanGolemBoss?.shutdown()

        if (::worldBossArenaManager.isInitialized) {
            worldBossArenaManager.shutdown()
        }

        if (::worldEventManager.isInitialized) {
            worldEventManager.stop()
        }

        if (::shrineManager.isInitialized) {
            shrineManager.shutdown()
        }

        // Shrine Proximity Task stoppen
        shrineProximityTask?.cleanup()

        // Trades abbrechen
        if (::tradeManager.isInitialized) {
            tradeManager.shutdown()
        }

        // Speichere alle Spieler-Daten
        if (::skillDataManager.isInitialized) {
            skillDataManager.saveAll()
        }

        logger.info("SurvivalPlus wurde deaktiviert")
    }
}
