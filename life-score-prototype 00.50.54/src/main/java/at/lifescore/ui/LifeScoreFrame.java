package at.lifescore.ui; // Ordnet das Hauptfenster dem UI-Paket zu.

import at.lifescore.model.DailyEntry; // Macht gespeicherte Tageswerte für die Anzeige verfügbar.
import at.lifescore.repository.EntryRepository; // Macht die lokale Speicherung für die Oberfläche verfügbar.
import at.lifescore.service.ScoreService; // Macht die Berechnungslogik für die Oberfläche verfügbar.
import java.awt.BorderLayout; // Ordnet Bereiche am Rand und in der Mitte eines Containers an.
import java.awt.CardLayout; // Blendet jeweils genau eine Seite ein.
import java.awt.Color; // Stellt Farben bereit.
import java.awt.Component; // Beschreibt eine allgemeine sichtbare UI-Komponente.
import java.awt.Dimension; // Legt Größen fest.
import java.awt.FlowLayout; // Ordnet Komponenten nebeneinander an.
import java.awt.Font; // Stellt Schriftstile bereit.
import java.awt.GridBagConstraints; // Beschreibt Position und Abstände im GridBagLayout.
import java.awt.GridBagLayout; // Ordnet Bereiche flexibel in einem Raster an.
import java.awt.GridLayout; // Ordnet Bereiche gleichmäßig an.
import java.awt.Graphics; // Stellt die Zeichenfläche bereit.
import java.awt.Graphics2D; // Ermöglicht erweiterte Zeichenfunktionen.
import java.awt.Insets; // Legt Abstände im Raster fest.
import java.time.LocalDate; // Stellt das ausgewählte Tagesdatum dar.
import java.time.LocalTime; // Stellt die Uhrzeit der Speicherbestätigung dar.
import java.time.format.DateTimeFormatter; // Formatiert Datum und Uhrzeit.
import java.util.LinkedHashMap; // Behält die Reihenfolge der Menüpunkte bei.
import java.util.List; // Stellt die geladenen Historieneinträge bereit.
import java.util.Locale; // Sorgt für deutsche Datumsnamen.
import java.util.Map; // Verknüpft Seitennamen mit Menüpunkten.
import javax.swing.Box; // Erzeugt feste Abstände.
import javax.swing.BoxLayout; // Ordnet Komponenten untereinander an.
import javax.swing.BorderFactory; // Erzeugt Abstände und Rahmen.
import javax.swing.JFrame; // Stellt das Hauptfenster der Desktop-Anwendung bereit.
import javax.swing.JComponent; // Beschreibt allgemeine Swing-Komponenten.
import javax.swing.JLabel; // Zeigt Texte an.
import javax.swing.JOptionPane; // Zeigt Fehlermeldungen an.
import javax.swing.JPanel; // Gruppiert zusammengehörige UI-Komponenten.
import javax.swing.JScrollPane; // Macht die Verlaufstabelle scrollbar.
import javax.swing.JSlider; // Stellt die Bewertungsregler bereit.
import javax.swing.JTable; // Zeigt die Score-Historie tabellarisch an.
import javax.swing.SwingConstants; // Stellt Ausrichtungskonstanten bereit.
import javax.swing.table.DefaultTableCellRenderer; // Dient als Basis für gestaltete Tabellenzellen.
import javax.swing.table.DefaultTableModel; // Verwaltet die Zeilen der Verlaufstabelle.

