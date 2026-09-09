> **Historical note — amended by dev.38:** simple Humanoid card removal still preserves the bodyless mannequin, but activating Horse or Generic clears hidden Humanoid Incoming/Projected snapshots because those GUI rows disappear. Activating Humanoid clears hidden Horse snapshots. See `ENTITY-WORKSPACE-LIFETIME-dev38.md`.

# Mirage Projector — GUI Architecture Contract (dev.19)

## Why dev.19 exists

The dev.18 primary screen mixed source import, source switching, image previews, motion, appearance, power, Core handling, clearance, network state, snapshot slots and player inventory in the same fixed panel. At normal GUI scales those systems visually collided and source-specific controls became difficult to discover.

From dev.19 onward the GUI follows a hard separation-of-responsibility rule:

> **The primary GUI edits how an already-selected projection is presented. Dedicated workspaces edit what is being projected.**

This is a permanent architecture rule and should not be collapsed back into one giant screen.

## Primary Projection Settings

The projector block opens the global presentation screen. It contains only:

- active source summary (read-only),
- source-workspace navigation,
- Scale,
- Projection Lift,
- Rotation enabled/period/direction/orientation,
- Floating enabled/amplitude/mode/timing,
- Lighting,
- Ghost Effect,
- Tint,
- Creative-only chassis debug override,
- physical Projection Core slot,
- Core capability summary,
- current power usage,
- clearance summary,
- player inventory.

Source mode is no longer changed by cycling a generic button. A source becomes active from its own workspace, which prevents accidentally switching away while editing unrelated presentation settings.

## Projection Core UX

The Core slot is a first-class section rather than a tiny slot embedded between unrelated buttons.

When empty, hovering it must show accepted Core tiers in ascending order. Each entry includes Power, maximum Scale, maximum Lift and maximum Float. The adjacent summary shows the installed Core and its active limits. The physical slot remains visible and an additional material-block icon makes the active Core readable at a glance.

The dev.19 provisional Core curve is:

| Core | Power | Scale | Lift | Float | Future source capacity |
|---|---:|---:|---:|---:|---:|
| Glass | 16 PU | 16 px | 32 px | 2 px | 1 |
| Quartz | 32 PU | 32 px | 64 px | 4 px | 1 |
| Amethyst | 96 PU | 80 px | 96 px | 12 px | 4 |
| Diamond | 192 PU | 128 px | 128 px | 24 px | 8 |
| Netherite | 384 PU | 160 px | 160 px | 32 px | 16 |

The chassis remains a hard geometric cap. A Netherite Core does not turn Compact into Colossal.

## Image Workspace

Image import no longer lives in the primary screen.

Current Plane geometry:

- Front preview + import/replace/clear,
- Back preview + import/replace/clear,
- Back semantic mode: Mirrored / Readable / Independent,
- Vertical Flip,
- Scanlines,
- explicit face count.

The preview area is deliberately large enough to identify the actual image rather than relying on hashes or filenames. Face labels and native import-dialog titles are resolved from translation keys, so the workspace follows the active Minecraft language.

### Four-face contract

Prism geometry is permanently defined as four physical faces: North, East, South and West. The Image Workspace already changes its preview layout to four face cards for Prism geometry.

**Current dev.19 boundary:** East/West persistence/import is still waiting for the Prism source-bank data model. Those cards are visibly marked as reserved rather than silently pretending they are editable. When Prism lands, all four cards must have independent import/clear controls and thumbnails.

## Item Snapshot Workspace

Item capture is no longer mixed with the global screen.

The workspace contains:

- one virtual snapshot slot,
- one large live 3D preview,
- player inventory,
- an explicit `Use snapshot` action.

Clicking or shift-clicking a real inventory item captures one visual snapshot without removing it. The slot remains non-obtainable and cannot become storage.

Capturing an item automatically makes Item Snapshot the active source. The explicit activation button exists for already-captured snapshots.

## Entity / Humanoid Workspace

Entity editing remains one **dynamic** workspace instead of separate menu types for every entity category. This is intentional because the UI must react to the inserted scan card:

- Humanoid context: six Incoming/Projected channels (Head, Chest, Legs, Feet, Main Hand, Off Hand).
- Horse context: Saddle + Body Armor.
- Generic context: no fabricated editable equipment slots.

Keeping this as one adaptive workspace avoids duplicated state and preserves the previously defined rule that removing a Humanoid card does not delete the six humanoid projected channels.

A future visual redesign may give Humanoid/Generic different panel compositions, but they should continue sharing the same underlying entity workspace/state model unless a real technical need appears.

## Preview ownership

Previews belong to the workspace that understands the source:

- Image Workspace -> face thumbnails / later Banner 3D face preview,
- Item Workspace -> 3D item/block/equipped armor preview,
- Entity Workspace -> fitted entity/humanoid/horse preview,
- Primary Settings -> no cramped source preview.

This rule prevents the primary screen from becoming a dashboard of tiny, unreadable previews again.

## Temporary Debug Handbook

`Mirage Debug Handbook` is a temporary non-stackable development manual available from the Mirage creative tab.

Its pages use `Component.translatable(...)`; therefore text follows the currently active Minecraft language instead of being baked into one language. dev.19 ships English, Spanish (Spain) and Spanish (Chile) entries.

It documents:

- workspace separation,
- Core tiers,
- Image workflow,
- Item Snapshot workflow,
- Entity Scan/Humanoid workflow,
- global presentation controls,
- known development boundaries.

The handbook is intentionally temporary. A final in-world manual/catalog can replace it once mechanics stabilize.

## Final dev.19 spacing/presentation pass

The primary screen uses distinct non-overlapping vertical bands. Appearance owns Lighting/Ghost plus Tint/Creative Debug; Projection Core starts below it and owns the physical slot, visual Core icon, limits, power bar, chassis and clearance. The player inventory starts only after the Core band. This is a hard layout rule: later controls must not be inserted by stealing pixels from an adjacent section.

The screen remains fixed-size in dev.19, so multi-GUI-scale validation is still mandatory. If a supported GUI scale cannot fit the panel, the next correction should compact or scroll a section rather than reintroduce overlapping controls.

## Waiting list after dev.19

Priority order:

1. Windows compile / API repair of dev.19.
2. In-game responsive QA at multiple GUI scales.
3. Prism four-face source-bank persistence + four independent imports.
4. Banner source workspace / 3D preview integration.
5. Humanoid pose presets + pose button.
6. Pose-aware / entity-bounds-aware clearance instead of conservative square-only assumptions.
7. Full special-renderer QA for Ghost/Tint (glint, eyes, trims, modded layers).
8. Optional accessories/backpack/artifact adapters only after base Humanoid workflow is stable.
9. Long-term Entity Scan catalog/book remains an idea, not current scope.
