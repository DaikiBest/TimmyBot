public class Coordinate {
    private int x;
    private int y;
    private int index;
    private String name;

    public Coordinate(int x, int y, int index, String name) {
        this.x = x;
        this.y = y;
        this.index = index;
        this.name = name;
    }

    public Coordinate(int x, int y, int index) {
        this.x = x;
        this.y = y;
        this.index = index;
        this.name = "";
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

    public String getName() {
        return name;
    }
}
