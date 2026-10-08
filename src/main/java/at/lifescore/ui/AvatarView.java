package at.lifescore.ui; // Zeichnet den Avatar als skalierbare Java-Grafik ohne Bilddateien oder Internetzugriff.

import at.lifescore.model.AvatarOption; // Liefert die ausgewählte Figur und ihre Extras.
import at.lifescore.model.AvatarProfile; // Hält den aktuell angelegten Look.
import java.awt.*; // Zeichnet Formen, Farben und Konturen.
import java.awt.geom.*; // Ermöglicht weiche Gesichtskonturen und Kurven.
import java.awt.image.BufferedImage; // Erzeugt kleine Vorschauen aus derselben Zeichnung.
import javax.swing.*; // Bindet die Zeichnung in Swing und Schaltflächen ein.
import static at.lifescore.model.AvatarOption.*; // Hält die Fallunterscheidungen der Darstellung lesbar.

public final class AvatarView extends JPanel { // Verwendet dieselbe Darstellung in Seitenleiste, Profil und Auswahlvorschau.
    private AvatarProfile profile = AvatarProfile.initial(); // Zeigt bereits vor dem ersten Check-in einen Avatar.
    public AvatarView(int size) { // Erlaubt unterschiedliche Größen ohne getrennte Grafiken.
        setOpaque(false); // Lässt die umgebende Karte oder Seitenleiste durchscheinen.
        setPreferredSize(new Dimension(size, size)); // Reserviert einen quadratischen Zeichenbereich.
        setProfile(profile); // Initialisiert auch die zugängliche Beschreibung.
    } // Beendet die Komponenteninitialisierung.
    public void setProfile(AvatarProfile profile) { // Übernimmt ausschließlich das tatsächlich ausgewählte Profil.
        this.profile = profile; // Merkt sich Figur und Extras für das nächste Zeichnen.
        getAccessibleContext().setAccessibleName("Avatar: " + profile.character().title() + ", " + profile.background().title() + ", " + profile.frame().title() + ", " + profile.accessory().title()); // Beschreibt das Aussehen auch ohne Bild.
        repaint(); // Aktualisiert die sichtbare Figur unmittelbar.
    } // Beendet die Profilübernahme.
    @Override protected void paintComponent(Graphics graphics) { // Passt die Darstellung an die aktuelle Größe an.
        super.paintComponent(graphics); // Beachtet den normalen Swing-Zeichenablauf.
        paintAvatar((Graphics2D) graphics, profile, getWidth(), getHeight()); // Verwendet dieselbe Zeichnung wie die Auswahlvorschauen.
    } // Beendet das Komponentenzeichnen.
    static Icon icon(AvatarProfile profile, int size) { // Erstellt ein kleines Vorschaubild für einen Auswahlknopf.
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB); // Behält außerhalb des Avatars einen transparenten Hintergrund.
        Graphics2D graphics = image.createGraphics(); // Zeichnet direkt in das Vorschaubild.
        paintAvatar(graphics, profile, size, size); // Nutzt dieselben Formen wie die große Figur.
        graphics.dispose(); // Gibt die temporären Zeichenressourcen frei.
        return new ImageIcon(image); // Liefert ein direkt von Swing verwendbares Symbol.
    } // Beendet die Vorschauerzeugung.
    private static void paintAvatar(Graphics2D original, AvatarProfile profile, int width, int height) { // Zeichnet alle Looks in einem gemeinsamen Koordinatensystem.
        Graphics2D g = (Graphics2D) original.create(); // Verändert nicht die Zeicheneinstellungen der umgebenden UI.
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Glättet Konturen auch in kleinen Vorschauen.
        double scale = Math.min(width, height) / 280.0; // Skaliert das feste 280er-Design ohne Verzerrung.
        g.translate((width - 280 * scale) / 2, (height - 280 * scale) / 2); // Zentriert das Motiv im verfügbaren Bereich.
        g.scale(scale, scale); // Wendet die Skalierung auf alle nachfolgenden Formen an.
        Color backdrop = profile.background() == FOREST ? new Color(199, 225, 200) : profile.background() == NIGHT ? new Color(43, 58, 84) : new Color(245, 231, 201); // Wählt den tatsächlich angelegten Hintergrund.
        g.setColor(backdrop); // Setzt die Hintergrundfarbe.
        g.fillOval(16, 16, 248, 248); // Zeichnet die runde Grundfläche.
        Shape originalClip = g.getClip(); // Merkt sich den äußeren Zeichenbereich.
        g.clip(new Ellipse2D.Double(21, 21, 238, 238)); // Hält Landschaft und Körper innerhalb des Avatar-Kreises.
        g.setColor(profile.background() == NIGHT ? new Color(62, 79, 103) : new Color(174, 194, 151, 80)); // Zeichnet eine dezente Landschaft hinter der Figur.
        g.fill(new Ellipse2D.Double(-30, 178, 245, 150)); // Legt einen weichen Hügel im Hintergrund an.
        g.fill(new Ellipse2D.Double(145, 157, 210, 165)); // Ergänzt den zweiten Hügel.
        if (profile.background() == NIGHT) { // Macht den Nacht-Hintergrund durch sichtbare Sterne eigenständig.
            g.setColor(new Color(253, 227, 160)); // Verwendet warmes Sternenlicht.
            for (int[] star : new int[][]{{55, 72}, {215, 65}, {236, 120}, {75, 35}}) { // Verteilt kleine Sterne außerhalb des Gesichts.
                g.fillOval(star[0], star[1], 4, 4); // Hält die Sterne auch in kleinen Ansichten erkennbar.
            } // Beendet die Sternenzeichnung.
            g.fillOval(182, 40, 23, 23); // Zeichnet den Vollmond.
            g.setColor(backdrop); // Nimmt einen Teil des Mondes wieder aus.
            g.fillOval(190, 35, 22, 22); // Formt dadurch eine Mondsichel.
        } // Beendet das Nachtmotiv.
        Color fur = profile.character() == FOX ? new Color(220, 133, 71) : profile.character() == CAT ? new Color(154, 149, 187) : new Color(170, 124, 90); // Unterscheidet die drei Figuren farblich.
        g.setColor(UiTheme.GREEN); // Gibt allen Figuren eine zur App passende Kleidung.
        g.fillRoundRect(80, 176, 120, 118, 72, 72); // Zeichnet den Körper hinter dem Kopf.
        g.setColor(new Color(255, 255, 255, 70)); // Ergänzt ein kleines dekoratives Blatt auf dem Pullover.
        g.fill(new Ellipse2D.Double(131, 227, 14, 23)); // Markiert den Bezug zu positiven Alltagsgewohnheiten.
        g.setColor(fur); // Beginnt die typischen Ohren der ausgewählten Figur.
        if (profile.character() == BEAR) { // Bären erhalten runde Ohren.
            g.fillOval(60, 70, 55, 55); g.fillOval(165, 70, 55, 55); // Zeichnet beide äußeren Ohren symmetrisch.
            g.setColor(new Color(217, 172, 142)); // Hebt die Ohrinnenflächen ab.
            g.fillOval(71, 81, 33, 33); g.fillOval(176, 81, 33, 33); // Ergänzt weiche innere Ohrflächen.
        } else { // Fuchs und Katze erhalten klar erkennbare spitze Ohren.
            g.fillPolygon(new int[]{62, 72, 124}, new int[]{124, 49, 96}, 3); // Zeichnet das linke Ohr.
            g.fillPolygon(new int[]{156, 208, 218}, new int[]{96, 49, 124}, 3); // Zeichnet das rechte Ohr.
            g.setColor(profile.character() == FOX ? new Color(112, 72, 53) : new Color(206, 182, 194)); // Unterscheidet die Ohrinnenfarben.
            g.fillPolygon(new int[]{75, 79, 108}, new int[]{107, 69, 99}, 3); // Fügt die linke Ohrinnenfläche ein.
            g.fillPolygon(new int[]{172, 201, 205}, new int[]{99, 69, 107}, 3); // Fügt die rechte Ohrinnenfläche ein.
        } // Beendet die Ohren.
        g.setColor(fur); // Verwendet dieselbe Fellfarbe für den Kopf.
        g.fillRoundRect(65, 86, 150, 121, 82, 82); // Zeichnet einen freundlichen, weich gerundeten Kopf.
        g.setColor(new Color(255, 242, 217)); // Verwendet helles Fell für Wangen und Schnauze.
        if (profile.character() == FOX) { // Betont die typische helle Fuchsmaske.
            Path2D cheeks = new Path2D.Double(); // Verbindet beide Wangen in einer weichen Fläche.
            cheeks.moveTo(67, 133); cheeks.curveTo(99, 136, 119, 158, 140, 169); // Führt die linke Wange zur Nasenspitze.
            cheeks.curveTo(164, 153, 185, 136, 213, 133); cheeks.curveTo(210, 222, 69, 222, 67, 133); // Verbindet rechte Wange und Kinn.
            g.fill(cheeks); // Füllt die helle Gesichtsmaske.
        } else { // Katze und Bär erhalten eine runde Schnauze.
            g.fillOval(108, 148, 64, 50); // Hebt die Gesichtsmitte vom Fell ab.
        } // Beendet die Gesichtsfläche.
        g.setColor(new Color(43, 48, 44)); // Verwendet kräftige Kontraste für die Gesichtszüge.
        g.fillOval(101, 130, 9, 13); g.fillOval(170, 130, 9, 13); // Zeichnet zwei freundliche Augen.
        g.fillRoundRect(132, 159, 17, 11, 9, 9); // Zeichnet die kleine Nase.
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); // Hält die Mundlinie weich.
        g.draw(new QuadCurve2D.Double(128, 180, 140, 190, 152, 180)); // Zeichnet ein dezentes Lächeln.
        g.setColor(new Color(222, 129, 111, 125)); // Legt einen sanften Wangenton fest.
        g.fillOval(83, 150, 19, 10); g.fillOval(179, 150, 19, 10); // Gibt der Figur einen lebendigen Ausdruck.
        if (profile.character() == CAT) { // Ergänzt die charakteristischen Schnurrhaare der Katze.
            g.setColor(new Color(89, 82, 112)); // Zeichnet die Linien dunkler als das Fell.
            g.setStroke(new BasicStroke(2)); // Hält Schnurrhaare feiner als Gesichtskonturen.
            g.drawLine(79, 166, 103, 171); g.drawLine(80, 177, 103, 177); // Zeichnet die linken Schnurrhaare.
            g.drawLine(177, 171, 201, 166); g.drawLine(177, 177, 200, 177); // Zeichnet die rechten Schnurrhaare.
        } // Beendet die katzenspezifischen Details.
        if (profile.accessory() == SCARF) { // Legt den freigeschalteten Schal sichtbar um den Hals.
            g.setColor(new Color(189, 91, 66)); // Nutzt ein warmes Terrakotta als Akzent.
            g.fillRoundRect(92, 195, 96, 20, 15, 15); // Zeichnet das waagerechte Schalstück.
            g.fillRoundRect(160, 202, 21, 41, 7, 7); // Lässt ein Schalende vor dem Pullover hängen.
            g.setColor(new Color(231, 157, 112)); // Hebt die Stoffstruktur dezent hervor.
            g.fillRect(164, 223, 13, 4); // Ergänzt einen hellen Streifen am Schalende.
        } // Beendet den Schal.
        if (profile.accessory() == CROWN) { // Setzt die Level-7-Belohnung auf den Kopf.
            g.setColor(new Color(228, 181, 75)); // Verwendet warmes Gold.
            g.fillPolygon(new int[]{107, 102, 122, 140, 158, 178, 173}, new int[]{93, 61, 73, 48, 73, 61, 93}, 7); // Zeichnet die drei Kronenspitzen.
            g.setColor(new Color(255, 220, 126)); // Betont den unteren Kronenrand.
            g.fillRoundRect(107, 86, 66, 10, 5, 5); // Gibt der Krone einen klaren Abschluss.
            g.setColor(UiTheme.GREEN); g.fillOval(135, 73, 10, 10); // Ergänzt einen kleinen grünen Schmuckstein.
        } // Beendet die Krone.
        g.setClip(originalClip); // Gibt den äußeren Rahmen wieder zum Zeichnen frei.
        if (profile.frame() != PLAIN) { // Zeichnet nur tatsächlich angelegte Rahmen.
            g.setColor(profile.frame() == GOLD ? new Color(201, 157, 60) : new Color(94, 143, 109)); // Unterscheidet Salbei- und Goldbelohnung.
            g.setStroke(new BasicStroke(profile.frame() == GOLD ? 8 : 5)); // Verleiht dem Goldrahmen etwas mehr Gewicht.
            g.drawOval(15, 15, 250, 250); // Umfasst den Avatar mit dem ausgewählten Rahmen.
            if (profile.frame() == GOLD) { // Verleiht Gold einen zusätzlichen feinen Innenring.
                g.setColor(new Color(250, 221, 148)); g.setStroke(new BasicStroke(2)); g.drawOval(23, 23, 234, 234); // Zeichnet eine zweite warme Kontur.
            } // Beendet den Goldakzent.
        } // Beendet die Rahmendarstellung.
        g.dispose(); // Gibt den isolierten Grafikkontext frei.
    } // Beendet das Zeichnen des vollständigen Avatars.
} // Beendet die Avatar-Komponente.
