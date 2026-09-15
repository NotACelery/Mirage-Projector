# Entity Projection and Snapshots — 1.0.0

## Snapshot model

Entity Scan Cards store frozen visual data rather than references to a live ticking entity. The projector reconstructs a client-side visual representation from serialized scan data.

The snapshot contract keeps:

- entity type / captured identity data;
- supported Player skin/model-part data;
- custom name/nameplate data;
- supported species-specific visual state;
- Humanoid equipment/held items where applicable;
- Horse Saddle / Body Armor state;
- pose selection;
- per-channel projected-equipment visibility.

The original entity is never stored inside the projector.

## Virtual equipment

`VirtualEquipmentSnapshots` owns projector-facing equipment copies. Incoming and Projected channels are virtual snapshots rather than containers that hold the real source stack.

Hiding a projected equipment channel suppresses rendering only. The saved snapshot remains intact and can be shown again immediately.

## Supported body modes

Current 1.0.0 behavior covers:

- generic living entities;
- Player reconstruction;
- Humanoid equipment rigs;
- bodyless equipment presentation where supported;
- Horse-specific Saddle / Body Armor channels.

Passenger/vehicle composites remain intentionally rejected because 1.0.0 does not define a stable relative-transform/composite ownership format.

## Pose / nameplate state

Supported pose presets are stored as projector presentation state. Custom names/nameplates are frozen from scan state rather than following a live entity after capture.

## Bounds and culling

Preview fitting, clearance checks and world-render culling use the same conservative pose/species-aware bounds authority. The envelope reserves room for visible held items, armor/head equipment and Horse Body Armor so displaced or overdriven projections are not clipped by generic body dimensions.

The renderer AABB includes both the chassis and projected content/nameplate so normal vanilla frustum culling can remain enabled.

## Ghost rendering

Semi-transparent Entity projections use a late rendering path where needed so already-composited world elements such as water/clouds do not overwrite the hologram afterward.

Image/Banner/Item ghost rendering retains source-appropriate color/alpha behavior. Entity rendering may use projection-local depth writes during the late pass to preserve body/equipment surface ordering.

## Third-party renderer safety

Mirage prefers a safe fallback over mutating shared renderer state in unsupported ways. Third-party compatibility adapters should be added only for concrete renderer failures that cannot be handled by the generic projection path.

Create Netherite Backtank remains a regression target because its chest rendering uses custom layered/synthetic surfaces. At full opacity, Create keeps its native rendering; Ghost mode applies Mirage's compatibility handling only to the projected copy.

## Persistence

Entity scan data, pose state and virtual equipment visibility are serialized with the projector/card state and must survive:

- save/reload;
- chunk unload/reload;
- normal state-preserving projector upgrades;
- source workspace navigation.

Clearing a virtual snapshot must not delete or duplicate a real inventory stack.

## Scan Codex library layer

Starting in 1.0.17, the Mirage Scan Codex stores multiple independent canonical scan roots in server Overworld SavedData. The physical Codex ItemStack stores only a stable library UUID and selected scan UUID. Search/favorites/category UI works from lightweight metadata summaries; full frozen entity data stays server-authoritative until a later system such as the Duplicating Lectern explicitly requests one exact scan root.

Repeated scans of the same entity type are intentionally separate. The scan UUID, not the entity registry ID, is the library identity.
