# NEXT CHAT HANDOFF — Mirage Projector 1.0.11 Handheld Projector

Baseline: `1.0.11`
Minecraft: `1.21.1`
NeoForge: `21.1.244+`
Java: `21`
Network protocol: `28`
ProjectionSettings format: `3`

## Delivered in 1.0.11

- Added `mirage_projector:mirage_hand_projector`.
- The item stores one exact rechargeable cell ItemStack internally.
- The item also stores one copied compact portable projector profile serialized from a placed Mirage Projector.
- Sneak + right-click on a placed `MirageProjectorBlockEntity` copies the current active source profile into the handheld device.
- Normal right-click toggles handheld hologram projection ON/OFF without deleting the stored profile.
- Sneak + right-click in air inserts/extracts the rechargeable cell through the opposite hand.
- Server-side held-only drain now applies to active handheld portable holograms.
- Added `ClientHeldProjectors`, which reconstructs temporary compact projector block entities from held ItemStacks + tracked player transform and renders them during the level render stage.
- Added local handheld-projector HUD/actionbar feedback plus texture/model/Creative exposure/lang strings.

## Important implementation files

### New source

- `src/main/java/celerbi/mirageprojector/item/MirageHandProjectorItem.java`
- `src/main/java/celerbi/mirageprojector/client/ClientHeldProjectors.java`
- `src/main/resources/assets/mirage_projector/models/item/mirage_hand_projector.json`
- `src/main/resources/assets/mirage_projector/textures/item/mirage_hand_projector.png`
- `docs/RELEASE-1.0.11-HANDHELD-PROJECTOR.md`

### Patched integration points

- `src/main/java/celerbi/mirageprojector/registry/ModItems.java`
- `src/main/java/celerbi/mirageprojector/registry/ModCreativeTabs.java`
- `src/main/java/celerbi/mirageprojector/client/ClientRuntimeEvents.java`
- `src/main/resources/assets/mirage_projector/lang/en_us.json`
- `src/main/resources/assets/mirage_projector/lang/es_cl.json`
- `src/main/resources/assets/mirage_projector/lang/es_es.json`
- `gradle.properties`
- current docs/changelog/version-scope/roadmap/development/registry inventory/waitlist files.

## Portable profile rules

The handheld projector intentionally does **not** open its own projector GUI in 1.0.11.
It copies and normalizes a placed-projector source profile.

Normalization currently does the following:

- target portable chassis becomes compact;
- Image mode collapses to single-source layout;
- Banner mode keeps one portable face only;
- Scale/Lift/Floating are clamped to smaller portable ceilings;
- opacity is capped at 90% so the hologram stays at least 10% ghostly;
- physical Core storage is stripped.

This means the handheld device is a **portable consumer** of the existing projection stack, not a second independent authoring workflow.

## Known likely follow-up work

1. **Portable placement feel / origin tuning**
   - The first placement uses a simple player eye/look + hand-side offset approximation.
   - QA may want the hologram slightly farther/closer or higher/lower.

2. **Handheld self-configuration**
   - The current workflow depends on a placed projector as the authoring station.
   - If the user wants a fully standalone handheld projector UI, that should be a later 1.0.x or 1.1.x task.

3. **Portable power balancing**
   - Current drain is Projection Power-derived with a QA minimum floor.
   - After gameplay QA, re-tune capacity/output/drain versus lantern/light projectors.

4. **Portable source normalization polish**
   - Multi-source Image/Banner collapse is intentionally simple.
   - If QA wants better selection logic or more explicit copy feedback, improve normalization rather than creating parallel source storage.

5. **Runtime/render polish**
   - Watch for first-person clipping, remote-player alignment, entity late-pass visuals and projector origin jitter.

## Verification target

Run:

```text
python tools/verify_current_line.py
```

Expected final line after the 1.0.11 verifier updates:

```text
MIRAGE PROJECTOR 1.0.11 VERIFICATION PASS (26 gates)
```

## Windows compile/runtime reminder

Static verification is not a substitute for the authoritative Windows build/runtime QA. The intended follow-up after this source snapshot is:

1. run `build.bat` on Windows Java 21;
2. confirm no compile errors;
3. test handheld-projector copy / battery service / ON-OFF / drain / relog persistence / remote-player visibility in-game.
