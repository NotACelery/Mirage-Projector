# Mirage Projector — Regression QA

Use this list after dev.59 and after any major rendering/state change.

## Build/bootstrap

- `build.bat` succeeds on Java 21.
- Minecraft reaches title screen with the mod enabled.
- No required Mixin target failure.
- Existing world loads without missing-registry corruption.

## Projector state

- place each of the six chassis in all horizontal facings;
- save/reload;
- normal Survival break returns exactly one stateful projector;
- re-place and verify Core, source and presentation state;
- upgrade Compact → Display and Display → each specialist with heavily populated state;
- verify custom item name survives stateful crafting where expected.

## Power/UI

Test every standard Core material and all five loaded Core Booster states across all chassis:

- capacity value;
- Scale/Lift/Float dynamic endpoints;
- nominal → overdrive transition;
- Ghost rebate;
- no-Core failure;
- Float amplitude cannot exceed Lift;
- GUI at multiple GUI scales/window sizes.

## Image/GIF

- PNG, JPEG, static WebP, BMP;
- renamed GIF still animates;
- animated WebP/APNG rejects clearly;
- Plane Front/Back/Mirrored/Readable/Independent;
- Wide SINGLE + 4×1 MULTI;
- Tall SINGLE + 1×4 MULTI;
- Field one continuous image;
- Prism independent N/E/S/W mixed aspect ratios;
- save/reload all layouts;
- multi-client server fetch after deleting one client's cache.

## Item/Banner

- virtual snapshot never consumes the original item/banner;
- clear virtual snapshot;
- enchanted/glint item;
- block item;
- modded item;
- Plane banner cloth;
- Prism four banners;
- save/reload.

## Entity

At minimum test:

- Chicken or another small mob;
- Baby Zombie;
- adult Humanoid;
- Player;
- Horse;
- large Generic entity;
- Piglin in Overworld;
- custom-named entity.

Verify:

- frozen pose/body;
- custom name placement;
- Humanoid six equipment channels;
- bodyless rig;
- Horse Saddle/Body Armor;
- staging items return safely;
- changing entity family clears incompatible virtual equipment;
- save/reload;
- no real entity is spawned/ticked by the projection.

## Ghost/render ordering

This is P0 after dev.58:

- opacity 100%, 99%, 90%, 50%, 10%;
- rotate camera through front/back/side/high/low angles at every non-100% opacity;
- clouds behind/intersecting a giant projection;
- water in front and behind;
- overlapping translucent projected entities;
- check body/armor/custom-equipment surfaces for z-fighting or camera-dependent layer flips;
- vanilla armor;
- armor trim;
- enchanted/glint equipment;
- held shield/tool/block;
- glowing eyes/special RenderTypes;
- Create Netherite Backtank, specifically comparing the separate tank geometry against the synthetic `netherite_diving_layer_1/2` chestpiece;
- at 90%, 50% and 10% opacity, verify the two-layer diving chestpiece fades approximately like one ordinary armor layer rather than accumulating two full-opacity Ghost passes;
- repeat the custom armor test with foil/glint active to exercise simultaneous render consumers.

Acceptance requires no opacity-triggered disappearance, no cloud-overwrite artifact, no water depth-hole regression, no crash from simultaneous consumers and no obvious self-depth fighting on the Backtank/body/armor stack.

## Core Booster

- empty placement;
- insert each supported material;
- reject unsupported material;
- Shift + right-click extract;
- visual center disappears immediately when emptied;
- save/reload preserves material and center renderer without requiring a neighbor block update;
- break with correct pickaxe without Silk Touch and preserve material;
- stacking only with identical material state;
- empty Booster rejected by projector Core slot;
- loaded Booster accepted;
- legacy improved_* block placements migrate to equivalent loaded Booster.

## Crying Obsidian growth

- lava source above Crying Obsidian;
- flowing lava above Crying Obsidian;
- air below;
- water below;
- accelerated random-tick QA;
- Small → Medium → Large → Mature;
- no-Silk drops 1/2/3/4;
- Fortune does not increase drops;
- Silk Touch stage preservation;
- directional replacement/decoration.

## Beacon/Crying Obsidian optics

