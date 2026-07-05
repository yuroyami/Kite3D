/*
 * Copyright © 2026 yuroyami — MIT.
 * JVM-only regression guard for audit §3.1: the port removed all file-level mutable
 * scratch, so math methods that three.js backed with shared module temps must now be
 * safe to run concurrently on distinct instances. If shared mutable state is ever
 * reintroduced, threads corrupt each other and this diverges from the single-thread
 * baseline. (Common code can't portably spawn threads, so this lives in jvmTest.)
 */
package io.github.yuroyami.kite3d.math

import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

class ConcurrencyTest {

    // Exercises the exact methods three.js backed with module-level temps:
    // Box2.setFromCenterAndSize / distanceToPoint, Matrix4.compose (position/quat/scale
    // temps), Quaternion.setFromEuler. Each call uses only locals + its own instances.
    private fun compute(seed: Int): Double {
        val s = seed.toDouble()
        val box = Box2().setFromCenterAndSize(Vector2(s, s), Vector2(2.0, 4.0))
        val d = box.distanceToPoint(Vector2(s + 10.0, s - 10.0))
        val m = Matrix4().compose(
            Vector3(s, 0.0, 0.0),
            Quaternion().setFromEuler(Euler(0.1 * s, 0.2, 0.3)),
            Vector3(1.0, 2.0, 3.0),
        )
        return d + m.elements.sum()
    }

    @Test
    fun mathMethodsHaveNoSharedMutableState() {
        val workers = 32
        val baseline = DoubleArray(workers) { compute(it) }

        val results = arrayOfNulls<Double>(workers)
        (0 until workers).map { i ->
            thread {
                var last = 0.0
                repeat(2000) { last = compute(i) }
                results[i] = last
            }
        }.forEach { it.join() }

        for (i in 0 until workers) {
            assertEquals(
                baseline[i],
                results[i]!!,
                0.0,
                "worker $i diverged under contention — a shared mutable temp was reintroduced",
            )
        }
    }
}
