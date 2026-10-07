package at.lifescore.ui; // Ordnet das Slider-Design dem UI-Paket zu.

import java.awt.BasicStroke; // Legt die Strichstärke des Reglerrands fest.
import java.awt.Dimension; // Legt die Reglergröße fest.
import java.awt.Graphics; // Stellt die Zeichenfläche bereit.
import java.awt.Graphics2D; // Ermöglicht erweiterte Zeichenfunktionen.
import javax.swing.JSlider; // Beschreibt den zu gestaltenden Regler.
import javax.swing.plaf.basic.BasicSliderUI; // Dient als Basis für ein eigenes Aussehen.

final class ModernSliderUI extends BasicSliderUI { // Zeichnet Regler mit schlanker Spur und rundem Knopf.
    ModernSliderUI(JSlider slider) { // Erzeugt das Design für einen Regler.
        super(slider); // Übergibt den Regler an die Basisklasse.
    } // Beendet den Konstruktor.

    @Override // Ersetzt die Standardgröße des Reglerknopfs.
    protected Dimension getThumbSize() { // Liefert die Knopfgröße.
        return new Dimension(24, 24); // Legt einen gut greifbaren runden Knopf fest.
    } // Beendet die Größenangabe.

    @Override // Ersetzt das Zeichnen des Fokusrahmens.
    public void paintFocus(Graphics g) { // Zeichnet bewusst nichts.
    } // Beendet das Unterdrücken des Fokusrahmens.

    @Override // Ersetzt das Zeichnen der Spur.
    public void paintTrack(Graphics g) { // Zeichnet leere und gefüllte Spur.
        Graphics2D g2 = (Graphics2D) g.create(); // Erstellt eine private Kopie der Zeichenfläche.
        Theme.antialias(g2); // Aktiviert glatte Kanten.
        int centerY = trackRect.y + trackRect.height / 2; // Bestimmt die vertikale Mitte der Spur.
        g2.setColor(Theme.LINE); // Wählt die Farbe der leeren Spur.
        g2.fillRoundRect(trackRect.x, centerY - 3, trackRect.width, 6, 6, 6); // Zeichnet die leere Spur.
        int filled = thumbRect.x + thumbRect.width / 2 - trackRect.x; // Berechnet die Länge des gefüllten Teils.
        g2.setColor(Theme.scoreColor(slider.getValue() * 10)); // Färbt die Füllung passend zum Wert.
        g2.fillRoundRect(trackRect.x, centerY - 3, filled, 6, 6, 6); // Zeichnet den gefüllten Teil.
        g2.dispose(); // Gibt die Kopie der Zeichenfläche frei.
    } // Beendet das Zeichnen der Spur.

    @Override // Ersetzt das Zeichnen des Reglerknopfs.
    public void paintThumb(Graphics g) { // Zeichnet den runden Knopf.
        Graphics2D g2 = (Graphics2D) g.create(); // Erstellt eine private Kopie der Zeichenfläche.
        Theme.antialias(g2); // Aktiviert glatte Kanten.
        g2.setColor(Theme.CARD); // Wählt die Füllfarbe des Knopfs.
        g2.fillOval(thumbRect.x + 2, thumbRect.y + 2, thumbRect.width - 4, thumbRect.height - 4); // Füllt den Knopf weiß.
        g2.setColor(Theme.scoreColor(slider.getValue() * 10)); // Wählt die Randfarbe passend zum Wert.
        g2.setStroke(new BasicStroke(3f)); // Wählt einen kräftigen Rand.
        g2.drawOval(thumbRect.x + 2, thumbRect.y + 2, thumbRect.width - 5, thumbRect.height - 5); // Zeichnet den farbigen Rand.
        g2.dispose(); // Gibt die Kopie der Zeichenfläche frei.
    } // Beendet das Zeichnen des Knopfs.
} // Beendet die Klasse ModernSliderUI.
