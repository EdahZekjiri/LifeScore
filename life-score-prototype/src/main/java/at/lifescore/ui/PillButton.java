package at.lifescore.ui; // Ordnet die Schaltfläche dem UI-Paket zu.

import java.awt.Color; // Stellt Farben bereit.
import java.awt.Cursor; // Stellt den Handzeiger bereit.
import java.awt.Font; // Stellt Schriftstile bereit.
import java.awt.Graphics; // Stellt die Zeichenfläche bereit.
import java.awt.Graphics2D; // Ermöglicht erweiterte Zeichenfunktionen.
import java.awt.event.MouseAdapter; // Vereinfacht die Maus-Ereignisbehandlung.
import java.awt.event.MouseEvent; // Beschreibt Mausereignisse.
import java.awt.geom.RoundRectangle2D; // Beschreibt abgerundete Flächen.
import javax.swing.BorderFactory; // Erzeugt den Innenabstand.
import javax.swing.JButton; // Dient als Basis der Schaltfläche.

final class PillButton extends JButton { // Stellt eine abgerundete Schaltfläche in Haupt- oder Nebenstil dar.
    private final boolean primary; // Merkt sich, ob die Schaltfläche die Akzentfarbe nutzt.
    private boolean hover; // Merkt sich, ob die Maus darüber steht.

    PillButton(String text, boolean primary) { // Erzeugt die Schaltfläche mit Beschriftung und Stil.
        super(text); // Übergibt die Beschriftung an die Basisklasse.
        this.primary = primary; // Speichert den Stil.
        setContentAreaFilled(false); // Verhindert den Standardhintergrund.
        setBorderPainted(false); // Verhindert den Standardrand.
        setFocusPainted(false); // Verhindert den Fokusrahmen.
        setOpaque(false); // Lässt die abgerundeten Ecken durchscheinen.
        setFont(Theme.font(Font.BOLD, 14f)); // Wählt die kräftige Schrift.
        setForeground(primary ? Color.WHITE : Theme.TEXT); // Wählt eine gut lesbare Textfarbe.
        setBorder(BorderFactory.createEmptyBorder(11, text.length() <= 2 ? 16 : 24, 11, text.length() <= 2 ? 16 : 24)); // Wählt den Innenabstand je nach Textlänge.
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Zeigt beim Darüberfahren die Hand.
        addMouseListener(new MouseAdapter() { // Reagiert auf die Maus.
            @Override // Ersetzt die Standardreaktion.
            public void mouseEntered(MouseEvent event) { // Wird beim Darüberfahren aufgerufen.
                hover = true; // Merkt sich den Hover-Zustand.
                repaint(); // Zeichnet die Schaltfläche neu.
            } // Beendet die Reaktion auf das Darüberfahren.

            @Override // Ersetzt die Standardreaktion.
            public void mouseExited(MouseEvent event) { // Wird beim Verlassen aufgerufen.
                hover = false; // Löscht den Hover-Zustand.
                repaint(); // Zeichnet die Schaltfläche neu.
            } // Beendet die Reaktion auf das Verlassen.
        }); // Beendet die Registrierung der Maus-Reaktion.
    } // Beendet den Konstruktor.

    @Override // Ersetzt das Standardzeichnen der Schaltfläche.
    protected void paintComponent(Graphics graphics) { // Zeichnet Fläche und anschließend den Text.
        Graphics2D g2 = (Graphics2D) graphics.create(); // Erstellt eine private Kopie der Zeichenfläche.
        Theme.antialias(g2); // Aktiviert glatte Kanten.
        RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 18, 18); // Beschreibt die abgerundete Form.
        Color fill = primary ? (hover ? Theme.ACCENT_DARK : Theme.ACCENT) : (hover ? Theme.BG : Theme.CARD); // Wählt die Füllfarbe nach Stil und Hover.
        if (!isEnabled()) { // Prüft, ob die Schaltfläche deaktiviert ist.
            fill = primary ? new Color(180, 214, 198) : Theme.BG; // Wählt eine blasse Füllfarbe.
        } // Beendet die Prüfung auf Deaktivierung.
        g2.setColor(fill); // Wählt die Füllfarbe.
        g2.fill(shape); // Füllt die Form.
        if (!primary) { // Prüft, ob eine Kontur nötig ist.
            g2.setColor(Theme.LINE); // Wählt die Konturfarbe.
            g2.draw(shape); // Zeichnet die Kontur.
        } // Beendet das Zeichnen der Kontur.
        g2.dispose(); // Gibt die Kopie der Zeichenfläche frei.
        super.paintComponent(graphics); // Zeichnet den Text über die Fläche.
    } // Beendet das Zeichnen der Schaltfläche.
} // Beendet die Klasse PillButton.
