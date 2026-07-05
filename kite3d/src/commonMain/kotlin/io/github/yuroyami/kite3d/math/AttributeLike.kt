/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Kite3D math-layer seam (no direct three.js source file).
 */
package io.github.yuroyami.kite3d.math

/**
 * The read seam that `fromBufferAttribute` methods pull vector components through.
 *
 * three.js vectors read from a `BufferAttribute` (a `core` concept). The pure math
 * layer must not depend on `core`, so it depends on this narrow interface instead;
 * the real `BufferAttribute` will implement [AttributeLike] once the core layer is
 * ported. See PORTING.md ("Cross-layer forward dependencies") and audit decision D5.
 */
public interface AttributeLike {

    /** Returns the x component of the vector at the given [index]. */
    public fun getX(index: Int): Double

    /** Returns the y component of the vector at the given [index]. */
    public fun getY(index: Int): Double

    /** Returns the z component of the vector at the given [index]. */
    public fun getZ(index: Int): Double

    /** Returns the w component of the vector at the given [index]. */
    public fun getW(index: Int): Double
}
