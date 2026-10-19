# Optimización aplicada: seguridad, validación y consistencia de datos

## Hallazgos

1. `app/build.gradle.kts` incorpora `XAI_API_KEY` en `BuildConfig`. Toda clave presente al compilar queda dentro del APK y puede extraerse. Las claves de usuario no deben incluirse en artefactos distribuibles.
2. `ApiKeyStore` guarda la clave en `SharedPreferences` sin cifrado.
3. La herramienta `save_deck` en `GrokClient` clampa cantidades individuales, pero guarda sin verificar el total de 20 cartas, los límites por nombre/entityId, la existencia de cartas ni las energías.

## Cambios aplicados

### Clave API

- Quitar la lectura de `XAI_API_KEY` y el `buildConfigField` del módulo Android; eliminar también el campo de `BuildConfig` en `ApiKeyStore`.
- Conservar el ingreso manual en Ajustes.
- Cifrar el valor guardado con Android Keystore (por ejemplo, `EncryptedSharedPreferences` y `MasterKey`, añadiendo la dependencia AndroidX Security Crypto compatible con el proyecto).
- Hacer que `clear()` borre el valor cifrado y que el estado de Ajustes refleje la presencia de la clave.
- Actualizar `README.md` y `local.properties.example` para dejar claro que una clave de compilación no se admite ni se distribuye.

### Guardado de mazo

- Parsear los argumentos estrictamente: no truncar energías con `take(3)` ni cambiar silenciosamente cantidades inválidas con `coerceIn`.
- Resolver cada referencia con `Catalog.findCard`; rechazar cartas ausentes o sin `entityId` válido para QR.
- Rechazar cantidades fuera de 1..2, mazos que no sumen 20 cartas y cartas que superen las copias disponibles en la colección.
- Rechazar energías vacías, más de tres, IDs fuera de 1..8 y duplicados.
- Validar con `deckProblems(resolveDeck(cards), energies)` en el guardado del asistente y en las pantallas del constructor y hub.
- Devolver las razones de rechazo como resultado de la herramienta para que Grok pueda corregir el mazo.
- Borrar mazos por ID y rechazar cambios de colección que apunten a cartas inexistentes.
- Hacer transaccional el reemplazo CSV y coordinar las pantallas con el fin del bootstrap del catálogo.

## Criterios de aceptación

- El APK de release no contiene valor ni campo de clave API compilado.
- La clave introducida por el usuario se conserva cifrada, se migra desde el almacenamiento anterior y se excluye del backup.
- `save_deck` no persiste mazos inválidos y el resultado de herramienta enumera los motivos.
- Un mazo válido de 20 cartas, con límites de copias y energías válidas, se guarda sin alterar su contenido.
- Ejecutar `gradlew.bat test` y `gradlew.bat assembleDebug` con JDK 17.

## Estado de la rama

Las correcciones anteriores y los hallazgos de la revisión estática se aplicaron en `fix/security-and-deck-validation`. Incluyen exclusión de preferencias cifradas del backup y transferencia del dispositivo, carga del catálogo antes de derivar pantallas, validación de mazos en todas las pantallas, límites por copias disponibles, borrado de mazos por ID, validación de referencias de colección y transacción para importación de reemplazo.

La comprobación final fue estática mediante los archivos de la rama. Aún falta ejecutar `gradlew.bat test` y `gradlew.bat assembleDebug`; el entorno no pudo clonar el repositorio privado con Git.