- Small/Medium/Large attenuation visually differs 75/50/25%;
- vertical beam stops at pixel 0 and resumes only above non-Mature crystal;
- buds emit no block light from excitation;
- Mature emits current level-15 light;
- Mature directly on Beacon suppresses vertical beam;
- Beacon block-light suppression/restoration behaves correctly;
- energized Mature texture changes to stronger purple/lavender;
- residual ray origin is centered X/Z at about Y pixel 2;
- residual ray is instantly full length, holds, then retracts/fades;
- stage ray width/length/frequency scaling is visible.

## Obsidian Spike

- recipe;
- inventory sprite stays inside slot bounds;
- placement/collision behavior;
- movement slowdown;
- 2 damage points per successful movement hurt event;
- no artificial rapid repeated damage to a stationary entity.

## dev.64 Create Netherite Backtank single-surface Ghost

- Equip/capture `create:netherite_backtank` on a Humanoid/Player projection.
- At 100% opacity, verify Create's native two-layer diving chest and separate tank remain unchanged.
- At 99%, 90%, 50% and 10% opacity, verify the chest and tank fade at comparable rates.
- Verify the chest no longer looks like two or three translucent copies stacked over the torso.
- Orbit above, below, front, rear and both sides; verify no synthetic-chest z-fighting or depth flipping.
- If the Backtank has foil/glint, verify no crash and no orphaned inner-layer glint.
- Recheck clouds behind the projection and water behind/in front of the projection.
- Recheck ordinary vanilla chestplates to ensure the Create-only rule does not alter them.


## dev.65 powered Crying Obsidian lighting
Historical regression only: confirm the internal Mirage light-node block remains non-item, replaceable, no-collision/no-drop and does not overwrite ordinary terrain. The original 8/16/24 ring strengths and width-derived tier values are superseded by dev.67–69 and must not be used as current expected output.

## dev.66 light-profile foundation

- Compile with Java 21 and confirm the new `light` package resolves without changing network protocol.
- Confirm the dev.65 Mature-cluster range tiers remain behaviorally unchanged.
- Confirm no world node path references `CONCENTRATE`, `DIRECTIONAL_SPOT` or `ROTATING_DIRECTIONAL_SPOT`; those modes are placeholders only.
- Confirm `build.bat` and the cleaner are unchanged because dev.66 adds files but deprecates/moves none.


## dev.67 Beacon optics and half-decay light field

- Build on Java 21 and confirm protocol remains 18.
- Verify a loaded Core Booster changes the beam exactly at `8.5/16` of its own block: lower half retains incoming width/brightness/rotation and upper half receives the material effect.
- Verify Small/Medium/Large beam continuation begins at `4.5/16`, `6.5/16`, `8.5/16`; Mature continues to stop the vertical beam entirely.
- Rotate the camera around vanilla, relayed and post-bud beam segments. No accidental diagonal side may create the former N/X topology.
- Confirm residual bursts appear again for Small/Medium/Large/Mature while energized, originate visually at pixel 2 and still stop at later solid obstacles.
- Verify Small/Medium/Large energized buds remain zero block-light sources while their residual bursts still appear. Verify Mature emits level 15 and, along an unobstructed axis, its field approximately repeats each light value twice before falling (`5,5,4,4,3,3...`).
- Verify removing the Beacon/crystal or blocking a node position causes auxiliary light to contract without leaving persistent ghost light.
- Verify relay/Radiance can strengthen the field but no block/node ever emits above vanilla level 15.
- Re-run the Create Netherite Backtank Ghost test to confirm the optics changes do not regress the now-working full fade.
- Confirm `build.bat` and cleaner are unchanged because dev.67 deprecates/moves no project files.


## dev.68 Core Booster extraction safety

- Shift-right-click loaded Booster with empty hand: stored core extracts and lands in that hand.
- Shift-right-click loaded Booster while holding the same core item: extraction is allowed and stacks when components/space permit.
- Shift-right-click loaded Booster while holding a different item/tool: core remains installed and Mirage does not cancel the other interaction.
- Normal right-click insertion behavior remains unchanged.

## dev.68 Mature Cluster light teardown

- Beacon-energized Mature Cluster with zero Core Boosters still produces the slow-decay field.
- Compare axial light levels against a normal level-15 source; each level should persist for roughly two blocks.
- Break the Mature Cluster while standing in the extended field: auxiliary illumination must begin disappearing in the same tick/render update cadence as a normal light source, with no scheduled 0.5–1 s hold.
- Test two overlapping Mature fields and break one: shared nodes must downgrade/remain according to the surviving source rather than being blindly deleted.
- De-energize a Mature Cluster without breaking it and verify the same immediate node reconciliation once the optics state detects de-energization.

