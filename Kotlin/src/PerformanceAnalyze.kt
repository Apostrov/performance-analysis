import Benchmarks.ReverseComplement
import kotlin.system.measureTimeMillis

private const val fastaOutputPath = "A:\\Projects\\performance-analysis\\Kotlin\\src\\Benchmarks\\assets\\fasta.txt"

fun main() {
    //PerformanceAnalyze.Run("Nbody", 15) { NbodyJava.runBenchmark(50000000) }
    //PerformanceAnalyze.Run("Fasta", 5) { FastaJava.runBenchmark(fastaOutputPath, 25000000 ) }
    PerformanceAnalyze.Run("ReverseComplement", 1 ) { ReverseComplement.runBenchmark(fastaOutputPath,) }
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