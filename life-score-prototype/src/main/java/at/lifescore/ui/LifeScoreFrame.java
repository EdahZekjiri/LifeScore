package at.lifescore.ui; // Ordnet das Hauptfenster dem UI-Paket zu.

import at.lifescore.model.DailyEntry; // Macht gespeicherte Tageswerte für die Anzeige verfügbar.
import at.lifescore.repository.EntryRepository; // Macht die lokale Speicherung für die Oberfläche verfügbar.
import at.lifescore.service.ScoreService; // Macht die Berechnungslogik für die Oberfläche verfügbar.
import java.awt.BorderLayout; // Ordnet Bereiche am Rand und in der Mitte eines Containers an.
import java.awt.Color; // Stellt Farben für die Oberfläche bereit.
import java.awt.Component; // Beschreibt eine allgemeine sichtbare UI-Komponente.
import java.awt.Dimension; // Legt bevorzugte Größen von Komponenten fest.
import java.awt.Font; // Stellt Schriftstile und Schriftgrößen bereit.
import java.awt.GridBagConstraints; // Beschreibt Position und Abstände im GridBagLayout.
import java.awt.GridBagLayout; // Ordnet Formularfelder flexibel in einem Raster an.
import java.awt.GridLayout; // Ordnet Bereiche gleichmäßig in Zeilen und Spalten an.
import java.time.LocalDate; // Stellt das ausgewählte Tagesdatum dar.
import java.time.ZoneId; // Wandelt zwischen altem Date-Typ und LocalDate um.
import java.util.Date; // Liefert den vom Swing-Datumsfeld verwendeten Datumstyp.
import java.util.List; // Stellt die geladenen Historieneinträge bereit.
import javax.swing.BorderFactory; // Erzeugt Abstände und Rahmen für Panels.
import javax.swing.JButton; // Stellt die Schaltfläche zum Speichern bereit.
import javax.swing.JFrame; // Stellt das Hauptfenster der Desktop-Anwendung bereit.
import javax.swing.JLabel; // Zeigt statische und dynamische Texte an.
import javax.swing.JOptionPane; // Zeigt Rückmeldungen und Fehlermeldungen an.
import javax.swing.JPanel; // Gruppiert zusammengehörige UI-Komponenten.
import javax.swing.JProgressBar; // Visualisiert Score und Level-Fortschritt.
import javax.swing.JScrollPane; // Macht die Verlaufstabelle bei Bedarf scrollbar.
import javax.swing.JSpinner; // Stellt Zahlen- und Datumseingaben bereit.
import javax.swing.JTabbedPane; // Teilt die Anwendung in übersichtliche Registerkarten.
import javax.swing.JTable; // Zeigt die Score-Historie tabellarisch an.
import javax.swing.SpinnerDateModel; // Liefert das Datenmodell für die Datumsauswahl.
import javax.swing.SpinnerNumberModel; // Begrenzt Bewertungen auf die Skala von null bis zehn.
import javax.swing.SwingConstants; // Stellt Konstanten für die Textausrichtung bereit.
import javax.swing.table.DefaultTableModel; // Verwaltet die Zeilen der Verlaufstabelle.

