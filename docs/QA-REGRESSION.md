# QA Regression Matrix — Mirage Projector 1.0.18

Use this matrix after any 1.0.x patch/compatibility change, and as the baseline before integrating the 1.1.0 feature expansion.

## Build / startup

- Build with Java 21 and NeoForge 21.1.244+.
- Launch with neither EMI nor JEI installed.
- Launch with EMI only.
- Launch with JEI only.
- Launch with EMI + JEI together.
- Confirm client/server protocol agreement at protocol 31.

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

## Mirage Light Projector / DYNAMIC_VISUAL consumer (current 1.0.18 contract)

- Place the projector facing each horizontal direction and confirm the physical lens and dynamic source follow block facing.
- Normal right-click opens the Light Projector GUI; direct world gestures must not insert/extract a battery or cycle modes.
- In the GUI, insert exactly one rechargeable cell and confirm the slot preserves its exact medium/components/charge.
- Cycle exactly Focus -> Flood -> Ambient -> Off -> Focus through the GUI control.
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

## Mirage Lantern / player-following DYNAMIC_VISUAL consumer (current 1.0.18 contract)

- A fresh Mirage Lantern starts in Off.
- Normal right-click opens the Lantern GUI. Battery insertion/extraction is possible only through the GUI; the old opposite-hand service gesture must do nothing.
- Sneak + right-click cycles exactly Off -> Focus -> Flood -> Ambient -> Off.
- Focus must follow the player's current look direction while rotating/moving; Flood follows the same transform with the wider profile; Ambient follows the player omnidirectionally.
- Test Focus/Flood while looking horizontally, diagonally, upward and downward. The field must not disappear merely because the cone misses voxel centers; the 1.0.18 cone/voxel-volume intersection must remain effective.
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
- projector/Crying Obsidian ingredient information is present.

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
