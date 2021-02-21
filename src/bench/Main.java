package bench;

public class Main {
    private static ContinuationScope FOO = new ContinuationScope("FOO");

    public static void main(String[] args) {
        System.out.println(run(1_000_000));
    }

    private static long run(int i) {
        Continuation cont;
        for (int j = 0; j < 2000; j++) {
            cont = new Continuation(FOO, () -> {
            });
            while (!cont.isDone()) {
                cont.run();
            }
        }

        long tStart = System.nanoTime();
        for (int j = 0; j < i; j++) {
            cont = new Continuation(FOO, () -> {
            });
            while (!cont.isDone()) {
                cont.run();
            }
        }
        return (System.nanoTime() - tStart) / i;
    }
}
