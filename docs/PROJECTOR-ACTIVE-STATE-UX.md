# Projector Active-State UX — 1.0.0

Status: **implemented and stable for all six fixed projector chassis.**

## State model

These are distinct concepts and must remain distinct:

1. **workspace currently open**;
2. **source currently selected**;
3. **projection enabled/disabled**.

Opening a workspace never activates that source by itself.

## Source activation

Each Image / Item / Entity / Banner workspace exposes `Use <mode> mode`.

When accepted:

- that source becomes the selected projection source;
- the projector is enabled;
- the source workspace reports `Mode currently Active` and blocks redundant activation;
- the main source menu outlines the active source with a white border.

## TURN OFF

`TURN OFF` disables projection rendering without deleting or resetting:

- source selection/content;
- imported images/GIFs;
- Item/Banner virtual snapshots;
- Entity scan/equipment/pose state;
- presentation transforms;
- installed Core;
- chassis-specific source-bank state.

When OFF, no source button is visually claimed as active even though the remembered source remains stored for later reactivation.

## Persistence

Enabled state and active source are saved/synchronized independently and must survive normal save/reload, chunk lifecycle and multiplayer GUI use.

## Future resonance rule

Special future states such as Dragon Egg / End Resonance must override normal activation through an explicit special-state contract rather than pretending to be a fifth normal projection source. That gameplay belongs to 1.1.0.
