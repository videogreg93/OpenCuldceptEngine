package models.cards.effects

import engine.battle.BattleStep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import models.cards.Card
import models.cards.creature.CreatureCard
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

// TODO change the trigger callback to take a dataset, which can more easily created it
@Serializable
sealed class CardEffect() {
    open val description: String = ""
    @Transient
    var source: Card? = null
    @Transient
    var owner: CreatureCard? = null

    // TODO this isnt used now, need to find a sure way of setting this correctly during fight
    @Transient
    lateinit var dataSet: DataSet
    open var priority: Int = 0 // lower priority = triggered first

    open fun trigger(opponent: CreatureCard): List<BattleStep> = emptyList()

    class AttackFirst : CardEffect("Attack First") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            owner?.attackFirst = true
            return super.trigger(opponent)
        }
    }

    // todo delete, this is for testing serialization
    constructor(s: String): this()

    class AttackLast : CardEffect("Attack Last") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            owner?.attackLast = true
            return super.trigger(opponent)
        }
    }

    // Timing Effects
    @Serializable
    sealed class TimingEffect() : CardEffect() {
        constructor(effect: CardEffect, s: String) : this()
        abstract val effect: CardEffect

        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            effect.owner = owner
            effect.source = source
            return effect.trigger(opponent)
        }
    }

    // Before Combat
    // See here for timing information https://www.culdceptcentral.com/culdcept-revolt/culdcept-revolt-guides/culdcept-revolt-battle-timetable

    @Serializable
    class BeforeItemSelection(override val effect: CardEffect) : TimingEffect()
    @Serializable
    class DuringItemSelection(override val effect: CardEffect) : TimingEffect()
    @Serializable
    class BeforeBattle(override val effect: CardEffect) : TimingEffect()

    // Combat Timing Effects
    @Serializable
    class BeforeAttackPhase(override val effect: CardEffect) : TimingEffect(effect, "Before Attack Phase")
    @Serializable
    class BeforeDamageCalculation(override val effect: CardEffect) : TimingEffect(effect, "Before Damage Calculation")
    @Serializable
    class OnSuccessfulAttack(override val effect: CardEffect) : TimingEffect(effect, "On Successful Attack")
    @Serializable
    class OnFailedAttack(override val effect: CardEffect) : TimingEffect(effect, "On Failed Attack")

    // End of Battle

    @Serializable
    class EndOfBattle1(override val effect: CardEffect) : TimingEffect(effect, "End of Battle, first phase")
    @Serializable
    class UponDefeat1(override val effect: CardEffect) : TimingEffect(effect, "Upon Defeat, first phase")
    @Serializable
    class EndOfBattle2(override val effect: CardEffect) : TimingEffect(effect, "End of Battle, second phase")
    @Serializable
    class UponDefeat2(override val effect: CardEffect) : TimingEffect(effect, "Upon Defeat, second phase")
    @Serializable
    class UponVictory(override val effect: CardEffect) : TimingEffect(effect, "Upon Victory")

    // TODO replace with endOfBattle1
    @Serializable
    class BattleEnd(val effect: CardEffect) : CardEffect("On Battle End") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            effect.owner = owner
            effect.source = source
            return effect.trigger(opponent)
        }
    }

    // todo Replace with on sucessful attack, see battering ram
    @Serializable
    class AttackBonus(val effect: CardEffect) : CardEffect("Attack Bonus") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            effect.owner = owner
            effect.source = source
            return effect.trigger(opponent)
        }
    }
    // End Timing Effects

    // Wrapper Effects

    @Serializable
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
    class SetOwnerIntVariable(val variable: KMutableProperty1<CreatureCard, Int>, val amount: IntCalculation): CardEffect("") {
        override var description: String = ""
            get() = "Set ${owner?.name}'s ${variable.name} to ${amount.simpleAmount}"

        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            return owner?.let {
                variable.set(it, amount.calculate(DataSet(owner, opponent, owner?.owner, opponent.owner)))
                listOf()
            } ?: error("No owner found for $this")
        }
    }

    class ModifyOwnerAttack(val amount: IntCalculation) : CardEffect("") {
        override val description: String
            get() = "Modify ${owner?.name} attack by ${amount.simpleAmount}"

        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            return owner?.let {
                val value = amount.calculate(DataSet(owner, opponent))
                it.currentStrength += value
                listOf(BattleStep.ModifyCreatureValue(description))
            } ?: error("No owner found for $this")
        }
    }

    @Serializable
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

    @Serializable
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

    object Reflect : CardEffect("") {
        override fun trigger(opponent: CreatureCard): List<BattleStep> {
            opponent.addBattleEffect(BattleEffects.Reflected())
            return listOf(BattleStep.Reflected())
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