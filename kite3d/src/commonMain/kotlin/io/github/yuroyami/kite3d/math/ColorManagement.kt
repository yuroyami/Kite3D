/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/ColorManagement.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.pow

/**
 * A color space identifier.
 *
 * In three.js color spaces are plain strings defined in `constants.js`
 * (`NoColorSpace = ''`, `SRGBColorSpace = 'srgb'`, `LinearSRGBColorSpace =
 * 'srgb-linear'`, plus Display-P3 variants). The Kotlin port models them as an
 * enum so the type system enforces valid values; only the members actually used
 * by the ported math layer are defined here.
 */
public enum class ColorSpace {
    /** No color space (three.js `''`). Treated as a linear transfer, no conversion. */
    NoColorSpace,

    /** sRGB color space (three.js `'srgb'`). */
    SRGB,

    /** sRGB-linear color space (three.js `'srgb-linear'`); the default working space. */
    LinearSRGB,

    /** Display-P3 color space (three.js `'display-p3'`). Not yet registered — reserved. */
    DisplayP3,

    /** Linear Display-P3 color space (three.js `'display-p3-linear'`). Not yet registered — reserved. */
    LinearDisplayP3,
}

/**
 * A color transfer function identifier.
 *
 * Mirrors three.js's `LinearTransfer = 'linear'` and `SRGBTransfer = 'srgb'`
 * constants from `constants.js`.
 */
public enum class ColorTransfer {
    /** Linear transfer (identity). */
    Linear,

    /** sRGB (gamma) transfer. */
    SRGB,
}

/**
 * Describes a single color space: its primaries, transfer function and the
 * RGB<->XYZ conversion matrices. Corresponds to one entry of three.js's
 * `ColorManagement.spaces` map.
 *
 * @property primaries Chromaticity coordinates `[ rx ry gx gy bx by ]`.
 * @property whitePoint Reference white `[ x y ]`.
 * @property transfer The transfer function.
 * @property toXYZ RGB-to-XYZ transform.
 * @property fromXYZ XYZ-to-RGB transform.
 * @property luminanceCoefficients RGB luminance coefficients.
 */
public class ColorSpaceDefinition(
    public val primaries: DoubleArray,
    public val whitePoint: DoubleArray,
    public val transfer: ColorTransfer,
    public val toXYZ: Matrix3,
    public val fromXYZ: Matrix3,
    public val luminanceCoefficients: DoubleArray,
)

/**
 * Manages the working color space and conversions between color spaces.
 *
 * Ported from three.js `ColorManagement` (created via `createColorManagement()`).
 * Modelled as a Kotlin `object` so that ported three.js code keeps calling
 * `ColorManagement.convert(...)` etc. verbatim. Like three.js, this holds mutable
 * global state ([enabled], [workingColorSpace]) and is **not thread-safe**.
 *
 * The renderer-facing helpers (`getToneMappingMode`, `_getDrawingBufferColorSpace`,
 * `_getUnpackColorSpace`, `define`, Display-P3 spaces) are omitted from the math
 * port because the color-space definitions here do not carry the optional
 * `outputColorSpaceConfig`/`workingColorSpaceConfig`; they belong to the (not yet
 * ported) renderer layer.
 */
public object ColorManagement {

    /** Whether color management (automatic conversions) is enabled. */
    public var enabled: Boolean = true

    /** The working color space. Colors store their components in this space. */
    public var workingColorSpace: ColorSpace = ColorSpace.LinearSRGB

    /**
     * Implementations of supported color spaces, keyed by [ColorSpace]. Mirrors
     * three.js's `ColorManagement.spaces`.
     */
    public val spaces: MutableMap<ColorSpace, ColorSpaceDefinition> = mutableMapOf()

    /**
     * Converts the given [color] from [sourceColorSpace] to [targetColorSpace],
     * mutating and returning it. Returns the color unchanged when color management
     * is disabled, the spaces match, or either space is [ColorSpace.NoColorSpace].
     *
     * @return The converted [color].
     */
    public fun convert(color: Color, sourceColorSpace: ColorSpace, targetColorSpace: ColorSpace): Color {

        if (!enabled || sourceColorSpace == targetColorSpace ||
            sourceColorSpace == ColorSpace.NoColorSpace || targetColorSpace == ColorSpace.NoColorSpace) {

            return color

        }

        val source = spaces.getValue(sourceColorSpace)
        val target = spaces.getValue(targetColorSpace)

        if (source.transfer == ColorTransfer.SRGB) {

            color.r = SRGBToLinear(color.r)
            color.g = SRGBToLinear(color.g)
            color.b = SRGBToLinear(color.b)

        }

        // three.js compares `primaries` by object identity; here the same
        // DoubleArray instance is shared by spaces with equal primaries, so
        // referential inequality still holds only when the primaries truly differ.
        if (source.primaries !== target.primaries) {

            color.applyMatrix3(source.toXYZ)
            color.applyMatrix3(target.fromXYZ)

        }

        if (target.transfer == ColorTransfer.SRGB) {

            color.r = LinearToSRGB(color.r)
            color.g = LinearToSRGB(color.g)
            color.b = LinearToSRGB(color.b)

        }

        return color

    }

    /**
     * Converts [color] from the [workingColorSpace] to [targetColorSpace].
     *
     * @return The converted [color].
     */
    public fun workingToColorSpace(color: Color, targetColorSpace: ColorSpace): Color =
        convert(color, workingColorSpace, targetColorSpace)

