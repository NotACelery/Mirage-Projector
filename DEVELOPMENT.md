## Estado `0.1.0-dev.40` — Power UI repair + dimension-neutral entity clones

> **Autoridad incremental.** dev.40 no reemplaza las fórmulas Power dev.38 ni el pipeline GIF/Image dev.39. Corrige presentación del Power panel y normaliza estados ambientales de entidades reconstruidas. Protocol **18** sin cambios.

### Power / Capacity GUI

- Core slot conserva `CORE_SLOT_X/Y`, pero el texto Power empieza a la derecha del slot.
- Effective load dispone otra vez de barra visible, calculada como `used / available` y limitada visualmente a 100%; un estado sobrecapacidad puede señalarse con fill de error sin ampliar el rango del control.
- El breakdown de PU deja de usar todo el rectángulo Power como hitbox. Sólo el pequeño `?` del header dispara el tooltip.
- El cálculo Power no cambia en esta oleada.

### Piglin/Hoglin shake fix

- Reproducción confirmada: Piglin escaneado/proyectado en Overworld tiembla tanto en preview como in-world.
- La entidad Mirage se crea dentro del `ClientLevel` actual. `AbstractPiglin#isConverting()` depende de si la dimensión es piglin-safe y el vanilla renderer usa ese estado para el shake de conversión. No hace falta que Mirage llame `tick()` para que el renderer vea ese estado.
- Después de `living.load(scan.entityData())`, `normalizeForProjection` fuerza `setImmuneToZombification(true)` sólo en clones `AbstractPiglin` y `Hoglin`.
- El scan congelado no se reescribe. La entidad original no cambia. No se introduce AI/ticking.
- Como preview y world projection usan `EntityProjectionClientEntityFactory`, un solo fix cubre ambos.
- Documento: `docs/ENTITY-DIMENSION-NORMALIZATION-dev40.md`.

### QA dev.40

Usar `docs/DEV40-UI-ENTITY-QA.md`. Prioridad: Core slot limpio, barra de capacidad, tooltip sólo en `?`, Piglin/Hoglin estables en Overworld y regresión rápida de GIF/Wide/Tall/Field/Prism.

### Próxima oleada

Improved Cores no se deben implementar por intuición. Antes de recipes/assets, crear una matriz por **material × variant** que defina: Base PU heredada, amplifier, efecto especial, coste/penalización si existe, materiales de receta, propósito, chassis donde destaca y por qué no vuelve obsoleto al resto. Las familias candidatas discutidas hasta ahora son Standard/Amplified como base y posibles especializaciones Focused/Stabilized/Resonant; todavía no son contrato jugable.

---

## Estado `0.1.0-dev.39` — GIF / Animated Image + Wide/Tall SINGLE aspect authority

### dev.39 import failsafe / Prism addendum

- Import identifica PNG/JPEG/WebP/BMP/GIF por contenido; la extensión no selecciona decoder. Animated WebP/APNG se detectan y rechazan temporalmente incluso renombrados.
- Prism conserva N/E/S/W, una fuente por cara y cero stacking. Cada cara usa nominal adaptativo 80x32 horizontal, 32x80 vertical o 48x48 casi cuadrado; PU/overdrive geométrico se calcula por cara.
- Autoridad: `docs/IMAGE-FORMAT-IMPORT-CONTRACT-dev39.md` y `docs/PRISM-ADAPTIVE-ASPECT-dev39.md`.

> **Contrato autoritativo actual para Image/GIF.** Dev.39 conserva el Power/chassis/lifetime rework de dev.38 y añade la implementación animada. Cuando una nota histórica llame GIF "futuro", mandan esta sección, `docs/GIF-ANIMATED-IMAGE-dev39.md` y `docs/WIDE-TALL-SINGLE-ASPECT-dev39.md`. Network protocol **18**.

Inventario y QA de cierre: `docs/CURRENT-IMPLEMENTATION-AUDIT-dev39.md` + `docs/DEV39-GIF-QA.md`.

### GIF ya es una fuente Image real

- GIF no crea un nuevo `SourceMode`: entra por Image Workspace y reutiliza exactamente los mismos settings de presentación.
- Compact/Display/Field: un Plane continuo estático o animado.
- Wide/Tall: `SINGLE` admite una imagen/GIF continua; `MULTI` admite cuatro fuentes independientes 4×1 / 1×4, cada una estática o GIF.
- Prism: North/East/South/West pueden ser cuatro GIFs distintos. `Same Source on All Faces` copia el mismo asset id, sin duplicar bytes ni frame cache.
- Front/Mirrored/Readable reutilizan el mismo asset/frame actual; Independent puede tener Front/Back distintos.
- Flip, Scanlines, Tint, Ghost, Lighting, Rotation y Float se aplican al frame actual sin una ruta especial por efecto.

### Asset pipeline dev.39

Static:

`PNG/JPG/JPEG/WebP/BMP -> content sniff -> decode -> RGBA -> PNG normalizado <=8 MiB -> SHA-256 -> .asset -> red/world store`.

Animated:

`GIF -> validar/decode completo -> preservar bytes GIF <=8 MiB -> SHA-256 -> .asset -> red/world store -> decode once -> DynamicTexture por frame`.

- El path local original nunca se guarda ni se transmite.
- Nuevos caches/world assets usan `<hash>.asset` porque el contenido puede ser PNG o GIF.
- `<hash>.png` de dev.1-dev.38 sigue siendo fallback compatible tanto en cliente como servidor.
- El servidor valida firma PNG/GIF, límite por tipo e igualdad SHA-256; el envelope de red sube a 8 MiB y conserva chunks de 32 KiB.
- Protocolo sube **17 -> 18** para no permitir mezclas silenciosas dev.38/dev.39.

### Decoder/timing

- `GifAssetDecoder` lee canvas lógico, offsets de ImageDescriptor, delay y disposal de cada frame.
- Composición soportada: keep (`none/doNotDispose`), `restoreToBackground` y `restoreToPrevious`.
- dev.39 limpia `restoreToBackground` a transparencia, comportamiento elegido para proyecciones decorativas con alpha.
- `delay=0` usa fallback 100 ms; delays no-cero se clamped a mínimo 20 ms; delay máximo 10 s.
- Loop completo máximo 5 min; Mirage ignora finite loop-count y repite continuamente mientras la fuente esté proyectada.
- Playback usa reloj monotónico compartido del cliente. El mismo GIF reutilizado por varios projectors/slots/faces se mantiene sincronizado en ese cliente.
- No existe `currentFrame` en NBT ni tráfico por frame/tick.

### Safety limits GIF

- GIF bytes: máximo 8 MiB.
- Canvas lógico: máximo 1024 px por eje.
- Frames: máximo 128.
- Decoded frame-pixel budget: `width * height * frames <= 16,777,216`.
- Esto equivale, por ejemplo, a 512×512×64 o 1024×1024×16 como máximos de memoria aproximados.
- Estas restricciones son safety rails de memoria/red y no modifican Scale/PU.
- GIF no añade surcharge de PU: PU sigue modelando geometría/emisor; decoder/GPU se limita técnicamente para no introducir un coste de gameplay basado en estado client-only.

### Wide/Tall SINGLE — condicionales congeladas

La regla común es **sin crop y sin stretch**. `ProjectionImageSizing` es autoridad compartida entre renderer y `ProjectionPower`.

Wide, nominal 80×32:

- landscape o square (`sourceW >= sourceH`): `projectedW=Scale`, `projectedH=round(Scale*H/W)`;
- portrait (`H>W`): `projectedH=Scale`, `projectedW=round(Scale*W/H)`;
- portrait sigue siendo válido, pero empuja antes el nominal Height=32 y por eso entra antes a Geometry Overdrive.

Tall, nominal 32×80:

- portrait o square (`sourceH >= sourceW`): `projectedH=Scale`, `projectedW=round(Scale*W/H)`;
- landscape (`W>H`): `projectedW=Scale`, `projectedH=round(Scale*H/W)`;
- landscape sigue válido, pero empuja antes el nominal Width=32 y entra antes a Geometry Overdrive.

Ejemplos Scale 80:

- Wide + 800×320 -> 80×32;
- Wide + 1920×1080 -> 80×45, por tanto overdrive vertical moderado;
- Wide + 1080×1920 -> 45×80, overdrive vertical fuerte;
- Tall + 320×800 -> 32×80;
- Tall + 1080×1920 -> 45×80, overdrive horizontal moderado;
- Tall + 1920×1080 -> 80×45, overdrive horizontal fuerte.

MULTI no usa estas condicionales: conserva las cuatro celdas cuadradas dev.38 (Scale 80 -> Wide 80×20 / Tall 20×80; cada celda 20×20 y cada asset se aspect-fit dentro de la celda).

### QA prioritario dev.39

1. Compilar `build.bat` en Windows antes de marcar build-clean.
2. GIF transparente/disposal-heavy en Compact, Display y Field.
3. Wide SINGLE landscape, portrait y square; confirmar dimensiones/PU/slider max coherentes.
4. Tall SINGLE portrait, landscape y square; confirmar dimensiones/PU/slider max coherentes.
5. Wide/Tall MULTI con mezcla static/GIF y cuatro GIFs distintos.
6. Prism con cuatro GIFs independientes y después Same Source on All Faces.
7. Front/Mirrored/Readable/Independent con GIF; Mirrored/Readable deben mostrar exactamente el mismo frame actual.
8. Scanlines/Tint/Ghost/Fullbright/Flip/Rotation/Float sobre GIF.
9. Save/reload y reconnect; segundo cliente sin archivo local debe descargar y reproducir.
10. Assets legacy `.png` dev.38 siguen visibles y transferibles.
11. Rechazo claro de >8 MiB, >1024px, >128 frames y exceso de frame-pixel budget.
12. Repetir QA pendiente dev.38 (Power, Field/Wide/Tall layouts, facing, idle books, Entity cleanup, Entity/projector/water).

### Próxima oleada tras estabilizar dev.39

- Improved Projection Cores (items/recipes/textures, amplifier target inicial ~×1.50) y balance real de la curva de PU.
- Posible refresh de texturas/modelos de chassis.
- Después: hardening/performance de animaciones visibles masivas y demás backlog gráfico.

## Estado `0.1.0-dev.38` — Power System Rework + chassis/image contract recovery

