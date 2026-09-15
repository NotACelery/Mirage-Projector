# Current Implementation — Mirage Projector 1.0.18

Version: **1.0.18**
Minecraft: **1.21.1**
NeoForge: **21.1.244+**
Network protocol: **34**

This document describes the current implementation behavior of Mirage Projector 1.0.18. Historical development notes are archived under `docs/history/` and are not current authority.

## Canonical projector family

The runtime exposes six projector chassis:

1. Mirage Projector
2. Mirage Display
3. Mirage Field Projector
4. Wide Mirage Projector
5. Tall Mirage Projector
6. Mirage Prism

The project no longer registers alternate/comparison projector IDs. Each chassis has one canonical block/item identity, model, VoxelShape and renderer layout.

Crafting upgrades preserve stored projector state. Mirage Projector upgrades into Mirage Display, which branches into Wide, Tall, Prism and Field variants.


## Physical Mirage Light Projector

`mirage_projector:mirage_light_projector` is the placed `DYNAMIC_VISUAL` light emitter. It is horizontally oriented and owns one real rechargeable-energy slot through the shared `RechargeableEnergyItem` contract.

Since 1.0.18, normal right-click opens a real Light Projector container screen. Battery insertion/extraction and mode configuration live in that GUI rather than being overloaded onto direct block gestures. The one-cell slot accepts rechargeable media and preserves exact charge/components. Focus/Flood/Ambient/Off remain the shared operating modes; Focus is the placed projector default.

Mode and energy-cell state persist in the block entity and synchronize to clients. Nearby clients submit a stable block-position light source into `ClientDynamicMirageLightManager`. Focus and Flood use directional-cone solving; Ambient is omnidirectional; Off submits no source. Server drain remains 4/2/1/0 units per second as the current QA balance values.

1.0.18 removes the old physical renderer that showed the battery floating below the projector. The dynamic source is now seeded just outside the projector chassis so the first propagation edge cannot self-occlude inside the emitter block. Jade continues to report mode and battery state.

## Handheld Mirage Lantern

`mirage_projector:mirage_lantern` is the player-following `DYNAMIC_VISUAL` light. It stores one exact rechargeable ItemStack internally, preserving cell type, components and partial charge.

Since 1.0.18, **normal right-click opens the Lantern GUI** and battery service is available only through that GUI. **Sneak + right-click** cycles `Off -> Focus -> Flood -> Ambient -> Off`; a fresh Lantern defaults to Off. The old opposite-hand battery insert/extract gesture is removed.

Server-side drain occurs once per second only while the Lantern is held or mounted in the Shoulder Slot and its selected mode emits. Current values remain Focus 4/s, Flood 2/s, Ambient 1/s and Off 0/s. Depletion keeps the selected mode but disables emission until a charged cell is installed.

Clients derive stable per-player light sources from vanilla tracked player position/look state. 1.0.18 moves the light origin forward from the player and changes the directional solver to test cone/voxel-volume intersection rather than voxel-center-only intersection, addressing the QA failure where Focus/Flood disappeared for most view angles. Battery percentage mutations no longer trigger a held-item re-equip animation, and the old continuously refreshed local actionbar status was removed; feedback is action/depletion driven instead.

## Handheld Mirage Projector

`mirage_projector:mirage_hand_projector` is the portable hologram consumer. It stores one exact rechargeable cell and one compact normalized copy of a configured placed Mirage Projector profile. Portable profiles preserve Image/Item/Entity/Banner source identity while clamping the moving presentation to portable limits.

**Normal right-click** is the explicit projection ON/OFF action. **Sneak + right-click** opens the Hand Projector GUI. Battery service, target-profile copying and portable presentation controls now live in that GUI; the old opposite-hand battery service and direct sneak-use profile copy are retired. The item tooltip is intentionally minimal because configuration belongs to the screen.

Since 1.0.12 an enabled Hand Projector remains active when stored anywhere in normal player inventory. Stable ItemStack UUID identity plus `PortableProjectorStatePayload` lets remote clients see active hidden-inventory projections without receiving arbitrary inventory contents. Vanilla entity tracking still supplies movement/orientation. Battery drain continues server-side while ON and content-valid.

Banner profiles retain the 1.0.16 Forward/War Banner presentation. War Banner is a smaller pole-free hologram above the owner, either Directional to body yaw or horizontally billboarded per viewer, with bounded size/height stored on the device. 1.0.18 keeps those controls in the real device GUI and suppresses battery-percentage re-equip animation.

`mirage_projector:creative_battery` remains an infinite Creative/debug rechargeable medium with no Survival recipe/loot path.

## Mirage Equipment / Shoulder Slot

