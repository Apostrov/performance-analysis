package Benchmarks

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.AbstractMap.SimpleEntry
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.experimental.and

/* The Computer Language Benchmarks Game
 https://salsa.debian.org/benchmarksgame-team/benchmarksgame/

 contributed by James McIlree
 modified by Tagir Valeev
 */

object Knucleotide {
    val codes = byteArrayOf(-1, 0, -1, 1, 3, -1, -1, 2)
    val nucleotides = charArrayOf('A', 'C', 'G', 'T')
    fun createFragmentTasks(
        sequence: ByteArray,
        fragmentLengths: IntArray
    ): ArrayList<Callable<Result>> {
        val tasks = ArrayList<Callable<Result>>()
        for (fragmentLength in fragmentLengths) {
            for (index in 0 until fragmentLength) {
                tasks.add(Callable { createFragmentMap(sequence, index, fragmentLength) })
            }
        }
        return tasks
    }

    fun createFragmentMap(sequence: ByteArray, offset: Int, fragmentLength: Int): Result {
        val res = Result(fragmentLength)
        val map = res.map
        val lastIndex = sequence.size - fragmentLength + 1
        var index = offset
        while (index < lastIndex) {
            map.addTo(getKey(sequence, index, fragmentLength), 1)
            index += fragmentLength
        }
        return res
    }

    fun sumTwoMaps(map1: Result, map2: Result): Result {
        map2.map.forEach { (key: Long?, value: Int?) -> map1.map.addTo(key!!, value!!) }
        return map1
    }

    fun writeFrequencies(totalCount: Float, frequencies: Result): String {
        val freq: MutableList<Map.Entry<String, Int>> = ArrayList<Map.Entry<String, Int>>(frequencies.map.size)
        frequencies.map.forEach { (key: Long, cnt: Int) ->
            freq.add(
                SimpleEntry(
                    keyToString(
                        key,
                        frequencies.keyLength
                    ), cnt
                )
            )
        }
        freq.sortWith(java.util.Map.Entry.comparingByValue(Comparator.reverseOrder()))
        val sb = StringBuilder()
        for ((key, value) in freq) {
            sb.append(
                String.format(
                    Locale.ENGLISH, "%s %.3f\n", key,
                    value * 100.0f / totalCount
                )
            )
        }
        return sb.append('\n').toString()
    }

    @Throws(Exception::class)
    fun writeCount(futures: List<Future<Result>>, nucleotideFragment: String): String {
        val key = toCodes(
            nucleotideFragment.toByteArray(StandardCharsets.ISO_8859_1),
            nucleotideFragment.length
        )
        val k = getKey(key, 0, nucleotideFragment.length)
        var count = 0
        for (future in futures) {
            val f = future.get()
            if (f.keyLength == nucleotideFragment.length) {
                count += f.map[k]
            }
        }
        return """$count	$nucleotideFragment
"""
    }

    /**
     * Convert long key to the nucleotides string
     */
    fun keyToString(key: Long, length: Int): String {
        var key = key
        val res = CharArray(length)
        for (i in 0 until length) {
            res[length - i - 1] = nucleotides[(key and 0x3).toInt()]
            key = key shr 2
        }
        return String(res)
    }

    /**
     * Get the long key for given byte array of codes at given offset and length
     * (length must be less than 32)
     */
    fun getKey(arr: ByteArray, offset: Int, length: Int): Long {
        var key: Long = 0
        for (i in offset until offset + length) {
            key = key * 4 + arr[i]
        }
        return key
    }

    /**
     * Convert given byte array (limiting to given length) containing acgtACGT
     * to codes (0 = A, 1 = C, 2 = G, 3 = T) and returns new array
     */
    fun toCodes(sequence: ByteArray, length: Int): ByteArray {
        val result = ByteArray(length)
        for (i in 0 until length) {
            result[i] = codes[(sequence[i] and 0x7).toInt()]
        }
        return result
    }

    @Throws(IOException::class)
    fun read(`is`: InputStream?): ByteArray {
        var line: String
        val `in` = BufferedReader(
            InputStreamReader(
                `is`,
                StandardCharsets.ISO_8859_1
            )
        )
        while (`in`.readLine().also { line = it } != null) {
            if (line.startsWith(">THREE")) break
        }
        var bytes = ByteArray(1048576)
        var position = 0
        while (`in`.readLine().also { line = it } != null && line[0] != '>') {
            if (line.length + position > bytes.size) {
                val newBytes = ByteArray(bytes.size * 2)
                System.arraycopy(bytes, 0, newBytes, 0, position)
                bytes = newBytes
            }
            for (i in 0 until line.length) bytes[position++] = line[i].toByte()
        }
        return toCodes(bytes, position)
    }

    @Throws(Exception::class)
    fun runBenchmark(inputFile: String, output: Boolean = false) {
        val sequence = File(inputFile).inputStream().readBytes()
        val pool = Executors.newFixedThreadPool(
            Runtime.getRuntime()
                .availableProcessors()
        )
        val fragmentLengths = intArrayOf(1, 2, 3, 4, 6, 12, 18)
        val futures = pool.invokeAll(
            createFragmentTasks(
                sequence,
                fragmentLengths
            )
        )
        pool.shutdown()
        val sb = StringBuilder()
        sb.append(writeFrequencies(sequence.size.toFloat(), futures[0].get()))
        sb.append(
            writeFrequencies(
                (sequence.size - 1).toFloat(),
                sumTwoMaps(futures[1].get(), futures[2].get())
            )
        )
        val nucleotideFragments = arrayOf(
            "GGT", "GGTA", "GGTATT", "GGTATTTTAATT",
            "GGTATTTTAATTTATAGT"
        )
        for (nucleotideFragment in nucleotideFragments) {
            sb.append(writeCount(futures, nucleotideFragment))
        }
        if (output) {
            print(sb)
        }
    }

    class Result(var keyLength: Int) {
        var map = Long2IntOpenHashMap()
    }
}