# FABLE5_AUDIT.md — Kite3D Deep Audit (Phase 1)

> **Auditor:** Claude Fable 5 · **Date:** 2026-07-05
> **Consumer:** Opus 4.8, piloted by Fable 5, for **Phase 2 — Revamping**
> **Method:** Code-only ground truth. Every claim below was verified against the actual files, an actual
> `./gradlew :kite3d:compileKotlinJvm` run, and the actual three.js `r184` upstream sources
> (`src/math/Box2.js`, `test/unit/utils/math-constants.js`, `test/unit/src/math/Box2.tests.js`,
> `src/math/Vector2.js`, `src/math/Vector3.js`, GitHub contents API for `src/math/`).
> Docs (README, comments) were treated as claims to verify, not as truth.

---

## 0. Executive summary

Kite3D today is **a publishing pipeline wrapped around code that does not compile**.

The repository contains exactly **two Kotlin source files** — `Box2.kt` (commonMain) and
`TestConstants.kt` (commonTest) — both of which reference a `Vector2` (and `Vector3`) that
**do not exist anywhere in the repository**. `:kite3d:compileKotlinJvm` fails with 25+
`Unresolved reference` errors (full log in §2). Meanwhile the build configures **22 Kotlin targets**,
Maven Central publishing, release signing, and Dokka aggregation. The project is simultaneously
over-engineered (infrastructure) and under-engineered (product): the cart is not merely before the
horse — there is no horse.

That said, what *does* exist is of higher quality than a typical day-one port:

- `Box2.kt` is **method-for-method complete and semantically faithful** to three.js r184 `Box2.js`
  (all 22 methods, identical logic and method order — verified line-by-line against upstream).
- The LICENSE correctly carries **dual copyright** (port + three.js authors).
- `TestConstants.kt` contains one **deliberate, correct improvement** over upstream
  (fresh-instance `get()` accessors instead of shared mutable constants).
- The build's target matrix and publishing metadata are coherent and would work once code compiles.

### Scorecard (user's 0% → 100% horizon)

| Dimension | Score | One-line justification |
|---|---|---|
| Compiles | **0%** | Proven build failure; `Vector2`/`Vector3` missing. |
| Port fidelity (of what exists) | **85%** | 22/22 methods, logic exact; loses points for de-sugar artifacts (§4.3) and one thread-safety regression-by-context (§3.1). |
| Kotlin idiomaticity | **15%** | JS transliteration: `this.` everywhere, secondary constructor, `while` loops, no `equals`/`hashCode`/`toString` overrides, `Array` params, `isBox2` duck-type flag. |
| Correctness under concurrency | **0%** | File-level shared mutable `_vector` breaks even correctly-confined user code (§3.1). |
| Tests | **0%** | 24 upstream Box2 test blocks exist; zero ported. Test constants file compiles against nothing. |
| API design (vs. a great Kotlin lib) | **20%** | Faithful three.js shape is a defensible base, but zero Kotlin affordances and one actively dangerous `equals` overload (§3.2). |
| Build hygiene | **45%** | Works, but: no `explicitApi()`, no BCV, dead flags, duplicated group/version, JVM 21 bytecode for a broad library, unused feature previews. |
| Repo/process hygiene | **5%** | **Not a git repository.** No `.gitignore`, no CI, no CHANGELOG, no CONTRIBUTING, no PORTING spec. |
| Docs honesty | **60%** | README architecture/status are accurate and well-written; but "porting in dependency order" is already violated by the very first file, and a test-file comment is stale/misleading (§4.5). |
| Licensing/attribution | **95%** | Dual MIT done right; missing only per-file upstream revision pins. |
| **Overall** | **≈10%** | A well-intentioned skeleton with excellent paperwork and no functioning body. |

### The five findings that matter most

1. **[BLOCKER] The library does not compile.** Box2 was ported before its only dependency
   (`Vector2`), violating the README's own "dependency order" rule on file #1. → §2
2. **[BLOCKER-BY-DESIGN] `private val _vector = Vector2()` file-level scratch temp** is a data race
   on JVM/Native even when users confine every Box2 instance to a single thread. This is *the*
   canonical three.js→multithreaded-runtime porting trap, and it is trivially avoidable here. → §3.1
3. **[HIGH] `fun equals(box: Box2)` overloads — does not override — `Any.equals`,** so
   `a == b` (reference equality) and `a.equals(b)` (structural) silently disagree. Worst-of-both-worlds. → §3.2
4. **[HIGH] Kotlin's `coerceIn` throws where JS `clamp` doesn't.** Not yet a bug (Vector2 doesn't
   exist) but the #1 semantic landmine for Phase 2's Vector2/MathUtils port; documented with the full
   trap catalogue so it never becomes a bug. → §6
5. **[HIGH] No git repository.** POM metadata points at `github.com/yuroyami/Kite3D`, yet the local
   tree has no `.git`, no `.gitignore`, no CI. One `rm -rf` from oblivion. → §8.1

---

## 1. Ground-truth inventory (what actually exists)

```
Kite3D/
├── LICENSE                          # MIT, dual copyright (yuroyami + three.js authors) ✔
├── README.md                        # Architecture/status/roadmap — mostly accurate
├── build.gradle.kts                 # root: plugin aliases, allprojects group/version, Dokka aggregation
├── settings.gradle.kts              # rootProject.name = "kite3d-KMP", includes :kite3d
├── gradle.properties                # JVM args, MPP flags, POM/publishing config
├── gradle/libs.versions.toml        # kotlin 2.4.0, vanniktech 0.36.0, dokka 2.0.0
├── gradle/wrapper/                  # Gradle 9.5.1 (bin)
└── kite3d/
    ├── build.gradle.kts             # KMP: 22 targets, kotlin-test, no runtime deps
    ├── gradle.properties            # POM_NAME / POM_DESCRIPTION
    └── src/
        ├── commonMain/kotlin/io/github/yuroyami/kite3d/math/
        │   └── Box2.kt              # 388 lines — the ONLY production source file
        └── commonTest/kotlin/io/github/yuroyami/kite3d/math/
            └── TestConstants.kt     # 30 lines — the ONLY test source file (constants, no tests)
```

Not present: any `Vector2`, `Vector3`, `MathUtils`, any actual test, `.git/`, `.gitignore`,
`.github/`, CI of any kind, CHANGELOG, CONTRIBUTING, Dokka module docs, samples, benchmarks.

**Targets declared** (kite3d/build.gradle.kts:25-69): jvm; iosSimulatorArm64, iosArm64, iosX64,
macosArm64, macosX64, tvosArm64, tvosSimulatorArm64, watchosArm32, watchosArm64,
watchosSimulatorArm64, watchosDeviceArm64; linuxX64, linuxArm64, mingwX64; androidNativeArm32/Arm64/X86/X64;
js(IR){browser,nodejs,binaries.library()}, wasmJs{browser,nodejs}, wasmWasi{nodejs}. **22 targets, 0 compiling.**

---

## 2. Verified build failure (evidence)

