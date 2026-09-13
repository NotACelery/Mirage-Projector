# NEXT CHAT HANDOFF — 0.1.0-dev.76b

## Tema
Build pipeline con limpieza acumulativa automática antes de cada compilación.

## Cambio principal
`build.bat` ejecuta automáticamente `CLEAN-MIRAGE-PROJECTOR.bat --from-build` antes de buscar/usar Gradle. Si la limpieza devuelve error, la compilación se detiene.

## Contrato del cleanup
- Vive siempre en la raíz como `CLEAN-MIRAGE-PROJECTOR.bat`.
- Se actualiza en cada snapshot cuando exista cualquier archivo legacy que pueda sobrevivir al copiar una entrega encima de otra.
- Es acumulativo e idempotente.
- No usa `pause` al terminar.
- No usa `exit` al final del flujo normal; vuelve al `build.bat` mediante EOF.
- No borra `.gradle-dist`, `run`, mundos ni configuraciones.
- Sí puede borrar `build`, porque es regenerable.
- Mantiene `cleanup-mirage-projector.log`.

## Limpieza dev.76b
Se añadió explícitamente la eliminación de:

`src/main/java/celerbi/mirageprojector/network/MirageLightSourceSyncPayload.java`

Esta clase pertenecía al transporte de descriptors de dev.75 y no forma parte de la arquitectura server-authoritative por sections de dev.76. El tombstone temporal de dev.76a ya no es necesario en el source canónico.

## Arquitectura runtime
Sin cambios respecto a dev.76/dev.76a:
- protocolo 22;
- servidor resuelve `STATIC_WORLD`;
- cliente recibe sections autoritativas empaquetadas a 4 bits por voxel;
- cliente no ejecuta el solver estático;
- `DYNAMIC_VISUAL` queda reservado para 1.1.0.

## QA inmediato
1. Copiar dev.76b encima de una carpeta que todavía tenga `MirageLightSourceSyncPayload.java`.
2. Ejecutar únicamente `build.bat`.
3. Confirmar que el cleanup borra el archivo legacy sin interacción.
4. Confirmar que Gradle comienza automáticamente después.
5. Si build pasa, continuar QA in-game de dev.76.
