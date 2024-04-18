import arrow.core.Either
import engine.CuldceptEngine
import models.cards.buckler
import models.cards.creature.CreatureCard
import models.cards.creature.CreatureElement
import models.cards.fireShield
import models.cards.item.ItemCard
import models.cards.item.ItemType
import models.player.Player

fun main(args: Array<String>) {
    val engine = CuldceptEngine()
    val player1 = Player("Player One", 100)
    val player2 = Player("Player Two", 100)
    engine.addPlayer(player1)
    engine.addPlayer(player2)
    val battle = engine.startBattle(
        CreatureCard(
            "Test Attacker",
            30,
            40,
            "",
            listOf(),
            emptyList(),
            10,
            1
        ),
        CreatureCard(
            "test defender",
            50,
            10,
            "",
            listOf(),
            emptyList(),
            10,
            1
        ),
        player1,
        player2
    )
    player2.addCardToHand(fireShield)
    battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
    battle.setDefenderItemCard(Either.Left(fireShield))
    val result = battle.fight()
    result.getOrNull()?.let {
        it.steps.forEach {
            println(it.description)
        }
        println(it.endResult.description)
    }
}