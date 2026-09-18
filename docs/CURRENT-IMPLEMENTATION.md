# Current Implementation — Mirage Projector 1.0.41

Version: **1.0.41**
Minecraft: **1.21.1**
NeoForge: **21.1.244+**
Network protocol: **43**

This document describes the current implementation behavior of Mirage Projector 1.0.41. Historical development notes are archived under `docs/history/` and are not current authority.


## 1.0.34 Roadmap & UX cleanup

- Restores the original presentation/Data-show **Mirage Wall Projector** as the only wall-projector identity; the separate `mirage_wall_illuminator` experiment is retired.
- Table workspace navigation and active SourceMode are separate again: opening a tab is non-mutating and only the explicit Use Mode action changes projection mode.
- Portable projector empty-state text now says no projection has been configured instead of referring to removed copied profiles.
- Mirage Flashlight uses one coherent work-flashlight material/shape language for held and placed forms; held orientation is horizontal/forward and Ambient emission/placed presentation point upward.
- Mirage Light Projector uses a Crying-Obsidian exterior with Iron internal structure, a larger front reflector with one-pixel housing margin, and a four-side upper Ambient lens state.
- `docs/NEXT-WAVES-1.1.0.md` is the authoritative implementation order for remaining 1.1 work; visible Light Projector yaw/pitch is deliberately last.


## 1.0.33 hidden special-resonance foundation

1.0.33 introduces a dedicated non-PU special-resonance provider contract without folding special catalysts into `ProjectionCoreProfile`. Only Field and Prism explicitly expose the End-resonance capability. Inserting the supported catalyst into either physical Core slot captures the complete projector-facing state into a persistent restore snapshot, suspends ordinary source/settings mutation, overrides normal shutdown and replaces normal projection rendering with fixed chassis-owned geometry. Field uses a facing-authoritative planar 2 × 3 aperture; Prism uses a 2 × 3 × 2 volumetric anomaly. Removing the catalyst restores the suspended projector state exactly.

The main GUI switches to a read-only `END RESONANCE` status panel while keeping `TURN OFF` attemptable for the established `This doesn't seem to work...` feedback. Creative pick-block sanitizes the unique catalyst, and chassis-upgrade recipes reject packed projectors that still contain a special resonance catalyst so state copying cannot duplicate or destroy it. The restore snapshot persists across world/chunk reload. Functional cross-dimension transfer, entity cooldown and destination safety remain intentionally pending for the next wave. Network protocol remains **43** and `ProjectionSettings` remains format **4**.




## 1.0.32 Survival progression / Flashlight / wall illumination

1.0.32 keeps network protocol **42** and `ProjectionSettings` format **4**. The former Mirage Lantern is now the public/runtime **Mirage Flashlight**; Java symbols, UI text and active documentation use Flashlight while the registry ID `mirage_projector:mirage_lantern` and existing `MirageLantern...` ItemStack NBT keys remain intentionally stable so old worlds retain configured devices. The Flashlight uses a compact 3D Crying-Obsidian / magenta-glass model and may be placed temporarily on a sturdy block top; placement transfers its exact rechargeable cell and mode into the world form, and breaking/support loss returns one Flashlight with that same state.

The illumination family currently has one fixed floor chassis: **Mirage Light Projector**. The temporary 1.0.32 wall-mounted illumination experiment (`mirage_wall_illuminator`) was removed in 1.0.34 because it duplicated the role/name of the established presentation chassis without a useful gameplay niche. **Mirage Wall Projector** is again the original presentation/Data-show chassis registered as `mirage_projector:mirage_wall_projector`.

Survival progression is committed for Light Battery, Mirage Flashlight, Mirage Light Projector, Shoulder Strap, Auto Battery Swap Patch, Shoulder Strap Slot Expansion, Charging Station, Mirage Hand Projector, Scan Codex, Mirage Table Projector and the presentation-oriented Mirage Wall Projector. The removed `mirage_wall_illuminator` experiment has no recipe or registry entry. Light Battery consumes exactly five Glow Dust media in an X, with Copper above, Redstone below and Iron on the sides; the recipe accepts full vanilla Glowstone Dust or the rechargeable custom Glow Dust so existing charge-preservation logic remains meaningful. Glow Dust itself intentionally has no crafting recipe. The remaining illumination blocker is visible yaw/pitch aiming for the floor Light Projector.

## 1.0.31 canonical workspace/runtime rebuild

