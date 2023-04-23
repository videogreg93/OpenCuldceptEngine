package models.cards.creature

import models.cards.Card
import models.cards.effects.CardEffect
import models.cards.item.ItemCard

class CreatureCard(
    override val name: String,
    val baseStrength: Int,
    val baseMHP: Int,
    val baseDescription: String,
    val element: List<CreatureElement>,
    override val baseCost: Int,
    override val baseRarity: Int
): Card() {
    val effects = ArrayList<CardEffect>()
    val addedEffects = ArrayList<CardEffect>()
    val allEffects: List<CardEffect>
        get() = effects + addedEffects
    var currentHP: Int = baseMHP
    var currentStrength = baseStrength

    val attackFirst: Boolean
        get() {
            return hasEffect<CardEffect.AttackFirst>()
        }

    val attackLast: Boolean
        get() {
            return hasEffect<CardEffect.AttackLast>()
        }


    inline fun <reified T: CardEffect> hasEffect(): Boolean {
        return allEffects.any { it is T }
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
    protected fun resetValues() {
        currentStrength = baseStrength // TODO add ongoing modifiers, like permanent buffs or whatever
    }
}