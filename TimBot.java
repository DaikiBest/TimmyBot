
public class TimBot {
    public static void main(String[] args) {
        try {
            WordSolver solver = new WordSolver();
            solver.solve();
        } catch (Exception e) {
            System.out.println("Exception thrown");
        }
    }
}