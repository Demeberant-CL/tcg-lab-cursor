import { CARDS, canonSet, cardKey, findCard, seedHoldings, SETS } from "@/lib/folio/catalog";
import { holdingsToCsv, parseCollectionCsv, type CsvRow } from "@/lib/folio/csv";
import { isExCard } from "@/lib/folio/game";
import type { ChatMessage, CollectionChange, Deck, DeckCard, Holding } from "@/lib/folio/types";

const STORAGE_KEYS = {
  HOLDINGS: "tcg_lab_holdings_v1",
  DECKS: "tcg_lab_decks_v1",
  CHAT: "tcg_lab_chat_v1",
  THEME: "tcg_lab_theme_v1",
  API_KEYS: "tcg_lab_api_keys_v1",
  AI_PROVIDER: "tcg_lab_ai_provider_v1",
  LOGS: "tcg_lab_diagnostic_logs_v1",
};

export type ThemeMode = "system" | "light" | "dark";
export type AiProviderId = "grok" | "openai" | "gemini" | "groq";

export interface LogEntry {
  timestamp: string;
  level: "info" | "warn" | "error";
  message: string;
  detail?: string;
}

export interface SetProgress {
  set: (typeof SETS)[number];
  ownedDistinct: number;
  total: number;
  percentage: number;
}

// Initial starter decks if empty
const DEFAULT_STARTER_DECKS: Deck[] = [
  {
    id: "deck-charizard-ex",
    name: "Charizard ex & Moltres",
    energies: [2], // Fuego
    cards: [
      { set: "A1", number: 36, count: 2 }, // Charmander
      { set: "A1", number: 37, count: 2 }, // Charmeleon
      { set: "A1", number: 38, count: 2 }, // Charizard ex
      { set: "A1", number: 47, count: 2 }, // Moltres ex
      { set: "A1", number: 219, count: 2 }, // Poké Ball
      { set: "A1", number: 220, count: 2 }, // Investigación de Profesores
      { set: "A1", number: 224, count: 2 }, // Sabrina
      { set: "A1", number: 221, count: 2 }, // Velocidad X
      { set: "A1", number: 222, count: 2 }, // Poción
      { set: "A1", number: 223, count: 2 }, // Tarjeta Roja
    ],
    updatedAt: Date.now() - 10000,
  },
  {
    id: "deck-pikachu-ex",
    name: "Pikachu ex Rayo Veloz",
    energies: [4], // Rayo
    cards: [
      { set: "A1", number: 95, count: 2 }, // Pikachu
      { set: "A1", number: 96, count: 2 }, // Pikachu ex
      { set: "A1", number: 104, count: 2 }, // Zapdos ex
      { set: "A1", number: 100, count: 2 }, // Voltorb
      { set: "A1", number: 101, count: 2 }, // Electrode
      { set: "A1", number: 219, count: 2 }, // Poké Ball
      { set: "A1", number: 220, count: 2 }, // Investigación de Profesores
      { set: "A1", number: 224, count: 2 }, // Sabrina
      { set: "A1", number: 221, count: 2 }, // Velocidad X
      { set: "A1", number: 222, count: 2 }, // Poción
    ],
    updatedAt: Date.now(),
  },
];

class LabRepository {
  private listeners: Set<() => void> = new Set();

  private holdingsMap: Map<string, number> = new Map();
  private decksList: Deck[] = [];
  private chatList: ChatMessage[] = [];
  private theme: ThemeMode = "system";
  private apiKeys: Record<string, string> = {};
  private aiProvider: AiProviderId = "grok";
  private logs: LogEntry[] = [];
  public initialized = false;

  constructor() {
    this.init();
  }

  public init() {
    if (typeof window === "undefined") return;

    // Load Theme
    const storedTheme = localStorage.getItem(STORAGE_KEYS.THEME) as ThemeMode | null;
    if (storedTheme && ["system", "light", "dark"].includes(storedTheme)) {
      this.theme = storedTheme;
    }
    this.applyThemeToDom(this.theme);

    // Load API Keys
    try {
      const storedKeys = localStorage.getItem(STORAGE_KEYS.API_KEYS);
      if (storedKeys) this.apiKeys = JSON.parse(storedKeys);
    } catch {
      this.apiKeys = {};
    }

    // Load Provider
    const storedProvider = localStorage.getItem(STORAGE_KEYS.AI_PROVIDER) as AiProviderId | null;
    if (storedProvider && ["grok", "openai", "gemini", "groq"].includes(storedProvider)) {
      this.aiProvider = storedProvider;
    }

    // Load Holdings
    try {
      const storedHoldings = localStorage.getItem(STORAGE_KEYS.HOLDINGS);
      if (storedHoldings) {
        const parsed = JSON.parse(storedHoldings) as Record<string, number>;
        this.holdingsMap = new Map(Object.entries(parsed));
      } else {
        // Seed default
        this.seedInitialHoldings();
      }
    } catch {
      this.seedInitialHoldings();
    }

    // Load Decks
    try {
      const storedDecks = localStorage.getItem(STORAGE_KEYS.DECKS);
      if (storedDecks) {
        this.decksList = JSON.parse(storedDecks);
      } else {
        this.decksList = DEFAULT_STARTER_DECKS;
        this.persistDecks();
      }
    } catch {
      this.decksList = DEFAULT_STARTER_DECKS;
    }

    // Load Chat
    try {
      const storedChat = localStorage.getItem(STORAGE_KEYS.CHAT);
      if (storedChat) {
        this.chatList = JSON.parse(storedChat);
      }
    } catch {
      this.chatList = [];
    }

    // Load Logs
    try {
      const storedLogs = localStorage.getItem(STORAGE_KEYS.LOGS);
      if (storedLogs) this.logs = JSON.parse(storedLogs);
    } catch {
      this.logs = [];
    }

    this.initialized = true;
    this.log("info", "TCG LAB inicializado correctamente.");
  }

