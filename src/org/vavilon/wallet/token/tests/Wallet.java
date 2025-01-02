package org.vavilon.wallet.token.tests;

import org.vavilon.wallet.token.model.Account;
import org.vavilon.utils.MD5;
import org.vavilon.utils.Request;
import org.vavilon.utils.Success;

import static org.vavilon.wallet.Utils.gson;
import static org.vavilon.utils.Params.map;

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
            Request.post(host + "/mfm-token/send", map(
                    "domain", domain,
                    "from", fromAddress,
                    "to", toAddress,
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
            Request.post(host + "/mfm-token/send", map(
                    "domain", domain,
                    "from", "owner",
                    "to", address,
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
        Request.post(host + "/mfm-token/account", map(
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
