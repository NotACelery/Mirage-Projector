# Mirage Light Profile Foundation — 1.0.0

`MirageLightProfile` is the solver-facing description of a Mirage light source.

## Profile fields

A profile defines:

- conceptual light level;
- fixed-point substeps per visible light level;
- normal air-step cost;
- geometry-detour extra cost;
- maximum solve radius;
- decay mode;
- source shape;
- optional direction/cone data;
- RGB identity.

The solver operates in fixed-point energy units and converts back to vanilla-compatible 0..15 visible light at read time.

## Runtime-supported 1.0.0 shape

`OMNIDIRECTIONAL` is the currently solved runtime shape for static Mature Cluster lighting. Directional/cone/plane-style profile values remain reserved by the architecture for future consumers and must not be presented as 1.0.0 gameplay features.

## Decay modes

Current runtime supports:

- `VANILLA` — one visible light level per normal step;
- `EXTEND` — fixed-point substeps allow slower visible decay.

The Mature Cluster uses two substeps per visible level, producing the stable open-space half-decay curve.

## Detour cost

Open travel and geometry-forced detour cost are separate parameters. The solver must not hard-code Crying-Obsidian-specific wall behavior. A profile can keep normal open decay while charging additional energy for path segments that exist only because geometry forced backtracking/overshoot.

## Runtime lifecycle

Profile description is independent from how a solved field is consumed:

- `STATIC_WORLD` — server-authoritative, section-synchronized gameplay/world light;
- `DYNAMIC_VISUAL` — reserved separate lifecycle for future moving/portable visual emitters.

A moving source must not be implemented by rebuilding `STATIC_WORLD` sections every frame.
