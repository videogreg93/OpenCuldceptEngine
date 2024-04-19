import arrow.core.Either
import engine.battle.BattleResult
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import models.cards.boomerang
import models.cards.counterAmulet
import models.cards.creature.CreatureCard
import models.cards.item.ItemCard

class ItemCardTests : ShouldSpec({
    context("Boomerang") {
        should("Return to hand") {
            val data = Fixtures.getDefaultData()
            val engine = data.engine
            val battle = engine.startBattle(data.attacker, data.defender, data.player1, data.player2)
            data.player1.addCardToHand(boomerang)
            battle.goToItemSelection()
            battle.setAttackerItemCard(Either.Left(boomerang))
            battle.setDefenderItemCard(Either.Right(ItemCard.EmptyItemCard))
            data.player1.cardsInHand.size shouldBe 0
            val result = battle.fight()
            data.player1.cardsInHand.size shouldBe 1
            data.player1.cardsInHand.first() shouldBe boomerang
        }
    }

    context("Counter Amulet") {
        should("Do its thing") {
            val data = Fixtures.getLeanData()
            val engine = data.engine
            val attacker = CreatureCard("Attacker", 100, 100, "", emptyList(), emptyList(), 1, 1)
            val defender = CreatureCard("Defender", 20, 10, "", emptyList(), emptyList(), 1, 1)
            data.player2.addCardToHand(counterAmulet)
            val battle = engine.startBattle(attacker, defender, data.player1, data.player2)
            battle.goToItemSelection()
            battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
            battle.setDefenderItemCard(Either.Left(counterAmulet))
            val result = battle.fight()
            result.getOrNull()!!.endResult shouldBe BattleResult.EndResult.ATTACKER_WINS
        }
    }
})