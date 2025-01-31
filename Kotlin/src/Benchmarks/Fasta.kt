package Benchmarks

import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicInteger

/*
 * The Computer Language Benchmarks Game
 * https://salsa.debian.org/benchmarksgame-team/benchmarksgame/
 *
 * modified by Mehmet D. AKIN
 * modified by Daryl Griffith
 */
object Fasta {
    const val LINE_LENGTH = 60
    const val LINE_COUNT = 1024
    val WORKERS = arrayOfNulls<NucleotideSelector>(
        if (Runtime.getRuntime().availableProcessors() > 1) Runtime.getRuntime().availableProcessors() - 1 else 1
    )
    val IN = AtomicInteger()
    val OUT = AtomicInteger()
    const val BUFFERS_IN_PLAY = 6
    const val IM = 139968
    const val IA = 3877
    const val IC = 29573
    const val ONE_OVER_IM = 1f / IM
    var last = 42

    @JvmStatic
    fun runBenchmark(outputFile: String, repeatTimes: Int = 1000) {
        var n = repeatTimes
        for (i in WORKERS.indices) {
            WORKERS[i] = NucleotideSelector()
            WORKERS[i]!!.isDaemon = true
            WORKERS[i]!!.start()
        }
        try {
            File(outputFile).outputStream().use { writer ->
                val bufferSize = LINE_COUNT * LINE_LENGTH
                for (i in 0 until BUFFERS_IN_PLAY) {
                    lineFillALU(
                        AluBuffer(LINE_LENGTH, bufferSize, i * bufferSize)
                    )
                }
                speciesFillALU(writer, n * 2, ">ONE Homo sapiens alu\n")
                for (i in 0 until BUFFERS_IN_PLAY) {
                    writeBuffer(writer)
                    lineFillRandom(Buffer(true, LINE_LENGTH, bufferSize))
                }
                speciesFillRandom(
                    writer, n * 3, ">TWO IUB ambiguity codes\n", true
                )
                for (i in 0 until BUFFERS_IN_PLAY) {
                    writeBuffer(writer)
                    lineFillRandom(Buffer(false, LINE_LENGTH, bufferSize))
                }
                speciesFillRandom(
                    writer, n * 5, ">THREE Homo sapiens frequency\n", false
                )
                for (i in 0 until BUFFERS_IN_PLAY) {
                    writeBuffer(writer)
                }
            }
        } catch (ex: IOException) {
        }
    }

    private fun lineFillALU(buffer: AbstractBuffer?) {
        WORKERS[OUT.incrementAndGet() % WORKERS.size]!!.put(buffer)
    }

    @Throws(IOException::class)
    private fun bufferFillALU(
        writer: OutputStream, buffers: Int
    ) {
        var buffer: AbstractBuffer?
        for (i in 0 until buffers) {
            buffer = WORKERS[IN.incrementAndGet() % WORKERS.size]!!.take()
            writer.write(buffer!!.nucleotides)
            lineFillALU(buffer)
        }
    }

    @Throws(IOException::class)
    private fun speciesFillALU(
        writer: OutputStream, nChars: Int, name: String
    ) {
        val bufferSize = LINE_COUNT * LINE_LENGTH
        val bufferCount = nChars / bufferSize
        val bufferLoops = bufferCount - BUFFERS_IN_PLAY
        val charsLeftover = nChars - bufferCount * bufferSize
        writer.write(name.toByteArray())
        bufferFillALU(writer, bufferLoops)
        if (charsLeftover > 0) {
            writeBuffer(writer)
            lineFillALU(
                AluBuffer(
                    LINE_LENGTH, charsLeftover, nChars - charsLeftover
                )
            )
        }
    }

    private fun lineFillRandom(buffer: Buffer?) {
        for (i in buffer!!.randoms.indices) {
            last = (last * IA + IC) % IM
            buffer.randoms[i] = last * ONE_OVER_IM
        }
        WORKERS[OUT.incrementAndGet() % WORKERS.size]!!.put(buffer)
    }

    @Throws(IOException::class)
    private fun bufferFillRandom(
        writer: OutputStream, loops: Int
    ) {
        var buffer: AbstractBuffer?
        for (i in 0 until loops) {
            buffer = WORKERS[IN.incrementAndGet() % WORKERS.size]!!.take()
            writer.write(buffer!!.nucleotides)
            lineFillRandom(buffer as Buffer?)
        }
    }

    @Throws(IOException::class)
    private fun speciesFillRandom(
        writer: OutputStream, nChars: Int, name: String, isIUB: Boolean
    ) {
        val bufferSize = LINE_COUNT * LINE_LENGTH
        val bufferCount = nChars / bufferSize
        val bufferLoops = bufferCount - BUFFERS_IN_PLAY
        val charsLeftover = nChars - bufferCount * bufferSize
        writer.write(name.toByteArray())
        bufferFillRandom(writer, bufferLoops)
        if (charsLeftover > 0) {
            writeBuffer(writer)
            lineFillRandom(Buffer(isIUB, LINE_LENGTH, charsLeftover))
        }
    }

    @Throws(IOException::class)
    private fun writeBuffer(writer: OutputStream) {
        writer.write(
            WORKERS[IN.incrementAndGet() % WORKERS.size]
                ?.take()!!.nucleotides
        )
    }

