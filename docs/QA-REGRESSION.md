# QA Regression Matrix — Mirage Projector 1.0.117

Use this matrix after any 1.0.x patch/compatibility change, and as the baseline before integrating the 1.1.0 feature expansion.

## Build / startup

- Build with Java 21 and NeoForge 21.1.244+.
- Launch with neither EMI nor JEI installed.
- Launch with EMI only.
- Launch with JEI only.
- Launch with EMI + JEI together.
- Confirm client/server protocol agreement at protocol 44.

## Placement and fixed-tab UI

- Lift is the only vertical placement axis and is clamped to zero or above.
- Geometry / Placement / Rotation / Floating are the four mutually exclusive settings tabs in one fixed-height area; Lighting, Ghost/opacity and Tint live under Geometry.
- Switching settings tabs never changes the Y position of Source Workspaces, Power / Capacity, Core slot, inventory or Apply / Cancel.
- At 1920×1080 GUI Scale 2 the main projector screen does not activate responsive overflow; smaller effective heights retain the scrollbar fallback.
- Horizontal Offset is not exposed; any legacy stored horizontal value sanitizes to zero.
- Tilt accepts the full -90° to +90° range for Image/GIF, Item, Banner and Entity projection.
- Plane projector Rotation remains centered on the projector rather than orbiting around a displaced point.
- Mirage Prism exposes Distance only while using Image/GIF or Banner sources.
- Prism Distance expands the four-face cross without changing projection Scale.
- At Prism Distance +0 px and Tilt 0°, equal adjacent faces meet at their lower corners/edges without overlapping. Mixed-size faces use adjacent-pair collision floors rather than the largest face globally.
- Positive/outward Tilt does not add artificial Prism spacing: at +45° the lower edges may remain touching at +0 px. Negative/inward Tilt raises the minimum Distance only when required.
- Prism Rotation turns the entire four-face cross around the central projector like a carousel.
- Prism face Tilt is local to each radial face and does not collapse adjacent faces through one another. If an inward angle would require more than +160 px (10 blocks) of extra radius, that angle is not accepted.
- Prism Distance cannot be manually raised beyond +160 px. Increasing it adds a modest PU cost; Tilt-required extra radial spacing is charged by the same small rule, while the no-tilt collision-safe radius is free.
- Reset Placement restores Lift and Prism Distance to their safe baseline; Reset Tilt restores identity orientation.
- Clearance preview and BER culling include Prism radial Distance and Tilt reach.
- A 1.0.0 world remains compatible; legacy horizontal placement is ignored and positive experimental Vertical Offset values are absorbed into Lift.

## Projector chassis

For each of the six chassis:

- place/orient the block;
- open the GUI;
- install/remove a Core;
- verify model ↔ VoxelShape alignment;
- verify inventory/held/dropped item presentation;
- verify save/reload and chunk unload/reload.

Chassis order:

1. Mirage Projector
2. Mirage Display
3. Mirage Field Projector
4. Wide Mirage Projector
5. Tall Mirage Projector
6. Mirage Prism

## Projection source matrix

Smoke-test every chassis with:

- Image/GIF;
- Item;
- Banner;
- Entity.

For every source:

- `Use <mode> mode` selects and activates it;
- active-source feedback is correct;
- `TURN OFF` hides the projection without deleting state;
- reopening workspaces does not silently change active source;
- Scale/Lift/Float/rotation/tint/ghost controls persist;
- large Lift/Scale projections recover correctly when entering/leaving view.

## Image / GIF

- PNG/JPG/static WebP/BMP import.
- animated GIF playback.
- server/client asset synchronization.
- cache miss/refetch.
- Wide/Tall multi-source layout.
- Prism cardinal faces.
- Field continuous plane.
- flip/scanlines/front-back behavior.

## Item / Banner

- virtual snapshot does not consume the source item/banner;
- block items render volumetrically where expected;
- clearing virtual state does not delete real inventory content;
- Prism banner cardinal faces remain independent.

## Entity

