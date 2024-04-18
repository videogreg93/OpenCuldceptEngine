package engine.battle

import arrow.core.Either
import arrow.core.None
import com.tinder.StateMachine
import engine.battle.state.BattleStateMachine.Action
import engine.battle.state.BattleStateMachine.State
import models.cards.creature.CreatureCard
import models.cards.effects.BattleEffects
import models.cards.effects.CardEffect
import models.cards.effects.CardEffect.Companion.triggerAll
import models.cards.item.ItemCard
import models.player.Player

class Battle(
    val attacker: CreatureCard,
    val defender: CreatureCard,
    val attackingPlayer: Player,
    val defendingPlayer: Player,
) {

    var attackerItem: Either<ItemCard, ItemCard.EmptyItemCard>? = null
    var defenderItem: Either<ItemCard, ItemCard.EmptyItemCard>? = null

    // Add snapshot to each step (creature state, items, etc)
    val steps = ArrayList<BattleStep>()
    var result: Either<BattleError, BattleResult>? = null
    private var canSelectItems = false

    fun setAttackerItemCard(card: Either<ItemCard, ItemCard.EmptyItemCard>) {
        when (card) {
            is Either.Left -> {
                // Check that we can actually play this
                val canEquip = attacker.canEquipCard(card.value)
                // lazy makes sure we don't spend the gold if the creature cant equip the item
                val canPlay by lazy { attackingPlayer.attemptToPlayCard(card.value) }
                if (canEquip && canPlay) {
                    attackerItem = card
                    card.value.setOwnerForEffects(attacker)
                } else {
                    error("Can not play card")
                }
            }

            is Either.Right -> {
                attackerItem = card
            }
        }
    }

    fun setDefenderItemCard(card: Either<ItemCard, ItemCard.EmptyItemCard>) {
        when (card) {
            is Either.Left -> {
                // Check that we can actually play this
                val canEquip = defender.canEquipCard(card.value)
                // lazy makes sure we don't spend the gold if the creature cant equip the item
                val canPlay by lazy { defendingPlayer.attemptToPlayCard(card.value) }
                if (canEquip && canPlay) {
                    defenderItem = card
                    card.value.setOwnerForEffects(defender)
                } else {
                    error("Can not play card")
                }
            }

            is Either.Right -> {
                defenderItem = card
            }
        }
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

    fun goToItemSelection() {
        val attackerSteps = attacker.effectsOfType<CardEffect.BeforeItemSelection>().triggerAll(defender)
        val defenderSteps = defender.effectsOfType<CardEffect.BeforeItemSelection>().triggerAll(attacker)
        steps.addAll(attackerSteps)
        steps.addAll(defenderSteps)
        canSelectItems = true
    }

    /**
     * Proceed with the fight. Must have called [setAttackerItemCard] & [setDefenderItemCard] otherwise will fail
     * TODO we need lamba calculations for strength so we can multiply by 0 correctly
     * TODO this should be replaced with a state machine
     */
    fun fight(): Either<BattleError, BattleResult> {
        if (!canSelectItems) return Either.Left(BattleError.ItemSelectionNotTriggered)
        attacker.addedEffects.addAll(attackerItem?.leftOrNull()?.effects.orEmpty())
        defender.addedEffects.addAll(defenderItem?.leftOrNull()?.effects.orEmpty())
        val result = when {
            attackerItem == null -> Either.Left(BattleError.NoAttackerItemDefined)
            defenderItem == null -> Either.Left(BattleError.NoDefenderItemDefined)
            else -> {
                val orderedCreatures = calculateOrdering(attacker, defender)

                // Modify values before battle
                orderedCreatures.first.allEffects.filterIsInstance<CardEffect.BeforeBattle>().forEach {
                    steps.addAll(it.trigger(orderedCreatures.second))
                }
                orderedCreatures.second.allEffects.filterIsInstance<CardEffect.BeforeBattle>().forEach {
                    steps.addAll(it.trigger(orderedCreatures.first))
                }

                // Modify Values (creature innate values/battle start)
                orderedCreatures.first.allEffects.filterIsInstance<CardEffect.BattleStart>().forEach {
                    steps.addAll(it.trigger(orderedCreatures.second))
                }
                orderedCreatures.second.allEffects.filterIsInstance<CardEffect.BattleStart>().forEach {
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
                val attackerDamageSteps = attemptToDamage(orderedCreatures.first, orderedCreatures.second)
                steps.addAll(attackerDamageSteps)
                // Trigger attacker bonus effects
                orderedCreatures.first.allEffects.filterIsInstance<CardEffect.AttackBonus>().forEach {
                    steps.addAll(it.trigger(orderedCreatures.second))
                }
                if (orderedCreatures.second.currentHP <= 0) {
                    handleDefenderDefeated(orderedCreatures)
                } else if (orderedCreatures.first.currentHP <= 0) {
                    handleAttackerDefeated(orderedCreatures)
                } else {
                    // TODO add 'On Failed Attack' for the defender step here
                    steps.add(
                        BattleStep.CardAttacks(
                            orderedCreatures.second,
                            orderedCreatures.second.currentStrength,
                            orderedCreatures.second == attacker
                        )
                    )
                    val defenderDamageSteps = attemptToDamage(orderedCreatures.second, orderedCreatures.first)
                    steps.addAll(defenderDamageSteps)
                    // Trigger defender attack bonus
                    orderedCreatures.second.allEffects.filterIsInstance<CardEffect.AttackBonus>().forEach {
                        steps.addAll(it.trigger(orderedCreatures.first))
                    }
                    if (orderedCreatures.first.currentHP <= 0) {
                        handleAttackerDefeated(orderedCreatures)
                    } else {
                        // TODO add 'On Failed Attack' for the attack step here
                        // TODO add 'End of Battle' for both cards step here
                        Either.Right(BattleResult(steps, BattleResult.EndResult.STALEMATE))
                    }
                }
            }
        }
        // Trigger battle end effects
        attacker.effectsOfType<CardEffect.BattleEnd>().forEach {
            steps.addAll(it.trigger(defender))
        }
        defender.effectsOfType<CardEffect.BattleEnd>().forEach {
            steps.addAll(it.trigger(attacker))
        }
        cleanup()
        return result
    }

    private fun handleAttackerDefeated(orderedCreatures: Pair<CreatureCard, CreatureCard>): Either.Right<BattleResult> {
        steps.add(
            BattleStep.CardDefeated(orderedCreatures.first)
        )
        return Either.Right(
            BattleResult(
                steps, if (orderedCreatures.second == defender) {
                    BattleResult.EndResult.DEFENDER_WINS
                } else {
                    BattleResult.EndResult.ATTACKER_WINS
                }
            )
        )
    }

    private fun handleDefenderDefeated(orderedCreatures: Pair<CreatureCard, CreatureCard>): Either.Right<BattleResult> {
        steps.add(
            BattleStep.CardDefeated(orderedCreatures.second)
        )
        return Either.Right(
            BattleResult(
                steps,
                if (orderedCreatures.first == attacker) {
                    BattleResult.EndResult.ATTACKER_WINS
                } else {
                    BattleResult.EndResult.DEFENDER_WINS
                }
            )
        )
    }

    // TODO handle scroll
    private fun attemptToDamage(attacker: CreatureCard, defender: CreatureCard): List<BattleStep> {
        val steps = ArrayList<BattleStep>(emptyList())
        val finalStrength = when {
            attacker.hasBattleEffect<BattleEffects.CriticalHit>() -> {
                steps.add(BattleStep.CriticalHitBonus(attacker))
                attacker.currentStrength * BattleEffects.CriticalHit.multiplier
            }
            else -> attacker.currentStrength.toFloat()
        }
        if (attacker.hasBattleEffect<BattleEffects.Reflected>()) {
            val reflectionMultiplier = attacker.getBattleEffect<BattleEffects.Reflected>().multiplier
            attacker.currentHP -= (finalStrength * reflectionMultiplier).toInt()
        } else if (!attacker.hasBattleEffect<BattleEffects.Neutralized>()) {
            // remove land bonus if attacker has penetrate
            if (attacker.hasBattleEffect<BattleEffects.Penetrate>()) {
                steps.add(BattleStep.Penetrates(attacker))
                defender.currentHP -= defender.currentLandHPBonus
            }
            defender.currentHP -= finalStrength.toInt()
        }
        if (attacker.hasBattleEffect<BattleEffects.Neutralized>()) {
            steps.add(BattleStep.NeutralizedAttackFailed(attacker))
        }

        return steps
    }

    /**
     * Needs to be called after fight
     */
    private fun cleanup() {
        // Reset item effects
        attackerItem?.leftOrNull()?.resetOwner()
        defenderItem?.leftOrNull()?.resetOwner()
        attacker.resetValues()
        defender.resetValues()
    }

}