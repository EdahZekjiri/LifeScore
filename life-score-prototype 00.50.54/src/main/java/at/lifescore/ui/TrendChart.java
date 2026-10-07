package at.lifescore.ui; // Ordnet das Diagramm dem UI-Paket zu.

import at.lifescore.model.DailyEntry; // Macht die darzustellenden Tageswerte verfügbar.
import java.awt.BasicStroke; // Legt die Strichstärke der Hilfslinien fest.
import java.awt.FontMetrics; // Misst Texte für die Zentrierung.
import java.awt.Font; // Stellt Schriftstile bereit.
import java.awt.Graphics; // Stellt die Zeichenfläche bereit.
import java.awt.Graphics2D; // Ermöglicht erweiterte Zeichenfunktionen.
import java.awt.geom.RoundRectangle2D; // Beschreibt Balken mit runden Ecken.
import java.time.format.DateTimeFormatter; // Formatiert die Datumsbeschriftung.
import java.util.List; // Stellt die Eintragsliste bereit.
import javax.swing.JComponent; // Dient als Basis der gezeichneten Komponente.

final class TrendChart extends JComponent { // Zeigt die letzten Scores als Balkendiagramm.
    private static final int SLOTS = 7; // Legt fest, wie viele Tage maximal nebeneinander stehen.
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd.MM."); // Legt das kurze Datumsformat der Achse fest.
    private List<DailyEntry> entries = List.of(); // Hält die aktuell dargestellten Einträge.

    void setEntries(List<DailyEntry> newEntries) { // Übernimmt neue Einträge und zeichnet neu.
        entries = newEntries; // Speichert die darzustellenden Einträge.
        repaint(); // Zeichnet das Diagramm neu.
    } // Beendet das Setzen der Einträge.

    @Override // Ersetzt das Standardzeichnen der Komponente.
    protected void paintComponent(Graphics graphics) { // Zeichnet Raster, Balken und Beschriftungen.
        Graphics2D g2 = (Graphics2D) graphics.create(); // Erstellt eine private Kopie der Zeichenfläche.
        Theme.antialias(g2); // Aktiviert glatte Kanten.
        int left = 34; // Reserviert Platz für die Zahlen der Achse.
        int top = 22; // Reserviert Platz für Werte über den Balken.
        int bottom = 24; // Reserviert Platz für die Datumsbeschriftung.
        int chartHeight = getHeight() - top - bottom; // Berechnet die nutzbare Diagrammhöhe.
        g2.setFont(Theme.font(Font.PLAIN, 12f)); // Wählt die kleine Beschriftungsschrift.
        FontMetrics metrics = g2.getFontMetrics(); // Misst die Beschriftungsschrift.
        for (int value : new int[]{0, 50, 100}) { // Durchläuft die drei Hilfslinien.
            int y = top + (int) Math.round(chartHeight * (1 - value / 100.0)); // Berechnet die Höhe der Hilfslinie.
            g2.setColor(Theme.LINE); // Wählt die Linienfarbe.
            g2.setStroke(new BasicStroke(1f)); // Wählt eine feine Linie.
            g2.drawLine(left, y, getWidth(), y); // Zeichnet die Hilfslinie.
            g2.setColor(Theme.MUTED); // Wählt die Textfarbe der Achse.
            g2.drawString(String.valueOf(value), 0, y + metrics.getAscent() / 2 - 2); // Beschriftet die Hilfslinie.
        } // Beendet das Zeichnen der Hilfslinien.
        if (entries.isEmpty()) { // Prüft, ob es noch nichts darzustellen gibt.
            String message = "Noch keine Daten – dein erster Check-in erscheint hier."; // Legt den Hinweistext fest.
            int messageX = left + (getWidth() - left - metrics.stringWidth(message)) / 2; // Berechnet die zentrierte Textposition.
            g2.setColor(Theme.CARD); // Wählt die Kartenfarbe als Untergrund.
            g2.fillRect(messageX - 10, top + chartHeight / 2 - metrics.getAscent() - 4, metrics.stringWidth(message) + 20, metrics.getHeight() + 8); // Verdeckt die Hilfslinie hinter dem Text.
            g2.setColor(Theme.MUTED); // Wählt die Textfarbe.
            g2.drawString(message, left + (getWidth() - left - metrics.stringWidth(message)) / 2, top + chartHeight / 2); // Zeichnet den zentrierten Hinweis.
            g2.dispose(); // Gibt die Kopie der Zeichenfläche frei.
            return; // Beendet das Zeichnen ohne Balken.
        } // Beendet den Sonderfall ohne Daten.
        double slot = (getWidth() - left) / (double) SLOTS; // Berechnet die Breite eines Tagesplatzes.
        double barWidth = Math.min(46, slot * 0.55); // Begrenzt die Balkenbreite.
        for (int i = 0; i < entries.size(); i++) { // Durchläuft alle darzustellenden Einträge.
            DailyEntry entry = entries.get(i); // Wählt den aktuellen Eintrag.
            double x = left + slot * i + (slot - barWidth) / 2; // Berechnet die linke Kante des Balkens.
            double height = Math.max(6, chartHeight * entry.score() / 100.0); // Berechnet die Balkenhöhe mit sichtbarem Minimum.
            double y = top + chartHeight - height; // Berechnet die obere Kante des Balkens.
            g2.setColor(Theme.scoreColor(entry.score())); // Wählt die Ampelfarbe des Scores.
            g2.fill(new RoundRectangle2D.Double(x, y, barWidth, height, 10, 10)); // Zeichnet den abgerundeten Balken.
            String value = String.valueOf(entry.score()); // Erzeugt den Text für den Wert.
            g2.setColor(Theme.TEXT); // Wählt die Textfarbe der Werte.
            g2.drawString(value, (int) (x + (barWidth - metrics.stringWidth(value)) / 2), (int) y - 5); // Zeichnet den Wert über dem Balken.
            String day = entry.date().format(DAY); // Formatiert das Datum des Eintrags.
            g2.setColor(Theme.MUTED); // Wählt die Textfarbe der Datumsangabe.
            g2.drawString(day, (int) (x + (barWidth - metrics.stringWidth(day)) / 2), getHeight() - 6); // Zeichnet das Datum unter dem Balken.
        } // Beendet das Zeichnen der Balken.
        g2.dispose(); // Gibt die Kopie der Zeichenfläche frei.
    } // Beendet das Zeichnen des Diagramms.
} // Beendet die Klasse TrendChart.
