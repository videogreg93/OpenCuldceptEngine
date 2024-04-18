package models.cards.effects

import engine.battle.Battle
import models.cards.creature.CreatureCard
import models.player.Player

/**
 * Contains all the information for calculating card effects on the fly
 */
data class DataSet(
    val ownerCreature: CreatureCard? = null,
    val opponentCreature: CreatureCard? = null,
    val owningPlayer: Player? = null,
    val opponentPlayer: Player? = null,
) {
    val damageOpponentDealt: Int
        get() = Battle.currentBattle?.creatureSpecificValues?.get(opponentCreature)?.damageDealt ?: 0
}
