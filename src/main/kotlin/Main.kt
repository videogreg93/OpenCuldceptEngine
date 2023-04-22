import arrow.core.Either
import engine.CuldceptEngine
import models.cards.creature.CreatureCard
import models.cards.effects.CardEffect
import models.cards.item.ItemCard

fun main(args: Array<String>) {
    val engine = CuldceptEngine()
    val battle = engine.startBattle(
        CreatureCard(
            "Test Attacker",
            50,
            100,
            "",
            emptyList(),
            10,
            1
        ).apply {
          //  addEffect(CardEffect.AttackLast)
        },
        CreatureCard(
            "test defender",
            50,
            50,
            "",
            emptyList(),
            10,
            1
        ).apply {
          //  addEffect(CardEffect.AttackFirst)
        }
    )
    val defenderItem = ItemCard(
        "Sword",
        100,
        1
    ).apply {
        addEffect(CardEffect.BattleStart(CardEffect.ModifyOwnerAttack(100)))
    }
    battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
    battle.setDefenderItemCard(Either.Left(defenderItem))
    val result = battle.fight()
    result.getOrNull()?.let {
        it.steps.forEach {
            println(it.description)
        }
        println(it.endResult.description)
    }
}