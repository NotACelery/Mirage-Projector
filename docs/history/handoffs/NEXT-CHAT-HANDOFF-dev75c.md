# NEXT CHAT HANDOFF — dev.75c

Baseline: **0.1.0-dev.75c source candidate**. Network protocol: **20**.

## Fix in this snapshot
Live QA of dev.75b showed that chunk lifecycle worked, but solid walls did not attenuate/block Mirage light. Root cause was `MirageLightOcclusion`: the final `simpleOpacity` argument passed to vanilla `LightEngine.getLightBlockInto(...)` was hard-coded to `1`. Vanilla expects the destination state's resolved light-block opacity. dev.75c now uses `Math.max(1, toState.getLightBlock(level, toPos))`, rejects opacity >=15 directly, and still delegates face-shape occlusion to vanilla.

Expected QA: a solid wall cannot be traversed. Light may appear behind a finite wall only by routing around a real edge; every additional voxel of that detour consumes fixed-point energy. Re-test the numbered iron platform with 1/2/3-block-high walls and compare against vanilla path behavior.

No protocol/NBT changes. All dev.75b lifecycle/tracking/legacy cleanup remains intact.
