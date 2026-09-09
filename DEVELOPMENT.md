# Mirage Projector — Development Guide (dev.41)

## 1. Estado de la línea actual

- Minecraft: **1.21.1**
- NeoForge: **21.1.244**
- Java: **21**
- Gradle: **9.2.1**
- Parchment: **2024.11.17**
- Mod version: **0.1.0-dev.41**
- Network protocol: **18** (`MirageProjector.NETWORK_PROTOCOL`)

`dev.41` es una pasada de consolidación/deprecación. No cambia intencionalmente Power, Image/GIF, Entity persistence ni payloads respecto de dev.40.

User QA confirmado antes de esta pasada:

- dev.40 corre in-game;
- Piglin shaking queda solucionado;
- Core físico actual tiene desaparición angle-dependent;
- superficies grandes de Glass/Glass Pane de chassis no funcionan visualmente como arte final.

Autoridad documental: `docs/DOCUMENTATION-AUTHORITY-dev41.md`.

---

# 2. Arquitectura actual

## 2.1 Chassis

`ProjectionChassisProfile` contiene **sólo seis chassis implementados**:

- COMPACT;
- DISPLAY;
- WIDE;
- TALL;
- FIELD;
- PRISM.

No introducir placeholders futuros en el enum. Los ordinals se usan en el menú Image; agregar un chassis requiere bloque real, UI/render contract y revisión de protocolo/compatibilidad.

Nominales actuales:

| Chassis | W×H | Lift | Float | Power |
|---|---:|---:|---:|---:|
| Compact | 10×10 | 32 | 4 | ×1.00 |
| Display | 32×32 | 48 | 12 | ×1.50 |
| Wide | 80×32 | 64 | 12 | ×2.00 |
| Tall | 32×80 | 96 | 16 | ×2.00 |
| Field | 128×128 | 144 | 24 | ×4.00 |
| Prism | 48×48 baseline/adaptive faces | 96 | 12 | ×2.00 |

Son targets nominales, no hard caps.

## 2.2 Facing

`MirageProjectorBlock.FACING` usa horizontal placement tipo Furnace.

- Plane orientation sigue FACING;
- Wide/Tall voxel shapes rotan cuando corresponde;
- Prism Image/Banner N/E/S/W siguen siendo cardinales del mundo.

## 2.3 Core / Power

`ProjectionCoreProfile` estándar:

- Glass 32;
- Quartz 48;
- Amethyst 64;
- Diamond 96;
- Netherite 128;
- amplification ×1.00.

`ProjectionPower` conserva dev.38:

```text
capacity = floor(basePU × chassisMultiplier × coreAmplification)
```

Los slider maxima son PU-aware. Overdrive aplica ratio² por componente excedido. Ghost rebate permanece pequeño.

`legacyBlockVisualStack()` es **temporal**. La siguiente implementación visual debe usar Core Chamber + ItemStack real según `CORES-AND-UPGRADES-dev41.md`.

## 2.4 Image source storage

`ImageSourceBank`:

```text
ACTIVE_MULTI_SLOTS = 4
PERSISTED_COMPAT_SLOTS = 9
```

- slots 0-3: Wide/Tall MULTI activo;
- slots 4-8: sólo migración dev.33-dev.37;
- Field jamás consume el bank como grid activo.

No reducir el wire/NBT de 9 slots sin un plan de migración/protocolo.

## 2.5 Image/GIF

Content sniffing identifica PNG/JPEG/WebP/BMP/GIF.

- static -> normalizado a PNG asset;
- GIF -> bytes animados preservados/decode bounded;
- Animated WebP/APNG -> reject explícito;
- extensión nunca elige decoder.

Nuevos assets: `<sha256>.asset`; fallback legacy `<sha256>.png`.

Wide/Tall SINGLE dimensions: `ProjectionImageSizing` es la única autoridad compartida por renderer y Power.

Prism: cuatro caras cardinales, adaptative aspect por cara, no stacking.

## 2.6 Entity

Mirage Entity clones:

- render-only client entities;
- no spawn/AI/tick;
- scan snapshot independiente de entidad original;
- equipment virtual separado de staging físico;
- changing entity family purga snapshots virtuales incompatibles;
- Piglin/Hoglin projection clones son inmunes a zombification sólo para neutralizar render shaking de dimensión.

