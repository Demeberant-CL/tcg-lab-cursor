import React from "react";
import { Settings } from "lucide-react";

interface NavbarProps {
  title: string;
  subtitle?: string;
  onOpenSettings: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ title, subtitle, onOpenSettings }) => {
  return (
    <header className="sticky top-0 z-30 w-full bg-[#F3F0E8]/90 dark:bg-[#101820]/90 backdrop-blur-md px-4 py-2.5 border-b border-[#E3DED2]/50 dark:border-[#34424E]/50">
      <div className="max-w-xl mx-auto flex items-center justify-between">
        <div>
          <span className="text-[11px] font-bold tracking-wider text-[#667586] dark:text-[#9DAAB5] uppercase block leading-none">
            TCG LAB
          </span>
          <h1 className="text-base font-semibold text-[#172536] dark:text-[#F0EEE7] leading-tight">
            {title}
          </h1>
          {subtitle && (
            <p className="text-xs text-[#667586] dark:text-[#9DAAB5]">
              {subtitle}
            </p>
          )}
        </div>

        <button
          onClick={onOpenSettings}
          className="p-2 rounded-full text-[#172536] dark:text-[#F0EEE7] hover:bg-black/5 dark:hover:bg-white/5 transition-colors cursor-pointer"
          aria-label="Ajustes"
        >
          <Settings className="w-5 h-5" />
        </button>
      </div>
    </header>
  );
};