Mirage Equipment provides a dedicated player equipment socket without consuming armor or the vanilla offhand. The user-facing harness is now **Shoulder Strap**; its historical registry ID remains `mirage_projector:arm_strap` for world/save compatibility.

Since 1.0.18 the player attachment itself stores only the currently equipped Shoulder Strap. The Strap ItemStack owns the Shoulder Device, Battery Pouch and upgrade inventory through vanilla `DataComponents.CONTAINER`. This makes a packed strap a portable mini-morral: multiple straps can be prepared with different batteries/upgrades, removed, stored and exchanged without unpacking their contents. Legacy 1.0.13–1.0.17 attachment layouts are folded into the Strap ItemStack automatically on load.

The inventory extension opens on the **right** side of the vanilla inventory. With no Strap installed, the expanded Mirage panel exposes only one Shoulder Strap socket. Installing a Strap dynamically reveals the Shoulder Device, six base power-cell slots and two base upgrade sockets. Expansion reveals the final three battery slots and third upgrade socket; inactive positions do not exist visually as X/locked slots. The toggle remains inside the vanilla inventory region below the crafting-result area.

The Shoulder Slot accepts `ShoulderMountableDevice` implementations, currently Mirage Lantern and Mirage Hand Projector. Right-clicking the occupied Shoulder Device slot opens the same real portable-device GUI used by handheld devices. Device replacement follows normal cursor swap semantics: the previous device stays on the cursor instead of being dropped into the world. The Strap cannot be removed while a device is mounted, but batteries/upgrades travel safely inside it. Shift-hovering a packed Strap in normal inventory exposes a compact contents preview.

Mounted devices retain their normal runtime. The right shoulder remains reserved against vanilla shoulder riders while occupied; the left shoulder remains available. Normal armor/offhand slots and vanilla F swap-hands behavior remain untouched.

### Shoulder Strap Battery Pouch / upgrades

The Shoulder Strap exposes **6 base rechargeable-media positions** and **2 base generic upgrade sockets**. `Shoulder Strap Slot Expansion` (historical registry ID `battery_pouch_expansion_patch`) expands the visible/usable inventory to **9 battery positions** and **3 upgrade sockets**. Duplicate upgrade families are rejected and Expansion cannot be removed while its extra positions are occupied.

`Auto Battery Swap Patch` remains shoulder-only: a depleted mounted-device cell is atomically replaced by the best charged compatible pouch cell only if the depleted cell can be returned safely. Its user-facing tooltip is intentionally omitted because its name already describes the behavior. The expansion patch has one concise tooltip stating that it adds three Shoulder Strap inventory slots.

## Projector state

A fixed projector keeps these concepts separate:

- **projection enabled/disabled**;
- **active projection source**;
- **workspace currently open**;
- **source-specific content**;
- **shared presentation transform**;
- **installed Core / power state**.

`TURN OFF` disables rendering without deleting Image, Item, Banner or Entity data. Selecting `Use <mode> mode` activates that source and re-enables projection. GUI source buttons indicate the source that is actually active, not merely the workspace currently being viewed.


## Main projector settings UI

The main projector screen uses a fixed option area with four mutually exclusive tabs: **Geometry**, **Placement**, **Rotation** and **Floating**. Geometry also contains Lighting, Ghost/opacity and Tint. Switching tabs only changes the controls inside that area; Source Workspaces, Power / Capacity, Core slot, inventory and Apply / Cancel stay anchored. The compact 412-pixel panel fits 1920×1080 at GUI Scale 2 without responsive scrolling, while the existing scrollbar remains available at smaller effective heights.

## Projection source architecture

Built-in source IDs are stable namespaced identifiers:

- `mirage_projector:image`
- `mirage_projector:item`
- `mirage_projector:entity`
- `mirage_projector:banner`

Source identity is ordinal-free. Save/network settings are versioned, legacy ordinal saves migrate, and unknown registered source IDs/payloads are preserved rather than destructively coerced into a built-in type.

`ProjectionSourceRegistry` owns common source definitions/content semantics. `ProjectionSourceRenderRegistry` owns client render dispatch. Chassis/source compatibility is queried centrally instead of being hard-coded independently into each screen/renderer path.

## Projection transforms

Shared presentation state is separated from source content through `ProjectionTransform`. The shared fixed-projector transform exposes Scale/Lift, animated yaw and quaternion-backed **Tilt**. Lift is the single non-negative vertical placement axis; independent horizontal/vertical translation is intentionally not a user-facing projector control.

Tilt supports the full **-90° to +90°** range. Mirage Prism applies Tilt independently to each radial face while preserving carousel rotation around the projector center.

