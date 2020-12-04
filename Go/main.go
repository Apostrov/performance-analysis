package main

import (
	"Benchmarks"
	"fmt"
	"runtime"
	"time"
)

func main() {
	run("Nbody", 10, func() {
		Benchmarks.NbodyRun(50000000, false)
	})
}

func run(name string, iterations int, action func()) {
	fmt.Printf("Running benchmark %s for %d iterations... ", name, iterations)

	// Perform garbage collection.
	runtime.GC()

	start := time.Now()
	for i := 0; i < iterations; i++ {
		fmt.Printf("%d ", i)
		action()
	}
	duration := time.Since(start)
	fmt.Println()
	fmt.Printf("Elapsed time %d ms.\n", duration.Milliseconds())
}