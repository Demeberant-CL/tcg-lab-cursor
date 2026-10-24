import React, { useRef, useState } from "react";
import { QRCodeSVG } from "qrcode.react";
import { X, Download, Copy, Check, QrCode } from "lucide-react";
import { resolveDeck, encodeResolved } from "@/lib/folio/rules";
import type { Deck } from "@/lib/folio/types";

interface QrModalProps {
  deck: Deck | null;
  onClose: () => void;
}

export const QrModal: React.FC<QrModalProps> = ({ deck, onClose }) => {
  const [copied, setCopied] = useState(false);
  const svgRef = useRef<SVGSVGElement | null>(null);

  if (!deck) return null;

  const resolved = resolveDeck(deck.cards);
  let base64Code = "";
  let errorMsg = "";

  try {
    base64Code = encodeResolved(resolved, deck.energies);
  } catch (err: unknown) {
    errorMsg = err instanceof Error ? err.message : "Error al codificar el mazo.";
  }

  const handleCopy = () => {
    if (!base64Code) return;
    navigator.clipboard.writeText(base64Code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownload = () => {
    if (!svgRef.current) return;
    const svgData = new XMLSerializer().serializeToString(svgRef.current);
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");
    const img = new Image();
    const size = 1024;
    canvas.width = size;
    canvas.height = size;

    img.onload = () => {
      if (!ctx) return;
      ctx.fillStyle = "#FFFFFF";
      ctx.fillRect(0, 0, size, size);
      // Draw with padding
      const pad = 64;
      ctx.drawImage(img, pad, pad, size - pad * 2, size - pad * 2);
      const pngUrl = canvas.toDataURL("image/png");
      const a = document.createElement("a");
      a.href = pngUrl;
      a.download = `tcg-lab-qr-${deck.name.toLowerCase().replace(/[^a-z0-9]+/g, "-")}.png`;
      a.click();
    };

    img.src = "data:image/svg+xml;base64," + btoa(unescape(encodeURIComponent(svgData)));
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/60 backdrop-blur-xs transition-opacity animate-fade-in">
      <div className="absolute inset-0" onClick={onClose} />

      <div className="relative w-full max-w-md bg-[#FFFFFE] dark:bg-[#202D38] rounded-t-3xl sm:rounded-3xl p-6 shadow-2xl border-t sm:border border-[#E3DED2] dark:border-[#34424E] max-h-[92vh] overflow-y-auto animate-slide-up">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-[#E3DED2] dark:border-[#34424E] mb-4">
          <div className="flex items-center gap-2">
            <QrCode className="w-5 h-5 text-[#172536] dark:text-[#FFC857]" />
            <h2 className="text-lg font-bold text-[#172536] dark:text-[#F0EEE7]">
              Código QR del mazo
            </h2>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-full text-[#667586] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {errorMsg ? (
          <div className="p-4 rounded-xl bg-red-50 dark:bg-red-950/30 text-red-600 dark:text-red-400 text-sm">
            {errorMsg}
          </div>
        ) : (
          <>
            {/* Deck title & info */}
            <div className="text-center mb-4">
              <h3 className="font-semibold text-base text-[#172536] dark:text-[#F0EEE7]">
                {deck.name}
              </h3>
              <p className="text-xs text-[#667586] dark:text-[#9DAAB5] mt-0.5">
                20 cartas · Listo para escanear en Pokémon TCG Pocket
              </p>
            </div>

            {/* QR Code Container */}
            <div className="bg-white p-6 rounded-2xl shadow-inner flex flex-col items-center justify-center mx-auto border border-neutral-200 w-full aspect-square max-w-[280px]">
              <QRCodeSVG
                ref={svgRef}
                value={base64Code}
                size={230}
                level="L"
                includeMargin={false}
              />
            </div>

            {/* Instructions specified in PROMPT.md */}
            <div className="mt-4 p-3.5 rounded-2xl bg-[#F9F7F1] dark:bg-[#18232E] border border-[#E3DED2] dark:border-[#34424E] text-xs text-[#667586] dark:text-[#9DAAB5] leading-relaxed">
              <p className="font-semibold text-[#172536] dark:text-[#F0EEE7] mb-1">
                ¿Cómo importar en el juego?
              </p>
              <p>
                En Pokémon TCG Pocket, abre <strong className="text-[#172536] dark:text-[#F0EEE7]">Mazos &gt; Crear &gt; Escanear código</strong>. Si tienes la app en este mismo teléfono, abre este código en otra pantalla o descárgalo para enviarlo.
              </p>
            </div>

            {/* Actions */}
            <div className="grid grid-cols-2 gap-3 mt-4">
              <button
                type="button"
                onClick={handleDownload}
                className="py-3 px-4 rounded-xl bg-[#172536] dark:bg-[#FFC857] text-[#FFFFFE] dark:text-[#172536] font-semibold text-sm flex items-center justify-center gap-2 active:scale-95 transition-all shadow-xs"
              >
                <Download className="w-4 h-4" />
                <span>Descargar</span>
              </button>

              <button
                type="button"
                onClick={handleCopy}
                className="py-3 px-4 rounded-xl border border-[#172536]/20 dark:border-[#FFC857]/30 text-[#172536] dark:text-[#F0EEE7] font-semibold text-sm flex items-center justify-center gap-2 hover:bg-neutral-50 dark:hover:bg-neutral-800 transition-colors active:scale-95"
              >
                {copied ? (
                  <>
                    <Check className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
                    <span>Copiado</span>
                  </>
                ) : (
                  <>
                    <Copy className="w-4 h-4" />
                    <span>Copiar Base64</span>
                  </>
                )}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};
