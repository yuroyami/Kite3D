/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Box2.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.sqrt

/**
 * An axis-aligned bounding box (AABB) in 2D space.
 *
 * The box is **mutable** and **not thread-safe**; confine an instance to a single
 * thread, as in three.js.
 *
 * The constructor stores the given vectors **by reference** (it does not copy
 * them). Passing the same vector as both bounds — or sharing a vector with another
 * object — means later mutations are visible through every alias; use [set] or
 * [clone] when you need independent storage.
 */
public class Box2(
    /** The lower (min) boundary of the box. */
    public val min: Vector2 = Vector2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
    /** The upper (max) boundary of the box. */
    public val max: Vector2 = Vector2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY),
) {

    /**
     * Copies the values of [min] and [max] into this box's bounds.
     *
     * @return A reference to this box.
     */
    public fun set(min: Vector2, max: Vector2): Box2 {
        this.min.copy(min)
        this.max.copy(max)
        return this
    }

    /**
     * Expands this box to enclose all the given [points]. An empty sequence leaves
     * the box empty.
     *
     * @return A reference to this box.
     */
    public fun setFromPoints(points: Iterable<Vector2>): Box2 {
        makeEmpty()
        for (point in points) expandByPoint(point)
        return this
    }

    /**
     * Centers this box on [center] and sets its width and height to [size].
     *
     * @return A reference to this box.
     */
    public fun setFromCenterAndSize(center: Vector2, size: Vector2): Box2 {
        val halfX = size.x * 0.5
        val halfY = size.y * 0.5
        min.set(center.x - halfX, center.y - halfY)
        max.set(center.x + halfX, center.y + halfY)
        return this
    }

    /**
     * Returns a new box with copied bounds (independent storage).
     *
     * @return A clone of this instance.
     */
    public fun clone(): Box2 = Box2(min.clone(), max.clone())

    /**
     * Copies the bounds of [box] into this instance.
     *
     * @return A reference to this box.
     */
    public fun copy(box: Box2): Box2 {
        min.copy(box.min)
        max.copy(box.max)
        return this
    }

    /**
     * Makes this box empty (encloses zero space): min = +infinity, max = -infinity.
     *
     * @return A reference to this box.
     */
    public fun makeEmpty(): Box2 {
        min.set(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        max.set(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
        return this
    }

    /**
     * Returns `true` if this box encloses no volume. A box with equal lower and
     * upper bounds is **not** empty — it still contains the single shared point.
     *
     * @return Whether this box is empty or not.
     */
    public fun isEmpty(): Boolean {
        // More robust than (volume <= 0), which can go positive with two negative axes.
        return max.x < min.x || max.y < min.y
    }

    /**
     * Writes the center of this box into [target] (the zero vector if empty).
     *
     * @return [target].
     */
    public fun getCenter(target: Vector2): Vector2 =
        if (isEmpty()) target.set(0.0, 0.0) else target.addVectors(min, max).multiplyScalar(0.5)

    /**
     * Writes the width/height of this box into [target] (the zero vector if empty).
     *
     * @return [target].
     */
    public fun getSize(target: Vector2): Vector2 =
        if (isEmpty()) target.set(0.0, 0.0) else target.subVectors(max, min)

    /**
     * Expands this box to include [point].
     *
     * @return A reference to this box.
     */
    public fun expandByPoint(point: Vector2): Box2 {
        min.min(point)
        max.max(point)
        return this
    }

    /**
     * Expands this box equilaterally by [vector] (x on both horizontal sides, y on
     * both vertical sides).
     *
     * @return A reference to this box.
     */
    public fun expandByVector(vector: Vector2): Box2 {
        min.sub(vector)
        max.add(vector)
        return this
    }

    /**
     * Expands each dimension by [scalar]. A negative scalar contracts the box and
     * can invert it (making it [isEmpty]).
     *
     * @return A reference to this box.
     */
    public fun expandByScalar(scalar: Double): Box2 {
        min.addScalar(-scalar)
        max.addScalar(scalar)
        return this
    }

    /**
     * Returns `true` if [point] lies within or on the boundary of this box.
     *
     * @return Whether the bounding box contains the given point or not.
     */
    public fun containsPoint(point: Vector2): Boolean =
        point.x >= min.x && point.x <= max.x &&
            point.y >= min.y && point.y <= max.y

    /**
     * Returns `true` if this box fully contains [box] (an identical box counts as
     * contained).
     *
     * @return Whether the bounding box contains the given bounding box or not.
     */
    public fun containsBox(box: Box2): Boolean =
        min.x <= box.min.x && box.max.x <= max.x &&
            min.y <= box.min.y && box.max.y <= max.y

    /**
     * Writes [point] as a proportion of this box's width and height into [target].
     *
     * A dimension of size `0` yields a division by zero (`+/-Infinity` or `NaN`) in
     * the corresponding component.
     *
     * @return [target].
     */
    public fun getParameter(point: Vector2, target: Vector2): Vector2 =
        target.set(
            (point.x - min.x) / (max.x - min.x),
            (point.y - min.y) / (max.y - min.y),
        )

    /**
     * Returns `true` if [box] intersects this box (touching edges count).
     *
     * @return Whether the given bounding box intersects with this bounding box.
     */
    public fun intersectsBox(box: Box2): Boolean =
        // 4 splitting planes rule out intersection.
        box.max.x >= min.x && box.min.x <= max.x &&
            box.max.y >= min.y && box.min.y <= max.y

    /**
     * Writes [point] clamped into this box's bounds into [target].
     *
     * @return [target].
     */
    public fun clampPoint(point: Vector2, target: Vector2): Vector2 =
        target.copy(point).clamp(min, max)

    /**
     * Returns the Euclidean distance from [point] to the nearest edge of this box
     * (`0` if inside). An empty box yields `+Infinity`.
     *
     * @return The euclidean distance.
     */
    public fun distanceToPoint(point: Vector2): Double {
        // Inlined clampPoint(point).distanceTo(point) — no shared scratch vector.
        val clampedX = MathUtils.clamp(point.x, min.x, max.x)
        val clampedY = MathUtils.clamp(point.y, min.y, max.y)
        val dx = clampedX - point.x
        val dy = clampedY - point.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Intersects this box with [box] (keeps the overlapping region), making this box
     * empty if there is no overlap.
     *
     * @return A reference to this box.
     */
    public fun intersect(box: Box2): Box2 {
        min.max(box.min)
        max.min(box.max)
        // Canonicalize a non-overlapping result to the standard empty box.
        if (isEmpty()) makeEmpty()
        return this
    }

    /**
     * Unions this box with [box] (grows to enclose both).
     *
     * @return A reference to this box.
     */
    public fun union(box: Box2): Box2 {
        min.min(box.min)
        max.max(box.max)
        return this
    }

    /**
     * Translates this box by [offset], moving both bounds.
     *
     * @return A reference to this box.
     */
    public fun translate(offset: Vector2): Box2 {
        min.add(offset)
        max.add(offset)
        return this
    }

    /**
     * Structural equality: `true` when [other] is a [Box2] with equal bounds.
     */
    override fun equals(other: Any?): Boolean =
        other is Box2 && other.min == min && other.max == max

    override fun hashCode(): Int = 31 * min.hashCode() + max.hashCode()

    override fun toString(): String = "Box2(min=$min, max=$max)"
}
