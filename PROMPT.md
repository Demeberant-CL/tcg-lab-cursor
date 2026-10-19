Construye TCG LAB, una app nativa de Android en Kotlin y Jetpack Compose. No es un WebView ni una PWA. Una sola Activity, Navigation Compose, Material 3, minSdk 26, targetSdk actual.

Idioma de toda la interfaz: español neutro. Sin voseo, sin emojis, sin texto de relleno.

Producto: álbum personal de Pokémon TCG Pocket. El usuario importa su colección, busca cartas, arma mazos de 20 cartas a mano o con Grok, y genera el QR que el juego importa en Mazos, Crear, Escanear código.

La interfaz de la versión web no se copia. Esta app tiene que verse clara, suave, ordenada y con poco ruido, como el juego.

En este mismo paquete están los archivos que hay que portar, no reescribir:
- src/data/catalog.ts
- src/data/seed.ts
- src/lib/folio/deck-code.ts
- src/lib/folio/rules.ts
- src/lib/folio/game.ts
- src/lib/folio/csv.ts
- src/lib/folio/catalog.ts
- src/lib/folio/types.ts

Interfaz
- Fondo gris muy claro con un tono azul, cerca de #E7EEF3. No negro, no dorado, no púrpura.
- Superficies blancas azuladas, un poco elevadas, con sombra suave y radio amplio.
- Texto azul grisáceo oscuro, #1C2833. Un solo botón principal: ese mismo tinta, con texto claro.
- Color de energía solo en un punto de 8 dp junto al nombre. Planta, fuego, agua, rayo, psíquica, lucha, oscura, metálica.
- Una sola familia tipográfica, geométrica y redondeada, por ejemplo Plus Jakarta Sans. Números en tabular-nums.
- Mucho aire. Una acción principal por pantalla. Toques de al menos 48 dp.
- Barra inferior clara, fija: Colección, Mazos, Grok.
- Arriba, discreto: TCG LAB. En la colección, el título es "Colección". Nunca "Tu colección".

Pantallas

Colección
- No abras con una pared de 1.500 miniaturas.
- Primero una lista de expansiones. Cada fila: nombre en español, código, "128/286" y una barra fina. Al tocarla, entra solo esa expansión.
- Dentro: cuadrícula de 3 columnas en el teléfono. Búsqueda fija por nombre en inglés o por "A1 36".
- Filtros solo ahí: Tengo, Me faltan, Rastreadas. "Me faltan" nunca muestra todas las cartas de todos los sets a la vez.
- Arriba de la lista, tres cifras: cartas distintas, copias y ex. Un ex es rareza RR o un nombre que termina en " ex".
- La ficha sale desde abajo: imagen, cantidad con menos y más, sumar a un mazo, cerrar.
- Menú corto: importar CSV, exportar CSV, volver al archivo original.

Mazos
- Lista simple. Cada mazo muestra nombre, X/20 y las energías. Botón Nuevo.
- Editor, de arriba a abajo: nombre, energías de 1 a 3, contador X/20 con veinte marcas, cartas del mazo, y una cuadrícula para sumar entre las que ya tiene.
- Filtros al sumar: Todas, los ocho tipos y Entrenador, más búsqueda.
- El botón Código QR solo se activa si el mazo está listo. Si no, una línea dice qué falta.
- El QR va en una hoja inferior, grande, con Descargar. Texto: en el juego, Mazos, Crear, Escanear código. Si el código está en el mismo teléfono, hay que abrirlo en otra pantalla.

Grok
- Conversación limpia. Sugerencias como filas.
- Sugerencias: "Arma un mazo de fuego con las cartas que tengo y déjalo listo para el QR", "Arma un mazo de agua de 20 cartas, solo con lo que tengo", "¿Qué ex de rayo tengo?".
- Al guardar un mazo, un botón abre ese mazo. Si cambian cantidades, muestra de hacia a.
- Una línea fija: cada mensaje manda un resumen, no toda la colección.

Reglas del juego
- Un mazo jugable tiene exactamente 20 cartas.
- Máximo 2 copias del mismo nombre. Dos ilustraciones del mismo nombre cuentan juntas. También cuentan juntas si comparten entityId.
- Energías, id fijo: 1 Planta, 2 Fuego, 3 Agua, 4 Rayo, 5 Psíquica, 6 Lucha, 7 Oscura, 8 Metálica. De 1 a 3. Nunca dragón ni incolora. No son cartas del mazo.
- El QR solo sale con 20 cartas del catálogo, cada una con entityId, y 1 a 3 energías.

Datos
- Room, en el teléfono. Sin cuenta obligatoria.
- Porta SETS y RAW_CARDS desde catalog.ts.
- Cada carta: set, number, name en inglés, rarity, entityId, trainer, element.
- Códigos canónicos: A1, A1a, A2, A2a, A2b, A3, A3a, A3b, A4, A4a, A4b, B1, B1a, B2, B2a, B2b, B3, B3a, B3b, B4, B4a, B4b, PROMO-A, PROMO-B. "A1A" y "promo-b" se normalizan a esos códigos.
- Semilla inicial: seed.ts. Si ya hay datos guardados, no los pises.
- Imagen: la URL está en catalog.ts. PROMO-A usa P-A y PROMO-B usa P-B. Coil, caché en disco. Si falla, muestra nombre y número.

CSV de Pokémon Zone
Columnas: Set, Número, Nombre, Rareza, Cantidad, Registrada, Link.
La rareza llega como rarity-C o rarity-RR. Guarda solo C, U, R, RR, AR, SR, SAR, IM, UR, S, SSR.
Cantidad 0 significa rastreada y no la tiene. Al importar, pregunta Combinar o Reemplazar.

Código del mazo
Porta encodeDeckCode de deck-code.ts sin cambiar el formato.
- Separan entrenadores y pokémon.
- Se ordenan por entityId.
- Bytes: cantidad de entrenadores, cada id de entrenador más 10000000 como uint24 big-endian, cantidad de pokémon, cada entityId de pokémon como uint24, cantidad de energías, ids de energía del 1 al 8.
- Cada copia es una entrada. El resultado es Base64 estándar.
- Un test tiene que coincidir con la función original.

Grok
- La clave XAI_API_KEY va en local.properties y BuildConfig. Nunca en el repositorio.
- POST https://api.x.ai/v1/chat/completions, modelo grok-4.5, temperature 0.3, max_tokens 1600.
- No envíes las 1.583 cartas en cada mensaje. Cada mensaje lleva solo un resumen: cartas distintas, copias, cantidad por set y por tipo, y los mazos ya guardados.
- Herramientas: search_catalog, list_owned con máximo 60 y filtros de tipo, set, texto o ex, update_collection, save_deck y delete_deck.
- Antes de guardar un mazo, el modelo pide list_owned del tipo que va a usar. No inventa set ni número.
- Fotos comprimidas, máximo 2. Un CSV de colección se importa directo, sin llamar al modelo.
- Si no hay clave, la pantalla lo dice y el resto de la app sigue offline.
- No llames a la API al abrir la app ni al escribir cada letra.

No hagas
- No uses WebView.
- No copies una interfaz oscura.
- No uses arte oficial ni la palabra Pokémon en el icono. El icono es el texto TCG LAB sobre el fondo claro.
- No cambies el formato del QR.

Orden
1. Tema claro, navegación y Room.
2. Catálogo, semilla y lista de expansiones.
3. Cuadrícula de una expansión, ficha y CSV.
4. Editor de mazos.
5. QR con tests.
6. Chat de Grok con resumen corto.