public final class LifeScoreFrame extends JFrame { // Definiert das zentrale Fenster der Anwendung.
    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", Locale.GERMAN); // Legt das ausführliche Datumsformat fest.
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN); // Legt das kurze Datumsformat fest.
    private static final DateTimeFormatter TABLE_DATE = DateTimeFormatter.ofPattern("EE, dd.MM.yyyy", Locale.GERMAN); // Legt das Datumsformat der Tabelle fest.
    private static final String[][] QUESTIONS = { // Legt Titel und Fragetext der fünf Bereiche fest.
        {"Schlaf", "Wie erholt bist du aufgewacht?"}, // Beschreibt den Bereich Schlaf.
        {"Bewegung", "Wie aktiv warst du heute?"}, // Beschreibt den Bereich Bewegung.
        {"Ernährung", "Wie ausgewogen hast du gegessen und getrunken?"}, // Beschreibt den Bereich Ernährung.
        {"Produktivität", "Wie gut hast du deine Aufgaben geschafft?"}, // Beschreibt den Bereich Produktivität.
        {"Soziale Aktivität", "Wie viel Zeit hattest du mit anderen Menschen?"} // Beschreibt den Bereich Soziales.
    }; // Beendet die Fragenliste.

    private final ScoreService scoreService; // Hält die fachliche Berechnungslogik bereit.
    private final EntryRepository repository; // Hält die lokale Speicherung bereit.
    private final CardLayout pages = new CardLayout(); // Steuert, welche Seite sichtbar ist.
    private final JPanel pageHost = new JPanel(pages); // Enthält alle Seiten der Anwendung.
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>(); // Verknüpft Seitennamen mit Menüpunkten.
    private List<DailyEntry> entries = List.of(); // Hält den zuletzt geladenen Datenstand.
    private LocalDate selectedDate = LocalDate.now(); // Hält den Tag, für den gerade eingecheckt wird.

    private final JLabel dashSubtitle = text("", 15f, Font.PLAIN, Theme.MUTED); // Zeigt den Status des heutigen Check-ins.
    private final PillButton dashCta = new PillButton("Jetzt einchecken", true); // Führt direkt zum Check-in.
    private final ScoreRing dashRing = new ScoreRing(200); // Zeigt den aktuellen Score als Ring.
    private final JLabel ringTitle = text("", 22f, Font.BOLD, Theme.TEXT); // Zeigt die Einordnung des Scores.
    private final JLabel ringDate = text("", 14f, Font.PLAIN, Theme.MUTED); // Zeigt das Datum des letzten Eintrags.
    private final JLabel levelTitle = text("Level 1", 32f, Font.BOLD, Theme.TEXT); // Zeigt das aktuelle Level.
    private final ProgressPill levelBar = new ProgressPill(); // Zeigt den Fortschritt zum nächsten Level.
    private final JLabel xpText = text("", 14f, Font.PLAIN, Theme.MUTED); // Erklärt den Level-Fortschritt in Zahlen.
    private final JLabel rewardText = text("", 14f, Font.BOLD, Theme.ACCENT_DARK); // Zeigt die aktuelle oder nächste Belohnung.
    private final JLabel streakValue = text("0 Tage", 32f, Font.BOLD, Theme.TEXT); // Zeigt die aktuelle Serie.
    private final JLabel streakText = text("", 14f, Font.PLAIN, Theme.MUTED); // Erklärt die Serie.
    private final TrendChart trendChart = new TrendChart(); // Zeigt die letzten Scores.

    private final JLabel dateLabel = text("", 16f, Font.BOLD, Theme.TEXT); // Zeigt den gewählten Tag des Check-ins.
    private final PillButton prevDay = new PillButton("‹", false); // Wechselt zum Vortag.
    private final PillButton nextDay = new PillButton("›", false); // Wechselt zum Folgetag.
    private final JSlider[] sliders = new JSlider[5]; // Hält die fünf Bewertungsregler.
    private final JLabel[] valueLabels = new JLabel[5]; // Hält die fünf Zahlenanzeigen der Regler.
    private final ScoreRing previewRing = new ScoreRing(180); // Zeigt den Score live während der Eingabe.
    private final JLabel previewTitle = text(" ", 18f, Font.BOLD, Theme.TEXT); // Zeigt die Live-Einordnung.
    private final JLabel previewXp = text(" ", 14f, Font.BOLD, Theme.ACCENT_DARK); // Zeigt die zu erwartenden XP.
    private final JLabel previewNotice = text(" ", 13f, Font.PLAIN, Theme.MUTED); // Erklärt, ob ein Eintrag ersetzt wird.
    private final JLabel previewHint = text(html("Verschiebe die Regler – dein Score aktualisiert sich live."), 13f, Font.PLAIN, Theme.MUTED); // Erklärt die Live-Vorschau.
    private final PillButton saveButton = new PillButton("Check-in speichern", true); // Speichert den Check-in.

    private final DefaultTableModel historyModel = new DefaultTableModel(new Object[]{"Datum", "Life Score", "XP"}, 0) { // Definiert die Spalten der Historie.
        @Override // Ersetzt die Standardregel für Bearbeitbarkeit.
        public boolean isCellEditable(int row, int column) { // Prüft, ob eine Zelle bearbeitet werden darf.
            return false; // Verbietet jede Bearbeitung in der Tabelle.
        } // Beendet die Bearbeitbarkeitsprüfung.
    }; // Beendet das Tabellenmodell.
    private final CardLayout historyCards = new CardLayout(); // Wechselt zwischen Tabelle und Leerzustand.
    private final JPanel historyHost = Theme.plain(historyCards); // Enthält Tabelle und Leerzustand.

    public LifeScoreFrame(ScoreService scoreService, EntryRepository repository) { // Baut das Fenster mit explizit übergebenen Abhängigkeiten auf.
        super("Life Score"); // Setzt den sichtbaren Fenstertitel.
        this.scoreService = scoreService; // Speichert die übergebene Berechnungslogik.
        this.repository = repository; // Speichert die übergebene Datenablage.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Beendet die Anwendung beim Schließen des Fensters.
        setMinimumSize(new Dimension(960, 680)); // Sichert eine gut nutzbare Mindestgröße.
        setSize(1060, 740); // Legt eine angenehme Startgröße fest.
        setLocationRelativeTo(null); // Zentriert das Fenster auf dem Bildschirm.
        setContentPane(buildContent()); // Fügt die komplette Oberfläche in das Fenster ein.
        refreshFromStorage(); // Lädt vorhandene Offline-Daten direkt beim Start.
        show("dashboard"); // Startet auf der Übersicht.
    } // Beendet den Konstruktor des Hauptfensters.

    private JPanel buildContent() { // Setzt Seitenleiste und Seiten zusammen.
        JPanel root = new JPanel(new BorderLayout()); // Erzeugt den Wurzelcontainer.
        root.setBackground(Theme.BG); // Setzt den hellen Hintergrund.
        pageHost.setOpaque(false); // Lässt den Hintergrund durchscheinen.
        pageHost.add(buildDashboard(), "dashboard"); // Fügt die Übersicht hinzu.
        pageHost.add(buildCheckin(), "checkin"); // Fügt den Check-in hinzu.
        pageHost.add(buildHistory(), "history"); // Fügt den Verlauf hinzu.
        pageHost.add(buildInfo(), "info"); // Fügt die Infoseite hinzu.
        root.add(buildSidebar(), BorderLayout.WEST); // Fügt das Menü links ein.
        root.add(pageHost, BorderLayout.CENTER); // Fügt die Seiten in der Mitte ein.
        return root; // Liefert die fertige Oberfläche zurück.
    } // Beendet den Aufbau der Oberfläche.

    private JPanel buildSidebar() { // Erstellt das dunkle Seitenmenü.
        JPanel sidebar = new JPanel(); // Erzeugt das Menü-Panel.
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS)); // Ordnet die Einträge untereinander an.
        sidebar.setBackground(Theme.SIDEBAR); // Setzt den dunklen Hintergrund.
        sidebar.setBorder(BorderFactory.createEmptyBorder(28, 16, 24, 16)); // Schafft Innenabstand.
        sidebar.setPreferredSize(new Dimension(236, 100)); // Legt die feste Breite des Menüs fest.
        JLabel logo = new JLabel("<html><span style='color:" + Theme.hex(Theme.ACCENT) + "'>●</span>&nbsp; Life Score</html>"); // Erzeugt das Logo mit grünem Punkt.
        logo.setFont(Theme.font(Font.BOLD, 22f)); // Wählt die Logo-Schrift.
        logo.setForeground(Color.WHITE); // Setzt weiße Schrift.
        logo.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0)); // Rückt das Logo an die Menüpunkte heran.
        sidebar.add(logo); // Fügt das Logo ein.
        sidebar.add(Box.createVerticalStrut(32)); // Schafft Abstand unter dem Logo.
        addNav(sidebar, "dashboard", "Übersicht"); // Fügt den Menüpunkt Übersicht hinzu.
        addNav(sidebar, "checkin", "Täglicher Check-in"); // Fügt den Menüpunkt Check-in hinzu.
        addNav(sidebar, "history", "Verlauf"); // Fügt den Menüpunkt Verlauf hinzu.
        addNav(sidebar, "info", "So funktioniert's"); // Fügt den Menüpunkt Info hinzu.
        sidebar.add(Box.createVerticalGlue()); // Schiebt den Fußtext nach unten.
        JLabel footer = new JLabel("<html>Läuft komplett offline.<br>Alle Daten bleiben<br>auf diesem Gerät.</html>"); // Erklärt den Datenschutz kurz.
        footer.setFont(Theme.font(Font.PLAIN, 12f)); // Wählt die kleine Schrift.
        footer.setForeground(new Color(130, 144, 164)); // Wählt eine gedämpfte Farbe.
        footer.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0)); // Rückt den Fußtext an die Menüpunkte heran.
        sidebar.add(footer); // Fügt den Fußtext ein.
        return sidebar; // Liefert das fertige Menü zurück.
    } // Beendet den Aufbau des Menüs.

    private void addNav(JPanel sidebar, String key, String title) { // Fügt einen Menüpunkt hinzu.
        NavButton button = new NavButton(title); // Erzeugt den Menüpunkt.
        button.addActionListener(event -> show(key)); // Wechselt beim Klick die Seite.
        navButtons.put(key, button); // Merkt sich den Menüpunkt.
        sidebar.add(button); // Fügt ihn dem Menü hinzu.
        sidebar.add(Box.createVerticalStrut(4)); // Schafft Abstand zum nächsten Punkt.
    } // Beendet das Hinzufügen eines Menüpunkts.

    private void show(String key) { // Zeigt eine Seite und markiert den passenden Menüpunkt.
        pages.show(pageHost, key); // Blendet die gewünschte Seite ein.
        for (Map.Entry<String, NavButton> item : navButtons.entrySet()) { // Durchläuft alle Menüpunkte.
            item.getValue().setActive(item.getKey().equals(key)); // Markiert genau den aktuellen Punkt.
        } // Beendet die Markierung.
        if (key.equals("dashboard")) { // Prüft, ob die Übersicht geöffnet wurde.
            dashRing.replay(); // Spielt die Ring-Animation erneut ab.
        } // Beendet die Prüfung auf die Übersicht.
    } // Beendet das Wechseln der Seite.

    private JPanel buildDashboard() { // Erstellt die Übersicht.
        JPanel page = pageShell(); // Erzeugt den Seitenrahmen.
        dashCta.addActionListener(event -> show("checkin")); // Führt den Button zum Check-in.
        page.add(header("Übersicht", dashSubtitle, dashCta), BorderLayout.NORTH); // Fügt die Kopfzeile ein.
        JPanel body = Theme.plain(new GridBagLayout()); // Erzeugt das Raster für die Karten.
        GridBagConstraints c = new GridBagConstraints(); // Erzeugt die Rastereinstellungen.
        c.fill = GridBagConstraints.BOTH; // Lässt Karten ihren Bereich ausfüllen.
        c.gridx = 0; // Platziert die Score-Karte links.
        c.gridy = 0; // Platziert sie in der oberen Reihe.
        c.weightx = 0.42; // Gibt ihr rund 42 Prozent der Breite.
        c.weighty = 1.0; // Lässt die obere Reihe wachsen.
        c.insets = new Insets(0, 0, 18, 18); // Legt Abstände nach unten und rechts fest.
        body.add(buildScoreCard(), c); // Fügt die Score-Karte ein.
        c.gridx = 1; // Platziert die Spalte rechts daneben.
        c.weightx = 0.58; // Gibt ihr rund 58 Prozent der Breite.
        c.insets = new Insets(0, 0, 18, 0); // Legt Abstand nach unten fest.
        JPanel column = Theme.plain(new GridLayout(2, 1, 0, 18)); // Stapelt Level- und Serienkarte.
        column.add(buildLevelCard()); // Fügt die Levelkarte ein.
        column.add(buildStreakCard()); // Fügt die Serienkarte ein.
        body.add(column, c); // Fügt die Spalte ein.
        c.gridx = 0; // Platziert die Diagrammkarte links.
        c.gridy = 1; // Platziert sie in der unteren Reihe.
        c.gridwidth = 2; // Lässt sie beide Spalten überspannen.
        c.weighty = 0; // Verhindert Wachstum der unteren Reihe.
        c.insets = new Insets(0, 0, 0, 0); // Entfernt Abstände.
        body.add(buildTrendCard(), c); // Fügt die Diagrammkarte ein.
        page.add(body, BorderLayout.CENTER); // Fügt das Kartenraster ein.
        return page; // Liefert die fertige Übersicht zurück.
    } // Beendet den Aufbau der Übersicht.

    private Card buildScoreCard() { // Erstellt die große Score-Karte.
        Card card = new Card(new BorderLayout()); // Erzeugt die Karte.
        JPanel box = vbox(); // Erzeugt die vertikale Anordnung.
        box.add(Box.createVerticalGlue()); // Zentriert den Inhalt vertikal.
        box.add(aligned(heading("AKTUELLER LIFE SCORE"), 0.5f)); // Fügt die Überschrift ein.
        box.add(Box.createVerticalStrut(14)); // Schafft Abstand.
        box.add(aligned(dashRing, 0.5f)); // Fügt den Score-Ring ein.
        box.add(Box.createVerticalStrut(14)); // Schafft Abstand.
        box.add(aligned(ringTitle, 0.5f)); // Fügt die Einordnung ein.
        box.add(Box.createVerticalStrut(4)); // Schafft Abstand.
        box.add(aligned(ringDate, 0.5f)); // Fügt das Datum ein.
        box.add(Box.createVerticalGlue()); // Zentriert den Inhalt vertikal.
        card.add(box, BorderLayout.CENTER); // Fügt den Inhalt in die Karte ein.
        return card; // Liefert die Karte zurück.
    } // Beendet den Aufbau der Score-Karte.

    private Card buildLevelCard() { // Erstellt die Karte für Level, XP und Belohnung.
        Card card = new Card(new BorderLayout()); // Erzeugt die Karte.
        JPanel box = vbox(); // Erzeugt die vertikale Anordnung.
        box.add(aligned(heading("LEVEL & BELOHNUNG"), 0f)); // Fügt die Überschrift ein.
        box.add(Box.createVerticalStrut(8)); // Schafft Abstand.
        box.add(aligned(levelTitle, 0f)); // Fügt das Level ein.
        box.add(Box.createVerticalStrut(10)); // Schafft Abstand.
        box.add(aligned(levelBar, 0f)); // Fügt den Fortschrittsbalken ein.
        box.add(Box.createVerticalStrut(8)); // Schafft Abstand.
        box.add(aligned(xpText, 0f)); // Fügt die XP-Erklärung ein.
        box.add(Box.createVerticalGlue()); // Schiebt die Belohnung nach unten.
        box.add(aligned(rewardText, 0f)); // Fügt die Belohnung ein.
        card.add(box, BorderLayout.CENTER); // Fügt den Inhalt in die Karte ein.
        return card; // Liefert die Karte zurück.
    } // Beendet den Aufbau der Levelkarte.

    private Card buildStreakCard() { // Erstellt die Karte für die Serie.
        Card card = new Card(new BorderLayout()); // Erzeugt die Karte.
        JPanel box = vbox(); // Erzeugt die vertikale Anordnung.
        box.add(aligned(heading("SERIE"), 0f)); // Fügt die Überschrift ein.
        box.add(Box.createVerticalStrut(8)); // Schafft Abstand.
        box.add(aligned(streakValue, 0f)); // Fügt die Serienlänge ein.
        box.add(Box.createVerticalStrut(6)); // Schafft Abstand.
        box.add(aligned(streakText, 0f)); // Fügt die Erklärung ein.
        card.add(box, BorderLayout.CENTER); // Fügt den Inhalt in die Karte ein.
        return card; // Liefert die Karte zurück.
    } // Beendet den Aufbau der Serienkarte.

    private Card buildTrendCard() { // Erstellt die Karte mit dem Verlaufsdiagramm.
        Card card = new Card(new BorderLayout(0, 10)); // Erzeugt die Karte.
        card.add(heading("LETZTE 7 CHECK-INS"), BorderLayout.NORTH); // Fügt die Überschrift ein.
        trendChart.setPreferredSize(new Dimension(100, 150)); // Legt die Diagrammhöhe fest.
        card.add(trendChart, BorderLayout.CENTER); // Fügt das Diagramm ein.
        return card; // Liefert die Karte zurück.
    } // Beendet den Aufbau der Diagrammkarte.

    private JPanel buildCheckin() { // Erstellt die Seite für den täglichen Check-in.
        JPanel page = pageShell(); // Erzeugt den Seitenrahmen.
        prevDay.setFont(Theme.font(Font.BOLD, 20f)); // Vergrößert den Pfeil nach links.
        nextDay.setFont(Theme.font(Font.BOLD, 20f)); // Vergrößert den Pfeil nach rechts.
        prevDay.addActionListener(event -> changeDay(-1)); // Wechselt zum Vortag.
        nextDay.addActionListener(event -> changeDay(1)); // Wechselt zum Folgetag.
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER); // Zentriert das Datum.
        dateLabel.setPreferredSize(new Dimension(250, 24)); // Gibt dem Datum eine feste Breite.
        JPanel navigator = Theme.plain(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Erzeugt die Tageswahl.
        navigator.add(prevDay); // Fügt den Zurück-Knopf ein.
        navigator.add(dateLabel); // Fügt das Datum ein.
        navigator.add(nextDay); // Fügt den Vorwärts-Knopf ein.
        JLabel subtitle = text("Bewerte jeden Bereich von 0 (sehr schlecht) bis 10 (ausgezeichnet).", 15f, Font.PLAIN, Theme.MUTED); // Erklärt die Skala.
        page.add(header("Täglicher Check-in", subtitle, navigator), BorderLayout.NORTH); // Fügt die Kopfzeile ein.
        JPanel body = Theme.plain(new GridBagLayout()); // Erzeugt das Raster für Fragen und Vorschau.
        GridBagConstraints c = new GridBagConstraints(); // Erzeugt die Rastereinstellungen.
        c.fill = GridBagConstraints.BOTH; // Lässt Karten ihren Bereich ausfüllen.
        c.weighty = 1.0; // Lässt beide Karten die Höhe nutzen.
        c.gridx = 0; // Platziert die Fragen links.
        c.weightx = 1.0; // Gibt den Fragen den restlichen Platz.
        c.insets = new Insets(0, 0, 0, 18); // Legt Abstand nach rechts fest.
        body.add(buildQuestionsCard(), c); // Fügt die Fragenkarte ein.
        c.gridx = 1; // Platziert die Vorschau rechts.
        c.weightx = 0; // Gibt der Vorschau eine feste Breite.
        c.insets = new Insets(0, 0, 0, 0); // Entfernt Abstände.
        body.add(buildPreviewCard(), c); // Fügt die Vorschaukarte ein.
        page.add(body, BorderLayout.CENTER); // Fügt das Raster ein.
        updatePreview(); // Berechnet die erste Vorschau.
        return page; // Liefert die fertige Seite zurück.
    } // Beendet den Aufbau des Check-ins.

    private Card buildQuestionsCard() { // Erstellt die Karte mit den fünf Fragen.
        Card card = new Card(new BorderLayout()); // Erzeugt die Karte.
        JPanel box = vbox(); // Erzeugt die vertikale Anordnung.
        for (int i = 0; i < QUESTIONS.length; i++) { // Durchläuft alle fünf Bereiche.
            if (i > 0) { // Prüft, ob ein Trennabstand nötig ist.
                box.add(Box.createVerticalGlue()); // Verteilt die Zeilen gleichmäßig.
            } // Beendet die Prüfung auf den Trennabstand.
            box.add(ratingRow(i)); // Fügt die Bewertungszeile ein.
        } // Beendet das Erzeugen der Zeilen.
        card.add(box, BorderLayout.CENTER); // Fügt den Inhalt in die Karte ein.
        return card; // Liefert die Karte zurück.
    } // Beendet den Aufbau der Fragenkarte.

    private JPanel ratingRow(int index) { // Erstellt eine Zeile mit Titel, Frage, Regler und Zahl.
        JSlider slider = new JSlider(0, 10, 5); // Erzeugt den Regler von null bis zehn mit Startwert fünf.
        slider.setUI(new ModernSliderUI(slider)); // Wendet das moderne Reglerdesign an.
        slider.setOpaque(false); // Lässt den Kartenhintergrund durchscheinen.
        slider.setFocusable(true); // Erlaubt Bedienung per Tastatur.
        slider.setPreferredSize(new Dimension(100, 32)); // Legt die Reglerhöhe fest.
        JLabel value = text("5", 22f, Font.BOLD, Theme.scoreColor(50)); // Erzeugt die große Zahlenanzeige.
        value.setHorizontalAlignment(SwingConstants.RIGHT); // Richtet die Zahl rechtsbündig aus.
        value.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12)); // Richtet die Zahl am Ende der Reglerspur aus.
        slider.addChangeListener(event -> { // Reagiert auf jede Änderung des Reglers.
            value.setText(String.valueOf(slider.getValue())); // Aktualisiert die Zahlenanzeige.
            value.setForeground(Theme.scoreColor(slider.getValue() * 10)); // Färbt die Zahl passend zum Wert.
            updatePreview(); // Aktualisiert die Live-Vorschau.
        }); // Beendet die Reaktion auf Änderungen.
        sliders[index] = slider; // Merkt sich den Regler.
        valueLabels[index] = value; // Merkt sich die Zahlenanzeige.
        int percent = (int) Math.round(ScoreService.WEIGHTS[index] * 100); // Berechnet das Gewicht in Prozent.
        JPanel title = Theme.plain(new FlowLayout(FlowLayout.LEFT, 0, 0)); // Erzeugt die Titelzeile ohne Randabstand.
        title.add(text(QUESTIONS[index][0], 17f, Font.BOLD, Theme.TEXT)); // Fügt den Bereichsnamen ein.
        JLabel weight = text("·  " + percent + " % des Scores", 13f, Font.PLAIN, Theme.MUTED); // Zeigt das Gewicht des Bereichs.
        weight.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0)); // Trennt das Gewicht vom Bereichsnamen.
        title.add(weight); // Fügt das Gewicht ein.
        JPanel top = Theme.plain(new BorderLayout()); // Erzeugt die obere Zeile.
        top.add(title, BorderLayout.WEST); // Fügt den Titel links ein.
        top.add(value, BorderLayout.EAST); // Fügt die Zahl rechts ein.
        JPanel row = Theme.plain(new BorderLayout(0, 2)); // Erzeugt die gesamte Zeile.
        row.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0)); // Richtet Titel und Frage am Anfang der Reglerspur aus.
        row.add(top, BorderLayout.NORTH); // Fügt die obere Zeile ein.
        JPanel middle = Theme.plain(new BorderLayout(0, 2)); // Erzeugt den Mittelteil.
        JLabel hint = text(QUESTIONS[index][1], 14f, Font.PLAIN, Theme.MUTED); // Erzeugt den Fragetext.
        middle.add(hint, BorderLayout.NORTH); // Fügt den Fragetext ein.
        middle.add(slider, BorderLayout.CENTER); // Fügt den Regler ein.
        row.add(middle, BorderLayout.CENTER); // Fügt den Mittelteil ein.
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92)); // Begrenzt die Zeilenhöhe.
        return row; // Liefert die Zeile zurück.
    } // Beendet den Aufbau einer Bewertungszeile.

    private Card buildPreviewCard() { // Erstellt die Karte mit Live-Vorschau und Speichern-Knopf.
        Card card = new Card(new BorderLayout()); // Erzeugt die Karte.
        card.setPreferredSize(new Dimension(300, 100)); // Legt die feste Breite fest.
        JPanel box = vbox(); // Erzeugt die vertikale Anordnung.
        box.add(aligned(heading("DEIN SCORE HEUTE"), 0.5f)); // Fügt die Überschrift ein.
        box.add(Box.createVerticalStrut(14)); // Schafft Abstand.
        box.add(aligned(previewRing, 0.5f)); // Fügt den Vorschau-Ring ein.
        box.add(Box.createVerticalStrut(12)); // Schafft Abstand.
        box.add(aligned(previewTitle, 0.5f)); // Fügt die Einordnung ein.
        box.add(Box.createVerticalStrut(4)); // Schafft Abstand.
        box.add(aligned(previewXp, 0.5f)); // Fügt die XP-Vorschau ein.
        box.add(Box.createVerticalStrut(14)); // Schafft Abstand.
        box.add(aligned(previewHint, 0.5f)); // Fügt den Hinweis zur Live-Vorschau ein.
        box.add(Box.createVerticalGlue()); // Schiebt den Rest nach unten.
        previewNotice.setHorizontalAlignment(SwingConstants.CENTER); // Zentriert den Hinweis.
        box.add(aligned(previewNotice, 0.5f)); // Fügt den Hinweis ein.
        box.add(Box.createVerticalStrut(12)); // Schafft Abstand.
        saveButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46)); // Lässt den Knopf die volle Breite nutzen.
        saveButton.addActionListener(event -> saveQuestionnaire()); // Verknüpft den Klick mit Berechnung und Speicherung.
        box.add(aligned(saveButton, 0.5f)); // Fügt den Speichern-Knopf ein.
        card.add(box, BorderLayout.CENTER); // Fügt den Inhalt in die Karte ein.
        return card; // Liefert die Karte zurück.
    } // Beendet den Aufbau der Vorschaukarte.

    private JPanel buildHistory() { // Erstellt die Verlaufsseite.
        JPanel page = pageShell(); // Erzeugt den Seitenrahmen.
        JLabel subtitle = text("Alle gespeicherten Check-ins, neueste zuerst.", 15f, Font.PLAIN, Theme.MUTED); // Erklärt die Ansicht.
        page.add(header("Verlauf", subtitle, null), BorderLayout.NORTH); // Fügt die Kopfzeile ein.
        JTable table = new JTable(historyModel); // Verknüpft eine Tabelle mit dem Historienmodell.
        table.setRowHeight(48); // Gibt Zeilen viel Luft.
        table.setShowGrid(false); // Blendet das Standardgitter aus.
        table.setShowHorizontalLines(true); // Zeigt nur feine Trennlinien zwischen Zeilen.
        table.setGridColor(Theme.LINE); // Färbt die Trennlinien dezent.
        table.setIntercellSpacing(new Dimension(0, 1)); // Schafft Platz für die Trennlinien.
        table.setBackground(Theme.CARD); // Setzt den weißen Hintergrund.
        table.setRowSelectionAllowed(false); // Verhindert die Markierung von Zeilen.
        table.setFocusable(false); // Verhindert den Fokusrahmen.
        table.setFillsViewportHeight(true); // Nutzt die gesamte verfügbare Höhe.
        table.getTableHeader().setReorderingAllowed(false); // Verhindert das Verschieben von Spalten.
        table.getTableHeader().setResizingAllowed(false); // Verhindert das Ändern von Spaltenbreiten.
        table.getTableHeader().setPreferredSize(new Dimension(100, 40)); // Legt die Kopfzeilenhöhe fest.
        table.getTableHeader().setDefaultRenderer(new HeaderCell()); // Gestaltet die Kopfzeile.
        table.setDefaultRenderer(Object.class, new TextCell()); // Gestaltet normale Zellen.
        table.getColumnModel().getColumn(1).setCellRenderer(new ScoreCell()); // Gestaltet die Score-Spalte als farbige Kapsel.
        JScrollPane scroll = new JScrollPane(table); // Macht die Tabelle scrollbar.
        scroll.setColumnHeaderView(table.getTableHeader()); // Zeigt die Kopfzeile sicher über der Tabelle an.
        scroll.setBorder(BorderFactory.createEmptyBorder()); // Entfernt den Standardrahmen.
        scroll.getViewport().setBackground(Theme.CARD); // Färbt den Bereich unter der Tabelle weiß.
        Card tableCard = new Card(new BorderLayout()); // Erzeugt die Karte um die Tabelle.
        tableCard.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10)); // Verkleinert den Kartenabstand für die Tabelle.
        tableCard.add(scroll, BorderLayout.CENTER); // Fügt die Tabelle ein.
        Card emptyCard = new Card(new GridBagLayout()); // Erzeugt die Karte für den Leerzustand.
        JLabel empty = text("<html><div style='text-align:center'>Noch keine Check-ins.<br><span style='color:" + Theme.hex(Theme.MUTED) + "'>Starte mit deinem ersten Check-in, dann erscheint hier dein Verlauf.</span></div></html>", 17f, Font.BOLD, Theme.TEXT); // Erklärt den Leerzustand freundlich.
        emptyCard.add(empty); // Fügt den Text mittig ein.
        historyHost.add(tableCard, "table"); // Registriert die Tabellenansicht.
        historyHost.add(emptyCard, "empty"); // Registriert den Leerzustand.
        page.add(historyHost, BorderLayout.CENTER); // Fügt den Inhalt ein.
        return page; // Liefert die fertige Seite zurück.
    } // Beendet den Aufbau des Verlaufs.

    private JPanel buildInfo() { // Erstellt die Erklärseite.
        JPanel page = pageShell(); // Erzeugt den Seitenrahmen.
        JLabel subtitle = text("Alles, was du über Score, XP und Datenschutz wissen musst.", 15f, Font.PLAIN, Theme.MUTED); // Erklärt die Seite.
        page.add(header("So funktioniert's", subtitle, null), BorderLayout.NORTH); // Fügt die Kopfzeile ein.
        StringBuilder weights = new StringBuilder(); // Sammelt die Gewichtungsliste.
        for (int i = 0; i < QUESTIONS.length; i++) { // Durchläuft alle Bereiche.
            weights.append("<tr><td width='170'>").append(QUESTIONS[i][0]).append("</td><td><b>").append(Math.round(ScoreService.WEIGHTS[i] * 100)).append(" %</b></td></tr>"); // Fügt Bereich und Gewicht als Tabellenzeile an.
        } // Beendet die Gewichtungsliste.
        String muted = Theme.hex(Theme.MUTED); // Bestimmt die Farbe für Nebentexte.
        String html = "<html><body style='width:520px'>" // Beginnt den formatierten Text mit fester Breite.
            + "<p><b>Was ist Life Score?</b><br><span style='color:" + muted + "'>Life Score ermittelt mit täglichen Fragen deinen allgemeinen Gesundheitszustand und berechnet daraus einen Score von 0 bis 100. Mit deinem Score sammelst du XP, steigst im Level auf und schaltest Belohnungen frei.</span></p><br>" // Erklärt den Zweck der App.
            + "<p><b>So wird dein Score berechnet</b><br><span style='color:" + muted + "'>Jeder Bereich zählt unterschiedlich stark:</span></p>" // Leitet die Gewichtungen ein.
            + "<table cellpadding='2'>" + weights + "</table><br>" // Zeigt die Gewichtungen.
            + "<p><b>XP und Level</b><br><span style='color:" + muted + "'>Jeder Check-in bringt 10 XP plus bis zu 10 weitere XP je nach Score. Alle 100 XP steigst du ein Level auf. Pro Tag zählt ein Eintrag; ein erneuter Check-in ersetzt den alten.</span></p><br>" // Erklärt XP und Level.
            + "<p><b>Datenschutz</b><br><span style='color:" + muted + "'>Die App läuft komplett lokal. Deine Daten liegen nur auf diesem Gerät in data/life-score-history.csv.</span></p>" // Erklärt die lokale Speicherung.
            + "</body></html>"; // Beendet den formatierten Text.
        JLabel content = text(html, 15f, Font.PLAIN, Theme.TEXT); // Erzeugt das Textfeld.
        content.setVerticalAlignment(SwingConstants.TOP); // Richtet den Text oben aus.
        Card card = new Card(new BorderLayout()); // Erzeugt die Karte.
        card.add(content, BorderLayout.CENTER); // Fügt den Text ein.
        JPanel wrap = Theme.plain(new BorderLayout()); // Erzeugt einen Rahmen, damit die Karte nicht übermäßig wächst.
        wrap.add(card, BorderLayout.NORTH); // Hält die Karte oben.
        page.add(wrap, BorderLayout.CENTER); // Fügt den Inhalt ein.
        return page; // Liefert die fertige Seite zurück.
    } // Beendet den Aufbau der Infoseite.

    private void changeDay(int delta) { // Wechselt den Tag des Check-ins.
        LocalDate target = selectedDate.plusDays(delta); // Berechnet den neuen Tag.
        if (target.isAfter(LocalDate.now())) { // Verhindert Einträge in der Zukunft.
            return; // Beendet die Methode ohne Änderung.
        } // Beendet die Prüfung auf die Zukunft.
        selectedDate = target; // Übernimmt den neuen Tag.
        refreshCheckinState(); // Aktualisiert Datum, Hinweis und Knopf.
    } // Beendet den Tageswechsel.

    private void refreshCheckinState() { // Aktualisiert alles, was vom gewählten Tag abhängt.
        dateLabel.setText(dateText(selectedDate)); // Zeigt den gewählten Tag an.
        nextDay.setEnabled(selectedDate.isBefore(LocalDate.now())); // Deaktiviert den Vorwärts-Knopf am heutigen Tag.
        DailyEntry existing = entries.stream().filter(entry -> entry.date().equals(selectedDate)).findFirst().orElse(null); // Sucht einen vorhandenen Eintrag für diesen Tag.
        if (existing != null) { // Prüft, ob der Tag schon belegt ist.
            previewNotice.setText(html("Für diesen Tag gibt es schon einen Eintrag (" + existing.score() + " / 100). Speichern ersetzt ihn.")); // Warnt vor dem Ersetzen.
            saveButton.setText("Eintrag aktualisieren"); // Passt die Beschriftung an.
        } else { // Behandelt den Fall ohne vorhandenen Eintrag.
            previewNotice.setText(html("Neuer Eintrag für diesen Tag.")); // Bestätigt einen neuen Eintrag.
            saveButton.setText("Check-in speichern"); // Setzt die Standardbeschriftung.
        } // Beendet die Fallunterscheidung.
    } // Beendet die Aktualisierung des Check-ins.

    private void updatePreview() { // Berechnet die Live-Vorschau aus den Reglern.
        if (sliders[4] == null) { // Prüft, ob schon alle Regler existieren.
            return; // Beendet die Methode, solange die Oberfläche noch aufgebaut wird.
        } // Beendet die Prüfung auf vollständige Regler.
        int score = scoreService.calculateScore(sliders[0].getValue(), sliders[1].getValue(), sliders[2].getValue(), sliders[3].getValue(), sliders[4].getValue()); // Berechnet den Score aus allen Reglern.
        previewRing.setScore(score, true); // Aktualisiert den Ring weich.
        previewTitle.setText(scoreService.describe(score)); // Zeigt die Einordnung.
        previewXp.setText("+" + scoreService.earnedXpFor(score) + " XP"); // Zeigt die zu erwartenden XP.
    } // Beendet die Vorschauberechnung.

    private void saveQuestionnaire() { // Verarbeitet den vollständig ausgefüllten täglichen Check-in.
        try { // Startet Berechnung und Speicherung mit gemeinsamer Fehlerbehandlung.
            int score = scoreService.calculateScore(sliders[0].getValue(), sliders[1].getValue(), sliders[2].getValue(), sliders[3].getValue(), sliders[4].getValue()); // Berechnet den gewichteten Life Score.
            DailyEntry entry = new DailyEntry(selectedDate, score, scoreService.earnedXpFor(score)); // Erstellt den zu speichernden Tagesdatensatz.
            repository.saveOrReplace(entry); // Speichert oder aktualisiert den Eintrag lokal.
            selectedDate = LocalDate.now(); // Springt für den nächsten Check-in wieder auf heute.
            refreshFromStorage(); // Aktualisiert alle Ansichten aus dem gespeicherten Stand.
            show("dashboard"); // Zeigt das Ergebnis direkt in der Übersicht.
        } catch (RuntimeException exception) { // Fängt Validierungs- und Speicherfehler der Anwendung ab.
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Speichern nicht möglich", JOptionPane.ERROR_MESSAGE); // Zeigt den Fehler verständlich im Fenster an.
        } // Beendet die Fehlerbehandlung des Check-ins.
    } // Beendet die Verarbeitung des Fragebogens.

    private void refreshFromStorage() { // Lädt den aktuellen Datenstand in alle Anzeigen.
        entries = repository.loadAll(); // Liest die gesamte lokale Historie.
        historyModel.setRowCount(0); // Entfernt veraltete Zeilen aus der Tabelle.
        for (int i = entries.size() - 1; i >= 0; i--) { // Durchläuft die Einträge von neu nach alt.
            DailyEntry entry = entries.get(i); // Wählt den aktuellen Eintrag.
            historyModel.addRow(new Object[]{entry.date().format(TABLE_DATE), entry.score(), "+" + entry.earnedXp() + " XP"}); // Fügt den Tagesdatensatz als Tabellenzeile hinzu.
        } // Beendet die Aktualisierung der Tabellenzeilen.
        historyCards.show(historyHost, entries.isEmpty() ? "empty" : "table"); // Zeigt Tabelle oder Leerzustand.
        trendChart.setEntries(entries.subList(Math.max(0, entries.size() - 7), entries.size())); // Zeigt die letzten sieben Einträge im Diagramm.
        int totalXp = scoreService.totalXp(entries); // Berechnet die gesamten Erfahrungspunkte.
        int level = scoreService.levelFor(totalXp); // Leitet daraus das aktuelle Level ab.
        levelTitle.setText("Level " + level); // Aktualisiert die Levelanzeige.
        levelBar.setFraction((totalXp % 100) / 100.0); // Zeigt den Fortschritt innerhalb des Levels.
        xpText.setText((totalXp % 100) + " / 100 XP bis Level " + (level + 1) + "  ·  insgesamt " + totalXp + " XP"); // Erklärt den Fortschritt in Zahlen.
        rewardText.setText("Belohnung: " + scoreService.rewardFor(level)); // Aktualisiert die Belohnung.
        int streak = scoreService.streakFor(entries, LocalDate.now()); // Berechnet die aktuelle Serie.
        streakValue.setText(streak + (streak == 1 ? " Tag" : " Tage")); // Zeigt die Serienlänge.
        streakText.setText(streak == 0 ? "Checke heute ein, um eine Serie zu starten." : "In Folge eingecheckt – bleib dran!"); // Motiviert zur Fortsetzung.
        boolean doneToday = entries.stream().anyMatch(entry -> entry.date().equals(LocalDate.now())); // Prüft, ob heute schon eingecheckt wurde.
        dashCta.setVisible(!doneToday); // Zeigt den Button nur, wenn der Check-in noch offen ist.
        dashSubtitle.setText(entries.isEmpty() ? "Willkommen! Starte mit deinem ersten Check-in." : doneToday ? "Heute schon eingecheckt – stark!" : "Dein heutiger Check-in steht noch aus."); // Zeigt den passenden Status.
        if (entries.isEmpty()) { // Prüft, ob noch kein Check-in gespeichert wurde.
            dashRing.setScore(-1, false); // Zeigt den leeren Ring.
            ringTitle.setText("Noch keine Daten"); // Erklärt den Anfangszustand.
            ringDate.setText("Mach deinen ersten Check-in"); // Verweist auf die nächste Aktion.
        } else { // Behandelt den Fall mit vorhandenen Einträgen.
            DailyEntry latest = entries.get(entries.size() - 1); // Wählt den zeitlich neuesten gespeicherten Eintrag aus.
            dashRing.setScore(latest.score(), true); // Zeigt den aktuellen Score im Ring.
            ringTitle.setText(scoreService.describe(latest.score())); // Zeigt die Einordnung.
            ringDate.setText(latest.date().equals(LocalDate.now()) ? "Heute" : "Zuletzt am " + latest.date().format(SHORT_DATE)); // Zeigt den Zeitbezug.
        } // Beendet die Fallunterscheidung.
        refreshCheckinState(); // Aktualisiert die vom Datenstand abhängigen Teile des Check-ins.
    } // Beendet die Aktualisierung aller Ansichten.

    private String dateText(LocalDate date) { // Formuliert ein Datum möglichst menschlich.
        LocalDate today = LocalDate.now(); // Bestimmt den heutigen Tag.
        if (date.equals(today)) { // Prüft auf heute.
            return "Heute · " + date.format(SHORT_DATE); // Beschriftet den heutigen Tag.
        } // Beendet die Prüfung auf heute.
        if (date.equals(today.minusDays(1))) { // Prüft auf gestern.
            return "Gestern · " + date.format(SHORT_DATE); // Beschriftet den gestrigen Tag.
        } // Beendet die Prüfung auf gestern.
        return date.format(LONG_DATE); // Beschriftet ältere Tage ausführlich.
    } // Beendet die Datumsformulierung.

    private static JPanel pageShell() { // Erzeugt den einheitlichen Rahmen jeder Seite.
        JPanel page = new JPanel(new BorderLayout(0, 22)); // Erzeugt die Seite mit Abstand zwischen Kopf und Inhalt.
        page.setBackground(Theme.BG); // Setzt den hellen Hintergrund.
        page.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32)); // Schafft einheitlichen Seitenrand.
        return page; // Liefert den Seitenrahmen zurück.
    } // Beendet die Seitenrahmen-Erzeugung.

    private static JPanel header(String title, JLabel subtitle, JComponent right) { // Erzeugt die Kopfzeile einer Seite.
        JPanel left = vbox(); // Erzeugt die vertikale Anordnung für Titel und Untertitel.
        left.add(aligned(text(title, 30f, Font.BOLD, Theme.TEXT), 0f)); // Fügt den Seitentitel ein.
        left.add(Box.createVerticalStrut(4)); // Schafft Abstand.
        left.add(aligned(subtitle, 0f)); // Fügt den Untertitel ein.
        JPanel header = Theme.plain(new BorderLayout()); // Erzeugt die Kopfzeile.
        header.add(left, BorderLayout.WEST); // Fügt die Texte links ein.
        if (right != null) { // Prüft, ob rechts etwas stehen soll.
            JPanel wrap = Theme.plain(new GridBagLayout()); // Zentriert die rechte Komponente vertikal.
            wrap.add(right); // Fügt die Komponente ein.
            header.add(wrap, BorderLayout.EAST); // Fügt sie rechts ein.
        } // Beendet die Prüfung auf eine rechte Komponente.
        return header; // Liefert die Kopfzeile zurück.
    } // Beendet die Kopfzeilen-Erzeugung.

    private static JPanel vbox() { // Erzeugt ein durchsichtiges Panel mit vertikaler Anordnung.
        JPanel panel = Theme.plain(null); // Erzeugt ein durchsichtiges Panel.
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Ordnet die Inhalte untereinander an.
        return panel; // Liefert das Panel zurück.
    } // Beendet die Erzeugung der vertikalen Anordnung.

    private static <T extends JComponent> T aligned(T component, float x) { // Setzt die horizontale Ausrichtung in einer vertikalen Anordnung.
        component.setAlignmentX(x); // Übernimmt die gewünschte Ausrichtung.
        return component; // Liefert dieselbe Komponente zurück.
    } // Beendet das Ausrichten.

    private static JLabel heading(String text) { // Erzeugt eine kleine Karten-Überschrift in Großbuchstaben.
        return text(text, 12f, Font.BOLD, Theme.MUTED); // Liefert den gedämpften, kleinen Text.
    } // Beendet die Überschrift-Erzeugung.

    private static JLabel text(String content, float size, int style, Color color) { // Erzeugt ein einheitlich gestaltetes Textfeld.
        JLabel label = new JLabel(content); // Erzeugt das Textfeld.
        label.setFont(Theme.font(style, size)); // Setzt Schrift und Größe.
        label.setForeground(color); // Setzt die Textfarbe.
        return label; // Liefert das Textfeld zurück.
    } // Beendet die Textfeld-Erzeugung.

    private static String html(String content) { // Verpackt Text so, dass er in der Vorschaukarte umbricht.
        return "<html><div style='width:190px;text-align:center'>" + content + "</div></html>"; // Liefert HTML mit fester Umbruchbreite.
    } // Beendet die HTML-Verpackung.

    private static final class TextCell extends DefaultTableCellRenderer { // Gestaltet normale Tabellenzellen.
        @Override // Ersetzt die Standardaufbereitung der Zelle.
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) { // Bereitet eine Zelle zur Anzeige vor.
            super.getTableCellRendererComponent(table, value, false, false, row, column); // Übernimmt den Text der Zelle.
            setFont(Theme.font(Font.PLAIN, 15f)); // Wählt die Zellschrift.
            setForeground(column == 2 ? Theme.ACCENT_DARK : Theme.TEXT); // Färbt die XP-Spalte grün.
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16)); // Schafft seitlichen Abstand.
            return this; // Liefert die vorbereitete Zelle zurück.
        } // Beendet die Zellaufbereitung.
    } // Beendet die Klasse TextCell.

    private static final class HeaderCell extends DefaultTableCellRenderer { // Gestaltet die Tabellenkopfzeile.
        @Override // Ersetzt die Standardaufbereitung der Kopfzelle.
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) { // Bereitet eine Kopfzelle zur Anzeige vor.
            super.getTableCellRendererComponent(table, String.valueOf(value).toUpperCase(Locale.GERMAN), false, false, row, column); // Übernimmt den Text in Großbuchstaben.
            setFont(Theme.font(Font.BOLD, 12f)); // Wählt die kleine kräftige Schrift.
            setForeground(Theme.MUTED); // Wählt die gedämpfte Farbe.
            setBackground(Theme.CARD); // Setzt den weißen Hintergrund.
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE), BorderFactory.createEmptyBorder(0, 16, 0, 16))); // Zeichnet eine Linie unter der Kopfzeile.
            setHorizontalAlignment(column == 1 ? SwingConstants.CENTER : SwingConstants.LEFT); // Zentriert die Score-Spalte.
            return this; // Liefert die vorbereitete Zelle zurück.
        } // Beendet die Kopfzellen-Aufbereitung.
    } // Beendet die Klasse HeaderCell.

    private static final class ScoreCell extends DefaultTableCellRenderer { // Zeigt Scores als farbige Kapsel.
        private int score; // Merkt sich den Score für die Farbe.

        ScoreCell() { // Erzeugt den Renderer.
            setHorizontalAlignment(SwingConstants.CENTER); // Zentriert den Text.
            setOpaque(false); // Lässt den Zellhintergrund durchscheinen.
        } // Beendet den Konstruktor.

        @Override // Ersetzt die Standardaufbereitung der Zelle.
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) { // Bereitet eine Zelle zur Anzeige vor.
            super.getTableCellRendererComponent(table, value, false, false, row, column); // Übernimmt den Wert der Zelle.
            score = ((Number) value).intValue(); // Merkt sich den Score.
            setText(score + " / 100"); // Formuliert den Anzeigetext.
            setFont(Theme.font(Font.BOLD, 14f)); // Wählt die kräftige Schrift.
            setForeground(Theme.scoreColor(score)); // Färbt den Text mit der Ampelfarbe.
            setOpaque(false); // Stellt sicher, dass kein Standardhintergrund gezeichnet wird.
            return this; // Liefert die vorbereitete Zelle zurück.
        } // Beendet die Zellaufbereitung.

        @Override // Ersetzt das Standardzeichnen der Zelle.
        protected void paintComponent(Graphics graphics) { // Zeichnet erst die Kapsel, dann den Text.
            Graphics2D g2 = (Graphics2D) graphics.create(); // Erstellt eine private Kopie der Zeichenfläche.
            Theme.antialias(g2); // Aktiviert glatte Kanten.
            Color base = Theme.scoreColor(score); // Bestimmt die Ampelfarbe.
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 32)); // Wählt eine transparente Variante der Farbe.
            int width = 96; // Legt die Kapselbreite fest.
            int height = 30; // Legt die Kapselhöhe fest.
            g2.fillRoundRect((getWidth() - width) / 2, (getHeight() - height) / 2, width, height, height, height); // Zeichnet die Kapsel.
            g2.dispose(); // Gibt die Kopie der Zeichenfläche frei.
            super.paintComponent(graphics); // Zeichnet den Text über die Kapsel.
        } // Beendet das Zeichnen der Zelle.
    } // Beendet die Klasse ScoreCell.
} // Beendet die Klasse LifeScoreFrame.
