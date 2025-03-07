import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import java.io.File;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class PuzzleScanner {
    private Robot bot;
    private static final int CROSSWORD_SOLVED_RGB = 0xFF3F1F20;
    private static final int CROSSWORD_BLANK_RGB = -6979980;
    private static final int CROSSWORD_HALF_SIZE = 225;
    private static final int CROSSWORD_Y = 405; // STATIC old: 385
    private static final int CROSSWORD_DIST_TOLERANCE = 12; // bound for distance to next letter

    private List<List<Coordinate>> wordsInPuzzle;

    public PuzzleScanner(Robot bot) {
        this.bot = bot;
        wordsInPuzzle = new ArrayList<>();
    }

    // Scans puzzle; will skip the puzzle scanner if the board is not empty
    public void scan(boolean isColumn, int wordBoxSize, int CENTER_X) {
        try {
            scanPuzzle(true, 1, CENTER_X);
        } catch (Exception e) {
            System.out.println("\u001B[31mPuzzle is not empty.\u001B[0m");
            wordsInPuzzle.clear(); // if puzzle not empty, omit PuzzleScanner
        }
    }

    // Obtains the list of words to be solved from the crossword puzzle
    private void scanPuzzle(boolean isColumn, int wordBoxSize, int CENTER_X) throws PuzzleNotEmptyException {
        BufferedImage img = bot.createScreenCapture(new Rectangle(CENTER_X - CROSSWORD_HALF_SIZE,
                CROSSWORD_Y - CROSSWORD_HALF_SIZE, CROSSWORD_HALF_SIZE * 2, CROSSWORD_HALF_SIZE * 2));

        // File file = new File("my" + ".png");
        // try {
        // ImageIO.write(img, "PNG", file);
        // } catch (Exception e) {

        // }

        List<Coordinate> currWord = new ArrayList<>();
        boolean isCheckingLine = false;
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
                    if (img.getRGB(x, y) == CROSSWORD_BLANK_RGB) {

                        if (!countingWord && distanceToNext > CROSSWORD_DIST_TOLERANCE) { // start new word
                            currWord = new ArrayList<>();
                            countingWord = true;
                        }
                        if (countingWord && distanceToNext > 1) { // dont count same letter many times
                            currWord.add(new Coordinate(x + CENTER_X - CROSSWORD_HALF_SIZE,
                                    y + CROSSWORD_Y - CROSSWORD_HALF_SIZE));
                            // bot.delay(300);
                        }
                        distanceToNext = 0;
                        toggleNextStopChecking = true;
                    } else if (img.getRGB(x, y) == CROSSWORD_SOLVED_RGB) { // if puzzle is not empty
                        img.flush();
                        throw new PuzzleNotEmptyException();
                    } else {
                        // save currWord if 3 or more letters and word has just ended
                        if (distanceToNext > CROSSWORD_DIST_TOLERANCE) {
                            if (countingWord && currWord.size() >= 3) {
                                wordsInPuzzle.add(currWord);
                                // bot.delay(1000);
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
            scanPuzzle(false, wordBoxSize, CENTER_X);
        }
    }

    // Updates words left to be solved from puzzle; returns true if puzzle was updated
    public boolean updatePuzzleWords() {
        boolean remove = false;
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
        return remove;
    }

    public List<List<Coordinate>> getWordsPuzzle() {
        return wordsInPuzzle;
    }

    public void clearWordsPuzzle() {
        wordsInPuzzle.clear();
    }
}
