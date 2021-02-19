package vthread;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread.builder().task(() -> {
            System.out.println(Thread.currentThread());
        }).start().join();
        Thread.builder().virtual().task(() -> {
            System.out.println(Thread.currentThread());
        }).start().join();
    }
}
