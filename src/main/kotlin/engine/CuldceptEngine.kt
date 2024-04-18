package engine

import engine.battle.Battle
import models.cards.creature.CreatureCard
import models.player.Player
import utils.wrappingCursor

class CuldceptEngine {
    private val players: ArrayList<Player> = ArrayList()
    private var currentPlayerIndex: Int = 0
        set(value) {
            field = players.wrappingCursor(value)
        }
    val currentPlayer: Player
        get() = players.getOrNull(currentPlayerIndex) ?: error("No players added to engine")

    fun startBattle(attacker: CreatureCard, defender: CreatureCard, attackingPlayer: Player, defendingPlayer: Player): Battle {
        return Battle(attacker, defender, attackingPlayer, defendingPlayer)
    }

    fun addPlayer(player: Player) = players.add(player)
    fun goToNextTurn() {
        currentPlayerIndex++
    }
}