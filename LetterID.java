import java.util.Map;

import javax.imageio.ImageIO;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

public class LetterID {
    private Map<int[][], Character> blueprints;
    private int LETTER_SIZE_BASELINE = 28; // square letters
    private int letterHalfSize;
    private Robot bot;

    public LetterID(Robot bot) {
        // setup blueprints (MANUAL)
        this.bot = bot;
        blueprints = new HashMap<>();
        addLetterBlueprints();
    }

    // Adjusts the sizes of the letters by ratio
    public void computeLetterSize(double ratio) {
        letterHalfSize = (int) (LETTER_SIZE_BASELINE * ratio);
        if (letterHalfSize == 0) {
            letterHalfSize = LETTER_SIZE_BASELINE;
        }
    }

    // Identify letter and return the character corresponding to letter
    public char identifyLetter(int x, int y) {
        char letter = identify(prepID(x, y));
        return letter;

    }

    // Prepares the letter to be ID'd
    private int[][] prepID(int letterX, int letterY) {
        BufferedImage img = cropLetter(letterX, letterY);
        return makeLetterArray(img);
    }

    // Crops image of letter to be as fit as possible
    private BufferedImage cropLetter(int letterX, int letterY) {
        BufferedImage initialImage = bot.createScreenCapture(new Rectangle(letterX - letterHalfSize,
                letterY - letterHalfSize, letterHalfSize * 2, letterHalfSize * 2));
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

        BufferedImage img;
        try {
            img = initialImage.getSubimage(firstX, firstY, letterWidth, letterHeight);
        } catch (RasterFormatException e) {
            img = initialImage;
        }
        return img;
    }

    // Makes the 10x10 letter array from the cropped image
    private int[][] makeLetterArray(BufferedImage img) {
        // letter arrays of 10x10
        int[][] letterArr = new int[10][10];
        // traverse image in 10 jumps
        double xIncrement = img.getWidth() / 10.0;
        double yIncrement = img.getHeight() / 10.0;
        for (int y = 0; y < 10; y++) {
            // System.out.print("\n");
            for (int x = 0; x < 10; x++) {
                letterArr[x][y] = ((img.getRGB((int) (xIncrement * x), (int) (yIncrement * y)) == 0xFFFFFFFF) ? 1 : 0);
                // System.out.print(((img.getRGB((int) (xIncrement * x), (int) (yIncrement * y))
                // == 0xFFFFFFFF)
                // ? "\u001B[31m" + "1" + "\u001B[0m"
                // : "0"));
            }
        }
        // System.out.print("\n");

        // String p = Arrays.deepToString(letterArr);
        // p = p.replaceAll("\\[", "{");
        // p = p.replaceAll("]", "}");
        // System.out.println(p);

        return letterArr;
    }

    // Identify letterArr by finding closest match from the blueprints
    private char identify(int[][] letterArr) {
        int maxPoints = 0;
        char letter = 'z';
        for (int[][] blueLetter : blueprints.keySet()) {
            int points = 0;
            for (int y = 0; y < 10; y++) {
                for (int x = 0; x < 10; x++) {
                    if (letterArr[x][y] == blueLetter[x][y]) {
                        points++;
                    }
                }
            }
            if (maxPoints < points) {
                maxPoints = points;
                letter = blueprints.get(blueLetter);
            }
        }
        System.out.println("Letter: " + letter +
                " | Confidence: " + maxPoints + "%");
        return letter;
    }

