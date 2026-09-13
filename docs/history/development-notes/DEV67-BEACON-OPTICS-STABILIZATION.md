# dev.67 — Beacon optics stabilization and half-decay field

## Scope

dev.67 consolidates the live QA findings after dev.64–66. It changes Beacon/Crying Obsidian rendering and powered world-light behavior without changing network protocol, projector state formats, build tooling or cleaner behavior.

## Optical transition planes

Core Booster relay effects begin at Y `8.5/16` inside each Booster block. This plane matches the visible nested core rather than the block boundary. The incoming segment below it uses the previous relay state. The outgoing segment above it uses the newly applied material state.

Crying Obsidian continuation begins at stage-specific visible heights after the incoming Beacon beam is intercepted at the crystal base:

- Small: `4.5/16`;
- Medium: `6.5/16`;
- Large: `8.5/16`;
- Mature: no continuation.

## Beam mesh

The custom beam uses the vanilla Beacon four-side topology. The former custom topology accidentally connected one side diagonally across the square, which could read as an N or X from several camera rotations. dev.67 restores the vanilla side ordering while retaining Mirage-controlled width, color, alpha and rotation.

## Residual bursts

Residual bursts still begin visually at pixel 2. Collision clipping begins just outside the source block's own volume, preventing the source bud/cluster collider from immediately clipping its own ray to zero length. Later world obstacles are still respected.

## Powered light

Small, Medium and Large buds remain zero-block-light stages. Their energized behavior is optical: attenuation/continuation of the Beacon beam plus residual-ray emission. Only an energized Mature Cluster emits source light at level 15.

Mirage-owned light nodes are placed around that Mature source along six axes at odd Manhattan distances. Their levels reconstruct a half-speed falloff along those paths. A conceptual source level 5 targets `5,5,4,4,3,3,2,2,1,1`. Mature therefore reaches approximately 29 blocks before relay amplification.

The existing relay tier still combines width growth and Quartz/Radiance. Each tier adds one conceptual source level for the auxiliary field, capped to four tiers. Actual block emission remains capped at vanilla level 15.

All existing node safety rules remain: loaded chunks only, air/node replacement only, obstacle-path validation, strongest-overlap resolution and periodic self-cleanup.

## Preserved placeholders

`CONCENTRATE`, `DIRECTIONAL_SPOT` and `ROTATING_DIRECTIONAL_SPOT` remain architectural placeholders from dev.66 and perform no new world mutation in dev.67.

## Build/tooling

No file was deprecated, moved or removed, so `build.bat` and `CLEAN-MIRAGE-PROJECTOR.bat` are unchanged. `README.md` remains the only Markdown documentation file at project root; all other project documentation remains under `/docs`.
