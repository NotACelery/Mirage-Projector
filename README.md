# Mirage Projector

> **Development baseline: `0.1.0-dev.40` (SOURCE candidate).** dev.39 ya fue ejecutado in-game y queda como baseline vivo de GIF/import/aspect. dev.40 conserva protocolo **18** y corrige la UX de POWER / CAPACITY más la estabilidad de Piglin/Hoglin como clones de proyección dimension-neutral. Windows build/in-game QA de dev.40 siguen pendientes.


## Contrato autoritativo actual — dev.40

### Power / Capacity UX

El Power System matemático de dev.38 **no cambia**: Base PU × chassis multiplier × Core amplification, nominales con Overdrive, sliders dinámicos y Ghost rebate mínimo siguen siendo autoridad. dev.40 corrige cómo se presenta:

- el Core slot tiene una columna reservada y nunca comparte píxeles con texto;
- la barra visual `Load / Effective capacity` vuelve a existir;
- pasar el mouse por el panel ya no abre un tooltip enorme;
- el breakdown exacto sólo aparece al buscarlo en el pequeño `?` del header.

### Projection-only entity environment normalization

Los clones Mirage deben representar el snapshot visual y **no reaccionar al dimension/bioma local como si fueran mobs vivos**. Piglin y Hoglin son el primer adapter explícito: después de cargar el NBT congelado, sólo la copia temporal client-side recibe inmunidad a zombificación. Esto elimina el shake vanilla de conversión en Overworld/End sin tocar la entidad real ni la Entity Scan Card. Preview y world projection usan la misma factory. Véase `docs/ENTITY-DIMENSION-NORMALIZATION-dev40.md`.

### Autoridad acumulativa

GIF/formats/Wide/Tall/Prism siguen gobernados por los documentos dev.39 (`GIF-ANIMATED-IMAGE-dev39.md`, `IMAGE-FORMAT-IMPORT-CONTRACT-dev39.md`, `WIDE-TALL-SINGLE-ASPECT-dev39.md`, `PRISM-ADAPTIVE-ASPECT-dev39.md`). Power/chassis/lifetime siguen gobernados por dev.38. Para recuperación general desde este punto, usar `docs/CURRENT-IMPLEMENTATION-AUDIT-dev40.md`.

## Contrato autoritativo actual — dev.39

Cuando cualquier nota histórica de dev.8–dev.37 contradiga esta sección, mandan esta sección y:

- `docs/POWER-SYSTEM-REWORK-dev38.md`;
- `docs/CHASSIS-IMAGE-LAYOUT-POWER-UX-dev38.md`.
- `docs/ENTITY-WORKSPACE-LIFETIME-dev38.md`.
- `docs/DEV38-CLOSURE-QA.md`;
- `docs/GIF-ANIMATED-IMAGE-dev39.md`;
- `docs/WIDE-TALL-SINGLE-ASPECT-dev39.md`;
- `docs/CURRENT-IMPLEMENTATION-AUDIT-dev39.md`;
- `docs/DEV39-GIF-QA.md`.


### GIF / Animated Image — dev.39

GIF ya no es backlog: es una fuente real del **Image Workspace**. No añade un SourceMode nuevo. Compact/Display/Field reproducen un Plane animado; Wide/Tall aceptan GIF tanto en SINGLE como en sus cuatro slots MULTI; Prism permite GIF independiente en North/East/South/West. Los efectos de Image (Flip, Scanlines, Tint, Ghost, Lighting, Rotation, Float y BackFaceMode) se aplican al frame actual.

El asset GIF se valida y conserva como GIF para no perder timing/disposal. Nuevos assets se almacenan como `<sha256>.asset`; los `<sha256>.png` históricos de dev.1-dev.38 siguen siendo compatibles. Límites vigentes: GIF <= 8 MiB, canvas <= 1024 px por eje, <= 128 frames, <= 16.777.216 frame-píxeles decodificados, delay mínimo 20 ms y loop máximo 5 min. Mirage decodifica una vez y cachea frames; no lee el archivo en cada frame.

GIF no paga un surcharge de PU: la PU sigue midiendo geometría/presentación del emisor, mientras memoria/decoder se controlan con límites técnicos independientes.

#### Detección por contenido y formatos

La extensión nunca decide el decoder. dev.39 acepta PNG, JPG/JPEG, WebP estático y BMP como fuentes estáticas, más GIF animado. GIF renombrado a `.png/.jpg/.webp` sigue siendo GIF. Animated WebP y APNG se detectan por sus bytes/chunks y se rechazan explícitamente por ahora, incluso si fueron renombrados; Mirage nunca los degrada silenciosamente al primer frame. Contrato completo: `docs/IMAGE-FORMAT-IMPORT-CONTRACT-dev39.md`.

#### Prism adaptativo

Prism sigue siendo exclusivamente cuatro caras cardinales N/E/S/W, una fuente por cara y sin stacking 4x1/1x4. Para no quedar limitado a arte cuadrado, cada cara usa un nominal adaptativo para PU: horizontal claro -> 80x32 (Wide-like), vertical claro -> 32x80 (Tall-like), casi cuadrado -> 48x48. La geometría/overdrive se cobra por cara. Ver `docs/PRISM-ADAPTIVE-ASPECT-dev39.md`.

### Wide/Tall SINGLE — regla de aspect ratio

SINGLE **nunca estira ni recorta**. Wide con fuente apaisada/cuadrada usa Scale como ancho; si la fuente es vertical, su altura pasa a ser el eje dominante y llega antes al overdrive vertical de Wide. Tall hace el inverso: vertical/cuadrada usa Scale como alto; una fuente apaisada conserva su ratio pero llega antes al overdrive horizontal. El renderer y `ProjectionPower` usan la misma función `ProjectionImageSizing`, así que el tamaño cobrado en PU es exactamente el que se dibuja. MULTI conserva las cuatro celdas cuadradas equivalentes de dev.38.

### Power

```text
Effective PU = floor(Core base PU × Chassis multiplier × Core amplification)
```

| Standard Core | Base PU |
|---|---:|
| Glass | 32 |
| Quartz | 48 |
| Amethyst | 64 |
| Diamond | 96 |
| Netherite | 128 |

Los Cores estándar actuales tienen amplificación ×1.00. El sistema ya reserva un multiplier para futuros Improved Cores (target inicial ≈ ×1.50 manteniendo la misma base PU del material), pero sus items/recipes/textures se diseñarán después del QA de balance.

Los chassis ya **no poseen hard caps de gameplay** para Scale/Lift/Float. Sus medidas son rangos **nominales de eficiencia**: se pueden superar con Overdrive si hay Effective PU, pero el componente que supera nominal paga una penalización cuadrática `ratio²`.

### Chassis/Image

| Chassis | Image contract | Nominal W×H | Nominal Lift | Nominal Float | PU multiplier |
|---|---|---:|---:|---:|---:|
| Mirage Projector / Compact | one continuous Plane | 10×10 px | 32 px | 4 px | ×1.00 |
| Mirage Display | one continuous Plane | 32×32 px | 48 px | 12 px | ×1.50 |
| Wide | toggle: one wide image **or** optional 4×1 | 80×32 px | 64 px | 12 px | ×2.00 |
| Tall | toggle: one tall image **or** optional 1×4 | 32×80 px | 96 px | 16 px | ×2.00 |
| Mirage Field | **one continuous large Plane; never a 3×3 grid** | 128×128 px | 144 px | 24 px | ×4.00 |
| Mirage Prism | N/E/S/W lateral cardinal faces | 48×48 px/face | 96 px | 12 px | ×2.00 |

Wide/Tall MULTI usa cuatro celdas cuadradas equivalentes: Scale 80 = Wide 80×20 o Tall 20×80, con cuatro celdas 20×20. Field no posee grid ni selector de 9 imágenes.

Scale/Lift/Float terminan en el **máximo efectivo pagable** por la configuración actual. La GUI muestra capacidad efectiva, carga, nominales y máximos dinámicos; `Remaining PU` deja de ser el mecanismo principal para entender límites.

Ghost da un rebate de PU deliberadamente mínimo: a 90% no puede superar el **3% del coste no-base** y se redondea hacia abajo.

Newly placed projectors usan horizontal furnace-style facing. Plane hereda esa orientación; Prism Image/Banner mantiene N/E/S/W como puntos cardinales reales del mundo.

### Entity workspace lifetime

Humanoid sigue soportando el **bodyless mannequin**: retirar una Humanoid Scan Card limpia el body, pero los seis canales virtuales pueden quedarse mientras no se active otra familia de entidad. La regla nueva de dev.38 es de invalidación por **cambio de tipo de GUI**:

| Card que pasa a estar activa | Workspace virtual que puede conservarse | Workspace virtual que debe limpiarse |
|---|---|---|
| Humanoid | Humanoid Head/Chest/Legs/Feet/Main/Off | Horse Saddle/Body |
| Horse | Horse Saddle/Body | los seis Humanoid |
| Generic | ninguno editable | Humanoid + Horse |

Esto evita que armor/manos guardadas queden invisibles detrás de una GUI Generic/Horse y vuelvan a aparecer al retirar esa card. Los snapshots incompatibles se borran cuando desaparecen sus filas y su pose contextual vuelve a default. Los **items físicos de staging no se destruyen** por esta limpieza; mantienen el retorno/drop seguro existente.

### Idle state de chassis

Compact, Display, Wide, Tall, Field y Prism deben mostrar el mismo **libro vanilla flotante** cuando no existe ninguna fuente renderizable. El idle marker no requiere Core. Esta regla corrige la asimetría donde sólo Compact lo mostraba porque Compact conserva el Glass Core histórico por defecto y los otros cinco empiezan con socket vacío.

### Estado pendiente inmediato

dev.39 fue ejecutado in-game por el usuario. dev.40 es el **SOURCE candidate** actual y todavía no debe marcarse build-clean hasta Windows `build.bat` + QA. Faltan Windows `build.bat` y QA acumulativo: GIF en los seis chassis, compositor/timing/transparencia, Wide/Tall SINGLE y MULTI, migración `.png` -> `.asset`, multiplayer asset download, Power/Overdrive/sliders, facing N/E/S/W, libro idle, lifetime Humanoid/Horse/Generic y la regresión Entity/projector/water de dev.36-dev.37. El inventario actual vive en `docs/CURRENT-IMPLEMENTATION-AUDIT-dev39.md` y el checklist específico en `docs/DEV39-GIF-QA.md`.

Tras estabilizar dev.40, la siguiente oleada prevista vuelve a **Improved Core items/recipes/textures y variantes especializadas**, definiendo primero para cada una rol, efecto, amplificación, materiales y caso de uso; después vendrán posibles retoques visuales de chassis. El backlog técnico adicional mantiene RenderTypes/glint especiales, bounds Entity específicos por renderer, preview Banner rico, performance/culling y multiplayer stress QA.

> Documento maestro de diseño y alcance para **Mirage Projector**, un mod de decoración y exhibición para Minecraft 1.21.1 / NeoForge.

## 1. Visión general

**Mirage Projector** nace para cubrir una función que actualmente queda a medio camino entre los mods de marcos de imágenes, proyectores multimedia, item displays, armor stands y hologramas tradicionales: permitir construir **proyecciones decorativas flotantes, animadas y configurables** que puedan mostrar imágenes externas, items, banners, equipamiento y otras formas visuales de Minecraft de una manera integrada al mundo.

La intención no es crear una pantalla de video ni un navegador dentro de Minecraft. El foco es el **display diegético**: un bloque físico pequeño o grande que genera una "ilusión" visible en el mundo, como si fuera un espejismo estable controlado por un núcleo.

Casos de uso previstos:

- Una imagen PNG/JPG/JPEG/WebP o GIF animado flotando sobre un pedestal.
- Pixel art o ilustraciones de personajes como decoración.
- Logos de base, emblemas de facción o carteles.
- Banners sin poste, flotando y rotando.
- Items raros o encantados expuestos sin item frame.
- Herramientas, armas, bloques u objetos modded proyectados.
- Una armadura completa ensamblada en forma de figura holográfica.
- Una figura de armadura enorme, de varios bloques de altura.
- Un "Mirage Prism" con cuatro caras laterales y una imagen distinta en cada una.
- Cuatro imágenes colocadas en tira 4×1/1×4 mediante Wide/Tall MULTI.
- GIFs animados reales, por ejemplo un estandarte ondeando o cuatro animaciones independientes en Prism.
- Instalaciones monumentales visibles desde lejos, como una estatua de armadura de netherita con un tridente.

El nombre **Mirage Projector** se elige precisamente porque las proyecciones de los modelos grandes deberían sentirse más cercanas a un espejismo artificial que a una simple pantalla.

---

## 2. Principio de diseño principal

Desde dev.38 el sistema se divide en tres responsabilidades separadas. Esta separación reemplaza la antigua regla donde Core y chassis poseían límites hardcodeados superpuestos.

### 2.1. Projector / Chassis

El **cuerpo físico del proyector** determina:

- la geometría y los Source layouts que soporta;
- el rango **nominal** de ancho/alto, Lift y Float donde opera a eficiencia normal;
- el multiplicador con el que aprovecha la PU base del Core;
- sus capacidades especiales (Plane, Prism, multi-source opcional, etc.);
- su forma física, Core socket y orientación al colocarse.

Los valores nominales **no son hard caps de gameplay**. Una configuración suficientemente alimentada puede superar nominal mediante Overdrive, pagando una penalización cuadrática por el componente excedido.

Regla conceptual:

> **El Chassis determina la forma del Mirage y cuán eficientemente usa la potencia disponible.**

### 2.2. Projection Core

El **núcleo insertado** aporta la PU base:

- Glass: 32 PU;
- Quartz: 48 PU;
- Amethyst: 64 PU;
- Diamond: 96 PU;
- Netherite: 128 PU.

El Core estándar **no** impone Scale/Lift/Float max. Su responsabilidad es aportar energía al sistema.

Regla conceptual:

> **El Core determina la materia prima energética del Mirage.**

### 2.3. Core amplification / grado

La capacidad efectiva incluye un multiplier de Core separado de la PU base. Los Cores raw actuales son Standard ×1.00. Los futuros Improved Cores deben conservar la PU base del material y aumentar este multiplier, con un target inicial aproximado de ×1.50.

Regla conceptual:

> **El grado del Core determina cuánto puede amplificarse la misma PU base sin inventar una segunda escalera arbitraria de energía.**

La capacidad final es:

```text
Effective PU = floor(Core base PU × Chassis multiplier × Core amplification)
```

Esto permite, por diseño, que un Compact con Netherite y un Field con Glass partan ambos de 128 Effective PU, pero el Field sea muchísimo más eficiente para superficies grandes porque su nominal geometry es 128×128 en vez de 10×10.

---

## 3. Estado del proyecto

### Plataforma objetivo inicial

- Minecraft: **1.21.1**
- Loader: **NeoForge**
- Java: **21**
- Nombre del mod: **Mirage Projector**
- Mod ID: `mirage_projector`
- Package base inicial: `celerbi.mirageprojector`

### Estado actual

La línea de desarrollo ya superó el prototipo visual mínimo y en `0.1.0-dev.30` posee **Image Mode, Item Mode, Entity/Humanoid Mode y Banner Mode**, transporte de assets cliente/servidor, clearance/preview, **Projection Core / Power**, Ghost/Tint 2D/3D y el primer chassis multi-face funcional: **Mirage Prism** para fuentes Image y Banner.

Estado acumulado actual:

