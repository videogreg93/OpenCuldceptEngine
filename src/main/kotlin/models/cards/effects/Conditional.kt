package models.cards.effects

import models.cards.creature.CreatureElement
import kotlin.reflect.KClass

sealed class Conditional {
    abstract fun checkCondition(data: DataSet): Boolean

    class OwnerHasEffect(val effect: KClass<CardEffect>) : Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return data.ownerCreature?.hasEffect(effect) ?: false
        }
    }

    class OpponentHasEffect(val effect: KClass<*>) : Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return data.opponentCreature?.hasEffect(CardEffect.Defensive::class) ?: false
        }
    }

    class OpponentHasAnyElement(val elements: List<CreatureElement>) : Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return data.opponentCreature?.let { owner ->
                elements.any { owner.hasElement(it) }
            } ?: false
        }
    }

    class OwnerHasAnyElement(val elements: List<CreatureElement>) : Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return data.ownerCreature?.let { owner ->
                elements.any { owner.hasElement(it) }
            } ?: false
        }
    }


    // Math checks

    class LessThanOrEqual(val x: IntCalculation, val y: IntCalculation) : Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return x.calculate(data) <= y.calculate(data)
        }
    }

    class GreaterThanOrEqual(val x: IntCalculation, val y: IntCalculation) : Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return x.calculate(data) >= y.calculate(data)
        }
    }

    // Meta condition
    /**
     * Evaluates to true if [first] or [second] is true, or if both are true. Otherwise evaluates to false.
     */
    class Or(val first: Conditional, val second: Conditional): Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return first.checkCondition(data) || second.checkCondition(data)
        }
    }

    class And(val first: Conditional, val second: Conditional): Conditional() {
        override fun checkCondition(data: DataSet): Boolean {
            return first.checkCondition(data) && second.checkCondition(data)
        }
    }
}