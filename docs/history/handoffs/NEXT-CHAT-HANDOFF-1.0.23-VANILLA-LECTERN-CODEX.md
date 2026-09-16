# Next Chat Handoff — Mirage Projector 1.0.23 Vanilla Lectern Codex

Baseline: **1.0.23**  
Minecraft: **1.21.1**  
NeoForge: **21.1.244+**  
Network protocol: **35**  
ProjectionSettings format: **3**

## What changed

The 1.0.22 standalone `mirage_projector:duplicating_lectern` was rejected during runtime QA. 1.0.23 removes that block/item/resources and makes the normal vanilla `minecraft:lectern` the physical Codex host.

Right-clicking an empty vanilla Lectern with a Mirage Scan Codex inserts the exact Codex ItemStack through vanilla `LecternBlock.tryPlaceBook(...)`. Right-clicking a Lectern that contains a Mirage Codex opens the Codex browser in Lectern copy mode. The screen can select/favorite scans, consume one Paper to duplicate the exact selected frozen root into an Entity Scan Card, or return the Codex to the player.

The normal handheld Codex browser remains available and still has no blur/dimming. Its layout is now explicitly book-like rather than the former flat dark rectangle.

## QA priorities

1. Put a Mirage Scan Codex on a vanilla Lectern and confirm the actual vanilla Lectern enters its book-present state.
2. Right-click it and verify the custom Codex UI opens instead of vanilla LecternScreen.
3. Duplicate several distinct captures, including repeated captures of one entity type.
4. Verify one Paper is consumed per Survival copy and no Paper in Creative.
5. Use Take Codex and confirm the exact same Codex library/selection survives removal.
6. Break the occupied Lectern and verify vanilla drop behavior returns the Codex.
7. Verify normal books/book-and-quill still use completely vanilla Lectern behavior.
8. Confirm normal right-click of a held Codex still opens the non-Lectern browser and exposes no duplication controls.
9. Confirm there is no `mirage_projector:duplicating_lectern` in Creative/registry resources.
