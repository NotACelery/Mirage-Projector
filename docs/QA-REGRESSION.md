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

## dev.69 occlusion-aware reflected light

- Build on Windows/Java 21; protocol must remain 18.
- With zero Boosters, an energized Mature Cluster must preserve the dev.68 six-axis half-decay baseline and ~29-block unobstructed axial reach.
- Place a full opaque wall across one primary branch. No downstream Mirage node/light island may appear on the far side solely from that branch; natural vanilla wrap around the wall edges is allowed.
- Compare full opaque, partial-opacity and open paths. Partial blockers should attenuate downstream reflected strength instead of behaving identically to air or cutting the branch completely.
- Orbit solid blocks around the field and verify opposite/non-exposed faces are not artificially fullbright. Mirage does not manually shade faces; accepted nodes must leave local shading/AO to vanilla.
- Glass/Diffusion: verify diagonal/broader coverage appears and remains shorter/dimmer than the primary axes. Four Glass Boosters must stay bounded; no full-cube flood of light nodes.
- Quartz/Radiance: verify it is the strongest static range/persistence amplifier. Four Quartz Boosters may reach the maximum ~37-block axial candidate distance but no real node emits above 15.
- Diamond/Focus: verify axial persistence increases more modestly than Quartz and mixed Diamond cancels Glass diffusion tier-for-tier.
- Amethyst/Resonance: verify residual bursts/rotation become more active without granting free static field range.
- Netherite/Inversion: verify reflected residual rotation reverses without granting free static field range.
- Mixed stacks: verify only the first four loaded Boosters contribute and changing their materials contracts/expands the field cleanly without orphan nodes.
- Break/de-energize a Mature source and retest overlapping sources; dev.68 immediate reconciliation must still hold for both axial and diffuse nodes.
- Re-run residual-ray collision clipping and Create Netherite Backtank Ghost regression to ensure the light work does not affect those render paths.
