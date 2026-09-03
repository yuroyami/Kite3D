/*
 * Copyright © 2026 yuroyami. MIT.
 * Kite3D addition, not a three.js port: a camera that only does maths.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraTest {

    private fun near(a: Double, b: Double, tolerance: Double = 1e-6) =
        assertTrue(abs(a - b) < tolerance, "expected $b, got $a")

    /** A viewer 1600 in front of a 1000 wide plane, level with its front edge. */
    private fun stageCamera(): Camera = Camera()
        .setPerspective(-500.0, 500.0, 500.0, -500.0, 1600.0, 100_000.0)
        .lookAt(Vector3(0.0, 0.0, 1600.0), Vector3(0.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0))
        .updateMatrices()

    @Test
    fun aPointOnTheNearPlaneKeepsItsPlace() {
        val out = Vector3()
        stageCamera().project(Vector3(250.0, 0.0, 0.0), out)
        near(out.x, 0.5)
        near(out.y, 0.0)
    }

    @Test
    fun aPointFurtherAwayPullsTowardTheCentre() {
        val out = Vector3()
        stageCamera().project(Vector3(250.0, 0.0, -1600.0), out)
        // Twice the distance, half the size.
        near(out.x, 0.25)
    }

    @Test
    fun upIsUp() {
        val out = Vector3()
        stageCamera().project(Vector3(0.0, 100.0, 0.0), out)
        near(out.y, 0.2)
    }

    @Test
    fun theFloatOverloadAgreesWithTheDoubleOne() {
        val camera = stageCamera()
        val doubles = Vector3()
        val floats = FloatArray(5)
        camera.project(Vector3(123.0, 45.0, -678.0), doubles)
        camera.project(123f, 45f, -678f, floats, offset = 2)
        near(floats[2].toDouble(), doubles.x, 1e-4)
        near(floats[3].toDouble(), doubles.y, 1e-4)
        near(floats[4].toDouble(), doubles.z, 1e-4)
    }

    @Test
    fun orthographicIgnoresDepth() {
        val camera = Camera()
            .setOrthographic(-500.0, 500.0, 500.0, -500.0, 1.0, 10_000.0)
            .lookAt(Vector3(0.0, 0.0, 1600.0), Vector3(0.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0))
            .updateMatrices()
        val nearOut = Vector3()
        val farOut = Vector3()
        camera.project(Vector3(250.0, 0.0, 0.0), nearOut)
        camera.project(Vector3(250.0, 0.0, -3000.0), farOut)
        near(nearOut.x, farOut.x)
    }

    @Test
    fun theFieldOfViewFormMatchesTheEdgeForm() {
        // 90 degrees at near 1 is edges of plus and minus 1.
        val byFov = Camera().setPerspectiveFov(90.0, 1.0, 1.0, 100.0)
        val byEdges = Camera().setPerspective(-1.0, 1.0, 1.0, -1.0, 1.0, 100.0)
        for (i in 0 until 16) near(byFov.projectionMatrix.elements[i], byEdges.projectionMatrix.elements[i], 1e-9)
    }

    @Test
    fun updateMatricesIsRepeatable() {
        val camera = stageCamera()
        val first = camera.viewProjectionMatrix.clone()
        camera.updateMatrices()
        assertEquals(first.elements.toList(), camera.viewProjectionMatrix.elements.toList())
    }
}
