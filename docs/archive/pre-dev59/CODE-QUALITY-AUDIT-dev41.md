# Code quality / deprecation audit — dev.41

## Scope

This is a low-risk consolidation pass. It intentionally avoids large behavioral refactors of render/persistence code immediately before the planned material/chassis rewrite.

The goals are:

- identify dead/superseded concepts;
- remove placeholders that can mislead future work;
- separate migration storage from active gameplay state;
- reduce magic constants;
- keep current implementation documents authoritative;
- record large classes that should be split only with dedicated QA.

## Static hygiene results

Across current Java source at the start of dev.41:

- no wildcard imports;
- no tab indentation;
- no trailing whitespace;
- only one intentionally ignored cache-delete IOException was found;
- very long lines exist mainly in GUI button setup, packet type definitions and Mixin descriptor strings;
- Mixin target descriptors should not be reformatted mechanically merely to satisfy line length.

## Changes applied in dev.41

### 1. Network protocol magic literal centralized

Before:

```java
PayloadRegistrar registrar = event.registrar("18");
```

Now `MirageProjector.NETWORK_PROTOCOL` is the single source used by registration.

Reason: protocol is compatibility architecture, not an incidental local string.

### 2. Removed non-gameplay chassis placeholders

`ProjectionChassisProfile` previously contained `EFFIGY` and `COLOSSAL` despite neither having registered blocks or playable implementation.

They are removed. Current enum now models exactly six real chassis.

This is safe for current transport order because the removed placeholders were appended after all six real chassis. Existing real ordinals remain unchanged.

### 3. Image bank compatibility made explicit

Before:

```java
MAX_SLOTS = 9
```

This made nine slots look like current capacity.

Now:

```java
ACTIVE_MULTI_SLOTS = 4
PERSISTED_COMPAT_SLOTS = 9
```

Wide/Tall rendering is explicitly bounded by active four slots. Copy/save/read paths retain all nine for dev.33-dev.37 migration data.

### 4. Removed dead profile fields

- `ProjectionChassisProfile#sourceCapacity` was unused; active image capacity already comes from explicit layout methods and Prism face rules. The field/constructor parameter/accessor were removed.
- `ProjectionCoreProfile` carried an unused raw English `displayName` string even though UI uses translated components. The dead field/accessor were removed.

- Removed the unused `PLANNED_IMPROVED_AMPLIFICATION` source constant. The x1.50 value remains a documented balance target until Improved Core items actually exist; source should gain the constant when gameplay consumes it.

### 5. Temporary Core visual named as legacy

The chassis Core-center/dimension accessors are also renamed with a `legacyCore...` prefix. They exist only to place/scale the old material-block visual and are expected to disappear when the universal chamber owns its own dimensions.


`ProjectionCoreProfile#visualStack()` looked like a stable core rendering API even though the agreed design replaces it.

It is now `legacyBlockVisualStack()` with an explicit removal/redesign comment.

No runtime appearance is intentionally changed by this rename.

## Large-class hotspots

Current approximate file sizes before future decomposition:

| File | Lines | Primary responsibilities |
|---|---:|---|
| `MirageProjectorRenderer` | ~1370 | core, idle book, Image, Item, Banner, Entity, deferred Entity pass, bounds |
| `MirageProjectorBlockEntity` | ~790 | persistence, virtual snapshots, migration, all workspace state |
| `ImageProjectorScreen` | ~710 | Plane/Prism/MULTI UI, import, previews |
| `ProjectionPower` | ~700 | dimensions, costs, slider maxima, breakdown |
| `MirageProjectorScreen` | ~630 | global settings + power UI |
| `EntityProjectorScreen` | ~580 | multiple entity-family workspace layouts |

These files are maintainability risks, but blindly splitting them during a behavior-heavy release creates more regression risk than value.

### Recommended future splits

After the chassis/core redesign stabilizes:

**Renderer**

- `PhysicalProjectorRenderer` / Core Chamber;
- `ImageProjectionRenderer`;
- `ItemProjectionRenderer`;
- `BannerProjectionRenderer`;
- Entity deferred renderer/coordinator.

**BlockEntity/state**

- asset/image state;
- Entity workspace state;
- virtual equipment state;
- upgrade-transfer data serializer.

**Power**

- dimensions/aspect geometry;
- PU cost calculation;
- dynamic control limit solver.

Do each split in an isolated build-clean snapshot with before/after QA; do not combine all decomposition with new gameplay mechanics.

## Documentation cleanup policy

README and DEVELOPMENT previously embedded many complete historical dev sections. That created contradictory “stable” statements even when old paragraphs carried small superseded notes.

From dev.41:

- README is current-user/current-feature focused;
- DEVELOPMENT is current architecture + active backlog focused;
- CHANGELOG owns chronology;
- old focused docs remain historical evidence;
- `DOCUMENTATION-AUTHORITY-dev41.md` decides precedence.

## Historical source that remains intentionally

### Nine image-bank persisted slots

Not dead code. Required so opening/saving a corrected world does not destroy dev.33-dev.37 data before migration can recover it.

### Legacy `.png` asset path

Not dead code. Required for worlds/assets created before dev.39 `.asset` transport.

### Pre-Core Compact Glass migration

Not automatically removed. It may still be required to load pre-Core worlds. The separate question of whether **new** Compact projectors get a starter Glass Core is unresolved and documented in the current implementation audit.

### Old NBT migration branches

Do not delete based only on apparent inactivity. Each must be tied to a known historical schema before removal.

## Formatting strategy

No repository-wide mechanical Java formatter is introduced in dev.41 because:

- no formatter configuration currently exists;
- Mixin descriptors and mapping-sensitive signatures contain intentionally long literals;
- a mass formatting diff would obscure the real logic changes in this audit.

Preferred style going forward:

- 4-space indentation;
- braces on Java block line as currently used;
- one statement per line;
- split GUI/network expressions when readability benefits;
- no wildcard imports;
- no hidden empty catches except best-effort cleanup paths with a comment;
- comments explain compatibility reasons, not obvious syntax.

A dedicated formatter may be adopted later as a separate no-behavior commit/snapshot.