Mirage Prism Image/Banner projection additionally uses **Prism Distance**. The UI value is extra radial separation above the no-tilt collision-safe radius: **+0 px** packs adjacent face boundaries as tightly as possible without overlap. `PrismProjectionSpacing` derives the internal absolute radius from each adjacent pair of active faces. At +0 px, equal square faces meet at their lower corners without crossing. Positive/outward Tilt keeps that compact lower-edge baseline; negative/inward Tilt raises the minimum only by the inward reach required to avoid overlap. User-controlled extra distance is capped at **+160 px (10 blocks)**. If an inward angle would need more room than that budget, the UI refuses that angle. Additional or Tilt-required radial separation consumes a small amount of PU.

`ProjectionSettings` network format remains version 3. Network protocol is **34** in 1.0.18. The compatibility token includes the portable-device menu/action surface introduced by the stabilization wave plus the earlier War Banner, Shoulder Equipment and portable-projector payloads. Legacy horizontal/vertical offset slots remain only for wire/NBT compatibility: horizontal sanitizes to zero, while positive legacy Vertical Offset is absorbed into Lift and then sanitized to zero. Worlds from 1.0.0 remain compatible.

## Image / GIF

Supported import families:

- PNG
- JPG/JPEG
- static WebP
- BMP
- animated GIF

Imported assets are content-addressed and synchronized through the Mirage asset pipeline. Wide/Tall can use multi-source layouts, Prism supports independent cardinal faces and Field uses one continuous plane.

## Item

The Item workspace stores a virtual serialized snapshot. The source inventory item is not consumed or physically stored inside the projector. Blocks use volumetric rendering when applicable; ordinary items use Minecraft's item renderer.

## Banner

Banner appearance is copied virtually. Plane chassis render cloth without a physical banner pole. Prism stores independent North/East/South/West banner snapshots and can copy the North source to the remaining faces.

## Entity

Entity Scan Cards contain frozen projection data rather than live entities. Supported state includes:

- generic living entities;
- Players and Player skin/model-part state;
- Humanoid equipment and held items;
- bodyless equipment rigs;
- Horse Saddle and Body Armor;
- custom names/nameplates;
- supported pose presets;
- per-channel projected-equipment visibility.

Entity preview fitting, clearance and world culling use conservative pose/species/equipment-aware bounds. Passenger/vehicle composite scans remain rejected because the 1.0.x line does not define a composite snapshot format.

## Projection Power

`ProjectionPower` is the authority for capacity, component cost, overdrive and feasible slider limits. Fixed projectors obtain energy through `ProjectionEnergySource` backed by the installed Projection Core; the energy boundary itself is not tied to a Core socket so future portable devices can use another backend.

Core base PU:

| Material | Base PU |
|---|---:|
| Glass | 32 |
| Quartz | 48 |
| Amethyst | 64 |
| Diamond | 96 |
| Netherite | 128 |

Effective capacity is:

```text
floor(Base PU × chassis multiplier × Core amplification)
```

A loaded Core Booster contributes ×1.50 Core amplification and retains material-specific Beacon/Mirage-light identity.

## Crying Obsidian ecosystem

The renewable crystal loop is:

```text
Lava source
    ↓
Crying Obsidian
    ↓
Small Bud → Medium Bud → Large Bud → Cluster
```

The space below Crying Obsidian must be available for growth. Silk Touch preserves the current bud/cluster stage. Normal harvesting produces Crying Obsidian Shards; Fortune does not multiply shard drops.

Crying Obsidian can be crafted from shards using either Fire Charge or Magma Cream recipes. EMI/JEI integrations expose the crafting chain and growth guidance when those viewers are installed.

## Core Booster / Beacon relay

There is one user-facing `core_booster` block/item. It accepts Glass, Quartz, Amethyst Shard, Diamond or Netherite Ingot and preserves loaded material state when properly mined.

Material relay identities:

- Glass — Diffusion
- Quartz — Radiance
- Amethyst — Resonance
- Diamond — Focus
- Netherite — Inversion

At most four effective loaded Boosters participate in Beacon relay calculations. Only an energized Mature Crying Obsidian Cluster publishes static Mirage world light.

## Mirage Light Engine

Static Mature Cluster lighting is server-authoritative. The causal six-neighbour solver uses vanilla destination opacity/face occlusion, exact fixed-point half-decay in open space, additional obstacle-detour cost, overlap-by-maximum aggregation and chunk-aware dependency windows.

Clients do not solve `STATIC_WORLD` geometry. They install server-resolved packed Mirage light sections and read effective light as:

```text
max(vanilla block light, Mirage light)
```

Mirage virtual light is never fed back into vanilla block-light propagation as a new emitter.

