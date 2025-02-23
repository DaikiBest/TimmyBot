import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.awt.Robot;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Set;
import static java.lang.Math.abs;

import javax.imageio.ImageIO;

import java.util.HashSet;

public class WordSolver {
    private Robot bot;
    private CirclePixelChecker circle;
    private List<Coordinate> coords;
    private Set<List<Coordinate>> words;

    // from center of letter to edges + padding
    private static final int LETTER_HALF_WIDTH = 25;
    private static final int LETTER_HALF_HEIGHT = 25;
    private static final int DONUT_CENTER_X = 735;
    private static final int DONUT_CENTER_Y = 775;

    private static final double DONUT_RADIUS_RATIO = 96.0 / 33.0;
    private static final int TOLERANCE = 10;

    private static final String FILE_NAME = "output.txt";
    private BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME));
    private Scanner scanner = new Scanner(System.in);
    private int radius;
    private String[] letters;

    public WordSolver() throws AWTException, FileNotFoundException {
        bot = new Robot();
        circle = new CirclePixelChecker();
        letters = new String[7];
    }

    public void solve() {
        while (true) {
            //input letters MANUALLY
            String letter = "";
            int i = 0;
            do {
                System.out.print("Input letter: ");
                letter = scanner.nextLine();
                if (!letter.equalsIgnoreCase("stop")) {
                    letters[i] = letter;
                }
                i++;
            } while (!letter.equalsIgnoreCase("stop"));

            words = new HashSet<>();
            System.out.println("RUN!");
            bot.delay(3000);

            radius = findDonutRadius();
            int numLetters = circle.countLetters(DONUT_CENTER_X, DONUT_CENTER_Y, radius);
            System.out.println(numLetters + " letters");
            coords = calculateCoodinates(numLetters);
    
            combineLetters(coords, numLetters, new ArrayList<Coordinate>());
            makeMoves(words);
            
            bot.mouseMove(100, 100);
        }
    }

    private int findDonutRadius() {
        int color = -11269612;
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

    private List<Coordinate> calculateCoodinates(int numLetters) {
        List<Coordinate> coords = new ArrayList<>();

        for (int i = 0; i < numLetters; i++) {
            int x = (int) (DONUT_CENTER_X + radius * Math.cos(Math.toRadians(360 / numLetters) * i));
            int y = (int) (DONUT_CENTER_Y + radius * Math.sin(Math.toRadians(360 / numLetters) * i));
            // letter = getLetter(x, y);
            coords.add(new Coordinate(x, y, i, letters[i]));
        }
        return coords;
    }

    // private String getLetter(int letterX, int letterY) {
        // int whiteCount = 0;
        // BufferedImage img = bot.createScreenCapture(new Rectangle(letterX - LETTER_HALF_WIDTH, letterY - LETTER_HALF_HEIGHT,
        //         LETTER_HALF_WIDTH * 2, LETTER_HALF_HEIGHT * 2));
        
        // for (int x = 0; x < img.getWidth(); x++) {
        //     for (int y = 0; y < img.getHeight(); y++) {

        //         int pixel = img.getRGB(x, y);
        //         Color color = new Color(pixel, true);
        //         if (color.getRed() > 160 && color.getGreen() < 100 && color.getBlue() < 100) {
        //             img.setRGB(x, y, Color.BLACK.getRGB());
        //         }
                
        //         if (abs(img.getRGB(x, y) - 0xFFFFFFFF) <= 0x6A0000) {
        //             whiteCount++;
        //             img.setRGB(x, y, 0x123456);
        //             // bot.delay(30);
        //             // bot.mouseMove(x + letterX - LETTER_HALF_WIDTH, y + letterY - LETTER_HALF_HEIGHT);
        //         }
        //     }
        // }

        // File file = new File("me" + letterX + letterY + ".png");
        // try {
        //     ImageIO.write(img, "PNG", file);
            
        // } catch (Exception e) {
        //     // TODO: handle exception
        // }

        // System.out.print(whiteCount + " ");
        // return identifyLetter(whiteCount);
    // }

    private void combineLetters(List<Coordinate> coords, int maxDepth, List<Coordinate> currWord) {

        
        if (currWord.size() < maxDepth) {
            for (Coordinate coord : coords) {

                if (!currWord.contains(coord)) {
                    List<Coordinate> newCoords = new ArrayList<>(currWord);
                    newCoords.add(coord);

                    addWord(newCoords);
                    combineLetters(coords, maxDepth, newCoords);
                }
            }
        }
    }

    private void addWord(List<Coordinate> currWord) {
        String word = "";
        for (Coordinate curr : currWord) {
            word = word + curr.getName();
        }

        // System.out.print(word + " ");

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (word.equalsIgnoreCase(line) && word.length() > 2) {
                    words.add(currWord);
                    // System.out.print(word + " ");
                }
            }
            reader = new BufferedReader(new FileReader(FILE_NAME));
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void makeMoves(Set<List<Coordinate>> validWords) {
        int color = bot.getPixelColor(1100, 800).getRed();
        for (List<Coordinate> currWord : validWords) {

            // print word
            String word = "";
            for (Coordinate curr : currWord) {
                word = word + curr.getName();
            }
            System.out.print(word + " ");
            
            if (abs(color - bot.getPixelColor(1100, 800).getRed()) > TOLERANCE) {
                bot.delay(5000);
                bot.mouseMove(750, 275);
                bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                break;
            }

            Coordinate prev = currWord.get(0);
            for (int i = 1; i < currWord.size(); i++) {
                move(prev, currWord.get(i));
                prev = currWord.get(i);
            }
            bot.delay(40);
            bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    private void move(Coordinate prev, Coordinate next) {
        bot.mouseMove(prev.getX(), prev.getY());
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.delay(40);
        bot.mouseMove(DONUT_CENTER_X, DONUT_CENTER_Y);
        bot.delay(40);
        bot.mouseMove(next.getX(), next.getY());
    }

    // private String identifyLetter(int whiteCount) {
    //     whiteCount = whiteCount; // adjust for ratio !!!
    //     int tol = TOLERANCE - 7;
    //     String letter = "";
    //     if (whiteCount < 323 + tol) {
    //         letter = "I";
    //     } else if (whiteCount < 491 + tol) {
    //         letter = "T";
    //     } else if (whiteCount < 514 + tol) {
    //         letter = "F";
    //     } else if (whiteCount < 608 + tol) {
    //         letter = "S";
    //     } else if (whiteCount < 625 + tol) {
    //         letter = "C";
    //     } else if (whiteCount < 622 + tol) {
    //         letter = "P";
    //     } else if (whiteCount < 626 + tol) {
    //         letter = "E";
    //     } else if (whiteCount < 664 + tol) {
    //         letter = "A";
    //     // } else if (whiteCount < 740 + tol) {
    //     //     letter = "R";
    //     } else if (whiteCount < 784 + tol) {
    //         letter = "B";
    //     // } else if (whiteCount < 753 + tol) {
    //     //     letter = "D";
    //     // } else if (whiteCount < 775 + tol) {
    //     //     letter = "O";
    //     // } else if (whiteCount < 829 + tol) {
    //     //     letter = "N";
    //     } else if (whiteCount < 967 + tol) {
    //         letter = "M";
    //     // } else if (whiteCount < 994 + tol) {
    //     //     letter = "W";
    //     } else {
    //         System.out.print("nuh uh, no ");
    //         letter = "O";
    //     }
    //     System.out.print(letter + "\n");
    //     return letter;
    // }
}