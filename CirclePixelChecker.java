/** CREDITS
 * PAT
 */

import java.awt.*;

public class CirclePixelChecker {
	private static final int TOLERANCE = 20;
	private static final int MIN_DISTANCE = 60;
    private Robot bot;

	public int countLetters(int centerX, int centerY, int radius) {
		try {
            bot = new Robot();
        } catch (Exception e) {
            // TODO: handle exception
        }

		int count = 1; //always assumes there's at least one letter
		boolean wasWhite = false;
		Point prevWhitePx = null, start = null;

		for (int angle = 0; angle < 325; angle++) {
			int x = (int) (centerX + radius * Math.cos(Math.toRadians(angle)));
			int y = (int) (centerY + radius * Math.sin(Math.toRadians(angle)));


			Color color = bot.getPixelColor(x, y);
			// bot.mouseMove(x, y);
            // bot.delay(10);

			boolean isCloseToWhite = (Math.abs(color.getRed() - 255) <= TOLERANCE)
					&& (Math.abs(color.getGreen() - 255) <= TOLERANCE)
					&& (Math.abs(color.getBlue() - 255) <= TOLERANCE);

			if (isCloseToWhite) {
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

	private static double distance(int x1, int y1, int x2, int y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}
}
