package org.bysenom.survivalPlus.enchantments

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class EnchantmentManager(private val plugin: SurvivalPlus) {

    private val enchantmentPrefix = "customench_"

    /**
     * Fügt ein Custom Enchantment zu einem Item hinzu
     */
    fun addEnchantment(item: ItemStack, enchantment: CustomEnchantment, level: Int): ItemStack {
        require(level in 1..enchantment.maxLevel) {
            "Level muss zwischen 1 und ${enchantment.maxLevel} sein"
        }

        val meta = item.itemMeta ?: return item

        // Speichere Enchantment in Persistent Data
        val key = NamespacedKey(plugin, "$enchantmentPrefix${enchantment.name.lowercase()}")
        meta.persistentDataContainer.set(key, PersistentDataType.INTEGER, level)

        // Update Lore
        updateLore(meta, item)

        item.itemMeta = meta
        return item
    }

    /**
     * Entfernt ein Custom Enchantment von einem Item
     */
    fun removeEnchantment(item: ItemStack, enchantment: CustomEnchantment): ItemStack {
        val meta = item.itemMeta ?: return item

        val key = NamespacedKey(plugin, "$enchantmentPrefix${enchantment.name.lowercase()}")
        meta.persistentDataContainer.remove(key)

        updateLore(meta, item)

        item.itemMeta = meta
        return item
    }

    /**
     * Holt das Level eines Custom Enchantments von einem Item
     */
    fun getEnchantmentLevel(item: ItemStack, enchantment: CustomEnchantment): Int {
        val meta = item.itemMeta ?: return 0

        val key = NamespacedKey(plugin, "$enchantmentPrefix${enchantment.name.lowercase()}")
        return meta.persistentDataContainer.get(key, PersistentDataType.INTEGER) ?: 0
    }

    /**
     * Gibt alle Custom Enchantments auf einem Item zurück
     */
    fun getEnchantments(item: ItemStack): Map<CustomEnchantment, Int> {
        val meta = item.itemMeta ?: return emptyMap()
        val enchantments = mutableMapOf<CustomEnchantment, Int>()

        CustomEnchantment.entries.forEach { enchantment ->
            val level = getEnchantmentLevel(item, enchantment)
            if (level > 0) {
                enchantments[enchantment] = level
            }
        }

        return enchantments
    }

    /**
     * Prüft ob ein Item ein bestimmtes Custom Enchantment hat
     */
    fun hasEnchantment(item: ItemStack, enchantment: CustomEnchantment): Boolean {
        return getEnchantmentLevel(item, enchantment) > 0
    }

    /**
     * Update die Lore eines Items um Custom Enchantments anzuzeigen
     */
    private fun updateLore(meta: org.bukkit.inventory.meta.ItemMeta, item: ItemStack) {
        val currentLore = meta.lore()?.toMutableList() ?: mutableListOf()

        // Finde den Index wo Custom Enchantments beginnen (nach Separator)
        var enchantStartIndex = -1
        var enchantEndIndex = -1

        for (i in currentLore.indices) {
            val plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(currentLore[i])

            // Finde Start (leere Zeile vor Enchantments oder erstes Enchantment)
            if (enchantStartIndex == -1 && CustomEnchantment.entries.any { plain.contains(it.displayName) }) {
                // Prüfe ob vorherige Zeile leer ist
                if (i > 0) {
                    val prevPlain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                        .serialize(currentLore[i - 1])
                    if (prevPlain.isEmpty()) {
                        enchantStartIndex = i - 1 // Include separator
                    } else {
                        enchantStartIndex = i
                    }
                } else {
                    enchantStartIndex = i
                }
            }

            // Finde Ende (letztes Enchantment oder Beschreibung)
            if (enchantStartIndex != -1 &&
                (CustomEnchantment.entries.any { plain.contains(it.displayName) } || plain.startsWith("  "))) {
                enchantEndIndex = i
            }
        }

        // Entferne alte Enchantment-Lore
        if (enchantStartIndex != -1 && enchantEndIndex != -1) {
            for (i in enchantEndIndex downTo enchantStartIndex) {
                currentLore.removeAt(i)
            }
        }

        // Finde alle Enchantments auf dem Item
        val enchantments = getEnchantments(item)

        if (enchantments.isNotEmpty()) {
            val showDescriptions = plugin.config.getBoolean("features.enchantment-descriptions", true)

            // Füge Separator nur wenn Lore nicht leer ist und kein Separator am Ende
            if (currentLore.isNotEmpty()) {
                val lastPlain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                    .serialize(currentLore.last())
                if (lastPlain.isNotEmpty()) {
                    currentLore.add(Component.empty())
                }
            }

            // Füge Enchantments zur Lore hinzu
            enchantments.forEach { (enchantment, level) ->
                val romanLevel = toRoman(level)
                val levelText = if (enchantment.maxLevel > 1) " $romanLevel" else ""

                val component = Component.text("${enchantment.displayName}$levelText")
                    .color(enchantment.color)
                    .decoration(TextDecoration.ITALIC, false)

                currentLore.add(component)

                // Füge Beschreibung direkt danach hinzu (kein extra Spacing)
                if (showDescriptions) {
                    val description = Component.text("  ${enchantment.description}")
                        .color(net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                    currentLore.add(description)
                }
            }
        }

        meta.lore(currentLore)
    }

    /**
     * Konvertiert eine Zahl zu römischen Ziffern
     */
    private fun toRoman(num: Int): String {
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
     * Gibt ein zufälliges Enchantment für ein Item basierend auf Qualität zurück
     */
    fun getRandomEnchantmentForItem(item: ItemStack, quality: org.bysenom.survivalPlus.models.Quality): CustomEnchantment? {
        val itemType = ItemType.fromMaterial(item.type) ?: return null
        val applicable = CustomEnchantment.getApplicableEnchantments(itemType, quality)

        if (applicable.isEmpty()) return null

        return applicable.random()
    }

    /**
     * Fügt ein zufälliges Enchantment zu einem Item hinzu
     */
    fun addRandomEnchantment(item: ItemStack, quality: org.bysenom.survivalPlus.models.Quality): ItemStack {
        val enchantment = getRandomEnchantmentForItem(item, quality) ?: return item
        val level = (1..enchantment.maxLevel).random()

        return addEnchantment(item, enchantment, level)
    }

    /**
     * Fügt mehrere zufällige Enchantments basierend auf Qualität hinzu
     */
    fun addRandomEnchantments(item: ItemStack, quality: org.bysenom.survivalPlus.models.Quality): ItemStack {
        val itemType = ItemType.fromMaterial(item.type) ?: return item
        var resultItem = item

        // Anzahl der Enchantments basierend auf Qualität
        val enchantmentCount = when (quality) {
            org.bysenom.survivalPlus.models.Quality.COMMON -> 0
            org.bysenom.survivalPlus.models.Quality.UNCOMMON -> 0
            org.bysenom.survivalPlus.models.Quality.RARE -> if (Math.random() < 0.3) 1 else 0
            org.bysenom.survivalPlus.models.Quality.EPIC -> if (Math.random() < 0.5) 1 else 0
            org.bysenom.survivalPlus.models.Quality.LEGENDARY -> (1..2).random()
            org.bysenom.survivalPlus.models.Quality.MYTHIC -> (2..3).random()
        }

        val applicable = CustomEnchantment.getApplicableEnchantments(itemType, quality)
            .shuffled()
            .take(enchantmentCount)

        applicable.forEach { enchantment ->
            val level = (1..enchantment.maxLevel).random()
            resultItem = addEnchantment(resultItem, enchantment, level)
        }

        return resultItem
    }

    /**
     * Erstellt ein zufälliges Custom Enchanted Book
     */
    fun createRandomEnchantedBook(quality: org.bysenom.survivalPlus.models.Quality): ItemStack {
        val applicableEnchants = CustomEnchantment.entries
            .filter { it.minQuality.tier <= quality.tier }

        if (applicableEnchants.isEmpty()) {
            // Fallback zu Uncommon wenn keine gefunden
            return createRandomEnchantedBook(org.bysenom.survivalPlus.models.Quality.UNCOMMON)
        }

        val enchant = applicableEnchants.random()
        val level = (1..enchant.maxLevel).random()

        val book = ItemStack(org.bukkit.Material.ENCHANTED_BOOK)
        return addEnchantment(book, enchant, level)
    }
}

