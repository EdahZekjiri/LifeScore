package at.lifescore.service; 

import java.util.List;

import at.lifescore.model.DailyEntry; 

public final class ScoreService { 
    public int calculateScore(int sleep, int movement, int nutrition, int productivity, int social) { 
        validateRating(sleep); 
        validateRating(movement); 
        validateRating(nutrition); 
        validateRating(productivity); 
        validateRating(social); 
        double weighted = sleep * 0.25 + movement * 0.20 + nutrition * 0.20 + productivity * 0.20 + social * 0.15; 
        return (int) Math.round(weighted * 10.0); 
    } 

    public int earnedXpFor(int score) { 
        return 10 + score / 10; 
    } 

    public int totalXp(List<DailyEntry> entries) { 
        return entries.stream().mapToInt(DailyEntry::earnedXp).sum(); 
    } 

    public int levelFor(int totalXp) { 
        return totalXp / 100 + 1;
    } 

    // Die Freischaltungen stehen zentral in AvatarOption und AvatarProfile; hier bleibt nur die Score- und XP-Berechnung.

    private void validateRating(int rating) { 
        if (rating < 0 || rating > 10) { 
            throw new IllegalArgumentException("Bewertungen müssen zwischen 0 und 10 liegen."); 
        } 
    } 
} 

