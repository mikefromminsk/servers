package com.hatosh.wallet.token.tests;

import com.hatosh.utils.Success;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hatosh.wallet.Utils.disableCertificateValidation;
import static com.hatosh.wallet.Utils.time;

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
                    Wallet.send("https://mytoken.space:8011", address, password, "usdt", address, "0", success, error);
                }
            });
        }
    }

    static Integer success = 0;
    static Integer error = 0;
    static Long start = 0L;

    public static void main(String[] args) throws Exception {
        start = time();
        disableCertificateValidation();
        TPS.startSendingRequests(TPS.generateAccounts(20),
                response -> {
                    success = success + 1;
                    System.out.println("TPS success " + success + " error " + error
                            + " avg " + (success + error) / (time() - start));
                }, response -> {
                    System.out.println("TPS error " + ++error + " success " + success);
                });
    }




}
