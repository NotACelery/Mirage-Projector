# WAITLIST — 1.2.0 Interactive Holograms

Theme: projections stop being passive displays and become directly manipulable objects.

## Primary interaction — grab + free rotate

- Aim at a hologram and hold RMB to grab/manipulate it.
- Mouse/camera movement rotates the hologram freely in arbitrary 3D orientation, deliberately allowing playful "spin it around" interaction.
- Use a stable pivot and quaternion-ready orientation model; do not accumulate unrestricted Euler deltas as authoritative state.
- releasing RMB commits/ends the manipulation cleanly.
- interaction must work with the projection's real rendered envelope rather than the physical projector block alone.

## Interaction architecture

- server-authoritative ownership/permission rules for persistent changes;
- multiplayer feedback/interpolation without fighting between simultaneous users;
- manipulation should operate on generic `ProjectionTransform`, not source-specific data;
- source types can opt into/limit manipulation through capabilities;
- rendering and hit-testing remain separate from the interaction controller.

## Potential follow-ups within/after 1.2.0

- axis/pivot visualization while held;
- optional snapping (e.g. 15°/45°) without removing free mode;
- position/pivot manipulation after rotation is stable;
- source-specific handles only where genuinely useful;
- direct manipulation hooks exposed for optional addons.

## Create-bridge relevance

The free-rotation interaction is a prerequisite/enabler for inspecting 3D schematic holograms naturally. The core mod still does not parse Create schematics itself.
