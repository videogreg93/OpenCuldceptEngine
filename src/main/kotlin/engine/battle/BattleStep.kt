package engine.battle

import models.cards.creature.CreatureCard

sealed class BattleStep(val description: String) {
    class AttackerAttacks(
        val card: CreatureCard,
        val attackValue: Int
    ): BattleStep("${card.name} attacks for $attackValue.")

    class CardSurvives(
        val card: CreatureCard
    ): BattleStep("${card.name} survives with ${card.currentHP} HP remaining.")

    class DefenderAttacks(
        val card: CreatureCard,
        val attackValue: Int
    ): BattleStep("Defending ${card.name} attacks for $attackValue.")

    class CardDefeated(
        val card: CreatureCard
    ) : BattleStep("${card.name} is defeated.")
}