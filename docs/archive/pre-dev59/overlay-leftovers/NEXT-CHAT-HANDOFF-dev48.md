# Mirage Projector — next-chat handoff after dev.48

Current source candidate: **0.1.0-dev.48**.

## What changed

This is a focused hotfix after user QA discovered that the Crying Obsidian growth system still did not nucleate buds in-world.

### dev.48 hotfix
- Retargeted the Crying Obsidian nucleation mixin from `BlockBehaviour.randomTick` to `Block.randomTick`.
- Left the `isRandomlyTicking` state hook in place so vanilla Crying Obsidian remains random-tick eligible.

## User QA target

Verify that: 
1. a Crying Obsidian block with a lava source above and air/water below now spawns a small Crying Obsidian bud;
2. the spawned bud can still grow through medium -> large -> cluster;
3. the dev.47 visual/UI fixes remain intact.

## Notes

- This snapshot is intentionally minimal and only addresses the nucleation bug reported after dev.47 QA.
- Local container compile was not available through the normal project wrapper, so this remains a source snapshot pending user Windows compile/QA.
