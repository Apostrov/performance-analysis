#include <stdio.h>
#include <time.h>

#include "Benchmarks/Benchmarks.h"

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
    printf("| Elapsed time %f ms.\n", (double)(end - begin) * 1000.0 / (iterations * CLOCKS_PER_SEC));
}

void FannkuchRedux()
{
    FannkuchReduxRun(12, 0);
}

void Nbody()
{
    NbodyRun(50000000, 0);
}


int main()
{
    Run("FannkuchRedux", 1, FannkuchRedux);
    //Run("Nbody", 1, Nbody);
    return 0;
}


