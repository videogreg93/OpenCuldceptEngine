package models.cards.creature

import models.cards.Card
import models.cards.effects.CardEffect

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
    var currentHP: Int = baseMHP

    val attackFirst: Boolean
        get() {
            return hasEffect<CardEffect.AttackFirst>()
        }

    val attackLast: Boolean
        get() {
            return hasEffect<CardEffect.AttackLast>()
        }


    inline fun <reified T: CardEffect> hasEffect(): Boolean {
        return effects.any { it is T }
    }
}