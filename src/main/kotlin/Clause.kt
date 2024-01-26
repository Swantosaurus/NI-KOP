

data class Literal(val variableId: Int, val positive: Boolean){
    init {
        check(variableId >= 0) { "variableId must be greater than 0" }
    }

    override fun toString(): String {
        return "Literal(variableId=${(variableId + 1)*if(positive) 1 else -1})"
    }
}

data class Clause(val literals: List<Literal>) {
    fun isSatisfied(assignments: List<Boolean>): Boolean {
        for(literal in literals) {
            val variable = assignments[literal.variableId]
            if(literal.positive) {
                if(variable) {
                    return true
                }
            } else {
                if(!variable) {
                    return true
                }
            }
        }
        return false
    }
}