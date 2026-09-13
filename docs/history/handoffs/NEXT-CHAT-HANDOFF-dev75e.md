# NEXT CHAT HANDOFF — 0.1.0-dev.75e

## Tema de esta snapshot
Línea paralela de comparación visual para los seis proyectores Mirage.

## Estado
Se implementó una segunda familia de bloques/item blocks para comparación lado a lado in-game, sin reemplazar los proyectores originales.

### Nuevos bloques/items registrados
- `mirage_projector_alt`
- `mirage_display_alt`
- `wide_mirage_projector_alt`
- `tall_mirage_projector_alt`
- `mirage_field_projector_alt`
- `mirage_prism_alt`

## Objetivo
Permitir QA visual directo de:
1. modelo en el mundo,
2. icono de inventario,
3. apariencia en mano,
4. comparación lado a lado con los chasis actuales.

## Diseño aplicado
- Se reutilizó la misma `MirageProjectorBlock` y la misma `MirageProjectorBlockEntity`.
- Los nuevos bloques comparten la misma lógica de GUI, sources, projection state, core slot, etc.
- Sólo cambia la identidad visual/modelo.
- Se usaron texturas vanilla para geometría estructural:
  - `minecraft:block/crying_obsidian`
  - `minecraft:block/obsidian`
  - `minecraft:block/purple_stained_glass`
- Se mantuvo la textura emissive existente del mod para el glow violeta:
  - `mirage_projector:block/crying_obsidian_emitter`

## Alcance de la implementación
- Registro de bloques e ítems alt.
- Inclusión en creative tab del mod y en Functional Blocks vanilla.
- Inclusión de los nuevos bloques en el `BlockEntityType` de proyectores.
- Mapeo de `chassisProfile()` actualizado para que Display/Wide/Tall/Field/Prism alt se comporten como su equivalente original.
- Blockstates, block models e item models implementados.
- Loot tables de drops implementadas.
- Nombres localizados ES/EN añadidos.

## Deliberadamente no hecho aún
- No se agregaron recetas survival para la línea alt; la intención es comparación/QA visual, no reemplazo definitivo todavía.
- No se modificaron aún los shapes de colisión para alinearlos exactamente a los nuevos modelos; siguen los envelopes funcionales por perfil.
- No se reemplazaron los modelos originales.

## Qué validar in-game
1. Los 6 originales y los 6 alt aparecen en creative.
2. Los alt se renderizan correctamente en:
   - inventario
   - mano
   - suelo/mundo
3. Cada alt abre la GUI correcta y conserva todas las funciones del proyector base correspondiente.
4. El core por defecto del compact alt sigue apareciendo igual que en el compact normal.
5. Wide/Tall alt respetan facing y no quedan rotados raro.
6. Prism alt y Field alt se distinguen claramente del Display alt.
7. Al romperlos, dropean su propio bloque/item correspondiente.

## Siguiente decisión esperada
Comparar visualmente original vs alt y decidir por cada chasis:
- mantener original,
- adoptar alt,
- mezclar ideas de ambos,
- o hacer una tercera pasada de modelado.
