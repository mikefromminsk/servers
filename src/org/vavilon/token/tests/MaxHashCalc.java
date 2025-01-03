package org.vavilon.token.tests;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class MaxHashCalc {

    static AtomicLong hashCount = new AtomicLong(1);
    static long start = System.currentTimeMillis();

    public static void main(String[] args) {
        int numThreads = 20; // Установите количество потоков
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA-512");
                    byte[] previousHash = "admin".getBytes();
                    while (true) {
                        previousHash = md.digest(previousHash);
                        long count = hashCount.incrementAndGet();
                        if (count % 10_000_000 == 0)
                            System.out.println("Hash count: " + count
                                    + " avg " + ((System.currentTimeMillis() - start) / (count / 10_000_000)));
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            });
        }
        //executor.shutdown();
    }
}
