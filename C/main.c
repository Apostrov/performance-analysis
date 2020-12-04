#include <stdio.h>
#include <time.h>

#include "Benchmarks/Benchmarks.h"

static const char fastaOuput[] = "/home/apo/work/performance-analysis/C/Benchmarks/assets/fasta.txt";

void Run(const char* name, int iterations, void (*action)())
{
    printf("Running benchmark %s for %d iterations... ", name, iterations);
    clock_t begin = clock();
    for(int i = 1; i <= iterations; i++)
    {
        printf("%d ", i);
        fflush(stdout);
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

void Fasta()
{
    FastaRun(fastaOuput, 25000000);
}

void KNucleotide()
{
    KNucleotideRun(fastaOuput, 0);
}


int main()
{
    Run("FannkuchRedux", 10, FannkuchRedux);
    Run("Nbody", 10, Nbody);
    Run("Fasta", 10, Fasta);
    Run("KNucleotide", 10, KNucleotide);
    return 0;
}


