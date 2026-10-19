# Archivos para Cursor

Pega el prompt de PROMPT.md en Cursor y deja esta carpeta dentro del proyecto Android. No reescribas el catálogo ni el código del QR a mano.

| Archivo | Para qué |
|---|---|
| src/data/catalog.ts | Expansiones y las cartas, con el entityId que usa el QR |
| src/data/seed.ts | La colección ya importada: set, número y cantidad |
| src/lib/folio/deck-code.ts | Formato exacto del código que el juego escanea |
| src/lib/folio/rules.ts | Mazo de 20, máximo 2 copias, energías |
| src/lib/folio/game.ts | Energías, rarezas y normalización de texto |
| src/lib/folio/csv.ts | Importar y exportar el CSV de Pokémon Zone |
| src/lib/folio/catalog.ts | Cómo se arma la URL de cada imagen |
| src/lib/folio/types.ts | Forma de una carta, una cantidad y un mazo |
