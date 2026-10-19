from pathlib import Path

p = Path(__file__).resolve().parents[1] / "app/src/main/java/com/tcglab/app/ui/hub/HubScreen.kt"
t = p.read_text(encoding="utf-8")
repls = [
    ("Album, mazo y asistente en un solo lugar", "Álbum, mazo y asistente en un solo lugar"),
    ('text = "Album"', 'text = "Álbum"'),
    ("Arma el mazo aqui y pide ayuda", "Arma el mazo aquí y pide ayuda"),
    ('text = "Energias"', 'text = "Energías"'),
    ('text = "Codigo QR"', 'text = "Código QR"'),
    ("Sumar desde la coleccion", "Sumar desde la colección"),
    ('"Psiquica"', '"Psíquica"'),
    ('"Metalica"', '"Metálica"'),
    ("este telefono", "este teléfono"),
    ("dejalo listo", "déjalo listo"),
    ("Que ex de rayo tengo?", "¿Qué ex de rayo tengo?"),
    ('"Tu"', '"Tú"'),
    ("Pide un mazo...", "Pide un mazo…"),
    ('"..."', '"…"'),
    ("Escanear codigo.", "Escanear código."),
    ("contentDescription = \"Codigo QR\"", 'contentDescription = "Código QR"'),
    ("Combinar o reemplazar la coleccion?", "¿Combinar o reemplazar la colección?"),
]
for a, b in repls:
    t = t.replace(a, b)
p.write_text(t, encoding="utf-8")
print("accents ok")
