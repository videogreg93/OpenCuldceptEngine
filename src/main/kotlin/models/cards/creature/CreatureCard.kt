package models.cards.creature

import models.cards.Card
import models.cards.effects.BattleEffects
import models.cards.effects.CardEffect
import models.cards.item.ItemCard
import models.cards.item.ItemType
import models.player.Player
import kotlin.math.min
import kotlin.reflect.KClass

class CreatureCard(
    override val name: String,
    val baseStrength: Int,
    val baseMHP: Int,
    val baseDescription: String,
    val element: List<CreatureElement>,
    val itemTypeRestrictions: List<ItemType> = emptyList(),
    override val baseCost: Int,
    override val baseRarity: Int
): Card() {
    val effects = ArrayList<CardEffect>()
    val addedEffects = ArrayList<CardEffect>()
    val allEffects: List<CardEffect>
        get() = effects + addedEffects
    val currentBattleEffects = ArrayList<BattleEffects>()
    var currentHP: Int = baseMHP
    var currentStrength = baseStrength
    var owner: Player? = null
    var currentLandHPBonus: Int = 0
    var healthBeforeBattle: Int = currentHP

    val isDestroyed: Boolean
        get() = currentHP <= 0

    init {
        // Most creatures have land bonus
        CardEffect.landBonus(this)
    }

    var attackFirst: Boolean = false

    var attackLast: Boolean = false

    fun hasElement(e: CreatureElement): Boolean {
        return element.contains(e)
    }

    fun canEquipCard(card: ItemCard): Boolean {
        if (itemTypeRestrictions.contains(card.type)) return false
        return true
    }

    inline fun <reified T: CardEffect> hasEffect(): Boolean {
        return allEffects.any { it is T }
    }

    inline fun <reified T: BattleEffects> hasBattleEffect(): Boolean {
        return currentBattleEffects.any { it is T }
    }

    inline fun <reified T: BattleEffects> getBattleEffect(): T {
        return currentBattleEffects.first { it is T } as T
    }

    fun hasEffect(effect: KClass<*>): Boolean {
        return allEffects.any { it::class == effect}
    }

    inline fun <reified T: CardEffect> effectsOfType(): List<T> {
        return allEffects.filterIsInstance<T>()
    }


    fun addEffect(effect: CardEffect) {
        effect.source = this
        effect.owner = this
        effects.add(effect)
    }

    fun addItemEffect(item: ItemCard, effect: CardEffect) {
        effect.source = item
        effect.owner = this
        effects.add(effect)
    }

    /**
     * Sets creature values back to their original values, normally after a battle concludes
     */
    fun resetValues() {
        currentStrength = baseStrength // TODO add ongoing modifiers, like permanent buffs or whatever
        currentHP = min(currentHP, healthBeforeBattle) // TODO this resets if the creature goes back to their hand
        currentBattleEffects.clear()
        addedEffects.clear()
    }

    fun addBattleEffect(effects: BattleEffects) {
        currentBattleEffects.add(effects)
    }
}