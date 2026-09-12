/*
 * Copyright © 2026 yuroyami. MIT.
 * The maths methods keep no shared scratch state, so threads can use separate instances
 * at the same time. If shared state comes back, threads corrupt each other and this
 * fails. JVM only, because common code cannot start threads.
 */
package io.github.yuroyami.kite3d.math

import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

class ConcurrencyTest {

    // Runs Box2.setFromCenterAndSize, Box2.distanceToPoint, Matrix4.compose and
    // Quaternion.setFromEuler. three.js runs the two Box2 methods with a shared scratch
    // vector; here every call uses only locals and its own instances.
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
                "worker $i diverged under contention, so a shared mutable temp was reintroduced",
            )
        }
    }
}
