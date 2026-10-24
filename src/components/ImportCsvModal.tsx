import React, { useState } from "react";
import { Upload, X, FileText, CheckCircle2 } from "lucide-react";
import { repository } from "@/lib/repository";

interface ImportCsvModalProps {
  onClose: () => void;
  onImportComplete: (count: number) => void;
}

export const ImportCsvModal: React.FC<ImportCsvModalProps> = ({
  onClose,
  onImportComplete,
}) => {
  const [csvContent, setCsvContent] = useState<string>("");
  const [fileName, setFileName] = useState<string>("");
  const [replaceMode, setReplaceMode] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setFileName(file.name);
    setError(null);
    const reader = new FileReader();
    reader.onload = (event) => {
      const text = event.target?.result as string;
      setCsvContent(text || "");
    };
    reader.onerror = () => {
      setError("No se pudo leer el archivo seleccionado.");
    };
    reader.readAsText(file);
  };

  const handleConfirm = () => {
    if (!csvContent.trim()) {
      setError("Selecciona o pega un archivo CSV válido.");
      return;
    }
    const res = repository.importCsv(csvContent, replaceMode);
    if (res.success) {
      onImportComplete(res.count);
      onClose();
    } else {
      setError(res.error || "Error al procesar el archivo CSV.");
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs transition-opacity animate-fade-in">
      <div className="absolute inset-0" onClick={onClose} />

      <div className="relative w-full max-w-md bg-[#FFFFFE] dark:bg-[#202D38] rounded-3xl p-6 shadow-2xl border border-[#E3DED2] dark:border-[#34424E] animate-scale-in">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-[#E3DED2] dark:border-[#34424E] mb-4">
          <div className="flex items-center gap-2">
            <Upload className="w-5 h-5 text-[#172536] dark:text-[#FFC857]" />
            <h2 className="text-lg font-bold text-[#172536] dark:text-[#F0EEE7]">
              Importar CSV
            </h2>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-full text-[#667586] hover:bg-black/5 dark:hover:bg-white/5 transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <p className="text-xs text-[#667586] dark:text-[#9DAAB5] mb-4 leading-relaxed">
          Compatible con exportaciones de <strong>Pokémon Zone</strong> (columnas Set, Número, Nombre, Rareza, Cantidad, Registrada).
        </p>

        {/* File input area */}
        <label className="border-2 border-dashed border-[#E3DED2] dark:border-[#34424E] hover:border-[#172536] dark:hover:border-[#FFC857] rounded-2xl p-6 flex flex-col items-center justify-center cursor-pointer transition-colors bg-[#F9F7F1]/50 dark:bg-[#18232E]/50 mb-4 block text-center">
          <input
            type="file"
            accept=".csv,text/csv,text/plain"
            onChange={handleFileChange}
            className="hidden"
          />
          {fileName ? (
            <div className="flex items-center gap-2 text-sm text-[#172536] dark:text-[#F0EEE7] font-semibold">
              <FileText className="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
              <span className="truncate max-w-[240px]">{fileName}</span>
            </div>
          ) : (
            <>
              <Upload className="w-7 h-7 text-[#667586] dark:text-[#9DAAB5] mb-2" />
              <span className="text-sm font-semibold text-[#172536] dark:text-[#F0EEE7]">
                Seleccionar archivo CSV
              </span>
              <span className="text-xs text-[#667586] dark:text-[#9DAAB5] mt-1">
                o toca para explorar tus archivos
              </span>
            </>
          )}
        </label>

        {/* Mode selection: Combinar vs Reemplazar */}
        <div className="bg-[#F9F7F1] dark:bg-[#18232E] p-4 rounded-2xl border border-[#E3DED2] dark:border-[#34424E] mb-4">
          <div className="text-xs font-semibold text-[#172536] dark:text-[#F0EEE7] mb-2">
            Método de importación
          </div>
          <div className="grid grid-cols-2 gap-2">
            <button
              type="button"
              onClick={() => setReplaceMode(false)}
              className={`p-3 rounded-xl border text-xs font-semibold flex items-center justify-center gap-2 transition-all ${
                !replaceMode
                  ? "bg-[#FFFFFE] dark:bg-[#202D38] border-[#172536] dark:border-[#FFC857] text-[#172536] dark:text-[#FFC857] shadow-xs"
                  : "border-transparent text-[#667586] dark:text-[#9DAAB5] hover:bg-neutral-200/50"
              }`}
            >
              {!replaceMode && <CheckCircle2 className="w-4 h-4" />}
              <span>Combinar</span>
            </button>

            <button
              type="button"
              onClick={() => setReplaceMode(true)}
              className={`p-3 rounded-xl border text-xs font-semibold flex items-center justify-center gap-2 transition-all ${
                replaceMode
                  ? "bg-[#FFFFFE] dark:bg-[#202D38] border-[#172536] dark:border-[#FFC857] text-[#172536] dark:text-[#FFC857] shadow-xs"
                  : "border-transparent text-[#667586] dark:text-[#9DAAB5] hover:bg-neutral-200/50"
              }`}
            >
              {replaceMode && <CheckCircle2 className="w-4 h-4" />}
              <span>Reemplazar</span>
            </button>
          </div>
          <p className="text-[11px] text-[#667586] dark:text-[#9DAAB5] mt-2">
            {!replaceMode
              ? "Conserva las cartas actuales y actualiza las cantidades importadas."
              : "Borra toda la colección previa e importa únicamente las cartas de este archivo."}
          </p>
        </div>

        {error && (
          <div className="text-xs text-red-600 dark:text-red-400 mb-4 p-3 rounded-xl bg-red-50 dark:bg-red-950/30">
            {error}
          </div>
        )}

        {/* Buttons */}
        <div className="grid grid-cols-2 gap-3">
          <button
            type="button"
            onClick={onClose}
            className="py-3 px-4 rounded-xl border border-[#172536]/20 dark:border-[#FFC857]/30 text-[#172536] dark:text-[#F0EEE7] font-semibold text-sm hover:bg-neutral-50 dark:hover:bg-neutral-800 transition-colors"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={handleConfirm}
            disabled={!csvContent}
            className="py-3 px-4 rounded-xl bg-[#172536] dark:bg-[#FFC857] text-[#FFFFFE] dark:text-[#172536] font-semibold text-sm disabled:opacity-40 disabled:cursor-not-allowed active:scale-95 transition-all shadow-xs"
          >
            Importar
          </button>
        </div>
      </div>
    </div>
  );
};
