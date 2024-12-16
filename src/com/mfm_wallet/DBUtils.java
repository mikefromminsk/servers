package com.mfm_wallet;

import com.mfm_wallet.model.Account;
import com.mfm_wallet.model.Token;
import com.mfm_wallet.model.Transaction;
import fi.iki.elonen.NanoHTTPD;

import java.util.*;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

class DBUtils extends BaseUtils {
    Map<String, List<String>> userDomains = new LinkedHashMap<>();
    Map<String, Account> allAccounts = new LinkedHashMap<>();
    Map<String, List<String>> userTransactions = new LinkedHashMap<>();
    Map<String, Transaction> allTransactions = new LinkedHashMap<>();
    Map<String, Token> allTokens = new LinkedHashMap<>();

    Map<String, Account> accounts = new LinkedHashMap<>();
    List<Transaction> transactions = new ArrayList<>();
    List<Token> tokens = new ArrayList<>();

    void saveTran(Transaction tran) {
        transactions.add(tran);
    }

    void saveToken(Token token) {
        tokens.add(token);
    }

    void saveAccount(Account account) {
        accounts.put(account.domain + account.address, account);
    }

    Token getToken(String domain) {
        Token token = allTokens.get(domain);
        if (token != null)
            token = token.clone();
        return token;
    }

    Transaction getTran(String nextHash) {
        return allTransactions.get(nextHash);
    }

    Account getAccount(String domain, String address) {
        Account account = accounts.get(domain + address);
        if (account == null) {
            account = allAccounts.get(domain + address);
            if (account != null)
                account = account.clone();
        }
        return account;
    }

    List<Account> getAccounts(String address) {
        List<Account> result = new ArrayList<>();
        if (userDomains.containsKey(address)) {
            for (String domain : userDomains.get(address)) {
                Account account = getAccount(domain, address);
                if (account != null) result.add(account.clone());
            }
        }
        return result;
    }

    void commitTrans() {
        if (transactions != null) {
            Collections.reverse(transactions);
            for (Transaction tran : transactions) {
                allTransactions.put(tran.next_hash, tran);
                if (!userTransactions.containsKey(tran.from))
                    userTransactions.put(tran.from, new ArrayList<>());
                userTransactions.get(tran.from).add(tran.next_hash);
                broadcast("transactions", gson.toJson(tran));
                trackAccumulate(tran.domain + "_trans");
            }
            trackAccumulate("trans_count", transactions.size());
            transactions.clear();
        }
    }

    void commitAccounts() {
        int newAccountsCount = 0;
        for (Account account : accounts.values()) {
            if (!allAccounts.containsKey(account.domain + account.address))
                newAccountsCount++;
            allAccounts.put(account.domain + account.address, account);

            if (!userDomains.containsKey(account.address))
                userDomains.put(account.address, new ArrayList<>());
            if (!userDomains.get(account.address).contains(account.domain))
                userDomains.get(account.address).add(account.domain);

            trackAccumulate(account.domain + "_accounts");
        }
        trackAccumulate("accounts_count", newAccountsCount);
        accounts.clear();
    }

    void commitTokens() {
        int newTokensCount = 0;
        for (Token token : tokens) {
            if (!allTokens.containsKey(token.domain))
                newTokensCount++;
            allTokens.put(token.domain, token);
        }
        trackAccumulate("token_count", newTokensCount);
        tokens.clear();
    }

    void commitTokenUtils() {
        commitAccounts();
        commitTrans();
        commitTokens();
    }

    List<Transaction> tokenTrans(String domain, String fromAddress, String toAddress) {
        List<Transaction> result = new ArrayList<>();
        if (userTransactions.containsKey(fromAddress)) {
            for (String nextHash : userTransactions.get(fromAddress)) {
                Transaction tran = allTransactions.get(nextHash);
                if (tran != null && tran.domain.equals(domain)) {
                    if (toAddress == null || toAddress.equals(tran.to))
                        result.add(tran);
                }
            }
        }
        result.sort(Comparator.comparingLong(o -> o.time));
        return result;
    }


/*    Map<String, String> tokenSecondTran(String domain) {
        return selectRow("SELECT * FROM trans WHERE domain = '" + domain + "' AND `from` = '" + GENESIS_ADDRESS + "' ORDER BY time LIMIT 1, 1");
    }

    Map<String, String> tokenTran(String nextHash) {
        return selectRow("SELECT * FROM trans WHERE next_hash = '" + nextHash + "'");
    }


    Map<String, String> tokenLastTran(String domain, String fromAddress, String toAddress) {
        List<Transaction> trans = tokenTrans(domain, fromAddress, toAddress, 0, 1);
        return trans.isEmpty() ? null : trans.get(0);
    }*/

    Double tokenBalance(String domain, String address) {
        Account account = getAccount(domain, address);
        return account != null ? account.balance : null;
    }

    static void trackAccumulate(String key) {
        // Implement tracking logic here
    }

    static void trackAccumulate(String key, int value) {
        // Implement tracking logic here
    }

    void commit(Map<String, Object> response) {
        commitTokenUtils();
        response.put("success", "true");
    }

}
