package ru.ifmo.rain.khalimov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        SocketAddress socketAddress;
        try {
            socketAddress = new InetSocketAddress(InetAddress.getByName(host), port);
        } catch (UnknownHostException e) {
            System.err.println("Incorrect host : " + host);
            return;
        }

        for (int j = 0; j < threads; j++) {
            int thread = j;
            threadPool.submit(() -> start(socketAddress, prefix, thread, requests));
        }

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(1000, TimeUnit.DAYS);
        } catch (InterruptedException ignored) {}
    }

    private void start(SocketAddress socketAddress, String prefix, int thread, int requests) {
        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.setSoTimeout(1000);
            int size = datagramSocket.getReceiveBufferSize();

            for (int i = 0; i < requests; i++) {
                boolean ok = false;
                while (!ok) {
                    try {
                        String requestString = String.format("%s%d_%d", prefix, thread, i);
                        byte requestBytes[] = requestString.getBytes("UTF-8");
                        datagramSocket.send(new DatagramPacket(requestBytes, requestBytes.length, socketAddress));

                        DatagramPacket response = new DatagramPacket(new byte[size], size);
                        datagramSocket.receive(response);
                        String responseString = new String(response.getData(), response.getOffset(), response.getLength(), "UTF-8");

                        if (checkResponse(responseString, requestString)) {
                            ok = true;
                            System.out.println("Client : " + requestString);
                            System.out.println("Server : " + responseString);
                        }
                    } catch (IOException ignored) {}
                }
            }
        } catch (SocketException e) {
            System.err.println("Can't connect to address : " + socketAddress.toString());
        }
    }

    private boolean checkResponse(String response, String request) {
        return response.length() > request.length() && (response.contains(request + " ") || response.endsWith(request));
    }

    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Incorrect arguments");
            return;
        }
        int port, threads, requests;
        try {
            port = Integer.parseInt(args[1]);
            threads = Integer.parseInt(args[3]);
            requests = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Incorrect arguments");
            return;
        }

        new HelloUDPClient().run(args[0], port, args[2], threads, requests);
    }
}
