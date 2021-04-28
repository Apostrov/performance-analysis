#include <stdio.h>
#include <time.h>
#include <sys/time.h>

#include "Benchmarks/Benchmarks.h"

static const char fastaOutputPath[] = "fasta.txt";

void Run(const char* name, int iterations, void (*action)())
{
    struct timeval tp;
    gettimeofday(&tp, 0);
    time_t curtime = tp.tv_sec;
    struct tm *t = localtime(&curtime);
    printf("Time '%02d:%02d:%02d.%03d'. ", t->tm_hour, t->tm_min, t->tm_sec, tp.tv_usec/1000);
    printf("Running benchmark '%s' for %d iterations... ", name, iterations);
    clock_t begin = clock();
    for(int i = 1; i <= iterations; i++)
    {
        printf("%d ", i);
        fflush(stdout);
        action();
    }
    clock_t end = clock();

    gettimeofday(&tp, 0);
    curtime = tp.tv_sec;
    t = localtime(&curtime);
    printf("| Time '%02d:%02d:%02d.%03d'. ", t->tm_hour, t->tm_min, t->tm_sec, tp.tv_usec/1000);
    printf("Elapsed time %f ms.\n", (double)(end - begin) * 1000.0 / (iterations * CLOCKS_PER_SEC));
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
    FastaRun(fastaOutputPath, 25000000);
}

void KNucleotide()
{
    KNucleotideRun(fastaOutputPath, 0);
}


int main()
{
    Run("FannkuchRedux", 10, FannkuchRedux);
    Run("Nbody", 10, Nbody);
    Run("Fasta", 10, Fasta);
    Run("KNucleotide", 10, KNucleotide);

    remove(fastaOutputPath);

    return 0;
}


