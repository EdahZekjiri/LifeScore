# Architektur

Der Prototyp trennt Oberfläche, Berechnung, Datenmodell und Speicherung:

- `ui`: zeigt Dashboard, Fragebogen und Verlauf an.
- `service`: enthält die fachliche Score-, XP- und Level-Logik.
- `repository`: speichert Einträge lokal in `data/life-score-history.csv`.
- `model`: beschreibt einen Tagesdatensatz.

Der Datenfluss ist bewusst klein: Die Oberfläche liest Eingaben, der Service berechnet den Score, das Repository speichert den Eintrag und die Oberfläche aktualisiert Dashboard und Verlauf.

