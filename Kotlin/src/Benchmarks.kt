import Nbody.nbody1.nbody1
import Nbody.nbody2.nbody2
import kotlin.system.measureTimeMillis

fun main() {
    Benchmarks.Run("nbody1", 10) { nbody1.runBenchmark(50000000) }
    Benchmarks.Run("nbody2", 10) { nbody2.runBenchmark(50000000) }
}

class Benchmarks {
    companion object {
        fun Run(name: String, iterations: Int, action: () -> Unit) {
            print("Running benchmark '${name}' for $iterations iterations... ")
            var time = 0L;
            for(i in 1..iterations)
            {
                print("$i ")
                time += measureTimeMillis {
                    action()
                }
            }
            println("Elapsed time ${time / iterations} ms.")
        }
    }
}