package com.sockets.test.benchmark;

import com.sockets.test.utils.Success;
import com.sockets.test.utils.Wallet;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TPS {
    public static Map<String, String> generateAccounts(int numberOfAccounts) {
        Map<String, String> accounts = new HashMap<>();
        Random random = new Random();
        for (int i = 0; i < numberOfAccounts; i++)
            accounts.put("test" + random.nextInt(100000), "" + random.nextInt(100000));
        return accounts;
    }

    public static void startSendingRequests(Map<String, String> accounts,
                                            Success success,
                                            Success error) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(accounts.keySet().size());
        for (String address : accounts.keySet()) {
            Thread.sleep(new Random().nextInt(1000));
            String password = accounts.get(address);
            executor.submit(() -> {
                while (true) {
                    Wallet.send(address, password, "usdt", address, "0", success, error);
                }
            });
        }
    }

    static Integer success = 0;
    static Integer error = 0;
    static Long start = 0L;

    public static void main(String[] args) throws InterruptedException {
        start = System.currentTimeMillis();
        TPS.startSendingRequests(TPS.generateAccounts(20),
                response -> {
                    success = success + 1;
                    System.out.println("TPS success " + success + " error " + error
                            + " avg " + ((System.currentTimeMillis() - start) / (success + error)));
                }, response -> {
                    System.out.println("TPS error " + ++error + " success "  + success);
                });
    }
}