/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Color.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.floor

/**
 * The result of [Color.getHSL]: hue, saturation and lightness, each in `[0, 1]`.
 * Mirrors the `{ h, s, l }` target object three.js writes into.
 */
public class HSL(
    public var h: Double = 0.0,
    public var s: Double = 0.0,
    public var l: Double = 0.0,
)

// hue2rgb helper (three.js module-level function).
private fun hue2rgb(p: Double, q: Double, t0: Double): Double {
    var t = t0
    if (t < 0) t += 1
    if (t > 1) t -= 1
    if (t < 1.0 / 6.0) return p + (q - p) * 6 * t
    if (t < 1.0 / 2.0) return q
    if (t < 2.0 / 3.0) return p + (q - p) * 6 * (2.0 / 3.0 - t)
    return p
}

/**
 * A color represented by RGB components in the linear *working color space*,
 * which defaults to [ColorSpace.LinearSRGB]. Inputs conventionally using
 * [ColorSpace.SRGB] (such as hexadecimals) are converted to the working color
 * space automatically. If [ColorManagement.enabled] is `false`, no conversions
 * occur.
 *
 * Colors are **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js. Most
 * methods mutate `this` and return it for chaining.
 *
 * Iterating a color yields its components `(r, g, b)` in order.
 *
 * ## Deferred CSS-string / named-color paths
 *
 * three.js's `setStyle` (CSS-string parsing: `rgb()`, `hsl()`, `#hex`), the X11
 * `setColorName` lookup with its 140-entry `_colorKeywords` table, `Color.NAMES`,
 * and `getStyle` (CSS-string output) are **deferred**: they require a large color
 * name table and regex-based CSS parsing that add no value to the pure math layer
 * yet. The numeric constructors ([Color] with `r,g,b` or a hex `Int`), [setHex],
 * [setRGB] and [setHSL] cover the algebraic paths. See port-ledger.yaml.
 */
public class Color : Iterable<Double> {

    /** The red component. */
    public var r: Double = 1.0

    /** The green component. */
    public var g: Double = 1.0

    /** The blue component. */
    public var b: Double = 1.0

    /**
     * Constructs a white color (`r = g = b = 1`).
     *
     * (three.js's polymorphic `Color(r, g, b)` constructor also accepts a hex
     * `Int`, a CSS string or another [Color]. The Kotlin port splits those into
     * distinct constructors/overloads: [Color] with three [Double]s, with a hex
     * [Int], or via [copy]. The CSS-string path is deferred — see the class doc.)
     */
    public constructor()

    /**
     * Constructs a color from the RGB components [r], [g], [b] (via [setRGB], i.e.
     * interpreted in the working color space).
     */
    public constructor(r: Double, g: Double, b: Double) {
        setRGB(r, g, b)
    }

    /**
     * Constructs a color from the hexadecimal value [hex] (via [setHex], i.e.
     * interpreted as [ColorSpace.SRGB] by default).
     */
    public constructor(hex: Int) {
        setHex(hex)
    }

    /**
     * Constructs a color as a copy of [color].
     */
    public constructor(color: Color) {
        copy(color)
    }

    /**
     * Sets this color from the RGB components [r], [g], [b] (in the working color
     * space). Kotlin equivalent of three.js's numeric `set(r, g, b)` path.
     *
     * @return A reference to this color.
     */
    public fun set(r: Double, g: Double, b: Double): Color = setRGB(r, g, b)

    /**
     * Sets this color from the hexadecimal value [hex] (interpreted in
     * [colorSpace]). Kotlin equivalent of three.js's `set(number)` path.
     *
     * @return A reference to this color.
     */
    public fun set(hex: Int): Color = setHex(hex)

    /**
     * Copies the components of [color] into this instance. Kotlin equivalent of
     * three.js's `set(Color)` path.
     *
     * @return A reference to this color.
     */
    public fun set(color: Color): Color = copy(color)

    /**
     * Sets all components to the scalar value [scalar].
     *
     * @return A reference to this color.
     */
    public fun setScalar(scalar: Double): Color {
        r = scalar
        g = scalar
        b = scalar
        return this
    }

    /**
     * Sets this color from a hexadecimal value.
     *
     * @param hex The hexadecimal value.
     * @param colorSpace The color space of [hex] (defaults to [ColorSpace.SRGB]).
     * @return A reference to this color.
     */
    public fun setHex(hex: Int, colorSpace: ColorSpace = ColorSpace.SRGB): Color {
        // three.js does `Math.floor(hex)`; hex is already an Int here.
        r = (hex shr 16 and 255) / 255.0
        g = (hex shr 8 and 255) / 255.0
        b = (hex and 255) / 255.0

        ColorManagement.colorSpaceToWorking(this, colorSpace)

        return this
    }

