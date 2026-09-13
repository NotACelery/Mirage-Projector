# NEXT CHAT HANDOFF — Mirage Projector dev.75a

Baseline: `0.1.0-dev.75a` source candidate.

dev.75 was intentionally split. dev.75a is the recoverable midpoint and must be preserved even if dev.75b work fails.

Implemented:
- authoritative read-time Mirage block-light merge for `Level` + `RenderChunkRegion`;
- protocol 20 source-descriptor sync;
- client deterministic solve + render-section dirtying;
- same-dimension server fanout and login/respawn/dimension full sync;
- Mature runtime no longer creates physical Crying Light Nodes;
- loaded legacy relays around known sources are removed;
- debug probe distinguishes Mirage / vanilla storage / effective.

Do NOT call it final dev.75 yet.

dev.75b next:
1. chunk load/unload source discovery and forced rebuild;
2. client chunk-arrival rebuild/coalescing;
3. tracking-chunk-scoped source delivery;
4. orphan legacy-node cleanup by loaded chunk/palette;
5. verify/directly bridge consumers that bypass BlockAndTintGetter if required;
6. optimize dirty-section batches;
7. full Windows + in-game regression and then rename/finalize as dev.75 if clean.
