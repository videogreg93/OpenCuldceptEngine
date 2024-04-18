package models.player

import models.cards.Card

class Player(
    val name: String,
    var currentGold: Int,
) {
    val library: ArrayList<Card> = ArrayList()
    val cardsInHand: ArrayList<Card> = ArrayList()
    val graveyard: ArrayList<Card> = ArrayList()

    fun attemptToPlayCard(card: Card): Boolean {
        if (!cardsInHand.contains(card)) return false
        if (currentGold < card.baseCost) return false
        currentGold -= card.baseCost
        cardsInHand.remove(card)
        graveyard.add(card)
        return true
    }

    fun addCardToHand(card: Card) {
        cardsInHand.add(card)
    }
}