1.0.31 advances network protocol to **42** while retaining `ProjectionSettings` format **4**. Fixed-projector source navigation is now atomic and server-authoritative: opening Image, Item, Entity or Banner first activates that source on the server and then opens the matching workspace. The main menu and all four source workspaces receive authoritative settings and projection-enabled snapshots in their opening data, so UI state no longer depends on a client BlockEntity update arriving before screen construction. This is especially important for the Table chassis, which continues to use the same `MirageProjectorMenu` / `MirageProjectorScreen` path as Mirage Display and the original projectors; Table-specific behavior remains limited to horizontal placement/render/capability semantics. Main-screen Cancel restores the opening baseline and closes the UI. Header/source-workspace spacing is padded so Wall controls do not collide with the source region. Scan Codex keeps vanilla blur/dimming disabled but draws its parchment/book canvas exactly once from the explicit screen render path before widgets, preventing both the transparent-book regression and historical double rendering.

## 1.0.30 UX/runtime interaction wave

1.0.30 advances network protocol to **40** while retaining `ProjectionSettings` format **4**. The Mirage Hand Projector becomes a compact multi-source device with Image/Item/Entity/Banner mode selection and a non-consuming virtual source well for Item, filled Entity Scan Card and Banner captures. Mirage Equipment captures both press and release for external pseudo-slots, removes the redundant expansion `+3`, and docks its toggle to the inventory frame. Wall/Data-show Scale/X/Y controls publish live preview state; Apply commits a baseline without closing and Cancel restores it without closing. Presentation Remote native cursor centering uses raw window pixels with a neutral dead-zone. Entity workspace refreshes after source activation and keeps Projected/Visibility controls within bounds. The Codex canvas renders once from `renderBg`, library filters/search and result list are page-separated, lectern extensions use compact text/padded slot frames, and Entity Scan Card owns a dedicated 16×16 texture.

## 1.0.29 runtime QA interaction corrections

1.0.29 fixes the first broad in-game QA of the 1.0.24–1.0.28 UI/anchor work. In Creative, Mirage Equipment is visible only on the player's inventory tab and its slots intercept clicks outside vanilla's GUI rectangle; Creative cursor contents are sent as an explicit snapshot and corrected back from the server after the action. The Scan Codex renders its book canvas explicitly before container widgets while preserving the live-world/no-blur contract. Table projector source buttons set the active source before opening a workspace, and its installed Core is rendered smaller/centered inside the real chamber. Presentation Remote docking binds the source stack before copying it into the Wall/Data-show dock, explicit Unpair invalidates the old link ID and ejects the physical controller, and Wall/Data-show selection/collision geometry includes the low body, front lens and top dock. Network protocol is **39**; ProjectionSettings remains format **4**.

## 1.0.28 recipe-sync hotfix

1.0.28 fixes a login-time `clientbound/minecraft:update_recipes` encoder failure introduced by the filled Entity Scan Card clearing recipe. Minecraft 1.21.1 `StreamCodec.unit(value)` refuses to encode any object that is not `.equals(value)`. The JSON codec previously constructed a fresh `ScanCardClearingRecipe`, while the stream codec expected a separate instance, so vanilla recipe synchronization disconnected the client while joining a world. The recipe is now stateless and canonical: both `MapCodec` and `StreamCodec` resolve the same `ScanCardClearingRecipe.INSTANCE`. Gameplay, network protocol **38**, and `ProjectionSettings` format **4** are unchanged.

## 1.0.27 build-stability note

1.0.27 does not change the 1.0.26 presentation gameplay contract. It repairs Java 1.21.1 source compatibility for the Presentation Remote/Wall renderer, restores the canonical `ProjectionSettings.withBackFaceMode(...)` copy helper used by chassis normalization, and extends cumulative cleanup so the removed Mirage-specific `DuplicatingLecternBlock.java` cannot survive when a newer snapshot is copied over an older Windows project folder. Network protocol remains 38 and `ProjectionSettings` remains format 4.

## Canonical projector family

The runtime exposes eight projector chassis:

1. Mirage Projector
2. Mirage Display
3. Mirage Field Projector
4. Wide Mirage Projector
5. Tall Mirage Projector
6. Mirage Prism
7. Mirage Table Projector
8. Mirage Wall Projector

The project no longer registers alternate/comparison projector IDs. Each chassis has one canonical block/item identity, model, VoxelShape and renderer layout.

