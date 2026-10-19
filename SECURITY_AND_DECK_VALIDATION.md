# Optimización aplicada: seguridad, validación y consistencia de datos

## Hallazgos

1. `app/build.gradle.kts` incorporaba `XAI_API_KEY` en `BuildConfig`. Toda clave presente al compilar queda dentro del APK y puede extraerse. Las claves de usuario no deben incluirse en artefactos distribuibles.
2. `ApiKeyStore` guardaba la clave en `SharedPreferences` sin cifrado.
3. La herramienta `save_deck` en `GrokClient` limitaba cantidades individuales, pero guardaba sin verificar el total de 20 cartas, los límites por nombre/entityId, la existencia de cartas ni las energías.
4. Un CSV vacío o mal formado podía interpretarse como una colección vacía y borrar las tenencias al usar “Reemplazar”.

## Cambios aplicados

### Clave API

- Se quitó la lectura de `XAI_API_KEY` y el `buildConfigField` del módulo Android.
- Se conserva el ingreso manual en Ajustes.
- La clave se cifra con AES/GCM usando una clave generada y protegida por Android Keystore.
- Se migra la clave guardada previamente en `tcg_lab_secure`; las preferencias anteriores y actuales se excluyen de backup y transferencia del dispositivo.
- Se eliminó la dependencia AndroidX Security Crypto, cuya API de preferencias cifradas está obsoleta.
- Se actualizaron `README.md` y `local.properties.example` para indicar que la clave no se compila ni distribuye.

### Guardado de mazo

- Se validan los argumentos sin truncar energías ni ajustar cantidades inválidas silenciosamente.
- Cada referencia se resuelve con `Catalog.findCard`; se rechazan cartas ausentes o sin `entityId` válido para QR.
- Se rechazan cantidades fuera de 1..2, mazos que no sumen 20 cartas y cartas que superen las copias disponibles en la colección.
- Se rechazan energías vacías, más de tres, IDs fuera de 1..8 y duplicados.
- Se valida con `deckProblems(resolveDeck(cards), energies)` en el guardado del asistente y en las pantallas del constructor y hub.
- El resultado de la herramienta enumera los motivos de rechazo para que Grok pueda corregir el mazo.
- Se borran mazos por ID y se rechazan cambios de colección que apunten a cartas inexistentes.

### Importación CSV

- Se rechazan filas con set vacío, número inválido o cantidad fuera de 0..99; no se importan parcialmente archivos mal formados.
- Un archivo sin filas de cartas devuelve un mensaje visible y no modifica la colección.
- El repositorio también ignora listas vacías como defensa adicional.
- El reemplazo y la combinación se aplican en una transacción.
- Se añadieron pruebas unitarias para CSV vacío, encabezado sin datos, filas inválidas, comillas sin cerrar y una fila válida.

## Criterios de aceptación

- El APK de release no contiene valor ni campo de clave API compilado.
- La clave introducida por el usuario se conserva cifrada, se migra desde el almacenamiento anterior y se excluye del backup.
- `save_deck` no persiste mazos inválidos y el resultado de herramienta enumera los motivos.
- Un mazo válido de 20 cartas, con límites de copias y energías válidas, se guarda sin alterar su contenido.
- Un CSV vacío o mal formado no puede eliminar la colección.

## Estado de la rama

Las correcciones se aplicaron en `fix/security-and-deck-validation`. Incluyen cifrado con Android Keystore, exclusión de preferencias del backup, carga del catálogo antes de derivar pantallas, validación de mazos en todas las pantallas y en el asistente, límites por copias disponibles, borrado por ID, validación de referencias de colección y protección de importación CSV.

La clonación del PR y la preparación del JDK 17 se completaron. No se pudo ejecutar `gradlew.bat test` ni `gradlew.bat assembleDebug`: Gradle falla antes de configurar el proyecto con `Unable to establish loopback connection` / `Invalid argument: connect` al inicializar su selector de Windows. La ejecución automatizada sigue pendiente.
