# Mirage Projector 0.1.0-dev.16 — Entity/Humanoid Renderer Handoff

> **dev.17 forward note:** la capa de presentación 3D Tint/Ghost que aquí figuraba pendiente ya tiene una primera implementación local en `ProjectionRenderBuffers`. Ver `docs/GHOST-3D-RENDERER-dev17.md`. Este documento conserva el estado histórico de dev.16.

## Fixed baseline

- Minecraft 1.21.1
- NeoForge 21.1.244
- Java 21
- Protocol registrar 6
- Build entrypoint: root `build.bat` only

## Implemented render graph

```text
Entity Scan / Humanoid virtual state
        |
        +-- active body? --> client-only LivingEntity reconstruction
        |
        +-- no body + Humanoid projected gear? --> invisible RemotePlayer rig
        |
        +-- Projected/Active equipment overlay only
        v
EntityRenderDispatcher
        v
Scale + Rotation + Lift + Floating
        v
world hologram + selective base nameplate
```

Nothing in this graph is spawned into the level. Source UUID is provenance, not live ownership.

## Player skin

Scan data version 3 saves the standard GameProfile packed `textures` value/signature. `MirageRemotePlayer#getSkin()` resolves from `SkinManager.lookupInsecure(frozenProfile)`, so the scan can remain visually self-contained after the source Player leaves. Test this explicitly in multiplayer.

## Bodyless Humanoid

An invisible RemotePlayer is deliberate. In vanilla 1.21.1 the base LivingEntity model is skipped when invisible, while render layers still execute. This is used to display armor/held items with no fake player/armor-stand body.

## Item Mode equipped armor

If `LivingEntity.getEquipmentSlotForItem(snapshot)` resolves to Head/Chest/Legs/Feet, Item Mode renders that one piece through the bodyless rig. All other items stay on the ordinary full 3D ItemRenderer path. GUI preview follows the same distinction.

## Visual animation safety

Never call `tick()` or `aiStep()` on projection clones. Use `prepareVisualFrame` or an explicit species adapter. Ender Dragon currently receives a stable 64-sample latency history + flap clock only; no phase AI, crystal targeting, sound, particle or movement tick is run.

## Nameplate

Only captured `NameplateText` is rendered:

- Player -> player name;
- renamed mob -> custom name;
- unnamed mob -> none.

Vanilla overhead naming is suppressed for Mirage Player clones to avoid duplicate labels.

## QA blockers before calling dev.16 build-clean

1. Windows `build.bat`;
2. scan and project Chicken, Baby Zombie, Player, Horse, Ender Dragon;
3. remove Humanoid card while keeping projected armor/hands;
4. trimmed/dyed/enchanted vanilla armor;
5. shield/tool/block in both hands;
6. modded armor/equipment;
7. Player scan after source logout;
8. standalone armor Item Mode and GUI sizing;
9. nameplate position under varying Lift/Float;
10. regress Image and ordinary Item modes.

## Remaining renderer work

- alpha-safe Ghost Effect/Tint for arbitrary entity render layers/glint;
- Humanoid pose presets;
- pose/model-aware clearance;
- special animation adapters where vanilla renderers need internal history;
- optional external accessory/backpack integrations only after baseline is stable.

## Snapshot invariant

Every further meaningful change must produce a recoverable source snapshot even if compilation/QA is still pending. Snapshot creation is not gated on release readiness.
