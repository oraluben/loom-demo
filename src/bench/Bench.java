package bench;

import java.util.HashMap;
import java.util.Map;

public class Bench {
    private static int iteration;

    private static final Map<Benchmark, Boolean> warmuped = new HashMap<>();

    public static void nop() throws InterruptedException {
        for (int i = 0; i < iteration; i++) {
            Thread.builder().task(() -> {
            }).start().join();
        }
    }

    public static void nopVirtual() throws InterruptedException {
        for (int i = 0; i < iteration; i++) {
            Thread.builder().virtual().task(() -> {
            }).start().join();
        }
    }

    public static long run(Benchmark r, int overwrite_iteration) throws Exception {
        if (!warmuped.getOrDefault(r, false)) {
            iteration = 2000;
            r.run();
            warmuped.put(r, true);
        }

        iteration = overwrite_iteration;

        long tStart = System.nanoTime();
        r.run();
        return System.nanoTime() - tStart;
    }

    public interface Benchmark {
        void run() throws Exception;
    }
}
