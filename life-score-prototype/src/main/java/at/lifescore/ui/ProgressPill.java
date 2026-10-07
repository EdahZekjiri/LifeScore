package at.lifescore.ui; // Ordnet den Fortschrittsbalken dem UI-Paket zu.

import java.awt.Dimension; // Legt Komponentengrößen fest.
import java.awt.Graphics; // Stellt die Zeichenfläche bereit.
import java.awt.Graphics2D; // Ermöglicht erweiterte Zeichenfunktionen.
import java.awt.geom.RoundRectangle2D; // Beschreibt abgerundete Balken.
import javax.swing.JComponent; // Dient als Basis der gezeichneten Komponente.

final class ProgressPill extends JComponent { // Zeigt einen schlanken, abgerundeten Fortschrittsbalken.
    private double fraction = 0; // Hält den Füllstand zwischen null und eins.

    ProgressPill() { // Erzeugt den Balken mit fester Höhe.
        setPreferredSize(new Dimension(100, 12)); // Legt die bevorzugte Größe fest.
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 12)); // Erlaubt volle Breite bei fester Höhe.
    } // Beendet den Konstruktor.

    void setFraction(double value) { // Setzt den Füllstand.
        fraction = Math.max(0, Math.min(1, value)); // Begrenzt den Wert auf den gültigen Bereich.
        repaint(); // Zeichnet den Balken neu.
    } // Beendet das Setzen des Füllstands.

    @Override // Ersetzt das Standardzeichnen der Komponente.
    protected void paintComponent(Graphics graphics) { // Zeichnet Spur und Füllung.
        Graphics2D g2 = (Graphics2D) graphics.create(); // Erstellt eine private Kopie der Zeichenfläche.
        Theme.antialias(g2); // Aktiviert glatte Kanten.
        g2.setColor(Theme.LINE); // Wählt die Farbe der leeren Spur.
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight())); // Zeichnet die leere Spur.
        if (fraction > 0) { // Prüft, ob etwas gefüllt werden muss.
            double width = Math.max(getHeight(), getWidth() * fraction); // Stellt sicher, dass die Füllung mindestens rund ist.
            g2.setColor(Theme.ACCENT); // Wählt die Akzentfarbe.
            g2.fill(new RoundRectangle2D.Double(0, 0, width, getHeight(), getHeight(), getHeight())); // Zeichnet die Füllung.
        } // Beendet das Zeichnen der Füllung.
        g2.dispose(); // Gibt die Kopie der Zeichenfläche frei.
    } // Beendet das Zeichnen des Balkens.
} // Beendet die Klasse ProgressPill.
