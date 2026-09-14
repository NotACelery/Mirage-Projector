# Changelog

## 1.0.7 — Rechargeable Glow Dust foundation

- Added Glow Dust as a charge-bearing item with persistent partial charge, depleted naming, tooltip percentage/status and an inventory charge bar.
- Reused the vanilla Glowstone Dust silhouette with a charge-driven tint so depleted dust becomes visibly dull without requiring a second item ID.
- Added a dedicated Glow Dust charging cradle to Core Boosters without replacing their existing Core material socket.
- An active Beacon beam recharges one inserted Glow Dust per Core Booster; a clear vertical path supports at most five simultaneous charging cells.
- Each actively charging Glow Dust removes 20 percentage points from the outgoing Beacon beam, and the custom Beacon renderer shows the attenuation.
- Crying Obsidian optics now see the attenuated beam, so charging cells and crystal transmission compose instead of behaving as unrelated systems.
- Glow Dust charge state persists through save/reload, extraction and block-entity synchronization.
- No survival recipe or lantern consumer is committed yet; Glow Dust is currently exposed for Creative/QA while the 1.1 portable-device progression is still being built.
- Protocol remains 28 and ProjectionSettings remains format 3.

## 1.0.6 — Prism carousel compaction and four-tab UI

- Capped user-controlled Prism Distance at +160 px (10 blocks) beyond the collision-safe baseline.
- Reworked Prism collision spacing per adjacent face pair instead of using the single largest face for the whole carousel.
- At +0 px and outward Tilt, adjacent lower face edges can meet without overlapping, producing a compact carousel instead of mostly empty radial space.
- Inward Tilt increases only the collision-safe minimum actually required; unsupported inward angles are refused if they would need more than the +160 px spacing budget.
- Kept Prism Rotation as a true carousel around the machine center and retained the small Prism Distance PU surcharge.
- Merged Appearance controls into Geometry, reducing the fixed settings area to four tabs: Geometry, Placement, Rotation and Floating.
- Refined Prism clearance/culling reach so inward and outward Tilt are no longer treated as the same radial expansion.
- Preserved the 1.0.5 Dynamic Mirage Light foundation; protocol remains 28 and ProjectionSettings remains format 3.

## 1.0.5 — Dynamic Mirage Light foundation

- Activated the client-side `DYNAMIC_VISUAL` runtime for future moving Mirage light emitters.
- Added moving-source snapshots with update cadence, camera culling and stale-source cleanup.
- Added directional-cone solving for future Focus/Flood-style portable lighting.
- Kept dynamic fields completely separate from the server-authoritative STATIC_WORLD publication channel.
- Added reusable dynamic profile factories and stable entity/device source identities.
- Added automatic retry for dynamic solves clipped by temporarily unavailable chunks.
- No lantern, Glow Dust battery or portable projector gameplay is exposed yet.
- Protocol remains 28 and ProjectionSettings remains format 3.

## 1.0.4 — Fixed-tab UI and placement polish

- Consolidated vertical placement into Lift; the experimental signed Vertical Offset control is removed.
- Reworked Mirage Prism Distance so +0 px means the tight collision-safe four-face baseline, with Tilt increasing the minimum only when required.
- Removed the extra one-pixel Prism spacing margin so adjacent face boundaries may meet without overlapping.
- Reorganized Geometry, Placement, Rotation, Floating and Appearance into a fixed settings-tab area.
- Reduced the main projector panel height so 1920×1080 at GUI Scale 2 no longer enters responsive overflow.
- Kept Prism carousel rotation and the small radial-distance PU cost.
- Protocol remains 28 and ProjectionSettings remains format 3; positive legacy Vertical Offset values are absorbed into Lift and the legacy slot is sanitized to zero.

## 1.0.3 — Renderer compile hotfix

- Fixed a renderer compile regression introduced during the 1.0.2 Prism-placement pass.
- Restored the missing projection base-height local in multi-source image layouts.
- Removed a duplicate projection base-height declaration in the single-image renderer.
- No gameplay, save-format, placement, PU or protocol behavior changed from 1.0.2.

## 1.0.2 — Prism placement correction

- Removed independent Horizontal Offset from projector controls.
- Restricted Distance Offset to Mirage Prism Image/Banner projection, where it expands the four-face cross without scaling content.
- Added collision-safe automatic minimum Prism spacing based on projected face size and Tilt.
- Extended Tilt to the full ±90° range.
- Preserved Prism Rotation as a carousel around the projector center instead of rotating each face around its own displaced center.
- Added a light PU cost for Prism radial separation; required base collision spacing remains free while extra/manual or Tilt-driven separation costs modest additional power.
- Kept `ProjectionSettings` format 3 / protocol 28; the legacy horizontal-offset wire/NBT slot is retained but always sanitizes to zero.

## 1.0.1 — Advanced projection placement

- Introduced the first advanced-placement foundation with Vertical/Distance storage and quaternion-backed Tilt.
- Added initial Tilt support for high-mounted displays; its final range/collision behavior is refined in 1.0.2.
- Added Reset Position and Reset Tilt controls.
- Added placement-aware rendering, clearance and culling groundwork across existing projection sources.
- Updated projection clearance, front/back image selection and render culling to follow displaced/tilted projections.
- Bumped `ProjectionSettings` network format to 3 and protocol to 28; 1.0.0 saves default cleanly to zero offsets and identity tilt.
- Expanded the main projector panel while retaining the responsive scrollbar behavior introduced before the initial release.

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
- Responsive vertical scrolling for projector GUIs on constrained GUI heights.

Development-version chronology is preserved in `docs/history/development-notes/CHANGELOG-DEVELOPMENT.md`.