## dev.69 occlusion-aware reflected light — historical/superseded by dev.70

- Build on Windows/Java 21; protocol must remain 18.
- With zero Boosters, an energized Mature Cluster must preserve the dev.68 six-axis half-decay baseline and ~29-block unobstructed axial reach.
- Place a full opaque wall across one primary branch. No downstream Mirage node/light island may appear on the far side solely from that branch; natural vanilla wrap around the wall edges is allowed.
- Compare full opaque, partial-opacity and open paths. Partial blockers should attenuate downstream reflected strength instead of behaving identically to air or cutting the branch completely.
- Orbit solid blocks around the field and verify opposite/non-exposed faces are not artificially fullbright. Mirage does not manually shade faces; accepted nodes must leave local shading/AO to vanilla.
- Historical dev.69-only check: Glass/Diffusion originally created shorter face-diagonal static-light branches. **Do not expect this in dev.70**; those branches were removed after live QA exposed wall leakage.
- Quartz/Radiance: verify it is the strongest static range/persistence amplifier. Four Quartz Boosters may reach the maximum ~37-block axial candidate distance but no real node emits above 15.
- Diamond/Focus: verify axial persistence increases more modestly than Quartz and mixed Diamond cancels Glass diffusion tier-for-tier.
- Amethyst/Resonance: verify residual bursts/rotation become more active without granting free static field range.
- Netherite/Inversion: verify reflected residual rotation reverses without granting free static field range.
- Mixed stacks: verify only the first four loaded Boosters contribute and changing their materials contracts/expands the field cleanly without orphan nodes.
- Break/de-energize a Mature source and retest overlapping sources; dev.68 immediate reconciliation must still hold for both axial and diffuse nodes.
- Re-run residual-ray collision clipping and Create Netherite Backtank Ghost regression to ensure the light work does not affect those render paths.


## dev.70 instant occlusion rebuild

- With one energized Mature Cluster and no Booster, place a full opaque block directly on +X/+Z/-X/-Z branch paths at several distances; downstream Mirage light must disappear by the end of the same server tick rather than after the 20/40-tick fallback.
- Remove the blocker; the axial branch must repopulate by the end of that tick and the client light engine should begin updating immediately.
- Build a wall wider/taller than vanilla corner-wrap distance. No Mirage-owned static node may exist on the far side of the blocked axial branch. Any remaining weak edge illumination must come only from vanilla propagation around an actual edge.
- Repeat with Glass Boosters. dev.70 must not recreate long face-diagonal static nodes; Glass should remain visible only in residual reflected-ray geometry.
- Repeat with Quartz and Diamond and confirm their existing axial range differences remain.
- Place/remove several blocks in one tick or use a multi-block placement such as a bed; each impacted Mature source should rebuild once without visible staged node teardown.
- Upgrade a world containing dev.69 Glass diffusion nodes; the first Mature refresh/removal must reconcile those legacy diagonal nodes without deleting an axial node still required by another source.
- Confirm overlapping Mature sources retain the strongest valid node contribution when one branch is blocked or one source is removed.


## dev.73 explicit full-range half-decay relays

- Build on Windows/Java 21; network protocol remains 19.
- Test an energized Mature Cluster with zero Core Boosters on a long flat opaque floor using a block-light overlay such as the Simple Light Level backport.
- Along an unobstructed primary axis, positions 1–30 away from the Cluster must read exactly `15,15,14,14,13,13,...,2,2,1,1` before outside/world light is considered. No middle-only doubling is acceptable.
- In particular verify the two QA failures that motivated dev.73: level 15 must occupy two positions, and every level from 10 down through 1 must also occupy two consecutive positions.
- With Quartz/Radiance, extra conceptual power must extend the saturated level-15 plateau and total axial tail rather than producing illegal 16+ real light values. Four effective Quartz tiers may extend the candidate tail to 38 blocks.
- With Diamond/Focus, verify the smaller axial bonus remains smaller than equivalent Quartz reinforcement.
- Re-run dev.70 opaque-wall placement/removal tests. Dense relays must not resurrect any node downstream of the first opaque blocker; same-tick terrain invalidation must still remove/repopulate the branch.
- Verify partial blockers still add path attenuation and overlapping Mature sources still keep the strongest surviving valid contribution.
- Compare performance with one, several and overlapping Mature fields. dev.73 roughly doubles axial relay count versus the previous sparse lattice, so visible server hitching is a regression.


