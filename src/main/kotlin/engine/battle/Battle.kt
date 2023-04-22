package engine.battle

import arrow.core.Either
import models.cards.creature.CreatureCard
import models.cards.effects.CardEffect
import models.cards.item.ItemCard

class Battle(
    val attacker: CreatureCard,
    val defender: CreatureCard,
) {

    var attackerItem: Either<ItemCard, ItemCard.EmptyItemCard>? = null
    var defenderItem: Either<ItemCard, ItemCard.EmptyItemCard>? = null

    // Add snapshot to each step (creature state, items, etc)
    val steps = ArrayList<BattleStep>()

    fun setAttackerItemCard(card: Either<ItemCard, ItemCard.EmptyItemCard>) {
        attackerItem = card
        card.leftOrNull()?.setOwnerForEffects(attacker)
    }

    fun setDefenderItemCard(card: Either<ItemCard, ItemCard.EmptyItemCard>) {
        defenderItem = card
        card.leftOrNull()?.setOwnerForEffects(defender)
    }

    private enum class Priority { FIRST, LAST, NONE }

    private fun getPriorityFor(creatureCard: CreatureCard): Priority {
        return when {
            creatureCard.attackFirst && creatureCard.attackLast -> Priority.NONE
            creatureCard.attackFirst -> {
                steps.add(BattleStep.AttacksFirst(creatureCard))
                Priority.FIRST
            }

            creatureCard.attackLast -> {
                steps.add(BattleStep.AttacksLast(creatureCard))
                Priority.LAST
            }

            else -> Priority.NONE
        }
    }

    private fun calculateOrdering(attacker: CreatureCard, defender: CreatureCard): Pair<CreatureCard, CreatureCard> {
        val attackerPriority = getPriorityFor(attacker)
        val defenderPriority = getPriorityFor(defender)
        return when {
            attackerPriority == defenderPriority -> attacker to defender
            attackerPriority == Priority.LAST || defenderPriority == Priority.FIRST -> defender to attacker
            else -> attacker to defender
        }
    }

    /**
     * Proceed with the fight. Must have called [setAttackerItemCard] & [setDefenderItemCard] otherwise will fail
     */
    fun fight(): Either<BattleError, BattleResult> {
        val result = when {
            attackerItem == null -> Either.Left(BattleError.NoAttackerItemDefined)
            defenderItem == null -> Either.Left(BattleError.NoDefenderItemDefined)
            else -> {
                val orderedCreatures = calculateOrdering(attacker, defender)

                // Modify Values (creature innate values)
                orderedCreatures.first.effects.filterIsInstance<CardEffect.BattleStart>().forEach {
                    steps.addAll(it.trigger(orderedCreatures.second))
                }
                orderedCreatures.second.effects.filterIsInstance<CardEffect.BattleStart>().forEach {
                    steps.addAll(it.trigger(orderedCreatures.first))
                }
                // Modify values (equipped item effects)
                attackerItem?.leftOrNull()?.effects.orEmpty().filterIsInstance<CardEffect.BattleStart>().forEach {
                    steps.addAll(it.trigger(orderedCreatures.second))
                }
                defenderItem?.leftOrNull()?.effects.orEmpty().filterIsInstance<CardEffect.BattleStart>().forEach {
                    steps.addAll(it.trigger(orderedCreatures.first))
                }
                // Proceed with battle
                steps.add(
                    BattleStep.CardAttacks(
                        orderedCreatures.first,
                        orderedCreatures.first.currentStrength,
                        orderedCreatures.first == attacker
                    )
                )
                orderedCreatures.second.currentHP -= orderedCreatures.first.currentStrength
                if (orderedCreatures.second.currentHP <= 0) {
                    steps.add(
                        BattleStep.CardDefeated(orderedCreatures.second)
                    )
                    Either.Right(
                        BattleResult(
                            steps,
                            if (orderedCreatures.first == attacker) {
                                BattleResult.EndResult.ATTACKER_WINS
                            } else {
                                BattleResult.EndResult.DEFENDER_WINS
                            }
                        )
                    )
                } else {
                    steps.add(
                        BattleStep.CardAttacks(
                            orderedCreatures.second,
                            orderedCreatures.second.currentStrength,
                            orderedCreatures.second == attacker
                        )
                    )
                    orderedCreatures.first.currentHP -= orderedCreatures.second.currentStrength
                    if (orderedCreatures.first.currentHP <= 0) {
                        steps.add(
                            BattleStep.CardDefeated(orderedCreatures.first)
                        )
                        Either.Right(
                            BattleResult(
                                steps, if (orderedCreatures.second == defender) {
                                    BattleResult.EndResult.DEFENDER_WINS
                                } else {
                                    BattleResult.EndResult.ATTACKER_WINS
                                }
                            )
                        )
                    } else {
                        Either.Right(BattleResult(steps, BattleResult.EndResult.STALEMATE))
                    }
                }
            }
        }
        cleanup()
        return result
    }

    /**
     * Needs to be called after fight
     */
    private fun cleanup() {
        // Reset item effects
        attackerItem?.leftOrNull()?.resetOwner()
        defenderItem?.leftOrNull()?.resetOwner()
    }

}