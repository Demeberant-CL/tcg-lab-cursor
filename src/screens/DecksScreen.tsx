import React from "react";
import { Plus, Sparkles, QrCode, ChevronRight, Trash2 } from "lucide-react";
import { LabSurfaceCard } from "@/components/LabSurfaceCard";
import { EnergyRow } from "@/components/EnergyBadge";
import { deckTotal, canEncode, resolveDeck } from "@/lib/folio/rules";
import { energyLabel } from "@/lib/folio/game";
import type { Deck } from "@/lib/folio/types";
import { repository } from "@/lib/repository";

interface DecksScreenProps {
  onOpenDeck: (deck: Deck) => void;
  onNewDeck: () => void;
  onOpenStrategy: () => void;
  onOpenQr: (deck: Deck) => void;
}

export const DecksScreen: React.FC<DecksScreenProps> = ({
  onOpenDeck,
  onNewDeck,
  onOpenStrategy,
  onOpenQr,
}) => {
  const decks = repository.getDecks();

  const handleDelete = (e: React.MouseEvent, id: string) => {
    e.stopPropagation();
    if (window.confirm("¿Seguro que deseas eliminar este mazo?")) {
      repository.deleteDeck(id);
    }
  };

  return (
    <div className="space-y-4 pb-24 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-[#172536] dark:text-[#F0EEE7]">
            Mazos
          </h2>
          <p className="text-xs text-[#667586] dark:text-[#9DAAB5]">
            Los mazos guardados y los creados con IA aparecen aquí.
          </p>
        </div>

        <button
          type="button"
          onClick={onNewDeck}
          className="py-2 px-3.5 rounded-xl bg-[#172536] dark:bg-[#FFC857] text-[#FFFFFE] dark:text-[#172536] font-semibold text-xs flex items-center gap-1.5 shadow-xs active:scale-95 transition-all cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>Nuevo</span>
        </button>
      </div>

      {/* Button to Create with AI */}
      <button
        type="button"
        onClick={onOpenStrategy}
        className="w-full py-3 px-4 rounded-2xl bg-[#172536] text-[#FFFFFE] dark:bg-[#FFC857] dark:text-[#172536] font-semibold text-xs flex items-center justify-center gap-2 shadow-sm active:scale-98 transition-all cursor-pointer"
      >
        <Sparkles className="w-4 h-4" />
        <span>Crear mazo con IA</span>
      </button>

      {/* Decks List */}
      {decks.length === 0 ? (
        <LabSurfaceCard className="p-6 text-center">
          <p className="text-sm font-semibold text-[#172536] dark:text-[#F0EEE7] mb-1">
            Todavía no hay mazos
          </p>
          <p className="text-xs text-[#667586] dark:text-[#9DAAB5] mb-4">
            Crea tu primer mazo de 20 cartas a mano o pídele a Grok que lo arme con tu colección.
          </p>
          <div className="flex justify-center gap-2">
            <button
              onClick={onNewDeck}
              className="py-2 px-4 rounded-xl border border-[#172536] text-xs font-semibold text-[#172536] dark:text-[#F0EEE7]"
            >
              Nuevo mazo manual
            </button>
          </div>
        </LabSurfaceCard>
      ) : (
        <div className="space-y-3">
          {decks.map((deck) => {
            const total = deckTotal(deck.cards);
            const resolved = resolveDeck(deck.cards);
            const isValid = canEncode(resolved, deck.energies);

            return (
              <LabSurfaceCard
                key={deck.id}
                onClick={() => onOpenDeck(deck)}
                className="p-4 flex flex-col justify-between"
                raised
              >
                <div className="flex items-start justify-between gap-2 mb-2">
                  <div>
                    <h3 className="font-semibold text-sm text-[#172536] dark:text-[#F0EEE7]">
                      {deck.name}
                    </h3>
                    <div className="flex items-center gap-2 mt-1">
                      <span
                        className={`text-xs font-bold tabular-nums px-2 py-0.5 rounded-full ${
                          total === 20
                            ? "bg-emerald-100 dark:bg-emerald-950/50 text-emerald-800 dark:text-emerald-300"
                            : "bg-amber-100 dark:bg-amber-950/50 text-amber-800 dark:text-amber-300"
                        }`}
                      >
                        {total}/20 cartas
                      </span>
                      {deck.energies.length > 0 && (
                        <span className="text-[11px] text-[#667586] dark:text-[#9DAAB5]">
                          {energyLabel(deck.energies)}
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-1">
                    {isValid && (
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          onOpenQr(deck);
                        }}
                        className="p-2 rounded-xl bg-neutral-100 dark:bg-neutral-800 text-[#172536] dark:text-[#FFC857] hover:bg-neutral-200 cursor-pointer"
                        title="Ver código QR"
                      >
                        <QrCode className="w-4 h-4" />
                      </button>
                    )}
                    <button
                      type="button"
                      onClick={(e) => handleDelete(e, deck.id)}
                      className="p-2 rounded-xl text-[#667586] hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-950/20 cursor-pointer"
                      title="Eliminar mazo"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                    <ChevronRight className="w-4 h-4 text-[#667586]" />
                  </div>
                </div>

                {deck.energies.length > 0 && (
                  <div className="pt-2 border-t border-[#E3DED2]/40 dark:border-[#34424E]/40 flex items-center justify-between">
                    <EnergyRow energies={deck.energies} size="sm" />
                    <span className="text-[10px] text-[#667586] dark:text-[#9DAAB5]">
                      {isValid ? "Listo para QR" : "Incompleto"}
                    </span>
                  </div>
                )}
              </LabSurfaceCard>
            );
          })}
        </div>
      )}
    </div>
  );
};
