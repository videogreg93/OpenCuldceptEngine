import engine.CuldceptEngine
import models.cards.creature.CreatureCard
import models.player.Player

data class TestDataComplete(
    val attacker: CreatureCard,
    val defender: CreatureCard,
    val player1: Player,
    val player2: Player,
    val engine: CuldceptEngine
)

data class LeanTestData(
    val player1: Player, val player2: Player, val engine: CuldceptEngine
)

object Fixtures {
    fun getDefaultData(): TestDataComplete {
        val engine = CuldceptEngine()
        val player1 = Player("Player One", 100)
        val player2 = Player("Player Two", 100)
        val attacker = CreatureCard(
            "Test Attacker",
            10,
            40,
            "",
            listOf(),
            emptyList(),
            10,
            1
        )
        val defender = CreatureCard(
            "test defender",
            50,
            10,
            "",
            listOf(),
            emptyList(),
            10,
            1
        )
        engine.addPlayer(player1)
        engine.addPlayer(player2)
        return TestDataComplete(attacker, defender, player1, player2, engine)
    }

    fun getLeanData(): LeanTestData {
        val engine = CuldceptEngine()
        val player1 = Player("Player One", 100)
        val player2 = Player("Player Two", 100)
        engine.addPlayer(player1)
        engine.addPlayer(player2)
        return LeanTestData(player1, player2, engine)
    }
}