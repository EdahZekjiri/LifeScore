package at.lifescore.model; // Ordnet die ursprünglichen Antworten dem Datenmodell zu.

public record DailyAnswers(int sleep, int movement, int nutrition, int productivity, int social) { // Hält die fünf Bewertungen für späteres Bearbeiten fest.
    public DailyAnswers { // Validiert auch Antworten, die aus einer Datei geladen werden.
        for (int value : new int[]{sleep, movement, nutrition, productivity, social}) { // Prüft jeden Lebensbereich mit derselben Regel.
            if (value < 0 || value > 10) { // Erlaubt nur die sichtbare Bewertungsskala.
                throw new IllegalArgumentException("Bewertungen müssen zwischen 0 und 10 liegen."); // Verhindert ungültige gespeicherte Antworten.
            } // Beendet die Bereichsprüfung.
        } // Beendet die Prüfung aller Bewertungen.
    } // Beendet die Validierung.
} // Beendet das Antwortmodell.
