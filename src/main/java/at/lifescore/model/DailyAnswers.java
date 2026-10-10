package at.lifescore.model; 

public record DailyAnswers(int sleep, int movement, int nutrition, int productivity, int social) { 
    public DailyAnswers { 
        for (int value : new int[]{sleep, movement, nutrition, productivity, social}) { 
            if (value < 0 || value > 10) { 
                throw new IllegalArgumentException("Bewertungen müssen zwischen 0 und 10 liegen."); 
            } 
        } 
    } 
} 
