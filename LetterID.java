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
        //setup blueprints (MANUAL)
        blueprints = new HashMap<>();
        blueprints.put(7614, 'i');
        blueprints.put(9597, 'l');
        blueprints.put(9731, 's');
        blueprints.put(11065, 'f');
        blueprints.put(11975, 't');
        blueprints.put(12504, 'a');
        blueprints.put(14199, 'e');
        blueprints.put(14287, 'k');
        blueprints.put(14418, 'g');
        blueprints.put(15328, 'o');
        blueprints.put(17008, 'r');
        blueprints.put(18561, 'b');
        blueprints.put(18846, 'n');
        blueprints.put(19791, 'm');
        blueprints.put(20730, 'w');
    }

    public char identifyLetter(int x, int y, Robot bot) {
        return findClosestMatch(scorePoints(x, y, bot));
    }

    private int scorePoints(int letterX, int letterY, Robot bot) {
        int columnScore = 0;
        int rowScore = 0;
        int colConsScore = 1; //column consecutive scoring
        int rowConsScore = 1; //row consecutive scoring
        BufferedImage img = bot.createScreenCapture(new Rectangle(letterX - LETTER_HALF_WIDTH, letterY - LETTER_HALF_HEIGHT,
                LETTER_HALF_WIDTH * 2, LETTER_HALF_HEIGHT * 2));
        
        for (int var = 0; var < img.getWidth(); var++) {
            
            //Column wise scoring (var = x)
            for (int y = 0; y < img.getHeight(); y++) {

                int pixel = img.getRGB(var, y);
                Color color = new Color(pixel, true);
                if (color.getRed() > 160 && color.getGreen() < 100 && color.getBlue() < 100) {
                    img.setRGB(var, y, Color.BLACK.getRGB());
                }
                
                if (Math.abs(img.getRGB(var, y) - 0xFFFFFFFF) <= 0x6A0000) {
                    columnScore += 1 * colConsScore;
                    colConsScore++;
                    // img.setRGB(var, y, 0x123456);
                    // bot.delay(20);
                    // bot.mouseMove(var + letterX - LETTER_HALF_WIDTH, y + letterY - LETTER_HALF_HEIGHT);
                } else {
                    colConsScore = 1;
                }
            }

            //Row wise scoring (var = y)
            for (int x = 0; x < img.getHeight(); x++) {

                int pixel = img.getRGB(x, var);
                Color color = new Color(pixel, true);
                if (color.getRed() > 160 && color.getGreen() < 100 && color.getBlue() < 100) {
                    img.setRGB(x, var, Color.BLACK.getRGB());
                }
                
                if (Math.abs(img.getRGB(x, var) - 0xFFFFFFFF) <= 0x6A0000) {
                    rowScore += 1 * rowConsScore;
                    rowConsScore++;
                    // img.setRGB(var, x, 0x123456);
                    // bot.delay(20);
                    // bot.mouseMove(x + letterX - LETTER_HALF_WIDTH, var + letterY - LETTER_HALF_HEIGHT);
                } else {
                    rowConsScore = 1;
                }
            }
        }

        File file = new File("me" + letterX + letterY + ".png");
        try {
            ImageIO.write(img, "PNG", file);
            
        } catch (Exception e) {
            // TODO: handle exception
        }

        System.out.print("Column score: " + columnScore + "\tRow score: " + rowScore + "\tTotal: " + (columnScore + rowScore));
        return columnScore + rowScore;
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