Crafting upgrades preserve stored projector state. Mirage Projector upgrades into Mirage Display, which branches into Wide, Tall, Prism and Field variants. Table and the presentation-oriented Wall chassis are 1.1 anchor-family foundations; their Survival recipes are now frozen in 1.0.32. The presentation chassis keeps its original public/code identity **Mirage Wall Projector**; there is no separate wall-mounted illumination projector in the current scope.

### Table / Horizontal anchor

`mirage_projector:mirage_table_projector` must stand on a sturdy top surface. Image and Banner plane sources use a horizontal tabletop anchor; Item and Entity sources remain upright above the same chassis so it can act as a holographic display plinth. The normal Lift/Tilt/Rotation/Floating contracts remain available. Sneak + empty-hand right-click packs the complete BlockEntity state back into one Table Projector item. Removing its support uses the same packed-state drop path instead of spilling the Core/card/staging inventories separately.

Runtime ownership is now Table-specific: `MirageTableProjectorScreen` mirrors the canonical settings GUI 1:1, while `MirageTableProjectorLogic` owns Table angle/bob, X/Z anchor offsets, horizontal planar placement, upright volumetric placement, front/back classification and render bounds. Rotation of an untilted tabletop image changes only its in-plane reading direction; visibility is derived from the exact render quaternion chain rather than an independent yaw approximation. The existing BlockEntity/menu/storage contract remains shared strictly for world/save compatibility.

### Wall / Data-show anchor

`mirage_projector:mirage_wall_projector` is a low-profile video/Data-show projector, **not a wall-mounted emitter**. It sits on a complete full-block top surface, rejects slab/stair/partial supports, faces N/E/S/W and searches forward for a real planar wall. Its physical model/VoxelShape stays below slab height and uses an Obsidian body with Crying Obsidian optical accents. Shift + empty-hand right-click packs the complete state; losing the floor/table support uses the same packed drop path.

Wall is Image-only. Target validation belongs to `WallProjectionSurface`, not generic hologram clearance. The validator measures the actual aspect-correct image rectangle after Scale and signed X/Y offsets. Every block cell touched by that rectangle must lie on one regular full wall plane with an unobstructed line from the projector; unused space around a tall, wide or 16:9 image is irrelevant even when that unused area contains irregular terrain. Requested Scale can resolve downward until the image fits both the wall and installed Core. Wall distance adds PU, currently +1 PU per full block beyond the first.

### Presentation Deck / automatic playback

Table and Wall/Data-show own the same ordered nine-image Presentation Deck. The Image Workspace can import/replace, clear, reorder, select the current slide and move Previous/Next. Automatic Presentation is optional and uses a **1–120 second** user interval. Server-side ticking advances only when projection is enabled and at least two deck images exist. Manual current-slide changes from either the workspace or Presentation Remote do not reset the elapsed automatic clock. Automatic mode stays enabled and the next timed advance occurs on the same cadence it already had; only toggling Automatic Presentation or changing its interval starts a fresh countdown. With automatic mode disabled, the selected slide is stable until the user explicitly changes it.

Wall/Data-show may contain one physical `Presentation Remote` in its top pairing dock. Inserting a remote binds it to a persistent projector link UUID plus current dimension/position. Sneak + empty-hand RMB retrieves it before the normal packed-projector pickup gesture. A bound handheld remote opens a non-pausing, no-background controller while RMB is held: `<--`, `-`, `-->`. The cursor starts neutral; releasing RMB on left/right sends one previous/next action. Server validation requires the same dimension, a currently loaded target chunk, the Wall chassis and the exact persistent link UUID, so a replacement projector at the same coordinates cannot inherit the old control. The remote does not force-load chunks.

Selecting a Wall slide that cannot resolve onto a valid regular target surface **does not skip or replace the deck entry**. That slide remains current and displays the Mirage red prohibition/cancellation symbol in place of the image. Previous/Next and automatic playback can still move away from it normally.

The Image Workspace exposes a persistent ordered nine-slot presentation playlist. Images can be imported/replaced, cleared and reordered; one slot is current and Previous/Next changes the live projected slide without leaving the GUI. Automatic slide timing/transitions remain later polish rather than being silently invented.

`ProjectionChassisProfile` carries explicit anchor, placement-capability and source-compatibility metadata. Table uses `TABLE_HORIZONTAL`; Wall uses `WALL_TARGET` + `WALL_XY_OFFSET`. Renderer placement, target-wall validation, power costs and workspace/widget visibility consume the same contract so unsupported controls cannot silently affect only one subsystem.


## Physical Mirage Light Projector

