package at.lifescore.ui; // Verbindet Avatar-Vorschau, Auswahl und echte Level-Freischaltungen.

import at.lifescore.model.AvatarOption; // Definiert verfügbare Looks und die benötigten Level.
import at.lifescore.model.AvatarProfile; // Hält die gespeicherte Auswahl und den höchsten Fortschritt.
import at.lifescore.repository.AvatarRepository; // Speichert Änderungen dauerhaft im lokalen Datenordner.
import java.awt.*; // Strukturiert Vorschau und Auswahlkarten.
import java.util.Arrays; // Filtert den Optionskatalog nach Kategorien.
import java.util.Comparator; // Findet die nächste noch nicht erreichte Schwelle.
import java.util.EnumMap; // Verknüpft jede Option eindeutig mit ihrer Schaltfläche.
import java.util.Map; // Beschreibt die Zuordnung der Auswahlknöpfe.
import java.util.function.Consumer; // Aktualisiert den sichtbaren Avatar in der Seitenleiste.
import java.util.stream.Collectors; // Fasst mehrere gleichzeitig verdiente Extras lesbar zusammen.
import javax.swing.*; // Nutzt normale, tastaturbedienbare Swing-Komponenten.
import javax.swing.border.EmptyBorder; // Hält Abstände innerhalb der Auswahlknöpfe konsistent.
import static at.lifescore.ui.UiTheme.*; // Verwendet dasselbe Design wie Dashboard und Check-in.

public final class AvatarStudio extends JPanel { // Zeigt einen sofort nutzbaren Avatar statt eines bloßen Belohnungstexts.
    private final AvatarRepository repository; // Hält den Speicherzugriff getrennt von der Darstellung.
    private final Consumer<AvatarProfile> onChange; // Synchronisiert die Seitenleistenfigur nach erfolgreicher Speicherung.
    private final AvatarView preview = new AvatarView(210); // Zeigt den aktuell angelegten Look in groß.
    private final Map<AvatarOption, JButton> choices = new EnumMap<>(AvatarOption.class); // Ermöglicht gezielte Aktualisierung von Sperren und Auswahlmarkierungen.
    private final JLabel name = label("Dein Fuchs", 25, INK); // Benennt die sichtbare Figur.
    private final JLabel progress = label("Level 1 · 0 XP", 16, GREEN); // Zeigt das aktuelle Level unabhängig vom höchsten erreichten Level.
    private final JLabel collection = label("0 Extras freigeschaltet", 14, MUTED); // Zählt alle dauerhaft verdienten Belohnungen.
    private final JTextArea status = new JTextArea(); // Zeigt Speichererfolg, Freischaltungen oder konkrete Fehler umbrechend an.
    private final JTextArea nextReward = new JTextArea(); // Erklärt den nächsten Meilenstein mit konkreten XP.
    private AvatarProfile profile = AvatarProfile.initial(); // Zeigt von Beginn an einen vollständigen Standardavatar.
    private boolean available = true; // Sperrt Änderungen bei einem beschädigten Profil, ohne die Tagesdaten zu blockieren.
    private int totalXp; // Dient zur verständlichen Anzeige des nächsten Belohnungsziels.
    private int currentLevel = 1; // Hält das aktuelle Level getrennt vom dauerhaft erreichten Höchststand.

