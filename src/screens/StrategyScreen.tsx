import React, { useEffect, useRef, useState } from "react";
import { Sparkles, Send, Key, Layers, ArrowRight, Trash2, CheckCircle2 } from "lucide-react";
import { LabSurfaceCard } from "@/components/LabSurfaceCard";
import { askAiAssistant } from "@/lib/ai/ai-client";
import type { ChatMessage, Deck } from "@/lib/folio/types";
import { repository, type AiProviderId } from "@/lib/repository";

interface StrategyScreenProps {
  onOpenDeck: (deck: Deck) => void;
}

const PROVIDERS: Array<{ id: AiProviderId; label: string }> = [
  { id: "grok", label: "Grok" },
  { id: "openai", label: "OpenAI" },
  { id: "gemini", label: "Gemini" },
  { id: "groq", label: "Groq" },
];

const SUGGESTIONS = [
  "Arma un mazo de fuego con las cartas que tengo y déjalo listo para el QR",
  "Arma un mazo de agua de 20 cartas, solo con lo que tengo",
  "¿Qué ex de rayo tengo?",
  "Arma un mazo de Pikachu ex y Rayo veloz",
];

export const StrategyScreen: React.FC<StrategyScreenProps> = ({ onOpenDeck }) => {
  const [messages, setMessages] = useState<ChatMessage[]>(repository.getChatMessages());
  const [inputPrompt, setInputPrompt] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);
  const [selectedProvider, setSelectedProvider] = useState<AiProviderId>(repository.getAiProvider());
  const [apiKeyInput, setApiKeyInput] = useState<string>("");
  const [showKeyForm, setShowKeyForm] = useState<boolean>(false);
  const [lastSavedDeck, setLastSavedDeck] = useState<Deck | null>(null);

  const messagesEndRef = useRef<HTMLDivElement | null>(null);
  const hasKey = repository.hasApiKey(selectedProvider);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  const handleProviderSelect = (provider: AiProviderId) => {
    setSelectedProvider(provider);
    repository.setAiProvider(provider);
    setApiKeyInput("");
  };

  const handleSaveApiKey = () => {
    if (apiKeyInput.trim()) {
      repository.saveApiKey(selectedProvider, apiKeyInput.trim());
      setApiKeyInput("");
      setShowKeyForm(false);
    }
  };

  const handleSend = async (textToSend?: string) => {
    const prompt = (textToSend || inputPrompt).trim();
    if (!prompt || loading) return;

    setInputPrompt("");
    const userMsg: ChatMessage = {
      id: `msg-${Date.now()}-user`,
      role: "user",
      content: prompt,
    };
    repository.addChatMessage(userMsg);
    setMessages(repository.getChatMessages());
    setLoading(true);

    try {
      const history = repository.getChatMessages().map((m) => ({
        role: m.role,
        content: m.content,
      }));

      const res = await askAiAssistant(prompt, history, selectedProvider);

      const assistantMsg: ChatMessage = {
        id: `msg-${Date.now()}-assistant`,
        role: "assistant",
        content: res.text,
        changes: res.changes.length > 0 ? res.changes : undefined,
        deckName: res.savedDeck?.name,
        deckEvent: res.savedDeck ? "saved" : undefined,
      };

      repository.addChatMessage(assistantMsg);
      setMessages(repository.getChatMessages());

      if (res.savedDeck) {
        setLastSavedDeck(res.savedDeck);
      }
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : "Error al procesar la respuesta.";
      const errMsg: ChatMessage = {
        id: `msg-${Date.now()}-error`,
        role: "assistant",
        content: `Error: ${errorMsg}`,
      };
      repository.addChatMessage(errMsg);
      setMessages(repository.getChatMessages());
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-[calc(100vh-140px)] animate-fade-in">
      {/* Top Header */}
      <div className="flex items-center justify-between pb-2 border-b border-[#E3DED2]/40 dark:border-[#34424E]/40 mb-2">
        <div>
          <h2 className="text-base font-bold text-[#172536] dark:text-[#F0EEE7]">
            TCG LAB AI
          </h2>
          <p className="text-xs text-[#667586] dark:text-[#9DAAB5]">
            Crea y mejora mazos usando las cartas de tu colección.
          </p>
        </div>

        {messages.length > 0 && (
          <button
            type="button"
            onClick={() => {
              repository.clearChat();
              setMessages([]);
              setLastSavedDeck(null);
            }}
            className="p-1.5 text-[#667586] hover:text-red-600 rounded-lg"
            title="Limpiar chat"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Provider Selector & API Key Status */}
      <LabSurfaceCard className="p-3 mb-3">
        <div className="flex items-center justify-between mb-2">
          <span className="text-[11px] font-semibold text-[#667586] dark:text-[#9DAAB5] uppercase">
            Proveedor de IA
          </span>
          <button
            type="button"
            onClick={() => setShowKeyForm(!showKeyForm)}
            className="text-xs font-semibold text-[#172536] dark:text-[#FFC857] flex items-center gap-1 cursor-pointer"
          >
            <Key className="w-3.5 h-3.5" />
            <span>{hasKey ? "Clave configurada" : "Configurar clave"}</span>
          </button>
        </div>

        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 text-xs">
          {PROVIDERS.map((p) => {
            const isSelected = selectedProvider === p.id;
            return (
              <button
                key={p.id}
                type="button"
                onClick={() => handleProviderSelect(p.id)}
                className={`px-3 py-1.5 rounded-full font-semibold transition-all cursor-pointer ${
                  isSelected
                    ? "bg-[#172536] text-[#FFFFFE] dark:bg-[#FFC857] dark:text-[#172536]"
                    : "bg-[#FFFFFE] dark:bg-[#202D38] text-[#667586] dark:text-[#9DAAB5] border border-[#E3DED2] dark:border-[#34424E]"
                }`}
              >
                {p.label}
              </button>
            );
          })}
        </div>

        {/* API Key Form toggle */}
        {(showKeyForm || (!hasKey && selectedProvider === "grok")) && (
          <div className="mt-3 pt-3 border-t border-[#E3DED2]/50 dark:border-[#34424E]/50">
            <p className="text-xs text-[#667586] dark:text-[#9DAAB5] mb-2 leading-relaxed">
              Ingresa la clave de API para {PROVIDERS.find((p) => p.id === selectedProvider)?.label}.
              Se guarda de forma segura en tu navegador.
              <em>(Nota: Sin clave, la app utiliza el motor local con reglas y sinónimos de Pocket).</em>
            </p>
            <div className="flex gap-2">
              <input
                type="password"
                value={apiKeyInput}
                onChange={(e) => setApiKeyInput(e.target.value)}
                placeholder="Pegar clave de API (sk-... o xai-...)"
                className="flex-1 bg-[#FFFFFE] dark:bg-[#202D38] border border-[#E3DED2] dark:border-[#34424E] rounded-xl px-3 py-2 text-xs text-[#172536] dark:text-[#F0EEE7] focus:outline-none"
              />
              <button
                type="button"
                onClick={handleSaveApiKey}
                className="py-2 px-3 rounded-xl bg-[#172536] dark:bg-[#FFC857] text-[#FFFFFE] dark:text-[#172536] text-xs font-semibold"
              >
                Guardar
              </button>
              {hasKey && (
                <button
                  type="button"
                  onClick={() => {
                    repository.clearApiKey(selectedProvider);
                    setShowKeyForm(false);
                  }}
                  className="py-2 px-3 rounded-xl border border-red-300 text-red-600 text-xs"
                >
                  Borrar
                </button>
              )}
            </div>
          </div>
        )}
      </LabSurfaceCard>

      {/* Messages Thread */}
      <div className="flex-1 overflow-y-auto space-y-3 pr-1 py-1">
        {messages.length === 0 ? (
          <div className="py-6 text-center">
            <div className="w-10 h-10 rounded-2xl bg-[#172536] text-[#FFC857] flex items-center justify-center mx-auto mb-3 shadow-sm">
              <Sparkles className="w-5 h-5" />
            </div>
            <h3 className="text-sm font-semibold text-[#172536] dark:text-[#F0EEE7] mb-1">
              ¿Qué mazo armamos hoy?
            </h3>
            <p className="text-xs text-[#667586] dark:text-[#9DAAB5] mb-4 max-w-xs mx-auto">
              Elige una sugerencia o escribe cualquier requerimiento.
            </p>

            {/* Suggestions as rows (specified in PROMPT.md) */}
            <div className="space-y-2 text-left max-w-md mx-auto">
              {SUGGESTIONS.map((sug, i) => (
                <button
                  key={i}
                  type="button"
                  onClick={() => handleSend(sug)}
                  className="w-full p-3 rounded-2xl bg-[#FFFFFE] dark:bg-[#18232E] border border-[#E3DED2] dark:border-[#34424E] text-xs font-medium text-[#172536] dark:text-[#F0EEE7] hover:border-[#172536] text-left flex items-center justify-between gap-2 shadow-xs transition-all active:scale-[0.99] cursor-pointer"
                >
                  <span>{sug}</span>
                  <ArrowRight className="w-3.5 h-3.5 text-[#667586] flex-shrink-0" />
                </button>
              ))}
            </div>
          </div>
        ) : (
          messages.map((msg) => {
            const isUser = msg.role === "user";
            return (
              <div
                key={msg.id}
                className={`flex flex-col ${isUser ? "items-end" : "items-start"}`}
              >
                <div
                  className={`max-w-[85%] rounded-2xl p-3.5 text-xs leading-relaxed whitespace-pre-wrap ${
                    isUser
                      ? "bg-[#172536] text-[#FFFFFE] rounded-tr-xs"
                      : "bg-[#FFFFFE] dark:bg-[#18232E] border border-[#E3DED2] dark:border-[#34424E] text-[#172536] dark:text-[#F0EEE7] rounded-tl-xs shadow-xs"
                  }`}
                >
                  {msg.content}
                </div>

                {/* Collection changes pill */}
                {msg.changes && msg.changes.length > 0 && (
                  <div className="mt-1.5 flex flex-wrap gap-1 max-w-[85%]">
                    {msg.changes.map((c, idx) => (
                      <span
                        key={idx}
                        className="text-[10px] bg-emerald-100 dark:bg-emerald-950/60 text-emerald-800 dark:text-emerald-300 px-2 py-0.5 rounded-full font-mono flex items-center gap-1"
                      >
                        <CheckCircle2 className="w-3 h-3" />
                        <span>
                          {c.name}: {c.from} → {c.to}
                        </span>
                      </span>
                    ))}
                  </div>
                )}

                {/* Saved Deck banner */}
                {msg.deckName && msg.deckEvent === "saved" && (
                  <div className="mt-2 w-full max-w-xs">
                    <LabSurfaceCard className="p-3 bg-neutral-50 dark:bg-neutral-800 border-emerald-300 dark:border-emerald-700">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <Layers className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
                          <span className="text-xs font-bold text-[#172536] dark:text-[#F0EEE7] truncate">
                            {msg.deckName}
                          </span>
                        </div>
                        <button
                          type="button"
                          onClick={() => {
                            const found = repository.getDecks().find((d) => d.name === msg.deckName) || lastSavedDeck;
                            if (found) onOpenDeck(found);
                          }}
                          className="py-1 px-2.5 rounded-lg bg-[#172536] dark:bg-[#FFC857] text-[#FFFFFE] dark:text-[#172536] text-[11px] font-semibold flex items-center gap-1 cursor-pointer"
                        >
                          <span>Abrir mazo</span>
                          <ArrowRight className="w-3 h-3" />
                        </button>
                      </div>
                    </LabSurfaceCard>
                  </div>
                )}
              </div>
            );
          })
        )}

        {loading && (
          <div className="flex items-center gap-2 text-xs text-[#667586] dark:text-[#9DAAB5] p-2">
            <Sparkles className="w-4 h-4 animate-spin text-[#172536] dark:text-[#FFC857]" />
            <span>Consultando tu colección y armando estrategia...</span>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input bar */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          handleSend();
        }}
        className="pt-2 flex items-center gap-2"
      >
        <input
          type="text"
          value={inputPrompt}
          onChange={(e) => setInputPrompt(e.target.value)}
          placeholder="Escribe tu consulta o pide un mazo..."
          className="flex-1 bg-[#FFFFFE] dark:bg-[#18232E] border border-[#E3DED2] dark:border-[#34424E] rounded-2xl px-4 py-3 text-xs text-[#172536] dark:text-[#F0EEE7] placeholder-[#667586]/60 focus:outline-none focus:ring-2 focus:ring-[#172536] dark:focus:ring-[#FFC857]"
        />
        <button
          type="submit"
          disabled={!inputPrompt.trim() || loading}
          className="w-11 h-11 rounded-2xl bg-[#172536] text-[#FFFFFE] dark:bg-[#FFC857] dark:text-[#172536] flex items-center justify-center disabled:opacity-40 disabled:cursor-not-allowed shadow-sm active:scale-95 transition-all cursor-pointer"
          aria-label="Enviar"
        >
          <Send className="w-4 h-4" />
        </button>
      </form>
    </div>
  );
};
