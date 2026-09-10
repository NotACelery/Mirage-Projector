# dev.34 — Entity custom names and Debug Handbook hand render

## Entity custom names

Entity Scan v5 stores whether a mob actually had a custom nametag and the frozen text separately from the ordinary display label. Projected mobs only receive Mirage's base nameplate when that explicit custom name exists; scanned Players continue to expose their frozen player name.

For older cards, Mirage conservatively treats a stored DisplayName that differs from the vanilla entity type description as a likely custom nametag. Rescanning remains the authoritative path.

## Debug Handbook

The screen keeps the local dark overlay from dev.32 and additionally cancels NeoForge `RenderHandEvent` only for the Debug Handbook stack while `DebugHandbookScreen` is active. Other held items/hands are not globally suppressed.