    class NucleotideSelector : Thread() {
        private val `in`: BlockingQueue<AbstractBuffer?> = ArrayBlockingQueue(BUFFERS_IN_PLAY)
        private val out: BlockingQueue<AbstractBuffer> = ArrayBlockingQueue(BUFFERS_IN_PLAY)
        fun put(line: AbstractBuffer?) {
            try {
                `in`.put(line)
            } catch (ex: InterruptedException) {
            }
        }

        override fun run() {
            var line: AbstractBuffer
            try {
                while (true) {
                    line = `in`.take()!!
                    line.selectNucleotides()
                    out.put(line)
                }
            } catch (ex: InterruptedException) {
            }
        }

        fun take(): AbstractBuffer? {
            try {
                return out.take()
            } catch (ex: InterruptedException) {
            }
            return null
        }
    }

    abstract class AbstractBuffer(val LINE_LENGTH: Int, nChars: Int) {
        val LINE_COUNT: Int
        lateinit var chars: ByteArray
        val nucleotides: ByteArray
        val CHARS_LEFTOVER: Int
        abstract fun selectNucleotides()

        init {
            val outputLineLength = LINE_LENGTH + 1
            LINE_COUNT = nChars / LINE_LENGTH
            CHARS_LEFTOVER = nChars % LINE_LENGTH
            val nucleotidesSize = nChars + LINE_COUNT + if (CHARS_LEFTOVER == 0) 0 else 1
            val lastNucleotide = nucleotidesSize - 1
            nucleotides = ByteArray(nucleotidesSize)
            var i = LINE_LENGTH
            while (i < lastNucleotide) {
                nucleotides[i] = '\n'.toByte()
                i += outputLineLength
            }
            nucleotides[nucleotides.size - 1] = '\n'.toByte()
        }
    }

    class AluBuffer(
        lineLength: Int, val nChars: Int, offset: Int
    ) : AbstractBuffer(lineLength, nChars) {
        val ALU = ("GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGG"
                + "GAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGA"
                + "CCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAAT"
                + "ACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCA"
                + "GCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGG"
                + "AGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCC"
                + "AGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA")
        val MAX_ALU_INDEX = ALU.length - LINE_LENGTH
        val ALU_ADJUST = LINE_LENGTH - ALU.length
        var charIndex: Int
        var nucleotideIndex = 0
        override fun selectNucleotides() {
            nucleotideIndex = 0
            for (i in 0 until LINE_COUNT) {
                ALUFillLine(LINE_LENGTH)
            }
            if (CHARS_LEFTOVER > 0) {
                ALUFillLine(CHARS_LEFTOVER)
            }
            charIndex = ((charIndex + nChars * (BUFFERS_IN_PLAY - 1))
                    % ALU.length)
        }

        private fun ALUFillLine(charCount: Int) {
            System.arraycopy(
                chars, charIndex, nucleotides, nucleotideIndex, charCount
            )
            charIndex += if (charIndex < MAX_ALU_INDEX) charCount else ALU_ADJUST
            nucleotideIndex += charCount + 1
        }

        init {
            chars = (ALU + ALU.substring(0, LINE_LENGTH)).toByteArray()
            charIndex = offset % ALU.length
        }
    }

    class Buffer(
        isIUB: Boolean, lineLength: Int, nChars: Int
    ) : AbstractBuffer(lineLength, nChars) {
        val iubChars = byteArrayOf(
            'a'.toByte(), 'c'.toByte(), 'g'.toByte(), 't'.toByte(),
            'B'.toByte(), 'D'.toByte(), 'H'.toByte(), 'K'.toByte(),
            'M'.toByte(), 'N'.toByte(), 'R'.toByte(), 'S'.toByte(),
            'V'.toByte(), 'W'.toByte(), 'Y'.toByte()
        )
        val iubProbs = doubleArrayOf(
            0.27, 0.12, 0.12, 0.27,
            0.02, 0.02, 0.02, 0.02,
            0.02, 0.02, 0.02, 0.02,
            0.02, 0.02, 0.02
        )
        val sapienChars = byteArrayOf(
            'a'.toByte(),
            'c'.toByte(),
            'g'.toByte(),
            't'.toByte()
        )
        val sapienProbs = doubleArrayOf(
            0.3029549426680,
            0.1979883004921,
            0.1975473066391,
            0.3015094502008
        )
        val probs: FloatArray
        val randoms: FloatArray
        val charsInFullLines: Int
        override fun selectNucleotides() {
            var i: Int
            var j: Int
            var m: Int
            var r: Float
            var k: Int
            i = 0
            j = 0
            while (i < charsInFullLines) {
                k = 0
                while (k < LINE_LENGTH) {
                    r = randoms[i++]
                    m = 0
                    while (probs[m] < r) {
                        m++
                    }
                    nucleotides[j++] = chars[m]
                    k++
                }
                j++
            }
            k = 0
            while (k < CHARS_LEFTOVER) {
                r = randoms[i++]
                m = 0
                while (probs[m] < r) {
                    m++
                }
                nucleotides[j++] = chars[m]
                k++
            }
        }

        init {
            var cp = 0.0
            val dblProbs = if (isIUB) iubProbs else sapienProbs
            chars = if (isIUB) iubChars else sapienChars
            probs = FloatArray(dblProbs.size)
            for (i in probs.indices) {
                cp += dblProbs[i]
                probs[i] = cp.toFloat()
            }
            probs[probs.size - 1] = 2f
            randoms = FloatArray(nChars)
            charsInFullLines = nChars / lineLength * lineLength
        }
    }
}