  private seedInitialHoldings() {
    this.holdingsMap.clear();
    const seeds = seedHoldings();
    for (const item of seeds) {
      this.holdingsMap.set(cardKey(item.set, item.number), item.qty);
    }
    this.persistHoldings();
  }

  public subscribe(listener: () => void): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  private notify() {
    this.listeners.forEach((fn) => fn());
  }

  // --- Theme ---
  public getTheme(): ThemeMode {
    return this.theme;
  }

  public setTheme(mode: ThemeMode) {
    this.theme = mode;
    localStorage.setItem(STORAGE_KEYS.THEME, mode);
    this.applyThemeToDom(mode);
    this.notify();
  }

  private applyThemeToDom(mode: ThemeMode) {
    const root = document.documentElement;
    const isDark =
      mode === "dark" ||
      (mode === "system" && window.matchMedia("(prefers-color-scheme: dark)").matches);
    if (isDark) {
      root.classList.add("dark");
    } else {
      root.classList.remove("dark");
    }
  }

  // --- API Keys ---
  public getApiKey(provider: AiProviderId): string {
    return this.apiKeys[provider] || "";
  }

  public hasApiKey(provider: AiProviderId): boolean {
    return Boolean(this.apiKeys[provider]?.trim());
  }

  public saveApiKey(provider: AiProviderId, key: string) {
    const trimmed = key.trim();
    if (trimmed) {
      this.apiKeys[provider] = trimmed;
    } else {
      delete this.apiKeys[provider];
    }
    localStorage.setItem(STORAGE_KEYS.API_KEYS, JSON.stringify(this.apiKeys));
    this.notify();
  }

  public clearApiKey(provider: AiProviderId) {
    delete this.apiKeys[provider];
    localStorage.setItem(STORAGE_KEYS.API_KEYS, JSON.stringify(this.apiKeys));
    this.notify();
  }

  public getAiProvider(): AiProviderId {
    return this.aiProvider;
  }

  public setAiProvider(provider: AiProviderId) {
    this.aiProvider = provider;
    localStorage.setItem(STORAGE_KEYS.AI_PROVIDER, provider);
    this.notify();
  }

  // --- Holdings ---
  public getHoldingsMap(): Map<string, number> {
    return this.holdingsMap;
  }

  public getHoldingsList(): Holding[] {
    const list: Holding[] = [];
    for (const [key, qty] of this.holdingsMap.entries()) {
      const [set, numStr] = key.split("#");
      list.push({ set, number: Number(numStr), qty });
    }
    return list;
  }

  public getQty(set: string, number: number): number {
    return this.holdingsMap.get(cardKey(canonSet(set), number)) ?? 0;
  }

  public setQty(set: string, number: number, qty: number) {
    const key = cardKey(canonSet(set), number);
    const clamped = Math.max(0, Math.min(99, qty));
    if (clamped > 0) {
      this.holdingsMap.set(key, clamped);
    } else {
      this.holdingsMap.set(key, 0); // Keep 0 for tracked missing cards
    }
    this.persistHoldings();
    this.notify();
  }

  private persistHoldings() {
    const obj: Record<string, number> = {};
    for (const [k, v] of this.holdingsMap.entries()) {
      obj[k] = v;
    }
    localStorage.setItem(STORAGE_KEYS.HOLDINGS, JSON.stringify(obj));
  }

  public resetToSeed() {
    this.seedInitialHoldings();
    this.log("info", "Colección reiniciada a la semilla original.");
    this.notify();
  }

