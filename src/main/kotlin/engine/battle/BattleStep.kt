package engine.battle

import models.cards.Card
import models.cards.creature.CreatureCard

sealed class BattleStep(open val description: String = "") {
    class CardAttacks(
        val card: CreatureCard,
        val attackValue: Int,
        val isAttacker: Boolean,
    ): BattleStep("${card.name} attacks for $attackValue.") {
        override val description: String
            get() = if (isAttacker) {
                "${card.name} attacks for $attackValue."
            } else {
                "Defender ${card.name} attacks for $attackValue."
            }
    }

    class CardSurvives(
        val card: CreatureCard
    ): BattleStep("${card.name} survives with ${card.currentHP} HP remaining.")

    class CardDefeated(
        val card: CreatureCard
    ) : BattleStep("${card.name} is defeated.")

    class AttacksFirst(val card: CreatureCard): BattleStep("${card.name} Attacks First Effect")

    class AttacksLast(val card: CreatureCard): BattleStep("${card.name} Attacks Last Effect")

    class ModifyAttack(receiver: CreatureCard, amount: Int, source: Card?): BattleStep("${receiver.name} changes attack by $amount via ${source?.name} effect.")
}