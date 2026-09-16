## 1.0.31

Canonical fixed-projector workspace/runtime rebuild. Image/Item/Entity/Banner workspace opening now atomically selects the corresponding source server-side, and main/workspace menus carry authoritative settings plus projection-enabled snapshots instead of relying on client BlockEntity timing. Table remains on the same base projector menu/screen path as Mirage Display. Main Cancel restores the opening baseline and exits; projector header/source spacing is padded; Scan Codex book canvas is explicitly rendered once before widgets while blur/dimming stays disabled. Network protocol 41; ProjectionSettings format 4.

## 1.0.30

Runtime UX/wiring correction wave. Hand Projector exposes Image/Item/Entity/Banner source modes with virtual non-consuming source capture; Shoulder pseudo-slots capture press+release; Wall Scale/X/Y preview live and Apply/Cancel remain open; Presentation Remote starts centered with a neutral dead-zone; Entity workspace source activation/layout is refreshed; Codex two-page layout and Lectern extension copy are compacted; Entity Scan Card receives dedicated 16×16 art. Network protocol 40; ProjectionSettings format 4.

## 1.0.29

Runtime QA interaction correction pass over 1.0.28. Network protocol **39**; `ProjectionSettings` format remains **4**. Fixes Mirage Equipment Creative-tab visibility and real cursor/slot interaction, restores the Codex book canvas in handheld/lectern modes, makes Table source selection authoritative before opening workspaces, reduces/recenters the Table Core render, repairs Presentation Remote binding in Creative and adds GUI-driven unpairing, and synchronizes Wall/Data-show collision/selection geometry with the low-profile physical model.

## 1.0.28

Runtime recipe-sync hotfix. The filled Entity Scan Card clearing recipe now uses one canonical stateless recipe instance for both JSON decoding and vanilla recipe stream synchronization. This prevents the `clientbound/minecraft:update_recipes` encoder disconnect on world/server join. Protocol remains 38 and `ProjectionSettings` format remains 4; no new gameplay scope.

## 1.0.26 — Presentation Deck automation + Data-show remote control

## 1.0.27

Build/source compatibility hotfix for the 1.0.26 presentation-control line. Protocol remains 38 and `ProjectionSettings` format remains 4. No new gameplay scope.


1.0.26 advances network protocol to **38** while keeping `ProjectionSettings` format **4**. Table and Wall/Data-show share the persistent ordered nine-image Presentation Deck. Automatic Presentation is optional with a user-controlled **1–120 second** interval; manual GUI/remote slide changes preserve the current elapsed interval without disabling automatic playback. Wall/Data-show adds one physical top Presentation Remote dock. A paired remote is bound to a persistent projector UUID plus location, opens a lightweight hold-RMB `<-- / - / -->` overlay and commits one previous/next action on release. Remote actions never force-load chunks and reject a projector that does not match the stored link identity. An unprojectable Wall slide remains selected, renders a transparent red circle/slash cancellation marker and remains traversable by Previous/Next/automatic playback. The remote Survival recipe is provisional balance content.

# Version Scope

## 1.0.25 — Table + Wall/Data-show presentation foundation

1.0.25 advances network protocol to 37 and `ProjectionSettings` format **3 → 4**. TABLE and WALL stay appended after the historical six chassis ordinals. Table keeps horizontal Image/Banner with upright Item/Entity plus packed pickup/support-loss behavior. Wall is redefined as a sub-slab-height **freestanding Data-show**, not a wall-mounted block: it requires a complete flat support, faces N/E/S/W, supports Image only, searches for a real wall ahead and validates only the actual aspect-correct image rectangle after Scale + signed X/Y offsets. Terrain outside that image footprint does not matter. Distance adds PU and the requested Scale may resolve downward to fit wall/power. The Wall Image Workspace provides a persistent ordered nine-image presentation playlist with reorder/current/Previous/Next controls. Survival recipes and final art/balance remain provisional.

## 1.0.24 — Scan Codex library/card rework + portable UI lite

1.0.24 advances network protocol to 36 while keeping `ProjectionSettings` format 3. The Scan Codex gains scrollable category/search/detail navigation, a 25-per-entity-type storage limit, passive Entity Scan Card containers, blank-card Lectern duplication, a filled-card clearing recipe, and a permanent Lectern import extension. Successful Mirage-card imports consume the source; optional Easy Mob Farm Mob Capture Cards use the same destructive-success rule. The Hand Projector GUI is compacted and its Forward/War Banner plus Directional/Billboard controls update dynamically from the live device state. Reverse Easy Mob Farm export remains deferred and, if later implemented, is balanced in experience levels rather than raw XP.

## 1.0.23 — vanilla Lectern Scan Codex workflow

