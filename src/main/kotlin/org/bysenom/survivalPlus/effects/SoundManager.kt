package org.bysenom.survivalPlus.effects

import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.Quality
import org.bukkit.Sound
import org.bukkit.entity.Player

class SoundManager(private val plugin: SurvivalPlus) {

    private val soundsEnabled: Boolean
        get() = plugin.config.getBoolean("features.sound-effects", true)

    private val volume: Float
        get() = plugin.config.getDouble("sound.volume", 1.0).toFloat()

    /**
     * Plays sound when a quality item drops
     */
    fun playItemDropSound(player: Player, quality: Quality) {
        if (!soundsEnabled) return

        when (quality) {
            Quality.COMMON -> {
                // No special sound
            }
            Quality.UNCOMMON -> {
                player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 1.0f)
            }
            Quality.RARE -> {
                player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 1.2f)
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, volume * 0.5f, 1.5f)
            }
            Quality.EPIC -> {
                player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, volume, 1.0f)
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, volume * 0.7f, 2.0f)
            }
            Quality.LEGENDARY -> {
                player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, volume, 1.2f)
                player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, volume, 1.0f)
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_CHIME, volume, 1.8f)
            }
            Quality.MYTHIC -> {
                player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, volume * 0.5f, 2.0f)
                player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, volume, 1.2f)
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_CHIME, volume, 2.0f)
                player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, volume, 1.5f)
            }
        }
    }

    /**
     * Plays sound when reforging succeeds
     */
    fun playReforgingSuccessSound(player: Player, quality: Quality) {
        if (!soundsEnabled) return

        player.playSound(player.location, Sound.BLOCK_ANVIL_USE, volume, 1.0f)

        when (quality) {
            Quality.MYTHIC -> {
                player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, volume, 1.5f)
                player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, volume, 1.2f)
            }
            Quality.LEGENDARY -> {
                player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, volume, 1.3f)
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, volume, 2.0f)
            }
            Quality.EPIC -> {
                player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, volume, 1.1f)
            }
            else -> {
                player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, 1.0f)
            }
        }
    }

    /**
     * Plays sound when reforging fails
     */
    fun playReforgingFailSound(player: Player) {
        if (!soundsEnabled) return

        player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, volume, 1.0f)
        player.playSound(player.location, Sound.BLOCK_ANVIL_LAND, volume * 0.5f, 0.8f)
    }

    /**
     * Plays sound when equipping a high-quality item
     */
    fun playEquipSound(player: Player, quality: Quality) {
        if (!soundsEnabled) return
        if (quality.tier < 5) return // Only for Legendary and Mythic

        when (quality) {
            Quality.LEGENDARY -> {
                player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_NETHERITE, volume, 1.2f)
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_CHIME, volume * 0.5f, 1.5f)
            }
            Quality.MYTHIC -> {
                player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_NETHERITE, volume, 1.5f)
                player.playSound(player.location, Sound.BLOCK_BEACON_ACTIVATE, volume * 0.3f, 2.0f)
            }
            else -> {}
        }
    }

    /**
     * Plays UI click sound
     */
    fun playClickSound(player: Player) {
        if (!soundsEnabled) return
        player.playSound(player.location, Sound.UI_BUTTON_CLICK, volume, 1.0f)
    }

    /**
     * Plays error sound
     */
    fun playErrorSound(player: Player) {
        if (!soundsEnabled) return
        player.playSound(player.location, Sound.ENTITY_VILLAGER_NO, volume, 1.0f)
    }
}