public final class LifeScoreFrame extends JFrame { // Definiert das zentrale Fenster des Prototyps.
    private static final Color GREEN = new Color(35, 112, 64); // Legt die grüne Akzentfarbe der App fest.
    private final ScoreService scoreService; // Hält die fachliche Berechnungslogik bereit.
    private final EntryRepository repository; // Hält die lokale Speicherung bereit.
    private final JLabel scoreLabel = new JLabel("Noch kein Eintrag", SwingConstants.CENTER); // Zeigt den aktuellen Score an.
    private final JLabel levelLabel = new JLabel("Level 1 · 0 XP", SwingConstants.CENTER); // Zeigt Level und gesamte XP an.
    private final JLabel rewardLabel = new JLabel("Nächste Belohnung ab Level 3", SwingConstants.CENTER); // Zeigt die aktuelle oder nächste Belohnung an.
    private final JProgressBar scoreBar = new JProgressBar(0, 100); // Visualisiert den aktuellen Score von null bis hundert.
    private final JProgressBar levelBar = new JProgressBar(0, 100); // Visualisiert den Fortschritt innerhalb des Levels.
    private final DefaultTableModel historyModel = new DefaultTableModel(new Object[]{"Datum", "Life Score", "XP"}, 0); // Definiert die drei Spalten der Historie.
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel()); // Ermöglicht heutige und rückwirkende Einträge.
    private final JSpinner sleepSpinner = ratingSpinner(); // Erfasst die Schlafbewertung.
    private final JSpinner movementSpinner = ratingSpinner(); // Erfasst die Bewegungsbewertung.
    private final JSpinner nutritionSpinner = ratingSpinner(); // Erfasst die Ernährungsbewertung.
    private final JSpinner productivitySpinner = ratingSpinner(); // Erfasst die Produktivitätsbewertung.
    private final JSpinner socialSpinner = ratingSpinner(); // Erfasst die Bewertung sozialer Aktivität.

    public LifeScoreFrame(ScoreService scoreService, EntryRepository repository) { // Baut das Fenster mit explizit übergebenen Abhängigkeiten auf.
        super("Life Score – Prototyp"); // Setzt den sichtbaren Fenstertitel.
        this.scoreService = scoreService; // Speichert die übergebene Berechnungslogik.
        this.repository = repository; // Speichert die übergebene Datenablage.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Beendet die Anwendung beim Schließen des Fensters.
        setMinimumSize(new Dimension(760, 560)); // Sichert eine gut nutzbare Mindestgröße.
        setSize(900, 650); // Legt eine angenehme Startgröße fest.
        setLocationRelativeTo(null); // Zentriert das Fenster auf dem Bildschirm.
        setContentPane(buildContent()); // Fügt die komplette Oberfläche in das Fenster ein.
        refreshFromStorage(); // Lädt vorhandene Offline-Daten direkt beim Start.
    } // Beendet den Konstruktor des Hauptfensters.

    private JTabbedPane buildContent() { // Erstellt die Registerkarten der Anwendung.
        JTabbedPane tabs = new JTabbedPane(); // Erzeugt den Registerkarten-Container.
        tabs.addTab("Dashboard", buildDashboard()); // Fügt die Übersichtsseite hinzu.
        tabs.addTab("Täglicher Check-in", buildQuestionnaire()); // Fügt den Fragebogen hinzu.
        tabs.addTab("Verlauf", buildHistory()); // Fügt die Score-Historie hinzu.
        tabs.addTab("Info", buildInfo()); // Fügt eine kurze Prototyp-Erklärung hinzu.
        return tabs; // Liefert die fertig aufgebauten Registerkarten zurück.
    } // Beendet den Aufbau der Registerkarten.

    private JPanel buildDashboard() { // Erstellt die Dashboard-Ansicht.
        JPanel panel = new JPanel(new GridLayout(5, 1, 12, 12)); // Ordnet fünf Statusbereiche gleichmäßig untereinander an.
        panel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80)); // Schafft großzügigen Innenabstand.
        JLabel title = new JLabel("Dein heutiger Life Score", SwingConstants.CENTER); // Erstellt die Dashboard-Überschrift.
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f)); // Hebt die Überschrift deutlich hervor.
        title.setForeground(GREEN); // Färbt die Überschrift in der Akzentfarbe.
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 34f)); // Macht den Score zum stärksten visuellen Element.
        scoreBar.setStringPainted(true); // Zeigt den Zahlenwert zusätzlich im Fortschrittsbalken an.
        levelBar.setStringPainted(true); // Zeigt den XP-Fortschritt zusätzlich als Text an.
        panel.add(title); // Fügt die Überschrift ein.
        panel.add(scoreLabel); // Fügt die Score-Anzeige ein.
        panel.add(scoreBar); // Fügt den Score-Balken ein.
        panel.add(levelLabel); // Fügt die Level-Anzeige ein.
        panel.add(wrapLevelDetails()); // Fügt XP-Balken und Belohnung gemeinsam ein.
        return panel; // Liefert das fertige Dashboard zurück.
    } // Beendet den Aufbau des Dashboards.

    private JPanel wrapLevelDetails() { // Gruppiert Level-Fortschritt und Belohnung.
        JPanel panel = new JPanel(new GridLayout(2, 1, 4, 4)); // Ordnet beide Informationen untereinander an.
        panel.add(levelBar); // Fügt den Fortschritt zum nächsten Level ein.
        panel.add(rewardLabel); // Fügt die Belohnungsinformation ein.
        return panel; // Liefert die Gruppierung zurück.
    } // Beendet die Level-Gruppierung.

    private JPanel buildQuestionnaire() { // Erstellt den täglichen Fragebogen.
        JPanel panel = new JPanel(new GridBagLayout()); // Nutzt ein flexibles Raster für Beschriftungen und Eingaben.
        panel.setBorder(BorderFactory.createEmptyBorder(25, 60, 25, 60)); // Schafft Abstand zum Fensterrand.
        GridBagConstraints constraints = new GridBagConstraints(); // Erstellt die wiederverwendeten Rastereinstellungen.
        constraints.fill = GridBagConstraints.HORIZONTAL; // Lässt Komponenten die verfügbare Breite nutzen.
        constraints.weightx = 1.0; // Verteilt zusätzlichen horizontalen Platz.
        constraints.insets.set(7, 7, 7, 7); // Legt gleichmäßige Abstände zwischen Formularzeilen fest.
        addRow(panel, constraints, 0, "Datum", dateSpinner); // Fügt die Datumsauswahl für heutige oder rückwirkende Einträge hinzu.
        addRow(panel, constraints, 1, "Schlafqualität (0–10)", sleepSpinner); // Fügt die Schlafbewertung hinzu.
        addRow(panel, constraints, 2, "Bewegung (0–10)", movementSpinner); // Fügt die Bewegungsbewertung hinzu.
        addRow(panel, constraints, 3, "Ernährung (0–10)", nutritionSpinner); // Fügt die Ernährungsbewertung hinzu.
        addRow(panel, constraints, 4, "Produktivität (0–10)", productivitySpinner); // Fügt die Produktivitätsbewertung hinzu.
        addRow(panel, constraints, 5, "Soziale Aktivität (0–10)", socialSpinner); // Fügt die Sozialbewertung hinzu.
        JButton saveButton = new JButton("Score berechnen und lokal speichern"); // Erstellt die zentrale Abschlussaktion.
        saveButton.setBackground(GREEN); // Verwendet die Akzentfarbe für die Schaltfläche.
        saveButton.setForeground(Color.WHITE); // Sorgt für gut lesbaren Text auf grünem Hintergrund.
        saveButton.addActionListener(event -> saveQuestionnaire()); // Verknüpft den Klick mit Berechnung und Speicherung.
        constraints.gridx = 0; // Positioniert die Schaltfläche in der ersten Spalte.
        constraints.gridy = 6; // Positioniert die Schaltfläche unter allen Eingaben.
        constraints.gridwidth = 2; // Lässt die Schaltfläche beide Formularspalten überspannen.
        constraints.insets.set(22, 7, 7, 7); // Vergrößert den Abstand oberhalb der Abschlussaktion.
        panel.add(saveButton, constraints); // Fügt die Schaltfläche in das Formular ein.
        return panel; // Liefert den fertigen Fragebogen zurück.
    } // Beendet den Aufbau des Fragebogens.

    private JPanel buildHistory() { // Erstellt die Ansicht der vergangenen Scores.
        JTable table = new JTable(historyModel); // Verknüpft eine Tabelle mit dem Historienmodell.
        table.setFillsViewportHeight(true); // Nutzt die gesamte verfügbare Höhe der Ansicht.
        table.setEnabled(false); // Verhindert versehentliche Änderungen direkt in der Tabelle.
        JPanel panel = new JPanel(new BorderLayout()); // Lässt die Tabelle den gesamten verfügbaren Bereich ausfüllen.
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Schafft Abstand zum Rand der Registerkarte.
        panel.add(new JScrollPane(table), BorderLayout.CENTER); // Fügt die Tabelle mit automatischer Scrollfunktion ein.
        return panel; // Liefert die fertige Verlaufsansicht zurück.
    } // Beendet den Aufbau der Historie.

    private JPanel buildInfo() { // Erstellt die Informationsseite des Prototyps.
        JPanel panel = new JPanel(new BorderLayout()); // Zentriert den Informationstext im verfügbaren Bereich.
        JLabel info = new JLabel("<html><div style='text-align:center'><h2>Life Score</h2><p>Lokaler Java-Prototyp nach den Anforderungen aus dem Konzept.</p><p>Fragebogen · Score 0–100 · Verlauf · XP · Level · Offline</p></div></html>", SwingConstants.CENTER); // Beschreibt den aktuell umgesetzten Funktionsumfang.
        panel.add(info, BorderLayout.CENTER); // Fügt den Informationstext in der Mitte ein.
        return panel; // Liefert die fertige Informationsseite zurück.
    } // Beendet den Aufbau der Informationsseite.

    private void addRow(JPanel panel, GridBagConstraints constraints, int row, String labelText, Component input) { // Fügt eine beschriftete Eingabezeile in das Formular ein.
        constraints.gridwidth = 1; // Stellt sicher, dass jede Komponente genau eine Spalte nutzt.
        constraints.gridx = 0; // Positioniert die Beschriftung in der linken Spalte.
        constraints.gridy = row; // Positioniert die Zeile an der übergebenen Rasterposition.
        panel.add(new JLabel(labelText), constraints); // Fügt die verständliche Feldbeschriftung ein.
        constraints.gridx = 1; // Positioniert die Eingabe in der rechten Spalte.
        panel.add(input, constraints); // Fügt die zugehörige Eingabekomponente ein.
    } // Beendet das Hinzufügen einer Formularzeile.

    private void saveQuestionnaire() { // Verarbeitet den vollständig ausgefüllten täglichen Check-in.
        try { // Startet Berechnung und Speicherung mit gemeinsamer Fehlerbehandlung.
            int score = scoreService.calculateScore(valueOf(sleepSpinner), valueOf(movementSpinner), valueOf(nutritionSpinner), valueOf(productivitySpinner), valueOf(socialSpinner)); // Berechnet den gewichteten Life Score aus allen fünf Angaben.
            LocalDate date = ((Date) dateSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); // Wandelt das Swing-Datum in ein modernes LocalDate um.
            DailyEntry entry = new DailyEntry(date, score, scoreService.earnedXpFor(score)); // Erstellt den zu speichernden Tagesdatensatz.
            repository.saveOrReplace(entry); // Speichert oder aktualisiert den Eintrag lokal.
            refreshFromStorage(); // Aktualisiert Dashboard und Verlauf aus dem gespeicherten Stand.
            JOptionPane.showMessageDialog(this, "Gespeichert: Dein Life Score ist " + score + " von 100.", "Check-in abgeschlossen", JOptionPane.INFORMATION_MESSAGE); // Bestätigt den erfolgreichen Check-in.
        } catch (RuntimeException exception) { // Fängt Validierungs- und Speicherfehler der Anwendung ab.
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Speichern nicht möglich", JOptionPane.ERROR_MESSAGE); // Zeigt den Fehler verständlich im Fenster an.
        } // Beendet die Fehlerbehandlung des Check-ins.
    } // Beendet die Verarbeitung des Fragebogens.

    private void refreshFromStorage() { // Lädt den aktuellen Datenstand in alle Anzeigen.
        List<DailyEntry> entries = repository.loadAll(); // Liest die gesamte lokale Historie.
        historyModel.setRowCount(0); // Entfernt veraltete Zeilen aus der Tabelle.
        for (DailyEntry entry : entries) { // Durchläuft alle chronologisch sortierten Einträge.
            historyModel.addRow(new Object[]{entry.date(), entry.score(), entry.earnedXp()}); // Fügt den Tagesdatensatz als neue Tabellenzeile hinzu.
        } // Beendet die Aktualisierung der Tabellenzeilen.
        int totalXp = scoreService.totalXp(entries); // Berechnet die gesamten Erfahrungspunkte.
        int level = scoreService.levelFor(totalXp); // Leitet daraus das aktuelle Level ab.
        levelLabel.setText("Level " + level + " · " + totalXp + " XP"); // Aktualisiert die Level- und XP-Anzeige.
        levelBar.setValue(totalXp % 100); // Zeigt den Fortschritt innerhalb des aktuellen Levels.
        levelBar.setString((totalXp % 100) + " / 100 XP bis zum nächsten Level"); // Erklärt den Level-Balken mit konkreten Werten.
        rewardLabel.setText(scoreService.rewardFor(level)); // Aktualisiert die freigeschaltete oder nächste Belohnung.
        if (entries.isEmpty()) { // Prüft, ob noch kein Check-in gespeichert wurde.
            scoreLabel.setText("Noch kein Eintrag"); // Zeigt einen verständlichen Anfangszustand an.
            scoreBar.setValue(0); // Setzt den Score-Balken auf null.
            scoreBar.setString("0 / 100"); // Beschriftet den leeren Score-Balken.
            return; // Beendet die Aktualisierung ohne Zugriff auf einen fehlenden Eintrag.
        } // Beendet die Prüfung auf eine leere Historie.
        DailyEntry latest = entries.get(entries.size() - 1); // Wählt den zeitlich neuesten gespeicherten Eintrag aus.
        scoreLabel.setText(latest.score() + " / 100"); // Zeigt den aktuellen Life Score als Text an.
        scoreBar.setValue(latest.score()); // Überträgt den aktuellen Life Score in den Balken.
        scoreBar.setString(latest.date() + " · " + latest.score() + " / 100"); // Ergänzt den Balken um Datum und Wert.
    } // Beendet die Aktualisierung aller Ansichten.

    private static JSpinner ratingSpinner() { // Erstellt ein einheitliches Bewertungsfeld.
        return new JSpinner(new SpinnerNumberModel(5, 0, 10, 1)); // Startet bei fünf und erlaubt ganze Werte von null bis zehn.
    } // Beendet die Erzeugung eines Bewertungsfeldes.

    private static int valueOf(JSpinner spinner) { // Liest eine Bewertung typsicher aus einem Zahlenfeld.
        return ((Number) spinner.getValue()).intValue(); // Wandelt den allgemeinen Zahlenwert in eine Ganzzahl um.
    } // Beendet das Auslesen einer Bewertung.
} // Beendet die Klasse LifeScoreFrame.

