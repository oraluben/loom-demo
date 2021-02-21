package bench;

public class Main {
    public static void main(String[] args) throws Exception {
        assert args.length == 3;

        int
                start = Integer.parseInt(args[0]),
                end = Integer.parseInt(args[1]),
                step = Integer.parseInt(args[2]);

        for (int iteration = start; iteration <= end; iteration += step) {
            System.out.println(iteration + " " +
                    Bench.run(Bench::nopVirtual, iteration) + " " +
                    Bench.run(Bench::nop, iteration));
        }
    }
}
