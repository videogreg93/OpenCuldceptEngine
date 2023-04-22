package models.cards.effects

import engine.battle.BattleStep
import models.cards.Card
import models.cards.creature.CreatureCard

sealed class CardEffect(open val description: String) {
    var source: Card? = null
    var owner: CreatureCard? = null

    open fun trigger(opponent: CreatureCard): List<BattleStep> = emptyList()

    object AttackFirst: CardEffect("Attack First")
    object AttackLast: CardEffect("Attack Last")
    class BattleStart(val effect: CardEffect): CardEffect("On Battle Start") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            effect.owner = owner
            effect.source = source
            return effect.trigger(opponent)
        }
    }
    class BattleEnd(val effect: CardEffect): CardEffect("On Battle End") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            effect.owner = owner
            effect.source = source
            return effect.trigger(opponent)
        }
    }
    class ModifyOwnerAttack(val amount: Int): CardEffect("") {
        override val description: String
            get() = "Modify ${owner?.name} attack by $amount"

        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            return owner?.let {
                it.currentStrength += amount
                listOf(BattleStep.ModifyAttack(it, amount, source))
            } ?: error("No owner found for $this")
        }
    }

    class ModifyOpponentAttack(val amount: Int): CardEffect("") {
        override val description: String
            get() = "Modify opponents attack by $amount"

        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            opponent.currentStrength += amount
            return listOf(BattleStep.ModifyAttack(opponent, amount, source))
        }
    }
}