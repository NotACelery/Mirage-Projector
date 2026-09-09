# Current implementation audit — Mirage Projector 0.1.0-dev.39

Status: source-candidate audit. Windows build and in-game QA pending.

## Authoritative implemented systems

- Minecraft 1.21.1 / NeoForge 21.1.244 / Java 21 / protocol 18.
- dev.38 Power system remains active: Core base PU × chassis multiplier × Core amplification, dynamic PU-bounded sliders, quadratic overdrive, tiny Ghost rebate.
- six current chassis use horizontal furnace-style FACING; Prism's N/E/S/W source semantics remain world-cardinal.
- idle vanilla book renders on all six chassis whenever no renderable source exists, independent of Core presence.
- Entity workspace incompatible virtual equipment is purged when a newly active scan changes the editable entity family; physical staging return/drop semantics remain safe.
- Field Image contract is one continuous Plane. No active 3x3 Field layout exists. Nine bank slots survive only as dev.33-dev.37 migration storage.
- Wide = SINGLE continuous source or optional four-cell 4x1 MULTI. Tall = SINGLE continuous source or optional four-cell 1x4 MULTI. MULTI cells are equivalent squares.
- Wide/Tall SINGLE sizing is centralized in `ProjectionImageSizing`; renderer and PU use the same dominant-axis aspect-preserving dimensions.
- Prism remains four lateral N/E/S/W faces, one source each, no top/bottom, no stacking. Image faces use adaptive nominal envelopes: horizontal 80x32, vertical 32x80, near-square 48x48; face geometry overdrive is charged independently before sum.

## Image/animation pipeline dev.39

- importer identifies real bytes, never decoder-by-extension;
- static PNG/JPEG/WebP/BMP supported and normalized to PNG;
- GIF supported as preserved animated asset;
- renamed GIF remains GIF;
- animated WebP and APNG are detected and explicitly rejected for now, regardless of extension;
- unknown content is rejected rather than opportunistically decoded;
- new client/world files use `<sha256>.asset`; historical `<sha256>.png` remains readable;
- transport envelope is 8 MiB with 32 KiB chunks and SHA-256 verification;
- `ProjectionTextureCache` owns one DynamicTexture for static assets or all composed GIF frame textures; invalidation releases the whole set;
- GIF decode is bounded by 1024x1024, 128 frames, 16,777,216 total frame-pixels, 20 ms minimum effective delay and 5 minute loop ceiling;
- Prism and Wide/Tall MULTI sample one animation clock per layout render so reused GIF IDs stay synchronized.

## Known pending QA / not yet build-clean

1. Run Windows `build.bat` and resolve mapping/API errors if any.
2. Real WebP decoder Jar-in-Jar verification in produced mod JAR.
3. Renamed-format QA: GIF-as-PNG accepted/animated; animated WebP-as-PNG/JPG/GIF rejected; APNG rejection.
4. GIF disposal/transparency/timing QA with real-world samples.
5. GIF memory/performance QA near technical limits and multiplayer transfer.
6. Prism adaptive aspect visual/PU QA with horizontal, vertical and near-square sources on different faces simultaneously.
7. Regression QA inherited from dev.38/dev.37: Field/Wide/Tall, Power sliders, facing, idle books, Entity workspace lifetime and Entity/projector/water depth ordering.

## Explicitly not in dev.39

- Animated WebP playback;
- APNG playback;
- video/MP4/WebM;
- Improved Core items/recipes/textures;
- chassis art/model refresh;
- Wide/Tall Banner multi-source redesign beyond existing Banner contract.
