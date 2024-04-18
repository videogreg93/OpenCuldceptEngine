import arrow.core.Either
import engine.battle.BattleResult
import engine.battle.BattleStep
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import models.cards.creature.CreatureCard
import models.cards.effects.CardEffect
import models.cards.effects.IntCalculation
import models.cards.effects.TargetPlayer
import models.cards.effects.toConstantCalculation
import models.cards.item.ItemCard

class MainTests : ShouldSpec({
    context("Default Battle") {
        should("attacker should win") {
            val data = Fixtures.getDefaultData()
            val engine = data.engine
            val battle = engine.startBattle(data.attacker, data.defender, data.player1, data.player2)
            battle.goToItemSelection()
            battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
            battle.setDefenderItemCard(Either.Right(ItemCard.EmptyItemCard))
            val result = battle.fight()
            result.getOrNull()!!.endResult shouldBe BattleResult.EndResult.ATTACKER_WINS
        }

        should("Defender Should Win") {
            val data = Fixtures.getLeanData()
            val engine = data.engine
            val attacker = CreatureCard("Attacker", 10, 10, "", emptyList(), emptyList(), 1, 1)
            val defender = CreatureCard("Defender", 20, 100, "", emptyList(), emptyList(), 1, 1)
            val battle = engine.startBattle(attacker, defender, data.player1, data.player2)
            battle.goToItemSelection()
            battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
            battle.setDefenderItemCard(Either.Right(ItemCard.EmptyItemCard))
            val result = battle.fight()
            result.getOrNull()!!.endResult shouldBe BattleResult.EndResult.DEFENDER_WINS
        }
        should("Stalemate") {
            val data = Fixtures.getLeanData()
            val engine = data.engine
            val attacker = CreatureCard("Attacker", 10, 100, "", emptyList(), emptyList(), 1, 1)
            val defender = CreatureCard("Defender", 20, 100, "", emptyList(), emptyList(), 1, 1)
            val battle = engine.startBattle(attacker, defender, data.player1, data.player2)
            battle.goToItemSelection()
            battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
            battle.setDefenderItemCard(Either.Right(ItemCard.EmptyItemCard))
            val result = battle.fight()
            result.getOrNull()!!.endResult shouldBe BattleResult.EndResult.STALEMATE
        }
        should("Mutual Destruction") {
            val data = Fixtures.getLeanData()
            val engine = data.engine
            val attacker = CreatureCard("Attacker", 10, 10, "", emptyList(), emptyList(), 1, 1)
            val defender = CreatureCard("Defender", 20, 10, "", emptyList(), emptyList(), 1, 1).apply {
                addEffect(CardEffect.BattleEnd(CardEffect.ModifyOpponentHealth(IntCalculation.Constant(-20))))
            }
            val battle = engine.startBattle(attacker, defender, data.player1, data.player2)
            battle.goToItemSelection()
            battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
            battle.setDefenderItemCard(Either.Right(ItemCard.EmptyItemCard))
            val result = battle.fight()
            result.getOrNull()!!.endResult shouldBe BattleResult.EndResult.MUTUAL_DESTRUCTION
        }
    }
    context("Timing Effects") {
        should("Defender Attack first") {
            val data = Fixtures.getLeanData()
            val engine = data.engine
            val attacker = CreatureCard("Attacker", 10, 20, "", emptyList(), emptyList(), 1, 1)
            val defender = CreatureCard("Defender", 20, 10, "", emptyList(), emptyList(), 1, 1).apply {
                addEffect(CardEffect.BeforeBattle(CardEffect.AttackFirst()))
            }
            val battle = engine.startBattle(attacker, defender, data.player1, data.player2)
            battle.goToItemSelection()
            battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
            battle.setDefenderItemCard(Either.Right(ItemCard.EmptyItemCard))
            val result = battle.fight()
            result.getOrNull()?.let {
                it.steps shouldContain BattleStep.AttacksFirst(defender)
                it.endResult shouldBe BattleResult.EndResult.DEFENDER_WINS
            }
        }
        should("Attacker attacks last") {
            val data = Fixtures.getLeanData()
            val engine = data.engine
            val attacker = CreatureCard("Attacker", 10, 20, "", emptyList(), emptyList(), 1, 1).apply {
                addEffect(CardEffect.BeforeBattle(CardEffect.AttackLast()))
            }
            val defender = CreatureCard("Defender", 20, 10, "", emptyList(), emptyList(), 1, 1)
            val battle = engine.startBattle(attacker, defender, data.player1, data.player2)
            battle.goToItemSelection()
            battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
            battle.setDefenderItemCard(Either.Right(ItemCard.EmptyItemCard))
            val result = battle.fight()
            result.getOrNull()?.let {
                it.steps shouldContain BattleStep.AttacksLast(attacker)
                it.endResult shouldBe BattleResult.EndResult.DEFENDER_WINS
            }
        }
        should("Attack first/last cancels out") {
            val data = Fixtures.getLeanData()
            val engine = data.engine
            val attacker = CreatureCard("Attacker", 10, 20, "", emptyList(), emptyList(), 1, 1)
            val defender = CreatureCard("Defender", 20, 10, "", emptyList(), emptyList(), 1, 1).apply {
                addEffect(CardEffect.BeforeBattle(CardEffect.AttackFirst()))
                addEffect(CardEffect.BeforeBattle(CardEffect.AttackLast()))
            }
            val battle = engine.startBattle(attacker, defender, data.player1, data.player2)
            battle.goToItemSelection()
            battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
            battle.setDefenderItemCard(Either.Right(ItemCard.EmptyItemCard))
            val result = battle.fight()
            result.getOrNull()?.let {
                it.endResult shouldBe BattleResult.EndResult.ATTACKER_WINS
            }
        }
        should("Kill attacker before he kills defender") {
            val data = Fixtures.getLeanData()
            val engine = data.engine
            val attacker = CreatureCard("Attacker", 10, 10, "", emptyList(), emptyList(), 1, 1)
            val defender = CreatureCard("Defender", 20, 10, "", emptyList(), emptyList(), 1, 1).apply {
                addEffect(CardEffect.BeforeBattle(CardEffect.ModifyOpponentHealth(IntCalculation.Constant(-20))))
            }
            val battle = engine.startBattle(attacker, defender, data.player1, data.player2)
            battle.goToItemSelection()
            battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
            battle.setDefenderItemCard(Either.Right(ItemCard.EmptyItemCard))
            val result = battle.fight()
            result.getOrNull()!!.endResult shouldBe BattleResult.EndResult.DEFENDER_WINS
        }
    }
    context("Player hand effects") {
        should("Discard amount of cards") {
            val data = Fixtures.getLeanData()
            val engine = data.engine
            val attacker = CreatureCard("Attacker", 10, 100, "", emptyList(), emptyList(), 1, 1).apply {
                addEffect(
                    CardEffect.AttackBonus(
                        CardEffect.TargetPlayerDiscardsCards(TargetPlayer.Opponent, 6.toConstantCalculation())
                    )
                )
            }
            val defender = CreatureCard("Defender", 20, 100, "", emptyList(), emptyList(), 1, 1)
            data.player2.cardsInHand.addAll(listOf(ItemCard("", 1, 1), ItemCard("", 1, 1), ItemCard("", 1, 1)))
            val battle = engine.startBattle(attacker, defender, data.player1, data.player2)
            battle.goToItemSelection()
            battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
            battle.setDefenderItemCard(Either.Right(ItemCard.EmptyItemCard))
            data.player2.cardsInHand.size shouldBe 3
            val result = battle.fight()
            data.player2.cardsInHand.size shouldBe 0
        }
        should("Draw amount of cards") {
            val data = Fixtures.getLeanData()
            val engine = data.engine
            val attacker = CreatureCard("Attacker", 10, 100, "", emptyList(), emptyList(), 1, 1).apply {
                addEffect(
                    CardEffect.AttackBonus(
                        CardEffect.TargetPlayerDrawsCards(TargetPlayer.Owner, 3.toConstantCalculation())
                    )
                )
            }
            val defender = CreatureCard("Defender", 20, 100, "", emptyList(), emptyList(), 1, 1)
            data.player1.library.addAll(listOf(ItemCard("", 1, 1), ItemCard("", 1, 1), ItemCard("", 1, 1)))
            val battle = engine.startBattle(attacker, defender, data.player1, data.player2)
            battle.goToItemSelection()
            battle.setAttackerItemCard(Either.Right(ItemCard.EmptyItemCard))
            battle.setDefenderItemCard(Either.Right(ItemCard.EmptyItemCard))
            data.player1.cardsInHand.size shouldBe 0
            data.player1.library.size shouldBe 3
            val result = battle.fight()
            data.player1.cardsInHand.size shouldBe 3
            data.player1.library.size shouldBe 0
        }
    }
}) {

}