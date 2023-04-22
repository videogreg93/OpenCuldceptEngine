import arrow.core.Either
import engine.CuldceptEngine
import models.cards.creature.CreatureCard
import models.cards.item.ItemCard

fun main(args: Array<String>) {
    val engine = CuldceptEngine()
    val battle = engine.startBattle(
        CreatureCard(
            "test",
            50,
            90,
            "",
            emptyList(),
            10,
            1
        ),
        CreatureCard(
            "test",
            50,
            60,
            "",
            emptyList(),
            10,
            1
        )
    )
    battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
    battle.setDefenderItemCard(Either.Right(ItemCard.EmptyItemCard))
    val result = battle.fight()
    result.getOrNull()?.let {
        it.steps.forEach {
            println(it.description)
        }
        println(it.endResult.description)
    }
}