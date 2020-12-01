package Benchmarks

import java.io.*
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/*
* The Computer Language Benchmarks Game
* https://salsa.debian.org/benchmarksgame-team/benchmarksgame/

* contributed by Han Kai
*/
object ReverseComplement {
    @Throws(Exception::class)
    @JvmStatic
    fun runBenchmark(outputFile: String, output: Boolean = false) {
        Strand().use { strand ->
            FileInputStream(outputFile).use { standIn ->
                FileOutputStream(FileDescriptor.out).use { standOut ->
                    while (strand.readOneStrand(standIn) >= 0) {
                        strand.reverse()
                        strand.write(standOut)
                        strand.reset()
                    }
                }
            }
        }
    }
}

internal class Chunk {
    var capacity = 0
    var length = 0
    val bytes = ByteArray(CHUNK_SIZE)
    fun clear() {
        capacity = 0
        length = 0
    }

    companion object {
        const val CHUNK_SIZE = 64 * 1024
    }
}

internal class Strand : Closeable {
    companion object {
        private const val NEW_LINE = '\n'.toByte()
        private const val ANGLE = '>'.toByte()
        private const val LINE_LENGTH = 61
        private val map = ByteArray(128)
        private val NCPU = Runtime.getRuntime().availableProcessors()

        init {
            for (i in map.indices) {
                map[i] = i.toByte()
            }
            map['T'.toInt()] = 'A'.toByte()
            map['t'.toInt()] = map['T'.toInt()]
            map['A'.toInt()] = 'T'.toByte()
            map['a'.toInt()] = map['A'.toInt()]
            map['G'.toInt()] = 'C'.toByte()
            map['g'.toInt()] = map['G'.toInt()]
            map['C'.toInt()] = 'G'.toByte()
            map['c'.toInt()] = map['C'.toInt()]
            map['V'.toInt()] = 'B'.toByte()
            map['v'.toInt()] = map['V'.toInt()]
            map['H'.toInt()] = 'D'.toByte()
            map['h'.toInt()] = map['H'.toInt()]
            map['R'.toInt()] = 'Y'.toByte()
            map['r'.toInt()] = map['R'.toInt()]
            map['M'.toInt()] = 'K'.toByte()
            map['m'.toInt()] = map['M'.toInt()]
            map['Y'.toInt()] = 'R'.toByte()
            map['y'.toInt()] = map['Y'.toInt()]
            map['K'.toInt()] = 'M'.toByte()
            map['k'.toInt()] = map['K'.toInt()]
            map['B'.toInt()] = 'V'.toByte()
            map['b'.toInt()] = map['B'.toInt()]
            map['D'.toInt()] = 'H'.toByte()
            map['d'.toInt()] = map['D'.toInt()]
            map['U'.toInt()] = 'A'.toByte()
            map['u'.toInt()] = map['U'.toInt()]
        }
    }

    private val executor = Executors.newFixedThreadPool(NCPU)
    private var chunkCount = 0
    private val chunks = ArrayList<Chunk>()
    private fun ensureSize() {
        if (chunkCount == chunks.size) {
            chunks.add(Chunk())
        }
    }

    private fun isLastChunk(chunk: Chunk): Boolean {
        return chunk.length != chunk.capacity
    }

    private fun correctLentgh(chunk: Chunk, skipFirst: Boolean) {
        val bytes = chunk.bytes
        val start = if (skipFirst) 1 else 0
        val end = chunk.capacity
        for (i in start until end) {
            if (ANGLE == bytes[i]) {
                chunk.length = i
                return
            }
        }
        chunk.length = chunk.capacity
    }

    private fun prepareNextStrand() {
        if (chunkCount == 0) {
            return
        }
        val first = chunks[0]
        val last = chunks[chunkCount - 1]
        if (last.capacity == last.length) {
            for (i in 0 until chunkCount) {
                chunks[i].clear()
            }
            return
        }
        System.arraycopy(last.bytes, last.length, first.bytes, 0, last.capacity - last.length)
        first.capacity = last.capacity - last.length
        correctLentgh(first, true)
        for (i in 1 until chunkCount) {
            chunks[i].clear()
        }
    }

    @Throws(IOException::class)
    fun readOneStrand(`is`: InputStream): Int {
        while (true) {
            ensureSize()
            val chunk = chunks[chunkCount]
            chunkCount++
            if (isLastChunk(chunk)) {
                return chunkCount
            }
            val bytes = chunk.bytes
            val readLength = `is`.read(bytes, chunk.length, Chunk.CHUNK_SIZE - chunk.length)
            if (chunkCount == 1 && readLength < 0 && chunk.length == 0) {
                return -1
            }
            if (readLength > 0) {
                chunk.capacity += readLength
                correctLentgh(chunk, chunkCount == 1)
            }
            if (readLength < 0 || isLastChunk(chunk)) {
                return chunkCount
            }
        }
    }

