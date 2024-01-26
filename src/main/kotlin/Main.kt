
import java.io.File
import kotlin.math.log

val dataFolder = File("data")

fun main(args: Array<String>) {
    println("Program arguments: ${args.joinToString()}")

    val folder = "20-91"
    val type = "Q"

    val optimums = loadOptimums("wuf$folder/wuf$folder-$type-opt.dat")

    val files = List(100) {
       dataFolder.resolve("wuf$folder").resolve("wuf$folder-$type").resolve("wuf${folder.split("-").get(0)}-0${it+1}.mwcnf")
    }

    var satisfied = 0
    var satisfiendMaximum = 0
    val resultMap = mutableMapOf (
        1.0 to (0 to 0),
        0.97 to (0 to 0),
        0.95 to (0 to 0),
        0.9 to (0 to 0),
        0.93 to (0 to 0),
        0.8 to (0 to 0),
        0.7 to (0 to 0),
        0.6 to (0 to 0),
        0.5 to (0 to 0)
    )

    files.forEach { file ->
        println(file)
        val formula = loadWuf(file.path)

       // println(log(formula.weights.max().toDouble(), formula.weights.min().toDouble() + 0.5))

        var iterations = 0

        formula.printStats()
        repeat(1) {
            val wsasat =
                Wsasat(coolingCoefficient = 0.99, initialTemp = null, targetTemp = 1.0, innerCycles = 30)
            //println(wsasat.estimateExponent(formula))
            val sol = wsasat.solve(formula)
            iterations += sol.iterations.toInt()
            println(sol)
            if (!sol.satisfied) {

            } else {
                satisfied++
                if (sol.state.weight == optimums[file.path.split("/").last().split(".")[0]]!!.weight) {
                    satisfiendMaximum++
                }
            }
        }
        val f = State(optimums[file.path.split("/").last().split(".")[0]]!!.variables, formula.normalizedWeights, formula, formula.weights.min().toDouble()/ formula.weights.max())
        println("iterations ${iterations/10}")
        println("satisfied $satisfied")
        println("satisfied maximum $satisfiendMaximum")
        satisfied = 0
        satisfiendMaximum = 0

    }
}

data class OptimalResult(val weight: Int, val variables: List<Boolean>)

fun loadOptimums(path: String): Map<String, OptimalResult> {
    val map = mutableMapOf<String, OptimalResult>()
    dataFolder.resolve(path).forEachLine { line ->
        val split = line.split(" ").dropLast(1)
        map["w"+split[0]] = OptimalResult(split[1].toInt(), List(split.size - 2){ split[it+2].toInt() > 0})


    }
    println(map["wuf20-01"])
    return map
}

