import Benchmarks.FannkuchRedux
import Benchmarks.Knucleotide
import Benchmarks.Fasta
import Benchmarks.Nbody
import kotlin.system.measureTimeMillis

private const val fastaOutputPath = "A:\\Projects\\performance-analysis\\Kotlin\\src\\Benchmarks\\assets\\fasta.txt"

fun main() {
    PerformanceAnalyze.Run("FannkuchRedux", 10 ) { FannkuchRedux.runBenchmark(12) }
    PerformanceAnalyze.Run("Nbody", 15) { Nbody.runBenchmark(50000000) }
    PerformanceAnalyze.Run("Fasta", 5) { Fasta.runBenchmark(fastaOutputPath, 25000000 ) }
    PerformanceAnalyze.Run("Knucleotide", 10 ) { Knucleotide.runBenchmark(fastaOutputPath,) }
}

class PerformanceAnalyze {
    companion object {
        fun Run(name: String, iterations: Int, action: () -> Unit) {
            // Perform garbage collection.
            System.gc()
            System.runFinalization();

            print("Running benchmark '${name}' for $iterations iterations... ")
            var time = 0L;
            for(i in 1..iterations)
            {
                print("$i ")
                time += measureTimeMillis {
                    action()
                }
            }
            println("| Elapsed time ${time / iterations} ms.")
        }
    }
}