`./gradlew :kite3d:compileKotlinJvm` (run 2026-07-05, Gradle 9.5.1, Kotlin 2.4.0 — toolchain itself
resolves and runs fine; failures are all source-level):

```
e: Box2.kt:276:20 Unresolved reference 'y'.            (getParameter — Vector2 members)
e: Box2.kt:291:24 Unresolved reference 'x'.            (intersectsBox)
e: Box2.kt:303:27 Unresolved reference 'Vector2'.      (clampPoint signature)
e: Box2.kt:305:23 Cannot infer type for type parameter 'K'…   ← target.copy(point) resolved to
e: Box2.kt:305:28 Too many arguments for 'fun <K, V> Map.Entry<K, V>.copy()'.  ← stdlib Map.Entry.copy!
e: Box2.kt:305:35 Unresolved reference 'clamp'.
e: Box2.kt:316:32 Unresolved reference 'Vector2'.      (distanceToPoint)
e: Box2.kt:318:48 Unresolved reference 'distanceTo'.
e: Box2.kt:366:27 Unresolved reference 'Vector2'.      (translate)
e: Box2.kt:368:18 Unresolved reference 'add'.
… (25 errors total)
BUILD FAILED in 17s
```

Two things worth noting beyond "it's broken":

- The `Map.Entry.copy()` resolution shows how Kotlin's stdlib extension surface can *silently*
  capture method names during porting. When `Vector2` lands, re-verify no call site still binds to a
  stdlib extension (`copy`, `set`, `plus`, `contains` are all common collisions).
- `commonTest` is equally uncompilable (`TestConstants.kt` references `Vector2` **and** `Vector3`),
  so even the test scaffolding was written two dependencies ahead of reality.

**Root cause is process, not typo:** README.md:27 says *"Porting in dependency order"* — and the
very first ported file (`Box2`) depends on the unported `Vector2`, which depends on the unported
`MathUtils.clamp` (verified: upstream `Vector2.js` line 1 is `import { clamp } from './MathUtils.js'`).
Phase 2 must make dependency order mechanically enforced, not aspirational (§9, step 0/2).

---

## 3. Correctness findings in the code that exists

### 3.1 [BLOCKER-BY-DESIGN] Shared mutable file-level scratch: `_vector` (Box2.kt:8)

```kotlin
private val _vector = Vector2()
```

Used by `setFromCenterAndSize` (Box2.kt:97) and `distanceToPoint` (Box2.kt:318). This is a direct
port of upstream `const _vector = /*@__PURE__*/ new Vector2();` — which is **safe in JavaScript only
because JS is single-threaded per realm**. In Kotlin/JVM and Kotlin/Native this is a plain data race:

- Thread A: `boxA.setFromCenterAndSize(c, s)` writes `_vector`.
- Thread B: `boxB.distanceToPoint(p)` writes `_vector`.
- A and B share **nothing user-visible** — each thread correctly confines its own Box2 — and still
  get corrupted results. The library breaks *correct* user code. This is strictly worse than
  "three.js objects aren't thread-safe" (true, documentable); it is "the *file* isn't thread-safe."

**Why the fix is free here:** every `_vector` use in Box2 is trivially expressible as component
arithmetic with zero allocations and zero temps — *faster* than the upstream dance:

```kotlin
fun setFromCenterAndSize(center: Vector2, size: Vector2): Box2 {
    val hx = size.x * 0.5; val hy = size.y * 0.5
    min.set(center.x - hx, center.y - hy)
    max.set(center.x + hx, center.y + hy)
    return this
}

fun distanceToPoint(point: Vector2): Double {
    // clamp must replicate JS max(min, min(max, v)) — NOT coerceIn (§6.1)
    val cx = max(this.min.x, min(this.max.x, point.x))
    val cy = max(this.min.y, min(this.max.y, point.y))
    val dx = cx - point.x; val dy = cy - point.y
    return sqrt(dx * dx + dy * dy)   // same op order as upstream distanceTo → bit-identical results
}
```

**Phase 2 policy (apply library-wide):**
1. Where a temp is trivially inlinable as component math (all Box2 cases, most Box3/Sphere cases) —
   **inline it**. Fastest, allocation-free, race-free.
2. Where it is not (Matrix4.decompose, Euler↔Quaternion conversions use several vector/quat/matrix
   temps) — **allocate locally**. JVM escape analysis usually erases it; Native/JS cost is real but
   correctness wins pre-benchmark. Revisit with kotlinx-benchmark data only (§9 step 8).
3. **Never** file-level mutable temps. `@ThreadLocal` is Native-only, `ThreadLocal<T>` is JVM-only,
   and the README's own "no expect/actual" rule (kite3d/build.gradle.kts:16-17 comment) forbids the
   workaround — so the rule is also self-consistent.
4. Document the *object-level* threading contract once, library-wide: "Kite3D math objects are
   mutable and not thread-safe; confine any given object graph to one thread — same as three.js."

### 3.2 [HIGH] `equals` overload that isn't an override (Box2.kt:381)

```kotlin
fun equals(box: Box2): Boolean
```

This does **not** override `Any.equals(other: Any?)`. Consequences today:

- `a == b` → reference equality (compiles to `equals(Any?)`).
- `a.equals(b)` → structural equality (statically picks the overload).
- The *same conceptual expression differs by call syntax*. In hash containers, boxes are
  identity-keyed while looking value-comparable. This will generate the library's first three bug
  reports all by itself.

**Recommendation (decision D3, §10):** override properly —

```kotlin
override fun equals(other: Any?): Boolean =
    other is Box2 && other.min == this.min && other.max == this.max
override fun hashCode(): Int = 31 * min.hashCode() + max.hashCode()
override fun toString(): String = "Box2(min=$min, max=$max)"
```

with the standard mutable-key caveat documented (same caveat `data class` with `var` has). This
requires `Vector2` to itself override `equals`/`hashCode` with primitive-`Double` semantics
(NaN ≠ NaN, −0.0 == 0.0 — see §6.3 for why boxing would silently change this). The alternative —
keeping only the overload — preserves upstream's shape but ships the `==`/`.equals` schism; rejected.
Note `hashCode` consistency: three.js `.equals` uses JS `===` per-component; Kotlin primitive `==`
matches it exactly, but `Double.hashCode()` differs between `0.0`/`-0.0`… which is *consistent* with
`-0.0 == 0.0` being true only if both hash equal — they do NOT (`(-0.0).hashCode() != 0.0.hashCode()`
on JVM). So either normalize `-0.0` in `hashCode` (`(x + 0.0).hashCode()`) or accept the
equal-but-different-hash violation. **Normalize; add a test locking it.**

### 3.3 [MEDIUM] Aliasing footguns inherited from upstream

- `constructor(min, max)` **stores references** (Box2.kt:41-42): `Box2(v, v)` yields a box whose
  min *is* its max; `expandByPoint` then mutates both. Upstream has the identical behavior, and the
  upstream test suite *relies* on reference semantics (`copy` test reassigns `a.min`), so a silent
  copy-in would be a fidelity break. **Keep reference semantics; document loudly in the ctor KDoc.**
