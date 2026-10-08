package at.lifescore.ui; // Enthält ausschließlich visuelle Darstellungen vorhandener Daten.

import at.lifescore.model.DailyEntry; // Liefert Datum und Score für das Diagramm.
import java.awt.*; // Stellt die Zeichenoperationen bereit.
import java.awt.geom.Path2D; // Verbindet die Messpunkte zu einem Verlauf.
import java.time.temporal.ChronoUnit; // Berechnet echte Zeitabstände statt gleichmäßiger fiktiver Tage.
import java.time.format.DateTimeFormatter; // Formatiert die Diagrammachse.
import java.util.List; // Hält die dargestellten Datensätze.
import javax.swing.JPanel; // Integriert die Zeichnungen in Swing.

final class ScoreVisuals { // Kapselt die beiden kleinen Diagramm-Komponenten.
    private ScoreVisuals() { } // Verhindert unnötige Instanzen.

    static final class Ring extends JPanel { // Zeigt den letzten gespeicherten Score als Ring.
        private Integer score; // Null bedeutet „noch kein Eintrag“, nicht einen Score von null.
        Ring() { // Legt die Ausgangsgröße fest.
            setOpaque(false); // Verwendet den Hintergrund der umgebenden Karte.
            setPreferredSize(new Dimension(220, 155)); // Lässt auch unter dem Score genug Platz für die Verlaufsgrafik.
        } // Beendet den Aufbau.
        void setScore(Integer score) { // Übernimmt einen tatsächlich gespeicherten Wert.
            this.score = score; // Merkt sich auch den expliziten Leerzustand.
            getAccessibleContext().setAccessibleName(score == null ? "Noch kein Life Score" : "Life Score: " + score + " von 100"); // Macht den gezeichneten Wert für Hilfstechnologien verfügbar.
            repaint(); // Löst die neue Darstellung aus.
        } // Beendet die Aktualisierung.
        @Override protected void paintComponent(Graphics graphics) { // Berechnet die Ringgröße aus dem aktuellen Platz.
            super.paintComponent(graphics); // Beachtet den normalen Swing-Zeichenablauf.
            Graphics2D g = canvas(graphics); // Aktiviert geglättete Linien und Texte.
            int diameter = Math.min(getWidth() - 28, getHeight() - 28); // Lässt Platz für die Linienbreite.
            int x = (getWidth() - diameter) / 2, y = (getHeight() - diameter) / 2; // Zentriert den Ring.
            g.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); // Erzeugt einen weichen Fortschrittsbogen.
            g.setColor(UiTheme.PALE); // Zeichnet zuerst den vollständigen Hintergrundring.
            g.drawOval(x, y, diameter, diameter); // Stellt den gesamten Wertebereich dar.
            g.setColor(UiTheme.GREEN); // Hebt den erreichten Anteil hervor.
            g.drawArc(x, y, diameter, diameter, 90, score == null ? 0 : -(int) Math.round(score * 3.6)); // Bildet 0–100 auf 0–360 Grad ab.
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 49)); // Macht den Score zum visuellen Mittelpunkt.
            g.setColor(UiTheme.INK); // Verwendet die Haupttextfarbe.
            centered(g, score == null ? "–" : score.toString(), getWidth() / 2, getHeight() / 2 + 10); // Zeigt für fehlende Daten einen Strich.
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); // Ordnet die Skala unterhalb der Zahl ein.
            g.setColor(UiTheme.MUTED); // Kennzeichnet die Skala als Zusatzinformation.
            centered(g, "VON 100 PUNKTEN", getWidth() / 2, getHeight() / 2 + 38); // Vermeidet unbeschriftete Kennzahlen.
            g.dispose(); // Gibt den Zeichenkontext frei.
        } // Beendet die Ringzeichnung.
    } // Beendet die Score-Komponente.

    static final class Trend extends JPanel { // Zeigt maximal die letzten 14 gespeicherten Tage.
        private List<DailyEntry> entries = List.of(); // Beginnt ohne erfundene Beispieldaten.
        Trend() { // Definiert eine kompakte Standardhöhe.
            setOpaque(false); // Nutzt die weiße Kartenfläche.
            setPreferredSize(new Dimension(460, 155)); // Passt auf Dashboard und Verlaufsseite.
        } // Beendet den Aufbau.
        void setEntries(List<DailyEntry> all) { // Reduziert lange Historien auf einen lesbaren Ausschnitt.
            entries = List.copyOf(all.subList(Math.max(0, all.size() - 14), all.size())); // Nimmt die letzten 14 tatsächlich gespeicherten Einträge.
            getAccessibleContext().setAccessibleName("Score-Verlauf; genaue Werte stehen in der Verlaufstabelle."); // Verweist auf die zugängliche tabellarische Alternative.
            repaint(); // Zeichnet nach jeder Speicherung erneut.
        } // Beendet die Datenübernahme.
        @Override protected void paintComponent(Graphics graphics) { // Zeichnet Achsen, Messpunkte und Verbindungslinien.
            super.paintComponent(graphics); // Beachtet die Swing-Zeichenreihenfolge.
            Graphics2D g = canvas(graphics); // Aktiviert Kantenglättung.
            int left = 35, right = getWidth() - 24, top = 16, bottom = getHeight() - 32; // Hält Platz für Achsenbeschriftungen frei.
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11)); // Hält Hilfstexte kompakt.
            for (int tick : new int[]{0, 50, 100}) { // Zeigt eine feste Skala für vergleichbare Verläufe.
                int y = bottom - (bottom - top) * tick / 100; // Bildet jeden Skalenwert auf seine Höhe ab.
                g.setColor(UiTheme.LINE); // Zeichnet unaufdringliche Hilfslinien.
                g.drawLine(left, y, right, y); // Führt die Linie über die verfügbare Breite.
                g.setColor(UiTheme.MUTED); // Hebt die Skalenwerte ausreichend hervor.
                g.drawString(Integer.toString(tick), 0, y + 4); // Beschriftet die vertikale Achse.
            } // Beendet die Achsenzeichnung.
            if (entries.isEmpty()) { // Vermeidet eine irreführende Nulllinie ohne Daten.
                centered(g, "Dein Verlauf beginnt mit dem ersten Check-in.", getWidth() / 2, (top + bottom) / 2); // Erklärt den leeren Zustand.
                g.dispose(); // Gibt den Kontext auch beim frühen Rücksprung frei.
                return; // Zeichnet keine fiktiven Punkte.
            } // Beendet die Leerprüfung.
            long span = ChronoUnit.DAYS.between(entries.get(0).date(), entries.get(entries.size() - 1).date()); // Bestimmt die tatsächliche Länge des Zeitraums.
            Path2D path = new Path2D.Double(); // Sammelt alle Punkte für die Verbindungslinie.
            for (int i = 0; i < entries.size(); i++) { // Durchläuft die ausgewählten Einträge chronologisch.
                DailyEntry entry = entries.get(i); // Liest den jeweiligen Tageswert.
                int x = span == 0 ? (left + right) / 2 : left + (int) ((right - left) * ChronoUnit.DAYS.between(entries.get(0).date(), entry.date()) / span); // Positioniert nach Datum; fehlende Tage bleiben als Abstand sichtbar.
                int y = bottom - (bottom - top) * entry.score() / 100; // Positioniert den Score auf der festen Skala.
                if (i == 0) { path.moveTo(x, y); } else { path.lineTo(x, y); } // Verbindet nur vorhandene Messpunkte.
                g.setColor(UiTheme.GREEN); // Kennzeichnet reale Datenpunkte.
                g.fillOval(x - 4, y - 4, 8, 8); // Macht auch einen einzelnen Eintrag sichtbar.
            } // Beendet die Punktezeichnung.
            g.setStroke(new BasicStroke(2.5f)); // Zeichnet eine klar erkennbare Verlaufslinie.
            g.draw(path); // Die Linie verbindet Einträge und stellt keine Messung fehlender Tage dar.
            g.setColor(UiTheme.MUTED); // Verwendet sekundäre Farbe für die Zeitachse.
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yy"); // Bezieht das Jahr bei längeren Zeiträumen ein.
            g.drawString(entries.get(0).date().format(format), left, getHeight() - 8); // Beschriftet den Beginn.
            if (entries.size() > 1) { // Verhindert doppelte Beschriftung bei einem einzelnen Tag.
                String end = entries.get(entries.size() - 1).date().format(format); // Formatiert das Enddatum.
                g.drawString(end, right - g.getFontMetrics().stringWidth(end), getHeight() - 8); // Richtet das Enddatum rechtsbündig aus.
            } // Beendet die Zeitachse.
            g.dispose(); // Gibt den Zeichenkontext frei.
        } // Beendet die Verlaufszeichnung.
    } // Beendet das Verlaufsdiagramm.

    private static Graphics2D canvas(Graphics graphics) { // Erstellt einen unabhängig konfigurierbaren Grafikkontext.
        Graphics2D g = (Graphics2D) graphics.create(); // Verändert nicht den Kontext anderer Komponenten.
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Glättet Linien und Formen.
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // Glättet Diagrammtexte.
        return g; // Liefert den vorbereiteten Kontext.
    } // Beendet die Zeichenhilfe.
    private static void centered(Graphics2D g, String text, int x, int y) { // Zentriert Text anhand seiner tatsächlichen Schriftbreite.
        g.drawString(text, x - g.getFontMetrics().stringWidth(text) / 2, y); // Funktioniert auch bei anderen Systemschriften.
    } // Beendet die Texthilfe.
} // Beendet die visuellen Komponenten.
