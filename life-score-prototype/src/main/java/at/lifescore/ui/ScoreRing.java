package at.lifescore.ui; // Ordnet den Ring dem UI-Paket zu.

import java.awt.BasicStroke; // Legt Strichstärke und Endform fest.
import java.awt.Dimension; // Legt Komponentengrößen fest.
import java.awt.FontMetrics; // Misst Texte für die Zentrierung.
import java.awt.Font; // Stellt Schriftstile bereit.
import java.awt.Graphics; // Stellt die Zeichenfläche bereit.
import java.awt.Graphics2D; // Ermöglicht erweiterte Zeichenfunktionen.
import java.awt.geom.Arc2D; // Beschreibt einen Kreisbogen.
import java.awt.geom.Ellipse2D; // Beschreibt einen Kreis.
import javax.swing.JComponent; // Dient als Basis der gezeichneten Komponente.
import javax.swing.Timer; // Treibt die Animation des Rings an.

final class ScoreRing extends JComponent { // Zeigt einen Score als animierten Kreisring mit Zahl in der Mitte.
    private int target = -1; // Hält den Zielwert; minus eins bedeutet "noch kein Wert".
    private double shown = 0; // Hält den aktuell animierten Anzeigewert.
    private String caption = "von 100"; // Hält den kleinen Text unter der Zahl.
    private final Timer timer = new Timer(16, event -> step()); // Erzeugt den Animationstakt von etwa 60 Bildern pro Sekunde.

    ScoreRing(int size) { // Erzeugt einen quadratischen Ring der gewünschten Größe.
        Dimension dimension = new Dimension(size, size); // Legt die quadratische Größe fest.
        setPreferredSize(dimension); // Setzt die bevorzugte Größe.
        setMinimumSize(dimension); // Verhindert ein Schrumpfen unter diese Größe.
        setMaximumSize(dimension); // Verhindert ein Strecken über diese Größe.
    } // Beendet den Konstruktor.

    void setScore(int score, boolean animate) { // Setzt den Zielwert, optional mit weicher Animation.
        target = score; // Übernimmt den neuen Zielwert.
        if (!animate || score < 0) { // Prüft, ob direkt ohne Animation gesetzt werden soll.
            shown = Math.max(score, 0); // Springt sofort auf den Zielwert.
            timer.stop(); // Beendet eine eventuell laufende Animation.
            repaint(); // Zeichnet den Ring neu.
            return; // Beendet die Methode ohne Animation.
        } // Beendet den Sonderfall ohne Animation.
        timer.start(); // Startet die Animation zum Zielwert.
    } // Beendet das Setzen des Scores.

    void replay() { // Spielt die Animation von null erneut ab.
        if (target >= 0) { // Prüft, ob überhaupt ein Wert vorhanden ist.
            shown = 0; // Setzt die Anzeige auf null zurück.
            timer.start(); // Startet die Animation neu.
        } // Beendet die Prüfung auf einen vorhandenen Wert.
    } // Beendet das erneute Abspielen.

    void setCaption(String text) { // Ändert den kleinen Text unter der Zahl.
        caption = text; // Übernimmt den neuen Text.
        repaint(); // Zeichnet den Ring neu.
    } // Beendet das Setzen der Beschriftung.

    private void step() { // Führt einen Animationsschritt aus.
        double diff = target - shown; // Berechnet den verbleibenden Abstand zum Ziel.
        if (Math.abs(diff) < 0.4) { // Prüft, ob das Ziel praktisch erreicht ist.
            shown = target; // Rastet exakt auf dem Zielwert ein.
            timer.stop(); // Beendet die Animation.
        } else { // Behandelt den noch laufenden Fall.
            shown += diff * 0.2; // Nähert sich weich dem Ziel an.
        } // Beendet die Fallunterscheidung.
        repaint(); // Zeichnet den neuen Zwischenstand.
    } // Beendet den Animationsschritt.

    @Override // Ersetzt das Standardzeichnen der Komponente.
    protected void paintComponent(Graphics graphics) { // Zeichnet Ring, Zahl und Beschriftung.
        Graphics2D g2 = (Graphics2D) graphics.create(); // Erstellt eine private Kopie der Zeichenfläche.
        Theme.antialias(g2); // Aktiviert glatte Kanten.
        int size = Math.min(getWidth(), getHeight()); // Bestimmt die Seitenlänge des Quadrats.
        float stroke = size * 0.085f; // Leitet die Ringdicke aus der Größe ab.
        double x = (getWidth() - size) / 2.0 + stroke / 2; // Berechnet die linke Kante des Rings.
        double y = (getHeight() - size) / 2.0 + stroke / 2; // Berechnet die obere Kante des Rings.
        double diameter = size - stroke; // Berechnet den Durchmesser der Ringmitte.
        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); // Zeichnet mit runden Enden.
        g2.setColor(Theme.LINE); // Wählt die Farbe der leeren Spur.
        g2.draw(new Ellipse2D.Double(x, y, diameter, diameter)); // Zeichnet die leere Spur.
        int rounded = (int) Math.round(shown); // Rundet den Anzeigewert für Farbe und Text.
        if (target >= 0 && shown > 0.5) { // Prüft, ob ein sichtbarer Bogen nötig ist.
            g2.setColor(Theme.scoreColor(rounded)); // Wählt die Ampelfarbe des Werts.
            g2.draw(new Arc2D.Double(x, y, diameter, diameter, 90, -360.0 * shown / 100.0, Arc2D.OPEN)); // Zeichnet den Fortschrittsbogen im Uhrzeigersinn.
        } // Beendet das Zeichnen des Bogens.
        String main = target < 0 ? "–" : String.valueOf(rounded); // Wählt den großen Text in der Mitte.
        String sub = target < 0 ? "kein Wert" : caption; // Wählt den kleinen Text darunter.
        g2.setColor(Theme.TEXT); // Wählt die Textfarbe der Zahl.
        g2.setFont(Theme.font(Font.BOLD, size * 0.30f)); // Wählt die große Schrift.
        FontMetrics big = g2.getFontMetrics(); // Misst die große Schrift.
        int centerY = getHeight() / 2; // Bestimmt die vertikale Mitte.
        g2.drawString(main, (getWidth() - big.stringWidth(main)) / 2, centerY + big.getAscent() / 2 - big.getDescent() / 2); // Zeichnet die zentrierte Zahl.
        g2.setColor(Theme.MUTED); // Wählt die Farbe des Nebentexts.
        g2.setFont(Theme.font(Font.PLAIN, size * 0.075f)); // Wählt die kleine Schrift.
        FontMetrics small = g2.getFontMetrics(); // Misst die kleine Schrift.
        g2.drawString(sub, (getWidth() - small.stringWidth(sub)) / 2, centerY + big.getAscent() / 2 + small.getAscent() + 2); // Zeichnet die zentrierte Beschriftung unter der Zahl.
        g2.dispose(); // Gibt die Kopie der Zeichenfläche frei.
    } // Beendet das Zeichnen des Rings.
} // Beendet die Klasse ScoreRing.
