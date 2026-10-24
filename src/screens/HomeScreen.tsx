import React from "react";
import { Sparkles, BookOpen, Layers, ArrowRight } from "lucide-react";
import { LabSurfaceCard } from "@/components/LabSurfaceCard";
import { ThinProgress } from "@/components/ThinProgress";
import { repository } from "@/lib/repository";

interface HomeScreenProps {
  onOpenCollection: () => void;
  onOpenDecks: () => void;
  onOpenStrategy: () => void;
  onOpenSet?: (setCode: string) => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  onOpenCollection,
  onOpenDecks,
  onOpenStrategy,
}) => {
  const stats = repository.getStats();

  return (
    <div className="space-y-4 pb-20 animate-fade-in">
      {/* Hero Banner: SIGUIENTE JUGADA */}
      <div className="w-full bg-[#172536] text-white rounded-3xl p-5 shadow-lg relative overflow-hidden">
        {/* Subtle decorative glow */}
        <div className="absolute top-0 right-0 w-48 h-48 bg-[#FFC857]/10 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10">
          <div className="flex items-center gap-2 mb-2.5">
            <div className="w-8 h-8 rounded-xl bg-[#FFC857] flex items-center justify-center text-[#172536]">
              <Sparkles className="w-4 h-4" />
            </div>
            <span className="text-[11px] font-bold tracking-wider text-white/80 uppercase">
              SIGUIENTE JUGADA
            </span>
          </div>

          <h2 className="text-xl sm:text-2xl font-bold leading-tight mb-2">
            Arma un mazo<br />que vaya contigo
          </h2>
          <p className="text-xs text-white/80 mb-4 max-w-xs leading-relaxed">
            Grok lo prepara con las cartas de tu colección y lo deja listo para el QR.
          </p>

          <button
            type="button"
            onClick={onOpenStrategy}
            className="py-3 px-5 rounded-2xl bg-[#FFC857] hover:bg-[#ffcf6e] text-[#172536] font-bold text-xs sm:text-sm flex items-center gap-2 shadow-md active:scale-95 transition-all cursor-pointer"
          >
            <span>Crear mazo con IA</span>
            <Sparkles className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Collection Overview Card */}
      <LabSurfaceCard onClick={onOpenCollection} className="p-4" raised>
        <div className="flex items-center justify-between mb-2">
          <div>
            <h3 className="text-sm font-semibold text-[#172536] dark:text-[#F0EEE7]">
              Colección
            </h3>
            <p className="text-xs text-[#667586] dark:text-[#9DAAB5]">
              {stats.distinct} de {stats.totalCards} cartas distintas
            </p>
          </div>
          <span className="text-xl font-bold text-[#172536] dark:text-[#FFC857] tabular-nums">
            {stats.percentage}%
          </span>
        </div>

        <ThinProgress
          owned={stats.distinct}
          total={stats.totalCards}
          height={6}
          className="my-3"
        />

        <div className="grid grid-cols-3 pt-2 border-t border-[#E3DED2]/50 dark:border-[#34424E]/50 text-center sm:text-left">
          <div>
            <div className="text-base font-bold text-[#172536] dark:text-[#F0EEE7] tabular-nums">
              {stats.distinct}
            </div>
            <div className="text-[11px] text-[#667586] dark:text-[#9DAAB5]">
              Cartas
            </div>
          </div>
          <div>
            <div className="text-base font-bold text-[#172536] dark:text-[#F0EEE7] tabular-nums">
              {stats.copies}
            </div>
            <div className="text-[11px] text-[#667586] dark:text-[#9DAAB5]">
              Copias
            </div>
          </div>
          <div>
            <div className="text-base font-bold text-[#172536] dark:text-[#F0EEE7] tabular-nums">
              {stats.ex}
            </div>
            <div className="text-[11px] text-[#667586] dark:text-[#9DAAB5]">
              Pokémon ex
            </div>
          </div>
        </div>
      </LabSurfaceCard>

      {/* Direct Action Navigation Cards */}
      <div>
        <h3 className="text-sm font-semibold text-[#172536] dark:text-[#F0EEE7] mb-2.5">
          Colección y mazos
        </h3>
        <div className="grid grid-cols-2 gap-3">
          <LabSurfaceCard
            onClick={onOpenCollection}
            className="p-3.5 flex flex-col justify-between h-28"
          >
            <div className="w-8 h-8 rounded-xl bg-[#F9F7F1] dark:bg-[#202D38] border border-[#E3DED2] dark:border-[#34424E] flex items-center justify-center text-[#172536] dark:text-[#FFC857]">
              <BookOpen className="w-4 h-4" />
            </div>
            <div>
              <div className="text-sm font-semibold text-[#172536] dark:text-[#F0EEE7] flex items-center justify-between">
                <span>Colección</span>
                <ArrowRight className="w-3.5 h-3.5 text-[#667586] dark:text-[#9DAAB5]" />
              </div>
              <div className="text-[11px] text-[#667586] dark:text-[#9DAAB5]">
                Explora cartas
              </div>
            </div>
          </LabSurfaceCard>

          <LabSurfaceCard
            onClick={onOpenDecks}
            className="p-3.5 flex flex-col justify-between h-28"
          >
            <div className="w-8 h-8 rounded-xl bg-[#F9F7F1] dark:bg-[#202D38] border border-[#E3DED2] dark:border-[#34424E] flex items-center justify-center text-[#172536] dark:text-[#FFC857]">
              <Layers className="w-4 h-4" />
            </div>
            <div>
              <div className="text-sm font-semibold text-[#172536] dark:text-[#F0EEE7] flex items-center justify-between">
                <span>Mazos</span>
                <ArrowRight className="w-3.5 h-3.5 text-[#667586] dark:text-[#9DAAB5]" />
              </div>
              <div className="text-[11px] text-[#667586] dark:text-[#9DAAB5]">
                Consultar mazos
              </div>
            </div>
          </LabSurfaceCard>
        </div>
      </div>
    </div>
  );
};
