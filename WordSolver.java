import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.awt.Robot;
import java.util.List;

import javax.imageio.ImageIO;

import java.util.ArrayList;
import static java.lang.Math.abs;

public class WordSolver {
    private Robot bot;
    private CirclePixelChecker circle;
    private PuzzleScanner puzzleScanner;
    private LetterID letterID;

    // from center of letter to edges + padding
    private static final int CENTER_X = 1212; // STATIC 735 fullscreen
    private int donut_center_y = 775;
    private static final int LETTER_VERTICAL_OFFSET = 20; // FOR SOME REASON LETTER HITBOX WAY ABOVE LETTER
    private static final int DONUT_HOLE_RGB = -11269612;
    private int REROLL_X = 1033; // STATIC: 80 pixels from left of screen
    private int reroll_y = 700;
    private static final int DONUT_BACKGROUND_RGB = 0xFFC90A27; // timmy red
    private static final int MOVEMENT_DELAY = 5;

    private static final int BASELINE_RADIUS = 95;
    private static final int BASELINE_DONUT_HEIGHT = 37;
    private double ratio;

    private static final String FILE_NAME = "output.txt";
    private BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME));
    List<Character> letters;
    List<List<Character>> previousAttempts;
    private int radius;

    public WordSolver() throws AWTException, FileNotFoundException {
        bot = new Robot();
        puzzleScanner = new PuzzleScanner(bot);
        letterID = new LetterID(bot);
        circle = new CirclePixelChecker(bot);
        previousAttempts = new ArrayList<>();
    }

    // Solve the timmy word challenge
    public void solve() {
        System.out.println("RUN!");
        // bot.delay(1000);

        while (true) {
            if (!isDonutScreen()) { // not in level
                nextLevel();
            } else {
                bot.delay(1500);
                // compute configs
                donut_center_y = computeHeight();
                // adjust for different sized donuts
                radius = findDonutRadius();
                letterID.computeLetterSize(ratio);
                
                letters = new ArrayList<>();
                int numLetters = circle.countLetters(CENTER_X, donut_center_y, radius);
                System.out.println(numLetters + " letters");
                List<Coordinate> coords = calculateCoodinates(letters, numLetters);
                puzzleScanner.scanPuzzle(true, 1, CENTER_X);

                if (!isRepeatedAttempt()) { // if not trying same solution
                    List<List<Coordinate>> words = findValidWords(letters, coords);
                    makeMoves(words);
                    previousAttempts.add(letters);
                }
                reroll(); // after checking solutions, reroll regardless of trying or not
                bot.mouseMove(100, 100);
            }
        }
    }

    // Computes the y-coords of the donut_center and the reroll button
    private int computeHeight() {
        BufferedImage img = bot.createScreenCapture(new Rectangle(CENTER_X, 450, 1, 956 - 450));

        int color = DONUT_BACKGROUND_RGB;
        int y = 0;
        int height = 0;
        boolean counting = false;
        do {
            if (counting) {
                height++;
            } else {
                if (abs(color - img.getRGB(0, y)) <= 50000)
                    counting = true;
            }
            y++;
        } while ((y < img.getHeight())); // bottom of screen

        reroll_y = 955 - height + 80; // 80 below the top of red-bottom panel
        System.out.print("Height: " + height + " ");

        return 955 - (height / 2);
    }

    // Finds the donut's Radius and the ratio (how much larger/smaller is the donut)
    private int findDonutRadius() {
        int color = DONUT_HOLE_RGB;
        int prevColor = color;
        int colorDistance;
        int y = donut_center_y;
        do {
            color = bot.getPixelColor(CENTER_X, y).getRGB();
            colorDistance = color - prevColor;
            prevColor = color;
            y--;
        } while ((colorDistance < 800000));

        ratio = (donut_center_y - y) / (double) BASELINE_DONUT_HEIGHT; // newDonutHeight / 34 (baseline)

        return (int) (BASELINE_RADIUS * ratio);
    }

    // Creates list of letters in the current puzzle; each entry has the coordinate
    // where the letter is located on the donut and the letter it is predicted to be
    private List<Coordinate> calculateCoodinates(List<Character> letters, int numLetters) {
        List<Coordinate> coords = new ArrayList<>();

        for (int i = 0; i < numLetters; i++) {
            int x = (int) (CENTER_X + radius * Math.cos(Math.toRadians(360 / numLetters) * i));
            int y = (int) (donut_center_y + radius * Math.sin(Math.toRadians(360 / numLetters) * i));
            char letter = letterID.identifyLetter(x, y);
            letters.add(letter);
            coords.add(new Coordinate(x, y - LETTER_VERTICAL_OFFSET, letter));
        }
        return coords;
    }

    // Find all words that can be formed using letters in curr puzzle; create list
    // of coordinate combinations to input said words
    private List<List<Coordinate>> findValidWords(List<Character> letters, List<Coordinate> coords) {
        List<List<Coordinate>> words = new ArrayList<>();
        String line;
        boolean isValid;
        try {
            while ((line = reader.readLine()) != null) {
                List<Character> temp = new ArrayList<>(letters);
                List<Coordinate> currWord = new ArrayList<>();
                char[] lineChars = line.toCharArray();
                isValid = true;

                for (int i = 0; i < lineChars.length; i++) {
                    if (!temp.contains(lineChars[i])) {
                        isValid = false;
                        break;
                    }
                    temp.remove(Character.valueOf(lineChars[i]));
                    addLetterCoordinate(lineChars[i], currWord, coords);
                }

                if (isValid) {
                    words.add(currWord);
                }
            }
            reader = new BufferedReader(new FileReader(FILE_NAME)); // reset reader
        } catch (UnsupportedOperationException e) {
            System.out.println("unsuported");
        } catch (IndexOutOfBoundsException e) {
            System.out.println("index out of bounds");
        } catch (Exception e) {
            System.out.println("Problem with bufferedReader");
        }
        return words;
    }

    // Makes sure a word is not made up of letter's of duplicate coordinates (eg.
    // two A's, using same A twice)
    private void addLetterCoordinate(char c, List<Coordinate> currWord, List<Coordinate> coords) {
        for (Coordinate coord : coords) {
            if (!currWord.contains(coord) && c == coord.getLetter()) {
                currWord.add(coord);
                break;
            }
        }
    }

    // Using possible word combinations, input them into word challenge
    private void makeMoves(List<List<Coordinate>> words) {
        for (List<Coordinate> currWord : words) {
            String word = "";
                for (Coordinate curr : currWord) {
                    word = word + curr.getLetter();
                }
            if (!isDonutScreen()) { // if no longer on donut screen
                nextLevel();
                break;
            } else if (filterOut(currWord)) { // filter for lengths and repeated words
                System.out.print("\u001B[31m[" + word + "] \u001B[0m");
            } else {
                System.out.print(word + " ");

                Coordinate prev = currWord.get(0);
                for (int i = 1; i < currWord.size(); i++) {
                    move(prev, currWord.get(i));
                    prev = currWord.get(i);
                }
                bot.delay(MOVEMENT_DELAY);
                bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

                puzzleScanner.updatePuzzleWords();
            }
        }
    }

    // Drag and drop from letter prev to letter next
    private void move(Coordinate prev, Coordinate next) {
        bot.mouseMove(prev.getX(), prev.getY());
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.delay(MOVEMENT_DELAY);
        bot.mouseMove(CENTER_X, donut_center_y);
        bot.delay(MOVEMENT_DELAY);
        bot.mouseMove(next.getX(), next.getY());
    }

    // Filters out currWord if it is not a needed length...
    // Returns true if it does Filter Out the currWord, false if word is to be tried
    private boolean filterOut(List<Coordinate> currWord) {
        // return false;
        List<List<Coordinate>> wordsInPuzzle = puzzleScanner.getWordsPuzzle();
        for (List<Coordinate> puzzleWord : wordsInPuzzle)
            if (puzzleWord.size() == currWord.size())
                return false;
        return true;
    }

    // Is current screen donut screen (word challenge)?
    private boolean isDonutScreen() {
        return abs(DONUT_BACKGROUND_RGB - bot.getPixelColor(1415, 775).getRGB()) <= 200000;
    }

    // Current combination of letters has been tried for this puzzle
    private boolean isRepeatedAttempt() {
        return previousAttempts.stream().anyMatch(sublist -> equalsInAnyOrder(sublist, letters));
    }

    // Helper for isRepeatedAttempt()
    private boolean equalsInAnyOrder(List<Character> sublist, List<Character> letters) {
        return sublist.containsAll(letters) && letters.containsAll(sublist) && (sublist.size() == letters.size());
    }

    // Press reroll button: reroll the letter to be in new positions
    private void reroll() {
        bot.mouseMove(REROLL_X, reroll_y);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseMove(100, 100);
    }

    // Move to next level; reset curr level data
    private void nextLevel() {
        bot.mouseMove(1120, 500); //750, 275 for fullscreen
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseMove(500, 275);
        previousAttempts.clear(); // next level, no previous attempts
        puzzleScanner.clearWordsPuzzle();
        bot.delay(1000);
    }
}