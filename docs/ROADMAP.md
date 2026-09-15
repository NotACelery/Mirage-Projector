# Roadmap

Current implementation snapshot: **1.0.18**
Network protocol: **33**

1.0.0 remains the initial stable fixed-projector baseline. Normal development now advances through monotonically increasing `1.0.x` implementation snapshots; the second component changes to `1.1.0` only when the complete planned expansion is ready. 1.0.1–1.0.4 established the fixed-projector placement/UI groundwork. 1.0.5 made `DYNAMIC_VISUAL` operational on the client and added directional-cone solving. 1.0.6 tightens Mirage Prism carousel spacing, caps user radial expansion at ten blocks and consolidates the fixed settings UI to four tabs. 1.0.7 begins the rechargeable Glow Dust gameplay loop and Beacon/Core-Booster charging path. 1.0.8 generalizes that path through a shared rechargeable-energy item contract, adds the 4000-unit Light Battery, and polishes Core Booster charging interaction/render/state feedback. 1.0.9 adds the first physical Focus/Flood/Ambient/Off light projector and connects synchronized rechargeable device state to the DYNAMIC_VISUAL runtime. 1.0.10 adds the first handheld/player-following Mirage Lantern, including exact embedded-cell preservation, held-device drain, persistent hotbar feedback and remote-player moving-light reconstruction from vanilla tracking. 1.0.11 adds the first handheld hologram projector: a portable copied-profile consumer that normalizes fixed-projector content into a compact moving projection while reusing the existing source/render stack and rechargeable-cell contract. 1.0.12 gives portable projectors stable device identity, persistent inventory-active operation, multiplayer state publication for hidden inventory devices and the infinite Creative Battery used by QA/admin/temporary game modes. 1.0.13 adds the first Mirage-owned player equipment layer: Arm Strap, Shoulder Slot, mounted-device ticking/sync, inventory controls and vanilla shoulder reservation without consuming armor/offhand slots. 1.0.14 expands the Arm Strap into its battery-management role with a six-cell pouch, generic upgrade sockets, automatic depleted-cell replacement and the 6→9-cell Expansion Patch. 1.0.15 adds the dedicated directional Beacon Charging Station: four queued inputs, one single-cell active charger, four ordered outputs, side-aware logistics capability exposure and hopper-like automatic front-face ejection. 1.0.16 adds the first War Banner presentation for portable Banner projections. 1.0.17 adds the Mirage Scan Codex persistent library foundation with server-backed snapshots, search/filter/favorites and exact capture selection. 1.0.18 is a QA-driven stabilization/rearchitecture pass: portable and placed light devices gain real GUIs, battery changes stop triggering re-equip flicker, Shoulder Strap becomes the owner of its packed device/pouch/upgrades, Charging Station input/UI/world presentation is tightened, Glow Dust regains vanilla identity at full charge, and Scan Codex becomes non-pausing/unblurred.

## 1.1.0 — Portable light, projector expansion and Scan Codex

Primary themes:

- Dynamic/Mobile Mirage Light consumers built on the operational `DYNAMIC_VISUAL` client runtime delivered in 1.0.5;
- portable lanterns with Focus/Flood/Ambient/Off modes;
- Glow Dust rechargeable energy/battery loop;
- portable projectors plus wall/table/ceiling/presentation variants;
- Mirage Scan Codex: persistent searchable/filterable scan library, favorites and multiple distinct snapshots of the same entity type;
- dedicated scan-copy station/lectern workflow that creates physical projector-facing Entity Scan Cards;
- Dragon Egg / End Resonance special Core semantics for compatible Field/Prism chassis.

Detailed requirements live in `WAITLIST-1.1.0.md`.

## 1.2.0 — Interactive holograms & UV ecosystem

Primary themes:

- direct grab/hold/free-rotation interaction for rendered holograms;
- stable pivot/ownership/multiplayer interaction rules;
- UV Shoulder Light as a high-drain shoulder device;
- Auto UV target detection as a later shoulder-upgrade family;
- UV defensive fire/damage/slow behavior shaped by Focus/Flood/Ambient geometry;
- UV Marks, hidden symbols and a mark-authoring workflow;
- Crying Obsidian UV reveal/resonance for permanent installations and RP/puzzle builds.

Detailed requirements live in `WAITLIST-1.2.0.md`.

## Optional/addon direction

Create Blueprint/schematic projection remains an optional bridge/addon direction rather than a required core feature. The 1.0 source registry/render-provider seams are intended to allow additions such as `BLUEPRINT` without rewriting the four built-in source families.

## Version progression rule

Normal implementation work advances through monotonically increasing `1.0.x` snapshots, even while pieces of the future 1.1 feature set are being built. The version becomes **1.1.0** only when that feature set is complete enough to ship as the next feature release. Internal `dev-X` labels are reserved for exceptional recovery/build snapshots rather than normal development. Preserve every shipped/relevant 1.0.x snapshot as a recoverable migration baseline.
