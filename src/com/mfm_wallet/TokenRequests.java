package com.mfm_wallet;

import com.mfm_wallet.model.Account;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class TokenRequests extends TokenUtils {

    static String tokenKey(String domain, String address, String password, String prevKey) throws NoSuchAlgorithmException {
        return md5(domain + address + password + prevKey);
    }

    static String tokenNextHash(String domain, String address, String password, String prevKey) throws NoSuchAlgorithmException {
        return md5(tokenKey(domain, address, password, prevKey));
    }

    static String tokenPass(String domain, String address, String password) throws NoSuchAlgorithmException, SQLException {
        /*Account account = DBUtils.getAccount(domain, address);
        String key = tokenKey(domain, address, password, account.prevKey);
        String nextHash = tokenNextHash(domain, address, password, key);
        return key + ":" + nextHash;*/
        return null;
    }

/*
    static boolean tokenSendAndCommit(String domain, String from, String to, double amount, String password) throws NoSuchAlgorithmException, SQLException {
        Account account = getAccount(domain, from);
        if (account != null) {
            String key = tokenKey(domain, from, password, (String) account.get("prev_key"));
            String nextHash = tokenNextHash(domain, from, password, key);
            return requestEquals("/mfm-token/send.php", map(
                    "domain", domain,
                    "from_address", from,
                    "to_address", to,
                    "amount", String.valueOf(amount),
                    "pass", key + ":" + nextHash
            ));
        } else {
            return false;
        }
    }

    public static boolean tokenRegAccount(String domain, String address, String password, double amount) throws NoSuchAlgorithmException, SQLException {
        return requestEquals("/mfm-token/send.php", map(
                "domain", domain,
                "from_address", GENESIS_ADDRESS,
                "to_address", address,
                "amount", String.valueOf(amount),
                "pass", ":" + tokenNextHash(domain, address, password)
        ));
    }

    public static boolean tokenRegScript(String domain, String address, String script) throws NoSuchAlgorithmException, SQLException {
        if (getAccount(domain, address) == null) {
            Backend.postToLocalhost("/mfm-token/send.php", map(
                    "domain", domain,
                    "from_address", GENESIS_ADDRESS,
                    "to_address", address,
                    "amount", "0",
                    "pass", ":" + md5(UUID.randomUUID().toString()),
                    "delegate", script
            ));
            return requestEquals("/mfm-token/send.php", map(
                    "domain", domain,
                    "from_address", GENESIS_ADDRESS,
                    "to_address", address,
                    "amount", "0",
                    "pass", ":" + md5(UUID.randomUUID().toString()),
                    "delegate", script
            ));
        } else {
            return false;
        }
    }

    public static boolean tokenDelegate(String domain, String address, String pass, String script) throws SQLException {
        if (getAccount(domain, address) != null) {
            return requestEquals("/mfm-token/send.php", map(
                    "domain", domain,
                    "from_address", GENESIS_ADDRESS,
                    "to_address", address,
                    "amount", "0",
                    "pass", pass
            ));
        } else {
            return false;
        }
    }

    public static void tokenChangePass(String domain, String address, String pass) throws NoSuchAlgorithmException, SQLException {
        tokenSend(domain, address, address, 0, pass);
    }*/

}
