# Mirage Projector 1.0.1 — Advanced Projection Placement

Minecraft: **1.21.1**
NeoForge: **21.1.244+**
Network protocol: **28**
ProjectionSettings format: **3**

## Scope

1.0.1 extends the existing fixed-projector presentation transform without adding a new content source or power system.

- **Horizontal** — signed left/right offset in projector-local pixels.
- **Vertical** — signed fine vertical offset in projector-local pixels.
- **Distance** — signed forward/back offset in projector-local pixels; this is the control that moves a projection away from the projector without scaling it.
- **Tilt** — ±89° up/down orientation using the persisted quaternion.
- **Reset Position** — zeroes Horizontal/Vertical/Distance.
- **Reset Tilt** — restores identity orientation.

Offsets are relative to the projector facing, not global world axes. They are applied before animated yaw so enabling Rotation does not make an offset projection orbit the chassis.

## Source coverage

The shared renderer placement path is used by:

- Image/GIF plane rendering;
- Wide/Tall multi-source image layouts;
- Prism image faces;
- Item/block projections;
- Banner projections;
- Entity projections.

Entity nameplates follow translated placement while remaining camera-facing for readability.

## Clearance / culling

Projection clearance scans use the displaced projection center and a conservative Tilt expansion. BER render bounding boxes union the physical projector with the displaced projection envelope so moving a hologram far from the block does not make it disappear when the chassis leaves the frustum.

## Compatibility / migration

1.0.0 save data does not contain the three offset fields. Missing fields load as zero. The quaternion fields already existed in 1.0.0 and default to identity, so an untouched 1.0.0 projector loads into 1.0.1 at exactly its previous placement.

The network codec accepts format 2 and writes format 3. The network protocol is 28 because the wire payload now includes three signed placement offsets.

## Power semantics

1.0.1 does **not** add PU cost for placement distance. Placement is treated as presentation QoL. Any future balance rule tying physical displacement to energy cost must be designed explicitly rather than emerging accidentally from this patch.

## QA focus

- Test all four built-in sources with non-zero Horizontal/Vertical/Distance.
- Test ±Tilt with high-mounted Image/GIF projections.
- Test Rotation ON with non-zero offsets; the projection must spin in place at its displaced center, not orbit the projector.
- Test Prism and Wide/Tall multi-source layouts.
- Test Reset Position and Reset Tilt.
- Test large offsets near blocks and verify clearance preview follows the projection.
- Test frustum behavior with the projector itself off-screen but the displaced projection visible.
- Load a 1.0.0 world and confirm existing projectors have unchanged placement.