    /**
     * Converts [color] from [sourceColorSpace] to the [workingColorSpace].
     *
     * @return The converted [color].
     */
    public fun colorSpaceToWorking(color: Color, sourceColorSpace: ColorSpace): Color =
        convert(color, sourceColorSpace, workingColorSpace)

    /**
     * Returns the primaries (chromaticity coordinates) of the given [colorSpace].
     */
    public fun getPrimaries(colorSpace: ColorSpace): DoubleArray =
        spaces.getValue(colorSpace).primaries

    /**
     * Returns the transfer function of the given [colorSpace]. Returns
     * [ColorTransfer.Linear] for [ColorSpace.NoColorSpace].
     */
    public fun getTransfer(colorSpace: ColorSpace): ColorTransfer {

        if (colorSpace == ColorSpace.NoColorSpace) return ColorTransfer.Linear

        return spaces.getValue(colorSpace).transfer

    }

    /**
     * Writes the luminance coefficients of the given [colorSpace] into [target].
     *
     * @return The [target] vector.
     */
    public fun getLuminanceCoefficients(target: Vector3, colorSpace: ColorSpace = workingColorSpace): Vector3 =
        target.fromArray(spaces.getValue(colorSpace).luminanceCoefficients)

    /**
     * Registers (or overrides) the given [colorSpaces] into [spaces]. Corresponds
     * to three.js's `ColorManagement.define()`.
     */
    public fun define(colorSpaces: Map<ColorSpace, ColorSpaceDefinition>) {
        spaces.putAll(colorSpaces)
    }

    /**
     * Copies `spaces[sourceColorSpace].toXYZ` into [targetMatrix] and multiplies it
     * by `spaces[targetColorSpace].fromXYZ`, producing the source->target RGB
     * transform.
     *
     * @return The [targetMatrix].
     */
    public fun getMatrix(targetMatrix: Matrix3, sourceColorSpace: ColorSpace, targetColorSpace: ColorSpace): Matrix3 =
        targetMatrix
            .copy(spaces.getValue(sourceColorSpace).toXYZ)
            .multiply(spaces.getValue(targetColorSpace).fromXYZ)

    // -- deprecated three.js aliases (renamed in r177) --------------------------

    /**
     * Deprecated alias for [workingToColorSpace] (renamed in three.js r177).
     *
     * @return The converted [color].
     */
    public fun fromWorkingColorSpace(color: Color, targetColorSpace: ColorSpace): Color {
        println("THREE.ColorManagement: .fromWorkingColorSpace() has been renamed to .workingToColorSpace().")
        return workingToColorSpace(color, targetColorSpace)
    }

    /**
     * Deprecated alias for [colorSpaceToWorking] (renamed in three.js r177).
     *
     * @return The converted [color].
     */
    public fun toWorkingColorSpace(color: Color, sourceColorSpace: ColorSpace): Color {
        println("THREE.ColorManagement: .toWorkingColorSpace() has been renamed to .colorSpaceToWorking().")
        return colorSpaceToWorking(color, sourceColorSpace)
    }

    // -- sRGB definitions -------------------------------------------------------

    // Shared across LinearSRGB and SRGB spaces (same primaries => identity in
    // `convert`'s referential primaries check, matching three.js).
    private val REC709_PRIMARIES = doubleArrayOf(0.640, 0.330, 0.300, 0.600, 0.150, 0.060)
    private val REC709_LUMINANCE_COEFFICIENTS = doubleArrayOf(0.2126, 0.7152, 0.0722)
    private val D65 = doubleArrayOf(0.3127, 0.3290)

    private val LINEAR_REC709_TO_XYZ: Matrix3 = Matrix3().set(
        0.4123908, 0.3575843, 0.1804808,
        0.2126390, 0.7151687, 0.0721923,
        0.0193308, 0.1191948, 0.9505322,
    )

    private val XYZ_TO_LINEAR_REC709: Matrix3 = Matrix3().set(
        3.2409699, -1.5373832, -0.4986108,
        -0.9692436, 1.8759675, 0.0415551,
        0.0556301, -0.2039770, 1.0569715,
    )

    init {
        define(
            mapOf(
                ColorSpace.LinearSRGB to ColorSpaceDefinition(
                    primaries = REC709_PRIMARIES,
                    whitePoint = D65,
                    transfer = ColorTransfer.Linear,
                    toXYZ = LINEAR_REC709_TO_XYZ,
                    fromXYZ = XYZ_TO_LINEAR_REC709,
                    luminanceCoefficients = REC709_LUMINANCE_COEFFICIENTS,
                ),
                ColorSpace.SRGB to ColorSpaceDefinition(
                    primaries = REC709_PRIMARIES,
                    whitePoint = D65,
                    transfer = ColorTransfer.SRGB,
                    toXYZ = LINEAR_REC709_TO_XYZ,
                    fromXYZ = XYZ_TO_LINEAR_REC709,
                    luminanceCoefficients = REC709_LUMINANCE_COEFFICIENTS,
                ),
            ),
        )
    }
}

/**
 * Converts a single sRGB (gamma) channel value [c] to linear.
 *
 * @return The linear channel value.
 */
public fun SRGBToLinear(c: Double): Double =
    if (c < 0.04045) c * 0.0773993808 else (c * 0.9478672986 + 0.0521327014).pow(2.4)

/**
 * Converts a single linear channel value [c] to sRGB (gamma).
 *
 * @return The sRGB channel value.
 */
public fun LinearToSRGB(c: Double): Double =
    if (c < 0.0031308) c * 12.92 else 1.055 * (c.pow(0.41666)) - 0.055