1.0.23 advances network protocol to 35 while keeping `ProjectionSettings` format 3. It removes the temporary `mirage_projector:duplicating_lectern` block/item from 1.0.22. A Mirage Scan Codex can instead be mounted directly on a vanilla `minecraft:lectern`; interacting with that occupied Lectern opens the Codex browser in a position-bound copy mode. The server validates that exact mounted Codex before selection/favorite/copy/take actions. One Paper still produces one physical Entity Scan Card containing the unchanged frozen scan root. The browser presentation is also changed from a flat dark panel to a two-page book-like layout while retaining the explicit no-blur/no-pause behavior.

## 1.0.22 — Duplicating Lectern foundation

1.0.22 keeps network protocol 34 and `ProjectionSettings` format 3. It adds the first physical Scan Codex copy workflow: `mirage_projector:duplicating_lectern` resolves the exact selected capture from server-side Codex SavedData, consumes one Paper and produces a physical Entity Scan Card containing the unchanged frozen scan root. The station is stateless; no block entity, menu payload or new wire format is introduced. First-pass art inherits vanilla Lectern presentation and no Survival recipe is frozen yet.

## 1.0.21 — Runtime QA render/invalidation corrections

1.0.21 keeps network protocol 34 and `ProjectionSettings` format 3. It follows the successful 1.0.20 Sodium startup/visible-light correction and fixes three runtime QA regressions: neighboring terrain meshes now receive the render-section invalidation required by baked light sampling, the Shoulder Device vertical transform is corrected to shoulder height, and Mirage Hand Projector uses its embedded-cell/compact-chassis power validation with a dedicated portable hologram render path instead of being rejected by the fixed-projector Core gate. No registry identities or wire formats change.

## 1.0.20 — Sodium packed-light bridge hotfix

1.0.20 keeps network protocol 34 and `ProjectionSettings` format 3. It is a focused compatibility hotfix over 1.0.19: `LevelRendererMirageLightMixin` no longer calls `BlockAndTintGetter#getLightEngine()` from the packed-light return hook because Sodium supplies a `LevelSlice` whose light-engine accessor throws `UnsupportedOperationException` during chunk meshing. The bridge now resolves Mirage virtual light from the active client level and preserves the renderer's already-packed sky channel. No gameplay IDs or wire formats change.

## 1.0.19 — Runtime QA follow-up

1.0.19 keeps network protocol 34 and `ProjectionSettings` format 3. It is the direct runtime-QA correction to 1.0.18: Lantern and placed Light Projector use normal RMB for mode cycling and Shift+RMB for their GUIs; custom device/station screens restore item hover tooltips; the compact Lantern screen is shortened; Mirage Equipment initializes in Creative inventory; Charging Station input/output world lanes and Jade active-charge percentage are corrected; Scan Codex explicitly suppresses vanilla `renderBackground` blur/dimming; and the first `LevelRenderer` packed-light bridge merges Mirage virtual block light into terrain vertex light. The existing floor Mirage Light Projector is retained; future visible yaw/pitch aiming, a separate wall light projector, and vanilla-lantern-like ground placement for the handheld Lantern remain 1.1.0 work.

## 1.0.18 — Massive portable / Shoulder / charging stabilization

1.0.18 advances network protocol to 34 while keeping `ProjectionSettings` format 3. It is a QA-driven stabilization snapshot rather than a new feature family. Mirage Lantern and Hand Projector gain a shared real battery/configuration GUI; Mirage Light Projector gains its own block GUI. Legacy opposite-hand battery service is removed, Lantern Shift+RMB owns the Off/Focus/Flood/Ambient cycle, Hand Projector RMB remains ON/OFF, and battery percentage changes no longer force held-item re-equip animation.

Shoulder Equipment is restructured so the player attachment contains only the equipped Shoulder Strap while the Strap ItemStack itself stores Shoulder Device, battery pouch and upgrades through `DataComponents.CONTAINER`. Legacy 1.0.13–1.0.17 attachment data migrates into the Strap. The inventory panel moves right, reveals only active slots and preserves old devices on the cursor during swaps. The charging/light pass also fixes narrow-cone voxel intersection, moves dynamic source origins outside emitters, restricts Charging Station inputs to incomplete normal media, adds physical station-inventory rendering, normalizes fully recharged custom Glow Dust back to vanilla Glowstone Dust, simplifies player-facing tooltips and makes Scan Codex non-pausing/unblurred.


## 1.0.17 — Mirage Scan Codex persistent-library foundation

1.0.17 advances network protocol to 33 while keeping `ProjectionSettings` format 3. It adds `mirage_projector:scan_codex`, a non-stackable physical key to a server-authoritative persistent entity-scan library. Shift + right-click captures canonical frozen `EntityScanData`; repeated captures of the same entity type remain distinct because every stored snapshot keeps its own scan UUID.

