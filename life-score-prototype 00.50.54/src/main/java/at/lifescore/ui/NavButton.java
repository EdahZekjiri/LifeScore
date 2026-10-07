package at.lifescore.ui; // Ordnet die Menüschaltfläche dem UI-Paket zu.

import java.awt.Color; // Stellt Farben bereit.
import java.awt.Cursor; // Stellt den Handzeiger bereit.
import java.awt.Dimension; // Legt Größen fest.
import java.awt.Font; // Stellt Schriftstile bereit.
import java.awt.Graphics; // Stellt die Zeichenfläche bereit.
import java.awt.Graphics2D; // Ermöglicht erweiterte Zeichenfunktionen.
import java.awt.event.MouseAdapter; // Vereinfacht die Maus-Ereignisbehandlung.
import java.awt.event.MouseEvent; // Beschreibt Mausereignisse.
import javax.swing.BorderFactory; // Erzeugt den Innenabstand.
import javax.swing.JButton; // Dient als Basis der Schaltfläche.
import javax.swing.SwingConstants; // Stellt Ausrichtungskonstanten bereit.

final class NavButton extends JButton { // Stellt einen Menüpunkt der Seitenleiste dar.
    private boolean active; // Merkt sich, ob die Seite gerade angezeigt wird.
    private boolean hover; // Merkt sich, ob die Maus darüber steht.

    NavButton(String text) { // Erzeugt den Menüpunkt mit Beschriftung.
        super(text); // Übergibt die Beschriftung an die Basisklasse.
        setContentAreaFilled(false); // Verhindert den Standardhintergrund.
        setBorderPainted(false); // Verhindert den Standardrand.
        setFocusPainted(false); // Verhindert den Fokusrahmen.
        setOpaque(false); // Lässt die abgerundeten Ecken durchscheinen.
        setHorizontalAlignment(SwingConstants.LEFT); // Richtet den Text linksbündig aus.
        setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18)); // Schafft großzügigen Innenabstand.
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Zeigt beim Darüberfahren die Hand.
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 46)); // Lässt den Menüpunkt die volle Breite nutzen.
        setActive(false); // Setzt den Anfangszustand auf inaktiv.
        addMouseListener(new MouseAdapter() { // Reagiert auf die Maus.
            @Override // Ersetzt die Standardreaktion.
            public void mouseEntered(MouseEvent event) { // Wird beim Darüberfahren aufgerufen.
                hover = true; // Merkt sich den Hover-Zustand.
                repaint(); // Zeichnet den Menüpunkt neu.
            } // Beendet die Reaktion auf das Darüberfahren.

            @Override // Ersetzt die Standardreaktion.
            public void mouseExited(MouseEvent event) { // Wird beim Verlassen aufgerufen.
                hover = false; // Löscht den Hover-Zustand.
                repaint(); // Zeichnet den Menüpunkt neu.
            } // Beendet die Reaktion auf das Verlassen.
        }); // Beendet die Registrierung der Maus-Reaktion.
    } // Beendet den Konstruktor.

    void setActive(boolean value) { // Markiert den Menüpunkt als aktuelle Seite.
        active = value; // Speichert den Zustand.
        setForeground(value ? Color.WHITE : new Color(176, 188, 205)); // Wählt die Textfarbe nach Zustand.
        setFont(Theme.font(value ? Font.BOLD : Font.PLAIN, 15f)); // Wählt die Schrift nach Zustand.
        repaint(); // Zeichnet den Menüpunkt neu.
    } // Beendet das Setzen des Zustands.

    @Override // Ersetzt das Standardzeichnen der Schaltfläche.
    protected void paintComponent(Graphics graphics) { // Zeichnet Hervorhebung und anschließend den Text.
        if (active || hover) { // Prüft, ob eine Hervorhebung nötig ist.
            Graphics2D g2 = (Graphics2D) graphics.create(); // Erstellt eine private Kopie der Zeichenfläche.
            Theme.antialias(g2); // Aktiviert glatte Kanten.
            g2.setColor(active ? Theme.ACCENT : new Color(255, 255, 255, 22)); // Wählt Akzentfarbe oder dezentes Weiß.
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16); // Zeichnet die abgerundete Hervorhebung.
            g2.dispose(); // Gibt die Kopie der Zeichenfläche frei.
        } // Beendet das Zeichnen der Hervorhebung.
        super.paintComponent(graphics); // Zeichnet den Text über die Hervorhebung.
    } // Beendet das Zeichnen des Menüpunkts.
} // Beendet die Klasse NavButton.
