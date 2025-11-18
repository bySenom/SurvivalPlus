package org.bysenom.survivalPlus.blocks

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import org.bysenom.survivalPlus.SurvivalPlus

/**
 * Registriert alle Custom Block Rezepte
 */
class CustomBlockRecipes(private val plugin: SurvivalPlus) {

    /**
     * Registriert alle Rezepte
     */
    fun registerRecipes() {
        registerCustomAnvilRecipe()
        registerReforgingStationRecipe()
        registerWorldTierAltarRecipe()

        plugin.logger.info("Custom Block Rezepte registriert!")
    }

    /**
     * Custom Anvil Rezept
     * Rezept:
     *   N N N
     *   N D N
     *   S S S
     *
     * N = Netherite Ingot
     * D = Diamond Block
     * S = Nether Star
     */
    private fun registerCustomAnvilRecipe() {
        val result = CustomBlock.CUSTOM_ANVIL.createItem()

        val key = NamespacedKey(plugin, "custom_anvil")
        val recipe = ShapedRecipe(key, result)

        recipe.shape(
            "NNN",
            "NDN",
            "SSS"
        )

        recipe.setIngredient('N', Material.NETHERITE_INGOT)
        recipe.setIngredient('D', Material.DIAMOND_BLOCK)
        recipe.setIngredient('S', Material.NETHER_STAR)

        try {
            plugin.server.addRecipe(recipe)
            plugin.logger.info("✓ Custom Anvil Rezept registriert!")
        } catch (e: Exception) {
            plugin.logger.warning("Fehler beim Registrieren des Custom Anvil Rezepts: ${e.message}")
        }
    }

    /**
     * Reforging Station Rezept
     * Rezept:
     *   E E E
     *   E A E
     *   O O O
     *
     * E = Emerald Block
     * A = Anvil
     * O = Obsidian
     */
    private fun registerReforgingStationRecipe() {
        val result = CustomBlock.REFORGING_STATION.createItem()

        val key = NamespacedKey(plugin, "reforging_station")
        val recipe = ShapedRecipe(key, result)

        recipe.shape(
            "EEE",
            "EAE",
            "OOO"
        )

        recipe.setIngredient('E', Material.EMERALD_BLOCK)
        recipe.setIngredient('A', Material.ANVIL)
        recipe.setIngredient('O', Material.OBSIDIAN)

        try {
            plugin.server.addRecipe(recipe)
            plugin.logger.info("✓ Reforging Station Rezept registriert!")
        } catch (e: Exception) {
            plugin.logger.warning("Fehler beim Registrieren des Reforging Station Rezepts: ${e.message}")
        }
    }

    /**
     * World Tier Altar Rezept
     * Rezept:
     *   D D D
     *   D L D
     *   N N N
     *
     * D = Diamond Block
     * L = Lodestone
     * N = Netherite Ingot
     */
    private fun registerWorldTierAltarRecipe() {
        val result = CustomBlock.WORLD_TIER_ALTAR.createItem()

        val key = NamespacedKey(plugin, "world_tier_altar")
        val recipe = ShapedRecipe(key, result)

        recipe.shape(
            "DDD",
            "DLD",
            "NNN"
        )

        recipe.setIngredient('D', Material.DIAMOND_BLOCK)
        recipe.setIngredient('L', Material.LODESTONE)
        recipe.setIngredient('N', Material.NETHERITE_INGOT)

        try {
            plugin.server.addRecipe(recipe)
            plugin.logger.info("✓ World Tier Altar Rezept registriert!")
        } catch (e: Exception) {
            plugin.logger.warning("Fehler beim Registrieren des World Tier Altar Rezepts: ${e.message}")
        }
    }

    /**
     * Entfernt alle Rezepte (für Reload)
     */
    fun unregisterRecipes() {
        plugin.server.removeRecipe(NamespacedKey(plugin, "custom_anvil"))
        plugin.server.removeRecipe(NamespacedKey(plugin, "reforging_station"))
        plugin.server.removeRecipe(NamespacedKey(plugin, "world_tier_altar"))
    }
}

