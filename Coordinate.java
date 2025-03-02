public class Coordinate {
    private int x;
    private int y;
    private char letter;

    // Constructor for only coords
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
        this.letter = 'z';
    }

    // Constructor with letter
    public Coordinate(int x, int y, char letter) {
        this.x = x;
        this.y = y;
        this.letter = letter;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public char getLetter() {
        return letter;
    }
}
