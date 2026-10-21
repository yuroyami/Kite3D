/*
 * Copyright (c) 2026 yuroyami - MIT.
 */
package io.github.yuroyami.kite3d.math

/**
 * A source of per-vertex vector components. Methods such as `fromBufferAttribute` and
 * [Box3.setFromBufferAttribute] read from it.
 *
 * Kite3D has no `BufferAttribute` class. Implement this interface on your own vertex
 * buffer to read vectors from it.
 */
public interface AttributeLike {

    /**
     * Returns the x component of the vector at the given [index].
     *
     * @param index The vertex index.
     * @return The x component of the vector at that index.
     */
    public fun getX(index: Int): Double

    /**
     * Returns the y component of the vector at the given [index].
     *
     * @param index The vertex index.
     * @return The y component of the vector at that index.
     */
    public fun getY(index: Int): Double

    /**
     * Returns the z component of the vector at the given [index].
     *
     * @param index The vertex index.
     * @return The z component of the vector at that index.
     */
    public fun getZ(index: Int): Double

    /**
     * Returns the w component of the vector at the given [index].
     *
     * @param index The vertex index.
     * @return The w component of the vector at that index.
     */
    public fun getW(index: Int): Double
}
