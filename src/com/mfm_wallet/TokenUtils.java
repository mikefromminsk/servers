package com.mfm_wallet;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

import static com.sockets.test.utils.Params.map;

class TokenUtils extends DBUtils {
    /*static final String GAS_DOMAIN = "usdt";
    static final String GENESIS_ADDRESS = "owner";

    static String tokenKey(String domain, String address, String password, String prevKey) throws NoSuchAlgorithmException {
        return md5(domain + address + password + prevKey);
    }

    static String tokenNextHash(String domain, String address, String password, String prevKey) throws NoSuchAlgorithmException {
        return md5(tokenKey(domain, address, password, prevKey));
    }

    static String tokenPass(String domain, String address, String password) throws NoSuchAlgorithmException, SQLException {
        Map<String, String> account = DBUtils.getAccount(domain, address);
        String key = tokenKey(domain, address, password, account.get("prev_key"));
        String nextHash = tokenNextHash(domain, address, password, key);
        return key + ":" + nextHash;
    }
*/
    static String tokenSend(String domain,
                                   String fromAddress,
                                   String toAddress,
                                   double amount,
                                   String pass,
                                   String delegate) {

        if (fromAddress.equals(toAddress) && amount != 0) error("from_address and to_address are the same");
        String key = pass != null ? pass.split(":")[0] : null;
        String nextHash = pass != null ? pass.split(":")[1] : null;
        if (amount != Math.round(amount * 100) / 100.0) error("amount tick is 0.01");
        if (amount < 0) error("amount less than 0");
        /*if (fromAddress.equals(GENESIS_ADDRESS)) {
            if (domain.length() < 3 || domain.length() > 16) error("domain length has to be between 3 and 16");
            if (tokenBalance(domain, GENESIS_ADDRESS) == null) {
                setAccount(domain, GENESIS_ADDRESS, map(
                        "prev_key", "",
                        "next_hash", "",
                        "balance", amount,
                        "delegate", "mfm-token/send.php"
                ));
                if (scalarWhere("tokens", "owner", map("domain", domain)) == null && amount > 0) {
                    insertRow("tokens", map(
                            "domain", domain,
                            "owner", toAddress,
                            "supply", amount,
                            "created", System.currentTimeMillis()
                    ));
                    trackAccumulate("tokens_count");
                }
            }
            Map<String, String> gasAccount = getAccount(GAS_DOMAIN, toAddress);
            if (!domain.equals(GAS_DOMAIN) && gasAccount.get("delegate") != null) {
                delegate = (String) gasAccount.get("delegate");
            }
            if (tokenBalance(domain, toAddress) == null) {
                setAccount(domain, toAddress, map(
                        "prev_key", "",
                        "next_hash", nextHash,
                        "balance", 0,
                        "delegate", delegate
                ));
            }
        }

        Map<String, String> from = getAccount(domain, fromAddress);
        Map<String, String> to = getAccount(domain, toAddress);
        from.put("balance", Math.round((Double) from.get("balance") * 100) / 100.0);
        if ((Double) from.get("balance") < amount) error(domain.toUpperCase() + " balance is not enough in " + fromAddress + " wallet. Balance: " + from.get("balance") + " Need: " + amount);
        if (to == null) error(toAddress + " receiver doesn't exist");
        if (from.get("delegate") != null) {
            if (!from.get("delegate").equals(getScriptPath())) error("script " + getScriptPath() + " cannot use " + fromAddress + " address. Only " + from.get("delegate"));
        } else {
            if (!from.get("next_hash").equals(md5(key))) error(domain + " key is not right");
        }

        if (from.get("delegate") != null) {
            setAccount(domain, fromAddress, of(
                    "balance", Math.round(((Double) from.get("balance") - amount) * 100) / 100.0
            ));
        } else {
            setAccount(domain, fromAddress, map(
                    "balance", Math.round(((Double) from.get("balance") - amount) * 100) / 100.0,
                    "prev_key", key,
                    "next_hash", nextHash
            ));
        }

        double fee = 0;

        setAccount(domain, toAddress, map(
                "balance", Math.round(((Double) to.get("balance") + amount - fee) * 100) / 100.0
        ));

        saveTran(map(
                "domain", domain,
                "from", fromAddress,
                "to", toAddress,
                "amount", amount,
                "fee", fee,
                "key", key,
                "next_hash", nextHash,
                "delegate", delegate,
                "time", System.currentTimeMillis()
        ));*/

        return nextHash;
    }

    /*static void broadcast(String channel, Map<String, String> message) {
        // Implement broadcast logic here
    }

    static void trackAccumulate(String key) {
        // Implement tracking logic here
    }

    static void trackAccumulate(String key, int value) {
        // Implement tracking logic here
    }

    static String getScriptPath() {
        // Implement getScriptPath logic here
        return "";
    }*/
}