- `var min: Vector2` / `var max: Vector2` (Box2.kt:23-28) — public *reassignable* references.
  three.js code never reassigns `.min`/`.max` internally (verified in upstream Box2.js — only
  `.copy()`/`.set()` into them); making them `val` (contents still mutable) removes an entire class
  of aliasing bugs at zero fidelity cost. The one upstream test line that reassigns (`a.min = zero2`
  in the copy test) adapts cleanly to `a.min.copy(zero2)` *and preserves the test's intent*
  (prove b is a deep copy). **Recommendation: `val` (decision D2, §10).**

### 3.4 [LOW] `clone()` wastes two allocations (Box2.kt:110-114)

`Box2().copy(this)` allocates two `Vector2(±∞, ±∞)` defaults, then overwrites them. Upstream does
this because `new this.constructor()` is polymorphic-clone JS idiom. Kotlin has no such constraint:
`fun clone() = Box2(min.clone(), max.clone())` — half the garbage, same semantics. (Do **not**
implement `java.lang.Cloneable` — it isn't, and shouldn't be.)

### 3.5 [LOW] Degenerate-input behaviors are correct but undocumented

Verified-by-inspection behaviors that match upstream and should be **locked by tests + KDoc** rather
than "fixed":

- `getParameter` on a zero-width/height or empty box → ±Infinity/NaN (division by zero). Upstream
  carries the same internal comment (Box2.js:263-264); the port keeps it as a code comment
  (Box2.kt:271-272) where it belongs in the KDoc for consumers to see.
- `expandByScalar` with a negative scalar can invert the box → subsequent `isEmpty()` is true.
- `containsPoint`/`intersectsBox` with NaN components → false (all comparisons false). Fine; NaN
  poisoning via `expandByPoint(NaN)` propagates through `min`/`max` — same as upstream JS.
- `intersect()` canonicalizes no-overlap results via `makeEmpty()` (Box2.kt:336) — matches r184
  (upstream added this normalization; the "Infinite empty" upstream test at Box2.tests.js:350-354
  locks it — port that test).
- `distanceToPoint` on an *empty* box → `+Infinity` (clamp of anything into [+∞, −∞] yields +∞ under
  the JS max/min formulation). This **only** holds if Phase 2 implements clamp the JS way — with
  `coerceIn` it *throws* instead (§6.1). Add an explicit test for this exact case; it is the
  regression-canary for the whole clamp-semantics decision.

### 3.6 [NIT] `isBox2: Boolean = true` (Box2.kt:18)

Per-instance field (~4-8 bytes each on JVM with padding) whose only purpose in JS is duck-typing —
Kotlin has real types (`is Box2`). The upstream `isBox2` *test* (Box2.tests.js:34-42) even includes
an untranslatable `new Object().isBox2` assertion. **Remove the field**; adapt the test to
`assertTrue(Box2() is Box2)` or drop it. If some later TSL-layer port genuinely needs
string/flag-based type tests, reintroduce as a zero-cost extension or interface marker then — not
speculatively now. (Applies to every future `isVector3`, `isMatrix4`, `isQuaternion`… — make this a
dialect rule, §5.)

---

## 4. Upstream fidelity report (port vs. three.js r184, verified)

### 4.1 Method parity: complete ✔

All 22 upstream methods present, in identical order (good — keeps files diffable against upstream):
`set, setFromPoints, setFromCenterAndSize, clone, copy, makeEmpty, isEmpty, getCenter, getSize,
expandByPoint, expandByVector, expandByScalar, containsPoint, containsBox, getParameter,
intersectsBox, clampPoint, distanceToPoint, intersect, union, translate, equals` + ctor + `isBox2`.
No method invented, none dropped. Logic per-method is expression-level identical.

### 4.2 Upstream doc bugs faithfully inherited (decide: fidelity vs. quality)

The port reproduces three.js r184's own documentation defects verbatim:

| Location (port) | Text | Upstream origin |
|---|---|---|
| Box2.kt:88-89 (`setFromCenterAndSize`) | "sets this box's width, height **and depth**" — a 2D box has no depth | Box2.js:82-83 (copy-paste from Box3 upstream) |
| Box2.kt:132 (`makeEmpty`) | "which means **in** encloses a zero space" | Box2.js:126 |
| Box2.kt:342-343 (`union`) | "the union of this box **and another and the given one**" | Box2.js:335 |
| Box2.kt:148-151 (`isEmpty`) | "includes zero points within its bounds" then immediately contradicts itself for the equal-bounds case — confusing upstream wording | Box2.js:140-142 |

**Recommendation:** fix the docs in the port (they are *bugs*, not semantics), and note each fix in
the port ledger (§7.2) so upstream-diff tooling doesn't flag them as drift. Optionally upstream a PR
to three.js — goodwill and it erases the delta at the source.

### 4.3 De-sugar artifacts (port made upstream *worse*, mechanically)

- Upstream `for ( let i = 0, il = points.length; i < il; i ++ )` became a manual
  `var i = 0; val il = …; while (i < il) { …; i++ }` (Box2.kt:74-81). Kotlin's actual equivalent is
  `for (point in points) expandByPoint(point)` — 12 lines → 4, zero semantic change.
- Upstream chained assignment `this.min.x = this.min.y = + Infinity` became the bizarre reversed
  two-step `min.y = +∞; min.x = min.y` (Box2.kt:138-141) — correct, but reads like a bug. Kotlin:
  `min.set(POSITIVE_INFINITY, POSITIVE_INFINITY)`.
- `+Double.POSITIVE_INFINITY` unary-plus noise (Box2.kt:37) — JS-source cosmetics.
- `this.` on every member access, blank line after `{` / before `}` (Mr.doob house style), secondary
  `constructor` block instead of a primary constructor — all transliteration residue.

None are bugs. All are exactly what the **dialect spec** (§5) exists to prevent across the next ~200 files.

### 4.4 Deliberate deviations found (and their verdicts)

| Deviation | Verdict |
|---|---|
| `Array<Vector2>` for `setFromPoints` (upstream: JS Array) | Wrong pick — see §7.1; use `Iterable<Vector2>`/`List<Vector2>`. |
| `Double` for all scalars (upstream: JS number) | Correct; matches README's stated policy and upstream test expectations. |
| TestConstants uses `get() =` fresh instances (upstream: shared `const` instances) | **Good improvement** — eliminates the shared-mutable-constant hazard the upstream tests tiptoe around with `.clone()`. Keep. |
| TestConstants keeps upstream's "clone before mutating" guidance comment (TestConstants.kt:6-7) | Now **misleading** — fresh instances make cloning pointless. Fix the comment to describe the `get()` design. |

### 4.5 TestConstants vs. upstream `math-constants.js`: content parity ✔