1. Bloque Compact Mirage Projector con Block Entity y GUI.
2. Renderer dinámico de imágenes planas con aspect ratio preservado.
3. PNG/JPG/JPEG local -> normalización PNG -> SHA-256 -> cache.
4. Front/Back con Mirrored, Readable e Independent Faces.
5. Rotación, dirección, offset, float temporal/rotation-synced, amplitud y Projection Lift.
6. Escala de debug hasta 10 bloques para stress-test del renderer.
7. Selector nativo de archivos mediante LWJGL/TinyFileDialogs.
8. Creative tab propio y modelo físico compacto con obsidiana/vidrio; el pilar central se renderiza dinámicamente según el Projection Core instalado.
9. Corrección de frustum/culling de proyecciones grandes durante la etapa debug.
10. Aislamiento real de caras: sólo se dibuja la cara físicamente visible del plano.
11. **Multiplayer asset transport dev.7**: la imagen normalizada se divide en chunks, se sube al servidor, se valida por SHA-256 y se guarda dentro del mundo; otros clientes la solicitan automáticamente si no está en su cache.
12. **Item Mode dev.7→dev.12**: renderer 3D con ItemRenderer vanilla; desde dev.12 el antiguo slot físico se reemplaza por un **Virtual Snapshot Slot** que copia el estado visual del ItemStack sin consumir, almacenar ni devolver el objeto real.
13. **Clearance dev.7→dev.9**: la GUI escanea el volumen de proyección; dev.9 añade overlay in-world, outline de obstructores y un envelope delgado orientado para Planes estáticas.
14. Botones para limpiar Front/Back sin tener que reemplazar el asset.
15. **Core / Power dev.8 (histórico; modelo de límites superseded en dev.38)**: Glass, Quartz, Amethyst, Diamond y Netherite pasan a ser núcleos físicos intercambiables y se crea el primer presupuesto Power. Sus antiguos hard caps de Scale/Lift/Float ya no son vigentes.
16. **Image completeness dev.9→dev.10**: WebP local mediante decoder embebido, preview dentro de la GUI, Fullbright/World Light, Ghost Effect/Transparency, Tint y Scanlines.
17. **Clearance preview dev.9**: envelope del Mirage y bloques obstructores dibujados directamente en el mundo mientras la GUI está abierta.
18. **Transfer feedback dev.9**: progreso de download, porcentaje de envío de upload, ACK final del servidor, estados locales de fallo y reintento manual desde la GUI.
19. **Physical chassis dev.10→dev.21**: Compact, Display, Wide, Tall y Field comparten BlockEntity/GUI/renderer/Core/Power; dev.21 añade Mirage Prism como bloque físico funcional. Effigy y Colossal siguen como contratos futuros.
20. **Render-family contract + source thumbnails dev.11**: Image/Banner queda definido como geometría de caras 2D, Item como objeto 3D único y Armor/Effigy como rig 3D posable; Front/Back ahora muestran miniaturas simultáneas con semántica real Mirrored/Readable/Independent.
21. **Virtual display snapshots dev.12**: Item Mode deja de ser inventario físico. Cada captura recibe un UUID de snapshot, conserva una copia serializada de un solo ItemStack para render y permite que el objeto real siga equipado/en inventario. El mismo contrato queda fijado para los seis canales de Humanoid Entity Mode.
22. **Entity Workspace dev.14**: Empty Scan Template, Shift + clic derecho, snapshot de entidad/equipment, seis canales Humanoid Incoming/Projected, Horse Saddle/Body, resolución de conflicto por ✓ y clear por click derecho.
23. **Live Entity Preview dev.15**: reconstrucción client-only de la entidad escaneada, equipment tomado sólo desde Projected/Active, auto-fit por bounds, scissor vanilla, seguimiento del cursor y panel derecho responsive.
24. **Entity/Humanoid Render dev.16**: world renderer volumétrico, nameplate de base, maniquí humanoide sin body, Player skin congelada y armor standalone de Item Mode renderizada como geometría equipada.
25. **3D Ghost/Tint dev.17**: Item/Entity/Humanoid usan un buffer local de proyección que multiplica Tint/alpha por vértice y rerutea las capas opacas/cutout comunes a RenderTypes translúcidos sin tocar shader color global; la preview de Entity comparte el mismo efecto y la preview de Item pasa a modelo FIXED 3D.
26. **NeoForge equipment API repair dev.18**: corrige el primer build Windows de la línea Entity/Humanoid, centraliza resolución de equipment y separa correctamente Saddle (inventario de caballo) de `EquipmentSlot.BODY`/slots humanoides.
27. **GUI architecture dev.19**: la pantalla primaria queda limitada a presentación global + Core/Power; Image e Item reciben workspaces dedicados, Entity/Humanoid mantiene su workspace dinámico, se añade el manual temporal traducible y se aumenta la curva provisional de potencia de Cores.
28. **Entity interaction/loadout dev.20**: Entity Scan toma prioridad en la interacción temprana para no perder el gesto ante Horse/Villager/etc.; Humanoid añade Capture Equipped Loadout no consumible hacia Incoming.
29. **Mirage Prism dev.21**: bloque/chassis Prism funcional para Image Mode con cuatro fuentes persistentes North/East/South/West, previews/import independientes, Same Source on All Faces, renderer de cuatro quads abiertos y Power/Clearance/bounds conscientes de la geometría Prism.
30. **Humanoid poses dev.22**: ocho presets persistentes separados de los snapshots, botón de ciclo en Entity Workspace, aplicación al rig vanilla después de `setupAnim` y primera pasada de preview/clearance/render bounds conscientes de la pose.
31. **Entity safety/UX dev.23**: staging físico se devuelve al aceptar o salir (overflow al suelo), Incoming se consume al pasar a Projected, GUI Entity consolidada, PU Used/Available explícitos + Core Capacity.
32. **Entity stabilization dev.24→dev.26**: inventarios Item/Entity recentrados, Horse Reposo/En dos patas seleccionable y Ghost migrado a RenderTypes Mirage que no escriben depth; cuerpo y armor humanoide comparten alpha.
33. **Held-item Ghost dev.27→dev.28**: Main/Off Hand normaliza también capas raw de ItemRenderer para conservar alpha sin escribir depth sobre agua.
34. **Banner Mode dev.28**: snapshots virtuales no consumibles, Plane Front, Prism North/East/South/West, copia de North a todas las caras y renderer de tela/patterns vanilla sin poste ni travesaño.
35. **Power/chassis recovery dev.38**: Core base PU × chassis multiplier × Core amplification, Overdrive cuadrático, sliders efectivos, Ghost rebate mínimo, Field continuo, Wide/Tall SINGLE/MULTI opcional, facing horizontal y reparación del layout Item Preview.

### 3.1A. Arquitectura de GUI desde dev.19

La GUI principal usa bandas visuales independientes; **Appearance y Core no pueden volver a compartir coordenadas**. El Core tiene slot físico, visual de material, potencia/limitaciones y tooltip de tiers; el inventario queda debajo. Las previews pertenecen exclusivamente al workspace de su fuente.

La captura/importación de contenido ya no comparte panel con los controles globales. La regla permanente es:

> **La GUI principal define cómo se presenta la proyección; cada Workspace define qué se proyecta.**

- **Projection Settings**: Scale, Lift, Rotation, Floating, Lighting, Ghost, Tint, Core, Power, Clearance e inventario.
- **Image Workspace**: previews/import de caras, Back mode, Vertical Flip y Scanlines.
- **Item Snapshot Workspace**: slot virtual + preview 3D + activación del snapshot.
- **Entity/Humanoid Workspace**: sigue siendo dinámico según la card: Humanoid, Horse o Generic.
- **Banner Workspace**: snapshots virtuales de estandarte; Plane Front o Prism North/East/South/West, sin consumir el banner real.

La pantalla principal ya no contiene previews diminutas ni botones de importación. Esto evita solapamientos y deja espacio real a cada fuente. El contrato detallado vive en `docs/GUI-ARCHITECTURE-dev19.md`.

El **Mirage Debug Handbook** temporal usa componentes traducibles y sirve como manual in-game mientras la documentación/jugabilidad todavía cambia rápido.

> **Nota histórica:** dev.19 usó temporalmente la curva 16/32/96/192/384 PU y hard caps independientes. **Superseded por dev.38.** La curva vigente es 32/48/64/96/128 base PU, multiplicada por chassis y Core amplification; chassis usa nominales + Overdrive.

### 3.2. Dependencias embebidas y notices

La dependencia usada para WebP se documenta en `THIRD_PARTY_NOTICES.md`. El mod no requiere que el jugador instale ese decoder por separado.

### 3.3. Semántica exacta de sincronización dev.9

El porcentaje de upload mostrado por el cliente significa **chunks despachados hacia el servidor**, no chunks ya persistidos. Al llegar a 100% el estado pasa a `awaiting server`. La transferencia sólo se considera completada después de recibir `AssetUploadAckPayload(accepted=true)`. Si el servidor rechaza el asset, o falla la cache/hash/I/O local, el estado queda visible y el botón `↻` permite reintentar Front y Back sin reimportar la imagen.

El download calcula su porcentaje a partir de chunks realmente recibidos. Al completar, el cliente vuelve a verificar SHA-256 antes de hacer el reemplazo atómico del PNG cacheado y de invalidar la DynamicTexture anterior.

La build sigue siendo de desarrollo. Desde dev.12 Item usa snapshots virtuales no robables y dev.14→dev.22 construyó Entity Scan, preview/world render y poses. dev.23→dev.26 estabilizó propiedad de staging, layouts, Horse Idle/Rearing y el pipeline Ghost no-depth-write para cuerpo/armor. dev.27 extiende ese contrato a Main/Off Hand para cerrar el artefacto de agua de ItemRenderer. dev.28 suma **Banner Mode** con snapshots virtuales y tela/patterns vanilla sin poste, incluyendo cuatro caras independientes en Mirage Prism. Siguen pendientes QA fino de glint/custom RenderTypes, renderers especiales no-LivingEntity y bounds especiales por renderer. El layout dev.33 queda histórico: dev.38 conserva MULTI sólo como opción de Wide/Tall y restaura Field a un único Plane continuo.

## 3.1. Política permanente de URLs y descripciones

Las descripciones públicas del mod deben permanecer válidas aunque el repositorio cambie de cuenta, organización o nombre. Por ello:

- **No hardcodear URLs de GitHub/repository en la descripción del mod** (`neoforge.mods.toml`, descripción de CurseForge, descripción de Modrinth u otros textos equivalentes).
- No usar una URL de repositorio como parte de la explicación funcional o del marketing del mod.
- Si una plataforma tiene campos de links externos, esos links se mantienen como metadata independiente de la descripción y se pueden actualizar sin reescribirla.
- La documentación de desarrollo puede referirse al "repositorio canónico" o al repositorio que contiene este README sin depender de una URL absoluta.
- Los scripts de toolchain sí pueden contener URLs técnicas estables necesarias para funcionar (por ejemplo la distribución oficial de Gradle); esta regla apunta a **descripciones/repository links del mod**, no a dependencias técnicas.
- Cuando se detecte una descripción antigua con una URL de repositorio obsoleta en otro mod del mismo autor, debe eliminarse/corregirse en su siguiente pasada de metadata. Easy Farmer's Delight queda explícitamente identificado como uno de esos casos pendientes.

Esta política es deliberadamente transversal a los proyectos del autor, no exclusiva de Mirage Projector.

---

# 4. Modelo compacto inicial

El primer modelo de referencia fue construido en Blockbench.

Archivo de referencia del proyecto:

`docs/reference/Mirage Projector.json`

## 4.1. Geometría física

El modelo actual contiene tres piezas físicas principales.

### Base de obsidiana

Coordenadas:

- `from: [0, 0, 0]`
- `to: [16, 1, 16]`

Dimensiones:

- 16 px de ancho.
- 16 px de profundidad.
- 1 px de alto.

Material conceptual:

- Obsidiana.

### Segunda capa

Coordenadas:

- `from: [2, 1, 2]`
- `to: [14, 2, 14]`

Dimensiones:

- 12 × 12 × 1 px.

Material conceptual:

- Vidrio.

### Pilar / núcleo visual central

Coordenadas:

- `from: [7, 2, 7]`
- `to: [9, 5, 9]`

Dimensiones:

- 2 × 2 × 3 px.

En el modelo de referencia actual usa textura de diamond block únicamente como prueba visual.

En el diseño definitivo, este elemento debe reflejar el núcleo real insertado o el estado del projector.

## 4.2. Proyección de referencia

El modelo Blockbench incluye un elemento `Image`:

- `from: [3, 6, 8]`
- `to: [13, 16, 8]`

Dimensiones:

- 10 px de ancho.
- 10 px de alto.
- **0 px de profundidad.**

Esto confirma el diseño deseado: la imagen no necesita ser un cubo. Debe renderizarse como un **plane/quad 2D**.

La versión final no debe depender de esa geometría estática del JSON. El plano de Blockbench es solamente una referencia de:

- posición;
- escala inicial;
- margen;
- altura;
- orientación.

El renderer dinámico debe encargarse de dibujar la proyección real.

## 4.3. Margen vertical del modelo compacto

La geometría física termina en Y = 5 px.

La proyección comienza en Y = 6 px.

Por lo tanto, el Mirage Projector compacto posee inicialmente:

- **1 pixel de margen vertical real** entre la parte superior del projector y el borde inferior de la imagen.

Ese dato es importante para la amplitud máxima de flotación.

---

# 5. Materiales del Mirage Projector

## 5.1. Diseño visual base

Se mantiene como dirección artística:

1. Base inferior: obsidiana.
2. Capa intermedia: vidrio.
3. Núcleo / emisor central: material dependiente del core.

La obsidiana entrega una base visual pesada y tecnológica/mágica.

El vidrio comunica:

- proyección;
- refracción;
- luz;
- energía;
- transparencia.

El núcleo central es el elemento que comunica qué material alimenta el projector.

## 5.2. Materiales de núcleo previstos

Materiales definidos o considerados:

- Glass.
- Quartz.
- Amethyst.
- Diamond.
- Netherite.

Materiales candidatos futuros, no confirmados:

- Echo Shard.
- End Crystal.
- Glowstone.
- Prismarine.
- Otros materiales modded si existe una buena integración.

No todos los materiales deben convertirse obligatoriamente en una escalera lineal de tiers.

La intención es que puedan existir diferencias de:

- potencia;
- eficiencia;
- afinidad con ciertas geometrías;
- cantidad de caras;
- estabilidad;
- animación;
- alcance.

La versión inicial puede comenzar con una progresión simple y posteriormente especializar los núcleos.

---

# 6. Projection Power — contrato dev.38

> **Supersedes dev.8/dev.19.** La documentación histórica que asigne Scale/Lift/Float max a cada Core ya no describe el sistema activo.

Projection Power sigue siendo un presupuesto propio de Mirage y **no es FE/redstone energy**.

## 6.1. Base PU de Core

| Standard Core | Base PU | Core amplification actual |
|---|---:|---:|
| Glass | 32 | ×1.00 |
| Quartz | 48 | ×1.00 |
| Amethyst | 64 | ×1.00 |
| Diamond | 96 | ×1.00 |
| Netherite | 128 | ×1.00 |

El material determina Base PU. No existe en dev.38 `Core Scale max`, `Core Lift max` ni `Core Float max`.

## 6.2. Chassis multiplier

| Chassis actual | PU multiplier |
|---|---:|
| Compact | ×1.00 |
| Display | ×1.50 |
| Wide | ×2.00 |
| Tall | ×2.00 |
| Field | ×4.00 |
| Prism | ×2.00 |

La capacidad utilizable es:

```text
Effective PU = floor(Core base PU × Chassis multiplier × Core amplification)
```

Ejemplo deliberado:

```text
Compact + Netherite = 128 × 1.00 = 128 Effective PU
Field + Glass        =  32 × 4.00 = 128 Effective PU
```

Ambos poseen el mismo presupuesto efectivo, pero Field es mucho más eficiente para geometría grande porque sus targets nominales son mucho mayores.

## 6.3. Improved Cores

La arquitectura separa `Core amplification` de Base PU. Los futuros Improved Cores deben conservar la Base PU de su material y aumentar la amplificación; el target inicial de diseño es aproximadamente ×1.50.

Ejemplo futuro:

```text
Improved Netherite
Base PU 128
Core amplification ×1.50
```

No se añaden todavía los items/recipes/textures de Improved Cores. Primero se valida el balance estándar. La fórmula ya está preparada para agregarlos sin rehacer el sistema.

## 6.4. Nominal y Overdrive

Chassis W×H, Lift y Float pasan a ser **targets nominales de eficiencia**, no hard caps. Superarlos es posible con PU suficiente.

Para cualquier componente por encima de nominal:

```text
ratio = current / nominal
Overdrive cost multiplier = ratio²
```

El penalty se aplica de forma independiente a Geometry, Lift y Float. Un Compact muy potente puede superar 10×10, pero pagará rápidamente mucho más que Display/Field por el mismo resultado.

## 6.5. Fórmula de consumo

Para una proyección no vacía:

```text
Gross PU =
    2 PU emitter/stability
  + Geometry PU
  + Source complexity PU
  + Lift PU
  + Float PU
  + Presentation feature PU
```

### Geometry

```text
baseGeometry = ceil(projectedAreaPx² / 256)
Geometry PU = ceil(baseGeometry × geometryOverdriveRatio²)
```

`256 px²` equivale a un área 16×16.

### Lift

```text
baseLift = ceil(LiftPx / 16)
Lift PU = ceil(baseLift × liftOverdriveRatio²)
```

### Float

Sólo Floating ON:

```text
baseFloat = ceil(FloatPx / 2)
Float PU = ceil(baseFloat × floatOverdriveRatio²)
```

Además existe la regla física:

```text
Float <= Lift
```

### Source complexity

- Independent Front+Back real: +1;
- Wide/Tall MULTI con más de una fuente: +1;
- Prism Image: +2;
- Item: +2;
- Entity: +4;
- Banner Plane: +1;
- Banner Prism: +2.

### Presentation features

- Rotation: +1;
- Rotation-synced Floating activo: +1;
- Fullbright: +1;
- Image Scanlines: +1;
- Tint/Flip: +0.

## 6.6. Ghost rebate

Ghost entrega sólo una reducción óptica mínima:

```text
eligible = max(0, Gross PU - 2)
Ghost saving = floor(eligible × Ghost% / 3000)
Final load = max(1, Gross PU - Ghost saving)
```

A Ghost 90%, el ahorro teórico máximo es 3% del coste no-base y se redondea hacia abajo. No está pensado como herramienta de min-max ni para volver gratis una proyección pequeña.

## 6.7. Dynamic sliders

Scale/Lift/Float usan como endpoint el mayor valor que puede sostener la configuración actual con su Effective PU.

El usuario no debe poder navegar una gran región inválida sólo para buscar el umbral donde el estado deja de ponerse naranja.

La GUI muestra como información principal:

- Base PU;
- chassis multiplier;
- Core amplification;
- Effective capacity;
- Final load;
- actual W×H vs nominal W×H;
- Scale current/effective max;
- Lift current/effective max + nominal;
- Float current/effective max + nominal;
- estado Overdrive.

El hover de la sección Power expone el desglose exacto por componente. `Remaining PU` puede derivarse para diagnóstico, pero deja de ser la métrica principal.

Para la especificación matemática completa ver `docs/POWER-SYSTEM-REWORK-dev38.md`.

---

# 7. Familias de Projectors — targets nominales vigentes

Los valores siguientes **no son hard caps** desde dev.38. Son el rango donde el chassis opera sin penalty de Overdrive.

| Chassis | Nominal W×H | Nominal Lift | Nominal Float | Sources/layout | Geometry | PU multiplier |
|---|---:|---:|---:|---|---|---:|
| Compact | 10×10 px | 32 px | 4 px | 1 | Plane | ×1.00 |
| Display | 32×32 px | 48 px | 12 px | 1 | Plane | ×1.50 |
| Wide | 80×32 px | 64 px | 12 px | SINGLE o 4×1 opcional | Plane | ×2.00 |
| Tall | 32×80 px | 96 px | 16 px | SINGLE o 1×4 opcional | Plane | ×2.00 |
| Field | 128×128 px | 144 px | 24 px | 1 continuo | Plane | ×4.00 |
| Prism | 48×48 px/face | 96 px | 12 px | N/E/S/W | Prism | ×2.00 |
| Effigy | 96×160 px | 128 px | 16 px | futuro | Effigy | provisional ×3.00 |
| Colossal | 160×160 px | 160 px | 32 px | futuro | Volumetric | provisional ×5.00 |

