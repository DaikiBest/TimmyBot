import java.awt.AWTException;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.awt.Robot;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import static java.lang.Math.abs;

import javax.imageio.ImageIO;

public class WordSolver {
    private Robot bot;
    private CirclePixelChecker circle = new CirclePixelChecker();
    private LetterID letterID = new LetterID();
    private List<Coordinate> coords;
    private List<List<Coordinate>> words;

    // from center of letter to edges + padding
    private static final int DONUT_CENTER_X = 735;
    private static final int DONUT_CENTER_Y = 775;
    private static final int DONUT_CENTER_RGB = -11269612;
    private static final int REROLL_X = 80;
    private static final int REROLL_Y = 700;
    private static final int DONUT_BACKGROUND_RGB = -3665874;
    private static final double DONUT_RADIUS_RATIO = 96.0 / 33.0;

    private static final String FILE_NAME = "output.txt";
    private BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME));
    // private Scanner scanner = new Scanner(System.in);
    List<Character> letters;
    private int radius;

    public WordSolver() throws AWTException, FileNotFoundException {
        bot = new Robot();
        words = new ArrayList<>();
        letters = new ArrayList<>();
    }

    public void solve() {
        System.out.println("RUN!");
        bot.delay(1000);
        while (true) {
            radius = findDonutRadius();
            int numLetters = circle.countLetters(DONUT_CENTER_X, DONUT_CENTER_Y, radius);
            System.out.println(numLetters + " letters");
            coords = calculateCoodinates(letters, numLetters);
    
            // findValidWords(letters);
            // makeMoves();
            // bot.delay(500);

            // // Continue after trying answer: either try again (reroll), or nextLevel
            // if (isDonutScreen()) {
            //     bot.mouseMove(REROLL_X, REROLL_Y);
            //     bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            //     bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            // } else {
            //     nextLevel();
            // }
            // bot.delay(250);
            bot.mouseMove(100, 100);
            break;
        }
    }

    private int findDonutRadius() {
        int color = DONUT_CENTER_RGB;
        int prevColor = color;
        int colorDistance;
        int y = DONUT_CENTER_Y;
        do {
            color = bot.getPixelColor(DONUT_CENTER_X, y).getRGB();
            colorDistance = color - prevColor;
            prevColor = color;
            y--;
        } while ((colorDistance < 2000000));

        System.out.println((DONUT_CENTER_Y - y));

        return (int) ((DONUT_CENTER_Y - y) * DONUT_RADIUS_RATIO);
    }

    private List<Coordinate> calculateCoodinates(List<Character> letters, int numLetters) {
        List<Coordinate> coords = new ArrayList<>();

        for (int i = 0; i < numLetters; i++) {
            int x = (int) (DONUT_CENTER_X + radius * Math.cos(Math.toRadians(360 / numLetters) * i));
            int y = (int) (DONUT_CENTER_Y + radius * Math.sin(Math.toRadians(360 / numLetters) * i));
            char letter = letterID.identifyLetter(x, y, bot);
            letters.add(letter);
            coords.add(new Coordinate(x, y, i, letter));
        }
        return coords;
    }

    private void findValidWords(List<Character> letters) {
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
                    addLetterCoordinate(lineChars[i], currWord);
                }

                if (isValid) {
                    words.add(currWord);
                }
            }
            reader.mark(0); //reset reader back to beginning
        } catch (UnsupportedOperationException e) {
            System.out.println("unsuported");
        } catch (IndexOutOfBoundsException e) {
            System.out.println("index out of bounds");
        } catch (Exception e) {
            System.out.println("Problem with bufferedReader");
        }
    }

    // Makes sure a word is not made up of letter's of duplicate coordinates (eg. two A's, using same A twice)
    private void addLetterCoordinate(char c, List<Coordinate> currWord) {
        for (Coordinate coord : coords) {
            if (!currWord.contains(coord) && c == coord.getLetter()) {
                currWord.add(coord);
                break;
            }
        }
    }

    private void makeMoves() {
        for (List<Coordinate> currWord : words) {
            //if no longer on donut screen
            if (!isDonutScreen()) {
                nextLevel();
                break;
            }

            // print word
            String word = "";
            for (Coordinate curr : currWord) {
                word = word + curr.getLetter();
            }
            System.out.print(word + " ");

            Coordinate prev = currWord.get(0);
            for (int i = 1; i < currWord.size(); i++) {
                move(prev, currWord.get(i));
                prev = currWord.get(i);
            }
            bot.delay(5);
            bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    private void move(Coordinate prev, Coordinate next) {
        bot.mouseMove(prev.getX(), prev.getY());
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.delay(5);
        bot.mouseMove(DONUT_CENTER_X, DONUT_CENTER_Y);
        bot.delay(5);
        bot.mouseMove(next.getX(), next.getY());
    }

    private boolean isDonutScreen() {
        return abs(DONUT_BACKGROUND_RGB - bot.getPixelColor(REROLL_X, REROLL_Y + 80).getRGB()) <= 2000000;
    }

    private void nextLevel() {
        bot.delay(3500); //in case power ups gained, click twice
        bot.mouseMove(750, 275);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseMove(500, 275);
        bot.delay(3500);
        bot.mouseMove(750, 275);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
}