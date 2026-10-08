package at.lifescore.model; // Beschreibt alle auswählbaren Avatar-Teile an einer zentralen Stelle.

public enum AvatarOption { // Ein gemeinsamer Katalog verbindet Gestaltung, Freischaltung und Speicherung.
    FOX(Slot.CHARACTER, "Fuchs", 1), // Der Fuchs steht von Anfang an zur Verfügung.
    CAT(Slot.CHARACTER, "Katze", 1), // Eine zweite Figur ist auch ohne Erfahrungspunkte wählbar.
    BEAR(Slot.CHARACTER, "Bär", 1), // Die dritte kostenlose Figur macht den Avatar sofort anpassbar.
    CREAM(Slot.BACKGROUND, "Morgenlicht", 1), // Der warme Standardhintergrund ist immer verfügbar.
    FOREST(Slot.BACKGROUND, "Waldgrün", 3), // Setzt die ursprünglich versprochene Level-3-Belohnung sichtbar um.
    NIGHT(Slot.BACKGROUND, "Sternennacht", 4), // Belohnt weiteren Fortschritt mit einem zweiten Hintergrund.
    PLAIN(Slot.FRAME, "Ohne Rahmen", 1), // Erlaubt jederzeit eine schlichte Darstellung.
    LEAF(Slot.FRAME, "Salbeirahmen", 2), // Der erste zusätzliche Rahmen wird bei 100 XP verfügbar.
    GOLD(Slot.FRAME, "Goldener Fokus", 5), // Setzt die ursprüngliche Level-5-Belohnung als sichtbaren Goldrahmen um.
    NO_ACCESSORY(Slot.ACCESSORY, "Ohne Accessoire", 1), // Ermöglicht auch nach Freischaltungen das Ablegen eines Extras.
    SCARF(Slot.ACCESSORY, "Lieblingsschal", 2), // Der erste Levelaufstieg schaltet einen tragbaren Schal frei.
    CROWN(Slot.ACCESSORY, "Kleine Krone", 7); // Die Krone bildet einen weiteren langfristigen Meilenstein.

    public enum Slot { CHARACTER, BACKGROUND, FRAME, ACCESSORY } // Jeder Avatar besitzt genau eine Auswahl pro Kategorie.
    private final Slot slot; // Verhindert beispielsweise die Verwendung einer Krone als Hintergrund.
    private final String title; // Hält die deutsche Bezeichnung für alle Ansichten bereit.
    private final int requiredLevel; // Verbindet jedes Extra mit einer eindeutigen Freischaltschwelle.

    AvatarOption(Slot slot, String title, int requiredLevel) { // Initialisiert einen Eintrag im festen Belohnungskatalog.
        this.slot = slot; // Speichert die Kategorie.
        this.title = title; // Speichert den sichtbaren Namen.
        this.requiredLevel = requiredLevel; // Speichert das benötigte Level.
    } // Beendet die Kataloginitialisierung.
    public Slot slot() { return slot; } // Gibt die Kategorie für Filter und Validierung zurück.
    public String title() { return title; } // Gibt den sichtbaren Namen zurück.
    public int requiredLevel() { return requiredLevel; } // Gibt die Freischaltschwelle zurück.
    public int requiredXp() { return (requiredLevel - 1) * 100; } // Übersetzt dieselbe Levelregel wie ScoreService in benötigte XP.
} // Beendet den Avatar-Katalog.