- Player skin/model parts;
- generic living entity snapshots;
- Humanoid armor/held items;
- Horse Saddle/Body Armor;
- pose presets;
- nameplates/custom names;
- per-channel equipment visibility;
- Ghost opacity/tint at 99/90/50/10%;
- glint/trims/eyes;
- Create Netherite Backtank compatibility;
- water/cloud/weather composition;
- conservative culling at large Lift/Scale.

Passenger/vehicle composite scans must remain rejected rather than partially serialized.

## Projection Power

- Glass/Quartz/Amethyst/Diamond/Netherite base Core capacity.
- dynamic feasible Scale/Lift/Float limits.
- Overdrive behavior with sufficient PU.
- Core Booster ×1.50 amplification.
- loaded Booster material persistence and extraction.

## Crying Obsidian

- shard crafting and Crying Obsidian reconstruction recipes;
- lava-above-Crying-Obsidian growth sequence;
- Small → Medium → Large → Cluster ordering;
- Silk Touch preserves stage;
- normal harvest produces fixed shard drops;
- Fortune does not multiply shards;
- Beacon attenuation/energization;
- material-specific Core Booster relay effects;
- Obsidian Spike damage/placement.

## Mirage Light Engine

- open curve `15,15,14,14,...,1,1`;
- 1/2/3-block walls;
- L-corner detours;
- slabs/stairs/partial opacity;
- chunk boundaries;
- unload/reload/relog/respawn/dimension changes;
- overlapping sources use maximum contribution;
- several simultaneous sources do not visibly stall the server;
- Glass diffusion, Quartz reach, Amethyst resonance, Diamond focus and Netherite inversion identities;
- two-client tracking/synchronization.

No current gameplay path may create `mirage_projector:crying_light_node`; that block is migration-only.

### Dynamic Mirage Light foundation (1.0.5)

- client `DYNAMIC_VISUAL` updates must never create or replace authoritative STATIC_WORLD chunk snapshots;
- omnidirectional dynamic profiles preserve existing solver behavior;
- narrow and wide `DIRECTIONAL_CONE` profiles reject cells behind/outside the beam while retaining obstacle occlusion inside the beam;
- moving one source reuses its stable source ID rather than accumulating orphaned fields;
- camera-culling/removal invalidates the previously touched render sections;
- stale submitted sources disappear automatically;
- a field clipped by unavailable chunks retries after those chunks become queryable;
- logout/world changes leave no dynamic source contributions behind;
- static Mature Cluster two-client synchronization remains unchanged with dynamic foundation code present.

## Rechargeable Glow Dust (1.0.7)

- fresh/default Glow Dust shows full charge and no depleted name;
- partial charge preserves its exact percentage through inventory moves, save/reload and Core Booster extraction;
- depleted Glow Dust reads `Discharged` / `Sin Cargar`, shows an empty charge bar and visibly darkens;
- right-clicking a Core Booster with Glow Dust inserts exactly one cell without replacing the loaded Core material;
- sneak + right-click with an empty hand removes the charging cell before Core extraction;
- no Beacon below / blocked Beam = no charge gain;
- one clear active Beacon + one charger reaches full charge from empty in roughly 50 seconds with the current baseline;
- five vertically stacked active chargers can charge simultaneously on a clear beam; a sixth above them receives no power;
- each active charger visibly weakens the outgoing Beam by one fifth of full transmission;
- a charger reaching 100% stops attenuating the Beam;
- Crying Obsidian above the chargers responds to the attenuated transmission;
- breaking a Core Booster in Survival returns the inserted Glow Dust and preserves its charge state.

## Mirage Light Projector / DYNAMIC_VISUAL consumer (current 1.0.21 contract)