`mirage_projector:mirage_light_projector` is the placed `DYNAMIC_VISUAL` light emitter. It is horizontally oriented and owns one real rechargeable-energy slot through the shared `RechargeableEnergyItem` contract.

Since 1.0.19, **normal right-click cycles Focus / Flood / Ambient / Off** and **sneak + right-click opens the Light Projector container screen**. Battery insertion/extraction lives only in that GUI; direct block gestures never insert or extract cells. The one-cell slot accepts rechargeable media and preserves exact charge/components. Focus/Flood/Ambient/Off remain the shared operating modes; Focus is the placed projector default.

Mode and energy-cell state persist in the block entity and synchronize to clients. Nearby clients submit a stable block-position light source into `ClientDynamicMirageLightManager`. Focus and Flood use directional-cone solving; Ambient is omnidirectional; Off submits no source. Server drain remains 4/2/1/0 units per second as the current QA balance values.

1.0.18 removed the old physical renderer that showed the battery floating below the projector and seeded the dynamic source outside the chassis. 1.0.19 adds the missing packed-light renderer bridge: Mirage virtual block-light is merged into both `LevelRenderer.getLightColor(...)` overloads while preserving vanilla sky light. 1.0.20 hardens that bridge for Sodium by resolving the Mirage field from the active `ClientLevel` instead of calling `getLightEngine()` on Sodium's temporary `LevelSlice`. 1.0.21 keeps that bridge and fixes its runtime invalidation contract: authoritative updates refresh a complete one-section render halo, while moving emitters refresh only the neighboring render sections whose shared face/edge/corner light bytes actually changed. This addresses terrain/wall meshes that otherwise remained stale until an unrelated block update. Jade continues to report mode and battery state.

## Handheld Mirage Flashlight

`mirage_projector:mirage_lantern` is the player-following `DYNAMIC_VISUAL` light. It stores one exact rechargeable ItemStack internally, preserving cell type, components and partial charge.

Since 1.0.19, **normal right-click** cycles `Off -> Focus -> Flood -> Ambient -> Off` and **sneak + right-click opens the Lantern GUI**. Battery service is available only through that GUI; a fresh Lantern defaults to Off. The old opposite-hand battery insert/extract gesture is removed.

Server-side drain occurs once per second only while the Lantern is held or mounted in the Shoulder Slot and its selected mode emits. Current values remain Focus 4/s, Flood 2/s, Ambient 1/s and Off 0/s. Depletion keeps the selected mode but disables emission until a charged cell is installed.

Clients derive stable per-player light sources from vanilla tracked player position/look state. 1.0.18 moves the light origin forward from the player and changes the directional solver to test cone/voxel-volume intersection rather than voxel-center-only intersection, addressing the QA failure where Focus/Flood disappeared for most view angles. Battery percentage mutations no longer trigger a held-item re-equip animation, and the old continuously refreshed local actionbar status was removed; feedback is action/depletion driven instead.

## Mirage Scan Codex / vanilla Lectern library workflow

`mirage_projector:scan_codex` is the physical UUID key to a persistent server-side library of exact frozen entity captures. Browser selection persists the exact scan UUID on the Codex ItemStack while full capture roots remain in Overworld `ScanCodexSavedData`. 1.0.24 caps storage at **25 captures per entity type** and exposes one scrollable/searchable browser in both handheld and vanilla-Lectern modes. Search combines with All/Favorites/Hostile/Passive/Farm/Nether/End/Water/Players/Other tabs. Opening an entry shows the frozen appearance, equipment and player/custom Name Tag; Back preserves the active tab, search and list scroll.

Entity scanning belongs only to the Codex. `mirage_projector:entity_scan_card` is now a passive one-snapshot transport container. A filled card alone in either crafting grid returns a blank card by intentionally deleting its snapshot.

Mounting the Codex on a vanilla `minecraft:lectern` adds physical side-page controls. In entry detail, the duplication page accepts only a Mirage Entity Scan Card and enables **Duplicate** only when that card is blank; the selected canonical scan root is written into that same card. Paper is no longer a duplication ingredient. In the library view, the Import toggle is always available. Importing a filled Mirage Entity Scan Card copies its exact frozen root into the Codex and consumes the source card only after success. Failed imports never consume it.

If Easy Mob Farm is installed, that same import extension also accepts `easy_mob_farm:mob_capture_card`. The optional reflection bridge converts its captured entity state into Mirage's own frozen scan format and consumes the Easy Mob Farm card only on successful insertion. Reverse export into Easy Mob Farm blank cards remains deferred; if implemented later, its balance cost is defined in **experience levels**, not raw XP points.

