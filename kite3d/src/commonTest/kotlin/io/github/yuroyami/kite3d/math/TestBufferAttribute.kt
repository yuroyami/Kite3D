/*
 * Copyright © 2026 yuroyami. MIT.
 * Test version of three.js's BufferAttribute: an AttributeLike backed by a DoubleArray.
 */
package io.github.yuroyami.kite3d.math

/** A packed-array [AttributeLike] with a fixed [itemSize], like `BufferAttribute`. */
internal class TestBufferAttribute(
    private val array: DoubleArray,
    private val itemSize: Int,
) : AttributeLike {
    override fun getX(index: Int): Double = array[index * itemSize]
    override fun getY(index: Int): Double = array[index * itemSize + 1]
    override fun getZ(index: Int): Double = array[index * itemSize + 2]
    override fun getW(index: Int): Double = array[index * itemSize + 3]
}
