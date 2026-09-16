# Next Chat Handoff — Mirage Projector 1.0.22 Duplicating Lectern

Baseline: **1.0.22**, Minecraft 1.21.1, NeoForge 21.1.244+, Java 21, network protocol 34, ProjectionSettings format 3.

1.0.21 remains awaiting runtime QA for dynamic-light stale-mesh convergence, corrected Shoulder Device placement and Hand Projector rendering. 1.0.22 intentionally advances an independent subsystem so those QA results can still be isolated.

1.0.22 adds `mirage_projector:duplicating_lectern` and `ScanDuplicationService`. Using a physical Mirage Scan Codex on the station resolves the exact selected capture from `ScanCodexSavedData`, consumes one Paper and creates one existing `entity_scan_card` containing the unchanged frozen capture root. It does not create a new ScanId, does not consume the Codex entry and does not copy favorite state. Output falls back to a safe player drop if inventory is full; Creative does not consume Paper.

The station is stateless by design and adds no BlockEntity/menu/network payload. First-pass visuals inherit vanilla Lectern art. Final recipe, bespoke art and richer station UI remain open.

Next QA gates: compile 1.0.22; test exact selected-snapshot duplication across multiple captures of the same entity type; confirm Paper accounting/full-inventory fallback; test produced cards in Entity/Humanoid/Horse projector workflows. Continue 1.0.21 light/shoulder/portable QA separately.
