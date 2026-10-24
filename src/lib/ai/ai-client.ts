import { canonSet, CARDS, findCard, searchCards } from "@/lib/folio/catalog";
import { deckProblems, qtyOf, resolveDeck } from "@/lib/folio/rules";
import { fold, isExCard } from "@/lib/folio/game";
import type { CollectionChange, Deck, DeckCard, Holding } from "@/lib/folio/types";
import { repository, type AiProviderId } from "@/lib/repository";

export interface AiResponse {
  text: string;
  changes: CollectionChange[];
  savedDeck: Deck | null;
  deletedDeckId: string | null;
}

const PROVIDER_CONFIGS: Record<
  AiProviderId,
  {
    name: string;
    url: string;
    models: string[];
    defaultModel: string;
  }
> = {
  grok: {
    name: "Grok",
    url: "https://api.x.ai/v1/chat/completions",
    models: ["grok-4.7", "grok-4.5", "grok-4", "grok-3-mini", "grok-beta"],
    defaultModel: "grok-4.5",
  },
  openai: {
    name: "OpenAI",
    url: "https://api.openai.com/v1/chat/completions",
    models: ["gpt-4o-mini", "gpt-4o"],
    defaultModel: "gpt-4o-mini",
  },
  gemini: {
    name: "Gemini",
    url: "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
    models: ["gemini-2.0-flash", "gemini-1.5-flash"],
    defaultModel: "gemini-2.0-flash",
  },
  groq: {
    name: "Groq",
    url: "https://api.groq.com/openai/v1/chat/completions",
    models: ["llama-3.3-70b-versatile"],
    defaultModel: "llama-3.3-70b-versatile",
  },
};

export const AI_TOOLS = [
  {
    type: "function",
    function: {
      name: "search_catalog",
      description: "Busca cartas en el catálogo de Pocket por nombre o set.",
      parameters: {
        type: "object",
        properties: {
          query: { type: "string", description: "Término de búsqueda o código (ej. Charizard o A1 36)" },
          set: { type: "string", description: "Código de expansión opcional (ej. A1)" },
          limit: { type: "integer", description: "Límite de resultados (máximo 40)" },
        },
        required: ["query"],
      },
    },
  },
  {
    type: "function",
    function: {
      name: "list_owned",
      description: "Lista cartas que el usuario tiene en su colección (máximo 60).",
      parameters: {
        type: "object",
        properties: {
          element: { type: "string", description: "Tipo elemental (grass, fire, water, lightning, psychic, fighting, darkness, metal)" },
          set: { type: "string", description: "Código de set (ej. A1)" },
          text: { type: "string", description: "Texto a filtrar en el nombre" },
          ex_only: { type: "boolean", description: "Filtrar solo cartas Pokémon ex" },
        },
      },
    },
  },
  {
    type: "function",
    function: {
      name: "update_collection",
      description: "Actualiza cantidades de cartas en la colección del usuario.",
      parameters: {
        type: "object",
        properties: {
          updates: {
            type: "array",
            items: {
              type: "object",
              properties: {
                set: { type: "string" },
                number: { type: "integer" },
                qty: { type: "integer", description: "Nueva cantidad entre 0 y 99" },
              },
              required: ["set", "number", "qty"],
            },
          },
        },
        required: ["updates"],
      },
    },
  },
  {
    type: "function",
    function: {
      name: "save_deck",
      description: "Guarda un mazo válido de exactamente 20 cartas para el código QR.",
      parameters: {
        type: "object",
        properties: {
          name: { type: "string", description: "Nombre del mazo" },
          energies: {
            type: "array",
            items: { type: "integer" },
            description: "1 a 3 IDs de energía (1 Planta, 2 Fuego, 3 Agua, 4 Rayo, 5 Psíquica, 6 Lucha, 7 Oscura, 8 Metálica)",
          },
          cards: {
            type: "array",
            items: {
              type: "object",
              properties: {
                set: { type: "string" },
                number: { type: "integer" },
                count: { type: "integer", description: "1 o 2 copias" },
              },
              required: ["set", "number", "count"],
            },
          },
        },
        required: ["name", "energies", "cards"],
      },
    },
  },
  {
    type: "function",
    function: {
      name: "delete_deck",
      description: "Elimina un mazo por su id.",
      parameters: {
        type: "object",
        properties: {
          id: { type: "string", description: "Identificador del mazo" },
        },
        required: ["id"],
      },
    },
  },
];

