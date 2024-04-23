package models.cards

import kotlinx.serialization.Serializable

@Serializable
abstract class Card() {
    abstract val name: String
    abstract val baseCost: Int
    abstract val baseRarity: Int
}