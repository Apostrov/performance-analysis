using System;
using System.Diagnostics;

namespace Benchmarks
{
    public static class Benchmark
    {
        static void Main(string[] args)
        {
            Run("Nbody1", 10, () => Nbody.NBody1.NBodyRun(50000000));
            Run("Nbody2", 10, () => Nbody.Nbody2.NBodyRun(50000000));
            Run("Nbody3", 10, () => Nbody.Nbody3.NBodyRun(50000000));
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
                Console.WriteLine($"Elapsed time {watch.ElapsedMilliseconds / iterations} ms.");
            }
            catch (OutOfMemoryException)
            {
                Console.WriteLine($"Out of memory!");
            }
        }
    }
}