`x,y,z,w = 2,3,4,5`, `eps = 0.0001`, `negInf2/posInf2/negOne2/zero2/one2/two2`,
`negInf3/posInf3/zero3/one3/two3` — exact match; nothing missing (verified: upstream has no
`negOne3`). Two notes: (a) top-level `const val x/y/z/w` in the *same package as production code*
invites shadowing confusion once tests have locals named `x`; acceptable for test scope, but an
`object MathTestConstants` (or `@file:JvmName`-style namespacing) would be cleaner — low priority;
(b) `eps` will pair with kotlin-test's common
`assertEquals(expected: Double, actual: Double, absoluteTolerance: Double)` — it exists in
commonTest; no helper porting needed.

### 4.6 Missing entirely: the Box2 test suite

Upstream `Box2.tests.js` = **24 QUnit test blocks**: Instancing, isBox2, set, setFromPoints,
setFromCenterAndSize, clone, copy, empty/makeEmpty, isEmpty, getCenter, getSize, expandByPoint,
expandByVector, expandByScalar, containsPoint, containsBox, getParameter, intersectsBox, clampPoint,
distanceToPoint, intersect (incl. the "Infinite empty" normalization case), union, translate, equals.
Zero ported. Port adaptations needed: `isBox2` → `is Box2` (or drop); `a.min = zero2` → `a.min.copy(zero2)`
if D2 (val) is accepted; `assert.ok(x == Math.sqrt(2))` → `assertEquals(sqrt(2.0), x, 0.0)` (exact —
the arithmetic is bit-deterministic if op order is preserved, §6.6).

---

## 5. The Port Dialect Spec (the heart of Phase 2)

The single highest-leverage artifact Phase 2 can produce is **PORTING.md** — a mechanical rulebook
applied to every file, so the ~200-file port is uniform, reviewable, and upstream-diffable. Rules
derived from everything found above:

**Structure & fidelity**
1. One upstream file → one Kotlin file, same name, same method order, same package layer
   (`src/math/X.js` → `…kite3d/math/X.kt`). Keeps upstream diffs re-applicable.
2. Pin provenance per file in the header: `Ported from three.js r184 src/math/Box2.js` (the current
   header omits the revision — add it; README is the only place r184 appears today).
3. Kotlin sugar (operators, destructuring, DSL) lives in **separate `ext/` files** (e.g.
   `ext/Box2Ext.kt`), never interleaved into ported files — ported files stay 1:1 mappable.
4. Preserve upstream *semantics* including degenerate cases (NaN, ±∞, inverted boxes, div-by-zero) —
   lock them with tests. Fix upstream *doc* bugs; record each in the port ledger (§7.2).
5. Port each file **together with its upstream test file in the same commit**. A file without its
   green tests is not "ported".

**Language mapping**
6. Primary constructors with default args, not secondary `constructor` blocks.
7. Drop `this.` except where required for disambiguation. Drop Mr.doob blank-line braces. Drop unary `+`.
8. JS `for(;;)` → Kotlin `for (x in …)` / `for (i in 0 until n)`. Never manual `while` counters.
9. Chained assignments → `set(...)` calls or separate statements in *natural* order.
10. JS Array params → `Iterable<T>` (accept `List`, `Array.asIterable()`, sequences); add `vararg`
    convenience overloads in ext files only.
11. `isFoo` duck-type flags: **omit**. Type tests use `is Foo`. (Revisit only if a later layer's
    ported logic string-dispatches on them.)
12. `equals`: override `equals(Any?)` + `hashCode` (−0.0-normalized) + `toString` on every math type
    (per D3). Never ship a bare `equals(SameType)` overload.
13. Mutable references exposed as `val` unless upstream *internally* reassigns them (D2).
14. File-level `_temp` objects: inline as component math when trivial; else local allocation. Never
    shared mutable file/module state (§3.1).
15. Scalar type: `Double` everywhere (README policy). Keep expression structure/op order identical
    to upstream for bit-reproducible results (§6.6). Do not "improve" numerics (no `hypot`, no FMA).
