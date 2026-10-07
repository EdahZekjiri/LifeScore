package at.lifescore.ui; // Ordnet die Karte dem UI-Paket zu.

import java.awt.Graphics; // Stellt die Zeichenfläche bereit.
import java.awt.Graphics2D; // Ermöglicht erweiterte Zeichenfunktionen.
import java.awt.LayoutManager; // Beschreibt die Anordnung der Karteninhalte.
import java.awt.geom.RoundRectangle2D; // Beschreibt ein Rechteck mit runden Ecken.
import javax.swing.BorderFactory; // Erzeugt den Innenabstand der Karte.
import javax.swing.JPanel; // Dient als Basis der Karte.

final class Card extends JPanel { // Stellt eine weiße Fläche mit runden Ecken und feiner Kontur dar.
    Card(LayoutManager layout) { // Erzeugt eine Karte mit dem gewünschten Layout.
        super(layout); // Übergibt das Layout an das Panel.
        setOpaque(false); // Lässt die abgerundeten Ecken den Hintergrund durchscheinen.
        setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22)); // Schafft einheitlichen Innenabstand.
    } // Beendet den Konstruktor.

    @Override // Ersetzt das Standardzeichnen des Panels.
    protected void paintComponent(Graphics graphics) { // Zeichnet die Kartenfläche.
        Graphics2D g2 = (Graphics2D) graphics.create(); // Erstellt eine private Kopie der Zeichenfläche.
        Theme.antialias(g2); // Aktiviert glatte Kanten.
        RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 22, 22); // Beschreibt die abgerundete Kartenform.
        g2.setColor(Theme.CARD); // Wählt die Kartenfarbe.
        g2.fill(shape); // Füllt die Kartenform.
        g2.setColor(Theme.LINE); // Wählt die Konturfarbe.
        g2.draw(shape); // Zeichnet die feine Kontur.
        g2.dispose(); // Gibt die Kopie der Zeichenfläche frei.
    } // Beendet das Zeichnen der Karte.
} // Beendet die Klasse Card.
