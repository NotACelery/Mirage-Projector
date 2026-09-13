# dev.81 — Projection Source / Future-Proofing Foundation

Version: **0.1.0-dev.81**  
Network protocol: **27**

This pass is intentionally architectural. It does not add a fifth public projection mode and does not change the visible behavior of Image, Item, Entity or Banner. Its purpose is to freeze stable seams before 1.0.0 so 1.1/1.2/addons can extend Mirage without rewriting save/network/render dispatch.

## Stable source identity

`ProjectionSettings.SourceMode` keeps its historical type name for source compatibility inside the codebase, but it is no longer a Java enum. It is an interned value object backed by a stable `ResourceLocation`.

Built-ins:

- `mirage_projector:image`
- `mirage_projector:item`
- `mirage_projector:entity`
- `mirage_projector:banner`

Current saves write `SourceId`. Old integer `SourceMode` values remain readable and migrate to the built-in IDs. Network source selection also sends the namespaced ID instead of an enum ordinal.

## Versioned settings codec

`ProjectionSettings` now has explicit serialization version **2**.

Network:

- writes the settings format version first;
- rejects unsupported settings formats instead of silently misreading bytes;
- source identity is UTF namespaced ID, never Java enum ordinal.

NBT:

- writes `ProjectionSettingsVersion`;
- writes `SourceId`;
- built-ins also keep the old integer `SourceMode` field as downgrade/migration assistance;
- old saves without `SourceId` still load from the legacy ordinal.

The global Mirage network protocol is bumped from 26 to **27** because both `ProjectionSettings` and `SetProjectionSourcePayload` changed wire shape.

## Common source registry

`ProjectionSourceRegistry` owns common-side source definitions.

A definition supplies:

- stable source ID;
- translation key;
- source content presence/count provider;
- chassis compatibility predicate;
- builtin/addon identity.

The existing four sources are registered through exactly this path. Future addons can call the registry instead of extending Mirage's source type.

`definitionsFor(chassis)` is the future menu/workspace enumeration seam. 1.0 intentionally keeps the four existing buttons rather than adding speculative addon UI.

## Client renderer registry

`ProjectionSourceRenderRegistry` owns client-only source renderer dispatch.

`MirageProjectorRenderer` no longer begins with a hard-coded Image/Item/Entity/Banner dispatch chain. The four built-ins register handlers with the client registry and the BER resolves the current source through that registry.

A future source may therefore register a renderer without adding another top-level `if (source == ...)` branch to the core renderer.

## Unknown source preservation

`MirageProjectorBlockEntity` now owns opaque `ProjectionSourcePayloads` NBT keyed by namespaced source ID.

If a world is opened while an optional provider/addon is missing:

- the unknown `SourceId` is retained;
- its opaque source payload is retained;
- the provider fails closed;
- the data is not destructively converted to Image or deleted.

If the provider returns later, its ID/payload are still present.

## Transform boundary

`ProjectionTransform` is now the source-agnostic transform representation.

It contains the current 1.0 transform controls plus a normalized quaternion orientation:

- Scale;
- Lift;
- spin enable/period/direction/offset;
- orientation X/Y/Z/W;
- Floating mode/amplitude/cycle/interval.

1.0 renderers intentionally leave free orientation at identity. The quaternion is already persisted in `ProjectionSettings`, so 1.2 free rotation can begin using it without a second save-format rewrite.

## Energy boundary

`ProjectionEnergySource` separates projection energy availability from the fixed-projector Core slot.

Current fixed projectors adapt `ProjectionCoreProfile` into this interface. `ProjectionPower` now has an overload that accepts a generic energy source while retaining all existing Core overloads for 1.0 callers.

This is the seam for 1.1 Glow Dust/portable energy without inventing fake Core inventory slots.

## Chassis compatibility

`ProjectionChassisProfile.supportsProjectionSource(...)` delegates to the source registry compatibility contract. Addons/future sources may reject unsupported chassis centrally instead of scattering class-name checks through menus/renderers.

## What did not change

- Image/GIF behavior;
- Item projection;
- Entity snapshots/renderer;
- Banner projection;
- Projection Power balance for current fixed projectors;
- Core/Booster behavior;
- Mirage Light solver/snapshot semantics;
- six projector models, VoxelShapes and item presentation;
- TURN OFF / active-mode UX.

## 1.0 freeze result

The major future-proofing seams required before stable 1.0.0 are now present. Remaining 1.0.0 work should be release hardening/document cleanup rather than another large renderer/light architecture wave.
