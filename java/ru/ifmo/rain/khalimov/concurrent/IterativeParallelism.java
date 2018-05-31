package ru.ifmo.rain.khalimov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {
    private ParallelMapper parallelMapper = null;

    private <T, R> R applyFunction(int cnt, List<T> list, Function<Stream<? extends T>, R> function, Function<Stream<R>, R> functionAfter) throws InterruptedException {
        List<R> results;
        List<Stream<T>> streamList = getSubLists(cnt, list);

        if (parallelMapper != null) {
            results = parallelMapper.map(function, streamList);
        } else {
            results = new ArrayList<>();
            for (int i = 0; i < cnt; i++) {
                results.add(null);
            }
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < cnt; i++) {
                threads.add(createThread(streamList.get(i), function, results, i));
                threads.get(i).start();
            }

            Utils.joinThreads(threads);
        }
        return functionAfter.apply(results.stream().filter(Objects::nonNull));
    }

    private <T> List<Stream<T>> getSubLists(int threads, List<T> list) {
        List<Stream<T>> result = new ArrayList<>();
        int len = list.size() / threads;
        int more = list.size() % threads;
        int index = 0;
        for (int i = 0; i < threads; i++) {
            int size = len + (i < more ? 1 : 0);
            result.add(list.subList(index, index + size).stream());
            index += size;
        }
        return result;
    }

    private <T, R> Thread createThread(Stream<T> stream, Function<Stream<? extends T>, R> function, List<R> result, int pos) {
        return new Thread(() -> {
            R res = function.apply(stream);
            synchronized (result) {
                result.set(pos, res);
            }
        });
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return applyFunction(threads, list,
                (s) -> s.max(comparator).orElse(null),
                (s) -> s.max(comparator).orElse(null));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, list, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return applyFunction(threads, list,
                (s) -> s.allMatch(predicate),
                (s) -> s.allMatch((x) -> x));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, list, predicate.negate());
    }

    @Override
    public String join(int threads, List<?> list) throws InterruptedException {
        return map(threads, list, Object::toString).stream().collect(Collectors.joining());
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return applyFunction(threads, list,
                (s) -> s.filter(predicate).collect(Collectors.toList()),
                (s) -> s.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return applyFunction(threads, list,
                (s) -> s.map(function).collect(Collectors.toList()),
                (s) -> s.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    public IterativeParallelism() {
    }
}
