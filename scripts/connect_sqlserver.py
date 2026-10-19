"""Redirige al conector global en ~/.cursor (válido en cualquier chat/proyecto)."""

from __future__ import annotations

import runpy
from pathlib import Path

GLOBAL_SCRIPT = Path.home() / ".cursor" / "tools" / "sqlserver" / "connect_sqlserver.py"

if not GLOBAL_SCRIPT.is_file():
    raise SystemExit(
        f"No existe el conector global: {GLOBAL_SCRIPT}\n"
        "Configura ~/.cursor/tools/sqlserver/connect_sqlserver.py"
    )

runpy.run_path(str(GLOBAL_SCRIPT), run_name="__main__")
