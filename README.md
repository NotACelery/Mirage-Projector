# Mirage Projector

**Minecraft 1.21.1 · NeoForge 21.1.244 · Java 21**  
Versión de desarrollo actual: **0.1.0-dev.41**  
Protocolo de red: **18**

Mirage Projector añade proyectores físicos capaces de mostrar imágenes, GIFs, items, banners y snapshots de entidades sin spawnear/tickear copias reales de las entidades proyectadas.

> `dev.41` es una oleada de consolidación. El sistema de Power dev.38, Image/GIF dev.39 y normalización Entity dev.40 siguen siendo la implementación activa. La nueva progresión de Cut Obsidian Shards, upgrades e Improved Cores está definida en `docs/CORES-AND-UPGRADES-dev41.md`, pero todavía no está implementada salvo que se indique expresamente.

## Estado de QA

- dev.40 fue ejecutada in-game.
- El temblor de Piglin proyectado en dimensiones no piglin-safe quedó confirmado como solucionado.
- Siguen pendientes el redesign visual del Core/Glass del chassis y una regresión completa de profundidad Entity/agua/projectors antes de considerar una release estable.
- No marcar dev.41 como build-clean hasta ejecutar `build.bat` en Windows y hacer QA in-game.

---

# Proyectores actuales

| Chassis | Uso principal | Nominal W×H | Lift nominal | Float nominal | Multiplicador Power |
|---|---|---:|---:|---:|---:|
| Mirage Projector | proyección compacta | 10×10 px | 32 px | 4 px | ×1.00 |
| Mirage Display | display mediano/cuadrado | 32×32 px | 48 px | 12 px | ×1.50 |
| Wide Mirage Projector | panoramas o 4 fuentes horizontales | 80×32 px | 64 px | 12 px | ×2.00 |
| Tall Mirage Projector | imágenes altas o 4 fuentes verticales | 32×80 px | 96 px | 16 px | ×2.00 |
| Mirage Field Projector | una proyección 2D masiva | 128×128 px | 144 px | 24 px | ×4.00 |
| Mirage Prism | cuatro caras N/E/S/W | adaptativo | 96 px | 12 px | ×2.00 |

Los valores nominales son **zonas de eficiencia**, no hard caps. Con PU suficiente se pueden superar mediante Overdrive, pagando una penalización progresiva.

Todos los chassis nuevos se colocan con orientación horizontal tipo Furnace. Las proyecciones Plane respetan esa orientación. Prism conserva North/East/South/West como caras cardinales del mundo.

Cuando no existe una fuente renderizable, los seis chassis muestran el mismo libro vanilla flotante como estado idle.

---

# Projection Cores / Power

Cores estándar implementados:

| Core | Base PU | Amplificación actual |
|---|---:|---:|
| Glass | 32 | ×1.00 |
| Quartz | 48 | ×1.00 |
| Amethyst | 64 | ×1.00 |
| Diamond | 96 | ×1.00 |
| Netherite | 128 | ×1.00 |

Capacidad efectiva:

```text
Effective PU = floor(Base Core PU × Chassis Multiplier × Core Amplification)
```

Scale/Lift/Float calculan su máximo dinámicamente en función de la capacidad real disponible y del resto de settings. El jugador no debería tener que arrastrar un slider a una zona inválida y tantear hacia atrás.

El coste actual incluye:

- emisor/estabilidad;
- área geométrica;
- Lift;
- Float;
- complejidad de fuente;
- features de presentación;
- Overdrive cuadrático al superar nominal;
- un ahorro Ghost muy pequeño, diseñado para tener sentido temático sin ser explotable.

Contrato completo: `docs/POWER-SYSTEM-REWORK-dev38.md`.

### Próximo redesign de Core

El renderer actual todavía transforma el material del Core en un bloque visual pequeño (`Netherite Ingot -> Netherite Block`, etc.). Esa representación está **deprecada como diseño final**.

La definición futura es una Core Chamber de vidrio pequeña (~4×4×4 px) con el ItemStack real flotando/girando dentro. Ver `docs/CORES-AND-UPGRADES-dev41.md`.

---

# Image / GIF Mode

Formatos implementados:

- PNG;
- JPG/JPEG;
- WebP estático;
- BMP;
- GIF animado.

Mirage detecta el **contenido real**, no confía en la extensión. Un GIF renombrado `.png` sigue siendo GIF.

Por ahora:

- Animated WebP: detectado y rechazado explícitamente;
- APNG: detectado y rechazado explícitamente;
- no se degrada silenciosamente un formato animado no soportado al primer frame.

Nuevos assets usan `<sha256>.asset`; assets legacy `<sha256>.png` continúan siendo compatibles.

## Wide / Tall

Wide y Tall tienen dos layouts de Image:

- `SINGLE`: una imagen/GIF continua, sin crop ni stretch;
- `MULTI`: cuatro fuentes independientes.

Wide MULTI = `4×1`.  
Tall MULTI = `1×4`.

Las celdas MULTI tienen el mismo tamaño relativo en ambos chassis.

## Field

Field muestra **una sola imagen/GIF continua**. El antiguo grid 3×3 de dev.33-dev.37 fue una interpretación incorrecta y no es comportamiento activo.