The physical Codex stores only a stable Codex UUID plus the selected scan UUID. Full frozen scan roots live in Overworld `SavedData` (`mirage_projector_scan_codices`), while the browser receives metadata summaries only. The browser supports search, All/Favorites/Players/Humanoids/Horses/Other filters, favorites, paging and exact scan selection. 1.0.17 deliberately does not print physical Entity Scan Cards; the selected `(codex UUID, scan UUID)` and `ScanCodexSavedData.copyScanRoot(...)` seam are reserved for the Duplicating Lectern in a later 1.1.0 snapshot.

## 1.0.16 — War Banner portable presentation

1.0.16 advances network protocol to 32 while keeping `ProjectionSettings` format 3. The existing Shoulder Device control payload gains Banner-presentation actions; no second portable-projector state packet is introduced. A Banner-profile Mirage Hand Projector can now switch between normal Forward Projection and a War Banner presentation that renders the holographic banner cloth above the owning player's head even when the active projector is stored in normal inventory.

War Banner defaults to 65% of the same Banner's Forward Projection scale and a +4 px height offset, bounded to 45–80% and 0–12 px. `Directional` follows interpolated player body yaw; `Always Face Viewer` computes a per-observer horizontal billboard yaw while keeping the banner vertical. The mounted-device configuration screen exposes presentation, facing, size and height controls. The device UUID, battery, ON state and profile remain unchanged when presentation is modified. Final positioning/readability QA remains open for 1.1.0 polish.

## 1.0.15 — Dedicated Charging Station / logistics foundation

1.0.15 keeps network protocol 31 and `ProjectionSettings` format 3. It adds `mirage_projector:charging_station`, a horizontal Beacon-powered batch charger with four rechargeable input slots, one player-accessible active slot hard-limited to one cell, and four ordered outputs. The marked output face points toward the placing player, rejects automated insertion, exposes output extraction and pushes one completed item every eight ticks into a compatible adjacent item handler. The other five faces expose insert-only queue access.

Completed cells move to the leftmost compatible output; when all outputs are blocked, the full cell remains in the active slot and queue advancement stops. Charging uses the existing `RechargeableEnergyItem` contract and 0.20 Beacon-transmission attenuation shared with Core Booster charging. The Core Booster material-relay mechanic remains separate.

## 1.0.14 — Shoulder Battery Pouch / upgrade foundation

1.0.14 advances network protocol to 31 while keeping `ProjectionSettings` format 3. The Arm Strap attachment now exposes six purpose-built Battery Pouch slots and two generic shoulder-upgrade sockets. Pouch slots accept only `RechargeableEnergyItem` stacks, preserving full/partial/depleted cell components rather than acting as general storage.

`Auto Battery Swap Patch` atomically replaces a depleted cell in the mounted Lantern/Hand Projector with a charged pouch cell only when the depleted cell can be returned safely. `Battery Pouch Expansion Patch` expands storage from six to nine cells and unlocks a third generic upgrade socket. Duplicate upgrade families are rejected, Expansion cannot be removed while expansion-only slots are occupied, and the Arm Strap cannot be removed while any dependent shoulder content remains. Recipes remain intentionally unfrozen for QA.

## 1.0.13 — Mirage Equipment / Shoulder Slot foundation

1.0.13 advances network protocol to 30 while keeping `ProjectionSettings` format 3. It adds `mirage_projector:arm_strap` plus a Mirage-owned serializable player equipment attachment with dedicated Arm Strap and Shoulder Device slots. The compact inventory extension does not replace armor or offhand space: Arm Strap unlocks the Shoulder Slot, and the slot initially accepts Mirage Lantern and Mirage Hand Projector through the extensible `ShoulderMountableDevice` contract.

Mounted devices continue ticking outside normal inventory/hand slots. Lantern retains its portable light/drain behavior, while Hand Projector retains persistent projection drain and 1.0.12 portable-state publication. The server synchronizes compact shoulder-equipment state for physical shoulder presentation and mounted-light reconstruction. The initial mount reserves the right vanilla shoulder while leaving the left available. Death/keepInventory behavior preserves the real attachment ItemStacks. Final shoulder transform, richer device GUIs, War Banner and cosmetic bird/Parrot skins remain polish/future 1.1 work.

## 1.0.12 — Persistent portable projector state / Creative Battery

1.0.12 advances network protocol to 29 while keeping `ProjectionSettings` format 3. Mirage Hand Projectors now own stable device UUIDs and remain active when moved from a hand into normal player inventory after being explicitly turned ON. Battery drain therefore follows the device ON/OFF state rather than whether the item is currently selected.

