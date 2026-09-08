# Development notes — 0.1.0-dev.8

## Target fijo

- Minecraft 1.21.1
- NeoForge **21.1.244**
- Java 21
- ModDevGradle 2.0.146
- Gradle 9.2.1
- Parchment 2024.11.17

NeoForge 21.1.244 es baseline deliberado. No actualizarlo automáticamente.

## Build de Windows

Sólo existe `build.bat`. Busca Java 21, instala Gradle localmente en `.gradle-dist/` si falta y ejecuta `clean build --stacktrace`. No incluir `.gradle-dist`, `build`, `run` ni caches en snapshots.

## Baselines acumulados

- dev.4: build/run limpio en 21.1.244.
- dev.5: selector nativo + primera proyección de imagen real.
- dev.6: Plane renderer grande/transparente; culling y aislamiento Front/Back corregidos.
- dev.7: asset transport multiplayer + Item Mode + clearance; el usuario reportó que el proyecto sigue avanzando muy bien.
- dev.8: Core/Power source preparado; build/QA Windows pendiente.

## dev.8 — Core / Power

### Core Socket

El BlockEntity tiene ahora dos handlers independientes:

```text
Projection Item [slot 0]
Projection Core [slot 1]
```

El Core acepta material real, no un item custom obligatorio:

```text
Glass          -> Glass Core
Quartz         -> Quartz Core
Amethyst Shard -> Amethyst Core
Diamond        -> Diamond Core
Netherite Ingot-> Netherite Core
```

Los block forms equivalentes también son válidos.

Un mundo viejo sin `CoreItem` migra a Glass Core. Si el jugador retira el Core después, el slot vacío se preserva como vacío y el Mirage queda apagado.

### Curva provisional

| Core | Power | Scale max | Lift max | Float max | Future source cap |
|---|---:|---:|---:|---:|---:|
| Glass | 8 | 10 px | 16 px | 1 px | 1 |
| Quartz | 16 | 16 px | 32 px | 2 px | 1 |
| Amethyst | 48 | 48 px | 64 px | 8 px | 2 |
| Diamond | 96 | 80 px | 96 px | 16 px | 4 |
| Netherite | 192 | 160 px dev ceiling | 160 px | 32 px | 8 |

No son números de release; son la primera curva jugable para medir sensación y coste.

### Power cost actual

`ProjectionPower` consume presupuesto por:

- estabilización base;
- área de imagen;
- tamaño de Item projection + surcharge 3D;
- Projection Lift;
- rotation;
- floating amplitude;
- rotation-synced floating;
- Independent Back image.

GIF, Prism, Banner, Effigy y múltiples slots agregarán sus propios costes después.

### Regla de no pérdida

No sanitizar settings contra el Core al guardar. Ejemplo:

```text
Diamond Core -> Mirage 70 px
retirar Diamond / poner Glass
=> Mirage OFF, settings 70 px se conservan
volver a Diamond
=> Mirage vuelve sin reconfigurar
```

### Doble límite

Core y chassis son independientes. Compact normal:

```text
scale <= 10 px
lift  <= 32 px
float <= 4 px
```

`Debug chassis: ON` sólo puede ser aplicado por Creative y salta esos límites de Compact para stress-test, pero NO salta límites del Core ni la regla física `floatAmplitude <= lift`.

### Core visual

El cubo de diamante hardcodeado fue removido del JSON. El BER dibuja un bloque de material escalado a 2x3x2 px en el mismo volumen de referencia. Esto permite cambio visual inmediato al swap del Core sin crear cinco blocks/modelos distintos.

### GUI

Añadido:

- Core slot;
- Core profile + límites;
- Power bar;
- reason de fallo;
- Debug chassis;
- tooltips de sliders.

La GUI crece a 416x466 para mantener inventario y status sin pisarse. Revisar GUI Scale bajo en QA.

## QA solicitado para dev.8

1. `build.bat`.
2. Abrir projector viejo de dev.7: debe aparecer Glass Core por migración.
3. Sacar Core: Mirage se apaga, settings no cambian.
4. Reinsertar Glass: vuelve sólo si configuración entra en Glass.
5. Probar Quartz / Amethyst / Diamond / Netherite y mirar color/material físico del pilar.
6. Usar Scale 80 + Diamond con Debug ON; usar Scale 160 + Netherite.
7. Desactivar Debug: Compact debe rechazar >10 px sin borrar el valor.
8. Float > Lift debe apagar el Mirage con reason físico.
9. Shift-click de Core debe ir al Core slot; un item común al Projection Item slot.
10. Romper el block: Projected Item y Core deben dropear una sola vez.
11. Verificar que Image/Item/multiplayer de dev.7 no regresaron.

## Siguiente oleada sugerida

Con Core/Power estabilizado, atacar Image completeness / feedback:

- world-space clearance overlay;
- preview GUI;
- optional opacity/scanline/tint presentation;
- transfer progress;
- evaluar WebP sin introducir una dependencia absurda;
- después comenzar Mirage Display / Wide / Tall usando el mismo BlockEntity/Core architecture.

---

## Historial heredado

# Development notes — 0.1.0-dev.7

## Target fijo

- Minecraft 1.21.1
- NeoForge **21.1.244**
- Java 21
- ModDevGradle 2.0.146
- Gradle 9.2.1
- Parchment 2024.11.17

NeoForge 21.1.244 es un baseline deliberado del proyecto. No debe cambiarse automáticamente por seguir el MDK más nuevo.

## Windows: un solo build con doble click

La única ruta soportada es:

```text
build.bat
```

`build.bat` busca Java 21, instala Gradle 9.2.1 localmente en `.gradle-dist/` cuando falta y ejecuta `clean build --stacktrace`. Tanto éxito como error terminan en `pause`.

