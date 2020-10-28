#include <stdio.h>
#include <time.h>

#include "Nbody/Nbody.h"

void Run(const char* name, int iterations, void (*action)())
{
    printf("Running benchmark %s for %d iterations... ", name, iterations);
    clock_t begin = clock();
    for(int i = 1; i <= iterations; i++)
    {
        printf("%d ", i);
        action();
    }
    clock_t end = clock();
    printf("Elapsed time %f ms.", (double)(end - begin) * 1000.0 / (iterations * CLOCKS_PER_SEC));
}

void Nbody()
{
    RunBenchmark(50000000, 0);
}


int main()
{
    Run("Nbody", 10, Nbody);
    return 0;
}