  public importCsv(csvContent: string, replace: boolean): { success: boolean; count: number; error?: string } {
    try {
      const rows = parseCollectionCsv(csvContent);
      if (!rows.length) {
        return { success: false, count: 0, error: "El archivo CSV no contiene cartas válidas." };
      }

      if (replace) {
        this.holdingsMap.clear();
      }

      for (const row of rows) {
        const key = cardKey(row.set, row.number);
        if (replace) {
          this.holdingsMap.set(key, row.qty);
        } else {
          const current = this.holdingsMap.get(key) ?? 0;
          this.holdingsMap.set(key, Math.max(current, row.qty));
        }
      }

      this.persistHoldings();
      this.log("info", `CSV importado: ${rows.length} cartas procesadas (modo ${replace ? "Reemplazar" : "Combinar"}).`);
      this.notify();
      return { success: true, count: rows.length };
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Error al procesar el archivo CSV.";
      this.log("error", "Error importando CSV", msg);
      return { success: false, count: 0, error: msg };
    }
  }

  public exportCsv(): string {
    const holdings = this.getHoldingsList();
    return holdingsToCsv(holdings);
  }

  public getStats() {
    let distinct = 0;
    let copies = 0;
    let ex = 0;

    for (const [key, qty] of this.holdingsMap.entries()) {
      if (qty > 0) {
        distinct += 1;
        copies += qty;
        const [set, numStr] = key.split("#");
        const card = findCard(set, Number(numStr));
        if (card && isExCard(card.name, card.rarity)) {
          ex += 1;
        }
      }
    }

    const totalCards = CARDS.length;
    const percentage = totalCards === 0 ? 0 : Math.round((distinct / totalCards) * 100);

    return { distinct, copies, ex, totalCards, percentage };
  }

  public getSetsProgress(): SetProgress[] {
    return SETS.map((set) => {
      const setCards = CARDS.filter((c) => c.set === set.code);
      let ownedDistinct = 0;
      for (const card of setCards) {
        if ((this.holdingsMap.get(cardKey(card.set, card.number)) ?? 0) > 0) {
          ownedDistinct += 1;
        }
      }
      return {
        set,
        ownedDistinct,
        total: set.count,
        percentage: set.count === 0 ? 0 : Math.round((ownedDistinct / set.count) * 100),
      };
    });
  }

  // --- Decks ---
  public getDecks(): Deck[] {
    return [...this.decksList].sort((a, b) => b.updatedAt - a.updatedAt);
  }

  public getDeck(id: string): Deck | undefined {
    return this.decksList.find((d) => d.id === id);
  }

  public saveDeck(deck: Deck): Deck {
    const existingIndex = this.decksList.findIndex((d) => d.id === deck.id);
    const updated = { ...deck, updatedAt: Date.now() };
    if (existingIndex >= 0) {
      this.decksList[existingIndex] = updated;
    } else {
      this.decksList.unshift(updated);
    }
    this.persistDecks();
    this.log("info", `Mazo guardado: ${deck.name}`);
    this.notify();
    return updated;
  }

  public deleteDeck(id: string): boolean {
    const prevLen = this.decksList.length;
    this.decksList = this.decksList.filter((d) => d.id !== id);
    if (this.decksList.length !== prevLen) {
      this.persistDecks();
      this.log("info", `Mazo eliminado: ${id}`);
      this.notify();
      return true;
    }
    return false;
  }

  private persistDecks() {
    localStorage.setItem(STORAGE_KEYS.DECKS, JSON.stringify(this.decksList));
  }

  // --- Chat Messages ---
  public getChatMessages(): ChatMessage[] {
    return this.chatList;
  }

  public addChatMessage(msg: ChatMessage) {
    this.chatList.push(msg);
    // Keep last 40 messages
    if (this.chatList.length > 40) {
      this.chatList = this.chatList.slice(-40);
    }
    localStorage.setItem(STORAGE_KEYS.CHAT, JSON.stringify(this.chatList));
    this.notify();
  }

  public clearChat() {
    this.chatList = [];
    localStorage.removeItem(STORAGE_KEYS.CHAT);
    this.notify();
  }

  // --- AI Mutations handler ---
  public applyAiMutations(changes: CollectionChange[], savedDeck?: Deck, deletedDeckId?: string) {
    if (changes.length > 0) {
      for (const change of changes) {
        this.setQty(change.set, change.number, change.to);
      }
    }
    if (savedDeck) {
      this.saveDeck(savedDeck);
    }
    if (deletedDeckId) {
      this.deleteDeck(deletedDeckId);
    }
    this.notify();
  }

  // --- Diagnostic Logs ---
  public log(level: "info" | "warn" | "error", message: string, detail?: string) {
    const entry: LogEntry = {
      timestamp: new Date().toISOString(),
      level,
      message,
      detail,
    };
    this.logs.unshift(entry);
    if (this.logs.length > 100) this.logs.pop();
    try {
      localStorage.setItem(STORAGE_KEYS.LOGS, JSON.stringify(this.logs));
    } catch {
      // Ignore storage errors for logs
    }
  }

  public getLogs(): LogEntry[] {
    return this.logs;
  }

  public exportLogs(): string {
    return JSON.stringify(
      {
        version: "1.0.2",
        exportedAt: new Date().toISOString(),
        stats: this.getStats(),
        deckCount: this.decksList.length,
        logs: this.logs,
      },
      null,
      2,
    );
  }
}

export const repository = new LabRepository();