    public AvatarStudio(AvatarRepository repository, Consumer<AvatarProfile> onChange) { // Erhält Speicherung und Rückmeldung explizit von außen.
        super(new BorderLayout(0, 18)); // Ordnet große Vorschau und Auswahlgruppen untereinander an.
        this.repository = repository; // Merkt sich die Datenablage.
        this.onChange = onChange; // Merkt sich die Aktualisierung der Seitenleiste.
        setOpaque(false); // Nutzt den gemeinsamen Seitenhintergrund.
        try { profile = repository.load(); } catch (RuntimeException exception) { available = false; status.setText(exception.getMessage()); } // Behandelt ein beschädigtes Profil getrennt von der Score-Historie.
        add(buildHero(), BorderLayout.NORTH); // Beginnt mit der tatsächlich angelegten Figur und ihren Fortschritten.
        JPanel groups = transparent(new GridLayout(4, 1, 0, 14)); // Gibt jeder Kategorie eine eigene, gut erklärbare Karte.
        String[] headings = {"01  Deine Figur", "02  Dein Hintergrund", "03  Dein Rahmen", "04  Dein Accessoire"}; // Benennt die vier unabhängigen Gestaltungsmöglichkeiten.
        AvatarOption.Slot[] slots = AvatarOption.Slot.values(); // Nutzt dieselbe Reihenfolge wie das validierte Datenmodell.
        for (int i = 0; i < slots.length; i++) { // Baut eine Auswahlgruppe pro Kategorie auf.
            JPanel group = card(new BorderLayout(0, 12)); // Fasst Überschrift und Optionen zusammen.
            group.setBorder(new EmptyBorder(15, 18, 15, 18)); // Hält die längere Gestaltungsseite kompakt.
            group.add(label(headings[i], 17, INK), BorderLayout.NORTH); // Erklärt, welche Eigenschaft geändert wird.
            JPanel row = transparent(new GridLayout(1, 3, 10, 0)); // Zeigt die drei Optionen jeder Kategorie nebeneinander.
            for (AvatarOption option : AvatarOption.values()) { // Nutzt den gemeinsamen Katalog auch für sichtbare Sperren.
                if (option.slot() != slots[i]) { continue; } // Begrenzt die Gruppe auf ihre Kategorie.
                JButton choice = button(option.title(), false); // Behält Tastaturfokus und Bedienbarkeit von Swing bei.
                choice.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // Verwendet für gesperrte Looks die kontrollierte Kontrastfarbe statt sehr heller Systemtexte.
                choice.setHorizontalTextPosition(SwingConstants.CENTER); // Zentriert die Bezeichnung unter dem Vorschaubild.
                choice.setVerticalTextPosition(SwingConstants.BOTTOM); // Trennt Vorschau und Status in zwei Ebenen.
                choice.setIconTextGap(3); // Hält Bild und Bezeichnung zusammen.
                choice.setPreferredSize(new Dimension(155, 106)); // Macht jeden Look als ausreichend große Klickfläche zugänglich.
                choice.addActionListener(event -> equip(option)); // Wendet einen freigeschalteten Look unmittelbar an und speichert ihn.
                choices.put(option, choice); // Merkt sich den Knopf für spätere Leveländerungen.
                row.add(choice); // Fügt den Look an seiner festen Katalogposition ein.
            } // Beendet die Optionsgruppe.
            group.add(row, BorderLayout.CENTER); // Platziert die drei Looks unter ihrer Überschrift.
            groups.add(group); // Fügt die Kategorie zur Gestaltungsseite hinzu.
        } // Beendet den Aufbau der vier Kategorien.
        add(groups, BorderLayout.CENTER); // Macht alle freigeschalteten und gesperrten Looks sichtbar.
        if (available) { status.setText("Wähle deinen Look. Jede Auswahl wird sofort lokal gespeichert."); } // Erklärt das Speichermodell direkt im Nutzerfluss.
        render(); // Zeigt die gespeicherte Figur und den aktuellen Freischaltzustand.
    } // Beendet den Aufbau des Avatar-Studios.

    private JPanel buildHero() { // Gruppiert den großen Avatar mit Fortschritt und nächstem Ziel.
        JPanel hero = card(new BorderLayout(22, 0)); // Trennt die Figur links von den Erläuterungen rechts.
        hero.add(preview, BorderLayout.WEST); // Hält den Avatar beim Einstieg sofort sichtbar.
        JPanel text = transparent(new BorderLayout(0, 12)); // Ordnet Name, Erfahrung und Rückmeldungen übersichtlich an.
        JPanel headings = transparent(new GridLayout(3, 1, 0, 5)); // Gruppiert die drei kurzen Kennzahlen.
        headings.add(name); headings.add(progress); headings.add(collection); // Zeigt Figur, aktuelles Level und Inventargröße.
        text.add(headings, BorderLayout.NORTH); // Platziert die Kennzahlen über den Erläuterungen.
        configureParagraph(nextReward); // Macht lange Belohnungsnamen auf schmalen Fenstern lesbar.
        text.add(nextReward, BorderLayout.CENTER); // Nennt die nächste erreichbare Belohnung.
        configureParagraph(status); // Bereitet Speicherbestätigung und Fehlermeldungen vor.
        status.setForeground(GREEN); // Hebt die Rückmeldung dezent hervor.
        text.add(status, BorderLayout.SOUTH); // Zeigt Änderungen direkt neben der aktualisierten Figur.
        hero.add(text, BorderLayout.CENTER); // Verwendet die verbleibende Breite für den erklärenden Text.
        return hero; // Gibt die vollständige Profilkarte zurück.
    } // Beendet den Aufbau der Vorschaukarte.