> **Baseline acumulativo de Power/chassis/lifetime.** dev.39 lo conserva, pero para Image/GIF y el inventario vigente mandan la sección dev.39 y `docs/CURRENT-IMPLEMENTATION-AUDIT-dev39.md`.

### Power System Rework

- El modelo dev.8/dev.19 queda **deprecado**: los Cores ya no poseen límites hardcodeados de Scale/Lift/Float y los valores del chassis ya no son paredes absolutas que puedan dejar PU inútiles.
- Curva provisional de **PU base** estándar: Glass 32, Quartz 48, Amethyst 64, Diamond 96, Netherite 128.
- Capacidad efectiva:

  `floor(Core base PU × chassis multiplier × Core amplification)`.

- Multiplicadores de chassis jugables actuales: Compact ×1.00, Display ×1.50, Wide ×2.00, Tall ×2.00, Field ×4.00, Prism ×2.00.
- Todos los cores raw actuales son `Standard` con amplificación ×1.00. La arquitectura ya reserva `Core amplification` para futuros Cores mejorados; el primer target de diseño es ~×1.50 **sin aumentar la PU base del material**. Items/recipes/textures de esos Improved Cores siguen pendientes hasta QA de balance.
- Compact+Netherite y Field+Glass poseen ambos 128 Effective PU, pero Field usa esa energía con mucha mayor eficiencia geométrica. Esto es intencional.
- Los tamaños/Lift/Float de cada chassis son ahora **nominales**. Superarlos es Overdrive legal si existe PU suficiente.
- Overdrive usa penalización cuadrática por componente: `cost × (current/nominal)^2` sólo cuando el ratio supera 1. Scale/geometry, Lift y Float se penalizan de forma independiente.
- Float mantiene una única barrera física independiente de Power: cuando Floating está activo, `Float amplitude <= Lift`.
- Technical ceilings internos (`Scale 512 / Lift 512 / Float 128`) son sólo safety rails para búsquedas/sliders; **no son límites de gameplay documentables**.

### Fórmula PU dev.38

- Emisor/estabilidad: 2 PU para toda proyección no vacía.
- Geometría base: `ceil(area proyectada / 256)` PU antes de Overdrive (256 px² = 16×16).
- Lift base: `ceil(Lift / 16)` PU antes de Overdrive.
- Float base, sólo Floating ON: `ceil(Float / 2)` PU antes de Overdrive.
- Complejidad actual: Independent Front+Back +1; Wide/Tall MULTI con >1 source +1; Prism Image +2; Item +2; Entity +4; Banner Plane +1; Banner Prism +2.
- Features: Rotation +1; Rotation-synced Float +1; Fullbright +1; Image scanlines +1; Tint/Flip no cuestan PU.
- Ghost entrega un rebate óptico deliberadamente mínimo: `floor((grossPU - 2) × Ghost% / 3000)`. A Ghost 90% nunca supera 3% del coste no-base, redondeado hacia abajo; no puede volver gratis una proyección pequeña.
- `ProjectionPower.Breakdown` expone cada componente al tooltip de la GUI.

### Dynamic sliders / Power UX

- Scale/Lift/Float se reconfiguran hasta el **máximo actualmente pagable** con el Core/chassis/settings activos. El jugador no debe poder arrastrar deliberadamente a una zona inválida y tantear el umbral naranja.
- Cambiar Core o activar una feature cara recalcula extremos inmediatamente. Si una configuración legacy queda inválida, se intenta reducir Float, luego Lift y Scale sólo como último recurso.
- La GUI muestra Base PU, chassis multiplier, Core amp, Effective capacity, Load, tamaño actual vs nominal y `current/effective max` de Scale/Lift/Float.
- `Remaining PU` deja de ser la métrica principal. El desglose completo de PU aparece al pasar el cursor por la sección Power.
- Creative `Debug chassis` elimina penalizaciones de Overdrive para stress-test, pero **no** elimina requisito de Core, presupuesto Effective PU ni `Float <= Lift`.

### Chassis/Image contract corregido

- `Field` vuelve definitivamente a **un solo Plane continuo**. La cuadrícula 3×3/9 imágenes introducida en dev.33 fue una interpretación incorrecta y no forma parte del diseño vigente.
- `Wide` y `Tall` sí conservan cuatro fuentes, pero son **opcionales** mediante `ImageLayoutMode`: `SINGLE` = una imagen continua; `MULTI` = Wide 4×1 / Tall 1×4.
- MULTI usa cuatro celdas cuadradas simétricas: Scale 80 produce Wide 80×20 o Tall 20×80 (celdas 20×20), eliminando la magnificación desigual Tall vs Wide.
- `Prism` conserva cuatro caras laterales N/E/S/W independientes y world-cardinales.
- Valores **nominales** vigentes: Compact 10×10/Lift32/Float4; Display 32×32/48/12; Wide 80×32/64/12; Tall 32×80/96/16; Field 128×128/144/24; Prism 48×48/96/12.
- `ImageSourceBank` mantiene 9 slots serializados sólo para no destruir saves dev.33-dev.37. Field no consume ni muestra grid; si una migración no tiene Front continuo, slot 0 se recupera como Front. Wide/Tall consumen sólo slots 0-3 en MULTI.
- `ImageLayoutMode` permanece en settings/payload y protocolo **17**.

### Placement / GUI / lifetime fixes de la misma oleada

- Todos los projectors físicos poseen `HorizontalDirectionalBlock.FACING` y se colocan con orientación estilo furnace, mirando al jugador.
- Plane usa ese facing como orientación base. Prism Image/Banner conserva N/E/S/W cardinal y no remapea caras por la orientación física del bloque.
- Los seis chassis registrados (Compact, Display, Wide, Tall, Field y Prism) muestran el mismo libro vanilla flotante cuando no existe ninguna fuente renderizable. El idle marker no depende de tener Core: Compact ya no es el único que lo muestra por venir históricamente con Glass.
- Item Snapshot Workspace elimina el segundo título interno del preview; la Screen es la única dueña de labels y reserva el viewport del modelo, evitando `ITEM SNAPSHOT`/`ITEM PREVIEW` superpuestos.
- El fix de render dev.37 se conserva: Entity diferido en `AFTER_TRIPWIRE_BLOCKS` con `BufferSource` privado de Mirage. No volver a hacer `endBatch()` sobre el buffer global para arreglar Entity, porque eso regresó Image Mode en dev.36.

### Entity workspace lifetime / card-kind invalidation

La ausencia temporal de card y el **cambio de familia de card** son dos eventos distintos:

- retirar una Humanoid card sin insertar otra conserva los seis canales virtuales, permitiendo el maniquí Humanoid sin body;
- insertar una Humanoid card mantiene Humanoid y limpia cualquier Horse Incoming/Projected oculto;
- insertar una Horse card mantiene Horse y limpia los seis Humanoid Incoming/Projected, porque esas filas desaparecen de la GUI;
- insertar una Generic card limpia Humanoid y Horse virtuales, porque Generic no expone equipment editable;
- al limpiar una familia incompatible también se restablece su pose a `Standing`/`Idle`;
- la limpieza afecta **sólo snapshots virtuales**. Los items físicos que todavía estuvieran en staging nunca se destruyen silenciosamente: siguen sujetos al retorno/drop seguro del menú.

Invariante dev.38: **si un cambio de `EntityScanData.Kind` hace desaparecer una familia de slots de la GUI, ningún snapshot virtual perteneciente a esos slots puede quedar escondido en NBT/memoria ni reaparecer después al retirar otra card.**

### Documentación autoritativa

- `docs/POWER-SYSTEM-REWORK-dev38.md`: fórmula completa, Overdrive, Ghost, sliders, Improved-Core hook y deprecaciones.
- `docs/CHASSIS-IMAGE-LAYOUT-POWER-UX-dev38.md`: contratos por chassis, Wide/Tall SINGLE/MULTI, Field continuo, Prism, facing, migración y QA.
- `docs/MULTI-SOURCE-IMAGE-LAYOUTS-dev33.md`: queda únicamente como historia y debe llevar advertencia SUPERSEDED.
- `docs/ENTITY-WORKSPACE-LIFETIME-dev38.md`: contrato vigente de ownership/invalidación de snapshots al cambiar Humanoid/Horse/Generic.
- `docs/DEV38-CLOSURE-QA.md`: checklist de cierre estático + build/QA in-game antes de declarar dev.38 build-clean.

### QA prioritario dev.38

1. Field con una sola imagen cuadrada/vertical/horizontal: nunca debe aparecer cuadrícula, replicación 3×3 ni selector de 9 slots.
2. Wide SINGLE + Wide MULTI 4×1; alternar/reabrir y confirmar persistencia. Repetir Tall SINGLE + MULTI 1×4.
3. Comparar Wide/Tall MULTI con la misma imagen y mismo Scale: cada celda debe tener exactamente el mismo tamaño disponible.
4. Verificar que los valores nominales **no** son hard caps: con Core suficiente debe aparecer Overdrive y el slider puede pasar nominal; con Core débil el extremo termina antes.
5. Cambiar Glass→Quartz→Amethyst→Diamond→Netherite y comprobar Effective PU/máximos dinámicos.
6. Comprobar el desglose PU contra geometría, Lift, Float, features y source complexity; Ghost 90% sólo debe ahorrar una fracción mínima.
7. Confirmar que Float nunca supera Lift y que cambiar Core no deja sliders en estado inválido.
8. Colocar todos los chassis mirando N/E/S/W; Plane sigue facing. Prism mantiene caras world-cardinales.
9. Sin fuente renderizable, Compact/Display/Wide/Tall/Field/Prism deben mostrar el mismo libro flotante incluso con socket de Core vacío.
10. Entity lifetime: Humanoid con armor/manos -> retirar card -> sigue maniquí bodyless; insertar Generic/Horse -> desaparecen y se eliminan esos snapshots; retirar la nueva card -> la armor vieja **no** reaparece. Repetir Horse -> Humanoid/Generic para Saddle/Body.
11. Item Workspace: ningún label/texto debe pisar preview, slots ni otro texto en GUI scales normales.
12. Repetir QA de profundidad dev.37: Entity contra projectors y agua delante/detrás, más dos Entity Ghost superpuestas.

- Build Windows e in-game QA de dev.38 siguen pendientes; **no declarar build-clean antes de ambos**.

### Prioridad inmediata después de cerrar dev.38 — SUPERSEDED por dev.39

