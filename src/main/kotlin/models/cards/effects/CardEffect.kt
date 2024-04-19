package models.cards.effects

import engine.battle.BattleStep
import models.cards.Card
import models.cards.creature.CreatureCard

// TODO change the trigger callback to take a dataset, which can more easily created it
sealed class CardEffect(open val description: String) {
    var source: Card? = null
    var owner: CreatureCard? = null

    // TODO this isnt used now, need to find a sure way of setting this correctly during fight
    lateinit var dataSet: DataSet
    open val priority: Int = 0 // lower priority = triggered first

    open fun trigger(opponent: CreatureCard): List<BattleStep> = emptyList()

    class AttackFirst : CardEffect("Attack First") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            owner?.attackFirst = true
            return super.trigger(opponent)
        }
    }

    class AttackLast : CardEffect("Attack Last") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            owner?.attackLast = true
            return super.trigger(opponent)
        }
    }

    // Timing Effects
    abstract class TimingEffect(val effect: CardEffect, description: String) : CardEffect(description) {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            effect.owner = owner
            effect.source = source
            return effect.trigger(opponent)
        }
    }

    // Before Combat
    // See here for timing information https://www.culdceptcentral.com/culdcept-revolt/culdcept-revolt-guides/culdcept-revolt-battle-timetable

    class BeforeItemSelection(effect: CardEffect) : TimingEffect(effect, "Before Item Selection")
    class DuringItemSelection(effect: CardEffect) : TimingEffect(effect, "During Item Selection")
    class BeforeBattle(effect: CardEffect) : TimingEffect(effect, "Before Battle")

    // Combat Timing Effects

    class BeforeAttackPhase(effect: CardEffect) : TimingEffect(effect, "Before Attack Phase")
    class BeforeDamageCalculation(effect: CardEffect) : TimingEffect(effect, "Before Damage Calculation")
    class OnSuccessfulAttack(effect: CardEffect) : TimingEffect(effect, "On Successful Attack")
    class OnFailedAttack(effect: CardEffect) : TimingEffect(effect, "On Failed Attack")

    // End of Battle

    class EndOfBattle1(effect: CardEffect) : TimingEffect(effect, "End of Battle, first phase")
    class UponDefeat1(effect: CardEffect) : TimingEffect(effect, "Upon Defeat, first phase")
    class EndOfBattle2(effect: CardEffect) : TimingEffect(effect, "End of Battle, second phase")
    class UponDefeat2(effect: CardEffect) : TimingEffect(effect, "Upon Defeat, second phase")
    class UponVictory(effect: CardEffect) : TimingEffect(effect, "Upon Victory")

    class BattleEnd(val effect: CardEffect) : CardEffect("On Battle End") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            effect.owner = owner
            effect.source = source
            return effect.trigger(opponent)
        }
    }

    // todo Replace with on sucessful attack, see battering ram
    class AttackBonus(val effect: CardEffect) : CardEffect("Attack Bonus") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            effect.owner = owner
            effect.source = source
            return effect.trigger(opponent)
        }
    }
    // End Timing Effects

    // Wrapper Effects

    class ConditionalEffect(
        private val conditional: Conditional,
        val onTrue: CardEffect? = null,
        val onFalse: CardEffect? = null
    ) :
        CardEffect("Condition") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            val effect = if (conditional.checkCondition(DataSet(owner, opponent))) onTrue else onFalse
            val steps = listOf(BattleStep.ConditionCheck(conditional.checkCondition(DataSet(owner, opponent))))
            return effect?.let {
                it.owner = owner
                it.source = source
                steps + effect.trigger(opponent)
            } ?: steps
        }
    }

    // End Wrapper Effects
    class ModifyOwnerAttack(val amount: IntCalculation) : CardEffect("") {
        override val description: String
            get() = "Modify ${owner?.name} attack by ${amount.simpleAmount}"

        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            return owner?.let {
                val value = amount.calculate(DataSet(owner, opponent))
                it.currentStrength += value
                listOf(BattleStep.ModifyAttack(it, value, source))
            } ?: error("No owner found for $this")
        }
    }

    class ModifyOwnerHealth(val amount: IntCalculation) : CardEffect("") {
        override val description: String
            get() = "Modify ${owner?.name} health by ${amount.simpleAmount}"

        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            return owner?.let {
                val value = amount.calculate(DataSet(owner, opponent))
                it.currentHP += value
                listOf(BattleStep.ModifyHealth(it, it.currentHP, value, source))
            } ?: error("No owner found for $this")
        }
    }

    class ModifyOpponentHealth(val amount: IntCalculation) : CardEffect("") {
        override val description: String
            get() = "Modify opponent's health by ${amount.simpleAmount}"

        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            return opponent.let {
                val value = amount.calculate(DataSet(owner, opponent))
                it.currentHP += value
                listOf(BattleStep.ModifyHealth(it, it.currentHP, value, source))
            }
        }
    }

    class ModifyOpponentAttack(val amount: IntCalculation) : CardEffect("") {
        override val description: String
            get() = "Modify opponents attack by $amount"

        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            val value = amount.calculate(DataSet(owner, opponent))
            opponent.currentStrength += value
            return listOf(BattleStep.ModifyAttack(opponent, value, source))
        }
    }

    class SetOpponentHealth(val newValue: IntCalculation) : CardEffect("") {
        override val description: String
            get() = "Set opponents health to $newValue"

        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            val value = newValue.calculate(DataSet(owner, opponent))
            opponent.currentHP = value
            return listOf(BattleStep.ModifyHealth(opponent, opponent.currentHP, value, source))
        }
    }

    class SetOpponentStrength(val newValue: IntCalculation) : CardEffect("") {
        override val description: String
            get() = "Set opponents strength to $newValue"

        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            val value = newValue.calculate(DataSet(owner, opponent))
            opponent.currentStrength = value
            return listOf(BattleStep.ModifyAttack(opponent, value, source))
        }
    }

    object NeutralizeOpponent : CardEffect("") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            opponent.addBattleEffect(BattleEffects.Neutralized)
            return listOf(BattleStep.Neutralized(opponent))
        }
    }

    object Defensive : CardEffect("")
    object InstantDeath : CardEffect("")

    class TargetPlayerDiscardsCards(val targetPlayer: TargetPlayer, val amount: IntCalculation) : CardEffect("") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            val data = DataSet(owner, opponent, owner?.owner, opponent.owner)
            val target = targetPlayer.resolve(data)
            val initialHandSize = target.cardsInHand.size
            val discardsCards = target.discardCardsFromHand(amount.calculate(data))
            return listOf(BattleStep.DiscardedCards(target, initialHandSize, discardsCards))
        }
    }

    class TargetPlayerDrawsCards(val targetPlayer: TargetPlayer, val amount: IntCalculation) : CardEffect("") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            val data = DataSet(owner, opponent, owner?.owner, opponent.owner)
            val target = targetPlayer.resolve(data)
            val calculatedAmount = amount.calculate(data)
            target.drawCardsFromDeck(calculatedAmount)
            return listOf(BattleStep.DrawCards(target, calculatedAmount))
        }
    }

    class RecycleToOwnersHand(): CardEffect("") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            source?.let {
                owner?.owner?.addCardToHand(it)
            }
            return listOf(BattleStep.Recycle(owner?.owner, source))
        }
    }

    companion object {
        fun landBonus(owner: CreatureCard) {
            owner.addEffect(
                BeforeItemSelection(
                    ModifyOwnerHealth(IntCalculation.OwnersVariable(CreatureCard::currentLandHPBonus))
                )
            )
        }

        fun List<CardEffect>.triggerAll(opponent: CreatureCard): List<BattleStep> = flatMap { it.trigger(opponent) }
    }
}