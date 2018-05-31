package ru.ifmo.rain.khalimov.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Walk {
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Incorrect arguments");
            return;
        }

        Path inputPath;
        try {
            inputPath = Paths.get(args[0]);
        } catch (InvalidPathException e) {
            System.err.println("Invalid path to input file");
            return;
        }

        Path outputPath;
        try {
            outputPath = Paths.get(args[1]);
        } catch (InvalidPathException e) {
            System.err.println("Invalid path to output file");
            return;
        }

        if (!inputPath.toFile().exists()) {
            System.err.println("Input file doesn't exist");
            return;
        }

        if (!outputPath.toFile().exists()) {
            System.err.println("Output file doesn't exist");
        }

        try (BufferedReader inputFile = new BufferedReader(Files.newBufferedReader(inputPath))) {
            try (BufferedWriter outputFile = new BufferedWriter(Files.newBufferedWriter(outputPath))) {
                String nextFileName;
                while ((nextFileName = inputFile.readLine()) != null) {
                    try {
                        outputFile.write(String.format("%08x %s\n", FNVHash(nextFileName), nextFileName));
                    } catch (IOException e) {
                        outputFile.write(String.format("%08x %s\n", 0, nextFileName));
                        System.out.println(e.getMessage());
                    }
                }
            } catch (SecurityException e) {
                System.out.println("Can't access to output file");
            } catch (IOException e) {
                System.out.println("Error writing to output file");
            }
        } catch (SecurityException e) {
            System.out.println("Can't access to input file");
        } catch (IOException e) {
            System.out.println("Error reading input file");
        }
    }

    private static int FNVHash(String fileName) throws IOException {
        Path nextFilePath;
        try {
            nextFilePath = Paths.get(fileName);
        } catch (InvalidPathException e) {
            throw new IOException("Invalid path to file " + fileName);
        }

        if (!nextFilePath.toFile().exists()) {
            throw new IOException(String.format("File %s doesn't exist", fileName));
        }

        try (BufferedInputStream nextFile = new BufferedInputStream(Files.newInputStream(nextFilePath))) {
            return FNVHash(nextFile);
        } catch (IOException e) {
            throw new IOException("Error reading file " + fileName);
        }
    }

    private static int FNVHash(InputStream file) throws IOException {
        int h = 0x811c9dc5, fnv = 0x01000193, cnt;

        byte[] buf = new byte[1024];
        while ((cnt = file.read(buf)) >= 0) {
            for (int i = 0; i < cnt; i++) {
                h = (h * fnv) ^ (buf[i] & 0xff);
            }
        }

        return h;
    }
}
