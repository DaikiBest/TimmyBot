import java.util.Map;

import javax.imageio.ImageIO;

import java.util.List;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.HashMap;

public class LetterID {
    public Map<int[][], Character> blueprints;
    private int LETTER_SIZE_BASELINE = 25; // square letters
    private int letter_size;

    public LetterID() {
        // setup blueprints (MANUAL)
        blueprints = new HashMap<>();
        addLetters();
    }

    public void computeLetterSize(double ratio) {
        letter_size = (int) (LETTER_SIZE_BASELINE * ratio);
        if (letter_size == 0) {
            letter_size = LETTER_SIZE_BASELINE;
        }
    }

    public char identifyLetter(int x, int y, Robot bot) {
        char letter = findClosestMatch(scorePoints(x, y, bot));
        // System.out.print("\tLetter: " + letter + "\n");
        return letter;

    }

    private String scorePoints(int letterX, int letterY, Robot bot) {
        BufferedImage img = cropLetter(letterX, letterY, bot);
        makeLetterArray(img);

        return "5;5";
    }

    private BufferedImage cropLetter(int letterX, int letterY, Robot bot) {
        BufferedImage initialImage = bot.createScreenCapture(new Rectangle(letterX - letter_size,
                letterY - letter_size, letter_size * 2, letter_size * 2));
        int firstX = 0;
        int firstY = 0;
        boolean countingWidth = false;
        boolean countingHeight = false;
        int letterWidth = 0;
        int letterHeight = 0;
        for (int var = 0; var < initialImage.getWidth(); var++) {
            // Column traversal: var = x
            for (int y = 0; y < initialImage.getHeight(); y++)
                if (initialImage.getRGB(var, y) == 0xFFFFFFFF) {
                    countingWidth = true;
                    if (firstX == 0)
                        firstX = var; // set firstX once
                }

            // Row traversal: var = y
            for (int x = 0; x < initialImage.getHeight(); x++)
                if (initialImage.getRGB(x, var) == 0xFFFFFFFF) {
                    countingHeight = true;
                    if (firstY == 0)
                        firstY = var; // set firstX once
                }

            if (countingWidth)
                letterWidth++;
            if (countingHeight)
                letterHeight++;
            countingWidth = false;
            countingHeight = false;
        }

        BufferedImage img = initialImage.getSubimage(firstX, firstY, letterWidth, letterHeight);
        return img;

        // File file = new File(letterX + letterY + ".png");
        // try {
        // ImageIO.write(img, "PNG", file);

        // } catch (Exception e) {
        // // TODO: handle exception
        // }
    }

    private void makeLetterArray(BufferedImage img) {
        // letter arrays of 10x10
        int[][] letterArr = new int[10][10];
        // traverse image in 10 jumps
        double xIncrement = img.getWidth() / 10.0;
        double yIncrement = img.getHeight() / 10.0;
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                letterArr[x][y] = ((img.getRGB((int) (xIncrement * x), (int) (yIncrement * y)) == 0xFFFFFFFF) ? 1 : 0);
                // System.out.print(((img.getRGB((int) (xIncrement * x), (int) (yIncrement * y))
                // == 0xFFFFFFFF) ? "\u001B[31m" + "1" + "\u001B[0m" : "0"));
            }
            // System.out.print("\n");
        }
        // System.out.println("\n");
        String p = Arrays.deepToString(letterArr);
        p = p.replaceAll("\\[", "{");
        p = p.replaceAll("]", "}");
        System.out.println(p);
    }

    private char findClosestMatch(String factors) {
        return 'a';
    }

    private void addLetters() {
        int[][] g = { { 0, 0, 0, 0, 1, 1, 1, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 0, 0, 0, 0, 1, 1, 1 }, { 1, 1, 1, 0, 0, 0, 0, 0, 1, 1 },
                { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 }, { 1, 1, 0, 0, 0, 1, 1, 0, 1, 1 },
                { 0, 1, 1, 0, 0, 1, 1, 1, 1, 1 }, { 0, 1, 0, 0, 0, 1, 1, 1, 1, 1 } };
        blueprints.put(g, 'g');
        int[][] o = { { 0, 0, 0, 0, 1, 1, 1, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 0, 0, 0, 0, 0, 1, 1 }, { 0, 1, 1, 0, 0, 0, 0, 0, 1, 1 },
                { 1, 1, 0, 0, 0, 0, 0, 0, 0, 1 }, { 0, 1, 1, 0, 0, 0, 0, 0, 1, 1 }, { 0, 1, 1, 0, 0, 0, 0, 0, 1, 1 },
                { 0, 0, 1, 1, 1, 1, 1, 1, 1, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 0 } };
        blueprints.put(o, 'o');
        int[][] m = { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 }, { 0, 0, 0, 1, 1, 1, 1, 1, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 1, 1, 0 }, { 0, 0, 0, 1, 1, 1, 1, 1, 0, 0 }, { 0, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } };
        blueprints.put(m, 'm');
        int[][] l = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 } };
        blueprints.put(l, 'l');
        int[][] a = { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 1, 1, 1 },
                { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 }, { 0, 0, 1, 1, 1, 1, 1, 1, 0, 0 }, { 1, 1, 1, 1, 1, 0, 0, 1, 0, 0 },
                { 1, 1, 1, 0, 0, 0, 0, 1, 0, 0 }, { 1, 1, 1, 1, 1, 1, 0, 1, 0, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 0 },
                { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 } };
        blueprints.put(a, 'a');
        int[][] u = { { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 } };
        blueprints.put(u, 'u');
        int[][] r = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 0, 0, 1, 1, 0, 0, 0 }, { 1, 1, 1, 0, 0, 1, 1, 0, 0, 0 },
                { 1, 1, 1, 0, 0, 1, 1, 1, 0, 0 }, { 1, 1, 1, 0, 0, 1, 1, 1, 1, 0 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 0, 1, 1, 1, 1, 1, 0, 1, 1, 1 }, { 0, 0, 0, 1, 1, 0, 0, 0, 0, 1 } };
        blueprints.put(r, 'r');
    }
}
