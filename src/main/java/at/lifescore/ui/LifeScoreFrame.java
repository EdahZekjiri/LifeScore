package at.lifescore.ui; // Verbindet die Ansichten mit Berechnung und Speicherung.

import at.lifescore.model.DailyAnswers; // Speichert und lädt die fünf Antworten gemeinsam.
import at.lifescore.model.DailyEntry; // Beschreibt einen abgeschlossenen Tages-Check-in.
import at.lifescore.repository.EntryRepository; // Kapselt die lokale CSV-Datei.
import at.lifescore.repository.AvatarRepository; // Speichert Avatar-Auswahl und erreichte Freischaltungen.
import at.lifescore.service.ScoreService; // Berechnet Score, Erfahrung und Level.
import java.awt.*; // Stellt Layout, Farben und Abstände bereit.
import java.text.ParseException; // Erkennt fehlerhafte manuelle Datumseingaben.
import java.time.LocalDate; // Arbeitet mit Kalendertagen ohne Uhrzeit.
import java.time.ZoneId; // Übersetzt den Datumstyp des Swing-Eingabefelds.
import java.time.format.DateTimeFormatter; // Formatiert Datumsangaben einheitlich.
import java.util.Date; // Verbindet Swing mit dem modernen Datumsmodell.
import java.util.List; // Hält die aktuelle Historie im Speicher.
import javax.swing.*; // Nutzt ausschließlich Java-Standardkomponenten.
import javax.swing.border.EmptyBorder; // Legt einheitliche Innenabstände fest.
import javax.swing.table.DefaultTableCellRenderer; // Formatiert die Verlaufstabelle.
import javax.swing.table.DefaultTableModel; // Verwaltet die sichtbaren Tabellenzeilen.
import static at.lifescore.ui.UiTheme.*; // Nutzt das zentrale Gestaltungssystem.

