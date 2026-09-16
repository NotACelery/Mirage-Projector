# Mirage Projector 1.0.22 — Duplicating Lectern Foundation

1.0.22 continues the 1.0.x implementation line toward 1.1.0. It keeps Minecraft 1.21.1, NeoForge 21.1.244+, Java 21, network protocol **34** and `ProjectionSettings` format **3**.

## Physical scan-copy workflow

The Duplicating Lectern is the first dedicated bridge from the persistent Mirage Scan Codex library back into the existing physical Entity Scan Card interoperability format.

Current flow:

```text
Entity -> Mirage Scan Codex capture -> exact selected capture
       -> Duplicating Lectern + 1 Paper
       -> physical Entity Scan Card
       -> normal projector Entity workflow
```

The copy path is server-authoritative. The station resolves the interacted Codex UUID and selected scan UUID against `ScanCodexSavedData`, validates the stored root through `EntityScanData`, consumes one Paper and writes the unchanged frozen root into the output card. The Codex capture remains in the library.

## Identity and safety

A duplicated card represents the same capture, not a new scan. `ScanId`, source provenance, frozen entity data, Player profile/skin fields, nameplate state and frozen equipment are preserved. Favorite state is intentionally not embedded because favorites are Codex-library metadata rather than part of the captured entity snapshot.

If inventory insertion fails, the finished card drops safely at the player. Creative players can duplicate without consuming Paper. Invalid/missing selections and missing Paper are rejected without mutating the Codex.

## Station scope

The 1.0.22 station is deliberately stateless: it has no BlockEntity, inventory or menu. The Codex already owns exact selection and SavedData already owns the heavy snapshot, so duplicating that state inside the block would create a second authority for no benefit.

The first-pass block model inherits the vanilla Lectern presentation strictly as implementation art. Final Mirage-specific art, Survival recipe and any richer station UI remain 1.1.0 polish and are not frozen by this snapshot.
