package at.lifescore.service; // Ordnet die Berechnungen dem Service-Paket zu.

import at.lifescore.model.DailyEntry; // Macht Tagesdatensätze für die XP-Auswertung verfügbar.
import java.util.List; // Stellt eine geordnete Sammlung von Einträgen bereit.

public final class ScoreService { // Bündelt die fachlichen Berechnungen der Anwendung.
    public int calculateScore(int sleep, int movement, int nutrition, int productivity, int social) { // Berechnet aus fünf Bewertungen einen gewichteten Gesamtscore.
        validateRating(sleep); // Prüft die Schlafbewertung.
        validateRating(movement); // Prüft die Bewegungsbewertung.
        validateRating(nutrition); // Prüft die Ernährungsbewertung.
        validateRating(productivity); // Prüft die Produktivitätsbewertung.
        validateRating(social); // Prüft die Sozialbewertung.
        double weighted = sleep * 0.25 + movement * 0.20 + nutrition * 0.20 + productivity * 0.20 + social * 0.15; // Verknüpft die Bereiche mit ihren fachlichen Gewichtungen.
        return (int) Math.round(weighted * 10.0); // Normalisiert das Ergebnis auf die Skala von 0 bis 100.
    } // Beendet die Score-Berechnung.

    public int earnedXpFor(int score) { // Bestimmt die XP für einen abgeschlossenen Fragebogen.
        return 10 + score / 10; // Vergibt zehn Basis-XP plus bis zu zehn leistungsabhängige XP.
    } // Beendet die XP-Berechnung.

    public int totalXp(List<DailyEntry> entries) { // Addiert die bisher gespeicherten Erfahrungspunkte.
        return entries.stream().mapToInt(DailyEntry::earnedXp).sum(); // Summiert das XP-Feld aller Einträge.
    } // Beendet die XP-Summierung.

    public int levelFor(int totalXp) { // Ermittelt das Level anhand der gesamten XP.
        return totalXp / 100 + 1; // Startet bei Level eins und erhöht alle 100 XP das Level.
    } // Beendet die Level-Berechnung.

    // Die Freischaltungen stehen zentral in AvatarOption und AvatarProfile; hier bleibt nur die Score- und XP-Berechnung.

    private void validateRating(int rating) { // Prüft einen einzelnen Fragebogenwert.
        if (rating < 0 || rating > 10) { // Erkennt Werte außerhalb der vorgesehenen Skala.
            throw new IllegalArgumentException("Bewertungen müssen zwischen 0 und 10 liegen."); // Bricht bei einer ungültigen Bewertung ab.
        } // Beendet die Bereichsprüfung.
    } // Beendet die Hilfsmethode zur Validierung.
} // Beendet die Klasse ScoreService.

