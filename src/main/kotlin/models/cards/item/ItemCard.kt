package models.cards.item

import models.cards.Card

class ItemCard(
    override val name: String,
    override val baseCost: Int,
    override val baseRarity: Int,
): Card() {

    object EmptyItemCard
}