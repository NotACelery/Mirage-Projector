# dev.36 — Entity base nameplate + block-entity render ordering

## Problems fixed

### 1. Named scans without a visible projection label
The scan already stores the visible `DisplayName` and, from data v5, explicit `HadCustomName` / `CustomNameText`. dev.36 treats a non-player `DisplayName` that differs from the vanilla EntityType description as a valid conservative nametag recovery signal on every scan version.

The world label is intentionally anchored to the projector base rather than to the scaled entity top. This matches the project UX requirement and prevents giant Scale/Lift values from moving the label out of view.

### 2. Other Mirage Projectors painting over an Entity projection
Rendering a hologram inside each individual `BlockEntityRenderer` made visual order depend on which projector BER happened to be processed later. Ghost projections additionally cannot solve that by writing depth because doing so cuts holes in water/translucent world geometry.

dev.36 therefore splits physical and projected rendering:

1. each Mirage BER renders its physical Core normally;
2. Entity projection work is queued;
3. NeoForge `RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES` flushes all queued Entity holograms;
4. entries are sorted far-to-near;
5. the shared buffer is flushed before the translucent chunk pass.

This gives all physical block entities a stable background before hologram blending while keeping water/translucent blocks later in the frame.

## Protocol
No network change. Protocol remains 15.

## QA
- fresh nametag scan -> base label;
- giant opaque entity crossing other projectors;
- same scenario with Ghost enabled;
- water behind Ghost projection;
- overlapping translucent Entity projections.