    private void configureParagraph(JTextArea area) { // Stellt kurze Hilfetexte konsistent dar.
        area.setEditable(false); area.setOpaque(false); // Hält die Texte auswählbar, aber nicht veränderbar.
        area.setLineWrap(true); area.setWrapStyleWord(true); // Verhindert abgeschnittene Statusmeldungen.
        area.setForeground(MUTED); area.setRows(3); // Reserviert genug Höhe für schmale Ansichten.
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); // Verwendet eine gut lesbare kleinere Textgröße.
    } // Beendet die Absatzgestaltung.

    private void equip(AvatarOption option) { // Ändert den Look ausschließlich bei gültiger Freischaltung.
        if (!available) { return; } // Verhindert Schreiben, solange das Profil nicht sicher gelesen werden konnte.
        try { // Zeigt Fehler, ohne die bisherige gespeicherte Auswahl zu verlieren.
            AvatarProfile updated = profile.equip(option); // Prüft die Freischaltung auch im Modell und ersetzt genau eine Kategorie.
            repository.save(updated); // Speichert zuerst; die sichtbare Auswahl folgt nur bei Erfolg.
            profile = updated; // Übernimmt den dauerhaft gesicherten Look.
            status.setText("Gespeichert · " + option.title() + " ist jetzt angelegt."); // Nennt die tatsächlich angewendete Änderung.
            status.setForeground(GREEN); // Markiert eine erfolgreiche Änderung.
            render(); // Synchronisiert große Vorschau, Auswahlknöpfe und Seitenleistenfigur.
        } catch (RuntimeException exception) { // Behandelt unzulässige Optionen und Schreibfehler gemeinsam.
            status.setForeground(new Color(151, 60, 46)); // Macht den Fehlerzustand zusätzlich zum Text erkennbar.
            status.setText(exception.getMessage()); // Behält die bisherige Auswahl bei und erklärt den Fehler.
        } // Beendet die Fehlerbehandlung.
    } // Beendet das Anlegen eines Looks.

    public String updateProgress(int level, int totalXp) { // Übernimmt Fortschritt nach dem Laden oder einem erfolgreichen Check-in.
        this.currentLevel = level; this.totalXp = totalXp; // Hält die angezeigten XP synchron zur Score-Historie.
        String unlocked = ""; // Nur neue, erfolgreich gespeicherte Freischaltungen erzeugen eine Erfolgsmeldung.
        if (available && level > profile.highestLevel()) { // Schreibt nur beim Überschreiten des bisherigen persönlichen Höchststands.
            try { // Trennt Profilfehler von einer bereits erfolgreichen Tagesdatenspeicherung.
                AvatarProfile updated = profile.advanceTo(level); // Bewahrt frühere Auswahl und frühere Belohnungen.
                unlocked = updated.unlockedRewards().stream().filter(option -> !profile.isUnlocked(option)).map(AvatarOption::title).collect(Collectors.joining(", ")); // Berücksichtigt auch mehrere gleichzeitig überschrittene Schwellen.
                repository.save(updated); // Speichert die Freischaltungen, bevor sie als verdient bestätigt werden.
                profile = updated; // Übernimmt den gesicherten Höchststand.
                status.setForeground(GREEN); // Hebt den Erfolg hervor.
                status.setText(unlocked.isEmpty() ? "Neues höchstes Level erreicht. Deine bisherigen Extras bleiben erhalten." : "Neu freigeschaltet: " + unlocked + ". Wähle die Extras unten aus!"); // Verweist auf die direkt nutzbaren Belohnungen.
            } catch (RuntimeException exception) { // Lässt den gespeicherten Tages-Check-in trotz Profilfehler bestehen.
                unlocked = ""; // Meldet keine ungespeicherten Freischaltungen als Erfolg.
                status.setForeground(new Color(151, 60, 46)); // Kennzeichnet das Problem im Avatar-Bereich.
                status.setText("Die Tagesdaten bleiben gespeichert. " + exception.getMessage()); // Erklärt, welcher Teil erfolgreich war.
            } // Beendet die Profilfehlerbehandlung.
        } // Beendet die Fortschrittsprüfung.
        render(); // Aktualisiert auch ohne Levelwechsel die fehlenden XP zum nächsten Extra.
        return unlocked; // Ermöglicht dem Check-in eine unmittelbare Freischaltungsbenachrichtigung.
    } // Beendet die Fortschrittsübernahme.

    public String milestoneText() { // Liefert eine kurze Zusammenfassung für das Dashboard.
        if (!available) { return "Avatar-Profil bitte prüfen"; } // Verschweigt einen Lesefehler nicht.
        AvatarOption next = nextOption(); // Sucht die nächste noch nicht verdiente Belohnung.
        return next == null ? "Alle " + profile.unlockedRewards().size() + " Extras freigeschaltet" : "Nächste Extras ab Level " + next.requiredLevel(); // Zeigt ein erreichbares Ziel statt nur eines Belohnungsnamens.
    } // Beendet den Dashboard-Hinweis.

    private AvatarOption nextOption() { // Findet die früheste noch gesperrte Schwelle im gemeinsamen Katalog.
        return Arrays.stream(AvatarOption.values()).filter(option -> !profile.isUnlocked(option)).min(Comparator.comparingInt(AvatarOption::requiredLevel)).orElse(null); // Funktioniert auch nach dem Freischalten aller Extras.
    } // Beendet die Zielsuche.

    private void render() { // Aktualisiert jede Darstellung aus demselben gespeicherten Profil.
        preview.setProfile(profile); // Zeigt Figur, Hintergrund, Rahmen und Accessoire gemeinsam.
        onChange.accept(profile); // Aktualisiert den auf jeder Seite sichtbaren Avatar.
        name.setText("Dein " + (profile.character() == AvatarOption.CAT ? "Katzen-Avatar" : profile.character().title() + "-Avatar")); // Benennt die aktuell angelegte Figur verständlich.
        progress.setText("Level " + currentLevel + " · " + totalXp + " XP"); // Zeigt das tatsächliche aktuelle Level.
        collection.setText(profile.unlockedRewards().size() + " von 6 Extras freigeschaltet"); // Zählt die sechs zusätzlichen Looks unabhängig von der aktuellen Auswahl.
        AvatarOption next = nextOption(); // Sucht das nächste Fortschrittsziel.
        nextReward.setText(next == null ? "Deine Sammlung ist vollständig! Alle Looks bleiben frei kombinierbar." : "Nächstes Ziel: Level " + next.requiredLevel() + " · noch " + Math.max(0, next.requiredXp() - totalXp) + " XP. Verdiente Extras bleiben auch nach Tageskorrekturen erhalten."); // Erklärt sowohl Ziel als auch dauerhafte Freischaltungen.
        for (Map.Entry<AvatarOption, JButton> entry : choices.entrySet()) { // Aktualisiert alle gesperrten, verfügbaren und angelegten Looks.
            AvatarOption option = entry.getKey(); JButton choice = entry.getValue(); // Liest Option und ihre Schaltfläche gemeinsam.
            boolean unlocked = available && profile.isUnlocked(option); // Berücksichtigt auch einen Fehler beim Laden des Profils.
            boolean selected = profile.selected(option.slot()) == option; // Markiert pro Kategorie genau eine aktive Auswahl.
            String detail = !unlocked ? "Gesperrt · Level " + option.requiredLevel() + " · " + option.requiredXp() + " XP" : selected ? "Angelegt" : option.requiredLevel() == 1 ? "Von Anfang an verfügbar" : "Freigeschaltet · Anlegen"; // Erklärt die Bedeutung jedes Zustands mit Text.
            choice.setText("<html><center style='color:" + (unlocked ? "#1c342b" : "#5b6d64") + "'><b>" + option.title() + "</b><br><span style='font-size:9px'>" + detail + "</span></center></html>"); // Hält auch gesperrte Belohnungsnamen kontrastreich und vollständig lesbar.
            AvatarProfile look = profile.advanceTo(Math.max(profile.highestLevel(), option.requiredLevel())).equip(option); // Baut ausschließlich für die Vorschau einen möglichen Look; diese Kopie wird nie gespeichert.
            choice.setIcon(AvatarView.icon(look, 54)); // Zeigt, was sich durch diese konkrete Option verändert.
            choice.setDisabledIcon(AvatarView.icon(look, 54)); // Lässt gesperrte Belohnungen als erkennbare Vorschau sichtbar.
            choice.setEnabled(unlocked); // Verhindert Auswahl noch nicht verdienter Extras.
            choice.setBackground(selected ? PALE : Color.WHITE); // Kennzeichnet den tatsächlich angelegten Look.
            choice.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(selected ? GREEN : LINE, selected ? 2 : 1), new EmptyBorder(5, 5, 5, 5))); // Ergänzt zur Textmarkierung einen sichtbaren Auswahlrahmen.
            choice.getAccessibleContext().setAccessibleName(option.title() + ": " + detail); // Macht Sperren und Auswahl für Hilfstechnologien verständlich.
            choice.setToolTipText(option.title() + " · " + detail); // Zeigt den vollständigen Status auch beim Darüberfahren.
        } // Beendet die Aktualisierung aller Looks.
    } // Beendet die gemeinsame Darstellung.
} // Beendet das Avatar-Studio.
