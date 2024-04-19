package models.cards.effects

/**
 * Things that affect the damage output, such as critical hit, neutralize and penetrate
 */
sealed class BattleEffects {
    object Neutralized: BattleEffects() // total damage = 0
    // Place on enemy, he will hit himself
    data class Reflected(val multiplier: Float = 1f): BattleEffects() // total damage = 0 & receives damage itself
    object CriticalHit: BattleEffects() {
        const val multiplier = 1.5f
    }          // strength * 1.5
    object Penetrate: BattleEffects() // Ignore land(?) bonus
}