Effigy/Colossal permanecen como arquitectura futura; sus multiplicadores todavía no son balance jugable congelado.

Creative `Debug chassis` elimina penalties de Overdrive para stress test, pero no ignora Core, Effective PU ni `Float <= Lift`.

## 7.1. Compact Mirage Projector

Objetivo: displays pequeños y baratos. Nominal 10×10. Puede entrar en Overdrive con un Core fuerte, pero no debe ser la opción eficiente para obras grandes.

## 7.2. Mirage Display

Objetivo: display general single-plane, nominal 32×32, con más Lift/Float que Compact. Es la opción natural para ilustraciones normales sin saltar a un chassis grande.

## 7.3. Wide

Objetivo: panoramas y filas de imágenes. Image Workspace permite toggle:

- `Single image`: un Plane ancho continuo;
- `4 images`: cuatro fuentes 4×1.

En MULTI las celdas son cuadradas y simétricas con Tall. Scale 80 = 80×20 total, cuatro celdas 20×20.

## 7.4. Tall

Objetivo: ilustraciones altas, banners visuales grandes y cascadas sin pagar Field. Toggle:

- `Single image`: un Plane alto continuo;
- `4 images`: cuatro fuentes 1×4.

Scale 80 = 20×80 total, cuatro celdas 20×20.

## 7.5. Mirage Field

Objetivo: una superficie 2D masiva. Field muestra **una sola imagen continua** y no posee modo 3×3/9 imágenes. Nominal 128×128 y Lift 144.

## 7.6. Mirage Prism

Objetivo: cuatro caras laterales world-cardinales North/East/South/West. No posee top/bottom. Cada cara puede tener fuente independiente; Same Source on All Faces puede reutilizar North.

## 7.7. Placement facing

Los projectors actuales almacenan FACING horizontal y se colocan estilo furnace mirando al jugador. Plane hereda ese facing como orientación base. Prism Image/Banner conserva las etiquetas N/E/S/W como cardinales reales del mundo.


# 8. Fuentes de proyección

La arquitectura debe tratar la fuente visual como un concepto separado.

Tipos previstos:

```text
IMAGE
ITEM
BANNER
PRISM
EQUIPMENT / EFFIGY
ANIMATED_IMAGE
```

---

# 9. Image Mode

## 9.1. Formatos requeridos

Objetivo final mínimo:

- `.png`
- `.jpg`
- `.jpeg`
- `.webp`

Extensión futura:

- `.gif`

No se planea:

- video;
- YouTube;
- streaming;
- navegador;
- video remoto;
- audio.

La filosofía es:

> Mirage Projector importa assets visuales, no reproduce Internet.

## 9.2. Conversión interna

El archivo original no debe almacenarse sin procesar por defecto.

Flujo objetivo:

```text
archivo seleccionado
      ↓
decode
      ↓
validación
      ↓
normalización
      ↓
resize si excede límites
      ↓
RGBA
      ↓
PNG normalizado
      ↓
hash
      ↓
storage del mundo/server
```

Esto permite que el renderer no necesite saber si el usuario importó:

- JPEG;
- WebP;
- PNG.

Internamente puede trabajar con PNG/RGBA.

## 9.3. Aspect ratio

Por defecto:

```text
[x] Lock Aspect Ratio
```

La imagen debe conservar su proporción.

Si:

```text
Image: 1920 × 1080
Ratio: 16:9
```

y se aumenta el ancho, el alto cambia automáticamente.

Cuando una dimensión llega al límite del chassis:

- la otra dimensión deja de aumentar;
- se mantiene el ratio;
- la GUI comunica qué límite fue alcanzado.

No se debe estirar silenciosamente una imagen.

Un modo debug puede permitir deformación temporal para pruebas, pero no es comportamiento final normal.

## 9.4. Escala del Compact Projector

La referencia inicial de 10 × 10 px representa el área máxima conceptual del projector compacto.

Para una imagen no cuadrada:

- no se fuerza a 10 × 10;
- se usa el mayor tamaño que quepa dentro del envelope;
- se preserva ratio.

Ejemplo:

- imagen vertical 448 × 512;
- alto máximo 10 px;
- ancho resultante ≈ 8.75 px.

---

# 10. Caras de una imagen plana

El nuevo modelo de Blockbench de referencia contiene una imagen distinta en North y South.

Eso genera tres modos oficiales para el reverso.

## 10.1. Mirrored

Un solo plano físico.

Al verlo por atrás, el contenido aparece invertido como si se mirara la parte trasera de una lámina.

Ejemplo con texto:

```text
FRONT: HELLO
BACK:  OLLEH
```

Uso:

- ilustraciones;
- imágenes sin texto;
- comportamiento físicamente intuitivo.

## 10.2. Readable / Same Orientation

Se renderiza una cara posterior propia de manera que el contenido sea legible en la misma orientación desde ambos lados.

```text
FRONT: HELLO
BACK:  HELLO
```

Uso:

- logos;
- carteles;
- letras;
- señales;
- banners con texto.

## 10.3. Independent Faces

Cada lado puede usar un asset distinto.

```text
Front: image_a.png
Back:  image_b.png
```

Este modo queda oficialmente dentro del diseño porque el modelo de referencia ya demuestra la utilidad.

---

# 11. Orientación de imagen

Opciones:

- Normal.
- Rotate +90°.
- Rotate +180°.
- Rotate +270°, aunque puede representarse mediante 90° repetido.
- Flip Vertical.

El flip vertical corresponde al efecto tipo Dinnerbone solicitado.

Un flip horizontal independiente no es prioritario porque:

- 180° cubre varios casos geométricos;
- el tratamiento de la cara trasera ya tiene sus propios modos.

Si durante pruebas aparece una necesidad real, puede agregarse.

---

# 12. Rotación

## 12.1. Toggle

```text
Rotation
[x] Enabled
```

## 12.2. Velocidad

El control principal representa el tiempo de una vuelta completa.

Default:

- 360° en 4 segundos.
- 90° por segundo.

GUI conceptual:

```text
Rotation Period
[──────●────] 4.0s
```

Tooltip del valor:

> Tiempo requerido para completar una rotación de 360°.

Rango final por definir.

Rango candidato:

- mínimo 1 segundo;
- máximo 60 segundos.

Las debug builds pueden tener un rango más amplio.

## 12.3. Dirección

Opciones:

- Clockwise.
- Counter-clockwise.

## 12.4. Rotación instantánea

Botones:

- `+90°`
- `+180°`

Opcionalmente:

- Reset.

Esto cambia el offset inicial/orientación sin alterar la animación.

---

# 13. Flotación / Bobbing

La proyección debe poder moverse verticalmente para evitar verse como una textura rígida suspendida.

## 13.1. Toggle

```text
Floating
[x] Enabled
```

## 13.2. Amplitud

La amplitud se expresa en **pixeles Minecraft**.

Conversión:

- 16 px = 1 bloque.

El límite máximo **no es global**.

Depende de:

1. margen físico del chassis;
2. envelope;
3. altura actual de la proyección;
4. potencia del núcleo.

### Compact Projector

Margen de referencia:

- 1 px.

Por lo tanto, con la posición inicial del modelo:

- amplitud máxima hacia abajo: 1 px;
- nunca debe atravesar el pedestal.

### Projectors grandes

Un Tall, Field o Colossal puede poseer mucho más margen.

Ejemplos posibles:

- 4 px;
- 8 px;
- 16 px;
- 32 px;
- incluso varios bloques en una proyección monumental si el diseño y rendimiento lo permiten.

## 13.3. Movimiento suave

El movimiento nunca debe saltar por pixels.

La unidad de configuración es pixel, pero el renderer interpola continuamente.

Ejemplo:

```text
0.0 px
0.1
0.2
...
1.0
...
0.0
```

## 13.4. Anchored Bob

Comportamiento inicial solicitado:

```text
Anchor
  ↓ suave
Anchor - amplitude
  ↑ suave
Anchor
```

Es decir:

- baja la cantidad configurada;
- vuelve a la posición base;
- repite.

El anchor es la posición elegida por el usuario.

Esto evita necesitar espacio simétrico arriba y abajo del punto inicial.

Un modo de oscilación centrada puede estudiarse después, pero no es requisito inicial.

---

# 14. Floating Mode: Time

Movimiento independiente de la rotación.

Controles:

```text
Floating Mode
Time

Amplitude
1 px

Cycle
2.0s
```

Interpretación:

- baja durante una parte del ciclo;
- vuelve durante la siguiente;
- transición suave.

Default candidato del Compact:

- amplitud 1 px;
- ciclo total 2 segundos.

---

# 15. Floating Mode: Rotation Synced

El bobbing puede sincronizarse con el ángulo de rotación.

Controles:

```text
Floating Mode
Rotation Synced

Amplitude
2 px

Vertical Leg
90°
```

Interpretación:

- durante los primeros 90° se desplaza verticalmente `Amplitude`;
- durante los siguientes 90° vuelve;
- el patrón se repite.

Ejemplo:

```text
0°    → anchor
90°   → anchor - amplitude
180°  → anchor
270°  → anchor - amplitude
360°  → anchor
```

Todo interpolado.

El valor angular no debe confundirse con segundos.

Tooltip de `90°`:

> Cantidad de grados recorridos antes de alcanzar el siguiente extremo del movimiento vertical.

La velocidad real del bobbing depende naturalmente de la velocidad de rotación.

---

# 16. Altura / Projection Lift

La opción de elevar la proyección **no pertenece sólo a Banner Mode**.

Debe funcionar con todas las fuentes compatibles:

- imágenes;
- items;
- banners;
- Prism;
- Effigy;
- GIF animado (implementado desde dev.39);
- multi-display.

GUI conceptual:

```text
Projection Height
[──────●──────] 3.5 blocks
```

o, para precisión:

```text
56 px
```

La GUI puede mostrar ambos:

```text
56 px / 3.5 blocks
```

## 16.1. Límite

La altura máxima depende principalmente de:

- core power;
- chassis;
- geometría;
- seguridad/obstrucciones.

La distancia vertical consume presupuesto.

Un Glass Core no debería levantar una proyección gigantesca varios bloques.

Un Netherite Core en un Colossal Projector sí podría.

---

# 17. Escala

Todo source que pueda escalar debe exponer Scale.

Para imágenes:

- width/height manteniendo ratio.

Para banners:

- escala de tela.

Para items:

- escala uniforme.

Para Effigy:

- escala uniforme del rig completo.

Para Prism:

- dimensiones del prisma respetando límites.

En la primera línea dev, el Compact Projector tendrá sliders de debug con límites artificialmente elevados para probar:

- 1 bloque;
- 3 bloques;
- 5 bloques;
- 8 bloques;
- 10 bloques.

Eso no significa que el Compact final vaya a soportarlos.

Es un laboratorio del renderer.

---

# 18. Detección de obstrucciones

La GUI debe poder advertir si la proyección será bloqueada o clippeada por geometría del mundo.

## 18.1. Imagen quieta

Calcular el bounding box del plano.

## 18.2. Imagen rotando

Calcular el volumen barrido durante 360°.

Para una imagen de ancho `W`, al rotar alrededor de Y:

- se necesita aproximadamente radio `W/2`;
- se revisa el volumen de giro.

## 18.3. Resultado visual

GUI:

```text
Projection Clearance
✓ Clear
```

o:

```text
Projection Clearance
! 3 blocks intersect the projection
```

## 18.4. Preview en mundo

Mejora futura recomendada:

- dibujar outline temporal;
- verde para espacio libre;
- rojo para intersección;
- mostrar exactamente qué bloques interfieren.

No debe impedir siempre colocar la proyección; podría ser advertencia configurable salvo casos físicamente inválidos.

---

# 19. GUI general

La GUI debe centralizar la configuración.

Propuesta conceptual:

```text
┌─────────────────────────────────────────┐
│ MIRAGE PROJECTOR                        │
│                                         │
│              [ PREVIEW ]                │
│                                         │
│ Source                                  │
│ [ Import Image... ]                     │
│                                         │
│ Item                                    │
│ [ slot ] [✓ Use Item]                   │
│                                         │
│ Geometry / Appearance                   │
│ Scale       [────────●]                 │
│ Height      [────●────]                 │
│ Back Face   [ Mirrored ▼ ]              │
│                                         │
│ Rotation                                │
│ [x] Enabled                             │
│ Period      [────●────] 4.0s            │
│ Direction   [ Clockwise ▼ ]             │
│ [ +90° ] [ +180° ]                      │
│                                         │
│ Floating                                │
│ [x] Enabled                             │
│ Mode        [ Time ▼ ]                  │
│ Amplitude   [──●──────] 1px             │
│ Cycle       [────●────] 2.0s            │
│                                         │
│ [Apply] [Cancel]                        │
└─────────────────────────────────────────┘
```

No todas las opciones deben aparecer si no corresponden al source.

---

# 20. Empty State

Cuando no existe ninguna fuente cargada:

- debe flotar un sprite/render de un libro vanilla;
- el libro actúa como señal visual de interacción;
- puede girar;
- puede hacer bobbing.

Click:

- sobre el pedestal;
- o sobre la zona del libro;

abre la misma GUI.

En una primera implementación, detectar click exacto en el libro puede llegar después del click de pedestal si complica hitbox dinámica.

Objetivo final: ambos.

---

# 21. Item Mode

Item Mode is a **3D source family**, not a textured Plane.

The projector has a dedicated **Virtual Snapshot Slot**:

```text
[ Virtual Snapshot Slot ]  <- click / shift-click a real item here
             |
             +--> serialized one-count render snapshot + snapshot UUID

real item -> stays in the player's inventory/equipment
```

The slot does **not** accept ownership of the real item. It captures a one-count copy of the ItemStack state for rendering, assigns that capture its own projection UUID, and leaves the source stack untouched. The world renderer must draw the **complete object floating in space** from that non-obtainable snapshot.

There is no universal UUID attached to every Minecraft ItemStack, so Mirage Projector must not pretend the source item already has one. The stable identifier is a **Mirage snapshot UUID generated at capture time**. Re-capturing the same physical object intentionally creates a new snapshot identity.

## 21.1. Core rendering contract

For ordinary items the current baseline uses the normal Minecraft/NeoForge item renderer. The intent is to preserve:

- the complete item model rather than a cropped inventory icon;
- enchantment glint;
- custom model data/components;
- damage-state model changes;
- vanilla and modded block models;
- ordinary custom item models supplied through the normal item rendering pipeline.

Examples:

- a stone block must appear as a full 3D cube, not as a 2D inventory square;
- a sword, pickaxe, wrench or gun-like item must appear as its complete model;
- a modded BlockItem should use its normal 3D baked block/item model when available;
- a shield/trident/tool should rotate as the object itself, not as an image pasted onto a plane.

The current dev renderer uses `ItemRenderer.renderStatic` with `ItemDisplayContext.FIXED`. That is the baseline for generic Item Mode and is already enough for ordinary blocks/items whose standard baked model is the desired display.

## 21.1.1. Snapshot ownership and immutability contract

An Item snapshot is **render data, not an inventory**. This rule is permanent.

Capture behavior:

1. the player clicks or shift-clicks a source ItemStack into the virtual slot;
2. Mirage copies exactly one unit of that stack, including the serialized ItemStack component state needed to reproduce its appearance;
3. Mirage generates a new snapshot UUID;
4. the source stack is not shrunk, moved, locked or stored by the projector;
5. the copied stack cannot be extracted, cloned through the slot, dropped by the projector, piped out or used for crafting;
6. clearing/replacing the snapshot destroys only the virtual reference.

This means a player can capture a fully enchanted/trimmed Netherite item, immediately equip/use the original again, and the projector keeps showing the captured appearance. If the real object is later repaired, renamed, re-trimmed or otherwise changed, the old projection does **not** update automatically: the player must capture it again to create a new snapshot.

The snapshot should preserve every ItemStack component that survives normal ItemStack serialization. Vanilla enchantments, trim data, dye, custom name, damage state and custom model data therefore travel with the copy. A mod whose renderer depends on external runtime state that is not serializable inside the ItemStack may require a compatibility fallback.

Security consequence: opening another player's projector no longer exposes a physical sword/armor/tool to theft, because no such item exists inside the projector. Access control is a separate future concern: another player may still be able to **replace the visual snapshot** unless ownership/permissions are added, but they cannot take the original equipment from it.

### dev.11 migration

dev.11 and older development worlds used a real stored ItemStack. dev.12 migrates that old stack into two roles:

- a non-obtainable virtual snapshot used for rendering from then on;
- a hidden legacy return copy representing the real pre-dev.12 stored item, which is dropped when the projector is broken so test worlds do not silently lose equipment during the transition.

New captures never create this legacy return item.

## 21.2. Item Mode is not image geometry

This distinction is permanent:

### Image source

An image lives on one or more **2D faces**.

When a two-face Plane rotates:

```text
front image -> edge -> back image -> edge -> front image
```

When a future four-face Prism rotates:

```text
North -> East -> South -> West -> North
```

The faces belong to the rotating projector geometry.

### Item source

An item is a **single 3D object at the projection origin**.

It does not have North/East/South/West image faces and it must not switch to a different sprite every 90 degrees. The entire model rotates continuously around its own projection axis.

This remains true even when Item Mode is hosted by a Prism-capable chassis: the Prism's face count is an Image/Banner geometry concept. A 3D Item source remains one volumetric object unless a future explicit Multi-Item layout is selected.

## 21.3. Standalone armor-piece rule

A piece of equippable armor is a special case.

If a helmet/chestplate/leggings/boots item is placed in ordinary Item Mode, the **final intended presentation is the equipped armor geometry for that one piece**, not the flat inventory/hand representation.

Examples:

