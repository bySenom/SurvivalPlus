package org.bysenom.survivalPlus.worldtier

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent

class WorldTierListener(private val plugin: SurvivalPlus) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val world = player.world
        if (!plugin.worldTierManager.isEnabledWorld(world)) return

        val tier = plugin.worldTierManager.getWorldTier(world)
        player.sendMessage(Component.text("Willkommen in ").color(NamedTextColor.GRAY)
            .append(Component.text(world.name).color(NamedTextColor.GOLD))
            .append(Component.text(" - World Tier: ").color(NamedTextColor.GRAY))
            .append(tier.getDisplayComponent()))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        val player = event.player
        val world = player.world
        if (!plugin.worldTierManager.isEnabledWorld(world)) return

        val tier = plugin.worldTierManager.getWorldTier(world)
        player.sendMessage(Component.text("Du bist jetzt in ").color(NamedTextColor.GRAY)
            .append(Component.text(world.name).color(NamedTextColor.GOLD))
            .append(Component.text(" - World Tier: ").color(NamedTextColor.GRAY))
            .append(tier.getDisplayComponent()))
        player.playSound(player.location, org.bukkit.Sound.UI_TOAST_IN, 0.5f, 1f)
    }
}

