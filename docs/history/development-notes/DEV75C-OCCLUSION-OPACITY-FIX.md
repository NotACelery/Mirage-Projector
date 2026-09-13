# Mirage Projector dev.75c — vanilla opacity handoff correction

Status: source candidate accumulated into dev.75d.

## QA symptom

The dev.75b authoritative virtual backend solved and synchronized fields correctly across chunks, but solid iron walls appeared almost transparent to Mirage light. Vanilla sources in the same numbered-floor test produced real shadows and had to route over/around the wall; the Mature virtual field appeared to continue through it.

## Root cause

`MirageLightOcclusion` correctly called vanilla `LightEngine.getLightBlockInto(...)`, but supplied a hard-coded final `simpleOpacity=1`. That parameter is the destination state's resolved light-block opacity fallback, not a generic minimum. Opaque destinations were therefore treated almost like air unless a shape-occlusion special case happened to stop the edge.

## Fix

For every candidate edge:

```text
destinationOpacity = max(1, toState.getLightBlock(level, toPos))
```

- opacity >=15 rejects the edge immediately;
- otherwise the real opacity is passed to `LightEngine.getLightBlockInto(...)`;
- face-shape occlusion remains vanilla-owned;
- partial opacity becomes additional fixed-point traversal cost;
- no slab/stair/mod-specific hard-coded table is introduced.

## Resulting contract

A fully opaque wall cannot be crossed. A finite wall may still receive light behind it only through an actual cardinal route around a real edge. dev.75c deliberately did **not** yet add special penalty to that detour, so its longer path still used the profile's normal half-decay rate. dev.75d adds the follow-up natural-shadow detour penalty without regressing this opacity fix.

Network protocol remained 20 in dev.75c. No NBT changes.