- Netherite Helmet -> helmet shell floating in the shape it has when worn;
- trimmed chestplate -> equipped chest/arm armor geometry with trim;
- dyed leather leggings -> equipped leggings geometry with dye;
- modded diving helmet -> its equipped model when the mod exposes a compatible NeoForge equipment/armor renderer.

This is not yet considered complete in dev.12. Generic 3D Item Mode and virtual capture exist; equipped-piece armor rendering needs the same humanoid/equipment pipeline that Effigy Mode will use. Until that pipeline lands, armor items use the generic ItemRenderer fallback.

The implementation target is to reuse NeoForge's armor/equipment hooks instead of rebuilding vanilla armor textures manually, so trims, dyes and compatible modded armor remain correct.

## 21.4. Shared transforms for 3D sources

Item Mode must support the same global presentation controls as image projection where they make physical sense:

- Scale;
- Projection Lift;
- Rotation ON/OFF;
- rotation period;
- clockwise/counter-clockwise;
- orientation offset;
- Floating ON/OFF;
- float amplitude;
- Time / Rotation-synced float modes;
- Fullbright / World Light where the renderer supports it;
- Tint only where applying tint does not destroy the source renderer's own colors/material logic;
- **Ghost Effect / Transparency**.

Rotation and floating act on the complete 3D model.

Ghost Effect for generic ItemRenderer content is a required feature, but dev.11 still keeps Item Mode opaque until there is a safe alpha-aware buffer path that preserves glint and modded render layers without contaminating shared render state.

Scanlines are currently an Image-only presentation effect. They are not implicitly applied to 3D items/armor unless a future dedicated volumetric hologram shader makes that visually and technically correct.

## 21.5. Item compatibility fallback

Compatibility order should be:

1. use the normal vanilla/NeoForge render path appropriate to the item;
2. if the item has a compatible custom model/equipment hook, use it;
3. for standalone armor, prefer equipped-piece geometry once the equipment renderer exists;
4. if a mod uses an inaccessible/nonstandard renderer, fall back to its ordinary item model;
5. never crash the projector because one modded item cannot provide a specialized display.

A fallback should be visible and usable, even if it is less faithful than the equipped model.

---

# 21A. Projection source families — permanent distinction

Mirage Projector must keep three rendering families conceptually separate even if they share Scale/Lift/Rotation/Floating settings:

| Family | Geometry | Multiple faces? | Typical source |
|---|---|---|---|
| Image / Banner | 2D face geometry | yes | PNG/JPG/WebP, banner cloth |
| Item | one 3D object | no face switching | block, tool, weapon, single armor piece |
| Armor / Effigy | one posed 3D humanoid equipment rig | no face switching | 1-4 armor pieces + two hands |

This prevents future Prism/multi-face work from accidentally treating a 3D sword, block or armor set like a texture card.

---

# 22. Banner Mode

Banner es una familia de source especial de geometría 2D. **Primera pasada funcional implementada en dev.28.**

Contrato actual:

- sólo se proyecta la **tela**, sin poste ni travesaño;
- se conserva color base y `BANNER_PATTERNS`;
- el banner real nunca sale del inventario: Mirage guarda un snapshot virtual de apariencia;
- Plane usa una fuente **Front**;
- Prism usa fuentes independientes **North / East / South / West** y puede copiar North a las demás;
- Scale conserva la proporción vanilla 20×40;
- Lift, Rotation, Floating, Lighting, Tint y Ghost son ajustes globales compartidos;
- Power de Prism se calcula con el número real de caras Banner pobladas.

Queda como extensión futura la animación mediante GIF/import visual alternativo y un preview de tela más rico dentro del workspace.

Casos de uso ya cubiertos por el renderer:

- cuatro banners flotando en un Mirage Prism;
- levantados varios bloques sobre una torre;
- rotando lentamente.

---

# 23. Multi-Projection

Projectors con suficiente chassis y power podrán tener múltiples slots.

Ejemplos:

```text
[1]
```

```text
[1] [2] [3] [4]
```

```text
[1] [2] [3]
[4] [5] [6]
```

Layouts candidatos:

- Horizontal.
- Vertical.
- Grid.
- Manual avanzado futuro.

Spacing:

- configurable;
- en pixels/bloques;
- sujeto al envelope.

Cada slot puede tener su propio source si el chassis lo permite.

Ejemplo de uso:

- cuatro ilustraciones de personajes juntas sobre el mismo pedestal.

---

# 24. Mirage Prism en detalle

El Prism posee cuatro caras laterales.

Configuración:

```text
North  [source]
East   [source]
South  [source]
West   [source]
```

Opciones:

- Same Source on All Faces.
- Independent Faces.

Debe poder:

- rotar;
- hacer bobbing;
- elevarse;
- escalar;
- usar banners;
- usar GIFs animados (implementado desde dev.39).

El techo y piso permanecen abiertos.

Esto evita que parezca un bloque sólido.

---

# 25. Effigy / Armor Mode en detalle

Armor Mode (también llamado **Effigy Mode** en los chassis dedicados) no es una variante visual de Item Mode: es un renderer 3D compuesto y posable.

## 25.1. Slots

El rig objetivo tiene seis slots:

```text
        [ Head ]

       [ Chest ]

        [ Legs ]

        [ Feet ]

[ Main Hand ]   [ Off Hand ]
```

Reglas:

- Head/Chest/Legs/Feet aceptan equipables compatibles con ese slot;
- Main Hand y Off Hand aceptan cualquier `ItemStack` renderizable;
- las manos pueden contener dos espadas, dos escudos, espada+escudo, dos bloques, tridente+totem, herramientas modded, etc.;
- Chest puede aceptar chestplate o alternativas equipables como Elytra cuando el pipeline vanilla/modded lo permita;
- no se requiere llenar los cuatro slots de armadura: 1, 2, 3 o 4 piezas son válidas.

## 25.2. Qué se renderiza

No debe aparecer:

- armor stand de madera;
- poste;
- placa de piedra;
- cuerpo/skin de jugador visible.

Debe existir un **rig humanoide invisible** utilizado sólo como estructura de pose y como contexto para los renderers de equipamiento.

Sobre ese rig se dibujan:

- las piezas de armor/equipment usando su geometría equipada;
- trims;
- dyes;
- enchantment glint;
- Elytra y equipables especiales compatibles;
- Main Hand / Off Hand con su contexto de tercera persona correspondiente.

La meta de compatibilidad NeoForge es reutilizar los hooks de armor/equipment del item para que una armadura modded pueda devolver su propio `HumanoidModel` cuando corresponda. Un mod que use un renderer totalmente privado/no reutilizable recibe fallback seguro.

## 25.2.1. Armor/Effigy uses six virtual snapshot channels

Armor Mode must never become a six-slot chest. Its future GUI slots are virtual capture targets that reuse the same dev.12 snapshot contract as Item Mode.

Logical channels:

```text
HEAD       -> snapshot UUID + ItemStack copy
CHEST      -> snapshot UUID + ItemStack copy
LEGS       -> snapshot UUID + ItemStack copy
FEET       -> snapshot UUID + ItemStack copy
MAIN_HAND  -> snapshot UUID + ItemStack copy
OFF_HAND   -> snapshot UUID + ItemStack copy
```

A player can therefore capture, for example:

- four pieces of fully enchanted/trimmed Netherite armor;
- a Netherite Hoe in Main Hand;
- a Netherite Block, shield, second weapon, tool or any other renderable ItemStack in Off Hand;

and then immediately put all six real objects back on the player or into normal storage. The Effigy continues to display the frozen visual state of the snapshots.

Each channel has its own snapshot UUID because each capture can be replaced independently. A future compound Effigy UUID may identify the whole pose/loadout preset, but it must not replace the per-slot identities.

Armor/Effigy should additionally expose **Capture Equipped Loadout**: snapshot Head, Chest, Legs, Feet, Main Hand and Off Hand directly from the player in one operation without unequipping or consuming anything. This is the canonical "flex the gear I am actually wearing" workflow; the same real armor/tools remain immediately usable after capture.

Pose is **not** stored inside the item snapshots. Armor/tool appearance snapshots and rig pose are separate data domains: changing Neutral -> Guard -> Hero changes the virtual mannequin transform without re-capturing any item.

The UI must make these channels visually unmistakable with equipped-style slot icons, item thumbnails and short snapshot IDs. No future armor GUI may require the real equipment to remain inserted after capture.

## 25.3. Standalone armor vs Armor Mode

Hay dos casos distintos:

### Item Mode + una pieza de armadura

Renderizar **sólo esa pieza** con su forma equipada y un anchor neutral del slot correspondiente.

No inventar el resto del cuerpo.

### Armor Mode / Effigy

Renderizar un conjunto coherente de hasta cuatro piezas + dos manos sobre el mismo rig y pose.

Así un casco solo puede ser un objeto decorativo independiente, mientras que casco+peto+pantalones+botas+espada+escudo forman una estatua/figura completa.

## 25.4. Poses

La GUI debe mostrar un botón de pose cuando Armor Mode esté activo.

Comportamiento objetivo:

```text
Pose: Standing
[ Change Pose ]
```

Cada click avanza al siguiente preset y actualiza preview + mundo.

La idea visual toma como referencia las poses configurables del armor stand vanilla/redstone, pero el rig no es un armor stand visible.

Presets iniciales candidatos:

- Standing / Neutral;
- Guard;
- Hero;
- Combat;
- Raised Main Hand;
- Raised Off Hand / Shield;
- Dual Wield;
- Crossed / Display;
- custom pose editor futuro.

Cada preset almacena rotaciones independientes para:

- head;
- body;
- left arm;
- right arm;
- left leg;
- right leg.

Los items de mano siguen las transformaciones de los brazos/manos; no flotan independientemente del rig salvo que un futuro modo lo pida explícitamente.

## 25.5. Transformaciones globales

Después de construir la pose local, todo el Effigy recibe las transformaciones del Mirage como una unidad:

1. Scale;
2. Projection Lift;
3. Float/bobbing;
4. Rotation/orientation;
5. Lighting;
6. Ghost Effect / Transparency.

Por lo tanto, si el Effigy gira, **giran juntos** casco, armor, Elytra, espada, escudo y cualquier otro item equipado.

La rotación nunca debe reinterpretar el Effigy como una secuencia de caras 2D.

## 25.6. Ghost Effect en Armor Mode

Ghost Effect debe afectar al conjunto completo:

- armor base;
- trim layers;
- leather dyes;
- glint;
- Elytra;
- hand items;
- renderers de equipamiento modded compatibles.

Esto exige un pipeline alpha-aware por buffers/vertex consumers; no basta con cambiar un shader global y restaurarlo después, porque múltiples capas pueden dibujarse en batches distintos. La implementación queda bloqueada hasta tener esa ruta común de transparencia para 3D Item/Effigy.

## 25.7. Escala y volumen

Effigy usa una relación 3D propia hardcodeada por el rig humanoide. El usuario controla **Scale uniforme**, no Width/Height independientes.

El chassis limita:

- escala máxima;
- lift;
- amplitud de float;
- volumen barrido al rotar;
- potencia necesaria.

En chassis adecuados debe ser posible pasar desde una figura cercana al tamaño de un jugador hasta una estatua monumental de varios bloques de altura. El objetivo extremo Colossal sigue siendo aproximadamente una figura de ~10 bloques si Power, chassis y performance lo permiten.

## 25.8. Clearance para Effigy

Clearance no puede usar el AABB plano de Image Mode.

Debe considerar:

- ancho del rig;
- altura escalada;
- profundidad real;
- armas largas;
- escudos/Elytra;
- pose seleccionada;
- volumen barrido por rotación;
- amplitud de bobbing.

Primera implementación puede ser conservadora; después puede usar bounds por parte/pose para reducir falsos positivos.

## 25.9. Estado de implementación

Dev.12 deja este contrato **documentado y en lista de espera**. El almacenamiento virtual ya está resuelto por Item Mode, pero faltan dos prerequisitos 3D que deben resolverse bien antes de activar Armor Mode:

1. renderer de armor-piece equipado que reutilice correctamente hooks vanilla/NeoForge y modded;
2. transparencia alpha-safe para ItemRenderer/armor/glint/múltiples render layers.

No se agregan slots de Armor Mode a la GUI hasta que el renderer pueda mostrar lo que el jugador puso ahí; evitamos crear un modo seleccionable que todavía sólo enseñe iconos o fallbacks incorrectos. El contrato de captura/seguridad también se mantiene resumido en `docs/ITEM-ARMOR-SNAPSHOT-CONTRACT.md`.

---

# 26. GIF / Animated Image — histórico (SUPERSEDED por dev.39)

Esta sección describe la intención previa a dev.39 y ya no gobierna la implementación. El contrato vigente está en `docs/GIF-ANIMATED-IMAGE-dev39.md`.

Flujo previsto:

```text
GIF
 ↓
decode frames
 ↓
validate
 ↓
resize
 ↓
normalize frames
 ↓
store
 ↓
sync
 ↓
playback
```

Límites necesarios:

- resolución máxima;
- tamaño total;
- cantidad máxima de frames;
- FPS máximo;
- duración máxima;
- memoria máxima por projector;
- cantidad máxima de animated projections visibles.

Ejemplo ideal:

- Mirage Prism masivo;
- cuatro GIFs de banners ondeando;
- rotación lenta del conjunto.

---

# 27. Multiplayer: requisito fundamental

Una ruta local como:

```text
C:\Users\User\Pictures\image.png
```

**jamás** debe ser la fuente de verdad del projector.

Eso sólo funciona en el PC que importó el archivo.

Arquitectura final:

```text
Cliente selecciona archivo
        ↓
cliente decodifica
        ↓
normaliza
        ↓
valida límites
        ↓
calcula SHA-256 / asset id
        ↓
sube asset al servidor
        ↓
servidor valida
        ↓
servidor almacena en el mundo
        ↓
BlockEntity referencia asset id
        ↓
otros clientes solicitan asset si no está cacheado
        ↓
servidor lo envía
        ↓
cliente cachea y renderiza
```

---

# 28. Privacidad de archivos

Nunca sincronizar:

- ruta absoluta;
- nombre de usuario del sistema;
- carpeta original;
- metadatos EXIF innecesarios.

Idealmente:

- decodificar imagen;
- crear PNG nuevo;
- eliminar metadatos;
- usar hash como nombre.

Ejemplo:

```text
a84bd41f8d....png
```

---

# 29. Storage del servidor

Ubicación conceptual:

```text
world/
  mirage_projector/
    assets/
      <sha256>.png
    metadata/
      <sha256>.json
```

La ruta exacta puede ajustarse a APIs NeoForge.

Metadata posible:

- hash;
- width;
- height;
- normalized byte size;
- media type;
- uploader UUID opcional para administración;
- fecha de importación;
- frame data futuro;
- references count futuro.

No almacenar la ruta original.

---

# 30. Cache del cliente

Concepto:

```text
.minecraft/
  mirage_projector/
    cache/
      <server-id>/
        <sha256>.png
```

Separar por server evita:

- colisiones administrativas;
- confusión entre mundos;
- futuros permisos.

Un hash idéntico puede deduplicarse opcionalmente.

---

# 31. Límites de archivo

Valores iniciales candidatos:

- resolución máxima: 2048 × 2048;
- archivo normalizado máximo: 4 MiB.

Estos valores no están congelados.

Motivo:

- seguridad;
- memoria;
- tráfico;
- VRAM;
- evitar imágenes absurdas para proyecciones pequeñas.

Una imagen de 12000 × 9000 no debe permanecer así si terminará mostrándose a unos pocos bloques.

---

# 32. Transferencia de red

Los assets grandes no deben enviarse como un único payload.

Se debe usar transferencia por chunks.

Arquitectura:

```text
UploadStart
UploadChunk 0
UploadChunk 1
...
UploadFinish
```

Servidor:

- valida tamaño declarado;
- limita cantidad de chunks;
- limita sesiones simultáneas;
- timeout;
- verifica hash final;
- rechaza payloads maliciosos.

Cliente:

- espera ACK;
- evita volver a subir un asset ya presente;
- muestra progreso en GUI.

Descarga:

```text
AssetRequest
AssetChunk...
AssetComplete
```

La primera dev puede implementar una etapa local antes de completar este pipeline, pero la arquitectura final no puede depender de paths locales.

---

# 33. Seguridad multiplayer

Requisitos:

- verificar distancia del jugador al projector antes de aceptar cambios;
- verificar que el bloque siga existiendo;
- verificar permisos;
- limitar tamaño;
- limitar resolución;
- limitar cantidad de uploads simultáneos;
- no confiar en width/height enviados por cliente;
- volver a decodificar/validar server-side si es necesario;
- sanitizar IDs;
- hash real server-side;
- evitar path traversal;
- no ejecutar contenido;
- no aceptar URLs remotas en MVP;
- no aceptar archivos arbitrarios fuera de formatos visuales.

---

# 34. Permisos

Diseño futuro:

- owner;
- team;
- operators;
- public edit.

No está definido todavía qué sistema se usará.

Mínimo:

- en servers, no cualquier jugador distante debe poder editar cualquier projector.

Opciones futuras:

- vanilla permissions;
- claims integration;
- owner UUID;
- lock toggle.

---

# 35. Preview

La GUI debe mostrar preview.

Funciones:

- imagen actual;
- aspect ratio;
- orientación;
- back face;
- rotación aproximada;
- escala;
- altura;
- bobbing.

Preview avanzado futuro:

- vista 3D del projector;
- envelope;
- obstrucciones.

---

# 36. Render dinámico

La proyección no pertenece al modelo baked del bloque.

Debe usar un:

- `BlockEntityRenderer`.

Razones:

- imágenes dinámicas;
- escala variable;
- rotación continua;
- bobbing;
- multiple sources;
- items;
- equipment;
- animated images.

El JSON del bloque contiene sólo la estructura física.

---

# 37. Iluminación

Dirección de diseño:

La proyección debe sentirse holográfica pero no necesariamente emitir iluminación real al mundo.

Opciones futuras:

