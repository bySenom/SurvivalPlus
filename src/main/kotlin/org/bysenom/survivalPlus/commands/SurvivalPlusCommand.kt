package org.bysenom.survivalPlus.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.Quality
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * Haupt-Command Handler für SurvivalPlus
 */
class SurvivalPlusCommand(private val plugin: SurvivalPlus) : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        // Prüfe ob Spieler in aktivierter Welt ist (außer für Admin-Commands)
        if (sender is Player && args.isNotEmpty()) {
            val adminCommands = listOf("reload", "debug")
            if (!adminCommands.contains(args[0].lowercase())) {
                if (!plugin.worldTierManager.isEnabledWorld(sender.world)) {
                    sender.sendMessage(Component.text("⚠ SurvivalPlus ist nur in Survival-Welten aktiv!")
                        .color(NamedTextColor.RED))
                    return true
                }
            }
        }
        
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "give" -> handleGive(sender, args)
            "giveblock" -> handleGiveBlock(sender, args)
            "givebook" -> handleGiveBook(sender, args)
            "enchant" -> handleEnchant(sender, args)
            "kit" -> handleKit(sender, args)
            "reforge" -> handleReforge(sender, args)
            "craft" -> handleCraft(sender)
            "info" -> handleInfo(sender, args)
            "worldtier" -> handleWorldTier(sender, args)
            "startevent" -> handleStartEvent(sender, args)
            "locate" -> handleLocate(sender, args)
            "shrine" -> handleShrine(sender, args)
            "butcher" -> handleButcher(sender, args)
            "boss" -> handleBoss(sender, args)
            "arena" -> handleArena(sender, args)
            "reload" -> handleReload(sender)
            "debug" -> handleDebug(sender, args)
            "sb" , "scoreboard" -> handleScoreboard(sender, args)
            "skills" -> handleSkills(sender)
            "achievements" -> handleAchievements(sender)
            "trade" -> handleTrade(sender, args)
            else -> sendHelp(sender)
        }

        return true
    }

    private fun handleGive(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("survivalplus.give")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        // /sp give <spieler> <material> [qualität]
        if (args.size < 3) {
            sender.sendMessage(Component.text("Verwendung: /sp give <spieler> <material> [qualität]")
                .color(NamedTextColor.RED))
            return
        }

        val targetPlayer = Bukkit.getPlayer(args[1])
        if (targetPlayer == null) {
            sender.sendMessage(Component.text("Spieler nicht gefunden!")
                .color(NamedTextColor.RED))
            return
        }

        val material = Material.getMaterial(args[2].uppercase())
        if (material == null) {
            sender.sendMessage(Component.text("Ungültiges Material!")
                .color(NamedTextColor.RED))
            return
        }

        val quality = if (args.size >= 4) {
            Quality.fromName(args[3]) ?: Quality.random()
        } else {
            Quality.random()
        }

        val item = plugin.itemManager.createItem(material, quality)
        targetPlayer.inventory.addItem(item)

        sender.sendMessage(Component.text("Item gegeben: ${quality.displayName} ${material.name}")
            .color(NamedTextColor.GREEN))
        targetPlayer.sendMessage(Component.text("Du hast ein ${quality.displayName} Item erhalten!")
            .color(quality.color))
    }

    private fun handleGiveBlock(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("survivalplus.give")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        // /sp giveblock <spieler> <blocktype>
        if (args.size < 3) {
            sender.sendMessage(Component.text("Verwendung: /sp giveblock <spieler> <blocktype>")
                .color(NamedTextColor.RED))
            sender.sendMessage(Component.text("Verfügbare Blocks: custom_anvil, reforging_station")
                .color(NamedTextColor.GRAY))
            return
        }

        val targetPlayer = Bukkit.getPlayer(args[1])
        if (targetPlayer == null) {
            sender.sendMessage(Component.text("Spieler nicht gefunden!")
                .color(NamedTextColor.RED))
            return
        }

        val blockType = try {
            org.bysenom.survivalPlus.blocks.CustomBlock.valueOf(args[2].uppercase())
        } catch (e: IllegalArgumentException) {
            sender.sendMessage(Component.text("Ungültiger Block-Typ!")
                .color(NamedTextColor.RED))
            sender.sendMessage(Component.text("Verfügbare Blocks: custom_anvil, reforging_station")
                .color(NamedTextColor.GRAY))
            return
        }

        val item = blockType.createItem()
        targetPlayer.inventory.addItem(item)

        sender.sendMessage(Component.text("✓ ${blockType.displayName} §aan ${targetPlayer.name} gegeben!")
            .color(NamedTextColor.GREEN))
        targetPlayer.sendMessage(Component.text("✓ Du hast einen ${blockType.displayName} §aerhalten!")
            .color(NamedTextColor.GREEN))
    }

    private fun handleGiveBook(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("survivalplus.givebook")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        // /sp givebook <spieler> [enchantment] [level] [quality]
        if (args.size < 2) {
            sender.sendMessage(Component.text("Verwendung: /sp givebook <spieler> [enchantment] [level] [quality]")
                .color(NamedTextColor.RED))
            sender.sendMessage(Component.text("Beispiel: /sp givebook Steve excavation 3 legendary")
                .color(NamedTextColor.GRAY))
            sender.sendMessage(Component.text("Lasse Enchantment leer für zufälliges Book")
                .color(NamedTextColor.GRAY))
            return
        }

        val targetPlayer = Bukkit.getPlayer(args[1])
        if (targetPlayer == null) {
            sender.sendMessage(Component.text("Spieler nicht gefunden!")
                .color(NamedTextColor.RED))
            return
        }

        // Zufälliges Book wenn keine Parameter
        if (args.size == 2) {
            val quality = Quality.random()
            val enchantedBook = createRandomEnchantedBook(quality)
            targetPlayer.inventory.addItem(enchantedBook)

            sender.sendMessage(Component.text("✓ ${quality.displayName} §aEnchanted Book an ${targetPlayer.name} gegeben!")
                .color(NamedTextColor.GREEN))
            targetPlayer.sendMessage(Component.text("✓ Du hast ein ${quality.displayName} §aEnchanted Book erhalten!")
                .color(NamedTextColor.GREEN))
            return
        }

        // Spezifisches Enchantment
        val enchantmentName = args[2].uppercase()
        val customEnchantment = try {
            org.bysenom.survivalPlus.enchantments.CustomEnchantment.valueOf(enchantmentName)
        } catch (e: IllegalArgumentException) {
            sender.sendMessage(Component.text("Ungültiges Enchantment!")
                .color(NamedTextColor.RED))
            sender.sendMessage(Component.text("Verfügbare: ${org.bysenom.survivalPlus.enchantments.CustomEnchantment.entries.joinToString(", ") { it.name.lowercase() }}")
                .color(NamedTextColor.GRAY))
            return
        }

        val level = if (args.size >= 4) {
            args[3].toIntOrNull()?.coerceIn(1, customEnchantment.maxLevel) ?: 1
        } else {
            1
        }

        val enchantedBook = org.bukkit.inventory.ItemStack(Material.ENCHANTED_BOOK)
        plugin.enchantmentManager.addEnchantment(enchantedBook, customEnchantment, level)

        targetPlayer.inventory.addItem(enchantedBook)

        sender.sendMessage(Component.text("✓ ${customEnchantment.displayName} ${toRomanNumeral(level)} Book an ${targetPlayer.name} gegeben!")
            .color(NamedTextColor.GREEN))
        targetPlayer.sendMessage(Component.text("✓ Du hast ein ${customEnchantment.displayName} ${toRomanNumeral(level)} §aEnchanted Book erhalten!")
            .color(NamedTextColor.GREEN))
        targetPlayer.playSound(targetPlayer.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f)
    }

    private fun handleEnchant(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("survivalplus.enchant")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        if (sender !is Player) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen!")
                .color(NamedTextColor.RED))
            return
        }

        // /sp enchant <enchantment> [level]
        if (args.size < 2) {
            sender.sendMessage(Component.text("Verwendung: /sp enchant <enchantment> [level]")
                .color(NamedTextColor.RED))
            sender.sendMessage(Component.text("Verfügbare: ${org.bysenom.survivalPlus.enchantments.CustomEnchantment.entries.joinToString(", ") { it.name.lowercase() }}")
                .color(NamedTextColor.GRAY))
            return
        }

        val item = sender.inventory.itemInMainHand
        if (item.type.isAir) {
            sender.sendMessage(Component.text("Du musst ein Item in der Hand halten!")
                .color(NamedTextColor.RED))
            return
        }

        val enchantmentName = args[1].uppercase()
        val customEnchantment = try {
            org.bysenom.survivalPlus.enchantments.CustomEnchantment.valueOf(enchantmentName)
        } catch (e: IllegalArgumentException) {
            sender.sendMessage(Component.text("Ungültiges Enchantment!")
                .color(NamedTextColor.RED))
            sender.sendMessage(Component.text("Verfügbare: ${org.bysenom.survivalPlus.enchantments.CustomEnchantment.entries.joinToString(", ") { it.name.lowercase() }}")
                .color(NamedTextColor.GRAY))
            return
        }

        val level = if (args.size >= 3) {
            args[2].toIntOrNull()?.coerceIn(1, customEnchantment.maxLevel) ?: 1
        } else {
            1
        }

        // Prüfe ob Item-Typ passt
        val itemType = org.bysenom.survivalPlus.enchantments.ItemType.fromMaterial(item.type)
        if (itemType == null || !customEnchantment.isApplicable(itemType)) {
            sender.sendMessage(Component.text("Dieses Enchantment kann nicht auf diesen Item-Typ angewendet werden!")
                .color(NamedTextColor.RED))
            sender.sendMessage(Component.text("${customEnchantment.displayName} ist nur für: ${customEnchantment.applicableItems.joinToString(", ") { it.name }}")
                .color(NamedTextColor.GRAY))
            return
        }

        plugin.enchantmentManager.addEnchantment(item, customEnchantment, level)

        sender.sendMessage(Component.text("✓ ${customEnchantment.displayName} ${toRomanNumeral(level)} wurde hinzugefügt!")
            .color(NamedTextColor.GREEN))
        sender.playSound(sender.location, org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.5f)
    }

    private fun createRandomEnchantedBook(quality: Quality): org.bukkit.inventory.ItemStack {
        val applicableEnchants = org.bysenom.survivalPlus.enchantments.CustomEnchantment.entries
            .filter { it.minQuality.tier <= quality.tier }

        if (applicableEnchants.isEmpty()) {
            // Fallback zu Uncommon wenn keine gefunden
            return createRandomEnchantedBook(Quality.UNCOMMON)
        }

        val enchant = applicableEnchants.random()
        val level = (1..enchant.maxLevel).random()

        val book = org.bukkit.inventory.ItemStack(Material.ENCHANTED_BOOK)
        plugin.enchantmentManager.addEnchantment(book, enchant, level)

        return book
    }

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

    private fun handleKit(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("survivalplus.kit")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        if (sender !is Player) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen!")
                .color(NamedTextColor.RED))
            return
        }

        // /sp kit [mythic|test]
        val kitType = if (args.size >= 2) args[1].lowercase() else "mythic"

        when (kitType) {
            "mythic", "test" -> giveTestKit(sender)
            else -> {
                sender.sendMessage(Component.text("Verfügbare Kits: mythic")
                    .color(NamedTextColor.RED))
            }
        }
    }

    private fun giveTestKit(player: Player) {
        player.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.GOLD))
        player.sendMessage(Component.text("⚔ Mythic Test Kit ⚔")
            .color(NamedTextColor.GOLD)
            .decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true))
        player.sendMessage(Component.empty())

        val quality = Quality.MYTHIC
        var itemsGiven = 0

        // Rüstung (Vanilla Max: Protection IV, Feather Falling IV, Unbreaking III)
        // Helmet
        val helmet = plugin.itemManager.createItem(Material.NETHERITE_HELMET, quality)
        plugin.enchantmentManager.addEnchantment(helmet, org.bysenom.survivalPlus.enchantments.CustomEnchantment.SOUL_BOUND, 1)
        plugin.enchantmentManager.addEnchantment(helmet, org.bysenom.survivalPlus.enchantments.CustomEnchantment.DIVINE_PROTECTION, 5)
        helmet.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 4)
        helmet.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3)
        helmet.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.MENDING, 1)
        player.inventory.addItem(helmet)
        itemsGiven++

        // Chestplate
        val chestplate = plugin.itemManager.createItem(Material.NETHERITE_CHESTPLATE, quality)
        plugin.enchantmentManager.addEnchantment(chestplate, org.bysenom.survivalPlus.enchantments.CustomEnchantment.SOUL_BOUND, 1)
        plugin.enchantmentManager.addEnchantment(chestplate, org.bysenom.survivalPlus.enchantments.CustomEnchantment.DIVINE_PROTECTION, 5)
        plugin.enchantmentManager.addEnchantment(chestplate, org.bysenom.survivalPlus.enchantments.CustomEnchantment.THORNS_PLUS, 3)
        chestplate.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 4)
        chestplate.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3)
        chestplate.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.MENDING, 1)
        player.inventory.addItem(chestplate)
        itemsGiven++

        // Leggings
        val leggings = plugin.itemManager.createItem(Material.NETHERITE_LEGGINGS, quality)
        plugin.enchantmentManager.addEnchantment(leggings, org.bysenom.survivalPlus.enchantments.CustomEnchantment.SOUL_BOUND, 1)
        plugin.enchantmentManager.addEnchantment(leggings, org.bysenom.survivalPlus.enchantments.CustomEnchantment.DIVINE_PROTECTION, 5)
        leggings.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 4)
        leggings.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3)
        leggings.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.MENDING, 1)
        player.inventory.addItem(leggings)
        itemsGiven++

        // Boots
        val boots = plugin.itemManager.createItem(Material.NETHERITE_BOOTS, quality)
        plugin.enchantmentManager.addEnchantment(boots, org.bysenom.survivalPlus.enchantments.CustomEnchantment.SOUL_BOUND, 1)
        plugin.enchantmentManager.addEnchantment(boots, org.bysenom.survivalPlus.enchantments.CustomEnchantment.DIVINE_PROTECTION, 5)
        plugin.enchantmentManager.addEnchantment(boots, org.bysenom.survivalPlus.enchantments.CustomEnchantment.SPEED_BOOST, 2)
        boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION, 4)
        boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.FEATHER_FALLING, 4)
        boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3)
        boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.MENDING, 1)
        player.inventory.addItem(boots)
        itemsGiven++

        // Schwert (Vanilla Max: Sharpness V, Looting III, Sweeping Edge III, Unbreaking III)
        val sword = plugin.itemManager.createItem(Material.NETHERITE_SWORD, quality)
        plugin.enchantmentManager.addEnchantment(sword, org.bysenom.survivalPlus.enchantments.CustomEnchantment.LIFESTEAL, 3)
        plugin.enchantmentManager.addEnchantment(sword, org.bysenom.survivalPlus.enchantments.CustomEnchantment.THUNDER_STRIKE, 2)
        plugin.enchantmentManager.addEnchantment(sword, org.bysenom.survivalPlus.enchantments.CustomEnchantment.SOUL_BOUND, 1)
        sword.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.SHARPNESS, 5)
        sword.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.LOOTING, 3)
        sword.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.SWEEPING_EDGE, 3)
        sword.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3)
        sword.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.MENDING, 1)
        player.inventory.addItem(sword)
        itemsGiven++

        // Spitzhacke (Vanilla Max: Efficiency V, Fortune III, Unbreaking III)
        val pickaxe = plugin.itemManager.createItem(Material.NETHERITE_PICKAXE, quality)
        plugin.enchantmentManager.addEnchantment(pickaxe, org.bysenom.survivalPlus.enchantments.CustomEnchantment.VEIN_MINER, 1)
        plugin.enchantmentManager.addEnchantment(pickaxe, org.bysenom.survivalPlus.enchantments.CustomEnchantment.AUTO_SMELT, 1)
        plugin.enchantmentManager.addEnchantment(pickaxe, org.bysenom.survivalPlus.enchantments.CustomEnchantment.SOUL_BOUND, 1)
        pickaxe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.EFFICIENCY, 5)
        pickaxe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.FORTUNE, 3)
        pickaxe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3)
        pickaxe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.MENDING, 1)
        player.inventory.addItem(pickaxe)
        itemsGiven++

        // Axt (Vanilla Max: Efficiency V, Fortune III, Sharpness V, Unbreaking III)
        val axe = plugin.itemManager.createItem(Material.NETHERITE_AXE, quality)
        plugin.enchantmentManager.addEnchantment(axe, org.bysenom.survivalPlus.enchantments.CustomEnchantment.TIMBER, 1)
        plugin.enchantmentManager.addEnchantment(axe, org.bysenom.survivalPlus.enchantments.CustomEnchantment.SOUL_BOUND, 1)
        axe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.EFFICIENCY, 5)
        axe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.FORTUNE, 3)
        axe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.SHARPNESS, 5)
        axe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3)
        axe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.MENDING, 1)
        player.inventory.addItem(axe)
        itemsGiven++

        // Schaufel (Vanilla Max: Efficiency V, Fortune III, Unbreaking III)
        val shovel = plugin.itemManager.createItem(Material.NETHERITE_SHOVEL, quality)
        plugin.enchantmentManager.addEnchantment(shovel, org.bysenom.survivalPlus.enchantments.CustomEnchantment.SOUL_BOUND, 1)
        shovel.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.EFFICIENCY, 5)
        shovel.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.FORTUNE, 3)
        shovel.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3)
        shovel.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.MENDING, 1)
        player.inventory.addItem(shovel)
        itemsGiven++

        // Hacke (Vanilla Max: Efficiency V, Fortune III, Unbreaking III)
        val hoe = plugin.itemManager.createItem(Material.NETHERITE_HOE, quality)
        plugin.enchantmentManager.addEnchantment(hoe, org.bysenom.survivalPlus.enchantments.CustomEnchantment.SOUL_BOUND, 1)
        hoe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.EFFICIENCY, 5)
        hoe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.FORTUNE, 3)
        hoe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3)
        hoe.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.MENDING, 1)
        player.inventory.addItem(hoe)
        itemsGiven++

        // Bogen (Vanilla Max: Power V, Punch II, Unbreaking III)
        val bow = plugin.itemManager.createItem(Material.BOW, quality)
        plugin.enchantmentManager.addEnchantment(bow, org.bysenom.survivalPlus.enchantments.CustomEnchantment.SOUL_BOUND, 1)
        bow.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.POWER, 5)
        bow.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PUNCH, 2)
        bow.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.FLAME, 1)
        bow.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.INFINITY, 1)
        bow.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 3)
        player.inventory.addItem(bow)
        itemsGiven++

        // Essen
        val goldenApples = org.bukkit.inventory.ItemStack(Material.GOLDEN_APPLE, 64)
        player.inventory.addItem(goldenApples)
        itemsGiven++

        val enchantedGoldenApples = org.bukkit.inventory.ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 16)
        player.inventory.addItem(enchantedGoldenApples)
        itemsGiven++

        val cookedBeef = org.bukkit.inventory.ItemStack(Material.COOKED_BEEF, 64)
        player.inventory.addItem(cookedBeef)
        itemsGiven++

        // Extras
        val arrows = org.bukkit.inventory.ItemStack(Material.ARROW, 1) // Infinity braucht nur 1
        player.inventory.addItem(arrows)
        itemsGiven++

        val totemOfUndying = org.bukkit.inventory.ItemStack(Material.TOTEM_OF_UNDYING, 3)
        player.inventory.addItem(totemOfUndying)
        itemsGiven++

        // Erfolgs-Nachricht
        player.sendMessage(Component.text("✓ $itemsGiven Items erhalten!")
            .color(NamedTextColor.GREEN))
        player.sendMessage(Component.text("  - 4x Mythic Netherite Armor")
            .color(NamedTextColor.GRAY))
        player.sendMessage(Component.text("  - 1x Mythic Netherite Sword")
            .color(NamedTextColor.GRAY))
        player.sendMessage(Component.text("  - 5x Mythic Netherite Tools")
            .color(NamedTextColor.GRAY))
        player.sendMessage(Component.text("  - 1x Mythic Bow")
            .color(NamedTextColor.GRAY))
        player.sendMessage(Component.text("  - Essen & Extras")
            .color(NamedTextColor.GRAY))
        player.sendMessage(Component.empty())
        player.sendMessage(Component.text("💡 Alle Items haben Soul Bound!")
            .color(NamedTextColor.AQUA))
        player.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.GOLD))

        // Sound & Effects
        player.playSound(player.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
        player.playSound(player.location, org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
        player.world.spawnParticle(
            org.bukkit.Particle.FIREWORK,
            player.location.clone().add(0.0, 1.0, 0.0),
            50, 0.5, 1.0, 0.5, 0.1
        )

        plugin.logger.info("[Kit] ${player.name} erhielt Mythic Test Kit")
    }

    private fun handleReforge(sender: CommandSender, @Suppress("UNUSED_PARAMETER") args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen!")
                .color(NamedTextColor.RED))
            return
        }

        if (!sender.hasPermission("survivalplus.reforge")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        val item = sender.inventory.itemInMainHand
        if (item.type.isAir) {
            sender.sendMessage(Component.text("Du musst ein Item in der Hand halten!")
                .color(NamedTextColor.RED))
            return
        }

        plugin.reforgingManager.openReforgingGUI(sender, item)
    }

    private fun handleCraft(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen!")
                .color(NamedTextColor.RED))
            return
        }

        if (!sender.hasPermission("survivalplus.craft")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        plugin.customCraftingGUI.openGUI(sender)
    }

    private fun handleInfo(sender: CommandSender, @Suppress("UNUSED_PARAMETER") args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen!")
                .color(NamedTextColor.RED))
            return
        }

        val item = sender.inventory.itemInMainHand
        if (item.type.isAir) {
            sender.sendMessage(Component.text("Du musst ein Item in der Hand halten!")
                .color(NamedTextColor.RED))
            return
        }

        val quality = plugin.itemManager.getQuality(item)
        if (quality == null) {
            sender.sendMessage(Component.text("Dies ist kein Custom Item!")
                .color(NamedTextColor.RED))
            return
        }

        sender.sendMessage(Component.text("=== Item Info ===")
            .color(NamedTextColor.GOLD))
        sender.sendMessage(Component.text("Qualität: ${quality.displayName}")
            .color(quality.color))
        sender.sendMessage(Component.text("Multiplikator: ${quality.statMultiplier}x")
            .color(NamedTextColor.YELLOW))
    }

    private fun handleWorldTier(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("survivalplus.worldtier")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        if (sender !is Player) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen!")
                .color(NamedTextColor.RED))
            return
        }

        // /sp worldtier [set|up|down] [tier]
        if (args.size < 2) {
            val currentTier = plugin.worldTierManager.getWorldTier(sender.world)
            sender.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.DARK_GRAY))
            sender.sendMessage(Component.text("Aktuelles World Tier: ").color(NamedTextColor.YELLOW)
                .append(currentTier.getDisplayComponent()))
            sender.sendMessage(Component.empty())
            currentTier.getDescription().forEach { sender.sendMessage(it) }
            sender.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.DARK_GRAY))
            sender.sendMessage(Component.text("Verwendung: /sp worldtier <set|up|down> [tier]").color(NamedTextColor.GRAY))
            return
        }

        when (args[1].lowercase()) {
            "set" -> {
                if (args.size < 3) {
                    sender.sendMessage(Component.text("Verwendung: /sp worldtier set <1-5>").color(NamedTextColor.RED))
                    return
                }
                val tier = args[2].toIntOrNull() ?: 1
                val worldTier = org.bysenom.survivalPlus.worldtier.WorldTier.fromTier(tier)
                plugin.worldTierManager.setWorldTier(sender.world, worldTier)
            }
            "up" -> {
                if (!plugin.worldTierManager.increaseWorldTier(sender.world)) {
                    sender.sendMessage(Component.text("✘ Bereits maximales World Tier erreicht!").color(NamedTextColor.RED))
                }
            }
            "down" -> {
                if (!plugin.worldTierManager.decreaseWorldTier(sender.world)) {
                    sender.sendMessage(Component.text("✘ Bereits minimales World Tier erreicht!").color(NamedTextColor.RED))
                }
            }
            else -> {
                sender.sendMessage(Component.text("Ungültiger Befehl! Nutze: set, up, down").color(NamedTextColor.RED))
            }
        }
    }

    private fun handleStartEvent(sender: CommandSender, @Suppress("UNUSED_PARAMETER") args: Array<out String>) {
        if (!sender.hasPermission("survivalplus.event")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        if (sender !is Player) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen!")
                .color(NamedTextColor.RED))
            return
        }

        // Starte zufälliges Event
        plugin.worldEventManager.startRandomEvent(sender.world)
        sender.sendMessage(Component.text("✓ World Event gestartet!").color(NamedTextColor.GREEN))
    }

    private fun handleLocate(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("survivalplus.locate")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        if (sender !is Player) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen!")
                .color(NamedTextColor.RED))
            return
        }

        // /sp locate shrine
        if (args.size < 2) {
            sender.sendMessage(Component.text("Verwendung: /sp locate shrine")
                .color(NamedTextColor.RED))
            return
        }

        when (args[1].lowercase()) {
            "shrine" -> {
                val nearestShrine = plugin.shrineManager.findNearestShrine(sender.location)

                if (nearestShrine == null) {
                    sender.sendMessage(Component.text("✘ Keine Shrines in dieser Welt gefunden!")
                        .color(NamedTextColor.RED))
                    sender.sendMessage(Component.text("Tipp: Shrines spawnen automatisch beim World-Load.")
                        .color(NamedTextColor.GRAY))
                    return
                }

                val distance = sender.location.distance(nearestShrine.location).toInt()
                val x = nearestShrine.location.blockX
                val y = nearestShrine.location.blockY
                val z = nearestShrine.location.blockZ

                sender.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.DARK_GRAY))
                sender.sendMessage(Component.text("⚔ Nächster World Tier Shrine:")
                    .color(NamedTextColor.GOLD)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true))
                sender.sendMessage(Component.empty())
                sender.sendMessage(Component.text("Koordinaten: ").color(NamedTextColor.YELLOW)
                    .append(Component.text("$x, $y, $z").color(NamedTextColor.WHITE)))
                sender.sendMessage(Component.text("Entfernung: ").color(NamedTextColor.YELLOW)
                    .append(Component.text("$distance Blöcke").color(NamedTextColor.WHITE)))
                sender.sendMessage(Component.empty())

                // Richtung berechnen
                val dx = x - sender.location.blockX
                val dz = z - sender.location.blockZ
                val direction = when {
                    kotlin.math.abs(dx) > kotlin.math.abs(dz) -> if (dx > 0) "Osten" else "Westen"
                    else -> if (dz > 0) "Süden" else "Norden"
                }

                sender.sendMessage(Component.text("Richtung: ").color(NamedTextColor.YELLOW)
                    .append(Component.text(direction).color(NamedTextColor.WHITE)))
                sender.sendMessage(Component.empty())
                sender.sendMessage(Component.text("💡 Suche nach dem Beacon-Laser!")
                    .color(NamedTextColor.AQUA))
                sender.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.DARK_GRAY))

                // Sound-Effekt
                sender.playSound(sender.location, org.bukkit.Sound.BLOCK_BELL_USE, 1f, 1f)
            }
            else -> {
                sender.sendMessage(Component.text("Unbekannte Struktur! Nutze: shrine")
                    .color(NamedTextColor.RED))
            }
        }
    }

    private fun handleShrine(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("survivalplus.shrine")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        if (sender !is Player) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen!")
                .color(NamedTextColor.RED))
            return
        }

        // /sp shrine generate|list|info
        if (args.size < 2) {
            sender.sendMessage(Component.text("Verwendung: /sp shrine <generate|list|info>")
                .color(NamedTextColor.RED))
            return
        }

        when (args[1].lowercase()) {
            "generate" -> {
                sender.sendMessage(Component.text("Generiere Shrines für ${sender.world.name}...")
                    .color(NamedTextColor.YELLOW))

                plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
                    plugin.shrineManager.generateShinesForWorld(sender.world)

                    plugin.server.scheduler.runTask(plugin, Runnable {
                        val count = plugin.shrineManager.getShrinesInWorld(sender.world).size
                        sender.sendMessage(Component.text("✓ Shrines generiert! Gesamt in dieser Welt: $count")
                            .color(NamedTextColor.GREEN))
                        sender.playSound(sender.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                    })
                })
            }

            "list" -> {
                val shrines = plugin.shrineManager.getShrinesInWorld(sender.world)

                if (shrines.isEmpty()) {
                    sender.sendMessage(Component.text("✘ Keine Shrines in ${sender.world.name} gefunden!")
                        .color(NamedTextColor.RED))
                    sender.sendMessage(Component.text("Tipp: Nutze '/sp shrine generate' zum Generieren")
                        .color(NamedTextColor.GRAY))
                    return
                }

                sender.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.DARK_GRAY))
                sender.sendMessage(Component.text("⚔ Shrines in ${sender.world.name}:")
                    .color(NamedTextColor.GOLD)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true))
                sender.sendMessage(Component.empty())

                shrines.forEachIndexed { index, shrine ->
                    val x = shrine.location.blockX
                    val y = shrine.location.blockY
                    val z = shrine.location.blockZ
                    val distance = sender.location.distance(shrine.location).toInt()

                    sender.sendMessage(Component.text("${index + 1}. ").color(NamedTextColor.GRAY)
                        .append(Component.text("$x, $y, $z").color(NamedTextColor.WHITE))
                        .append(Component.text(" ($distance Blöcke)").color(NamedTextColor.GRAY)))
                }

                sender.sendMessage(Component.empty())
                sender.sendMessage(Component.text("Gesamt: ${shrines.size} Shrines")
                    .color(NamedTextColor.YELLOW))
                sender.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.DARK_GRAY))
            }

            "info" -> {
                val allShrines = plugin.shrineManager.getAllShrines()
                val worldShrines = plugin.shrineManager.getShrinesInWorld(sender.world)

                sender.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.DARK_GRAY))
                sender.sendMessage(Component.text("⚔ Shrine System Info:")
                    .color(NamedTextColor.GOLD)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true))
                sender.sendMessage(Component.empty())
                sender.sendMessage(Component.text("Shrines gesamt: ").color(NamedTextColor.YELLOW)
                    .append(Component.text("${allShrines.size}").color(NamedTextColor.WHITE)))
                sender.sendMessage(Component.text("Shrines in ${sender.world.name}: ").color(NamedTextColor.YELLOW)
                    .append(Component.text("${worldShrines.size}").color(NamedTextColor.WHITE)))
                sender.sendMessage(Component.empty())
                sender.sendMessage(Component.text("Min. Abstand: ").color(NamedTextColor.YELLOW)
                    .append(Component.text("${plugin.config.getInt("shrines.min-distance")} Blöcke").color(NamedTextColor.WHITE)))
                sender.sendMessage(Component.text("Min. Spawn-Distanz: ").color(NamedTextColor.YELLOW)
                    .append(Component.text("${plugin.config.getInt("shrines.min-spawn-distance")} Blöcke").color(NamedTextColor.WHITE)))
                sender.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.DARK_GRAY))
            }

            else -> {
                sender.sendMessage(Component.text("Unbekannter Befehl! Nutze: generate, list, info")
                    .color(NamedTextColor.RED))
            }
        }
    }

    private fun handleScoreboard(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen!").color(NamedTextColor.RED))
            return
        }
        if (!sender.hasPermission("survivalplus.scoreboard")) {
            sender.sendMessage(Component.text("Keine Berechtigung!").color(NamedTextColor.RED))
            return
        }
        // /sp sb toggle
        if (args.size < 2) {
            sender.sendMessage(Component.text("Verwendung: /sp sb toggle").color(NamedTextColor.RED))
            return
        }
        when (args[1].lowercase()) {
            "toggle" -> plugin.scoreboardManager.toggle(sender)
            else -> sender.sendMessage(Component.text("Unbekannter Befehl! Nutze: toggle").color(NamedTextColor.RED))
        }
    }

    private fun handleButcher(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("survivalplus.butcher")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        if (sender !is Player) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen!")
                .color(NamedTextColor.RED))
            return
        }

        // /sp butcher spawn [tier]
        if (args.size < 2) {
            sender.sendMessage(Component.text("Verwendung: /sp butcher spawn [tier]")
                .color(NamedTextColor.RED))
            return
        }

        when (args[1].lowercase()) {
            "spawn" -> {
                val worldTier = if (args.size >= 3) {
                    args[2].toIntOrNull()?.coerceIn(2, 5) ?: plugin.worldTierManager.getWorldTier(sender.world).tier
                } else {
                    plugin.worldTierManager.getWorldTier(sender.world).tier
                }

                if (worldTier < org.bysenom.survivalPlus.mobs.ButcherBoss.MIN_WORLD_TIER) {
                    sender.sendMessage(Component.text("✘ Der Butcher kann nur ab World Tier Heroic (2) spawnen!")
                        .color(NamedTextColor.RED))
                    return
                }

                val location = sender.location
                plugin.butcherBoss.spawnButcher(location, worldTier)

                sender.sendMessage(Component.text("✓ The Butcher wurde gespawnt! (Tier $worldTier)")
                    .color(NamedTextColor.GREEN))
                sender.playSound(sender.location, org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.5f, 0.5f)
            }
            else -> {
                sender.sendMessage(Component.text("Unbekannter Befehl! Nutze: spawn")
                    .color(NamedTextColor.RED))
            }
        }
    }

    private fun handleBoss(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("survivalplus.boss")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        if (sender !is Player) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen!")
                .color(NamedTextColor.RED))
            return
        }

        // /sp boss spawn <harvester|frosttitan> [tier]
        if (args.size < 3) {
            sender.sendMessage(Component.text("Verwendung: /sp boss spawn <harvester|frosttitan> [tier]")
                .color(NamedTextColor.RED))
            sender.sendMessage(Component.text("Verfügbare Bosse: harvester, frosttitan")
                .color(NamedTextColor.GRAY))
            return
        }

        when (args[1].lowercase()) {
            "spawn" -> {
                val bossType = args[2].lowercase()
                val worldTier = if (args.size >= 4) {
                    args[3].toIntOrNull()?.coerceIn(1, 5) ?: plugin.worldTierManager.getWorldTier(sender.world).tier
                } else {
                    plugin.worldTierManager.getWorldTier(sender.world).tier
                }

                val location = sender.location

                when (bossType) {
                    "harvester" -> {
                        plugin.harvesterBoss.spawn(location, worldTier)
                        sender.sendMessage(Component.text("✓ Der Ernter wurde gespawnt! (Tier $worldTier)")
                            .color(NamedTextColor.GREEN))
                        sender.playSound(sender.location, org.bukkit.Sound.ENTITY_WITHER_SPAWN, 0.5f, 0.7f)
                    }
                    "frosttitan" -> {
                        plugin.frostTitanBoss.spawn(location, worldTier)
                        sender.sendMessage(Component.text("✓ Der Frost-Titan wurde gespawnt! (Tier $worldTier)")
                            .color(NamedTextColor.GREEN))
                        sender.playSound(sender.location, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f)
                    }
                    else -> {
                        sender.sendMessage(Component.text("Unbekannter Boss-Typ!")
                            .color(NamedTextColor.RED))
                        sender.sendMessage(Component.text("Verfügbare Bosse: harvester, frosttitan")
                            .color(NamedTextColor.GRAY))
                    }
                }
            }
            else -> {
                sender.sendMessage(Component.text("Unbekannter Befehl! Nutze: spawn")
                    .color(NamedTextColor.RED))
            }
        }
    }

    private fun handleArena(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen!")
                .color(NamedTextColor.RED))
            return
        }

        if (!sender.hasPermission("survivalplus.arena")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        if (args.size < 2) {
            sender.sendMessage(Component.text("Verwendung: /sp arena <enter|leave|info>")
                .color(NamedTextColor.RED))
            return
        }

        val arenaManager = plugin.worldBossArenaManager

        when (args[1].lowercase()) {
            "enter" -> {
                if (arenaManager.isInArena(sender)) {
                    sender.sendMessage(Component.text("⚠ Du bist bereits in der Arena!")
                        .color(NamedTextColor.YELLOW))
                    return
                }

                if (arenaManager.teleportToArena(sender)) {
                    val timeUntilBoss = arenaManager.getTimeUntilNextBoss()
                    val minutes = timeUntilBoss / 60
                    val seconds = timeUntilBoss % 60

                    sender.sendMessage(Component.text("⚔ World Boss Arena ⚔").color(NamedTextColor.GOLD))
                    sender.sendMessage(Component.text("Nächster Boss spawn in: ${minutes}m ${seconds}s").color(NamedTextColor.YELLOW))
                    sender.sendMessage(Component.text("Verlasse mit: /sp arena leave").color(NamedTextColor.GRAY))
                }
            }
            "leave" -> {
                if (!arenaManager.isInArena(sender)) {
                    sender.sendMessage(Component.text("⚠ Du bist nicht in der Arena!")
                        .color(NamedTextColor.YELLOW))
                    return
                }

                arenaManager.teleportFromArena(sender)
            }
            "info" -> {
                val timeUntilBoss = arenaManager.getTimeUntilNextBoss()
                val minutes = timeUntilBoss / 60
                val seconds = timeUntilBoss % 60

                sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.DARK_GRAY))
                sender.sendMessage(Component.text("⚔ World Boss Arena Info ⚔").color(NamedTextColor.GOLD))
                sender.sendMessage(Component.empty())
                sender.sendMessage(Component.text("Boss: ").color(NamedTextColor.GRAY)
                    .append(Component.text("Titan Golem").color(NamedTextColor.YELLOW)))
                sender.sendMessage(Component.text("Nächster Spawn: ").color(NamedTextColor.GRAY)
                    .append(Component.text("${minutes}m ${seconds}s").color(NamedTextColor.GREEN)))
                sender.sendMessage(Component.text("Arena-Welt: ").color(NamedTextColor.GRAY)
                    .append(Component.text("Survival_boss").color(NamedTextColor.AQUA)))
                sender.sendMessage(Component.empty())
                sender.sendMessage(Component.text("Befehle:").color(NamedTextColor.YELLOW))
                sender.sendMessage(Component.text("  /sp arena enter").color(NamedTextColor.GRAY)
                    .append(Component.text(" - Betrete die Arena").color(NamedTextColor.WHITE)))
                sender.sendMessage(Component.text("  /sp arena leave").color(NamedTextColor.GRAY)
                    .append(Component.text(" - Verlasse die Arena").color(NamedTextColor.WHITE)))
                sender.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.DARK_GRAY))
            }
            else -> {
                sender.sendMessage(Component.text("Unbekannter Befehl! Nutze: enter, leave, info")
                    .color(NamedTextColor.RED))
            }
        }
    }

    private fun handleReload(sender: CommandSender) {
        if (!sender.hasPermission("survivalplus.reload")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        sender.sendMessage(Component.text("⏳ Lade Config neu...")
            .color(NamedTextColor.YELLOW))

        // Async reload um Server nicht zu blockieren
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            try {
                // Config neu laden
                plugin.configCacheManager.reload()
                
                // Item-Cache leeren (optional)
                plugin.itemManager.clearCache()
                
                plugin.server.scheduler.runTask(plugin, Runnable {
                    sender.sendMessage(Component.text("✓ Config erfolgreich neu geladen!")
                        .color(NamedTextColor.GREEN))
                    sender.sendMessage(Component.text("  - Config-Cache: ${plugin.configCacheManager.getCacheSize()} Einträge")
                        .color(NamedTextColor.GRAY))
                    sender.sendMessage(Component.text("  - Item-Cache: ${plugin.itemManager.getCacheStats()}")
                        .color(NamedTextColor.GRAY))
                })
            } catch (e: Exception) {
                plugin.server.scheduler.runTask(plugin, Runnable {
                    sender.sendMessage(Component.text("✘ Fehler beim Neu-Laden: ${e.message}")
                        .color(NamedTextColor.RED))
                })
                plugin.logger.severe("Fehler beim Config-Reload: ${e.message}")
                e.printStackTrace()
            }
        })
    }

    private fun handleDebug(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("survivalplus.debug")) {
            sender.sendMessage(Component.text("Keine Berechtigung!")
                .color(NamedTextColor.RED))
            return
        }

        when (args.getOrNull(1)?.lowercase()) {
            "spawn" -> {
                // /sp debug spawn worldboss
                if (args.size < 3) {
                    sender.sendMessage(Component.text("Verwendung: /sp debug spawn worldboss")
                        .color(NamedTextColor.RED))
                    return
                }
                
                when (args[2].lowercase()) {
                    "worldboss", "boss", "titan" -> {
                        val success = plugin.worldBossArenaManager.forceSpawnBoss()
                        if (success) {
                            sender.sendMessage(Component.text("✓ World Boss wird in 60 Sekunden gespawnt!")
                                .color(NamedTextColor.GREEN))
                        } else {
                            sender.sendMessage(Component.text("⚠ Boss ist bereits aktiv oder Arena nicht verfügbar!")
                                .color(NamedTextColor.RED))
                        }
                    }
                    else -> {
                        sender.sendMessage(Component.text("Unbekannter Boss-Typ!")
                            .color(NamedTextColor.RED))
                        sender.sendMessage(Component.text("Verfügbar: worldboss")
                            .color(NamedTextColor.GRAY))
                    }
                }
            }
            "memory", "cache" -> {
                sender.sendMessage(Component.text("=== SurvivalPlus Debug Info ===")
                    .color(NamedTextColor.GOLD))
                sender.sendMessage(Component.text("Config-Cache:")
                    .color(NamedTextColor.YELLOW))
                sender.sendMessage(Component.text("  - Einträge: ${plugin.configCacheManager.getCacheSize()}")
                    .color(NamedTextColor.GRAY))
                sender.sendMessage(Component.text("  - Keys: ${plugin.configCacheManager.getCachedKeys().take(10).joinToString(", ")}")
                    .color(NamedTextColor.GRAY))
                
                sender.sendMessage(Component.text("Item-Cache:")
                    .color(NamedTextColor.YELLOW))
                sender.sendMessage(Component.text("  - ${plugin.itemManager.getCacheStats()}")
                    .color(NamedTextColor.GRAY))
                
                sender.sendMessage(Component.text("Quality Plates:")
                    .color(NamedTextColor.YELLOW))
                sender.sendMessage(Component.text("  - Aktive Hologramme: ${plugin.qualityPlateManager.getPlateCount()}")
                    .color(NamedTextColor.GRAY))
                
                sender.sendMessage(Component.text("Custom Blocks:")
                    .color(NamedTextColor.YELLOW))
                sender.sendMessage(Component.text("  - Platzierte Blöcke: ${plugin.customBlockManager.getBlockCount()}")
                    .color(NamedTextColor.GRAY))
                
                sender.sendMessage(Component.text("Special Mobs:")
                    .color(NamedTextColor.YELLOW))
                sender.sendMessage(Component.text("  - Aktive Mobs: ${plugin.specialMobManager.getActiveMobCount()}")
                    .color(NamedTextColor.GRAY))
            }
            "clear" -> {
                plugin.itemManager.clearCache()
                plugin.configCacheManager.clearCache()
                sender.sendMessage(Component.text("✓ Alle Caches geleert!")
                    .color(NamedTextColor.GREEN))
            }
            else -> {
                sender.sendMessage(Component.text("Verwendung:")
                    .color(NamedTextColor.YELLOW))
                sender.sendMessage(Component.text("/sp debug spawn worldboss - Forciert Boss-Spawn in 60s")
                    .color(NamedTextColor.GRAY))
                sender.sendMessage(Component.text("/sp debug memory - Zeigt Cache-Statistiken")
                    .color(NamedTextColor.GRAY))
                sender.sendMessage(Component.text("/sp debug clear - Leert alle Caches")
                    .color(NamedTextColor.GRAY))
            }
        }
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage(Component.text("=== SurvivalPlus Befehle ===")
            .color(NamedTextColor.GOLD))
        sender.sendMessage(Component.text("/sp give <spieler> <material> [qualität] - Gibt ein Item")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp giveblock <spieler> <blocktype> - Gibt einen Custom Block")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp givebook <spieler> [enchant] [level] [qualität] - Gibt Enchanted Book")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp enchant <enchantment> [level] - Verzaubert Item in Hand")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp kit [mythic] - Gibt Test-Kit (Admin)")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp reforge - Öffnet das Reforging-GUI")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp craft - Öffnet das Custom Crafting-GUI")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp info - Zeigt Item-Informationen")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp worldtier [set|up|down] - Verwaltet World Tiers")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp startevent - Startet ein World Event")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp locate shrine - Findet den nächsten Shrine")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp butcher spawn [tier] - Spawnt den Butcher Boss")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp boss spawn <harvester|frosttitan> [tier] - Spawnt neue Bosse")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp arena enter/leave - Teleportiere zur/von Boss Arena")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp reload - Lädt die Config neu")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp debug spawn worldboss - Forciert World Boss Spawn (60s)")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp debug [memory|clear] - Debug-Informationen")
            .color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp sb toggle - Aktiviert/Deaktiviert das Scoreboard").color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp skills - Öffne deine Skills").color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp achievements - Zeige deine Erfolge").color(NamedTextColor.YELLOW))
        sender.sendMessage(Component.text("/sp trade <spieler> - Starte einen Handel").color(NamedTextColor.YELLOW))
    }

    /**
     * Öffnet das Skills GUI
     */
    private fun handleSkills(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(Component.text("Dieser Command ist nur für Spieler!").color(NamedTextColor.RED))
            return
        }

        plugin.skillsGUI.openGUI(sender)
        sender.playSound(sender.location, org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f)
    }

    /**
     * Öffnet das Achievements GUI
     */
    private fun handleAchievements(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(Component.text("Dieser Command ist nur für Spieler!").color(NamedTextColor.RED))
            return
        }

        plugin.achievementsGUI.openGUI(sender)
        sender.playSound(sender.location, org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f)
    }

    /**
     * Handelt Trade-Commands
     */
    private fun handleTrade(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage(Component.text("Dieser Command ist nur für Spieler!").color(NamedTextColor.RED))
            return
        }

        // /sp trade <player> - Trade-Request senden
        // /sp trade accept - Trade-Request akzeptieren
        // /sp trade deny - Trade-Request ablehnen

        if (args.size < 2) {
            sender.sendMessage(Component.text("Verwendung: /sp trade <spieler|accept|deny>")
                .color(NamedTextColor.RED))
            return
        }

        when (args[1].lowercase()) {
            "accept" -> {
                plugin.tradeManager.acceptTradeRequest(sender)
            }
            "deny" -> {
                plugin.tradeManager.denyTradeRequest(sender)
            }
            else -> {
                // Trade-Request senden
                val targetPlayer = Bukkit.getPlayer(args[1])
                if (targetPlayer == null) {
                    sender.sendMessage(Component.text("Spieler nicht gefunden!")
                        .color(NamedTextColor.RED))
                    return
                }

                plugin.tradeManager.sendTradeRequest(sender, targetPlayer)
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> listOf("give", "giveblock", "givebook", "enchant", "kit", "reforge", "craft", "info", "worldtier", "startevent", "locate", "shrine", "butcher", "boss", "sb", "reload", "debug", "skills", "achievements", "trade")
                .filter { it.startsWith(args[0].lowercase()) }
            2 -> when (args[0].lowercase()) {
                "give", "giveblock", "givebook" -> Bukkit.getOnlinePlayers().map { it.name }
                "kit" -> listOf("mythic", "test")
                "worldtier" -> listOf("set", "up", "down")
                "locate" -> listOf("shrine")
                "shrine" -> listOf("generate", "list", "info")
                "debug" -> listOf("spawn", "memory", "cache", "clear")
                "butcher" -> listOf("spawn")
                "boss" -> listOf("spawn")
                "sb" -> listOf("toggle")
                "trade" -> listOf("accept", "deny") + Bukkit.getOnlinePlayers().map { it.name }
                else -> emptyList()
            }
            3 -> when (args[0].lowercase()) {
                "give" -> Material.entries.map { it.name.lowercase() }
                "giveblock" -> listOf("custom_anvil", "reforging_station", "world_tier_altar")
                "givebook" -> org.bysenom.survivalPlus.enchantments.CustomEnchantment.entries.map { it.name.lowercase() }
                "worldtier" -> if (args[1].lowercase() == "set") listOf("1", "2", "3", "4", "5") else emptyList()
                "butcher" -> if (args[1].lowercase() == "spawn") listOf("2", "3", "4", "5") else emptyList()
                "boss" -> if (args[1].lowercase() == "spawn") listOf("harvester", "frosttitan") else emptyList()
                "debug" -> if (args[1].lowercase() == "spawn") listOf("worldboss", "boss", "titan") else emptyList()
                else -> emptyList()
            }
            4 -> when (args[0].lowercase()) {
                "give" -> Quality.entries.map { it.name.lowercase() }
                "boss" -> if (args[1].lowercase() == "spawn") listOf("1", "2", "3", "4", "5") else emptyList()
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
