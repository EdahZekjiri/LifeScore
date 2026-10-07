package at.lifescore.model; // Ordnet das Datenmodell dem Model-Paket zu.

import java.time.LocalDate; // Stellt ein Datum ohne Uhrzeit bereit.

public record DailyEntry(LocalDate date, int score, int earnedXp) { // Speichert Datum, Score und verdiente Erfahrungspunkte unveränderlich.
    public DailyEntry { // Führt Prüfungen bei jeder Erstellung eines Eintrags aus.
        if (date == null) { // Prüft, ob ein gültiges Datum vorhanden ist.
            throw new IllegalArgumentException("Das Datum darf nicht fehlen."); // Bricht bei einem fehlenden Datum mit verständlicher Meldung ab.
        } // Beendet die Datumsprüfung.
        if (score < 0 || score > 100) { // Prüft, ob der Score im erlaubten Bereich liegt.
            throw new IllegalArgumentException("Der Score muss zwischen 0 und 100 liegen."); // Verhindert ungültige Score-Werte.
        } // Beendet die Score-Prüfung.
        if (earnedXp < 0) { // Prüft, ob die Erfahrungspunkte negativ sind.
            throw new IllegalArgumentException("XP dürfen nicht negativ sein."); // Verhindert ungültige Erfahrungspunkte.
        } // Beendet die XP-Prüfung.
    } // Beendet den kompakten Record-Konstruktor.
} // Beendet den Record DailyEntry.