- Normal Lighting.
- Fullbright.
- Adjustable brightness.
- Glow-like render.

Default candidato:

- fullbright suave para imagen;
- sin colocar light blocks;
- no iluminar bloques vecinos físicamente.

Esto evita coste extra y comportamiento extraño.

---

# 38. Transparencia

PNG con alpha debe conservar transparencia.

JPEG:

- no tiene alpha;
- se renderiza rectangular salvo procesamiento manual.

WebP:

- debe conservar alpha cuando corresponda.

No agregar automáticamente fondo.

---

# 39. Render distance y culling

Projectors grandes necesitan límites especiales.

El renderer debe tener bounding box que abarque:

- escala;
- altura;
- movimiento;
- rotación.

Si no, Minecraft podría dejar de renderizar la proyección cuando el pedestal sale del frustum aunque parte del Mirage siga visible.

Colossal necesita un render bounding box mucho mayor.

Debe evitarse usar `AABB.INFINITE` permanentemente salvo debug, porque puede dañar rendimiento.

---

# 40. Performance

Riesgos:

- demasiadas DynamicTextures;
- demasiadas imágenes 2K;
- demasiados GIFs;
- demasiadas Effigies;
- demasiados projectors visibles;
- geometrías enormes;
- assets no liberados de VRAM.

Requisitos:

- texture cache;
- ref counting o lifetime control;
- unload al cambiar de world/server;
- LRU posible;
- limitar animated projections;
- evitar decodificar imagen cada frame;
- render sólo cuando visible;
- no hacer I/O en render thread.

---

# 41. Power, Lift y altura — dev.38

Lift consume PU y ya no depende de un `core_lift_limit` hardcodeado.

```text
baseLift = ceil(LiftPx / 16)
liftRatio = max(1, LiftPx / chassisNominalLift)
Lift PU = ceil(baseLift × liftRatio²)
```

El endpoint del slider se calcula buscando el mayor Lift que la Effective PU actual puede sostener junto con Scale, Float y features ya seleccionadas.

---

# 42. Power y Float — dev.38

Float consume PU sólo cuando Floating está activo:

```text
baseFloat = ceil(FloatPx / 2)
floatRatio = max(1, FloatPx / chassisNominalFloat)
Float PU = ceil(baseFloat × floatRatio²)
```

La barrera física que sí permanece es:

```text
Float amplitude <= Lift
```

El slider Float se genera hasta el menor máximo que resulte de esa condición física y el presupuesto Effective PU actual. No debe existir un tramo naranja/inutilizable que obligue a tantear pixel por pixel.

---

# 43. Core Socket

La GUI conserva un único Core Socket físico:

```text
Projection Core
[ core slot ]
```

Cambiar Core nunca borra los settings/snapshots. Desde dev.38 el cambio recalcula:

```text
Effective PU = Core base PU × chassis multiplier × Core amplification
```

y reconstruye inmediatamente los endpoints de Scale/Lift/Float.

Si una configuración antigua ya no cabe con el nuevo Core, la GUI intenta preservar el resultado reduciendo primero Float, luego Lift y Scale sólo al final. El renderer nunca debe dibujar una configuración cuyo Final load exceda Effective PU.

Los Cores actuales aceptan los materiales legacy Glass/Quartz/Amethyst/Diamond/Netherite. Futuros Improved Cores usarán el mismo socket y el multiplier `Core amplification`; sus recipes/items/textures todavía no están definidos.

---

# 44. Core visual

**Implementado en dev.8 para Compact.** El pilar 2×3×2 px ya no está hardcodeado como diamond en el block model: el BER renderiza el block material correspondiente al Core instalado (Glass/Quartz/Amethyst/Diamond/Netherite). Slot vacío = pilar material ausente + proyección apagada. No se duplican blocks/modelos por Core.

---

# 45. Crafting

Los crafts exactos NO están congelados.

Dirección:

- obsidiana como estructura;
- vidrio como capa óptica;
- componente central relacionado al core.

Compact podría construirse barato y luego recibir el core en GUI.

Los chassis grandes deben costar más.

Colossal puede requerir Netherite en la estructura, independiente del Core Socket.

---

# 46. Redstone

No forma parte del MVP, pero encaja en el producto.

Modo futuro:

```text
Redstone Behavior
Always On
Powered
Unpowered
Toggle on Pulse
```

Casos:

- sala que activa hologramas;
- cartel que aparece;
- vitrina;
- decoración Create;
- entrada secreta;
- instalación temática.

---

# 47. Item source vs copia

Decisión futura importante:

Cuando se inserte un item en el projector:

Opción recomendada:

- el ItemStack queda realmente almacenado en el projector;
- no se duplica;
- romper el projector lo devuelve.

Esto evita duplicación.

Modo "copy visual only" podría existir sólo en Creative/commands.

---

# 48. Compatibilidad con mods

Item Mode:

- usar ItemRenderer vanilla.

Effigy:

- usar rutas de equipment compatibles;
- fallback para custom renderer.

Banner:

- reutilizar patrones vanilla.

No hardcodear mods salvo compat modules necesarios.

---

# 49. Compatibilidad visual

Se debe probar:

- shaders;
- Sodium-like renderer equivalents para NeoForge;
- Embeddium si aplica;
- Oculus/Iris equivalents;
- resource packs;
- transparency;
- mipmaps.

No se promete compat perfecta en dev.1.

---

# 50. Book Empty State

Renderizar un libro vanilla flotante cuando:

```text
source == NONE
```

Animación:

- rotación;
- bobbing.

Click objetivo:

- pedestal siempre;
- libro como hit target futuro.

El libro no es un item real almacenado.

---

# 51. Estado de fuente

Enum conceptual:

```text
NONE
IMAGE
ITEM
BANNER
PRISM
EFFIGY
ANIMATED_IMAGE
```

No mezclar comportamiento mediante múltiples booleans difíciles de mantener.

---

# 52. Configuración persistente

Block Entity debe persistir:

- source type;
- asset id;
- width/height metadata;
- scale;
- projection lift;
- rotation enabled;
- rotation period;
- rotation direction;
- rotation offset;
- floating enabled;
- floating mode;
- float amplitude;
- float cycle;
- float degree interval;
- back face mode;
- flip vertical;
- source-specific settings;
- core;
- slots futuros.

---

# 53. Defaults del Compact Projector

Valores iniciales de diseño:

```text
Source: NONE
Image envelope: 10 × 10 px
Rotation: ON
Rotation period: 4.0s
Direction: Clockwise
Rotation offset: 0°
Floating: ON
Floating mode: Time
Amplitude: 1px
Cycle: 2.0s
Projection lift: 1px
Back face: Mirrored
Vertical flip: OFF
Aspect ratio: LOCKED
```

En debug:

- escala y lift pueden superar los límites finales.

---

# 54. Dev Debug Controls

Antes de crear todos los chassis, el Compact se usa como laboratorio.

Permitir temporalmente:

- scale hasta varios bloques;
- projection lift exagerado;
- amplitude mayor;
- rotation speed extrema;
- back modes;
- flip;
- huge render box.

Objetivo:

- validar el renderer antes de hacer modelos de otros projectors.

Estas opciones deben quedar claramente marcadas como debug o ser recortadas en release.

---

# 55. Primera implementación: alcance dev.1

## Implementar

- Registro del bloque.
- Registro del BlockEntity.
- Modelo físico del pedestal.
- Shape del pedestal.
- GUI básica.
- Import button.
- Import local PNG/JPG/JPEG.
- Normalización a PNG.
- Cache local por hash.
- DynamicTexture cache.
- Plane renderer.
- Aspect ratio.
- Rotation toggle.
- Rotation period.
- Clockwise/Counter-clockwise.
- Rotation offset.
- Floating toggle.
- Time bobbing.
- Amplitude.
- Projection lift.
- Scale debug.
- Back face modes en estructura de datos.
- Vertical flip.
- NBT.
- Block update sync.
- Server-authoritative settings.
- Validación de distancia para updates.

## Deliberadamente pendiente después de dev.1

- Upload real multiplayer de bytes.
- Descarga de assets.
- WebP (pendiente en dev.1; implementado en dev.9 mediante provider ImageIO embebido).
- Item slot.
- Core slot.
- power budget real.
- banner.
- Prism.
- Effigy.
- collision preview.
- GIF.
- redstone.
- other chassis.
- recipes definitivos.

La dev.1 local no se debe confundir con la arquitectura final multiplayer.

---

# 56. Primera implementación: limitación multiplayer temporal

En dev.1, el asset normalizado puede existir sólo en cache local del cliente importador.

El BlockEntity guarda:

- hash;
- metadata;
- settings.

Resultado:

- singleplayer: funcional;
- uploader en un server: puede verlo;
- otro cliente: aún no tendrá bytes.

Esto es una etapa de debug.

La siguiente oleada debe reemplazar esa limitación con el Asset Transfer Protocol documentado arriba.

---

# 57. Asset identity

Usar SHA-256 del PNG normalizado.

Ventajas:

- deduplicación;
- cache estable;
- no revelar filename;
- validación;
- mismo contenido = mismo asset.

---

# 58. WebP

WebP es requisito del alcance base y desde dev.9 forma parte del importador real.

Implementación dev.9:

- proveedor ImageIO WebP empaquetado **Jar-in-Jar** dentro del mod;
- el usuario no instala dependencias adicionales;
- `ImageIO.scanForPlugins()` fuerza el descubrimiento del provider antes del primer decode;
- el WebP seleccionado se decodifica en cliente y pasa inmediatamente por el mismo pipeline que PNG/JPEG;
- el resultado se convierte a RGBA/PNG normalizado;
- sólo ese PNG normalizado participa en SHA-256, cache, upload, server store y download.

Por lo tanto el servidor y otros clientes no necesitan conocer el formato original ni conservar `.webp`. El archivo de origen sigue siendo privado del cliente importador.

---

# 59. GIF — histórico (SUPERSEDED por dev.39)

La arquitectura final de dev.39 ya implementa decode-once + frame cache y asset genérico. Véase `docs/GIF-ANIMATED-IMAGE-dev39.md`. El siguiente esquema queda sólo como nota histórica que inspiró la implementación:

```text
<hash>/
  manifest
  frame_000.png
  frame_001.png
...
```

o contenedor interno más eficiente.

---

# 60. Obstruction + rotation

Para una imagen rotatoria:

- se calcula un cilindro/AABB conservador del radio máximo;
- bobbing extiende minY/maxY;
- projection lift desplaza el volumen.

Effigy:

- usa bounding box 3D.

Prism:

- usa volumen del prisma rotado.

---

# 61. Server configs futuros

Opciones de servidor recomendadas:

```text
max_image_resolution
max_asset_bytes
max_assets_per_player
max_assets_per_world
allow_gif
max_gif_frames
max_gif_fps
max_projection_size
allow_colossal
max_projection_height
upload_permission_level
```

No hardcodear todos los límites para siempre.

---

# 62. Limpieza de assets

Problema:

Assets ya no usados pueden quedar ocupando espacio.

Futuro:

- reference scan;
- admin command;
- "prune unused assets";
- backup safety;
- nunca borrar asset actualmente referenciado.

---

# 63. Commands futuros

Candidatos:

```text
/mirage assets list
/mirage assets prune
/mirage projector inspect
/mirage reload
```

Administración, no MVP.

---

# 64. Projector ownership

Futuro:

- owner UUID;
- lock;
- allow public;
- team access.

No mezclar seguridad de archivos con permisos de edición.

Todos pueden descargar el asset necesario para verlo, pero no necesariamente editar el projector.

---

# 65. Hologram color/tint / presentation — dev.9→dev.10

Image Mode posee una capa de presentación no destructiva. Los bytes normalizados/cacheados **no se modifican** al cambiar estas opciones; se aplican durante render y persisten como settings del projector.

Controles actuales:

- `Light: Fullbright / World`;
- `Ghost effect: 0-90%` (**transparencia**, no opacidad);
- Tint RGB;
- presets GUI: White, Cyan, Amethyst, Rose, Amber, Green y Red.

Semántica del Ghost Effect:

- `0%` = imagen completamente visible / sin efecto fantasma;
- aumentar el porcentaje aumenta la transparencia;
- `90%` = proyección extremadamente tenue pero todavía visible;
- no se permite `100%` para evitar dejar accidentalmente un Mirage totalmente invisible e imposible de diagnosticar visualmente.

Defaults:

- Fullbright: ON;
- Tint: `#FFFFFF` / Neutral;
- Ghost effect / Transparency: `0%`.

La implementación conserva internamente el campo legado `OpacityPercent` por compatibilidad de NBT/network con dev.9. La GUI convierte de forma reversible `Transparency = 100 - Opacity`; **el concepto presentado al jugador desde dev.10 es siempre transparencia**.

El preview de GUI aplica ya Tint y Ghost Effect a la DynamicTexture, además de simular Scanlines. `World Light` usa la iluminación que NeoForge entrega al BlockEntityRenderer. Fullbright conserva el comportamiento holográfico luminoso original.

La infraestructura guarda RGB directo y no sólo el índice del preset, de modo que una GUI futura puede ofrecer color completamente libre sin cambiar el formato de datos.

Por ahora el alpha configurable se aplica al Plane/Image renderer. Item Mode conserva el alpha del renderer vanilla hasta implementar una ruta segura que no contamine buffers/render state compartido.

---

# 66. Scanline / hologram effects

### Implementado en dev.9

- Scanlines ON/OFF para Image Mode.
- Se renderizan dividiendo el quad en bandas horizontales equivalentes aproximadamente a un pixel de proyección.
- Bandas alternas reducen multiplicativamente el brillo, sin modificar el PNG original.
- Máximo interno de 64 bandas por cara para que una proyección gigante no multiplique vértices sin límite.
- El efecto consume una pequeña cantidad adicional de Projection Power.
- OFF sigue siendo el default: la imagen normal limpia continúa siendo la presentación base.

### Efectos todavía opcionales/futuros

- flicker;
- chromatic separation;
- particles;
- ruido/glitch muy sutil.

No se planea obligar a usar shaders para la presentación básica.

---

# 67. Sonido

No necesario para MVP.

Opcional:

- sonido al activar;
- sonido de cambio de core;
- hum muy suave.

Evitar loops molestos.

---

# 68. Partículas

No necesarias.

Si se agregan:

- discretas;
- configurables;
- no llenar la base de partículas.

---

# 69. Color del Core

El core puede ayudar a identificar potencia.

Ejemplo visual:

- Glass: casi transparente.
- Quartz: blanco.
- Amethyst: violeta.
- Diamond: cyan.
- Netherite: oscuro.

No implica que toda la proyección cambie de color.

---

# 70. Modelos de otros chassis

Regla visual:

Todos deben conservar lenguaje común:

- base oscura/pesada;
- óptica de vidrio;
- core visible;
- zona de emisión central;
- footprint reconocible.

Pero no deben ser simplemente el mismo bloque reescalado.

Wide:

- estructura horizontal.

Tall:

- emisor reforzado vertical.

Field:

- base más ancha o marco energético.

Prism:

- cuatro emisores o geometría radial.

Effigy:

- emitter dedicado 3D.

Colossal:

- infraestructura de gran escala.

---

# 71. Modelo del Compact: decisión de renderer

El elemento `Image` del JSON de Blockbench debe eliminarse del block model final.

Motivo:

- el renderer de BlockEntity lo reemplaza.

El JSON final sólo representa:

- obsidian base;
- glass layer;
- core/emitter.

---

# 72. Recipe roadmap

Pendiente de diseño visual final.

No crear crafts caros antes de saber:

- power curve;
- envelopes;
- cores.

La dev build puede no tener recipe y aparecer en creative tab.

---

# 73. Creative tab

Durante desarrollo:

- insertar Mirage Projector en Functional Blocks o tab propia.

Tab propia sólo si el mod crece lo suficiente.

---

# 74. Break/drop

Al romper un projector:

- debe dropear el bloque.
- Item/Core almacenado futuro debe devolverse.
- configuraciones pueden o no conservarse en item mediante data components; decisión futura.

Si se conserva asset reference en el item:

- no duplicar bytes;
- sólo referenciar asset existente en el world.

Cross-world export requeriría otro mecanismo.

---

# 75. Copy/Paste settings

Idea futura:

- wrench/config card;
- copy projector settings;
- paste a otro.

Debe distinguir:

- settings visuales;
- asset references;
- inventory.

No MVP.

---

# 76. Blueprint integration

Idea futura si tiene sentido:

- Create schematic puede copiar el bloque;
- asset reference debe resolverse.
- No asumir compat automática.

---

# 77. Power vs redstone

Projection Power no significa energía FE.

Inicialmente:

- core = capacidad interna;
- no requiere Forge Energy.

Una integración de FE sería una filosofía diferente y no está planeada.

---

# 78. Transparencia del vidrio del block model

El bloque debe usar `noOcclusion`.

Se debe configurar render layer adecuado si el modelo usa vidrio real.

Evitar caras negras o culling raro.

---

# 79. Hitbox

El hitbox físico debe corresponder a la base.

No incluir la imagen flotante como collision box.

Posible selection/hit helper futuro para click directo al Mirage.

---

# 80. Click sobre proyección

Objetivo futuro:

Raycast contra projection bounds.

Si impacta:

- abrir GUI;
- o permitir interacción source-specific.

No debe bloquear combat/mining accidentalmente si el holograma es enorme.

Por eso posiblemente:

- click projection sólo con mano vacía;
- o configurable;
- o sólo cerca.

Pendiente.

---

# 81. Projection visibility

Idea futura:

- always visible;
- redstone;
- distance fade;
- owner-only no, porque rompe decoración multiplayer.

No MVP.

---

# 82. Distance fade

Para Colossal puede ser útil:

- evitar pop-in duro;
- fade opcional.

No requisito inicial.

---

# 83. Render sorting

Planes transparentes rotatorios pueden tener problemas de orden.

Se debe probar:

- alpha;
- double-sided;
- multiple projections;
- glass nearby.

---

