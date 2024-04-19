import arrow.core.Either
import engine.battle.BattleResult
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import models.cards.boomerang
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
})