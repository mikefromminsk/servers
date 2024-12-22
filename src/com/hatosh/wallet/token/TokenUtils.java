package com.hatosh.wallet.token;

import com.google.gson.reflect.TypeToken;
import com.hatosh.wallet.analytics.AnalyticsUtils;
import com.hatosh.wallet.token.model.Token;
import com.hatosh.wallet.token.model.Transaction;

import java.io.IOException;
import java.util.*;

import com.hatosh.wallet.token.model.Account;

import static com.hatosh.exchange.ExchangeServer.onTranSuccess;
import static com.hatosh.wallet.Node.broadcast;

public abstract class TokenUtils extends AnalyticsUtils {
    public static final String GENESIS_ADDRESS = "owner";

    public static Long transHistorySaveTime = 0L;
    public static final List<Transaction> transHistory = new ArrayList<>();
    public static final Map<String, String> userDomains = new HashMap<>();
    public static final Map<String, Account> allAccounts = new HashMap<>();
    public static final Map<String, Transaction> transByHash = new HashMap<>();
    public static final Map<String, Token> tokensByDomain = new HashMap<>();

    public static final PriorityQueue<Token> topExchange = new PriorityQueue<>(5, Comparator.comparingDouble(t -> t.volume24));
    public static final PriorityQueue<Token> topGainers = new PriorityQueue<>(5, Comparator.comparingDouble(t -> t.price24 - t.price));

    Map<String, Account> accountsNew = new LinkedHashMap<>();
    List<Transaction> transactionsNew = new ArrayList<>();
    List<Token> tokensNew = new ArrayList<>();

    static String tokenKey(String domain, String address, String password, String prevKey) {
        return md5(domain + address + password + (prevKey == null ? "" : prevKey));
    }

    public static String tokenNextHash(String domain, String address, String password, String prevKey) {
        return md5(tokenKey(domain, address, password, prevKey));
    }

    public String tokenPass(String domain, String address, String password) {
        Account account = allAccounts.get(domain + address);
        String key = tokenKey(domain, address, password, account == null ? "" : account.prev_key);
        String nextHash = tokenNextHash(domain, address, password, key);
        return key + ":" + nextHash;
    }

    public String tokenPass(String domain, String address) {
        return tokenPass(domain, address, address);
    }

    public double tokenPrice(String domain) {
        return getCandleLastValue(domain + "_price");
    }

    private void setTran(Transaction tran) {
        transactionsNew.add(tran);
    }

    public void setToken(Token token) {
        tokensNew.add(token);
    }

    private void setAccount(Account account) {
        accountsNew.put(account.domain + account.address, account);
    }

    public Token getToken(String domain) {
        Token token = tokensByDomain.get(domain);
        if (token != null)
            token = token.clone();
        return token;
    }

    protected Transaction getTran(String nextHash) {
        return transByHash.get(nextHash);
    }

    public Account getAccount(String domain, String address) {
        Account account = accountsNew.get(domain + address);
        if (account == null) {
            account = allAccounts.get(domain + address);
            if (account != null)
                account = account.clone();
        }
        return account;
    }

    public List<Account> getSubAccounts(String address) {
        List<Account> result = new ArrayList<>();
        if (userDomains.containsKey(address)) {
            for (String domain : userDomains.getOrDefault(address, "").split(",")) {
                Account account = getAccount(domain, address);
                if (account != null) result.add(account.clone());
            }
        }
        return result;
    }

    public synchronized void commitTrans() {
        if (transactionsNew != null) {
            Collections.reverse(transactionsNew);
            for (Transaction tran : transactionsNew) {
                Account account = getAccount(tran.domain, tran.from);
                tran.prev_hash = account.prev_hash;
                account.prev_hash = tran.next_hash;
                setAccount(account);
                transByHash.put(tran.next_hash, tran);
                transHistory.add(tran);
                if (transHistorySaveTime != 0) {
                    Map<String, String> map = gson.fromJson(gson.toJson(tran), new TypeToken<Map<String, String>>() {
                    }.getType());
                    onTranSuccess(tran);
                    broadcast("transactions", map);
                }
                trackAccumulate(tran.domain + "_trans");
            }
            trackAccumulate("trans_count", transactionsNew.size());
            if (time() > transHistorySaveTime + 60) {
                if (transHistorySaveTime != 0) {
                    new Thread(() -> {
                        try {
                            writeFile("trans.json", transHistory);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                }
                transHistorySaveTime = time();
            }
            transactionsNew.clear();
        }
    }

    public void commitAccounts() {
        int newAccountsCount = 0;
        for (Account account : accountsNew.values()) {
            if (!allAccounts.containsKey(account.domain + account.address))
                newAccountsCount++;
            allAccounts.put(account.domain + account.address, account);

            String userDomain = userDomains.getOrDefault(account.address, "");
            if (!userDomain.contains(account.domain)) {
                userDomains.put(account.address, userDomain + (userDomain.isEmpty() ? "" : ",") + account.domain);
            }
            trackAccumulate(account.domain + "_accounts");
        }
        trackAccumulate("accounts_count", newAccountsCount);
        accountsNew.clear();
    }

    public void commitTokens() {
        int newTokensCount = 0;
        for (Token token : tokensNew) {
            if (!tokensByDomain.containsKey(token.domain))
                newTokensCount++;
            topExchange.add(token);
            if (topExchange.size() > 5) topExchange.poll();
            topGainers.add(token);
            if (topGainers.size() > 5) topGainers.poll();
            tokensByDomain.put(token.domain, token);
        }
        trackAccumulate("token_count", newTokensCount);
        tokensNew.clear();
    }

    protected List<Transaction> tokenTrans(String domain, String address, String toAddress) {
        List<Transaction> result = new ArrayList<>();
        Account account = getAccount(domain, address);
        Transaction tran = transByHash.get(account.prev_hash);
        int i = 0;
        while (tran != null && i < 100) {
            if (toAddress == null || (toAddress.equals(tran.to)))
                result.add(tran);
            tran = transByHash.get(tran.prev_hash);
            i++;
        }
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
            if (scriptPath != null && !from.delegate.equals(scriptPath))
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


    public void commit() {
        super.commit();
        commitTrans();
        commitAccounts();
        commitTokens();
    }
}