# 84. Independent Faces implementation

Para plane:

- front texture ID;
- back texture ID.

Si back no está asignada:

- fallback según mode.

Modes:

```text
MIRRORED
READABLE
INDEPENDENT
```

INDEPENDENT exige segundo asset.

---

# 85. Multi-image metadata

Cada slot debe tener:

- asset ID;
- local transform;
- face behavior;
- scale;
- source type.

Global projector settings pueden aplicar a todos.

Futuro avanzado:

- per-slot settings.

---

# 86. Global vs per-slot animation

Default multi:

- rotation global;
- float global;
- mantiene composición junta.

Modo futuro:

- animate slots independently.

No MVP.

---

# 87. Prism rotation

Prism rota alrededor de eje Y central.

Cada cara conserva su orientación relativa.

Top/bottom no existen.

---

# 88. Effigy rotation

Effigy rota como figura completa.

Opcional:

- lock facing;
- manual yaw.

Bobbing mueve rig completo.

---

# 89. Projection Height en Effigy

El anchor es la base/pies de la figura.

No el centro.

Así elevar una estatua 5 bloques tiene comportamiento intuitivo.

---

# 90. Projection Height en Image

El anchor se define respecto al borde inferior del plano.

Compact:

- base top = Y5;
- lift = 1 px;
- image bottom = Y6.

Esto replica el modelo Blockbench.

---

# 91. Float Direction

Default original:

- desde anchor baja;
- luego vuelve.

Si el projector necesita preservar clearance debajo:

- el rango máximo se recorta.

Futuro:

- Up only;
- Down only;
- Centered.

No necesario para MVP.

---

# 92. Rotación y float al pausar

Animación debe basarse en game time + partial tick.

No `System.currentTimeMillis()` puro si queremos consistencia con pause/ticks.

En multiplayer:

- usar level game time como base;
- todos los clientes ven fase semejante.

---

# 93. Phase synchronization

Guardar un phase seed/start tick si queremos que una edición reinicie animación.

O usar:

```text
(gameTime + offset) % period
```

MVP puede usar gameTime.

Futuro:

- reset animation button.

---

# 94. Server-authoritative settings

GUI edita localmente preview.

Al presionar Apply:

- enviar payload;
- server valida;
- server cambia BlockEntity;
- server sync a tracking clients.

Nunca modificar world state sólo en cliente.

---

# 95. Native file picker

El botón Import Image debe abrir selector del sistema.

No pedir al usuario copiar manualmente rutas.

Filtros:

- PNG
- JPG
- JPEG
- WebP
- GIF.

La GUI no debe almacenar la ruta.

---

# 96. File import errors

Mensajes claros:

- Unsupported format.
- File too large.
- Resolution too large.
- Decode failed.
- Empty image.
- Upload rejected.
- Server limit exceeded.

No crash.

---

# 97. Import progress

Cuando multiplayer upload llegue:

```text
Normalizing...
Uploading 45%
Verifying...
Done
```

No congelar main thread durante archivos grandes.

---

# 98. Asset dedup

Antes de upload:

- cliente manda hash;
- server responde si ya existe.

Si existe:

- sólo asignar asset.

Ahorra tráfico.

---

# 99. Chunk transfer timeout

Sessions deben expirar.

No dejar arrays gigantes retenidos si un cliente se desconecta a mitad.

---

# 100. MVP success criteria

El primer objetivo visual se considera logrado cuando:

1. colocamos Compact Mirage Projector;
2. abrimos GUI;
3. elegimos una imagen local;
4. se ve flotando sobre el pedestal;
5. conserva aspect ratio;
6. rota suavemente;
7. completa 360° en 4 segundos por default;
8. bobbing default de 1 pixel sin saltos;
9. puede elevarse;
10. puede cambiar escala en debug;
11. persiste después de cerrar/reabrir mundo;
12. no forma parte del JSON estático.

---

# 101. Roadmap de desarrollo

La numeración original de milestones dejó de coincidir con los `dev.N` reales porque dev.2-dev.6 absorbieron toolchain, baseline y correcciones visuales. Desde dev.7 el roadmap se sigue por **oleadas funcionales**, no por los nombres antiguos.

## Completado hasta dev.7

### Plane/Image foundation
- Compact block.
- BER.
- GUI.
- local normalized asset cache.
- PNG/JPEG import.
- rotation.
- floating.
- lift.
- scale debug.
- persistence.
- settings packet.
- independent front/back images.
- true one-visible-face rendering.

### Multiplayer asset transport — first complete pass
- chunked client -> server upload;
- 32 KiB network chunks;
- 4 MiB normalized asset cap;
- SHA-256 identity/validation;
- PNG signature validation server-side;
- world-local server asset store;
- dedup by hash;
- client request when texture cache misses;
- chunked server -> client download;
- client-side hash verification before cache write;
- retry throttling and stale session cleanup.

### Item Mode — historical dev.7 first pass (superseded by dev.12)
- dev.7 originally used a dedicated physical projector ItemStack slot;
- Image/Item source toggle;
- fullbright vanilla ItemRenderer projection;
- enchantment glint and ordinary modded item model path preserved;
- scale/lift/rotation/float shared with Image Mode.

> **Superseded:** since dev.12 the projected object is a non-obtainable virtual snapshot. New captures never transfer ownership of the real ItemStack to the projector and therefore never drop a captured item when the projector is destroyed. The one exception is the hidden migration-return stack for a real item that was already physically stored by dev.11 or older.

### Projection clearance — first pass
- conservative projection envelope scan;
- image/item aware;
- lift and bob amplitude included;
- GUI warning with number of intersecting non-air blocks.

## Oleada completada en dev.8 — Core / Power

> **HISTÓRICO / SUPERSEDED POR dev.38:** la lista siguiente describe lo que dev.8 implementó en ese momento. Los hard caps de Core/chassis ya no forman parte del sistema vigente; ver sección 6 y `docs/POWER-SYSTEM-REWORK-dev38.md`.

- core slot;
- material profiles: Glass / Quartz / Amethyst / Diamond / Netherite;
- power budget;
- limits for scale, lift, float amplitude and future slot count;
- dynamic core visual in the pedestal;
- Compact hard envelope;
- Creative-only debug chassis override;
- non-destructive invalid configuration behavior.

## Oleada completada en dev.9 — Image completeness / preview / presentation

Estado actual adicional de clearance: una Plane estática usa un AABB delgado orientado según su offset; una Plane con rotación usa el volumen barrido conservador. El overlay in-world dibuja envelope y hasta 128 bloques obstructores.


- WebP local mediante proveedor ImageIO embebido;
- toda imagen, incluido WebP, sigue normalizándose a PNG antes de hash/cache/red;
- preview de la fuente dentro de la GUI, conservando aspect ratio; dev.10 aplica Tint + Ghost Effect también al preview; las scanlines se simulan en el mismo panel;
- preview de Item Mode dentro de la misma zona;
- world-space projection envelope mientras se edita;
- outline rojo de bloques que intersectan el envelope;
- Fullbright / World Light para imágenes;
- Ghost Effect / Transparency de 0-90% (almacenamiento legacy como `OpacityPercent` para compatibilidad);
- presets de Tint con soporte RGB persistente en settings;
- Scanlines opcionales renderizadas como tiras horizontales separadas por huecos transparentes (8-64 segmentos);
- feedback de download;
- progreso de envío + ACK final de upload servidor -> cliente, con reintento manual desde la GUI;
- `ProjectionChassisProfile` centraliza los contratos provisionales de Compact/Display/Wide/Tall/Field/Prism/Effigy/Colossal;
- settings/networking/NBT extendidos sin destruir configuraciones antiguas.

## Oleada completada en dev.10 — Ghost Effect + first physical chassis family

### Ghost Effect / Transparency

- la GUI deja de exponer el concepto inverso `Opacity`;
- slider `Ghost effect: 0-90%` donde aumentar el valor hace la proyección más transparente;
- 0% es totalmente visible y 90% es el máximo fantasma permitido;
- el renderer sigue usando alpha internamente, pero NBT/network conservan `OpacityPercent` para no romper mundos dev.9;
- el preview 2D aplica Tint y transparencia reales mediante el render state de 1.21.1 y restaura el shader color inmediatamente después;
- Item Mode sigue opaco deliberadamente hasta disponer de un alpha path seguro para ItemRenderer.

### Chassis físicos compartidos

> **HISTÓRICO:** los números de esta tabla son los de dev.10 y no deben recuperarse como balance actual. dev.38 usa nominales 10×10, 32×32, 80×32, 32×80, 128×128 y Prism 48×48, con Overdrive en vez de hard caps.

Dev.10 registra como contenido jugable/de prueba, además de Compact:

| Chassis | Envelope Plane | Lift | Float | Sources futuras | Estado de modelo dev |
|---|---:|---:|---:|---:|---|
| Compact | 10×10 px | 32 px | 4 px | 1 | modelo original |
| Mirage Display | 16×16 px | 48 px | 6 px | 1 | modelo provisional propio |
| Wide | 80×32 px (5×2 bloques) | 64 px | 8 px | 4 | modelo horizontal provisional |
| Tall | 32×80 px (2×5 bloques) | 96 px | 12 px | 4 | modelo vertical provisional |
| Field | 80×80 px (5×5 bloques) | 96 px | 16 px | 9 | modelo de campo provisional |

Reglas implementadas:

- todos usan el **mismo** `MirageProjectorBlockEntity`, menu, networking, asset store, renderer y Core Socket;
- el tipo de chassis se deriva del block colocado, no de subclasses por tier;
- el envelope ahora valida **ancho y alto reales tras preservar aspect ratio**, no sólo la dimensión más grande;
- Wide puede rechazar una imagen muy alta aunque su Scale nominal quepa; Tall hace lo equivalente con imágenes demasiado anchas;
- `Debug chassis` sigue saltándose sólo el envelope del cuerpo y nunca los límites/power del Core;
- cada chassis tiene `physicalTopPixels` y dimensiones/posición propias para el Core visual, por lo que Lift/clearance/render comienzan desde la altura física correcta;
- los nuevos chassis aparecen en la Creative tab propia y Functional Blocks;
- Display/Wide/Tall/Field nacen con Core Socket vacío; Compact conserva Glass como default/migración histórica para no romper saves de dev.7;
- al romper cualquier chassis se devuelven Core e Item almacenado exactamente como en Compact.

Los modelos dev.10 son deliberadamente **provisionales** y distintos entre sí; fijan footprint, emitter height y lenguaje obsidian/glass/core para QA, pero no congelan el arte final. Los crafts continúan sin congelarse hasta aprobar modelos y curva final de Power.

### Pendiente de pulido del Plane, no bloqueante para nuevos chassis

- optimizar el `shouldRenderOffScreen` temporal con culling/LOD real;
- preview 3D más fiel a rotación/bobbing dentro de GUI si aporta valor;
- límites/configuración de servidor para admins;
- QA multiplayer con latencia real y archivos cercanos al máximo de 4 MiB.

## Oleada completada en dev.11 — Render-family contract + per-face thumbnails

Esta pasada congela la semántica de los tres tipos de render para que futuras expansiones no mezclen geometría 2D y 3D:

- Image/Banner = caras 2D que cambian según la orientación física de Plane/Prism;
- Item = un objeto 3D único que gira como modelo completo;
- Armor/Effigy = un rig humanoide 3D posable con hasta cuatro piezas y dos manos.

También cambia la GUI de Image Mode:

- Front y Back poseen **miniaturas simultáneas**;
- Back muestra el resultado efectivo de Mirrored / Readable / Independent;
- cada miniatura conserva aspect ratio, Tint, Ghost Effect, Vertical Flip y scanlines del preview;
- hover muestra rol de la cara, resolución y hash corto del asset;
- si Independent no tiene Back propio, la miniatura deja explícito que Front actúa como fallback;
- los futuros N/E/S/W de Prism y slots multi-source deben reutilizar el mismo patrón de thumbnail por source.

El filename local nunca se usa como identidad de red. La identidad técnica sigue siendo SHA-256; la miniatura es la identidad visual humana en la GUI, por lo que nombres ambiguos como `3j5hk4h6hj35k34h52h3i53hj4j3.png` dejan de ser un problema de orientación.

### Lista de espera 3D creada en dev.11

Antes de activar Armor Mode deben completarse:

- equipped-piece renderer para una sola pieza en Item Mode;
- rig invisible de Effigy;
- 4 armor slots + Main/Off Hand;
- pose presets y botón `Change Pose`;
- Ghost/Tint 3D local implementado en dev.17; queda QA/refinamiento de glint y custom/modded RenderTypes;
- compatibility fallback para equipables modded;
- bounds/clearance 3D por pose;
- preview 3D del Effigy.

Mientras eso se desarrolla, generic Item Mode conserva su renderer `FIXED` para bloques/items normales y ArmorItem usa fallback de item model.

## Siguiente — Chassis polish + multi-source / Entity bounds

- QA in-game de Display/Wide/Tall/Field/Prism;
- modelos finales (los chassis actuales siguen siendo prototipos funcionales);
- crafts una vez congelados modelos/power curve;
- markers/rangos visuales dinámicos de sliders según Core + chassis;
- Banner renderer without pole: **implementado en dev.28**;
- Humanoid pose presets + clearance conservador: **implementado en dev.22**; bounds exactos por especie/renderer siguen pendientes.

## Banner + Prism

- Prism static Image N/E/S/W: **implementado en dev.21**;
- banner renderer without pole: **implementado en dev.28**;
- Banner como fuente para caras Prism: **implementado en dev.28**;
- Prism rotation global: reutiliza Rotation existente desde dev.21;
- banner Scale y Projection Lift: **implementados en dev.28** mediante settings globales.

## Effigy

- equipment rig;
- armor;
- hands;
- shield / elytra;
- poses;
- scaling;
- modded equipment tests.

## Colossal

- Netherite-scale chassis;
- very large bounds;
- performance limits/LOD;
- giant planes and Effigies.

## Animated images

- GIF;
- frame limits;
- animated Prism/banner use cases.

El orden posterior a Core/Power puede cambiar según QA y complejidad.

# 102. Fuera de alcance

No planificado:

- YouTube.
- Twitch.
- streams.
- browser.
- arbitrary web pages.
- audio player.
- remote video.
- webcam.

No convertir Mirage Projector en un multimedia framework.

---

# 103. Filosofía de implementación

Prioridades:

1. Visualmente limpio.
2. Fácil de entender.
3. Multiplayer real.
4. No depender de rutas locales.
5. No hardcodear compat innecesaria.
6. Mantener fuente/geometry/core separados.
7. Conservar aspect ratio.
8. Evitar duplicación de items.
9. No perder configuración al cambiar core.
10. Escalar desde un pedestal pequeño hasta instalaciones monumentales sin rehacer la arquitectura.

---

# 104. Decisiones todavía abiertas

- Valores exactos de Power.
- Crafts exactos.
- Qué ventajas especiales tendrá cada material además de potencia.
- Cantidad final de chassis.
- Medidas exactas de cada envelope.
- Máximo final de Netherite/Colossal.
- Si existe Quartz antes o después de Amethyst en potencia.
- Si Echo Shard tendrá un perfil especial.
- Si glow/fullbright tendrá coste de power.
- Permisos multiplayer.
- Cómo se exportan assets entre mundos.
- Custom poses de Effigy.
- Si multi-slot tendrá animación individual.
- Límites GIF finales.

Todo lo anterior está documentado como área de diseño y no debe perderse entre iteraciones.

---

# 105. Referencia visual actual

El modelo Blockbench aportado ya establece varias decisiones:

- base inferior de obsidiana;
- capa de vidrio;
- pilar central;
- altura física total de 5 px;
- gap de 1 px;
- plane 10 × 10 × 0;
- front/back distintos técnicamente posibles.

El renderer final debe preservar la sensación visual del prototipo aunque el plano ya no viva dentro del block model.

---

# 106. Regla final del proyecto

La idea central a preservar durante todo el desarrollo es:

> **Mirage Projector no es un marco de fotos con animación. Es un sistema de exhibición proyectada cuya geometría depende del Projector y cuya capacidad depende del Core.**

La implementación inicial es pequeña por diseño, pero toda decisión de código debe dejar espacio para:

- imagen;
- item;
- banner;
- prism;
- effigy;
- multi-display;
- GIF;
- escala monumental.


---

# 107. Estado concreto de `0.1.0-dev.1`

Esta primera snapshot ya transforma el documento de diseño en una base de código real.

Implementado en source:

- registro del bloque compacto `mirage_projector`;
- geometría física de 5 px basada en el modelo de referencia;
- base de obsidiana, layer de vidrio y core visual de vidrio para el primer prototipo;
- BlockEntity persistente;
- GUI sin inventario para configuración visual;
- selector de archivos del sistema en cliente;
- importación PNG/JPG/JPEG;
- normalización a PNG RGBA/ARGB y eliminación implícita de metadata al re-encodear;
- límite de dimensión y tamaño normalizado;
- ID SHA-256 del asset;
- cache local por hash;
- DynamicTexture registrada en runtime;
- plane 2D dinámico manteniendo aspect ratio;
- front y back renderizados;
- `Mirrored`, `Readable` e infraestructura de `Independent`;
- rotación configurable;
- clockwise/counter-clockwise;
- offset +90°/+180°;
- vertical flip;
- float time-based;
- float rotation-synced;
- amplitud en Minecraft pixels;
- intervalo angular configurable;
- Projection Lift general, no exclusivo de Banner;
- debug scale hasta 160 px / 10 blocks;
- empty state con libro vanilla flotante;
- placeholder de papel si el bloque referencia un asset que el cliente todavía no posee;
- payload serverbound para guardar settings de forma autoritativa;
- sincronización normal de los settings del BlockEntity hacia clientes;
- límites/validación de distancia para cambios enviados por GUI.

Todavía NO implementado en esta snapshot:

- transferencia de bytes de imagen cliente → servidor;
- almacenamiento permanente de assets en el world/server;
- descarga automática de assets servidor → otros clientes;
- deduplicación server-side;
- segundo asset separado para `Independent Back Face`;
- WebP;
- Core Socket físico/lógico;
- Power budget;
- Item Mode;
- Banner Mode;
- Mirage Prism;
- Multi Projection;
- Effigy;
- Colossal;
- GIF.

La ausencia de esas funciones no cambia el diseño documentado: dev.1 está deliberadamente enfocada en validar primero **import → dynamic texture → plane → rotation/bobbing → GUI → persistence**.

---

# 108. Estado concreto de `0.1.0-dev.2`

`dev.2` mantiene todo el alcance MASTER anterior y avanza la primera implementación sin alterar la arquitectura definida.

## 108.1 Independent Back Face deja de ser sólo infraestructura

El estado de una proyección plana ahora puede conservar dos assets separados:

```text
Front image
- SHA-256
- width
- height

Back image
- SHA-256
- width
- height
```

La GUI permite importarlos de forma independiente.

Los tres comportamientos quedan definidos así:

### Mirrored

- usa sólo Front;
- la cara trasera corresponde a observar físicamente el reverso de la misma lámina;
- texto/logos se ven espejados desde atrás.

### Readable

- usa sólo Front;
- la cara trasera invierte U para que el mismo contenido sea legible desde ambos lados.

### Independent

- Front usa el primer asset;
- Back usa el segundo asset;
- Back se orienta para ser legible desde su lado;
- si todavía no hay Back configurado, Front se utiliza temporalmente como fallback legible.

Esto permite reproducir directamente el concepto del modelo de referencia donde una cara puede mostrar una imagen y la cara opuesta otra distinta.

## 108.2 Aspect ratio de caras independientes

No se deformará una imagen trasera sólo porque tenga una relación de aspecto distinta a Front.

En `dev.2` la regla provisional/implementada es:

- ambas caras comparten el mismo eje de rotación;
- ambas comparten el mismo punto inferior de anclaje;
- ambas comparten `scalePixels`;
- `scalePixels` representa la dimensión mayor de cada imagen;
- cada cara calcula su ancho/alto individual respetando su propio aspect ratio.

Por ello dos imágenes 1:1 coinciden perfectamente, mientras una 16:9 y una 9:16 pueden tener siluetas diferentes vistas de perfil sin ser estiradas.

Más adelante un modo Multi-face/Prism puede introducir envelopes más estrictos si se requiere una geometría común por cara.

## 108.3 Render bounding box dependiente de la proyección

Una proyección no puede depender del AABB físico de un pedestal de un bloque.

Desde `dev.2`, el renderer calcula un volumen de render aproximado usando:

- `scalePixels`;
- `liftPixels`;
- `floatAmplitudePixels`;
- centro del projector;
- radio necesario para una rotación completa.

El objetivo es que una imagen de debug elevada o de varios bloques no desaparezca sólo porque el pedestal físico salió del frustum.

Esto también prepara la arquitectura para:

- Wide;
- Tall;
- Field;
- Prism;
- Effigy;
- Colossal.

No se debe volver a una solución permanente basada en `shouldRenderOffScreen = true` para todos los proyectores, porque impediría aprovechar frustum culling cuando existan muchas instalaciones Mirage simultáneas.

## 108.4 Toolchain y build reproducible en Windows

Desde `dev.3` existe **una sola entrada de build para Windows**:

```text
build.bat
```

No existe un instalador de Gradle separado y no existe un launcher `runClient` separado. El objetivo es copiar exactamente el flujo ya probado en los otros mods del proyecto: el usuario hace doble click en un único archivo y ese mismo archivo resuelve la toolchain necesaria antes de compilar.

### Flujo de `build.bat`

Al ejecutarse:

1. cambia automáticamente al directorio raíz del proyecto;
2. lee `mod_version` desde `gradle.properties`;
3. busca un **JDK 21 real**, no sólo el primer `java.exe` del PATH;
4. intenta `JAVA_HOME`, `JAVA_HOME_21_X64` y `JDK21_HOME`;
5. busca instalaciones Temurin/Adoptium, Java, Microsoft, Amazon Corretto e IntelliJ `.jdks`;
6. también puede encontrar el Java 21 administrado por Prism Launcher;
7. ignora Java de otras versiones aunque aparezcan primero en PATH;
8. si no existe Gradle local, crea `.gradle-dist/`;
9. descarga Gradle 9.2.1 desde `services.gradle.org`, usando `curl.exe` y PowerShell como fallback;
10. descomprime la distribución dentro del proyecto;
11. ejecuta `--no-daemon clean build --stacktrace`;
12. busca el JAR final en `build/libs/`;
13. tanto en éxito como en error, mantiene la ventana abierta con `pause`.

La instalación queda en:

```text
.gradle-dist/gradle-9.2.1/
```

y **no modifica el PATH global de Windows**.

El helper no depende de `gradlew.bat`, `INSTALL-GRADLE.bat` ni de ningún Gradle instalado globalmente. Esto evita que la ausencia de wrapper binario en una snapshot impida compilar y evita conflictos de versión con otros proyectos.

`.gradle-dist/`, `.gradle/`, `build/` y `run/` son artefactos locales y nunca deben viajar en snapshots de source.

## 108.5 Estado después de dev.2

Implementado acumulativamente:

- Compact Mirage Projector;
- BlockEntity;
- GUI;
- PNG/JPG/JPEG local import;
- normalización y SHA-256;
- dynamic texture;
- Front + Back reales;
- Mirrored;
- Readable;
- Independent con segundo asset;
- aspect ratio independiente;
- rotation;
- clockwise/counter-clockwise;
- orientation offset;
- vertical flip;
- time floating;
- rotation-synced floating;
- float amplitude;
- float angular interval;
- Projection Lift universal;
- debug scaling;
- projection-aware render AABB;
- empty vanilla book;
- NBT/settings synchronization;
- one-click portable Gradle bootstrap/build through the single `build.bat`.

Siguiente bloque grande recomendado:

> **asset networking real: upload chunked al servidor, world storage por hash, deduplicación y descarga/cache automática para otros clientes.**

`dev.3` llegó a compilar en Windows, pero se detectó que el snapshot apuntaba accidentalmente a NeoForge 21.1.249. Antes de esa fase, `dev.4` debe recompilarse y probarse contra el baseline correcto **NeoForge 21.1.244**.


---

# 109. Estado concreto de `0.1.0-dev.3`

`dev.3` es un parche de toolchain/build sobre la implementación acumulativa de `dev.2`. No elimina ninguna función de proyección.

## 109.1 Motivo del parche

La primera estrategia separaba:

```text
BUILD.bat
INSTALL-GRADLE.bat
RUN-CLIENT.bat
```

En Windows esos helpers podían cerrarse inmediatamente antes de entregar una salida útil. Además, dividir una operación que para el usuario debe ser de un click añadía complejidad innecesaria.

La estrategia queda reemplazada por una única ruta:

```text
build.bat
```

Esta ruta replica el patrón ya utilizado y probado en otros mods del mismo conjunto de desarrollo.

## 109.2 Invariante de build desde dev.3

Para snapshots futuras:

- debe existir un único `build.bat` como entry point de Windows;
- `build.bat` debe encontrar Java 21 por sí mismo;
- si Gradle no está presente dentro del proyecto, debe descargarlo y extraerlo por sí mismo;
- después debe compilar el JAR sin requerir un segundo script;
- cualquier fallo debe terminar en `pause` para que el error pueda copiarse;
- `.gradle-dist/` es caché/toolchain local y nunca debe incluirse en snapshots;
- no se debe volver a introducir un `INSTALL-GRADLE.bat` separado salvo que exista una necesidad técnica nueva y explícita.

## 109.3 Funcionalidad acumulativa conservada

Todo lo implementado en `dev.1` y `dev.2` permanece: Compact Mirage Projector, GUI, importación local PNG/JPG/JPEG, Front/Back independientes, modos Mirrored/Readable/Independent, rotation, bobbing, lift universal, scaling debug, render AABB ampliado, persistencia y sincronización de settings.


---

# 110. Estado concreto de `0.1.0-dev.4`

`dev.4` es un parche de baseline/toolchain sobre `dev.3`. Mantiene intacta la implementación acumulativa de proyección y corrige una desviación de versión detectada después del primer build exitoso en Windows.

## 110.1 Baseline NeoForge obligatorio

Para Mirage Projector y los proyectos Minecraft de esta línea, el baseline fijado es:

```text
Minecraft 1.21.1
NeoForge 21.1.244
Java 21
```

La revisión NeoForge no debe seguir automáticamente la versión que use el MDK upstream. Una revisión más nueva de `21.1.x` sólo se adopta si se decide y valida explícitamente.

## 110.2 Estado de validación

- `dev.3`: compiló en Windows, pero accidentalmente contra NeoForge 21.1.249.
- `dev.4`: target corregido a 21.1.244 y posteriormente **confirmado build/run-clean en Windows e in-game**.

## 110.3 Siguiente paso

`dev.4` queda fijado como baseline build/run-clean en NeoForge 21.1.244. Las siguientes snapshots deben mantener esa revisión salvo decisión explícita.


---

# 111. Estado concreto de `0.1.0-dev.5`

`dev.5` es la primera pasada derivada de observación in-game real del Compact Mirage Projector. No expande todavía el alcance a Cores, Prism, Effigy o multiplayer asset transport: primero corrige la interacción y la representación visual necesarias para que el MVP de imagen pueda probarse correctamente.

## 111.1 Hallazgos de dev.4 en juego

Confirmado:

- el bloque se registra y puede colocarse;
- click derecho abre correctamente la GUI;
- el libro de empty state aparece, rota y usa bobbing;
- los controles de la GUI se muestran;
- la build funciona sobre NeoForge 21.1.244.

Problemas observados:

1. `Import front image...` / `Import back image...` no abrían ninguna ventana visible.
2. El centro del modelo no mostraba el diamond block de la referencia.
3. El deck de vidrio era prácticamente invisible porque el modelo generado recortaba el borde de la textura vanilla de glass.
4. Faltaba una pestaña propia del mod en Creative Mode.
5. El libro tenía un tilt X adicional que lo hacía verse inclinado hacia el suelo.

## 111.2 File picker nativo

La implementación inicial usaba AWT `FileDialog`. Aunque compila, AWT vive en una pila de ventanas distinta a GLFW/LWJGL y no resultó fiable dentro del cliente de Minecraft.

Desde dev.5 se usa `org.lwjgl.util.tinyfd.TinyFileDialogs`, disponible en el stack LWJGL usado por Minecraft. El botón llama un picker nativo con filtros:

```text
*.png
*.jpg
*.jpeg
```

Si el usuario cancela, la GUI vuelve a estado `Import cancelled`. Si selecciona un archivo, continúa el pipeline existente de normalización/cache.

## 111.3 Modelo físico corregido

El modelo runtime debe respetar la referencia de Blockbench para la base física, excluyendo solamente el plano `Image` porque ese plano pertenece al renderer dinámico.

Capas actuales:

```text
Y 2..5 px : 2x3x2 diamond core
Y 1..2 px : 12x1x12 glass deck / glass-pane edge
Y 0..1 px : 16x1x16 obsidian base
```

El glass deck usa UV completa en su cara superior para conservar el contorno visual de la textura vanilla. Los laterales usan `glass_pane_top`, tal como indicaba el modelo de referencia.

## 111.4 Creative tab

Mirage Projector posee desde dev.5 una pestaña propia de Creative Mode llamada **Mirage Projector**, usando el projector como icono. El bloque se conserva además en Functional Blocks para descubrimiento natural.

## 111.5 Empty-state book

El libro conserva:

- full-bright;
- rotación Y;
- floating/bobbing;
- interacción indirecta mediante el mismo pedestal/GUI.

Se elimina el tilt X de 20° para que el placeholder quede vertical y no parezca recostado sobre el pedestal.

## 111.6 Estado de validación

- `dev.4`: build/run-clean confirmado en Windows, Minecraft 1.21.1, NeoForge 21.1.244.
- `dev.5`: **build/run funcional confirmado** en Windows sobre Minecraft 1.21.1 + NeoForge 21.1.244. TinyFileDialogs abre correctamente y una imagen importada se normaliza/cachea y aparece como proyección real con escala, rotación y bobbing.
- hallazgos posteriores de dev.5: las proyecciones grandes podían desaparecer en ciertos ángulos cercanos al mirar hacia arriba y las transparencias permitían ver simultáneamente la cara contraria.


---

# 112. Estado concreto de `0.1.0-dev.6`

`dev.6` es una pasada de corrección del renderer después de conseguir el primer Mirage de imagen completamente funcional en juego. No cambia todavía el alcance del MVP: corrige dos propiedades fundamentales que deben quedar sólidas antes de avanzar a Cores, múltiples slots o multiplayer asset transport.

## 112.1 Hallazgo: culling incorrecto en proyecciones grandes/elevadas

Durante stress tests con escalas muy superiores al futuro límite del Compact Projector, la imagen podía desaparecer al acercarse al pedestal y mirar en diagonal hacia arriba. El contenido proyectado seguía estando frente a la cámara, pero el bloque físico podía quedar fuera de la región usada por el frustum del BlockEntityRenderer.

La solución de prototipo es:

```text
projection-aware AABB
+
shouldRenderOffScreen = true
+
getViewDistance = 256
```

La prioridad en esta fase es que el Mirage no desaparezca mientras se validan escalas de hasta 10 bloques. `shouldRenderOffScreen=true` no se considera la solución de rendimiento final: antes de escalar a muchas instalaciones simultáneas se diseñará culling/LOD contra el volumen proyectado real.

## 112.2 Regla física definitiva para un Plane Mirage

Una proyección plana tiene **dos lados, pero nunca dos caras visibles simultáneamente desde un mismo punto de cámara**.

En dev.5 se dibujaban Front y Back en el mismo frame. Esto era especialmente visible con PNG transparentes: la transparencia de una silueta dejaba ver la cara contraria, produciendo una segunda copia superpuesta, contornos fantasma y composiciones imposibles físicamente.

Desde dev.6:

1. se obtiene la posición de la cámara;
2. se calcula la normal frontal del plane después de su rotación actual;
3. se calcula el producto punto entre la normal y el vector projector -> cámara;
4. si el resultado pertenece al lado frontal, se dibuja sólo Front;
5. si pertenece al lado trasero, se dibuja sólo Back.

La selección se recalcula cada frame, por lo que acompaña correctamente una proyección que rota continuamente.

## 112.3 Back Face modes después del aislamiento

Las opciones existentes conservan su significado, pero ahora sólo se evalúan cuando la cámara está físicamente detrás del plane:

- **Mirrored**: reverso físico de la misma imagen; texto/logos aparecen espejados.
- **Readable**: reutiliza la imagen frontal invirtiendo UV para que se lea con la misma orientación desde ambos lados.
- **Independent**: usa el asset Back independiente; si falta, Front actúa como fallback legible.

Esto significa que la transparencia de Front jamás revela Back y viceversa.

## 112.4 Geometría coplanar

El pequeño `BACK_FACE_EPSILON` de builds anteriores deja de ser necesario porque ya no existe Z-fighting entre dos quads simultáneos. Front y Back pasan a compartir exactamente `Z=0` dentro del espacio local del Mirage. Conceptualmente vuelven a ser una sola lámina infinitamente fina con dos superficies lógicas.

## 112.5 Estado de validación

- `dev.5`: build/run-functional confirmado; primera imagen importada y proyectada correctamente.
- `dev.6`: **build/run y QA visual confirmados en juego**. Las capturas posteriores muestran proyecciones grandes y PNG transparentes sin la mezcla Front/Back que motivó el parche.
- La línea dev.7 parte por tanto desde dev.6 como baseline funcional del renderer Plane.


# 113. Estado concreto de `0.1.0-dev.7` — multiplayer + Item Mode + clearance

## 113.1. Transporte de assets

El asset normalizado sigue siendo identificado exclusivamente por su SHA-256 de 64 caracteres hexadecimales. Al pulsar Apply, el cliente intenta subir los assets Front/Back presentes en su cache local. Además, cuando una textura cacheada se carga por primera vez durante una sesión, el cliente intenta sembrarla oportunísticamente en el servidor: esto permite que mundos creados antes de dev.7 migren sus imágenes al world asset store sin exigir reimportarlas.

- chunk size: 32 KiB;
- normalized cap: 4 MiB;
- max chunks: derivado automáticamente del cap;
- uploads duplicados: el servidor los ignora si el hash ya existe;
- upload assembly: session por player UUID + asset id;
- stale upload/download session timeout: 30 s;
- server storage: `<world>/mirage_projector/assets/<sha256>.png`;
- servidor valida tamaño, firma PNG y SHA-256 antes de persistir;
- cliente que no tenga el PNG pide `assetId` al servidor automáticamente desde el texture cache;
- downloads se verifican nuevamente por SHA-256 antes de escribir el cache local.

Esto convierte la imagen en un recurso real del mundo/server. La ruta original del PC nunca viaja por red.

## 113.2. Item Mode

El Block Entity posee un ItemStackHandler de un slot, con stack limit 1. El ItemStack permanece realmente almacenado: no se guarda sólo una copia visual.

La GUI permite cambiar `Projection source` entre `Image` e `Item`. El Item Mode reutiliza:

- Scale;
- Projection Lift;
- Rotation;
- Direction;
- orientation offset;
- Floating;
- Float Mode y amplitud.

El render usa `ItemRenderer.renderStatic(..., ItemDisplayContext.FIXED, ...)`, de forma que el objetivo es conservar modelos vanilla/modded, block items, glint y componentes visuales normales siempre que el mod de origen utilice el pipeline estándar de items.

Al romper el Mirage Projector, el ItemStack almacenado se extrae y se dropea en el mundo para evitar pérdida del objeto expuesto.

## 113.3. Clearance

