# TCG LAB

Aplicación web en React (Vite, TypeScript y Tailwind CSS) para el álbum personal de cartas, mazos de 20 cartas y generación del código QR que Pokémon TCG Pocket importa directamente.

Toda la interfaz está en español neutro (sin voseo).

## Características

- **Centro de juego**: Resumen de colección, porcentaje de completitud, estadísticas de cartas distintas, copias totales y Pokémon ex.
- **Colección**:
  - Lista de todas las expansiones oficiales de Pocket (Genes Formidables, La Isla Singular, Pugna Espaciotemporal, Promo A/B, etc.).
  - Cuadrícula de 3 columnas dentro de cada set con búsqueda rápida por nombre o por código (ej. `A1 36`).
  - Filtros por cartas: Todas, Tengo, Me faltan, Rastreadas.
  - Ficha de detalle de carta con control de copias (`-` / `+`), indicador de tipo elemental y adición directa al mazo.
  - Importación y exportación de CSV compatible con Pokémon Zone (opciones de *Combinar* o *Reemplazar*).
- **Mazos**:
  - Creador y editor de mazos de 20 cartas.
  - Selector de 1 a 3 energías (Planta, Fuego, Agua, Rayo, Psíquica, Lucha, Oscura, Metálica).
  - Contador con 20 marcas visuales.
  - Validación de reglas en tiempo real (exactamente 20 cartas, máx. 2 copias por nombre o `entityId`, 1-3 tipos de energía).
  - Generador de código QR compatible con el escáner del juego (*Mazos > Crear > Escanear código*), con descarga en PNG y copiado de Base64.
- **TCG LAB AI**:
  - Asistente de creación y optimización de mazos con compatibilidad para Grok (xAI), OpenAI, Gemini y Groq.
  - Soporte de herramientas en tiempo real (`search_catalog`, `list_owned`, `update_collection`, `save_deck`, `delete_deck`).
  - Motor inteligente integrado sin depender forzosamente de claves externas.
- **Ajustes**:
  - Tema claro, oscuro o del sistema.
  - Gestión de claves de API.
  - Exportación de registro técnico.

## Desarrollo

```bash
npm install
npm run dev
```

El servidor de desarrollo corre en el puerto 3000 (`0.0.0.0:3000`).
Para construir la versión de producción:

```bash
npm run build
```