Esta prioridad ya fue ejecutada: **GIF / Animated Image import está implementado en dev.39** con decodificación controlada, timing/disposal, límites de resolución/frames/FPS/memoria, identidad SHA-256, transporte multiplayer y cache de frames. Los Improved Cores (items/recipes/textures) y posibles retoques de modelos/texturas de chassis quedan como la siguiente oleada de balance/arte y no forman parte de dev.39.

## Estado `0.1.0-dev.37`

- Baseline verificado anterior: dev.35 build-clean/in-game. dev.36 **sí llegó a ejecutarse in-game**, pero su QA reportó regresiones de profundidad/orden e Image Mode; queda clasificado como QA-failed y no debe usarse como baseline visual.
- Entity holograms se siguen recolectando fuera del BER individual, pero el flush cambia a `RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS`: la geometría sólida, proyectores físicos y bloques translúcidos ya han establecido profundidad antes del holograma.
- El flush diferido usa un `BufferSource` privado de Mirage. **Nunca** terminar el `minecraft.renderBuffers().bufferSource()` global desde esta pasada: el `endBatch()` global de dev.36 podía vaciar/reordenar batches ajenos y es la causa principal identificada para la regresión de Image Mode.
- Ghost Entity conserva `LEQUAL` + no depth-write en sus RenderTypes Mirage. Al ejecutarse después del agua, la profundidad ya escrita por el mundo decide correctamente qué está delante sin reintroducir los agujeros de agua de dev.25/dev.26.
- Image/Banner/Item no cambian de semántica ni layout en esta oleada. La reparación de imágenes consiste en retirar el side effect global de batching introducido en dev.36, preservando el comportamiento previamente correcto.
- Debug Handbook mantiene `BookViewScreen`, pero su superficie vanilla de 192×192 se escala dinámicamente, se centra de verdad en pantalla y añade tabs laterales nativos de Mirage.
- Tabs handbook definitivos para esta etapa: **General / Compact / Display / Wide / Tall / Field / Prism**. Cada chassis debe indicar explícitamente para qué sirve y sus límites; General concentra power/Core, source modes, snapshots, clearance y controles globales.
- Protocolo permanece **15**. No hay cambio NBT/payload.
- Build Windows e in-game QA de dev.37 pendientes.

### QA prioritario dev.37

1. Entidad gigante atravesando visualmente varios Mirage Projectors: los proyectores detrás deben quedar detrás; los físicamente delante deben ocluirla.
2. Ghost Entity con agua claramente detrás y luego claramente delante; el resultado debe obedecer distancia/profundidad, no tipo de render.
3. Dos Entity projections Ghost superpuestas desde varios ángulos.
4. **Histórico dev.37, superseded en dev.38:** en esa build se revalidaba todavía el contrato accidental dev.33 (incluido Field 3×3). No recuperar esa expectativa como diseño actual; dev.38 define Field continuo y Wide/Tall SINGLE/MULTI opcional.
5. Debug Handbook: centrado real, tamaño cómodo, page arrows clicables y tabs General/Compact/Display/Wide/Tall/Field/Prism en GUI scales usados normalmente.
6. Confirmar que abrir el handbook no vuelve a introducir blur ni render de la mano/libro.

### Invariante de recuperación

Si dev.37 necesita otra corrección de render, conservar este source como snapshot recuperable antes de tocar nuevamente la pasada global. No sacrificar Image Mode para arreglar Entity Mode: ambos pipelines deben permanecer aislados.

## Estado `0.1.0-dev.36`

- Baseline de código: dev.35 confirmado in-game/build-clean por QA del usuario.
- Esta pasada corrige la persistencia/visibilidad del nametag proyectado y el orden de render entre proyectores físicos y Entity holograms.
- Entity projections se difieren a `RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES`; los cores físicos se dibujan primero y los hologramas se ordenan far-to-near antes de los bloques translúcidos.
- El nombre congelado se muestra en la base del proyector, independiente de Scale/Lift, con fallback robusto `DisplayName != nombre vanilla`.
- Protocolo: 15.
- dev.36 llegó a compilar/ejecutarse in-game durante el QA del usuario, pero **falló QA**: Entity projections quedaron detrás de otros proyectores/agua y se observó una regresión adicional en Image Mode. dev.35 permanece como baseline build-clean.

### QA prioritario dev.36

1. Nombrar un gato/caballo nuevo, reescanear y comprobar que el nombre aparece en la base del proyector.
2. Colocar varios Mirage Projectors dentro/detrás del volumen de una entidad gigante y comprobar que no se pintan por encima por orden de BER.
3. Repetir con Ghost > 0% y agua detrás: el agua no debe desaparecer.
4. Probar dos Entity projections translúcidas superpuestas desde distintos ángulos.

## Estado `0.1.0-dev.35`

- Debug Handbook now subclasses vanilla `BookViewScreen`; generic Screen blur/background handling is removed from Mirage's handbook implementation.
- Handbook sections are split into vanilla-width physical pages, preserving translations without clipping.
- Projection labels resolve canonical CustomName metadata and render above species/pose-aware Entity bounds.
- Reconstructed named mobs regain their frozen CustomName internally while Mirage suppresses the vanilla duplicate nameplate.
- Protocol remains **15**.

### Waiting list after dev.35

- Special-renderer visual adapters/bounds beyond EntityType dimensions.
- Special/modded RenderType Ghost/Tint hardening and glint QA.
- Multi-source Banner layouts if the Image layout contract proves good in QA.
- Final chassis models/crafts after geometry/Power stabilization.
- Rich cloth-model Banner preview inside its workspace.

---

## Estado `0.1.0-dev.34`

- Entity Scan v5 freezes mob CustomName explicitly and keeps conservative compatibility recovery for older cards.
- Entity Workspace preview exposes `name · entity type` whenever the frozen scan carries a projection nameplate.
- Debug Handbook keeps the dev.32 local dark overlay and now also suppresses its own first-person hand/item render while the screen is open.
- **Histórico dev.34:** Wide/Tall/Field retenían en ese momento los bancos dev.33 (4×1 / 1×4 / 3×3). **Superseded en dev.38:** Field es un Plane continuo y sólo Wide/Tall conservan MULTI opcional.
- Protocol remains **15**; the scan card internal data version changes without altering packet shape.

### Waiting list after dev.34

- Special-renderer visual adapters/bounds beyond EntityType dimensions.
- Special/modded RenderType Ghost/Tint hardening and glint QA.
- Multi-source Banner layouts if the Image layout contract proves good in QA.
- Final chassis models/crafts after geometry/Power stabilization.
- Rich cloth-model Banner preview inside its workspace.

---

## Estado `0.1.0-dev.32`

- Debug Handbook background is now a local dim overlay instead of the generic blur path.
- Protocol remains **14**.
- dev.31 entity fidelity remains unchanged underneath this UI-only hotfix.

### Waiting list after dev.32

- Wide/Tall/Field multi-source layout banks.
- Special-renderer visual adapters/bounds beyond EntityType dimensions.
- Special/modded RenderType Ghost/Tint hardening and glint QA.
- Final chassis models/crafts after geometry/Power stabilization.

---

## Estado `0.1.0-dev.31`

- Entity reconstruction fidelity pass on top of dev.30.
- Player scan format v4 freezes `slim/wide`, model-part visibility mask and dominant arm in addition to the packed textures property.
- Reconstructed Mirage RemotePlayer reapplies those visual flags after entity NBT load.
- Generic Cat/Wolf/Parrot pose now invalidates the cached render entity correctly.
- Protocol remains **14**; Windows build/in-game QA pending.
- Existing Player cards remain compatible but must be rescanned to gain v4 fidelity metadata.

### Waiting list after dev.31

- Wide/Tall/Field multi-source layout banks.
- Special-renderer visual adapters/bounds beyond EntityType dimensions.
- Special/modded RenderType Ghost/Tint hardening and glint QA.
- Final chassis models/crafts after geometry/Power stabilization.
- Rich cloth-model Banner preview inside its workspace.

---

## Estado `0.1.0-dev.30`

- Adds species-aware Entity clearance/render bounds from scanned EntityType width/height.
- Scale is interpreted exactly like the world renderer: target largest dimension.
- Humanoid pose expansion remains applied; Horse Rearing reserves extra vertical room.
- Protocol remains **14**. Windows build/in-game QA pending.
- Detailed contract: `docs/ENTITY-BOUNDS-dev30.md`.

### Waiting list after dev.30

- Wide/Tall/Field multi-source layout banks.
- Special/modded RenderType Ghost/Tint hardening and glint QA.
- Special-renderer visual bounds beyond EntityType dimensions.
- Final chassis models/crafts after geometry/Power stabilization.
- Rich cloth-model Banner preview inside its workspace.

## Estado `0.1.0-dev.30`

- Power UI now separates Projection cost, Core capacity, Remaining power, exact Core limits and exact chassis limits.
- Clearance reports blocked-block count and envelope size in the main GUI.
- Generic Cat/Wolf/Parrot scans gain persistent Idle/Sitting pose selection.
- Return gear is contextual to Humanoid/Horse equipment-capable workspaces.
- Duplicate Lift PU billing and duplicate Main Hand UI row fixed.
- Protocol **14**. Windows build/in-game QA pending.
- Detailed contract: `docs/POWER-POSE-UX-dev29.md`.

### Waiting list after dev.29

- Exact Horse/Generic/special-renderer bounds.
- Wide/Tall/Field multi-source layout banks.
- Special/modded RenderType Ghost/Tint hardening and glint QA.
- Final chassis models/crafts after geometry/Power stabilization.
- Rich cloth-model Banner preview inside its workspace.

## Estado `0.1.0-dev.27`

- dev.26 live QA confirmed Ghost on entity body + Humanoid armor.
- Remaining water-hole bug was isolated to Main/Off Hand ItemRenderer paths.
- Added `ItemInHandRendererMixin` plus held-item RenderType normalization.
- Raw solid/cutout/translucent block layers used while rendering a held ItemStack now enter Mirage's colour-only `ghostItem` pass.
- Tint/alpha remain applied once by the existing projection buffer.
- Network protocol remains `12`.
- Windows build + in-game water QA pending.

## Estado `0.1.0-dev.26`

### Ghost depth + armor repair