    /**
     * Sets this color from RGB values.
     *
     * @param r Red channel value between `0.0` and `1.0`.
     * @param g Green channel value between `0.0` and `1.0`.
     * @param b Blue channel value between `0.0` and `1.0`.
     * @param colorSpace The color space of the inputs (defaults to
     * [ColorManagement.workingColorSpace]).
     * @return A reference to this color.
     */
    public fun setRGB(r: Double, g: Double, b: Double, colorSpace: ColorSpace = ColorManagement.workingColorSpace): Color {
        this.r = r
        this.g = g
        this.b = b

        ColorManagement.colorSpaceToWorking(this, colorSpace)

        return this
    }

    /**
     * Sets this color from HSL values.
     *
     * @param h Hue value between `0.0` and `1.0`.
     * @param s Saturation value between `0.0` and `1.0`.
     * @param l Lightness value between `0.0` and `1.0`.
     * @param colorSpace The color space of the inputs (defaults to
     * [ColorManagement.workingColorSpace]).
     * @return A reference to this color.
     */
    public fun setHSL(h: Double, s: Double, l: Double, colorSpace: ColorSpace = ColorManagement.workingColorSpace): Color {
        // h,s,l ranges are in 0.0 - 1.0
        val hue = MathUtils.euclideanModulo(h, 1.0)
        val sat = MathUtils.clamp(s, 0.0, 1.0)
        val light = MathUtils.clamp(l, 0.0, 1.0)

        if (sat == 0.0) {
            r = light
            g = light
            b = light
        } else {
            val p = if (light <= 0.5) light * (1 + sat) else light + sat - (light * sat)
            val q = (2 * light) - p

            r = hue2rgb(q, p, hue + 1.0 / 3.0)
            g = hue2rgb(q, p, hue)
            b = hue2rgb(q, p, hue - 1.0 / 3.0)
        }

        ColorManagement.colorSpaceToWorking(this, colorSpace)

        return this
    }

    // setStyle(style, colorSpace): CSS-string parsing (rgb()/hsl()/#hex) deferred
    // (needs regex parsing + falls through to setColorName). See class doc / port-ledger.yaml.

    // setColorName(style, colorSpace): X11 named-color lookup deferred (needs the
    // 140-entry _colorKeywords table). See class doc / port-ledger.yaml.

    /**
     * Returns a new color with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Color = Color(r, g, b)

    /**
     * Copies the values of [color] into this instance.
     *
     * @return A reference to this color.
     */
    public fun copy(color: Color): Color {
        r = color.r
        g = color.g
        b = color.b

        return this
    }

    /**
     * Copies [color] into this color, then converts from [ColorSpace.SRGB] to
     * [ColorSpace.LinearSRGB].
     *
     * @return A reference to this color.
     */
    public fun copySRGBToLinear(color: Color): Color {
        r = SRGBToLinear(color.r)
        g = SRGBToLinear(color.g)
        b = SRGBToLinear(color.b)

        return this
    }

    /**
     * Copies [color] into this color, then converts from [ColorSpace.LinearSRGB] to
     * [ColorSpace.SRGB].
     *
     * @return A reference to this color.
     */
    public fun copyLinearToSRGB(color: Color): Color {
        r = LinearToSRGB(color.r)
        g = LinearToSRGB(color.g)
        b = LinearToSRGB(color.b)

        return this
    }

    /**
     * Converts this color from [ColorSpace.SRGB] to [ColorSpace.LinearSRGB].
     *
     * @return A reference to this color.
     */
    public fun convertSRGBToLinear(): Color {
        copySRGBToLinear(this)

        return this
    }

    /**
     * Converts this color from [ColorSpace.LinearSRGB] to [ColorSpace.SRGB].
     *
     * @return A reference to this color.
     */
    public fun convertLinearToSRGB(): Color {
        copyLinearToSRGB(this)

        return this
    }

    /**
     * Returns the hexadecimal value of this color (in [colorSpace], default
     * [ColorSpace.SRGB]).
     *
     * @return The hexadecimal value.
     */
    public fun getHex(colorSpace: ColorSpace = ColorSpace.SRGB): Int {
        // three.js uses a module-level `_color` scratch; a local matches the
        // dialect (no file-level mutable scratch) at no fidelity cost.
        val c = Color().copy(this)
        ColorManagement.workingToColorSpace(c, colorSpace)

        // three.js: Math.round(...) — half toward +inf (MathUtils.jsRound), not
        // Kotlin's half-to-even round. `* 65536`/`* 256` widened via Int arithmetic.
        return MathUtils.jsRound(MathUtils.clamp(c.r * 255, 0.0, 255.0)).toInt() * 65536 +
            MathUtils.jsRound(MathUtils.clamp(c.g * 255, 0.0, 255.0)).toInt() * 256 +
            MathUtils.jsRound(MathUtils.clamp(c.b * 255, 0.0, 255.0)).toInt()
    }

