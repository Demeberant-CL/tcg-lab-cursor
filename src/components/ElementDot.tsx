import React from "react";
import { ELEMENT_COLORS } from "@/lib/folio/game";

interface ElementDotProps {
  element: string;
  size?: number;
  className?: string;
}

export const ElementDot: React.FC<ElementDotProps> = ({ element, size = 8, className = "" }) => {
  const color = ELEMENT_COLORS[element.toLowerCase()] || "#9DAAB5";
  return (
    <span
      className={`inline-block rounded-full flex-shrink-0 ${className}`}
      style={{
        width: `${size}px`,
        height: `${size}px`,
        backgroundColor: color,
      }}
      title={`Tipo: ${element}`}
    />
  );
};