- Place the projector facing each horizontal direction and confirm the physical lens and dynamic source follow block facing.
- Normal right-click cycles exactly Focus -> Flood -> Ambient -> Off -> Focus; it must not insert/extract a battery.
- Sneak + right-click opens the Light Projector GUI. In the GUI, insert exactly one rechargeable cell and confirm the slot preserves its exact medium/components/charge.
- The GUI cycle button must also change the same persisted mode without creating a second mode state.
- Confirm Focus produces a narrow long directional field, Flood a wider shorter field, Ambient a local omnidirectional field, and Off removes its dynamic field.
- Rotate the projector through all four horizontal facings and verify Focus/Flood emit reliably rather than disappearing at most angles. The source origin must start outside the chassis and must not self-occlude on its own block.
- Confirm nearby opaque blocks occlude/reroute Focus/Flood through the Mirage solver rather than behaving as vanilla invisible light sources.
- Confirm server drain occurs once per second at the current QA values 4/2/1/0.
- Drain a cell to zero: selected mode must persist, emission must stop, and replacing/recharging the cell from the GUI must resume that selected mode without resetting it.
- Save/reload the world and relog a second client: mode, cell type and exact charge must agree.
- Break the projector with a cell installed: the projector block and inserted cell must both survive as drops; no cell duplication or deletion.
- No battery ItemStack may render floating beneath/inside the placed projector.
- Jade should show current mode and battery state, or the explicit empty state.
- Verify multiple nearby light projectors keep separate stable source IDs and stale/off/depleted contributions are removed.

## Mirage Flashlight / player-following DYNAMIC_VISUAL consumer (legacy 1.0.21 contract, current Flashlight identity)

- A fresh Mirage Flashlight starts in Off.
- Normal right-click cycles exactly Off -> Focus -> Flood -> Ambient -> Off. It must never insert/extract the cell directly.
- Sneak + right-click opens the Lantern GUI. Battery insertion/extraction is possible only through the GUI; the old opposite-hand service gesture must do nothing.
- Focus must follow the player's current look direction while rotating/moving; Flood follows the same transform with the wider profile; Ambient follows the player omnidirectionally.
- Test Focus/Flood while looking horizontally, diagonally, upward and downward. The field must not disappear merely because the cone misses voxel centers; the 1.0.18 cone/voxel-volume intersection must remain effective. Since 1.0.19, also verify that the same field visibly illuminates world geometry, not only Simple Light Level/debug overlays; in 1.0.20 this additionally validates that the packed-light renderer bridge survives Sodium chunk meshing without touching `LevelSlice#getLightEngine()`.
- Off removes the local dynamic source and consumes no charge.
- Server drain occurs once per second while the Lantern is held or mounted in the Shoulder Slot; an ordinary inventory-stored Lantern does not drain.
- Each battery percentage update must NOT trigger the vanilla held-item re-equip/swap animation.
- The old continuously refreshed actionbar status must not remain after the Lantern is unheld. Action/depletion feedback may appear transiently only.
- Depleting a cell stops emission without resetting the selected mode; replacing/recharging the cell through the GUI resumes that mode.
- Test main-hand, off-hand and Shoulder Slot lanterns; stable source identities must not collide.
- Two-player QA: each client sees the other player's held/mounted Lantern light follow position/aim and mode/depletion state.
- Dropping/unholding/logging out removes stale player-following dynamic fields within the normal stale window.
- Save/reload with a partial cell installed: mode, cell type and exact charge survive.
- Verify the Lantern item remains 16x16 and GUI battery manipulation never changes the installed medium except by explicit player action/drain.

## Rechargeable media / Light Battery (1.0.8)

- Glow Dust still loads old partial/depleted charge values correctly.
- Light Battery defaults to 4000/4000 and persists exact partial/depleted charge through inventory moves and save/reload.
- with one clear active Beacon, an empty Light Battery reaches full in roughly 250 seconds at the current +8 / 10-tick baseline.
- inserting either incomplete Glow Dust or Light Battery into a supported charger preserves exact partial charge and does not replace the Core Booster material socket.
- Core Booster historical direct cradle interaction remains a separate legacy charger path; portable devices use their real GUIs instead.
- a fully restored custom Glow Dust output normalizes back to vanilla Glowstone Dust; partial/depleted Glow Dust remains the Mirage rechargeable medium.
- charging Glow Dust and Light Battery render centrally without visibly clipping through the Core Booster glass.
- Jade shows the inserted medium name + charge percentage, and shows an explicit empty charging-cell state when vacant.
- a full rechargeable medium stops attenuating the Beacon; any actively charging supported medium uses the same 0.20 transmission cost.
- five active rechargeable cells remain the clear-column ceiling; a sixth receives no charge.
- language-key parity is preserved across en_us/es_cl/es_es.