No incluir `.gradle-dist/`, `build/`, `run/` ni caches similares en snapshots.

## Baseline validado

- dev.4: build/run-clean confirmado sobre NeoForge 21.1.244.
- dev.5: importador nativo + primera imagen proyectada confirmados in-game.
- dev.6: renderer Plane confirmado in-game con proyecciones grandes y PNG transparentes; correcciones de culling y aislamiento Front/Back funcionan como baseline visual.
- dev.7: **source preparado; build/QA de Windows pendiente**.

## Oleada dev.7

Esta pasada toma varias implementaciones pendientes de una vez.

### 1. Multiplayer asset transport

Flujo nuevo:

```text
local image
  -> normalized PNG
  -> SHA-256 asset id
  -> 32 KiB chunks
  -> server assembly
  -> PNG signature + SHA-256 validation
  -> <world>/mirage_projector/assets/<hash>.png
  -> other client cache miss
  -> request by hash
  -> server 32 KiB chunks
  -> client SHA-256 validation
  -> local cache
  -> DynamicTexture
```

Reglas:

- normalized max: 4 MiB;
- network chunk: 32 KiB;
- upload session timeout: 30 s;
- download session timeout: 30 s;
- max simultaneous unfinished uploads per player: 4;
- max unfinished uploads globally: 64;
- server dedup: if `<hash>.png` already exists, repeated uploads are ignored;
- client request retry throttle: 5 s;
- local source path is never sent to the server;
- client runtime transfer/texture state resets on logout so changing worlds/servers in one Minecraft process does not reuse stale session state.

The client also opportunistically seeds the server when a cached asset is first loaded into a DynamicTexture. This helps migrate pre-dev.7 worlds: the original importing client can repopulate the new world asset store simply by loading the projection.

### 2. Item Mode

BlockEntity now has a real one-slot `ItemStackHandler`.

GUI:

- dedicated projected-item slot;
- player inventory + hotbar slots;
- `Projection source: Image / Item` toggle;
- the item remains stored even while Image is the active source.

Renderer:

- `ItemRenderer.renderStatic`;
- `ItemDisplayContext.FIXED`;
- fullbright;
- same rotation / lift / bob / scale controls as Image Mode;
- intended to preserve vanilla/modded item models and enchantment glint when they use the standard item renderer path.

Persistence:

- ItemStackHandler serialized with 1.21.1 registry-aware NBT;
- BlockEntity update tag synchronizes the stored stack for world rendering;
- destroying the projector extracts and drops the stored item.

### 3. Clearance warning

The GUI runs a conservative scan around the current projected envelope roughly every 10 game ticks.

Inputs:

- source mode;
- image aspect ratio or item scale;
- Scale;
- Projection Lift;
- Float amplitude.

Current result:

```text
Clearance: clear
```

or:

```text
Clearance: N block(s) intersect the projection envelope
```

The scan is intentionally conservative, especially for rotating projections. The future world preview/outline will be more geometrically exact.

### 4. Image source management

Front and Back import rows now include a small `×` button so either asset reference can be cleared without importing a replacement.

## QA objetivo de dev.7

### Build

1. Run `build.bat`.
2. Confirm Java 21 + NeoForge 21.1.244 in the build output.
3. Confirm JAR under `build/libs/`.

### Existing image behavior

1. Existing dev.6 Plane image still loads.
2. Rotation, bobbing, lift and large debug scale still work.
3. Mirrored / Readable / Independent still isolate one visible face.
4. Native import still works.
5. `×` clears Front/Back correctly after Apply.

### Multiplayer transport

Best test with two clients against one server/integrated LAN if practical:

1. Client A imports a new image and presses Apply.
2. Confirm server world gains `mirage_projector/assets/<hash>.png`.
3. Client B must not manually receive/copy the PNG.
4. Client B approaches/loads the projector.
5. It may briefly show the missing-asset paper while downloading.
6. The real image should appear automatically afterward.
7. Delete Client B's local `mirage_projector/cache/<hash>.png`, reconnect/reload and confirm it can fetch again.

### Item Mode

1. Open projector GUI.
2. Put a normal item in the projected-item slot.
3. Switch source to Item and Apply.
4. Confirm item floats, rotates and bobs.
5. Test a block item.
6. Test an enchanted item and verify glint.
7. Test at least one modded item.
8. Change Scale and Lift.
9. Break projector and confirm the stored item drops exactly once.
10. Re-place/reopen and ensure normal image mode remains independent from the stored ItemStack.

### Clearance

1. Image in open air -> `Clearance: clear`.
2. Increase Scale until it intersects a wall/tree -> warning count > 0.
3. Increase Lift so the image clears the obstacle -> count should decrease/clear.
4. Switch to Item source and confirm scan follows item scale.

## Known dev.7 limits

- WebP still pending.
- No upload/download progress bar yet.
- Server writes/assembles image bytes on the logical server thread in this first pass; the 4 MiB cap bounds the cost.
- No Core/Power system yet: debug Scale/Lift/Float limits remain global.
- GUI now includes inventory slots and is taller; responsive behavior on small windows/large GUI scale needs explicit QA.
- Item Mode is not Effigy Mode. Armor sets, hands/poses, banners and special equipment rigs remain separate future renderers.
- Clearance warning does not yet render a red in-world outline.
- `shouldRenderOffScreen=true` remains a debug-phase renderer safety net for giant projections; final LOD/culling still pending.

## Siguiente oleada recomendada

**Core / Power**:

- core slot;
- Glass / Quartz / Amethyst / Diamond / Netherite profiles;
- power budget;
- dynamic scale/lift/float limits;
- chassis hard limits;
- dynamic core visual.

Después de eso conviene hacer WebP + preview avanzado antes de multiplicar chassis.