- Added Mirage-owned translucent RenderTypes for entity/item projection passes. They keep `LEQUAL` depth testing so world blocks still occlude the projection, but use `COLOR_WRITE` instead of `COLOR_DEPTH_WRITE`, preventing a translucent projection from punching holes in water and other already-rendered translucent geometry.
- Base LivingEntity bodies now use the Mirage ghost RenderType rather than vanilla `entityTranslucent`.
- `HumanoidArmorLayer` is explicitly redirected during Mirage render context, so vanilla humanoid armor receives the same alpha path as the body instead of remaining opaque.
- Held block/item atlas passes, shields, banners and armor trim atlas passes are remapped to Mirage no-depth-write ghost RenderTypes.
- Image/Prism faces also switch to the colour-only ghost RenderType whenever Ghost is below 100%, so 2D holograms obey the same water/depth contract.
- Compatible already-translucent entity/item layers are also normalized to the Mirage pass; effect-only RenderTypes (glint, eyes, beams, shadows, text, masks, outlines) remain native and fail closed.
- Network protocol remains `12`: dev.26 is client-render-only and adds no persisted/network state.

See `docs/GHOST-DEPTH-ARMOR-dev26.md` for the renderer contract and live QA matrix.

---

## Estado `0.1.0-dev.25`

### Hotfix de compilación dev.25

- `EntityProjectionPreviewRenderer` vuelve a usar la firma 1.21.1 correcta de `EntityRenderDispatcher#render`: entidad + XYZ + yaw + partialTick + pose/buffer/light.
- dev.24 tenía un `double` posicional extra y fallaba en `compileJava`; dev.25 no cambia el comportamiento funcional de la oleada dev.24.


dev.25 is the compile-fixed continuation of the dev.24 Entity stabilization wave on top of the user-confirmed build-clean dev.22 baseline and dev.23 repair snapshot. It retains the widened/recentered Item and Entity workspaces, actionable Incoming queue, optional persisted Horse rearing pose, and projection-local LivingEntity Ghost render context. Network protocol remains `12`. Windows build/in-game QA are pending.

See `docs/ENTITY-GHOST-LAYOUT-dev24.md` for the exact layout and render-pipeline contract.

---

## Histórico `0.1.0-dev.23`

dev.23 is the Entity safety/UX repair wave on top of the user-confirmed build-clean dev.22 baseline. Banner work stays paused until this pass survives build/in-game QA.

### dev.23 — Entity item recovery + GUI consolidation + Ghost/Horse fixes

- Physical Entity staging is now transient: successful Apply/Replace returns the real stack immediately; leaving the menu returns all leftovers and drops overflow at the player.
- Incoming is consumed when accepted into Projected, including the already-identical case, so the UI no longer presents two persistent copies of one virtual snapshot.
- Entity Workspace uses one centred panel with explicit Source / Equipment / Actions / Inventory / Preview regions and matching slot coordinates.
- Projection Power shows Used and Available remaining in one compact line beside the Core Capacity; empty sources cost 0 PU.
- Common opaque/cutout entity layers can now be remapped to translucent RenderTypes because the `Optional[resource]` texture parser is fixed.
- Projection-only Horse forces full vanilla stand/rearing animation.
- Protocol **11**.
- Detailed contract + QA: `docs/ENTITY-UX-RECOVERY-dev23.md`.
- Build status: source/static validation complete; Windows NeoForge build pending.


## Estado `0.1.0-dev.22`

Dev.22 cierra la primera pasada funcional de **poses Humanoid** sobre la base acumulativa de dev.21. El projector persiste una pose separada de Entity Scan y de los snapshots de equipment; cambiarla no recaptura ni mueve objetos. El Entity Workspace expone ocho presets: Standing, Guard, Hero, Combat, Raised Main Hand, Raised Off Hand, Dual Wield y Display.

El renderer enlaza únicamente las entidades temporales de Mirage a un hook client-only de `HumanoidModel` y aplica las rotaciones después del frame vanilla. Armor layers y Main/Off Hand heredan así la postura del rig sin convertirlo en Armor Stand ni entidad tickeada. Preview, clearance y render bounds reservan espacio adicional según el preset. Esta pasada sigue siendo conservadora: **no equivale aún a bounds exactos por especie o por mesh** para Horse/Generic. Protocolo de red: `10`. El usuario confirmó posteriormente build SUCCESS en Windows; dev.22 queda como baseline build-clean para dev.23. El QA in-game sigue abierto.

Dev.21 permanece acumulado: **Mirage Prism** físico con cuatro fuentes Image North/East/South/West, renderer lateral, Same Source on All Faces y Power/Clearance propios. Dev.20 permanece acumulado: prioridad temprana de Entity Scan y Capture Equipped Loadout.

## dev.22 — Humanoid pose presets + pose-aware conservative bounds

### Implemented

- Eight persistent Humanoid pose presets stored in `EntityProjectionState`.
- Pose state stays independent from body scan UUIDs and the six virtual equipment snapshot channels.
- Humanoid Entity Workspace pose-cycle control with translated labels/status/tooltips.
- Client-only `HumanoidModel` post-`setupAnim` pose hook bound only to temporary Mirage render entities.
- Armor and hand item layers inherit arm/body rotations from the same rig.
- Main/off-hand raised presets respect the scanned/render entity's main-arm handedness.
- Pose-aware GUI preview auto-fit.
- Conservative pose-aware world clearance and BlockEntity render bounding box.
- Debug Handbook updated and `docs/HUMANOID-POSES-dev22.md` added.
- Network protocol bumped from 9 to **10**.

### QA / build status

- Source/resource/static validation in the assistant environment only.
- **Windows `build.bat` was subsequently confirmed successful by the user; dev.22 is the build-clean baseline for dev.23.**
- Priority live QA: Player, Zombie, Skeleton, bodyless equipment rig, handedness, all presets, armor/trims/dye/glint, shield/tools, Ghost/Tint, reload/multiplayer and clearance visualization.
- Exact species/model-part bounds are intentionally not claimed by this pass.

### Waiting list

- Banner source/editor and banner 3D preview, including Banner on Prism faces.
- Exact species/entity/model-part bounds for Horse/Generic and special renderers.
- Multi-source layouts for Wide/Tall/Field beyond Prism's dedicated four-face bank.
- Special-layer Ghost/Tint QA and fallback hardening.
- Final chassis models/crafts after power/geometry stabilization.
- Optional accessory/backpack/artifact compatibility adapters: explicitly outside base scope.
- Entity Scan catalog/binder: long-term idea only.

## dev.21 — Prism four-face static-image pass

### Implemented

- Registered physical `mirage_prism` block/item and BlockEntity support.
- Four persistent Image assets: North, East, South and West.
- Four independent preview/import/clear controls in Image Workspace.
- `Same Source on All Faces` copies North's asset reference/dimensions to the other faces without duplicating files.
- Plane remains Front/Back and keeps Mirrored/Readable/Independent semantics unchanged.
- Four independent lateral world-render quads; no top or bottom quad.
- Global Scale/Lift/Rotation/Floating/Lighting/Ghost/Tint/Scanlines apply coherently to the Prism assembly.
- Prism-aware Power, clearance envelope and BlockEntity render bounds.
- Network protocol bumped from 8 to **9**.
- Added `docs/PRISM-FOUR-FACE-dev21.md`.

### QA / build status

- Source/JSON/static validation in the assistant environment only.
- **Windows `build.bat` was subsequently confirmed successful by the user; dev.22 is the build-clean baseline for dev.23.**
- Priority live QA: one/two/four populated faces, mixed aspect ratios, Same Source, reload persistence, multiplayer asset transfer, Rotation on/off, Flip/Scanlines/Tint/Ghost and obstruction checks.

### Historical dev.21 waiting list

- Banner source/editor and banner 3D preview, including Banner on Prism faces.
- Humanoid pose presets and pose-cycle control. **Completed in dev.22.**
- Pose-aware conservative Humanoid clearance. **Completed in dev.22; exact species/model-part bounds remain pending.**
- Multi-source layouts for Wide/Tall/Field beyond Prism's dedicated four-face bank.
- Special-layer Ghost/Tint QA and fallback hardening.
- Optional accessory/backpack/artifact compatibility adapters: explicitly outside base scope.
- Entity Scan catalog/binder: long-term idea only.

# dev.19 — GUI separation / Core UX / temporary handbook

## Implemented

- Primary GUI rewritten around sections instead of one dense control grid.
- Primary GUI owns only global presentation, Core/Power, clearance and inventory.
- New dedicated `ImageProjectorMenu` + `ImageProjectorScreen`.
- New dedicated `ItemProjectorMenu` + `ItemProjectorScreen`.
- Entity/Humanoid remains one card-adaptive workspace by design.
- Source activation moved into source-specific actions; the primary source label is read-only.
- New `UpdateImageWorkspacePayload` merges image-only fields instead of overwriting global presentation from a stale workspace copy.
- New source-workspace open payloads and `SetProjectionSourcePayload`.
- Network protocol bumped from 6 to **7** because dev.19 adds mandatory workspace payload registrations.
- Core slot moved into a dedicated visible section with empty-slot accepted-Core tooltip and live capability/power summary.
- Provisional Core curve raised to 16 / 32 / 96 / 192 / 384 PU.
- Temporary `Mirage Debug Handbook` added. It opens a custom non-pausing screen whose text uses translatable components so the active client language controls the manual.
- Handbook included in Mirage creative tab; no survival recipe is frozen while it remains a debug/development aid.
- Image Workspace uses large face cards. Plane = Front/Back. Prism layout = North/East/South/West cards, with East/West visibly reserved until the Prism source-bank persistence pass.

## Architecture decisions frozen

- Never put file import, entity equipment staging and global presentation back into one screen.
- Previews live beside the source editor that understands them.
- Entity/Humanoid stays one dynamic state model even if later skins/layouts visually diverge.
- Core and chassis are separate constraints. Core power buffs do not bypass chassis geometry.
- Empty Core tooltip must remain ordered weakest→strongest and explain capabilities.

## QA / build status

- dev.18 Windows build output was authoritative and used to repair the 1.21.1 equipment API assumptions.
- dev.19 has only source/static validation in the assistant environment at packaging time.
- **Windows `build.bat` was subsequently confirmed successful by the user; dev.22 is the build-clean baseline for dev.23.**

Priority live QA:

1. Primary GUI at GUI scale 2/3/4 and smaller window sizes.
2. Core slot insertion/removal + tooltip + live power bar.
3. Image Workspace Front/Back import, Mirror/Readable/Independent, Flip and Scanlines.
4. Return from Image Workspace preserves presentation settings.
5. Item Workspace capture does not consume the item and activates ITEM.
6. Entity Workspace still keeps Humanoid slots when the card is removed.
7. Source transitions do not erase another source's stored snapshot/assets.
8. Debug Handbook opens, does not pause, and changes language with Minecraft locale after reopening.

## Waiting list at dev.19 (historical)

- Prism East/West persistent source bank + four active import controls. **Completed in dev.21.**
- Banner source/editor and banner 3D preview.
- Humanoid pose presets and pose-cycle control. **Completed in dev.22.**
- Conservative Humanoid pose-aware clearance. **Completed in dev.22; exact species/entity bounds remain pending.**
- Special-layer Ghost/Tint QA and fallback hardening.
- Optional accessory/backpack/artifact compatibility adapters: explicitly outside base scope.
- Entity Scan catalog/binder: long-term idea only.

# Development notes — 0.1.0-dev.19

## dev.18 — first Windows compile repair pass

The dev.17 Windows build is now the authoritative compile baseline for the Entity/Humanoid stack. It reached `:compileJava` and failed with 13 errors. dev.18 addresses all three root causes in one wave:

1. missing `Player` import in `MirageProjectorBlockEntity`;
2. incorrect static use of the 1.21.1 instance method `LivingEntity#getEquipmentSlotForItem`;
3. incorrect model of saddle as `EquipmentSlot.SADDLE`, which does not exist in 1.21.1.

### Equipment resolution invariant

`EquipmentSnapshotRules` is now the single static resolver used by staging and standalone armor detection. Resolution order intentionally mirrors NeoForge's 1.21.1 behavior: stack-provided equipment slot first, then vanilla `Equipable`, otherwise Main Hand. Humanoid hand rows continue to accept arbitrary items by design.

### Horse invariant

Mirage has two **virtual horse channels**, `SADDLE` and `BODY`, but only Body maps to a vanilla `EquipmentSlot`. Saddle is stored/captured independently from vanilla `SaddleItem` data and accepted only from Saddle items. Do not reintroduce `EquipmentSlot.SADDLE` in future code.

The client reconstruction writes the saddle snapshot through `AbstractHorse` slot access. Because vanilla `HorseModel` gates saddle geometry through `isSaddled()` and a client-only reconstructed horse does not receive the normal server-synced saddle flag, dev.18 also uses a projection-only `MirageProjectionHorse` subclass whose `isSaddled()` reads the virtual saddle inventory slot directly. This keeps the renderer correct without inventing an `EquipmentSlot.SADDLE`. Runtime QA must still verify the visual result.

### Build status

- dev.17: Windows build **FAILED** at `:compileJava` with 13 errors.
- dev.18: source repaired and statically checked; **Windows NeoForge rebuild pending**.
- The six deprecation warnings for `EventBusSubscriber.Bus` are warnings only and are not part of this compile blocker. They remain a cleanup item after build correctness.


## Target fijo

- Minecraft 1.21.1
- NeoForge **21.1.244**
- Java 21
- ModDevGradle 2.0.146
- Gradle 9.2.1
- Parchment 2024.11.17

**No mover NeoForge desde 21.1.244** por seguir automáticamente el MDK o una revisión más nueva.

## Windows / snapshot invariant

La única ruta de build soportada por el proyecto sigue siendo:

```text
build.bat
```

`build.bat` busca Java 21, instala Gradle 9.2.1 localmente bajo `.gradle-dist/` si falta y ejecuta `clean build --stacktrace`.

Snapshots de desarrollo:

- siempre entregar snapshot recuperable aunque QA quede pendiente;
- excluir `.gradle-dist`, `.gradle`, `build`, `run` y caches/pesos generados;
- conservar `src`, `docs`, raíz, build files y scripts necesarios.

## Política de URLs en descripciones — permanente

No volver a hardcodear URLs de GitHub/repository dentro de descripciones de mods, incluyendo `neoforge.mods.toml`, textos de CurseForge/Modrinth y copy equivalente.

Motivo: el repository puede cambiar de cuenta/nombre y deja una descripción vieja o engañosa. Los links externos, si la plataforma los soporta, se mantienen en campos separados. Los scripts técnicos pueden seguir conteniendo URLs necesarias (ej. descarga oficial de Gradle).

Easy Farmer's Delight queda anotado como metadata a limpiar en su próxima pasada porque su descripción pública mantiene un GitHub desactualizado. No mezclar esa corrección con el source de Mirage.

## Baseline de trabajo

- dev.4: build/run limpio en NeoForge 21.1.244.
- dev.5: selector nativo + primera imagen real proyectada.
- dev.6: Plane grande/transparente con culling/Front-Back corregido.
- dev.7: transporte multiplayer + Item Mode + clearance.
- dev.8: Core/Power, Core visual dinámico y debug chassis; el usuario lo subió al repositorio canónico y lo usa como baseline de esta oleada.
- dev.9: Image completeness/presentation/chassis contracts; build/QA Windows no quedó confirmado explícitamente en el chat.
- dev.10: Ghost Effect + Display/Wide/Tall/Field físicos; build/QA Windows no confirmado todavía.
- dev.11: render-family contract + miniaturas Front/Back; build/QA Windows no confirmado todavía.
- dev.12: Virtual Item Snapshot foundation + UUID por captura + contrato de ownership 3D; build/QA Windows pendiente.
- dev.13: Entity Scan Card foundation + contrato final Humanoid Entity/Entity GUI.
- dev.14: Empty Scan Template + primera implementación real del Entity Workspace (card staging, incoming/projected por canal, conflicto, Horse, retorno de staging); build/QA Windows pendiente.
- dev.15: preview 3D real client-only con auto-fit/clipping, composición desde Projected/Active y teardown coherente del body al retirar la card; build/QA Windows pendiente.
- dev.16: primer renderer Entity/Humanoid in-world, nameplate bajo la proyección, maniquí humanoide bodyless, snapshot de skin de Player, pieza de armadura standalone como geometría equipada y primer adaptador visual seguro para Ender Dragon; build/QA Windows pendiente.
- dev.17: buffer local de presentación 3D para Tint/Ghost en Item/Entity/Humanoid, remapeo translúcido de capas opacas/cutout comunes y preview Item verdaderamente 3D FIXED; build Windows ejecutado y falló en `:compileJava` por API de equipment.
- dev.18: corrige los 13 errores reportados por ese build; rebuild/QA Windows pendiente.
- dev.19: reorganización mayor de GUI, workspaces Image/Item dedicados, Core UX/potencia y Debug Handbook traducible; build/QA Windows pendiente.
- dev.20: prioridad temprana de Entity Scan + Capture Equipped Loadout; build/QA Windows pendiente.
- dev.21: Mirage Prism funcional con cuatro caras Image persistentes y geometría/Power/Clearance dedicados; build/QA Windows pendiente.
- dev.22: presets de pose Humanoid persistentes + hook de rig + preview/clearance/render bounds conservadores por pose; **source actual**, build/QA Windows pendiente.



## dev.16 — Entity/Humanoid world renderer + equipped geometry

### 1. Estado implementado

`ProjectionSettings.SourceMode` incluye ya `ENTITY` y el protocol registrar es `6`. El renderer del BlockEntity consume el mismo `EntityProjectionState` que la GUI: no spawnea una entidad real, no conserva AI y no depende de que el sujeto original siga cargado.

- body escaneado -> reconstrucción client-only desde `EntityType` + `EntityData` sanitizado;
- Player -> `RemotePlayer` de render con GameProfile congelado;
- Humanoid equipment -> sólo `Projected / Active`; Incoming nunca se muestra como aceptado;
- Horse -> sólo los overrides Saddle/BODY del contexto Horse;
- Scale/Rotation/Lift/Floating transforman el **modelo 3D completo**;
- nameplate selectivo se renderiza en la separación vertical entre base física y punto inferior del holograma.

### 2. Maniquí humanoide sin body

Si no existe Entity body activo pero sí alguno de `Head/Chest/Legs/Feet/Main/Off`, GUI y world renderer crean un `RemotePlayer` puramente client-side e invisible. Vanilla 1.21.1 omite el base model invisible pero sigue ejecutando las equipment render layers, por lo que armor/manos pueden existir como holograma sin Player ni Armor Stand visible.

No convertir este rig en entidad del mundo ni hacerlo lootable/interactuable. Es infraestructura de render exclusivamente.

### 3. Player skin congelada

Desde Entity Scan data version 3, los scans de Player conservan el `GameProfile` visual mínimo necesario: Name + packed `textures` property (value/signature cuando existen). El `MirageRemotePlayer` resuelve su `PlayerSkin` mediante `SkinManager` desde esa propiedad congelada, en vez de depender únicamente del PlayerInfo actual.

Esto debe probarse con Player online -> escaneo -> logout/desconexión -> reapertura. Si un servidor/mod de skins no expone una textures property estándar, su compatibilidad será un adapter posterior y no una excusa para volver a un lookup vivo obligatorio.

### 4. Item Mode: armor standalone equipada

`ItemProjectionPreviewRenderer` y `MirageProjectorRenderer` detectan snapshots cuyo `LivingEntity.getEquipmentSlotForItem` devuelve Head/Chest/Legs/Feet. Esos snapshots ya no usan el icon/modelo de inventario como representación principal: se equipan sobre el mismo rig bodyless y se renderizan mediante las layers vanilla.

Items no-equipment, herramientas y bloques continúan por el ItemRenderer 3D normal. El tamaño/alineación de casco/peto/pantalones/botas sobre el rig todavía requiere QA visual in-game.

### 5. Reloj visual seguro / Ender Dragon

Las entidades de preview/proyección **no ejecutan `tick()` ni `aiStep()`**. Hacerlo podría reproducir sonidos, partículas, AI, movimiento o consultas de mundo. `prepareVisualFrame` sólo actualiza clocks de render seguros.

Ender Dragon necesita además historial de latencia para su renderer; dev.16 inicializa de forma side-effect-free su ring de posiciones y `flapTime`, sin fase/AI/cristal ni lógica de combate. Esto da una primera animación visual base segura, pero no equivale aún a un sistema general de animation presets por especie.

### 6. P0 de QA / siguientes cambios

