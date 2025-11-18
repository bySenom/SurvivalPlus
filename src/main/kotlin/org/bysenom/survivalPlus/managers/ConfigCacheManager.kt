package org.bysenom.survivalPlus.managers

import org.bysenom.survivalPlus.SurvivalPlus
import java.util.concurrent.ConcurrentHashMap

/**
 * Cacht Config-Werte für bessere Performance
 * Verhindert wiederholtes Lesen der config.yml
 */
class ConfigCacheManager(private val plugin: SurvivalPlus) {

    private val cache = ConcurrentHashMap<String, Any>()

    /**
     * Holt einen Config-Wert aus dem Cache oder lädt ihn
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, default: T): T {
        return cache.getOrPut(key) {
            when (default) {
                is Int -> plugin.config.getInt(key, default)
                is Boolean -> plugin.config.getBoolean(key, default)
                is Double -> plugin.config.getDouble(key, default)
                is Long -> plugin.config.getLong(key, default)
                is String -> plugin.config.getString(key) ?: default
                is List<*> -> plugin.config.getStringList(key)
                else -> default
            }
        } as T
    }

    /**
     * Holt einen String-Wert
     */
    fun getString(key: String, default: String): String {
        return get(key, default)
    }

    /**
     * Holt einen Int-Wert
     */
    fun getInt(key: String, default: Int): Int {
        return get(key, default)
    }

    /**
     * Holt einen Boolean-Wert
     */
    fun getBoolean(key: String, default: Boolean): Boolean {
        return get(key, default)
    }

    /**
     * Holt einen Double-Wert
     */
    fun getDouble(key: String, default: Double): Double {
        return get(key, default)
    }

    /**
     * Holt einen Long-Wert
     */
    fun getLong(key: String, default: Long): Long {
        return get(key, default)
    }

    /**
     * Holt eine String-Liste
     */
    fun getStringList(key: String): List<String> {
        return get(key, emptyList<String>())
    }

    /**
     * Lädt die Config neu und leert den Cache
     */
    fun reload() {
        cache.clear()
        plugin.reloadConfig()
        plugin.logger.info("✓ Config-Cache geleert und neu geladen")
    }

    /**
     * Gibt die Anzahl gecachter Werte zurück (für Debug)
     */
    fun getCacheSize(): Int = cache.size

    /**
     * Leert den Cache ohne Config neu zu laden
     */
    fun clearCache() {
        cache.clear()
    }

    /**
     * Prüft ob ein Key im Cache ist
     */
    fun isCached(key: String): Boolean = cache.containsKey(key)

    /**
     * Gibt alle gecachten Keys zurück (für Debug)
     */
    fun getCachedKeys(): Set<String> = cache.keys.toSet()
}