No meter excepciones de dimensión directamente en renderer; normalización común vive en `EntityProjectionClientEntityFactory`.

---

# 3. Render order / depth invariants

Entity projections usan pasada diferida propia. Mantener estas reglas:

- no llamar `endBatch()` sobre `minecraft.renderBuffers().bufferSource()` global desde Mirage;
- el buffer diferido Entity es privado;
- Ghost necesita depth-test pero no debe reintroducir depth-write que corte agua/translucent geometry;
- Image/Banner/Item no deben sufrir side effects de flush Entity.

Antes de tocar este pipeline, conservar snapshot source y repetir QA Entity/projectors/water.

---

# 4. GUI invariants

- Main projector screen = presentation global + Core/Power.
- Workspaces = contenido de cada SourceMode.
- Preview renderers no dibujan labels/layout de la Screen.
- Power tooltip detallado sólo debe activarse desde su hotspot `?`, no desde toda la sección.
- Core slot debe tener columna reservada y barra used/capacity visible.
- No volver a añadir texto sin reservar su rectángulo; UI collision ya ha regresado varias veces.

---

# 5. Persistencia / migrations

No eliminar código aparentemente antiguo sin identificar la versión que recupera.

Compatibilidad deliberada actual:

- 9-slot Image bank para dev.33-dev.37;
- legacy `.png` assets pre-dev.39;
- pre-Core Compact Glass migration;
- Entity scan versions y compatibility recovery;
- old item-projection return migration.

Cualquier limpieza de NBT debe documentar exactamente qué worlds/cards deja de soportar.

---

# 6. Nueva progresión material (PLANNED)

Autoridad completa: `docs/CORES-AND-UPGRADES-dev41.md`.

Resumen:

- Cut Obsidian Shard: 4 por Crying Obsidian en Stonecutter;
- 8 shards + Fire Charge o Magma Cream -> Crying Obsidian;
- shards en loot temático;
- proceso natural Obsidian -> Crying Obsidian con lava/dripstone/cauldron, dos estados intermedios y optional Jade progress;
- Obsidian Spike;
- chassis visual sin broad Glass layers;
- universal Core Chamber;
- sólo Mirage Projector se craftea desde materias primas;
- Mirage -> Display -> Wide/Tall/Prism/Field;
- custom upgrade recipe debe copiar estado completo;
- cinco Improved Cores, same base PU + amplification;
- Improved Core blocks con tres shells y shell medio realmente rotado;
- Beacon optical relay/stacking;
- Scan Codex queda 1.1.0+.

No implementar partial recipes que rompan la futura transferencia de datos. Primero crear infraestructura de upgrade state-copy.

---

# 7. Código / mantenimiento

Audit: `docs/CODE-QUALITY-AUDIT-dev41.md`.

Hotspots grandes que necesitan refactor futuro con QA dedicado:

- `MirageProjectorRenderer`;
- `MirageProjectorBlockEntity`;
- `ImageProjectorScreen`;
- `ProjectionPower`;
- `MirageProjectorScreen`;
- `EntityProjectorScreen`.

No mezclar una descomposición masiva de estas clases con el mismo snapshot que introduce Improved Cores/Beacon/chassis models.

Style actual:

- 4 spaces;
- no wildcard imports;
- no tabs/trailing whitespace;
- comentarios de compatibilidad explican *por qué* existe la ruta;
- descriptors Mixin largos pueden superar line length por legibilidad/API exacta.

---

# 8. QA actual pendiente

Antes de release estable:

1. build Windows de dev.41;
2. confirmar que dev.41 no altera Power/Image/GIF/Entity behavior;
3. Entity Ghost vs water/projectors;
4. overlapping Entity Ghost;
5. special/modded RenderTypes/glint/eyes;
6. GIF stress + multiplayer transfer;
7. Prism adaptive aspect mixed faces;
8. después del redesign: Core Chamber visible desde todos los ángulos y desaparición de glass ghost layers.

---

# 9. Snapshot policy

Cada oleada debe producir source snapshot recuperable aunque quede QA pendiente.

No incluir:

- `.gradle`;
- `.gradle-dist`;
- `build`;
- `run`;
- IDE caches;
- downloaded distributions.

Priorizar raíz, `src`, `docs`, `gradle/wrapper` metadata necesaria.
