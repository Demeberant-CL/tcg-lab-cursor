import React from "react";

interface ThinProgressProps {
  owned: number;
  total: number;
  className?: string;
  height?: number;
}

export const ThinProgress: React.FC<ThinProgressProps> = ({
  owned,
  total,
  className = "",
  height = 5,
}) => {
  const percent = total > 0 ? Math.min(100, Math.max(0, (owned / total) * 100)) : 0;

  return (
    <div
      className={`w-full bg-[#E9E2D4] dark:bg-[#33414B] rounded-full overflow-hidden ${className}`}
      style={{ height: `${height}px` }}
    >
      <div
        className="h-full bg-[#D99A27] dark:bg-[#FFC857] transition-all duration-300 rounded-full"
        style={{ width: `${percent}%` }}
      />
    </div>
  );
};
