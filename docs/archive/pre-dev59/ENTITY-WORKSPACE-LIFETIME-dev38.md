# Entity Workspace lifetime and card-kind invalidation — dev.38

Status: **authoritative current contract for Mirage Projector 0.1.0-dev.38+.**

## 1. Problem fixed

Before this pass, Humanoid Projected armor/hand snapshots deliberately survived removal of the Humanoid Scan Card so Mirage could act as a bodyless mannequin. That rule was correct for an **empty card slot**, but it was applied too broadly.

A reproducible failure was:

1. project a Humanoid body;
2. accept armor and/or Main/Off Hand snapshots;
3. remove the Humanoid card;
4. insert a Generic card, which removes the six Humanoid rows from the GUI;
5. remove the Generic card;
6. the old armor/hands reappear floating because the hidden virtual snapshots were never destroyed.

Besides being confusing, those snapshots remained serialized in projector state while there was no GUI capable of showing/editing them.

## 2. Two different lifetime events

Mirage must distinguish:

### A. Card becomes empty

Removing a Humanoid card **without activating another kind** clears only the scanned body. The six Humanoid virtual channels remain valid because the empty Entity workspace intentionally falls back to Humanoid/bodyless-mannequin mode.

This remains supported:

```text
Humanoid body + armor
        ↓ remove card
bodyless mannequin + armor
```

Horse remains contextual: removing a Horse card clears the Horse body and Horse virtual equipment as already defined before dev.38.

### B. A different card family becomes active

This changes which equipment rows exist. Any virtual snapshot owned by rows that disappear becomes invalid and must be destroyed immediately.

## 3. Transition matrix

| Newly active card kind | GUI equipment rows | Virtual state retained | Virtual state invalidated |
|---|---|---|---|
| `HUMANOID` | Head, Chest, Legs, Feet, Main, Off | Humanoid | Horse Incoming + Projected |
| `HORSE` | Saddle, Body Armor | Horse | all six Humanoid Incoming + Projected |
| `GENERIC` | none | none editable | Humanoid + Horse Incoming + Projected |

This rule is intentionally based on the **new active kind**, so it also repairs old/projector states that already contain stale hidden snapshots.

## 4. Same-kind replacement

Replacing Humanoid with another Humanoid card does **not** wipe Projected Humanoid equipment merely because the body changed. The slot family is still valid. Scanned equipment enters Incoming using the existing conflict/apply rules.

Likewise Horse -> Horse may preserve the current Horse Projected state while the new card contributes new Incoming data.

## 5. What gets cleared

Kind invalidation clears:

- every `VirtualEquipmentSnapshots` Incoming entry owned by the incompatible family;
- every Projected entry owned by that family;
- the incompatible family's pose preset back to its default (`Standing` for Humanoid, `Idle` for Horse).

The active scanned body is then replaced by the new card normally.

## 6. What must never be silently destroyed

**Physical staging ItemStacks are real inventory items, not virtual snapshots.** Card-kind cleanup does not delete them. Existing safety rules remain:

- accepted staging returns to the player immediately;
- leaving the Entity Workspace returns every remaining physical staging item;
- if inventory insertion cannot fit the remainder, it drops exactly once beside the player;
- breaking the projector drops any real staging that still physically exists.

A future UX pass may proactively return hidden staging at the instant of a kind switch, but that is separate from the dev.38 virtual-memory leak fix and must never be implemented by deleting real stacks.

## 7. Rendering invariant

`EntityProjectionState.hasProjectedEntityContent()` may legitimately be true without an active body only for a valid bodyless Humanoid mannequin. It must not become true because Generic/Horse context secretly retained Humanoid equipment.

After Humanoid -> Generic/Horse cleanup, later removal of that Generic/Horse card must **not** resurrect the previous armor/hands.

## 8. Persistence / migration invariant

Saving after a kind switch must serialize only virtual equipment compatible with the active/effective workspace family. dev.38 also repairs already-saved stale state on load **when an active scanned body exists**:

- active Humanoid body -> prune Horse virtual state;
- active Horse body -> prune Humanoid virtual state;
- active Generic body -> prune both virtual equipment families.

A save with **no active body** is intentionally not auto-pruned, because that may be a legitimate bodyless Humanoid mannequin. New card imports apply the same compatibility rule again, so stale dev.37-era data cannot hide behind an unrelated active card and later resurrect.

## 9. QA matrix

Required in-game checks:

1. Humanoid + full armor + both hands -> remove card: body disappears, six projected channels remain and render as bodyless mannequin.
2. From that bodyless state insert Generic: all Humanoid virtual rows/state disappear. Remove Generic: old armor/hands do not return.
3. Repeat Humanoid -> Horse: Humanoid virtual state disappears; Horse rows become the only editable family.
4. Horse with Saddle/Body -> replace with Humanoid: Horse virtual state is removed and does not return later.
5. Horse -> Generic: both Horse virtual channels clear.
6. Humanoid -> Humanoid: existing Projected Humanoid snapshots remain; new scan equipment follows Incoming/apply conflict semantics.
7. Same-kind Horse -> Horse: Projected Horse state remains unless explicitly changed.
8. Save/reload after every cross-kind transition and confirm stale virtual snapshots do not resurrect.
9. Repeat transitions with real physical staging present, close workspace and confirm every real item returns/drops safely rather than being deleted.

## 10. Recovery rule

Do not restore the pre-dev.38 interpretation that “Humanoid equipment always survives any card change.” The correct rule is narrower:

> **Humanoid equipment survives an empty-card/bodyless state, but no virtual equipment family survives activation of a card whose GUI no longer exposes that family.**
