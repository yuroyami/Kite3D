# Kite3D

![status](https://img.shields.io/badge/status-alpha%20(math%20layer%20ported)-orange)
![kotlin](https://img.shields.io/badge/Kotlin-2.4.0-7F52FF?logo=kotlin&logoColor=white)
![license](https://img.shields.io/badge/license-MIT-blue)

**One pure-Kotlin 3D engine for Kotlin Multiplatform. A from-scratch port of [three.js](https://github.com/mrdoob/three.js) — scene graph, math, geometry and shading — from `commonMain`, with pluggable GPU backends.**

Kite3D follows the KITE lineage (KiteQR, KiteTorrent, KitePDF, KiteCodec): take a
beloved library from another language and bring it to Kotlin Multiplatform behind
one coherent API.

## Architecture

three.js (r184) already splits along the seam Kite3D needs:

- **Pure core** (`:kite3d`) — math, core scene graph, geometry, animation, and the
  TSL shader graph. Line-for-line port into common Kotlin. No `expect`/`actual`,
  no cinterop, no native binary. Runs on every Kotlin target.
- **Backend** (`:kite3d-backend-*`, later) — the GPU submission seam. three.js
  abstracts this as `renderers/common/Backend`. The native backend binds
  `wgpu-native` via cinterop (one C API → Metal / Vulkan / D3D12); the web backend
  interops with browser WebGPU.

## Status

Porting in dependency order. **The `math` layer is ported.** Every class in
three.js `src/math` is now common Kotlin, with the upstream unit-test suites
translated to `kotlin-test` and green on the JVM, native (macOS arm64) and JS
(Node). See [port-ledger.yaml](port-ledger.yaml) for the per-file record and
[PORTING.md](PORTING.md) for the porting dialect.

| Layer | three.js src | State |
|-------|--------------|-------|
| math  | `src/math`   | **ported** — vectors, matrices, quaternion, euler; box/sphere/plane/ray/line3/triangle; frustum; spherical/cylindrical; color; interpolants |
| core / scene graph | `src/core`, `src/objects`, ... | next |
| geometry | `src/geometries` | later |
| TSL nodes | `src/nodes` | later |
| renderer core + backend | `src/renderers/common` + cinterop | later |

A few `math` methods that reach into not-yet-ported layers are deferred (tracked
in the ledger): `Box3.setFromObject`, `Frustum.intersectsObject` (need the core
scene graph), `Vector3.project`/`unproject` (need a `Camera`), and `Color`'s
CSS-string / named-color parsing.

Numeric note: three.js math uses JS `number` (IEEE-754 double). Kite3D math
classes use Kotlin `Double` to match three.js semantics and its unit-test
expectations exactly. GPU buffers use `Float` further down the stack. Because
`sin`/`cos`/`exp`/… are not bit-identical across JS, JVM and native `libm`, tests
of transcendental results use tolerances; pure algebraic results are asserted exactly.

## License

MIT. Port copyright © 2026 yuroyami; original three.js © 2010-2026 three.js authors.