public final class LifeScoreFrame extends JFrame { // Das Fenster koordiniert die Ansichten, aber berechnet selbst keine Scores.
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy"); // Verwendet ein klar erkennbares Datumsformat.
    private final ScoreService scoreService; // Hält die unabhängig testbare Fachlogik.
    private final EntryRepository repository; // Hält die unabhängig testbare Speicherung.
    private final CardLayout pages = new CardLayout(); // Schaltet Ansichten um, ohne neue Fenster zu öffnen.
    private final JPanel pageContainer = new JPanel(pages); // Enthält alle Inhaltsseiten.
    private final JButton[] navigation = new JButton[5]; // Markiert die jeweils aktive Seite einschließlich Avatar-Gestaltung.
    private final AvatarView sidebarAvatar = new AvatarView(118); // Zeigt den persönlichen Avatar dauerhaft auf jeder Seite.
    private final AvatarStudio avatarStudio; // Verbindet Vorschau, Auswahl und nutzbare Level-Belohnungen.
    private String newRewards = ""; // Merkt sich neu verdiente Extras für die Rückmeldung nach einem Check-in.
    private final JLabel pageTitle = label("Dein Überblick", 29, INK); // Zeigt den Titel der aktuellen Seite.
    private final JLabel pageSubtitle = label("Kleine Schritte. Ein bewussterer Alltag.", 14, MUTED); // Erklärt den Zweck der Seite.
    private final JLabel scoreDate = label("Noch kein Eintrag", 13, MUTED); // Nennt das tatsächliche Datum des angezeigten Scores.
    private final JLabel averageLabel = label("–", 31, INK); // Zeigt den Durchschnitt aller gespeicherten Tage.
    private final JLabel countLabel = label("0", 31, INK); // Zeigt die Zahl tatsächlich gespeicherter Tage.
    private final JLabel levelLabel = label("Level 1", 24, INK); // Zeigt den aus XP berechneten Fortschritt.
    private final JLabel xpLabel = label("0 XP insgesamt", 13, MUTED); // Macht den Erfahrungsstand transparent.
    private final JLabel rewardLabel = label("Nächste Belohnung ab Level 3", 13, MUTED); // Nennt die einfache bestehende Belohnungslogik.
    private final JLabel welcomeLabel = label("Nimm dir einen Moment für dich.", 24, INK); // Reagiert darauf, ob heute bereits ein Eintrag besteht.
    private final JLabel welcomeDetail = label("Fünf Fragen helfen dir, deinen Tag einzuordnen.", 14, MUTED); // Gibt eine kurze Orientierung vor der Hauptaktion.
    private final JButton checkInButton = button("Heutigen Check-in starten  →", true); // Führt direkt zum heutigen Datum.
    private final JProgressBar levelBar = new JProgressBar(0, 100); // Zeigt Erfahrung innerhalb des aktuellen Levels.
    private final ScoreVisuals.Ring scoreRing = new ScoreVisuals.Ring(); // Stellt den letzten Score ohne zusätzliche Bibliothek dar.
    private final ScoreVisuals.Trend dashboardTrend = new ScoreVisuals.Trend(); // Visualisiert den aktuellen Verlauf auf der Übersicht.
    private final ScoreVisuals.Trend historyTrend = new ScoreVisuals.Trend(); // Ergänzt die vollständige Verlaufstabelle.
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel()); // Ermöglicht auch rückwirkende Einträge.
    private final JSlider[] ratings = new JSlider[5]; // Bietet fünf direkt verständliche Skalen.
    private final JLabel entryHint = label("", 13, MUTED); // Erklärt, ob ein neuer oder vorhandener Tag bearbeitet wird.
    private final JLabel previewLabel = label("Vorschau: 50 / 100", 19, INK); // Macht die Wirkung der Antworten vor dem Speichern sichtbar.
    private final JLabel saveStatus = label(" ", 13, GREEN); // Bestätigt die Speicherung ohne unterbrechendes Popup.
    private final JButton saveButton = button("Check-in speichern", true); // Speichert den vollständigen Tagesdatensatz.
    private final DefaultTableModel historyModel = new DefaultTableModel(new Object[]{"Datum", "Life Score", "XP", "Antworten"}, 0) { // Definiert eine lesbare Ergebnistabelle.
        @Override public boolean isCellEditable(int row, int column) { return false; } // Verhindert versehentliche Tabellenänderungen, erlaubt aber Auswahl und Kopieren.
    }; // Beendet das Tabellenmodell.
    private final JTable historyTable = new JTable(historyModel); // Hält die Auswahl zum Bearbeiten eines Tages bereit.
    private List<DailyEntry> entries = List.of(); // Speichert den zuletzt erfolgreich geladenen Stand.
    private boolean loadingAnswers; // Verhindert Zwischenmeldungen während des Formularladens.
    private boolean storageAvailable; // Verhindert Speichern nach einem fehlgeschlagenen Start-Lesezugriff.
    private LocalDate loadedDate; // Unterscheidet echte Tageswechsel von internen Uhrzeitkorrekturen des Datumsfelds.

    public LifeScoreFrame(ScoreService scoreService, EntryRepository repository) { // Erhält seine Abhängigkeiten explizit vom Programmeinstieg.
        super("Life Score · Dein Alltag im Blick"); // Beschreibt die Anwendung im Fenstertitel.
        this.scoreService = scoreService; // Übernimmt die Berechnungslogik.
        this.repository = repository; // Übernimmt die Speicherlogik.
        this.avatarStudio = new AvatarStudio(new AvatarRepository(repository.dataDirectory().resolve("avatar.properties")), sidebarAvatar::setProfile); // Lädt den persönlichen Look und hält Profil- und Testdaten sauber getrennt.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Beendet die Anwendung beim Schließen.
        setMinimumSize(new Dimension(940, 700)); // Hält Formular und Navigation auch im kleinsten Fenster nutzbar.
        setSize(1160, 880); // Zeigt das vollständige Dashboard bei der üblichen Startgröße.
        setContentPane(buildContent()); // Baut die fünf verständlichen Hauptansichten.
        setLocationRelativeTo(null); // Zentriert das Fenster auf dem Bildschirm.
        showPage(0); // Beginnt immer mit dem Überblick.
        try { // Hält das Fenster auch bei einer defekten Datei bedienbar.
            refreshFromStorage(); // Lädt vorhandene Daten ohne sie zu verändern.
            loadSelectedDate(); // Stellt vorhandene Antworten des heutigen Tages wieder her.
        } catch (RuntimeException exception) { // Meldet Lesefehler statt einen leeren neuen Datenstand vorzutäuschen.
            saveButton.setEnabled(false); // Verhindert Überschreiben bei unbekanntem Datenstand.
            welcomeLabel.setText("Historie nicht verfügbar"); // Macht den Fehler direkt auf der Übersicht sichtbar.
            welcomeDetail.setText("Bitte die lokale CSV-Datei prüfen und die App neu starten."); // Gibt einen konkreten nächsten Schritt.
            SwingUtilities.invokeLater(() -> showError(exception)); // Zeigt die Fehlermeldung nach dem Aufbau des Fensters.
        } // Beendet die Startfehlerbehandlung.
    } // Beendet den Fensteraufbau.

    private JPanel buildContent() { // Legt Navigation und Inhaltsbereich nebeneinander.
        JPanel root = new JPanel(new BorderLayout()); // Verwendet eine feste Seitenleiste und flexiblen Inhalt.
        root.add(buildSidebar(), BorderLayout.WEST); // Hält Navigation auf jeder Seite sichtbar.
        JPanel main = new JPanel(new BorderLayout(0, 24)); // Trennt Seitenkopf und Inhalt durch klaren Abstand.
        main.setBorder(new EmptyBorder(30, 30, 24, 30)); // Schafft Luft zum Fensterrand.
        JPanel header = transparent(new BorderLayout()); // Gruppiert Titel und Datum.
        JPanel titles = transparent(new GridLayout(2, 1, 0, 7)); // Ordnet Seitentitel über Untertitel an.
        titles.add(pageTitle); // Zeigt die aktive Ansicht.
        titles.add(pageSubtitle); // Zeigt den passenden Erläuterungstext.
        header.add(titles, BorderLayout.CENTER); // Nutzt die linke Seite für die Orientierung.
        header.add(label(LocalDate.now().format(DATE), 13, MUTED), BorderLayout.EAST); // Zeigt das Datum beim App-Start als Kontext.
        main.add(header, BorderLayout.NORTH); // Hält den Seitenkopf außerhalb des Scrollbereichs.
        pageContainer.add(scroll(buildDashboard()), "0"); // Ermöglicht bei kleinen Fenstern vertikales Scrollen.
        pageContainer.add(scroll(buildQuestionnaire()), "1"); // Hält alle fünf Fragen erreichbar.
        pageContainer.add(buildHistory(), "2"); // Nutzt einen eigenen Scrollbereich für die Tabelle.
        pageContainer.add(scroll(avatarStudio), "3"); // Öffnet die vollständige Avatar-Auswahl mit echten Freischaltungen.
        pageContainer.add(scroll(buildInfo()), "4"); // Hält die Erklärung auch bei größeren Schriften lesbar.
        main.add(pageContainer, BorderLayout.CENTER); // Lässt den Inhalt den restlichen Raum nutzen.
        root.add(main, BorderLayout.CENTER); // Vervollständigt das Fensterlayout.
        return root; // Gibt die zusammengesetzte Oberfläche zurück.
    } // Beendet den Inhaltsaufbau.

    private JPanel buildSidebar() { // Erstellt eine dauerhafte, ruhige Navigation.
        JPanel sidebar = new JPanel(new BorderLayout(0, 20)); // Schafft auch für den sichtbaren Avatar ausreichend Platz.
        sidebar.setBackground(INK); // Gibt der Navigation eine dunkle Kontrastfläche.
        sidebar.setPreferredSize(new Dimension(205, 0)); // Hält die Navigation unabhängig von der Fensterbreite stabil.
        sidebar.setBorder(new EmptyBorder(24, 19, 22, 19)); // Hält Inhalte auch bei Mindestfensterhöhe vom Rand fern.
        JPanel brand = transparent(new GridLayout(2, 1, 0, 7)); // Gruppiert Namen und kurze Positionierung.
        brand.add(label("life score", 27, Color.WHITE)); // Verwendet eine einfache typografische Marke.
        brand.add(label("DEIN ALLTAG IM BLICK", 10, new Color(192, 210, 192))); // Erklärt den App-Zweck in einer Zeile.
        JPanel identity = transparent(new BorderLayout(0, 10)); // Verbindet App-Marke und sichtbare persönliche Figur.
        identity.add(brand, BorderLayout.NORTH); // Platziert die Marke oberhalb des Avatars.
        identity.add(sidebarAvatar, BorderLayout.CENTER); // Zeigt den angelegten Look sofort beim Start.
        sidebar.add(identity, BorderLayout.NORTH); // Verankert die Identität über der Navigation.
        JPanel nav = transparent(new GridLayout(5, 1, 0, 8)); // Verwendet gleich große Navigationsflächen für alle fünf Seiten.
        String[] names = {"01   Überblick", "02   Check-in", "03   Verlauf", "04   Avatar", "05   Erklärung"}; // Macht den Avatar mit einem eigenen sichtbaren Einstieg auffindbar.
        for (int i = 0; i < names.length; i++) { // Erzeugt jede Hauptnavigation genau einmal.
            final int index = i; // Hält den Zielindex für den Klick stabil.
            JButton item = button(names[i], false); // Verwendet tastaturbedienbare Standardknöpfe.
            item.setHorizontalAlignment(SwingConstants.LEFT); // Richtet Navigationstexte einheitlich links aus.
            item.addActionListener(event -> showPage(index)); // Öffnet die gewünschte Seite.
            navigation[i] = item; // Merkt sich den Knopf für die Auswahlmarkierung.
            nav.add(item); // Fügt die nächste Navigationszeile hinzu.
        } // Beendet den Navigationsaufbau.
        JPanel navTop = transparent(new BorderLayout()); // Verhindert vertikales Strecken der Navigation.
        navTop.add(nav, BorderLayout.NORTH); // Verankert die Navigation unterhalb der Marke.
        sidebar.add(navTop, BorderLayout.CENTER); // Nutzt den verbleibenden Raum als ruhige Fläche.
        sidebar.add(label("<html><b>● Lokal &amp; offline</b><br><br>Deine Einträge bleiben<br>auf diesem Gerät.<br><br>Life Score · v1.1</html>", 12, new Color(192, 210, 192)), BorderLayout.SOUTH); // Beschreibt den tatsächlichen lokalen Betrieb ohne Verschlüsselung zu behaupten.
        return sidebar; // Gibt die Seitenleiste zurück.
    } // Beendet den Seitenleistenaufbau.

    private JPanel buildDashboard() { // Stellt Tagesaktion, Score und Entwicklung in einer klaren Reihenfolge dar.
        JPanel panel = transparent(new BorderLayout(0, 18)); // Ordnet die drei Dashboard-Bereiche vertikal an.
        JPanel welcome = card(new BorderLayout(18, 14)); // Erstellt die obere Einladung zum Check-in.
        welcome.setBorder(new EmptyBorder(16, 24, 16, 24)); // Hält die Einladung kompakt, damit der Verlauf im Startfenster sichtbar bleibt.
        JPanel welcomeText = transparent(new GridLayout(2, 1, 0, 9)); // Trennt Haupttext und Erläuterung.
        welcomeText.add(welcomeLabel); // Zeigt eine vom Tagesstatus abhängige Ansprache.
        welcomeText.add(welcomeDetail); // Beschreibt die nächste Aktion.
        welcome.add(welcomeText, BorderLayout.CENTER); // Gibt den Texten die volle Breite.
        checkInButton.addActionListener(event -> openDate(LocalDate.now())); // Öffnet stets heute, auch nach dem Bearbeiten alter Tage.
        JPanel action = transparent(new FlowLayout(FlowLayout.LEFT, 0, 0)); // Hält den Knopf auf seiner natürlichen Breite.
        action.add(checkInButton); // Macht den nächsten Schritt sichtbar.
        JButton avatarButton = button("Avatar gestalten →", false); // Bietet auch auf dem Dashboard einen direkten Zugang zum eigenen Look.
        avatarButton.addActionListener(event -> showPage(3)); // Öffnet die Avatar- und Belohnungsseite.
        action.add(Box.createHorizontalStrut(10)); // Trennt die beiden Aktionen ohne eine zusätzliche Zeile.
        action.add(avatarButton); // Macht die neue Funktion sofort auffindbar.
        welcome.add(action, BorderLayout.SOUTH); // Platziert die Hauptaktion unter der Erklärung.
        panel.add(welcome, BorderLayout.NORTH); // Beginnt die Seite mit dem persönlichen Tagesstatus.
        JPanel middle = transparent(new GridLayout(1, 2, 18, 0)); // Gibt Score und Fortschritt gleich viel Raum.
        JPanel scoreCard = card(new BorderLayout(0, 8)); // Bündelt Zahl, Skala und Datum.
        scoreCard.add(label("LETZTER LIFE SCORE", 12, MUTED), BorderLayout.NORTH); // Verwechselt den letzten Eintrag nicht mit heute.
        scoreCard.add(scoreRing, BorderLayout.CENTER); // Zeigt den Score als klaren Mittelpunkt.
        scoreDate.setHorizontalAlignment(SwingConstants.CENTER); // Zentriert das zugehörige Datum unter dem Ring.
        scoreCard.add(scoreDate, BorderLayout.SOUTH); // Bindet den Score an einen konkreten Kalendertag.
        middle.add(scoreCard); // Fügt die Score-Karte links ein.
        JPanel progress = card(new BorderLayout(0, 18)); // Fasst Fortschritt und zwei Kennzahlen zusammen.
        JPanel stats = transparent(new GridLayout(1, 2, 16, 0)); // Stellt Durchschnitt und Check-ins nebeneinander.
        stats.add(statistic(averageLabel, "Ø Life Score")); // Beschriftet den Durchschnitt ausdrücklich.
        stats.add(statistic(countLabel, "Check-ins")); // Nennt die gezählten Einträge.
        progress.add(stats, BorderLayout.NORTH); // Zeigt die Kennzahlen oben.
        JPanel levels = transparent(new GridLayout(4, 1, 0, 9)); // Ordnet Level, XP, Balken und Ziel untereinander.
        levels.add(levelLabel); // Zeigt das aktuelle Level.
        levels.add(xpLabel); // Zeigt die gesamte gesammelte Erfahrung.
        levelBar.setStringPainted(true); // Macht den Fortschritt auch ohne Farberkennung lesbar.
        levelBar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI()); // Verhindert eine abweichende blaue Systemfarbe unter macOS.
        levelBar.setForeground(GREEN); // Verwendet dieselbe Fortschrittsfarbe wie der Score-Ring.
        levelBar.setBackground(PALE); // Macht den noch offenen Anteil erkennbar.
        levelBar.setOpaque(true); // Zeichnet den ungefüllten Anteil auch unter dem macOS-Look-and-Feel.
        levelBar.setBorderPainted(false); // Vermeidet einen zusätzlichen dekorativen Rahmen.
        levels.add(levelBar); // Fügt den Erfahrungsbalken ein.
        levels.add(rewardLabel); // Zeigt das nächste erreichbare Avatar-Ziel.
        progress.add(levels, BorderLayout.CENTER); // Nutzt den Hauptbereich der Fortschrittskarte.
        middle.add(progress); // Fügt die Fortschrittskarte rechts ein.
        panel.add(middle, BorderLayout.CENTER); // Platziert beide Karten zwischen Einstieg und Verlauf.
        JPanel trend = card(new BorderLayout(0, 10)); // Erstellt eine breite Verlaufskarte.
        JPanel trendHeading = transparent(new BorderLayout()); // Verbindet Titel und Navigationsaktion.
        trendHeading.add(label("Deine Entwicklung", 22, INK), BorderLayout.WEST); // Erklärt die Grafik ohne Fachsprache.
        JButton history = button("Verlauf ansehen →", false); // Bietet den Weg zu allen Daten.
        history.addActionListener(event -> showPage(2)); // Öffnet die tabellarische Historie.
        trendHeading.add(history, BorderLayout.EAST); // Ordnet die Nebenaktion rechts an.
        trend.add(trendHeading, BorderLayout.NORTH); // Beschriftet die Verlaufskarte.
        trend.add(dashboardTrend, BorderLayout.CENTER); // Zeigt ausschließlich reale Einträge.
        dashboardTrend.setPreferredSize(new Dimension(460, 110)); // Hält die Übersicht kompakt; die Verlaufsseite bietet die größere Grafik.
        trend.add(label("Letzte 14 Einträge · Fehlende Tage sind keine Nullwerte.", 12, MUTED), BorderLayout.SOUTH); // Erklärt den Ausschnitt und fehlende Messungen.
        panel.add(trend, BorderLayout.SOUTH); // Schließt die Übersicht mit dem zeitlichen Verlauf ab.
        return panel; // Liefert das Dashboard.
    } // Beendet den Dashboard-Aufbau.

    private JPanel buildQuestionnaire() { // Macht die fünf Bewertungen direkt verständlich.
        JPanel panel = card(new BorderLayout(0, 18)); // Hält den gesamten Check-in in einer weißen Karte.
        JPanel top = transparent(new BorderLayout(14, 12)); // Gruppiert Datum, Hilfe und Bearbeitungsstatus.
        JPanel dateRow = transparent(new FlowLayout(FlowLayout.LEFT, 10, 0)); // Hält das Datumsfeld kompakt.
        JLabel dateLabel = label("Dein Tag", 15, INK); // Beschriftet die Datumseingabe.
        dateLabel.setLabelFor(dateSpinner); // Verknüpft Beschriftung und Feld für Hilfstechnologien.
        dateRow.add(dateLabel); // Fügt die Datumsbeschriftung ein.
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "dd.MM.yyyy"); // Zeigt ein vertrautes Datum statt eines Zeitstempels.
        editor.getFormat().setLenient(false); // Verhindert stilles Umdeuten ungültiger Kalendertage.
        dateSpinner.setEditor(editor); // Verwendet den expliziten Datumeditor.
        dateSpinner.setPreferredSize(new Dimension(158, 34)); // Macht die Datumseingabe ausreichend groß.
        dateSpinner.addChangeListener(event -> { if (!selectedDate().equals(loadedDate)) { loadSelectedDate(); } }); // Lädt nur bei einem anderen Kalendertag; commitEdit darf Antworten nicht zurücksetzen.
        dateRow.add(dateSpinner); // Fügt die bearbeitbare Datumsauswahl hinzu.
        JButton today = button("Heute", false); // Verkürzt die Rückkehr zum heutigen Check-in.
        today.addActionListener(event -> openDate(LocalDate.now())); // Setzt Datum und Antworten gemeinsam zurück.
        dateRow.add(today); // Fügt den Datums-Kurzbefehl hinzu.
        top.add(dateRow, BorderLayout.NORTH); // Platziert das Datum vor den Fragen.
        top.add(label("Bewerte deinen Tag: 0 = gar nicht zufrieden, 10 = sehr zufrieden.", 13, MUTED), BorderLayout.CENTER); // Erklärt die Bedeutung der subjektiven Skala.
        top.add(entryHint, BorderLayout.SOUTH); // Macht vorhandene oder fehlende Antworten sichtbar.
        panel.add(top, BorderLayout.NORTH); // Fügt die Formularanleitung ein.
        JPanel questions = transparent(new GridLayout(5, 1, 0, 6)); // Ordnet alle Fragen in einer nachvollziehbaren Reihenfolge an.
        String[] names = {"Schlaf", "Bewegung", "Ernährung", "Produktivität", "Soziale Kontakte"}; // Benennt die fünf bewerteten Lebensbereiche.
        String[] prompts = {"Wie erholt fühlst du dich nach deinem Schlaf?", "Wie zufrieden bist du mit deiner Bewegung heute?", "Wie zufrieden bist du mit deiner Ernährung heute?", "Wie zufrieden bist du mit dem, was du geschafft hast?", "Wie zufrieden bist du mit deinen sozialen Kontakten?"}; // Formuliert jede Bewertung als konkrete Reflexionsfrage.
        String[] weights = {"25 %", "20 %", "20 %", "20 %", "15 %"}; // Macht die unveränderten fachlichen Gewichtungen sichtbar.
        for (int i = 0; i < ratings.length; i++) { // Erstellt eine einheitliche Zeile pro Lebensbereich.
            JPanel row = transparent(new BorderLayout(15, 5)); // Trennt Beschriftung und interaktive Skala.
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE)); // Trennt die Fragen dezent voneinander.
            JPanel words = transparent(new GridLayout(2, 1, 0, 3)); // Ordnet Bereich und konkrete Frage übereinander.
            words.add(label(names[i] + "  ·  " + weights[i], 15, INK)); // Zeigt die Gewichtung direkt an der Frage.
            words.add(label(prompts[i], 12, MUTED)); // Erklärt, was bewertet werden soll.
            row.add(words, BorderLayout.NORTH); // Stellt die Erklärung vor die Eingabe.
            JSlider slider = new JSlider(0, 10, 5); // Beginnt bei einem neutralen, ausdrücklich editierbaren Mittelwert.
            slider.setOpaque(false); // Übernimmt den Kartenhintergrund.
            slider.setMajorTickSpacing(1); // Definiert Schritte von genau einem Bewertungspunkt.
            slider.setPaintTicks(true); // Macht alle elf möglichen Werte sichtbar.
            slider.setSnapToTicks(true); // Verhindert Zwischenwerte beim Ziehen.
            slider.getAccessibleContext().setAccessibleName(names[i] + ": " + prompts[i]); // Gibt Tastatur- und Screenreader-Nutzern den vollständigen Kontext.
            slider.setToolTipText("0 bis 10 · Mit den Pfeiltasten ändern"); // Erklärt die Tastaturbedienung.
            JLabel value = label("5 / 10", 18, GREEN); // Zeigt die aktuelle Auswahl zusätzlich als Zahl.
            value.setPreferredSize(new Dimension(68, 30)); // Verhindert Layoutsprünge zwischen ein- und zweistelligen Werten.
            slider.addChangeListener(event -> { // Reagiert unmittelbar auf jede Bewertung.
                value.setText(slider.getValue() + " / 10"); // Synchronisiert den sichtbaren Zahlenwert.
                if (!loadingAnswers) { updatePreview(); saveStatus.setText("Noch nicht gespeichert"); } // Zeigt Änderungen erst nach vollständigem Laden des Formulars an.
            }); // Beendet die Reaktion auf Skalenänderungen.
            ratings[i] = slider; // Hält den Wert für Berechnung und späteres Wiederladen bereit.
            row.add(slider, BorderLayout.CENTER); // Gibt der Skala die volle verfügbare Breite.
            row.add(value, BorderLayout.EAST); // Ordnet den Zahlenwert rechts neben der Skala an.
            questions.add(row); // Fügt den Lebensbereich in den Fragebogen ein.
        } // Beendet den Aufbau aller fünf Fragen.
        panel.add(questions, BorderLayout.CENTER); // Stellt die Fragen in die Mitte des Check-ins.
        JPanel footer = transparent(new BorderLayout(12, 12)); // Verbindet Vorschau, Aktion und Rückmeldung.
        JPanel summary = transparent(new GridLayout(2, 1, 0, 6)); // Unterscheidet Vorschau deutlich von gespeichertem Ergebnis.
        summary.add(previewLabel); // Zeigt den noch nicht gespeicherten Score.
        summary.add(saveStatus); // Zeigt den Speicherstatus als Text.
        footer.add(summary, BorderLayout.CENTER); // Ordnet die Rückmeldung links an.
        saveButton.addActionListener(event -> saveQuestionnaire()); // Validiert und speichert die Antworten.
        footer.add(saveButton, BorderLayout.EAST); // Setzt die Hauptaktion an das Ende des Formulars.
        panel.add(footer, BorderLayout.SOUTH); // Schließt den Check-in ab.
        panel.setPreferredSize(new Dimension(690, 620)); // Hält Fragen auch beim Verkleinern über Scrollen erreichbar.
        return panel; // Gibt den fertigen Fragebogen zurück.
    } // Beendet den Formularaufbau.

    private JPanel buildHistory() { // Zeigt Diagramm und genaue Werte gemeinsam.
        JPanel panel = transparent(new BorderLayout(0, 18)); // Trennt die zusammenfassende Grafik von den Datensätzen.
        JPanel chart = card(new BorderLayout(0, 6)); // Gibt der Grafik eine eigene Karte.
        chart.add(label("Jeder Eintrag ist ein Schritt", 22, INK), BorderLayout.NORTH); // Ordnet den Verlauf als Reflexionshilfe ein.
        chart.add(historyTrend, BorderLayout.CENTER); // Zeigt den gleichen echten Datenstand wie das Dashboard.
        chart.add(label("Letzte 14 Einträge · Alle gespeicherten Tage findest du unten.", 12, MUTED), BorderLayout.SOUTH); // Erklärt den Unterschied zwischen Grafik und Tabelle.
        panel.add(chart, BorderLayout.NORTH); // Stellt die Entwicklung vor die Details.
        JPanel tableCard = card(new BorderLayout(0, 16)); // Gruppiert Tabelle und Bearbeitungsaktion.
        historyTable.setRowHeight(39); // Macht die Werte gut lesbar und auswählbar.
        historyTable.setShowGrid(false); // Vermeidet ein dichtes Tabellenraster.
        historyTable.setIntercellSpacing(new Dimension(0, 0)); // Hält die Zeilen ruhig und geschlossen.
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Erlaubt genau einen Tag zur Bearbeitung.
        historyTable.setSelectionBackground(PALE); // Markiert die Auswahl mit der hellen Akzentfarbe.
        historyTable.setSelectionForeground(INK); // Hält ausgewählte Werte lesbar.
        historyTable.setFillsViewportHeight(true); // Verwendet auch leeren Tabellenraum als weiße Fläche.
        historyTable.getTableHeader().setPreferredSize(new Dimension(0, 36)); // Gibt den Spaltentiteln ausreichend Platz.
        historyTable.getTableHeader().setReorderingAllowed(false); // Hält die Spaltenreihenfolge für den Überblick stabil.
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() { // Formatiert alle Datenzellen einheitlich.
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) { // Behält die Swing-Auswahl- und Fokusdarstellung bei.
                Component component = super.getTableCellRendererComponent(table, value, selected, focus, row, column); // Nutzt die Standarddarstellung als Grundlage.
                setBorder(new EmptyBorder(0, 12, 0, 12)); // Gibt jeder Zelle ausreichend horizontalen Abstand.
                if (!selected) { component.setBackground(row % 2 == 0 ? Color.WHITE : BACKGROUND); component.setForeground(INK); } // Unterscheidet Nachbarzeilen dezent.
                return component; // Liefert die fertig formatierte Zelle.
            } // Beendet die Zellendarstellung.
        }); // Beendet den Tabellenrenderer.
        JScrollPane tableScroll = new JScrollPane(historyTable); // Hält große Historien scrollbar.
        tableScroll.setColumnHeaderView(historyTable.getTableHeader()); // Zeigt Spaltentitel auch beim unabhängigen Rendern der Ansicht.
        tableScroll.setBorder(BorderFactory.createLineBorder(LINE)); // Fasst die Tabelle mit einer dezenten Linie ein.
        tableCard.add(tableScroll, BorderLayout.CENTER); // Nutzt den meisten Platz für die Werte.
        JButton edit = button("Ausgewählten Tag bearbeiten →", false); // Macht die Bearbeitungsfunktion sichtbar.
        edit.setEnabled(false); // Benötigt zuerst eine konkrete Auswahl.
        historyTable.getSelectionModel().addListSelectionListener(event -> edit.setEnabled(historyTable.getSelectedRow() >= 0)); // Aktiviert die Aktion erst nach Auswahl.
        edit.addActionListener(event -> { // Öffnet den ausgewählten Tag mit seinen gespeicherten Antworten.
            int index = historyTable.getSelectedRow(); // Liest die ausgewählte Tabellenzeile.
            if (index >= 0) { openDate(entries.get(entries.size() - 1 - index).date()); } // Berücksichtigt die umgekehrt chronologische Tabellenreihenfolge.
        }); // Beendet die Bearbeitungsaktion.
        tableCard.add(edit, BorderLayout.SOUTH); // Platziert die Aktion direkt bei der Tabelle.
        panel.add(tableCard, BorderLayout.CENTER); // Fügt die vollständige Historie ein.
        return panel; // Gibt die Verlaufsseite zurück.
    } // Beendet den Aufbau der Historie.

    private JPanel buildInfo() { // Erklärt den Prototyp ehrlich und ohne technische Überladung.
        JPanel panel = transparent(new GridLayout(3, 1, 0, 18)); // Gliedert die Erklärung in drei nachvollziehbare Themen.
        panel.add(infoCard("01  Dein Score, verständlich erklärt", "Jeder Bereich wird von 0 bis 10 bewertet. Schlaf zählt 25 %, Bewegung, Ernährung und Produktivität zählen jeweils 20 %, soziale Kontakte 15 %. Die gewichtete Summe wird mit 10 multipliziert und gerundet.", "Beispiel: 8 · 6 · 7 · 9 · 5 ergibt 72 von 100 Punkten.")); // Erklärt dieselbe Berechnung wie ScoreService.
        panel.add(infoCard("02  Fortschritt, den du tragen kannst", "Pro gespeichertem Tag erhältst du 10 Basis-XP plus den ganzzahligen Anteil von Score ÷ 10. Alle 100 XP steigt dein Level. Bearbeiten ersetzt die bisherigen Tages-XP. Bereits verdiente Avatar-Extras bleiben freigeschaltet.", "Level 2: Schal und Salbeirahmen · Level 3: Waldgrün · Level 4: Sternennacht · Level 5: Goldrahmen · Level 7: Krone. Auf der Avatar-Seite kannst du freie Extras anlegen; dein Look wird automatisch gespeichert.")); // Erklärt echte, dauerhaft nutzbare Belohnungen und ihre Schwellen.
        panel.add(infoCard("03  Lokal. Einfach. Noch ein Prototyp.", "Score, XP und neue Antworten werden lokal als CSV gespeichert. Avatar-Auswahl und Freischaltungen liegen daneben in avatar.properties. Die Dateien sind nicht verschlüsselt; der Schutz hängt vom Betriebssystem und seinen Zugriffsrechten ab.", "Noch offen: Konten, Deutsch/Englisch, Fragenverwaltung und ein einstellbarer Zeitraum für Nachträge. Dein Score ist eine Reflexionshilfe, keine medizinische Bewertung.")); // Beschreibt die nach dem Avatar-Ausbau verbleibenden Grenzen.
        return panel; // Gibt die Erklärungsseite zurück.
    } // Beendet die Info-Seite.

    private JPanel infoCard(String title, String description, String detail) { // Erzeugt eine wiederverwendbare Erklärungskarte.
        JPanel panel = card(new BorderLayout(0, 12)); // Trennt Titel, Erklärung und Beispiel.
        panel.add(label(title, 22, INK), BorderLayout.NORTH); // Hebt das Thema hervor.
        JTextArea text = paragraph(description); // Verwendet umbrechenden, auswählbaren Text.
        panel.add(text, BorderLayout.CENTER); // Gibt der Erklärung die volle Breite.
        JTextArea note = paragraph(detail); // Verwendet dieselbe lesbare Darstellung für Details.
        note.setForeground(MUTED); // Ordnet Beispiele und Grenzen visuell unter.
        panel.add(note, BorderLayout.SOUTH); // Platziert die Zusatzinformation unter der Erklärung.
        return panel; // Gibt die fertige Karte zurück.
    } // Beendet die Kartenhilfe.

    private JTextArea paragraph(String text) { // Ermöglicht lesbare Erklärungstexte ohne feste HTML-Breiten.
        JTextArea area = new JTextArea(text); // Hält Text markierbar und kopierbar.
        area.setLineWrap(true); // Passt den Text an die verfügbare Breite an.
        area.setWrapStyleWord(true); // Bricht ausschließlich an Wortgrenzen um.
        area.setEditable(false); // Verhindert Bearbeiten der Erklärung.
        area.setOpaque(false); // Verwendet den Kartenhintergrund.
        area.setForeground(INK); // Nutzt die gemeinsame Textfarbe.
        area.setRows(3); // Reserviert eine angenehm lesbare Mindesthöhe.
        return area; // Gibt den Textabsatz zurück.
    } // Beendet die Absatzhilfe.

    private JPanel statistic(JLabel number, String caption) { // Verbindet jede Kennzahl mit ihrer Bedeutung.
        JPanel panel = transparent(new BorderLayout(0, 6)); // Ordnet Zahl über Beschriftung an.
        panel.add(number, BorderLayout.CENTER); // Hebt den Wert hervor.
        panel.add(label(caption, 13, MUTED), BorderLayout.SOUTH); // Erklärt die Kennzahl direkt darunter.
        return panel; // Gibt die Kennzahlengruppe zurück.
    } // Beendet die Kennzahlenhilfe.

    private JScrollPane scroll(JPanel panel) { // Ermöglicht Nutzung bei kleinen Fensterhöhen.
        JPanel wrapper = new WidthTrackingPanel(); // Passt Inhalte an die Fensterbreite an, ohne horizontales Scrollen zu erzwingen.
        wrapper.setBackground(BACKGROUND); // Hält den freien Bereich unter kurzen Seiten in derselben Hintergrundfarbe.
        wrapper.add(panel, BorderLayout.NORTH); // Bewahrt die natürliche Inhaltshöhe statt Inhalte zu verzerren.
        JScrollPane scroll = new JScrollPane(wrapper); // Macht überlange Seiten scrollbar.
        scroll.setBorder(null); // Verhindert einen zusätzlichen Rahmen um die gesamte Seite.
        scroll.getViewport().setBackground(BACKGROUND); // Hält auch den freien Platz unter kurzen Seiten in der gemeinsamen Hintergrundfarbe.
        scroll.getVerticalScrollBar().setUnitIncrement(18); // Sorgt für angenehmes Scrollen mit Maus oder Trackpad.
        return scroll; // Gibt die umschlossene Seite zurück.
    } // Beendet die Scrollhilfe.

    private static final class WidthTrackingPanel extends JPanel implements Scrollable { // Definiert das Verhalten aller scrollbar eingebetteten Seiten.
        WidthTrackingPanel() { super(new BorderLayout()); } // Behält den Inhalt am oberen Rand.
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); } // Nutzt die natürliche Inhaltshöhe.
        @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return 18; } // Scrollt in kleinen, gut lesbaren Schritten.
        @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) { return Math.max(18, visible.height - 36); } // Behält beim seitenweisen Scrollen etwas Kontext.
        @Override public boolean getScrollableTracksViewportWidth() { return true; } // Verhindert seitlich abgeschnittene Formulare bei Mindestbreite.
        @Override public boolean getScrollableTracksViewportHeight() { return false; } // Ermöglicht vertikales Scrollen bei kleinen Fenstern.
    } // Beendet das anpassungsfähige Seitenpanel.

    private void showPage(int index) { // Wechselt Ansicht und Orientierung gemeinsam.
        String[] titles = {"Dein Überblick", "Wie war dein Tag?", "Dein Verlauf", "Dein Avatar. Dein Stil.", "So funktioniert Life Score"}; // Benennt alle fünf Hauptseiten.
        String[] subtitles = {"Kleine Schritte. Ein bewussterer Alltag.", "Fünf Fragen. Ein Moment nur für dich.", "Erkenne Entwicklungen und schau auf deine Tage zurück.", "Gestalte deinen Begleiter und entdecke deine Belohnungen.", "Transparente Werte statt einer undurchsichtigen Zahl."}; // Erklärt den jeweiligen Nutzen.
        pageTitle.setText(titles[index]); // Aktualisiert die Seitenüberschrift.
        pageSubtitle.setText(subtitles[index]); // Aktualisiert die Seitenbeschreibung.
        pages.show(pageContainer, Integer.toString(index)); // Bringt die gewählte Ansicht nach vorne.
        for (int i = 0; i < navigation.length; i++) { // Aktualisiert den sichtbaren Navigationszustand.
            navigation[i].setBackground(i == index ? PALE : INK); // Hebt nur die aktive Seite hell hervor.
            navigation[i].setForeground(i == index ? INK : new Color(210, 225, 213)); // Sichert auf beiden Flächen ausreichend Kontrast.
        } // Beendet die Auswahlmarkierung.
    } // Beendet den Seitenwechsel.

    private LocalDate selectedDate() { // Liest das Kalenderdatum aus dem Swing-Feld.
        return ((Date) dateSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); // Verwendet die lokale Zeitzone des Nutzers.
    } // Beendet die Datumsumwandlung.

    private void openDate(LocalDate date) { // Öffnet einen bestimmten Tag aus Dashboard oder Tabelle.
        dateSpinner.setValue(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())); // Aktualisiert das Datum und löst das Laden aus.
        loadSelectedDate(); // Lädt auch dann neu, wenn das Datum unverändert ist.
        showPage(1); // Zeigt den zugehörigen Fragebogen.
    } // Beendet das Öffnen eines Tages.

    private void loadSelectedDate() { // Füllt das Formular mit den Antworten des ausgewählten Tages.
        if (ratings[0] == null) { return; } // Wartet beim initialen Aufbau auf die Eingabekomponenten.
        LocalDate date = selectedDate(); // Liest das ausgewählte Datum einmal.
        loadedDate = date; // Merkt sich den Kalendertag, zu dem die geladenen Antworten gehören.
        DailyEntry existing = entries.stream().filter(entry -> entry.date().equals(date)).findFirst().orElse(null); // Sucht genau diesen Tag.
        DailyAnswers answers = existing == null ? null : existing.answers(); // Unterscheidet gespeicherte Bewertungen von alten Score-only-Daten.
        int[] values = answers == null ? new int[]{5, 5, 5, 5, 5} : new int[]{answers.sleep(), answers.movement(), answers.nutrition(), answers.productivity(), answers.social()}; // Nutzt neutrale Startwerte nur dort, wo keine Antworten existieren.
        loadingAnswers = true; // Verhindert unnötige Vorschauänderungen während der Zuweisung.
        for (int i = 0; i < ratings.length; i++) { ratings[i].setValue(values[i]); } // Lädt alle fünf Bewertungen gemeinsam.
        loadingAnswers = false; // Aktiviert die normale Eingabereaktion wieder.
        entryHint.setText(existing == null ? "Neuer Eintrag · Die Startwerte 5 kannst du frei anpassen." : answers == null ? "Älterer Eintrag ohne Antworten · Bitte alle fünf Bereiche neu bewerten." : "Gespeicherte Antworten geladen · Änderungen ersetzen diesen Tag."); // Macht den Datenzustand verständlich.
        saveStatus.setText("Noch nicht gespeichert"); // Kennzeichnet die Vorschau als ungespeichert.
        if (existing != null && answers != null) { saveStatus.setText("Gespeicherten Stand geladen"); } // Unterscheidet echte alte Antworten von Standardwerten.
        saveButton.setText(existing == null ? "Check-in speichern" : "Tag aktualisieren"); // Benennt die tatsächliche Speicheroperation.
        saveButton.setEnabled(storageAvailable && !date.isAfter(LocalDate.now())); // Verhindert zukünftige Check-ins bereits in der Oberfläche.
        if (date.isAfter(LocalDate.now())) { entryHint.setText("Bitte wähle heute oder einen vergangenen Tag."); } // Erklärt die deaktivierte Aktion.
        updatePreview(); // Berechnet die Vorschau erst aus dem vollständigen Formular.
    } // Beendet das Laden der Antworten.

    private DailyAnswers answers() { // Fasst die Formularwerte zu einem validierten Datensatz zusammen.
        return new DailyAnswers(ratings[0].getValue(), ratings[1].getValue(), ratings[2].getValue(), ratings[3].getValue(), ratings[4].getValue()); // Liest ausschließlich ganzzahlige Skalenwerte.
    } // Beendet das Lesen der Antworten.

    private int score(DailyAnswers answers) { // Delegiert die fachliche Berechnung an den Service.
        return scoreService.calculateScore(answers.sleep(), answers.movement(), answers.nutrition(), answers.productivity(), answers.social()); // Vermeidet eine zweite Berechnungsformel in der UI.
    } // Beendet die Berechnungsdelegation.

    private void updatePreview() { // Zeigt die Wirkung des noch nicht gespeicherten Fragebogens.
        previewLabel.setText("Vorschau: " + score(answers()) + " / 100"); // Verwechselt die Vorschau nicht mit dem zuletzt gespeicherten Score.
    } // Beendet die Vorschauaktualisierung.

    private void saveQuestionnaire() { // Validiert Datum und speichert den vollständigen Tagesdatensatz.
        try { // Behandelt Eingabe- und Speicherfehler zentral.
            LocalDate beforeCommit = selectedDate(); // Merkt sich das Datum, zu dem die sichtbaren Antworten gehören.
            dateSpinner.commitEdit(); // Übernimmt auch ein manuell eingegebenes, noch nicht bestätigtes Datum.
            LocalDate date = selectedDate(); // Liest ausschließlich den validierten Feldwert.
            if (!beforeCommit.equals(date)) { // Ein Datumswechsel lädt andere Antworten und braucht deshalb eine erneute Prüfung durch den Nutzer.
                saveStatus.setText("Datum übernommen. Bitte Antworten prüfen und erneut speichern."); // Verhindert versehentliches Speichern eines anderen Tages.
                return; // Speichert nach einem solchen Datumswechsel noch nichts.
            } // Beendet die Datumswechselprüfung.
            if (date.isAfter(LocalDate.now())) { throw new IllegalArgumentException("Ein Check-in ist nur für heute oder vergangene Tage möglich."); } // Prüft die Regel auch unabhängig vom deaktivierten Knopf.
            DailyAnswers answers = answers(); // Liest und validiert alle fünf Eingaben.
            int score = score(answers); // Berechnet den Score mit der bestehenden Fachlogik.
            repository.saveOrReplace(new DailyEntry(date, score, scoreService.earnedXpFor(score), answers)); // Speichert Ergebnisse und Antworten gemeinsam.
            refreshFromStorage(); // Übernimmt ausschließlich den tatsächlich gespeicherten Stand.
            loadSelectedDate(); // Aktualisiert den Bearbeitungsstatus des Formulars.
            saveStatus.setText("Gespeichert · " + date.format(DATE) + " · " + score + " Punkte"); // Bestätigt Datum und Ergebnis direkt bei der Aktion.
            if (!newRewards.isEmpty()) { saveStatus.setText("<html>Gespeichert · " + score + " Punkte<br>Neue Extras! Unter Avatar ansehen.</html>"); } // Macht neue Belohnungen direkt nach dem verdienten Levelaufstieg sichtbar.
        } catch (ParseException exception) { // Erkennt ungültige manuelle Datumsangaben.
            showError(new IllegalArgumentException("Bitte ein gültiges Datum im Format TT.MM.JJJJ eingeben.")); // Nennt das erwartete Format.
        } catch (RuntimeException exception) { // Erkennt Validierungs-, Lese- und Schreibfehler.
            showError(exception); // Bestätigt bei Fehlern niemals fälschlich eine Speicherung.
        } // Beendet die Fehlerbehandlung.
    } // Beendet das Speichern des Check-ins.

    private void refreshFromStorage() { // Aktualisiert alle Ansichten aus demselben gespeicherten Stand.
        entries = repository.loadAll(); // Übernimmt den Datenstand erst nach erfolgreichem Laden.
        storageAvailable = true; // Gibt Speichern nach erfolgreichem Start-Lesezugriff frei.
        historyModel.setRowCount(0); // Entfernt veraltete Tabellenzeilen.
        for (int i = entries.size() - 1; i >= 0; i--) { // Zeigt den neuesten Tag zuerst.
            DailyEntry entry = entries.get(i); // Liest den nächsten Eintrag.
            historyModel.addRow(new Object[]{entry.date().format(DATE), entry.score() + " / 100", "+" + entry.earnedXp(), entry.answers() == null ? "Nicht vorhanden (Altbestand)" : "5 von 5 gespeichert"}); // Macht fehlende alte Antworten ausdrücklich sichtbar.
        } // Beendet die Tabellenaktualisierung.
        int totalXp = scoreService.totalXp(entries); // Summiert die aktuell gespeicherten XP ohne Duplikate.
        int level = scoreService.levelFor(totalXp); // Leitet das Level aus dem Gesamtergebnis ab.
        levelLabel.setText("Level " + level); // Aktualisiert das sichtbare Level.
        xpLabel.setText("<html>" + totalXp + " XP insgesamt · Noch " + (100 - totalXp % 100) + " XP<br>bis Level " + (level + 1) + "</html>"); // Hält den nächsten Meilenstein auch in schmalen Karten vollständig sichtbar.
        levelBar.setValue(totalXp % 100); // Zeigt den Fortschritt im aktuellen Level.
        levelBar.setString((totalXp % 100) + " / 100 XP"); // Beschriftet den Balken verständlich.
        newRewards = avatarStudio.updateProgress(level, totalXp); // Schaltet erreichte Belohnungen tatsächlich frei und speichert sie dauerhaft.
        rewardLabel.setText(avatarStudio.milestoneText()); // Zeigt das nächste noch nicht verdiente Extra statt eines wirkungslosen Belohnungstexts.
        countLabel.setText(Integer.toString(entries.size())); // Zählt gespeicherte Tage statt Speicheroperationen.
        averageLabel.setText(entries.isEmpty() ? "–" : Long.toString(Math.round(entries.stream().mapToInt(DailyEntry::score).average().orElse(0)))); // Bildet den gerundeten Durchschnitt aller Einträge.
        DailyEntry latest = entries.isEmpty() ? null : entries.get(entries.size() - 1); // Unterscheidet echte Ergebnisse von einem leeren Start.
        scoreRing.setScore(latest == null ? null : latest.score()); // Aktualisiert den Ring samt zugänglichem Namen.
        scoreDate.setText(latest == null ? "Dein erster Check-in wartet auf dich." : "Gespeichert am " + latest.date().format(DATE)); // Nennt immer das Datum des dargestellten Scores.
        boolean todayDone = entries.stream().anyMatch(entry -> entry.date().equals(LocalDate.now())); // Prüft, ob heute tatsächlich erfasst wurde.
        welcomeLabel.setText(todayDone ? "Dein heutiger Moment ist festgehalten." : "Nimm dir einen Moment für dich."); // Passt den Einstieg an den Tagesstatus an.
        welcomeDetail.setText(todayDone ? "Du kannst deine Antworten jederzeit noch anpassen." : "Fünf Fragen helfen dir, deinen Tag einzuordnen."); // Gibt einen sinnvollen nächsten Schritt.
        checkInButton.setText(todayDone ? "Heutigen Check-in ansehen  →" : "Heutigen Check-in starten  →"); // Vermeidet Aufforderungen zu doppelten Einträgen.
        dashboardTrend.setEntries(entries); // Aktualisiert die Übersichtsgrafik.
        historyTrend.setEntries(entries); // Aktualisiert die Verlaufsgrafik aus denselben Daten.
    } // Beendet die Aktualisierung aller Ansichten.

    private void showError(RuntimeException exception) { // Zeigt verständliche Fehler mit einheitlichem Titel.
        JOptionPane.showMessageDialog(this, exception.getMessage(), "Life Score · Bitte prüfen", JOptionPane.ERROR_MESSAGE); // Hält technische Stacktraces aus dem Nutzerfluss heraus.
    } // Beendet die Fehlermeldung.
} // Beendet das Hauptfenster.
