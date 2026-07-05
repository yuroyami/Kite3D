# PORTING.md — the Kite3D port dialect

Normative rules for porting three.js (`r184`) into common Kotlin. Every file in
`:kite3d` follows these. Derived from FABLE5_AUDIT.md §5/§6. When upstream and
this document disagree, **this document wins** — but deviate from upstream
*semantics* only when a rule below forces it, and record every deviation in
`port-ledger.yaml`.

Ground truth is the three.js source at tag `r184`, not its docs. Port behavior,
including degenerate cases (NaN, ±∞, div-by-zero, inverted boxes); lock them with
tests. Fix upstream *doc* bugs.

## Structure & fidelity

1. One upstream file → one Kotlin file, same name, same method order, same layer
   (`src/math/X.js` → `io/github/yuroyami/kite3d/math/X.kt`). Keeps the port
   diffable against upstream as three.js moves.
2. Pin provenance in every file header: `Ported from three.js r184 src/math/X.js`.
3. Kotlin-only sugar (operators, destructuring, DSL builders, `vararg`
   convenience) lives in **separate `ext/` files** (`ext/Vector2Ext.kt`), never
   interleaved into a ported file. Ported files stay 1:1 mappable.
4. Port each class **with its upstream test file in the same commit**. A class
   without its green tests is not "ported" — it is "started".

## Language mapping

5. Primary constructors with default args, not secondary `constructor` blocks.
6. Drop `this.` except where required to disambiguate. Drop the Mr.doob
   blank-line-after-`{` house style. Drop unary `+` noise (`+Double.X` → `Double.X`).
7. JS `for (;;)` → `for (x in xs)` / `for (i in 0 until n)`. Never a manual
   `while` counter.
8. JS chained assignment (`a = b = v`) → `set(v, v)` or separate statements in
   natural order.
9. JS `Array` params → `Iterable<T>` (accept `List`, `Array.asIterable()`,
   sequences). Add `vararg` overloads only in `ext/`.
10. `isFoo` duck-type flags: **omit**. Type tests use `is Foo`. (Revisit only if a
    later layer's ported logic dispatches on the string/flag.)
11. `equals` / `hashCode` / `toString`: **override all three** on every value-ish
    math type. Never ship a bare `equals(SameType)` overload — it desyncs `==`
    from `.equals`. `hashCode` must normalize `-0.0` (`(x + 0.0)`), so it stays
    consistent with `-0.0 == 0.0`. `equals` compares components with primitive
    `Double` `==` (so `NaN != NaN`, matching JS `===`).
12. Mutable references (`min`, `max`, `elements`, …) are exposed as `val` unless
    upstream *internally reassigns* them; contents stay mutable. Removes an
    aliasing-bug class at zero fidelity cost.
13. File-level `_temp` scratch objects (three.js `const _v = new Vector3()`):
    - Trivially inlinable as component math → **inline it** (allocation-free,
      race-free, usually faster).
    - Otherwise → **allocate a local** inside the method. JVM escape analysis
      usually erases it; correctness before micro-perf.
    - **Never** a file/module-level mutable object. It is a data race on
      JVM/Native even for correctly single-thread-confined user code, and the
      "no expect/actual" rule forbids the `@ThreadLocal`/`ThreadLocal<T>`
      workaround anyway.
14. Scalars are `Double` everywhere (three.js `number` semantics + upstream test
    expectations). Keep expression structure and operation order **identical** to
    upstream so results are bit-reproducible. No `hypot`, no FMA, no
    reassociation, no "nicer" numerics.
15. Component/bit types where upstream is integer-semantic (`isPowerOfTwo`,
    UUID/seed bit-twiddling, normalize/denormalize) use `Int`, and replicate JS
    int32 semantics explicitly (see §Traps). Never port JS bitwise ops onto `Long`.
16. After a new type lands, re-compile and check no call site silently bound to a
    stdlib extension (`copy`, `set`, `plus`, `contains`, `component1`) instead of
    the intended member.

## Cross-layer forward dependencies (out of `math`)

- `BufferAttribute` (core) → the `AttributeLike` interface in `math`
  (`getX/getY/getZ/getW(index)`). Keep `fromBufferAttribute`; core's real
  `BufferAttribute` implements `AttributeLike` later.
- `Camera` (core/objects), used only by `Vector3.project`/`unproject` → **defer**.
  Ship as extension functions in the later camera-aware module, not in `math`.
  Ledger records the deferral.
- Any other upward reference → interface-in-`math` if the method is core to the
  type; otherwise defer to an `ext/` file in the owning layer. Never pull a
  core/objects/renderer type into `math`.

## Docs

17. JSDoc → KDoc: `@param {T} name - desc` → `@param name desc`; `{@link T}` →
    `[T]`; `@return {T}` → the KDoc `@return desc`. Move consumer-relevant
    behavioral notes (div-by-zero, empty-box results, threading) *into* the KDoc.
18. Document, on each class that has them: the mutable + not-thread-safe contract
    ("confine an object graph to one thread, same as three.js"), and
    reference-aliasing semantics of the constructor (`Box2(v, v)` shares `v`).

## Traps (each silently corrupts the port if unhandled)

- **clamp throws in Kotlin.** `x.coerceIn(lo, hi)` throws when `lo > hi`, which is
  a *normal* state (empty box: min=+∞, max=−∞). three.js `clamp` is
  `max(lo, min(hi, x))` and never throws. **Always** use
  `max(lo, min(hi, x))` (`kotlin.math`), never `coerceIn`.
- **`Math.round` differs.** JS `Math.round` is half-up-toward-+∞;
  `kotlin.math.round` is half-to-even. Where upstream uses `Math.round`, port as
  `floor(x + 0.5)`.
- **`Double.toInt()` saturates; JS `| 0` wraps.** To replicate JS `ToInt32`, use
  `(d.toLong() and 0xFFFFFFFFL).toInt()`. `Math.imul(a,b)` ≡ Kotlin `Int` `a*b`
  (both wrap mod 2³²). JS `>>>` → `ushr`; `>>` → `shr`; `>>> 0` →
  `.toLong() and 0xFFFFFFFFL`.
- **Boxing flips Double equality.** Primitive `Double ==` matches JS `===`
  (`NaN != NaN`, `-0.0 == 0.0`); boxed/generic `equals` inverts both. Math fields
  are non-null primitive `Double`, never `Double?`, never compared via generic
  containers.
- **Float determinism = op-order determinism.** Upstream tests assert exact
  doubles. Keep `sqrt(dx*dx + dy*dy)` verbatim.
- **`Number.EPSILON`** = `2.220446049250313E-16`. Define once in `MathUtils`.
- **`%`** is identical in JS and Kotlin (sign of dividend); `euclideanModulo`'s
  `((n % m) + m) % m` ports verbatim.
- **NaN via `min`/`max`**: use `kotlin.math.min`/`max` (NaN-propagating like JS
  `Math.min`/`max`), not comparison reimplementations.

## Checklist per file

- [ ] Header names the upstream file + `r184`.
- [ ] Method set = upstream method set (± declared, ledgered deviations).
- [ ] `equals`/`hashCode`(-0.0 norm)/`toString` present.
- [ ] No `coerceIn`; no `Math.round`-as-half-even; no file-level mutable temp.
- [ ] KDoc covers degenerate behavior + threading/aliasing where relevant.
- [ ] Upstream test file ported to `kotlin-test` and green on jvm + ≥1 native + js.
- [ ] `port-ledger.yaml` updated.
