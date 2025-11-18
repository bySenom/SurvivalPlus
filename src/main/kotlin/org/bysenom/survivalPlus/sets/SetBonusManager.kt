package org.bysenom.survivalPlus.sets

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import java.util.UUID

class SetBonusManager(private val plugin: SurvivalPlus) {

    private val setKey = NamespacedKey(plugin, "armor_set")
    private val activeSetBonuses = mutableMapOf<UUID, Pair<ArmorSet, Int>>() // Player UUID -> (Set, Piece Count)

    /**
     * Weist ein Item einem Set zu
     */
    fun assignSet(item: ItemStack, set: ArmorSet): ItemStack {
        val meta = item.itemMeta ?: return item
        meta.persistentDataContainer.set(setKey, PersistentDataType.STRING, set.name)

        // Update Lore
        updateSetLore(meta, set)

        item.itemMeta = meta
        return item
    }

    /**
     * Gibt das Set eines Items zurück
     */
    fun getSet(item: ItemStack): ArmorSet? {
        val meta = item.itemMeta ?: return null
        val setName = meta.persistentDataContainer.get(setKey, PersistentDataType.STRING) ?: return null

        return try {
            ArmorSet.valueOf(setName)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * Prüft welche Set-Boni ein Spieler hat
     */
    fun checkSetBonuses(player: Player) {
        val armor = listOfNotNull(
            player.inventory.helmet,
            player.inventory.chestplate,
            player.inventory.leggings,
            player.inventory.boots
        )

        if (armor.isEmpty()) {
            removeSetBonuses(player)
            return
        }

        // Zähle Set-Teile
        val setCounts = mutableMapOf<ArmorSet, Int>()
        armor.forEach { piece ->
            val set = getSet(piece)
            if (set != null) {
                setCounts[set] = setCounts.getOrDefault(set, 0) + 1
            }
        }

        // Finde das beste Set (meiste Teile)
        val bestSet = setCounts.maxByOrNull { it.value }

        if (bestSet != null && bestSet.value >= 2) {
            applySetBonuses(player, bestSet.key, bestSet.value)
        } else {
            removeSetBonuses(player)
        }
    }

    /**
     * Wendet Set-Boni auf einen Spieler an
     */
    private fun applySetBonuses(player: Player, set: ArmorSet, pieceCount: Int) {
        // Prüfe ob sich etwas geändert hat
        val current = activeSetBonuses[player.uniqueId]
        if (current?.first == set && current.second == pieceCount) {
            return // Keine Änderung
        }

        // Entferne alte Boni
        removeSetBonuses(player)

        // Speichere neue Boni
        activeSetBonuses[player.uniqueId] = Pair(set, pieceCount)

        // Wende 2-Piece Bonus an
        if (pieceCount >= 2) {
            applyBonus(player, set.twoPieceBonus)

            player.sendMessage(
                Component.text("✦ Set-Bonus aktiviert: ${set.twoPieceBonus.name}")
                    .color(set.color)
            )
        }

        // Wende 4-Piece Bonus an
        if (pieceCount >= 4) {
            applyBonus(player, set.fourPieceBonus)

            player.sendMessage(
                Component.text("✦✦ Voller Set-Bonus aktiviert: ${set.fourPieceBonus.name}")
                    .color(set.color)
            )
        }
    }

    /**
     * Entfernt alle Set-Boni von einem Spieler
     */
    private fun removeSetBonuses(player: Player) {
        val current = activeSetBonuses.remove(player.uniqueId) ?: return

        // Entferne Potion-Effekte
        val (set, pieceCount) = current

        if (pieceCount >= 2) {
            removeBonus(player, set.twoPieceBonus)
        }
        if (pieceCount >= 4) {
            removeBonus(player, set.fourPieceBonus)
        }
    }

    /**
     * Wendet einen einzelnen Bonus an
     */
    private fun applyBonus(player: Player, bonus: SetBonus) {
        bonus.effects.forEach { effect ->
            when (effect) {
                is BonusEffect.AttributeBonus -> {
                    // Attribute werden über AttributeModifier gesteuert
                    // Diese werden bei Armor Changes automatisch neu berechnet
                }
                is BonusEffect.PotionBonus -> {
                    player.addPotionEffect(
                        PotionEffect(
                            effect.effect,
                            effect.duration,
                            effect.amplifier,
                            true,
                            false,
                            true
                        )
                    )
                }
                is BonusEffect.CritChanceBonus -> {
                    // Wird in CombatListener geprüft
                }
                is BonusEffect.CritDamageBonus -> {
                    // Wird in CombatListener geprüft
                }
                is BonusEffect.ElementalDamageBonus -> {
                    // Wird in CombatListener geprüft
                }
                is BonusEffect.AbilityBonus -> {
                    // Spezielle Fähigkeiten werden in AbilityListener gehandhabt
                }
            }
        }
    }

    /**
     * Entfernt einen einzelnen Bonus
     */
    private fun removeBonus(player: Player, bonus: SetBonus) {
        bonus.effects.forEach { effect ->
            when (effect) {
                is BonusEffect.PotionBonus -> {
                    player.removePotionEffect(effect.effect)
                }
                else -> {
                    // Andere Effekte werden automatisch entfernt
                }
            }
        }
    }

    /**
     * Update die Lore eines Items mit Set-Informationen
     */
    private fun updateSetLore(meta: org.bukkit.inventory.meta.ItemMeta, set: ArmorSet) {
        val lore = meta.lore()?.toMutableList() ?: mutableListOf()

        // Entferne alte Set-Lore
        lore.removeIf { component ->
            val plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(component)
            plain.contains("Set:") || plain.contains("✦")
        }

        // Füge Set-Informationen hinzu
        lore.add(Component.empty())
        lore.add(Component.text("Set: ${set.displayName}")
            .color(set.color)
            .decoration(TextDecoration.ITALIC, false))

        lore.add(Component.empty())
        lore.add(Component.text("✦ 2 Teile: ${set.twoPieceBonus.name}")
            .color(net.kyori.adventure.text.format.NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false))

        set.twoPieceBonus.effects.take(2).forEach { effect ->
            lore.add(Component.text("  ${getEffectDescription(effect)}")
                .color(net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false))
        }

        lore.add(Component.empty())
        lore.add(Component.text("✦✦ 4 Teile: ${set.fourPieceBonus.name}")
            .color(net.kyori.adventure.text.format.NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false))

        set.fourPieceBonus.effects.take(3).forEach { effect ->
            lore.add(Component.text("  ${getEffectDescription(effect)}")
                .color(net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false))
        }

        meta.lore(lore)
    }

    /**
     * Gibt eine Beschreibung eines Effekts zurück
     */
    private fun getEffectDescription(effect: BonusEffect): String {
        return when (effect) {
            is BonusEffect.AttributeBonus -> "+${effect.value} ${effect.attribute.key.key}"
            is BonusEffect.PotionBonus -> "${effect.effect.key.key} ${effect.amplifier + 1}"
            is BonusEffect.CritChanceBonus -> "+${effect.chance}% Krit-Chance"
            is BonusEffect.CritDamageBonus -> "+${effect.multiplier}% Krit-Schaden"
            is BonusEffect.ElementalDamageBonus -> "+${effect.bonus}% Elementar-Schaden"
            is BonusEffect.AbilityBonus -> "Fähigkeit: ${effect.abilityName}"
        }
    }

    /**
     * Gibt die aktiven Set-Boni eines Spielers zurück
     */
    fun getActiveSetBonuses(player: Player): Pair<ArmorSet, Int>? {
        return activeSetBonuses[player.uniqueId]
    }

    /**
     * Gibt die Krit-Chance eines Spielers von Set-Boni zurück
     */
    fun getCritChanceBonus(player: Player): Double {
        val (set, pieceCount) = activeSetBonuses[player.uniqueId] ?: return 0.0
        var bonus = 0.0

        if (pieceCount >= 2) {
            set.twoPieceBonus.effects.filterIsInstance<BonusEffect.CritChanceBonus>()
                .forEach { bonus += it.chance }
        }

        if (pieceCount >= 4) {
            set.fourPieceBonus.effects.filterIsInstance<BonusEffect.CritChanceBonus>()
                .forEach { bonus += it.chance }
        }

        return bonus
    }

    /**
     * Gibt den Krit-Schaden eines Spielers von Set-Boni zurück
     */
    fun getCritDamageBonus(player: Player): Double {
        val (set, pieceCount) = activeSetBonuses[player.uniqueId] ?: return 0.0
        var bonus = 0.0

        if (pieceCount >= 2) {
            set.twoPieceBonus.effects.filterIsInstance<BonusEffect.CritDamageBonus>()
                .forEach { bonus += it.multiplier }
        }

        if (pieceCount >= 4) {
            set.fourPieceBonus.effects.filterIsInstance<BonusEffect.CritDamageBonus>()
                .forEach { bonus += it.multiplier }
        }

        return bonus
    }

    /**
     * Cleanup beim Logout
     */
    fun cleanup(player: Player) {
        removeSetBonuses(player)
    }
}

