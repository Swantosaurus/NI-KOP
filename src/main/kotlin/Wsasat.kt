import java.io.File
import kotlin.math.exp
import kotlin.math.pow

/**
 * solves weighted satisfiability problem using simulated annealing
 * @param coolingCoefficient the cooling coefficient
 * @param initialTemp the initial temperature
 * @param targetTemp the target temperature
 * @param innerCycles the number of inner cycles
 * @param windowSize the window size
 */
class Wsasat(
    val coolingCoefficient: Double,
    val initialTemp: Double?,
    val targetTemp: Double,
    val innerCycles: Int = 30,
    private val windowSize: Int = 500
) {
    init {
        check(innerCycles > 0){ "innerCycles must be greater than 0" }
        check(windowSize > 0){ "windowSize must be greater than 0" }
        if(initialTemp != null) check(initialTemp > 0){ "initialTemp must be greater than 0" }
    }

    val outF = File("./out/heuristicValue.txt").printWriter()
    val outP = File("./out/p.txt").printWriter()


    var bestIteration = 0L
    var iteration = 0L
    var resultExponent = 1

    fun solve(formula: WeightedFormula, hExp : Double = 2.9, initState: State? = null): Solution {
        //variance(formula)
        val initialTemp = initialTemp ?: estimateInitialTemp(formula)
        var temp = initialTemp

        var currentState = initState ?: State.randomInit(
            formula.weights.size,
            formula,
            hExp
        )

        var bestState = currentState


        while(temp > targetTemp /*|| iteration - bestIteration < windowSize * innerCycles*/) {
            for(i in 0..< innerCycles) {
                iteration++
                val newState = currentState.randomNeighbor()


                val delta = (newState.heuristicValue - currentState.heuristicValue)

                outF.println("${currentState.heuristicValue}")

                if (delta > 0) {
                    if(currentState.isSatisfied() && !newState.isSatisfied()) {
                        val newHeuristicCoefficient = fixRatio(currentState, newState)
                        currentState = currentState.setHeuristicCoefficient(newHeuristicCoefficient)
                        bestState = bestState.setHeuristicCoefficient(newHeuristicCoefficient)
                        temp = initialTemp
                        continue
                    }

                    currentState = newState

                    if(currentState.heuristicValue - bestState.heuristicValue > 0) {
                        if(!currentState.isSatisfied() && bestState.isSatisfied()) {
                            val newHeuristicCoefficient = fixRatio(bestState, currentState)
                            currentState = currentState.setHeuristicCoefficient(newHeuristicCoefficient)
                            bestState = bestState.setHeuristicCoefficient(newHeuristicCoefficient)
                            temp = initialTemp
                            continue
                        }
                        bestState = currentState
                        bestIteration = iteration
                    }
                } else {
                    if(!currentState.isSatisfied() && newState.isSatisfied()) {
                        val newHeuristicCoefficient = fixRatio(newState, currentState)
                        currentState = currentState.setHeuristicCoefficient(newHeuristicCoefficient)
                        bestState = bestState.setHeuristicCoefficient(newHeuristicCoefficient)
                        val tmpNew = newState.setHeuristicCoefficient(newHeuristicCoefficient)
                        if(!bestState.isSatisfied() || tmpNew.heuristicValue > bestState.heuristicValue) {
                            bestState = tmpNew
                        }
                        temp = initialTemp
                        continue
                    }

                    var p = exp(delta / temp)
                    if (p > 1.1) p = 0.0
                    p = p.coerceIn(0.0..1.0)
                    outP.println(p)
                    if (p > Math.random()) {
                        currentState = newState
                    }
                }

            }
            // cool down the temperature
            temp *= coolingCoefficient
        }
        val satisfied = formula.eval(bestState.getVariables())

        if(bestState.isSatisfied()) {
            outP.flush()
            outP.close()
            outF.flush()
            outF.close()
            return Solution(
                bestState,
                iterations = iteration,
                bestIteration,
                satisfied = satisfied == formula.numberOfClauses,
                satisfied
            )
        }
        else {
            resultExponent += 2
            return solve(formula, bestState.heuristicValue * resultExponent, bestState)
        }
    }

    fun fixRatio(satisfied: State, unsatisfied: State) : Double {
        var newHeuristicCoefficient = satisfied.heuristicCoefficient * 1.1
        var tmpSatisfied = satisfied.setHeuristicCoefficient(newHeuristicCoefficient)
        var tmpUnsatisfied = unsatisfied.setHeuristicCoefficient(newHeuristicCoefficient)
        while (tmpSatisfied.heuristicValue < tmpUnsatisfied.heuristicValue) {
            newHeuristicCoefficient *= 1.1
            //println(newHeuristicCoefficient)
            tmpSatisfied = tmpSatisfied.setHeuristicCoefficient(newHeuristicCoefficient)
            tmpUnsatisfied = tmpUnsatisfied.setHeuristicCoefficient(newHeuristicCoefficient)
            //satisfied.setHeuristicCoefficient(newHeuristicCoefficient)
        }
        return newHeuristicCoefficient
    }


    fun variance(formula: WeightedFormula) {
        val avrg = formula.normalizedWeights.average()

        var variance = 0.0
        for(i in formula.normalizedWeights.indices) {
             variance += (formula.normalizedWeights[i] - avrg).pow(2)
        }
        variance /= formula.normalizedWeights.size
        println("min: ${formula.normalizedWeights.min()}")
        println("max: ${formula.normalizedWeights.max()}")
        println("avrg: $avrg")
        println("variance: $variance")
    }


    private fun estimateInitialTemp(wFormula: WeightedFormula): Double {
        return wFormula.normalizedWeights.sum() / 4
    }


    data class Solution(val state: State, val iterations: Long, val bestIteration: Long, val satisfied: Boolean, val numberOfSatisfiedClauses: Int)
}