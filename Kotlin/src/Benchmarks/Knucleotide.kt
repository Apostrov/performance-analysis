/* The Computer Language Benchmarks Game
 https://salsa.debian.org/benchmarksgame-team/benchmarksgame/

 contributed by James McIlree
 ByteString code thanks to Matthieu Bentot and The Anh Tran
 modified by Andy Fingerhut
 */
package Benchmarks

import java.util.concurrent.Callable
import kotlin.Throws
import java.lang.Exception
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future

object Knucleotide {
    fun createFragmentTasks(sequence: ByteArray, fragmentLengths: IntArray): ArrayList<Callable<Map<ByteString, ByteString>>> {
        val tasks = ArrayList<Callable<Map<ByteString, ByteString>>>()
        for (fragmentLength in fragmentLengths) {
            for (index in 0 until fragmentLength) {
                tasks.add(Callable { createFragmentMap(sequence, index, fragmentLength) })
            }
        }
        return tasks
    }

    fun createFragmentMap(sequence: ByteArray, offset: Int, fragmentLength: Int): Map<ByteString, ByteString> {
        val map = HashMap<ByteString, ByteString>()
        val lastIndex = sequence.size - fragmentLength + 1
        var key = ByteString(fragmentLength)
        var index = offset
        while (index < lastIndex) {
            key.calculateHash(sequence, index)
            val fragment = map[key]
            if (fragment != null) {
                fragment.count++
            } else {
                map[key] = key
                key = ByteString(fragmentLength)
            }
            index += fragmentLength
        }
        return map
    }

    // Destructive!
    fun sumTwoMaps(map1: MutableMap<ByteString, ByteString>, map2: Map<ByteString, ByteString>): Map<ByteString, ByteString> {
        for ((key, value) in map2) {
            val sum = map1[key]
            if (sum != null) sum.count += value.count else map1[key] = value
        }
        return map1
    }

    fun writeFrequencies(totalCount: Float, frequencies: Map<ByteString, ByteString>): String {
        val list: SortedSet<ByteString> = TreeSet(frequencies.values)
        val sb = StringBuilder()
        for (k in list) sb.append(String.format("%s %.3f\n", k.toString().toUpperCase(), k.count.toFloat() * 100.0f / totalCount))
        return sb.append('\n').toString()
    }

    @Throws(Exception::class)
    fun writeCount(futures: MutableList<Future<Map<ByteString, ByteString>>>, nucleotideFragment: String): String {
        val key = ByteString(nucleotideFragment.length)
        key.calculateHash(nucleotideFragment.toByteArray(), 0)
        var count = 0
        for (future in futures) {
            val temp = future.get()[key]
            if (temp != null) count += temp.count
        }
        return """$count	${nucleotideFragment.toUpperCase()}
"""
    }

    @Throws(Exception::class)
    fun runBenchmark(inputFile: String, output: Boolean = false) {
        val `in` = File(inputFile).inputStream().bufferedReader()
        var start = false
        val baos = ByteArrayOutputStream()
        for (line in `in`.lines()) {
            if (start) {
                val bytes = ByteArray(line.length)
                var i: Int = 0
                while (i < line.length) {
                    bytes[i] = line[i].toByte()
                    i++
                }
                baos.write(bytes, 0, i)
            } else if (line.startsWith(">THREE")) {
                start = true
            }
        }
        val sequence = baos.toByteArray()
        val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val fragmentLengths = intArrayOf(1, 2, 3, 4, 6, 12, 18)
        val futures: MutableList<Future<Map<ByteString, ByteString>>> = pool.invokeAll(createFragmentTasks(sequence, fragmentLengths))
        pool.shutdown()
        val sb = StringBuilder()
        sb.append(writeFrequencies(sequence.size.toFloat(), futures[0].get()))
        sb.append(writeFrequencies((sequence.size - 1).toFloat(), sumTwoMaps(futures[1].get() as MutableMap<ByteString, ByteString>, futures[2].get())))
        val nucleotideFragments = arrayOf("ggt", "ggta", "ggtatt", "ggtattttaatt", "ggtattttaatttatagt")
        for (nucleotideFragment in nucleotideFragments) {
            sb.append(writeCount(futures, nucleotideFragment))
        }
        if (output)
            print(sb.toString())
    }

    class ByteString(size: Int) : Comparable<ByteString> {
        var hash = 0
        var count = 1
        val bytes: ByteArray
        fun calculateHash(k: ByteArray, offset: Int) {
            var temp = 0
            for (i in bytes.indices) {
                val b = k[offset + i]
                bytes[i] = b
                temp = temp * 31 + b
            }
            hash = temp
        }

        override fun hashCode(): Int {
            return hash
        }

        override fun equals(obj: Any?): Boolean {
            return Arrays.equals(bytes, (obj as ByteString)!!.bytes)
        }

        override fun compareTo(other: ByteString): Int {
            return if (other.count != count) {
                other.count - count
            } else {
                // Without this case, if there are two or more strings
                // with exactly the same count in a Map, then the
                // TreeSet constructor called in writeFrequencies will
                // only add the first one, and the rest will not
                // appear in the output.  Also this is required to
                // satisfy the rules of the k-nucleotide problem.
                toString().compareTo(other.toString())
            }
        }

        override fun toString(): String {
            return String(bytes)
        }

        init {
            bytes = ByteArray(size)
        }
    }
}