Favorite state remains Codex-library metadata and is not written onto projector-facing cards. The Codex UI remains non-pausing and intentionally does not invoke vanilla background blur/dimming. Breaking the Lectern continues to use vanilla book-drop behavior, normal vanilla Lectern books remain untouched, and **Take Codex** returns the same mounted Codex.

## Handheld Mirage Projector

`mirage_projector:mirage_hand_projector` is the portable hologram consumer. It stores one exact rechargeable cell and one compact normalized copy of a configured placed Mirage Projector profile. Portable profiles preserve Image/Item/Entity/Banner source identity while clamping the moving presentation to portable limits.

**Normal right-click** is the explicit projection ON/OFF action. **Sneak + right-click** opens the Hand Projector GUI. Battery service, target-profile copying and portable presentation controls now live in that GUI; the old opposite-hand battery service and direct sneak-use profile copy are retired. The item tooltip is intentionally minimal because configuration belongs to the screen.

Since 1.0.12 an enabled Hand Projector remains active when stored anywhere in normal player inventory. 1.0.21 gives the portable hologram a dedicated render entry point that reuses the source renderer without requiring a physical fixed-projector Core; handheld power remains validated from the embedded rechargeable cell before rendering. Stable ItemStack UUID identity plus `PortableProjectorStatePayload` lets remote clients see active hidden-inventory projections without receiving arbitrary inventory contents. Vanilla entity tracking still supplies movement/orientation. Battery drain continues server-side while ON and content-valid.

Banner profiles retain the 1.0.16 Forward/War Banner presentation. War Banner is a smaller pole-free hologram above the owner, either Directional to body yaw or horizontally billboarded per viewer, with bounded size/height stored on the device. 1.0.24 compacts the Hand Projector into a lite portable screen: its player inventory no longer sits beneath an oversized control stack, Banner/War Banner widgets react to the live synchronized device state, and presentation/facing/size/height can appear or update without reopening the GUI.

`mirage_projector:creative_battery` remains an infinite Creative/debug rechargeable medium with no Survival recipe/loot path.

## Mirage Equipment / Shoulder Slot

Mirage Equipment provides a dedicated player equipment socket without consuming armor or the vanilla offhand. The user-facing harness is now **Shoulder Strap**; its historical registry ID remains `mirage_projector:arm_strap` for world/save compatibility.

Since 1.0.18 the player attachment itself stores only the currently equipped Shoulder Strap. The Strap ItemStack owns the Shoulder Device, Battery Pouch and upgrade inventory through vanilla `DataComponents.CONTAINER`. This makes a packed strap a portable mini-morral: multiple straps can be prepared with different batteries/upgrades, removed, stored and exchanged without unpacking their contents. Legacy 1.0.13–1.0.17 attachment layouts are folded into the Strap ItemStack automatically on load.

The inventory extension opens on the **right** side of both the normal Survival inventory and the Creative inventory. With no Strap installed, the expanded Mirage panel exposes only one Shoulder Strap socket. Installing a Strap dynamically reveals the Shoulder Device, six base power-cell slots and two base upgrade sockets. Expansion reveals the final three battery slots and third upgrade socket; inactive positions do not exist visually as X/locked slots. The toggle remains inside the vanilla inventory region below the crafting-result area.

The Shoulder Slot accepts `ShoulderMountableDevice` implementations, currently Mirage Flashlight and Mirage Hand Projector. In 1.0.21 the mounted-item transform uses the corrected positive shoulder-height translation rather than mirroring the device down toward the player feet, and the shoulder Lantern light anchor follows the right-shoulder position while retaining the player look vector for Focus/Flood direction. Right-clicking the occupied Shoulder Device slot opens the same real portable-device GUI used by handheld devices. Device replacement follows normal cursor swap semantics: the previous device stays on the cursor instead of being dropped into the world. The Strap cannot be removed while a device is mounted, but batteries/upgrades travel safely inside it. Shift-hovering a packed Strap in normal inventory exposes a compact contents preview.

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

`ProjectionSettings` network format remains **4** in 1.0.26 and network protocol is **38**. Format 4 activates the historical horizontal/vertical placement slots as signed Wall/Data-show X/Y offsets; when loading pre-4 saves, the old compatibility behavior is retained so legacy positive Vertical Offset is migrated into Lift and old horizontal offset is discarded. Historical chassis ordinals remain unchanged because TABLE and WALL are appended after the original six.

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

