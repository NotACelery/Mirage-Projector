# NEXT CHAT HANDOFF — 0.1.0-dev.76a

## Purpose
Build-only hardening for cumulative Windows checkouts after the dev.76 STATIC_WORLD architecture rewrite.

## What failed
The clean dev.76 snapshot removed `MirageLightSourceSyncPayload.java`, but extracting/copying the snapshot over an existing dev.75 project does not delete files that disappeared from the ZIP. The stale dev.75 Java source therefore remained in `src/main/java/.../network/` and Gradle compiled it. Its old handler called `ClientMirageLightSync.apply(MirageLightSourceSyncPayload)` even though dev.76 accepts only `MirageLightSectionSyncPayload`.

## Fix
A compatibility tombstone now deliberately occupies the legacy source path. It is a plain final Java class with no payload type, codec, handler, or registration. Overlaying dev.76a therefore overwrites the stale implementation.

## Runtime architecture
Unchanged from dev.76:
- server-only STATIC_WORLD solve;
- authoritative 16x16x16 section transport;
- client mirrors sections and merges via max(vanilla, Mirage);
- protocol 22;
- no client static source-descriptor solver.

## QA gate
Run `build.bat`. Expected result: the previous `MirageLightSourceSyncPayload cannot be converted to MirageLightSectionSyncPayload` compile error is gone. Deprecation warnings are unrelated and non-fatal.
