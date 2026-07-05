/*
 * Copyright © 2026 yuroyami — MIT.
 * Kite3D-only invariants that three.js does not test but the Kotlin port must
 * hold (audit §7.1). These guard the decisions that diverge from three.js:
 * the clamp-not-coerceIn empty-box behavior, and the equals/hashCode contract.
 */
package io.github.yuroyami.kite3d.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MathContractTest {

    // --- empty-box clamp canary (audit §3.5 / §6.1) --------------------------------

    @Test
    fun emptyBox2DistanceToPointIsInfinite() {
        // Box2() is empty: min = +inf, max = -inf. clamp() must be max(lo, min(hi, v)),
        // never coerceIn (which throws when lo > hi). The distance therefore diverges.
        val d = Box2().distanceToPoint(Vector2(1.0, 2.0))
        assertEquals(Double.POSITIVE_INFINITY, d, "empty box distance must be +Infinity, not a throw")
    }

    // --- equals/hashCode contract (audit §3.2) -------------------------------------

    @Test
    fun negativeZeroHashesConsistentlyWithEquals() {
        // -0.0 == +0.0 (primitive Double), so their hashCodes must agree too.
        assertTrue(Vector2(0.0, 0.0) == Vector2(-0.0, -0.0), "-0.0 equals +0.0 component-wise")
        assertEquals(
            Vector2(0.0, 0.0).hashCode(),
            Vector2(-0.0, -0.0).hashCode(),
            "hashCode must normalize -0.0",
        )
        assertEquals(Vector3(0.0, -0.0, 0.0).hashCode(), Vector3(-0.0, 0.0, -0.0).hashCode())
    }

    @Test
    fun nanIsNotEqualToItself() {
        // Matches JS === semantics: NaN != NaN, so a NaN-bearing vector never equals another.
        assertFalse(Vector2(Double.NaN, 1.0) == Vector2(Double.NaN, 1.0))
        assertFalse(Vector3(1.0, Double.NaN, 3.0) == Vector3(1.0, Double.NaN, 3.0))
    }

    @Test
    fun equalsOperatorAgreesWithEqualsMethod() {
        // The whole reason equals(Any?) is overridden instead of a bare equals(Vector2)
        // overload: `==` and `.equals(...)` must never disagree (audit §3.2).
        val a = Vector2(1.0, 2.0)
        val b = Vector2(1.0, 2.0)
        val c = Vector2(1.0, 9.0)
        assertEquals(a == b, a.equals(b))
        assertEquals(a == c, a.equals(c))
        assertTrue(a == b)
        assertFalse(a == c)
    }

    @Test
    fun equalValuesShareHashCode() {
        // The core hashCode invariant: equal objects must hash equally.
        assertEquals(Vector2(3.0, 4.0).hashCode(), Vector2(3.0, 4.0).hashCode())
        assertEquals(Vector3(3.0, 4.0, 5.0).hashCode(), Vector3(3.0, 4.0, 5.0).hashCode())
        assertEquals(Box2(Vector2(0.0, 0.0), Vector2(1.0, 1.0)).hashCode(), Box2(Vector2(0.0, 0.0), Vector2(1.0, 1.0)).hashCode())
        assertEquals(Matrix3().hashCode(), Matrix3().hashCode())
        assertTrue(Matrix3() == Matrix3())
    }

    @Test
    fun boxEqualsIsStructuralAndTypeChecked() {
        val a = Box2(Vector2(0.0, 0.0), Vector2(1.0, 1.0))
        val b = Box2(Vector2(0.0, 0.0), Vector2(1.0, 1.0))
        assertTrue(a == b)
        assertFalse(a.equals(Vector2(0.0, 0.0)))
    }
}
