package at.lifescore.ui; // Bündelt die Gestaltung unabhängig von der Fachlogik.

import java.awt.*; // Stellt Farben, Schrift und Layout bereit.
import javax.swing.*; // Liefert die Standardkomponenten ohne zusätzliche Bibliotheken.
import javax.swing.border.EmptyBorder; // Erzeugt einheitliche Innenabstände.

public final class UiTheme { // Hält alle wiederkehrenden Gestaltungsentscheidungen an einem Ort.
    static final Color BACKGROUND = new Color(245, 247, 242); // Ein warmes Hellgrau beruhigt den Seitenhintergrund.
    static final Color INK = new Color(28, 52, 43); // Dunkles Grün sorgt für gut lesbare Überschriften.
    static final Color MUTED = new Color(91, 109, 100); // Sekundäre Texte bleiben kontrastreich.
    static final Color GREEN = new Color(38, 103, 75); // Grün kennzeichnet Aktionen und Fortschritt.
    static final Color PALE = new Color(231, 239, 224); // Helle Flächen strukturieren die Inhalte.
    static final Color LINE = new Color(225, 231, 220); // Dezente Linien trennen Tabellenzeilen.

    private UiTheme() { } // Verhindert Instanzen dieser Sammlung von Gestaltungshilfen.

    public static void install() { // Vereinheitlicht Swing vor dem Erstellen der Komponenten.
        Font base = new Font(Font.SANS_SERIF, Font.PLAIN, 14); // Nutzt eine auf allen Java-Plattformen verfügbare Schrift.
        for (Object key : UIManager.getDefaults().keySet().toArray()) { // Durchläuft eine Kopie, damit Änderungen sicher bleiben.
            if (key.toString().endsWith(".font")) { // Ändert ausschließlich Schriftvorgaben.
                UIManager.put(key, base); // Verhindert eine Mischung aus unterschiedlich großen Systemschriften.
            } // Beendet die Auswahl der Schriftwerte.
        } // Beendet das Vereinheitlichen.
        UIManager.put("Label.foreground", INK); // Verwendet die gemeinsame Textfarbe.
        UIManager.put("Button.disabledText", MUTED); // Hält die Freischaltschwellen gesperrter Belohnungen gut lesbar.
        UIManager.put("Panel.background", BACKGROUND); // Gibt Standardpanels den Seitenhintergrund.
        UIManager.put("Slider.focus", GREEN); // Hält Tastaturfokus innerhalb der Farbpalette sichtbar.
        UIManager.put("ProgressBar.foreground", GREEN); // Markiert Fortschritt einheitlich.
        UIManager.put("ProgressBar.selectionForeground", Color.WHITE); // Hält XP-Text auf dem gefüllten Balken lesbar.
        UIManager.put("ProgressBar.selectionBackground", INK); // Hält XP-Text auf dem ungefüllten Balken lesbar.
    } // Beendet die globalen Vorgaben.

    static JLabel label(String text, int size, Color color) { // Erstellt konsistente Textbausteine.
        JLabel label = new JLabel(text); // Bewahrt Text als zugängliche Swing-Komponente.
        label.setFont(new Font(Font.SANS_SERIF, size >= 22 ? Font.BOLD : Font.PLAIN, size)); // Hebt große Zahlen und Titel hervor.
        label.setForeground(color); // Wendet die passende Hierarchiefarbe an.
        return label; // Liefert den formatierten Text.
    } // Beendet die Texterzeugung.

    static JPanel transparent(LayoutManager layout) { // Gruppiert Inhalte ohne eigene Hintergrundfläche.
        JPanel panel = new JPanel(layout); // Verwendet das zur Gruppe passende Layout.
        panel.setOpaque(false); // Lässt die übergeordnete Karte sichtbar.
        return panel; // Liefert die transparente Gruppe.
    } // Beendet die Gruppierungshilfe.

    static JPanel card(LayoutManager layout) { // Erstellt eine wiederverwendbare abgerundete Karte.
        JPanel panel = new JPanel(layout) { // Zeichnet nur den Hintergrund selbst; Inhalte bleiben normale Swing-Komponenten.
            @Override protected void paintComponent(Graphics graphics) { // Zeichnet die Karte bei jeder Größenänderung neu.
                Graphics2D g = (Graphics2D) graphics.create(); // Isoliert die Zeicheneinstellungen.
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Glättet die Rundungen.
                g.setColor(Color.WHITE); // Verwendet eine ruhige weiße Inhaltsfläche.
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22); // Passt die Karte an die aktuelle Größe an.
                g.dispose(); // Gibt die Kopie des Grafikkontexts frei.
            } // Beendet die Hintergrundzeichnung.
        }; // Beendet die spezielle Kartenkomponente.
        panel.setOpaque(false); // Verhindert ein rechteckiges Übermalen der Rundungen.
        panel.setBorder(new EmptyBorder(22, 24, 22, 24)); // Definiert einheitliche Abstände innerhalb der Karte.
        return panel; // Liefert die fertige Kartenfläche.
    } // Beendet die Kartenerzeugung.

    static JButton button(String text, boolean primary) { // Vereinheitlicht Haupt- und Nebenaktionen.
        JButton button = new JButton(text); // Behält Tastaturbedienung und zugänglichen Namen von Swing bei.
        button.setBackground(primary ? GREEN : PALE); // Unterscheidet die wichtigste Aktion farblich.
        button.setForeground(primary ? Color.WHITE : INK); // Sichert lesbaren Text auf beiden Flächen.
        button.setOpaque(true); // Verhindert vom Betriebssystem abhängige transparente Knöpfe.
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? GREEN : LINE), new EmptyBorder(11, 17, 11, 17))); // Gibt dem Knopf eine großzügige Klickfläche.
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Kennzeichnet die Interaktion zusätzlich zum Text.
        return button; // Liefert die Schaltfläche mit sichtbarem Standard-Tastaturfokus.
    } // Beendet die Knopferzeugung.
} // Beendet das Gestaltungssystem.
