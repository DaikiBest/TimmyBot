
/** CREDITS
 * PAT
 */

import java.awt.*;
import java.awt.image.BufferedImage;

public class CirclePixelChecker {
    private static final int MIN_DISTANCE = 60;
    private static final int DONUT_RADIUS_BASELINE = 135;
    private Robot bot;

    public CirclePixelChecker(Robot bot) {
        this.bot = bot;
    }

    // Revolve around the donut and counts the number of letters it finds
    public int countLetters(int centerX, int centerY, int radius, double ratio) {
        int donutRadius = (int) (DONUT_RADIUS_BASELINE / ratio);
        BufferedImage img = bot.createScreenCapture(new Rectangle(centerX - donutRadius,
                centerY - donutRadius, donutRadius * 2, donutRadius * 2));

        int count = 1; // always assumes there's at least one letter
        boolean wasWhite = false;
        Point prevWhitePx = null, start = null;

        for (int angle = 0; angle < 325; angle++) {
            int x = (int) (donutRadius + radius * Math.cos(Math.toRadians(angle)));
            int y = (int) (donutRadius + radius * Math.sin(Math.toRadians(angle)));

            int color = img.getRGB(x, y);
            // bot.mouseMove(x, y);
            // bot.delay(10);

            if (color == 0xFFFFFFFF) {
                if (start == null)
                    start = new Point(x, y);

                if (!wasWhite && (prevWhitePx == null || distance(x, y, prevWhitePx.x, prevWhitePx.y) > MIN_DISTANCE)) {
                    if (!start.equals(new Point(x, y))) {
                        count++;
                        // bot.delay(300);
                    }
                    prevWhitePx = new Point(x, y);
                }
                wasWhite = true;
            } else {
                wasWhite = false;
            }
        }
        return count;
    }

    // Distance between two points
    private static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
