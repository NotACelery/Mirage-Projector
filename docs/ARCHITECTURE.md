# Architecture — Mirage Projector 1.0.0

This document describes the stable architecture of the 1.0.0 fixed-projector release. Historical implementation notes live under `docs/history/`.

## 1. State ownership

Projector state is deliberately split into independent layers:

- **device state** — chassis, enabled/disabled state, installed Core;
- **source identity/content** — Image, Item, Entity, Banner or future registered source;
- **presentation state** — scale, lift, orientation, float, tint, ghost/opacity, lighting options;
- **source-specific workspace state** — image banks/faces, virtual item/banner snapshots, Entity scan/equipment state;
- **power state** — current energy provider and feasible limits.

This separation prevents workspace navigation, source selection and projector power state from overwriting one another.

## 2. Projection sources

Built-in sources are identified by stable namespaced IDs:

- `mirage_projector:image`
- `mirage_projector:item`
- `mirage_projector:entity`
- `mirage_projector:banner`

`ProjectionSourceRegistry` owns common source definitions/content semantics. `ProjectionSourceRenderRegistry` owns client render dispatch. Source IDs are serialized as strings, not enum ordinals.

Unknown registered source IDs and opaque payloads are preserved where possible so missing addons do not silently destroy saved content.

## 3. ProjectionTransform

Source content does not own presentation settings. `ProjectionTransform` is the source-agnostic presentation contract and includes:

- scale;
- lift;
- orientation/rotation data;
- float settings;
- tint;
- ghost/opacity;
- related visual controls.

Persisted orientation is quaternion-ready. Current 1.0.0 UI still exposes the established controls, but later direct-manipulation work can use the same serialized transform instead of rewriting every source format.

## 4. Chassis capabilities

Chassis-specific limits and compatibility are centralized rather than scattered through source code. `ProjectionChassisProfile` provides the fixed-projector capability/power envelope and source-compatibility seam.

The six canonical chassis are:

- Mirage Projector
- Mirage Display
- Mirage Field Projector
- Wide Mirage Projector
- Tall Mirage Projector
- Mirage Prism

## 5. Projection power

`ProjectionPower` is the authority for:

- effective capacity;
- per-component cost;
- Overdrive;
- dynamic feasible slider limits.

`ProjectionEnergySource` separates generic energy consumption from the fixed-projector Core implementation. Current projectors adapt their installed `ProjectionCoreProfile`; future portable devices can provide a different backend without pretending to have a Core socket.

## 6. Persistent projector items / upgrade crafting

Projector block items can carry serialized machine state. Upgrade crafting uses the custom `projector_upgrade` recipe serializer and transfers source/content/transform/Core state into the target chassis rather than replacing the machine with a blank block.

The canonical progression is:

```text
Mirage Projector → Mirage Display → Wide / Tall / Prism / Field
```

## 7. Image/GIF asset pipeline

Imported image data is normalized and content-addressed. Multiplayer transfer is server-mediated rather than assuming clients share a filesystem.

The pipeline handles:

- import/normalization;
- content hash identity;
- server-side asset ownership/cache;
- client request/response;
- local client decode/cache;
- animated GIF frame playback.

See `ASSET-PIPELINE.md`.

## 8. Entity snapshots

Entity Scan Cards contain frozen projection data rather than a live Entity reference. `EntityScanData` owns captured identity/state while `EntityProjectionState` and `VirtualEquipmentSnapshots` own projector-facing pose/equipment configuration.

Entity rendering uses conservative bounds shared by preview/clearance/world-culling logic. Passenger/vehicle composites are intentionally rejected until a dedicated relative-transform format exists.

See `ENTITY-AND-SNAPSHOTS.md`.

## 9. Rendering

`MirageProjectorRenderer` coordinates chassis anchors and source rendering but does not own a closed source switch. Registered source renderers handle their source family.

Ghost Entity rendering uses a late world-render pass where required to avoid water/cloud/deferred-composition holes. Image/Banner/Item ghost paths retain their own color/alpha rendering semantics. Third-party renderer compatibility is handled only where a concrete renderer requires a safe adapter.

## 10. Mirage Light Engine

Static Mature Crying Obsidian Cluster lighting uses a server-authoritative virtual light field.

Key properties:

- causal six-neighbour propagation;
- fixed-point half-decay in open space;
- vanilla destination opacity and face-shape occlusion;
- obstacle-detour extra cost;
- overlap by maximum contribution;
- chunk/dependency-window awareness;
- atomic publication;
- revisioned chunk snapshots to clients.

Clients combine Mirage and vanilla block light at read time:

```text
max(vanilla, Mirage)
```

Virtual Mirage light is never fed back into vanilla propagation as a new emitter.

`STATIC_WORLD` and `DYNAMIC_VISUAL` are separate lifecycles. Moving future light sources must not rebuild static gameplay-light sections every frame.

See `MIRAGE-LIGHT-ENGINE.md`.

## 11. Optional recipe viewers

EMI and JEI integrations are optional compile/runtime integrations. Their plugin classes are not required by the core gameplay path.

- EMI receives explicit custom projector-upgrade recipes plus Crying Obsidian World Interaction/Block Drops presentation.
- JEI receives a crafting-category extension for custom projector upgrades plus ingredient information.

The mod must load correctly with neither viewer present.

## 12. Migration compatibility

The release retains compatibility shims only where old serialized worlds require them:

- `crying_light_node` physical relay block — migration-only/self-cleaning;
- historical `improved_*_core` blocks and `improved_core` block entity — migration-only;
- legacy numeric projection-source IDs — migrated to namespaced source IDs.

Migration IDs are not gameplay products and must not receive recipes, BlockItems or Creative exposure.