    fun reset() {
        prepareNextStrand()
        chunkCount = 0
    }

    @Throws(IOException::class)
    fun write(out: OutputStream) {
        for (i in 0 until chunkCount) {
            val chunk = chunks[i]
            out.write(chunk.bytes, 0, chunk.length)
        }
    }

    @Throws(InterruptedException::class)
    fun reverse() {
        val sumLength = sumLength
        val titleLength = titleLength
        val dataLength = sumLength - titleLength
        val realDataLength = dataLength - ceilDiv(dataLength, LINE_LENGTH)
        val leftEndIndex = realDataLength / 2
        val rawLeftEndIndex = leftEndIndex + leftEndIndex / (LINE_LENGTH - 1)
        val leftEndChunkIndex = ceilDiv(rawLeftEndIndex + titleLength, Chunk.CHUNK_SIZE) - 1
        val realLeftEndIndex = (rawLeftEndIndex + titleLength) % Chunk.CHUNK_SIZE - 1
        val tasks: MutableList<Callable<Void?>> = ArrayList(NCPU)
        val itemCount = ceilDiv(leftEndChunkIndex + 1, NCPU)
        for (t in 0 until NCPU) {
            val start = itemCount * t
            val end = Math.min(start + itemCount, leftEndChunkIndex + 1)
            val task = Callable<Void?> {
                for (i in start until end) {
                    val rawLeftIndex = if (i == 0) 0 else i * Chunk.CHUNK_SIZE - titleLength
                    val leftIndex = rawLeftIndex - rawLeftIndex / LINE_LENGTH
                    val rightIndex = realDataLength - leftIndex - 1
                    val rawRightIndex = rightIndex + rightIndex / (LINE_LENGTH - 1)
                    val rightChunkIndex = ceilDiv(rawRightIndex + titleLength, Chunk.CHUNK_SIZE) - 1
                    val realLeftIndex = (rawLeftIndex + titleLength) % Chunk.CHUNK_SIZE
                    val realRightIndex = (rawRightIndex + titleLength) % Chunk.CHUNK_SIZE
                    val endIndex = if (leftEndChunkIndex == i) realLeftEndIndex else chunks[i].length - 1
                    reverse(i, rightChunkIndex, realLeftIndex, realRightIndex, endIndex)
                }
                null
            }
            tasks.add(task)
        }
        executor.invokeAll(tasks)
    }

    private fun reverse(leftChunkIndex: Int, rightChunkIndex: Int, leftIndex: Int, rightIndex: Int, leftEndIndex: Int) {
        var rightChunkIndex = rightChunkIndex
        var leftIndex = leftIndex
        var rightIndex = rightIndex
        val map = map
        val leftChunk = chunks[leftChunkIndex]
        var rightChunk = chunks[rightChunkIndex]
        val leftBytes = leftChunk.bytes
        var rightBytes = rightChunk.bytes
        while (leftIndex <= leftEndIndex) {
            if (rightIndex < 0) {
                rightChunk = chunks[--rightChunkIndex]
                rightBytes = rightChunk.bytes
                rightIndex = rightChunk.length - 1
            }
            if (leftBytes[leftIndex] == NEW_LINE) {
                leftIndex++
            }
            if (rightBytes[rightIndex] == NEW_LINE) {
                rightIndex--
                if (rightIndex < 0) {
                    rightChunk = chunks[--rightChunkIndex]
                    rightBytes = rightChunk.bytes
                    rightIndex = rightChunk.length - 1
                }
            }
            if (leftIndex <= leftEndIndex) {
                val lByte = leftBytes[leftIndex]
                val rByte = rightBytes[rightIndex]
                leftBytes[leftIndex++] = map[rByte.toInt()]
                rightBytes[rightIndex--] = map[lByte.toInt()]
            }
        }
    }

    private fun ceilDiv(a: Int, b: Int): Int {
        return (a + b - 1) / b
    }

    private val sumLength: Int
        private get() {
            var sumLength = 0
            for (i in 0 until chunkCount) {
                sumLength += chunks[i].length
            }
            return sumLength
        }
    private val titleLength: Int
        private get() {
            val first = chunks[0]
            val bytes = first.bytes
            for (i in 0 until first.length) {
                if (bytes[i] == NEW_LINE) {
                    return i + 1
                }
            }
            return -1
        }

    @Throws(IOException::class)
    override fun close() {
        executor.shutdown()
    }
}