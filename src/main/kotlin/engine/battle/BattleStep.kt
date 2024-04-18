package engine.battle

import models.cards.Card
import models.cards.creature.CreatureCard
import utils.withSign

sealed class BattleStep(open val description: String = "") {
    class CardAttacks(
        val card: CreatureCard,
        val attackValue: Int,
        val isAttacker: Boolean,
    ) : BattleStep("${card.name} attacks for $attackValue.") {
        override val description: String
            get() = if (isAttacker) {
                "${card.name} attacks for $attackValue."
            } else {
                "Defender ${card.name} attacks for $attackValue."
            }
    }

    class CardSurvives(
        val card: CreatureCard
    ) : BattleStep("${card.name} survives with ${card.currentHP} HP remaining.")

    class CardDefeated(
        val card: CreatureCard
    ) : BattleStep("${card.name} is defeated.")

    class AttacksFirst(val card: CreatureCard) : BattleStep("${card.name} Attacks First Effect")

    class AttacksLast(val card: CreatureCard) : BattleStep("${card.name} Attacks Last Effect")

    class ModifyAttack(receiver: CreatureCard, amount: Int, source: Card?) :
        BattleStep("${receiver.name} changes attack by $amount via ${source?.name} effect.")

    class SetAttack(receiver: CreatureCard, amount: Int, source: Card?) :
        BattleStep("${receiver.name} changes attack to $amount via ${source?.name} effect.")

    class ModifyHealth(receiver: CreatureCard, newValue: Int, delta: Int, source: Card?) :
        BattleStep("${receiver.name} health set to $newValue (${delta.withSign()}) via ${source?.name} effect.")

    // TODO change text
    class ConditionCheck(value: Boolean) : BattleStep("Condition evaluated to $value")
    class Neutralized(target: CreatureCard) : BattleStep("${target.name} has been neutralized")
    class NeutralizedAttackFailed(target: CreatureCard): BattleStep("The attack failed because ${target.name} is neutralized")
    class Penetrates(target: CreatureCard) : BattleStep("${target.name} will penetrate")
    class CriticalHitBonus(target: CreatureCard) : BattleStep("${target.name} has critical hit")
}