package models.cards.item

import models.cards.Card
import models.cards.creature.CreatureCard
import models.cards.effects.CardEffect

class ItemCard(
    override val name: String,
    override val baseCost: Int,
    override val baseRarity: Int,
    val type: ItemType = ItemType.Weapon
): Card() {
    val effects: ArrayList<CardEffect> = ArrayList()

    fun addEffect(effect: CardEffect) {
        effect.source = this
        effects.add(effect)
    }

    fun setOwnerForEffects(owner: CreatureCard) {
        effects.forEach { it.owner = owner }
    }

    fun resetOwner() {
        effects.forEach { it.owner = null }
    }

    object EmptyItemCard
}