// Execute a tool call against the live local collection state
export function executeTool(
  name: string,
  args: Record<string, any>,
  workingHoldings: Map<string, number>,
  workingDecks: Deck[],
): { result: string; changeList: CollectionChange[]; newDeck: Deck | null; deletedId: string | null } {
  let changeList: CollectionChange[] = [];
  let newDeck: Deck | null = null;
  let deletedId: string | null = null;

  switch (name) {
    case "search_catalog": {
      const q = String(args.query || "");
      const set = args.set ? String(args.set) : undefined;
      const limit = Math.min(40, Number(args.limit) || 20);
      const hits = searchCards(q, { set, limit });
      return {
        result: JSON.stringify(
          hits.map((c) => ({
            set: c.set,
            number: c.number,
            name: c.name,
            rarity: c.rarity,
            element: c.element,
            trainer: c.trainer,
          })),
        ),
        changeList,
        newDeck,
        deletedId,
      };
    }

    case "list_owned": {
      const element = args.element ? fold(String(args.element)) : null;
      const set = args.set ? canonSet(String(args.set)) : null;
      const text = args.text ? fold(String(args.text)) : null;
      const exOnly = Boolean(args.ex_only);

      const rows: Array<{ set: string; number: number; name: string; qty: number; element: string; rarity: string }> = [];

      for (const [key, qty] of workingHoldings.entries()) {
        if (qty <= 0) continue;
        const [s, nStr] = key.split("#");
        const num = Number(nStr);
        const card = findCard(s, num);
        if (!card) continue;
        if (set && card.set !== set) continue;
        if (element) {
          const cardType = card.trainer ? "trainer" : fold(card.element);
          if (cardType !== element && fold(card.element) !== element) continue;
        }
        if (text && !fold(card.name).includes(text)) continue;
        if (exOnly && !isExCard(card.name, card.rarity)) continue;

        rows.push({
          set: card.set,
          number: card.number,
          name: card.name,
          qty,
          element: card.element,
          rarity: card.rarity,
        });

        if (rows.length >= 60) break;
      }

      return {
        result: JSON.stringify(rows),
        changeList,
        newDeck,
        deletedId,
      };
    }

    case "update_collection": {
      const updates = Array.isArray(args.updates) ? args.updates : [];
      if (!updates.length || updates.length > 60) {
        return { result: JSON.stringify({ error: "Debes enviar entre 1 y 60 cartas." }), changeList, newDeck, deletedId };
      }
      for (const u of updates) {
        const s = canonSet(String(u.set));
        const n = Number(u.number);
        const q = Math.max(0, Math.min(99, Number(u.qty)));
        const card = findCard(s, n);
        if (!card) continue;
        const key = `${s}#${n}`;
        const from = workingHoldings.get(key) ?? 0;
        workingHoldings.set(key, q);
        changeList.push({
          set: s,
          number: n,
          name: card.name,
          from,
          to: q,
        });
      }
      return {
        result: JSON.stringify({ ok: true, updated: changeList.length }),
        changeList,
        newDeck,
        deletedId,
      };
    }

    case "save_deck": {
      const name = String(args.name || "Mazo").trim();
      const energies = Array.isArray(args.energies) ? args.energies.map(Number) : [];
      const cardsIn = Array.isArray(args.cards) ? args.cards : [];

      if (!name) throw new Error("El mazo requiere un nombre.");
      if (energies.length < 1 || energies.length > 3) throw new Error("Elige entre 1 y 3 energías.");
      if (new Set(energies).size !== energies.length) throw new Error("No repitas tipos de energía.");

      const cards: DeckCard[] = [];
      for (const c of cardsIn) {
        const s = canonSet(String(c.set));
        const n = Number(c.number);
        const count = Number(c.count);
        const known = findCard(s, n);
        if (!known) throw new Error(`Carta desconocida: ${s} ${n}`);
        cards.push({ set: known.set, number: n, count });
      }

      // Check user has enough copies
      const holdingsList: Holding[] = [];
      for (const [k, qty] of workingHoldings.entries()) {
        const [s, nStr] = k.split("#");
        holdingsList.push({ set: s, number: Number(nStr), qty });
      }

      for (const entry of cards) {
        const owned = qtyOf(holdingsList, entry.set, entry.number);
        const cardName = findCard(entry.set, entry.number)?.name ?? `${entry.set} ${entry.number}`;
        if (entry.count > owned) {
          throw new Error(`Tienes ${owned} de ${cardName} y el mazo pide ${entry.count}.`);
        }
      }

      const resolved = resolveDeck(cards);
      const problems = deckProblems(resolved, energies);
      if (problems.length > 0) {
        return {
          result: JSON.stringify({ ok: false, error: problems.join(" ") }),
          changeList,
          newDeck: null,
          deletedId: null,
        };
      }

      const createdDeck: Deck = {
        id: `deck-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
        name,
        energies,
        cards,
        updatedAt: Date.now(),
      };
      newDeck = createdDeck;
      workingDecks.unshift(createdDeck);

      return {
        result: JSON.stringify({ ok: true, id: createdDeck.id, name: createdDeck.name }),
        changeList,
        newDeck,
        deletedId,
      };
    }

    case "delete_deck": {
      const id = String(args.id);
      const idx = workingDecks.findIndex((d) => d.id === id);
      if (idx >= 0) {
        const d = workingDecks[idx];
        workingDecks.splice(idx, 1);
        deletedId = id;
        return {
          result: JSON.stringify({ ok: true, deleted: d.name }),
          changeList,
          newDeck,
          deletedId,
        };
      }
      return {
        result: JSON.stringify({ ok: false, error: "No existe el mazo con id " + id }),
        changeList,
        newDeck,
        deletedId,
      };
    }

    default:
      return {
        result: JSON.stringify({ error: `Herramienta ${name} no encontrada.` }),
        changeList,
        newDeck,
        deletedId,
      };
  }
}

// Build a concise collection summary for the LLM
function buildCollectionSummary(holdings: Map<string, number>, decks: Deck[]): string {
  let ownedCount = 0;
  let totalCopies = 0;
  const bySet: Record<string, number> = {};
  const byType: Record<string, number> = {};

  for (const [key, qty] of holdings.entries()) {
    if (qty <= 0) continue;
    ownedCount += 1;
    totalCopies += qty;
    const [set, nStr] = key.split("#");
    bySet[set] = (bySet[set] || 0) + 1;
    const card = findCard(set, Number(nStr));
    if (card) {
      const typeKey = card.trainer ? "trainer" : card.element || "other";
      byType[typeKey] = (byType[typeKey] || 0) + 1;
    }
  }

  const setSummary = Object.entries(bySet)
    .map(([k, v]) => `${k}:${v}`)
    .join(", ");
  const typeSummary = Object.entries(byType)
    .map(([k, v]) => `${k}:${v}`)
    .join(", ");
  const deckSummary = decks.map((d) => `${d.name} [id=${d.id}] (${d.cards.reduce((acc, c) => acc + c.count, 0)}/20)`).join("; ");

  return `Cartas distintas=${ownedCount}, copias=${totalCopies}. Por set=[${setSummary}]. Por tipo=[${typeSummary}]. Mazos guardados=[${deckSummary}].`;
}

// Built-in intelligent local deck generator for offline or key-less instant recommendations
function runLocalDeckAssistant(userPrompt: string, holdings: Map<string, number>, decks: Deck[]): AiResponse {
  const q = fold(userPrompt);

  // 1. Check for specific archetype requests
  let targetElement = "fire";
  let targetEnergy = 2; // Fuego
  let deckTitle = "Mazo Fuego Pocket";

  if (q.includes("agua") || q.includes("water") || q.includes("blastoise") || q.includes("starmie")) {
    targetElement = "water";
    targetEnergy = 3;
    deckTitle = "Mazo Agua y Mareas";
  } else if (q.includes("rayo") || q.includes("lightning") || q.includes("pikachu") || q.includes("zapdos")) {
    targetElement = "lightning";
    targetEnergy = 4;
    deckTitle = "Pikachu ex & Rayo";
  } else if (q.includes("planta") || q.includes("grass") || q.includes("venusaur") || q.includes("erika")) {
    targetElement = "grass";
    targetEnergy = 1;
    deckTitle = "Venusaur ex & Planta";
  } else if (q.includes("psiquica") || q.includes("psychic") || q.includes("mewtwo") || q.includes("gardevoir")) {
    targetElement = "psychic";
    targetEnergy = 5;
    deckTitle = "Mewtwo ex & Psíquico";
  } else if (q.includes("lucha") || q.includes("fighting") || q.includes("machamp") || q.includes("marowak")) {
    targetElement = "fighting";
    targetEnergy = 6;
    deckTitle = "Lucha & Poder";
  } else if (q.includes("oscura") || q.includes("darkness") || q.includes("weezing") || q.includes("arbok")) {
    targetElement = "darkness";
    targetEnergy = 7;
    deckTitle = "Control Sombra & Veneno";
  } else if (q.includes("metal") || q.includes("acero") || q.includes("melmetal")) {
    targetElement = "metal";
    targetEnergy = 8;
    deckTitle = "Armadura Metálica";
  }

  // Question about EX cards
  if (q.includes("ex") && (q.includes("tengo") || q.includes("cuales") || q.includes("lista"))) {
    const exList: string[] = [];
    for (const [k, qty] of holdings.entries()) {
      if (qty <= 0) continue;
      const [s, nStr] = k.split("#");
      const card = findCard(s, Number(nStr));
      if (card && isExCard(card.name, card.rarity)) {
        if (!targetElement || card.element === targetElement || !q.includes("rayo") && !q.includes("fuego") && !q.includes("agua")) {
          exList.push(`• **${card.name}** (${card.set} #${card.number}): ${qty} ${qty === 1 ? "copia" : "copias"}`);
        }
      }
    }
    if (exList.length > 0) {
      return {
        text: `En tu colección tienes las siguientes cartas Pokémon ex:\n\n${exList.slice(0, 15).join("\n")}\n\n¿Quieres que arme un mazo con alguna de ellas?`,
        changes: [],
        savedDeck: null,
        deletedDeckId: null,
      };
    } else {
      return {
        text: "Actualmente no tienes cartas Pokémon ex de ese tipo en tu colección. Puedes abrir sobres o agregarlas desde la pestaña Colección.",
        changes: [],
        savedDeck: null,
        deletedDeckId: null,
      };
    }
  }

  // Build a 20-card deck from owned cards
  const candidatePokemon: Array<{ set: string; number: number; count: number; name: string }> = [];
  const candidateTrainers: Array<{ set: string; number: number; count: number; name: string }> = [];

  for (const [key, qty] of holdings.entries()) {
    if (qty <= 0) continue;
    const [s, nStr] = key.split("#");
    const num = Number(nStr);
    const card = findCard(s, num);
    if (!card) continue;

    const availableCopies = Math.min(2, qty);
    if (card.trainer) {
      candidateTrainers.push({ set: card.set, number: card.number, count: availableCopies, name: card.name });
    } else if (card.element === targetElement) {
      // Prioritize ex cards and stage lines
      candidatePokemon.push({ set: card.set, number: card.number, count: availableCopies, name: card.name });
    }
  }

  // If not enough Pokemon of that type, fallback to any owned Pokemon
  if (candidatePokemon.length < 3) {
    for (const [key, qty] of holdings.entries()) {
      if (qty <= 0) continue;
      const [s, nStr] = key.split("#");
      const num = Number(nStr);
      const card = findCard(s, num);
      if (card && !card.trainer && candidatePokemon.every((c) => c.set !== card.set || c.number !== card.number)) {
        candidatePokemon.push({ set: card.set, number: card.number, count: Math.min(2, qty), name: card.name });
        if (candidatePokemon.length >= 8) break;
      }
    }
  }

  const selectedCards: DeckCard[] = [];
  let currentTotal = 0;

  // Add 10-12 Pokemon
  for (const p of candidatePokemon) {
    if (currentTotal >= 12) break;
    const toAdd = Math.min(p.count, 20 - currentTotal, 2);
    if (toAdd > 0) {
      selectedCards.push({ set: p.set, number: p.number, count: toAdd });
      currentTotal += toAdd;
    }
  }

  // Add Trainer cards to reach exactly 20
  for (const t of candidateTrainers) {
    if (currentTotal >= 20) break;
    const toAdd = Math.min(t.count, 20 - currentTotal, 2);
    if (toAdd > 0) {
      selectedCards.push({ set: t.set, number: t.number, count: toAdd });
      currentTotal += toAdd;
    }
  }

  // If still under 20, fill with remaining owned cards
  if (currentTotal < 20) {
    for (const [key, qty] of holdings.entries()) {
      if (currentTotal >= 20) break;
      if (qty <= 0) continue;
      const [s, nStr] = key.split("#");
      const num = Number(nStr);
      if (selectedCards.some((c) => c.set === s && c.number === num)) continue;
      const toAdd = Math.min(qty, 20 - currentTotal, 2);
      if (toAdd > 0) {
        selectedCards.push({ set: s, number: num, count: toAdd });
        currentTotal += toAdd;
      }
    }
  }

  if (currentTotal === 20) {
    const createdDeck: Deck = {
      id: `deck-${Date.now()}`,
      name: deckTitle,
      energies: [targetEnergy],
      cards: selectedCards,
      updatedAt: Date.now(),
    };
    repository.saveDeck(createdDeck);

    return {
      text: `He armado el mazo **${deckTitle}** con exactamente 20 cartas de tu colección y energía lista para el juego.\n\nPuedes abrirlo y generar el código QR de importación directamente.`,
      changes: [],
      savedDeck: createdDeck,
      deletedDeckId: null,
    };
  }

  return {
    text: `Analicé tu colección actual. Tienes ${holdings.size} entradas registradas. ¿Qué tipo de estrategia te gustaría probar? Puedes especificar elementos como Fuego, Agua, Rayo o pedirme que arme un mazo con tus mejores Pokémon ex.`,
    changes: [],
    savedDeck: null,
    deletedDeckId: null,
  };
}

