# Mirage Projector

> Documento maestro de diseño y alcance para **Mirage Projector**, un mod de decoración y exhibición para Minecraft 1.21.1 / NeoForge.

## 1. Visión general

**Mirage Projector** nace para cubrir una función que actualmente queda a medio camino entre los mods de marcos de imágenes, proyectores multimedia, item displays, armor stands y hologramas tradicionales: permitir construir **proyecciones decorativas flotantes, animadas y configurables** que puedan mostrar imágenes externas, items, banners, equipamiento y otras formas visuales de Minecraft de una manera integrada al mundo.

La intención no es crear una pantalla de video ni un navegador dentro de Minecraft. El foco es el **display diegético**: un bloque físico pequeño o grande que genera una "ilusión" visible en el mundo, como si fuera un espejismo estable controlado por un núcleo.

Casos de uso previstos:

- Una imagen PNG/JPG/JPEG/WebP flotando sobre un pedestal.
- Pixel art o ilustraciones de personajes como decoración.
- Logos de base, emblemas de facción o carteles.
- Banners sin poste, flotando y rotando.
- Items raros o encantados expuestos sin item frame.
- Herramientas, armas, bloques u objetos modded proyectados.
- Una armadura completa ensamblada en forma de figura holográfica.
- Una figura de armadura enorme, de varios bloques de altura.
- Un "Mirage Prism" con cuatro caras laterales y una imagen distinta en cada una.
- Varias imágenes colocadas juntas en un mismo campo de proyección.
- GIFs animados como extensión futura, por ejemplo un estandarte ondeando.
- Instalaciones monumentales visibles desde lejos, como una estatua de armadura de netherita con un tridente.

El nombre **Mirage Projector** se elige precisamente porque las proyecciones de los modelos grandes deberían sentirse más cercanas a un espejismo artificial que a una simple pantalla.

---

## 2. Principio de diseño principal

El sistema se divide en dos conceptos independientes:

### 2.1. Projector / Chassis

El **cuerpo físico del proyector** determina:

- La geometría de proyección que soporta.
- El volumen máximo donde puede existir la proyección.
- El ancho máximo.
- El alto máximo.
- La profundidad máxima, cuando corresponda.
- La cantidad física de espacio disponible para el efecto de flotación.
- El tipo de fuente visual que puede manejar.
- Si soporta una sola cara, varias caras, volumen 3D, figura humanoide, etc.
- Las funciones especiales propias de ese cuerpo.

Regla conceptual:

> **El Projector determina qué forma puede tomar el Mirage.**

### 2.2. Projection Core

El **núcleo insertado en el proyector** determina la potencia disponible dentro de los límites físicos del chassis.

El núcleo afecta, según el diseño final:

- Tamaño máximo efectivo dentro del envelope del projector.
- Altura máxima a la que puede elevarse la proyección.
- Amplitud máxima de flotación.
- Cantidad máxima de fuentes simultáneas.
- Coste de geometrías especiales.
- Coste de proyecciones 3D.
- Coste de figuras de equipamiento.
- Cantidad de caras activas.
- Alcance de proyección.
- Potencia total disponible para efectos.
- Potencialmente distancia de render recomendada o capacidad de una instalación monumental.

Regla conceptual:

> **El Core determina cuánto Mirage puede sostener el Projector.**

Un núcleo muy potente no convierte un projector compacto en un projector gigantesco. El chassis sigue imponiendo su límite físico.

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

La línea de desarrollo ya superó el prototipo visual mínimo y en `0.1.0-dev.8` posee cinco pilares funcionales: **Image Mode**, **Item Mode**, transporte de assets cliente/servidor, clearance y la primera arquitectura real de **Projection Core / Power**.

Estado acumulado actual:

1. Bloque Compact Mirage Projector con Block Entity y GUI.
2. Renderer dinámico de imágenes planas con aspect ratio preservado.
3. PNG/JPG/JPEG local -> normalización PNG -> SHA-256 -> cache.
4. Front/Back con Mirrored, Readable e Independent Faces.
5. Rotación, dirección, offset, float temporal/rotation-synced, amplitud y Projection Lift.
6. Escala de debug hasta 10 bloques para stress-test del renderer.
7. Selector nativo de archivos mediante LWJGL/TinyFileDialogs.
8. Creative tab propio y modelo físico compacto con obsidiana, vidrio y núcleo visual de diamante.
9. Corrección de frustum/culling de proyecciones grandes durante la etapa debug.
10. Aislamiento real de caras: sólo se dibuja la cara físicamente visible del plano.
11. **Multiplayer asset transport dev.7**: la imagen normalizada se divide en chunks, se sube al servidor, se valida por SHA-256 y se guarda dentro del mundo; otros clientes la solicitan automáticamente si no está en su cache.
12. **Item Mode dev.7**: slot dedicado, ItemStack real persistente, render 3D con ItemRenderer vanilla, glint/modelos modded y devolución del item al romper el projector.
13. **Clearance warning dev.7**: la GUI escanea un envelope conservador de la proyección y avisa cuántos bloques intersectan el volumen previsto.
14. Botones para limpiar Front/Back sin tener que reemplazar el asset.

