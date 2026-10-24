import React from "react";

interface LabSurfaceCardProps {
  children: React.ReactNode;
  onClick?: () => void;
  className?: string;
  raised?: boolean;
}

export const LabSurfaceCard: React.FC<LabSurfaceCardProps> = ({
  children,
  onClick,
  className = "",
  raised = false,
}) => {
  return (
    <div
      onClick={onClick}
      className={`rounded-2xl border transition-all ${
        raised
          ? "bg-[#FFFFFE] dark:bg-[#202D38] border-[#E3DED2] dark:border-[#34424E] shadow-sm"
          : "bg-[#F9F7F1] dark:bg-[#18232E] border-[#E3DED2] dark:border-[#34424E]"
      } ${
        onClick
          ? "cursor-pointer active:scale-[0.99] hover:border-[#172536]/20 dark:hover:border-[#FFC857]/30"
          : ""
      } ${className}`}
    >
      {children}
    </div>
  );
};
