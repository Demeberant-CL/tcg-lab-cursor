import React, { useMemo, useState } from "react";
import { ArrowLeft, Search, X } from "lucide-react";
import { CardArt } from "@/components/CardArt";
import { CardDetailModal } from "@/components/CardDetailModal";
import { CARDS, setInfo } from "@/lib/folio/catalog";
import { fold, padNumber } from "@/lib/folio/game";
import type { CardRow } from "@/lib/folio/types";
import { repository } from "@/lib/repository";

interface SetDetailScreenProps {
  setCode: string;
  onBack: () => void;
  onAddToDeck?: (card: CardRow) => void;
}

type OwnFilter = "all" | "owned" | "missing" | "tracked";

export const SetDetailScreen: React.FC<SetDetailScreenProps> = ({
  setCode,
  onBack,
  onAddToDeck,
}) => {
  const [searchQuery, setSearchQuery] = useState("");
  const [filter, setFilter] = useState<OwnFilter>("all");
  const [selectedCard, setSelectedCard] = useState<CardRow | null>(null);

  const set = setInfo(setCode);
  const setCards = useMemo(() => {
    return CARDS.filter((c) => c.set === setCode).sort((a, b) => a.number - b.number);
  }, [setCode]);

  const filteredCards = useMemo(() => {
    const q = fold(searchQuery);

    return setCards.filter((card) => {
      const qty = repository.getQty(card.set, card.number);

      // Filter by ownership
      if (filter === "owned" && qty <= 0) return false;
      if (filter === "missing" && qty > 0) return false;
      if (filter === "tracked" && !repository.getHoldingsMap().has(`${card.set}#${card.number}`)) return false;

      // Filter by search query: by name or by set & number e.g. "A1 36"
      if (!q) return true;
      const nameFolded = fold(card.name);
      const codeNumber = `${card.set.toLowerCase()} ${card.number}`;
      const codePad = `${card.set.toLowerCase()} ${padNumber(card.number)}`;

      return (
        nameFolded.includes(q) ||
        codeNumber.includes(q) ||
        codePad.includes(q) ||
        String(card.number) === q
      );
    });
  }, [setCards, searchQuery, filter]);

  const selectedQty = selectedCard
    ? repository.getQty(selectedCard.set, selectedCard.number)
    : 0;

  return (
    <div className="space-y-3 pb-24 animate-fade-in">
      {/* Header with back button */}
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onBack}
          className="p-2 -ml-2 rounded-full text-[#172536] dark:text-[#F0EEE7] hover:bg-black/5 dark:hover:bg-white/5 transition-colors cursor-pointer"
          aria-label="Volver"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h2 className="text-base font-bold text-[#172536] dark:text-[#F0EEE7] leading-tight">
            {set?.es || setCode}
          </h2>
          <span className="text-xs text-[#667586] dark:text-[#9DAAB5] font-mono">
            {setCode} · {setCards.length} cartas
          </span>
        </div>
      </div>

      {/* Fixed Search Bar */}
      <div className="relative">
        <Search className="w-4 h-4 text-[#667586] dark:text-[#9DAAB5] absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Nombre o A1 36"
          className="w-full bg-[#FFFFFE] dark:bg-[#18232E] border border-[#E3DED2] dark:border-[#34424E] rounded-2xl pl-10 pr-9 py-2.5 text-xs text-[#172536] dark:text-[#F0EEE7] placeholder-[#667586]/70 focus:outline-none focus:ring-2 focus:ring-[#172536] dark:focus:ring-[#FFC857]"
        />
        {searchQuery && (
          <button
            type="button"
            onClick={() => setSearchQuery("")}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-[#667586] hover:text-[#172536]"
          >
            <X className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Filter Chips: Todas, Tengo, Me faltan, Rastreadas */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-1 no-scrollbar text-xs">
        {(
          [
            { id: "all", label: "Todas" },
            { id: "owned", label: "Tengo" },
            { id: "missing", label: "Me faltan" },
            { id: "tracked", label: "Rastreadas" },
          ] as const
        ).map((chip) => {
          const isActive = filter === chip.id;
          return (
            <button
              key={chip.id}
              type="button"
              onClick={() => setFilter(chip.id)}
              className={`px-3 py-1.5 rounded-full font-semibold transition-all cursor-pointer whitespace-nowrap active:scale-95 ${
                isActive
                  ? "bg-[#172536] text-[#FFFFFE] dark:bg-[#FFC857] dark:text-[#172536] shadow-xs"
                  : "bg-[#F9F7F1] dark:bg-[#18232E] text-[#667586] dark:text-[#9DAAB5] border border-[#E3DED2] dark:border-[#34424E]"
              }`}
            >
              {chip.label}
            </button>
          );
        })}
      </div>

      {/* 3-Column Card Grid */}
      {filteredCards.length > 0 ? (
        <div className="grid grid-cols-3 gap-2.5 sm:gap-3 pt-1">
          {filteredCards.map((card) => {
            const qty = repository.getQty(card.set, card.number);
            const isMissing = qty <= 0;

            return (
              <div
                key={`${card.set}#${card.number}`}
                onClick={() => setSelectedCard(card)}
                className="bg-[#F9F7F1] dark:bg-[#18232E] border border-[#E3DED2] dark:border-[#34424E] rounded-xl p-1.5 sm:p-2 flex flex-col justify-between cursor-pointer active:scale-95 transition-all hover:border-[#172536]/30 dark:hover:border-[#FFC857]/40 shadow-xs"
              >
                <div className="relative">
                  <CardArt
                    set={card.set}
                    number={card.number}
                    name={card.name}
                    element={card.element}
                    trainer={card.trainer}
                    className={isMissing ? "opacity-45 grayscale-[20%]" : ""}
                  />
                  {qty > 0 && (
                    <span className="absolute bottom-1 right-1 bg-[#172536]/85 dark:bg-[#FFC857] text-[#FFFFFE] dark:text-[#172536] text-[10px] font-bold px-1.5 py-0.5 rounded-md shadow-xs tabular-nums">
                      ×{qty}
                    </span>
                  )}
                </div>

                <div className="mt-1.5 px-0.5">
                  <div className="text-[11px] font-semibold text-[#172536] dark:text-[#F0EEE7] truncate">
                    {card.name}
                  </div>
                  <div className="flex items-center justify-between text-[9px] text-[#667586] dark:text-[#9DAAB5]">
                    <span>#{padNumber(card.number)}</span>
                    <span>{qty > 0 ? `×${qty}` : "—"}</span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="text-center py-12 text-[#667586] dark:text-[#9DAAB5] text-xs">
          No se encontraron cartas con los filtros seleccionados.
        </div>
      )}

      {/* Card Detail Modal */}
      <CardDetailModal
        card={selectedCard}
        qty={selectedQty}
        onClose={() => setSelectedCard(null)}
        onQtyChange={(newQty) => {
          if (selectedCard) {
            repository.setQty(selectedCard.set, selectedCard.number, newQty);
          }
        }}
        onAddToDeck={onAddToDeck}
      />
    </div>
  );
};