La build sigue siendo de desarrollo: Core/Power ya tiene una primera implementación, pero todavía faltan WebP, Prism, Banner, Effigy, chassis mayores y optimizaciones finales de red/render.

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

# 6. Potencia del núcleo

Se prefiere un sistema de **Projection Power** por sobre una colección de restricciones totalmente arbitrarias.

Ejemplo conceptual:

```text
Projection Power
██████████████░░░░░░
14 / 20
```

Cada característica puede consumir parte de ese presupuesto.

Ejemplos de posibles consumidores de energía:

- área de imagen;
- volumen;
- cantidad de caras;
- cantidad de slots;
- distancia vertical al projector;
- proyección 3D;
- figura de equipamiento;
- escala;
- cantidad de objetos simultáneos;
- animación compleja;
- GIF;
- layout multi-display.

Los valores numéricos finales no están congelados todavía. En **dev.8** existe una primera curva jugable/provisional:

| Core | Power | Scale max | Lift max | Float max | Capacidad futura de sources |
|---|---:|---:|---:|---:|---:|
| Glass | 8 | 10 px | 16 px | 1 px | 1 |
| Quartz | 16 | 16 px | 32 px | 2 px | 1 |
| Amethyst | 48 | 48 px | 64 px | 8 px | 2 |
| Diamond | 96 | 80 px | 96 px | 16 px | 4 |
| Netherite | 192 | 160 px en debug | 160 px | 32 px | 8 |

El coste actual suma estabilización base, área/tamaño, lift, rotation, floating, surcharge 3D para Item Mode y extras como Independent Back. Estos números son deliberadamente fáciles de modificar una vez que tengamos QA real.

## 6.1. Regla de doble límite

Toda proyección debe pasar dos límites:

### Límite de chassis

El cuerpo físico permite o no permite esa forma/tamaño.

### Límite de core

El núcleo puede o no alimentar esa configuración.

Ejemplo:

```text
Compact Mirage Projector
Hard envelope: < 1 × 1 block

Glass Core
Power: baja

Diamond Core
Power: alta
```

Aunque Diamond tenga energía sobrante, no puede superar el envelope físico del Compact Projector.

---

# 7. Familias de Projectors previstas

Los nombres y medidas exactas pueden cambiar durante desarrollo.

## 7.1. Compact Mirage Projector

Objetivo:

- Decoración pequeña.
- Primer prototipo.
- Una sola imagen.
- Un solo item en futuras versiones.
- Menos de 1 × 1 bloque de proyección.

Modelo actual:

- proyección de referencia de 10 × 10 px.
- un pixel de margen vertical.
- pedestal completo de 16 × 16 px de footprint.

Core previsto de entrada:

- Glass o núcleo equivalente de baja potencia.

## 7.2. Mirage Display

Projector general de aproximadamente 1 × 1.

Objetivo:

- Imagen estándar.
- Item estándar.
- Banner pequeño.
- Mayor margen vertical.
- Mayor libertad de posicionamiento.

## 7.3. Wide Mirage Projector

Envelope orientativo:

- hasta aproximadamente 5 bloques de ancho;
- aproximadamente 2 bloques de alto.

Pero el usuario no elige arbitrariamente 5 × 2 deformando la imagen.

La proyección debe mantener aspect ratio.

Ejemplo:

- envelope 5 × 2;
- imagen 16:9;
- si el ancho seleccionado excede lo que permite la altura, el ancho efectivo se limita automáticamente.

Uso:

- banners horizontales;
- logos;
- carteles;
- panoramas;
- varias imágenes en fila.

## 7.4. Tall Mirage Projector

Envelope orientativo:

- aproximadamente 2 × 5.

Uso:

- banners altos;
- personajes;
- señalética vertical;
- estatuas planas;
- decoraciones altas.

## 7.5. Mirage Field Projector

Envelope orientativo:

- aproximadamente 3 × 3 o 5 × 5 según versión.

Uso:

- grandes superficies;
- varias imágenes;
- composiciones decorativas;
- carteles visibles desde lejos.

## 7.6. Mirage Prism

Projector especializado en geometría de cuatro caras.

Forma:

