# Kite3D

![status](https://img.shields.io/badge/status-pre--alpha%20(math%20layer)-orange)
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

Porting in dependency order. **Current: the `math` layer.**

| Layer | three.js src | State |
|-------|--------------|-------|
| math  | `src/math`   | porting |
| core / scene graph | `src/core`, `src/objects`, ... | next |
| geometry | `src/geometries` | next |
| TSL nodes | `src/nodes` | later |
| renderer core + backend | `src/renderers/common` + cinterop | later |

Numeric note: three.js math uses JS `number` (IEEE-754 double). Kite3D math
classes use Kotlin `Double` to match three.js semantics and its unit-test
expectations exactly. GPU buffers use `Float` further down the stack.

## License

MIT. Port copyright © 2026 yuroyami; original three.js © 2010-2026 three.js authors.
