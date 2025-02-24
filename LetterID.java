import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.util.List;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LetterID {
    public Map<String, Character> blueprints;
    private static final int LETTER_HALF_WIDTH = 25;
    private static final int LETTER_HALF_HEIGHT = 25;

    public LetterID() {
        // setup blueprints (MANUAL)
        blueprints = new HashMap<>();
        blueprints.put("159528;25866", 'i');
        blueprints.put("132183;138564", 'u');
        blueprints.put("148842;255430", 'w');
        
        blueprints.put("50580;206000", 'e');
        blueprints.put("45214;58812", 'y');
        
        blueprints.put("53729;153663", 'a');
        blueprints.put("53603;286585", 'o');
        blueprints.put("63557;128800", 't');
        blueprints.put("62055;59136", 'l'); //l sucks
        
        
        blueprints.put("91084;205378", 'r');
        blueprints.put("88575;189280", 'p');
        blueprints.put("75228;277062", 'b');
        blueprints.put("93800;262117", 'd');
        
        blueprints.put("63315;90209", 'v');
        blueprints.put("172620;225085", 'h');
        blueprints.put("63024;118982", 'f');
        blueprints.put("41540;286965", 'g');
        blueprints.put("183760;239162", 'm');
        blueprints.put("219052;207621", 'n');
        blueprints.put("19064;160416", 's');
        
        blueprints.put("25542;172032", 'c');
    }

    public char identifyLetter(int x, int y, Robot bot) {
        char letter = findClosestMatchScore1(scorePoints(x, y, bot));
        System.out.print("\tLetter: " + letter + "\n");
        return letter;

    }

    private String scorePoints(int letterX, int letterY, Robot bot) {
        int columnScore = 0;
        int colConsScore = 0; // column consecutive scoring
        
        int whiteScore = 0; // number of white pixels
        int rowScore = 0;
        int rowConsScore = 0; // row consecutive scoring
        
        int uninterruptedLengthSum = 0;
        int uninterruptedCount = 0;

        BufferedImage img = bot
                .createScreenCapture(new Rectangle(letterX - LETTER_HALF_WIDTH, letterY - LETTER_HALF_HEIGHT,
                        LETTER_HALF_WIDTH * 2, LETTER_HALF_HEIGHT * 2));

        for (int var = 0; var < img.getWidth(); var++) { // REQUIRES: IMG WIDTH = HEIGHT!!!
            
            // Column wise scoring (var = x)
            for (int y = 0; y < img.getHeight(); y++) {
                if (img.getRGB(var, y) == 0xFFFFFFFF) {
                    columnScore += colConsScore;
                    colConsScore++;
                    // System.out.print(colConsScore + " "); //COOL VIZ!
                } else {
                    if (colConsScore != 0) {
                        uninterruptedCount++;
                        uninterruptedLengthSum += colConsScore;
                    }
                    colConsScore = 0;
                    // System.out.print("  "); //COOL VIZ!
                }
            }

            //Row scoring: var = y
            for (int x = 0; x < img.getHeight(); x++) {
                if (img.getRGB(x, var) == 0xFFFFFFFF) {
                    rowScore += rowConsScore;
                    rowConsScore++;
                    whiteScore++;
                } else {
                    rowConsScore = 0;
                }
            }
        }

        int averageWidth = 0;
        if (uninterruptedCount != 0) {
            averageWidth = uninterruptedLengthSum / uninterruptedCount;
        }

        int score1 = columnScore * averageWidth;
        int score2 = rowScore * (whiteScore / 10);

        System.out.print("column " + columnScore + "row " + rowScore + "\tTotal: " + score1 + ";" + score2);
        return score1 + ";" + score2;
    }

    private char findClosestMatchScore1(String factors) {
        //parse string ("score-width")
        List<String> split = Arrays.asList(factors.split(";"));
        int score1 = Integer.valueOf(split.get(0));
        int score2 = Integer.valueOf(split.get(1));

        int distance = 0;
        int minDistance = Integer.MAX_VALUE;
        String minFactors = ""; //best-fit letter's factors
        for (String blueFactors : blueprints.keySet()) {
            //parse string ("score-width")
            List<String> blueSplit = Arrays.asList(blueFactors.split(";"));
            int blueScore1 = Integer.valueOf(blueSplit.get(0));

            distance = Math.abs(score1 - blueScore1);
            if (distance < minDistance) {
                minDistance = distance;
                minFactors = blueFactors;
            }
        }
        return handleSpecialCases(minFactors, score2);
    }

    private char handleSpecialCases(String factorsKey, int score2) {
        char letter = blueprints.get(factorsKey);
        if (letter == 'i' || letter == 'w' || letter == 'u') {
            List<Character> potentialLetters = Arrays.asList('i', 'w', 'u');
            return findClosestMatchScore2(potentialLetters, score2);
        } else if (letter == 'e' || letter == 'y') {
            List<Character> potentialLetters = Arrays.asList('a', 'o', 'e', 'y');
            return findClosestMatchScore2(potentialLetters, score2);
        } else if (letter == 'p' || letter == 'r' || letter == 'b' || letter == 'd') {
            List<Character> potentialLetters = Arrays.asList('p', 'r', 'b', 'd');
            return findClosestMatchScore2(potentialLetters, score2);
        } else if (letter == 'l' || letter == 't' || letter == 'a' || letter == 'o') {
            List<Character> potentialLetters = Arrays.asList('t', 'l', 'a', 'o');
            return findClosestMatchScore2(potentialLetters, score2);
        }
        return letter;
    }

    private char findClosestMatchScore2(List<Character> potentialLetters, int score2) {
        System.out.print("\t\tSecond Try!! ");
        int distance = 0;
        int minDistance = Integer.MAX_VALUE;
        String minFactors = ""; //minFactors is necessary to obtain the letter (using get(Key))

        for (String blueFactors : blueprints.keySet()) {
            //parse string ("score-width")
            List<String> blueSplit = Arrays.asList(blueFactors.split(";"));
            int blueScore2 = Integer.valueOf(blueSplit.get(1));

            // if curr letter is one of the potential letters
            if (potentialLetters.contains(blueprints.get(blueFactors))) {
                distance = Math.abs(score2 - blueScore2);
                if (distance < minDistance) {
                    minDistance = distance;
                    minFactors = blueFactors;
                }
            }
        }
        return blueprints.get(minFactors);
    }
}
