package at.lifescore.model; 

import java.time.LocalDate; 

public record DailyEntry(LocalDate date, int score, int earnedXp, DailyAnswers answers) { 
    public DailyEntry(LocalDate date, int score, int earnedXp) { 
        this(date, score, earnedXp, null); 
    } 
    public DailyEntry { 
        if (date == null) { 
            throw new IllegalArgumentException("Das Datum darf nicht fehlen."); 
        } 
        if (score < 0 || score > 100) { 
            throw new IllegalArgumentException("Der Score muss zwischen 0 und 100 liegen."); 
        } 
        if (earnedXp < 0) { 
            throw new IllegalArgumentException("XP dürfen nicht negativ sein."); 
        } 
    } 
} 