    // Letter blueprints
    private void addLetterBlueprints() {
        int[][] a = { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 1, 1, 1 },
                { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 }, { 0, 0, 1, 1, 1, 1, 1, 1, 0, 0 }, { 1, 1, 1, 1, 1, 0, 0, 1, 0, 0 },
                { 1, 1, 1, 0, 0, 0, 0, 1, 0, 0 }, { 1, 1, 1, 1, 1, 1, 0, 1, 0, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 0 },
                { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 } };
        blueprints.put(a, 'a');
        int[][] b = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 },
                { 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 }, { 1, 1, 1, 0, 1, 1, 0, 0, 1, 1 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 1, 1, 0, 0, 1, 1, 1, 1 } };
        blueprints.put(b, 'b');
        int[][] c = { { 0, 0, 0, 0, 1, 1, 1, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 0, 0, 0, 0, 1, 1, 1 }, { 0, 1, 1, 0, 0, 0, 0, 0, 1, 1 },
                { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 },
                { 0, 1, 1, 0, 0, 0, 0, 0, 1, 1 }, { 0, 1, 1, 0, 0, 0, 0, 0, 1, 1 } };
        blueprints.put(c, 'c');
        int[][] d = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 0, 0, 0, 0, 0, 0, 1 }, { 1, 1, 1, 0, 0, 0, 0, 0, 0, 1 },
                { 1, 1, 1, 0, 0, 0, 0, 0, 0, 1 }, { 0, 1, 1, 0, 0, 0, 0, 0, 1, 1 }, { 0, 1, 1, 1, 0, 0, 0, 1, 1, 1 },
                { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 1, 1, 1, 1, 1, 1, 0 } };
        blueprints.put(d, 'd');
        int[][] e = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 },
                { 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 }, { 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 }, { 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 },
                { 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 0, 1 } };
        blueprints.put(e, 'e');
        int[][] f = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 0, 0, 1, 1, 0, 0, 0 },
                { 1, 1, 0, 0, 0, 1, 1, 0, 0, 0 }, { 1, 1, 0, 0, 0, 1, 1, 0, 0, 0 }, { 1, 1, 0, 0, 0, 1, 1, 0, 0, 0 },
                { 1, 1, 0, 0, 0, 1, 1, 0, 0, 0 }, { 1, 1, 0, 0, 0, 1, 1, 0, 0, 0 } };
        blueprints.put(f, 'f');
        int[][] g = { { 0, 0, 0, 0, 1, 1, 1, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 0, 0, 0, 0, 1, 1, 1 }, { 1, 1, 1, 0, 0, 0, 0, 0, 1, 1 },
                { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 }, { 1, 1, 0, 0, 0, 1, 1, 0, 1, 1 },
                { 0, 1, 1, 0, 0, 1, 1, 1, 1, 1 }, { 0, 1, 0, 0, 0, 1, 1, 1, 1, 1 } };
        blueprints.put(g, 'g');
        int[][] h = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 }, { 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 }, { 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 }, { 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 } };
        blueprints.put(h, 'h');
        int[][] i = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } };
        blueprints.put(i, 'i');
        int[][] j = { { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 1, 1, 1 },
                { 0, 0, 0, 0, 0, 0, 0, 1, 1, 1 }, { 1, 1, 0, 0, 0, 0, 0, 1, 1, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 },
                { 1, 1, 0, 0, 0, 0, 0, 0, 0, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } };
        blueprints.put(j, 'j');
        int[][] k = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0 },
                { 0, 0, 0, 1, 1, 1, 1, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 0 }, { 0, 1, 1, 1, 1, 0, 1, 1, 1, 1 },
                { 1, 1, 1, 0, 0, 0, 0, 1, 1, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 0, 1 } };
        blueprints.put(k, 'k');
        int[][] l = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 } };
        blueprints.put(l, 'l');
        int[][] m = { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 }, { 0, 0, 0, 1, 1, 1, 1, 1, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 1, 1, 0 }, { 0, 0, 0, 1, 1, 1, 1, 1, 0, 0 }, { 0, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } };
        blueprints.put(m, 'm');
        int[][] n = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 0, 0, 0, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 1, 1, 1, 0, 0, 0 }, { 0, 0, 0, 0, 0, 1, 1, 1, 1, 0 }, { 0, 0, 0, 0, 0, 0, 0, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } };
        blueprints.put(n, 'n');
        int[][] o = { { 0, 0, 0, 0, 1, 1, 1, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 0, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 0, 0, 0, 0, 0, 1, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 },
                { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 1, 1, 0, 0, 0, 0, 0, 1, 1 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 0, 0 } };
        blueprints.put(o, 'o');
        int[][] p = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 0, 0, 1, 1, 0, 0, 0 }, { 1, 1, 0, 0, 0, 1, 1, 0, 0, 0 },
                { 1, 1, 1, 0, 0, 1, 1, 0, 0, 0 }, { 1, 1, 1, 0, 0, 1, 1, 0, 0, 0 }, { 0, 1, 1, 1, 1, 1, 1, 0, 0, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 } };
        blueprints.put(p, 'p');
        int[][] q = { { 0, 0, 0, 0, 1, 1, 1, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 0, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 0, 0, 0, 0, 0, 1, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 },
                { 1, 1, 0, 0, 0, 0, 0, 0, 1, 1 }, { 1, 1, 0, 0, 0, 0, 1, 1, 1, 1 }, { 0, 1, 1, 0, 0, 0, 1, 1, 1, 1 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 } };
        blueprints.put(q, 'q');
        int[][] r = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 0, 0, 1, 1, 0, 0, 0 }, { 1, 1, 1, 0, 0, 1, 1, 0, 0, 0 },
                { 1, 1, 1, 0, 0, 1, 1, 1, 0, 0 }, { 1, 1, 1, 0, 0, 1, 1, 1, 1, 0 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 0, 1, 1, 1, 1, 1, 0, 1, 1, 1 }, { 0, 0, 0, 1, 1, 0, 0, 0, 0, 1 } };
        blueprints.put(r, 'r');
        int[][] s = { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 1, 1, 1, 0, 0, 0, 1, 1 },
                { 0, 1, 1, 1, 1, 0, 0, 0, 1, 1 }, { 0, 1, 1, 1, 1, 1, 0, 0, 1, 1 }, { 1, 1, 0, 0, 1, 1, 0, 0, 1, 1 },
                { 1, 1, 0, 0, 1, 1, 0, 0, 1, 1 }, { 1, 1, 0, 0, 1, 1, 1, 0, 1, 1 }, { 1, 1, 0, 0, 0, 1, 1, 0, 1, 1 },
                { 0, 1, 1, 0, 0, 1, 1, 1, 1, 1 }, { 0, 1, 0, 0, 0, 0, 1, 1, 1, 0 } };
        blueprints.put(s, 's');
        int[][] t = { { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 } };
        blueprints.put(t, 't');
        int[][] u = { { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 } };
        blueprints.put(u, 'u');
        int[][] v = { { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 1, 0, 0, 0, 0, 0, 0, 0 },
                { 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 0, 0 }, { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 }, { 0, 0, 1, 1, 1, 1, 1, 1, 0, 0 },
                { 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 }, { 1, 1, 1, 0, 0, 0, 0, 0, 0, 0 } };
        blueprints.put(v, 'v');
        int[][] w = { { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
                { 0, 0, 0, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 1, 1, 1, 1, 1, 1, 0, 0 },
                { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 },
                { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 } };
        blueprints.put(w, 'w');
        int[][] x = { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
                { 1, 1, 1, 0, 0, 0, 0, 0, 1, 1 }, { 1, 1, 1, 1, 1, 0, 1, 1, 1, 1 }, { 0, 0, 1, 1, 1, 1, 1, 1, 0, 0 },
                { 0, 0, 0, 0, 1, 1, 1, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 0, 0 }, { 1, 1, 1, 1, 1, 0, 1, 1, 1, 1 },
                { 1, 1, 1, 0, 0, 0, 0, 0, 1, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 0, 1 } };
        blueprints.put(x, 'x');
        int[][] y = { { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 1, 0, 0, 0, 0, 0, 0, 0 },
                { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0 }, { 0, 1, 1, 1, 1, 0, 0, 0, 0, 0 }, { 0, 0, 0, 1, 1, 1, 1, 1, 1, 1 },
                { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 },
                { 1, 1, 1, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 } };
        blueprints.put(y, 'y');

        // int[][] z = {{1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        // {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 0, 0, 0, 0, 0, 0, 1}, {1, 1, 1, 0,
        // 0, 0, 0, 0, 0, 1}, {1, 1, 1, 0, 0, 0, 0, 0, 0, 1}, {0, 1, 1, 0, 0, 0, 0, 0,
        // 1, 1}, {0, 1, 1, 1, 0, 0, 0, 1, 1, 1}, {0, 0, 1, 1, 1, 1, 1, 1, 1, 1}, {0, 0,
        // 0, 1, 1, 1, 1, 1, 1, 0}};
        // blueprints.put(z, 'z');
    }
}
