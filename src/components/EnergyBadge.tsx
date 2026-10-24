import React from "react";
import { ENERGIES, energyById } from "@/lib/folio/game";

interface EnergyBadgeProps {
  id: number;
  size?: "sm" | "md" | "lg";
  showLabel?: boolean;
  selected?: boolean;
  onClick?: () => void;
}

const ENERGY_COLORS: Record<number, string> = {
  1: "#4CAF50", // Planta
  2: "#E53935", // Fuego
  3: "#1E88E5", // Agua
  4: "#F9A825", // Rayo
  5: "#8E24AA", // Psíquica
  6: "#D84315", // Lucha
  7: "#455A64", // Oscura
  8: "#78909C", // Metálica
};

export const EnergyBadge: React.FC<EnergyBadgeProps> = ({
  id,
  size = "md",
  showLabel = false,
  selected = false,
  onClick,
}) => {
  const energy = energyById(id);
  const color = ENERGY_COLORS[id] || "#9DAAB5";

  const sizeClasses = {
    sm: "w-3 h-3 text-[10px]",
    md: "w-4 h-4 text-xs",
    lg: "w-6 h-6 text-sm",
  }[size];

  return (
    <button
      type="button"
      onClick={onClick}
      disabled={!onClick}
      className={`inline-flex items-center gap-1.5 rounded-full transition-all ${
        onClick ? "cursor-pointer active:scale-95" : ""
      } ${
        selected
          ? "ring-2 ring-[#172536] dark:ring-[#FFC857] px-2 py-1 bg-neutral-100 dark:bg-neutral-800"
          : ""
      }`}
      title={energy?.es || `Energía ${id}`}
    >
      <span
        className={`rounded-full flex-shrink-0 ${sizeClasses}`}
        style={{ backgroundColor: color }}
      />
      {showLabel && energy && (
        <span className="text-xs font-medium text-[#172536] dark:text-[#F0EEE7]">
          {energy.es}
        </span>
      )}
    </button>
  );
};

export const EnergyRow: React.FC<{ energies: number[]; size?: "sm" | "md" }> = ({
  energies,
  size = "md",
}) => {
  if (!energies.length) return null;
  return (
    <div className="flex items-center gap-1.5 flex-wrap">
      {energies.map((id) => (
        <EnergyBadge key={id} id={id} size={size} />
      ))}
    </div>
  );
};
