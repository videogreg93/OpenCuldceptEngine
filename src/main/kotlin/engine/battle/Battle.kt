package engine.battle

import arrow.core.Either
import models.cards.creature.CreatureCard
import models.cards.item.ItemCard

class Battle(
    val attacker: CreatureCard,
    val defender: CreatureCard,
) {

    var attackerItem: Either<ItemCard, ItemCard.EmptyItemCard>? = null
    var defenderItem: Either<ItemCard, ItemCard.EmptyItemCard>? = null

    fun setAttackerItemCard(card: Either<ItemCard, ItemCard.EmptyItemCard>) {
        attackerItem = card
    }

    fun setDefenderItemCard(card: Either<ItemCard, ItemCard.EmptyItemCard>) {
        defenderItem = card
    }

    private enum class Priority {FIRST, LAST, NONE}

    private fun getPriorityFor(creatureCard: CreatureCard): Priority {
        return when {
            creatureCard.attackFirst && creatureCard.attackLast -> Priority.NONE
            creatureCard.attackFirst -> Priority.FIRST
            creatureCard.attackLast -> Priority.LAST
            else -> Priority.NONE
        }
    }

    private fun calculateOrdering(attacker: CreatureCard, defender: CreatureCard): Pair<CreatureCard, CreatureCard> {
        val attackerPriority = getPriorityFor(attacker)
        val defenderPriority = getPriorityFor(defender)
        return when  {
            attackerPriority == defenderPriority -> attacker to defender
            attackerPriority == Priority.LAST || defenderPriority == Priority.FIRST -> defender to attacker
            else -> attacker to defender
        }
    }

    /**
     * Proceed with the fight. Must have called [setAttackerItemCard] & [setDefenderItemCard] otherwise will fail
     */
    fun fight(): Either<BattleError, BattleResult> {
        return when {
            attackerItem == null -> Either.Left(BattleError.NoAttackerItemDefined)
            defenderItem == null -> Either.Left(BattleError.NoDefenderItemDefined)
            else -> {
                // todo implement fight
                // todo use ordering logic from above
                val steps = ArrayList<BattleStep>()
                steps.add(
                    BattleStep.AttackerAttacks(
                        attacker,
                        attacker.baseStrength
                    )
                )
                defender.currentHP -= attacker.baseStrength
                if (defender.currentHP <= 0) {
                    steps.add(
                        BattleStep.CardDefeated(defender)
                    )
                    Either.Right(
                        BattleResult(
                            steps,
                            BattleResult.EndResult.ATTACKER_WINS
                        )
                    )
                } else {
                    steps.add(
                        BattleStep.DefenderAttacks(defender, defender.baseStrength)
                    )
                    attacker.currentHP -= defender.baseStrength
                    if (attacker.currentHP <= 0) {
                        steps.add(
                            BattleStep.CardDefeated(attacker)
                        )
                        Either.Right(BattleResult(steps, BattleResult.EndResult.DEFENDER_WINS))
                    } else {
                        Either.Right(BattleResult(steps, BattleResult.EndResult.STALEMATE))
                    }
                }
            }
        }
    }

}