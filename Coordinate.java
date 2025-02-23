public class Coordinate {
    private int x;
    private int y;
    private int index;
    private char name;

    public Coordinate(int x, int y, int index, char name) {
        this.x = x;
        this.y = y;
        this.index = index;
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public int getIndex() {
        return index;
    }

    public char getLetter() {
        return name;
    }
}