    /**
     * Returns the hexadecimal value of this color as a 6-digit lowercase string
     * (for example, `"ffffff"`).
     *
     * @return The hexadecimal value as a string.
     */
    public fun getHexString(colorSpace: ColorSpace = ColorSpace.SRGB): String =
        ("000000" + getHex(colorSpace).toString(16)).takeLast(6)

    /**
     * Converts this color's RGB values into HSL and stores them into [target].
     *
     * @param target The object that stores the method's result.
     * @param colorSpace The color space (defaults to [ColorManagement.workingColorSpace]).
     * @return The [target] HSL representation.
     */
    public fun getHSL(target: HSL, colorSpace: ColorSpace = ColorManagement.workingColorSpace): HSL {
        // h,s,l ranges are in 0.0 - 1.0

        // three.js uses a module-level `_color` scratch; a local matches the dialect.
        val c = Color().copy(this)
        ColorManagement.workingToColorSpace(c, colorSpace)

        val r = c.r
        val g = c.g
        val b = c.b

        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)

        var hue = 0.0
        var saturation = 0.0
        val lightness = (min + max) / 2.0

        if (min == max) {
            hue = 0.0
            saturation = 0.0
        } else {
            val delta = max - min

            saturation = if (lightness <= 0.5) delta / (max + min) else delta / (2 - max - min)

            hue = when (max) {
                r -> (g - b) / delta + (if (g < b) 6 else 0)
                g -> (b - r) / delta + 2
                else -> (r - g) / delta + 4 // b
            }

            hue /= 6
        }

        target.h = hue
        target.s = saturation
        target.l = lightness

