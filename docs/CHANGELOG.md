# Changelog

## 1.0.0 — Stable fixed-projector release

First stable Mirage Projector release for Minecraft 1.21.1 / NeoForge 21.1.244+.

### Projectors

- Six canonical chassis: Mirage Projector, Mirage Display, Mirage Field Projector, Wide Mirage Projector, Tall Mirage Projector and Mirage Prism.
- State-preserving projector upgrade crafting.
- Final canonical models, VoxelShapes, inventory/held/dropped presentation and fixed projector ON/OFF UX.
- Active source and workspace navigation are independent; `TURN OFF` preserves configured state.

### Projection sources

- Image/GIF projection with server/client content-addressed asset synchronization.
- Item virtual snapshots.
- Banner virtual snapshots and Prism cardinal faces.
- Frozen Entity Scan Cards with Player/generic living/Humanoid/Horse equipment support, pose presets, nameplates and per-channel visibility.

### Presentation / power

- Scale, Lift, rotation, Float, tint, Ghost/opacity, Fullbright/world lighting and source-specific visual controls.
- Projection Cores with PU-based capacity and dynamic feasible slider limits.
- Overdrive behavior when sufficient PU is available.
- Core Booster with Glass/Quartz/Amethyst/Diamond/Netherite loaded states and ×1.50 Core amplification.

### Crying Obsidian / Mirage Light

- Crying Obsidian Shards and shard-based Crying Obsidian crafting.
- Renewable Small → Medium → Large → Cluster growth with lava above Crying Obsidian.
- Silk Touch stage preservation; fixed shard drops without Fortune multiplication.
- Material-specific Core Booster Beacon identities.
- Server-authoritative virtual Mirage Light Engine for energized Mature Clusters.
- Exact open-space half-decay, vanilla shape/opacity occlusion, obstacle-detour decay, overlap-by-maximum aggregation and revisioned chunk synchronization.

### Compatibility / extensibility

- Stable namespaced projection-source IDs and versioned settings serialization.
- Source content separated from `ProjectionTransform` presentation state.
- Source/content and client render provider registries.
- Quaternion-ready persisted orientation.
- Generic `ProjectionEnergySource` boundary for future non-Core devices.
- Legacy source-ID, Improved Core and physical Mirage-light migration paths retained where needed.

### Recipe viewers / packaging

- Optional EMI and JEI integrations.
- EMI crafting coverage for all projector upgrades and Crying Obsidian recipes.
- EMI icon-only Crying Obsidian World Interaction tutorial and age-ordered Block Drops.
- JEI projector crafting extension and ingredient guidance.
- Final mod logo/metadata integration.
- Public handbook/documentation cleanup and release audit.

Development-version chronology is preserved in `docs/history/development-notes/CHANGELOG-DEVELOPMENT.md`.