## dev.71 per-channel Entity equipment visibility

- Historical dev.71 build used protocol 19; current dev.75d protocol is 21. Build on Windows/Java 21 and confirm both client/server agree on the current protocol.
- Humanoid: populate all six Projected channels, toggle each independently, and verify only the selected renderer contribution disappears/reappears.
- Horse: toggle Saddle and Body Armor separately. OFF must not clear their Projected slot/snapshot.
- Hide the only item on a bodyless Humanoid mannequin. No empty phantom body/render should remain; turning the channel ON must restore the same snapshot immediately.
- Hide several channels, save/reload world, and verify exact visibility state plus snapshot item/components persist.
- Verify stateful Survival break/re-place and Projector -> Display -> advanced chassis upgrade preserve the visibility mask with all Entity state.
- Replace a hidden projected item: new snapshot remains hidden. Clear the projected slot, then capture/apply a new item: visibility resets to ON.
- Verify right-click Projected clear remains destructive while VIS. is explicitly non-destructive.
- Multiplayer: toggle from the menu and verify remote clients update after BlockEntity sync.
- Re-run Player skins, trims/dyes/glint, held items and Create Netherite Backtank Ghost at several opacities with channels ON; OFF should suppress the channel entirely without entering the custom renderer.

## dev.72 Entity envelope, nameplate and frustum hardening

- Build on Windows/Java 21; network protocol remains 19.
- Scan a Player and at least one tall/short non-player with a visible nameplate. Verify the label sits above the projected body at low/high Scale and follows Lift plus Floating without returning to the chassis/base region.
- Change Tint and Ghost opacity at 100%, 90%, 50% and 10%; the nameplate should tint/fade with the projection and remain readable through its two-pass text path.
- Humanoid: apply Main Hand, Off Hand, Chest and Head snapshots individually and together. At large/overdriven Scale, rotate/orbit the projection and verify equipment is not clipped by BER frustum culling near screen edges.
- Horse: test Body Armor in Idle and Rearing and verify preview/clearance/world bounds remain conservative.
- Raise Lift to large values and move the camera so only the projection (not the chassis) is inside view; it must remain rendered because the AABB unions machine and displaced projection.
- Move the entire complete envelope outside the camera frustum while remaining inside 256-block view distance; the projection should stop submitting/rendering instead of being forced off-screen. Return it to view and verify immediate recovery.
- Re-run Create Netherite Backtank Ghost at several opacities to ensure the wider generic equipment envelope and frustum change do not affect its established render compatibility path.


## dev.74 Mirage Light Engine shadow solver

- Build on Windows with Java 21. Network protocol must remain 19.
- Important: Simple Light Level still reads the dev.73 physical relay backend in this snapshot. Judge dev.74's new solver through `/miragelight`, not by expecting the floor overlay to change yet.
- Energize one Mature Cluster with zero Boosters in open loaded terrain. Run `/miragelight axis east` (and at least one other axis). The nearest source contribution across distances 1–30 must be exactly `15,15,14,14,13,13,...,2,2,1,1`.
- Run `/miragelight probe` on several numbered floor cells. Record `aggregate`, `nearest` and `vanilla`. `aggregate/nearest` are the dev.74 virtual values; `vanilla` is expected to retain dev.73 artifacts until dev.75.
- Build a barrier that completely separates a test corridor/volume from the source within the remaining propagation budget. Re-probe the separated side. A region with no causal voxel path must read virtual 0 from that source.
- Build a finite wall instead. The virtual field may route around a real edge, but the hidden-side value must be weaker than the unobstructed path because the detour consumes fixed-point energy. Do not classify legitimate corner wrap as relay leakage.
- Place/break multiple wall blocks in one tick. The existing dev.70 `LevelTickEvent.Post` coalescing should force each affected Mature shadow source to solve once for the committed final geometry.
- Change a Core Booster material without breaking/replacing the block. Nearby Mature shadow sources must refresh immediately; Quartz must increase conceptual persistence more strongly than Diamond, while visible virtual output remains capped to 15.
- Use `/miragelight rebuild` after loading a chunk that was previously outside the solved loaded area. This manual command is expected in dev.74; automatic chunk-load invalidation is a dev.75 prerequisite.
- With two or more overlapping Mature sources, `/miragelight probe` aggregate must equal the strongest surviving source contribution. Removing/de-energizing one source must reveal the remaining contribution rather than clear the aggregate blindly.
- Run `/miragelight stats` and `/miragelight axis ...` / `rebuild` around one, several and overlapping sources. Record solve milliseconds, lit voxels and section counts. Visible server hitching is a regression worth addressing before dev.75 authority handoff.
- Re-run the existing dev.70 physical wall tests only as a regression check for the transitional backend; failure of old physical relays does not invalidate the new shadow solver if `/miragelight` shows correct causality.
- Re-run Entity Ghost/Create Backtank and dev.72 high-Lift/frustum smoke tests to ensure the new server-side light foundation did not touch rendering/network behavior.