1. `build.bat` real en Windows contra NeoForge 21.1.244;
2. bodyless armor + ambas manos: trims, dye, glint, escudos/bloques/tools, armor modded;
3. Player frozen skin después de que el Player original deje la sesión;
4. Item Mode standalone armor: escala/posición por Head/Chest/Legs/Feet;
5. Entity world: Chicken/Baby Zombie/Horse/Player/Ender Dragon, Scale/Lift/Rotation/Floating y nameplate;
6. Ghost Effect/Tint 3D: validar la ruta dev.17 y refinar sólo glint/custom RenderTypes que muestren residuos;
7. pose presets Humanoid;
8. clearance basado en bounds/pose reales;
9. adapters especiales de animación sólo donde el renderer vanilla lo requiera.

### 7. Riesgos compile-sensitive

Hasta Windows QA no declarar build-clean. Revisar especialmente `Property(name,value,signature)`, `GameProfile#getProperties`, `SkinManager#lookupInsecure`, override de `RemotePlayer#getSkin`, `EntityType#create(ClientLevel)`, `InventoryScreen.renderEntityInInventoryFollowsMouse`, acceso visual de EnderDragon y signatures del `EntityRenderDispatcher`.


## dev.15 — Entity preview real + regla de snapshot prioritaria

### 1. Invariante de recuperación

La snapshot no es un release ni depende de compilar. **Antes de seguir acumulando cambios arriesgados debe existir un ZIP recuperable del estado actual.** Si una oleada queda a medio implementar, se entrega ese WIP/safety snapshot de todas formas y se indica qué QA falta.

Para esta transición se conserva además `Mirage-Projector-0.1.0-dev.14-SAFETY-SNAPSHOT-20260908-1218.zip` como punto de rescate previo a dev.15.

### 2. Preview client-only

`EntityProjectionPreviewRenderer` reconstruye una `LivingEntity` sólo para render de GUI. No se agrega al `ClientLevel`, no tiene AI/world ownership y se invalida al cerrar la pantalla, cambiar de nivel, cambiar `ScanId` o cambiar los UUID de snapshots Projected/Active.

- Mob normal: resolver `EntityType` -> `type.create(clientLevel)` -> cargar `EntityData` sanitizado.
- Player: crear `RemotePlayer(clientLevel, GameProfile(sourceUuid, name))`. En 1.21.1 vanilla, `AbstractClientPlayer#getSkin()` consulta `PlayerInfo` por UUID; por ello el player online puede resolver su skin actual, pero **skin exacta congelada/offline aún no está cerrada** y no se debe declarar completa.
- Equipamiento: aplicar sólo `humanoidProjected` o `horseProjected`. Incoming no se dibuja como si ya hubiera sido aceptado.

### 3. Auto-fit y viewport

El tamaño sale de `getBbWidth()/getBbHeight()` y del área disponible. Se usa el renderer vanilla de inventario, que ya hace scissor y restauración de rotaciones del entity. El panel exterior prefiere 150 px de ancho y baja hasta un ancho compacto cuando la pantalla no permite el layout completo.

### 4. Retiro de Scan Card

La card central es la fuente del **body**. Al retirarla, el body importado se limpia. Las seis capas Humanoid Incoming/Projected no se limpian: quedan disponibles como maniquí virtual sin cuerpo. Horse es contextual: al retirar su card se limpian body + Saddle/Body Armor virtuales, después de obligar a devolver cualquier staging físico real.

### 5. QA prioritaria dev.15

1. `build.bat`;
2. escanear Chicken, Baby Zombie, Player, Horse y una entidad grande;
3. comprobar que todos quedan dentro del panel de preview y que el cursor no saca el modelo del viewport;
4. Baby Zombie con armor escaneada: antes de aceptar los ✓ la preview no debe fingir que esa armor ya está Projected;
5. aplicar Head/Chest/Legs/Feet/Main/Off individualmente y comprobar update de preview;
6. right-click en slot derecho y comprobar eliminación inmediata del layer;
7. retirar Zombie/Player card: body desaparece, seis canales Humanoid permanecen;
8. retirar Horse card: se limpia body y estado Horse; staging físico debe haberse devuelto primero;
9. Player online: revisar skin; Player desconectado/offline queda como caso pendiente de snapshot de skin exacta;
10. confirmar que Image/Item workspace no sufrió regresión.

### 6. Siguiente línea directa

- activar SourceMode Entity/Humanoid en el renderer in-world;
- compartir la reconstrucción/composición con GUI sin spawnear entidades reales;
- nameplate in-world entre base y punto inferior del holograma;
- renderer de armor equipada/pose, incluida armadura standalone de Item Mode;
- Ghost Effect alpha-safe para entidad + equipment + glint;
- clearance basado en bounds/pose;
- luego Banner/multi-source/Prism según roadmap.


## dev.12 — Virtual Item Snapshots + Armor/Effigy ownership contract

### 1. El projector deja de almacenar el item proyectado

La ranura de Item Mode pasa a ser un **Virtual Snapshot Slot**. El usuario puede hacer click o shift-click con un ItemStack y el servidor copia exactamente una unidad como dato de render, sin mover ni reducir el stack original.

Estado persistente nuevo en BlockEntity:

```text
ProjectionSnapshot              ItemStack copy (count=1)
ProjectionSnapshotId            UUID generado al capturar
```

El snapshot es deliberadamente no-obtenible. No se usa como inventario, no se dropea y no puede convertirse en duplicado físico.

### 2. Semántica de click del slot virtual

- cursor con item + click -> captura copia; cursor no cambia;
- cursor vacío + click -> limpia snapshot;
- shift-click de item normal desde inventario -> captura sin consumir;
- number key/offhand swap sobre slot -> captura ese stack;
- clone/drag/pickup-all -> ignorados para el snapshot;
- `mayPickup=false`, `isFake=true`;
- `canDragTo` rechaza el slot virtual.

Core Slot permanece completamente físico.

### 3. Identidad

No asumir que un ItemStack arbitrario posee UUID propio. Mirage genera un **snapshot UUID** nuevo por captura. El renderer usa la copia serializada; no busca el objeto original en inventarios.

### 4. Migración dev.11

Dev.11 guardaba un ItemStack físico. Al cargar NBT viejo:

1. se crea snapshot visual;
2. el viejo item real se conserva como `LegacyProjectionReturnItem`;
3. al romper el projector ese item legacy se devuelve exactamente una vez;
4. capturas dev.12 nuevas no crean legacy return items.

Esto evita perder equipamiento en mundos de testing sin mantener el diseño viejo para nuevas capturas.

### 5. Armor/Effigy

Queda congelado que Armor Mode tendrá seis canales virtuales:

```text
HEAD / CHEST / LEGS / FEET / MAIN_HAND / OFF_HAND
```

Cada canal tendrá snapshot UUID + ItemStack copy. El usuario nunca deja su armor/tools dentro del projector.

La GUI Effigy debe incluir `Capture Equipped Loadout`: leer Head/Chest/Legs/Feet/Main Hand/Off Hand actuales del jugador y generar seis snapshots en una sola operación, sin desequipar, mover ni reducir los ItemStacks reales.

No activar todavía los seis slots porque faltan:

1. renderer de pieza equipada (trims/dye/modded hooks);
2. rig humanoide invisible;
3. transforms correctos de manos;
4. pose presets;
5. Ghost Effect alpha-safe para 3D/glint/layers modded.

Contrato completo: `docs/ITEM-ARMOR-SNAPSHOT-CONTRACT.md`.

### 6. QA prioritaria dev.12

1. `build.bat`;
2. poner espada/bloque/tool en Snapshot Slot y comprobar que el item original no se mueve ni reduce;
3. shift-click de un item normal debe capturarlo sin moverlo;
4. cambiar/limpiar snapshot no debe entregar ningún item;
5. romper projector nuevo: sólo Core real debe dropear, nunca el snapshot;
6. save/reload mantiene stack visual + snapshot UUID;
7. abrir el mismo projector desde otro cliente no permite robar la fuente, sólo ver/reemplazar el snapshot;
8. comprobar render 3D/glint del snapshot;
9. migrar un projector dev.11 con item real y confirmar que el render continúa y el item legacy vuelve al romper el block;
10. confirmar que Image Mode/Front/Back thumbnails no sufrieron regresión.

## dev.10 — Ghost Effect + physical chassis family

### 1. Ghost Effect / Transparency

La GUI ya no presenta `Opacity`, porque para el usuario ese control era conceptualmente al revés de lo que quiere ajustar. Desde dev.10 se expone:

```text
Ghost effect: 0% .. 90%
```

Semántica:

- 0% = sin transparencia adicional;
- 50% = holograma notablemente translúcido;
- 90% = extremadamente fantasma, todavía visible;
- 100% no se permite para evitar una proyección invisible.

Compatibilidad:

- `ProjectionSettings` conserva `OpacityPercent` en wire/NBT;
- `transparencyPercent()` devuelve `100 - opacityPercent`;
- al aplicar la GUI se vuelve a guardar `100 - transparency`;
- por ello dev.9 no necesita migración destructiva ni protocol bump.

El preview 2D aplica Tint y alpha actual usando `RenderSystem.setShaderColor`, hace flush antes/después y restaura `(1,1,1,1)` inmediatamente. Item Mode continúa opaco por ahora.

### 2. Chassis registrados

Bloques reales en dev.10:

| Registry ID | Perfil | Envelope W×H | Physical top | Sources futuras |
|---|---|---:|---:|---:|
| `mirage_projector` | Compact | 10×10 px | 5 px | 1 |
| `mirage_display` | Display | 16×16 px | 6 px | 1 |
| `wide_mirage_projector` | Wide | 80×32 px | 6 px | 4 |
| `tall_mirage_projector` | Tall | 32×80 px | 8 px | 4 |
| `mirage_field_projector` | Field | 80×80 px | 7 px | 9 |

Todos usan:

```text
MirageProjectorBlock
MirageProjectorBlockEntity
MirageProjectorMenu
MirageProjectorRenderer
ProjectionSettings
ProjectionPower
ServerAssetStore / transport
```

No crear subclasses por tier/chassis salvo que una geometría futura (Prism/Effigy) realmente requiera un contrato de render distinto.

### 3. Envelope aspect-aware

`ProjectionPower.dimensions()` calcula el ancho/alto real de Image Mode preservando aspect ratio. El chassis valida ese par contra `maxWidthPixels/maxHeightPixels`.

Ejemplo Wide:

```text
max = 80×32 px
source = 16:9
Scale = 80 px
actual = 80×45 px
=> inválido por altura
```