Because remote clients cannot inspect arbitrary inventory slots, the server now publishes active portable-projector state by owner UUID + device UUID. Player movement/orientation still comes from vanilla tracking; Mirage only synchronizes portable device state. Clients cache/reconstruct the portable projector through the existing renderer and expire stale entries if publication stops. The new `mirage_projector:creative_battery` implements the normal rechargeable contract with infinite/no-depletion energy for Creative, QA, admin and temporary game-mode use; it has no Survival recipe or loot path. War Banner's final overhead/billboard presentation remains documented for 1.1.0 rather than being rushed into this infrastructure snapshot.

## 1.0.11 — Handheld Mirage Projector / portable copied-profile holograms

1.0.11 keeps protocol 28 and `ProjectionSettings` format 3. It adds `mirage_projector:mirage_hand_projector`, a non-stackable rechargeable handheld hologram projector that stores one exact Glow Dust or Light Battery ItemStack internally plus one compact normalized copy of a placed Mirage Projector profile. Sneak + right-click on a placed Mirage Projector copies the current active source profile; normal right-click toggles the handheld hologram on/off without deleting it; sneak + right-click in air services the removable cell through the opposite hand.

The portable profile preserves the active source family (`Image`, `Item`, `Entity` or `Banner`) but normalizes presentation for moving use: compact chassis, single-source image layout, reduced scale/lift/floating ceilings and opacity capped at 90% so at least 10% ghost remains. Clients reconstruct a temporary projector directly from tracked held ItemStacks plus vanilla player transform and render the hologram in front of the tracked player without adding a custom movement packet. Final handheld self-configuration UI, survival recipe and balance remain open.

## 1.0.10 — Handheld Mirage Lantern / first player-following light consumer

1.0.10 keeps protocol 28 and `ProjectionSettings` format 3. It adds `mirage_projector:mirage_lantern`, a non-stackable handheld Focus/Flood/Ambient/Off emitter that stores one exact Glow Dust or Light Battery ItemStack internally. Sneak + right-click services the removable cell through the opposite hand; normal right-click cycles the shared portable-light mode sequence while powered.

The server drains the installed cell once per second only while the lantern is actually held. Clients derive stable per-player-hand `DYNAMIC_VISUAL` sources from tracked held ItemStacks plus vanilla player position/look state, so local and remote lantern beams move/aim without publishing static chunk light or introducing a dedicated movement packet. A persistent local actionbar shows lantern mode and battery percentage, with depleted/empty state shown as `Discharged` / `Sin Cargar`. Final profile balance and survival recipes remain open.

## 1.0.9 — Physical light projector / first DYNAMIC_VISUAL gameplay consumer

1.0.9 keeps protocol 28 while keeping `ProjectionSettings` format 3. It adds `mirage_projector:mirage_light_projector`, a placed horizontally oriented reflector with one real rechargeable-energy slot. Glow Dust and Light Battery insert through the shared `RechargeableEnergyItem` contract; Shift + right-click returns the exact cell through normal inventory merge semantics.

A loaded projector cycles exactly Focus -> Flood -> Ambient -> Off -> Focus, starting at Focus. Mode/cell state persist and synchronize per block. Clients convert synchronized placed emitters into `DYNAMIC_VISUAL` snapshots; Focus/Flood use directional cones and Ambient uses an omnidirectional local field. The server drains charge once per second using centralized QA values of 4/2/1/0 units for Focus/Flood/Ambient/Off. A depleted cell suspends light without deleting the selected mode. Survival recipe, final handheld/placed light balance, projection-energy PU/Lift and hologram-light values remain open.

## 1.0.8 — Rechargeable energy polish / Light Battery foundation

1.0.8 keeps protocol 28 and `ProjectionSettings` format 3. The 1.0.7 Glow Dust charging path is generalized through `RechargeableEnergyItem`, and the Core Booster charging cradle accepts rechargeable media rather than hard-coding Glow Dust. Glow Dust remains 1000 units at +10 charge per 10-tick pulse. The new Light Battery stores 4000 units at +8 per pulse, giving four times the capacity and roughly five times the empty-to-full charge time (~250 s versus ~50 s).

This snapshot also fixes charging-item extraction so the returned stack can merge with an identical inventory stack, recenters/scales the in-Booster charging render, and exposes the inserted charging medium/percentage through Jade. The Light Battery recipe is intentionally not committed because only the target of about five Glow Dust plus additional materials was recovered. Rechargeable media are not yet assigned guessed projection PU/Lift/light behavior.

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

- rechargeable Glow Dust;
- portable projectors;
- Scan Codex;
- Dragon Egg / End Resonance gameplay.

Direct grab/free rotation remains planned for 1.2.0. Create Blueprint support remains an optional bridge/addon direction rather than a required core feature.

### 1.0.17

Scan Codex persistent-library foundation: physical Codex UUID key, server-side SavedData snapshots, metadata-only searchable/filterable/favoritable browser and exact selected capture identity. Duplicating Lectern remains later 1.1.0 work. Protocol 33; ProjectionSettings format 3.
