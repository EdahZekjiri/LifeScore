# Life Score – Java-Prototyp

Ein bewusst einfacher Desktop-Prototyp auf Basis der Anforderungen aus `Life Score-2.pdf`. Die Anwendung läuft lokal und benötigt für die Kernfunktionen weder Konto noch Internet.

## Bereits umgesetzt

## Weitere Änderungen via Claude gemacht

- täglicher Fragebogen für Schlaf, Bewegung, Ernährung, Produktivität und soziale Aktivität
- gewichtete Score-Berechnung von 0 bis 100
- rückwirkende Einträge über eine Datumsauswahl
- moderne Oberfläche mit Seitenmenü, Karten und animiertem Score-Ring
- Check-in mit Reglern, Live-Vorschau des Scores und Anzeige der Gewichtung je Bereich
- Übersicht mit Score, Level, Serie (Tage in Folge) und Diagramm der letzten 7 Check-ins
- lokale CSV-Speicherung und farbig gestaltete Verlaufstabelle
- einfache Level- und Belohnungslogik

## Projekt in IntelliJ IDEA starten

1. Den Ordner `life-score-prototype` in IntelliJ IDEA öffnen.
2. Als Project SDK Java 17 oder neuer auswählen.
3. `src/main/java/at/lifescore/Main.java` öffnen.
4. Die grüne Start-Schaltfläche neben `main` verwenden.

Alternativ kann das Projekt als Maven-Projekt importiert werden. Es verwendet ausschließlich die Java-Standardbibliothek und lädt keine externen Abhängigkeiten.

## Struktur

```text
life-score-prototype/
├── data/                         Lokale Laufzeitdaten
├── docs/                         Architektur und nächste Schritte
├── src/main/java/at/lifescore/
│   ├── Main.java                 Einstiegspunkt
│   ├── model/DailyEntry.java     Datenmodell
│   ├── repository/EntryRepository.java
│   ├── service/ScoreService.java
│   └── ui/                       Swing-Oberfläche
│       ├── LifeScoreFrame.java   Fenster, Seitenmenü und die vier Seiten
│       ├── Theme.java            Farben, Schrift, Zeichenhilfen
│       ├── Card.java             Karte mit runden Ecken
│       ├── ScoreRing.java        animierter Score-Ring
│       ├── TrendChart.java       Balkendiagramm der letzten Check-ins
│       ├── ProgressPill.java     Fortschrittsbalken
│       ├── ModernSliderUI.java   Regler-Design
│       ├── PillButton.java       abgerundete Schaltfläche
│       └── NavButton.java        Menüpunkt der Seitenleiste
├── src/test/java/                Kleiner Test ohne externe Bibliothek
├── .gitignore
├── pom.xml
└── README.md
```

## Hinweis zu den Kommentaren

Wie gewünscht ist jede nicht-leere Java-Codezeile direkt kommentiert. Die Kommentare erklären jeweils Zweck und Wirkung der Zeile. In einem produktiven Projekt wären Kommentare üblicherweise sparsamer und würden vor allem Entscheidungen statt Syntax beschreiben.

## Aktuelle Grenzen

Der Prototyp enthält noch kein Benutzerkonto, keine Cloud-Synchronisierung, keinen Adminbereich und keine grafische Avatar-Anpassung. Diese Punkte sind in `docs/NEXT_STEPS.md` als Ausbaupfad festgehalten.

