package engine.battle

sealed class BattleError {
    object NoAttackerItemDefined: BattleError()
    object NoDefenderItemDefined: BattleError()
}