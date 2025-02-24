import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

public class LetterID {
    public Map<Integer, Character> blueprints;
    private static final int LETTER_HALF_WIDTH = 25;
    private static final int LETTER_HALF_HEIGHT = 25;

    public LetterID() {
        // setup blueprints (MANUAL)
        blueprints = new HashMap<>();
        blueprints.put(35904, 'i');
        blueprints.put(93031, 'c');
        blueprints.put(98513, 'y');
        blueprints.put(103194, 'f');
        blueprints.put(123801, 't'); //139287
        blueprints.put(111276, 'e');
        blueprints.put(134633, 'a');
        blueprints.put(166375, 'b');
        blueprints.put(171684, 'o');
        blueprints.put(191484, 'r');
        blueprints.put(201492, 'd');
        blueprints.put(266945, 'h');
        blueprints.put(272571, 'n');
        blueprints.put(340474, 'm');
        blueprints.put(374040, 'w');
    }

    public char identifyLetter(int x, int y, Robot bot) {
        return findClosestMatch(scorePoints(x, y, bot));
    }

    private int scorePoints(int letterX, int letterY, Robot bot) {
        // int whiteScore = 0; // number of white pixels
        int columnScore = 0;
        int colConsScore = 0; // column consecutive scoring
        int letterWidth = 0;
        boolean countingWidth = false;
        // int rowScore = 0;
        // int rowConsScore = 0; // row consecutive scoring

        BufferedImage img = bot
                .createScreenCapture(new Rectangle(letterX - LETTER_HALF_WIDTH, letterY - LETTER_HALF_HEIGHT,
                        LETTER_HALF_WIDTH * 2, LETTER_HALF_HEIGHT * 2));

        for (int var = 0; var < img.getWidth(); var++) { // REQUIRES: IMG WIDTH = HEIGHT!!!
            // Column wise scoring (var = x)
            for (int y = 0; y < img.getHeight(); y++) {

                if (img.getRGB(var, y) == 0xFFFFFFFF) {
                    columnScore += colConsScore;
                    colConsScore++;

                    countingWidth = true;
                    // System.out.print(colConsScore + " ");
                } else {
                    // System.out.print("  ");
                    colConsScore = 0;
                }
            }

            if (countingWidth) {
                letterWidth++;
                countingWidth = false;
            }
        }

        // File file = new File("me" + letterX + letterY + ".png");
        // try {
        //     ImageIO.write(img, "PNG", file);

        // } catch (Exception e) {
        //     // TODO: handle exception
        // }

        System.out.print("Column: " + columnScore + "\tWidth: " + letterWidth + "\tTotal: " + (columnScore * letterWidth));
        // System.out.print("Column: " + columnScore + "\tRow: " + rowScore + "\tWhite: " + whiteScore +
        //         "\tTotal: " + ((columnScore - rowScore) / whiteScore));
        // weight of # white
        return columnScore * letterWidth;
    }

    private char findClosestMatch(int score) {
        int distance = 0;
        int minDistance = Integer.MAX_VALUE;
        int minScore = 0;
        for (int letterScore : blueprints.keySet()) {
            distance = Math.abs(score - letterScore);
            if (distance < minDistance) {
                minDistance = distance;
                minScore = letterScore;
            }
        }

        System.out.print(" " + blueprints.get(minScore) + "\n");
        return blueprints.get(minScore);
    }
}
