using System;
using System.Diagnostics;
using System.IO;
using Benchmarks;

namespace PerformanceAnalyze
{
    public static class PerformanceAnalyze
    {
        private const string fastaOutputPath = @"fasta.txt";

        public static void Main(string[] args)
        {
            Run("FannkuchRedux", 10, () => FannkuchRedux.RunBenchmark(12));
            Run("Nbody", 10, () => NBody.RunBenchmark(50000000));
            // Run Fasta before KNucleotide, because it uses his output
            Run("Fasta", 10, () => Fasta.RunBenchmark(fastaOutputPath, 25000000));
            Run("KNucleotide", 10, () => KNucleotide.RunBenchmark(fastaOutputPath));

            if (File.Exists(fastaOutputPath))
            {
                File.Delete(fastaOutputPath);
            }
        }

        public static void Run(string name, int iterations, Action action)
        {
            Console.Write($"Running benchmark '{name}' for {iterations} iterations... ");

            // Perform garbage collection.
            GC.Collect();
            GC.WaitForPendingFinalizers();

            // Force JIT compilation of the method.
            action.Invoke();

            // Run the benchmark.
            Stopwatch watch = Stopwatch.StartNew();
            for (int i = 0; i < iterations; i++)
            {
                Console.Write($"{i + 1} ");
                action.Invoke();
            }

            watch.Stop();

            // Output results.
            Console.WriteLine($"| Elapsed time {watch.ElapsedMilliseconds / iterations} ms.");
        }
    }
}