`DYNAMIC_VISUAL` is now an operational client-only runtime for moving/portable emitters. Consumers submit moving-source snapshots with stable identity, position, profile, update cadence, camera-cull distance and stale timeout. Directional-cone geometry is solved by the same causal voxel engine, while dynamic fields remain local and never enter the authoritative `STATIC_WORLD` publication channel. The placed Mirage Light Projector and handheld Mirage Flashlight both consume this runtime directly; the latter follows tracked player position/aim and uses stable per-player-hand source identities.

Physical `mirage_projector:crying_light_node` exists only as migration compatibility for old development worlds and is not created by current gameplay.

## Rechargeable energy foundation

Rechargeable energy is ItemStack-owned through `RechargeableEnergyItem`. Mirage Glow Dust carries 1000 units while Light Battery carries 4000; charging cadence remains 10 units / 10 ticks for Glow Dust and 8 / 10 ticks for Light Battery.

1.0.18 tightens the Glow Dust identity boundary. Partial/depleted energy is represented by `mirage_projector:glow_dust`, so it does not satisfy vanilla Glowstone Dust crafting/brewing identity. When that custom medium reaches 100% in a Beacon charger, it normalizes back to vanilla `minecraft:glowstone_dust`, restoring normal vanilla semantics. Conversely, vanilla Glowstone Dust is treated as a fully charged source when inserted into a Mirage device and is normalized to the device-owned custom charge form internally.

Glow Dust tooltips now show only `Discharged` at zero or percentage while partial; fully restored vanilla dust needs no Mirage tooltip. Light Battery tooltips show only percentage/discharged state and no longer explain internal capacity ratios or charger implementation.

Core Booster and Charging Station both use the shared Beacon recharge contract and both normalize completed custom Glow Dust back to vanilla output. In 1.0.32 the Light Battery recipe is frozen around exactly five Glow Dust media (`G C G / I G I / G R G`); the crafting hook transfers the average charge fraction of those five media into the resulting battery. The recipe accepts both vanilla full Glowstone Dust and rechargeable Mirage Glow Dust.

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

The 1.0.32 Survival-progression set is intentionally data-driven with vanilla `minecraft:crafting_shaped` recipes. JEI and EMI both read those rows directly from the vanilla RecipeManager, so Light Battery, Mirage Flashlight, Mirage Light Projector, Shoulder Strap, both Strap patches, Charging Station, Mirage Hand Projector, Mirage Scan Codex, Mirage Table Projector and Mirage Wall Projector appear in the ordinary Crafting category without duplicate custom viewer recipes. The retired `mirage_wall_illuminator` recipe is intentionally absent. The Light Battery row uses the shared `mirage_projector:glow_dust_media` tag, allowing the viewer ingredient slot to cycle between vanilla Glowstone Dust and rechargeable Mirage Glow Dust.

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

1.0.19 corrects the glass-chamber visualization so queued/depleted input stacks render on the rear/input lane and completed outputs render nearest the block's front/output face. Jade now adds the name and live percentage of the active charging cell rather than exposing only inventory contents.

The GUI is enlarged so Input Queue, Charging, Output and player Inventory no longer overlap and there is more clearance for third-party inventory buttons. A block-entity renderer now visualizes the four queued stacks, the active charging item and the four completed-output stacks in distinct physical regions that rotate with station facing.

## Mirage Scan Codex (1.0.17)

The Scan Codex remains a physical UUID key to server-side `ScanCodexSavedData`, supporting multiple independent snapshots, search, filters, favorites and exact scan selection without synchronizing full scan NBT to the browser.

1.0.19 closes the Codex background regression observed in runtime QA. `isPauseScreen()` returns false and `renderBackground(...)` is explicitly overridden as a no-op, preventing vanilla `Screen.render()` from applying its blur/dim pass while the live world continues behind the Codex panel.

## Deferred to later releases

1.0.17 does not include:

- handheld projector self-configuration UI (the 1.0.11 device currently copies from placed projectors rather than opening its own source workspace);
- Dragon Egg / End Resonance gameplay;
- direct grab/free-rotate hologram manipulation;
- UV Shoulder Light / Auto UV / UV Marks ecosystem (reserved for 1.2.0);
- Create Blueprint projection source.

See `ROADMAP.md`, `WAITLIST-1.1.0.md` and `WAITLIST-1.2.0.md`.
