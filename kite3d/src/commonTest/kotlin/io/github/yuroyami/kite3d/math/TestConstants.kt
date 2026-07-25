/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/utils/math-constants.js (MIT).
 *
 * Shared constants for the math unit tests. Mirrors three.js's math-constants.js.
 *
 * Unlike three.js (whose constants are shared mutable singletons that tests must
 * clone before mutating), each vector constant here is a `get()` property that
 * returns a fresh instance on every access, so tests can mutate them freely
 * without affecting each other. Do NOT convert these to `val` singletons.
 */
package io.github.yuroyami.kite3d.math

const val x = 2.0
const val y = 3.0
const val z = 4.0
const val w = 5.0

const val eps = 0.0001

val negInf2 get() = Vector2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
val posInf2 get() = Vector2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
val negOne2 get() = Vector2(-1.0, -1.0)
val zero2 get() = Vector2()
val one2 get() = Vector2(1.0, 1.0)
val two2 get() = Vector2(2.0, 2.0)

val negInf3 get() = Vector3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
val posInf3 get() = Vector3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
val zero3 get() = Vector3()
val one3 get() = Vector3(1.0, 1.0, 1.0)
val two3 get() = Vector3(2.0, 2.0, 2.0)
