import Benchmarks.Fasta
import Benchmarks.Knucleotide
import kotlin.system.measureTimeMillis
import java.io.File

private const val fastaOutputPath = "fasta.txt"

fun main() {
    PerformanceAnalyze.Run("FannkuchRedux", 10) { Benchmarks.FannkuchRedux.runBenchmark(12) }
    PerformanceAnalyze.Run("Nbody", 10) { Benchmarks.Nbody.runBenchmark(50000000) }
    PerformanceAnalyze.Run("Fasta", 1) { Fasta.runBenchmark(fastaOutputPath, 25000000) }
    PerformanceAnalyze.Run("Knucleotide", 1) { Knucleotide.runBenchmark(fastaOutputPath) }

    val myFile = File(fastaOutputPath)
    if (myFile.exists())
        myFile.delete()
}

class PerformanceAnalyze {
    companion object {
        fun Run(name: String, iterations: Int, action: () -> Unit) {
            // Perform garbage collection.
            System.gc()
            System.runFinalization();

            print("Running benchmark '${name}' for $iterations iterations... ")
            var time = 0L;
            for (i in 1..iterations) {
                print("$i ")
                time += measureTimeMillis {
                    action()
                }
            }
            println("| Elapsed time ${time / iterations} ms.")
        }
    }
}