## EMI / JEI

### EMI

- Crafting tab appears for every projector chassis.
- Crying Obsidian has both shard-based crafting variants.
- World Interaction page opens from Crying Obsidian, all buds, Cluster and Shard.
- growth arrows/icons do not overlap.
- harvest examples show Silk Touch → Cluster and normal pickaxe → Shards.
- Block Drops order is Small → Medium → Large → Cluster.

### JEI

- custom projector upgrades appear under Crafting;
- Crying Obsidian shaped recipes appear normally;
- projector/Crying Obsidian ingredient information is present;
- the eleven surviving 1.0.32 Survival recipes appear once each under Crafting: Light Battery, Mirage Flashlight, Mirage Light Projector, Shoulder Strap, Auto Battery Swap Patch, Shoulder Strap Slot Expansion, Charging Station, Mirage Hand Projector, Mirage Scan Codex, Mirage Table Projector and Mirage Wall Projector; the retired `mirage_wall_illuminator` recipe is absent;
- the Light Battery grid is `GCG / IGI / GRG`, and each `G` slot accepts/cycles both vanilla Glowstone Dust and rechargeable Mirage Glow Dust.

### EMI 1.0.32 Survival progression

- the same eleven surviving 1.0.32 Survival recipes appear once each under Crafting, with no retired wall-illuminator entry;
- none is duplicated by a Mirage synthetic EMI wrapper;
- the Light Battery grid is `GCG / IGI / GRG`, with `mirage_projector:glow_dust_media` exposing both supported Glow Dust media;
- looking up either a recipe output or one of its ingredients opens the expected Crafting row.

## Persistence / migration

- current-world save/reload preserves all four source families and transforms;
- legacy numeric projection-source saves migrate to stable namespaced IDs;
- migration-only Improved Core blocks convert safely;
- legacy physical Mirage light nodes are cleaned without deleting unrelated blocks;
- unknown future source IDs/payloads are preserved where supported.

## Release packaging

- `logo.png` appears in NeoForge metadata without blur;
- public version is correct;
- README/current docs describe stable behavior rather than development history;
- source-side suite passes:

```text
python tools/verify_current_line.py
```

## 1.0.20 Sodium packed-light bridge hotfix

- Creative inventory must expose the same Mirage Equipment toggle/panel instead of hiding the Shoulder Strap socket.
- Portable Device, Mirage Light Projector and Charging Station container slots must show normal item hover tooltips.
- Lantern GUI must use the compact layout without title/mode overlap or the large Hand Projector dead space.
- Charging Station world visualization must place queue/input stacks on the back/input lane and completed outputs on the front/output lane.
- Jade must add the currently charging cell name + live percent when a Charging Station has an active cell.
- Scan Codex must keep the live world sharp: no pause and no vanilla blurred/dim `renderBackground` pass.
- Existing floor Mirage Light Projector remains a supported device. Its later visible yaw/pitch aiming, a separate wall projector and handheld Lantern ground placement are future 1.1.0 work, not regressions of this wave.


## 1.0.21 runtime QA corrections

- Move a Focus/Flood/Ambient Lantern across chunk-section boundaries in darkness. Previously lit terrain must darken again without breaking blocks, and newly reached terrain/walls must illuminate without walking close enough to trigger an unrelated rebuild.
- Repeat with Sodium enabled and watch broad/tall walls plus floor/ceiling boundaries; no stale rectangular mesh patches should persist.
- Equip Mirage Flashlight in Shoulder Device and inspect third person from multiple angles. The physical item must sit near the right shoulder rather than below the feet; its light origin should follow the shoulder while Focus/Flood direction follows the player look vector.
- Configure a Mirage Hand Projector, insert a charged cell and enable projection from both RMB and GUI. Non-War-Banner Image/Item/Entity/Banner content must render even though the portable projector has no physical fixed-projector Core.
- Disable/re-enable and move the active Hand Projector between hand, inventory and Shoulder Device; persistent-state rendering/drain must remain intact.

