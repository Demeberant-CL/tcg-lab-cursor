import React, { useState } from "react";
import { ArrowLeft, Moon, Sun, Monitor, Download, Key, Check } from "lucide-react";
import { LabSurfaceCard } from "@/components/LabSurfaceCard";
import { downloadText } from "@/lib/folio/csv";
import { repository, type ThemeMode } from "@/lib/repository";

interface SettingsScreenProps {
  onBack: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({ onBack }) => {
  const [theme, setTheme] = useState<ThemeMode>(repository.getTheme());
  const [grokKey, setGrokKey] = useState<string>(repository.getApiKey("grok"));
  const [openaiKey, setOpenaiKey] = useState<string>(repository.getApiKey("openai"));
  const [geminiKey, setGeminiKey] = useState<string>(repository.getApiKey("gemini"));
  const [groqKey, setGroqKey] = useState<string>(repository.getApiKey("groq"));
  const [savedNotice, setSavedNotice] = useState<string | null>(null);

  const handleThemeChange = (mode: ThemeMode) => {
    setTheme(mode);
    repository.setTheme(mode);
  };

  const handleSaveKeys = () => {
    repository.saveApiKey("grok", grokKey);
    repository.saveApiKey("openai", openaiKey);
    repository.saveApiKey("gemini", geminiKey);
    repository.saveApiKey("groq", groqKey);
    showSaved("Claves guardadas correctamente.");
  };

  const handleExportLogs = () => {
    const logData = repository.exportLogs();
    downloadText("tcg-lab-diagnostico.json", logData, "application/json");
    showSaved("Registro diagnóstico descargado.");
  };

  const showSaved = (msg: string) => {
    setSavedNotice(msg);
    setTimeout(() => setSavedNotice(null), 3000);
  };

  return (
    <div className="space-y-4 pb-24 animate-fade-in">
      {/* Header */}
      <div className="flex items-center gap-2 pb-2 border-b border-[#E3DED2]/40 dark:border-[#34424E]/40">
        <button
          type="button"
          onClick={onBack}
          className="p-2 -ml-2 rounded-full text-[#172536] dark:text-[#F0EEE7] hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h2 className="text-base font-bold text-[#172536] dark:text-[#F0EEE7]">
          Ajustes
        </h2>
      </div>

      {/* 1. Theme Selection */}
      <div>
        <h3 className="text-xs font-bold uppercase tracking-wider text-[#667586] dark:text-[#9DAAB5] mb-2 px-1">
          Tema
        </h3>
        <LabSurfaceCard className="p-3">
          <div className="grid grid-cols-3 gap-2">
            {[
              { id: "system" as ThemeMode, label: "Sistema", icon: Monitor },
              { id: "light" as ThemeMode, label: "Claro", icon: Sun },
              { id: "dark" as ThemeMode, label: "Oscuro", icon: Moon },
            ].map((opt) => {
              const Icon = opt.icon;
              const isSelected = theme === opt.id;
              return (
                <button
                  key={opt.id}
                  type="button"
                  onClick={() => handleThemeChange(opt.id)}
                  className={`py-3 px-2 rounded-xl flex flex-col items-center justify-center gap-1.5 transition-all text-xs font-semibold cursor-pointer ${
                    isSelected
                      ? "bg-[#172536] text-[#FFFFFE] dark:bg-[#FFC857] dark:text-[#172536] shadow-xs"
                      : "bg-[#F9F7F1] dark:bg-[#202D38] text-[#667586] dark:text-[#9DAAB5] border border-[#E3DED2] dark:border-[#34424E]"
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span>{opt.label}</span>
                </button>
              );
            })}
          </div>
        </LabSurfaceCard>
      </div>

      {/* 2. Asistente (Grok & AI Keys) */}
      <div>
        <h3 className="text-xs font-bold uppercase tracking-wider text-[#667586] dark:text-[#9DAAB5] mb-2 px-1">
          Claves de Asistentes IA
        </h3>
        <LabSurfaceCard className="p-4 space-y-3" raised>
          <p className="text-xs text-[#667586] dark:text-[#9DAAB5] leading-relaxed">
            Se almacenan localmente en tu dispositivo. Puedes usar la clave oficial de Grok (xAI) o de otros proveedores compatibles.
          </p>

          <div className="space-y-2.5">
            <div>
              <label className="text-[11px] font-semibold text-[#172536] dark:text-[#F0EEE7] block mb-1">
                Grok (xAI API Key)
              </label>
              <input
                type="password"
                value={grokKey}
                onChange={(e) => setGrokKey(e.target.value)}
                placeholder="xai-..."
                className="w-full bg-[#FFFFFE] dark:bg-[#18232E] border border-[#E3DED2] dark:border-[#34424E] rounded-xl px-3 py-2 text-xs text-[#172536] dark:text-[#F0EEE7] focus:outline-none"
              />
            </div>

            <div>
              <label className="text-[11px] font-semibold text-[#172536] dark:text-[#F0EEE7] block mb-1">
                Gemini API Key
              </label>
              <input
                type="password"
                value={geminiKey}
                onChange={(e) => setGeminiKey(e.target.value)}
                placeholder="AIzaSy..."
                className="w-full bg-[#FFFFFE] dark:bg-[#18232E] border border-[#E3DED2] dark:border-[#34424E] rounded-xl px-3 py-2 text-xs text-[#172536] dark:text-[#F0EEE7] focus:outline-none"
              />
            </div>

            <div>
              <label className="text-[11px] font-semibold text-[#172536] dark:text-[#F0EEE7] block mb-1">
                OpenAI API Key
              </label>
              <input
                type="password"
                value={openaiKey}
                onChange={(e) => setOpenaiKey(e.target.value)}
                placeholder="sk-..."
                className="w-full bg-[#FFFFFE] dark:bg-[#18232E] border border-[#E3DED2] dark:border-[#34424E] rounded-xl px-3 py-2 text-xs text-[#172536] dark:text-[#F0EEE7] focus:outline-none"
              />
            </div>

            <div>
              <label className="text-[11px] font-semibold text-[#172536] dark:text-[#F0EEE7] block mb-1">
                Groq API Key
              </label>
              <input
                type="password"
                value={groqKey}
                onChange={(e) => setGroqKey(e.target.value)}
                placeholder="gsk_..."
                className="w-full bg-[#FFFFFE] dark:bg-[#18232E] border border-[#E3DED2] dark:border-[#34424E] rounded-xl px-3 py-2 text-xs text-[#172536] dark:text-[#F0EEE7] focus:outline-none"
              />
            </div>
          </div>

          <div className="pt-2 flex justify-end">
            <button
              type="button"
              onClick={handleSaveKeys}
              className="py-2 px-4 rounded-xl bg-[#172536] dark:bg-[#FFC857] text-[#FFFFFE] dark:text-[#172536] text-xs font-semibold shadow-xs active:scale-95 transition-all flex items-center gap-1.5 cursor-pointer"
            >
              <Key className="w-3.5 h-3.5" />
              <span>Guardar claves</span>
            </button>
          </div>
        </LabSurfaceCard>
      </div>

      {/* 3. Diagnóstico y Registro */}
      <div>
        <h3 className="text-xs font-bold uppercase tracking-wider text-[#667586] dark:text-[#9DAAB5] mb-2 px-1">
          Diagnóstico
        </h3>
        <LabSurfaceCard className="p-4 flex items-center justify-between gap-4">
          <div>
            <div className="text-xs font-semibold text-[#172536] dark:text-[#F0EEE7]">
              Registro técnico
            </div>
            <p className="text-[11px] text-[#667586] dark:text-[#9DAAB5] mt-0.5">
              Descarga eventos técnicos y estado para depuración.
            </p>
          </div>
          <button
            type="button"
            onClick={handleExportLogs}
            className="py-2 px-3.5 rounded-xl border border-[#172536]/20 dark:border-[#FFC857]/30 text-xs font-semibold text-[#172536] dark:text-[#F0EEE7] hover:bg-neutral-50 dark:hover:bg-neutral-800 transition-colors flex items-center gap-1.5 flex-shrink-0 cursor-pointer"
          >
            <Download className="w-4 h-4" />
            <span>Exportar</span>
          </button>
        </LabSurfaceCard>
      </div>

      {/* 4. Acerca de */}
      <div>
        <h3 className="text-xs font-bold uppercase tracking-wider text-[#667586] dark:text-[#9DAAB5] mb-2 px-1">
          Acerca de
        </h3>
        <LabSurfaceCard className="p-4">
          <div className="text-sm font-bold text-[#172536] dark:text-[#F0EEE7]">
            TCG LAB v1.0.2
          </div>
          <p className="text-xs text-[#667586] dark:text-[#9DAAB5] mt-1 leading-relaxed">
            Álbum personal de cartas, mazos de 20 y código QR para Pokémon TCG Pocket.
            Toda la interfaz en español neutro sin publicidad.
          </p>
        </LabSurfaceCard>
      </div>

      {/* Toast Notification */}
      {savedNotice && (
        <div className="fixed bottom-20 left-1/2 -translate-x-1/2 z-50 bg-[#172536] dark:bg-[#202D38] text-[#FFFFFE] text-xs px-4 py-2.5 rounded-full shadow-lg border border-white/10 animate-fade-in flex items-center gap-2">
          <Check className="w-3.5 h-3.5 text-emerald-400" />
          <span>{savedNotice}</span>
        </div>
      )}
    </div>
  );
};