El usuario puede bajar Scale hasta entrar en 80×32. Una pasada posterior puede hacer que el slider muestre dinámicamente el máximo válido para el ratio actual sin destruir valores guardados.

### 4. Modelos dev.10

Display/Wide/Tall/Field tienen modelos JSON y VoxelShapes distintos para probar identidad y physical top, pero son **prototipos funcionales**, no arte final.

- material común: obsidian + glass + Core material dinámico;
- no existe Core material hardcodeado en los JSON;
- el BER posiciona/escala el Core según `ProjectionChassisProfile`;
- no congelar recipes hasta aprobar modelos.

### 5. Core defaults

- Compact nuevo/migrado conserva Glass Core para compatibilidad histórica.
- Display/Wide/Tall/Field nacen con socket vacío.
- todos devuelven el Core físico al romperse; desde dev.12 el Item source es snapshot virtual y no dropea.

### 6. QA prioritaria dev.10

1. build con `build.bat`;
2. creative tab contiene los cinco blocks;
3. cada block abre la misma GUI pero muestra su nombre/perfil correcto;
4. Core dinámico queda centrado y con altura correcta en cada modelo;
5. Wide 5×2 y Tall 2×5 rechazan ratios que exceden el eje corto;
6. Debug chassis permite stress-test sin saltarse Core Power;
7. Ghost 0/25/50/75/90% se ve progresivo y el resto de la GUI no queda teñido/transparente;
8. clearance comienza sobre la altura correcta de cada emitter;
9. romper cada chassis devuelve el Core exactamente una vez y ningún snapshot virtual;
10. save/reload conserva source/settings/Core.

## dev.9 — Image completeness + preview + presentation + chassis contracts

Esta oleada deliberadamente termina casi todo el bloque pendiente del **Plane/Image foundation** antes de empezar a registrar nuevos chassis físicos.

### 1. WebP real, sin dependencia manual del usuario

Se agrega `org.sejda.imageio:webp-imageio:0.1.6` como dependencia **Jar-in-Jar**.

Pipeline:

```text
PNG / JPG / JPEG / WebP local
  -> ImageIO decode
  -> ARGB
  -> resize si excede 2048 px
  -> PNG normalizado <= 4 MiB
  -> SHA-256
  -> cache local
  -> upload/download multiplayer del PNG normalizado
```

Reglas:

- el formato original nunca viaja al server;
- la ruta local nunca viaja al server;
- WebP no cambia el protocolo de almacenamiento: el world asset store sigue conteniendo PNG normalizado;
- el decoder viaja dentro del JAR del mod;
- `ImageIO.scanForPlugins()` fuerza el descubrimiento del provider antes del primer decode.

### 2. Presentation settings para Image Mode

Nuevos settings persistentes y sincronizados:

```text
Fullbright
OpacityPercent
TintRgb
Scanlines
```

Defaults:

```text
Fullbright ON
Opacity 100%
Tint #FFFFFF
Scanlines OFF
```

GUI:

- `Lighting: Fullbright / World Light`;
- Opacity 10-100%;
- Tint presets: White/Cyan/Amethyst/Rose/Amber/Green/Red;
- `Scanlines ON/OFF`.

El formato interno guarda RGB directo, no el ordinal del preset, para poder agregar selector libre de color sin migrar NBT/networking otra vez.

### 3. Scanlines sin shader obligatorio

El renderer no altera el PNG ni crea una textura derivada. Divide la cara en tiras horizontales visibles separadas por pequeños huecos transparentes:

- entre 8 y 64 segmentos según altura proyectada;
- máximo 64 segmentos por cara;
- sólo Image Mode;
- cuesta +1 Projection Power;
- OFF por default.

Esto evita convertir un simple efecto visual en dependencia de shader.

### 4. Preview dentro de GUI

La GUI ahora tiene un panel de preview:

- Image Mode muestra la DynamicTexture actual conservando aspect ratio;
- conserva el aspect ratio y muestra el source real;
- simula scanlines en el preview 2D y muestra un swatch del Tint actual;
- el preview de dev.9 no recolorea todavía sus píxeles ni aplica alpha: Tint/Opacity se validan en el renderer del mundo;
- Item Mode muestra el ItemStack del slot;

El preview no pretende reemplazar el renderer del mundo; sólo da feedback rápido de source/aspect/presentation.

### 5. Clearance world overlay

`ProjectionClearance.Result` ahora contiene:

- blocked count;
- checked count;
- hasta 128 `BlockPos` obstructores;
- AABB envelope.

Mientras `MirageProjectorScreen` esté abierto:

- se dibuja el envelope conservador del Mirage;
- amber/green-ish cuando está libre;
- red cuando hay interferencias;
- cada bloque capturado que obstruye recibe outline rojo;
- la lista de outlines se limita a 128 para no inundar vértices durante stress-test gigantes.
- una imagen sin rotación usa un AABB alineado con su `rotationOffset`; una imagen que rota conserva el envelope barrido conservador para no perder colisiones futuras.

El preview se limpia al cerrar GUI y al logout.

### 6. Transfer progress + ACK real de upload

Nueva respuesta S2C final `AssetUploadAckPayload`:

```text
assetId
accepted: true / false
message
```

El cliente calcula el porcentaje de **envío** mientras despacha sus chunks, muestra `awaiting server` al terminar de enviarlos y sólo marca `Upload complete` cuando llega el ACK positivo. Un rechazo quita el asset del set de enviados de la sesión para permitir reintento. El progreso de download se deriva de los chunks recibidos.

La GUI incluye además un botón `↻` para reintentar explícitamente la sincronización de Front/Back. Esto corrige una limitación de dev.7: antes el cliente podía enviar todos los chunks pero no tenía confirmación explícita de que el server hubiera almacenado el asset.

### 7. ProjectionChassisProfile

Se separan definitivamente los **hard physical limits** del Compact de `ProjectionPower`.

Contratos provisionales dev.9:

| Chassis | Scale | Lift | Float | Sources | Geometry |
|---|---:|---:|---:|---:|---|
| Compact | 10 | 32 | 4 | 1 | Plane |
| Display | 16 | 48 | 6 | 1 | Plane |
| Wide | 80 | 64 | 8 | 4 | Plane |
| Tall | 80 | 96 | 12 | 4 | Plane |
| Field | 80 | 96 | 16 | 9 | Plane |
| Prism | 48 | 96 | 12 | 4 | Prism |
| Effigy | 96 | 128 | 16 | 8 | Effigy |
| Colossal | 160 | 160 | 32 | 16 | Volumetric |

Sólo **Compact** está registrado como block en dev.9. Los demás son contratos de arquitectura, no contenido jugable todavía.

`Debug chassis` salta sólo este contrato físico en Creative. No salta Core limits, Power budget ni `floatAmplitude <= lift`.

### 8. Networking/settings cleanup

- `ProjectionSettings` centraliza directamente su serialización de red y NBT para menu, payload y BlockEntity;
- protocol registrar pasa de dev.8 protocol `3` a dev.9 protocol `4`;
- settings de presentation viajan en menu/payload y persisten en NBT;
- mundos antiguos usan defaults sanos si los nuevos tags no existen.

## QA solicitado para dev.9

1. Ejecutar `build.bat` y comprobar que WebP Jar-in-Jar resuelve correctamente.
2. Abrir mundo dev.8: imágenes/cores/settings deben sobrevivir.
3. Importar PNG, JPG y WebP.
4. Revisar preview GUI con imagen horizontal, vertical, cuadrada y transparencia.
5. Probar Fullbright vs World Light de día/noche/interior oscuro.
6. Probar opacity, cada tint y Scanlines por ambas caras.
7. Verificar Mirrored / Readable / Independent después de la nueva presentación.
8. Cambiar Scale/Lift/Float con GUI abierta y mirar el envelope actualizarse en mundo.
9. Poner bloques alrededor: deben salir outlines rojos correctos; con Rotation OFF, +90°/+180° debe rotar también el envelope delgado sin marcar bloques que quedan fuera del plano.
10. Probar Item Mode para confirmar que presentation controls no alteran items.
11. Multiplayer: importar en cliente A, confirmar porcentaje `Upload sent`, estado `awaiting server` y ACK final; probar también el botón `↻`. Entrar con cliente B sin cache y confirmar download + aparición.
12. Reiniciar world/server y confirmar que el server asset store sigue resolviendo la imagen.
13. Probar GUI Scale bajo: el panel de 416x518 no debe quedar fuera de pantalla de forma inutilizable.
14. Romper projector con Item + Core: ambos deben dropear una sola vez.

## Próxima prioridad después de dev.10

Con Display/Wide/Tall/Field ya registrados físicamente en dev.10, la siguiente oleada debe validar y pulir esta familia antes de ampliar la geometría del sistema:

1. build/QA real de los cinco chassis sobre NeoForge 21.1.244;
2. ajustar modelos, hitboxes, Core alignment y envelopes según feedback in-game;
3. definir crafts finales sólo cuando los cuerpos físicos y la curva de Power estén aprobados;
4. implementar multi-source layout para Wide/Tall/Field reutilizando el mismo BlockEntity/menu/networking;
5. implementar Banner como source type con tela sin poste;
6. implementar Mirage Prism de cuatro caras;
7. implementar Effigy/equipment rig;
8. implementar Colossal + LOD/culling;
9. histórico dev.10: GIF quedaba al final del pipeline; **implementado finalmente en dev.39**.

Video, YouTube, browser, streaming y audio siguen explícitamente fuera de alcance.

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


## dev.11 — render-family contract + source thumbnails

### Permanent render-family rule

Do not collapse all sources into the same "quad" abstraction.

- **Image/Banner**: face-based 2D geometry. Plane front/back and future Prism N/E/S/W are physical faces. Rotation exposes edges and then the next face.
- **Item**: one volumetric 3D object. `ItemRenderer.renderStatic(..., ItemDisplayContext.FIXED, ...)` remains the generic baseline for blocks/items. A block must render as its 3D model; tools/weapons as their complete item model. Face-count from Plane/Prism must not cause item sprite switching.
- **Armor/Effigy**: one posed 3D equipment rig. Up to Head/Chest/Legs/Feet + Main/Off Hand move as one assembled figure.

### Standalone armor prerequisite

A single armor piece placed in Item Mode should eventually render using its **equipped armor geometry**, not the inventory/hand model. This requires the same reusable humanoid/equipment path as Effigy and must honor NeoForge armor-model hooks for compatible modded equipment.

