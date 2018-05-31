package ru.ifmo.rain.khalimov.concurrent;

import java.util.List;

public class Utils {
    public static void joinThreads(List<Thread> threads) throws InterruptedException {
        InterruptedException exception = null;
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                exception = e;
            }
        }
        if (exception != null) {
            throw exception;
        }
    }
}
