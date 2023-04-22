package models.cards.effects

sealed class CardEffect(val description: String) {
    object AttackFirst: CardEffect("Attack First")
    object AttackLast: CardEffect("Attack Last")
}