import React, { useMemo, useState } from "react";
import { ArrowLeft, Plus, Minus, QrCode, Search, Trash2, Check, AlertCircle } from "lucide-react";
import { CardArt } from "@/components/CardArt";
import { EnergyBadge } from "@/components/EnergyBadge";
import { LabSurfaceCard } from "@/components/LabSurfaceCard";
import { QrModal } from "@/components/QrModal";
import { ENERGIES, fold, padNumber } from "@/lib/folio/game";
import { CARDS, findCard } from "@/lib/folio/catalog";
import { deckProblems, deckTotal, resolveDeck, copyLimitReached } from "@/lib/folio/rules";
import type { CardRow, Deck, DeckCard } from "@/lib/folio/types";
import { repository } from "@/lib/repository";

interface DeckEditorScreenProps {
  initialDeck?: Deck | null;
  onBack: () => void;
}

export const DeckEditorScreen: React.FC<DeckEditorScreenProps> = ({
  initialDeck,
  onBack,
}) => {
  const [deckId] = useState<string>(
    initialDeck?.id || `deck-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
  );
  const [name, setName] = useState<string>(initialDeck?.name || "Nuevo mazo");
  const [energies, setEnergies] = useState<number[]>(initialDeck?.energies || [2]); // Default Fuego
  const [cards, setCards] = useState<DeckCard[]>(initialDeck?.cards || []);
  const [pickerFilter, setPickerFilter] = useState<string>("all");
  const [pickerSearch, setPickerSearch] = useState<string>("");
  const [showQrModal, setShowQrModal] = useState<boolean>(false);

  // Resolved cards for validation
  const resolved = useMemo(() => resolveDeck(cards), [cards]);
  const totalCards = useMemo(() => deckTotal(cards), [cards]);
  const problems = useMemo(() => deckProblems(resolved, energies), [resolved, energies]);
  const isValid = problems.length === 0;

  // Toggle energy (1 to 3 allowed)
  const toggleEnergy = (id: number) => {
    if (energies.includes(id)) {
      if (energies.length > 1) {
        setEnergies(energies.filter((e) => e !== id));
      }
    } else {
      if (energies.length < 3) {
        setEnergies([...energies, id]);
      } else {
        // Replace last
        setEnergies([...energies.slice(1), id]);
      }
    }
  };

  // Add card to deck
  const addCard = (card: CardRow) => {
    if (totalCards >= 20) return;
    if (copyLimitReached(cards, card.set, card.number)) return;

    const existingIndex = cards.findIndex(
      (c) => c.set === card.set && c.number === card.number,
    );
    if (existingIndex >= 0) {
      const updated = [...cards];
      updated[existingIndex] = {
        ...updated[existingIndex],
        count: Math.min(2, updated[existingIndex].count + 1),
      };
      setCards(updated);
    } else {
      setCards([...cards, { set: card.set, number: card.number, count: 1 }]);
    }
  };

  // Remove or decrease card
  const removeCard = (set: string, number: number) => {
    const existingIndex = cards.findIndex((c) => c.set === set && c.number === number);
    if (existingIndex < 0) return;
    const updated = [...cards];
    if (updated[existingIndex].count > 1) {
      updated[existingIndex] = {
        ...updated[existingIndex],
        count: updated[existingIndex].count - 1,
      };
      setCards(updated);
    } else {
      updated.splice(existingIndex, 1);
      setCards(updated);
    }
  };

  // Save current deck
  const saveDeck = () => {
    const deckToSave: Deck = {
      id: deckId,
      name: name.trim() || "Mazo sin nombre",
      energies,
      cards,
      updatedAt: Date.now(),
    };
    repository.saveDeck(deckToSave);
  };

  // Filter available cards to add from owned collection
  const availableToAdd = useMemo(() => {
    const holdings = repository.getHoldingsMap();
    const q = fold(pickerSearch);

    const list: Array<{ card: CardRow; owned: number; inDeck: number }> = [];

    for (const [key, qty] of holdings.entries()) {
      if (qty <= 0) continue;
      const [s, nStr] = key.split("#");
      const num = Number(nStr);
      const card = findCard(s, num);
      if (!card) continue;

      // Filter by type: 'all', 8 energies, 'trainer'
      if (pickerFilter !== "all") {
        if (pickerFilter === "trainer") {
          if (!card.trainer) continue;
        } else {
          if (card.trainer || fold(card.element) !== pickerFilter) continue;
        }
      }

      // Filter by search query
      if (q) {
        const nameFolded = fold(card.name);
        const codeNum = `${card.set.toLowerCase()} ${card.number}`;
        if (!nameFolded.includes(q) && !codeNum.includes(q)) continue;
      }

      const inDeck = cards.find((c) => c.set === card.set && c.number === card.number)?.count || 0;
      list.push({ card, owned: qty, inDeck });
    }

    return list.sort((a, b) => {
      // Prioritize in-deck or ex cards
      return a.card.name.localeCompare(b.card.name);
    });
  }, [cards, pickerFilter, pickerSearch]);

  const currentDeckObj: Deck = {
    id: deckId,
    name: name.trim() || "Mazo",
    energies,
    cards,
    updatedAt: Date.now(),
  };

  return (
    <div className="space-y-4 pb-28 animate-fade-in">
      {/* Top Bar with Back, Title & Save */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => {
              saveDeck();
              onBack();
            }}
            className="p-2 -ml-2 rounded-full text-[#172536] dark:text-[#F0EEE7] hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer"
            aria-label="Volver"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <span className="text-base font-bold text-[#172536] dark:text-[#F0EEE7]">
            Editor de mazo
          </span>
        </div>

        <button
          type="button"
          onClick={saveDeck}
          className="py-1.5 px-3 rounded-xl bg-[#172536] dark:bg-[#FFC857] text-[#FFFFFE] dark:text-[#172536] text-xs font-semibold shadow-xs active:scale-95 transition-all"
        >
          Guardar
        </button>
      </div>

      {/* 1. Deck Name Input */}
      <div>
        <label className="text-[11px] font-bold text-[#667586] dark:text-[#9DAAB5] uppercase tracking-wider block mb-1">
          Nombre del mazo
        </label>
        <input
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Ej. Charizard ex & Moltres"
          className="w-full bg-[#FFFFFE] dark:bg-[#18232E] border border-[#E3DED2] dark:border-[#34424E] rounded-2xl px-4 py-3 text-sm font-semibold text-[#172536] dark:text-[#F0EEE7] placeholder-[#667586]/60 focus:outline-none focus:ring-2 focus:ring-[#172536] dark:focus:ring-[#FFC857]"
        />
      </div>

      {/* 2. Energies Selector (1 to 3) */}
      <LabSurfaceCard className="p-3.5" raised>
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs font-semibold text-[#172536] dark:text-[#F0EEE7]">
            Energías del mazo (elige de 1 a 3)
          </span>
          <span className="text-xs font-bold text-[#667586] dark:text-[#9DAAB5] tabular-nums">
            {energies.length}/3
          </span>
        </div>
        <div className="flex items-center justify-between gap-1 overflow-x-auto py-1">
          {ENERGIES.map((energy) => {
            const isSelected = energies.includes(energy.id);
            return (
              <button
                key={energy.id}
                type="button"
                onClick={() => toggleEnergy(energy.id)}
                className={`flex flex-col items-center gap-1 p-2 rounded-xl transition-all cursor-pointer ${
                  isSelected
                    ? "bg-[#172536]/10 dark:bg-[#FFC857]/20 ring-1 ring-[#172536] dark:ring-[#FFC857]"
                    : "opacity-60 hover:opacity-100"
                }`}
                title={energy.es}
              >
                <EnergyBadge id={energy.id} size="md" />
                <span className="text-[10px] font-medium text-[#172536] dark:text-[#F0EEE7]">
                  {energy.es}
                </span>
              </button>
            );
          })}
        </div>
      </LabSurfaceCard>

      {/* 3. 20-Card Counter with 20 Marks */}
      <LabSurfaceCard className="p-3.5">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs font-semibold text-[#172536] dark:text-[#F0EEE7]">
            Contador de cartas
          </span>
          <span
            className={`text-xs font-bold tabular-nums ${
              totalCards === 20
                ? "text-emerald-600 dark:text-emerald-400"
                : "text-amber-600 dark:text-amber-400"
            }`}
          >
            {totalCards} / 20
          </span>
        </div>

        {/* 20 Marks grid / row */}
        <div className="grid grid-cols-10 gap-1.5 sm:gap-2">
          {Array.from({ length: 20 }).map((_, index) => {
            const isFilled = index < totalCards;
            return (
              <div
                key={index}
                className={`h-2.5 rounded-full transition-all duration-200 ${
                  isFilled
                    ? "bg-[#172536] dark:bg-[#FFC857]"
                    : "bg-[#E9E2D4] dark:bg-[#33414B]"
                }`}
              />
            );
          })}
        </div>
      </LabSurfaceCard>

      {/* Validation status / QR button */}
      <div className="space-y-2">
        {problems.length > 0 ? (
          <div className="p-3 rounded-2xl bg-amber-50 dark:bg-amber-950/20 border border-amber-200/50 dark:border-amber-800/30 text-xs text-amber-800 dark:text-amber-300 space-y-1">
            <div className="flex items-center gap-1.5 font-semibold">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>Para generar el código QR:</span>
            </div>
            {problems.map((prob, i) => (
              <div key={i} className="pl-5 text-[11px] leading-tight">
                • {prob}
              </div>
            ))}
          </div>
        ) : (
          <div className="p-3 rounded-2xl bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-200/50 dark:border-emerald-800/30 text-xs text-emerald-800 dark:text-emerald-300 flex items-center justify-between">
            <div className="flex items-center gap-2 font-semibold">
              <Check className="w-4 h-4" />
              <span>Mazo completo de 20 cartas listo para el QR</span>
            </div>
          </div>
        )}

        <button
          type="button"
          disabled={!isValid}
          onClick={() => {
            saveDeck();
            setShowQrModal(true);
          }}
          className="w-full py-3.5 px-4 rounded-2xl bg-[#172536] text-[#FFFFFE] dark:bg-[#FFC857] dark:text-[#172536] font-bold text-sm flex items-center justify-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed shadow-md active:scale-98 transition-all cursor-pointer"
        >
          <QrCode className="w-5 h-5" />
          <span>Generar Código QR</span>
        </button>
      </div>

      {/* 4. Cards in Deck */}
      <div>
        <div className="flex items-center justify-between mb-2">
          <h3 className="text-xs font-bold uppercase tracking-wider text-[#667586] dark:text-[#9DAAB5]">
            Cartas en el mazo ({cards.length} entradas)
          </h3>
          <span className="text-xs text-[#667586] dark:text-[#9DAAB5]">
            Máx. 2 por nombre
          </span>
        </div>

        {cards.length === 0 ? (
          <LabSurfaceCard className="p-6 text-center text-xs text-[#667586] dark:text-[#9DAAB5]">
            El mazo aún no tiene cartas. Suma cartas desde tu colección abajo.
          </LabSurfaceCard>
        ) : (
          <div className="space-y-2">
            {cards.map((entry) => {
              const card = findCard(entry.set, entry.number);
              return (
                <LabSurfaceCard
                  key={`${entry.set}#${entry.number}`}
                  className="p-3 flex items-center justify-between gap-3"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="w-10 flex-shrink-0">
                      <CardArt
                        set={entry.set}
                        number={entry.number}
                        name={card?.name || `${entry.set} ${entry.number}`}
                        element={card?.element}
                        trainer={card?.trainer}
                      />
                    </div>
                    <div className="min-w-0">
                      <div className="text-xs font-semibold text-[#172536] dark:text-[#F0EEE7] truncate">
                        {card?.name || `${entry.set} ${entry.number}`}
                      </div>
                      <div className="text-[10px] text-[#667586] dark:text-[#9DAAB5]">
                        {entry.set} #{padNumber(entry.number)} {card?.trainer ? "· Entrenador" : ""}
                      </div>
                    </div>
                  </div>

                  {/* Quantity adjustment buttons */}
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => removeCard(entry.set, entry.number)}
                      className="w-8 h-8 rounded-xl bg-neutral-200/70 dark:bg-neutral-800 text-[#172536] dark:text-[#F0EEE7] flex items-center justify-center cursor-pointer active:scale-95"
                      aria-label="Restar una copia"
                    >
                      <Minus className="w-4 h-4" />
                    </button>
                    <span className="w-6 text-center font-bold text-xs text-[#172536] dark:text-[#F0EEE7] tabular-nums">
                      ×{entry.count}
                    </span>
                    <button
                      type="button"
                      onClick={() => card && addCard(card)}
                      disabled={entry.count >= 2 || totalCards >= 20}
                      className="w-8 h-8 rounded-xl bg-[#172536] dark:bg-[#FFC857] text-[#FFFFFE] dark:text-[#172536] flex items-center justify-center disabled:opacity-30 disabled:cursor-not-allowed cursor-pointer active:scale-95"
                      aria-label="Sumar una copia"
                    >
                      <Plus className="w-4 h-4" />
                    </button>
                  </div>
                </LabSurfaceCard>
              );
            })}
          </div>
        )}
      </div>

      {/* 5. Add Cards from Owned Collection */}
      <div className="pt-2">
        <h3 className="text-xs font-bold uppercase tracking-wider text-[#667586] dark:text-[#9DAAB5] mb-2">
          Sumar cartas de tu colección
        </h3>

        {/* Filter chips: Todas, 8 tipos, Entrenador */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-2 no-scrollbar text-xs">
          {[
            { id: "all", label: "Todas" },
            { id: "grass", label: "Planta" },
            { id: "fire", label: "Fuego" },
            { id: "water", label: "Agua" },
            { id: "lightning", label: "Rayo" },
            { id: "psychic", label: "Psíquica" },
            { id: "fighting", label: "Lucha" },
            { id: "darkness", label: "Oscura" },
            { id: "metal", label: "Metálica" },
            { id: "trainer", label: "Entrenador" },
          ].map((item) => (
            <button
              key={item.id}
              type="button"
              onClick={() => setPickerFilter(item.id)}
              className={`px-3 py-1.5 rounded-full font-semibold transition-all cursor-pointer whitespace-nowrap active:scale-95 ${
                pickerFilter === item.id
                  ? "bg-[#172536] text-[#FFFFFE] dark:bg-[#FFC857] dark:text-[#172536]"
                  : "bg-[#F9F7F1] dark:bg-[#18232E] text-[#667586] dark:text-[#9DAAB5] border border-[#E3DED2] dark:border-[#34424E]"
              }`}
            >
              {item.label}
            </button>
          ))}
        </div>

        {/* Search within picker */}
        <div className="relative mb-3">
          <Search className="w-4 h-4 text-[#667586] dark:text-[#9DAAB5] absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={pickerSearch}
            onChange={(e) => setPickerSearch(e.target.value)}
            placeholder="Buscar en cartas que tienes..."
            className="w-full bg-[#FFFFFE] dark:bg-[#18232E] border border-[#E3DED2] dark:border-[#34424E] rounded-xl pl-9 pr-4 py-2 text-xs text-[#172536] dark:text-[#F0EEE7] placeholder-[#667586]/60 focus:outline-none"
          />
        </div>

        {/* 3-Column Grid of Owned Cards */}
        {availableToAdd.length > 0 ? (
          <div className="grid grid-cols-3 gap-2.5 max-h-96 overflow-y-auto p-1">
            {availableToAdd.map(({ card, owned, inDeck }) => {
              const limitReached = copyLimitReached(cards, card.set, card.number) || inDeck >= owned;
              const deckFull = totalCards >= 20;

              return (
                <div
                  key={`${card.set}#${card.number}`}
                  onClick={() => !limitReached && !deckFull && addCard(card)}
                  className={`bg-[#F9F7F1] dark:bg-[#18232E] border border-[#E3DED2] dark:border-[#34424E] rounded-xl p-1.5 flex flex-col justify-between transition-all select-none ${
                    limitReached || deckFull
                      ? "opacity-40 cursor-not-allowed"
                      : "cursor-pointer active:scale-95 hover:border-[#172536]"
                  }`}
                >
                  <div className="relative">
                    <CardArt
                      set={card.set}
                      number={card.number}
                      name={card.name}
                      element={card.element}
                      trainer={card.trainer}
                    />
                    <div className="absolute top-1 right-1 bg-black/70 text-white text-[9px] font-bold px-1.5 py-0.5 rounded-md">
                      {inDeck}/{owned}
                    </div>
                  </div>
                  <div className="mt-1 px-0.5">
                    <div className="text-[10px] font-semibold text-[#172536] dark:text-[#F0EEE7] truncate">
                      {card.name}
                    </div>
                    <div className="text-[9px] text-[#667586] dark:text-[#9DAAB5]">
                      {card.set} #{card.number}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="text-center py-8 text-xs text-[#667586] dark:text-[#9DAAB5]">
            No tienes cartas disponibles con ese filtro.
          </div>
        )}
      </div>

      {/* QR Code Modal */}
      {showQrModal && (
        <QrModal deck={currentDeckObj} onClose={() => setShowQrModal(false)} />
      )}
    </div>
  );
};
