import re
from pathlib import Path

root = Path(__file__).resolve().parents[1]
catalog = root / "src" / "data" / "catalog.ts"
text = catalog.read_text(encoding="utf-8")
replacements = {
    "B1a": "Llama Carmesí",
    "B2": "Desfile Fantástico",
    "B2a": "Maravillas de Paldea",
    "B2b": "Mega Brillo",
    "B3": "Aura Pulsante",
    "B3a": "Impulso Paradójico",
    "B3b": "Maravillas Cotidianas",
    "B4": "Soberano de los Cielos",
    "B4a": "Ambición del Team Rocket",
    "B4b": "Sobre Deluxe Mega",
}
for code, es in replacements.items():
    text = re.sub(
        rf'("code"\s*:\s*"{re.escape(code)}"\s*,\s*"es"\s*:\s*")[^"]*(")',
        rf"\1{es}\2",
        text,
        count=1,
    )
catalog.write_text(text, encoding="utf-8")
print("catalog.ts actualizado")
