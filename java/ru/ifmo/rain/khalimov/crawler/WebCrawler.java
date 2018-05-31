package ru.ifmo.rain.khalimov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private ExecutorService downloaders;
    private ExecutorService extractors;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
    }

    @Override
    public Result download(String url, int depth) {
        Map<String, Boolean> visited = new ConcurrentHashMap<>();
        Map<String, IOException> errors = new HashMap<>();
        Set<String> downloaded = ConcurrentHashMap.newKeySet();
        Phaser phaser = new Phaser();

        phaser.register();
        downloaders.submit(() -> {
            downloadPage(url, downloaded, errors, visited, depth, phaser);
            phaser.arrive();
        });

        phaser.awaitAdvance(0);
        return new Result(new ArrayList<>(downloaded), errors);
    }

    private void downloadPage(String url, Set<String> downloaded, Map<String, IOException> errors,
                              Map<String, Boolean> visited, int depth, Phaser phaser) {
        if (visited.putIfAbsent(url, true) == null) {
            try {
                Document document = downloader.download(url);
                downloaded.add(url);
                if (depth > 1) {
                    phaser.register();
                    extractors.submit(() -> {
                        extractLinks(document, downloaded, errors, visited, depth, phaser);
                        phaser.arrive();
                    });
                }
            } catch (IOException e) {
                errors.put(url, e);
            }
        }
    }

    private void extractLinks(Document document, Set<String> downloaded, Map<String, IOException> errors,
                              Map<String, Boolean> visited, int depth, Phaser phaser) {
        try {
            for (String link : document.extractLinks()) {
                phaser.register();
                downloaders.submit(() -> {
                    downloadPage(link, downloaded, errors, visited, depth - 1, phaser);
                    phaser.arrive();
                });
            }
        } catch (IOException ignored) {
        }
    }


    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Incorrect arguments");
            return;
        }

        int depth, downloaders, extractors, perHost;
        try {
            depth = Integer.parseInt(args[1]);
            downloaders = Integer.parseInt(args[2]);
            extractors = Integer.parseInt(args[3]);
            perHost = Integer.parseInt(args[4]);
        } catch (NumberFormatException | NullPointerException e) {
            System.err.println("Incorrect arguments");
            return;
        }
        try (Crawler crawler = new WebCrawler(new CachingDownloader(), downloaders, extractors, perHost)){
            Result result = crawler.download(args[0], depth);
            for (String url : result.getDownloaded()) {
                System.out.println(url);
            }
        } catch (IOException e) {
            System.err.println("Can't create downloader");
        }
    }

    @Override
    public void close() {
        downloaders.shutdown();
        extractors.shutdown();
    }
}
