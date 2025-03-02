import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

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
        boolean isCheckingLine = true;
        boolean toggleNextStopChecking;
        boolean toggleNextStartChecking;

        for (int var1 = 0; var1 < CROSSWORD_HALF_SIZE * 2; var1++) {
            boolean countingWord = false;
            int distanceToNext = 0;
            toggleNextStopChecking = false;
            toggleNextStartChecking = true;
            // Column words
            for (int var2 = 0; var2 < img.getHeight(); var2++) {
                int x = ((isColumn) ? var1 : var2);
                int y = ((isColumn) ? var2 : var1);
                // bot.mouseMove(x + CENTER_X - CROSSWORD_HALF_SIZE, y + CROSSWORD_Y -
                // CROSSWORD_HALF_SIZE);
                if (isCheckingLine) {
                    if (Math.abs(img.getRGB(x, y) - CROSSWORD_BLANK_RGB) <= 800000) {

                        if (!countingWord && distanceToNext > CROSSWORD_DIST_TOLERANCE) { // start new word
                            currWord = new ArrayList<>();
                            countingWord = true;
                        }
                        if (countingWord && distanceToNext > 3) { // dont count same letter many times
                            currWord.add(new Coordinate(x + CENTER_X - CROSSWORD_HALF_SIZE,
                                    y + CROSSWORD_Y - CROSSWORD_HALF_SIZE));
                            // bot.delay(300);
                        }
                        distanceToNext = 0;
                        toggleNextStopChecking = true;
                    } else {
                        // save currWord if 3 or more letters and word has just ended
                        if (distanceToNext > CROSSWORD_DIST_TOLERANCE) {
                            if (countingWord && currWord.size() >= 3 && distanceToNext > CROSSWORD_DIST_TOLERANCE) {
                                wordsInPuzzle.add(currWord);
                                // bot.delay(2000);
                            }    
                            countingWord = false;
                        }
                        distanceToNext++;
                    }
                } else {
                    if (img.getRGB(x, y) == CROSSWORD_BLANK_RGB) {
                        toggleNextStartChecking = false;
                    }
                }
            }
            if (toggleNextStopChecking) {
                isCheckingLine = false;
            } else if (toggleNextStartChecking) {
                isCheckingLine = true;
                var1 += CROSSWORD_DIST_TOLERANCE;
            }
        }
        if (isColumn) { // after doing the column words, go again now for rows
            scanPuzzle(false, wordBoxSize);
        }
    }

    // Updates words left to be solved from puzzle
    public void updatePuzzleWords() {
        boolean remove;
        for (Iterator<List<Coordinate>> itr = wordsInPuzzle.iterator(); itr.hasNext();) {
            remove = true;
            List<Coordinate> puzzleWords = itr.next();
            for (Coordinate letter : puzzleWords) {
                if (Math.abs(bot.getPixelColor(letter.getX(), letter.getY()).getRGB() - CROSSWORD_BLANK_RGB) <= 800000) {
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
