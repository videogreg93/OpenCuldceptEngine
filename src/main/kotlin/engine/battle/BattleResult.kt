package engine.battle

class BattleResult(val steps: List<BattleStep>, val endResult: EndResult) {

    enum class EndResult(val description: String) {
        ATTACKER_WINS("The offense wins."),
        DEFENDER_WINS("The defense wins."),
        STALEMATE("The battle was a draw.")
    }
}