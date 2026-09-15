# Mirage Projector 1.0.11 — Handheld Mirage Projector

1.0.11 extends the 1.0.5–1.0.10 mobile-device work from light-only handhelds into the first portable hologram projector. It keeps the existing fixed-projector source/render architecture intact and does not change the save/network schema.

## Scope

This snapshot adds a first playable handheld hologram projector foundation rather than the final full 1.1 portable-projector family.

Delivered in 1.0.11:

- `mirage_projector:mirage_hand_projector` item;
- exact removable rechargeable-cell storage using the same one-cell embedded ItemStack pattern already used by the lantern;
- portable copied-profile storage for one compact normalized hologram profile;
- sneak + right-click copy from any placed Mirage Projector block entity;
- right-click handheld ON/OFF activation without deleting the stored profile;
- client reconstruction/rendering of moving handheld holograms from tracked held ItemStacks + vanilla player transform;
- server-side held-only cell drain while an active portable hologram is actually running;
- actionbar/HUD, tooltip, model/texture, Creative exposure and multilingual strings.

## Portable profile contract

The handheld device does **not** expose the full placed-projector UI in 1.0.11. Instead, it copies the currently active source profile from a configured placed Mirage Projector and normalizes it for handheld use.

The portable copy preserves the active source family:

- `Image`
- `Item`
- `Entity`
- `Banner`

Normalization rules in 1.0.11:

- target chassis is always compact;
- Image layout is forced to single-source;
- multi-slot or multi-face data collapse to one portable source where required;
- presentation is clamped to reduced portable ceilings for Scale/Lift/Floating;
- opacity is capped at **90%**, guaranteeing at least **10% ghost** remains visible;
- the copied portable profile carries no physical Core.

This gives the handheld projector a consistent moving-device budget while preserving the existing source contracts and avoiding a parallel projector-configuration system.

## Controls

- **Sneak + Right-click on a placed Mirage Projector**: copy its active portable-compatible source profile into the handheld projector.
- **Right-click in air**: toggle the stored portable hologram ON/OFF.
- **Sneak + Right-click in air**: service the rechargeable cell through the opposite hand.
  - If the device is empty and the opposite hand holds a rechargeable cell, that exact cell is inserted.
  - If a cell is installed and the opposite hand is empty, the exact stored cell is extracted.

## Runtime behavior

The handheld projector only drains while:

- the handheld item is actually held,
- a portable profile is loaded,
- the device is active,
- a rechargeable cell is installed and still has charge,
- the copied portable profile still has source content.

Drain is evaluated once per second on the server using the current Projection Power math as a QA balance source, with a higher drain floor than the lantern so handheld holograms remain more expensive than handheld light.

## Client rendering

`ClientHeldProjectors` reconstructs a temporary compact `MirageProjectorBlockEntity` from the tracked held ItemStack data. Rendering reuses the current projector renderer and source-render registry instead of creating a second portable-only renderer path.

Entity projections continue to use the existing deferred entity-projection pass, so handheld entity holograms participate in the same late ghost rendering path as placed projectors. No custom movement/projector packet was added.

## Versioning

- `ProjectionSettings` format remains **3**.
- Network protocol remains **28**.
- No world migration format change was required.

## Explicitly not in scope

1.0.11 does **not** include:

- a handheld projector self-configuration GUI;
- survival recipe/balance finalization;
- portable projector family variants beyond the first handheld device;
- Scan Codex gameplay;
- Dragon Egg / End Resonance behavior.
