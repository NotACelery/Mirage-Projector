> **Historical dev.37 note — image-layout portions superseded by dev.38:** render-order/handbook findings remain useful, but any reference here to Field 3x3 or mandatory Wide/Tall multi-source is not current design. Field is one continuous Plane; Wide/Tall use optional SINGLE/MULTI.

# dev.37 — Render-order recovery and tabbed handbook

## Why dev.36 regressed

dev.36 correctly identified that Entity holograms could not be rendered independently inside each Mirage BlockEntityRenderer. It deferred them until `AFTER_BLOCK_ENTITIES`, but the deferred pass reused Minecraft's global `renderBuffers().bufferSource()` and then called `endBatch()`.

That flush does not belong only to Mirage. It can also finish pending render types submitted by Image projections, Banner rendering, vanilla entities/block entities and modded renderers. Therefore a change intended only for Entity Mode could disturb Image Mode even though the image source/layout code itself did not change.

The second problem was the chosen stage. Rendering a no-depth-write Ghost Entity before translucent chunks means water rendered afterwards has no hologram depth to test against. Water can therefore blend over the hologram even when the water is physically farther away.

## dev.37 ordering contract

Entity projection order is now:

```text
opaque/cutout world
-> normal entities
-> physical Mirage projector / core BlockEntities
-> translucent world blocks (water, etc.)
-> tripwire translucent stage
-> Mirage deferred Entity holograms (far-to-near)
-> later particles/weather/UI stages
```

NeoForge `AFTER_TRIPWIRE_BLOCKS` is used because it occurs after translucent blocks and is a safer late stage for custom translucency than injecting directly before the world's translucent pass.

The physical projector has already written depth. Water/translucent world geometry has also had its normal opportunity to write/test depth. The late hologram can therefore keep Mirage's existing `LEQUAL` depth-test and no-depth-write Ghost contract:

- physical projector behind hologram -> hologram fragment is nearer and passes;
- physical projector in front -> hologram fragment fails depth and stays behind;
- water behind hologram -> hologram draws over the already-rendered farther water;
- water in front -> nearer water depth prevents the farther hologram fragment.

## Buffer ownership contract

The deferred Entity pass owns:

```text
ByteBufferBuilder
-> MultiBufferSource.immediate(...)
-> endBatch() only on that private BufferSource
```

It must never end Minecraft's shared `renderBuffers().bufferSource()`.

This is also the Image Mode pipeline recovery: the dev.36 render-state side effect is removed so the previously-correct Plane/Prism geometry can use the normal BlockEntity batching lifecycle again. Aspect-ratio/layout transforms are not rewritten as part of the depth fix.

## Plane Image face-mode contract completed in dev.37

A separate design decision from the render-order repair had been defined before the previous chat ended but was not yet present in dev.36. Plane projectors now expose five explicit presentation modes:

```text
FRONT       -> only the stored Front asset is visible, only from the front
BACK        -> only the stored Back asset is visible, only from the back
MIRRORED    -> Front is reused on the rear with mirror orientation
READABLE    -> Front is reused on the rear with readable orientation
INDEPENDENT -> Front and Back are independent assets; no missing-Back fallback
```

`FRONT` is the new default for newly-created settings. The persisted historical values remain `MIRRORED=0`, `READABLE=1`, `INDEPENDENT=2`; `FRONT` and `BACK` are appended so old saves are not reinterpreted. This requires protocol 16 because new clients can send the appended values.

Wide/Tall/Field currently own one source bank, so their dev.37 selector intentionally offers only Front / Mirrored / Readable. A true Back/Independent multi-source layout needs a second persisted/networked rear bank and is not faked. Prism already has real North/East/South/West assets and does not use the Plane mode selector.

## Debug Handbook contract

The handbook continues to use vanilla `BookViewScreen` for page texture, text wrapping and page navigation. Mirage only adds presentation/navigation around that vanilla surface:

- dynamic scale, capped to remain usable on smaller screens;
- book texture centred vertically and horizontally rather than anchored near y=2;
- local dark overlay only; no generic Screen blur;
- no wide vanilla Done button; Esc closes the manual;
- seven persistent side tabs:
  - General
  - Compact
  - Display
  - Wide
  - Tall
  - Field
  - Prism

General owns mechanics shared by the mod: Source Mode/workspaces, Projection Cores and PU, independent Core/chassis limits, virtual snapshots, global presentation, clearance and entity scanning.

Each chassis section starts by saying **what that projector is for**, then gives its hard chassis limits and its special layout behaviour where applicable.

No Patchouli/Ars Nouveau dependency is introduced. The interaction pattern is inspired by category-tab guide books, but the implementation remains native to Mirage and vanilla Minecraft.

## QA gate

Do not mark dev.37 build-clean until Windows compile and the following in-game matrix passes:

1. giant Entity + multiple physical Mirage projectors at mixed depths;
2. Ghost Entity with water in front and behind;
3. two overlapping Ghost Entities from multiple camera angles;
4. Compact/Display: Front, Back, Mirrored, Readable and Independent all obey the selected side semantics;
5. Independent with no Back asset leaves the rear empty rather than falling back to Front;
6. Wide 4x1, Tall 1x4, Field 3x3 keep aspect/layout and support Front/Mirrored/Readable;
7. Prism keeps four independent Image faces unchanged;
8. handbook centred, enlarged and tabs/page arrows clickable at multiple GUI scales;
9. handbook still produces no blur and suppresses only its own held item.
