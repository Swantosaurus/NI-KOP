import kotlin.math.pow

class State(private val variables: List<Boolean>, private val normalizedWeights: List<Double>, private val formula: WeightedFormula, val heuristicCoefficient: Double) {

    val eval = formula.eval(this)

    val heuristicValue : Double = run {
        val numOfSatisfied = eval.numOfSatisfiedClauses.toDouble()
        var sumOfWeights = 1.0
        for (i in variables.indices) {
            if (variables[i]) {
                sumOfWeights += normalizedWeights[i]
            }
        }
        //println("heuristicCoefficient: $heuristicCoefficient")
        val heuristicVal = sumOfWeights * (numOfSatisfied / formula.numberOfClauses).pow(heuristicCoefficient) //* hExponent
        heuristicVal
        //if(eval.satisfied) heuristicVal else //heuristicVal * 0.7
    }

    val weight by lazy {
        if(!eval.satisfied) return@lazy 0
        var sumOfWeights = 0
        for (i in variables.indices) {
            if (variables[i]) {
                sumOfWeights += formula.weights[i]
            }
        }
        sumOfWeights
    }

    fun isSatisfied(): Boolean {
        return eval.satisfied
    }

    fun getVariables(): List<Boolean> {
        return variables
    }

    /**
     * Returns a random neighbor of this state by flipping a random variable
     */
    fun randomNeighbor(): State {
        val newVariables = variables.toMutableList()
        val index = variables.indices.random()
        newVariables[index] = !newVariables[index]
        return State(newVariables, normalizedWeights, formula = formula, heuristicCoefficient)
    }


    override fun toString(): String {
        return "State(variables=$variables, weight=$weight, heuristicValue=$heuristicValue, heuristicCoefficient=$heuristicCoefficient)"
    }

//    fun getHeuristicCoefficient(): Double {
//        return heuristicCoefficient
//    }

    fun setHeuristicCoefficient(to: Double): State {
        return State(variables, normalizedWeights, formula, to)
    }

    companion object {
        fun randomInit(numberOfVariables: Int, formula: WeightedFormula, hExponent: Double): State {
            val variables = mutableListOf<Boolean>()
            for (i in 0..<numberOfVariables) {
                variables.add(Math.random() < 0.5)
            }
            return State(variables, formula.normalizedWeights, formula = formula, heuristicCoefficient = hExponent)
        }
    }
}