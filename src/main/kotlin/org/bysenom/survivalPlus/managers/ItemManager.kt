package org.bysenom.survivalPlus.managers

import org.bysenom.survivalPlus.SurvivalPlus
import org.bysenom.survivalPlus.models.CustomItem
import org.bysenom.survivalPlus.models.Quality
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.lang.ref.SoftReference
import java.util.concurrent.ConcurrentHashMap

/**
 * Verwaltet alle Custom Items und deren Operationen
 */
class ItemManager(private val plugin: SurvivalPlus) {

    private val qualityKey = NamespacedKey(plugin, "quality")
    
    // Item-Cache mit SoftReferences (werden bei Memory-Druck automatisch freigegeben)
    private val itemCache = ConcurrentHashMap<String, SoftReference<ItemStack>>()

    /**
     * Erstellt ein neues Custom Item mit zufälliger Qualität
     */
    fun createRandomItem(material: Material): ItemStack {
        val quality = Quality.random()
        return createItem(material, quality)
    }

    /**
     * Erstellt ein neues Custom Item mit World-Tier Boost
     */
    fun createRandomItemWithWorldBoost(material: Material, world: org.bukkit.World): ItemStack {
        val worldTier = plugin.worldTierManager.getWorldTier(world)
        val qualityBoost = worldTier.dropQualityBoost

        val quality = Quality.randomWithBoost(qualityBoost)
        return createItem(material, quality)
    }

    /**
     * Erstellt ein Custom Item mit spezifischer Qualität
     */
    fun createItem(material: Material, quality: Quality): ItemStack {
        // Prüfe Cache zuerst
        val cacheKey = "${material.name}_${quality.name}"
        
        // Wenn im Cache, clone das Item (um Referenz-Probleme zu vermeiden)
        itemCache[cacheKey]?.get()?.let { cachedItem ->
            return cachedItem.clone()
        }
        
        // Erstelle neues Item
        val customItem = CustomItem(material, quality)

        // Basis-Stats basierend auf Item-Typ und Material generieren
        val materialType = when {
            material.name.contains("NETHERITE") -> "NETHERITE"
            material.name.contains("DIAMOND") -> "DIAMOND"
            material.name.contains("IRON") || material.name.contains("CHAINMAIL") -> "IRON"
            material.name.contains("GOLDEN") -> "GOLD"
            material.name.contains("COPPER") -> "COPPER"  // Für zukünftige Copper-Tools/Armor
            material.name.contains("STONE") -> "STONE"
            material.name.contains("WOODEN") -> "WOOD"
            material.name.contains("LEATHER") -> "LEATHER"
            material == Material.TURTLE_HELMET -> "TURTLE"
            else -> "DEFAULT"
        }

        when {
            material.name.contains("SWORD") -> {
                customItem.stats["Schaden"] = when (materialType) {
                    "NETHERITE" -> 8.0
                    "DIAMOND" -> 7.0
                    "IRON" -> 6.0
                    "COPPER" -> 5.5  // Zwischen Stein und Eisen
                    "GOLD" -> 4.0
                    "STONE" -> 5.0
                    "WOOD" -> 4.0
                    else -> 5.0
                }
                customItem.stats["Angriffsgeschwindigkeit"] = 1.6
            }
            material.name.contains("AXE") -> {
                customItem.stats["Schaden"] = when (materialType) {
                    "NETHERITE" -> 10.0
                    "DIAMOND" -> 9.0
                    "IRON" -> 9.0
                    "COPPER" -> 8.0  // Zwischen Stein und Eisen
                    "GOLD" -> 7.0
                    "STONE" -> 9.0
                    "WOOD" -> 7.0
                    else -> 7.0
                }
                customItem.stats["Angriffsgeschwindigkeit"] = when (materialType) {
                    "NETHERITE", "DIAMOND" -> 1.0
                    "IRON", "STONE", "COPPER" -> 0.9
                    "GOLD", "WOOD" -> 0.8
                    else -> 1.0
                }
            }
            material.name.contains("PICKAXE") -> {
                customItem.stats["Abbaugeschwindigkeit"] = when (materialType) {
                    "NETHERITE" -> 9.0
                    "DIAMOND" -> 8.0
                    "IRON" -> 6.0
                    "COPPER" -> 5.0  // Zwischen Stein und Eisen
                    "GOLD" -> 12.0  // Gold ist schnell
                    "STONE" -> 4.0
                    "WOOD" -> 2.0
                    else -> 6.0
                }
                customItem.stats["Effizienz"] = 1.0
            }
            material.name.contains("SHOVEL") -> {
                customItem.stats["Abbaugeschwindigkeit"] = when (materialType) {
                    "NETHERITE" -> 9.0
                    "DIAMOND" -> 8.0
                    "IRON" -> 6.0
                    "COPPER" -> 5.0  // Zwischen Stein und Eisen
                    "GOLD" -> 12.0
                    "STONE" -> 4.0
                    "WOOD" -> 2.0
                    else -> 5.5
                }
            }
            material.name.contains("HOE") -> {
                customItem.stats["Angriffsgeschwindigkeit"] = when (materialType) {
                    "NETHERITE", "DIAMOND" -> 4.0
                    "IRON", "GOLD", "COPPER" -> 3.0
                    "STONE", "WOOD" -> 2.0
                    else -> 1.0
                }
            }
            material == Material.BOW -> {
                customItem.stats["Zugkraft"] = 1.0
                customItem.stats["Schaden"] = 9.0
            }
            material == Material.CROSSBOW -> {
                customItem.stats["Zugkraft"] = 1.25
                customItem.stats["Schaden"] = 11.0
            }
            material == Material.TRIDENT -> {
                customItem.stats["Schaden"] = 9.0
                customItem.stats["Wurf-Schaden"] = 8.0
            }
            material.name.contains("HELMET") || material == Material.TURTLE_HELMET -> {
                customItem.stats["Rüstung"] = when (materialType) {
                    "NETHERITE", "DIAMOND" -> 3.0
                    "IRON", "CHAINMAIL" -> 2.0
                    "TURTLE" -> 2.0  // Turtle Shell = Iron-Level
                    "COPPER" -> 2.0  // Copper = Iron-Level
                    "GOLD", "LEATHER" -> 2.0
                    else -> 1.0
                }
                customItem.stats["Härte"] = when (materialType) {
                    "NETHERITE" -> 3.0
                    "DIAMOND" -> 2.0
                    "TURTLE" -> 0.5  // Turtle Shell hat etwas Härte
                    else -> 0.0
                }
            }
            material.name.contains("CHESTPLATE") -> {
                customItem.stats["Rüstung"] = when (materialType) {
                    "NETHERITE", "DIAMOND" -> 8.0
                    "IRON", "CHAINMAIL" -> 6.0
                    "COPPER" -> 5.5  // Zwischen Gold und Iron
                    "GOLD" -> 5.0
                    "LEATHER" -> 3.0
                    else -> 4.0
                }
                customItem.stats["Härte"] = when (materialType) {
                    "NETHERITE" -> 3.0
                    "DIAMOND" -> 2.0
                    else -> 0.0
                }
            }
            material.name.contains("LEGGINGS") -> {
                customItem.stats["Rüstung"] = when (materialType) {
                    "NETHERITE", "DIAMOND" -> 6.0
                    "IRON", "CHAINMAIL" -> 5.0
                    "COPPER" -> 4.0  // Zwischen Gold und Iron
                    "GOLD" -> 3.0
                    "LEATHER" -> 2.0
                    else -> 3.0
                }
                customItem.stats["Härte"] = when (materialType) {
                    "NETHERITE" -> 3.0
                    "DIAMOND" -> 2.0
                    else -> 0.0
                }
            }
            material.name.contains("BOOTS") -> {
                customItem.stats["Rüstung"] = when (materialType) {
                    "NETHERITE", "DIAMOND" -> 3.0
                    "IRON", "CHAINMAIL" -> 2.0
                    "COPPER" -> 1.5  // Zwischen Leather und Iron
                    "GOLD", "LEATHER" -> 1.0
                    else -> 1.0
                }
                customItem.stats["Härte"] = when (materialType) {
                    "NETHERITE" -> 3.0
                    "DIAMOND" -> 2.0
                    else -> 0.0
                }
            }
            material == Material.SHIELD -> {
                customItem.stats["Blockstärke"] = 100.0
            }
        }

        // Qualitäts-Multiplikator anwenden
        customItem.applyQualityMultiplier()

        val itemStack = customItem.toItemStack()

        // Qualität in Persistent Data speichern
        val meta = itemStack.itemMeta ?: return itemStack
        meta.persistentDataContainer.set(qualityKey, PersistentDataType.STRING, quality.name)

        // Enchantment Glint für Epic+ Items
        if (quality.tier >= 4) { // Epic, Legendary, Mythic
            meta.setEnchantmentGlintOverride(true)
        }

        itemStack.itemMeta = meta
        
        // Speichere im Cache (SoftReference erlaubt GC bei Memory-Druck)
        itemCache[cacheKey] = SoftReference(itemStack)

        return itemStack
    }