- cuatro caras laterales;
- arriba abierto;
- abajo abierto.

Esencialmente:

```text
      ┌────────┐
     /        /│
    / IMAGE  / │
   ├────────┤  │
   │ IMAGE  │ IMAGE
   │        │ /
   └────────┘/
```

No es un cubo sólido.

Es una colección de cuatro quads.

Slots:

- North.
- East.
- South.
- West.

Cada cara puede contener:

- la misma imagen;
- imágenes diferentes;
- banners;
- eventualmente GIFs.

Debe poder rotar como conjunto.

## 7.7. Mirage Effigy Projector

Projector especializado en una figura humanoide / equipamiento.

No funciona como cuatro items simplemente flotando.

Debe renderizar un rig/modelo humanoide invisible sobre el cual se aplican:

- Head.
- Chest.
- Legs.
- Feet.
- Main Hand.
- Off Hand.

Puede incluir:

- armadura vanilla;
- trims;
- leather armor tintada;
- enchantment glint;
- elytra;
- shield;
- trident;
- herramientas;
- armas;
- bloques;
- equipamiento modded que use las rutas normales de render.

Debe permitir postura.

Poses candidatas:

- Standing.
- Guard.
- Hero.
- Combat.
- Raised Weapon.
- Custom, si es técnicamente viable.

Debe tener una geometría 3D real y límites propios de:

- ancho;
- alto;
- profundidad.

Escala:

- modificable;
- limitada por chassis y core.

Proyecciones monumentales futuras:

- una figura de 5 bloques;
- una figura de 10 bloques de altura;
- una "estatua holográfica" de armadura de netherita con un tridente.

## 7.8. Colossal Mirage Projector

No debe ser solamente el projector pequeño con Netherite Core.

Debe ser una infraestructura física distinta.

Puede incluir netherite en el craft del chassis.

Después, aun así, debe poseer un Core Socket.

Ejemplo conceptual:

```text
Colossal Projector + Quartz Core
→ chassis enorme, pero potencia insuficiente para explotarlo.

Colossal Projector + Diamond Core
→ escala intermedia.

Colossal Projector + Netherite Core
→ envelope completo.
```

Envelope candidato:

- hasta 8 × 8 para planos;
- hasta aproximadamente 10 bloques de alto para Effigy;
- límites finales por rendimiento y legibilidad.

---

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
- GIF futuro;
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

El projector tendrá un slot dedicado.

Concepto:

```text
[ Item Slot ] [✓ Project Item]
```

Debe aceptar un `ItemStack`.

Render:

- usar el renderer normal de Minecraft;
- conservar componentes;
- enchantment glint;
- custom model;
- damage state;
- trim si aplica a un render equipable específico;
- modelos proporcionados por otros mods cuando usen APIs vanilla/NeoForge compatibles.

Ejemplos:

- espada encantada;
- herramienta;
- bloque;
- comida;
- cabeza;
- reliquia;
- Create wrench;
- objeto modded.

El item debe poder:

- rotar;
- flotar;
- cambiar escala;
- elevarse.

---

# 22. Banner Mode

Banner debe ser tratado como source especial.

No queremos el poste.

Queremos:

- tela;
- color base;
- patterns;
- dyes.

Debe poder:

- flotar;
- rotar;
- elevarse;
- escalarse;
- entrar en Prism Mode;
- eventualmente animarse mediante GIF importado como alternativa visual.

Caso de uso:

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
- usar GIFs futuros.

El techo y piso permanecen abiertos.

Esto evita que parezca un bloque sólido.

---

# 25. Effigy Mode en detalle

## 25.1. Slots

```text
Head
Chest
Legs
Feet
Main Hand
Off Hand
```

## 25.2. Compatibilidad

Objetivo:

- vanilla armor;
- armor trims;
- leather tint;
- elytra;
- shields;
- tridents;
- hand-held models;
- armaduras modded;
- Create diving equipment si usa un pipeline compatible;
- otros equipables.

Si un mod usa un renderer completamente custom que no puede reutilizarse:

- fallback;
- advertencia;
- soporte específico opcional.

## 25.3. Pose

No usar un armor stand visible.

Crear una figura virtual/humanoid render.

Poses:

- Standing.
- Guard.
- Hero.
- Combat.
- Raised Weapon.
- Custom future.

## 25.4. Escala monumental

En chassis adecuados:

- 2×;
- 4×;
- varios bloques;
- potencialmente 10 bloques.

Debe revisarse:

- culling;
- render distance;
- bounding box;
- iluminación;
- impacto FPS.

---

# 26. GIF / Animated Image

GIF es una extensión futura explícitamente deseada.

No forma parte del MVP.

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

# 41. Núcleo y altura

