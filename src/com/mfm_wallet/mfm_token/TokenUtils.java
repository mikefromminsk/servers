package com.mfm_wallet.mfm_token;

import com.mfm_wallet.mfm_analytics.AnalyticsUtils;
import com.mfm_wallet.model.Account;
import com.mfm_wallet.model.Token;
import com.mfm_wallet.model.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.mfm_wallet.Node.broadcast;

public class TokenUtils extends AnalyticsUtils {
    static final String GENESIS_ADDRESS = "owner";

    public static final List<Transaction> transHistory = new ArrayList<>();
    public static Long transHistorySaveTime = 0L;
    public static final Map<String, List<String>> userDomains = new LinkedHashMap<>();
    public static final Map<String, Account> allAccounts = new LinkedHashMap<>();
    public static final Map<String, List<String>> transByUser = new LinkedHashMap<>();
    public static final Map<String, Transaction> transByHash = new LinkedHashMap<>();
    public static final Map<String, Token> tokensByDomain = new LinkedHashMap<>();

    public static final PriorityQueue<Token> topExchange = new PriorityQueue<>(5, Comparator.comparingDouble(t -> t.volume24));
    public static final PriorityQueue<Token> topGainers = new PriorityQueue<>(5, Comparator.comparingDouble(t -> t.price24 - t.price));

    Map<String, Account> accountsNew = new LinkedHashMap<>();
    List<Transaction> transactionsNew = new ArrayList<>();
    List<Token> tokensNew = new ArrayList<>();

    public void setTran(Transaction tran) {
        transactionsNew.add(tran);
    }

    public void setToken(Token token) {
        tokensNew.add(token);
    }

    public void setAccount(Account account) {
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

    public synchronized void commitTrans() {
        if (transactionsNew != null) {
            Collections.reverse(transactionsNew);
            for (Transaction tran : transactionsNew) {
                transHistory.add(tran);
                transByHash.put(tran.next_hash, tran);
                if (!transByUser.containsKey(tran.from))
                    transByUser.put(tran.from, new ArrayList<>());
                transByUser.get(tran.from).add(tran.next_hash);
                if (transHistorySaveTime != 0) {
                    broadcast("transactions", tran);
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

            if (!userDomains.containsKey(account.address))
                userDomains.put(account.address, new ArrayList<>());
            if (!userDomains.get(account.address).contains(account.domain))
                userDomains.get(account.address).add(account.domain);

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

    public void commit() {
        super.commit();
        commitTrans();
        commitAccounts();
        commitTokens();
    }

    protected List<Transaction> tokenTrans(String domain, String fromAddress, String toAddress) {
        List<Transaction> result = new ArrayList<>();
        if (transByUser.containsKey(fromAddress)) {
            for (String nextHash : transByUser.get(fromAddress)) {
                Transaction tran = transByHash.get(nextHash);
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
}
