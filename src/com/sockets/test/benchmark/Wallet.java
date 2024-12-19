package com.sockets.test.benchmark;

import com.mfm_wallet.model.Account;
import com.sockets.test.utils.MD5;
import com.sockets.test.utils.Request;
import com.sockets.test.utils.Success;

import java.util.HashMap;

import static com.mfm_wallet.Utils.gson;
import static com.sockets.test.utils.Params.map;

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

    public static void send(String host,
                            String fromAddress,
                            String password,
                            String domain,
                            String prev_key,
                            String toAddress,
                            String amount,
                            Success success,
                            Success error) {
        calcKeyHash(fromAddress, password, domain, prev_key, pass -> {
            Request.post(host + "/mfm-token/send.php", map(
                    "domain", domain,
                    "from_address", fromAddress,
                    "to_address", toAddress,
                    "amount", amount,
                    "pass", pass
            ), success, error);
        });
    }

    public static void reg(String host,
                           String address,
                           String password,
                           String domain,
                           Success success,
                           Success error) {
        calcStartHash(address, password, domain, pass -> {
            Request.post(host + "/mfm-token/send.php", map(
                    "domain", domain,
                    "from_address", "owner",
                    "to_address", address,
                    "amount", "0",
                    "pass", ":" + pass
            ), success, error);
        });
    }

    class AccountResponse {
        Account account;
    }

    public static void send(String host,
                            String fromAddress,
                            String password,
                            String domain,
                            String toAddress,
                            String amount,
                            Success success,
                            Success error) {
        Request.post(host + "/mfm-token/account.php", map(
                "domain", domain,
                "address", fromAddress
        ), responseStr -> {
            AccountResponse response = gson.fromJson(responseStr, AccountResponse.class);
            send(host, fromAddress, password, domain, response.account.prev_key, toAddress, amount, success, error);
        }, response -> {
            reg(host, fromAddress, password, domain, response1 -> {
                send(host, fromAddress, password, domain, toAddress, amount, success, error);
            }, error);
        });
    }
}
