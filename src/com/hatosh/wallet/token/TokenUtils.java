package com.hatosh.wallet.token;

import com.google.gson.reflect.TypeToken;
import com.hatosh.wallet.analytics.AnalyticsUtils;
import com.hatosh.wallet.token.model.Token;
import com.hatosh.wallet.token.model.Tran;

import java.util.*;

import com.hatosh.wallet.token.model.Account;
import com.metabrain.gdb.BigArray;
import com.metabrain.gdb.BigMap;

import static com.hatosh.wallet.Node.broadcast;
import static com.hatosh.wallet.data.Contract.GAS_DOMAIN;

public abstract class TokenUtils extends AnalyticsUtils {
    public static final String GENESIS_ADDRESS = "owner";

    public static final BigArray<Tran> transHistory = new BigArray<>("transHistory", Tran.class);
    public static final BigMap<Tran> transByHash = new BigMap<>("transByHash", Tran.class);
    public static final BigMap<Account> allAccounts = new BigMap<>("allAccounts", Account.class);
    public static final BigMap<Token> tokensByDomain = new BigMap<>("tokensByDomain", Token.class);

    public static final PriorityQueue<Token> topExchange = new PriorityQueue<>(5, Comparator.comparingDouble(t -> t.volume24));
    public static final PriorityQueue<Token> topGainers = new PriorityQueue<>(5, Comparator.comparingDouble(t -> t.price24 - t.price));

    Map<String, Account> accountsNew = new LinkedHashMap<>();
    List<Tran> transactionsNew = new ArrayList<>();
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

    private void setTran(Tran tran) {
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
        if (token != null) token = token.clone();
        return token;
    }

    protected Tran getTran(String nextHash) {
        return transByHash.get(nextHash);
    }

    public Account getAccount(String domain, String address) {
        Account account = accountsNew.get(domain + address);
        if (account == null) {
            account = allAccounts.get(domain + address);
            if (account != null) account = account.clone();
        }
        return account;
    }

    public List<Account> getSubAccounts(String address) {
        List<Account> result = new ArrayList<>();
        Account account = getAccount(GAS_DOMAIN, address);
        while (account != null) {
            result.add(account.clone());
            account = getAccount(account.next_domain, account.address);
        }
        return result;
    }

    public synchronized void commitTrans() {
        if (transactionsNew != null) {
            Collections.reverse(transactionsNew);
            for (Tran tran : transactionsNew) {
                Account account = getAccount(tran.domain, tran.from);
                tran.prev_hash = account.next_hash;
                account.next_hash = tran.next_hash;
                setAccount(account);
                transByHash.put(tran.next_hash, tran);
                transHistory.add(tran);
                broadcast("transactions", gson.fromJson(gson.toJson(tran), Map.class));
                trackAccumulate(tran.domain + "_trans");
            }
            trackAccumulate("trans_count", transactionsNew.size());
            transactionsNew.clear();
        }
    }

    public void commitAccounts() {
        int newAccountsCount = 0;
        for (Account account : accountsNew.values()) {
            if (!allAccounts.containsKey(account.domain + account.address)) {
                newAccountsCount++;
                if (!account.domain.equals(GAS_DOMAIN)) {
                    Account gasAccount = getAccount(GAS_DOMAIN, account.address);
                    if (gasAccount != null) {
                        account.next_domain = gasAccount.next_domain;
                        gasAccount.next_domain = account.domain;
                        allAccounts.put(GAS_DOMAIN + gasAccount.address, gasAccount);
                    }
                }
            }
            allAccounts.put(account.domain + account.address, account);
            trackAccumulate(account.domain + "_accounts");
        }
        trackAccumulate("accounts_count", newAccountsCount);
        accountsNew.clear();
    }

    public void commitTokens() {
        int newTokensCount = 0;
        for (Token token : tokensNew) {
            if (!tokensByDomain.containsKey(token.domain)) newTokensCount++;
            topExchange.add(token);
            if (topExchange.size() > 5) topExchange.poll();
            topGainers.add(token);
            if (topGainers.size() > 5) topGainers.poll();
            tokensByDomain.put(token.domain, token);
        }
        trackAccumulate("token_count", newTokensCount);
        tokensNew.clear();
    }

    protected List<Tran> tokenTrans(String domain, String address, String toAddress) {
        if (domain == null) domain = GAS_DOMAIN;
        List<Tran> result = new ArrayList<>();
        Account account = getAccount(domain, address);
        Tran tran = transByHash.get(account.next_hash);
        for (int i = 0; i < 20; i++) {
            if (tran == null) break;
            if (toAddress == null || (toAddress.equals(tran.to)))
                result.add(tran);
            if (tran.prev_hash == null) break;
            tran = transByHash.get(tran.prev_hash);
        }
        return result;
    }

    public Double tokenBalance(String domain, String address) {
        Account account = getAccount(domain, address);
        return account != null ? account.balance : null;
    }

    public String tokenSend(String scriptPath, String domain, String from_address, String to_address, Double amount, String pass, String delegate) {
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
                owner.delegate = "mfm-token/send";
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

        setTran(new Tran(domain, from_address, to_address, amount, fee, key, next_hash, delegate, time()));
        return next_hash;
    }


    public void commit() {
        super.commit();
        commitTrans();
        commitAccounts();
        commitTokens();
    }
}
