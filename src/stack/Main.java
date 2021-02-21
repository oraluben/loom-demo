package stack;

public class Main {
    public static void main(String[] args) {
        Cont.run();
    }
}


/**
 * <pre>
 *    :                                :
 *    |                                |
 *    |                                |
 *    +--------------------------------+
 *    | Continuation.run()             |
 *    +--------------------------------+
 *    : frames of running continuation :
 *    +--------------------------------+
 *    | Continuation.yield()           |
 *    +--------------------------------+
 *    |                                |
 *    :     ...                        :
 * </pre>
 */
class Cont {
    private static final ContinuationScope FOO = new ContinuationScope("FOO");

    public static void run() {
        Continuation cont1 = new Continuation(FOO,
                () -> {
                    Continuation.yield(FOO);
                }
        );
        Continuation cont2 = new Continuation(FOO,
                () -> {
                }
        );
        cont1.run(); // returned from cont1.run()
        cont2.run(); // returned from cont2.run()
        cont1.run(); // returned from cont1.run()
    }
}
