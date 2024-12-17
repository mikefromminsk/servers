package com.mfm_wallet.mfm_token;

import com.mfm_wallet.mfm_analytics.AnalyticsUtils;
import com.mfm_wallet.model.Account;
import com.mfm_wallet.model.Token;
import com.mfm_wallet.model.Transaction;

import java.util.*;

public class TokenUtils extends AnalyticsUtils {
    static final String GENESIS_ADDRESS = "owner";

    public static final Map<String, List<String>> userDomains = new LinkedHashMap<>();
    public static final Map<String, Account> allAccounts = new LinkedHashMap<>();
    public static final Map<String, List<String>> userTransactions = new LinkedHashMap<>();
    public static final Map<String, Transaction> allTransactions = new LinkedHashMap<>();
    public static final Map<String, Token> allTokens = new LinkedHashMap<>();

    Map<String, Account> accounts = new LinkedHashMap<>();
    List<Transaction> transactions = new ArrayList<>();
    List<Token> tokens = new ArrayList<>();


    void setTran(Transaction tran) {
        transactions.add(tran);
    }

    void setToken(Token token) {
        tokens.add(token);
    }

    void setAccount(Account account) {
        accounts.put(account.domain + account.address, account);
    }

    public Token getToken(String domain) {
        Token token = allTokens.get(domain);
        if (token != null)
            token = token.clone();
        return token;
    }

    protected Transaction getTran(String nextHash) {
        return allTransactions.get(nextHash);
    }

    public Account getAccount(String domain, String address) {
        Account account = accounts.get(domain + address);
        if (account == null) {
            account = allAccounts.get(domain + address);
            if (account != null)
                account = account.clone();
        }
        return account;
    }

    public List<Account> getAccounts(String address) {
        List<Account> result = new ArrayList<>();
        if (userDomains.containsKey(address)) {
            for (String domain : userDomains.get(address)) {
                Account account = getAccount(domain, address);
                if (account != null) result.add(account.clone());
            }
        }
        return result;
    }

    public void commitTrans() {
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

    public void commitAccounts() {
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

    public void commitTokens() {
        int newTokensCount = 0;
        for (Token token : tokens) {
            if (!allTokens.containsKey(token.domain))
                newTokensCount++;
            allTokens.put(token.domain, token);
        }
        trackAccumulate("token_count", newTokensCount);
        tokens.clear();
    }

    public void commit() {
        super.commit();
        commitTrans();
        commitAccounts();
        commitTokens();
    }

    protected List<Transaction> tokenTrans(String domain, String fromAddress, String toAddress) {
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

    public Double tokenBalance(String domain, String address) {
        Account account = getAccount(domain, address);
        return account != null ? account.balance : null;
    }

    public String tokenSend(String scriptPath,
                            String domain,
                            String from_address,
                            String to_address,
                            Double amount,
                            String pass,
                            String delegate) {
        if (from_address.equals(to_address) && amount != 0) error("from_address and to_address are the same");
        String key = pass != null ? pass.split(":")[0] : null;
        String next_hash = pass != null ? pass.split(":")[1] : null;
        if (amount != Math.round(amount * 100) / 100.0) error("amount tick is 0.01");
        if (amount < 0) error("amount less than 0");
        if (from_address.equals(GENESIS_ADDRESS)) {
            if (domain.length() < 3 || domain.length() > 16) error("domain length has to be between 3 and 16");
            if (tokenBalance(domain, GENESIS_ADDRESS) == null) {
                Account owner = new Account();
                owner.domain = domain;
                owner.address = GENESIS_ADDRESS;
                owner.prev_key = "";
                owner.next_hash = "";
                owner.balance = amount;
                owner.delegate = "mfm-token/send.php";
                setAccount(owner);
                if (amount > 0) {
                    setToken(new Token(domain, to_address, amount, time()));
                    trackAccumulate("tokens_count");
                }
            }
            if (tokenBalance(domain, to_address) == null) {
                Account to = new Account();
                to.domain = domain;
                to.address = to_address;
                to.prev_key = "";
                to.next_hash = next_hash;
                to.balance = 0.0;
                to.delegate = delegate;
                setAccount(to);
            }
        }

        Account from = getAccount(domain, from_address);
        Account to = getAccount(domain, to_address);
        if (from.balance < amount)
            error(domain.toUpperCase() + " balance is not enough in " + from_address + " wallet. Balance: " + from.balance + " Need: " + amount);
        if (to == null) error(to_address + " receiver doesn't exist");
        if (from.delegate != null) {
            if (!from.delegate.equals(scriptPath))
                error("script " + scriptPath + " cannot use " + from_address + " address. Only " + from.delegate);
        } else {
            if (!from.next_hash.equals(md5(key))) error(domain + " key is not right");
        }

        if (from.delegate != null) {
            from.balance = Math.round((from.balance - amount) * 100) / 100.0;
            setAccount(from);
        } else {
            from.prev_key = key;
            from.next_hash = next_hash;
            from.balance = Math.round((from.balance - amount) * 100) / 100.0;
            setAccount(from);
        }

        double fee = 0;

        to.balance = Math.round((to.balance + amount - fee) * 100) / 100.0;
        setAccount(to);

        setTran(new Transaction(domain,
                from_address,
                to_address,
                amount,
                fee,
                key,
                next_hash,
                delegate,
                time()
        ));
        return next_hash;
    }
}
