# Porting three.js code to Kotlin

The rules for turning three.js JavaScript (release `r184`) into the common Kotlin
in `:kite3d`. The three.js source is the reference, not its documentation. Match
its behaviour in edge cases too (NaN, infinity, division by zero, inverted
boxes), and cover those cases with tests.

## Structure

1. One three.js file becomes one Kotlin file, with the same name and the same
   method order: `src/math/X.js` becomes `io/github/yuroyami/kite3d/math/X.kt`.
   The two then stay easy to compare when three.js changes.
2. The file header names the source:
   `Ported to Kotlin for Kite3D from three.js r184 src/math/X.js (MIT).`
3. Kotlin-only additions (operators, destructuring, `vararg` helpers) go in
   separate files, never inside a ported file.
4. A class lands in the same commit as its tests, brought over from
   `test/unit/src/math/`.

## Language mapping

5. Use primary constructors with default arguments.
6. Write `this.` only when a name would be ambiguous. Leave out three.js's blank
   line after `{` and its unary `+` (`+Double.X` becomes `Double.X`).
7. `for (;;)` loops become `for (x in xs)` or `for (i in 0 until n)`.
8. Chained assignment (`a = b = v`) becomes `set(v, v)` or two statements.
9. JavaScript array parameters become `Iterable<T>`.
10. Leave out `isFoo` type flags. Use `is Foo`.
11. Value types override `equals`, `hashCode` and `toString` together. Never add a
    separate `equals(SameType)`, because `==` would then disagree with
    `.equals`. `hashCode` adds `0.0` to each component, so `-0.0` and `0.0` hash
    the same. `equals` compares with primitive `Double ==`, so `NaN != NaN`, as
    in JavaScript. Matrices are the exception: they use `contentEquals` and
    `contentHashCode`, so for them `NaN` equals `NaN` and `-0.0` does not equal `0.0`.
12. Expose mutable members such as `min`, `max` and `elements` as `val`, unless
    three.js assigns a new object to them. Their contents stay mutable.
13. three.js keeps scratch objects at file level (`const _v = new Vector3()`).
    Inline them as plain component maths, or create a local inside the method.
    Never keep a mutable object at file level: two threads that use two
    separate instances would still share it.
14. Every scalar is `Double`. Keep each expression and its order of operations
    exactly as three.js writes it, so results match its tests to the last bit.
    No `hypot`, no fused multiply-add, no reordering.
15. Where three.js uses integer bit operations (`isPowerOfTwo`, seeds, UUIDs,
    normalize and denormalize), use `Int` and copy JavaScript's 32-bit rules
    exactly. See the traps below. Never use `Long` for them.
16. After adding a type, check that no call site now binds to a standard library
    extension (`copy`, `set`, `plus`, `contains`, `component1`) instead of the
    member you meant.

## Types from outside the maths layer

Kite3D has no scene, geometry or renderer types, and it never imports one.

- `AttributeLike` stands in for three.js's `BufferAttribute`, so the
  `fromBufferAttribute` methods stay.
- Any other method that needs such a type is left out. The class KDoc says it is
  missing and what to use instead, and the README's Limits section lists it.

## Documentation

17. JSDoc becomes KDoc: `@param {T} name - desc` becomes `@param name desc`, and
    `{@link T}` becomes `[T]`. Put behaviour a caller must know (division by
    zero, empty boxes, threading) into the KDoc.
18. Each mutable class says that it is not thread-safe. Each constructor that
    keeps the objects passed to it says so (`Box2(v, v)` shares `v`).

## Traps

Each of these changes results without an error.

- **`coerceIn` throws.** `x.coerceIn(lo, hi)` throws when `lo > hi`, and that is a
  normal state here: an empty box has `min = +∞` and `max = -∞`. three.js's
  `clamp` never throws. Always write `max(lo, min(hi, x))` from `kotlin.math`.
- **`Math.round` rounds differently.** JavaScript rounds a half up, towards +∞.
  `kotlin.math.round` rounds a half to the even number. Write `floor(x + 0.5)`.
- **`toInt()` saturates, but JavaScript's `| 0` wraps.** For JavaScript's
  `ToInt32`, write `(d.toLong() and 0xFFFFFFFFL).toInt()`. `Math.imul(a, b)` is
  Kotlin's `Int` multiplication `a * b`. `>>>` is `ushr`, `>>` is `shr`, and
  `>>> 0` is `.toLong() and 0xFFFFFFFFL`.
- **Boxing changes `Double` equality.** Primitive `==` matches JavaScript's
  `===`: `NaN != NaN` and `-0.0 == 0.0`. Boxed `equals` gives the opposite answer
  for both. Keep maths fields as non-null primitive `Double`.
- **The order of operations is part of the result.** three.js tests check exact
  doubles. Keep `sqrt(dx * dx + dy * dy)` as it is.
- **`Number.EPSILON`** is `2.220446049250313E-16`, defined once in `MathUtils`.
- **`%`** works the same in both languages (the sign follows the dividend), so
  `euclideanModulo` copies over as written.
- **`min` and `max` with NaN:** use `kotlin.math.min` and `max`. They return NaN
  when an argument is NaN, as JavaScript does. Do not write your own comparison.

## Checklist for each file

- [ ] The header names the three.js file and `r184`.
- [ ] Every three.js method is there, or the class KDoc says why it is missing.
- [ ] `equals`, `hashCode` (with the `-0.0` fix for vectors) and `toString` are there.
- [ ] No `coerceIn`, no half-to-even rounding, no mutable file-level object.
- [ ] The KDoc covers edge cases, threading and shared references where they apply.
- [ ] The three.js tests are in `kotlin-test` and pass on the JVM, one native
      target and JavaScript.