La GUI ejecuta cada ~10 ticks un scan conservador. Para proyecciones que giran se trata intencionalmente como envelope horizontal completo alrededor del eje; esto puede marcar más bloques que la intersección exacta de un frame concreto, pero evita falsos negativos al validar un Mirage que va a girar.

La primera pasada sólo informa `clear` o el número de bloques no-air dentro del envelope. El outline rojo in-world sigue reservado para la pasada de preview avanzada.

## 113.4. Limitaciones dev.7

- No hay barra de progreso de upload/download todavía.
- El servidor realiza el ensamblado/escritura del asset en el hilo lógico; 4 MiB es el límite precisamente para mantener acotado el coste de esta primera versión.
- WebP todavía no está habilitado.
- Item Mode no posee aún perfiles especiales para armadura/banner; esos siguen siendo modos propios futuros.
- El GUI ampliado para inventario se debe revisar en resoluciones/GUI scales pequeñas y podrá migrar a tabs/páginas cuando Core Mode añada más slots.
- Clearance es conservador y no reemplaza todavía un preview geométrico exacto.


# Dev.8 implementation note — Core / Power

> **HISTÓRICO / SUPERSEDED POR dev.38:** esta nota conserva el comportamiento de dev.8 para trazabilidad. Compact 10 px es ahora un target nominal; Debug elimina penalties de Overdrive, y el presupuesto vigente se calcula con Core base PU × chassis multiplier × Core amplification.

`0.1.0-dev.8` convierte Projection Core y Projection Power de roadmap a sistema real. El Core es un ItemStack persistido en un socket del BlockEntity y su material se resuelve a un `ProjectionCoreProfile`. `ProjectionPower` evalúa settings sin mutarlos y devuelve estado activo, Power usado/disponible y motivo de fallo. El renderer consulta ese estado antes de dibujar cualquier Mirage y el core físico se dibuja dinámicamente.

Regla nueva confirmada por arquitectura:

> **Cambiar a un Core insuficiente apaga la proyección; jamás borra la configuración.**

Compact mantiene `10 px` como envelope normal de Plane. Para stress-test de desarrollo, Creative puede activar `Debug chassis`, que salta sólo el envelope del chassis; el Core sigue mandando. Esto permite seguir probando imágenes de 3, 5, 8 o 10 bloques sin convertir accidentalmente el Compact final en un projector monumental.

# 110. Estado concreto de 0.1.0-dev.13 — Entity Scan foundation + Humanoid Entity GUI contract

`0.1.0-dev.13` freezes the entity-projection architecture before the dedicated renderer/UI is wired into the projector.

## 110.1. Armor Mode is renamed to Humanoid Entity Mode

The old working name **Armor Mode / Effigy Mode** is no longer expressive enough. The mode is formally **Humanoid Entity Mode** because its primary object is a humanoid render rig, not an armor inventory.

It supports the design target of:

- invisible mannequin with equipment only;
- scanned Player body with or without armor;
- Zombie/Baby Zombie/Husk/Drowned;
- Skeleton/Stray/Wither Skeleton;
- Piglin-family humanoids;
- later compatible modded humanoids;
- four armor snapshots + Main Hand + Off Hand;
- pose changes independent from captured equipment.

## 110.2. Entity Scan Card foundation now exists

Dev.13 adds the first real `Entity Scan Card` item.

The card is non-stackable and scans a single `LivingEntity` by interaction. The scan is frozen and self-contained: the source UUID is metadata/provenance and is not required to render the projection later.

The first data format records:

- independent Mirage scan UUID;
- source entity UUID;
- entity type;
- display name;
- bounded/sanitized entity visual NBT;
- classification as Humanoid / Horse / Generic;
- explicit six-slot humanoid equipment snapshots when applicable;
- explicit Horse Saddle/BODY snapshots when applicable.

Mounted/composite entities are rejected in this first pass so jockeys/passenger compositions are not accidentally serialized as one entity.

A populated card requires sneak-use to overwrite, preventing accidental rescans.

**Important implementation boundary:** dev.13 creates/scans the card data but the projector-side Entity/Humanoid screen, entity import state and world entity renderer are still the next implementation block.

## 110.3. Humanoid editor layout is frozen

The dedicated Humanoid Entity GUI must use paired incoming/active channels:

```text
INCOMING / STAGING                  PROJECTED / ACTIVE

Head       [✓]                      Head
Chest      [✓]                      Chest
Legs       [✓]                      Legs
Feet       [✓]                      Feet
Main Hand  [✓]                      Main Hand
Off Hand   [✓]                      Off Hand
```

Both hands exist on **both sides**.

The left side receives either:

- equipment snapshots extracted from a scanned humanoid card; or
- temporary physical items supplied by the player for snapshot capture.

The right side is always render-only snapshot state.

If a projected slot is already occupied, its matching ✓ action resolves that one conflict explicitly instead of silently overwriting the entire loadout.

Removing a humanoid card does not remove those six rows and does not erase already-projected equipment, because they belong to Humanoid Entity Mode itself, not to card presence.

A `Return inserted gear` action returns physical staging inputs. Card-derived incoming snapshots are virtual and therefore have nothing physical to return.

## 110.4. Horse and Generic Entity layouts are dynamic

Horse scans receive dedicated Saddle and Body Armor incoming/projected rows instead of the six humanoid rows.

Those fields exist only in Horse context and must be cleared when Horse context is replaced by an unrelated Entity context.

Generic Entity scans do not automatically expose editable armor slots. If a modded/non-humanoid mob's visible armor is part of its private model/entity state rather than a supported equipment channel, it remains inside the frozen entity snapshot.

## 110.5. Preview contract

The entity editor places player inventory below the entity controls and reserves a clipped, auto-fitted **live 3D preview panel** to the right of the main GUI.

Preview scale must be derived from the entity/model bounds so a Chicken, Baby Zombie, Horse and Ender Dragon all remain inside the same reasonable viewport.

Humanoids should follow the cursor in the spirit of the vanilla inventory player preview when technically compatible. Non-humanoids may yaw toward the cursor without forcing unsupported head transforms.

The preview always represents the active projected/right-hand state. Incoming equipment must not look applied until accepted.

The same broader preview policy applies to all Mirage modes:

- Image/multi-face: individual face thumbnails plus composed plane/prism preview;
- Banner: per-face identification plus real 3D banner preview where appropriate;
- Item: real 3D item/block preview;
- Humanoid Entity: body + active armor/hands + pose;
- Entity: complete scanned entity.

The complete normative specification is in `docs/ENTITY-PROJECTION-CONTRACT.md`.

## 110.6. Compatibility boundary

Backpack/back-slot, accessory, artifact, trinket and other external equipment systems are **not baseline scope**.

After Humanoid Entity Mode is complete, optional compatibility adapters may add extra staging/projected rows and renderer contributions without turning those mods into hard dependencies.

A future Entity Catalog / Scan Binder is recorded only as a long-term idea and does not belong to the current implementation queue.


# 114. Estado concreto de `0.1.0-dev.14` — Empty Scan Template + Entity Workspace

## 114.1 Empty Scan Template

El medio de escaneo vacío se presenta al jugador como **Empty Scan Template**. Es un item no stackeable con apariencia de papel. Su receta queda fijada como ocho pepitas de hierro rodeando un papel:

```text
N N N
N P N
N N N

N = Iron Nugget
P = Paper
```

El escaneo requiere **Shift + clic derecho** sobre una sola entidad viva o Player. Un clic derecho normal no debe secuestrar la interacción propia de Villager, Horse, etc. Repetir Shift + clic derecho sobre otra entidad reemplaza deliberadamente el contenido de la misma plantilla. Jockeys/passengers/vehicles siguen fuera del alcance inicial.

La tarjeta escaneada conserva un `ScanId` propio de Mirage y el UUID original sólo como procedencia. El origen puede morir, descargarse o desconectarse sin invalidar el snapshot.

## 114.2 Política definitiva de nombre bajo la proyección

El nombre no es el nombre genérico del tipo de mob. Mirage guarda un nameplate sólo en dos casos:

- source Player: siempre el nombre del jugador, por ejemplo `Haku`;
- source no-Player: solamente `CustomName`, si la entidad fue renombrada.

Por tanto:

- Horse sin nombre → sin texto;
- Horse llamado `Aurelio` → `Aurelio`;
- Zombie normal → sin texto;
- Zombie llamado `Steve` → `Steve`;
- Player `Haku` → `Haku`.

Ese texto se renderizará entre el punto más bajo de la proyección y la base. Dev.14 ya conserva la metadata correcta; el dibujo in-world entra junto al renderer Entity/Humanoid.

## 114.3 Entity Workspace funcional

El projector ya posee un editor dedicado accesible desde el GUI principal. La organización normativa es:

```text
INCOMING / STAGING         ENTITY SOURCE        PROJECTED / ACTIVE

[Head]      [✓]                               [Head]
[Chest]     [✓]          [ scanned card ]      [Chest]
[Legs]      [✓]                               [Legs]
[Feet]      [✓]                               [Feet]
[Main Hand] [✓]                               [Main Hand]
[Off Hand]  [✓]                               [Off Hand]

[Return inserted gear]
---------------------------------------------------------------
                      PLAYER INVENTORY
```

En Humanoid Entity Mode las seis filas pertenecen al modo, no a la presencia de una tarjeta. La entidad base, el incoming equipment y el projected equipment son estados separados.

La izquierda acepta dos orígenes: un item físico puesto temporalmente por el jugador o el snapshot incoming extraído de la entidad escaneada. La derecha nunca es inventario: contiene snapshots render-only.

Cada ✓ afecta sólo a su canal. Si el destino está ocupado por otra pieza, el GUI pide Replace/Cancel para esa fila. Click derecho en una celda derecha borra sólo esa pieza holográfica, permitiendo vestir luego la entidad con otro snapshot.

## 114.4 Horse y Generic

Una Horse scan habilita únicamente `Saddle` y `Body Armor`. Esos overrides son contextuales. Si hay gear físico en staging, Mirage bloquea el retiro normal de la Horse card hasta devolverlo, evitando ocultar o perder items reales.

Una entidad Generic no recibe slots de armadura inventados. Si su supuesto armor es parte de su modelo/variant data, permanece congelado dentro del entity snapshot.

## 114.5 Preview 3D: frontera exacta de dev.14

La nueva pantalla reserva el panel exterior derecho y ya expone allí el entity/nameplate activo, pero **dev.14 todavía no reconstruye/renderiza la entidad 3D dentro de ese panel**. La siguiente implementación debe:

1. reconstruir una entidad client-side sin spawnearla en el mundo;
2. aplicar el body snapshot;
3. aplicar únicamente el equipment `Projected / Active`;
4. derivar escala de width/height/bounds para auto-fit;
5. clippear el render al viewport;
6. seguir el cursor cuando el renderer/modelo lo soporte;
7. reutilizar el mismo pipeline para el world hologram.

No se simula una preview falsa antes de resolver correctamente ese pipeline.

## 114.6 Compatibilidad externa

Backpacks, accesorios, Artifacts/Curios/Trinkets y slots equivalentes permanecen fuera del baseline. Después de cerrar el renderer base se podrán añadir adapters opcionales que aporten canales y layers adicionales sin convertirlos en dependencias obligatorias.

El Entity Catalog / Scan Binder queda registrado únicamente como idea de futuro lejano.

# 115. Estado concreto de `0.1.0-dev.15` — Live Entity Preview

## 115.1 Preview reconstruida, no entidad spawneada

El Entity Workspace ya no usa un placeholder textual. `EntityProjectionPreviewRenderer` crea una entidad **sólo del lado cliente y sólo como objeto de render**, sin agregarla al mundo. Para mobs se reconstruye el `EntityType` y se carga el `EntityData` sanitizado del scan; para Player se usa un `RemotePlayer` con UUID/nombre de origen.

La preview equipa exclusivamente el estado **Projected / Active**. El equipamiento Incoming de la izquierda no aparece sobre el cuerpo hasta que el usuario lo acepta mediante su ✓ correspondiente. Esto mantiene visualmente honesta la resolución de conflictos.

## 115.2 Auto-fit, clipping y cursor

El viewport obtiene `getBbWidth()`/`getBbHeight()` de la entidad reconstruida y deriva una escala ajustada al ancho/alto disponible. El render usa el pipeline vanilla `InventoryScreen.renderEntityInInventoryFollowsMouse`, que limita el dibujo con scissor y restaura las rotaciones temporales del modelo. Chicken, Baby Zombie, Player, Horse y entidades grandes comparten así una sola región de preview sin una tabla rígida de escalas por mob.

El panel prefiere 150 px a la derecha. Si la resolución lógica es más estrecha, el main panel se desplaza y la preview se reduce hasta un mínimo usable en vez de sacar controles fuera de pantalla. Si ni siquiera cabe ese mínimo, el editor principal conserva prioridad.

## 115.3 Card/body vs equipment persistente

La tarjeta central define el **body actual**. Al retirarla, ese body desaparece. En Humanoid Entity Mode los seis canales Incoming/Projected siguen intactos, porque pertenecen al maniquí virtual y no a la card. Esto permite quitar una Zombie/Player card y conservar armor/manos previamente configuradas.

Horse sigue siendo contextual: retirar/reemplazar su card elimina el body Horse y limpia Saddle/Body Armor virtuales. El retiro continúa bloqueado mientras haya staging físico Horse que deba devolverse al inventario.

## 115.4 Nameplate en preview y pendiente in-world

La preview respeta la política congelada: Player siempre muestra su nombre; un mob sólo muestra texto si tenía CustomName. Ese texto aparece en la franja inferior del panel, separado del nombre/tipo técnico de inspección. El **nameplate in-world**, entre base y punto inferior de la proyección, continúa como siguiente tarea junto al renderer Entity/Humanoid del mundo.

## 115.5 Límite conocido de skin de Player

En Minecraft 1.21.1 `RemotePlayer` resuelve su skin mediante el `PlayerInfo` cliente del UUID. Por eso el preview puede resolver correctamente a un Player presente en la sesión, pero una **skin congelada exacta y autocontenida para Player offline** todavía requiere un canal explícito de snapshot/transfer de skin. No se considera cerrado hasta resolver eso sin depender de que el jugador siga conectado.

## 115.6 Próximo bloque

1. SourceMode Entity/Humanoid in-world;
2. renderer world reutilizando la misma composición body + Projected/Active;
3. nameplate bajo la proyección;
4. armor equipada/poses y modelo humanoide sin body;
5. Ghost Effect 3D alpha-safe;
6. clearance por entity bounds/pose.

El proyecto mantiene la regla operacional de que **cada oleada entrega snapshot recuperable aunque build/QA esté incompleto**.



# 116. Estado concreto de `0.1.0-dev.16` — Entity/Humanoid in-world + virtual mannequin

## 116.1 Un Entity snapshot ya es una proyección 3D real

`SourceMode.ENTITY` renderiza el body congelado como un `LivingEntity` client-only. No se agrega al level como entidad jugable, no tiene IA, hitbox interactiva ni ownership de mundo. Scale, Lift, Rotation y Floating afectan al objeto 3D completo; no existen caras N/E/S/W como en Image/Prism.

La misma composición se usa en GUI y mundo: body snapshot opcional + exclusivamente equipment `Projected / Active`. Incoming nunca aparece antes del ✓ correspondiente.

## 116.2 Humanoid sin tarjeta = maniquí virtual invisible

Los seis canales Humanoid pertenecen al modo, no a la card. Si se retira el Player/Zombie/Skeleton body y todavía existe Head/Chest/Legs/Feet/Main/Off proyectado, Mirage crea sólo para render un rig humanoide invisible. El base model no se ve; las layers de armor y manos siguen renderizándose.

Por tanto un conjunto de God Armor + Netherite Hoe + Netherite Block puede permanecer como holograma después de retirar la tarjeta corporal, sin dejar ninguno de esos items reales dentro del projector.

## 116.3 Armor standalone en Item Mode

Un snapshot Item que corresponde a Head/Chest/Legs/Feet pasa por geometría **equipada**: casco como casco tridimensional alrededor de una cabeza invisible, chestplate como chestplate equipado, etc. No se usa la textura plana del inventario como representación del holograma. Herramientas/bloques/items normales continúan como objeto 3D individual completo.

La preview de Item Mode usa la misma regla para armor. El ajuste fino de escala por slot necesita QA in-game antes de considerarlo final.

## 116.4 Player scan autocontenido

Entity Scan data version 3 guarda, cuando existe, la propiedad packed `textures` del GameProfile del Player junto al UUID/nombre de origen. El `RemotePlayer` de Mirage resuelve su skin a partir de esa propiedad congelada. El UUID original sigue siendo provenance, no una dependencia de lookup vivo.

Esto debe permitir que `Haku` siga viéndose como Haku después de desconectarse; servidores/mods de skins con formatos no estándar quedan para compat adapters posteriores.

## 116.5 Nameplate bajo la proyección

El renderer in-world usa únicamente `NameplateText` capturado: Player siempre; mob sólo CustomName. El texto se sitúa en el gap entre el top físico del chassis y el bottom del modelo. No se añade un segundo nametag encima de la cabeza ni un caption genérico `Zombie/Horse`.

## 116.6 Animación sin AI

Los clones visuales no ejecutan `tick()`/`aiStep()`. Mirage alimenta sólo relojes/estado estrictamente visual. Ender Dragon es el primer caso especial: su renderer necesita historial de posiciones además de `tickCount`, así que se inicializa un historial estable y flap clock sin fase, combate, cristal, sonido ni movimiento real.

## 116.7 Frontera restante

Aún no declarar terminado: el **primer** Ghost Effect/Tint 3D ya existe en dev.17 mediante buffers locales, pero falta QA/refinamiento de glint y RenderTypes custom/modded; siguen pendientes pose presets, clearance dependiente de pose/bounds, adapters de animación específicos donde sean necesarios, QA de armor/equipment modded y build real de NeoForge 21.1.244.

La regla operacional continúa siendo absoluta: **cada avance sustancial se preserva primero en snapshot recuperable, aunque build/QA todavía esté pendiente**.