    /**
     * Holt die Qualität eines ItemStacks
     */
    fun getQuality(item: ItemStack): Quality? {
        val meta = item.itemMeta ?: return null
        val qualityName = meta.persistentDataContainer.get(qualityKey, PersistentDataType.STRING) ?: return null
        return Quality.fromName(qualityName)
    }

    /**
     * Prüft ob ein Item ein Custom Item ist
     */
    fun isCustomItem(item: ItemStack): Boolean {
        return getQuality(item) != null
    }

    /**
     * Setzt die Qualität eines Items (für Reforging)
     */
    fun setQuality(item: ItemStack, quality: Quality): ItemStack {
        // Item neu erstellen mit neuer Qualität
        return createItem(item.type, quality)
    }
    
    /**
     * Leert den Item-Cache (für Reload oder Memory-Management)
     */
    fun clearCache() {
        itemCache.clear()
        plugin.logger.info("✓ Item-Cache geleert (${itemCache.size} Einträge)")
    }
    
    /**
     * Gibt die Anzahl gecachter Items zurück (für Debug)
     */
    fun getCacheSize(): Int {
        // Zähle nur noch gültige References
        return itemCache.values.count { it.get() != null }
    }
    
    /**
     * Gibt Cache-Statistiken zurück (für Debug)
     */
    fun getCacheStats(): String {
        val total = itemCache.size
        val valid = itemCache.values.count { it.get() != null }
        val invalid = total - valid
        return "Cache: $valid gültig, $invalid freigegeben, $total total"
    }
}