`DYNAMIC_VISUAL` is now an operational client-only runtime for moving/portable emitters. Consumers submit moving-source snapshots with stable identity, position, profile, update cadence, camera-cull distance and stale timeout. Directional-cone geometry is solved by the same causal voxel engine, while dynamic fields remain local and never enter the authoritative `STATIC_WORLD` publication channel. The placed Mirage Light Projector and handheld Mirage Lantern both consume this runtime directly; the latter follows tracked player position/aim and uses stable per-player-hand source identities.

Physical `mirage_projector:crying_light_node` exists only as migration compatibility for old development worlds and is not created by current gameplay.

## Rechargeable energy foundation

Rechargeable energy is ItemStack-owned through `RechargeableEnergyItem`. Mirage Glow Dust carries 1000 units while Light Battery carries 4000; charging cadence remains 10 units / 10 ticks for Glow Dust and 8 / 10 ticks for Light Battery.

1.0.18 tightens the Glow Dust identity boundary. Partial/depleted energy is represented by `mirage_projector:glow_dust`, so it does not satisfy vanilla Glowstone Dust crafting/brewing identity. When that custom medium reaches 100% in a Beacon charger, it normalizes back to vanilla `minecraft:glowstone_dust`, restoring normal vanilla semantics. Conversely, vanilla Glowstone Dust is treated as a fully charged source when inserted into a Mirage device and is normalized to the device-owned custom charge form internally.

Glow Dust tooltips now show only `Discharged` at zero or percentage while partial; fully restored vanilla dust needs no Mirage tooltip. Light Battery tooltips show only percentage/discharged state and no longer explain internal capacity ratios or charger implementation.

Core Booster and Charging Station both use the shared Beacon recharge contract and both normalize completed custom Glow Dust back to vanilla output. A crafting hook is prepared so a future Light Battery recipe consuming exactly five Glow Dust media inherits the average charge fraction of those five dust inputs. The remaining recipe materials are still intentionally unfrozen.

## Recipe viewers

### EMI

EMI integration exposes:

- all custom projector upgrade recipes under Crafting;
- both Crying Obsidian shard recipes;
- icon/tool-tip-based Crying Obsidian World Interaction guidance;
- age-ordered crystal Block Drops.

### JEI

JEI integration exposes custom projector upgrade recipes through the vanilla Crafting category and supplies ingredient information for projector progression and renewable Crying Obsidian growth.

Both integrations are optional. Mirage Projector loads normally when either or both recipe viewers are absent.

## Public handbook

The in-game `Mirage Handbook` documents General behavior plus one section for each of the six chassis. The registry ID remains `debug_handbook` for save compatibility, but the public display name and content are release-facing.

## Compatibility and migration

The 1.0.x line retains explicit compatibility/migration surfaces where removing them would damage existing worlds:

- old `crying_light_node` relay blocks are migration-only and self-remove;
- five historical `improved_*_core` block IDs remain migration shims without BlockItems/recipes/Creative exposure;
- legacy numeric projection-source saves migrate to namespaced source IDs;
- unknown future source IDs/payloads are preserved where possible.

## Dedicated Charging Station (1.0.15)

The directional Charging Station keeps the 1.0.15 `4 input -> 1 active -> 4 output` contract, front-face output logistics and one-item-per-eight-ticks automatic ejection.

1.0.18 restricts the charging queue to **incomplete Glow Dust / Light Battery media**. Full media and Creative Battery are rejected. The active slot remains hard-limited to one physical item; a completed cell stays there if all four output positions are blocked.

The GUI is enlarged so Input Queue, Charging, Output and player Inventory no longer overlap and there is more clearance for third-party inventory buttons. A block-entity renderer now visualizes the four queued stacks, the active charging item and the four completed-output stacks in distinct physical regions that rotate with station facing.

## Mirage Scan Codex (1.0.17)

The Scan Codex remains a physical UUID key to server-side `ScanCodexSavedData`, supporting multiple independent snapshots, search, filters, favorites and exact scan selection without synchronizing full scan NBT to the browser.

1.0.18 changes only its screen behavior: the Codex now behaves like an inventory/book overlay rather than a pause menu. `isPauseScreen()` returns false and the screen does not invoke vanilla's blurred/dim background pass, so the world continues running visibly behind the Codex panel.

## Deferred to later releases

1.0.17 does not include:

- handheld projector self-configuration UI (the 1.0.11 device currently copies from placed projectors rather than opening its own source workspace);
- Duplicating Lectern / physical Codex-to-Entity-Scan-Card copying;
- Dragon Egg / End Resonance gameplay;
- direct grab/free-rotate hologram manipulation;
- UV Shoulder Light / Auto UV / UV Marks ecosystem (reserved for 1.2.0);
- Create Blueprint projection source.

See `ROADMAP.md`, `WAITLIST-1.1.0.md` and `WAITLIST-1.2.0.md`.
