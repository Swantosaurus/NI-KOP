import java.io.File
import kotlin.math.absoluteValue


val showDebugsLoader = false


fun loadWuf(path: String): WeightedFormula {
    val f = File(path)
    val testF = File(".")
    println(testF.absolutePath)
    check(f.exists()) { "File $path does not exist" }
    check(f.isFile) { "$path is not a file" }
    check(f.canRead()) { "File $path cannot be read" }
    var weights = mutableListOf<Int>()
    var numberOfVariabes: Int = -1
    var numberOfClauses: Int = -1
    val clauses = mutableListOf<Clause>()
    for(line in f.readLines()){
        when(line[0]){
            'c' -> continue // comment
            'w' -> {
                val weightsLine = line.split(" ").toMutableList()
                weightsLine.removeFirst()
                weightsLine.removeLast()
                weights = weightsLine.map { it.toInt() }.toMutableList()
            }
            'p' -> {
                val pLine = line.split(" ")
                println(pLine.joinToString ())
                numberOfVariabes = pLine[2].toInt()
                numberOfClauses = pLine[3].toInt()
            }
            else -> {
                val l = line.trim()
                val clause = l.split(" ").map { it.toInt() }
                repeat(3){
                    check(clause[it].absoluteValue <= numberOfVariabes) { "Variable id ${clause[it]} is greater than the number of variables $numberOfVariabes" }
                }
                if(showDebugsLoader) println("clause :+ ${clause.joinToString()}")
                clauses.add(
                    Clause(
                        listOf(
                            Literal(clause[0].absoluteValue - 1, clause[0] > 0),
                            Literal(clause[1].absoluteValue - 1, clause[1] > 0),
                            Literal(clause[2].absoluteValue - 1, clause[2] > 0)
                        )
                    )
                )
            }
        }
    }

    if(showDebugsLoader) {
        print(numberOfVariabes)
        print(numberOfClauses)
        println(clauses)
        println(weights)
    }

    check(numberOfVariabes > 0) { "numberOfVariables must be greater than 0" }
    check(numberOfClauses > 0) { "numberOfClauses must be greater than 0" }
    check(clauses.size == numberOfClauses) { "numberOfClauses must be equal to the number of clauses" }
    check(weights.size == numberOfVariabes) { "numberOfClauses must be equal to the number of weights" }

    return WeightedFormula("test", clauses, weights)
}