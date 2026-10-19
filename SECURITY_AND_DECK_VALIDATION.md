# Optimización aplicada: seguridad, validación y consistencia de datos

## Hallazgos

1. `app/build.gradle.kts` incorporaba `XAI_API_KEY` en `BuildConfig`. Toda clave presente al compilar queda dentro del APK y puede extraerse. Las claves de usuario no deben incluirse en artefactos distribuibles.
2. `ApiKeyStore` guardaba la clave en `SharedPreferences` sin cifrado.
3. La herramienta `save_deck` en `GrokClient` limitaba cantidades individuales, pero guardaba sin verificar el total de 20 cartas, los límites por nombre/entityId, la existencia de cartas ni las energías.

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
- El reemplazo CSV es transaccional y las pantallas esperan a que termine el bootstrap del catálogo.

## Criterios de aceptación

- El APK de release no contiene valor ni campo de clave API compilado.
- La clave introducida por el usuario se conserva cifrada, se migra desde el almacenamiento anterior y se excluye del backup.
- `save_deck` no persiste mazos inválidos y el resultado de herramienta enumera los motivos.
- Un mazo válido de 20 cartas, con límites de copias y energías válidas, se guarda sin alterar su contenido.
- Ejecutar `gradlew.bat test` y `gradlew.bat assembleDebug` con JDK 17.

## Estado de la rama

Las correcciones y los hallazgos de la revisión estática se aplicaron en `fix/security-and-deck-validation`. Incluyen el cifrado con Android Keystore, exclusión de preferencias del backup, carga del catálogo antes de derivar pantallas, validación de mazos en todas las pantallas, límites por copias disponibles, borrado de mazos por ID, validación de referencias de colección y transacción para importación de reemplazo.

La comprobación final fue estática mediante los archivos de la rama. Aún falta ejecutar `gradlew.bat test` y `gradlew.bat assembleDebug`; el entorno no pudo clonar el repositorio privado con Git.
