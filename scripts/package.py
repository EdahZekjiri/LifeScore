#!/usr/bin/env python3
"""Packt Quellcode, Dokumentation und die gebaute JAR ohne persönliche Laufzeitdaten."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

# Alle Pfade werden am Skript ausgerichtet, nicht am zufälligen Arbeitsverzeichnis.
root = Path(__file__).resolve().parent.parent
jar = root / "LifeScore.jar"
if not jar.is_file():
    raise SystemExit("Bitte zuerst build.sh oder build-windows.bat ausführen.")

# Eine bewusste Positivliste hält IDE-Caches, persönliche Daten und ältere ZIPs draußen.
files = [root / name for name in (
    "LifeScore.jar", "README.md", "START-HIER.md", "pom.xml", ".gitignore",
    "Start-macOS.command", "Start-Windows.bat", "start-linux.sh",
    "build.sh", "build-windows.bat", "test.sh", "data/.gitkeep",
)]
for directory in ("src", "docs", "scripts"):
    files.extend(path for path in (root / directory).rglob("*")
                 if path.is_file() and not path.is_symlink()
                 and "__pycache__" not in path.parts and path.name != ".DS_Store")

output = root / "delivery" / "Life-Score-Projekt.zip"
output.parent.mkdir(exist_ok=True)
# write() übernimmt auch die ausführbaren Dateirechte der macOS-/Linux-Startskripte.
with ZipFile(output, "w", ZIP_DEFLATED) as archive:
    for path in sorted(files):
        archive.write(path, Path("Life-Score") / path.relative_to(root))

# Prüft die CRC aller Einträge und stellt sicher, dass die wichtigen Bestandteile enthalten sind.
with ZipFile(output) as archive:
    if archive.testzip() is not None:
        raise SystemExit("ZIP-Prüfung fehlgeschlagen.")
    for required in ("Life-Score/LifeScore.jar", "Life-Score/START-HIER.md",
                     "Life-Score/src/main/java/at/lifescore/ui/AvatarStudio.java"):
        if required not in archive.namelist():
            raise SystemExit(f"Im Paket fehlt: {required}")
    print(f"Geprüft: {len(archive.namelist())} Dateien, {output.stat().st_size:,} Bytes")
print(output)
