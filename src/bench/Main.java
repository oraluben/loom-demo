package bench;

import java.util.Arrays;

public class Main {
    private static final ContinuationScope SCOPE = new ContinuationScope("FOO");
    private static boolean warmup;

    public static void main(String[] args) throws InterruptedException {
        System.out.println(Arrays.toString(args));
        assert args.length > 2;
        switch (args[0]) {
            case "loom", "jdk" -> {
                warmup = true;
            }
            case "svm", "native-image" -> {
                warmup = false;
            }
            default -> {
                throw new IllegalArgumentException(args[0]);
            }
        }
        switch (args[1]) {
            case "cont" -> {
                if (warmup) runCont(20000);
                System.out.println(runCont(Integer.parseInt(args[2])));
            }
            case "yield-cont" -> {
                if (warmup) runYieldCont(20000);
                System.out.println(runYieldCont(Integer.parseInt(args[2])));
            }
            case "vthread" -> {
                if (warmup) runVThread(20000);
                System.out.println(runVThread(Integer.parseInt(args[2])));
            }
            case "yield-vthread" -> {
                if (warmup) runYieldVThread(20000);
                System.out.println(runYieldVThread(Integer.parseInt(args[2])));
            }
            case "yield-deep-cont" -> {
                if (warmup) runYieldDeepCont(20000, 10);
                System.out.println(runYieldDeepCont(Integer.parseInt(args[2]), Integer.parseInt(args[3])));
            }
            case "yields-cont" -> {
                if (warmup) runYieldsCont(20000, 10);
                System.out.println(runYieldsCont(Integer.parseInt(args[2]), Integer.parseInt(args[3])));
            }
            default -> System.out.println("unsupported bench: " + args[0] + ".");
        }
    }

    private static long runCont(int i) {
        Continuation cont;

        long tStart = System.nanoTime();
        for (int j = 0; j < i; j++) {
            cont = new Continuation(SCOPE, () -> {
            });
            while (!cont.isDone()) {
                cont.run();
            }
        }
        return System.nanoTime() - tStart;
    }

    private static long runYieldCont(int i) {
        Continuation cont;

        long tStart = System.nanoTime();
        for (int j = 0; j < i; j++) {
            cont = new Continuation(SCOPE, () -> {
                Continuation.yield(SCOPE);
            });
            while (!cont.isDone()) {
                cont.run();
            }
        }
        return System.nanoTime() - tStart;
    }

    private static long runVThread(int i) throws InterruptedException {
        long tStart = System.nanoTime();
        for (int j = 0; j < i; j++) {
            Thread.builder().virtual().task(() -> {
            }).start().join();
        }
        return System.nanoTime() - tStart;
    }

    private static long runYieldVThread(int i) throws InterruptedException {
        long tStart = System.nanoTime();
        for (int j = 0; j < i; j++) {
            Thread.builder().virtual().task(() -> {
                Thread.yield();
            }).start().join();
        }
        return System.nanoTime() - tStart;
    }

    private static long runYieldDeepCont(int i, int depth) {
        Continuation cont;

        long tStart = System.nanoTime();
        for (int j = 0; j < i; j++) {
            cont = Yielder.continuation(2, depth, false);
            while (!cont.isDone())
                cont.run();
        }

        return (System.nanoTime() - tStart) / i;
    }

    private static long runYieldsCont(int i, int depth) {
        Continuation cont;

        long tStart = System.nanoTime();
        for (int j = 0; j < i; j++) {
            cont = Yielder.continuation(2, depth, true);
            while (!cont.isDone())
                cont.run();
        }

        return (System.nanoTime() - tStart) / i;
    }

    static class Arg {
        volatile int field;
    }

    static class Yielder implements Runnable {
        private final int paramCount;
        private final int maxDepth;
        private final boolean yieldAtEveryLevel;

        private Yielder(int paramCount, int maxDepth, boolean yieldAtEveryLevel) {
            if (paramCount < 1 || paramCount > 3)
                throw new IllegalArgumentException();
            this.paramCount = paramCount;
            this.maxDepth = maxDepth;
            this.yieldAtEveryLevel = yieldAtEveryLevel;
        }

        @Override
        public void run() {
            switch (paramCount) {
                case 1 -> run1(maxDepth);
                case 2 -> run2(maxDepth, new Arg());
                case 3 -> run3(maxDepth, new Arg(), new Arg());
                default -> throw new Error("should not happen");
            }
        }

        private void run1(int depth) {
            if (depth > 0) {
                run1(depth - 1);
            }
            if (depth == 0) {
                Continuation.yield(SCOPE);
            } else if (yieldAtEveryLevel) {
                Continuation.yield(SCOPE);
            }
        }

        private void run2(int depth, Arg arg2) {
            if (depth > 0) {
                run2(depth - 1, arg2);
            }
            if (depth == 0) {
                Continuation.yield(SCOPE);
            } else if (yieldAtEveryLevel) {
                Continuation.yield(SCOPE);
            }
        }

        private void run3(int depth, Arg arg2, Arg arg3) {
            if (depth > 0) {
                run3(depth - 1, arg2, arg3);
            }
            if (depth == 0) {
                Continuation.yield(SCOPE);
            } else if (yieldAtEveryLevel) {
                Continuation.yield(SCOPE);
            }
        }

        static Continuation continuation(int paramCount, int maxDepth, boolean yieldAtEveryLevel) {
            Runnable task = new Yielder(paramCount, maxDepth, yieldAtEveryLevel);
            return new Continuation(SCOPE, 2000, task);
        }
    }
}