// Multi-turn calling for external OpenAI / Grok / Gemini / Groq APIs
export async function askAiAssistant(
  userText: string,
  history: Array<{ role: "user" | "assistant"; content: string }>,
  providerId: AiProviderId,
): Promise<AiResponse> {
  const apiKey = repository.getApiKey(providerId);

  // Fallback to local intelligent assistant if no key provided
  if (!apiKey) {
    return runLocalDeckAssistant(userText, repository.getHoldingsMap(), repository.getDecks());
  }

  const config = PROVIDER_CONFIGS[providerId] || PROVIDER_CONFIGS.grok;
  const holdingsMap = new Map(repository.getHoldingsMap());
  const decksList = [...repository.getDecks()];
  const summary = buildCollectionSummary(holdingsMap, decksList);

  const systemPrompt = `Eres el asistente oficial de TCG LAB para Pokémon TCG Pocket.
Responde siempre en español neutro, conciso y profesional (sin voseo).
Ayudas al usuario a crear y optimizar mazos de 20 cartas listos para el código QR del juego con las cartas que él tiene.
REGLAS ESTRICTAS:
- No inventes set ni número: consulta antes con list_owned o search_catalog.
- Mazos jugables: exactamente 20 cartas, entre 1 y 3 energías (1 a 8), máximo 2 copias por nombre o entityId.
- Usa save_deck para guardar el mazo directamente en la app cuando el usuario te pida armar un mazo.
- Usa update_collection si el usuario te indica que consiguió nuevas cartas.
Resumen de colección del usuario:
${summary}`;

  const messages: any[] = [
    { role: "system", content: systemPrompt },
    ...history.slice(-10).map((h) => ({ role: h.role, content: h.content })),
    { role: "user", content: userText },
  ];

  let currentModel = config.defaultModel;
  let allChanges: CollectionChange[] = [];
  let savedDeck: Deck | null = null;
  let deletedDeckId: string | null = null;
  let finalText = "";

  for (let round = 0; round < 5; round++) {
    const payload: any = {
      model: currentModel,
      messages,
      tools: AI_TOOLS,
      max_tokens: 1600,
    };
    if (providerId !== "gemini") {
      payload.temperature = 0.3;
    }

    const res = await fetch(config.url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${apiKey}`,
      },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const errText = await res.text();
      // Try fallback model if 404 or model not found
      if ((res.status === 404 || errText.includes("model")) && config.models.length > 1) {
        const nextIdx = config.models.indexOf(currentModel) + 1;
        if (nextIdx < config.models.length) {
          currentModel = config.models[nextIdx];
          continue;
        }
      }
      throw new Error(`Error de ${config.name} (${res.status}): ${errText.slice(0, 200)}`);
    }

    const data = await res.json();
    const choice = data?.choices?.[0]?.message;
    if (!choice) break;

    messages.push(choice);

    const toolCalls = choice.tool_calls;
    if (!toolCalls || toolCalls.length === 0) {
      finalText = choice.content || "";
      break;
    }

    // Execute each tool call
    for (const call of toolCalls) {
      const name = call.function.name;
      let args = {};
      try {
        args = JSON.parse(call.function.arguments || "{}");
      } catch {
        args = {};
      }

      const { result, changeList, newDeck, deletedId } = executeTool(name, args, holdingsMap, decksList);

      if (changeList.length > 0) allChanges.push(...changeList);
      if (newDeck) savedDeck = newDeck;
      if (deletedId) deletedDeckId = deletedId;

      messages.push({
        role: "tool",
        tool_call_id: call.id,
        content: result,
      });
    }
  }

  // Apply mutations to repository
  repository.applyAiMutations(allChanges, savedDeck || undefined, deletedDeckId || undefined);

  return {
    text: finalText || "Operación completada con éxito.",
    changes: allChanges,
    savedDeck,
    deletedDeckId,
  };
}
