import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;

public class PuzzleScanner {
    private Robot bot;
    private static final int CENTER_X = 735; // a little redundant; alrdy in WordSolver
    private static final int CROSSWORD_BLANK_RGB = -6979980;
    // private static final int CROSSWORD_FILL_RGB = -14737633;
    // private static final int CROSSWORD_WHITE_RGB = 0xFFFFFFFF;
    private static final int CROSSWORD_HALF_SIZE = 200;
    private static final int CROSSWORD_Y = 385;
    private static final int CROSSWORD_DIST_TOLERANCE = 12; // bound for distance to next letter

    private List<List<Coordinate>> wordsInPuzzle;

    public PuzzleScanner(Robot bot) {
        this.bot = bot;
        wordsInPuzzle = new ArrayList<>();
    }

    // Obtains the list of words to be solved from the crossword puzzle
    public void scanPuzzle(boolean isColumn, int wordBoxSize) {
        BufferedImage img = bot.createScreenCapture(new Rectangle(CENTER_X - CROSSWORD_HALF_SIZE,
                CROSSWORD_Y - CROSSWORD_HALF_SIZE, CROSSWORD_HALF_SIZE * 2, CROSSWORD_HALF_SIZE * 2));

        List<Coordinate> currWord = new ArrayList<>();
        int increment = 1;

        for (int var1 = 0; var1 < CROSSWORD_HALF_SIZE * 2; var1 += increment) {
            boolean countingWord = false;
            int distanceToNext = 0;
            // Column words
            for (int var2 = 0; var2 < img.getHeight(); var2++) {
                int x = ((isColumn) ? var1 : var2);
                int y = ((isColumn) ? var2 : var1);
                bot.mouseMove(x + CENTER_X - CROSSWORD_HALF_SIZE, y + CROSSWORD_Y -
                CROSSWORD_HALF_SIZE);
                if (Math.abs(img.getRGB(x, y) - CROSSWORD_BLANK_RGB) <= 800000) {

                    if (increment == 1) { // does only once: update increment to jump to next letter
                        if (isColumn) {
                            wordBoxSize = computeWordBoxSize(x, y, img);
                        }
                        increment = wordBoxSize;
                    }

                    if (!countingWord && distanceToNext > CROSSWORD_DIST_TOLERANCE) { // start new word
                        currWord = new ArrayList<>();
                        countingWord = true;
                    }
                    if (countingWord && distanceToNext > 3) { // dont count same letter many times
                        currWord.add(new Coordinate(x + CENTER_X - CROSSWORD_HALF_SIZE,
                                y + CROSSWORD_Y - CROSSWORD_HALF_SIZE));
                        bot.delay(300);
                    }
                    distanceToNext = 0;
                } else {
                    // save currWord if 3 or more letters and word has just ended
                    if (countingWord && currWord.size() >= 3 && distanceToNext > CROSSWORD_DIST_TOLERANCE) {
                        wordsInPuzzle.add(currWord);
                        countingWord = false;
                        bot.delay(1200);
                    }

                    distanceToNext++;
                }
            }
        }
        if (isColumn) { // after doing the column words, go again now for rows
            scanPuzzle(false, wordBoxSize);
        }
    }

    // Compute size of the word-boxes in curr crossword
    private int computeWordBoxSize(int x, int y, BufferedImage img) {
        int width = 0;
        int blankColor = CROSSWORD_BLANK_RGB;
        while (Math.abs(blankColor - CROSSWORD_BLANK_RGB) <= 3000000) {
            width++;
            blankColor = img.getRGB(x + width, y);
        }
        System.out.println(width);
        return width;
    }

    // Updates words left to be solved from puzzle
    public void updatePuzzleWords() {
        boolean remove;
        for (Iterator<List<Coordinate>> itr = wordsInPuzzle.iterator(); itr.hasNext();) {
            remove = true;
            List<Coordinate> puzzleWords = itr.next();
            for (Coordinate letter : puzzleWords) {
                if (bot.getPixelColor(letter.getX(), letter.getY()).getRGB() == CROSSWORD_BLANK_RGB) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                itr.remove();
            }
        }
    }

    public List<List<Coordinate>> getWordsPuzzle() {
        return wordsInPuzzle;
    }

    public void clearWordsPuzzle() {
        wordsInPuzzle.clear();
    }
}