## Prism

Prism siempre sigue siendo un prisma de cuatro caras laterales:

- North;
- East;
- South;
- West.

Una fuente por cara, sin stacking 4×1/1×4.

Las caras Image usan nominal adaptativo según aspect ratio:

- horizontal claro: 80×32;
- vertical claro: 32×80;
- casi cuadrado: 48×48.

Detalles: `docs/GIF-ANIMATED-IMAGE-dev39.md`, `docs/IMAGE-FORMAT-IMPORT-CONTRACT-dev39.md`, `docs/WIDE-TALL-SINGLE-ASPECT-dev39.md`, `docs/PRISM-ADAPTIVE-ASPECT-dev39.md`.

---

# Item Mode

- el item real no queda almacenado como la proyección;
- Mirage crea un snapshot virtual;
- armor standalone se representa equipada sobre un rig invisible cuando corresponde;
- presentation settings globales siguen aplicando sobre la proyección.

---

# Banner Mode

- snapshots virtuales, sin consumir el banner real;
- Plane usa la tela/patrones del banner;
- Prism puede usar las cuatro caras N/E/S/W;
- el poste/travesaño vanilla no forma parte de la proyección.

---

# Entity Mode

Entity Scan reconstruye entidades **sólo para render en cliente**:

- no se añaden al `ClientLevel` como entidades vivas;
- no tienen AI;
- no ejecutan `tick()` / `aiStep()`;
- el snapshot sigue siendo válido aunque la entidad original desaparezca.

El gesto de scan actual usa Empty Scan Template y Shift + right click.

Humanoides poseen canales virtuales independientes:

- Head;
- Chest;
- Legs;
- Feet;
- Main Hand;
- Off Hand.

Horse usa Saddle / Body Armor. Otras entidades sólo exponen canales que realmente tengan sentido.

Al cambiar a una familia de entidad incompatible, los snapshots virtuales de slots que dejan de existir se limpian para no retener memoria/estado invisible. El staging físico se devuelve/dropea de forma segura.

Piglin/Hoglin reconstruidos son normalizados sólo en la copia Mirage para impedir el shake de conversión por dimensión; el usuario confirmó el fix in-game en dev.40.

---

# Nueva progresión definida para próximas oleadas

`docs/CORES-AND-UPGRADES-dev41.md` es la autoridad de diseño para:

- **Cut Obsidian Shard**;
- 1 Crying Obsidian -> 4 shards mediante Stonecutter;
- re-formado con 8 shards + Fire Charge/Magma Cream;
- shard loot en Ruined Portals, Mineshafts, smith-related chests y fuentes temáticas;
- transformación natural lenta Obsidian -> Crying Obsidian con lava + dripstone + cauldron;
- dos estados intermedios visuales y progreso opcional Jade;
- **Obsidian Spike** (3 shards + 2 String + 1 Stick, nueve puntas, 2 damage por hurt event);
- eliminación del lenguaje visual de grandes paneles de Glass en los chassis;
- receta base del Mirage Projector;
- upgrade obligatorio Mirage Projector -> Mirage Display;
- Display -> Wide/Tall/Prism/Field;
- recetas de upgrade que preservan todo el estado/imports/snapshots;
- cinco Improved Cores como bloques decorativos/instalables;
- shells morados anidados con shell intermedio realmente rotado;
- Improved Core = misma Base PU + amplificación (~×1.50 target);
- interacción con Beacon beam: relay en Y+0.5, haz más ancho, efectos por material y stacking limitado;
- Scan Codex / copia de scans diferido a 1.1.0+.

Estas features están **diseñadas, no implementadas todavía** en dev.41.

---

# Problemas / backlog prioritario

1. Core actual desaparece en ciertos ángulos y usa un visual de bloque artificial: reemplazar por Core Chamber + item real.
2. Modelos actuales tienen capas amplias de Glass/Glass Pane que se leen como layers fantasma: rediseñar con Cut Obsidian + Glass sólo en Chamber.
3. Implementar Cut Obsidian Shard / progresión de crafting / upgrades preservando estado.
4. Improved Cores + Beacon relay después de estabilizar materiales/modelos.
5. QA de profundidad Entity vs agua/projectors y RenderTypes especiales/modded.
6. QA de rendimiento/transferencia de muchos GIFs.
7. Futuro 1.1.0: Mirage Scan Codex + estación de copia a tarjetas.

---

# Compilar

Requisitos:

- Java 21;
- Windows: ejecutar `build.bat` desde la raíz del proyecto.

El proyecto usa:

- Minecraft 1.21.1;
- NeoForge 21.1.244;
- Gradle 9.2.1;
- Parchment 2024.11.17.

No incluir `.gradle`, `.gradle-dist`, `build`, `run` u otros caches en snapshots source.

---

# Documentación

Para saber qué documento manda cuando hay notas históricas contradictorias, leer primero:

`docs/DOCUMENTATION-AUTHORITY-dev41.md`

Inventario exacto de implementación actual:

`docs/CURRENT-IMPLEMENTATION-AUDIT-dev41.md`

Historial cronológico:

`CHANGELOG.md`
