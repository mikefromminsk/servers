package com.sockets.test.utils;

import com.sockets.test.Backend;

import java.util.HashMap;

public class Wallet {

    public static String calcHash(String domain, String username, String password, String prevKey) {
        return MD5.hash(domain + username + password + (prevKey != null ? prevKey : ""));
    }

    public static void calcStartHash(String address, String password, String domain, Success success) {
        success.run(MD5.hash(calcHash(domain, address, password, "")));
    }

    public static void calcKeyHash(String address, String password, String domain, String prevKey, Success success) {
        String key = calcHash(domain, address, password, prevKey);
        String nextHash = MD5.hash(calcHash(domain, address, password, key));
        success.run(key + ":" + nextHash);
    }

    public static void send(String fromAddress,
                            String password,
                            String domain,
                            String prev_key,
                            String toAddress,
                            String amount,
                            Success success,
                            Success error) {
        calcKeyHash(fromAddress, password, domain, prev_key, pass -> {
            Backend.postToLocalhost("/mfm-token/send.php", new HashMap<String, String>() {{
                put("domain", domain);
                put("from_address", fromAddress);
                put("to_address", toAddress);
                put("amount", amount);
                put("pass", pass);
            }}, success, error);
        });
    }

    public static void reg(String address,
                            String password,
                            String domain,
                            Success success,
                            Success error) {
        calcStartHash(address, password, domain, pass -> {
            Backend.postToLocalhost("/mfm-token/send.php", new HashMap<String, String>() {{
                put("domain", domain);
                put("from_address", "owner");
                put("to_address", address);
                put("amount", "0");
                put("pass", ":" + pass);
            }}, success, error);
        });
    }

    public static void send(String fromAddress,
                            String password,
                            String domain,
                            String toAddress,
                            String amount,
                            Success success,
                            Success error) {
        Backend.postToLocalhost("/mfm-token/account.php", new HashMap<String, String>() {{
            put("domain", domain);
            put("address", fromAddress);
        }}, response -> {
            HashMap<String, String> data = Backend.json.fromJson(response, HashMap.class);
            send(fromAddress, password, domain, data.get("prev_key"), toAddress, amount, success, error);
        }, response -> {
            reg(fromAddress, password, domain, response1 -> {
                send(fromAddress, password, domain, toAddress, amount, success, error);
            }, error);
        });
    }
}
