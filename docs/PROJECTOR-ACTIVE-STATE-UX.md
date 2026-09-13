# Projector active-state UX contract

Status: **implemented for fixed projectors since dev.79d and retained through dev.79h; End Resonance override hook is present but Dragon Egg resonance itself remains 1.1.0.**

## State model

Never collapse these concepts:

```text
workspace currently open != active projection source != projection enabled
```

A player may edit/view Image while Entity remains the active world projection. Merely opening a source workspace never changes world output.

## `Use <mode> mode`

`Use <mode> mode` is the explicit activation boundary. When accepted:

1. that workspace's source becomes the active projection source;
2. projection becomes enabled;
3. the workspace shows that it is currently in use;
4. the corresponding Image/Item/Entity/Banner button in the main source menu receives a clear white outline.

## `TURN OFF`

Normal projectors expose an explicit `TURN OFF` action. It:

- stops/hides the current world projection;
- does **not** clear source data;
- does **not** reset presentation settings;
- does **not** eject/replace the Core;
- does **not** destroy Entity snapshots, Image banks/faces, Banner state or Item snapshots;
- preserves the previous source/configuration for later reuse.

While OFF, the main source menu shows no Image/Item/Entity/Banner button as actively projected. The remembered source may remain stored internally, but the UI must not represent it as presently active.

Do not represent OFF as another source enum/registry entry. Activation is device state, not projection content.

## Workspace feedback

Each source workspace/tab needs a clear indicator that answers **“is this what the projector is currently using?”** after `Use <mode> mode`. Exact art/copy can be refined during GUI polish, but it must not be confused with the selected/open tab state.

## 1.1.0 override — Dragon Egg / End Resonance

Field and Prism End Resonance deliberately override this normal control contract while a Dragon Egg remains installed:

- ordinary source/options remain suspended/read-only;
- normal shutdown cannot stop the portal/anomaly;
- pressing `TURN OFF` changes no state and emits the event message exactly:

```text
This doesn't seem to work...
```

The bypass ends only when the Dragon Egg is physically removed. At that point the saved ProjectionState is restored and normal source/ON-OFF controls regain authority.

This override must not mutate the suspended source or its restore snapshot.
