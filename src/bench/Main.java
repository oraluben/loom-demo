package bench;

public class Main {
    public static void main(String[] args) throws Exception {
        for (int iteration = 100; iteration <= 10_000; iteration += 100) {
            System.out.println(iteration + " " +
                    Bench.run(Bench::nopVirtual, iteration) + " " +
                    Bench.run(Bench::nop, iteration));
        }
    }
}