## dev.75a–75b authoritative virtual-light lifecycle

- Build with Windows Java 21. Protocol 20 was introduced in dev.75a; current dev.75d protocol is 21.
- Confirm no current runtime path creates `mirage_projector:crying_light_node`; load an old dev.73/74 world and verify only Mirage legacy nodes are cleaned.
- Simple Light Level/world render should reflect `max(vanilla, Mirage)`. `/miragelight probe` distinguishes raw vanilla storage from Mirage/effective.
- Cross chunk borders with the field. Destination chunk unload/reload must remove/recreate only geometry-dependent portions after lifecycle solve; source-origin unload must retract/re-discover the source.
- Relog, same-dimension respawn and dimension change must not leave stale/missing client fields.
- Multiplayer: move a second client into/out of watched chunks and verify source descriptor delivery/retraction without disturbing other clients.
- Terrain mutations through place/multi-place/break/fluid/crop/feature/piston/explosion must coalesce and rebuild affected fields.
- Overlapping Mature sources aggregate by max; removing the stronger source must reveal the surviving weaker value.

## dev.75c vanilla opacity / face-occlusion

- Re-run iron-wall comparison beside a vanilla source. Mirage must not traverse an opaque wall directly.
- Test 1-, 2- and 3-block-high finite walls: hidden-side light may return only by a real route over/under/around an edge.
- Test slabs, stairs, transparent/partial-opacity blocks and at least one modded block with nontrivial shape. Do not require Mirage-specific block tables; behavior should follow vanilla state opacity/face semantics.
- A complete separating barrier inside the propagation budget must yield zero contribution from that source on the disconnected side.

## dev.75d obstacle-detour penalty

- Network protocol must be **21** on both peers.
- Open no-Booster axis remains exactly `15,15,14,14,...,1,1` at 1–30. Any open-space change is a regression.
- Repeat the same finite wall at several heights. The direct side remains half-decay; hidden positions reached only by wrapping must lose more light than dev.75c because obstacle-only route distance is penalized.
- L-shaped 3-block-high wall: compare exposed edge, middle pocket and deepest inner corner. Values should form a gradual shadow gradient; there must be no abrupt global switch to vanilla decay behind the first collision.
- Extend the L arms or raise the wall. Deep-pocket light must be non-increasing as the cheapest causal route becomes longer; if an alternate shorter edge route appears, the solver may legitimately choose it.
- Build a wall that leaves two alternate routes (short/long). Probe should reflect the stronger/cheaper route, not the first route visited by iteration order.
- Remove one wall block. Same-tick/coalesced rebuild should immediately improve the cheapest route and brighten affected cells.
- `/miragelight probe` behind obstacles should show `cost=... (direct=..., extra=...)`; open monotonic cells should have `extra=0` unless partial opacity contributes additional cost.
- Partial-opacity blocks may add to `extra` even without geometric detour; this is intentional diagnostic aggregation of non-direct traversal cost.
- Quartz/Diamond reinforced fields must obey the exact same wall/detour rules. Booster power extends available energy; it never grants penetration.
- Stress several overlapping radius-30/38 fields with walls; record solve ms/server tick/frame impact before considering incremental invalidation.
