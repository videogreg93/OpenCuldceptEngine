package models.cards

import models.cards.creature.CreatureCard
import models.cards.creature.CreatureElement
import models.cards.effects.CardEffect
import models.cards.effects.Conditional
import models.cards.effects.IntCalculation
import models.cards.effects.toConstantCalculation
import models.cards.item.ItemCard
import models.cards.item.ItemType

/**
 * See here for testing out items
 * https://culdceptcentral.com/culdcept-saga/culdcept-saga-card-lists/culdcept-saga-item-cards
 */

val batteringRam = ItemCard(
    "BatteringRam",
    30,
    1
).apply {
    addEffect(CardEffect.BeforeBattle(CardEffect.ModifyOwnerAttack(20.toConstantCalculation())))
    addEffect(
        CardEffect.AttackBonus(
            CardEffect.ConditionalEffect(
                Conditional.OpponentHasEffect(CardEffect.Defensive::class),
                onTrue = CardEffect.SetOpponentHealth(0.toConstantCalculation()), // TODO maybe we need to use InstantDeath effect
                onFalse = null
            )
        )
    )
}

val buckler = ItemCard(
    "Buckler",
    30,
    1,
    type = ItemType.Armor
).apply {
    addEffect(
        CardEffect.BeforeBattle(
            CardEffect.ConditionalEffect(
                Conditional.LessThanOrEqual(
                    IntCalculation.OpponentVariable(CreatureCard::baseStrength),
                    30.toConstantCalculation(),
                ),
                onTrue = CardEffect.NeutralizeOpponent
            )
        )
    )
}

val fireShield = ItemCard(
    "Fire Shield",
    40,
    1,
    type = ItemType.Armor
).apply {
    addEffect(
        CardEffect.BeforeBattle(
            CardEffect.ConditionalEffect(
                Conditional.Or(
                    Conditional.OpponentHasAnyElement(listOf(CreatureElement.FIRE)),
                    Conditional.OwnerHasAnyElement(listOf(CreatureElement.FIRE)),
                ),
                onTrue = CardEffect.NeutralizeOpponent
            )
        )
    )
}

val angryMask = ItemCard(
    "Angry Mask",
    10,
    1,
    type = ItemType.Tool
).apply {
    addEffect(CardEffect.BeforeBattle(CardEffect.ModifyOwnerHealth(30.toConstantCalculation())))
    addEffect(
        CardEffect.BattleEnd(
            CardEffect.ModifyOpponentHealth(IntCalculation.Inverted(IntCalculation.OpponentDamageDealt))
        )
    )
}

val boomerang = ItemCard(
    "Boomerang",
    40,
    1,
    type = ItemType.Tool
).apply {
    addEffect(CardEffect.BeforeBattle(CardEffect.ModifyOwnerAttack(20.toConstantCalculation())))
    addEffect(CardEffect.BeforeBattle(CardEffect.ModifyOwnerHealth(10.toConstantCalculation())))
    addEffect(
        CardEffect.BattleEnd(CardEffect.RecycleToOwnersHand())
    )
}