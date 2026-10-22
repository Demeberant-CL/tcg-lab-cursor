# TCG LAB

Aplicación nativa de Android (Kotlin y Jetpack Compose) para el álbum personal de cartas, mazos de 20 y el código QR que el juego importa.

Toda la interfaz está en español neutro (sin voseo).

## Requisitos

- Android Studio (versión reciente)
- JDK 17
- SDK de Android (mínimo 26, objetivo 35)

## Cómo empezar

1. Abre esta carpeta en Android Studio.
2. Si hace falta, copia `local.properties.example` a `local.properties` y ajusta `sdk.dir`.
3. Pega la clave de Grok dentro de la app (sección Ajustes). Se guarda cifrada en el teléfono; no configures claves API en la compilación.
4. Sincroniza Gradle y ejecuta el módulo `app`.

Imágenes de cartas: CDN de Limitless (`limitlesstcg.../pocket/...`), con respaldos automáticos.

## Versionado y actualizaciones

La aplicación usa:

- `versionName`: versión visible para el usuario (por ejemplo, `1.0.2`).
- `versionCode`: número interno que debe aumentar en cada actualización (por ejemplo, `3`).

Para que Android permita instalar una versión encima de otra deben mantenerse el mismo `applicationId` y la misma clave de firma. TCG LAB mantiene el mismo `applicationId` (`com.tcglab.app`) y el proyecto ya admite una clave Release persistente mediante variables de entorno.

### Firma Release para GitHub Actions

No guardes un keystore, contraseña ni clave privada dentro del repositorio.

Configura estos GitHub Actions Secrets:

- `TCG_LAB_KEYSTORE_BASE64`
- `TCG_LAB_KEYSTORE_PASSWORD`
- `TCG_LAB_KEY_ALIAS`
- `TCG_LAB_KEY_PASSWORD`

El workflow decodifica temporalmente el keystore y genera `app-release.apk`. Si los secretos no están configurados, CI continúa generando el APK Debug y las pruebas, pero no un Release firmado.

Si existe una instalación antigua firmada con otra clave, será necesario desinstalarla una vez e instalar la primera versión firmada con la clave permanente. A partir de esa instalación, las versiones posteriores podrán actualizarse normalmente mientras se conserve la misma clave.

## Datos portados

La carpeta `src/` conserva el TypeScript de referencia. En tiempo de ejecución la aplicación carga:

- `app/src/main/assets/sets.json`
- `app/src/main/assets/cards.json`
- `app/src/main/assets/seed.json`

No reescribas a mano el formato del código QR (`encodeDeckCode`); hay pruebas en `DeckCodeTest`.

<!-- CI trigger: provider integration verification -->
