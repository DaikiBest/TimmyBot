import java.awt.AWTException;
import java.io.FileNotFoundException;

public class TimBot {
    public static void main(String[] args) {
        try {
            WordSolver solver = new WordSolver();
            solver.solve();
        } catch (AWTException e) {
            System.out.println("Exception thrown");
        } catch (FileNotFoundException e) {
            System.out.println("Exception thrown");
        }
    }
}