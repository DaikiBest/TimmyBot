import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.awt.Robot;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat.Encoding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static java.lang.Math.abs;
import static java.lang.Math.nextDown;

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
    private int REROLL_X = 1033; // STATIC: 70 pixels from left of screen
    private int reroll_y = 700;
    private static final int DONUT_BACKGROUND_RGB = 0xFFC90A27; // timmy red
    private static final int MOVEMENT_DELAY = 5;

    private static final int BASELINE_RADIUS = 95;
    private static final int BASELINE_DONUT_HEIGHT = 37;
    private double ratio;

    private static final String FILE_NAME = "output.txt";
    private List<Character> letters;
    private int radius;

    public WordSolver() throws AWTException, FileNotFoundException {
        bot = new Robot();
        puzzleScanner = new PuzzleScanner(bot);
        letterID = new LetterID(bot);
        circle = new CirclePixelChecker(bot);
    }

    // Solve the timmy word challenge
    public void solve() {
        System.out.println("RUN!");
        // bot.delay(1000);

        while (true) {
            if (!isDonutScreen()) { // not in level
                nextLevel();
            } else {
                // long startTime = System.currentTimeMillis();
                donut_center_y = computeHeight();
                radius = findDonutRadius();
                letterID.computeLetterSize(ratio);

                letters = new ArrayList<>();
                int numLetters = circle.countLetters(CENTER_X, donut_center_y, radius, ratio);
                System.out.println(numLetters + " letters");
                List<Coordinate> coords = calculateCoodinates(letters, numLetters);
                puzzleScanner.scan(true, 1, CENTER_X);

                Map<String, Integer> validWords = findValidWords(letters);
                // long finishTime = System.currentTimeMillis();
                // System.out.println("Time taken: " + (finishTime - startTime) + " ms");
                makeMoves(validWords, coords);

                reroll(); // after checking solutions, reroll regardless of trying or not
                bot.mouseMove(100, 100);
            }
        }
    }

    // Computes the y-coords of the donut_center and the reroll button
    private int computeHeight() {
        BufferedImage img = bot.createScreenCapture(new Rectangle(CENTER_X, 450, 1, 956 - 450));

        int y = 0;
        int height = 0;
        boolean counting = false;
        do {
            if (counting) {
                height++;
            } else {
                if (abs(DONUT_BACKGROUND_RGB - img.getRGB(0, y)) <= 70000)
                    counting = true;
            }
            y++;
        } while ((y < img.getHeight())); // bottom of screen

        reroll_y = 955 - height + 70; // 70 below the top of red-bottom panel
        System.out.print("\nHeight: " + height + " ");

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

    // Find all words that can be formed using letters in curr puzzle; create map
    // with a list
    // of coordinate combinations to input said words, and offset of word in
    // dictionary
    private Map<String, Integer> findValidWords(List<Character> letters) {
        Map<String, Integer> validWords = new HashMap<>();

        String line;
        boolean isValid;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME)); // reset reader
            int offset = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                String word = parts[0].trim();

                List<Character> temp = new ArrayList<>(letters);
                char[] wordChars = word.toCharArray();
                isValid = true;
                for (int i = 0; i < wordChars.length; i++) {
                    if (!temp.contains(wordChars[i])) {
                        isValid = false;
                        break;
                    }
                    temp.remove(Character.valueOf(wordChars[i])); // no duplicate letters
                }

                if (isValid) {
                    validWords.put(line, offset);
                }
                offset += 10;
            }
        } catch (Exception e) {
            System.out.println("File reading exception.");
        }
        return validWords;
    }

    // Makes sure a word is not made up of letter's of duplicate coordinates (eg.
    // two A's, using same A twice)
    private List<Coordinate> makeLetterCoords(String word, List<Coordinate> coords) {
        List<Coordinate> wordsCoords = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            for (Coordinate coord : coords) {
                if (!wordsCoords.contains(coord) && c == coord.getLetter()) {
                    wordsCoords.add(coord);
                    break;
                }
            }
        }
        return wordsCoords;
    }

    // Using possible word combinations, input them into word challenge
    private void makeMoves(Map<String, Integer> validWordsMap, List<Coordinate> coords) {
        List<String> validWords = sortByFrequency(validWordsMap);

        for (String key : validWords) {
            String[] parts = key.split(":");
            String word = parts[0].trim();
            int frequency = Integer.valueOf(parts[1]);
            List<Coordinate> wordCoords = makeLetterCoords(word, coords);

            if (!isDonutScreen()) { // if no longer on donut screen
                nextLevel();
                break;
            } else if (filterOut(wordCoords)) { // filter for necessary lengths
                System.out.print("\u001B[31m[" + word + "]" + frequency + "\u001B[0m ");
            } else {
                Coordinate prev = wordCoords.get(0);
                for (int i = 1; i < wordCoords.size(); i++) {
                    move(prev, wordCoords.get(i));
                    prev = wordCoords.get(i);
                }
                bot.delay(MOVEMENT_DELAY);
                bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

                System.out.print(word + "-");

                bot.delay(21);
                // If current word solved the puzzle, increase frequency
                if (puzzleScanner.updatePuzzleWords()) {
                    try {
                        System.out.print("\u001B[34m" + (frequency + 1) + "\u001B[0m ");
                        RandomAccessFile fileHandler = new RandomAccessFile(FILE_NAME, "rw");
                        int offset = Integer.valueOf(validWordsMap.get(key));
                        String updatedDict = word + String.join("", Collections.nCopies(7 - word.length(), " ")) + ":"
                                + ++frequency;
                        fileHandler.seek(offset);
                        fileHandler.writeBytes(updatedDict);
                        fileHandler.close();
                    } catch (Exception e) {
                        System.out.println("Problems writing into file.");
                    }
                } else {
                    System.out.print(frequency + " ");
                }
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

    // Returns the list of validWords for puzzle sorted by their frequency
    private List<String> sortByFrequency(Map<String, Integer> wordsMap) {
        // Keys and frequencies have same indices; make indices[]
        int n = wordsMap.keySet().size();
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; ++i) {
            indices[i] = i;
        }

        // Make list of frequencies (parsing map key; parts[1])
        List<String> keys = new ArrayList<>(wordsMap.keySet());
        List<Integer> frequencies = new ArrayList<>();
        for (String key : keys) {
            String[] parts = key.split(":");
            frequencies.add(Integer.valueOf(parts[1]));
        }

        // Sort indices[] by order of frequencies
        Arrays.sort(indices,
                new Comparator<Integer>() {
                    public int compare(Integer a, Integer b) {
                        return frequencies.get(b).compareTo(frequencies.get(a));
                    }
                });

        // Sort keys by indices[]
        List<String> orderedWords = new ArrayList<>();
        for (int index : indices) {
            orderedWords.add(keys.get(index));
        }
        return orderedWords;
    }

    // Filters out currWord if it is not a needed length
    // Returns true if it does filter currWord, false if word is to be tried
    // Omit word filtering if the puzzle was not empty when scanned
    private boolean filterOut(List<Coordinate> currWord) {
        // if board is not empty, don't filter out
        if (puzzleScanner.getWordsPuzzle().isEmpty()) {
            return false;
        }
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

    // Press reroll button: reroll the letter to be in new positions
    private void reroll() {
        bot.mouseMove(REROLL_X, reroll_y);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseMove(100, 100);
        bot.delay(300);
    }

    // Move to next level; reset curr level data
    private void nextLevel() {
        bot.mouseMove(1120, 510); // 750, 275 for fullscreen
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK); // click twice
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseMove(1120, 550);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        puzzleScanner.clearWordsPuzzle();

        // click on console (to make quitting easier)
        bot.mouseMove(625, 825);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        bot.delay(250);
        
        bot.mouseMove(1120, 510);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
}