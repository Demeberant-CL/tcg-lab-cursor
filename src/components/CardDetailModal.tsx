import React from "react";
import { X, Plus, Minus, Layers } from "lucide-react";
import { CardArt } from "@/components/CardArt";
import { ElementDot } from "@/components/ElementDot";
import { padNumber, RARITIES, rarityMark } from "@/lib/folio/game";
import type { CardRow } from "@/lib/folio/types";

interface CardDetailModalProps {
  card: CardRow | null;
  qty: number;
  onClose: () => void;
  onQtyChange: (newQty: number) => void;
  onAddToDeck?: (card: CardRow) => void;
}

export const CardDetailModal: React.FC<CardDetailModalProps> = ({
  card,
  qty,
  onClose,
  onQtyChange,
  onAddToDeck,
}) => {
  if (!card) return null;

  const rarityInfo = RARITIES[card.rarity];

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/60 backdrop-blur-xs transition-opacity animate-fade-in">
      {/* Backdrop click to close */}
      <div className="absolute inset-0" onClick={onClose} />

      <div className="relative w-full max-w-sm bg-[#FFFFFE] dark:bg-[#202D38] rounded-t-3xl sm:rounded-3xl p-6 shadow-2xl border-t sm:border border-[#E3DED2] dark:border-[#34424E] max-h-[90vh] overflow-y-auto animate-slide-up">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-2.5 rounded-full text-[#667586] hover:bg-black/5 dark:hover:bg-white/5 transition-colors cursor-pointer"
          aria-label="Cerrar"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Card Artwork */}
        <div className="flex justify-center mb-4">
          <div className="w-48 shadow-lg rounded-xl overflow-hidden">
            <CardArt
              set={card.set}
              number={card.number}
              name={card.name}
              element={card.element}
              trainer={card.trainer}
              className="w-full"
            />
          </div>
        </div>

        {/* Title & Element */}
        <div className="text-center mb-4">
          <div className="flex items-center justify-center gap-2 mb-1">
            {card.element && !card.trainer && (
              <ElementDot element={card.element} size={10} />
            )}
            <h2 className="text-lg font-bold text-[#172536] dark:text-[#F0EEE7]">
              {card.name}
            </h2>
          </div>
          <p className="text-xs text-[#667586] dark:text-[#9DAAB5]">
            {card.set} · #{padNumber(card.number)}
            {card.rarity && ` · ${rarityMark(card.rarity)} (${rarityInfo?.es || card.rarity})`}
          </p>
        </div>

        {/* Quantity Controls */}
        <div className="bg-[#F9F7F1] dark:bg-[#18232E] rounded-2xl p-4 mb-4 border border-[#E3DED2] dark:border-[#34424E]">
          <div className="text-xs text-center text-[#667586] dark:text-[#9DAAB5] mb-2 font-medium">
            Copias en tu colección
          </div>
          <div className="flex items-center justify-center gap-6">
            <button
              type="button"
              onClick={() => onQtyChange(Math.max(0, qty - 1))}
              disabled={qty <= 0}
              className="w-12 h-12 flex items-center justify-center rounded-2xl bg-[#172536] dark:bg-[#FFC857] text-[#FFFFFE] dark:text-[#172536] font-bold text-lg disabled:opacity-30 disabled:cursor-not-allowed active:scale-95 transition-all shadow-xs"
              aria-label="Restar una copia"
            >
              <Minus className="w-5 h-5" />
            </button>
            <div className="text-2xl font-bold tabular-nums text-[#172536] dark:text-[#F0EEE7] w-12 text-center">
              {qty}
            </div>
            <button
              type="button"
              onClick={() => onQtyChange(Math.min(99, qty + 1))}
              className="w-12 h-12 flex items-center justify-center rounded-2xl bg-[#172536] dark:bg-[#FFC857] text-[#FFFFFE] dark:text-[#172536] font-bold text-lg active:scale-95 transition-all shadow-xs"
              aria-label="Sumar una copia"
            >
              <Plus className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col gap-2">
          {onAddToDeck && (
            <button
              type="button"
              onClick={() => {
                onAddToDeck(card);
                onClose();
              }}
              className="w-full py-3.5 px-4 rounded-xl border border-[#172536]/20 dark:border-[#FFC857]/30 text-[#172536] dark:text-[#F0EEE7] font-semibold flex items-center justify-center gap-2 hover:bg-neutral-50 dark:hover:bg-neutral-800 transition-colors"
            >
              <Layers className="w-4 h-4" />
              <span>Sumar al mazo actual</span>
            </button>
          )}

          <button
            type="button"
            onClick={onClose}
            className="w-full py-3 px-4 rounded-xl text-[#667586] dark:text-[#9DAAB5] font-medium text-sm hover:text-[#172536] dark:hover:text-[#F0EEE7] transition-colors"
          >
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
};
