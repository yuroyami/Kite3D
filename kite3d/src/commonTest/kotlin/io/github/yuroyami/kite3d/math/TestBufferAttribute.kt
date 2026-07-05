/*
 * Copyright © 2026 yuroyami — MIT.
 * Test double for the AttributeLike seam, standing in for three.js's
 * core/BufferAttribute in the math unit tests (mirrors its getX/Y/Z/W(index)).
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