Dev.11 deliberately does **not** expose Armor Mode yet. The current generic ItemRenderer is not sufficient for fitted armor geometry.

### 3D Ghost Effect prerequisite

Image Ghost Effect already writes alpha per projection vertex. Generic Item/Effigy rendering can contain several RenderTypes (base model, glint, trim/equipment layers, hand items). Do not implement 3D transparency by leaving a global shader color active across shared batches.

Required solution: alpha/tint-aware buffer/vertex-consumer path (or another render-isolated equivalent) that can multiply alpha while preserving glint and modded layers, then restore with no shared render-state leakage.

### Armor / Effigy waitlist

1. equipped-piece renderer for a single armor item;
2. invisible humanoid rig;
3. 4 armor/equipment slots + Main Hand + Off Hand;
4. compatible slot validation while allowing any renderable ItemStack in hands;
5. pose data model and preset cycle button, inspired by armor-stand pose changes but without rendering the stand;
6. hand-item transforms driven by arm pose;
7. Elytra/shield/trident and modded equipment validation;
8. Ghost Effect for every 3D layer;
9. uniform Scale/Lift/Rotation/Floating on the complete rig;
10. conservative pose-aware 3D clearance;
11. Effigy GUI preview.

### dev.11 GUI image thumbnails

Image Mode now reserves two simultaneous preview cards:

- Front;
- effective Back.

Back preview reflects Mirrored / Readable / Independent semantics. Independent with no dedicated Back asset visibly falls back to Front. Hover tooltips expose face role, dimensions and short SHA-256 id.

Future multi-source/Prism UI must generalize this same component to one thumbnail per source/face rather than relying on filenames. Filenames are not network identity and may be meaningless; SHA-256 remains technical identity, thumbnail remains human visual identity.

## dev.13 — Entity Scan / Humanoid Entity implementation boundary

### Implemented in source

- `EntityScanCardItem` registered as a non-stackable Mirage item.
- `EntityScanData` provides the first frozen scan-card data format.
- card scanning captures one LivingEntity without consuming/moving the entity or its equipment;
- independent scan UUID and source UUID metadata;
- Humanoid/Horse/Generic classification foundation;
- six humanoid equipment snapshots extracted separately;
- Horse `SADDLE` + `BODY` snapshots extracted separately;
- common runtime/nonvisual NBT is stripped from the copied entity tag;
- scan NBT has a 256 KiB safety ceiling;
- passenger/vehicle scan rejection prevents v1 jockey/composite capture;
- sneak-use overwrite guard.

### Not yet implemented; next wave

Do not claim Entity Mode is functional in the projector yet. The following remain pending:

1. BlockEntity entity-snapshot storage separate from the physical card;
2. Entity Scan Card staging/import slot in projector menus;
3. dedicated Entity/Humanoid editor screen;
4. effective layout resolver: Humanoid / Horse / Generic;
5. Humanoid incoming staging handlers: Head/Chest/Legs/Feet/Main/Off;
6. Humanoid active virtual snapshot handlers: same six channels;
7. one ✓ Apply action per paired slot and conflict confirmation;
8. safe return-to-player logic for real staging equipment;
9. Horse incoming/active Saddle + BODY channels with dynamic teardown;
10. GUI reconstruction/preview of scanned entities with automatic viewport fit;
11. world Entity renderer;
12. equipped-piece armor renderer shared by standalone Item Mode and Humanoid Entity Mode;
13. pose controls;
14. alpha-safe Ghost Effect across entity/equipment/glint render layers.

### GUI invariants

- Player inventory sits below the entity editor.
- Humanoid mode keeps all six equipment rows visible even if no card is inserted.
- Emptying the humanoid card slot cannot erase active equipment snapshots.
- Scanned humanoid gear enters LEFT incoming rows; it never silently replaces RIGHT active rows.
- Main Hand and Off Hand exist on both left and right.
- Generic entity layouts do not fake editable equipment support.
- Horse-only fields must not leak into non-Horse contexts.
- External accessory/backpack/artifact slots are future compatibility adapters, not base scope.

See `docs/ENTITY-PROJECTION-CONTRACT.md` for the normative UX/data contract.


## dev.14 — Entity workspace / scan implementation boundary

### Implementado en esta oleada

- `Empty Scan Template` no stackeable, visualmente papel.
- Recipe 3x3: 8 Iron Nuggets alrededor de 1 Paper.
- Scan sólo con Shift + clic derecho sobre `LivingEntity`/Player.
- Snapshot independiente del origen: `ScanId` Mirage + `SourceUuid` sólo como procedencia.
- Snapshot congelado de body/variant data con límite y sanitización.
- Separación de equipo editable: Humanoid = Head/Chest/Legs/Feet/Main/Off; Horse = Saddle/Body.
- Nameplate metadata: Player siempre; mob sólo si tiene Custom Name.
- Estado Entity persistente dentro del BlockEntity, separado del item físico que inició la importación.
- Menu/Screen dedicado `Mirage Entity Workspace`.
- Slot central físico de scan poblado.
- Humanoid: seis slots físicos de staging a la izquierda y seis snapshots virtuales projected a la derecha.
- Equipo proveniente de card aparece como incoming virtual si no hay una fuente física ocupando ese staging row.
- ✓ por canal; conflictos son locales al slot; Replace/Cancel explícito.
- Click derecho en el snapshot derecho = borrar sólo esa pieza holográfica.
- `Return inserted gear` devuelve fuentes físicas sin tocar snapshots virtuales.
- Horse: Saddle/Body dinámicos y guard de retiro de card mientras exista staging físico.
- Generic: sin slots inventados para armor visual no equipable.
- Botón para volver al editor principal.
- Protocol v5 para abrir/cerrar workspace y acciones.

### Prerequisitos todavía pendientes

1. reconstrucción client-side segura de una entidad desde el snapshot;
2. preview 3D real con scissor + auto-fit por bounding box;
3. preview Humanoid alimentado por el lado **Projected/Active**, no por Incoming;
4. renderer equipado real compartido con Item Mode (armor geometry, trim, dye, glint, modded hooks);
5. world Entity/Humanoid renderer;
6. nameplate real entre el punto inferior de la proyección y el pedestal;
7. pose presets para Humanoid;
8. Ghost Effect alpha-safe sobre body + armor + hands + layers;
9. entity idle/base animation y freeze control;
10. clearance calculado desde el modelo/pose real.

### QA obligatorio para dev.14

- Craft del Empty Scan Template con 8 nuggets + paper.
- Verificar que no stackea.
- Click derecho normal en Villager/Horse/etc. no escanea ni rompe su interacción.
- Shift + clic derecho escanea y el item cambia de nombre.
- Repetir Shift + clic derecho reemplaza el scan.
- Intentar scanear rider/passenger se rechaza.
- Player scan conserva nombre para futuro nameplate.
- Horse sin nombre no crea nameplate; Horse renombrado sí.
- Abrir Entity Workspace desde el projector y volver al editor principal.
- Card Humanoid: sus seis piezas/manos aparecen incoming sin pisar el lado right.
- Conflicto por slot: Replace cambia sólo esa fila.
- Right-click en right borra sólo esa pieza.
- Staging físico permanece físico hasta `Return inserted gear`; con inventario lleno no debe borrarse.
- Horse con staging físico no debe permitir retirar la scan card hasta devolver el gear.
- Romper projector devuelve Core, scan card y staging físicos una vez; snapshots virtuales no dropean.

### Lista de espera ordenada después de dev.14

**P0 — renderer/preview que desbloquea Entity Mode:** reconstrucción de entidad, auto-fit, preview 3D real, equipment overlay.

**P1 — world projection:** Entity/Humanoid BER path, nameplate, poses, Ghost Effect 3D, animation/clearance.

**P2 — contenido de proyección:** multi-source, Banner, Prism y geometrías posteriores sobre la infraestructura ya estabilizada.

**Fuera del alcance base:** backpacks/accessories/artifacts/trinkets; sólo adapters opcionales cuando el renderer base esté cerrado. Entity Catalog/Scan Binder permanece idea de futuro lejano.


## dev.17 — projection-local 3D presentation buffer

### Implemented

- `ProjectionRenderBuffers` wraps only the current Mirage render call.
- Tint RGB multiplies Item/Entity/Humanoid vertex colour.
- Ghost Effect multiplies vertex alpha and remaps common opaque/cutout textured entity layers to translucent equivalents.
- Unknown/special RenderTypes fail closed to their original shader/state rather than being forced through an incompatible format.
- Entity Workspace preview uses the same local presentation path while retaining vanilla inventory-style cursor tracking, auto-fit and scissor.
- Ordinary Item preview now uses full `ItemDisplayContext.FIXED` 3D geometry.
- Standalone armor preview/world render remains equipped geometry on the invisible Humanoid rig.
- Base nameplate follows projection Tint/Ghost.

### QA required

1. `build.bat` on Windows/NeoForge 21.1.244.
2. Item Mode: stone block, glass, sword/tool, shield, enchanted item at Ghost 0/25/50/90.
3. Standalone armor: vanilla, dyed leather, trim, glint.
4. Humanoid scan: body + six projected channels with Tint presets and Ghost range.
5. Bodyless mannequin: full armor + two hands.
6. Generic entities: Chicken/Creeper/Blaze/Ender Dragon.
7. Special layers: charged Creeper, glowing eyes, dragon eyes, enchanted layers.
8. At least one modded entity with a custom RenderType and one modded armor model.
9. Two nearby projectors with different Tint/Ghost values: verify no render-state contamination between them.
10. Reopen both GUIs repeatedly and verify subsequent text/items/world rendering are not tinted.

### Known boundary

Glint/custom shaders with no vertex colour channel may not fade with exactly the same curve as the base translucent model. Do not solve that by reintroducing persistent `RenderSystem.setShaderColor`; if QA exposes residue, add a dedicated glint-aware projection path.

### Next direct work after QA/static cleanup

- Humanoid pose data/presets and a GUI cycle control.
- Pose/model-aware clearance instead of the current conservative square for Entity.
- Species-specific visual animation adapters only where vanilla renderer history requires them.
- Banner/multi-source/Prism after the 3D baseline is stable.


### dev.34 — Custom names / handbook hand render
- Entity Scan v5 freezes nametag CustomName explicitly and keeps a legacy fallback.
- Debug Handbook cancels first-person rendering of its own held stack while its Screen is active.
