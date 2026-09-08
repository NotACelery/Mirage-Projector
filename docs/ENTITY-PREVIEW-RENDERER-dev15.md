# Mirage Projector — Entity Preview Renderer handoff (dev.15)

## Purpose

This handoff records the first real GUI entity renderer and the exact boundary before world projection work. It is a development note, not user-facing marketing.

## Current pipeline

```text
Entity Scan Template
        ↓ import
EntityProjectionState.activeEntity
        +
Projected/Active virtual equipment snapshots
        ↓
EntityProjectionPreviewRenderer
        ↓
client-only LivingEntity (never added to ClientLevel)
        ↓
InventoryScreen.renderEntityInInventoryFollowsMouse
        ↓
scissor-clipped / auto-fitted GUI preview
```

### Non-player LivingEntity

1. resolve `EntityType` from the frozen scan;
2. `type.create(clientLevel)`;
3. load the sanitized `EntityData`;
4. reset position/motion and suppress vanilla custom-name rendering;
5. apply only Projected/Active equipment;
6. render.

### Player

Dev.15 constructs `RemotePlayer(clientLevel, GameProfile(sourceUuid, scanName))`. This is sufficient to share the vanilla Player renderer and current-session skin resolution. It is **not yet an offline frozen-skin solution**: in MC 1.21.1 `AbstractClientPlayer#getSkin()` resolves through current `PlayerInfo` and falls back to a default skin if that info is unavailable.

Do not mark Player snapshot fidelity complete until a self-contained skin snapshot/asset path is implemented.

## Equipment truth rule

Only the right-side Projected/Active set contributes to the composed entity preview. Incoming card equipment and physical left staging are candidates only. This is required so the preview matches what the hologram is actually configured to display.

## Auto-fit

The preview uses `getBbWidth()` + `getBbHeight()` and the viewport dimensions, with a bounded GUI scale. Vanilla `InventoryScreen.renderEntityInInventoryFollowsMouse` then provides scissor clipping and temporary cursor-facing rotations.

The external panel prefers 150 logical pixels, can shrink to a compact 68-pixel minimum, and yields to the main workspace if the screen is narrower still.

## Card removal semantics

- Humanoid card removed: body clears; six Humanoid Incoming/Projected channels survive.
- Horse card removed: body clears; Horse Saddle/Body virtual state clears; physical Horse staging must be returned first.
- Generic card removed: body clears.

## Next renderer tasks

1. add explicit Entity/Humanoid world source mode;
2. reuse a shared reconstruction/composition service for GUI and world render rather than duplicating snapshot interpretation;
3. render nameplate in the gap between projection base and entity bottom;
4. capture/transfer exact Player skin for offline scans;
5. add equipped armor/pose pipeline, including standalone armor Item Mode;
6. implement alpha-safe Ghost Effect across entity, armor, glint and hand layers;
7. derive clearance from final entity/pose bounds.

## QA

Compile-sensitive APIs for NeoForge 1.21.1:

- `EntityType#create(Level)`;
- `Entity#load(CompoundTag)` / inherited `LivingEntity#load`;
- `RemotePlayer(ClientLevel, GameProfile)`;
- `InventoryScreen.renderEntityInInventoryFollowsMouse(GuiGraphics,int,int,int,int,int,float,float,float,LivingEntity)`;
- `LivingEntity#setItemSlot`;
- `Mth.clamp(int,int,int)`.

A real `build.bat` compile remains authoritative.
