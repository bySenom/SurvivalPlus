package org.bysenom.survivalPlus.listeners

import net.kyori.adventure.text.Component
import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.Quality
import org.bukkit.Material
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerFishEvent
import kotlin.random.Random

class ItemListener(private val plugin: SurvivalPlus) : Listener {

    /**
     * Monster-Kills droppen Custom Items
     */
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        if (entity !is Monster) return

        // Boss-Minions droppen kein Loot
        val minionKey = org.bukkit.NamespacedKey(plugin, "boss_minion")
        if (entity.persistentDataContainer.has(minionKey, org.bukkit.persistence.PersistentDataType.BYTE)) {
            event.drops.clear()
            event.droppedExp = 0
            return
        }

        val killer = entity.killer ?: return

        // Drop-Chance basierend auf Monster-Typ
        val dropChance = when (entity) {
            is org.bukkit.entity.Zombie -> 0.15        // 15%
            is org.bukkit.entity.Skeleton -> 0.15      // 15%
            is org.bukkit.entity.Creeper -> 0.20       // 20%
            is org.bukkit.entity.Enderman -> 0.30      // 30%
            is org.bukkit.entity.Blaze -> 0.25         // 25%
            is org.bukkit.entity.Wither -> 1.0         // 100% (Boss)
            is org.bukkit.entity.EnderDragon -> 1.0    // 100% (Boss)
            else -> 0.10                               // 10%
        }

        if (Random.nextDouble() < dropChance) {
            val quality = Quality.random()
            val material = getRandomWeaponOrArmor()
            val customItem = plugin.itemManager.createItem(material, quality)

            // Enchantments automatisch hinzufügen
            val itemWithEnchants = plugin.enchantmentManager.addRandomEnchantments(customItem, quality)

            event.drops.add(itemWithEnchants)

            killer.sendMessage(Component.text("💎 ${quality.displayName} Item gedroppt!")
                .color(quality.color))

            // Particle und Sound Effekte
            plugin.particleEffectManager.spawnItemDropParticles(entity.location, quality)
            plugin.soundManager.playItemDropSound(killer, quality)

            // Title für Legendary und Mythic Items
            plugin.messageManager.showItemReceivedTitle(killer, quality)
        }
    }

    /**
     * Angeln kann Custom Items droppen
     */
    @EventHandler
    fun onPlayerFish(event: PlayerFishEvent) {
        if (event.state != PlayerFishEvent.State.CAUGHT_FISH) return

        val player = event.player

        // 20% Chance für Custom Item statt normalem Fisch
        if (Random.nextDouble() < 0.20) {
            val quality = Quality.random()
            val material = getRandomFishingLoot()
            val customItem = plugin.itemManager.createItem(material, quality)

            // Enchantments hinzufügen
            val itemWithEnchants = plugin.enchantmentManager.addRandomEnchantments(customItem, quality)

            // Ersetze normalen Fang
            val caught = event.caught
            if (caught is org.bukkit.entity.Item) {
                caught.itemStack = itemWithEnchants
            }

            player.sendMessage(Component.text("🎣 Du hast ein ${quality.displayName} Item geangelt!")
                .color(quality.color))

            // Particle und Sound Effekte
            plugin.particleEffectManager.spawnItemDropParticles(player.location, quality)
            plugin.soundManager.playItemDropSound(player, quality)

            // Title für Legendary und Mythic Items
            plugin.messageManager.showItemReceivedTitle(player, quality)
        }
    }

    /**
     * Hilfsmethode: Zufällige Waffe oder Rüstung
     */
    private fun getRandomWeaponOrArmor(): Material {
        val items = listOf(
            // Waffen
            Material.DIAMOND_SWORD,
            Material.NETHERITE_SWORD,
            Material.DIAMOND_AXE,
            Material.NETHERITE_AXE,
            Material.BOW,
            Material.CROSSBOW,
            // Rüstung
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS,
            Material.NETHERITE_HELMET,
            Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS,
            Material.NETHERITE_BOOTS,
            // Werkzeuge
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL
        )
        return items.random()
    }

    /**
     * Hilfsmethode: Zufällige Angel-Beute (seltene Items)
     */
    private fun getRandomFishingLoot(): Material {
        val items = listOf(
            // Waffen (selten)
            Material.DIAMOND_SWORD,
            Material.NETHERITE_SWORD,
            Material.IRON_SWORD,
            Material.TRIDENT,
            Material.MACE,

            // Bögen
            Material.BOW,
            Material.CROSSBOW,

            // Angel-Equipment
            Material.FISHING_ROD,

            // Rüstung (selten)
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.NETHERITE_HELMET,
            Material.NETHERITE_CHESTPLATE,
            Material.IRON_HELMET,
            Material.IRON_CHESTPLATE,
            Material.CHAINMAIL_HELMET,
            Material.CHAINMAIL_CHESTPLATE,

            // Werkzeuge
            Material.DIAMOND_PICKAXE,
            Material.IRON_PICKAXE,

            // Spezial
            Material.ENCHANTED_BOOK,
            Material.SHIELD
        )
        return items.random()
    }

    /**
     * Custom Item Schaden-Berechnung
     */
    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return

        val player = event.damager as Player
        val weapon = player.inventory.itemInMainHand

        if (weapon.type.isAir) return

        val quality = plugin.itemManager.getQuality(weapon) ?: return

        // Schaden mit Qualitäts-Multiplikator erhöhen
        val bonusDamage = event.damage * (quality.statMultiplier - 1.0)
        event.damage += bonusDamage

        // Visual Feedback
        if (bonusDamage > 0) {
            player.sendActionBar(Component.text("+${String.format("%.1f", bonusDamage)} Bonusschaden")
                .color(quality.color))
        }
    }
}