## 1.0.117 Shoulder Strap and War Banner stabilization

- Open the inventory in Survival and Creative. The same shoulder toggle must appear immediately, and the same panel must open or close without reopening the inventory.
- Exercise every Shoulder Strap slot with click, release, drag and occupied-slot replacement. The previous stack must remain on the cursor; no stack may drop or duplicate.
- Close and reopen the inventory with a carried stack after a shoulder-slot action. The server-corrected cursor must remain consistent.
- Remove a populated Strap and equip it again. Its mounted device, item compartment and upgrades must round-trip unchanged.
- Verify the base 2×4 item grid accepts rechargeable media, Flashlights, Hand Projectors, Scan Codices and empty or filled Entity Scan Cards. Expansion must add exactly one four-slot row.
- Inspect shoulder devices and War Banners in first and third person. The owner must not see either attachment in first person; third-person anchors must follow the player body without world-space jumping.
- Compare War Banner facing modes from multiple clients. Directional must follow body yaw; Always Face Viewer must rotate independently for each viewer without changing its anchor.
- Sweep War Banner size from 0–100% and height from -32–+32 px, then reopen the GUI and verify the synchronized values persist.


## 1.0.23 Scan Codex + vanilla Lectern duplication

1. Put a Mirage Scan Codex on an empty vanilla Lectern; verify the vanilla Lectern visibly enters its occupied/book-present state and the Codex leaves the player's hand.
2. Right-click the occupied Lectern; verify the custom Mirage Scan Codex browser opens instead of vanilla LecternScreen.
3. Verify the browser remains non-pausing and does not blur/dim the world, and that its presentation reads as a two-page book rather than a flat dark utility panel.
4. Select one stored capture, keep one Paper in Survival inventory and press Duplicate Scan; verify exactly one Paper is consumed and exactly one physical Entity Scan Card is created.
5. Project that card and confirm it reproduces the selected frozen capture, including player skin/name/equipment/nameplate where applicable.
6. Duplicate the same selected capture twice; verify both outputs represent the same `ScanId` while the Codex entry remains intact.
7. Select a different capture of the same entity type and duplicate again; verify the output follows that exact snapshot rather than species/type alone.
8. Remove all Paper; verify no card is created. In Creative, verify duplication succeeds without consuming Paper.
9. Fill the player inventory and duplicate; verify the completed card drops once at the player rather than disappearing.
10. Press Take Codex; verify the exact same physical Codex returns with its library UUID and selected capture intact.
11. Break a Lectern while the Codex is mounted and verify vanilla book-drop behavior returns the Codex.
12. Place normal vanilla books/book-and-quill on Lecterns and verify their vanilla interaction/UI remains unchanged.
13. Open a held Codex normally and verify duplication/Take Codex controls are absent.
14. Verify no standalone Mirage Duplicating Lectern exists in Creative or the registry/resource inventory.

## 1.0.25 Table / Wall Data-show QA

