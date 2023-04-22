package engine.battle.state

import arrow.core.Either
import models.cards.item.ItemCard

object BattleStateMachine {
    sealed class State {
        object AttackerChooseItem: State()
        object DefenderChooseItem: State()
        object CalculateAttackOrder: State()
        object FirstAttack: State()
    }

    sealed class Action {
        class AttackerChooseItem(val itemCard: Either<ItemCard, ItemCard.EmptyItemCard>): Action()
        class DefenderChooseItem(val itemCard: Either<ItemCard, ItemCard.EmptyItemCard>): Action()
    }
}