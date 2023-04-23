package engine.battle.state

import arrow.core.Either
import models.cards.creature.CreatureCard
import models.cards.item.ItemCard

object BattleStateMachine {
    sealed class State {
        object AttackerChooseItem: State()
        object DefenderChooseItem: State()
        object CalculateAttackOrder: State()
        class BattleStartEffects(val first: CreatureCard, val second: CreatureCard): State()
        class FirstAttack(val first: CreatureCard, val second: CreatureCard): State()
        object Cleanup: State()
    }

    sealed class Action {
        class AttackerChooseItem(val itemCard: Either<ItemCard, ItemCard.EmptyItemCard>): Action()
        class DefenderChooseItem(val itemCard: Either<ItemCard, ItemCard.EmptyItemCard>): Action()
    }
}