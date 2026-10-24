import React, { useState } from "react";
import { cardImageFallbacks } from "@/lib/folio/catalog";
import { padNumber } from "@/lib/folio/game";
import { ElementDot } from "@/components/ElementDot";

interface CardArtProps {
  set: string;
  number: number;
  name: string;
  element?: string;
  trainer?: boolean;
  className?: string;
  onClick?: () => void;
}

export const CardArt: React.FC<CardArtProps> = ({
  set,
  number,
  name,
  element = "",
  trainer = false,
  className = "",
  onClick,
}) => {
  const fallbacks = cardImageFallbacks(set, number);
  const [srcIndex, setSrcIndex] = useState(0);
  const [hasError, setHasError] = useState(false);
  const [isLoaded, setIsLoaded] = useState(false);

  const handleError = () => {
    if (srcIndex + 1 < fallbacks.length) {
      setSrcIndex(srcIndex + 1);
    } else {
      setHasError(true);
    }
  };

  return (
    <div
      onClick={onClick}
      className={`relative rounded-xl overflow-hidden bg-[#E9E2D4] dark:bg-[#202D38] border border-[#E3DED2]/50 dark:border-[#34424E]/50 aspect-[0.72] flex items-center justify-center cursor-pointer select-none transition-transform active:scale-[0.98] ${className}`}
    >
      {!hasError ? (
        <>
          {!isLoaded && (
            <div className="absolute inset-0 bg-neutral-200/50 dark:bg-neutral-800/50 animate-pulse flex items-center justify-center">
              <span className="text-[10px] text-neutral-400 font-mono">
                {set} #{number}
              </span>
            </div>
          )}
          <img
            src={fallbacks[srcIndex]}
            alt={name}
            loading="lazy"
            onLoad={() => setIsLoaded(true)}
            onError={handleError}
            className={`w-full h-full object-cover transition-opacity duration-200 ${
              isLoaded ? "opacity-100" : "opacity-0"
            }`}
          />
        </>
      ) : (
        // Fallback typography card frame
        <div className="w-full h-full p-2 flex flex-col justify-between bg-gradient-to-br from-[#F9F7F1] to-[#E9E2D4] dark:from-[#202D38] dark:to-[#18232E] text-center">
          <div className="flex justify-between items-center text-[10px] text-[#667586] dark:text-[#9DAAB5]">
            <span className="font-semibold">{set}</span>
            <span>#{padNumber(number)}</span>
          </div>
          <div className="flex flex-col items-center justify-center my-auto px-1">
            {element && !trainer && <ElementDot element={element} size={10} className="mb-1" />}
            <span className="text-[11px] font-semibold text-[#172536] dark:text-[#F0EEE7] line-clamp-2 leading-tight">
              {name}
            </span>
          </div>
          <div className="text-[9px] text-[#667586] dark:text-[#9DAAB5] truncate">
            {trainer ? "Entrenador" : element}
          </div>
        </div>
      )}
    </div>
  );
};
