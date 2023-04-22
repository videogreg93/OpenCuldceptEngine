package models.cards

abstract class Card() {
    abstract val name: String
    abstract val baseCost: Int
    abstract val baseRarity: Int
}