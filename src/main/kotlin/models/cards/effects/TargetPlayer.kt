package models.cards.effects

import models.player.Player

sealed class TargetPlayer {
    abstract fun resolve(dataSet: DataSet): Player

    data object Opponent: TargetPlayer() {
        override fun resolve(dataSet: DataSet): Player {
            return dataSet.opponentPlayer!!
        }
    }

    data object Owner: TargetPlayer() {
        override fun resolve(dataSet: DataSet): Player {
            return dataSet.owningPlayer!!
        }
    }
}