        return target
    }

    /**
     * Returns the RGB values of this color and stores them into [target].
     *
     * @param target The color that stores the method's result.
     * @param colorSpace The color space (defaults to [ColorManagement.workingColorSpace]).
     * @return The [target] color.
     */
    public fun getRGB(target: Color, colorSpace: ColorSpace = ColorManagement.workingColorSpace): Color {
        // three.js uses a module-level `_color` scratch; a local matches the dialect.
        val c = Color().copy(this)
        ColorManagement.workingToColorSpace(c, colorSpace)

        target.r = c.r
        target.g = c.g
        target.b = c.b

        return target
    }

    // getStyle(colorSpace): CSS-string output deferred (needs CSS Color Module
    // Level 4 formatting for non-sRGB spaces). See class doc / port-ledger.yaml.

    /**
     * Adds the given HSL values to this color's values (via a round-trip through
     * HSL).
     *
     * @return A reference to this color.
     */
    public fun offsetHSL(h: Double, s: Double, l: Double): Color {
        // three.js uses a module-level `_hslA` scratch; a local matches the dialect.
        val hsl = HSL()
        getHSL(hsl)

        return setHSL(hsl.h + h, hsl.s + s, hsl.l + l)
    }

    /**
     * Adds the RGB values of [color] to this color.
     *
     * @return A reference to this color.
     */
    public fun add(color: Color): Color {
        r += color.r
        g += color.g
        b += color.b

        return this
    }

    /**
     * Adds the RGB values of [color1] and [color2] and stores the result here.
     *
     * @return A reference to this color.
     */
    public fun addColors(color1: Color, color2: Color): Color {
        r = color1.r + color2.r
        g = color1.g + color2.g
        b = color1.b + color2.b

        return this
    }

    /**
     * Adds the scalar value [s] to this color's RGB values.
     *
     * @return A reference to this color.
     */
    public fun addScalar(s: Double): Color {
        r += s
        g += s
        b += s

        return this
    }

    /**
     * Subtracts the RGB values of [color] from this color, clamping each component
     * at `0`.
     *
     * @return A reference to this color.
     */
    public fun sub(color: Color): Color {
        r = maxOf(0.0, r - color.r)
        g = maxOf(0.0, g - color.g)
        b = maxOf(0.0, b - color.b)

        return this
    }

    /**
     * Multiplies this color's RGB values by [color]'s (component-wise).
     *
     * @return A reference to this color.
     */
    public fun multiply(color: Color): Color {
        r *= color.r
        g *= color.g
        b *= color.b

        return this
    }

    /**
     * Multiplies this color's RGB values by the scalar [s].
     *
     * @return A reference to this color.
     */
    public fun multiplyScalar(s: Double): Color {
        r *= s
        g *= s
        b *= s

        return this
    }

    /**
     * Linearly interpolates this color's RGB values toward [color] by [alpha]
     * (`0.0` is this color, `1.0` is [color]).
     *
     * @return A reference to this color.
     */
    public fun lerp(color: Color, alpha: Double): Color {
        r += (color.r - r) * alpha
        g += (color.g - g) * alpha
        b += (color.b - b) * alpha

        return this
    }

    /**
     * Sets this color to the linear interpolation of [color1] and [color2] by
     * [alpha] (`0.0` is [color1], `1.0` is [color2]).
     *
     * @return A reference to this color.
     */
    public fun lerpColors(color1: Color, color2: Color, alpha: Double): Color {
        r = color1.r + (color2.r - color1.r) * alpha
        g = color1.g + (color2.g - color1.g) * alpha
        b = color1.b + (color2.b - color1.b) * alpha

        return this
    }

    /**
     * Linearly interpolates this color's HSL values toward [color]'s HSL values by
     * [alpha], going through all the intermediate hues.
     *
     * @return A reference to this color.
     */
    public fun lerpHSL(color: Color, alpha: Double): Color {
        // three.js uses module-level `_hslA`/`_hslB` scratch; locals match the dialect.
        val hslA = HSL()
        val hslB = HSL()
        getHSL(hslA)
        color.getHSL(hslB)

        val h = MathUtils.lerp(hslA.h, hslB.h, alpha)
        val s = MathUtils.lerp(hslA.s, hslB.s, alpha)
        val l = MathUtils.lerp(hslA.l, hslB.l, alpha)

        setHSL(h, s, l)

        return this
    }

    /**
     * Sets this color's RGB components from the 3D vector [v] (`r = x`, `g = y`,
     * `b = z`).
     *
     * @return A reference to this color.
     */
    public fun setFromVector3(v: Vector3): Color {
        r = v.x
        g = v.y
        b = v.z

        return this
    }

    /**
     * Transforms this color with the 3x3 matrix [m].
     *
     * @return A reference to this color.
     */
    public fun applyMatrix3(m: Matrix3): Color {
        val r = this.r
        val g = this.g
        val b = this.b
        val e = m.elements

        this.r = e[0] * r + e[3] * g + e[6] * b
        this.g = e[1] * r + e[4] * g + e[7] * b
        this.b = e[2] * r + e[5] * g + e[8] * b

        return this
    }

    /**
     * Sets this color's RGB components from [array] at [offset].
     *
     * @return A reference to this color.
     */
    public fun fromArray(array: DoubleArray, offset: Int = 0): Color {
        r = array[offset]
        g = array[offset + 1]
        b = array[offset + 2]

        return this
    }

    /**
     * Writes this color's RGB components into [array] at [offset], growing the list
     * as needed, and returns it.
     */
    public fun toArray(array: MutableList<Double> = mutableListOf(), offset: Int = 0): MutableList<Double> {
        while (array.size < offset + 3) array.add(0.0)
        array[offset] = r
        array[offset + 1] = g
        array[offset + 2] = b

        return array
    }

    /**
     * Sets this color's components from [attribute] at [index].
     *
     * @return A reference to this color.
     */
    public fun fromBufferAttribute(attribute: AttributeLike, index: Int): Color {
        r = attribute.getX(index)
        g = attribute.getY(index)
        b = attribute.getZ(index)

        return this
    }

    /**
     * Returns this color's serialization result: its hexadecimal value. Kotlin
     * equivalent of three.js's `toJSON()`.
     *
     * @return The hexadecimal value.
     */
    public fun toJSON(): Int = getHex()

    override fun iterator(): Iterator<Double> = listOf(r, g, b).iterator()

    /**
     * Structural equality: `true` when [other] is a [Color] with equal components
     * (component-wise `==`, so `NaN != NaN`).
     */
    override fun equals(other: Any?): Boolean =
        other is Color && other.r == r && other.g == g && other.b == b

    override fun hashCode(): Int {
        // (v + 0.0) collapses -0.0 to +0.0, keeping hashCode consistent with
        // equals treating -0.0 == +0.0.
        var result = (r + 0.0).hashCode()
        result = 31 * result + (g + 0.0).hashCode()
        result = 31 * result + (b + 0.0).hashCode()
        return result
    }

    override fun toString(): String = "Color(r=$r, g=$g, b=$b)"
}
