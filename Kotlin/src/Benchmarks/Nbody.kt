package Benchmarks

object Nbody {
    @JvmStatic
    fun runBenchmark(repeatTimes: Int = 10000, output: Boolean = false) {
        val bodies = NBodySystem()
        if(output)
            System.out.printf("%.9f\n", bodies.energy())
        for (i in 0 until repeatTimes) bodies.advance(0.01)
        if(output)
            System.out.printf("%.9f\n", bodies.energy())
    }
}

private class NBodySystem {
    private val bodies: Array<Body>
    fun advance(dt: Double) {
        val b = bodies
        for (i in 0 until LENGTH - 1) {
            val iBody = b[i]
            val iMass = iBody.mass
            val ix = iBody.x
            val iy = iBody.y
            val iz = iBody.z
            for (j in i + 1 until LENGTH) {
                val jBody = b[j]
                val dx = ix - jBody.x
                val dy = iy - jBody.y
                val dz = iz - jBody.z
                val dSquared = dx * dx + dy * dy + dz * dz
                val distance = Math.sqrt(dSquared)
                val mag = dt / (dSquared * distance)
                val jMass = jBody.mass
                iBody.vx -= dx * jMass * mag
                iBody.vy -= dy * jMass * mag
                iBody.vz -= dz * jMass * mag
                jBody.vx += dx * iMass * mag
                jBody.vy += dy * iMass * mag
                jBody.vz += dz * iMass * mag
            }
        }
        for (i in 0 until LENGTH) {
            val body = b[i]
            body.x += dt * body.vx
            body.y += dt * body.vy
            body.z += dt * body.vz
        }
    }

    fun energy(): Double {
        var dx: Double
        var dy: Double
        var dz: Double
        var distance: Double
        var e = 0.0
        for (i in bodies.indices) {
            val iBody = bodies[i]
            e += 0.5 * iBody.mass *
                    (iBody.vx * iBody.vx + iBody.vy * iBody.vy + iBody.vz * iBody.vz)
            for (j in i + 1 until bodies.size) {
                val jBody = bodies[j]
                dx = iBody.x - jBody.x
                dy = iBody.y - jBody.y
                dz = iBody.z - jBody.z
                distance = Math.sqrt(dx * dx + dy * dy + dz * dz)
                e -= iBody.mass * jBody.mass / distance
            }
        }
        return e
    }

    companion object {
        private const val LENGTH = 5
    }

    init {
        bodies = arrayOf(
            Body.Companion.sun(),
            Body.Companion.jupiter(),
            Body.Companion.saturn(),
            Body.Companion.uranus(),
            Body.Companion.neptune()
        )
        var px = 0.0
        var py = 0.0
        var pz = 0.0
        for (i in 0 until LENGTH) {
            px += bodies[i].vx * bodies[i].mass
            py += bodies[i].vy * bodies[i].mass
            pz += bodies[i].vz * bodies[i].mass
        }
        bodies[0].offsetMomentum(px, py, pz)
    }
}

private class Body {
    var x = 0.0
    var y = 0.0
    var z = 0.0
    var vx = 0.0
    var vy = 0.0
    var vz = 0.0
    var mass = 0.0
    fun offsetMomentum(px: Double, py: Double, pz: Double): Body {
        vx = -px / Body.Companion.SOLAR_MASS
        vy = -py / Body.Companion.SOLAR_MASS
        vz = -pz / Body.Companion.SOLAR_MASS
        return this
    }

    companion object {
        const val PI = 3.141592653589793
        const val SOLAR_MASS = 4 * Body.Companion.PI * Body.Companion.PI
        const val DAYS_PER_YEAR = 365.24
        fun jupiter(): Body {
            val p = Body()
            p.x = 4.84143144246472090e+00
            p.y = -1.16032004402742839e+00
            p.z = -1.03622044471123109e-01
            p.vx = 1.66007664274403694e-03 * Body.Companion.DAYS_PER_YEAR
            p.vy = 7.69901118419740425e-03 * Body.Companion.DAYS_PER_YEAR
            p.vz = -6.90460016972063023e-05 * Body.Companion.DAYS_PER_YEAR
            p.mass = 9.54791938424326609e-04 * Body.Companion.SOLAR_MASS
            return p
        }

        fun saturn(): Body {
            val p = Body()
            p.x = 8.34336671824457987e+00
            p.y = 4.12479856412430479e+00
            p.z = -4.03523417114321381e-01
            p.vx = -2.76742510726862411e-03 * Body.Companion.DAYS_PER_YEAR
            p.vy = 4.99852801234917238e-03 * Body.Companion.DAYS_PER_YEAR
            p.vz = 2.30417297573763929e-05 * Body.Companion.DAYS_PER_YEAR
            p.mass = 2.85885980666130812e-04 * Body.Companion.SOLAR_MASS
            return p
        }

        fun uranus(): Body {
            val p = Body()
            p.x = 1.28943695621391310e+01
            p.y = -1.51111514016986312e+01
            p.z = -2.23307578892655734e-01
            p.vx = 2.96460137564761618e-03 * Body.Companion.DAYS_PER_YEAR
            p.vy = 2.37847173959480950e-03 * Body.Companion.DAYS_PER_YEAR
            p.vz = -2.96589568540237556e-05 * Body.Companion.DAYS_PER_YEAR
            p.mass = 4.36624404335156298e-05 * Body.Companion.SOLAR_MASS
            return p
        }

        fun neptune(): Body {
            val p = Body()
            p.x = 1.53796971148509165e+01
            p.y = -2.59193146099879641e+01
            p.z = 1.79258772950371181e-01
            p.vx = 2.68067772490389322e-03 * Body.Companion.DAYS_PER_YEAR
            p.vy = 1.62824170038242295e-03 * Body.Companion.DAYS_PER_YEAR
            p.vz = -9.51592254519715870e-05 * Body.Companion.DAYS_PER_YEAR
            p.mass = 5.15138902046611451e-05 * Body.Companion.SOLAR_MASS
            return p
        }

        fun sun(): Body {
            val p = Body()
            p.mass = Body.Companion.SOLAR_MASS
            return p
        }
    }
}