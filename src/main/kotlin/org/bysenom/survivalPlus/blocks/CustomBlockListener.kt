package org.bysenom.survivalPlus.blocks

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlot
import org.bysenom.survivalPlus.SurvivalPlus

/**
 * Event Listener für Custom Blocks
 */
class CustomBlockListener(private val plugin: SurvivalPlus) : Listener {

    /**
     * Block-Platzierung
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        val item = event.itemInHand

        // Prüfe ob es ein Custom Block ist
        val customBlock = CustomBlock.fromItem(item) ?: return

        val location = event.blockPlaced.location

        // Prüfe ob bereits ein Custom Block hier ist
        if (plugin.customBlockManager.isCustomBlock(location)) {
            event.isCancelled = true
            player.sendMessage(Component.text("Hier ist bereits ein Custom Block!")
                .color(NamedTextColor.RED))
            return
        }

        // Erstelle Armor Stand für visuelle Darstellung
        val world = location.world ?: return
        val armorStandLoc = location.clone().add(0.5, 0.0, 0.5)

        val armorStand = world.spawn(armorStandLoc, ArmorStand::class.java) { stand ->
            stand.isVisible = false
            stand.isInvulnerable = true
            stand.setGravity(false)
            stand.isMarker = true
            stand.setCanPickupItems(false)
            stand.customName(customBlock.getDisplayComponent())
            stand.isCustomNameVisible = true

            // Setze Custom Block als Helm
            val helmet = customBlock.createItem()
            stand.equipment?.helmet = helmet
        }

        // Registriere Block
        plugin.customBlockManager.placeBlock(
            location,
            customBlock,
            armorStand.uniqueId,
            player.uniqueId
        )

        // Effekte
        plugin.particleEffectManager.spawnBlockPlaceParticles(location)
        player.playSound(location, org.bukkit.Sound.BLOCK_ANVIL_LAND, 1f, 1f)

        player.sendMessage(Component.text("✓ ${customBlock.displayName} §aplatziert!")
            .color(NamedTextColor.GREEN))

        plugin.logger.info("${player.name} hat einen ${customBlock.name} bei ${location.blockX}, ${location.blockY}, ${location.blockZ} platziert")
    }

    /**
     * Block-Abbau
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockBreak(event: BlockBreakEvent) {
        val location = event.block.location
        val placedBlock = plugin.customBlockManager.getBlock(location) ?: return

        // Verhindere normalen Drop
        event.isDropItems = false
        event.isCancelled = false

        val player = event.player

        // Entferne Armor Stand
        placedBlock.armorStandUUID?.let { uuid ->
            location.world?.entities?.find { it.uniqueId == uuid }?.remove()
        }

        // Entferne aus Manager
        plugin.customBlockManager.removeBlock(location)

        // Droppe Custom Block Item
        val drop = placedBlock.type.createItem()
        location.world?.dropItemNaturally(location, drop)

        // Effekte
        player.playSound(location, org.bukkit.Sound.BLOCK_ANVIL_DESTROY, 1f, 1f)
        player.sendMessage(Component.text("✓ ${placedBlock.type.displayName} §aabgebaut!")
            .color(NamedTextColor.GREEN))

        plugin.logger.info("${player.name} hat einen ${placedBlock.type.name} bei ${location.blockX}, ${location.blockY}, ${location.blockZ} abgebaut")
    }

    /**
     * Rechtsklick auf Custom Block (via Block)
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return

        val block = event.clickedBlock ?: return
        val location = block.location
        val player = event.player

        // Prüfe ob es ein Custom Block ist
        val placedBlock = plugin.customBlockManager.getBlock(location)
        if (placedBlock != null) {
            // Verhindere normale Block-Interaktion
            event.isCancelled = true
            openGUIForBlock(player, placedBlock.type)
            return
        }

        // Prüfe ob es ein Shrine ist (Lodestone)
        if (block.type == Material.LODESTONE && plugin.shrineManager.isShrineLocation(location)) {
            event.isCancelled = true
            plugin.worldTierGUI.openGUI(player)
            player.playSound(player.location, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f)
        }
    }

    /**
     * Rechtsklick auf Armor Stand (Alternative Interaktion)
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onArmorStandInteract(event: PlayerInteractAtEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return

        val entity = event.rightClicked
        if (entity.type != EntityType.ARMOR_STAND) return

        val placedBlock = plugin.customBlockManager.getBlockByArmorStand(entity.uniqueId) ?: return

        event.isCancelled = true

        val player = event.player
        openGUIForBlock(player, placedBlock.type)
    }

    /**
     * Verhindere Schaden an Custom Block Armor Stands
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onArmorStandDamage(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        if (entity.type != EntityType.ARMOR_STAND) return

        val placedBlock = plugin.customBlockManager.getBlockByArmorStand(entity.uniqueId) ?: return

        // Verhindere Schaden
        event.isCancelled = true

        // Wenn Spieler, öffne GUI
        if (event.damager is Player) {
            val player = event.damager as Player
            openGUIForBlock(player, placedBlock.type)
        }
    }

    /**
     * Öffnet das entsprechende GUI für einen Custom Block
     */
    private fun openGUIForBlock(player: Player, blockType: CustomBlock) {
        when (blockType) {
            CustomBlock.CUSTOM_ANVIL -> {
                plugin.customCraftingGUI.openGUI(player)
                player.playSound(player.location, org.bukkit.Sound.BLOCK_ANVIL_USE, 0.5f, 1f)
            }
            CustomBlock.REFORGING_STATION -> {
                val itemInHand = player.inventory.itemInMainHand
                if (itemInHand.type.isAir) {
                    player.sendMessage(Component.text("✘ Halte ein Item in der Hand zum Reforgen!")
                        .color(NamedTextColor.RED))
                    player.playSound(player.location, org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f)
                    return
                }
                plugin.reforgingManager.openReforgingGUI(player, itemInHand)
                player.playSound(player.location, org.bukkit.Sound.BLOCK_ANVIL_USE, 0.5f, 1f)
            }
            CustomBlock.WORLD_TIER_ALTAR -> {
                plugin.worldTierGUI.openGUI(player)
                player.playSound(player.location, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f)
            }
        }
    }
}

