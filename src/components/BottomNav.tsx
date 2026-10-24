import React from "react";
import { BookOpen, Layers, Sparkles, Home } from "lucide-react";

export type TabType = "home" | "collection" | "decks" | "strategy";

interface BottomNavProps {
  currentTab: TabType;
  onSelectTab: (tab: TabType) => void;
}

export const BottomNav: React.FC<BottomNavProps> = ({ currentTab, onSelectTab }) => {
  const tabs = [
    { id: "home" as TabType, label: "Inicio", icon: Home },
    { id: "collection" as TabType, label: "Colección", icon: BookOpen },
    { id: "decks" as TabType, label: "Mazos", icon: Layers },
    { id: "strategy" as TabType, label: "Grok", icon: Sparkles },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#FFFFFE]/95 dark:bg-[#18232E]/95 backdrop-blur-md border-t border-[#E3DED2] dark:border-[#34424E] px-2 py-1.5 safe-area-bottom shadow-lg">
      <div className="max-w-md mx-auto grid grid-cols-4 gap-1">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = currentTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onSelectTab(tab.id)}
              className={`flex flex-col items-center justify-center py-1.5 px-2 rounded-2xl transition-all cursor-pointer select-none active:scale-95 ${
                isActive
                  ? "text-[#172536] dark:text-[#FFC857] font-semibold"
                  : "text-[#667586] dark:text-[#9DAAB5] hover:text-[#172536] dark:hover:text-[#F0EEE7]"
              }`}
            >
              <div
                className={`p-1 rounded-xl transition-all ${
                  isActive
                    ? "bg-[#172536]/10 dark:bg-[#FFC857]/20"
                    : "bg-transparent"
                }`}
              >
                <Icon className="w-5 h-5" />
              </div>
              <span className="text-[11px] mt-0.5 tracking-tight">{tab.label}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
