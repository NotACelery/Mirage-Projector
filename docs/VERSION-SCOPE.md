# Version Scope

## 1.0.7 — Rechargeable Glow Dust foundation

1.0.7 keeps protocol 28 and `ProjectionSettings` format 3. Glow Dust is now a persistent charge-bearing item with visible depleted/partial state, a Core Booster charging cradle and active Beacon recharge. Each actively charging cell subtracts 20 percentage points from the outgoing beam; on an otherwise clear column the fifth cell is the last one that can receive power. Charging attenuation is shared by the visual Beacon renderer and Crying Obsidian optics. The item has no committed survival recipe yet and no lantern consumes it yet; those progression/device pieces remain part of the 1.1.0 feature set.

## 1.0.6 — Prism compaction and four-tab settings UI

1.0.6 keeps protocol 28 and `ProjectionSettings` format 3. Prism Distance is now a bounded +0..+160 px expansion above a per-adjacent-face collision floor instead of a 1024 px-scale radial control. Positive/outward Tilt keeps the lower face edges at the tight baseline; negative/inward Tilt grows only the required safe radius and is limited if the ten-block extra-distance budget cannot prevent overlap. Appearance controls are merged into Geometry, leaving four fixed tabs while Source Workspaces, PU and inventory remain anchored. The 1.0.5 Dynamic Mirage Light foundation remains intact.

## 1.0.5 — Dynamic Mirage Light foundation

1.0.5 begins implementation of the future 1.1 mobile-light stack while remaining an incremental `1.0.x` snapshot. `DYNAMIC_VISUAL` now has a real client runtime, directional-cone solving and reusable moving-source lifecycle/culling primitives. No lantern, Glow Dust battery or portable-projector gameplay is exposed yet. Protocol remains 28 and `ProjectionSettings` format remains 3.

## 1.0.4 — Fixed-tab UI and placement consolidation

1.0.4 keeps protocol 28 and `ProjectionSettings` format 3 while correcting the maintenance-line placement/UI model: Lift becomes the only vertical placement control, Prism Distance is displayed as extra separation above the exact collision-safe baseline, and Geometry/Placement/Rotation/Floating/Appearance share a fixed tabbed options area. This keeps source workspaces, PU information and inventory anchored while greatly reducing unnecessary responsive overflow.

## 1.0.3 — Renderer compile hotfix

1.0.3 repaired the renderer compiler errors discovered by the first Windows build of the Prism-placement pass. It did not intentionally change gameplay, placement, saves, PU or protocol behavior.

## 1.0.2 — Prism-aware fixed-projector placement

1.0.2 corrects the placement model introduced in 1.0.1:

- the earlier experimental Vertical Offset is superseded by Lift as the sole vertical placement axis;
- Tilt expands to ±90°;
- Horizontal Offset is removed as a user-facing control;
- Distance becomes a Mirage Prism-only radial spacing control for Image/Banner faces;
- Prism spacing automatically respects collision-safe minimums and adds a small PU surcharge beyond base spacing;
- Prism Rotation remains a carousel around the projector center.

## 1.0.1 — Advanced fixed-projector placement

1.0.1 is a bounded QoL/placement patch on top of the 1.0.0 fixed-projector release:

- projector-local Horizontal / Vertical / Distance offsets independent from Scale;
- Tilt using the already persisted quaternion orientation;
- Reset Position / Reset Tilt controls;
- displaced/tilted clearance, front/back selection and render-culling support;
- `ProjectionSettings` format 3 / protocol 28 with clean 1.0.0 defaults.

It does not introduce portable devices, batteries, new projection-source families or the 1.1.0 gameplay expansion.

## 1.0.0 — Stable fixed-projector release

1.0.0 contains the complete fixed-projector system:

- six canonical projector chassis;
- Image/GIF, Item, Banner and Entity sources;
- Projection Cores, Core Boosters, Overdrive and dynamic slider limits;
- frozen Entity Scan Cards and virtual equipment state;
- server-synchronized image/GIF asset pipeline;
- Crying Obsidian growth, shards, Beacon relay behavior and Mirage Light;
- optional EMI/JEI recipe-viewer integration;
- stable source-ID/provider/transform/energy extension seams.

Renderer/projection/light acceptance is closed for 1.0.0. Reopen those systems only for a concrete regression.

## 1.1.0 — Portable illumination / projector expansion / Scan Codex

Planned 1.1.0 work includes:

- Dynamic/Mobile Mirage Light Foundation consumers;
- portable lanterns and Glow Dust battery loop;
- portable/stationary presentation-projector variants;
- Mirage Scan Codex and physical scan-card duplication workflow;
- Dragon Egg / End Resonance behavior for compatible chassis.

See `WAITLIST-1.1.0.md`.

## 1.2.0 — Direct hologram interaction

Planned 1.2.0 work focuses on direct manipulation such as grab/free-rotate hologram interaction over the generic `ProjectionTransform` contract.

See `WAITLIST-1.2.0.md`.

## Feature-complete release boundary

The following may be developed incrementally across later `1.0.x` snapshots, but the project does not become **1.1.0** until the intended 1.1 feature set is complete enough to ship:

- portable lantern gameplay;
- rechargeable Glow Dust;
- portable projectors;
- Scan Codex;
- Dragon Egg / End Resonance gameplay.

Direct grab/free rotation remains planned for 1.2.0. Create Blueprint support remains an optional bridge/addon direction rather than a required core feature.
