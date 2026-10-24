import React, { useState } from "react";
import { MoreVertical, Upload, Download, RotateCcw, ChevronRight } from "lucide-react";
import { LabSurfaceCard } from "@/components/LabSurfaceCard";
import { ThinProgress } from "@/components/ThinProgress";
import { ImportCsvModal } from "@/components/ImportCsvModal";
import { downloadText } from "@/lib/folio/csv";
import { repository } from "@/lib/repository";

interface CollectionScreenProps {
  onSelectSet: (setCode: string) => void;
}

export const CollectionScreen: React.FC<CollectionScreenProps> = ({ onSelectSet }) => {
  const [menuOpen, setMenuOpen] = useState(false);
  const [showImportModal, setShowImportModal] = useState(false);
  const [showResetConfirm, setShowResetConfirm] = useState(false);
  const [notification, setNotification] = useState<string | null>(null);

  const stats = repository.getStats();
  const setsProgress = repository.getSetsProgress();

  const handleExportCsv = () => {
    setMenuOpen(false);
    const csvData = repository.exportCsv();
    downloadText("tcg-lab-coleccion.csv", csvData);
    showNotice("Colección exportada correctamente en CSV.");
  };

  const handleResetSeed = () => {
    setMenuOpen(false);
    setShowResetConfirm(true);
  };

  const confirmResetSeed = () => {
    repository.resetToSeed();
    setShowResetConfirm(false);
    showNotice("Colección restablecida a la semilla inicial.");
  };

  const showNotice = (msg: string) => {
    setNotification(msg);
    setTimeout(() => setNotification(null), 3000);
  };

  return (
    <div className="space-y-4 pb-20 animate-fade-in relative">
      {/* Top action bar with title & menu */}
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-bold text-[#172536] dark:text-[#F0EEE7]">
          Colección
        </h2>

        {/* 3-dots Menu */}
        <div className="relative">
          <button
            type="button"
            onClick={() => setMenuOpen(!menuOpen)}
            className="p-2 rounded-xl text-[#172536] dark:text-[#F0EEE7] hover:bg-black/5 dark:hover:bg-white/5 transition-colors cursor-pointer"
            aria-label="Opciones de colección"
          >
            <MoreVertical className="w-5 h-5" />
          </button>

          {menuOpen && (
            <>
              <div className="fixed inset-0 z-40" onClick={() => setMenuOpen(false)} />
              <div className="absolute right-0 top-full mt-1 w-56 bg-[#FFFFFE] dark:bg-[#202D38] rounded-2xl shadow-xl border border-[#E3DED2] dark:border-[#34424E] py-1.5 z-50 animate-scale-in">
                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    setShowImportModal(true);
                  }}
                  className="w-full px-4 py-2.5 text-left text-xs font-semibold text-[#172536] dark:text-[#F0EEE7] hover:bg-[#F9F7F1] dark:hover:bg-[#18232E] flex items-center gap-2.5 cursor-pointer"
                >
                  <Upload className="w-4 h-4 text-[#667586] dark:text-[#9DAAB5]" />
                  <span>Importar CSV</span>
                </button>

                <button
                  type="button"
                  onClick={handleExportCsv}
                  className="w-full px-4 py-2.5 text-left text-xs font-semibold text-[#172536] dark:text-[#F0EEE7] hover:bg-[#F9F7F1] dark:hover:bg-[#18232E] flex items-center gap-2.5 cursor-pointer"
                >
                  <Download className="w-4 h-4 text-[#667586] dark:text-[#9DAAB5]" />
                  <span>Exportar CSV</span>
                </button>

                <div className="my-1 border-t border-[#E3DED2]/50 dark:border-[#34424E]/50" />

                <button
                  type="button"
                  onClick={handleResetSeed}
                  className="w-full px-4 py-2.5 text-left text-xs font-semibold text-[#E53935] hover:bg-red-50 dark:hover:bg-red-950/20 flex items-center gap-2.5 cursor-pointer"
                >
                  <RotateCcw className="w-4 h-4 text-[#E53935]" />
                  <span>Volver al archivo original</span>
                </button>
              </div>
            </>
          )}
        </div>
      </div>

      {/* Global Collection Stats Header */}
      <LabSurfaceCard className="p-4" raised>
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs font-semibold text-[#667586] dark:text-[#9DAAB5]">
            Progreso general del álbum
          </span>
          <span className="text-xs font-bold text-[#172536] dark:text-[#FFC857] tabular-nums">
            {stats.distinct} / {stats.totalCards}
          </span>
        </div>
        <ThinProgress owned={stats.distinct} total={stats.totalCards} height={6} className="mb-3" />
        <div className="grid grid-cols-3 pt-2 border-t border-[#E3DED2]/50 dark:border-[#34424E]/50 text-center">
          <div>
            <div className="text-base font-bold text-[#172536] dark:text-[#F0EEE7] tabular-nums">
              {stats.distinct}
            </div>
            <div className="text-[11px] text-[#667586] dark:text-[#9DAAB5]">Cartas distintas</div>
          </div>
          <div>
            <div className="text-base font-bold text-[#172536] dark:text-[#F0EEE7] tabular-nums">
              {stats.copies}
            </div>
            <div className="text-[11px] text-[#667586] dark:text-[#9DAAB5]">Copias totales</div>
          </div>
          <div>
            <div className="text-base font-bold text-[#172536] dark:text-[#F0EEE7] tabular-nums">
              {stats.ex}
            </div>
            <div className="text-[11px] text-[#667586] dark:text-[#9DAAB5]">Pokémon ex</div>
          </div>
        </div>
      </LabSurfaceCard>

      {/* Sets Grid / List */}
      <div>
        <h3 className="text-xs font-bold uppercase tracking-wider text-[#667586] dark:text-[#9DAAB5] mb-2 px-1">
          Expansiones
        </h3>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
          {setsProgress.map((item) => (
            <LabSurfaceCard
              key={item.set.code}
              onClick={() => onSelectSet(item.set.code)}
              className="p-3.5 flex flex-col justify-between"
            >
              <div>
                <div className="flex items-start justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <h4 className="font-semibold text-sm text-[#172536] dark:text-[#F0EEE7] truncate">
                      {item.set.es}
                    </h4>
                    <span className="text-xs text-[#667586] dark:text-[#9DAAB5] font-mono">
                      {item.set.code}
                    </span>
                  </div>
                  <ChevronRight className="w-4 h-4 text-[#667586] dark:text-[#9DAAB5] flex-shrink-0 mt-0.5" />
                </div>
              </div>

              <div className="mt-3">
                <div className="flex items-center justify-between text-xs text-[#667586] dark:text-[#9DAAB5] mb-1.5 tabular-nums">
                  <span>{item.ownedDistinct} / {item.total}</span>
                  <span className="font-medium text-[#172536] dark:text-[#FFC857]">{item.percentage}%</span>
                </div>
                <ThinProgress owned={item.ownedDistinct} total={item.total} height={4} />
              </div>
            </LabSurfaceCard>
          ))}
        </div>
      </div>

      {/* Notification Toast */}
      {notification && (
        <div className="fixed bottom-20 left-1/2 -translate-x-1/2 z-50 bg-[#172536] dark:bg-[#202D38] text-[#FFFFFE] text-xs px-4 py-2.5 rounded-full shadow-lg border border-white/10 animate-fade-in flex items-center gap-2">
          <span>{notification}</span>
        </div>
      )}

      {/* Import Modal */}
      {showImportModal && (
        <ImportCsvModal
          onClose={() => setShowImportModal(false)}
          onImportComplete={(count) => {
            showNotice(`Se importaron ${count} cartas con éxito.`);
          }}
        />
      )}

      {/* Reset to Seed Confirmation Modal */}
      {showResetConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs">
          <div className="bg-[#FFFFFE] dark:bg-[#202D38] rounded-3xl p-6 max-w-sm w-full border border-[#E3DED2] dark:border-[#34424E] shadow-2xl animate-scale-in">
            <h3 className="text-base font-bold text-[#172536] dark:text-[#F0EEE7] mb-2">
              Volver al archivo original
            </h3>
            <p className="text-xs text-[#667586] dark:text-[#9DAAB5] mb-5 leading-relaxed">
              ¿Deseas restablecer la colección a los valores de la semilla inicial? Se perderán las modificaciones no respaldadas en CSV.
            </p>
            <div className="grid grid-cols-2 gap-3">
              <button
                type="button"
                onClick={() => setShowResetConfirm(false)}
                className="py-2.5 px-3 rounded-xl border border-[#172536]/20 text-xs font-semibold text-[#172536] dark:text-[#F0EEE7]"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={confirmResetSeed}
                className="py-2.5 px-3 rounded-xl bg-[#E53935] text-white text-xs font-semibold shadow-xs active:scale-95 transition-all"
              >
                Restablecer
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