- [ ] Table places only with support below and survives normal floor/table support.
- [ ] Table Image and Banner sources lie horizontally; Item and Entity projections remain upright.
- [ ] Sneak + empty-hand RMB picks up Table as one stateful item with Core/source/settings preserved and no duplicate drops.
- [ ] Removing Table support drops exactly one packed stateful projector.
- [ ] Wall/Data-show can be placed on full flat blocks facing N/E/S/W; top slabs, stairs and partial collision supports are rejected.
- [ ] Wall physical collision/model stays below slab height and its lens faces the block `FACING` direction.
- [ ] Wall is Image-only; Item/Entity/Banner source buttons/workspaces are unavailable.
- [ ] With a clean wall ahead, square/tall/16:9 images preserve aspect ratio and project directly on the wall plane.
- [ ] Build an irregular area outside a tall image's narrow footprint; projection remains valid. Repeat with unused space above/below a 16:9 footprint.
- [ ] Put a stair/slab/recess/protrusion inside the actual image footprint; projection cancels or scales down until no image-covered cell is irregular.
- [ ] X and Y offsets move the real validated image footprint independently and can move it from an invalid patch to a valid patch (and vice versa).
- [ ] Increase requested Scale beyond available clean wall area; resolved Scale reduces without cropping/stretching.
- [ ] Increase projector-to-wall distance and verify used PU increases; sufficiently distant/large projections stop when the Core budget cannot satisfy even the minimum valid scale.
- [ ] Place an obstruction between projector and any image-covered wall cell; projection cancels. Obstructions outside the image footprint do not matter.
- [ ] Load several images in the Wall playlist, reorder them, set current, use Previous/Next, close/reopen/reload and verify order/current slide persist.
- [ ] Removing Wall floor/table support drops exactly one packed stateful projector preserving playlist/current slide/Core/settings.
- [ ] Compact/Display/Wide/Tall/Field/Prism placement/render/power behavior shows no regression.

## 1.0.26 Presentation Deck / Data-show Remote QA

1. Load 2–9 images into both Table and Wall/Data-show Presentation Decks; reorder and reload the world; order/current slide must persist.
2. Enable Automatic Presentation at several values including 1 s, 10 s and 120 s. With two or more slides, advance must happen at the configured interval; with zero/one image it must remain stable.
3. Disable Automatic Presentation. Wait longer than the configured interval; the current slide must not change.
4. While automatic mode is active, select another slide from the GUI. The already-running countdown must continue rather than restarting.
5. Insert a Presentation Remote into the physical Data-show top dock. It must bind and be visibly rendered on top of the projector.
6. Sneak + empty-hand RMB retrieves the remote. A second sneak + empty-hand RMB may then pack the projector normally.
7. Hold RMB with a bound remote: lightweight overlay appears without pausing/dimming/blur, begins on `-`, tracks left/right mouse movement and commits exactly one `<--` or `-->` step on RMB release.
8. Manual remote navigation while auto mode is active must keep automatic mode ON and preserve the existing elapsed timer/cadence.
9. A remote in another dimension must fail cleanly. An unloaded target chunk must not be force-loaded.
10. Replace a paired Data-show at the same coordinates with a different projector. The old remote must reject it because the persistent link UUID differs.
11. Create a Wall slide whose actual image footprint intersects irregular/obstructed wall terrain. Selecting it must advance the deck index, suppress the image and show the transparent red circle/slash cancellation marker.
12. From the invalid slide, Previous/Next and automatic playback must remain able to reach neighboring valid slides.
13. Confirm the invalidity test ignores irregular terrain outside the actual aspect-correct image rectangle, including tall and 16:9 images.
14. Pack/move/re-place a Data-show with automatic state and a docked remote. Deck and automation state must survive; extracting the remote must refresh its location binding.
15. Multiplayer: a bound remote action is server-authoritative and changes the same active slide for all tracking clients.


### Table dedicated runtime / rotation visibility

- Open a Mirage Table Projector and compare its main screen against a normal fixed projector: widget positions, tabs, source buttons, Placement/Rotation/Floating controls, Apply and Cancel must remain 1:1.
- Keep the camera completely still above the Table, set Tilt to 0, and sweep Rotation Offset through the entire 0–360° range in both directions. The image must remain rendered for the full revolution; Rotation may change only in-plane reading direction.
- Enable automatic Rotation and observe at least two complete revolutions without moving the camera. No ~80–90° disappearance window is allowed.
- Repeat with positive and negative Tilt. Front/back changes must follow the physically tilted plane, not an independent yaw threshold.
- Repeat with X/Z offsets and high Lift; the projection must not disappear because the physical chassis leaves the frustum.
- Verify Image/Banner remain horizontal and Item/Entity remain upright above the same Table anchor.
- Reopen the Table UI immediately after placement/chunk load. It must route to the Table-specific screen from server-provided chassis identity, without relying on a client BlockEntity synchronization race.