16. Every stdlib name collision (`copy`, `set`, `plus`, `clamp`, `contains`) — after Vector2 lands,
    re-compile and verify no call site silently binds to a stdlib extension (§2's `Map.Entry.copy`).

**Docs**
17. Ported JSDoc → KDoc: `@param {Vector2} min - …` → `@param min …`; `{@link Vector2}` → `[Vector2]`
    (already done correctly in Box2.kt ✔). Behavioral comments that matter to consumers (e.g.
    getParameter's div-by-zero) move *into* the KDoc.
18. KDoc the threading contract and reference-aliasing semantics on every class that has them.

---

## 6. JS → Kotlin semantic trap catalogue (Phase 2 pre-flight)

Each of these WILL corrupt the port silently if unhandled. Verified against Kotlin semantics.

1. **`coerceIn` throws on inverted ranges; JS clamp doesn't.** three.js `MathUtils.clamp` /
   `Vector2.clamp` compute `max(min, min(max, v))`. Kotlin `v.coerceIn(lo, hi)` **throws
   `IllegalArgumentException`** when `lo > hi` — which is a *normal state* for empty boxes
   (min=+∞, max=−∞). Every clamp in the port must be the explicit two-call form
   (`kotlin.math.max(lo, kotlin.math.min(hi, v))`). Affected immediately: `MathUtils.clamp`,
   `Vector2.clamp/clampScalar/clampLength`, `Box2.clampPoint`/`distanceToPoint` (empty-box → +∞ 
   behavior depends on it, §3.5).
2. **NaN ordering:** `kotlin.math.min/max` propagate NaN like JS `Math.min/max` ✔ — but
   `coerceAtLeast/coerceAtMost` are `if (this < min) …` forms (NaN-preserving, OK), while
   *sorting/compareTo* treats NaN as greater-than-everything (differs from JS sort comparators).
   Use `kotlin.math` functions, not comparison-operator reimplementations, wherever upstream used
   `Math.*`.
3. **Boxing changes Double equality.** Primitive `Double` `==` matches JS `===`
   (NaN≠NaN, −0.0==0.0). **Boxed** `Double?`/generic `equals` inverts both (NaN==NaN, −0.0≠0.0).
   Dialect consequence: math-class fields are non-nullable `var x: Double` primitives, never
   `Double?`, never compared via generic containers.
4. **`-0.0` vs `hashCode`:** normalize (−0.0 → 0.0) inside `hashCode` implementations (§3.2).
5. **JS bitwise = int32 coercion.** Upstream `| 0`, `>>>`, `& 0xff` (MathUtils.generateUUID,
   normalize/denormalize, Color packing) need explicit `.toInt()` / `ushr` / masking with attention
   to 32-bit overflow — JS `<<` on numbers wraps at 32 bits; Kotlin `Int` matches, `Long` doesn't.
   Port bit-twiddling on `Int`, not `Long`.
6. **Float determinism = op-order determinism.** Upstream tests assert *exact* doubles
   (e.g. `distanceToPoint == Math.sqrt(2)`). IEEE-754 ops are deterministic given identical op
   order, so keep `sqrt(dx*dx + dy*dy)` exactly — no `hypot` (different rounding), no reassociation.
7. **`Number.EPSILON`** → `2.220446049250313E-16` (2⁻⁵²). Used by upstream Quaternion.slerp,
   Matrix decompose, Plane, Ray. Define once in the ported `MathUtils`.
8. **JS `%` vs Kotlin `%`:** identical (both remainder, dividend sign) ✔ — `euclideanModulo`'s
   `((n % m) + m) % m` ports verbatim.
9. **Integer division:** JS `/` is always floating; watch any ported index math (`(i / 3) | 0` →
   `i / 3` on Int is already truncating — but `i / 3` on Double is not; keep types straight).
10. **`fromBufferAttribute(attribute, index)`** (Vector2/3/4) references the core-layer
    `BufferAttribute` — a forward dependency out of the math layer. Options: (a) omit until core
    lands (temporary parity hole, honest); (b) define a minimal
    `interface AttributeLike { fun getX(i: Int): Double; … }` in math and have core implement it
    (dependency inversion). **Recommend (b)** — keeps Vector2 100% method-complete on day one
    (decision D5, §10).

---

## 7. Testing & tooling strategy

### 7.1 Test port

- Upstream QUnit math tests are extremely regular: `assert.ok(expr, 'Passed!')`,
  `assert.strictEqual`, numeric `==` asserts, `eps` comparisons. Map to kotlin-test:
  `assertTrue`, `assertEquals`, `assertEquals(e, a, absoluteTolerance = eps)`.
- three.js has on the order of ~25 math test files (one per class, mirroring `src/math/`). **Rule 5
  of the dialect spec:** class + tests land together.
- **High-leverage tool:** a ~200-line QUnit→kotlin-test transpiler script (regex/structural — the
  corpus is that regular) will mechanize 90% of test porting. Build it when porting file #2
  (Vector2's test file is the biggest, ~1000+ lines — immediate payoff). Keep hand-finishing the
  last 10% (isBox2-style duck-typing asserts, `new Object()` cases).
- **Additive Kite3D-only tests** (upstream doesn't have them; Kotlin needs them):
  - `distanceToPoint(empty box) == +∞` (the coerceIn canary, §3.5/§6.1)
  - `equals`/`hashCode` contract incl. −0.0 normalization and NaN inequality
  - `a == b` ⇔ `a.equals(b)` (kills the §3.2 class of bug forever)
  - Concurrency smoke: N threads hammering *distinct* instances of each class with temp-using
    methods; assert results equal single-thread baseline (kills §3.1 regressions; JVM-only test is fine).
  - toString sanity, `in` operator ⇔ containsPoint (once ext layer exists).

### 7.2 Port ledger (the revolutionary-but-practical piece)

A machine-checked `port-ledger.yaml` at repo root:

```yaml
threejs_rev: r184
files:
  src/math/Box2.js:
    kotlin: kite3d/src/commonMain/kotlin/io/github/yuroyami/kite3d/math/Box2.kt
    status: ported
    tests: ported          # test/unit/src/math/Box2.tests.js
    deviations:
      - "doc fixes: setFromCenterAndSize 'and depth' removed; union wording; makeEmpty typo"
      - "isBox2 flag omitted (dialect rule 11)"
      - "min/max are val (D2)"
  src/math/Vector2.js: { status: pending }
  ...
```

Plus a small script (`tools/port-ledger-check`) that (a) fails CI if a `ported` file's upstream
method list ≠ Kotlin member list modulo declared deviations, and (b) on upstream rev bump, lists
which ported files changed upstream. This converts "line-for-line port of a moving 1000-file
project" from folklore into an auditable process. **Strongly recommended; cheap; do it early**
(§9 step 6).

### 7.3 Benchmarks

Add a `kite3d-bench` module (kotlinx-benchmark) once Vector3/Matrix4 exist — *before* any
perf-motivated deviation from dialect rule 14/15. No benchmarks → no perf-driven design changes.
That kills speculative object pools, SIMD adventures, and layout flattening until data exists.

---

## 8. Build & repo audit (item-by-item)

### 8.1 [HIGH] Repo/process

| # | Finding | Fix |
|---|---|---|
| R1 | **Not a git repository** (verified `git rev-parse` fails) while POMs advertise GitHub SCM | `git init`, first commit, create remote matching gradle.properties:32-40, push |
| R2 | No `.gitignore` | Standard Kotlin/Gradle ignore (`.gradle/`, `build/`, `.kotlin/`, `local.properties`, `.idea/`, `kotlin-js-store/` — note `kotlin.js.yarn=true` will generate a `kotlin-js-store/yarn.lock` that SHOULD be committed) |
| R3 | No CI | GitHub Actions: PR = build + test on `jvm`, `js(node)`, `wasmJs(node)`, `macosArm64`, `linuxX64`, `mingwX64` (three OS runners); full 22-target compile on release tags only. Testing all 22 per-PR is the overkill trap. |
| R4 | No CHANGELOG / CONTRIBUTING / PORTING.md | PORTING.md is §5 and is mandatory; others cheap |
| R5 | No Dokka module docs (`includes`), no `sourceLink` to GitHub | Add once repo exists |

### 8.2 kite3d/build.gradle.kts

| # | Line | Finding | Fix |
|---|---|---|---|
| B1 | — | **No `explicitApi()`** — for a published library every unmarked declaration silently becomes public API | `explicitApi()` in the `kotlin {}` block. Do it *now* while the API surface is one file. |
| B2 | 26 | `jvmToolchain(21)` alone → **JVM 21 bytecode** → consumers on JDK 8/11/17 and most Android toolchains excluded | Keep toolchain 21 for building; set `jvmTarget = 1.8` or `11` via `compilerOptions` for the jvm target (recommend **11**; decision D6) |
| B3 | 56 | `js(IR)` — the `IR` argument is legacy residue (IR is the only compiler in Kotlin 2.x) | `js {` |
| B4 | 55 | `@OptIn(ExperimentalKotlinGradlePluginApi::class)` on the js block — nothing in the block needs it (wasm's `@OptIn(ExperimentalWasmDsl)` is the needed one and is present) | Remove; re-add only if a genuinely experimental DSL member is used |
| B5 | 59 | `binaries.library()` on JS — this is for **npm-consumable JS library** output and implies a `@JsExport` strategy that doesn't exist (zero exports today → the produced JS package exposes mangled internals only) | Decide: either drop `binaries.library()` until an npm-distribution story exists (recommend), or adopt an `@JsExport` policy (heavy: name mangling, `Double`→`Number` surface, no default-arg interop) |
| B6 | 74 | `optIn("kotlin.RequiresOptIn")` — obsolete no-op since Kotlin 1.6 | Delete |
| B7 | 75 | `optIn("kotlin.experimental.ExperimentalNativeApi")` — **nothing in the code uses Native API**, and blanket-opting-in invites silent use of experimental API in a "pure common" library | Delete; opt in per-file if ever truly needed |
| B8 | 71-77 | `sourceSets.all { languageSettings { … } }` wrapper exists only for the two dead opt-ins | Delete whole block after B6/B7 |
| B9 | 38, 49-52 | `watchosArm32`, `androidNativeX86` are end-of-life-ish 32-bit tiers | Keep if zero maintenance cost is confirmed on current Kotlin; verify deprecation status at each Kotlin upgrade; drop at first friction |
| B10 | — | No binary-compatibility-validator (`org.jetbrains.kotlinx.binary-compatibility-validator`) | Add before first publish; baseline once math layer stabilizes |
| B11 | — | No lint (ktlint/detekt) — dialect spec (§5) needs mechanical enforcement | Add detekt + ktlint (or spotless); wire into CI. Custom detekt rule for "no file-level mutable state in commonMain" would directly enforce §3.1 forever. |
| B12 | 29-69 | 22 targets, code compiles on 0 | Keep the matrix (pure-common code makes it nearly free at *compile* level) but see R3 for CI tiering |

### 8.3 Root build / settings / properties

| # | Location | Finding | Fix |
|---|---|---|---|
| C1 | build.gradle.kts:7-10 + gradle.properties:25-26 | `group`/`version` defined in **two places** (`allprojects {}` block AND `gradle.properties`) | Keep gradle.properties only; delete the `allprojects` block |
| C2 | gradle.properties:6 + kite3d/build.gradle.kts:26 | Toolchain 21 declared twice (`org.gradle.toolchains.jvm.version=21` + `jvmToolchain(21)`) | Keep one (the DSL call); delete the property |
| C3 | settings.gradle.kts:1 vs build.gradle.kts:14 | `TYPESAFE_PROJECT_ACCESSORS` enabled but root uses string `project(":kite3d")` | Use `projects.kite3d` (or drop the feature preview) |
| C4 | settings.gradle.kts:22 | `rootProject.name = "kite3d-KMP"` vs module `kite3d`, Dokka `Kite3D`, POM `Kite3D` | Rename to `kite3d` |
| C5 | gradle.properties:17 | `kotlin.mpp.enableCInteropCommonization=true` — **zero cinterop in the project** (and core policy forbids it) | Delete (backends can add it in their own build) |
| C6 | gradle.properties:18 | `kotlin.mpp.stability.nowarn=true` — obsolete (MPP stable since 1.9.20) | Delete |
| C7 | gradle.properties:22 | `kotlin.js.yarn=true` — explicit statement of the default | Delete, or consciously migrate to npm and commit the lockfile either way |
| C8 | gradle.properties:2,10 | `-Xmx8g` build JVM + `-Xmx6G` Kotlin daemon for a 2-file project | Harmless; right-size when CI runners complain (4g/2g plenty for the math layer) |
| C9 | gradle.properties:30 | `RELEASE_SIGNING_ENABLED=true` with no key config → local `publishToMavenLocal` of release versions fails for contributors | Standard pattern: keep for CI, document `RELEASE_SIGNING_ENABLED=false` override for local, or gate on presence of signing keys |
| C10 | libs.versions.toml | Only 3 entries, but structure is right; versions (Kotlin 2.4.0 / Gradle 9.5.1 / Dokka 2.0.0 V2 mode / vanniktech 0.36.0 / foojay 1.0.0) are mutually consistent — **toolchain provably works** (it reached compilation) | Add BCV/detekt/kotlinx-benchmark entries as they arrive |
| C11 | kite3d/gradle.properties | `POM_NAME`/`POM_DESCRIPTION` present; artifactId defaults to module name `kite3d` ✔ | Fine as-is |
| C12 | README.md:4 | Badge says Kotlin 2.4.0 — keep in sync with toml (true today) | Consider a badge generated from the toml in CI |

### 8.4 Android note (README/build comment claim verification)

The build comment (kite3d/build.gradle.kts:20-23) claims Android target omission is "purely a
build-file change" — **verified plausible**: plain-JVM artifacts of a KMP library are consumable
from Android apps, and adding `androidTarget()`/`com.android.kotlin.multiplatform.library` later is
additive. But B2 (JVM 21 bytecode) currently *undermines* it: Android's D8 chokes on class-file
major 65 in older AGP. Fixing B2 to `jvmTarget 11` restores the claim.

---

## 9. Phase 2 execution plan (for Opus 4.8 — ordered, atomic, with acceptance criteria)

> Steps are ordered so the tree compiles from step 3 onward and every subsequent commit keeps it green.

**Step 0 — Repo bootstrap.** `git init`; write `.gitignore` (R2); initial commit of current state
*verbatim* (preserve the archaeology); create GitHub repo per POM URLs; push.
✅ *Accept:* `git log` has ≥1 commit; remote matches gradle.properties SCM entries.

**Step 1 — Build hygiene batch.** Apply §8.2/§8.3: B1 explicitApi, B2 jvmTarget 11, B3-B8 cleanups,
C1-C7 dedup/dead-flag removal, C4 rename. Add BCV + detekt/ktlint (B10, B11) with CI-ready configs.
One commit per logical group.
✅ *Accept:* `./gradlew help` clean; `apiDump` runs (empty-ish baseline OK pre-compile-fix… in
practice do apiDump after step 3); detekt passes.

**Step 2 — Write PORTING.md** = §5 dialect spec + §6 trap catalogue verbatim, as the normative doc.
Add `port-ledger.yaml` seeded with the full §11.1 inventory, all `pending` except Box2 `in-rework`.
✅ *Accept:* file exists; ledger lists all 29 upstream math files.

**Step 3 — Port `MathUtils` + tests.** First real dependency-order node. Apply trap rules
(clamp = max/min form §6.1; EPSILON §6.7; bitwise §6.5 for generateUUID; euclideanModulo §6.8).
✅ *Accept:* `:kite3d:jvmTest` green; ledger updated.

**Step 4 — Port `Vector2` + tests.** 56-member surface (§11.2). Decisions D3 (equals/hashCode/toString),
D5 (`fromBufferAttribute` via interface or omission), dialect rules throughout. Build the QUnit
transpiler (§7.1) here — Vector2's test file is big enough to pay for it immediately.
✅ *Accept:* all upstream Vector2 tests green on jvm + js + one native target; `Box2.kt` still
broken is *expected* (fixed next).

**Step 5 — Rewrite `Box2` per this audit + port its 24-block test suite.** Kill `_vector` (§3.1
inline forms), primary ctor, `val min/max` (D2), proper equals/hashCode/toString (D3),
`Iterable` param (§7.1/D4), remove `isBox2` (§3.6), fix inherited doc bugs (§4.2), KDoc degenerate
behaviors (§3.5), add additive tests (§7.1: empty-box distance +∞, −0.0 hash, concurrency smoke).
Fix TestConstants comment (§4.4).
✅ *Accept:* full test suite green on jvm/js/native; `apiDump` baseline committed; **the library
compiles for the first time** — tag it.

**Step 6 — Tooling.** `tools/port-ledger-check` in CI (§7.2); QUnit transpiler committed under
`tools/`; CI matrix per R3.
✅ *Accept:* CI red if ledger vs. code drifts.

**Step 7 — March the math layer in topological order**, one class+tests per commit:
`Vector3` (+`Quaternion` — mutually recursive, port as a pair) → `Vector4` → `Matrix2` → `Matrix3` →
`Matrix4` → `Euler` → `Box3` → `Sphere` → `Plane` → `Ray` → `Line3` → `Triangle` → `Frustum`+`FrustumArray` →
`Spherical`/`Cylindrical` → `SphericalHarmonics3` → `Color`+`ColorManagement` → `Interpolant` →
`interpolants/{Linear,Discrete,Cubic,Bezier,QuaternionLinear}`.
✅ *Accept per class:* upstream tests green; ledger `ported`; deviations recorded.

**Step 8 — Benchmarks + release.** kotlinx-benchmark module; baseline Vector3/Matrix4/Quaternion
hot ops; only then consider perf deviations. Publish `0.1.0` = complete math layer (Dokka site,
BCV baseline, CHANGELOG).

---

## 10. Open decisions (defaults chosen — Opus may proceed without asking)

| ID | Decision | Recommended default | Rationale |
|---|---|---|---|
| D1 | API philosophy: strict three.js mirror vs. Kotlin-first redesign | **Mirror core + `ext/` sugar files** | Preserves upstream-diff mapping, migration docs, and test parity; sugar (operators `+ - *`, `in`, destructuring, `Box2(…) {}` builders) costs nothing in separate files. Full immutable/value-class redesign **rejected**: three.js scene-graph semantics (shared `.position` references) require mutable identity objects; a redesign forfeits the entire "port three.js" premise. |
| D2 | `min`/`max` (& all similar members): `var` vs `val` | **`val`** | Upstream never internally reassigns; removes aliasing bug class; one test line adapts cleanly (§3.3). |
| D3 | equals policy | **Override `equals(Any?)`/`hashCode`(−0.0-normalized)/`toString` everywhere** | §3.2. The overload-only option ships a `==`≠`.equals` schism. |
| D4 | Collection params | **`Iterable<T>`** (+ `vararg` ext overloads) | §7.1. |
| D5 | `fromBufferAttribute` forward-dep | **Minimal `AttributeLike` interface in math** | Keeps Vector classes method-complete; core implements the interface later (§6.10). |
| D6 | JVM bytecode target | **11** (toolchain stays 21) | Android + JDK-11 LTS reach; 8 buys little in 2026. |
| D7 | `isBox2`-style flags | **Omit** | §3.6; revisit only if TSL-layer ported logic dispatches on them. |
| D8 | Precision strategy | **Double now; Float variants via codegen *only if* benchmarks demand, post-math-layer** | README policy + upstream test parity; dual-precision codegen (KSP/templates) is real but premature. |
| D9 | npm/JS distribution (`binaries.library()` + `@JsExport`) | **Defer; drop `binaries.library()`** | §8.2 B5; Maven klib publishing is unaffected. |

---

## 11. Appendices

### 11.1 three.js r184 math-layer inventory (port ledger seed; verified via GitHub contents API)

24 files + `interpolants/` (5): Box2 ✅(rework), Box3, Color, ColorManagement, Cylindrical, Euler,
Frustum, FrustumArray, Interpolant, Line3, MathUtils, Matrix2, Matrix3, Matrix4, Plane, Quaternion,
Ray, Sphere, Spherical, SphericalHarmonics3, Triangle, Vector2, Vector3, Vector4;
interpolants/{Bezier,Cubic,Discrete,Linear,QuaternionLinear}Interpolant.

Key dependency edges (verified from upstream imports): Vector2 → MathUtils(clamp);
Vector3 → MathUtils(clamp) + Quaternion (mutual pair with Euler/Matrix4 temps); Box2 → Vector2;
ColorManagement ↔ Color + Matrix3; Frustum → Plane/Sphere/Vector3; Ray → Vector3;
Triangle → Vector3/Plane/Box3.

### 11.2 Vector2 API surface (the exact prerequisite for Box2; extracted from upstream r184)

ctor(x=0,y=0); width/height get+set; set, setScalar, setX, setY, setComponent, getComponent, clone,
copy, add, addScalar, addVectors, addScaledVector, sub, subScalar, subVectors, multiply,
multiplyScalar, divide, divideScalar, min, max, clamp, clampScalar, clampLength, floor, ceil, round,
roundToZero, negate, dot, cross, lengthSq, length, manhattanLength, normalize, angle, angleTo,
distanceTo, distanceToSquared, manhattanDistanceTo, setLength, lerp, lerpVectors, equals, fromArray,
toArray, fromBufferAttribute (→D5), rotateAround, random (→ needs a seedable-or-injected random
decision for determinism in tests; upstream uses Math.random — port with kotlin.random.Random
default and note it), plus iterator/symbol members that don't port (JS `*[Symbol.iterator]` →
consider Kotlin `operator fun component1/component2` in ext).

**Subset Box2.kt actually calls today:** ctor(x,y), set, copy, clone(tests), add, sub, addScalar,
addVectors, subVectors, multiplyScalar, min, max, clamp, distanceTo, equals, negate(tests).

### 11.3 Overkill / underkill ledger (user's framing, condensed)

**Overdone:** 22 targets w/ 0 compiling code; Maven Central + signing + Dokka aggregation pre-code;
8G/6G heap; cinterop commonization flag with no cinterop; feature-preview enabled and unused;
`binaries.library()` with no exports; blanket Native-API opt-in.
**Underdone:** compilation; tests; git; CI; explicitApi; BCV; lint; PORTING spec; per-file rev pins;
KDoc for degenerate behaviors; threading contract.
**Done right (keep):** dual-license file; method-order-preserving port style; Double policy;
fresh-instance test constants; version catalog; Dokka V2 mode; no-runtime-deps stance; README's
layer/seam architecture (it matches three.js's actual `renderers/common/Backend` seam).

### 11.4 Rejected "revolutionary" options (so Phase 2 doesn't relitigate)

- **Immutable/value-class math types** — breaks three.js aliasing semantics the scene graph requires. Rejected (D1).
- **Struct-of-doubles Box2 (flatten min/max into 4 fields)** — breaks `.min`/`.max` object shape
  every upstream file and consumer touches; hot paths live in BufferAttribute/Matrix4 anyway.
  Rejected until benchmarks say otherwise.
- **SIMD / JVM Vector API / Multik** — JVM-only or dep-adding; violates zero-dep common core. Rejected.
- **Auto-transpiling the whole of three.js** — the per-file dialect is too semantic for full
  automation (§6 traps); but the *test* corpus transpiler (§7.1) and *port ledger* (§7.2) capture
  the automatable 80%. Adopted in reduced form.

### 11.5 Evidence trail

- Build failure log: `./gradlew :kite3d:compileKotlinJvm` (2026-07-05) — §2 excerpt, 25 errors.
- Upstream fetches (r184 tag): `src/math/Box2.js` (381 lines), `test/unit/utils/math-constants.js`
  (26 lines), `test/unit/src/math/Box2.tests.js` (420 lines), `src/math/Vector2.js` (870 lines),
  `src/math/Vector3.js` (1263 lines), contents API listing of `src/math/`.
- `git rev-parse --is-inside-work-tree` → "fatal: not a git repository".
- Source-set listing: `commonMain`, `commonTest` only.

---

# Phase 2 — Execution Record (Opus 4.8, piloted by this audit)

> Executed 2026-07-05 in one session. The repo went from **one broken file that did not compile**
> to the **entire three.js `r184` `src/math` layer ported to common Kotlin**, compiling on all 22
> targets and green on three engines. What follows is the record so the next phase (core / scene
> graph) starts from ground truth.

## Outcome vs. the 0%→100% scorecard

| Dimension | Then | Now |
|---|---|---|
| Compiles | 0% (proven failure) | **100%** — 22/22 targets `assemble`; the `math` layer is complete |
| Port fidelity | 85% (of one file) | **~100%** for `src/math` — every class ported against upstream code, faithful op-order |
| Kotlin idiomaticity | 15% | **high** — primary ctors, no `this.` noise, `Iterable`, enums for orders/spaces, no duck-type flags |
| Concurrency correctness | 0% (file-level `_vector` race) | **fixed** — zero file-level mutable scratch; a JVM `ConcurrencyTest` guards it |
| Tests | 0% | **447 test methods** (ported upstream suites + Kite3D-only guards), green on jvm / macosArm64 / js(node) |
| API hygiene | 20% | `explicitApi()` strict; `equals`/`hashCode`(-0.0)/`toString` on every value type |
| Build hygiene | 45% | dead flags/dups removed, jvmTarget 11, rename, version catalog |
| Repo/process | 5% | git repo + 7 commits, `.gitignore`, CI matrix, CONTRIBUTING, PORTING.md, machine ledger |

## What was delivered

- **Full `src/math` port** (30 commonMain files): `MathUtils`, `Vector2/3/4`, `Matrix2/3/4`,
  `Quaternion`, `Euler`, `Box2/3`, `Sphere`, `Plane`, `Ray`, `Line3`, `Triangle`, `Frustum`,
  `FrustumArray`, `Spherical`, `Cylindrical`, `SphericalHarmonics3`, `Color`, `ColorManagement`,
  `Interpolant` + `Linear/Discrete/Cubic/QuaternionLinear/Bezier` interpolants, plus the
  `AttributeLike` seam (D5).
- **Every upstream test suite ported** to `kotlin-test` (28 test files, 447 `@Test`), applying the
  transcendental-tolerance rule discovered in execution (V8/JVM/native `libm` differ ~1 ulp, so
  transcendental results assert with tolerance; algebraic results stay exact).
- **Kite3D-only guards** the audit asked for (§7.1): `MathContractTest` (empty-box `+∞` clamp canary,
  `-0.0` hashCode normalization, `NaN` inequality, `==`⇔`.equals` agreement) and a JVM
  `ConcurrencyTest` proving the no-shared-scratch invariant under contention.
- **Scaffolding**: `PORTING.md` (the normative dialect), `port-ledger.yaml` (per-file status +
  deviations), GitHub Actions CI (linux/apple/windows matrix), CONTRIBUTING.

## The five top findings — resolved

1. **[BLOCKER] Did not compile** → the whole layer compiles on 22 targets; `Vector2`/`Vector3` exist and are complete.
2. **[BLOCKER] File-level `_vector` race (§3.1)** → policy applied library-wide: temps inlined as
   component math or localized; **zero** file-level mutable state; `ConcurrencyTest` locks it.
3. **[HIGH] `equals` overload schism (§3.2)** → `equals(Any?)`/`hashCode`(-0.0 normalized)/`toString`
   overridden on every value type; `MathContractTest` asserts `==`⇔`.equals`.
4. **[HIGH] `coerceIn` clamp trap (§6.1)** → all clamps go through `MathUtils.clamp` (`max(lo,min(hi,x))`);
   the empty-box `distanceToPoint == +∞` canary passes on all three engines.
5. **[HIGH] No git repo (§8.1)** → `git init` + `.gitignore` + 7 structured commits (baseline preserved).

## Decisions taken during execution (beyond the audit's D1–D9)

- **Enums replace JS string/int constants**: `EulerOrder`, `ProperEulerOrder`, `CoordinateSystem`
  (WebGL/WebGPU), `ColorSpace`, `ColorTransfer`, `ComponentType`, `InterpolantEnding` — exhaustive
  `when`, so upstream's unreachable "unknown order/system" `throw`/`warn` branches are dropped.
- **`slerp`** ported faithfully to r184 (the `acos`/`0.9995` form — NOT the older `cosHalfTheta`
  version; verified against source, not memory).
- **Color transfer tests** use upstream's `numEqual` tolerance (0.1); the sRGB pair is byte-identical
  to r184 (truncated `0.41666` exponent → the pair is not an exact inverse; ~2e-6 round-trip).
- **Method / arg overloads** replace JS `x.isFoo` runtime dispatch (`Matrix4.setPosition(Vector3)` +
  `(x,y,z)`, etc.).

## Deferred (need not-yet-ported layers — tracked in `port-ledger.yaml`)

- `Vector3.project`/`unproject` (core `Camera`) — ship as ext fns in the camera layer.
- `Box3.setFromObject`/`expandByObject`, `Frustum(Array).intersectsObject`/`intersectsSprite`
  (core `Object3D`/geometry).
- `Color` CSS-string (`setStyle`/`getStyle`) + X11 named-color table — likely an `ext/` CSS module.
- `Box3.setFromBufferAttribute` takes an explicit `count: Int` until the real `BufferAttribute` (with
  `.count`) implements `AttributeLike`.

## Remaining follow-ups (not blockers; deliberately not done this session)

- **BCV + detekt/ktlint** — audit B10/B11. Deferred: the public API will churn heavily as core/geometry/
  renderer layers land, so a binary-compat baseline now is premature (audit itself says "before first
  publish"). Add when the API stabilizes.
- **Push to a remote** — `git init` and local commits done; POMs point at `github.com/yuroyami/Kite3D`,
  but creating/pushing the remote is left to the owner (not done without explicit ask).
- **Dokka `sourceLink` + module docs** — add once the remote exists.
- **kotlinx-benchmark** — audit step 8; correctly premature until there's perf-motivated work.

## Next phase (core / scene graph) — entry notes

- The seam is ready: `AttributeLike` is the read seam the real `BufferAttribute` implements; the
  `math` layer takes no `core` type. `CameraLike`/`Object3D` methods listed above get restored as the
  core types land, then their skipped tests re-enabled.
- Keep porting under `PORTING.md` and update `port-ledger.yaml` per file; a class isn't "ported"
  until its upstream tests are green on jvm + one native + js.
