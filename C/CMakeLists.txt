# cmake_minimum_required(VERSION <specify CMake version here>)
project(C C)

if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release)
endif()

set(CMAKE_C_STANDARD 99)
set(CMAKE_C_FLAGS "-O3")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -msse4.1")

add_executable(C main.c Benchmarks/Benchmarks.h Benchmarks/Nbody.c Benchmarks/FannkuchRedux.c Benchmarks/Fasta.c Benchmarks/Knucleotide.c)
target_link_libraries(C PRIVATE m)