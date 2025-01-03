package org.vavilon.token;

import com.google.gson.reflect.TypeToken;
import org.vavilon.analytics.AnalyticsUtils;
import org.vavilon.token.model.SearchList;
import org.vavilon.token.model.Token;
import org.vavilon.token.model.Tran;

import java.util.*;

import org.vavilon.token.model.Account;
import com.metabrain.gdb.BigArray;
import com.metabrain.gdb.BigMap;

import static org.vavilon.Node.broadcast;
import static org.vavilon.data.Contract.GAS_DOMAIN;

public abstract class TokenUtils extends AnalyticsUtils {
    public static final String GENESIS_ADDRESS = "owner";

    public static final BigArray<Tran> transHistory = new BigArray<>("transHistory", Tran.class);
    public static final BigMap<Tran> transByHash = new BigMap<>("transByHash", Tran.class);
    public static final BigMap<Account> allAccounts = new BigMap<>("allAccounts", Account.class);
    private static final BigMap<Token> tokensByDomain = new BigMap<>("tokensByDomain", Token.class);
    private static final BigMap<SearchList> tokensSearch = new BigMap<>("tokensSearch", SearchList.class);

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

    public Token getToken(String domain) {
        Token token = tokensByDomain.get(domain);
        if (token != null) token = token.clone();
        return token;
    }

    public List<Token> searchTokens(String searchText) {
        List<Token> response = new ArrayList<>();
        SearchList searchList = tokensSearch.get(searchText);
        if (searchList == null)
            return response;
        for (String domain: searchList.list)
            response.add(getToken(domain));
        return response;
    }

    public void setToken(Token token) {
        tokensNew.add(token);
    }

    private void setAccount(Account account) {
        accountsNew.put(account.domain + account.address, account);
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
        Collections.reverse(transactionsNew);
        for (Tran tran : transactionsNew) {
            transByHash.put(tran.next_hash, tran);
            transHistory.add(tran);
            Map<String, String> map = gson.fromJson(gson.toJson(tran), new TypeToken<Map<String, String>>() {
            }.getType());
            broadcast("transactions", map);
            trackAccumulate(tran.domain + "_trans");
        }
        trackAccumulate("trans_count", transactionsNew.size());
        transactionsNew.clear();
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
        for (Token token : tokensNew) {
            topExchange.add(token);
            if (topExchange.size() > 5)
                topExchange.poll();
            topGainers.add(token);
            if (topGainers.size() > 5)
                topGainers.poll();
            tokensByDomain.put(token.domain, token);
            for (int i = 2; i < token.domain.length(); i++) {
                String substr = token.domain.substring(0, i + 1);
                SearchList searchList = tokensSearch.get(substr);
                if (searchList == null)
                    searchList = new SearchList();
                searchList.add(token.domain);
                tokensSearch.put(substr, searchList);
            }
        }
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
        if (amount - round(amount) != 0) error("amount tick is 0.0001");
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

        String prev_hash = from.next_hash;
        if (from.delegate != null) {
            from.balance = round(from.balance - amount);
            setAccount(from);
        } else {
            from.prev_key = key;
            from.next_hash = next_hash;
            from.balance = round(from.balance - amount);
            setAccount(from);
        }

        double fee = 0;

        to.balance = round(to.balance + amount - fee);
        setAccount(to);

        setTran(new Tran(
                domain,
                from_address,
                to_address,
                amount,
                fee,
                key,
                next_hash,
                prev_hash,
                delegate,
                time()));
        return next_hash;
    }


    public void commit() {
        super.commit();
        commitTrans();
        commitAccounts();
        commitTokens();
    }
}
