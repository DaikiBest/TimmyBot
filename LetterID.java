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
    private Map<int[][], Character> blueprints;
    private int LETTER_SIZE_BASELINE = 25; // square letters
    private int letter_size;
    private Robot bot;

    public LetterID(Robot bot) {
        // setup blueprints (MANUAL)
        this.bot = bot;
        blueprints = new HashMap<>();
        addLetters();
    }

    public void computeLetterSize(double ratio) {
        letter_size = (int) (LETTER_SIZE_BASELINE * ratio);
        if (letter_size == 0) {
            letter_size = LETTER_SIZE_BASELINE;
        }
    }

    public char identifyLetter(int x, int y) {
        char letter = identify(prepID(x, y));
        return letter;

    }

    private int[][] prepID(int letterX, int letterY) {
        BufferedImage img = cropLetter(letterX, letterY);
        return makeLetterArray(img);
    }

    private BufferedImage cropLetter(int letterX, int letterY) {
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

    private int[][] makeLetterArray(BufferedImage img) {
        // letter arrays of 10x10
        int[][] letterArr = new int[10][10];
        // traverse image in 10 jumps
        double xIncrement = img.getWidth() / 10.0;
        double yIncrement = img.getHeight() / 10.0;
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                letterArr[x][y] = ((img.getRGB((int) (xIncrement * x), (int) (yIncrement * y)) == 0xFFFFFFFF) ? 1 : 0);
                System.out.print(((img.getRGB((int) (xIncrement * x), (int) (yIncrement * y)) == 0xFFFFFFFF)
                        ? "\u001B[31m" + "1" + "\u001B[0m"
                        : "0"));
            }
            System.out.print("\n");
        }
        System.out.println("\n");

        String p = Arrays.deepToString(letterArr);
        p = p.replaceAll("\\[", "{");
        p = p.replaceAll("]", "}");
        System.out.println(p);

        return letterArr;
    }

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
        System.out.println("Letter: " + letter + " | Confidence: " + maxPoints + "%");
        return letter;
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
        int[][] i = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } };
        blueprints.put(i, 'i');
        int[][] s = { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 1, 1, 1, 0, 0, 0, 1, 1 },
                { 0, 1, 1, 1, 1, 0, 0, 0, 1, 1 }, { 0, 1, 1, 1, 1, 1, 0, 0, 1, 1 }, { 1, 1, 0, 0, 1, 1, 0, 0, 1, 1 },
                { 1, 1, 0, 0, 1, 1, 0, 0, 1, 1 }, { 1, 1, 0, 0, 1, 1, 1, 0, 1, 1 }, { 1, 1, 0, 0, 0, 1, 1, 0, 1, 1 },
                { 0, 1, 1, 0, 0, 1, 1, 1, 1, 1 }, { 0, 1, 0, 0, 0, 0, 1, 1, 1, 0 } };
        blueprints.put(s, 's');
        int[][] e = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 },
                { 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 }, { 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 }, { 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 },
                { 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 0, 1 } };
        blueprints.put(e, 'e');
        int[][] p = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 0, 0, 1, 1, 0, 0, 0 }, { 1, 1, 0, 0, 0, 1, 1, 0, 0, 0 },
                { 1, 1, 1, 0, 0, 1, 1, 0, 0, 0 }, { 1, 1, 1, 0, 0, 1, 1, 0, 0, 0 }, { 0, 1, 1, 1, 1, 1, 1, 0, 0, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 } };
        blueprints.put(p, 'p');

        int[][] t = { { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 } };
        blueprints.put(t, 't');
        int[][] n = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 0, 0, 0, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 1, 1, 1, 0, 0, 0 }, { 0, 0, 0, 0, 0, 1, 1, 1, 1, 0 }, { 0, 0, 0, 0, 0, 0, 0, 1, 1, 1 },
                { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } };
        blueprints.put(n, 'n');
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

        // int[][] p = {{1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 0, 0, 1, 1, 0, 0, 0}, {1, 1, 0, 0, 0, 1, 1, 0, 0, 0}, {1, 1, 1, 0, 0, 1, 1, 0, 0, 0}, {1, 1, 1, 0, 0, 1, 1, 0, 0, 0}, {0, 1, 1, 1, 1, 1, 1, 0, 0, 0}, {0, 1, 1, 1, 1, 1, 1, 0, 0, 0}, {0, 0, 1, 1, 1, 1, 0, 0, 0, 0}};
        // blueprints.put(e, 'e');
        // int[][] p = {{1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 0, 0, 1, 1, 0, 0, 0}, {1, 1, 0, 0, 0, 1, 1, 0, 0, 0}, {1, 1, 1, 0, 0, 1, 1, 0, 0, 0}, {1, 1, 1, 0, 0, 1, 1, 0, 0, 0}, {0, 1, 1, 1, 1, 1, 1, 0, 0, 0}, {0, 1, 1, 1, 1, 1, 1, 0, 0, 0}, {0, 0, 1, 1, 1, 1, 0, 0, 0, 0}};
        // blueprints.put(e, 'e');
        // int[][] p = {{1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 0, 0, 1, 1, 0, 0, 0}, {1, 1, 0, 0, 0, 1, 1, 0, 0, 0}, {1, 1, 1, 0, 0, 1, 1, 0, 0, 0}, {1, 1, 1, 0, 0, 1, 1, 0, 0, 0}, {0, 1, 1, 1, 1, 1, 1, 0, 0, 0}, {0, 1, 1, 1, 1, 1, 1, 0, 0, 0}, {0, 0, 1, 1, 1, 1, 0, 0, 0, 0}};
        // blueprints.put(e, 'e');
    }
}
