package engine

import engine.battle.Battle
import models.cards.creature.CreatureCard

class CuldceptEngine {
    fun startBattle(attacker: CreatureCard, defender: CreatureCard): Battle {
        return Battle(attacker, defender)
    }
}