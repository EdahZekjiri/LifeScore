package at.lifescore.ui; // Ordnet die Gestaltungsvorgaben dem UI-Paket zu.

import java.awt.Color; // Stellt Farben für die Oberfläche bereit.
import java.awt.Font; // Stellt Schriftstile und Schriftgrößen bereit.
import java.awt.GraphicsEnvironment; // Liefert die auf dem System installierten Schriften.
import java.awt.Graphics2D; // Ermöglicht qualitativ hochwertiges Zeichnen.
import java.awt.LayoutManager; // Beschreibt die Anordnung von Komponenten in einem Panel.
import java.awt.RenderingHints; // Steuert Kantenglättung beim Zeichnen.
import java.util.Arrays; // Wandelt Schriftnamen in eine Liste um.
import java.util.HashSet; // Ermöglicht schnelles Nachschlagen installierter Schriften.
import java.util.Set; // Stellt den allgemeinen Mengentyp bereit.
import javax.swing.JPanel; // Gruppiert zusammengehörige UI-Komponenten.

final class Theme { // Bündelt Farben, Schriften und Zeichenhilfen der gesamten Oberfläche.
    static final Color BG = new Color(243, 245, 249); // Legt den hellen Seitenhintergrund fest.
    static final Color CARD = Color.WHITE; // Legt die Kartenfarbe fest.
    static final Color SIDEBAR = new Color(23, 32, 45); // Legt das dunkle Seitenmenü fest.
    static final Color ACCENT = new Color(38, 166, 108); // Legt die grüne Hauptakzentfarbe fest.
    static final Color ACCENT_DARK = new Color(29, 134, 87); // Legt die dunklere Akzentfarbe für Hover und Text fest.
    static final Color TEXT = new Color(27, 36, 50); // Legt die Haupttextfarbe fest.
    static final Color MUTED = new Color(112, 123, 140); // Legt die Farbe für Nebentexte fest.
    static final Color LINE = new Color(229, 233, 240); // Legt die Farbe für Linien und leere Balken fest.
    private static final String FAMILY = pickFamily(); // Wählt einmalig die schönste verfügbare Schrift.

    private Theme() { // Verhindert das Erzeugen einer Hilfsklasse.
    } // Beendet den privaten Konstruktor.

    static Font font(int style, float size) { // Erzeugt eine Schrift im einheitlichen Stil der App.
        return new Font(FAMILY, style, 12).deriveFont(style, size); // Liefert die gewählte Schriftfamilie in Stil und Größe.
    } // Beendet die Schrifterzeugung.

    static Color scoreColor(int score) { // Ordnet einem Score eine Ampelfarbe zu.
        if (score >= 70) { // Prüft auf einen guten Wert.
            return ACCENT; // Zeigt gute Werte in Grün.
        } // Beendet die Prüfung auf gute Werte.
        if (score >= 40) { // Prüft auf einen mittleren Wert.
            return new Color(240, 165, 0); // Zeigt mittlere Werte in Bernstein.
        } // Beendet die Prüfung auf mittlere Werte.
        return new Color(226, 86, 72); // Zeigt niedrige Werte in Rot.
    } // Beendet die Farbwahl.

    static String hex(Color color) { // Wandelt eine Farbe in einen HTML-Farbwert um.
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); // Formatiert die Farbanteile als Hexadezimalwert.
    } // Beendet die Farbumwandlung.

    static JPanel plain(LayoutManager layout) { // Erzeugt ein durchsichtiges Panel ohne eigenen Hintergrund.
        JPanel panel = new JPanel(layout); // Erstellt das Panel mit dem gewünschten Layout.
        panel.setOpaque(false); // Verhindert graue Flächen hinter Karten.
        return panel; // Liefert das durchsichtige Panel zurück.
    } // Beendet die Panel-Erzeugung.

    static void antialias(Graphics2D g2) { // Aktiviert glatte Kanten für Formen und Text.
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Glättet Formen und Linien.
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // Glättet Schrift.
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE); // Zeichnet Linien pixelgenau.
    } // Beendet die Aktivierung der Kantenglättung.

    private static String pickFamily() { // Sucht die erste vorhandene Wunschschrift.
        Set<String> installed = new HashSet<>(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())); // Liest alle installierten Schriftfamilien.
        for (String candidate : new String[]{"Segoe UI", "SF Pro Text", "Helvetica Neue", "Inter", "Noto Sans", "DejaVu Sans"}) { // Durchläuft die Wunschliste nach Priorität.
            if (installed.contains(candidate)) { // Prüft, ob die Schrift auf diesem Gerät vorhanden ist.
                return candidate; // Verwendet die erste gefundene Schrift.
            } // Beendet die Prüfung einer Schrift.
        } // Beendet die Suche.
        return Font.SANS_SERIF; // Fällt auf die Standardschrift des Systems zurück.
    } // Beendet die Schriftwahl.
} // Beendet die Klasse Theme.
