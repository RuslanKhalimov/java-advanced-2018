package ru.ifmo.rain.khalimov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket datagramSocket;
    private ExecutorService threadPool;

    @Override
    public void start(int port, int threads) {
        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException ignored) {
            System.err.println("Can't bind to port : " + port);
            return;
        }
        threadPool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            threadPool.submit(() -> start());
        }
    }

    private void start() {
        try {
            int size = datagramSocket.getReceiveBufferSize();
            while (!datagramSocket.isClosed()) {
                try {
                    DatagramPacket request = new DatagramPacket(new byte[size], size);
                    datagramSocket.receive(request);
                    String responseString = "Hello, " + new String(request.getData(), request.getOffset(), request.getLength(), "UTF-8");
                    byte responseBytes[] = responseString.getBytes("UTF-8");
                    datagramSocket.send(new DatagramPacket(responseBytes, responseBytes.length, request.getSocketAddress()));
                } catch (IOException e) {
                    System.err.println("send/receive exception");
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Incorrect arguments");
            return;
        }
        int port, threads;
        try {
            port = Integer.parseInt(args[0]);
            threads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Incorrect arguments");
            return;
        }

        new HelloUDPServer().start(port, threads);
    }

    @Override
    public void close() {
        datagramSocket.close();
        threadPool.shutdown();
    }
}
