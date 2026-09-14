# Mirage Projector 1.0.5 — Dynamic Mirage Light Foundation

1.0.5 begins the mobile-light groundwork planned for the 1.1.0 feature expansion without adding lantern items or Glow Dust gameplay yet.

## Delivered foundation

- `DYNAMIC_VISUAL` is now an actual client runtime path rather than only a reserved enum value.
- Moving emitters can submit `MirageDynamicLightSnapshot` samples containing stable source identity, world position, profile, update cadence, cull distance and stale timeout.
- `ClientDynamicMirageLightManager` owns client-side lifecycle, camera culling, stale-source cleanup, solve cadence and render-section invalidation.
- Dynamic sources are solved into the local Mirage aggregate only; they never enter the authoritative server `STATIC_WORLD` publication channel.
- Directional cone geometry is implemented in the shared voxel solver.
- Dynamic profile factories expose omnidirectional and directional-cone shapes without baking lantern balance values into the engine.
- Entity UUID and explicit-key source IDs are available for future player/entity/device attachments.
- Incomplete dynamic fields automatically retry when their required chunks become queryable.

## Intentionally not included yet

- lantern items;
- Focus/Flood/Ambient user controls;
- Glow Dust charge storage;
- remote-player/device networking;
- battery drain;
- portable projector rendering.

Those consumers will bind to this foundation in later 1.0.x implementation snapshots. The second version component moves to 1.1.0 only when the planned feature set is complete.

## Compatibility

- Network protocol remains 28.
- `ProjectionSettings` format remains 3.
- Existing static Mature Cluster / Beacon Mirage Light behavior is unchanged.
- Existing worlds/saves require no migration for this foundation.
