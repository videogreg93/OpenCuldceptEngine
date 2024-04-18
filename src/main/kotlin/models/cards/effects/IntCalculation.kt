package models.cards.effects

import models.cards.creature.CreatureCard
import kotlin.reflect.KProperty1

/**
 * Knows how to calculate a value for a [CardEffect]
 */
sealed class IntCalculation {
    abstract val simpleAmount: String
    abstract fun calculate(
        dataSet: DataSet
    ): Int

    override fun toString(): String {
        return simpleAmount
    }

    class Constant(val amount: Int) : IntCalculation() {
        override val simpleAmount: String
            get() = amount.toString()

        override fun calculate(dataSet: DataSet): Int {
            return amount
        }
    }

    class Inverted(val original: IntCalculation): IntCalculation() {
        override val simpleAmount: String
            get() = "inverted"

        override fun calculate(dataSet: DataSet): Int {
            return -1 * original.calculate(dataSet)
        }
    }

    // TODO maybe we can use reflection to state a variable of [CreatureCard] instead of having seperate classes for each variable
    object OwnerMHP : IntCalculation() {
        override val simpleAmount: String
            get() = "owner's MHP"

        override fun calculate(dataSet: DataSet): Int {
            return dataSet.ownerCreature?.baseMHP ?: error("No owner card found")
        }
    }

    object OwnerST : IntCalculation() {
        override val simpleAmount: String
            get() = "owner's MHP"

        override fun calculate(dataSet: DataSet): Int {
            return dataSet.ownerCreature?.baseStrength ?: error("No owner card found")
        }
    }

    object OpponentBaseStrength : IntCalculation() {
        override val simpleAmount: String
            get() = "opponents base strength"

        override fun calculate(dataSet: DataSet): Int {
            return dataSet.opponentCreature?.baseStrength ?: error("No opponent card found")
        }
    }

    object OpponentDamageDealt : IntCalculation() {
        override val simpleAmount: String
            get() = "Damage opponent dealt"

        override fun calculate(dataSet: DataSet): Int {
            return dataSet.damageOpponentDealt
        }
    }

    class OpponentVariable(val variable: KProperty1<CreatureCard, Int>) : IntCalculation() {
        override val simpleAmount: String
            get() = "Opponents ${variable.name}"

        override fun calculate(dataSet: DataSet): Int {
            return dataSet.opponentCreature?.let(variable) ?: error("No opponent card found")
        }
    }

    class OwnersVariable(val variable: KProperty1<CreatureCard, Int>) : IntCalculation() {
        override val simpleAmount: String
            get() = "Owners ${variable.name}"

        override fun calculate(dataSet: DataSet): Int {
            return dataSet.ownerCreature?.let(variable) ?: error("No owner card found")
        }
    }
}