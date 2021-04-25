package main

import (
	"fmt"
	"os"
	"runtime"
	"time"
)

const fastaOutputPath = "fasta.txt"

func main() {
	run("FannkuchReduxRun", 10, func() {
		FannkuchReduxRun(12 , false)
	})
	run("Nbody", 10, func() {
		NbodyRun(50000000, false)
	})
	run("Fasta", 10, func() {
		FastaRun(fastaOutputPath, 25000000)
	})
	run("Knucleotide", 10, func() {
		KnucleotidexRun(fastaOutputPath, false)
	})

	var _ = os.Remove(fastaOutputPath)
}

func run(name string, iterations int, action func()) {


	// Perform garbage collection.
	runtime.GC()

	fmt.Print("Time '", time.Now().Format("15:04:05.0000"))
	fmt.Printf("'. Running benchmark '%s' for %d iterations... ", name, iterations)

	start := time.Now()
	for i := 1; i <= iterations; i++ {
		fmt.Printf("%d ", i)
		action()
	}
	watch := time.Since(start)

	fmt.Printf("| Elapsed time %d ms.\n", watch.Milliseconds()/int64(iterations))
}
