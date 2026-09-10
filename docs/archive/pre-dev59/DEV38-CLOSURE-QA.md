# Mirage Projector 0.1.0-dev.38 — closure and QA contract

Status: **SOURCE candidate closed for build/in-game validation.** This document does not declare the build clean; Windows `build.bat` + in-game QA remain authoritative.

## 1. Static closure checks completed

The dev.38 source candidate passes the following static checks:

- `mod_version=0.1.0-dev.38`;
- network payload registrar protocol = **17**;
- 31 resource JSON files parse correctly;
- `en_us`, `es_es`, `es_cl` contain the same **373 translation keys**;
- all six registered projector blockstates expose north/east/south/west variants;
- current chassis nominal values/multipliers match the dev.38 authority table;
- current Standard Core curve is 32/48/64/96/128 base PU;
- only Wide/Tall advertise optional Image MULTI;
- Field does not advertise or consume a 3x3 Image layout;
- old Core/chassis hard-limit failure symbols are absent from runtime Java;
- Entity kind-change cleanup + load migration guard are present;
- idle-book rendering executes before the Core/power gate when no renderable source exists;
- Entity deferred rendering does not call `endBatch()` on Minecraft's shared block/entity buffer.

A dependency-less `javac` syntax sweep was also run. As expected it cannot type-check Minecraft/NeoForge imports without the dependency graph, but it reported no syntax-pattern failures (`';' expected`, illegal start, orphaned case, premature EOF, etc.).

## 2. Power-system QA

Test every registered chassis with at least Glass and Netherite first, then sample intermediate Cores.

### Required observations

- Effective PU follows `floor(Base PU × Chassis multiplier × Core amplification)`.
- Standard Cores use amplification x1.00.
- Current nominal dimensions are efficiency targets, not gameplay walls.
- Scale/Lift/Float sliders end at currently feasible values rather than extending deliberately into an invalid orange range.
- Overdrive appears only after a corresponding nominal geometry/Lift/Float threshold is exceeded.
- Float never exceeds Lift while Floating is enabled.
- Ghost at 90% produces only a tiny rebate; it must never become a meaningful exploit for free scale.
- Changing Core live recalculates slider endpoints.

### Sanity anchors

- Compact + Netherite = 128 Effective PU.
- Field + Glass = 128 Effective PU.
- Despite equal Effective PU, Field must be much more efficient for large surfaces because its nominal geometry is 128x128 instead of 10x10.

## 3. Image chassis QA

### Compact

- one continuous Plane only;
- nominal 10x10;
- Front/Back/Mirrored/Readable/Independent behaviour as currently defined.

### Display

- one continuous Plane only;
- nominal 32x32.

### Wide

- `SINGLE`: one continuous wide image;
- `MULTI`: exactly four independent sources in 4x1;
- same source/same Scale as Tall MULTI must get the same square cell size.

### Tall

- `SINGLE`: one continuous tall image;
- `MULTI`: exactly four independent sources in 1x4;
- same source/same Scale as Wide MULTI must get the same square cell size.

### Field

- one continuous Plane only;
- nominal 128x128;
- no 3x3 picker;
- no nine-source rendering;
- no hidden active 3x3 interpretation after save/reload.

### Prism

- four independent lateral faces North/East/South/West;
- no top/bottom face;
- cardinal naming remains world-cardinal regardless of physical block facing.

## 4. Placement orientation QA

For Compact, Display, Wide, Tall, Field and Prism:

1. place while facing north/east/south/west;
2. verify the block model rotates furnace-style toward the player;
3. reload the world and confirm facing persists;
4. for Plane-like sources, verify projection base orientation follows block facing;
5. Prism Image/Banner face names remain world-cardinal.

## 5. Idle-book QA

With no renderable source configured:

- Compact;
- Display;
- Wide;
- Tall;
- Field;
- Prism;

must all show the same floating vanilla-book idle marker above their own physical top.

Repeat with the Core socket empty on the five chassis that normally start empty. The book must still render. The idle marker is chassis feedback, not an active projection and not a reward for having PU.

## 6. Entity virtual-workspace lifetime QA

The authoritative matrix is in `ENTITY-WORKSPACE-LIFETIME-dev38.md`.

Critical regression sequence:

1. Humanoid card + full armor + Main/Off Hand;
2. accept them to Projected;
3. remove Humanoid card: body disappears, armor/hands remain as supported bodyless mannequin;
4. insert Generic card: Humanoid virtual equipment must be deleted because those rows disappear;
5. remove Generic card: old armor/hands must **not** reappear.

Repeat:

- Humanoid -> Horse;
- Horse with Saddle/Body -> Humanoid;
- Horse -> Generic;
- Humanoid -> Humanoid, where same-family Projected equipment should remain;
- Horse -> Horse, where same-family Projected equipment should remain.

Save/reload after cross-kind changes to verify stale dev.37-era virtual data does not resurrect.

Physical staging ItemStacks are not part of the virtual cleanup. If present, close the workspace and confirm they return to inventory or drop exactly once when inventory is full.

## 7. GUI QA

- Item Snapshot Workspace: no duplicated title inside preview; no label may overlap preview/slots/text.
- Primary settings: Power/nominal/effective values fit at common GUI scales and reduced window sizes.
- Debug Handbook: centred, readable scale, seven tabs, no blur, no first-person handbook item visible behind it.

## 8. Render-order QA carried from dev.37

Still mandatory before dev.38 can be called stable:

- giant Entity hologram crossing several physical Mirage projectors;
- projector physically in front must occlude hologram;
- projector physically behind must remain behind hologram;
- water clearly in front vs clearly behind Ghost Entity;
- two Ghost Entity projections overlapping from several camera angles;
- static Image projections must remain visually correct while Entity deferred rendering is active.

## 9. What dev.38 deliberately does not include

- purpose-built Improved Core items/recipes/textures;
- final chassis model/texture redesign;
- GIF decoder/animated-image runtime;
- Effigy/Colossal registered playable blocks;
- special compatibility adapters and remaining renderer-specific hardening.

## 10. First feature after dev.38

Once dev.38 passes build + in-game QA, the first new feature priority is **GIF / Animated Image import**.

Before coding it, freeze:

- accepted GIF semantics and frame disposal handling;
- per-frame duration handling;
- resolution/frame/FPS/memory limits;
- normalized asset representation and hashing;
- multiplayer upload/download/cache representation;
- world playback clock/synchronization;
- behaviour across Compact/Display/Wide/Tall/Field/Prism;
- interaction with Ghost/Tint/Lighting/Rotation/Float and Power cost;
- pause/unload/reload behaviour and performance safeguards.

Improved Cores and projector visual redesign remain planned after/around that work, but are intentionally separated from dev.38 closure.
