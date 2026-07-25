/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/ColorManagement.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColorManagementTest {

    // PROPERTIES

    @Test
    fun enabled() {
        // Note: this reads a mutable global (Color tests toggle it); the default is
        // true, and Color tests restore it, so this holds when run in isolation.
        assertTrue(ColorManagement.enabled, "ColorManagement.enabled is true by default.")
    }

    // The remaining assertions below have no direct upstream counterpart (the
    // upstream test file only checks `enabled`); they lock the ported transfer
    // functions and working-space defaults.

    @Test
    fun workingColorSpaceDefault() {
        assertEquals(ColorSpace.LinearSRGB, ColorManagement.workingColorSpace, "workingColorSpace defaults to LinearSRGB.")
    }

    @Test
    fun sRGBTransferRoundTrip() {
        // LinearToSRGB then SRGBToLinear should round-trip (transcendental → tolerance).
        for (v in listOf(0.0, 0.001, 0.05, 0.25, 0.5, 0.73, 0.9, 1.0)) {
            val roundTrip = SRGBToLinear(LinearToSRGB(v))
            // three.js uses the truncated exponent 0.41666 (not 1/2.4) in LinearToSRGB, so
            // the transfer pair is not an exact inverse. The round-trip error is ~2e-6.
            assertTrue(abs(roundTrip - v) <= 1e-4, "sRGB round-trip for $v (got $roundTrip)")
        }
    }

    @Test
    fun sRGBToLinearKnownValues() {
        // 0.5 sRGB ≈ 0.21404 linear (transcendental → tolerance).
        assertTrue(abs(SRGBToLinear(0.5) - 0.21404114) <= 1e-6, "SRGBToLinear(0.5)")
        assertEquals(0.0, SRGBToLinear(0.0), 0.0, "SRGBToLinear(0.0)")
        assertTrue(abs(SRGBToLinear(1.0) - 1.0) <= 1e-6, "SRGBToLinear(1.0)")
    }

    @Test
    fun getTransferNoColorSpaceIsLinear() {
        assertEquals(ColorTransfer.Linear, ColorManagement.getTransfer(ColorSpace.NoColorSpace), "NoColorSpace → Linear transfer")
        assertEquals(ColorTransfer.Linear, ColorManagement.getTransfer(ColorSpace.LinearSRGB), "LinearSRGB → Linear transfer")
        assertEquals(ColorTransfer.SRGB, ColorManagement.getTransfer(ColorSpace.SRGB), "SRGB → sRGB transfer")
    }

    @Test
    fun convertNoOpCases() {
        // Same source/target, or NoColorSpace, leaves the color unchanged.
        val c = Color().setRGB(0.3, 0.5, 0.7, ColorSpace.LinearSRGB)

        ColorManagement.convert(c, ColorSpace.LinearSRGB, ColorSpace.LinearSRGB)
        assertEquals(0.3, c.r, 0.0, "identity convert r")
        assertEquals(0.5, c.g, 0.0, "identity convert g")
        assertEquals(0.7, c.b, 0.0, "identity convert b")

        ColorManagement.convert(c, ColorSpace.NoColorSpace, ColorSpace.SRGB)
        assertEquals(0.3, c.r, 0.0, "NoColorSpace source is a no-op r")
    }

    @Test
    fun convertLinearToSRGB() {
        // Same primaries (Rec709), so convert only applies the sRGB transfer.
        val c = Color().setRGB(0.3, 0.5, 0.7, ColorSpace.LinearSRGB)
        ColorManagement.convert(c, ColorSpace.LinearSRGB, ColorSpace.SRGB)

        assertTrue(abs(c.r - LinearToSRGB(0.3)) <= 1e-12, "convert applies LinearToSRGB on r")
        assertTrue(abs(c.g - LinearToSRGB(0.5)) <= 1e-12, "convert applies LinearToSRGB on g")
        assertTrue(abs(c.b - LinearToSRGB(0.7)) <= 1e-12, "convert applies LinearToSRGB on b")
    }
}
