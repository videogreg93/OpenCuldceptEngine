package models.cards.effects

import kotlinx.serialization.Serializable
import models.cards.creature.CreatureElement
import kotlin.reflect.KClass

@Serializable
sealed class Conditional {
    abstract fun checkCondition(data: DataSet): Boolean

    @Serializable
    class OwnerHasEffect(val effect: KClass<CardEffect>) : Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return data.ownerCreature?.hasEffect(effect) ?: false
        }
    }

    //@Serializable
    class OpponentHasEffect(val effect: KClass<*>) : Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return data.opponentCreature?.hasEffect(CardEffect.Defensive::class) ?: false
        }
    }

    @Serializable
    class OpponentHasAnyElement(val elements: List<CreatureElement>) : Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return data.opponentCreature?.let { owner ->
                elements.any { owner.hasElement(it) }
            } ?: false
        }
    }

    @Serializable
    class OwnerHasAnyElement(val elements: List<CreatureElement>) : Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return data.ownerCreature?.let { owner ->
                elements.any { owner.hasElement(it) }
            } ?: false
        }
    }


    // Math checks

    @Serializable
    class LessThanOrEqual(val x: IntCalculation, val y: IntCalculation) : Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return x.calculate(data) <= y.calculate(data)
        }
    }

    @Serializable
    class GreaterThanOrEqual(val x: IntCalculation, val y: IntCalculation) : Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return x.calculate(data) >= y.calculate(data)
        }
    }

    // Meta condition
    /**
     * Evaluates to true if [first] or [second] is true, or if both are true. Otherwise evaluates to false.
     */
    @Serializable
    class Or(val first: Conditional, val second: Conditional): Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return first.checkCondition(data) || second.checkCondition(data)
        }
    }

    @Serializable
    class And(val first: Conditional, val second: Conditional): Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return first.checkCondition(data) && second.checkCondition(data)
        }
    }
}