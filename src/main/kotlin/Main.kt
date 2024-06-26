import arrow.core.Either
import engine.CuldceptEngine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import models.cards.Card
import models.cards.angryMask
import models.cards.creature.CreatureCard
import models.cards.effects.CardEffect
import models.cards.item.ItemCard
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
            10,
            40,
            "",
            listOf(),
            emptyList(),
            10,
            1
        ).apply {

        },
        CreatureCard(
            "test defender",
            30,
            100,
            "",
            listOf(),
            emptyList(),
            10,
            1
        ).apply {

        },
        player1,
        player2
    )

    val s = Json {
        prettyPrint = true
    }.encodeToString(angryMask)
    println(s)
    player2.addCardToHand(angryMask)
    battle.goToItemSelection()
    battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
    battle.setDefenderItemCard(Either.Left(angryMask))
    val result = battle.fight()
    result.getOrNull()?.let {
        it.steps.forEach {
            println(it.description)
        }
        println(it.endResult.description)
    }
    result.leftOrNull()?.let {
        println(it)
    }
}