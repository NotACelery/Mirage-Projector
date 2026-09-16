# Mirage Projector 1.0.21 — Runtime QA Render / Invalidation Corrections

1.0.21 is the direct runtime-QA follow-up to the 1.0.20 Sodium light-bridge hotfix. It keeps network protocol **34** and `ProjectionSettings` format **3**.

## Virtual-light render invalidation

1.0.20 confirmed that Focus/Flood/Ambient Mirage light can reach rendered terrain through the packed-light bridge. Runtime QA then exposed a second problem: baked chunk meshes do not own only the light samples inside their exact 16×16×16 section. Faces and ambient-occlusion edges/corners can sample the one-block neighborhood across section boundaries. Mirage previously dirtied only the section whose virtual-light bytes changed (plus the older vertical overlay workaround), so adjacent wall/floor/ceiling meshes could remain dark or lit until proximity or a block update forced an unrelated rebuild.

1.0.21 separates static and moving invalidation. Static/authoritative updates are rare and invalidate the complete one-section render halo. `DYNAMIC_VISUAL` emitters instead snapshot aggregate light before a solve and compare pre/post boundary bytes: neighboring render sections are rebuilt only when the shared face/edge/corner actually changed. This preserves moving-Lantern responsiveness without forcing a full 3×3×3 halo every refresh.

## Shoulder Device transform

The mounted item transform used a negative 1.32-block local Y translation and appeared near/below the player's feet. 1.0.21 corrects that axis to positive shoulder height. The shoulder Lantern dynamic-light origin is also moved from a generic point in front of the eyes to the right-shoulder area while retaining the player look vector for Focus/Flood beam direction.

## Mirage Hand Projector output

The Hand Projector correctly copied/serialized portable profiles and its GUI/state synchronization worked, but ordinary Image/Item/Entity/Banner holograms were passed through the normal fixed-projector renderer. That renderer evaluates a physical Core before rendering content. Portable projectors deliberately clear that Core because they are powered by the embedded rechargeable cell, so the fixed render gate silently rejected otherwise valid portable output.

1.0.21 adds a hologram-only portable render entry point. The Hand Projector validates embedded-cell/compact-chassis power first, then reuses the existing source renderer without the physical Core/book presentation gate. War Banner keeps its dedicated path. GUI toggling now uses the same portable-power validation so an overloaded copied profile cannot enter an inert ON state.

## Runtime QA gate

Build under Java 21 / NeoForge 21.1.244+, then verify with Sodium 0.8.12 enabled:

- move/turn Focus, Flood and Ambient sources across section/chunk boundaries and confirm no stale dark/lit mesh patches persist;
- inspect broad walls, floor/ceiling transitions and obstacle edges;
- inspect Shoulder Lantern placement in third person and ensure the physical device sits at the right shoulder;
- copy valid fixed-projector Image/Item/Entity/Banner profiles into the Hand Projector, insert a charged cell and confirm output through RMB and GUI activation;
- move an active Hand Projector between hand, inventory and Shoulder Device and confirm persistent output/drain remains correct.
