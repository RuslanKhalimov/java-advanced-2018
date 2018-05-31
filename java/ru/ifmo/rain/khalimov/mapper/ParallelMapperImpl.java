package ru.ifmo.rain.khalimov.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import ru.ifmo.rain.khalimov.concurrent.Utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private List<Thread> threads;
    private final Queue<Runnable> tasks;

    public ParallelMapperImpl(int cnt) {
        tasks = new ArrayDeque<>();
        threads = new ArrayList<>();

        for (int i = 0; i < cnt; ++i) {
            threads.add(new Thread(() -> {
                while (!Thread.interrupted()) {
                    Runnable task;
                    synchronized (tasks) {
                        try {
                            while (tasks.isEmpty()) {
                                tasks.wait();
                            }
                        } catch (InterruptedException e) {
                            break;
                        }
                        task = tasks.poll();
                    }
                    task.run();
                }
            }));
            threads.get(i).start();
        }
    }

    private class SynchronizedList<T> {
        private final List<T> list;
        private int size;

        SynchronizedList(int size) {
            list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(null);
            }
        }

        synchronized private void set(int i, T value) {
            list.set(i, value);
            size++;
            if (size == list.size()) {
                notify();
            }
        }

        synchronized private List<T> getResult() throws InterruptedException {
            while (size < list.size()) {
                wait();
            }
            return list;
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        SynchronizedList<R> result = new SynchronizedList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            final int ii = i;
            synchronized (tasks) {
                tasks.add(() -> result.set(ii, function.apply(list.get(ii))));
                tasks.notify();
            }
        }

        return result.getResult();
    }

    @Override
    public void close() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
        try {
            Utils.joinThreads(threads);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
