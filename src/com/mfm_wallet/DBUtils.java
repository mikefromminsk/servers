package com.mfm_wallet;

import java.sql.SQLException;
import java.util.*;

import static com.sockets.test.utils.Params.map;

public class DBUtils extends BaseUtils {
/*
    private static Map<String, Map<String, Map<String, String>>> mfmAccounts = new HashMap<>();
    private static List<Map<String, String>> mfmTokenTrans = new ArrayList<>();


    public static void saveTran(Map<String, String> tran) {
        mfmTokenTrans.add(tran);
    }

    public static void commitTrans() throws SQLException {
        if (mfmTokenTrans != null) {
            List<Map<String, String>> transInInsertSequence = new ArrayList<>(mfmTokenTrans);
            Collections.reverse(transInInsertSequence);
            for (Map<String, String> tran : transInInsertSequence) {
                insertRow("trans", tran);
                broadcast("transactions", tran);
                trackAccumulate(tran.get("domain") + "_trans");
            }
            trackAccumulate("trans_count", transInInsertSequence.size());
            mfmTokenTrans = null;
        }
    }


    public static void setAccount(String domain, String address, Map<String, String> params) throws SQLException {
        Map<String, String> account = getAccount(domain, address);
        if (account == null) {
            account = new HashMap<>(params);
            account.put("commit", "insert");
            account.put("domain", domain);
            account.put("address", address);
        } else {
            if (account.get("commit") == null) {
                account.put("commit", "update");
            }
            account.putAll(params);
        }
        mfmAccounts.computeIfAbsent(domain, k -> new HashMap<>()).put(address, account);
    }

    public static Map<String, String> getAccount(String domain, String address) throws SQLException {
        mfmAccounts.computeIfAbsent(domain, k -> new HashMap<>());
        Map<String, String> account = mfmAccounts.get(domain).get(address);
        if (account == null) {
            account = selectRowWhere("accounts", map("domain", domain, "address", address));
        }
        mfmAccounts.get(domain).put(address, account);
        return account;
    }

    static List<Map<String, String>> getAccounts(String address, int limit, int page) throws SQLException {
        return select("SELECT * FROM accounts t1 LEFT JOIN tokens t2 ON t1.domain = t2.domain WHERE address = '" + address + "' LIMIT " + page * limit + ", " + limit);
    }


    public static void commitAccounts() throws SQLException {
        if (mfmAccounts != null) {
            int totalInsertCount = 0;
            for (String domain : mfmAccounts.keySet()) {
                int domainInsertCount = 0;
                for (String address : mfmAccounts.get(domain).keySet()) {
                    Map<String, String> account = mfmAccounts.get(domain).get(address);
                    String commit = (String) account.get("commit");
                    account.remove("commit");
                    if ("insert".equals(commit)) {
                        insertRow("accounts", account);
                        domainInsertCount++;
                        totalInsertCount++;
                    } else if ("update".equals(commit)) {
                        updateWhere("accounts", account, map("domain", domain, "address", address));
                    }
                }
                trackAccumulate(domain + "_accounts", domainInsertCount);
            }
            trackAccumulate("accounts_count", totalInsertCount);
            mfmAccounts = null;
        }
    }

    static void commitTokens() throws SQLException {
        commitAccounts();
        commitTrans();
    }


    public static List<Map<String, String>> tokenTrans(String domain, String fromAddress, String toAddress, int page, int size) throws SQLException {
        String sql = "SELECT * FROM trans t1 LEFT JOIN tokens t2 ON t1.domain = t2.domain WHERE 1=1";
        if (fromAddress != null) sql += " AND (`from` = '" + fromAddress + "' OR `to` = '" + fromAddress + "')";
        if (toAddress != null) sql += " AND (`from` = '" + toAddress + "' OR `to` = '" + toAddress + "')";
        if (domain != null) sql += " AND t1.domain = '" + domain + "'";
        sql += " ORDER BY t1.time DESC LIMIT " + page * size + ", " + size;
        return select(sql);
    }


    public static Map<String, String> tokenSecondTran(String domain) throws SQLException {
        return selectRow("SELECT * FROM trans WHERE domain = '" + domain + "' AND `from` = '" + GENESIS_ADDRESS + "' ORDER BY time LIMIT 1, 1");
    }

    public static Map<String, String> tokenTran(String nextHash) throws SQLException {
        return selectRow("SELECT * FROM trans WHERE next_hash = '" + nextHash + "'");
    }


    public static Map<String, String> tokenLastTran(String domain, String fromAddress, String toAddress) throws SQLException {
        List<Map<String, String>> trans = tokenTrans(domain, fromAddress, toAddress, 0, 1);
        return trans.isEmpty() ? null : trans.get(0);
    }

    public static Double tokenBalance(String domain, String address) throws SQLException {
        Map<String, String> account = getAccount(domain, address);
        return account != null ? (Double) account.get("balance") : null;
    }*/
}
