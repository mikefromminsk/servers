package com.mfm_wallet;

import com.sockets.test.Backend;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import static com.sockets.test.utils.Params.map;

public class Token extends TokenUtils {
/*
    static boolean tokenSendAndCommit(String domain, String from, String to, double amount, String password) throws NoSuchAlgorithmException, SQLException {
        Map<String, String> account = getAccount(domain, from);
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
