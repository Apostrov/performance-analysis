using System;
using System.Diagnostics;
using Benchmarks;

namespace PerformanceAnalyze
{
    public static class Benchmark
    {
        private const string fastaOutputPath =
            @"A:\Projects\performance-analysis\Csharp\Benchmarks\assets\fasta.txt"; // Warning! file can have big size

        public static void Main(string[] args)
        {
            Run("FannkuchRedux", 10, () => FannkuchRedux.RunBenchmark(12));
            Run("Nbody", 10, () => NBody.RunBenchmark(50000000));
            // Run fasta before ReverseComplement, because it uses his output
            Run("Fasta", 10, () => Fasta.RunBenchmark(fastaOutputPath, 25000000));
            Run("ReverseComplement", 3, () => ReverseComplement.RunBenchmark(fastaOutputPath)); // TODO n > 3 iteration fail to execute
        }
        
        public static void Run(string name, int iterations, Action action)
        {
            try
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
            catch (OutOfMemoryException)
            {
                Console.WriteLine($"Out of memory!");
            }
        }
    }
}