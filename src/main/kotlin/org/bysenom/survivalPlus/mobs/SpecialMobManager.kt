package org.bysenom.survivalPlus.mobs

import net.kyori.adventure.text.Component
import org.bysenom.survivalPlus.SurvivalPlus
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Verwaltet Special Mobs in der Welt
 */
class SpecialMobManager(private val plugin: SurvivalPlus) {

    // Entity UUID -> MobAffix
    private val specialMobs = ConcurrentHashMap<UUID, MobAffix>()

    // Schlüssel für Persistent Data
    private val affixKey = NamespacedKey(plugin, "mob_affix")

    // Max Special Mobs pro Welt
    private val maxPerWorld: Int
        get() = plugin.config.getInt("special-mobs.max-per-world", 10)

    /**
     * Erstellt einen Special Mob
     */
    fun createSpecialMob(entity: Mob, affix: MobAffix? = null): Boolean {
        // Prüfe ob Plugin in dieser Welt aktiv ist
        if (!plugin.worldTierManager.isEnabledWorld(entity.world)) {
            return false
        }

        // Prüfe Max-Limit
        val worldMobs = entity.world.entities.count {
            it is Mob && isSpecialMob(it)
        }

        if (worldMobs >= maxPerWorld) {
            return false
        }

        // Wähle zufälligen Affix wenn nicht angegeben
        val selectedAffix = affix ?: MobAffix.random()

        // Speichere Affix
        specialMobs[entity.uniqueId] = selectedAffix
        entity.persistentDataContainer.set(affixKey, PersistentDataType.STRING, selectedAffix.name)

        // Wende Modifikationen an
        applyAffixModifications(entity, selectedAffix)

        return true
    }

    /**
     * Prüft ob ein Entity ein Special Mob ist
     */
    fun isSpecialMob(entity: LivingEntity): Boolean {
        return entity.persistentDataContainer.has(affixKey)
    }

    /**
     * Gibt den Affix eines Special Mobs zurück
     */
    fun getMobAffix(entity: LivingEntity): MobAffix? {
        val affixName = entity.persistentDataContainer.get(affixKey, PersistentDataType.STRING)
        return affixName?.let {
            try {
                MobAffix.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Wendet Affix-Modifikationen auf einen Mob an
     */
    private fun applyAffixModifications(entity: Mob, affix: MobAffix) {
        // Gesundheit
        val maxHealth = entity.getAttribute(Attribute.MAX_HEALTH)
        maxHealth?.let {
            val newHealth = it.baseValue * affix.healthMultiplier
            it.baseValue = newHealth
            entity.health = newHealth
        }

        // Schaden
        val attackDamage = entity.getAttribute(Attribute.ATTACK_DAMAGE)
        attackDamage?.let {
            it.baseValue *= affix.damageMultiplier
        }

        // Geschwindigkeit
        val movementSpeed = entity.getAttribute(Attribute.MOVEMENT_SPEED)
        movementSpeed?.let {
            it.baseValue *= affix.speedMultiplier
        }

        // Custom Name mit Farbe
        entity.customName(affix.getNameWithPrefix(entity.type.name))
        entity.isCustomNameVisible = true

        // Passive Fähigkeiten anwenden
        affix.abilities.forEach { ability ->
            ability.applyPassive(entity)
        }

        // Partikel-Effekt spawnen
        spawnAffixParticles(entity, affix)

        plugin.logger.fine("Special Mob erstellt: ${affix.displayName} ${entity.type.name} bei ${entity.location}")
    }

    /**
     * Spawnt Partikel für Special Mob Affix
     */
    private fun spawnAffixParticles(entity: LivingEntity, affix: MobAffix) {
        val location = entity.location.add(0.0, 1.0, 0.0)
        val world = entity.world

        when (affix) {
            MobAffix.BERSERKER -> {
                world.spawnParticle(org.bukkit.Particle.FLAME, location, 10, 0.3, 0.5, 0.3, 0.02)
            }
            MobAffix.FROSTWARDEN -> {
                world.spawnParticle(org.bukkit.Particle.SNOWFLAKE, location, 10, 0.3, 0.5, 0.3, 0.02)
            }
            MobAffix.STORMBRINGER -> {
                world.spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, location, 10, 0.3, 0.5, 0.3, 0.05)
            }
            MobAffix.VENOMOUS -> {
                world.spawnParticle(org.bukkit.Particle.ITEM_SLIME, location, 10, 0.3, 0.5, 0.3, 0.02)
            }
            MobAffix.VAMPIRIC -> {
                world.spawnParticle(org.bukkit.Particle.CRIMSON_SPORE, location, 10, 0.3, 0.5, 0.3, 0.02)
            }
            MobAffix.SHADOW -> {
                world.spawnParticle(org.bukkit.Particle.SMOKE, location, 10, 0.3, 0.5, 0.3, 0.02)
            }
            MobAffix.MOLTEN -> {
                world.spawnParticle(org.bukkit.Particle.LAVA, location, 5, 0.3, 0.5, 0.3, 0.02)
            }
        }
    }

    /**
     * Entfernt einen Special Mob aus dem Tracking
     */
    fun removeSpecialMob(uuid: UUID) {
        specialMobs.remove(uuid)
    }

    /**
     * Gibt alle aktiven Special Mobs zurück
     */
    fun getActiveSpecialMobs(): Map<UUID, MobAffix> {
        return specialMobs.toMap()
    }

    /**
     * Gibt die Anzahl aktiver Special Mobs in einer Welt zurück
     */
    fun getSpecialMobCount(worldUUID: UUID): Int {
        return specialMobs.keys.count { uuid ->
            plugin.server.getEntity(uuid)?.world?.uid == worldUUID
        }
    }

    /**
     * Cleanup - Entfernt tote Mobs aus dem Tracking
     */
    fun cleanup() {
        val toRemove = mutableListOf<UUID>()

        specialMobs.keys.forEach { uuid ->
            val entity = plugin.server.getEntity(uuid)
            if (entity == null || entity !is LivingEntity || entity.isDead) {
                toRemove.add(uuid)
            }
        }

        toRemove.forEach { specialMobs.remove(it) }

        if (toRemove.isNotEmpty()) {
            plugin.logger.fine("${toRemove.size} tote Special Mobs aus Tracking entfernt")
        }
    }
    
    /**
     * Gibt die Anzahl aktiver Special Mobs zurück (für Debug)
     */
    fun getActiveMobCount(): Int {
        // Zähle nur noch lebende Mobs
        return specialMobs.keys.count { uuid ->
            val entity = plugin.server.getEntity(uuid)
            entity != null && entity is LivingEntity && !entity.isDead
        }
    }
}