El `Projection Height` consume capacidad del core.

Ejemplo conceptual, NO números finales:

```text
Glass Core:
- pequeño lift
- 1 source

Quartz Core:
- lift moderado

Amethyst Core:
- lift alto
- buena afinidad a multi-face/animation

Diamond Core:
- gran lift
- gran superficie

Netherite Core:
- monumental
```

El chassis también puede imponer un máximo.

---

# 42. Núcleo y amplitud

La amplitud máxima se calcula usando:

```text
min(
  chassis_clearance_limit,
  core_float_limit,
  current_projection_clearance
)
```

No debe existir un slider que permita atravesar el pedestal.

La GUI refleja dinámicamente el rango válido.

---

# 43. Core Socket

**Primera implementación presente desde dev.8.** La GUI usa:

```text
Projection Core
[ core slot ]
```

Al cambiar core, dev.8 recalcula Power y valida límites sin destruir settings. Si el nuevo Core es insuficiente, el Mirage se apaga y la GUI muestra la razón; al reinsertar un Core suficiente vuelve a funcionar con la configuración anterior. Los sliders conservan todavía el rango de debug completo para no perder valores; una pasada posterior puede añadir markers/rangos visuales dinámicos sin clamping destructivo.

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
- WebP final si el decoder no se integra aún.
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

WebP es requisito de alcance final.

Opciones técnicas:

- ImageIO plugin empaquetado Jar-in-Jar.
- Decoder dedicado.

Se debe evitar exigir al usuario instalar una librería aparte.

El mod final debe llevar su decoder si NeoForge/Minecraft no lo provee.

---

# 59. GIF

GIF puede decodificarse a frames internos.

No renderizar desde disco cada frame.

Predecodificar/cargar con cache controlada.

Posible almacenamiento futuro:

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

# 65. Hologram color/tint

Idea futura:

- global tint;
- alpha;
- brightness.

No debe destruir los colores originales.

Default:

- `#FFFFFF`;
- alpha 100%.

---

# 66. Scanline / hologram effects

Idea opcional futura, no requisito:

- subtle scanlines;
- flicker;
- chromatic separation;
- particles;
- no forced shader.

Deben ser toggles.

La imagen normal limpia debe seguir siendo opción.

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
- WebP final
- GIF futuro.

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

### Item Mode — first pass
- dedicated projector ItemStack slot;
- normal inventory/hotbar access in GUI;
- Image/Item source toggle;
- server-authoritative persisted ItemStack;
- fullbright vanilla ItemRenderer projection;
- enchantment glint and ordinary modded item model path preserved;
- scale/lift/rotation/float shared with Image Mode;
- stored item drops when the projector is destroyed.

### Projection clearance — first pass
- conservative projection envelope scan;
- image/item aware;
- lift and bob amplitude included;
- GUI warning with number of intersecting non-air blocks.

## Oleada completada en dev.8 — Core / Power

- core slot;
- material profiles: Glass / Quartz / Amethyst / Diamond / Netherite;
- power budget;
- limits for scale, lift, float amplitude and future slot count;
- dynamic core visual in the pedestal;
- Compact hard envelope;
- Creative-only debug chassis override;
- non-destructive invalid configuration behavior.

## Siguiente — Image completeness / preview

- WebP;
- better in-GUI projection preview;
- world-space obstruction overlay instead of warning text only;
- optional fullbright/tint/scanline presentation settings;
- transfer progress/feedback for large assets.

## More chassis

- Mirage Display;
- Wide;
- Tall;
- Field;
- models;
- recipes;
- chassis-specific envelopes.

## Banner + Prism

- banner renderer without pole;
- Mirage Prism;
- four independent lateral faces;
- Prism rotation;
- banner scale and Projection Lift.

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

`0.1.0-dev.8` convierte Projection Core y Projection Power de roadmap a sistema real. El Core es un ItemStack persistido en un socket del BlockEntity y su material se resuelve a un `ProjectionCoreProfile`. `ProjectionPower` evalúa settings sin mutarlos y devuelve estado activo, Power usado/disponible y motivo de fallo. El renderer consulta ese estado antes de dibujar cualquier Mirage y el core físico se dibuja dinámicamente.

Regla nueva confirmada por arquitectura:

> **Cambiar a un Core insuficiente apaga la proyección; jamás borra la configuración.**

Compact mantiene `10 px` como envelope normal de Plane. Para stress-test de desarrollo, Creative puede activar `Debug chassis`, que salta sólo el envelope del chassis; el Core sigue mandando. Esto permite seguir probando imágenes de 3, 5, 8 o 10 bloques sin convertir accidentalmente el Compact final en un projector monumental.
