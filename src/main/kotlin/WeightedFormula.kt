import kotlin.math.pow


data class WeightedFormula(val name: String, val clauses: List<Clause>, val weights: List<Int>){

    val normalizedWeights = getWeightNormlized()

    val numberOfClauses = clauses.size
    val numberOfVariables = weights.size

    init {
        check(clauses.isNotEmpty()) { "clauses must not be empty" }
        check(weights.all { it > 0 }) { "weights must be positive" }
    }

    fun getNormalizationCoeficient(): Double {
        return TARGET_WEIGHT_ROOF.toDouble() / weights.max()!!
    }

    fun getWeightNormlized(): List<Double> {
        val coeficient = getNormalizationCoeficient()
        return weights.map { it * coeficient }
    }

    fun eval(state: State): EvalResult {
        val satisfied = eval(state.getVariables())
        return EvalResult(satisfied == numberOfClauses, satisfied)
    }

    /**
     * Evaluates the formula
     * @return the number of satisfied clauses
     */
    fun eval(variables: List<Boolean>): Int {
        var numOfSatisfiedClauses = 0
        for(clausule in clauses) {
            //println(clausule)
            if(clausule.isSatisfied(variables)) {
                numOfSatisfiedClauses++
            } //else {
              //  println("not satisfied: $clausule")
            //}
        }
        return numOfSatisfiedClauses
    }

    fun printStats(){
        println("min: ${normalizedWeights.min()}")
        println("max: ${normalizedWeights.max()}")
        println("avrg: ${normalizedWeights.average()}")
        println("variance: ${variance()}")
    }

    fun variance(): Double {
        val avrg = normalizedWeights.average()

        var variance = 0.0
        for(i in normalizedWeights.indices) {
            variance += (normalizedWeights[i] - avrg).pow(2)
        }
        variance /= normalizedWeights.size
        return variance
    }

    data class EvalResult(val satisfied: Boolean, val numOfSatisfiedClauses: Int)

    companion object {
        val TARGET_WEIGHT_ROOF = 100
    }
}