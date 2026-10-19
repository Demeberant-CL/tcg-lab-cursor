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
3. Opcional: añade `XAI_API_KEY=...` en `local.properties`, o pega la clave dentro de la app (sección Mazo y asistente).
4. Sincroniza Gradle y ejecuta el módulo `app`.

Imágenes de cartas: CDN de Limitless (`limitlesstcg.../pocket/...`), con respaldos automáticos.

## Datos portados

La carpeta `src/` conserva el TypeScript de referencia. En tiempo de ejecución la aplicación carga:

- `app/src/main/assets/sets.json`
- `app/src/main/assets/cards.json`
- `app/src/main/assets/seed.json`

No reescribas a mano el formato del código QR (`encodeDeckCode`); hay pruebas en `DeckCodeTest`.
