package cont;

public class Main {
    private static final ContinuationScope FOO = new ContinuationScope("FOO");

    public static void main(String[] args) {
        Continuation cont = new Continuation(FOO, () -> {
            System.out.println("enter");
            Continuation.yield(FOO);
            System.out.println("reenter");
        });

        cont.run();
        System.out.println("after first run");

        cont.run();
        System.out.println("after second run");
    }
}
